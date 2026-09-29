package com.jesz.createdieselgenerators.other;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.ModelEvent.RegisterAdditional;

public class CDGPartialModel {
   private static final List<CDGPartialModel> ALL = new ArrayList<>();
   protected final ResourceLocation modelLocation;
   protected BakedModel bakedModel;

   public CDGPartialModel(ResourceLocation modelLocation) {
      this.modelLocation = modelLocation;
      ALL.add(this);
   }

   public static void onModelRegistry(RegisterAdditional event) {
      for (CDGPartialModel partial : ALL) {
         event.register(partial.getLocation());
      }
   }

   public static void onModelBake(BakingCompleted event) {
      Map<ResourceLocation, BakedModel> models = event.getModels();

      for (CDGPartialModel partial : ALL) {
         partial.set(models.get(partial.getLocation()));
      }
   }

   protected void set(BakedModel bakedModel) {
      this.bakedModel = bakedModel;
   }

   public ResourceLocation getLocation() {
      return this.modelLocation;
   }

   public BakedModel get() {
      return this.bakedModel;
   }
}
