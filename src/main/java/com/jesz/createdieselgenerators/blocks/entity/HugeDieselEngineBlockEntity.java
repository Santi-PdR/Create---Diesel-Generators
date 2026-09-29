package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock;
import com.jesz.createdieselgenerators.blocks.HugeDieselEngineBlock;
import com.jesz.createdieselgenerators.blocks.PoweredEngineShaftBlock;
import com.jesz.createdieselgenerators.compat.computercraft.CCProxy;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.jesz.createdieselgenerators.sounds.SoundRegistry;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate.StressImpact;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
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
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;

public class HugeDieselEngineBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
   public WeakReference<PoweredEngineShaftBlockEntity> target = new WeakReference<>(null);
   public SmartFluidTankBehaviour tank;
   int partialSecond;
   boolean validFuel;
   float oldAngle = 0.0F;
   public ScrollOptionBehaviour<RotationDirection> movementDirection;
   public AbstractComputerBehaviour computerBehaviour;

   public HugeDieselEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   @Override
   protected void write(CompoundTag tag, boolean clientPacket) {
      super.write(tag, clientPacket);
      this.tank.write(tag, clientPacket);
      tag.putInt("PartialSecond", this.partialSecond);
   }

   @Override
   protected void read(CompoundTag tag, boolean clientPacket) {
      super.read(tag, clientPacket);
      this.tank.read(tag, clientPacket);
      this.partialSecond = tag.getInt("PartialSecond");
   }

   @Override
   protected AABB createRenderBoundingBox() {
      return super.createRenderBoundingBox().inflate(2.0);
   }

   @Override
   public void tick() {
      super.tick();
      PoweredEngineShaftBlockEntity shaft = this.getShaft();
      if (shaft != null) {
         if ((Boolean)this.getBlockState().getValue(DieselGeneratorBlock.POWERED)) {
            this.validFuel = false;
         } else {
            this.validFuel = FuelTypeManager.getGeneratedSpeed(this, this.tank.getPrimaryHandler().getFluid().getFluid()) != 0.0F;
         }

         this.partialSecond++;
         if (this.partialSecond >= 20) {
            this.partialSecond = 0;
            if (this.validFuel) {
               if (this.tank.getPrimaryHandler().getFluid().getAmount()
                  >= FuelTypeManager.getBurnRate(this, this.tank.getPrimaryHandler().getFluid().getFluid())) {
                  this.tank
                     .getPrimaryHandler()
                     .setFluid(
                        FluidHelper.copyStackWithAmount(
                           this.tank.getPrimaryHandler().getFluid(),
                           this.tank.getPrimaryHandler().getFluid().getAmount()
                              - FuelTypeManager.getBurnRate(this, this.tank.getPrimaryHandler().getFluid().getFluid())
                        )
                     );
               } else {
                  this.tank.getPrimaryHandler().setFluid(FluidStack.EMPTY);
               }
            }
         }

         if (this.validFuel) {
            if (shaft.movementDirection != 0 && shaft.movementDirection != (this.movementDirection.get() == RotationDirection.CLOCKWISE ? 1 : -1)) {
               shaft.removeGenerator(this.worldPosition);
               this.onDirectionChanged();
               return;
            }

            shaft.update(
               this.worldPosition,
               this.movementDirection.get() == RotationDirection.CLOCKWISE ? 1 : -1,
               FuelTypeManager.getGeneratedStress(this, this.tank.getPrimaryHandler().getFluid().getFluid()),
               FuelTypeManager.getGeneratedSpeed(this, this.tank.getPrimaryHandler().getFluid().getFluid())
            );
            if (!this.level.isClientSide) {
               return;
            }

            Float angle = this.getTargetAngle();
            if (angle == null) {
               return;
            }

            angle = (float)(angle * 180.0F / Math.PI);
            angle = angle < 0.0F ? 360.0F - angle : angle;
            Direction facing = (Direction)this.getBlockState().getValue(HugeDieselEngineBlock.FACING);
            float shaftR = facing == Direction.NORTH
               ? 180.0F
               : (
                  facing == Direction.SOUTH
                     ? 0.0F
                     : (facing == Direction.EAST ? 0.0F : (facing == Direction.WEST ? 180.0F : (facing == Direction.DOWN ? 90.0F : -90.0F)))
               );
            if ((this.oldAngle + shaftR) % 360.0F > (angle + shaftR) % 360.0F) {
               this.level
                  .playLocalSound(
                     this.worldPosition.getX(),
                     this.worldPosition.getY(),
                     this.worldPosition.getZ(),
                     (SoundEvent)SoundRegistry.DIESEL_ENGINE_SOUND.get(),
                     SoundSource.BLOCKS,
                     1.0F,
                     1.0F,
                     false
                  );
            }

            this.oldAngle = angle;
         } else {
            shaft.removeGenerator(this.worldPosition);
         }
      }
   }

   public PoweredEngineShaftBlockEntity getShaft() {
      PoweredEngineShaftBlockEntity shaft = this.target.get();
      if (shaft == null || shaft.isRemoved() || !shaft.canBePoweredBy(this.worldPosition)) {
         if (shaft != null) {
            this.target = new WeakReference<>(null);
         }

         BlockEntity anyShaftAt = this.level.getBlockEntity(this.worldPosition.relative((Direction)this.getBlockState().getValue(HugeDieselEngineBlock.FACING), 2));
         BlockState sState = this.level.getBlockState(this.worldPosition.relative((Direction)this.getBlockState().getValue(HugeDieselEngineBlock.FACING), 2));
         if (anyShaftAt instanceof PoweredEngineShaftBlockEntity ps) {
            shaft = ps;
            this.target = new WeakReference<>(ps);
         } else if (sState.getBlock() instanceof ShaftBlock
            && sState.getValue(RotatedPillarKineticBlock.AXIS) != ((Direction)this.getBlockState().getValue(HugeDieselEngineBlock.FACING)).getAxis()) {
            this.level
               .setBlock(
                  this.worldPosition.relative((Direction)this.getBlockState().getValue(HugeDieselEngineBlock.FACING), 2),
                  PoweredEngineShaftBlock.getEquivalent(
                     this.level.getBlockState(this.worldPosition.relative((Direction)this.getBlockState().getValue(HugeDieselEngineBlock.FACING), 2))
                  ),
                  3
               );
         }
      }

      return shaft;
   }

   @Override
   public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
      behaviours.add(this.computerBehaviour = CCProxy.behaviour(this));
      this.movementDirection = new ScrollOptionBehaviour<>(
         RotationDirection.class, Lang.translateDirect("contraptions.windmill.rotation_direction"), this, new HugeDieselEngineValueBox()
      );
      this.movementDirection.withCallback($ -> this.onDirectionChanged());
      behaviours.add(this.movementDirection);
      this.tank = SmartFluidTankBehaviour.single(this, 100);
      behaviours.add(this.tank);
   }

   private void onDirectionChanged() {
      PoweredEngineShaftBlockEntity shaft = this.getShaft();
      if (shaft != null) {
         shaft.engines.forEach((p, s) -> {
            if (this.level.getBlockEntity(p) instanceof HugeDieselEngineBlockEntity be) {
               be.movementDirection.setValue(this.movementDirection.getValue());
            }
         });
      }
   }

   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      if (side == null) {
         return this.tank.getCapability().cast();
      } else {
         return cap == ForgeCapabilities.FLUID_HANDLER
               && this.getBlockState().getValue(BooleanProperty.create(side.toString()))
               && side.getAxis() != ((Direction)this.getBlockState().getValue(HugeDieselEngineBlock.FACING)).getAxis()
            ? this.tank.getCapability().cast()
            : super.getCapability(cap, side);
      }
   }

   @Override
   public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
      if (StressImpact.isEnabled() && this.validFuel) {
         PoweredEngineShaftBlockEntity shaft = this.getShaft();
         if (shaft == null) {
            return false;
         } else {
            float stressBase = FuelTypeManager.getGeneratedStress(this, this.tank.getPrimaryHandler().getFluid().getFluid());
            if (Mth.equal(stressBase, 0.0F)) {
               return false;
            } else {
               Lang.translate("gui.goggles.generator_stats").forGoggles(tooltip);
               Lang.translate("tooltip.capacityProvided").style(ChatFormatting.GRAY).forGoggles(tooltip);
               float stressTotal = Math.abs(stressBase);
               Lang.number(stressTotal)
                  .translate("generic.unit.stress")
                  .style(ChatFormatting.AQUA)
                  .space()
                  .add(Lang.translate("gui.goggles.at_current_speed").style(ChatFormatting.DARK_GRAY))
                  .forGoggles(tooltip, 1);
               return this.containedFluidTooltip(tooltip, isPlayerSneaking, this.tank.getCapability().cast());
            }
         }
      } else {
         return false;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public Float getTargetAngle() {
      BlockState state = this.getBlockState();
      if (!BlockRegistry.HUGE_DIESEL_ENGINE.has(state)) {
         return null;
      } else {
         Direction facing = (Direction)state.getValue(HugeDieselEngineBlock.FACING);
         PoweredEngineShaftBlockEntity shaft = this.getShaft();
         Axis facingAxis = facing.getAxis();
         if (shaft == null) {
            return null;
         } else {
            Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
            float angle = KineticBlockEntityRenderer.getAngleForTe(shaft, shaft.getBlockPos(), axis);
            if (axis == facingAxis) {
               return null;
            } else {
               if (axis.isHorizontal() && facingAxis == Axis.X ^ facing.getAxisDirection() == AxisDirection.POSITIVE) {
                  angle *= -1.0F;
               }

               if (axis == Axis.X && facing == Direction.DOWN) {
                  angle *= -1.0F;
               }

               return angle;
            }
         }
      }
   }
}
