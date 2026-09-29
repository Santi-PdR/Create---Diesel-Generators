package com.jesz.createdieselgenerators.compat.computercraft.peripherals;

import com.jesz.createdieselgenerators.blocks.entity.LargeDieselGeneratorBlockEntity;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class ModularDieselEnginePeripheral extends SyncedPeripheral<LargeDieselGeneratorBlockEntity> {
   public ModularDieselEnginePeripheral(LargeDieselGeneratorBlockEntity blockEntity) {
      super(blockEntity);
   }

   public String getType() {
      return "CDG_DieselEngine";
   }

   @LuaFunction
   public final void setMovementDirection(boolean direction) {
      this.blockEntity.movementDirection.setValue(direction ? 1 : 0);
   }

   @LuaFunction
   public final boolean getMovementDirection() {
      return this.blockEntity.movementDirection.getValue() == 1;
   }

   @LuaFunction
   public final float getStressCapacity() {
      LargeDieselGeneratorBlockEntity frontEngine = this.blockEntity.frontEngine.get();
      return frontEngine == null ? this.blockEntity.calculateAddedStressCapacity() : frontEngine.calculateAddedStressCapacity();
   }

   @LuaFunction
   public final int getEngineMultiBlockSize() {
      LargeDieselGeneratorBlockEntity frontEngine = this.blockEntity.frontEngine.get();
      return frontEngine == null ? this.blockEntity.stacked : frontEngine.stacked;
   }

   @LuaFunction
   public final float getSpeed() {
      LargeDieselGeneratorBlockEntity frontEngine = this.blockEntity.frontEngine.get();
      return frontEngine == null ? Math.abs(this.blockEntity.getGeneratedSpeed()) : Math.abs(frontEngine.getGeneratedSpeed());
   }

   @LuaFunction
   public final float getFuelAmount() {
      LargeDieselGeneratorBlockEntity frontEngine = this.blockEntity.frontEngine.get();
      return frontEngine == null
         ? this.blockEntity.tank.getPrimaryHandler().getFluid().getAmount()
         : frontEngine.tank.getPrimaryHandler().getFluid().getAmount();
   }

   @LuaFunction
   public final float getFuelBurnRate() {
      return FuelTypeManager.getBurnRate(
         ((IFluidHandler)this.blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(new FluidTank(1))).getFluidInTank(0).getFluid()
      );
   }
}
