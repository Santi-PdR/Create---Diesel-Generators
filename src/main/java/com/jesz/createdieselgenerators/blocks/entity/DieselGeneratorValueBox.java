package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DieselGeneratorValueBox extends Sided {
   @Override
   protected boolean isSideActive(BlockState state, Direction side) {
      if (state.getValue(DieselGeneratorBlock.FACING) == Direction.UP) {
         return side == Direction.WEST;
      } else {
         return state.getValue(DieselGeneratorBlock.FACING) == Direction.DOWN ? side == Direction.NORTH : side == Direction.UP;
      }
   }

   @Override
   public Vec3 getLocalOffset(BlockState state) {
      if (state.getValue(DieselGeneratorBlock.FACING) == Direction.UP) {
         return VecHelper.voxelSpace(3.0, 8.0, 8.0);
      } else {
         return state.getValue(DieselGeneratorBlock.FACING) == Direction.DOWN ? VecHelper.voxelSpace(8.0, 8.0, 3.0) : VecHelper.voxelSpace(8.0, 13.0, 8.0);
      }
   }

   @Override
   public void rotate(BlockState state, PoseStack ms) {
      super.rotate(state, ms);
   }

   @Override
   protected Vec3 getSouthLocation() {
      return Vec3.ZERO;
   }
}
