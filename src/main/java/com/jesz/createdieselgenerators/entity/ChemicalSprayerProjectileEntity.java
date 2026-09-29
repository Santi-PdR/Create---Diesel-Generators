package com.jesz.createdieselgenerators.entity;

import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.FluidFX;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.common.Tags.Fluids;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidStack;

public class ChemicalSprayerProjectileEntity extends AbstractHurtingProjectile {
   public FluidStack stack;
   public boolean fire;
   public boolean cooling;
   int t = 0;
   static final EntityDataAccessor<CompoundTag> DATA = SynchedEntityData.defineId(ChemicalSprayerProjectileEntity.class, EntityDataSerializers.COMPOUND_TAG);

   protected ChemicalSprayerProjectileEntity(EntityType<? extends AbstractHurtingProjectile> type, Level level) {
      super(type, level);
   }

   public static ChemicalSprayerProjectileEntity spray(Level level, FluidStack stack, boolean fire, boolean cooling) {
      ChemicalSprayerProjectileEntity projectile = new ChemicalSprayerProjectileEntity(
         (EntityType<? extends AbstractHurtingProjectile>)EntityRegistry.CHEMICAL_SPRAYER_PROJECTILE.get(), level
      );
      projectile.stack = stack;
      projectile.fire = fire;
      projectile.cooling = cooling;
      CompoundTag tag = new CompoundTag();
      tag.putBoolean("Fire", fire);
      tag.putBoolean("Cooling", cooling);
      tag.put("FluidStack", new CompoundTag());
      stack.writeToNBT(tag.getCompound("FluidStack"));
      projectile.getEntityData().set(DATA, tag);
      return projectile;
   }

   protected void onHitEntity(EntityHitResult hit) {
      if (this.fire) {
         hit.getEntity().setSecondsOnFire(hit.getEntity().getRemainingFireTicks() / 20 + 10);
      } else if (this.cooling) {
         hit.getEntity().clearFire();
      }

      if (this.stack.getFluid().isSame((Fluid)AllFluids.POTION.get()) && hit.getEntity() instanceof LivingEntity le && le.isAffectedByPotions()) {
         for (MobEffectInstance effectInstance : PotionUtils.getMobEffects(PotionFluidHandler.fillBottle(new ItemStack(Items.GLASS_BOTTLE), this.stack))) {
            MobEffect effect = effectInstance.getEffect();
            if (effect.isInstantenous()) {
               effect.applyInstantenousEffect(null, null, le, effectInstance.getAmplifier(), 0.5);
            } else {
               le.addEffect(new MobEffectInstance(effectInstance));
            }
         }
      }

      if (FluidHelper.isTag(this.stack, Fluids.MILK) && hit.getEntity() instanceof LivingEntity le && le.isAffectedByPotions()) {
         ItemStack curativeItem = new ItemStack(Items.MILK_BUCKET);
         le.curePotionEffects(curativeItem);
      }

      super.onHitEntity(hit);
      this.remove(RemovalReason.DISCARDED);
   }

   public void load(CompoundTag compound) {
      if (this.stack == null) {
         this.stack = FluidStack.loadFluidStackFromNBT(compound.getCompound("FluidStack"));
      }

      super.load(compound);
   }

   public CompoundTag saveWithoutId(CompoundTag compound) {
      if (this.stack != null) {
         this.stack.writeToNBT(compound.getCompound("FluidStack"));
      }

      return super.saveWithoutId(compound);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      CompoundTag tag = new CompoundTag();
      tag.putBoolean("Fire", this.fire);
      tag.putBoolean("Cooling", this.cooling);
      tag.put("FluidStack", new CompoundTag());
      FluidStack.EMPTY.writeToNBT(tag.getCompound("FluidStack"));
      this.entityData.define(DATA, tag);
   }

