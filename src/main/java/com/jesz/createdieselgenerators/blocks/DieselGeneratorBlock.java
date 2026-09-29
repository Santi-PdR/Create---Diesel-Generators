package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.DieselGeneratorBlockEntity;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.schematics.requirement.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.Nullable;

public class DieselGeneratorBlock
   extends DirectionalKineticBlock
   implements ISpecialBlockItemRequirement,
   IBE<DieselGeneratorBlockEntity>,
   ProperWaterloggedBlock,
   ICDGKinetics {
   public static final DirectionProperty FACING = BlockStateProperties.FACING;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty SILENCED = BooleanProperty.create("silenced");
   public static final BooleanProperty TURBOCHARGED = BooleanProperty.create("turbocharged");

   public DieselGeneratorBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)super.defaultBlockState().setValue(WATERLOGGED, false)).setValue(SILENCED, false))
               .setValue(TURBOCHARGED, false))
            .setValue(POWERED, false)
      );
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)super.getStateForPlacement(context).setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
   }

   public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
      return true;
   }

   @Override
   public InteractionResult onWrenched(BlockState state, UseOnContext context) {
      if ((Boolean)state.getValue(SILENCED) && context.getPlayer() != null && !context.getLevel().isClientSide) {
         if (!context.getPlayer().isCreative()) {
            context.getPlayer().getInventory().placeItemBackInInventory(ItemRegistry.ENGINE_SILENCER.asStack());
         }

         context.getLevel().setBlock(context.getClickedPos(), (BlockState)state.setValue(SILENCED, false), 3);
         this.playRotateSound(context.getLevel(), context.getClickedPos());
         return InteractionResult.SUCCESS;
      } else if ((Boolean)state.getValue(TURBOCHARGED) && context.getPlayer() != null && !context.getLevel().isClientSide) {
         if (!context.getPlayer().isCreative()) {
            context.getPlayer().getInventory().placeItemBackInInventory(ItemRegistry.ENGINE_TURBO.asStack());
         }

         context.getLevel().setBlock(context.getClickedPos(), (BlockState)state.setValue(TURBOCHARGED, false), 3);
         this.playRotateSound(context.getLevel(), context.getClickedPos());
         return InteractionResult.SUCCESS;
      } else {
         return super.onWrenched(state, context);
      }
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{WATERLOGGED, SILENCED, TURBOCHARGED, POWERED});
      super.createBlockStateDefinition(builder);
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

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean moving) {
      level.setBlock(pos, (BlockState)state.setValue(POWERED, level.hasNeighborSignal(pos)), 2);
      super.neighborChanged(state, level, pos, block, otherPos, moving);
   }

   @Override
   public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
      if (state.hasBlockEntity()) {
         this.withBlockEntityDo(
            worldIn,
            pos,
            be -> {
               if (worldIn.getBlockEntity(pos.relative((Direction)state.getValue(FACING))) instanceof DieselGeneratorBlockEntity nbe
                  && nbe.getBlockState().getValue(FACING) == state.getValue(FACING)) {
                  be.movementDirection.setValue(nbe.movementDirection.getValue());
               }

               if (worldIn.getBlockEntity(pos.relative(((Direction)state.getValue(FACING)).getOpposite())) instanceof DieselGeneratorBlockEntity nbe
                  && nbe.getBlockState().getValue(FACING) == state.getValue(FACING)) {
                  be.movementDirection.setValue(nbe.movementDirection.getValue());
               }

               if (worldIn.getBlockEntity(pos.relative((Direction)state.getValue(FACING))) instanceof DieselGeneratorBlockEntity nbe
                  && nbe.getBlockState().getValue(FACING) == ((Direction)state.getValue(FACING)).getOpposite()) {
                  be.movementDirection.setValue(nbe.movementDirection.getValue() == 1 ? 0 : 1);
               }

               if (worldIn.getBlockEntity(pos.relative(((Direction)state.getValue(FACING)).getOpposite())) instanceof DieselGeneratorBlockEntity nbe
                  && nbe.getBlockState().getValue(FACING) == ((Direction)state.getValue(FACING)).getOpposite()) {
                  be.movementDirection.setValue(nbe.movementDirection.getValue() == 1 ? 0 : 1);
               }
            }
         );
      }

      super.onPlace(state, worldIn, pos, oldState, isMoving);
   }

   @Override
   public DieselGeneratorBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new DieselGeneratorBlockEntity(getBlockEntityType(), pos, state);
   }

   @Override
   public Class<DieselGeneratorBlockEntity> getBlockEntityClass() {
      return DieselGeneratorBlockEntity.class;
   }

   @Override
   public BlockEntityType<? extends DieselGeneratorBlockEntity> getBlockEntityType() {
      return BlockEntityRegistry.DIESEL_ENGINE.get();
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      ItemStack itemInHand = player.getItemInHand(hand);
      if (ItemRegistry.ENGINE_SILENCER.isIn(itemInHand) && !(Boolean)state.getValue(SILENCED) && !(Boolean)state.getValue(TURBOCHARGED)) {
         if (!player.isCreative()) {
            itemInHand.shrink(1);
         }

         level.setBlock(pos, (BlockState)state.setValue(SILENCED, true), 3);
         this.playRotateSound(level, pos);
         return InteractionResult.SUCCESS;
      } else if (ItemRegistry.ENGINE_TURBO.isIn(itemInHand) && !(Boolean)state.getValue(TURBOCHARGED) && !(Boolean)state.getValue(SILENCED)) {
         if (!player.isCreative()) {
            itemInHand.shrink(1);
         }

         level.setBlock(pos, (BlockState)state.setValue(TURBOCHARGED, true), 3);
         this.playRotateSound(level, pos);
         return InteractionResult.SUCCESS;
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

         return super.use(state, level, pos, player, hand, hit);
      }
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      if (pState.getValue(FACING) == Direction.NORTH || pState.getValue(FACING) == Direction.SOUTH) {
         return Shapes.or(Block.box(3.0, 3.0, 0.0, 13.0, 13.0, 16.0), Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0));
      } else if (pState.getValue(FACING) == Direction.DOWN) {
         return Shapes.or(Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0), Block.box(0.0, 4.0, 4.0, 16.0, 12.0, 12.0));
      } else {
         return pState.getValue(FACING) == Direction.UP
            ? Shapes.or(Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0), Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 16.0))
            : Shapes.or(Block.box(0.0, 3.0, 3.0, 16.0, 13.0, 13.0), Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0));
      }
   }

   @Override
   public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
      return ((Direction)state.getValue(FACING)).getAxis() == face.getAxis();
   }

   @Override
   public Axis getRotationAxis(BlockState blockState) {
      return ((Direction)blockState.getValue(FACING)).getAxis();
   }

   @Override
   public ItemRequirement getRequiredItems(BlockState state, BlockEntity blockEntity) {
      List<ItemStack> list = new ArrayList<>();
      list.add(BlockRegistry.DIESEL_ENGINE.asStack());
      if ((Boolean)state.getValue(SILENCED)) {
         list.add(ItemRegistry.ENGINE_SILENCER.asStack());
      }

      if ((Boolean)state.getValue(TURBOCHARGED)) {
         list.add(ItemRegistry.ENGINE_TURBO.asStack());
      }

      return new ItemRequirement(ItemUseType.CONSUME, list);
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

   public static enum EngineTypes {
      NORMAL(ConfigRegistry.NORMAL_ENGINES),
      MODULAR(ConfigRegistry.MODULAR_ENGINES),
      HUGE(ConfigRegistry.HUGE_ENGINES);

      final Supplier<Boolean> isEnabled;

      private EngineTypes(Supplier<Boolean> isEnabled) {
         this.isEnabled = isEnabled;
      }

      public boolean enabled() {
         return this.isEnabled.get();
      }
   }
}
