package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

public class CanisterBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
   CanisterBlockEntity.CapacityEnchantedFluidTankBehaviour tank;
   BlockState state;
   private Component customName;
   private int capacityEnchantLevel;
   private ListTag enchantmentTag;

   public CanisterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
      this.state = state;
   }

   @Override
   public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
      return this.containedFluidTooltip(tooltip, isPlayerSneaking, this.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.DOWN));
   }

   @Override
   public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
      this.tank = CanisterBlockEntity.CapacityEnchantedFluidTankBehaviour.single(
         this, Math.abs((Integer)ConfigRegistry.CANISTER_CAPACITY.get()), (Integer)ConfigRegistry.CANISTER_CAPACITY_ENCHANTMENT.get()
      );
      behaviours.add(this.tank);
   }

   public <T> LazyOptional<T> getCapability(Capability<T> cap) {
      return cap == ForgeCapabilities.FLUID_HANDLER ? this.tank.getCapability().cast() : super.getCapability(cap);
   }

   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      return cap == ForgeCapabilities.FLUID_HANDLER ? this.tank.getCapability().cast() : super.getCapability(cap, side);
   }

   @Override
   protected void write(CompoundTag compound, boolean clientPacket) {
      super.write(compound, clientPacket);
      compound.putInt("CapacityEnchantment", this.capacityEnchantLevel);
      if (this.customName != null) {
         compound.putString("CustomName", Serializer.toJson(this.customName));
      }

      if (this.enchantmentTag != null) {
         compound.put("Enchantments", this.enchantmentTag);
      }
   }

   @Override
   protected void read(CompoundTag compound, boolean clientPacket) {
      super.read(compound, clientPacket);
      this.capacityEnchantLevel = compound.getInt("CapacityEnchantment");
      if (compound.contains("Enchantments")) {
         this.enchantmentTag = compound.getList("Enchantments", 10);
      }

      if (compound.contains("CustomName", 8)) {
         this.customName = Serializer.fromJson(compound.getString("CustomName"));
      }
   }

   public void setCustomName(Component customName) {
      this.customName = customName;
   }

   public Component getCustomName() {
      return this.customName;
   }

   public ListTag getEnchantmentTag() {
      return this.enchantmentTag;
   }

   public void setEnchantmentTag(ListTag enchantmentTag) {
      this.enchantmentTag = enchantmentTag;
   }

   public void setCapacityEnchantLevel(int capacityEnchantLevel) {
      this.capacityEnchantLevel = capacityEnchantLevel;
      this.tank.getPrimaryHandler().setCapacity(this.tank.baseCapacity + this.tank.capacityAddition * capacityEnchantLevel);
   }

   public static class CapacityEnchantedFluidTankBehaviour extends SmartFluidTankBehaviour {
      int capacityAddition;
      int baseCapacity;

      public CapacityEnchantedFluidTankBehaviour(
         BehaviourType<SmartFluidTankBehaviour> type, SmartBlockEntity be, int tanks, int tankCapacity, boolean enforceVariety, int capacityAddition
      ) {
         super(type, be, tanks, tankCapacity, enforceVariety);
         this.capacityAddition = capacityAddition;
         this.baseCapacity = tankCapacity;
      }

      public static CanisterBlockEntity.CapacityEnchantedFluidTankBehaviour single(SmartBlockEntity be, int capacity, int capacityAddition) {
         return new CanisterBlockEntity.CapacityEnchantedFluidTankBehaviour(TYPE, be, 1, capacity, false, capacityAddition);
      }

      @Override
      public void read(CompoundTag compound, boolean clientPacket) {
         super.read(compound, clientPacket);
         if (compound.contains("CapacityEnchantment")) {
            this.getPrimaryHandler().setCapacity(this.baseCapacity + compound.getInt("CapacityEnchantment") * this.capacityAddition);
         }
      }
   }
}
