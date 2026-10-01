package com.jesz.createdieselgenerators.packets;

import com.jesz.createdieselgenerators.content.entity_filter.ReverseLootTable;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Loot tables are server-only; clients need this index to recognize mob drops. */
public class ReverseLootTablePacket extends SimplePacketBase {
    public static volatile boolean clientDataReady;
    final Map<Item, List<EntityType<?>>> index;

    public ReverseLootTablePacket(Map<Item, List<EntityType<?>>> index) {
        Map<Item, List<EntityType<?>>> copy = new HashMap<>();
        index.forEach((item, types) -> copy.put(item, List.copyOf(types)));
        this.index = Map.copyOf(copy);
    }

    public ReverseLootTablePacket(FriendlyByteBuf buffer) {
        Map<Item, List<EntityType<?>>> decoded = new HashMap<>();
        int count = readCount(buffer);
        for (int i = 0; i < count; i++) {
            var itemId = buffer.readResourceLocation();
            int typesCount = readCount(buffer);
            List<EntityType<?>> types = new ArrayList<>();
            for (int j = 0; j < typesCount; j++) {
                var id = buffer.readResourceLocation();
                if (ForgeRegistries.ENTITY_TYPES.containsKey(id))
                    types.add(ForgeRegistries.ENTITY_TYPES.getValue(id));
            }
            if (ForgeRegistries.ITEMS.containsKey(itemId))
                decoded.put(ForgeRegistries.ITEMS.getValue(itemId), List.copyOf(types));
        }
        index = Map.copyOf(decoded);
    }

    private static int readCount(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > 65536)
            throw new DecoderException("Invalid reverse-loot index length: " + count);
        return count;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(index.size());
        index.forEach((item, types) -> {
            buffer.writeResourceLocation(ForgeRegistries.ITEMS.getKey(item));
            buffer.writeVarInt(types.size());
            types.forEach(type -> buffer.writeResourceLocation(ForgeRegistries.ENTITY_TYPES.getKey(type)));
        });
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ReverseLootTable.replace(index);
            clientDataReady = true;
        });
        return true;
    }
}
