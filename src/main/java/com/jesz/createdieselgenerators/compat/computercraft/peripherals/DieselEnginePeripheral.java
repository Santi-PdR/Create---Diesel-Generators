package com.jesz.createdieselgenerators.compat.computercraft.peripherals;

import com.jesz.createdieselgenerators.blocks.entity.DieselGeneratorBlockEntity;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import dan200.computercraft.api.lua.LuaFunction;

public class DieselEnginePeripheral extends SyncedPeripheral<DieselGeneratorBlockEntity> {
   public DieselEnginePeripheral(DieselGeneratorBlockEntity blockEntity) {
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
      return this.blockEntity.calculateAddedStressCapacity();
   }

   @LuaFunction
   public final float getSpeed() {
      return Math.abs(this.blockEntity.getGeneratedSpeed());
   }

   @LuaFunction
   public final float getFuelAmount() {
      return this.blockEntity.tank.getPrimaryHandler().getFluid().getAmount();
   }

   @LuaFunction
   public final float getFuelBurnRate() {
      return FuelTypeManager.getBurnRate(this.blockEntity, this.blockEntity.tank.getPrimaryHandler().getFluid().getFluid());
   }
}
