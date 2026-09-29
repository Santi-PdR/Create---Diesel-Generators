package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackCrankBlockEntity;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.placement.IPlacementHelper;
import com.simibubi.create.foundation.placement.PlacementHelpers;
import com.simibubi.create.foundation.placement.PlacementOffset;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PumpjackCrankBlock extends HorizontalKineticBlock implements IBE<PumpjackCrankBlockEntity>, ICDGKinetics {
   private static final int placementHelperId = PlacementHelpers.register(new PumpjackCrankBlock.PlacementHelper());

   public PumpjackCrankBlock(Properties properties) {
      super(properties);
   }

   @Override
   public Axis getRotationAxis(BlockState state) {
      return ((Direction)state.getValue(HORIZONTAL_FACING)).getAxis();
   }

   public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
      return ((Direction)state.getValue(HORIZONTAL_FACING)).getAxis() == Axis.X
         ? Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0), Block.box(4.0, 16.0, 0.0, 12.0, 22.0, 16.0))
         : Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0), Block.box(0.0, 16.0, 4.0, 16.0, 22.0, 12.0));
   }

   @Override
   public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
      return ((Direction)state.getValue(HORIZONTAL_FACING)).getAxis() == face.getAxis();
   }

   @Override
   public PumpjackCrankBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new PumpjackCrankBlockEntity(getBlockEntityType(), pos, state);
   }

   @Override
   public Class<PumpjackCrankBlockEntity> getBlockEntityClass() {
      return PumpjackCrankBlockEntity.class;
   }

   @Override
   public BlockEntityType<? extends PumpjackCrankBlockEntity> getBlockEntityType() {
      return BlockEntityRegistry.PUMPJACK_CRANK.get();
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray) {
      ItemStack heldItem = player.getItemInHand(hand);
      IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
      if (!player.isShiftKeyDown() && player.mayBuild() && placementHelper.matchesItem(heldItem)) {
         placementHelper.getOffset(player, level, state, pos, ray).placeInWorld(level, (BlockItem)heldItem.getItem(), player, hand, ray);
         return InteractionResult.SUCCESS;
      } else {
         return super.use(state, level, pos, player, hand, ray);
      }
   }

   @Override
   public float getDefaultStressCapacity() {
      return 0.0F;
   }

   @Override
   public float getDefaultStressStressImpact() {
      return 16.0F;
   }

   @Override
   public float getDefaultSpeed() {
      return 0.0F;
   }

   private static class PlacementHelper implements IPlacementHelper {
      @Override
      public Predicate<ItemStack> getItemPredicate() {
         return BlockRegistry.PUMPJACK_BEARING::isIn;
      }

      @Override
      public Predicate<BlockState> getStatePredicate() {
         return b -> true;
      }

      @Override
      public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
         if (state.getBlock() instanceof PumpjackCrankBlock) {
            boolean isLarge = world.getBlockEntity(pos) instanceof PumpjackCrankBlockEntity crankBE && crankBE.crankSize.getValue() == 1;
            if (world.getBlockState(pos.above(isLarge ? 4 : 3)).getBlock() instanceof AirBlock) {
               return PlacementOffset.success(pos.above(isLarge ? 4 : 3))
                  .withTransform(
                     b -> (BlockState)BlockRegistry.PUMPJACK_BEARING_B
                        .getDefaultState()
                        .setValue(PumpjackBearingBBlock.FACING, (Direction)state.getValue(HorizontalKineticBlock.HORIZONTAL_FACING))
                  )
                  .withGhostState(
                     (BlockState)BlockRegistry.PUMPJACK_BEARING_B
                        .getDefaultState()
                        .setValue(PumpjackBearingBBlock.FACING, (Direction)state.getValue(HorizontalKineticBlock.HORIZONTAL_FACING))
                  );
            }
         }

         return PlacementOffset.fail();
      }
   }
}
