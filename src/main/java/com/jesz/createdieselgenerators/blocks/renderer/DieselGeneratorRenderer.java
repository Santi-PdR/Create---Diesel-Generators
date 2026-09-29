package com.jesz.createdieselgenerators.blocks.renderer;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock;
import com.jesz.createdieselgenerators.blocks.entity.DieselGeneratorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;

public class DieselGeneratorRenderer extends ShaftRenderer<DieselGeneratorBlockEntity> {
   public DieselGeneratorRenderer(Context context) {
      super(context);
   }

   protected void renderSafe(DieselGeneratorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
      int angle = (int)(
            Math.abs(KineticBlockEntityRenderer.getAngleForTe(be, be.getBlockPos(), KineticBlockEntityRenderer.getRotationAxisOf(be)) * 180.0F / Math.PI)
               * 3.0
               % 360.0
         )
         / 36;
      if (!(Boolean)be.getBlockState().getValue(DieselGeneratorBlock.TURBOCHARGED)) {
         if (((Direction)be.getBlockState().getValue(DieselGeneratorBlock.FACING)).getAxis().isHorizontal()) {
            CachedBufferer.partial(
                  angle == 10
                     ? PartialModels.ENGINE_PISTONS_0
                     : (
                        angle == 9
                           ? PartialModels.ENGINE_PISTONS_1
                           : (
                              angle == 8
                                 ? PartialModels.ENGINE_PISTONS_2
                                 : (
                                    angle == 7
                                       ? PartialModels.ENGINE_PISTONS_3
                                       : (
                                          angle == 6
                                             ? PartialModels.ENGINE_PISTONS_4
                                             : (
                                                angle == 5
                                                   ? PartialModels.ENGINE_PISTONS_4
                                                   : (
                                                      angle == 4
                                                         ? PartialModels.ENGINE_PISTONS_3
                                                         : (
                                                            angle == 3
                                                               ? PartialModels.ENGINE_PISTONS_2
                                                               : (angle == 2 ? PartialModels.ENGINE_PISTONS_1 : PartialModels.ENGINE_PISTONS_0)
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
               .rotateY(((Direction)be.getBlockState().getValue(DieselGeneratorBlock.FACING)).toYRot())
               .unCentre()
               .light(light)
               .renderInto(ms, buffer.getBuffer(RenderType.solid()));
         } else {
            CachedBufferer.partial(
                  angle == 10
                     ? PartialModels.ENGINE_PISTONS_VERTICAL_0
                     : (
                        angle == 9
                           ? PartialModels.ENGINE_PISTONS_VERTICAL_1
                           : (
                              angle == 8
                                 ? PartialModels.ENGINE_PISTONS_VERTICAL_2
                                 : (
                                    angle == 7
                                       ? PartialModels.ENGINE_PISTONS_VERTICAL_3
                                       : (
                                          angle == 6
                                             ? PartialModels.ENGINE_PISTONS_VERTICAL_4
                                             : (
                                                angle == 5
                                                   ? PartialModels.ENGINE_PISTONS_VERTICAL_4
                                                   : (
                                                      angle == 4
                                                         ? PartialModels.ENGINE_PISTONS_VERTICAL_3
                                                         : (
                                                            angle == 3
                                                               ? PartialModels.ENGINE_PISTONS_VERTICAL_2
                                                               : (
                                                                  angle == 2
                                                                     ? PartialModels.ENGINE_PISTONS_VERTICAL_1
                                                                     : PartialModels.ENGINE_PISTONS_VERTICAL_0
                                                               )
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
               .rotateY(be.getBlockState().getValue(DieselGeneratorBlock.FACING) == Direction.DOWN ? 180.0 : 270.0)
               .rotateZ(be.getBlockState().getValue(DieselGeneratorBlock.FACING) == Direction.DOWN ? 180.0 : 0.0)
               .unCentre()
               .light(light)
               .renderInto(ms, buffer.getBuffer(RenderType.solid()));
         }
      }

      super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
   }
}
