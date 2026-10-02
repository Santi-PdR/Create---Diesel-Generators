package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.jesz.createdieselgenerators.content.diesel_engine.IEngine;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelBlock;
import com.jesz.createdieselgenerators.mixin_interfaces.IEntity;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.jesz.createdieselgenerators.PortGameTests.check;

@GameTestHolder(CreateDieselGenerators.ID)
@PrefixGameTestTemplate(false)
public class MechanicsGameTests {
    @GameTest(template = "port_test_empty")
    public static void engineCapacityIsFiniteAndPreservesPower(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), CDGBlocks.DIESEL_ENGINE.get());
        helper.setBlock(new BlockPos(4, 1, 1), CDGBlocks.MODULAR_DIESEL_ENGINE.get());
        var normal = (DieselEngineBlockEntity) helper.getBlockEntity(new BlockPos(1, 1, 1));
        var modular = (ModularDieselEngineBlockEntity) helper.getBlockEntity(new BlockPos(4, 1, 1));
        check(normal.calculateAddedStressCapacity() == 0 && modular.calculateAddedStressCapacity() == 0,
                "Empty engines report NaN/Infinity or nonzero capacity");
        for (IEngine engine : new IEngine[]{normal, modular}) {
            engine.getTank().fill(new FluidStack(CDGFluids.DIESEL.getSource(), 100), IFluidHandler.FluidAction.EXECUTE);
            engine.setUpgrade(EngineUpgrades.EMPTY);
            float capacity = engine.getUpgradedFuelCapacity(1);
            check(Float.isFinite(capacity) && capacity > 0, "Fueled engine capacity is invalid");
            engine.setUpgrade(EngineUpgrades.TURBOCHARGER);
            check(Math.abs(engine.getUpgradedFuelCapacity(1) * engine.getUpgrade().getSpeed(engine.getFuelSpeed(), engine)
                    - capacity * engine.getFuelSpeed() * CDGConfig.TURBOCHARGED_ENGINE_MULTIPLIER.get()) < .01,
                    "Turbocharger changed the established total-power formula");
        }
        double multiplier = CDGConfig.TURBOCHARGED_ENGINE_MULTIPLIER.get();
        try {
            CDGConfig.TURBOCHARGED_ENGINE_MULTIPLIER.set(0d);
            check(normal.calculateAddedStressCapacity() == 0 && modular.calculateAddedStressCapacity() == 0,
                    "Zero-speed upgrade produces invalid capacity");
        } finally {
            CDGConfig.TURBOCHARGED_ENGINE_MULTIPLIER.set(multiplier);
        }
        helper.succeed();
    }

    @GameTest(template = "port_test_empty")
    public static void engineConfigurationActuallyApplies(GameTestHelper helper) {
        helper.setBlock(new BlockPos(2, 1, 2), CDGBlocks.DIESEL_ENGINE.get());
        var engine = (DieselEngineBlockEntity) helper.getBlockEntity(new BlockPos(2, 1, 2));
        engine.getTank().fill(new FluidStack(CDGFluids.DIESEL.getSource(), 100), IFluidHandler.FluidAction.EXECUTE);
        boolean enabled = CDGConfig.NORMAL_ENGINES.get();
        double burn = CDGConfig.TURBOCHARGED_ENGINE_BURN_RATE_MULTIPLIER.get();
        try {
            CDGConfig.NORMAL_ENGINES.set(true);
            check(engine.enabled(), "Fueled engine is disabled");
            CDGConfig.NORMAL_ENGINES.set(false);
            check(!engine.enabled() && engine.getGeneratedSpeed() == 0, "Disabled engine still runs");
            engine.setUpgrade(EngineUpgrades.EMPTY);
            float baseBurn = engine.getFuelBurnRate();
            engine.setUpgrade(EngineUpgrades.TURBOCHARGER);
            CDGConfig.TURBOCHARGED_ENGINE_BURN_RATE_MULTIPLIER.set(2d);
            check(engine.getFuelBurnRate() == baseBurn * 2, "Turbo burn-rate config has no effect");
        } finally {
            CDGConfig.NORMAL_ENGINES.set(enabled);
            CDGConfig.TURBOCHARGED_ENGINE_BURN_RATE_MULTIPLIER.set(burn);
        }
        helper.succeed();
    }

    @GameTest(template = "port_test_empty")
    public static void turretOwnershipSurvivesEverySavePath(GameTestHelper helper) {
        var entity = EntityType.PIG.create(helper.getLevel());
        BlockPos turret = new BlockPos(13, 27, -4);
        ((IEntity) entity).setTurretPos(turret);
        CompoundTag tag = entity.saveWithoutId(new CompoundTag());
        check(turret.equals(NbtUtils.readBlockPos(tag.getCompound("TurretPos"))), "Direct saveWithoutId lost turret ownership");
        CompoundTag passenger = new CompoundTag();
        check(entity.saveAsPassenger(passenger) && passenger.contains("TurretPos"), "Passenger save lost turret ownership");
        var restored = EntityType.PIG.create(helper.getLevel());
        restored.load(tag);
        check(turret.equals(((IEntity) restored).getTurretPos()), "Turret ownership did not load");
        tag.remove("TurretPos");
        restored.load(tag);
        check(((IEntity) restored).getTurretPos() == null, "Load retained stale turret ownership");
        helper.succeed();
    }

    @GameTest(template = "port_test_empty")
    public static void dyeOnlyAppliesToOilBarrelCopycats(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, AllBlocks.COPYCAT_PANEL.get());
        var copycat = (CopycatBlockEntity) helper.getBlockEntity(pos);
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.RED_DYE, 2));
        BlockPos absolute = helper.absolutePos(pos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
        copycat.setMaterial(Blocks.STONE.defaultBlockState());
        helper.getLevel().getBlockState(absolute).use(helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
        check(player.getMainHandItem().getCount() == 2, "Unrelated copycat material consumed dye");
        copycat.setMaterial(CDGBlocks.OIL_BARREL.getDefaultState());
        helper.getLevel().getBlockState(absolute).use(helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
        check(player.getMainHandItem().getCount() == 1, "Oil-barrel dye did not consume exactly one item");
        check(copycat.getMaterial().getValue(OilBarrelBlock.OIL_BARREL_COLOR)
                == OilBarrelBlock.OilBarrelColor.getForDyeColor(net.minecraft.world.item.DyeColor.RED), "Copycat barrel color did not change");
        helper.succeed();
    }

    @GameTest(template = "port_test_empty")
    public static void lighterCreeperInteractionUsesRealFuel(GameTestHelper helper) {
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        ItemStack lighter = CDGItems.LIGHTER.asStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, lighter);
        var tank = lighter.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(IllegalStateException::new);
        tank.fill(new FluidStack(CDGFluids.DIESEL.getSource(), 3), IFluidHandler.FluidAction.EXECUTE);
        var creeper = EntityType.CREEPER.create(helper.getLevel());
        check(creeper.interact(player, InteractionHand.MAIN_HAND).consumesAction() && creeper.isIgnited(),
                "Fueled lighter did not report a successful ignition");
        check(tank.getFluidInTank(0).getAmount() == 2, "Creeper ignition did not consume exactly one mB");
        tank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
        tank.fill(new FluidStack(Fluids.WATER, 3), IFluidHandler.FluidAction.EXECUTE);
        creeper = EntityType.CREEPER.create(helper.getLevel());
        creeper.interact(player, InteractionHand.MAIN_HAND);
        check(!creeper.isIgnited() && tank.getFluidInTank(0).getAmount() == 3, "Nonflammable fluid ignited a creeper");
        helper.succeed();
    }

    @GameTest(template = "port_test_empty")
    public static void emptyLighterCannotLightBlocks(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT, false));
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        ItemStack lighter = CDGItems.LIGHTER.asStack();
        lighter.getOrCreateTag().putInt("Type", 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, lighter);
        BlockPos absolute = helper.absolutePos(pos);
        CDGItems.LIGHTER.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false)));
        check(!helper.getLevel().getBlockState(absolute).getValue(BlockStateProperties.LIT), "Empty lighter lit the block before checking fuel");
        helper.succeed();
    }
}
