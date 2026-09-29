package com.jesz.createdieselgenerators.blocks.renderer;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.BasinLidBlock;
import com.jesz.createdieselgenerators.blocks.entity.BasinLidBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;

public class BasinLidRenderer extends SafeBlockEntityRenderer<BasinLidBlockEntity> {
   public BasinLidRenderer(Context context) {
   }

   protected void renderSafe(BasinLidBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
      if ((Boolean)be.getBlockState().getValue(BasinLidBlock.ON_A_BASIN)) {
         Direction facing = (Direction)be.getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
         CachedBufferer.partial(PartialModels.SMALL_GAUGE_DIAL, be.getBlockState())
            .centre()
            .rotateY(-facing.toYRot() + 180.0F)
            .translate(0.5625, -0.375, 0.5)
            .unCentre()
            .rotateZ(be.progress * -90.0F + 90.0F)
            .renderInto(ms, bufferSource.getBuffer(RenderType.solid()));
      }
   }
}
