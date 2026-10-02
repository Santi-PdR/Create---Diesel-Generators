package com.jesz.createdieselgenerators.content.track_layers_bag;

import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.mixins.UseOnContextInvoker;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.trains.track.TrackBlockItem;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.fml.DistExecutor;

import java.util.List;
import java.util.Optional;

public class TrackLayersBagItem extends Item {
    public TrackLayersBagItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        ItemStack track = getTracks(stack);
        if(track.isEmpty())
            return Optional.empty();
        return Optional.of(new TrackLayersBagComponent(track));
    }

    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.minecraft.bundle.fullness", getTracks(stack).getCount(), 1024).withStyle(ChatFormatting.GRAY));
    }

    int add(ItemStack bag, ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof TrackBlockItem))
            return 0;
        ItemStack stored = getTracks(bag);
        if (!stored.isEmpty() && !ItemStack.isSameItemSameTags(stack, stored))
            return 0;
        int oldCount = stored.getCount();
        int added = Math.min(stack.getCount(), 1024 - oldCount);
        if (added <= 0)
            return 0;
        if (stored.isEmpty())
            stored = stack.copy();
        stored.setCount(oldCount + added);
        saveTracks(bag, stored);
        return added;
    }

    static void saveTracks(ItemStack bag, ItemStack tracks) {
        CompoundTag tag = bag.getOrCreateTag();
        int count = tracks.getCount();
        tag.putInt("Count", count);
        if (count == 0) {
            tag.remove("Item");
            return;
        }
        // ItemStack's 1.20.1 Count is a signed byte. Store a representative
        // item and keep the actual bag quantity only in our integer field.
        ItemStack template = tracks.copy();
        template.setCount(1);
        tag.put("Item", template.save(new CompoundTag()));
    }

    static ItemStack removeOne(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("Item"))
            return ItemStack.EMPTY;

        ItemStack extractedStack = getTracks(stack);

        ItemStack savedStack = extractedStack.copy();
        savedStack.shrink(64);
        saveTracks(stack, savedStack);
        extractedStack.setCount(Math.min(64, extractedStack.getCount()));
        return extractedStack;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return 1 + (int) (((float)getTracks(stack).getCount() / 1024) * 12);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x66ff66;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getTracks(stack).getCount() != 0;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction click, Player player) {
        if(stack.getCount() != 1 || click != ClickAction.SECONDARY)
            return false;

        ItemStack stackInSlot = slot.getItem();

        if(stackInSlot.isEmpty() && slot.mayPlace(getTracks(stack))){
            player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
            slot.set(removeOne(stack));
        }else if(!stackInSlot.isEmpty() && slot.mayPickup(player)){
            int added = add(stack, stackInSlot);
            if(added > 0) {
                player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                stackInSlot.shrink(added);
            }
        }
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack bag, ItemStack otherStack, Slot slot, ClickAction click, Player player, SlotAccess slotAccess) {
        if(bag.getCount() != 1 || click != ClickAction.SECONDARY || !slot.allowModification(player))
            return false;
        if(otherStack.isEmpty()) {
            ItemStack extractedStack = removeOne(bag);
            if(!extractedStack.isEmpty()){
                player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                slotAccess.set(extractedStack);
            }
        }else{
            int added = add(bag, otherStack);
            if(added > 0){
                player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                otherStack.shrink(added);
            }
        }
        return true;
    }

    public static ItemStack getTracks(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Item"))
            return ItemStack.EMPTY;
        CompoundTag item = tag.getCompound("Item").copy();
        item.putByte("Count", (byte) 1); // Also read legacy full-bag Count=0/-128 NBT.
        ItemStack tracks = ItemStack.of(item);
        if (tracks.isEmpty() || !(tracks.getItem() instanceof TrackBlockItem))
            return ItemStack.EMPTY;
        tracks.setCount(Math.max(0, Math.min(1024, tag.getInt("Count"))));
        return tracks;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack bag = context.getItemInHand();
        ItemStack tracks = getTracks(bag);
        if (tracks.isEmpty())
            return InteractionResult.PASS;
        Player player = context.getPlayer();
        if (player == null) {
            InteractionResult result = tracks.getItem().useOn(new BagTrackContext(context, tracks));
            saveTracks(bag, tracks);
            return result;
        }
        // Create's curve placement counts/removes materials from the player's
        // inventory and clears selection on the actual held stack. Temporarily
        // expose the bag's tracks to that code, then restore the bag even on failure.
        ItemStack held = player.getItemInHand(context.getHand());
        player.setItemInHand(context.getHand(), tracks);
        try {
            return tracks.getItem().useOn(new BagTrackContext(context, tracks));
        } finally {
            saveTracks(bag, player.getItemInHand(context.getHand()));
            player.setItemInHand(context.getHand(), held);
        }
    }

    private static class BagTrackContext extends UseOnContext {
        BagTrackContext(UseOnContext source, ItemStack tracks) {
            super(source.getLevel(), source.getPlayer(), source.getHand(), tracks,
                    ((UseOnContextInvoker) source).cdg_getHitResult());
        }
    }

    public void registerModelOverrides() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ItemProperties.register(CDGItems.TRACK_LAYERS_BAG.get(), CreateDieselGenerators.rl("tracks"),
                    (stack, level, entity, seed) -> getTracks(stack).getCount());
        });
    }

    public static ItemModelBuilder addOverrideModels(DataGenContext<Item, TrackLayersBagItem> c,
                                                     RegistrateItemModelProvider p) {
        ItemModelBuilder builder = p.generated(() -> c.get());

        builder.override()
                .predicate(CreateDieselGenerators.rl("tracks"), 0.01f)
                .model(p.getBuilder(c.getName() + "_filled")
                        .parent(new ModelFile.UncheckedModelFile("item/generated"))
                        .texture("layer0", CreateDieselGenerators.rl("item/track_layers_bag_filled")))
                .end();
        return builder;
    }

    public static ItemStack full(){
        ItemStack stack = CDGItems.TRACK_LAYERS_BAG.asStack();
        ((TrackLayersBagItem)stack.getItem()).add(stack, AllBlocks.TRACK.asStack(1024));
        return stack;
    }
}
