package com.jesz.createdieselgenerators.content.pumpjack;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackBearingBlockEntity;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackCrankBlockEntity;
import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;

public class PumpjackCrankRenderer extends ShaftRenderer<PumpjackCrankBlockEntity> {
   public PumpjackCrankRenderer(Context context) {
      super(context);
   }

   protected void renderSafe(PumpjackCrankBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
      if (!Backend.canUseInstancing(be.getLevel())) {
         BlockState blockState = be.getBlockState();
         BlockPos pos = be.getBlockPos();
         float angle = AngleHelper.angleLerp(partialTicks, be.prevAngle, be.angle);
         boolean isXAxis = ((Direction)blockState.getValue(HorizontalKineticBlock.HORIZONTAL_FACING)).getAxis() == Axis.X;
         double v = ((isXAxis ? angle : -angle) + 90.0F) / 180.0F * Math.PI;
         double sin = Math.sin(v) * (be.crankSize.getValue() == 0 ? 0.8125 : 1.125);
         double cos = Math.cos(v) * (be.crankSize.getValue() == 0 ? 0.8125 : 1.125);
         SuperByteBuffer crank = CachedBufferer.partial(
            be.crankSize.getValue() == 0 ? CDGPartialModels.PUMPJACK_CRANK_SMALL : CDGPartialModels.PUMPJACK_CRANK_LARGE, blockState
         );
         SuperByteBuffer rod = CachedBufferer.partial(
            be.crankSize.getValue() == 0 ? CDGPartialModels.PUMPJACK_CRANK_ROD_SMALL : CDGPartialModels.PUMPJACK_CRANK_ROD_LARGE, blockState
         );
         if (be.bearingPos == null) {
            if (isXAxis) {
               crank.translate(0.5, 1.25, 0.0).rotateZ(angle);
            } else {
               crank.translate(0.0, 1.25, 0.5).rotateY(90.0).rotateZ(angle);
            }

            double dstY = -1000.0 - sin - 1.25 - pos.getY();
            double dstX = pos.getX() - cos - 0.5 - pos.getX();
            double dstZ = pos.getZ() - cos - 0.5 - pos.getZ();
            if (isXAxis) {
               rod.translate(0.5, 1.25, 0.0).translate(cos, sin, 0.0).rotateZ(Math.atan2(dstY, dstX) * 180.0 / Math.PI - 90.0);
            } else {
               rod.translate(0.0, 1.25, 0.5).translate(0.0, sin, cos).rotateY(90.0).rotateZ(Math.atan2(dstZ, dstY) * 180.0 / Math.PI);
            }

            rod.renderInto(ms, buffer.getBuffer(RenderType.solid()));
            crank.renderInto(ms, buffer.getBuffer(RenderType.solid()));
            super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
         } else {
            PumpjackBearingBlockEntity bearing = be.bearing.get();
            float interpolatedAngle = 0.0F;
            if (bearing != null) {
               interpolatedAngle = bearing.getInterpolatedAngle(partialTicks);
            }

            if (!isXAxis) {
               interpolatedAngle *= -1.0F;
            }

            Vec2 crankBearingLocation = new Vec2(
               (float)(
                     be.crankBearingLocation.x * Math.cos(interpolatedAngle / 180.0F * Math.PI)
                        - be.crankBearingLocation.y * Math.sin(interpolatedAngle / 180.0F * Math.PI)
                  )
                  + 0.5F,
               (float)(
                     be.crankBearingLocation.x * Math.sin(interpolatedAngle / 180.0F * Math.PI)
                        + be.crankBearingLocation.y * Math.cos(interpolatedAngle / 180.0F * Math.PI)
                  )
                  + 0.5F
            );
            if (isXAxis) {
               crankBearingLocation = crankBearingLocation.add(new Vec2(be.bearingPos.getX(), be.bearingPos.getY()));
            } else {
               crankBearingLocation = crankBearingLocation.add(new Vec2(be.bearingPos.getZ(), be.bearingPos.getY()));
            }

            if (isXAxis) {
               crank.translate(0.5, 1.25, 0.0).rotateZ(angle);
            } else {
               crank.translate(0.0, 1.25, 0.5).rotateY(90.0).rotateZ(angle);
            }

            double dstY = crankBearingLocation.y - sin - 1.25 - pos.getY();
            double dstX = crankBearingLocation.x - cos - 0.5 - pos.getX();
            double dstZ = crankBearingLocation.x - cos - 0.5 - pos.getZ();
            if (isXAxis) {
               rod.translate(0.5, 1.25, 0.0).translate(cos, sin, 0.0).rotateZ(Math.atan2(dstY, dstX) * 180.0 / Math.PI - 90.0);
            } else {
               rod.translate(0.0, 1.25, 0.5).translate(0.0, sin, cos).rotateY(90.0).rotateZ(Math.atan2(dstZ, dstY) * 180.0 / Math.PI);
            }

            rod.light(light);
            crank.light(light);
            rod.renderInto(ms, buffer.getBuffer(RenderType.solid()));
            crank.renderInto(ms, buffer.getBuffer(RenderType.solid()));
            super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
         }
      }
   }
}
