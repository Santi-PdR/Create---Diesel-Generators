package com.jesz.createdieselgenerators.blocks.renderer;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackHoleBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;

public class PumpjackHoleRenderer extends SafeBlockEntityRenderer<PumpjackHoleBlockEntity> {
   public PumpjackHoleRenderer(Context context) {
   }

   @Override
   public void render(PumpjackHoleBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
      renderSafe(be, partialTicks, ms, buffer, light, overlay);
   }

   protected void renderSafe(PumpjackHoleBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
      CachedBufferer.partial(PartialModels.PUMPJACK_ROPE, be.getBlockState())
         .translate(0.5, 0.0, 0.5)
         .scale(1.0F, (float)be.pipeLength, 1.0F)
         .renderInto(ms, buffer.getBuffer(RenderType.solid()));
   }
}
