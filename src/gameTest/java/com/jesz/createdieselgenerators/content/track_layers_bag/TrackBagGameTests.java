package com.jesz.createdieselgenerators.content.track_layers_bag;

import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.simibubi.create.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(CreateDieselGenerators.ID)
@PrefixGameTestTemplate(false)
public class TrackBagGameTests {
    @GameTest(template = "port_test_empty")
    public static void fullAndLegacyBagsRoundTrip(GameTestHelper helper) {
        ItemStack bag = TrackLayersBagItem.full();
        check(TrackLayersBagItem.getTracks(bag).getCount() == 1024, "Full bag lost tracks");
        check(bag.getTag().getCompound("Item").getByte("Count") == 1, "Physical NBT count overflowed the signed byte");
        bag.getTag().getCompound("Item").putByte("Count", (byte) 0);
        check(TrackLayersBagItem.getTracks(bag).getCount() == 1024 && AllBlocks.TRACK.isIn(TrackLayersBagItem.getTracks(bag)),
                "Legacy full-bag NBT cannot be recovered");
        ItemStack extracted = TrackLayersBagItem.removeOne(bag);
        check(extracted.getCount() == 64 && TrackLayersBagItem.getTracks(bag).getCount() == 960, "Bag extraction lost items");
        helper.succeed();
    }

    @GameTest(template = "port_test_empty")
    public static void trackPlacementAndSelectionPreserveBagContents(GameTestHelper helper) {
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(helper.absolutePos(new BlockPos(6, 1, 6)).getX(), helper.absolutePos(new BlockPos(6, 1, 6)).getY(),
                helper.absolutePos(new BlockPos(6, 1, 6)).getZ());
        ItemStack bag = CDGItems.TRACK_LAYERS_BAG.asStack();
        TrackLayersBagItem item = (TrackLayersBagItem) bag.getItem();
        item.add(bag, AllBlocks.TRACK.asStack(128));
        player.setItemInHand(InteractionHand.MAIN_HAND, bag);
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, Blocks.STONE);
        helper.setBlock(pos.above(), Blocks.BEDROCK);
        BlockPos absolute = helper.absolutePos(pos);
        var hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
        item.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        check(TrackLayersBagItem.getTracks(bag).getCount() == 128, "Failed placement consumed a track");
        check(player.getMainHandItem() == bag, "Temporary virtual tracks replaced the held bag");
        helper.setBlock(pos.above(), Blocks.AIR);
        check(item.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction(), "Valid track placement failed");
        check(TrackLayersBagItem.getTracks(bag).getCount() == 127, "Placement did not consume exactly one track");
        BlockPos track = absolute.above();
        check(AllBlocks.TRACK.has(helper.getLevel().getBlockState(track)), "Track was not placed");
        var trackHit = new BlockHitResult(Vec3.atCenterOf(track), Direction.UP, track, false);
        item.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, trackHit));
        ItemStack selected = TrackLayersBagItem.getTracks(bag);
        check(selected.hasTag() && selected.getTag().contains("ConnectingFrom"), "Clicking track silently skipped Create selection");
        check(selected.getCount() == 127, "Selecting a connection consumed tracks");
        player.setShiftKeyDown(true);
        try {
            item.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, trackHit));
            selected = TrackLayersBagItem.getTracks(bag);
            check(!selected.hasTag() || !selected.getTag().contains("ConnectingFrom"), "Connection selection was not cleared");
        } finally {
            player.setShiftKeyDown(false);
        }
        helper.succeed();
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException(message);
    }
}
