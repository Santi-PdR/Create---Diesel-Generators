package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackBearingBlockEntity;
import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;

public class PumpjackBearingBlock extends BearingBlock implements IBE<PumpjackBearingBlockEntity> {
   public PumpjackBearingBlock(Properties properties) {
      super(properties);
   }

   public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
      if (!player.mayBuild()) {
         return InteractionResult.FAIL;
      } else if (player.isShiftKeyDown()) {
         return InteractionResult.FAIL;
      } else if (player.getItemInHand(handIn).isEmpty()) {
         if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
         } else {
            this.withBlockEntityDo(worldIn, pos, be -> {
               if (be.isRunning()) {
                  be.disassemble();
               } else {
                  be.assembleNextTick();
               }
            });
            return InteractionResult.SUCCESS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   public Axis getRotationAxis(BlockState state) {
      return ((Direction)state.getValue(FACING)).getAxis();
   }

   @Override
   public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
      return false;
   }

   @Override
   public InteractionResult onWrenched(BlockState state, UseOnContext context) {
      context.getLevel()
         .setBlock(
            context.getClickedPos(),
            (BlockState)BlockRegistry.PUMPJACK_BEARING_B
               .getDefaultState()
               .setValue(
                  PumpjackBearingBBlock.FACING, ((Direction)state.getValue(FACING)).getAxis() != Axis.Y ? (Direction)state.getValue(FACING) : Direction.NORTH
               ),
            2
         );
      return InteractionResult.SUCCESS;
   }

   @Override
   public PumpjackBearingBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new PumpjackBearingBlockEntity(getBlockEntityType(), pos, state);
   }

   @Override
   public Class<PumpjackBearingBlockEntity> getBlockEntityClass() {
      return PumpjackBearingBlockEntity.class;
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction preferred = context.getHorizontalDirection();
      return context.getPlayer().isShiftKeyDown()
         ? (BlockState)this.defaultBlockState().setValue(FACING, preferred.getOpposite())
         : (BlockState)this.defaultBlockState().setValue(FACING, preferred);
   }

   @Override
   public BlockEntityType<? extends PumpjackBearingBlockEntity> getBlockEntityType() {
      return BlockEntityRegistry.PUMPJACK_BEARING.get();
   }
}
