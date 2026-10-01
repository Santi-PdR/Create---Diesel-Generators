#!/usr/bin/env python3
"""Validate the resource overlay, model references and 1.20.1 recipe schema.

Run without dependencies, or pass --jar to also check the packaged mod. Actual
registry, blockstate and recipe loading is covered separately by Forge tests.
"""

import argparse
import json
import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MOD = "createdieselgenerators"
# Texture variables used by the vanilla 1.20.1 templates. Models defining
# their own elements do not inherit the parent's faces.
VANILLA_TEXTURES = {
    "block/cube": {"down", "up", "north", "east", "south", "west"},
    "block/cube_all": {"all"},
    "block/cube_column": {"end", "side"},
    "block/cube_column_horizontal": {"end", "side"},
    "block/slab": {"bottom", "top", "side"},
    "block/slab_top": {"bottom", "top", "side"},
    "block/stairs": {"bottom", "top", "side"},
    "block/inner_stairs": {"bottom", "top", "side"},
    "block/outer_stairs": {"bottom", "top", "side"},
    "item/generated": {"layer0"},
    "item/handheld": {"layer0"},
}


def strict_object(pairs):
    result = {}
    for key, value in pairs:
        if key in result:
            raise ValueError(f"duplicate JSON key: {key}")
        result[key] = value
    return result


def resource_path(identifier, folder, suffix):
    namespace, path = identifier.split(":", 1) if ":" in identifier else ("minecraft", identifier)
    return f"assets/{namespace}/{folder}/{path}{suffix}"


