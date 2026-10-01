package com.jesz.createdieselgenerators.packets;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.content.entity_filter.EntityAttribute;
import com.simibubi.create.content.logistics.filter.FilterScreenPacket;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(CreateDieselGenerators.ID)
@PrefixGameTestTemplate(false)
public class PacketGameTests {
    @GameTest(template = "port_test_empty")
    public static void filterPacketPreservesIntAndNbtEncoding(GameTestHelper helper) {
        var data = EntityAttribute.StandardTraits.IS_HOSTILE.write();
        FriendlyByteBuf encoded = new FriendlyByteBuf(Unpooled.buffer());
        FriendlyByteBuf roundTrip = new FriendlyByteBuf(Unpooled.buffer());
        try {
            new EntityFilterScreenPacket(FilterScreenPacket.Option.ADD_TAG, data).write(encoded);
            check(encoded.readInt() == FilterScreenPacket.Option.ADD_TAG.ordinal(), "Option encoding changed");
            check(data.equals(encoded.readNbt()), "Attribute encoding changed");
            encoded.readerIndex(0);
            new EntityFilterScreenPacket(encoded).write(roundTrip);
            check(roundTrip.readInt() == FilterScreenPacket.Option.ADD_TAG.ordinal(), "Option did not round-trip");
            check(data.equals(roundTrip.readNbt()), "Attribute did not round-trip");
            check(CDGPackets.NETWORK_VERSION == 4, "Loot-index sync requires protocol 4 on both peers");
        } finally {
            encoded.release();
            roundTrip.release();
        }
        helper.succeed();
    }

    @GameTest(template = "port_test_empty")
    public static void malformedFilterPacketsAreRejected(GameTestHelper helper) {
        for (int ordinal : new int[]{-1, Integer.MAX_VALUE, FilterScreenPacket.Option.values().length}) {
            FriendlyByteBuf encoded = new FriendlyByteBuf(Unpooled.buffer());
            try {
                encoded.writeInt(ordinal);
                encoded.writeNbt(EntityAttribute.StandardTraits.IS_HOSTILE.write());
                expectRejected(encoded);
            } finally {
                encoded.release();
            }
        }
        FriendlyByteBuf encoded = new FriendlyByteBuf(Unpooled.buffer());
        try {
            encoded.writeInt(FilterScreenPacket.Option.ADD_TAG.ordinal());
            encoded.writeNbt(null);
            expectRejected(encoded);
        } finally {
            encoded.release();
        }
        helper.succeed();
    }

    @GameTest(template = "port_test_empty")
    public static void loadedLootIndexIsCompleteAndIdempotent(GameTestHelper helper) {
        var first = com.jesz.createdieselgenerators.content.entity_filter.ReverseLootTable.ALL;
        check(first.getOrDefault(net.minecraft.world.item.Items.PORKCHOP, java.util.List.of())
                .contains(net.minecraft.world.entity.EntityType.PIG), "Loaded loot index has no pig drops");
        var rebuilt = com.jesz.createdieselgenerators.content.entity_filter.ReverseLootTable.build(
                helper.getLevel().getServer().getLootData());
        check(first.equals(rebuilt), "Rebuilding loot data changes or loses entries");
        check(rebuilt.values().stream().allMatch(types -> types.stream().distinct().count() == types.size()),
                "Duplicate loot entries created duplicate entity attributes");
        helper.succeed();
    }

    @GameTest(template = "port_test_empty")
    public static void reverseLootPacketPreservesRegistryIdentifiers(GameTestHelper helper) {
        var index = com.jesz.createdieselgenerators.content.entity_filter.ReverseLootTable.ALL;
        FriendlyByteBuf encoded = new FriendlyByteBuf(Unpooled.buffer());
        try {
            new ReverseLootTablePacket(index).write(encoded);
            var decoded = new ReverseLootTablePacket(encoded);
            check(index.equals(decoded.index), "Loot packet did not round-trip registered items/entities");
            check(encoded.readableBytes() == 0, "Loot packet left unread data");
        } finally {
            encoded.release();
        }
        helper.succeed();
    }

    private static void expectRejected(FriendlyByteBuf encoded) {
        try {
            new EntityFilterScreenPacket(encoded);
        } catch (DecoderException expected) {
            return;
        }
        throw new IllegalStateException("Malformed packet was accepted");
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException(message);
    }
}
