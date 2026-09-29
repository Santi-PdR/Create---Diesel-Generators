package com.jesz.createdieselgenerators.items;

import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.entity.ChemicalSprayerProjectileEntity;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.content.equipment.armor.CapacityEnchantment.ICapacityEnchantable;
import com.simibubi.create.foundation.item.CustomArmPoseItem;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import com.simibubi.create.foundation.utility.Lang;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.ModList;

public class ChemicalSprayerItem extends Item implements CustomArmPoseItem, ICapacityEnchantable {
   boolean lighter;

   public ChemicalSprayerItem(Properties properties, boolean lighter) {
      super(properties.stacksTo(1));
      this.lighter = lighter;
   }

   public void appendHoverText(ItemStack stack, Level level, List<Component> components, TooltipFlag tooltipFlag) {
      super.appendHoverText(stack, level, components, tooltipFlag);
      if (stack.getTag() != null) {
         CompoundTag tankCompound = stack.getTag().getCompound("Fluid");
         FluidStack fStack = FluidStack.loadFluidStackFromNBT(tankCompound);
         if (fStack.isEmpty()) {
            components.add(Component.translatable("createdieselgenerators.tooltip.empty").withStyle(ChatFormatting.GRAY));
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
                           (Integer)ConfigRegistry.TOOL_CAPACITY.get()
                              + stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get()) * (Integer)ConfigRegistry.TOOL_CAPACITY_ENCHANTMENT.get()
                        )
                        .style(ChatFormatting.GRAY)
                        .component()
                  )
                  .append(Component.translatable("create.generic.unit.millibuckets").withStyle(ChatFormatting.GRAY))
            );
         }
      } else {
         components.add(Component.translatable("createdieselgenerators.tooltip.empty").withStyle(ChatFormatting.GRAY));
      }
   }

   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.NONE;
   }

   @Override
   public ArmPose getArmPose(ItemStack stack, AbstractClientPlayer player, InteractionHand hand) {
      return !player.swinging ? ArmPose.CROSSBOW_HOLD : null;
   }

   public int getBarColor(ItemStack stack) {
      return 15724527;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stackInHand = player.getItemInHand(hand);
      if (stackInHand.getTag() != null) {
         FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(stackInHand.getTag().getCompound("Fluid"));
         if (fluidStack.getAmount() != 0) {
            player.startUsingItem(hand);
         }
      }

      return super.use(level, player, hand);
   }

   public boolean isEnchantable(ItemStack stack) {
      return true;
   }

   public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
      return enchantment == AllEnchantments.CAPACITY.get() ? true : super.canApplyAtEnchantingTable(stack, enchantment);
   }

   public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
      if (stack.getTag() != null) {
         FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(stack.getTag().getCompound("Fluid"));
         if (!fluidStack.isEmpty() && !level.isClientSide) {
            if (count % 2 == 0) {
               ChemicalSprayerProjectileEntity projectile = ChemicalSprayerProjectileEntity.spray(
                  level,
                  fluidStack,
                  FuelTypeManager.getGeneratedSpeed(fluidStack.getFluid()) != 0.0F && this.lighter || fluidStack.getFluid().isSame(Fluids.LAVA),
                  fluidStack.getFluid().isSame(Fluids.WATER)
               );
               projectile.setPos(player.position().add(0.0, 1.5, 0.0));
               projectile.shootFromRotation(
                  player, player.getXRot() + new Random().nextFloat(-5.0F, 5.0F), player.getYRot() + new Random().nextFloat(-5.0F, 5.0F), 0.0F, 1.0F, 1.0F
               );
               level.addFreshEntity(projectile);
               fluidStack.setAmount(fluidStack.getAmount() - 1);
            }

            if (!(player instanceof Player p && p.isCreative()) && count % 25 == 0) {
               fluidStack.writeToNBT(stack.getTag().getCompound("Fluid"));
            }
         }
      }

      super.onUseTick(level, player, stack, count);
   }

   public int getUseDuration(ItemStack stack) {
      return 1000;
   }

   public boolean isBarVisible(ItemStack stack) {
      if (stack.getTag() != null) {
         CompoundTag tankCompound = stack.getTag().getCompound("Fluid");
         FluidStack fStack = FluidStack.loadFluidStackFromNBT(tankCompound);
         return !fStack.isEmpty();
      } else {
         return false;
      }
   }

   public int getBarWidth(ItemStack stack) {
      if (stack.getTag() == null) {
         return 0;
      } else {
         CompoundTag tankCompound = stack.getTag().getCompound("Fluid");
         return Math.round(
            13.0F
               * Mth.clamp(
                  FluidStack.loadFluidStackFromNBT(tankCompound).getAmount()
                     / (
                        (float)((Integer)ConfigRegistry.TOOL_CAPACITY.get()).intValue()
                           + stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get()) * (Integer)ConfigRegistry.TOOL_CAPACITY_ENCHANTMENT.get()
                     ),
                  0.0F,
                  1.0F
               )
         );
      }
   }

   public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
      return !ModList.get().isLoaded("dungeons_libraries")
         ? new FluidHandlerItemStack(
            stack,
            (Integer)ConfigRegistry.TOOL_CAPACITY.get()
               + stack.getEnchantmentLevel(AllEnchantments.CAPACITY.get()) * (Integer)ConfigRegistry.TOOL_CAPACITY_ENCHANTMENT.get()
         )
         : new FluidHandlerItemStack(stack, (Integer)ConfigRegistry.TOOL_CAPACITY.get());
   }

   @OnlyIn(Dist.CLIENT)
   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(SimpleCustomRenderer.create(this, new ChemicalSprayerItemRenderer()));
   }
}
