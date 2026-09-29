package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.CanisterBlockEntity;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CanisterBlock extends Block implements IBE<CanisterBlockEntity>, ProperWaterloggedBlock, IWrenchable {
   public static final DirectionProperty FACING = BlockStateProperties.FACING;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

   public CanisterBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)super.defaultBlockState().setValue(WATERLOGGED, false));
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, context.getClickedFace()))
         .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (!level.isClientSide) {
         if (stack != null) {
            this.withBlockEntityDo(level, pos, be -> {
               be.setCapacityEnchantLevel(stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get()));
               if (stack.isEnchanted()) {
                  be.setEnchantmentTag(stack.getEnchantmentTags());
               }

               if (stack.hasCustomHoverName()) {
                  be.setCustomName(stack.getHoverName());
               }
            });
         }
      }
   }

   public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
      if (((Direction)state.getValue(FACING)).getAxis() == Axis.Y) {
         return Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
      } else {
         return ((Direction)state.getValue(FACING)).getAxis() == Axis.X
            ? Block.box(0.0, 2.0, 2.0, 16.0, 14.0, 14.0)
            : Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 16.0);
      }
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{FACING, WATERLOGGED});
   }

   public FluidState getFluidState(BlockState pState) {
      return this.fluidState(pState);
   }

   public BlockState updateShape(
      BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos
   ) {
      this.updateWater(pLevel, pState, pCurrentPos);
      return pState;
   }

   @Override
   public CanisterBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new CanisterBlockEntity(getBlockEntityType(), pos, state);
   }

   @Override
   public Class<CanisterBlockEntity> getBlockEntityClass() {
      return CanisterBlockEntity.class;
   }

   @Override
   public BlockEntityType<? extends CanisterBlockEntity> getBlockEntityType() {
      return BlockEntityRegistry.CANISTER.get();
   }
}
