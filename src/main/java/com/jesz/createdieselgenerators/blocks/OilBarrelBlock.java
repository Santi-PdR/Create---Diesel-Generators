package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.OilBarrelBlockEntity;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.Tags.Items;
import net.minecraftforge.common.util.ForgeSoundType;

public class OilBarrelBlock extends Block implements IBE<OilBarrelBlockEntity>, IWrenchable {
   public static final EnumProperty<OilBarrelBlock.OilBarrelColor> OIL_BARREL_COLOR = EnumProperty.create("color", OilBarrelBlock.OilBarrelColor.class);
   public static final EnumProperty<Axis> AXIS = BlockStateProperties.AXIS;
   public static final SoundType SILENCED_METAL = new ForgeSoundType(
      0.1F, 1.5F, () -> SoundEvents.METAL_BREAK, () -> SoundEvents.METAL_STEP, () -> SoundEvents.METAL_PLACE, () -> SoundEvents.METAL_HIT, () -> SoundEvents.METAL_FALL
   );

   public OilBarrelBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(OIL_BARREL_COLOR, OilBarrelBlock.OilBarrelColor.NONE));
   }

   public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
      if (oldState.getBlock() != state.getBlock()) {
         if (!moved) {
            this.withBlockEntityDo(world, pos, OilBarrelBlockEntity::updateConnectivity);
         }
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState state = context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
      return state.getBlock() instanceof OilBarrelBlock && !context.getPlayer().isShiftKeyDown()
         ? (BlockState)this.defaultBlockState().setValue(AXIS, (Axis)state.getValue(AXIS))
         : (BlockState)this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{OIL_BARREL_COLOR, AXIS});
   }

   public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
      if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
         if (!(world.getBlockEntity(pos) instanceof OilBarrelBlockEntity tankBE)) {
            return;
         }

         world.removeBlockEntity(pos);
         ConnectivityHandler.splitMulti(tankBE);
      }
   }

   @Override
   public InteractionResult onWrenched(BlockState state, UseOnContext context) {
      if (context.getClickedFace().getAxis() != state.getValue(AXIS) && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof OilBarrelBlockEntity tankBE) {
         context.getLevel().removeBlockEntity(context.getClickedPos());
         ConnectivityHandler.splitMulti(tankBE);
      }

      return IWrenchable.super.onWrenched(state, context);
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      ItemStack stackInHand = player.getItemInHand(hand);
      if (stackInHand.is(Items.DYES_WHITE)) {
         InteractionResult r = this.tryDye(state, level, pos, player, stackInHand, OilBarrelBlock.OilBarrelColor.WHITE);
         if (r != null) {
            return r;
         }
      }

      if (stackInHand.is(Items.DYES_ORANGE)) {
         InteractionResult r = this.tryDye(state, level, pos, player, stackInHand, OilBarrelBlock.OilBarrelColor.ORANGE);
         if (r != null) {
            return r;
         }
      }

      if (stackInHand.is(Items.DYES_MAGENTA)) {
         InteractionResult r = this.tryDye(state, level, pos, player, stackInHand, OilBarrelBlock.OilBarrelColor.MAGENTA);
         if (r != null) {
            return r;
         }
      }

      if (stackInHand.is(Items.DYES_LIGHT_BLUE)) {
         InteractionResult r = this.tryDye(state, level, pos, player, stackInHand, OilBarrelBlock.OilBarrelColor.LIGHT_BLUE);
         if (r != null) {
            return r;
         }
      }

      if (stackInHand.is(Items.DYES_YELLOW)) {
         InteractionResult r = this.tryDye(state, level, pos, player, stackInHand, OilBarrelBlock.OilBarrelColor.YELLOW);
         if (r != null) {
            return r;
         }
      }

      if (stackInHand.is(Items.DYES_LIME)) {
         InteractionResult r = this.tryDye(state, level, pos, player, stackInHand, OilBarrelBlock.OilBarrelColor.LIME);
         if (r != null) {
            return r;
         }
      }

      if (stackInHand.is(Items.DYES_PINK)) {
         InteractionResult r = this.tryDye(state, level, pos, player, stackInHand, OilBarrelBlock.OilBarrelColor.PINK);
         if (r != null) {
            return r;
         }
      }

      if (stackInHand.is(Items.DYES_GRAY)) {
         InteractionResult r = this.tryDye(state, level, pos, player, stackInHand, OilBarrelBlock.OilBarrelColor.GRAY);
         if (r != null) {
            return r;
         }
      }

      if (stackInHand.is(Items.DYES_LIGHT_GRAY)) {
         InteractionResult r = this.tryDye(state, level, pos, player, stackInHand, OilBarrelBlock.OilBarrelColor.LIGHT_GRAY);
         if (r != null) {
            return r;
         }
      }

      if (stackInHand.is(Items.DYES_CYAN)) {
         InteractionResult r = this.tryDye(state, level, pos, player, stackInHand, OilBarrelBlock.OilBarrelColor.CYAN);
         if (r != null) {
            return r;
         }
      }

      if (stackInHand.is(Items.DYES_PURPLE)) {
         InteractionResult r = this.tryDye(state, level, pos, player, stackInHand, OilBarrelBlock.OilBarrelColor.PURPLE);
         if (r != null) {
            return r;
         }
      }

      if (stackInHand.is(Items.DYES_BLUE)) {
         InteractionResult r = this.tryDye(state, level, pos, player, stackInHand, OilBarrelBlock.OilBarrelColor.BLUE);
         if (r != null) {
            return r;
         }
      }

      if (stackInHand.is(Items.DYES_BROWN)) {
         InteractionResult r = this.tryDye(state, level, pos, player, stackInHand, OilBarrelBlock.OilBarrelColor.BROWN);
         if (r != null) {
            return r;
         }
      }

      if (stackInHand.is(Items.DYES_GREEN)) {
         InteractionResult r = this.tryDye(state, level, pos, player, stackInHand, OilBarrelBlock.OilBarrelColor.GREEN);
         if (r != null) {
            return r;
         }
      }

      if (stackInHand.is(Items.DYES_RED)) {
         InteractionResult r = this.tryDye(state, level, pos, player, stackInHand, OilBarrelBlock.OilBarrelColor.RED);
         if (r != null) {
            return r;
         }
      }

      if (stackInHand.is(Items.DYES_BLACK)) {
         InteractionResult r = this.tryDye(state, level, pos, player, stackInHand, OilBarrelBlock.OilBarrelColor.BLACK);
         if (r != null) {
            return r;
         }
      }

      return super.use(state, level, pos, player, hand, hit);
   }

   public InteractionResult tryDye(BlockState state, Level level, BlockPos pos, Player player, ItemStack stack, OilBarrelBlock.OilBarrelColor color) {
      if (state.getValue(OIL_BARREL_COLOR) != color) {
         level.setBlock(pos, (BlockState)state.setValue(OIL_BARREL_COLOR, color), 2);
         if (!player.isCreative()) {
            stack.shrink(1);
         }

         return InteractionResult.SUCCESS;
      } else {
         if (level.getBlockEntity(pos) instanceof OilBarrelBlockEntity be) {
            OilBarrelBlockEntity controllerBE = be.getControllerBE();
            if (controllerBE != null) {
               boolean successful = false;

               for (int x = 0; x < controllerBE.getWidth(); x++) {
                  for (int z = 0; z < controllerBE.getWidth(); z++) {
                     BlockPos offsetPos = state.getValue(AXIS) == Axis.X
                        ? new BlockPos(pos.getX(), be.getController().getY() + x, be.getController().getZ() + z)
                        : (
                           state.getValue(AXIS) == Axis.Y
                              ? be.getController().offset(x, 0, z).atY(pos.getY())
                              : new BlockPos(be.getController().getX() + x, be.getController().getY() + z, pos.getZ())
                        );
                     BlockState blockState = level.getBlockState(offsetPos);
                     if (blockState.getBlock() instanceof OilBarrelBlock && !stack.isEmpty() && blockState.getValue(OIL_BARREL_COLOR) != color) {
                        level.setBlock(offsetPos, (BlockState)state.setValue(OIL_BARREL_COLOR, color), 2);
                        if (!player.isCreative()) {
                           stack.shrink(1);
                        }

                        successful = true;
                     }
                  }
               }

               if (successful) {
                  return InteractionResult.SUCCESS;
               }
            }
         }

         return null;
      }
   }

   @Override
   public OilBarrelBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new OilBarrelBlockEntity(getBlockEntityType(), pos, state);
   }

   @Override
   public Class<OilBarrelBlockEntity> getBlockEntityClass() {
      return OilBarrelBlockEntity.class;
   }

   @Override
   public BlockEntityType<? extends OilBarrelBlockEntity> getBlockEntityType() {
      return BlockEntityRegistry.OIL_BARREL.get();
   }

   public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
      SoundType soundType = super.getSoundType(state, world, pos, entity);
      return entity != null && entity.getPersistentData().contains("SilenceTankSound") ? SILENCED_METAL : soundType;
   }

   public static enum OilBarrelColor implements StringRepresentable {
      WHITE,
      ORANGE,
      MAGENTA,
      LIGHT_BLUE,
      YELLOW,
      LIME,
      PINK,
      GRAY,
      LIGHT_GRAY,
      CYAN,
      PURPLE,
      BLUE,
      BROWN,
      GREEN,
      RED,
      BLACK,
      NONE;

      public String getSerializedName() {
         return this.name().toLowerCase(Locale.ROOT);
      }
   }
}
