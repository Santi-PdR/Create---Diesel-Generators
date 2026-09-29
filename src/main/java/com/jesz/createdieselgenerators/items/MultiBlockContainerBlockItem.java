package com.jesz.createdieselgenerators.items;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MultiBlockContainerBlockItem extends BlockItem {
   BlockEntityType<?> type;

   public MultiBlockContainerBlockItem(Block block, Properties properties) {
      super(block, properties);
   }

   public InteractionResult place(BlockPlaceContext ctx) {
      if (this.type == null) {
         this.type = ((IBE)this.getBlock()).getBlockEntityType();
      }

      InteractionResult initialResult = super.place(ctx);
      if (!initialResult.consumesAction()) {
         return initialResult;
      } else {
         this.tryMultiPlace(ctx);
         return initialResult;
      }
   }

   protected boolean updateCustomBlockEntityTag(BlockPos p_195943_1_, Level p_195943_2_, Player p_195943_3_, ItemStack p_195943_4_, BlockState p_195943_5_) {
      MinecraftServer minecraftserver = p_195943_2_.getServer();
      if (minecraftserver == null) {
         return false;
      } else {
         CompoundTag nbt = p_195943_4_.getTagElement("BlockEntityTag");
         if (nbt != null) {
            nbt.remove("Size");
            nbt.remove("Height");
            nbt.remove("Controller");
            nbt.remove("LastKnownPos");
         }

         return super.updateCustomBlockEntityTag(p_195943_1_, p_195943_2_, p_195943_3_, p_195943_4_, p_195943_5_);
      }
   }

   private <T extends BlockEntity & IMultiBlockEntityContainer> void tryMultiPlace(BlockPlaceContext ctx) {
      Player player = ctx.getPlayer();
      if (player != null) {
         if (!player.isSteppingCarefully()) {
            Direction face = ctx.getClickedFace();
            ItemStack stack = ctx.getItemInHand();
            Level world = ctx.getLevel();
            BlockPos pos = ctx.getClickedPos();
            BlockPos placedOnPos = pos.relative(face.getOpposite());
            BlockState placedOnState = world.getBlockState(placedOnPos);
            if (placedOnState.getBlock() == this.getBlock()) {
               T tankAt = ConnectivityHandler.partAt(this.type, world, placedOnPos);
               if (tankAt != null) {
                  T controllerBE = tankAt.getControllerBE();
                  if (controllerBE != null) {
                     int width = controllerBE.getWidth();
                     if (width != 1) {
                        int tanksToPlace = 0;
                        Axis blockAxis = tankAt.getMainConnectionAxis();
                        if (face.getAxis() == blockAxis) {
                           Direction facing = Direction.fromAxisAndDirection(blockAxis, AxisDirection.POSITIVE);
                           BlockPos startPos = face == facing.getOpposite()
                              ? controllerBE.getBlockPos().relative(facing.getOpposite())
                              : controllerBE.getBlockPos().relative(facing, controllerBE.getHeight());
                           if (VecHelper.getCoordinate(startPos, blockAxis) == VecHelper.getCoordinate(pos, blockAxis)) {
                              for (int xOffset = 0; xOffset < width; xOffset++) {
                                 for (int zOffset = 0; zOffset < width; zOffset++) {
                                    BlockPos offsetPos = blockAxis == Axis.X
                                       ? startPos.offset(0, xOffset, zOffset)
                                       : (blockAxis == Axis.Y ? startPos.offset(xOffset, 0, zOffset) : startPos.offset(xOffset, zOffset, 0));
                                    BlockState blockState = world.getBlockState(offsetPos);
                                    if (blockState.getBlock() != this.getBlock()) {
                                       if (!blockState.canBeReplaced()) {
                                          return;
                                       }

                                       tanksToPlace++;
                                    }
                                 }
                              }

                              if (player.isCreative() || stack.getCount() >= tanksToPlace) {
                                 for (int xOffset = 0; xOffset < width; xOffset++) {
                                    for (int zOffsetx = 0; zOffsetx < width; zOffsetx++) {
                                       BlockPos offsetPos = blockAxis == Axis.X
                                          ? startPos.offset(0, xOffset, zOffsetx)
                                          : (blockAxis == Axis.Y ? startPos.offset(xOffset, 0, zOffsetx) : startPos.offset(xOffset, zOffsetx, 0));
                                       BlockState blockState = world.getBlockState(offsetPos);
                                       if (blockState.getBlock() != this.getBlock()) {
                                          BlockPlaceContext context = BlockPlaceContext.at(ctx, offsetPos, face);
                                          player.getPersistentData().putBoolean("SilenceTankSound", true);
                                          super.place(context);
                                          player.getPersistentData().remove("SilenceTankSound");
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
