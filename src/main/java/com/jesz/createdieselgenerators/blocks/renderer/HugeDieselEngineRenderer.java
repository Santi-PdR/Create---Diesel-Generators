package com.jesz.createdieselgenerators.blocks.renderer;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.HugeDieselEngineBlock;
import com.jesz.createdieselgenerators.blocks.entity.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.PoweredEngineShaftBlockEntity;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class HugeDieselEngineRenderer extends SafeBlockEntityRenderer<HugeDieselEngineBlockEntity> {
   public HugeDieselEngineRenderer(Context context) {
   }

   protected void renderSafe(HugeDieselEngineBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
      if (!Backend.canUseInstancing(be.getLevel())) {
         Float angle = be.getTargetAngle();
         VertexConsumer vb = bufferSource.getBuffer(RenderType.solid());
         BlockState state = be.getBlockState();
         Direction facing = (Direction)state.getValue(HugeDieselEngineBlock.FACING);
         Axis facingAxis = facing.getAxis();
         if (angle == null) {
            this.transformed(PartialModels.ENGINE_PISTON, state, facing, false).translate(0.0, 0.53475, 0.0).light(light).renderInto(ms, vb);
         } else {
            PoweredEngineShaftBlockEntity shaft = be.getShaft();
            if (shaft == null) {
               this.transformed(PartialModels.ENGINE_PISTON, state, facing, false).translate(0.0, 0.53475, 0.0).light(light).renderInto(ms, vb);
            } else {
               Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
               boolean roll90 = facingAxis.isHorizontal() && axis == Axis.Y || facingAxis.isVertical() && axis == Axis.Z;
               float shaftR = facing == Direction.DOWN
                  ? -90.0F
                  : (facing == Direction.UP ? 90.0F : (facing == Direction.WEST ? -90.0F : (facing == Direction.EAST ? 90.0F : 0.0F)));
               if (roll90) {
                  shaftR = facing == Direction.NORTH
                     ? 180.0F
                     : (facing == Direction.SOUTH ? 0.0F : (facing == Direction.EAST ? -90.0F : (facing == Direction.WEST ? 90.0F : 0.0F)));
               }

               angle = angle + (float)(shaftR * Math.PI / 180.0);
               float sine = Mth.sin(angle) * (((Direction)state.getValue(HugeDieselEngineBlock.FACING)).getAxis() == Axis.Y ? -1 : 1);
               float sine2 = Mth.sin(angle - (float) (Math.PI / 2))
                  * (((Direction)state.getValue(HugeDieselEngineBlock.FACING)).getAxis() == Axis.Y ? -1 : 1);
               float piston = (1.0F - sine) / 4.0F + 0.4375F;
               this.transformed(PartialModels.ENGINE_PISTON, state, facing, roll90).translate(0.0, (double)piston, 0.0).light(light).renderInto(ms, vb);
               this.transformed(PartialModels.ENGINE_PISTON_LINKAGE, state, facing, roll90)
                  .centre()
                  .translate(0.0, 1.0, 0.0)
                  .unCentre()
                  .translate(0.0, (double)piston, 0.0)
                  .translate(0.0, 0.25, 0.5)
                  .rotateX(sine2 * 23.0F)
                  .translate(0.0, -0.25, -0.5)
                  .light(light)
                  .renderInto(ms, vb);
               if (shaft.isEngineForConnectorDisplay(be.getBlockPos())) {
                  this.transformed(PartialModels.ENGINE_PISTON_CONNECTOR, state, facing, roll90)
                     .translate(0.0, 2.0, 0.0)
                     .centre()
                     .rotateXRadians(-angle + (float) (Math.PI / 2))
                     .unCentre()
                     .light(light)
                     .renderInto(ms, vb);
               }
            }
         }
      }
   }

   private SuperByteBuffer transformed(PartialModel model, BlockState blockState, Direction facing, boolean roll90) {
      return CachedBufferer.partial(model, blockState)
         .centre()
         .rotateY(AngleHelper.horizontalAngle(facing))
         .rotateX(AngleHelper.verticalAngle(facing) + 90.0F)
         .rotateY(roll90 ? -90.0 : 0.0)
         .unCentre();
   }

   public int getViewDistance() {
      return 128;
   }
}
