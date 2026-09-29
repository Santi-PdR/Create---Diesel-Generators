package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BasinLidBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BasinLidBlock extends Block implements ProperWaterloggedBlock, IBE<BasinLidBlockEntity>, IWrenchable {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final BooleanProperty ON_A_BASIN = BooleanProperty.create("on_a_basin");
   public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

   public BasinLidBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)super.defaultBlockState().setValue(ON_A_BASIN, false));
      this.registerDefaultState((BlockState)super.defaultBlockState().setValue(WATERLOGGED, false));
      this.registerDefaultState((BlockState)super.defaultBlockState().setValue(OPEN, false));
      this.registerDefaultState((BlockState)super.defaultBlockState().setValue(POWERED, false));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      if (!(Boolean)pState.getValue(OPEN)) {
         return Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0), Block.box(5.0, 2.0, 5.0, 11.0, 4.0, 11.0));
      } else if (pState.getValue(FACING) == Direction.SOUTH) {
         return Shapes.or(Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0), Block.box(5.0, 5.0, 16.0, 11.0, 11.0, 18.0));
      } else if (pState.getValue(FACING) == Direction.WEST) {
         return Shapes.or(Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0), Block.box(-2.0, 5.0, 5.0, 0.0, 11.0, 11.0));
      } else {
         return pState.getValue(FACING) == Direction.NORTH
            ? Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0), Block.box(5.0, 5.0, -2.0, 11.0, 11.0, 0.0))
            : Shapes.or(Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0), Block.box(16.0, 5.0, 5.0, 18.0, 11.0, 11.0));
      }
   }

   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState p_60569_, boolean p_60570_) {
      super.onPlace(state, level, pos, p_60569_, p_60570_);
      if (level.getBlockEntity(pos.below()) instanceof BasinBlockEntity) {
         level.setBlock(pos, (BlockState)state.setValue(ON_A_BASIN, true), 2);
      } else {
         level.setBlock(pos, (BlockState)state.setValue(ON_A_BASIN, false), 2);
      }
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos p_57551_, boolean p_57552_) {
      boolean flag = level.hasNeighborSignal(pos);
      if (level.getBlockEntity(pos.below()) instanceof BasinBlockEntity) {
         state = (BlockState)state.setValue(ON_A_BASIN, true);
      } else {
         state = (BlockState)state.setValue(ON_A_BASIN, false);
      }

      if (flag != (Boolean)state.getValue(POWERED)) {
         if (flag != (Boolean)state.getValue(OPEN)) {
            level.levelEvent(null, flag ? 1037 : 1036, pos, 0);
         }

         level.setBlock(pos, (BlockState)((BlockState)state.setValue(POWERED, flag)).setValue(OPEN, flag), 2);
         if (flag && level.getBlockEntity(pos) instanceof BasinLidBlockEntity a && a.steamInside) {
            level.playSound(null, pos, AllSoundEvents.STEAM.getMainEvent(), SoundSource.BLOCKS, 1.1F, 0.3F);
            a.steamInside = false;

            for (int i = 0; i < 3; i++) {
               ((ServerLevel)level)
                  .sendParticles(
                     ParticleTypes.CAMPFIRE_COSY_SMOKE,
                     pos.getX() + 0.5F + new Random().nextDouble(-0.3, 0.3),
                     pos.getY(),
                     pos.getZ() + 0.5F + new Random().nextDouble(-0.3, 0.3),
                     0,
                     0.0,
                     1.0,
                     0.0,
                     0.01
                  );
            }
         }
      } else {
         level.setBlock(pos, state, 2);
      }
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      boolean currentState = (Boolean)state.getValue(OPEN);
      if (!currentState && level.getBlockEntity(pos) instanceof BasinLidBlockEntity a && a.steamInside) {
         for (int i = 0; i < 3; i++) {
            if (level instanceof ServerLevel sl) {
               sl.sendParticles(
                  ParticleTypes.CAMPFIRE_COSY_SMOKE,
                  pos.getX() + 0.5F + new Random().nextDouble(-0.3, 0.3),
                  pos.getY(),
                  pos.getZ() + 0.5F + new Random().nextDouble(-0.3, 0.3),
                  0,
                  0.0,
                  1.0,
                  0.0,
                  0.01
               );
               sl.playSound(null, pos, AllSoundEvents.STEAM.getMainEvent(), SoundSource.BLOCKS, 0.1F, 0.3F);
               a.steamInside = false;
            }
         }
      }

      if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND) {
         LogUtils.getLogger().debug(level + "");
         level.setBlock(pos, (BlockState)state.setValue(OPEN, !currentState), 3);
         level.levelEvent(null, currentState ? 1037 : 1036, pos, 0);
      }

      return InteractionResult.SUCCESS;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{ON_A_BASIN});
      builder.add(new Property[]{FACING});
      builder.add(new Property[]{OPEN});
      builder.add(new Property[]{WATERLOGGED});
      builder.add(new Property[]{POWERED});
      super.createBlockStateDefinition(builder);
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return context.getPlayer().isShiftKeyDown()
         ? (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()))
            .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER))
         : (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection()))
            .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
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
   public BasinLidBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new BasinLidBlockEntity(getBlockEntityType(), pos, state);
   }

   @Override
   public Class<BasinLidBlockEntity> getBlockEntityClass() {
      return BasinLidBlockEntity.class;
   }

   @Override
   public BlockEntityType<? extends BasinLidBlockEntity> getBlockEntityType() {
      return BlockEntityRegistry.BASIN_LID.get();
   }
}
