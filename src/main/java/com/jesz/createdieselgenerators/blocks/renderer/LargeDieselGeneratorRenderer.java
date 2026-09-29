package com.jesz.createdieselgenerators.blocks.renderer;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.LargeDieselGeneratorBlock;
import com.jesz.createdieselgenerators.blocks.entity.LargeDieselGeneratorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;

public class LargeDieselGeneratorRenderer extends ShaftRenderer<LargeDieselGeneratorBlockEntity> {
   public LargeDieselGeneratorRenderer(Context context) {
      super(context);
   }

   protected void renderSafe(LargeDieselGeneratorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
      int angle = (int)(
            Math.abs(KineticBlockEntityRenderer.getAngleForTe(be, be.getBlockPos(), KineticBlockEntityRenderer.getRotationAxisOf(be)) * 180.0F / Math.PI)
               * 3.0
               % 360.0
         )
         / 36;
      CachedBufferer.partial(
            angle == 10
               ? PartialModels.MODULAR_ENGINE_PISTONS_0
               : (
                  angle == 9
                     ? PartialModels.MODULAR_ENGINE_PISTONS_1
                     : (
                        angle == 8
                           ? PartialModels.MODULAR_ENGINE_PISTONS_2
                           : (
                              angle == 7
                                 ? PartialModels.MODULAR_ENGINE_PISTONS_3
                                 : (
                                    angle == 6
                                       ? PartialModels.MODULAR_ENGINE_PISTONS_4
                                       : (
                                          angle == 5
                                             ? PartialModels.MODULAR_ENGINE_PISTONS_4
                                             : (
                                                angle == 4
                                                   ? PartialModels.MODULAR_ENGINE_PISTONS_3
                                                   : (
                                                      angle == 3
                                                         ? PartialModels.MODULAR_ENGINE_PISTONS_2
                                                         : (angle == 2 ? PartialModels.MODULAR_ENGINE_PISTONS_1 : PartialModels.MODULAR_ENGINE_PISTONS_0)
                                                   )
                                             )
                                       )
                                 )
                           )
                     )
               ),
            be.getBlockState()
         )
         .centre()
         .rotateY(((Direction)be.getBlockState().getValue(LargeDieselGeneratorBlock.FACING)).toYRot())
         .unCentre()
         .light(light)
         .renderInto(ms, buffer.getBuffer(RenderType.solid()));
      super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
   }
}
