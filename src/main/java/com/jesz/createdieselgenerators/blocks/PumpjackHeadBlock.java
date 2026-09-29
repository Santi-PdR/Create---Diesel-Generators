package com.jesz.createdieselgenerators.blocks;

import com.simibubi.create.content.contraptions.actors.AttachedActorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PumpjackHeadBlock extends AttachedActorBlock {
   protected PumpjackHeadBlock(Properties properties) {
      super(properties);
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      if (state.getValue(FACING) == Direction.SOUTH) {
         return Block.box(0.0, 2.0, 0.0, 16.0, 14.0, 2.0);
      } else if (state.getValue(FACING) == Direction.NORTH) {
         return Block.box(0.0, 2.0, 14.0, 16.0, 14.0, 16.0);
      } else {
         return state.getValue(FACING) == Direction.EAST ? Block.box(0.0, 2.0, 0.0, 2.0, 14.0, 16.0) : Block.box(14.0, 2.0, 0.0, 16.0, 14.0, 16.0);
      }
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)super.getStateForPlacement(context).setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
   }

   @Override
   public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      return true;
   }
}
