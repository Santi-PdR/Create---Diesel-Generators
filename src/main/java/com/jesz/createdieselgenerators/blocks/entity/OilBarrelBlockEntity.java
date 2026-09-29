package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.OilBarrelBlock;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer.Fluid;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.infrastructure.config.AllConfigs;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class OilBarrelBlockEntity extends SmartBlockEntity implements Fluid, IHaveGoggleInformation {
   private static final int MAX_SIZE = 3;
   protected LazyOptional<IFluidHandler> fluidCapability;
   protected boolean forceFluidLevelUpdate;
   protected FluidTank tankInventory = this.createInventory();
   protected BlockPos controller;
   protected BlockPos lastKnownPos;
   protected boolean updateConnectivity;
   protected int width;
   protected int height;
   private static final int SYNC_RATE = 8;
   protected int syncCooldown;
   protected boolean queuedSync;

   public OilBarrelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
      this.fluidCapability = LazyOptional.of(() -> this.tankInventory);
      this.forceFluidLevelUpdate = true;
      this.updateConnectivity = false;
      this.height = 1;
      this.width = 1;
      this.refreshCapability();
   }

   @Override
   public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
   }

   protected SmartFluidTank createInventory() {
      return new SmartFluidTank(getCapacityMultiplier(), this::onFluidStackChanged);
   }

   public void updateConnectivity() {
      this.updateConnectivity = false;
      if (!this.level.isClientSide) {
         if (this.isController()) {
            ConnectivityHandler.formMulti(this);
         }
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (this.syncCooldown > 0) {
         this.syncCooldown--;
         if (this.syncCooldown == 0 && this.queuedSync) {
            this.sendData();
         }
      }

      if (this.lastKnownPos == null) {
         this.lastKnownPos = this.getBlockPos();
      } else if (!this.lastKnownPos.equals(this.worldPosition) && this.worldPosition != null) {
         this.onPositionChanged();
         return;
      }

      if (this.updateConnectivity) {
         this.updateConnectivity();
      }
   }

   @Override
   public BlockPos getLastKnownPos() {
      return this.lastKnownPos;
   }

   @Override
   public boolean isController() {
      return this.controller == null
         || this.worldPosition.getX() == this.controller.getX()
            && this.worldPosition.getY() == this.controller.getY()
            && this.worldPosition.getZ() == this.controller.getZ();
   }

   @Override
   public void initialize() {
      super.initialize();
      this.sendData();
      if (this.level.isClientSide) {
         this.invalidateRenderBoundingBox();
      }
   }

   private void onPositionChanged() {
      this.removeController(true);
      this.lastKnownPos = this.worldPosition;
   }

   protected void onFluidStackChanged(FluidStack newFluidStack) {
      if (this.hasLevel()) {
         int maxY = (int)(this.getFillState() * this.height + 1.0F);

         for (int yOffset = 0; yOffset < this.height; yOffset++) {
            for (int xOffset = 0; xOffset < this.width; xOffset++) {
               for (int zOffset = 0; zOffset < this.width; zOffset++) {
                  BlockPos pos = this.worldPosition.offset(xOffset, yOffset, zOffset);
                  OilBarrelBlockEntity tankAt = ConnectivityHandler.partAt(this.getType(), this.level, pos);
                  if (tankAt != null) {
                     this.level.updateNeighbourForOutputSignal(pos, tankAt.getBlockState().getBlock());
                  }
               }
            }
         }

         if (!this.level.isClientSide) {
            this.setChanged();
            this.sendData();
         }
      }
   }

   public OilBarrelBlockEntity getControllerBE() {
      if (this.isController()) {
         return this;
      } else {
         BlockEntity blockEntity = this.level.getBlockEntity(this.controller);
         return blockEntity instanceof OilBarrelBlockEntity ? (OilBarrelBlockEntity)blockEntity : null;
      }
   }

   public void applyFluidTankSize(int blocks) {
      this.tankInventory.setCapacity(blocks * getCapacityMultiplier());
      int overflow = this.tankInventory.getFluidAmount() - this.tankInventory.getCapacity();
      if (overflow > 0) {
         this.tankInventory.drain(overflow, FluidAction.EXECUTE);
      }

      this.forceFluidLevelUpdate = true;
   }

   @Override
   public void removeController(boolean keepFluids) {
      if (!this.level.isClientSide) {
         this.updateConnectivity = true;
         if (!keepFluids) {
            this.applyFluidTankSize(1);
         }

         this.controller = null;
         this.width = 1;
         this.height = 1;
         this.onFluidStackChanged(this.tankInventory.getFluid());
         this.refreshCapability();
         this.setChanged();
         this.sendData();
      }
   }

   @Override
   public void sendData() {
      if (this.syncCooldown > 0) {
         this.queuedSync = true;
      } else {
         super.sendData();
         this.queuedSync = false;
         this.syncCooldown = 8;
      }
   }

   @Override
   public void setController(BlockPos controller) {
      if (!this.level.isClientSide || this.isVirtual()) {
         if (!controller.equals(this.controller)) {
            this.controller = controller;
            this.refreshCapability();
            this.setChanged();
            this.sendData();
         }
      }
   }

   private void refreshCapability() {
      LazyOptional<IFluidHandler> oldCap = this.fluidCapability;
      this.fluidCapability = LazyOptional.of(() -> this.handlerForCapability());
      oldCap.invalidate();
   }

   private IFluidHandler handlerForCapability() {
      return (IFluidHandler)(this.isController()
         ? this.tankInventory
         : (this.getControllerBE() != null ? this.getControllerBE().handlerForCapability() : new FluidTank(0)));
   }

   @Override
   public BlockPos getController() {
      return this.isController() ? this.worldPosition : this.controller;
   }

   @Override
   protected AABB createRenderBoundingBox() {
      return this.isController() ? super.createRenderBoundingBox().expandTowards(this.width - 1, this.height - 1, this.width - 1) : super.createRenderBoundingBox();
   }

   @Override
   public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
      OilBarrelBlockEntity controllerBE = this.getControllerBE();
      return controllerBE == null ? false : this.containedFluidTooltip(tooltip, isPlayerSneaking, controllerBE.getCapability(ForgeCapabilities.FLUID_HANDLER));
   }

   @Override
   protected void read(CompoundTag compound, boolean clientPacket) {
      super.read(compound, clientPacket);
      BlockPos controllerBefore = this.controller;
      int prevSize = this.width;
      int prevHeight = this.height;
      this.updateConnectivity = compound.contains("Uninitialized");
      this.controller = null;
      this.lastKnownPos = null;
      if (compound.contains("LastKnownPos")) {
         this.lastKnownPos = NbtUtils.readBlockPos(compound.getCompound("LastKnownPos"));
      }

      if (compound.contains("Controller")) {
         this.controller = NbtUtils.readBlockPos(compound.getCompound("Controller"));
      }

      if (this.isController()) {
         this.width = compound.getInt("Size");
         this.height = compound.getInt("Height");
         this.tankInventory.setCapacity(this.getTotalTankSize() * getCapacityMultiplier());
         this.tankInventory.readFromNBT(compound.getCompound("TankContent"));
         if (this.tankInventory.getSpace() < 0) {
            this.tankInventory.drain(-this.tankInventory.getSpace(), FluidAction.EXECUTE);
         }
      }

      if (clientPacket) {
         boolean changeOfController = !Objects.equals(controllerBefore, this.controller);
         if (changeOfController || prevSize != this.width || prevHeight != this.height) {
            if (this.hasLevel()) {
               this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 16);
            }

            if (this.isController()) {
               this.tankInventory.setCapacity(getCapacityMultiplier() * this.getTotalTankSize());
            }

            this.invalidateRenderBoundingBox();
         }
      }
   }

   public float getFillState() {
      return (float)this.tankInventory.getFluidAmount() / this.tankInventory.getCapacity();
   }

   @Override
   public void write(CompoundTag compound, boolean clientPacket) {
      if (this.updateConnectivity) {
         compound.putBoolean("Uninitialized", true);
      }

      if (this.lastKnownPos != null) {
         compound.put("LastKnownPos", NbtUtils.writeBlockPos(this.lastKnownPos));
      }

      if (!this.isController()) {
         compound.put("Controller", NbtUtils.writeBlockPos(this.controller));
      }

      if (this.isController()) {
         compound.put("TankContent", this.tankInventory.writeToNBT(new CompoundTag()));
         compound.putInt("Size", this.width);
         compound.putInt("Height", this.height);
      }

      super.write(compound, clientPacket);
      if (clientPacket) {
         if (this.forceFluidLevelUpdate) {
            compound.putBoolean("ForceFluidLevel", true);
         }

         if (this.queuedSync) {
            compound.putBoolean("LazySync", true);
         }

         this.forceFluidLevelUpdate = false;
      }
   }

   @Nonnull
   public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
      if (!this.fluidCapability.isPresent()) {
         this.refreshCapability();
      }

      return cap == ForgeCapabilities.FLUID_HANDLER ? this.fluidCapability.cast() : super.getCapability(cap, side);
   }

   @Override
   public void invalidate() {
      super.invalidate();
   }

   public int getTotalTankSize() {
      return this.width * this.width * this.height;
   }

   public static int getCapacityMultiplier() {
      return AllConfigs.server().fluids.fluidTankCapacity.get() * 1000;
   }

   @Override
   public void preventConnectivityUpdate() {
      this.updateConnectivity = false;
   }

   @Override
   public void notifyMultiUpdated() {
      this.onFluidStackChanged(this.tankInventory.getFluid());
      this.setChanged();
   }

   @Override
   public Axis getMainConnectionAxis() {
      return (Axis)this.getBlockState().getValue(OilBarrelBlock.AXIS);
   }

   @Override
   public int getMaxLength(Axis longAxis, int width) {
      return width * 4;
   }

   @Override
   public int getMaxWidth() {
      return (Integer)ConfigRegistry.MAX_OIL_BARREL_WIDTH.get();
   }

   @Override
   public int getHeight() {
      return this.height;
   }

   @Override
   public void setHeight(int height) {
      this.height = height;
   }

   @Override
   public int getWidth() {
      return this.width;
   }

   @Override
   public void setWidth(int width) {
      this.width = width;
   }

   @Override
   public boolean hasTank() {
      return true;
   }

   @Override
   public int getTankSize(int tank) {
      return getCapacityMultiplier();
   }

   @Override
   public void setTankSize(int tank, int blocks) {
      this.applyFluidTankSize(blocks);
   }

   @Override
   public IFluidTank getTank(int tank) {
      return this.tankInventory;
   }

   @Override
   public FluidStack getFluid(int tank) {
      return this.tankInventory.getFluid().copy();
   }
}
