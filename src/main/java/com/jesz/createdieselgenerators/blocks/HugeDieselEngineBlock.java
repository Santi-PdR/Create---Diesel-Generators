package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.PoweredEngineShaftBlockEntity;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.placement.IPlacementHelper;
import com.simibubi.create.foundation.placement.PlacementHelpers;
import com.simibubi.create.foundation.placement.PlacementOffset;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.Nullable;

public class HugeDieselEngineBlock extends Block implements IBE<HugeDieselEngineBlockEntity>, IWrenchable, ICDGKinetics, ProperWaterloggedBlock {
   public static final DirectionProperty FACING = BlockStateProperties.FACING;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final int placementHelperId = PlacementHelpers.register(new HugeDieselEngineBlock.PlacementHelper());

   public HugeDieselEngineBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(WATERLOGGED, false))
                              .setValue(BlockStateProperties.NORTH, false))
                           .setValue(BlockStateProperties.EAST, false))
                        .setValue(BlockStateProperties.SOUTH, false))
                     .setValue(BlockStateProperties.WEST, false))
                  .setValue(BlockStateProperties.UP, false))
               .setValue(BlockStateProperties.DOWN, false))
            .setValue(DieselGeneratorBlock.POWERED, false)
      );
   }

   public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
      return true;
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      ItemStack itemInHand = player.getItemInHand(hand);
      IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
      if (placementHelper.matchesItem(itemInHand)) {
         return placementHelper.getOffset(player, level, state, pos, hit).placeInWorld(level, (BlockItem)itemInHand.getItem(), player, hand, hit);
      } else if (!(Boolean)ConfigRegistry.ENGINES_FILLED_WITH_ITEMS.get()) {
         return super.use(state, level, pos, player, hand, hit);
      } else if (itemInHand.isEmpty()) {
         return InteractionResult.PASS;
      } else {
         if (level.getBlockEntity(pos) instanceof SmartBlockEntity be) {
            IFluidHandler tank = (IFluidHandler)be.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
            if (tank == null) {
               return InteractionResult.PASS;
            }

            if (itemInHand.getItem() instanceof BucketItem bi) {
               if (!tank.getFluidInTank(0).isEmpty()) {
                  return InteractionResult.FAIL;
               }

               tank.fill(new FluidStack(bi.getFluid(), 1000), FluidAction.EXECUTE);
               if (!player.isCreative()) {
                  player.setItemInHand(hand, new ItemStack(Items.BUCKET));
               }

               return InteractionResult.SUCCESS;
            }

            if (itemInHand.getItem() instanceof MilkBucketItem) {
               if (!tank.getFluidInTank(0).isEmpty()) {
                  return InteractionResult.FAIL;
               }

               tank.fill(new FluidStack((Fluid)ForgeMod.MILK.get(), 1000), FluidAction.EXECUTE);
               if (!player.isCreative()) {
                  player.setItemInHand(hand, new ItemStack(Items.BUCKET));
               }

               return InteractionResult.SUCCESS;
            }

            IFluidHandlerItem itemTank = (IFluidHandlerItem)itemInHand.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
            if (itemTank == null) {
               return InteractionResult.PASS;
            }

            itemTank.drain(tank.fill(itemTank.getFluidInTank(0), FluidAction.EXECUTE), FluidAction.EXECUTE);
         }

         return InteractionResult.PASS;
      }
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

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(
         new Property[]{
            DieselGeneratorBlock.POWERED,
            FACING,
            BlockStateProperties.NORTH,
            BlockStateProperties.EAST,
            BlockStateProperties.WEST,
            BlockStateProperties.SOUTH,
            BlockStateProperties.UP,
            BlockStateProperties.DOWN,
            WATERLOGGED
         }
      );
      super.createBlockStateDefinition(builder);
   }

   @Override
   public InteractionResult onWrenched(BlockState state, UseOnContext context) {
      boolean c = (Boolean)state.getValue(BooleanProperty.create(context.getClickedFace().toString()));
      if (context.getClickedFace().getAxis() == ((Direction)state.getValue(FACING)).getAxis()) {
         return IWrenchable.super.onWrenched(state, context);
      } else {
         if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof HugeDieselEngineBlockEntity be) {
            PoweredEngineShaftBlockEntity shaft = be.getShaft();
            if (shaft != null) {
               shaft.removeGenerator(context.getClickedPos());
            }
         }

         context.getLevel().setBlock(context.getClickedPos(), (BlockState)state.setValue(BooleanProperty.create(context.getClickedFace().toString()), !c), 3);
         return InteractionResult.SUCCESS;
      }
   }

   public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
      return Block.box(1.0, 1.0, 1.0, 15.0, 15.0, 15.0);
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean moving) {
      if ((Boolean)state.getValue(BooleanProperty.create(((Direction)state.getValue(FACING)).toString()))
         || (Boolean)state.getValue(BooleanProperty.create(((Direction)state.getValue(FACING)).getOpposite().toString()))) {
         level.setBlock(
            pos,
            (BlockState)((BlockState)state.setValue(BooleanProperty.create(((Direction)state.getValue(FACING)).toString()), false))
               .setValue(BooleanProperty.create(((Direction)state.getValue(FACING)).getOpposite().toString()), false),
            3
         );
      }

      level.setBlock(pos, (BlockState)state.setValue(DieselGeneratorBlock.POWERED, level.hasNeighborSignal(pos)), 2);
      super.neighborChanged(state, level, pos, block, otherPos, moving);
   }

   public Direction getPreferredFacing(BlockPlaceContext context) {
      Direction preferredSide = null;

      for (Direction side : Iterate.directions) {
         BlockState blockState = context.getLevel().getBlockState(context.getClickedPos().relative(side));
         if (blockState.getBlock() instanceof IRotate
            && ((IRotate)blockState.getBlock()).hasShaftTowards(context.getLevel(), context.getClickedPos().relative(side), blockState, side.getOpposite())) {
            if (preferredSide != null && preferredSide.getAxis() != side.getAxis()) {
               preferredSide = null;
               break;
            }

            preferredSide = side;
         }
      }

      return preferredSide;
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction preferred = this.getPreferredFacing(context);
      if (preferred == null || context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
         Direction nearestLookingDirection = context.getNearestLookingDirection();
         return (BlockState)((BlockState)this.defaultBlockState()
               .setValue(FACING, context.getPlayer() != null && context.getPlayer().isShiftKeyDown() ? nearestLookingDirection : nearestLookingDirection.getOpposite()))
            .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
      } else {
         return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, preferred.getOpposite()))
            .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
      }
   }

   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
      BlockPos shaftPos = pos.relative((Direction)state.getValue(FACING), 2);
      BlockState shaftState = level.getBlockState(shaftPos);
      if (shaftState.getBlock() instanceof ShaftBlock && shaftState.getValue(RotatedPillarKineticBlock.AXIS) != ((Direction)state.getValue(FACING)).getAxis()
         )
       {
         level.setBlock(shaftPos, PoweredEngineShaftBlock.getEquivalent(shaftState), 3);
      }
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())) {
         level.removeBlockEntity(pos);
      }

      BlockPos shaftPos = pos.relative((Direction)state.getValue(FACING), 2);
      BlockState shaftState = level.getBlockState(shaftPos);
      if (BlockRegistry.POWERED_ENGINE_SHAFT.has(shaftState)) {
         level.scheduleTick(shaftPos, shaftState.getBlock(), 1);
      }
   }

   @Override
   public HugeDieselEngineBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new HugeDieselEngineBlockEntity(getBlockEntityType(), pos, state);
   }

   @Override
   public Class<HugeDieselEngineBlockEntity> getBlockEntityClass() {
      return HugeDieselEngineBlockEntity.class;
   }

   @Override
   public BlockEntityType<? extends HugeDieselEngineBlockEntity> getBlockEntityType() {
      return BlockEntityRegistry.HUGE_DIESEL_ENGINE.get();
   }

   @Override
   public float getDefaultStressCapacity() {
      return 2048.0F;
   }

   @Override
   public float getDefaultStressStressImpact() {
      return 0.0F;
   }

   @Override
   public float getDefaultSpeed() {
      return 96.0F;
   }

   public static enum HugeEngineDirection implements StringRepresentable {
      BOTTOM,
      DOWN,
      MIDDLE,
      UP,
      TOP;

      public String getSerializedName() {
         return Lang.asId(this.name());
      }
   }

   private static class PlacementHelper implements IPlacementHelper {
      @Override
      public Predicate<ItemStack> getItemPredicate() {
         return AllBlocks.SHAFT::isIn;
      }

      @Override
      public Predicate<BlockState> getStatePredicate() {
         return s -> s.getBlock() instanceof HugeDieselEngineBlock;
      }

      @Override
      public PlacementOffset getOffset(Player player, Level level, BlockState state, BlockPos pos, BlockHitResult ray) {
         BlockPos shaftPos = pos.relative((Direction)state.getValue(HugeDieselEngineBlock.FACING), 2);
         BlockState shaft = AllBlocks.SHAFT.getDefaultState();

         for (Direction direction : Direction.orderedByNearest(player)) {
            shaft = (BlockState)shaft.setValue(ShaftBlock.AXIS, direction.getAxis());
            if (shaft.getValue(RotatedPillarKineticBlock.AXIS) != ((Direction)state.getValue(HugeDieselEngineBlock.FACING)).getAxis()) {
               break;
            }
         }

         BlockState newState = level.getBlockState(shaftPos);
         if (!newState.canBeReplaced()) {
            return PlacementOffset.fail();
         } else {
            Axis axis = (Axis)shaft.getValue(ShaftBlock.AXIS);
            return PlacementOffset.success(
               shaftPos,
               s -> (BlockState)BlockHelper.copyProperties(s, BlockRegistry.POWERED_ENGINE_SHAFT.getDefaultState()).setValue(PoweredShaftBlock.AXIS, axis)
            );
         }
      }
   }
}
