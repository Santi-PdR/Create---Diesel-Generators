package com.jesz.createdieselgenerators.other;

import com.jesz.createdieselgenerators.blocks.entity.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.LargeDieselGeneratorBlockEntity;
import com.simibubi.create.foundation.utility.Couple;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CDGFuelType {
   float normalSpeed;
   float modularSpeed;
   float hugeSpeed;
   float normalStrength;
   float modularStrength;
   float hugeStrength;
   int normalBurn;
   int modularBurn;
   int hugeBurn;
   int soundSpeed;

   public CDGFuelType(
      float normalSpeed,
      float normalStrength,
      int normalBurn,
      float modularSpeed,
      float modularStrength,
      int modularBurn,
      float hugeSpeed,
      float hugeStrength,
      int hugeBurn,
      int soundSpeed
   ) {
      this.normalSpeed = normalSpeed;
      this.modularSpeed = modularSpeed;
      this.hugeSpeed = hugeSpeed;
      this.normalStrength = normalStrength;
      this.modularStrength = modularStrength;
      this.hugeStrength = hugeStrength;
      this.normalBurn = normalBurn;
      this.modularBurn = modularBurn;
      this.hugeBurn = hugeBurn;
      this.soundSpeed = soundSpeed;
   }

   public Couple<Float> getGenerated(BlockEntity be) {
      if (be instanceof HugeDieselEngineBlockEntity) {
         return this.getGeneratedHuge();
      } else {
         return be instanceof LargeDieselGeneratorBlockEntity ? this.getGeneratedModular() : this.getGeneratedNormal();
      }
   }

   public Couple<Float> getGeneratedNormal() {
      return Couple.create(this.normalSpeed, this.normalStrength);
   }

   public Couple<Float> getGeneratedModular() {
      return Couple.create(this.modularSpeed, this.modularStrength);
   }

   public Couple<Float> getGeneratedHuge() {
      return Couple.create(this.hugeSpeed, this.hugeStrength);
   }

   public int getBurn(BlockEntity be) {
      if (be instanceof HugeDieselEngineBlockEntity) {
         return this.getBurnHuge();
      } else {
         return be instanceof LargeDieselGeneratorBlockEntity ? this.getBurnModular() : this.getBurnNormal();
      }
   }

   public int getBurnNormal() {
      return this.normalBurn;
   }

   public int getBurnModular() {
      return this.modularBurn;
   }

   public int getBurnHuge() {
      return this.hugeBurn;
   }

   public int getSoundSpeed() {
      return this.soundSpeed;
   }
}
