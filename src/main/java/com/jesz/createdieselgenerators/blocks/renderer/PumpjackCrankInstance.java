package com.jesz.createdieselgenerators.blocks.renderer;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackBearingBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackCrankBlockEntity;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.materials.FlatLit;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityInstance;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec2;

public class PumpjackCrankInstance extends KineticBlockEntityInstance<PumpjackCrankBlockEntity> implements DynamicInstance {
   protected final ModelData crank = this.getTransformMaterial().getModel(PartialModels.PUMPJACK_CRANK_SMALL).createInstance();
   protected final ModelData crank_rod = this.getTransformMaterial().getModel(PartialModels.PUMPJACK_CRANK_ROD_SMALL).createInstance();
   protected final ModelData large_crank = this.getTransformMaterial().getModel(PartialModels.PUMPJACK_CRANK_LARGE).createInstance();
   protected final ModelData large_crank_rod = this.getTransformMaterial().getModel(PartialModels.PUMPJACK_CRANK_ROD_LARGE).createInstance();
   protected final RotatingData shaft = this.setup(this.getRotatingMaterial().getModel(this.shaft()).createInstance());

   public PumpjackCrankInstance(MaterialManager materialManager, PumpjackCrankBlockEntity blockEntity) {
      super(materialManager, blockEntity);
   }

   @Override
   public void beginFrame() {
      float partialTicks = AnimationTickHolder.getPartialTicks() * 0.0F;
      float angle = AngleHelper.angleLerp(partialTicks, this.blockEntity.prevAngle, this.blockEntity.angle);
      PoseStack ms = new PoseStack();
      TransformStack msr = TransformStack.cast(ms);
      msr.translate(this.getInstancePosition());
      boolean isXAxis = ((Direction)this.blockState.getValue(HorizontalKineticBlock.HORIZONTAL_FACING)).getAxis() == Axis.X;
      double v = ((isXAxis ? angle : -angle) + 90.0F) / 180.0F * Math.PI;
      double sin = Math.sin(v) * (this.blockEntity.crankSize.getValue() == 0 ? 0.8125 : 1.125);
      double cos = Math.cos(v) * (this.blockEntity.crankSize.getValue() == 0 ? 0.8125 : 1.125);
      if (this.blockEntity.bearingPos == null) {
         if (isXAxis) {
            msr.translate(0.5, 1.25, 0.0).rotateZ(angle);
         } else {
            msr.translate(0.0, 1.25, 0.5).rotateY(90.0).rotateZ(angle);
         }

         (this.blockEntity.crankSize.getValue() == 0 ? this.crank : this.large_crank).setTransform(ms);
         (this.blockEntity.crankSize.getValue() == 0 ? this.large_crank : this.crank).setEmptyTransform();
         double dstY = -1000.0 - sin - 1.25 - this.pos.getY();
         double dstX = this.pos.getX() - cos - 0.5 - this.pos.getX();
         double dstZ = this.pos.getZ() - cos - 0.5 - this.pos.getZ();
         ms = new PoseStack();
         msr = TransformStack.cast(ms);
         msr.translate(this.getInstancePosition());
         if (isXAxis) {
            msr.translate(0.5, 1.25, 0.0).translate(cos, sin, 0.0).rotateZ(Math.atan2(dstY, dstX) * 180.0 / Math.PI - 90.0);
         } else {
            msr.translate(0.0, 1.25, 0.5).translate(0.0, sin, cos).rotateY(90.0).rotateZ(Math.atan2(dstZ, dstY) * 180.0 / Math.PI);
         }

         (this.blockEntity.crankSize.getValue() == 0 ? this.crank_rod : this.large_crank_rod).setTransform(ms);
         (this.blockEntity.crankSize.getValue() == 0 ? this.large_crank_rod : this.crank_rod).setEmptyTransform();
      } else {
         PumpjackBearingBlockEntity bearing = this.blockEntity.bearing.get();
         float interpolatedAngle = 0.0F;
         if (bearing != null) {
            interpolatedAngle = bearing.getInterpolatedAngle(partialTicks);
         }

         if (!isXAxis) {
            interpolatedAngle *= -1.0F;
         }

         Vec2 crankBearingLocation = new Vec2(
            (float)(
                  this.blockEntity.crankBearingLocation.x * Math.cos(interpolatedAngle / 180.0F * Math.PI)
                     - this.blockEntity.crankBearingLocation.y * Math.sin(interpolatedAngle / 180.0F * Math.PI)
               )
               + 0.5F,
            (float)(
                  this.blockEntity.crankBearingLocation.x * Math.sin(interpolatedAngle / 180.0F * Math.PI)
                     + this.blockEntity.crankBearingLocation.y * Math.cos(interpolatedAngle / 180.0F * Math.PI)
               )
               + 0.5F
         );
         if (isXAxis) {
            crankBearingLocation = crankBearingLocation.add(new Vec2(this.blockEntity.bearingPos.getX(), this.blockEntity.bearingPos.getY()));
         } else {
            crankBearingLocation = crankBearingLocation.add(new Vec2(this.blockEntity.bearingPos.getZ(), this.blockEntity.bearingPos.getY()));
         }

         if (isXAxis) {
            msr.translate(0.5, 1.25, 0.0).rotateZ(angle);
         } else {
            msr.translate(0.0, 1.25, 0.5).rotateY(90.0).rotateZ(angle);
         }

         (this.blockEntity.crankSize.getValue() == 0 ? this.crank : this.large_crank).setTransform(ms);
         (this.blockEntity.crankSize.getValue() == 0 ? this.large_crank : this.crank).setEmptyTransform();
         ms = new PoseStack();
         msr = TransformStack.cast(ms);
         msr.translate(this.getInstancePosition());
         double dstY = crankBearingLocation.y - sin - 1.25 - this.pos.getY();
         double dstX = crankBearingLocation.x - cos - 0.5 - this.pos.getX();
         double dstZ = crankBearingLocation.x - cos - 0.5 - this.pos.getZ();
         if (isXAxis) {
            msr.translate(0.5, 1.25, 0.0).translate(cos, sin, 0.0).rotateZ(Math.atan2(dstY, dstX) * 180.0 / Math.PI - 90.0);
         } else {
            msr.translate(0.0, 1.25, 0.5).translate(0.0, sin, cos).rotateY(90.0).rotateZ(Math.atan2(dstZ, dstY) * 180.0 / Math.PI);
         }

         (this.blockEntity.crankSize.getValue() == 0 ? this.crank_rod : this.large_crank_rod).setTransform(ms);
         (this.blockEntity.crankSize.getValue() == 0 ? this.large_crank_rod : this.crank_rod).setEmptyTransform();
      }
   }

   @Override
   public void update() {
      this.updateRotation(this.shaft);
   }

   @Override
   public void updateLight() {
      this.relight(this.pos, new FlatLit[]{this.shaft, this.crank, this.crank_rod, this.large_crank_rod, this.large_crank});
   }

   @Override
   public void remove() {
      this.shaft.delete();
      this.crank.delete();
      this.crank_rod.delete();
      this.large_crank_rod.delete();
      this.large_crank.delete();
   }
}
