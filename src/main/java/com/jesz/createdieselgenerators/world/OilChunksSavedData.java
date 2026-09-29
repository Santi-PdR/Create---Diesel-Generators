package com.jesz.createdieselgenerators.world;

import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.simibubi.create.foundation.utility.NBTHelper;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

public class OilChunksSavedData extends SavedData {
   Map<ChunkPos, Integer> chunks = new HashMap<>();

   public CompoundTag save(CompoundTag compound) {
      ListTag lt = new ListTag();
      this.chunks.forEach((pos, amount) -> {
         CompoundTag c = new CompoundTag();
         c.put("x", IntTag.valueOf(pos.x));
         c.put("z", IntTag.valueOf(pos.z));
         c.put("Amount", IntTag.valueOf(amount));
         lt.add(c);
      });
      compound.put("OilChunks", lt);
      return compound;
   }

   private OilChunksSavedData() {
   }

   private static OilChunksSavedData load(CompoundTag compound) {
      OilChunksSavedData sd = new OilChunksSavedData();
      sd.chunks = new HashMap<>();
      NBTHelper.iterateCompoundList(
         compound.getList("OilChunks", 10), c -> sd.chunks.put(new ChunkPos(c.getInt("x"), c.getInt("z")), c.getInt("Amount"))
      );
      return sd;
   }

   public static OilChunksSavedData load(ServerLevel level) {
      return (OilChunksSavedData)level.getDataStorage().computeIfAbsent(OilChunksSavedData::load, OilChunksSavedData::new, "cdg_oil_chunks");
   }

   public void setChunkAmount(ChunkPos chunk, int amount) {
      if (this.chunks.containsKey(chunk)) {
         this.chunks.replace(chunk, amount);
      } else {
         this.chunks.put(chunk, amount);
      }

      this.setDirty();
   }

   public void removeChunkAmount(ChunkPos chunk) {
      this.chunks.remove(chunk);
      this.setDirty();
   }

   public int getChunkOilAmount(ChunkPos chunk) {
      if (this.chunks.containsKey(chunk)) {
         return ConfigRegistry.OIL_DEPOSITS_INFINITE.get() ? Integer.MAX_VALUE : this.chunks.get(chunk);
      } else {
         return -1;
      }
   }
}
