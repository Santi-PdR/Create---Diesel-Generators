package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.DistillationTankBlockEntity;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankBlock.Shape;
import com.simibubi.create.content.schematics.requirement.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.block.IBE;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class DistillationTankBlock extends Block implements IBE<DistillationTankBlockEntity>, IWrenchable, ISpecialBlockItemRequirement {
   public static final BooleanProperty TOP = BooleanProperty.create("top");
   public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
   public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);

   public DistillationTankBlock(Properties properties) {
      super(properties);
   }

   public static boolean isTank(BlockState state) {
      return state.getBlock() instanceof DistillationTankBlock;
   }

   @Override
   public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
      if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof DistillationTankBlockEntity dtbe) {
         int width = dtbe.getControllerBE().getWidth();
         BlockPos pos = dtbe.getController();
         FluidStack stackInTank = ((IFluidHandler)dtbe.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(new FluidTank(1))).getFluidInTank(0).copy();

         for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
               context.getLevel().setBlock(pos.offset(x, 0, z), AllBlocks.FLUID_TANK.getDefaultState(), 3);
               context.getLevel().updateNeighborsAt(pos.offset(x, 0, z), AllBlocks.FLUID_TANK.get());
            }
         }

         if (!stackInTank.isEmpty() && context.getLevel().getBlockEntity(pos) instanceof FluidTankBlockEntity be) {
            IFluidHandler tank = (IFluidHandler)be.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(new FluidTank(1));
            tank.fill(stackInTank, FluidAction.EXECUTE);
         }

         if (!context.getPlayer().isCreative()) {
            context.getPlayer().getInventory().placeItemBackInInventory(ItemRegistry.DISTILLATION_CONTROLLER.asStack(width * width));
         }
      }

      return InteractionResult.SUCCESS;
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
      if (direction == Direction.DOWN && neighbourState.getBlock() != this) {
         this.withBlockEntityDo(level, pos, DistillationTankBlockEntity::updateTemperature);
      }

      return super.updateShape(state, direction, neighbourState, level, pos, neighbourPos);
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean p_60514_) {
      super.neighborChanged(state, level, pos, block, otherPos, p_60514_);
      this.withBlockEntityDo(level, pos, DistillationTankBlockEntity::updateVerticalMulti);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{TOP, BOTTOM, SHAPE});
   }

   @Override
   public DistillationTankBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new DistillationTankBlockEntity(getBlockEntityType(), pos, state);
   }

   @Override
   public Class<DistillationTankBlockEntity> getBlockEntityClass() {
      return DistillationTankBlockEntity.class;
   }

   @Override
   public BlockEntityType<? extends DistillationTankBlockEntity> getBlockEntityType() {
      return BlockEntityRegistry.DISTILLATION_TANK.get();
   }

   @Override
   public InteractionResult onWrenched(BlockState state, UseOnContext context) {
      this.withBlockEntityDo(context.getLevel(), context.getClickedPos(), DistillationTankBlockEntity::toggleWindows);
      return InteractionResult.SUCCESS;
   }

   public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
      if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
         if (!(world.getBlockEntity(pos) instanceof DistillationTankBlockEntity tankBE)) {
            return;
         }

         world.removeBlockEntity(pos);
         ConnectivityHandler.splitMulti(tankBE);
      }
   }

   public ItemStack getCloneItemStack(BlockGetter getter, BlockPos pos, BlockState state) {
      return AllBlocks.FLUID_TANK.asStack();
   }

   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
      if (oldState.getBlock() != state.getBlock()) {
         if (!moved) {
            this.withBlockEntityDo(level, pos, DistillationTankBlockEntity::updateConnectivity);
            this.withBlockEntityDo(level, pos, DistillationTankBlockEntity::updateVerticalMulti);
         }
      }
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      if (mirror == Mirror.NONE) {
         return state;
      } else {
         boolean x = mirror == Mirror.FRONT_BACK;

         return switch ((Shape)state.getValue(SHAPE)) {
            case WINDOW_NE -> (BlockState)state.setValue(SHAPE, x ? Shape.WINDOW_NW : Shape.WINDOW_SE);
            case WINDOW_NW -> (BlockState)state.setValue(SHAPE, x ? Shape.WINDOW_NE : Shape.WINDOW_SW);
            case WINDOW_SE -> (BlockState)state.setValue(SHAPE, x ? Shape.WINDOW_SW : Shape.WINDOW_NE);
            case WINDOW_SW -> (BlockState)state.setValue(SHAPE, x ? Shape.WINDOW_SE : Shape.WINDOW_NW);
            default -> state;
         };
      }
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      for (int i = 0; i < rotation.ordinal(); i++) {
         state = this.rotateOnce(state);
      }

      return state;
   }

   private BlockState rotateOnce(BlockState state) {
      return switch ((Shape)state.getValue(SHAPE)) {
         case WINDOW_NE -> (BlockState)state.setValue(SHAPE, Shape.WINDOW_SE);
         case WINDOW_NW -> (BlockState)state.setValue(SHAPE, Shape.WINDOW_NE);
         case WINDOW_SE -> (BlockState)state.setValue(SHAPE, Shape.WINDOW_SW);
         case WINDOW_SW -> (BlockState)state.setValue(SHAPE, Shape.WINDOW_NW);
         default -> state;
      };
   }

   @Override
   public ItemRequirement getRequiredItems(BlockState state, BlockEntity blockEntity) {
      List<ItemStack> list = new ArrayList<>();
      list.add(AllBlocks.FLUID_TANK.asStack());
      list.add(ItemRegistry.DISTILLATION_CONTROLLER.asStack());
      return new ItemRequirement(ItemUseType.CONSUME, list);
   }
}
