package com.jesz.createdieselgenerators.items;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.blocks.entity.DistillationTankBlockEntity;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class DistillationControllerItem extends Item {
   public DistillationControllerItem(Properties properties) {
      super(properties);
   }

   public InteractionResult useOn(UseOnContext context) {
      if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FluidTankBlockEntity ftbe)) {
         return super.useOn(context);
      } else {
         ItemStack item = context.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
         BlockPos cPos = ftbe.getController();
         int width = ftbe.getControllerBE().getWidth();
         int height = ftbe.getControllerBE().getHeight();
         FluidStack fluidInTank = ((IFluidHandler)ftbe.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(new FluidTank(0))).getFluidInTank(0).copy();

         for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
               for (int y = 0; y < height && (item.getCount() != 0 || context.getPlayer().isCreative()); y++) {
                  context.getLevel().setBlock(cPos.offset(x, y, z), BlockRegistry.DISTILLATION_TANK.getDefaultState(), 1);
                  context.getLevel().updateNeighborsAt(cPos.offset(x, y, z), BlockRegistry.DISTILLATION_TANK.getDefaultState().getBlock());
                  if (!context.getPlayer().isCreative()) {
                     item.shrink(1);
                  }
               }
            }
         }

         AllSoundEvents.WRENCH_ROTATE
            .playAt(context.getLevel(), cPos.getX() + width / 2.0, cPos.getY() + height / 2.0, cPos.getZ() + width / 2.0, 1.0F, 1.0F, false);

         for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
               for (int yx = 0; yx < height; yx++) {
                  if (context.getLevel().getBlockEntity(cPos.offset(x, yx, z)) instanceof DistillationTankBlockEntity dtbe) {
                     dtbe.updateVerticalMulti();
                     dtbe.updateConnectivity();
                     if (x == 0 && yx == 0 && z == 0) {
                        IFluidHandler tank = (IFluidHandler)dtbe.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
                        if (tank != null) {
                           tank.fill(fluidInTank, FluidAction.EXECUTE);
                        }

                        dtbe.updateTemperature();
                     }
                  }
               }
            }
         }

         return InteractionResult.SUCCESS;
      }
   }
}
