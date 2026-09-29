package com.jesz.createdieselgenerators.other;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock;
import com.simibubi.create.AllTags;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.core.Holder.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

public class FuelTypeManager {
   public static Map<Fluid, CDGFuelType> fuelTypes = new HashMap<>();
   static Map<String, CDGFuelType> fuelTags = new HashMap<>();

   public static void tryPopulateTags() {
      if (!fuelTags.isEmpty()) {
         if (!ForgeRegistries.FLUIDS.tags().stream().toList().isEmpty()) {
            for (Entry<String, CDGFuelType> entry : Map.copyOf(fuelTags).entrySet()) {
               ForgeRegistries.FLUIDS
                  .tags()
                  .getTag(AllTags.optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation(entry.getKey())))
                  .stream()
                  .distinct()
                  .toList()
                  .forEach(fluid -> {
                     fuelTypes.put(fluid, entry.getValue());
                     fuelTags.remove(entry.getKey(), entry.getValue());
                  });
            }
         }
      }
   }

   public static CDGFuelType getType(Fluid fluid) {
      return fuelTypes.get(fluid);
   }

   public static float getGeneratedSpeed(BlockEntity be, Fluid fluid) {
      tryPopulateTags();
      return fuelTypes.containsKey(fluid) ? fuelTypes.get(fluid).getGenerated(be).getFirst() : 0.0F;
   }

   public static float getGeneratedStress(BlockEntity be, Fluid fluid) {
      tryPopulateTags();
      return fuelTypes.containsKey(fluid) ? fuelTypes.get(fluid).getGenerated(be).getSecond() : 0.0F;
   }

   public static float getGeneratedSpeed(DieselGeneratorBlock.EngineTypes engine, Fluid fluid) {
      tryPopulateTags();
      if (fuelTypes.containsKey(fluid)) {
         if (engine == DieselGeneratorBlock.EngineTypes.NORMAL) {
            return fuelTypes.get(fluid).getGeneratedNormal().getFirst();
         }

         if (engine == DieselGeneratorBlock.EngineTypes.MODULAR) {
            return fuelTypes.get(fluid).getGeneratedModular().getFirst();
         }

         if (engine == DieselGeneratorBlock.EngineTypes.HUGE) {
            return fuelTypes.get(fluid).getGeneratedHuge().getFirst();
         }
      }

      return 0.0F;
   }

   public static float getGeneratedStress(DieselGeneratorBlock.EngineTypes engine, Fluid fluid) {
      tryPopulateTags();
      if (fuelTypes.containsKey(fluid)) {
         if (engine == DieselGeneratorBlock.EngineTypes.NORMAL) {
            return fuelTypes.get(fluid).getGeneratedNormal().getSecond();
         }

         if (engine == DieselGeneratorBlock.EngineTypes.MODULAR) {
            return fuelTypes.get(fluid).getGeneratedModular().getSecond();
         }

         if (engine == DieselGeneratorBlock.EngineTypes.HUGE) {
            return fuelTypes.get(fluid).getGeneratedHuge().getSecond();
         }
      }

      return 0.0F;
   }

   public static int getBurnRate(DieselGeneratorBlock.EngineTypes engine, Fluid fluid) {
      tryPopulateTags();
      if (fuelTypes.containsKey(fluid)) {
         if (engine == DieselGeneratorBlock.EngineTypes.NORMAL) {
            return fuelTypes.get(fluid).getBurnNormal();
         }

         if (engine == DieselGeneratorBlock.EngineTypes.MODULAR) {
            return fuelTypes.get(fluid).getBurnModular();
         }

         if (engine == DieselGeneratorBlock.EngineTypes.HUGE) {
            return fuelTypes.get(fluid).getBurnHuge();
         }
      }

      return 0;
   }

   public static float getGeneratedSpeed(Fluid fluid) {
      tryPopulateTags();
      return fuelTypes.containsKey(fluid) ? fuelTypes.get(fluid).getGeneratedNormal().getFirst() : 0.0F;
   }

   public static float getGeneratedStress(Fluid fluid) {
      tryPopulateTags();
      return fuelTypes.containsKey(fluid) ? fuelTypes.get(fluid).getGeneratedNormal().getSecond() : 0.0F;
   }

   public static int getBurnRate(BlockEntity be, Fluid fluid) {
      tryPopulateTags();
      return fuelTypes.containsKey(fluid) ? fuelTypes.get(fluid).getBurn(be) : 0;
   }

   public static int getBurnRate(Fluid fluid) {
      tryPopulateTags();
      return fuelTypes.containsKey(fluid) ? fuelTypes.get(fluid).getBurnNormal() : 0;
   }

   public static int getSoundSpeed(Fluid fluid) {
      tryPopulateTags();
      return fuelTypes.containsKey(fluid) ? fuelTypes.get(fluid).getSoundSpeed() : 1;
   }

   public static class ReloadListener extends SimpleJsonResourceReloadListener {
      private static final Gson GSON = new Gson();
      public static final FuelTypeManager.ReloadListener INSTANCE = new FuelTypeManager.ReloadListener();

      public ReloadListener() {
         super(GSON, "diesel_engine_fuel_types");
      }

      protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
         FuelTypeManager.fuelTypes.clear();

         for (Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            JsonElement element = entry.getValue();
            if (!element.isJsonObject()) {
               return;
            }

            JsonObject normalEngineObject = element.getAsJsonObject().get("normal").getAsJsonObject();
            JsonObject modularEngineObject = element.getAsJsonObject().has("modular")
               ? element.getAsJsonObject().get("modular").getAsJsonObject()
               : normalEngineObject;
            JsonObject hugeEngineObject = element.getAsJsonObject().has("huge") ? element.getAsJsonObject().get("huge").getAsJsonObject() : normalEngineObject;
            String fluidId = element.getAsJsonObject().get("fluid").getAsString();
            if (fluidId.startsWith("#")) {
               FuelTypeManager.fuelTags
                  .put(
                     fluidId.substring(1),
                     new CDGFuelType(
                        normalEngineObject.get("speed").getAsFloat(),
                        normalEngineObject.get("strength").getAsFloat(),
                        normalEngineObject.get("burn_rate").getAsInt(),
                        modularEngineObject.get("speed").getAsFloat(),
                        modularEngineObject.get("strength").getAsFloat(),
                        modularEngineObject.get("burn_rate").getAsInt(),
                        hugeEngineObject.get("speed").getAsFloat(),
                        hugeEngineObject.get("strength").getAsFloat(),
                        hugeEngineObject.get("burn_rate").getAsInt(),
                        element.getAsJsonObject().get("sound_speed").getAsInt()
                     )
                  );
               FuelTypeManager.tryPopulateTags();
            } else {
               Optional<Reference<Fluid>> fluid = ForgeRegistries.FLUIDS.getDelegate(new ResourceLocation(fluidId));
               if (fluid.isEmpty()) {
                  return;
               }

               FuelTypeManager.fuelTypes
                  .put(
                     (Fluid)fluid.get().get(),
                     new CDGFuelType(
                        normalEngineObject.get("speed").getAsFloat(),
                        normalEngineObject.get("strength").getAsFloat(),
                        normalEngineObject.get("burn_rate").getAsInt(),
                        modularEngineObject.get("speed").getAsFloat(),
                        modularEngineObject.get("strength").getAsFloat(),
                        modularEngineObject.get("burn_rate").getAsInt(),
                        hugeEngineObject.get("speed").getAsFloat(),
                        hugeEngineObject.get("strength").getAsFloat(),
                        hugeEngineObject.get("burn_rate").getAsInt(),
                        element.getAsJsonObject().get("sound_speed").getAsInt()
                     )
                  );
            }
         }
      }
   }
}
