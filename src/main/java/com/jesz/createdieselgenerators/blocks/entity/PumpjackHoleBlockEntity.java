package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.world.OilChunksSavedData;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.registries.ForgeRegistries;

public class PumpjackHoleBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, IHaveHoveringInformation {
   BlockState state;
   SmartFluidTankBehaviour tank;
   public int headPos = 0;
   public int bearingPos = 0;
   public boolean started = false;
   public int oilAmount = 0;
   public int storedOilAmount = 0;
   byte tt = 0;
   public int pipeLength = 0;
   boolean valid = false;

   public PumpjackHoleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
      this.state = state;
   }

   @Override
   protected void write(CompoundTag compound, boolean clientPacket) {
      super.write(compound, clientPacket);
      this.tank.write(compound, false);
      compound.putInt("StoredOilAmount", this.storedOilAmount);
      compound.putInt("OilAmount", this.oilAmount);
      compound.putBoolean("Started", this.started);
   }

   @Override
   public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
      if (this.valid) {
         return false;
      } else {
         Lang.builder().add(Components.translatable("createdieselgenerators.goggle.problem_encountered")).style(ChatFormatting.GOLD).forGoggles(tooltip);
         Lang.builder().add(Components.translatable("createdieselgenerators.goggle.pumpjack_invalid_pipes")).style(ChatFormatting.GRAY).forGoggles(tooltip);
         return true;
      }
   }

   @Override
   public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
      IHaveGoggleInformation.super.addToGoggleTooltip(tooltip, isPlayerSneaking);
      if (this.valid && this.started) {
         Lang.builder().add(Components.translatable("createdieselgenerators.goggle.oil_amount")).style(ChatFormatting.GRAY).forGoggles(tooltip);
         Lang.number(this.oilAmount).add(Lang.translate("generic.unit.buckets")).style(ChatFormatting.GOLD).forGoggles(tooltip);
         return true;
      } else {
         return false;
      }
   }

   @Override
   protected void read(CompoundTag compound, boolean clientPacket) {
      super.read(compound, clientPacket);
      this.tank.read(compound, false);
      this.storedOilAmount = compound.getInt("StoredOilAmount");
      this.oilAmount = compound.getInt("OilAmount");
      this.started = compound.getBoolean("Started");
   }

   @Override
   public void handleUpdateTag(CompoundTag compound) {
      super.handleUpdateTag(compound);
      this.oilAmount = compound.getInt("OilAmount");
      this.started = compound.getBoolean("Started");
   }

   @Override
   public void tick() {
      super.tick();
      this.tt++;
      if (this.tt >= 20) {
         int pipeLength = 0;
         this.tt = 0;
         boolean v = false;

         for (int i = 0; i < this.getBlockPos().getY() - this.level.getMinBuildHeight(); i++) {
            pipeLength++;
            BlockState bs = this.level.getBlockState(this.getBlockPos().below(i + 1));
            if (!(bs.getBlock() instanceof PipeBlock) && !(bs.getBlock() instanceof EncasedPipeBlock)) {
               if (bs.getBlock() instanceof GlassFluidPipeBlock) {
                  if (bs.getValue(BlockStateProperties.AXIS) != Axis.Y) {
                     break;
                  }
               } else if (!bs.is(AllTags.optionalTag(ForgeRegistries.BLOCKS, new ResourceLocation("createdieselgenerators:pumpjack_pipe")))) {
                  if (bs.is(AllTags.optionalTag(ForgeRegistries.BLOCKS, new ResourceLocation("createdieselgenerators:oil_deposit")))) {
                     v = true;
                  }
                  break;
               }
            } else if (!(Boolean)bs.getValue(BlockStateProperties.UP) || !(Boolean)bs.getValue(BlockStateProperties.DOWN)) {
               break;
            }
         }

         if (v) {
            this.pipeLength = pipeLength;
         } else {
            this.pipeLength = 0;
         }

         this.valid = v;
      }
   }

   @Override
   public AABB getRenderBoundingBox() {
      return super.getRenderBoundingBox().inflate(this.pipeLength);
   }

   @Override
   public CompoundTag getUpdateTag() {
      CompoundTag compound = super.getUpdateTag();
      compound.putInt("OilAmount", this.oilAmount);
      compound.putBoolean("Started", this.started);
      return compound;
   }

   @Override
   public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
      this.tank = SmartFluidTankBehaviour.single(this, 1000);
      behaviours.add(this.tank);
   }

   public void tickFluid(boolean isCrankLarge) {
      if (!this.level.isClientSide && this.valid) {
         ChunkPos chunkPos = new ChunkPos(this.getBlockPos());
         OilChunksSavedData sd = OilChunksSavedData.load((ServerLevel)this.level);
         int amount = sd.getChunkOilAmount(chunkPos);
         if (amount == -1) {
            amount = CreateDieselGenerators.getOilAmount(
               this.level.getBiome(new BlockPos(chunkPos.x * 16, 64, chunkPos.z * 16)),
               chunkPos.x,
               chunkPos.z,
               ((ServerLevel)this.level).getSeed()
            );
         }

         this.oilAmount = amount;
         this.started = true;
         if (amount == 0) {
            return;
         }

         if (this.storedOilAmount == 0) {
            sd.setChunkAmount(chunkPos, amount - 1);
            this.oilAmount = amount - 1;
            this.storedOilAmount = 1000;
         }

         int subtractedAmount = Mth.clamp((int)(100.0F * Math.abs((float)this.headPos / this.bearingPos)) * (isCrankLarge ? 2 : 1), 0, 1000);
         this.storedOilAmount = this.storedOilAmount < subtractedAmount
            ? 0
            : (int)(this.storedOilAmount - 100.0F / Math.abs((float)this.headPos / this.bearingPos));
         List<Fluid> stackList = ForgeRegistries.FLUIDS
            .tags()
            .getTag(AllTags.optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("createdieselgenerators:pumpjack_output")))
            .stream()
            .distinct()
            .toList();
         if (stackList.isEmpty()) {
            return;
         }

         FluidStack oilStack = new FluidStack(stackList.get(0), subtractedAmount);
         this.tank.getPrimaryHandler().fill(oilStack, FluidAction.EXECUTE);
      }
   }

   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      if (cap != ForgeCapabilities.FLUID_HANDLER) {
         return super.getCapability(cap, side);
      } else if (side == Direction.NORTH && (Boolean)this.getBlockState().getValue(BlockStateProperties.NORTH)) {
         return this.tank.getCapability().cast();
      } else if (side == Direction.EAST && (Boolean)this.getBlockState().getValue(BlockStateProperties.EAST)) {
         return this.tank.getCapability().cast();
      } else if (side == Direction.SOUTH && (Boolean)this.getBlockState().getValue(BlockStateProperties.SOUTH)) {
         return this.tank.getCapability().cast();
      } else {
         return side == Direction.WEST && this.getBlockState().getValue(BlockStateProperties.WEST)
            ? this.tank.getCapability().cast()
            : super.getCapability(cap, side);
      }
   }
}
