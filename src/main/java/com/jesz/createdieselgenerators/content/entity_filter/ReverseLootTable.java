package com.jesz.createdieselgenerators.content.entity_filter;

import com.jesz.createdieselgenerators.mixins.LootItemAccessor;
import com.jesz.createdieselgenerators.mixins.LootPoolAccessor;
import com.jesz.createdieselgenerators.mixins.LootTableAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ReverseLootTable implements PreparableReloadListener {
    // Immutable snapshots: never clear a live index while async loot parsing
    // or a client reference-item scan is in progress.
    public static volatile Map<Item, List<EntityType<?>>> ALL = Map.of();
    private final LootDataManager lootData;

    public ReverseLootTable(LootDataManager lootData) {
        this.lootData = lootData;
    }

    public static void replace(Map<Item, List<EntityType<?>>> data) {
        Map<Item, List<EntityType<?>>> snapshot = new HashMap<>();
        data.forEach((item, types) -> snapshot.put(item, List.copyOf(types)));
        ALL = Map.copyOf(snapshot);
    }

    public static Map<Item, List<EntityType<?>>> build(LootDataManager lootData) {
        Map<Item, List<EntityType<?>>> index = new HashMap<>();
        for (ResourceLocation id : lootData.getKeys(LootDataType.TABLE)) {
            if (!id.getPath().startsWith("entities/"))
                continue;
            ResourceLocation entityId = new ResourceLocation(id.getNamespace(), id.getPath().substring("entities/".length()));
            if (!ForgeRegistries.ENTITY_TYPES.containsKey(entityId))
                continue;
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entityId);
            ((LootTableAccessor) lootData.getLootTable(id)).getPools().forEach(pool -> {
                for (var entry : ((LootPoolAccessor) pool).getEntries()) {
                    if (entry instanceof LootItemAccessor lootItem) {
                        var types = index.computeIfAbsent(lootItem.getItem(), item -> new ArrayList<>());
                        if (!types.contains(type))
                            types.add(type);
                    }
                }
            });
        }
        return index;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resources, ProfilerFiller preparations,
                                          ProfilerFiller reload, Executor backgroundExecutor, Executor gameExecutor) {
        // Forge appends this listener after LootDataManager. The preparation
        // barrier also orders application after the preceding listeners.
        return stage.wait(null).thenRunAsync(() -> replace(build(lootData)), gameExecutor);
    }
}
