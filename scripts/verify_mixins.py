#!/usr/bin/env python3
"""Verify Create mixin targets against the shipped 0.5.1.j bytecode.

Vanilla/Forge targets are checked by the Mixin annotation processor, packaged
refmap verification and the server/client runtime tests, not guessed here.
"""

import io
import json
import struct
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MC = "net/minecraft/"
CREATE = "com/simibubi/create/"


def members(data):
    stream = io.BytesIO(data)

    def read(fmt):
        return struct.unpack(">" + fmt, stream.read(struct.calcsize(">" + fmt)))

    def attributes():
        for _ in range(read("H")[0]):
            _, size = read("HI")
            stream.seek(size, 1)

    assert read("I")[0] == 0xCAFEBABE
    stream.read(4)
    pool = [None] * read("H")[0]
    i = 1
    sizes = {3: 4, 4: 4, 5: 8, 6: 8, 7: 2, 8: 2, 9: 4, 10: 4,
             11: 4, 12: 4, 15: 3, 16: 2, 17: 4, 18: 4, 19: 2, 20: 2}
    while i < len(pool):
        kind = read("B")[0]
        if kind == 1:
            pool[i] = stream.read(read("H")[0]).decode("utf-8", errors="replace")
        else:
            stream.seek(sizes[kind], 1)
        i += 2 if kind in (5, 6) else 1
    stream.read(6)
    stream.read(read("H")[0] * 2)
    result = []
    for category in ("field", "method"):
        for _ in range(read("H")[0]):
            access, name, descriptor = read("HHH")
            result.append((category, pool[name], pool[descriptor], bool(access & 0x0008)))
            attributes()
    return result


def main():
    # Exact JVM descriptors: changing a callback parameter is not compatible
    # merely because a method with the same name still exists.
    targets = {
        "BasinBlockEntityAccessor": ("content/processing/basin/BasinBlockEntity", [
            ("field", "itemCapability", "Lnet/minecraftforge/common/util/LazyOptional;", False)]),
        "BasinRecipeMixin": ("content/processing/basin/BasinRecipe", [
            ("method", "apply", f"(L{CREATE}content/processing/basin/BasinBlockEntity;L{MC}world/item/crafting/Recipe;Z)Z", True)]),
        "BasinRendererMixin": ("content/processing/basin/BasinRenderer", [
            ("method", "renderItem", f"(Lcom/mojang/blaze3d/vertex/PoseStack;L{MC}client/renderer/MultiBufferSource;IIL{MC}world/item/ItemStack;)V", False),
            ("method", "renderSafe", f"(L{CREATE}content/processing/basin/BasinBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;L{MC}client/renderer/MultiBufferSource;II)V", False)]),
        "ContraptionMixin": ("content/contraptions/Contraption", [
            ("method", "getBlockEntityNBT", f"(L{MC}world/level/Level;L{MC}core/BlockPos;)L{MC}nbt/CompoundTag;", False),
            ("method", "toLocalPos", f"(L{MC}core/BlockPos;)L{MC}core/BlockPos;", False)]),
        "CopycatBlockMixin": ("content/decoration/copycat/CopycatBlock", [
            # Overrides the mapped Minecraft Block.use method, unlike the
            # Create-only methods above. Keep remapping enabled for this one.
            ("method", "m_6227_", f"(L{MC}world/level/block/state/BlockState;L{MC}world/level/Level;L{MC}core/BlockPos;L{MC}world/entity/player/Player;L{MC}world/InteractionHand;L{MC}world/phys/BlockHitResult;)L{MC}world/InteractionResult;", False)]),
        "MechanicalPressBlockEntityMixin": ("content/kinetics/press/MechanicalPressBlockEntity", [
            ("method", "matchStaticFilters", f"(L{MC}world/item/crafting/Recipe;)Z", False)]),
        "MountedFluidStorageMixin": ("content/contraptions/MountedFluidStorage", [
            ("method", "canUseAsStorage", f"(L{MC}world/level/block/entity/BlockEntity;)Z", True),
            ("method", "createMountedTank", f"(L{MC}world/level/block/entity/BlockEntity;)L{CREATE}foundation/fluid/SmartFluidTank;", False),
            ("method", "onFluidStackChanged", "(Lnet/minecraftforge/fluids/FluidStack;)V", False)]),
        "ShaftBlockMixin": ("content/kinetics/simpleRelays/ShaftBlock", [
            ("method", "pickCorrectShaftType", f"(L{MC}world/level/block/state/BlockState;L{MC}world/level/Level;L{MC}core/BlockPos;)L{MC}world/level/block/state/BlockState;", True)]),
    }
    config = json.loads((ROOT / "src/main/resources/createdieselgenerators.mixins.json").read_text())
    configured = set(config["mixins"] + config["client"])
    errors = []
    checked = 0
    with zipfile.ZipFile(ROOT / "libs/create-0.5.1.j.jar") as archive:
        for mixin, (target, signatures) in targets.items():
            if mixin not in configured:
                errors.append(f"Mixin is no longer configured: {mixin}")
            available = members(archive.read(CREATE + target + ".class"))
            for signature in signatures:
                if signature not in available:
                    errors.append(f"{mixin}: target signature absent from Create 0.5.1.j: {signature}")
                checked += 1
    for mixin in configured:
        if not (ROOT / f"src/main/java/com/jesz/createdieselgenerators/mixins/{mixin}.java").is_file():
            errors.append(f"Missing configured mixin source: {mixin}")
    if errors:
        print("\n".join(errors), file=sys.stderr)
        return 1
    print(f"Verified {checked} exact Create mixin signatures and all {len(configured)} configured mixin sources.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
