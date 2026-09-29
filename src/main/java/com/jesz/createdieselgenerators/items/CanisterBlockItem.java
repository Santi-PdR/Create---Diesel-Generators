package com.jesz.createdieselgenerators.items;

import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.content.equipment.armor.CapacityEnchantment.ICapacityEnchantable;
import com.simibubi.create.foundation.utility.Lang;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

public class CanisterBlockItem extends BlockItem implements ICapacityEnchantable {
   public CanisterBlockItem(Block block, Properties properties) {
      super(block, properties.stacksTo(1));
   }

   public void appendHoverText(ItemStack stack, Level level, List<Component> components, TooltipFlag tooltipFlag) {
      super.appendHoverText(stack, level, components, tooltipFlag);
      if (stack.getTag() != null) {
         CompoundTag primaryTankCompound = stack.getTag().getCompound("BlockEntityTag").getList("Tanks", 10).getCompound(0).getCompound("TankContent");
         FluidStack fStack = FluidStack.loadFluidStackFromNBT(primaryTankCompound);
         if (fStack.isEmpty()) {
            components.add(Component.translatable("createdieselgenerators.tooltip.empty").withStyle(ChatFormatting.GRAY));
            this.getBlock().appendHoverText(stack, level, components, tooltipFlag);
         } else {
            components.add(
               Lang.fluidName(fStack)
                  .component()
                  .withStyle(ChatFormatting.GRAY)
                  .append(" ")
                  .append(Lang.number(fStack.getAmount()).style(ChatFormatting.GOLD).component())
                  .append(Component.translatable("create.generic.unit.millibuckets").withStyle(ChatFormatting.GOLD))
                  .append(Component.literal(" / "))
                  .append(
                     Lang.number(
                           (Integer)ConfigRegistry.CANISTER_CAPACITY.get()
                              + (Integer)ConfigRegistry.CANISTER_CAPACITY_ENCHANTMENT.get() * stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get())
                        )
                        .style(ChatFormatting.GRAY)
                        .component()
                  )
                  .append(Component.translatable("create.generic.unit.millibuckets").withStyle(ChatFormatting.GRAY))
            );
            this.getBlock().appendHoverText(stack, level, components, tooltipFlag);
         }
      } else {
         components.add(Component.translatable("createdieselgenerators.tooltip.empty").withStyle(ChatFormatting.GRAY));
         this.getBlock().appendHoverText(stack, level, components, tooltipFlag);
      }
   }

   public InteractionResult useOn(UseOnContext p_40581_) {
      return super.useOn(p_40581_);
   }

   public boolean isEnchantable(ItemStack stack) {
      return true;
   }

   public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
      return enchantment == AllEnchantments.CAPACITY.get() ? true : super.canApplyAtEnchantingTable(stack, enchantment);
   }

   public int getBarColor(ItemStack stack) {
      return 15724527;
   }

   public boolean isBarVisible(ItemStack stack) {
      if (stack.getTag() != null) {
         CompoundTag primaryTankCompound = stack.getTag().getCompound("BlockEntityTag").getList("Tanks", 10).getCompound(0).getCompound("TankContent");
         FluidStack fStack = FluidStack.loadFluidStackFromNBT(primaryTankCompound);
         return !fStack.isEmpty();
      } else {
         return false;
      }
   }

   public int getBarWidth(ItemStack stack) {
      if (stack.getTag() == null) {
         return 0;
      } else {
         CompoundTag primaryTankCompound = stack.getTag().getCompound("BlockEntityTag").getList("Tanks", 10).getCompound(0).getCompound("TankContent");
         return Math.round(
            13.0F
               * Mth.clamp(
                  (float)FluidStack.loadFluidStackFromNBT(primaryTankCompound).getAmount()
                     / (
                        (Integer)ConfigRegistry.CANISTER_CAPACITY.get()
                           + (Integer)ConfigRegistry.CANISTER_CAPACITY_ENCHANTMENT.get() * stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get())
                     ),
                  0.0F,
                  1.0F
               )
         );
      }
   }

   public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
      return !ModList.get().isLoaded("dungeons_libraries")
         ? new CanisterBlockItem.CanisterFluidHandlerItemStack(
            stack,
            (Integer)ConfigRegistry.CANISTER_CAPACITY.get()
               + stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get()) * (Integer)ConfigRegistry.CANISTER_CAPACITY_ENCHANTMENT.get()
         )
         : new CanisterBlockItem.CanisterFluidHandlerItemStack(stack, (Integer)ConfigRegistry.CANISTER_CAPACITY.get());
   }

   static class CanisterFluidHandlerItemStack extends FluidHandlerItemStack {
      public CanisterFluidHandlerItemStack(@NotNull ItemStack container, int capacity) {
         super(container, capacity);
      }

      public FluidStack getFluid() {
         CompoundTag tagCompound = this.container.getTag();
         if (tagCompound == null || !tagCompound.getCompound("BlockEntityTag").contains("Tanks")) {
            return FluidStack.EMPTY;
         } else {
            return tagCompound.getCompound("BlockEntityTag").getList("Tanks", 10).isEmpty()
               ? FluidStack.EMPTY
               : FluidStack.loadFluidStackFromNBT(tagCompound.getCompound("BlockEntityTag").getList("Tanks", 10).getCompound(0).getCompound("TankContent"));
         }
      }

      protected void setFluid(FluidStack fluid) {
         if (!this.container.hasTag()) {
            this.container.setTag(new CompoundTag());
         }

         CompoundTag fluidTag = new CompoundTag();
         fluidTag.put("TankContent", new CompoundTag());
         fluid.writeToNBT(fluidTag.getCompound("TankContent"));
         CompoundTag tag = new CompoundTag();
         ListTag list = new ListTag();
         list.add(fluidTag);
         tag.put("Tanks", list);
         this.container.getTag().put("BlockEntityTag", tag);
      }

      protected void setContainerToEmpty() {
         if (this.container.getTag() != null) {
            this.container.getTag().getCompound("BlockEntityTag").remove("Tanks");
         }
      }
   }
}
