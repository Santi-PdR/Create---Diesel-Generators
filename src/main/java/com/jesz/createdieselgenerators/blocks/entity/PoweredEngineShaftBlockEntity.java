package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.HugeDieselEngineBlock;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.utility.Couple;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PoweredEngineShaftBlockEntity extends GeneratingKineticBlockEntity {
   float stressCapacity;
   float speed;
   int movementDirection;
   int initialTicks;
   public Map<BlockPos, Couple<Float>> engines = new HashMap<>();

   public PoweredEngineShaftBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
      super(typeIn, pos, state);
      this.movementDirection = 0;
   }

   public boolean isEngineForConnectorDisplay(BlockPos pos) {
      Axis axis = (Axis)this.getBlockState().getValue(RotatedPillarKineticBlock.AXIS);

      for (Direction d : List.of(
         axis == Axis.Z ? Direction.UP : Direction.NORTH,
         axis == Axis.Z ? Direction.DOWN : Direction.SOUTH,
         axis == Axis.X ? Direction.UP : Direction.EAST,
         axis == Axis.X ? Direction.DOWN : Direction.WEST
      )) {
         BlockState st = this.getLevel().getBlockState(this.getBlockPos().relative(d, 2));
         if (st.getBlock() instanceof HugeDieselEngineBlock && st.getValue(HugeDieselEngineBlock.FACING) == d.getOpposite()) {
            return this.getBlockPos().relative(d, 2).equals(pos);
         }
      }

      return false;
   }

   public void update(BlockPos sourcePos, int direction, float stress, float speed) {
      if (this.engines.containsKey(sourcePos)) {
         this.engines.replace(sourcePos, Couple.create(stress, speed));
      } else {
         this.engines.put(sourcePos, Couple.create(stress, speed));
      }

      AtomicReference<Float> maxSpeed = new AtomicReference<>(0.0F);
      Map<BlockPos, Couple<Float>> map = Map.copyOf(this.engines);

      for (Couple<Float> s : map.values()) {
         if (s.getSecond() > maxSpeed.get()) {
            maxSpeed.set(s.getSecond());
         }
      }

      this.speed = maxSpeed.get();
      this.movementDirection = direction;
      this.reActivateSource = true;
   }

   public boolean canBePoweredBy(BlockPos globalPos) {
      return this.initialTicks == 0;
   }

   public void removeGenerator(BlockPos sourcePos) {
      this.engines.remove(sourcePos);
      if (this.engines.isEmpty()) {
         this.movementDirection = 0;
         this.speed = 0.0F;
         this.stressCapacity = 0.0F;
      }

      this.reActivateSource = true;
   }

   @Override
   protected void write(CompoundTag compound, boolean clientPacket) {
      super.write(compound, clientPacket);
      compound.putInt("Direction", this.movementDirection);
      if (this.initialTicks > 0) {
         compound.putInt("Warmup", this.initialTicks);
      }

      ListTag engineList = new ListTag();
      Map<BlockPos, Couple<Float>> map = Map.copyOf(this.engines);
      map.forEach((p, s) -> {
         CompoundTag tag = new CompoundTag();
         tag.putFloat("Capacity", s.getFirst());
         tag.putFloat("Speed", s.getSecond());
         tag.put("Pos", NbtUtils.writeBlockPos(p));
         engineList.add(tag);
      });
      compound.putFloat("GeneratedSpeed", this.speed);
      compound.put("Engines", engineList);
   }

   @Override
   protected void read(CompoundTag compound, boolean clientPacket) {
      super.read(compound, clientPacket);
      this.movementDirection = compound.getInt("Direction");
      this.initialTicks = compound.getInt("Warmup");
      ListTag engineList = compound.getList("Engines", 10);
      HashMap<BlockPos, Couple<Float>> map = new HashMap<>();

      for (int i = 0; i < engineList.size(); i++) {
         map.put(
            NbtUtils.readBlockPos(engineList.getCompound(i).getCompound("Pos")),
            Couple.create(engineList.getCompound(i).getFloat("Capacity"), engineList.getCompound(i).getFloat("Speed"))
         );
      }

      this.engines.clear();
      this.engines = map;
      this.speed = compound.getFloat("GeneratedSpeed");
   }

   @Override
   public float getGeneratedSpeed() {
      return this.movementDirection * this.speed;
   }

   @Override
   public float calculateAddedStressCapacity() {
      if (this.movementDirection == 0) {
         return 0.0F;
      } else {
         AtomicReference<Float> stress = new AtomicReference<>(0.0F);
         Map<BlockPos, Couple<Float>> map = Map.copyOf(this.engines);
         map.forEach((b, s) -> stress.updateAndGet(f -> f + (Float)s.getFirst() / (Float)s.getSecond()));
         this.lastCapacityProvided = this.capacity;
         return stress.get();
      }
   }

   @Override
   public int getRotationAngleOffset(Axis axis) {
      int combinedCoords = axis.choose(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ());
      return super.getRotationAngleOffset(axis) + (combinedCoords % 2 == 0 ? 180 : 0);
   }
}