def walk(value):
    if isinstance(value, dict):
        yield value
        for child in value.values():
            yield from walk(child)
    elif isinstance(value, list):
        for child in value:
            yield from walk(child)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jar", type=Path, help="Check the built jar against the source overlay too")
    args = parser.parse_args()
    errors = []
    sources = {}
    effective = {}
    for folder in ("src/main/resources", "src/generated/resources"):
        files = {p.relative_to(ROOT / folder).as_posix(): p.read_bytes()
                 for p in (ROOT / folder).rglob("*") if p.is_file() and ".cache" not in p.parts}
        sources[folder] = files
        for path, content in files.items():
            effective.setdefault(path, content)
            if path.endswith((".json", ".mcmeta")):
                try:
                    json.loads(content, object_pairs_hook=strict_object)
                except (ValueError, UnicodeError) as exc:
                    errors.append(f"{folder}/{path}: {exc}")

    main_resources, generated = sources.values()
    for path in main_resources.keys() & generated.keys():
        if path.endswith(".json") and ("/tags/" in path or "/lang/" in path):
            main_json = json.loads(main_resources[path])
            generated_json = json.loads(generated[path])
            if "/tags/" in path:
                missing = [value for value in generated_json["values"] if value not in main_json["values"]]
            else:
                missing = sorted(generated_json.keys() - main_json.keys())
            if missing:
                errors.append(f"{path}: main override masks generated entries: {missing}")

    all_resources = dict(effective)
    with zipfile.ZipFile(ROOT / "libs/create-0.5.1.j.jar") as archive:
        for entry in archive.namelist():
            if entry.startswith(("assets/", "data/")) and not entry.endswith("/"):
                all_resources.setdefault(entry, archive.read(entry))
    models = {path: json.loads(content) for path, content in all_resources.items()
              if "/models/" in path and path.endswith(".json")}

    def model_data(path, ancestors=()):
        if path in ancestors:
            raise ValueError("cyclic model parent: " + " -> ".join((*ancestors, path)))
        model = models[path]
        textures, elements, template = {}, None, None
        if "parent" in model:
            parent = resource_path(model["parent"], "models", ".json")
            if parent in models:
                textures, elements, template = model_data(parent, (*ancestors, path))
            elif parent.startswith("assets/minecraft/models/"):
                template = parent[len("assets/minecraft/models/"):-5]
            else:
                raise ValueError(f"missing model parent {model['parent']}")
        textures = {**textures, **model.get("textures", {})}
        elements = model.get("elements", elements)
        return textures, elements, template

    checked_models = 0
    for path in sorted(effective):
        if path not in models:
            continue
        checked_models += 1
        try:
            textures, elements, template = model_data(path)
            used = {textures.get("particle", "#particle")} if "particle" in textures else set()
            if elements is not None:
                used.update(face["texture"] for element in elements for face in element.get("faces", {}).values())
            else:
                used.update("#" + name for name in VANILLA_TEXTURES.get(template, ()))
            for texture in used:
                visited = set()
                while texture.startswith("#"):
                    key = texture[1:]
                    if key in visited or key not in textures:
                        raise ValueError(f"unresolved texture variable {texture}")
                    visited.add(key)
                    texture = textures[key]
                texture_path = resource_path(texture, "textures", ".png")
                if texture_path.startswith((f"assets/{MOD}/", "assets/create/")):
                    if texture_path not in all_resources:
                        raise ValueError(f"missing texture {texture}")
                    sprite = texture.split(":", 1)[-1]
                    if not sprite.startswith(("block/", "item/")):
                        raise ValueError(f"texture is not stitched into the 1.20.1 block atlas: {texture}")
            # Also check concrete texture paths not used by this model's faces.
            for texture in textures.values():
                if texture.startswith("#"):
                    continue
                texture_path = resource_path(texture, "textures", ".png")
                if texture_path.startswith((f"assets/{MOD}/", "assets/create/")) and texture_path not in all_resources:
                    raise ValueError(f"missing texture {texture}")
        except ValueError as exc:
            errors.append(f"{path}: {exc}")

    recipes = 0
    for path, content in effective.items():
        if not path.endswith(".json"):
            continue
        data = json.loads(content)
        if "/blockstates/" in path:
            for node in walk(data):
                if "model" in node:
                    target = resource_path(node["model"], "models", ".json")
                    if target.startswith((f"assets/{MOD}/", "assets/create/")) and target not in models:
                        errors.append(f"{path}: missing blockstate model {node['model']}")
        if "/recipes/" in path:
            recipes += 1
            recipe_type = data.get("type", "")
            if not recipe_type:
                errors.append(f"{path}: missing recipe type")
            for ingredient in data.get("ingredients", []):
                if (recipe_type.startswith(("create:", MOD + ":")) and isinstance(ingredient, dict)
                        and "count" in ingredient and ("item" in ingredient or "tag" in ingredient)):
                    errors.append(f"{path}: counted ingredient is ignored by Create 0.5.1.j; repeat the ingredient instead")
            for node in walk(data):
                if isinstance(node.get("item"), str):
                    item = node["item"]
                    target = resource_path(item, "models", ".json").replace("/models/", "/models/item/", 1)
                    if item.startswith(("create:", MOD + ":")) and target not in models:
                        errors.append(f"{path}: item not present in target: {item}")
                if "components" in node or "minecraft:set_components" in node.values():
                    errors.append(f"{path}: post-1.20.1 item component schema")
        if path.endswith("/sounds.json"):
            for sound_event in data.values():
                for sound in sound_event["sounds"]:
                    sound = sound["name"] if isinstance(sound, dict) else sound
                    namespace, sound = sound.split(":", 1) if ":" in sound else (MOD, sound)
                    if namespace == MOD and f"assets/{MOD}/sounds/{sound}.ogg" not in effective:
                        errors.append(f"{path}: missing sound {sound}")

    if args.jar:
        with zipfile.ZipFile(args.jar) as archive:
            names = archive.namelist()
            if len(names) != len(set(names)):
                errors.append(f"{args.jar}: duplicate jar entries")
            for path, expected in effective.items():
                # Gradle builds the manifest (including timestamp and mixin
                # metadata); validate its contract below, not byte equality.
                if path == "META-INF/MANIFEST.MF":
                    continue
                if path not in names or archive.read(path) != expected:
                    errors.append(f"{args.jar}: packaged resource differs from explicit source overlay: {path}")
            manifest = archive.read("META-INF/MANIFEST.MF").decode("utf-8").replace("\r\n ", "").splitlines()
            attributes = dict(line.split(": ", 1) for line in manifest if ": " in line)
            if attributes.get("Implementation-Version") != "1.20.1-1.3.12-create-0.5.1j":
                errors.append(f"{args.jar}: incorrect implementation version in manifest")
            if MOD + ".mixins.json" not in attributes.get("MixinConfigs", "").split(","):
                errors.append(f"{args.jar}: missing mixin manifest entry")
            for path in (MOD + ".mixins.json", MOD + ".refmap.json", "META-INF/mods.toml"):
                if path not in names:
                    errors.append(f"{args.jar}: missing {path}")
            refmap = json.loads(archive.read(MOD + ".refmap.json")) if MOD + ".refmap.json" in names else {}
            if not refmap.get("mappings"):
                errors.append(f"{args.jar}: empty mixin refmap")

    if errors:
        print("\n".join(dict.fromkeys(errors)), file=sys.stderr)
        return 1
    print(f"Verified {len(effective)} resources, {checked_models} models and {recipes} recipes for the 1.20.1 overlay.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
