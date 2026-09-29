package com.jesz.createdieselgenerators.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class ChemicalSprayerProjectileRenderer extends EntityRenderer<ChemicalSprayerProjectileEntity> {
   protected ChemicalSprayerProjectileRenderer(Context context) {
      super(context);
   }

   public void render(ChemicalSprayerProjectileEntity entity, float yaw, float pt, PoseStack ms, MultiBufferSource buffer, int light) {
      ms.pushPose();
      ms.translate(0.0, entity.getBoundingBox().getYsize() / 2.0 - 0.125, 0.0);
      ms.popPose();
   }

   public ResourceLocation getTextureLocation(ChemicalSprayerProjectileEntity entity) {
      return null;
   }
}
