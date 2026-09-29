package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.HugeDieselEngineBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;

public class HugeDieselEngineValueBox extends Sided {
   @Override
   protected boolean isSideActive(BlockState state, Direction side) {
      return !(Boolean)state.getValue(BooleanProperty.create(side.toString()))
         && ((Direction)state.getValue(HugeDieselEngineBlock.FACING)).getAxis() != side.getAxis();
   }

   @Override
   public Vec3 getLocalOffset(BlockState state) {
      Vec3 location = new Vec3(0.5, 0.5, 0.9485);
      location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(this.getSide()), Axis.Y);
      return VecHelper.rotateCentered(location, AngleHelper.verticalAngle(this.getSide()), Axis.X);
   }

   @Override
   protected Vec3 getSouthLocation() {
      return Vec3.ZERO;
   }
}
