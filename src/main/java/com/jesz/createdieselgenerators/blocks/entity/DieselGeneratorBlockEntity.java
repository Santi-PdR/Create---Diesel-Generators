package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock;
import com.jesz.createdieselgenerators.compat.computercraft.CCProxy;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.jesz.createdieselgenerators.sounds.SoundRegistry;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate.StressImpact;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.Lang;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;

public class DieselGeneratorBlockEntity extends GeneratingKineticBlockEntity {
   BlockState state;
   public boolean validFuel;
   public SmartFluidTankBehaviour tank;
   int partialSecond;
   public AbstractComputerBehaviour computerBehaviour;
   public ScrollOptionBehaviour<RotationDirection> movementDirection;
   int t = 0;

   public DieselGeneratorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
      super(typeIn, pos, state);
      this.state = state;
   }

   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      if (this.computerBehaviour.isPeripheralCap(cap)) {
         return this.computerBehaviour.getPeripheralCapability();
      } else {
         if (this.state.getValue(DieselGeneratorBlock.FACING) == Direction.DOWN) {
            if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.WEST) {
               return this.tank.getCapability().cast();
            }

            if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.EAST) {
               return this.tank.getCapability().cast();
            }
         } else if (this.state.getValue(DieselGeneratorBlock.FACING) == Direction.UP) {
            if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.NORTH) {
               return this.tank.getCapability().cast();
            }

            if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.SOUTH) {
               return this.tank.getCapability().cast();
            }
         } else if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.DOWN) {
            return this.tank.getCapability().cast();
         }

         return super.getCapability(cap, side);
      }
   }

   public <T> LazyOptional<T> getCapability(Capability<T> cap) {
      return cap == ForgeCapabilities.FLUID_HANDLER ? this.tank.getCapability().cast() : super.getCapability(cap);
   }

   @Override
   protected void write(CompoundTag compound, boolean clientPacket) {
      super.write(compound, clientPacket);
      compound.putInt("PartialSecond", this.partialSecond);
      this.tank.write(compound, false);
   }

   @Override
   protected void read(CompoundTag compound, boolean clientPacket) {
      super.read(compound, clientPacket);
      this.partialSecond = compound.getInt("PartialSecond");
      this.tank.read(compound, false);
   }

   @Override
   public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
      behaviours.add(this.computerBehaviour = CCProxy.behaviour(this));
      this.movementDirection = new ScrollOptionBehaviour<>(
         RotationDirection.class, Lang.translateDirect("contraptions.windmill.rotation_direction"), this, new DieselGeneratorValueBox()
      );
      this.movementDirection.withCallback($ -> this.onDirectionChanged());
      behaviours.add(this.movementDirection);
      this.tank = SmartFluidTankBehaviour.single(this, 1000);
      behaviours.add(this.tank);
      super.addBehaviours(behaviours);
   }

   public void onDirectionChanged() {
   }

   @Override
   public void initialize() {
      super.initialize();
      if (!this.hasSource() || this.getGeneratedSpeed() > this.getTheoreticalSpeed()) {
         this.updateGeneratedRotation();
      }
   }

   @Override
   public float calculateAddedStressCapacity() {
      return this.getGeneratedSpeed() != 0.0F && !this.state.getValue(DieselGeneratorBlock.POWERED)
         ? FuelTypeManager.getGeneratedStress(this, this.tank.getPrimaryHandler().getFluid().getFluid()) / Math.abs(this.getGeneratedSpeed())
         : 0.0F;
   }

   @Override
   public float getGeneratedSpeed() {
      return this.state.getValue(DieselGeneratorBlock.POWERED)
         ? 0.0F
         : convertToDirection(
               (this.movementDirection.getValue() == 1 ? -1 : 1) * FuelTypeManager.getGeneratedSpeed(this, this.tank.getPrimaryHandler().getFluid().getFluid()),
               (Direction)this.getBlockState().getValue(DieselGeneratorBlock.FACING)
            )
            * (this.state.getValue(DieselGeneratorBlock.TURBOCHARGED) ? ((Double)ConfigRegistry.TURBOCHARGED_ENGINE_MULTIPLIER.get()).floatValue() : 1.0F);
   }

   @Override
   public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
      boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
      if (!StressImpact.isEnabled()) {
         return added;
      } else {
         float stressBase = this.calculateAddedStressCapacity();
         return Mth.equal(stressBase, 0.0F) ? added : this.containedFluidTooltip(tooltip, isPlayerSneaking, this.tank.getCapability().cast());
      }
   }

   @Override
   public void tick() {
      super.tick();
      this.state = this.getBlockState();
      this.reActivateSource = true;
      if (this.level.isClientSide && !(Boolean)this.state.getValue(DieselGeneratorBlock.SILENCED)) {
         if (this.state.getValue(DieselGeneratorBlock.TURBOCHARGED)
            ? this.t > FuelTypeManager.getSoundSpeed(this.tank.getPrimaryHandler().getFluid().getFluid()) / 2
            : this.t > FuelTypeManager.getSoundSpeed(this.tank.getPrimaryHandler().getFluid().getFluid())) {
            if (this.validFuel) {
               this.t = 0;
               this.level
                  .playLocalSound(
                     this.worldPosition.getX(),
                     this.worldPosition.getY(),
                     this.worldPosition.getZ(),
                     (SoundEvent)SoundRegistry.DIESEL_ENGINE_SOUND.get(),
                     SoundSource.BLOCKS,
                     this.state.getValue(DieselGeneratorBlock.TURBOCHARGED) ? 0.5F : 0.3F,
                     this.state.getValue(DieselGeneratorBlock.TURBOCHARGED) ? 1.1F : 1.0F,
                     false
                  );
            }
         } else {
            this.t++;
         }
      }

      if ((Boolean)this.state.getValue(DieselGeneratorBlock.POWERED)) {
         this.validFuel = false;
      } else {
         this.validFuel = FuelTypeManager.getGeneratedSpeed(this, this.tank.getPrimaryHandler().getFluid().getFluid()) != 0.0F;
      }

      this.partialSecond++;
      if (this.partialSecond >= 20) {
         this.partialSecond = 0;
         if (this.validFuel) {
            if (this.tank.getPrimaryHandler().getFluid().getAmount()
               >= FuelTypeManager.getBurnRate(this, this.tank.getPrimaryHandler().getFluid().getFluid())
                  * (
                     !this.state.getValue(DieselGeneratorBlock.TURBOCHARGED)
                        ? 1.0F
                        : ((Double)ConfigRegistry.TURBOCHARGED_ENGINE_BURN_RATE_MULTIPLIER.get()).floatValue()
                  )) {
               this.tank
                  .getPrimaryHandler()
                  .setFluid(
                     FluidHelper.copyStackWithAmount(
                        this.tank.getPrimaryHandler().getFluid(),
                        (int)(
                           this.tank.getPrimaryHandler().getFluid().getAmount()
                              - FuelTypeManager.getBurnRate(this, this.tank.getPrimaryHandler().getFluid().getFluid())
                                 * (
                                    !this.state.getValue(DieselGeneratorBlock.TURBOCHARGED)
                                       ? 1.0F
                                       : ((Double)ConfigRegistry.TURBOCHARGED_ENGINE_BURN_RATE_MULTIPLIER.get()).floatValue()
                                 )
                        )
                     )
                  );
            } else {
               this.tank.getPrimaryHandler().setFluid(FluidStack.EMPTY);
            }
         }
      }
   }
}
