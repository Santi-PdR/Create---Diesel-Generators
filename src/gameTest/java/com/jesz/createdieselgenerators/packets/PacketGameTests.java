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
    public static void filterPacketPreservesProtocolV3(GameTestHelper helper) {
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
            check(CDGPackets.NETWORK_VERSION == 3, "Wire-compatible changes must not change the protocol version");
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
