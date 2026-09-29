package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock;
import com.jesz.createdieselgenerators.blocks.LargeDieselGeneratorBlock;
import com.jesz.createdieselgenerators.compat.computercraft.CCProxy;
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
import java.lang.ref.WeakReference;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
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
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class LargeDieselGeneratorBlockEntity extends GeneratingKineticBlockEntity {
   BlockState state;
   public boolean validFuel;
   public int stacked;
   boolean end = true;
   public WeakReference<LargeDieselGeneratorBlockEntity> forw;
   public WeakReference<LargeDieselGeneratorBlockEntity> back;
   public SmartFluidTankBehaviour tank;
   int partialSecond;
   public ScrollOptionBehaviour<RotationDirection> movementDirection;
   public AbstractComputerBehaviour computerBehaviour;
   public WeakReference<LargeDieselGeneratorBlockEntity> frontEngine = new WeakReference<>(null);
   int t = 0;
   int totalSize = 0;

   public LargeDieselGeneratorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
      super(typeIn, pos, state);
      this.forw = new WeakReference<>(null);
      this.back = new WeakReference<>(null);
      this.state = state;
   }

   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      if (this.computerBehaviour.isPeripheralCap(cap)) {
         return this.computerBehaviour.getPeripheralCapability();
      } else {
         if ((Boolean)this.state.getValue(LargeDieselGeneratorBlock.PIPE)) {
            LargeDieselGeneratorBlockEntity frontEngine = this.frontEngine.get();
            if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.UP) {
               if (frontEngine != null) {
                  return frontEngine.tank.getCapability().cast();
               }

               return this.tank.getCapability().cast();
            }
         }

         return super.getCapability(cap, side);
      }
   }

   public <T> LazyOptional<T> getCapability(Capability<T> cap) {
      LargeDieselGeneratorBlockEntity frontEngine = this.frontEngine.get();
      if (cap == ForgeCapabilities.FLUID_HANDLER) {
         return frontEngine != null ? frontEngine.tank.getCapability().cast() : this.tank.getCapability().cast();
      } else {
         return super.getCapability(cap);
      }
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
         RotationDirection.class, Lang.translateDirect("contraptions.windmill.rotation_direction"), this, new LargeDieselGeneratorValueBox()
      );
      this.movementDirection.withCallback($ -> this.onDirectionChanged(true));
      behaviours.add(this.movementDirection);
      this.tank = SmartFluidTankBehaviour.single(this, 1000);
      behaviours.add(this.tank);
      super.addBehaviours(behaviours);
   }

   public void onDirectionChanged(boolean first) {
      LargeDieselGeneratorBlockEntity frontEngine = this.frontEngine.get();
      if (frontEngine != null) {
         if (first && this.getEngineFor() != null) {
            frontEngine.movementDirection.setValue(this.movementDirection.getValue());
            frontEngine.onDirectionChanged(false);
         } else {
            this.movementDirection.setValue(frontEngine.movementDirection.getValue());
            if (this.getEngineBack() != null) {
               this.getEngineBack().onDirectionChanged(false);
            }
         }
      }
   }

   @Override
   public void initialize() {
      super.initialize();
      this.updateStacked();
      if (!this.hasSource() || this.getGeneratedSpeed() > this.getTheoreticalSpeed()) {
         this.updateGeneratedRotation();
      }
   }

   @Override
   public float calculateAddedStressCapacity() {
      if (this.getGeneratedSpeed() == 0.0F || !this.end) {
         return 0.0F;
      } else {
         return this.state.getValue(DieselGeneratorBlock.POWERED)
            ? 0.0F
            : FuelTypeManager.getGeneratedStress(this, this.tank.getPrimaryHandler().getFluid().getFluid()) / Math.abs(this.getGeneratedSpeed()) * this.stacked;
      }
   }

   @Override
   public float getGeneratedSpeed() {
      if (!this.end) {
         return 0.0F;
      } else {
         return this.state.getValue(DieselGeneratorBlock.POWERED)
            ? 0.0F
            : convertToDirection(
               (this.movementDirection.getValue() == 1 ? -1 : 1) * FuelTypeManager.getGeneratedSpeed(this, this.tank.getPrimaryHandler().getFluid().getFluid()),
               (Direction)this.getBlockState().getValue(LargeDieselGeneratorBlock.FACING)
            );
      }
   }

   public void updateStacked() {
      LargeDieselGeneratorBlockEntity engineForward = this.getEngineFor();
      LargeDieselGeneratorBlockEntity engineBack = this.getEngineBack();
      if (engineBack == null) {
         this.totalSize = 1;
         this.stacked = 1;
      } else {
         this.stacked = engineBack.stacked + 1;
      }

      if (engineForward == null) {
         this.totalSize = this.stacked;
         this.setEveryEnginesFront();
      } else {
         engineForward.updateStacked();
      }
   }

   public void setEveryEnginesFront() {
      LargeDieselGeneratorBlockEntity engineForward = this.getEngineFor();
      LargeDieselGeneratorBlockEntity engineBack = this.getEngineBack();
      if (engineForward == null) {
         this.frontEngine = new WeakReference<>(this);
      } else {
         this.frontEngine = engineForward.frontEngine;
         this.totalSize = engineForward.totalSize;
      }

      if (engineBack != null) {
         engineBack.setEveryEnginesFront();
      }
   }

   @Override
   public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
      boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
      LargeDieselGeneratorBlockEntity frontEngine = this.frontEngine.get();
      if (StressImpact.isEnabled() && frontEngine != null) {
         float stressBase = frontEngine.calculateAddedStressCapacity();
         if (Mth.equal(stressBase, 0.0F)) {
            return added;
         } else {
            if (frontEngine != this) {
               Lang.translate("gui.goggles.generator_stats").forGoggles(tooltip);
               Lang.translate("tooltip.capacityProvided").style(ChatFormatting.GRAY).forGoggles(tooltip);
               float stressTotal = Math.abs(frontEngine.getGeneratedSpeed() * stressBase);
               Lang.number(stressTotal)
                  .translate("generic.unit.stress")
                  .style(ChatFormatting.AQUA)
                  .space()
                  .add(Lang.translate("gui.goggles.at_current_speed").style(ChatFormatting.DARK_GRAY))
                  .forGoggles(tooltip, 1);
            }

            return this.containedFluidTooltip(tooltip, isPlayerSneaking, frontEngine.tank.getCapability().cast());
         }
      } else {
         return added;
      }
   }

   @Override
   public void tick() {
      super.tick();
      LargeDieselGeneratorBlockEntity engineForward = this.getEngineFor();
      this.state = this.getBlockState();
      this.end = engineForward == null;
      this.reActivateSource = true;
      LargeDieselGeneratorBlockEntity frontEngine = this.frontEngine.get();
      if (!this.tank.isEmpty() && engineForward != null && frontEngine != null) {
         frontEngine.tank.getPrimaryHandler().fill(this.tank.getPrimaryHandler().getFluid(), FluidAction.EXECUTE);
         this.tank.getPrimaryHandler().drain(this.tank.getPrimaryHandler().getFluid(), FluidAction.EXECUTE);
      }

      if ((Boolean)this.state.getValue(DieselGeneratorBlock.POWERED)) {
         this.validFuel = false;
      } else {
         this.validFuel = FuelTypeManager.getGeneratedSpeed(this, this.tank.getPrimaryHandler().getFluid().getFluid()) != 0.0F;
      }

      if (frontEngine == null
         || this.t <= FuelTypeManager.getSoundSpeed(frontEngine.tank.getPrimaryHandler().getFluid().getFluid())
         || !frontEngine.validFuel
         || (Boolean)this.state.getValue(DieselGeneratorBlock.SILENCED)
         || this.stacked % 6 != 0 && !this.end) {
         this.t++;
      } else {
         this.level
            .playLocalSound(
               this.worldPosition.getX(),
               this.worldPosition.getY(),
               this.worldPosition.getZ(),
               (SoundEvent)SoundRegistry.DIESEL_ENGINE_SOUND.get(),
               SoundSource.BLOCKS,
               0.5F,
               1.0F,
               false
            );
         this.t = 0;
      }

      this.partialSecond++;
      if (this.partialSecond >= 20) {
         this.partialSecond = 0;
         if (this.validFuel) {
            if (this.tank.getPrimaryHandler().getFluid().getAmount()
               >= FuelTypeManager.getBurnRate(this, this.tank.getPrimaryHandler().getFluid().getFluid()) * this.stacked) {
               this.tank
                  .getPrimaryHandler()
                  .setFluid(
                     FluidHelper.copyStackWithAmount(
                        this.tank.getPrimaryHandler().getFluid(),
                        this.tank.getPrimaryHandler().getFluid().getAmount()
                           - FuelTypeManager.getBurnRate(this, this.tank.getPrimaryHandler().getFluid().getFluid()) * this.stacked
                     )
                  );
            } else {
               this.tank.getPrimaryHandler().setFluid(FluidStack.EMPTY);
            }
         }
      }
   }

   public LargeDieselGeneratorBlockEntity getEngineFor() {
      LargeDieselGeneratorBlockEntity engine = this.forw.get();
      if (engine == null
         || engine.isRemoved()
         || engine.state.getValue(LargeDieselGeneratorBlock.FACING) == this.state.getValue(LargeDieselGeneratorBlock.FACING)) {
         if (engine != null) {
            this.forw = new WeakReference<>(null);
         }

         Direction facing = (Direction)this.state.getValue(LargeDieselGeneratorBlock.FACING);
         if (this.level.getBlockEntity(this.worldPosition.relative(facing.getAxis() == Axis.Z ? Direction.SOUTH : Direction.EAST)) instanceof LargeDieselGeneratorBlockEntity engineBE
            )
          {
            engine = engineBE;
            this.forw = new WeakReference<>(engineBE);
         }
      }

      if (engine != null
         && ((Direction)engine.state.getValue(LargeDieselGeneratorBlock.FACING)).getAxis()
            != ((Direction)this.state.getValue(LargeDieselGeneratorBlock.FACING)).getAxis()) {
         this.forw = new WeakReference<>(null);
         return null;
      } else {
         return engine;
      }
   }

   public LargeDieselGeneratorBlockEntity getEngineBack() {
      LargeDieselGeneratorBlockEntity engine = this.back.get();
      if (engine == null || engine.isRemoved()) {
         if (engine != null) {
            this.back = new WeakReference<>(null);
         }

         Direction facing = (Direction)this.state.getValue(LargeDieselGeneratorBlock.FACING);
         if (this.level.getBlockEntity(this.worldPosition.relative(facing.getAxis() == Axis.Z ? Direction.NORTH : Direction.WEST)) instanceof LargeDieselGeneratorBlockEntity engineBE
            )
          {
            engine = engineBE;
            this.back = new WeakReference<>(engineBE);
         }
      }

      if (engine != null
         && ((Direction)engine.state.getValue(LargeDieselGeneratorBlock.FACING)).getAxis()
            != ((Direction)this.state.getValue(LargeDieselGeneratorBlock.FACING)).getAxis()) {
         this.back = new WeakReference<>(null);
         return null;
      } else {
         return engine;
      }
   }
}
