package com.jesz.createdieselgenerators.content.entity_filter;

import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.CDGMenuTypes;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.simibubi.create.content.logistics.filter.AttributeFilterMenu;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(CreateDieselGenerators.ID)
@PrefixGameTestTemplate(false)
public class FilterGameTests {
    @GameTest(template = "port_test_empty")
    public static void invalidAttributesDoNotCrashMenus(GameTestHelper helper) {
        check(EntityAttribute.fromNBT(null) == null, "Null attribute was accepted");
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", "INVALID ID");
        check(EntityAttribute.fromNBT(tag) == null, "Invalid attribute ID was accepted");
        tag.putString("Id", CreateDieselGenerators.rl("is_mob").toString());
        tag.putString("Entity", "INVALID ENTITY");
        check(EntityAttribute.fromNBT(tag) == null, "Invalid entity ID was accepted");
        tag.putString("Entity", "createdieselgenerators:removed_entity");
        check(EntityAttribute.fromNBT(tag) == null, "Unknown entity was silently replaced with a default");
        EntityAttribute pig = new EntityAttribute.IsMob(EntityType.PIG);
        check(pig.write().equals(EntityAttribute.fromNBT(pig.write()).write()), "Valid attribute did not round-trip");
        helper.succeed();
    }

    @GameTest(template = "port_test_empty")
    public static void ghostReferenceShiftClickDoesNotLoseItems(GameTestHelper helper) {
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        ItemStack filter = CDGItems.ENTITY_FILTER.asStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, filter);
        EntityFilterMenu menu = new EntityFilterMenu(CDGMenuTypes.ENTITY_FILTER.get(), 1, player.getInventory(), filter);
        player.getInventory().setItem(9, new ItemStack(Items.PORKCHOP, 12));
        menu.quickMoveStack(player, 9);
        check(menu.ghostInventory.getStackInSlot(0).is(Items.PORKCHOP), "Reference item was not copied");
        menu.quickMoveStack(player, 36);
        check(menu.ghostInventory.getStackInSlot(0).isEmpty(), "Reference slot was not cleared");
        check(menu.ghostInventory.getStackInSlot(1).is(Items.NAME_TAG), "Attribute display slot was corrupted");
        check(player.getInventory().getItem(9).getCount() == 12, "Ghost-slot operation consumed real items");
        helper.succeed();
    }

    @GameTest(template = "port_test_empty")
    public static void filterModeAndAttributesPersist(GameTestHelper helper) {
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        ItemStack filter = CDGItems.ENTITY_FILTER.asStack();
        filter.getOrCreateTag().putInt("Whitelist", Integer.MAX_VALUE);
        player.setItemInHand(InteractionHand.MAIN_HAND, filter);
        EntityFilterMenu menu = new EntityFilterMenu(CDGMenuTypes.ENTITY_FILTER.get(), 1, player.getInventory(), filter);
        check(menu.whitelist == AttributeFilterMenu.WhitelistMode.WHITELIST_DISJ, "Invalid mode was not safely defaulted");
        menu.whitelist = AttributeFilterMenu.WhitelistMode.BLACKLIST;
        menu.appendSelectedAttribute(null, false);
        menu.appendSelectedAttribute(EntityAttribute.StandardTraits.IS_HOSTILE, false);
        menu.appendSelectedAttribute(EntityAttribute.StandardTraits.IS_HOSTILE, true);
        menu.removed(player);
        check(EntityFilterItem.getWhitelistMode(filter) == AttributeFilterMenu.WhitelistMode.BLACKLIST, "Mode key differs from tooltip key");
        check(filter.getTag().getList("MatchedAttributes", Tag.TAG_COMPOUND).size() == 1, "Invalid or duplicate attributes were persisted");
        menu.clearContents();
        menu.removed(player);
        check(filter.getTag().getList("MatchedAttributes", Tag.TAG_COMPOUND).isEmpty(), "Clear did not remove selected attributes");
        helper.succeed();
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException(message);
    }
}
