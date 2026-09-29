package com.jesz.createdieselgenerators.blocks.renderer;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.entity.DistillationTankBlockEntity;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class DistillationTankRenderer extends SafeBlockEntityRenderer<DistillationTankBlockEntity> {
   public DistillationTankRenderer(Context context) {
   }

   protected void renderSafe(DistillationTankBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
      if (be.isController()) {
         if (be.isBottom()) {
            this.renderAsBoiler(be, partialTicks, ms, buffer, light, overlay);
         }

         LerpedFloat fluidLevel = be.getFluidLevel();
         if (fluidLevel != null) {
            float capHeight = 0.25F;
            float tankHullWidth = 0.0703125F;
            float minPuddleHeight = 0.0625F;
            float totalHeight = be.getHeight() - 2.0F * capHeight - minPuddleHeight;
            float level = fluidLevel.getValue(partialTicks);
            if (!(level < 1.0F / (512.0F * totalHeight))) {
               float clampedLevel = Mth.clamp(level * totalHeight, 0.0F, totalHeight);
               FluidTank tank = be.tankInventory;
               FluidStack fluidStack = tank.getFluid();
               if (!fluidStack.isEmpty()) {
                  boolean top = fluidStack.getFluid().getFluidType().isLighterThanAir();
                  float xMax = tankHullWidth + be.getWidth() - 2.0F * tankHullWidth;
                  float yMin = totalHeight + capHeight + minPuddleHeight - clampedLevel;
                  float yMax = yMin + clampedLevel;
                  if (top) {
                     yMin += totalHeight - clampedLevel;
                     yMax += totalHeight - clampedLevel;
                  }

                  float zMax = tankHullWidth + be.getWidth() - 2.0F * tankHullWidth;
                  ms.pushPose();
                  ms.translate(0.0F, clampedLevel - totalHeight, 0.0F);
                  FluidRenderer.renderFluidBox(fluidStack, tankHullWidth, yMin, tankHullWidth, xMax, yMax, zMax, buffer, ms, light, false);
                  ms.popPose();
               }
            }
         }
      }
   }

   protected void renderAsBoiler(DistillationTankBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
      BlockState blockState = be.getBlockState();
      VertexConsumer vb = buffer.getBuffer(RenderType.solid());
      ms.pushPose();
      TransformStack msr = TransformStack.cast(ms);
      msr.translate(be.getWidth() / 2.0F, 0.5, be.getWidth() / 2.0F);
      float dialPivot = 0.359375F;
      float progress = Mth.clamp(be.progress, 0.0F, 1.0F);

      for (Direction d : Iterate.horizontalDirections) {
         ms.pushPose();
         CachedBufferer.partial(PartialModels.DISTILLATION_GAUGE, blockState)
            .rotateY(d.toYRot())
            .unCentre()
            .translate((double)(be.getWidth() / 2.0F - 0.375F), 0.0, 0.0)
            .light(light)
            .renderInto(ms, vb);
         CachedBufferer.partial(AllPartialModels.BOILER_GAUGE_DIAL, blockState)
            .rotateY(d.toYRot())
            .unCentre()
            .translate((double)(be.getWidth() / 2.0F - 0.375F), 0.0, 0.0)
            .translate(0.0, (double)dialPivot, (double)dialPivot)
            .rotateX(-90.0F * progress)
            .translate(0.0, (double)(-dialPivot), (double)(-dialPivot))
            .light(light)
            .renderInto(ms, vb);
         ms.popPose();
      }

      ms.popPose();
   }

   public boolean shouldRenderOffScreen(DistillationTankBlockEntity be) {
      return be.isController();
   }
}
