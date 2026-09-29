package com.jesz.createdieselgenerators.blocks.entity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import java.lang.ref.WeakReference;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PumpjackCrankBlockEntity extends KineticBlockEntity {
   public float angle = 0.0F;
   public float prevAngle = 0.0F;
   public float bearingAngle;
   public float prevBearingAngle;
   public BlockPos bearingPos;
   public WeakReference<PumpjackBearingBlockEntity> bearing = new WeakReference<>(null);
   public Vec3 crankBearingLocation = new Vec3(0.0, -100.0, 0.0);
   public ScrollOptionBehaviour<PumpjackCrankBlockEntity.CrankSize> crankSize;

   public PumpjackCrankBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
      super(typeIn, pos, state);
   }

   @Override
   protected void read(CompoundTag compound, boolean clientPacket) {
      this.angle = compound.getFloat("Angle");
      this.crankBearingLocation = new Vec3(compound.getDouble("BackPosX"), compound.getDouble("BackPosY"), compound.getDouble("BackPosZ"));
      super.read(compound, clientPacket);
   }

   @Override
   public void handleUpdateTag(CompoundTag compound) {
      this.angle = compound.getFloat("Angle");
      this.crankBearingLocation = new Vec3(compound.getDouble("BackPosX"), compound.getDouble("BackPosY"), compound.getDouble("BackPosZ"));
      super.handleUpdateTag(compound);
   }

   @Override
   public CompoundTag getUpdateTag() {
      CompoundTag compound = super.getUpdateTag();
      compound.putFloat("Angle", this.angle);
      compound.putDouble("BackPosX", this.crankBearingLocation.x);
      compound.putDouble("BackPosY", this.crankBearingLocation.y);
      compound.putDouble("BackPosZ", this.crankBearingLocation.z);
      return compound;
   }

   @Override
   protected void write(CompoundTag compound, boolean clientPacket) {
      compound.putFloat("Angle", this.angle);
      compound.putDouble("BackPosX", this.crankBearingLocation.x);
      compound.putDouble("BackPosY", this.crankBearingLocation.y);
      compound.putDouble("BackPosZ", this.crankBearingLocation.z);
      super.write(compound, clientPacket);
   }

   @Override
   public float calculateStressApplied() {
      float impact = 16.0F;
      this.lastStressApplied = impact;
      return impact;
   }

   public PumpjackBearingBlockEntity getBearing() {
      if (this.bearing.get() != null) {
         if (!this.bearing.get().isRemoved() && this.bearing.get().isRunning()) {
            return this.bearing.get();
         } else {
            this.bearing = new WeakReference<>(null);
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   protected AABB createRenderBoundingBox() {
      return super.createRenderBoundingBox().inflate(3.0);
   }

   @Override
   public void tick() {
      super.tick();
      PumpjackBearingBlockEntity bearing = this.getBearing();
      if (bearing == null || !bearing.isStalled()) {
         this.prevAngle = this.angle;
         if (this.angle >= 359.0F || this.angle <= -359.0F) {
            this.angle = 0.0F;
         }

         if (this.getSpeed() != 0.0F) {
            this.angle = this.angle + Mth.clamp(Math.abs(this.getSpeed()), 0.0F, 64.0F) / 10.0F;
         }
      }
   }

   @Override
   public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
      this.crankSize = new ScrollOptionBehaviour<>(
         PumpjackCrankBlockEntity.CrankSize.class, Component.translatable("createdieselgenerators.pumpjack_crank.crank_size"), this, new PumpjackCrankValueBox()
      );
      this.crankSize.withCallback($ -> this.onSizeChanged());
      behaviours.add(this.crankSize);
      super.addBehaviours(behaviours);
   }

   private void onSizeChanged() {
   }

   public static enum CrankSize implements INamedIconOptions {
      NORMAL(AllIcons.I_CLEAR),
      LARGE(AllIcons.I_PLACE);

      private final AllIcons icon;

      private CrankSize(AllIcons icon) {
         this.icon = icon;
      }

      @Override
      public AllIcons getIcon() {
         return this.icon;
      }

      @Override
      public String getTranslationKey() {
         return "tooltip.capacityProvided." + (this.icon == AllIcons.I_CLEAR ? "low" : "high");
      }
   }
}