   public void tick() {
      if (this.level().isClientSide) {
         this.stack = FluidStack.loadFluidStackFromNBT(((CompoundTag)this.getEntityData().get(DATA)).getCompound("FluidStack"));
         this.fire = ((CompoundTag)this.getEntityData().get(DATA)).getBoolean("Fire");
         this.cooling = ((CompoundTag)this.getEntityData().get(DATA)).getBoolean("Cooling");
         if (this.t >= 1) {
            if (this.fire) {
               this.level().addParticle(ParticleTypes.LAVA, this.position().x, this.position().y, this.position().z, 0.0, -0.1, 0.0);
            }

            if (this.stack != null && !this.stack.isEmpty()) {
               this.level()
                  .addParticle(FluidFX.getFluidParticle(this.stack), this.position().x, this.position().y, this.position().z, 0.0, -0.1, 0.0);
            }

            this.t = 0;
         } else {
            this.t++;
         }
      }

      this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.015, 0.0));
      if (this.fire) {
         if (FuelTypeManager.getGeneratedSpeed(
                  this.level()
                     .getFluidState(new BlockPos((int)this.getPosition(1.0F).x, (int)this.getPosition(1.0F).y, (int)this.getPosition(1.0F).z))
                     .getType()
               )
               != 0.0F
            && (Boolean)ConfigRegistry.COMBUSTIBLES_BLOW_UP.get()) {
            this.level().explode(null, this.getX(), this.getY(), this.getZ(), 3.0F, ExplosionInteraction.BLOCK);
         } else if (this.level()
               .getFluidState(new BlockPos((int)this.getPosition(1.0F).x, (int)this.getPosition(1.0F).y, (int)this.getPosition(1.0F).z))
               .is(net.minecraft.world.level.material.Fluids.FLOWING_WATER)
            || this.level()
               .getFluidState(new BlockPos((int)this.getPosition(1.0F).x, (int)this.getPosition(1.0F).y, (int)this.getPosition(1.0F).z))
               .is(net.minecraft.world.level.material.Fluids.WATER)) {
            this.fire = false;
            if (this.stack.getFluid().isSame(net.minecraft.world.level.material.Fluids.LAVA)) {
               this.remove(RemovalReason.DISCARDED);
            }

            ((CompoundTag)this.getEntityData().get(DATA)).putBoolean("Fire", false);
         }
      }

      Entity entity = this.getOwner();
      if (this.level().isClientSide || (entity == null || !entity.isRemoved()) && this.level().hasChunkAt(this.blockPosition())) {
         if (this.shouldBurn()) {
            this.setSecondsOnFire(1);
         }

         HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, x$0 -> this.canHitEntity(x$0));
         if (hitresult.getType() != Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hitresult)) {
            this.onHit(hitresult);
         }

         this.checkInsideBlocks();
         Vec3 vec3 = this.getDeltaMovement();
         double d0 = this.getX() + vec3.x;
         double d1 = this.getY() + vec3.y;
         double d2 = this.getZ() + vec3.z;
         ProjectileUtil.rotateTowardsMovement(this, 0.2F);
         float f = this.getInertia();
         if (this.isInWater()) {
            for (int i = 0; i < 4; i++) {
               float f1 = 0.25F;
               this.level()
                  .addParticle(
                     ParticleTypes.BUBBLE,
                     d0 - vec3.x * 0.25,
                     d1 - vec3.y * 0.25,
                     d2 - vec3.z * 0.25,
                     vec3.x,
                     vec3.y,
                     vec3.z
                  );
            }

            f = 0.8F;
         }

         this.setDeltaMovement(vec3.add(this.xPower, this.yPower, this.zPower).scale(f));
         this.level().addParticle(this.getTrailParticle(), d0, d1 + 0.5, d2, 0.0, 0.0, 0.0);
         this.setPos(d0, d1, d2);
      } else {
         this.discard();
      }
   }

   public boolean isOnFire() {
      return false;
   }

   protected void onHitBlock(BlockHitResult hit) {
      super.onHitBlock(hit);
      BlockPos pos = new BlockPos((int)this.getPosition(1.0F).x, (int)this.getPosition(1.0F).y, (int)this.getPosition(1.0F).z);
      if (this.cooling) {
         if (this.level().getBlockState(pos).getBlock() instanceof FireBlock) {
            this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            this.level()
               .playLocalSound(
                  this.position().x, this.position().y, this.position().z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.0F, true
               );
         }

         for (int i = 0; i < 6; i++) {
            if (this.level().getBlockState(pos.relative(Direction.values()[i], 1)).getBlock() instanceof FireBlock) {
               this.level().setBlock(pos.relative(Direction.values()[i], 1), Blocks.AIR.defaultBlockState(), 3);
               this.level()
                  .playLocalSound(
                     this.position().x, this.position().y, this.position().z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.0F, true
                  );
            }
         }
      }

      if (this.fire
         && this.level().getBlockState(pos).getBlock() instanceof AirBlock
         && BlockHelper.hasBlockSolidSide(this.level().getBlockState(pos.below()), this.level(), pos.below(), Direction.UP)) {
         this.level().setBlock(pos, FireBlock.getState(this.level(), pos), 3);
      }

      this.remove(RemovalReason.DISCARDED);
   }

   public static Builder<?> build(Builder<?> builder) {
      return builder.sized(0.25F, 0.25F);
   }
}
