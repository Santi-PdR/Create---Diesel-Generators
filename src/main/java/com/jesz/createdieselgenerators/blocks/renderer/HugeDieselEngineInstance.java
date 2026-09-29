package com.jesz.createdieselgenerators.blocks.renderer;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.HugeDieselEngineBlock;
import com.jesz.createdieselgenerators.blocks.entity.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.PoweredEngineShaftBlockEntity;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.materials.FlatLit;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class HugeDieselEngineInstance extends BlockEntityInstance<HugeDieselEngineBlockEntity> implements DynamicInstance {
   protected final ModelData piston = this.getTransformMaterial().getModel(PartialModels.ENGINE_PISTON).createInstance();
   protected final ModelData connector = this.getTransformMaterial().getModel(PartialModels.ENGINE_PISTON_CONNECTOR).createInstance();
   protected final ModelData linkage = this.getTransformMaterial().getModel(PartialModels.ENGINE_PISTON_LINKAGE).createInstance();

   public HugeDieselEngineInstance(MaterialManager materialManager, HugeDieselEngineBlockEntity blockEntity) {
      super(materialManager, blockEntity);
   }

   @Override
   public void beginFrame() {
      Float angle = this.blockEntity.getTargetAngle();
      BlockState state = this.blockEntity.getBlockState();
      Direction facing = (Direction)state.getValue(HugeDieselEngineBlock.FACING);
      Axis facingAxis = facing.getAxis();
      if (angle == null) {
         this.transformed(this.piston, facing, false).translate(0.0, 0.53475, 0.0);
         this.linkage.setEmptyTransform();
         this.connector.setEmptyTransform();
      } else {
         PoweredEngineShaftBlockEntity shaft = this.blockEntity.getShaft();
         if (shaft == null) {
            this.transformed(this.piston, facing, false).translate(0.0, 0.53475, 0.0);
            this.linkage.setEmptyTransform();
            this.connector.setEmptyTransform();
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
            float pistonOffset = (1.0F - sine) / 4.0F + 0.4375F;
            this.transformed(this.piston, facing, roll90).translate(0.0, (double)pistonOffset, 0.0);
            this.transformed(this.linkage, facing, roll90)
               .centre()
               .translate(0.0, 1.0, 0.0)
               .unCentre()
               .translate(0.0, (double)pistonOffset, 0.0)
               .translate(0.0, 0.25, 0.5)
               .rotateX(sine2 * 23.0F)
               .translate(0.0, -0.25, -0.5);
            if (shaft.isEngineForConnectorDisplay(this.blockEntity.getBlockPos())) {
               this.transformed(this.connector, facing, roll90)
                  .translate(0.0, 2.0, 0.0)
                  .centre()
                  .rotateXRadians(-angle + (float) (Math.PI / 2) - (facingAxis.isVertical() ? Math.PI : 0.0))
                  .unCentre();
            } else {
               this.connector.setEmptyTransform();
            }
         }
      }
   }

   protected ModelData transformed(ModelData modelData, Direction facing, boolean roll90) {
      return modelData.loadIdentity()
         .translate(this.getInstancePosition())
         .centre()
         .rotateY(AngleHelper.horizontalAngle(facing))
         .rotateX(AngleHelper.verticalAngle(facing) + 90.0F)
         .rotateY(roll90 ? -90.0 : 0.0)
         .unCentre();
   }

   @Override
   public void updateLight() {
      this.relight(this.pos, new FlatLit[]{this.piston, this.connector, this.linkage});
   }

   @Override
   protected void remove() {
      this.piston.delete();
      this.linkage.delete();
      this.connector.delete();
   }
}
