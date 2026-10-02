package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.molds.CompressionMoldingRecipe;
import com.jesz.createdieselgenerators.content.molds.MoldType;
import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelBlockEntity;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.jesz.createdieselgenerators.mixin_interfaces.IEntity;
import com.jesz.createdieselgenerators.mixins.BasinBlockEntityAccessor;
import com.jesz.createdieselgenerators.mixins.LootItemAccessor;
import com.jesz.createdieselgenerators.mixins.LootPoolAccessor;
import com.jesz.createdieselgenerators.mixins.LootTableAccessor;
import com.jesz.createdieselgenerators.mixins.UseOnContextInvoker;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.MountedFluidStorage;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

@GameTestHolder(CreateDieselGenerators.ID)
@PrefixGameTestTemplate(false)
public class PortGameTests {
    private static final String TEMPLATE = "port_test_empty";

    @GameTest(template = TEMPLATE)
    public static void commonMixinTargetsLoad(GameTestHelper helper) throws ClassNotFoundException {
        // Loading each target applies the required mixin and checks injection
        // points in the actual Forge 1.20.1 development mappings.
        for (String target : new String[]{
                "com.simibubi.create.content.contraptions.Contraption",
                "com.simibubi.create.content.contraptions.MountedFluidStorage",
                "com.simibubi.create.content.decoration.copycat.CopycatBlock",
                "com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity",
                "com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock",
                "com.simibubi.create.content.processing.basin.BasinBlockEntity",
                "com.simibubi.create.content.processing.basin.BasinRecipe",
                "net.minecraft.world.entity.monster.Creeper"}) {
            Class.forName(target);
        }
        check(IEntity.class.isAssignableFrom(Entity.class), "Entity turret-position mixin is absent");
        check(BasinBlockEntityAccessor.class.isAssignableFrom(BasinBlockEntity.class), "Basin accessor is absent");
        check(LootTableAccessor.class.isAssignableFrom(LootTable.class), "Forge loot-table accessor is absent");
        check(LootPoolAccessor.class.isAssignableFrom(LootPool.class), "Loot-pool accessor is absent");
        check(LootItemAccessor.class.isAssignableFrom(LootItem.class), "Loot-item accessor is absent");
        check(UseOnContextInvoker.class.isAssignableFrom(UseOnContext.class), "UseOnContext invoker is absent");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void allCoreRecipesLoad(GameTestHelper helper) {
        var level = helper.getLevel();
        var resources = level.getServer().getResourceManager().listResources("recipes", id ->
                id.getNamespace().equals(CreateDieselGenerators.ID)
                        && id.getPath().endsWith(".json") && !id.getPath().contains("/compat/"));
        check(!resources.isEmpty(), "No core recipe resources were found");
        resources.keySet().forEach(path -> {
            ResourceLocation id = new ResourceLocation(path.getNamespace(),
                    path.getPath().substring("recipes/".length(), path.getPath().length() - ".json".length()));
            check(level.getRecipeManager().byKey(id).isPresent(), "Recipe failed to load: " + id);
        });
        var asphalt = (ProcessingRecipe<?>) level.getRecipeManager()
                .byKey(CreateDieselGenerators.rl("mixing/asphalt_block")).orElseThrow();
        check(asphalt.getIngredients().size() == 4, "Asphalt must consume two gravel and two sand");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void fuelRegistryLoads(GameTestHelper helper) {
        var lookup = helper.getLevel().registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE);
        FuelType diesel = FuelType.getTypeFor(lookup, CDGFluids.DIESEL.getSource());
        check(diesel != FuelType.EMPTY, "Diesel fuel datapack registry did not load");
        check(diesel.normal().speed() == 96 && diesel.normal().strength() == 6144,
                "Diesel fuel properties were not decoded correctly");
        check(FuelType.getTypeFor(lookup, Fluids.EMPTY) == FuelType.EMPTY, "Empty fluid is not fuel");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void mountedOilBarrelRoundTrip(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, CDGBlocks.OIL_BARREL.get());
        var barrel = (OilBarrelBlockEntity) helper.getBlockEntity(pos);
        IFluidHandler handler = barrel.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        check(handler.fill(new FluidStack(CDGFluids.DIESEL.getSource(), 500), IFluidHandler.FluidAction.EXECUTE) == 500,
                "Barrel could not accept diesel");
        check(MountedFluidStorage.canUseAsStorage(barrel), "Barrel cannot be mounted");
        MountedFluidStorage mounted = new MountedFluidStorage(barrel);
        mounted.removeStorageFromWorld();
        check(mounted.isValid(), "Mounted barrel failed to capture its tank");
        CompoundTag saved = mounted.serialize();
        MountedFluidStorage restored = MountedFluidStorage.deserialize(saved);
        check(restored.getFluidHandler().getFluidInTank(0).getAmount() == 500, "Mounted fuel was lost during serialization");
        check(restored.getFluidHandler().getTankCapacity(0) == handler.getTankCapacity(0), "Mounted capacity differs from barrel");
        handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
        restored.addStorageToWorld(barrel);
        check(handler.getFluidInTank(0).getAmount() == 500, "Fuel was lost when disassembling the barrel");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void compressionRequiresTheCorrectMold(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, AllBlocks.BASIN.get());
        var basin = (BasinBlockEntity) helper.getBlockEntity(pos);
        IItemHandler inventory = basin.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
        ItemHandlerHelper.insertItemStacked(inventory, CDGItems.WOOD_CHIPS.asStack(4), false);
        var recipe = (CompressionMoldingRecipe) helper.getLevel().getRecipeManager()
                .byKey(CreateDieselGenerators.rl("compression_molding/bowl")).orElseThrow();
        check(!BasinRecipe.match(basin, recipe), "Compression molding matched without a mold");
        ItemStack mold = CDGItems.MOLD.asStack();
        mold.getOrCreateTag().putString("Mold", MoldType.BAR_MOLD.getId().toString());
        ItemHandlerHelper.insertItemStacked(inventory, mold, false);
        check(!BasinRecipe.match(basin, recipe), "Compression molding accepted the wrong mold");
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (CDGItems.MOLD.isIn(inventory.getStackInSlot(slot)))
                inventory.extractItem(slot, 1, false);
        }
        mold = CDGItems.MOLD.asStack();
        mold.getOrCreateTag().putString("Mold", MoldType.BOWL_MOLD.getId().toString());
        ItemHandlerHelper.insertItemStacked(inventory, mold, false);
        check(BasinRecipe.match(basin, recipe), "Compression molding rejected the correct mold and ingredients");
        helper.succeed();
    }

    static void check(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException(message);
    }
}
