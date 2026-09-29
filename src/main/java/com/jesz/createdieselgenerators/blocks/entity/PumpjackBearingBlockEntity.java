package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.PumpjackBearingBBlock;
import com.jesz.createdieselgenerators.blocks.PumpjackHeadBlock;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PumpjackBearingBlockEntity extends MechanicalBearingBlockEntity {
   public BlockPos bearingBPos = BlockPos.ZERO;
   public BlockPos crankPos = BlockPos.ZERO;
   public boolean isLarge;
   public float crankSpeed;
   public float crankAngle;
   private float prevAngle;

   public PumpjackBearingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   @Override
   protected void applyRotation() {
      this.movedContraption.setAngle(this.angle);
   }

   @Override
   public float getInterpolatedAngle(float partialTicks) {
      if (this.isVirtual()) {
         return Mth.lerp(partialTicks + 0.5F, this.prevAngle, this.angle);
      } else {
         if (this.movedContraption == null || this.movedContraption.isStalled() || !this.running) {
            partialTicks = 0.0F;
         }

         float angularSpeed = this.getAngularSpeed();
         if (this.sequencedAngleLimit >= 0.0) {
            angularSpeed = (float)Mth.clamp(angularSpeed, -this.sequencedAngleLimit, this.sequencedAngleLimit);
         }

         return Mth.lerp(partialTicks, this.angle, this.angle + angularSpeed);
      }
   }

   @Override
   public void assemble() {
      if (((Direction)this.getBlockState().getValue(DirectionalKineticBlock.FACING)).getAxis() != Axis.Y) {
         this.crankPos = BlockPos.ZERO;
         this.bearingBPos = BlockPos.ZERO;
         this.crankSpeed = 0.0F;
         this.angle = 0.0F;
         if (this.level.getBlockState(this.worldPosition).getBlock() instanceof BearingBlock) {
            Direction direction = (Direction)this.getBlockState().getValue(BearingBlock.FACING);
            BearingContraption contraption = new BearingContraption(false, direction);
            AtomicInteger hinges = new AtomicInteger(0);
            AtomicInteger heads = new AtomicInteger(0);
            AtomicInteger hingeDistance = new AtomicInteger(0);
            AtomicInteger hingeHeight = new AtomicInteger(0);

            try {
               contraption.searchMovedStructure(
                  this.level, this.getBlockPos().relative((Direction)this.getBlockState().getValue(DirectionalKineticBlock.FACING)), null
               );
            } catch (AssemblyException var8) {
               return;
            }

            contraption.getBlocks()
               .forEach(
                  (pos, info) -> {
                     if (info.state().getBlock() instanceof PumpjackHeadBlock) {
                        heads.set(heads.get() + 1);
                     } else if (info.state().getBlock() instanceof PumpjackBearingBBlock
                        && ((Direction)info.state().getValue(PumpjackBearingBBlock.FACING)).getAxis()
                           == ((Direction)this.getBlockState().getValue(DirectionalKineticBlock.FACING)).getClockWise().getAxis()) {
                        int f = Math.abs(pos.getZ());
                        if (((Direction)this.getBlockState().getValue(DirectionalKineticBlock.FACING)).getAxis() == Axis.Z) {
                           f = Math.abs(pos.getX());
                        }

                        hingeHeight.set(pos.getY());
                        hingeDistance.set(f);
                        hinges.set(hinges.get() + 1);
                     }
                  }
               );
            if (hinges.get() == 0) {
               this.lastException = new AssemblyException(Component.translatable("createdieselgenerators.gui.assembly.exception.bearing_missing"));
            } else if (hinges.get() > 1) {
               this.lastException = new AssemblyException(Component.translatable("createdieselgenerators.gui.assembly.exception.too_many_bearings"));
            } else if (heads.get() == 0) {
               this.lastException = new AssemblyException(Component.translatable("createdieselgenerators.gui.assembly.exception.head_missing"));
            } else if (heads.get() > 1) {
               this.lastException = new AssemblyException(Component.translatable("createdieselgenerators.gui.assembly.exception.too_many_heads"));
            } else if (hingeDistance.get() < 4) {
               this.lastException = new AssemblyException(Component.translatable("createdieselgenerators.gui.assembly.exception.arm_too_short"));
            } else if (hingeDistance.get() > 16) {
               this.lastException = new AssemblyException(Component.translatable("createdieselgenerators.gui.assembly.exception.arm_too_long"));
            } else {
               if (hingeHeight.get() == 0) {
                  super.assemble();
                  return;
               }

               this.lastException = new AssemblyException(Component.translatable("createdieselgenerators.gui.assembly.exception.back_bearing_not_centered"));
            }

            this.sendData();
         }
      }
   }

   @Override
   public void tick() {
      this.prevAngle = this.angle;
      if (this.level.isClientSide) {
         this.clientAngleDiff /= 2.0F;
      }

      if (!this.level.isClientSide && this.assembleNextTick) {
         this.assembleNextTick = false;
         if (this.running) {
            if (this.movedContraption == null || this.movedContraption.getContraption().getBlocks().isEmpty()) {
               if (this.movedContraption != null) {
                  this.movedContraption.getContraption().stop(this.level);
               }

               this.disassemble();
               return;
            }
         } else {
            this.assemble();
         }
      }

      if (this.running) {
         if (this.movedContraption == null || !this.movedContraption.isStalled()) {
            int f = Math.abs(this.bearingBPos.getZ());
            if (((Direction)this.getBlockState().getValue(DirectionalKineticBlock.FACING)).getAxis() == Axis.Z) {
               f = Math.abs(this.bearingBPos.getX());
            }

            float b = 0.0F;
            if (f == 4) {
               b = 13.0F;
            }

            if (f == 5) {
               b = 10.0F;
            }

            if (f == 6) {
               b = 8.2F;
            }

            if (f == 7) {
               b = 7.0F;
            }

            if (f == 8) {
               b = 6.0F;
            }

            if (f == 9) {
               b = 5.3F;
            }

            if (f == 10) {
               b = 4.9F;
            }

            if (f == 11) {
               b = 4.4F;
            }

            if (f == 12) {
               b = 4.0F;
            }

            if (f == 13) {
               b = 3.7F;
            }

            if (f == 14) {
               b = 3.4F;
            }

            if (f == 15) {
               b = 3.2F;
            }

            if (f == 16) {
               b = 3.0F;
            }

            float[] angleAnimation = new float[]{0.0F, 70.0F, 130.0F, 180.0F, 220.0F, 255.0F, 280.0F, 300.0F, 260.0F, 199.0F, 127.0F, 67.0F};
            if (this.isLarge) {
               angleAnimation = new float[]{
                  0.0F,
                  27.0F,
                  60.0F,
                  90.0F,
                  120.0F,
                  145.0F,
                  166.0F,
                  189.0F,
                  205.0F,
                  220.0F,
                  240.0F,
                  260.0F,
                  280.0F,
                  305.0F,
                  330.0F,
                  310.0F,
                  290.0F,
                  245.0F,
                  200.0F,
                  163.0F,
                  127.0F,
                  93.0F,
                  60.0F,
                  30.0F
               };
            }

            int lIndex = (int)Math.abs(Math.floor(this.crankAngle / (360.0 / angleAnimation.length)) % angleAnimation.length);
            float partialAngle = (float)Math.abs(Math.abs(this.crankAngle / (360.0 / angleAnimation.length)) - lIndex);
            float newCrankAngle;
            if ((((Direction)this.getBlockState().getValue(DirectionalKineticBlock.FACING)).getAxis() != Axis.Z || this.bearingBPos.getX() >= 0)
               && (((Direction)this.getBlockState().getValue(DirectionalKineticBlock.FACING)).getAxis() != Axis.X || this.bearingBPos.getZ() <= 0)) {
               newCrankAngle = AngleHelper.angleLerp(partialAngle, angleAnimation[lIndex], angleAnimation[(lIndex + 1) % angleAnimation.length]);
            } else {
               newCrankAngle = AngleHelper.angleLerp(
                  partialAngle,
                  angleAnimation[(angleAnimation.length - lIndex) % angleAnimation.length],
                  angleAnimation[(angleAnimation.length - (lIndex + 1)) % angleAnimation.length]
               );
            }

            float a = (float)Math.pow(Math.sin(newCrankAngle / (360.0 / angleAnimation.length) / Math.PI / (this.isLarge ? 4.4 : 2.2)), 2.0) * 2.0F + 1.0F;
            if (Math.abs(newCrankAngle) >= 359.5) {
               a = 0.99F;
            }

            this.angle = (a * b - b)
               * (
                  (((Direction)this.getBlockState().getValue(DirectionalKineticBlock.FACING)).getAxis() == Axis.Z ? -1 : 1)
                        * (
                           (
                                    ((Direction)this.getBlockState().getValue(DirectionalKineticBlock.FACING)).getAxis() == Axis.Z
                                       ? this.bearingBPos.getX()
                                       : this.bearingBPos.getZ()
                                 )
                                 < 0
                              ? -1
                              : 1
                        )
                     * (this.isLarge ? 1.5F : 1.0F)
               );
         }

         if (this.movedContraption != null) {
            this.applyRotation();
         }
      }
   }

   public boolean isStalled() {
      return this.movedContraption == null ? false : this.movedContraption.isStalled();
   }

   @Override
   public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
      return false;
   }

   @Override
   public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
   }

   @Override
   public void attach(ControlledContraptionEntity contraption) {
      BlockState blockState = this.getBlockState();
      if (contraption.getContraption() instanceof BearingContraption) {
         if (blockState.hasProperty(DirectionalKineticBlock.FACING)) {
            this.movedContraption = contraption;
            this.setChanged();
            BlockPos anchor = this.worldPosition.relative((Direction)blockState.getValue(DirectionalKineticBlock.FACING));
            this.movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
            if (!this.level.isClientSide) {
               this.running = true;
               this.sendData();
            }
         }
      }
   }

   public void assembleNextTick() {
      this.assembleNextTick = true;
   }
}
