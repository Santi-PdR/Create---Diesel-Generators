package com.jesz.createdieselgenerators.blocks.entity;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PumpjackCrankValueBox extends Sided {
   @Override
   protected boolean isSideActive(BlockState state, Direction side) {
      return side.getAxis() == ((Direction)state.getValue(HorizontalDirectionalBlock.FACING)).getClockWise(Axis.Y).getAxis();
   }

   @Override
   public Vec3 getLocalOffset(BlockState state) {
      Vec3 location = new Vec3(0.5, 0.5, 1.0);
      location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(this.getSide()), Axis.Y);
      return VecHelper.rotateCentered(location, AngleHelper.verticalAngle(this.getSide()), Axis.X);
   }

   @Override
   protected Vec3 getSouthLocation() {
      return Vec3.ZERO;
   }
}
