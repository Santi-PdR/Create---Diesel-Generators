package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.entity_filter.EntityFilterScreen;
import com.jesz.createdieselgenerators.content.molds.MoldType;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.config.BackendType;
import com.jozufozu.flywheel.config.FlwConfig;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.WorldDataConfiguration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Opt-in real client/world smoke test; never part of the distribution jar. */
@Mod.EventBusSubscriber(modid = CreateDieselGenerators.ID, value = Dist.CLIENT)
public class PortClientSmokeTest {
    private static final Map<BlockPos, BlockEntityType<?>> placed = new LinkedHashMap<>();
    private static final List<String> checks = new ArrayList<>();
    private static int stage;
    private static int ticks;
    private static CompletableFuture<Void> setup;
    private static BlockPos hugePos;
    private static Float previousAngle;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (!Boolean.getBoolean("cdg.port.clientSmoke") || event.phase != TickEvent.Phase.END)
            return;
        Minecraft mc = Minecraft.getInstance();
        try {
            if (stage == 0 && mc.screen instanceof TitleScreen) {
                verifyBakedAssets(mc);
                stage = 1;
                mc.createWorldOpenFlows().createFreshLevel("cdg-client-smoke",
                        new LevelSettings("CDG client smoke", GameType.CREATIVE, false, Difficulty.PEACEFUL,
                                false, new GameRules(), WorldDataConfiguration.DEFAULT),
                        new WorldOptions(42L, false, false),
                        access -> access.registryOrThrow(Registries.WORLD_PRESET)
                                .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions());
            } else if (stage == 1 && mc.level != null && mc.player != null && mc.getSingleplayerServer() != null) {
                setup = CompletableFuture.runAsync(() -> placeRenderTargets(mc), mc.getSingleplayerServer());
                stage = 2;
            } else if (stage == 2 && setup.isDone()) {
                setup.join();
                if (placed.keySet().stream().anyMatch(pos -> mc.level.getBlockEntity(pos) == null))
                    return;
                verifyRendererRegistration(mc);
                check(Backend.canUseInstancing(mc.level), "Software GL did not enable the instanced backend");
                checks.add("Flywheel INSTANCING enabled; six instance factories registered");
                previousAngle = ((HugeDieselEngineBlockEntity) mc.level.getBlockEntity(hugePos)).getTargetAngle();
                stage = 3;
                ticks = 0;
            } else if (stage == 3 && ++ticks == 60) {
                Float angle = ((HugeDieselEngineBlockEntity) mc.level.getBlockEntity(hugePos)).getTargetAngle();
                check(angle != null && previousAngle != null && !angle.equals(previousAngle), "Huge-engine piston angle is stationary");
                checks.add("Huge-engine animation follows the rotating powered shaft");
                setBackend(BackendType.OFF);
                check(!Backend.canUseInstancing(mc.level), "Fallback backend did not activate");
                stage = 4;
                ticks = 0;
            } else if (stage == 4 && ++ticks == 60) {
                checks.add("World rendered with Flywheel OFF (fallback renderers)");
                int scenes = 0;
                for (var entry : PonderRegistry.ALL.entrySet()) {
                    if (!entry.getKey().getNamespace().equals(CreateDieselGenerators.ID))
                        continue;
                    for (var scene : PonderRegistry.compile(entry.getKey())) {
                        for (int tick = 0; tick < 400; tick++)
                            scene.tick();
                        scenes++;
                    }
                }
                check(scenes == 11, "Expected 11 registered Ponder storyboards, got " + scenes);
                checks.add("All 11 Ponder storyboards compiled and ticked");
                openFilterScreen(mc);
                stage = 5;
                ticks = 0;
            } else if (stage == 5 && ++ticks == 40) {
                check(mc.screen instanceof EntityFilterScreen, "Entity filter screen did not remain open");
                checks.add("Registered entity filter menu factory and screen initialized and rendered");
                Path report = Path.of("../build/verification/client-smoke.txt");
                Files.createDirectories(report.getParent());
                Files.writeString(report, "CLIENT_SMOKE_PASSED\n" + String.join("\n", checks) + "\n");
                System.out.println("CLIENT_SMOKE_PASSED: " + String.join("; ", checks));
                stage = 6;
                mc.stop();
            }
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.err.println("CLIENT_SMOKE_FAILED: " + failure);
            System.exit(1);
        }
    }

    private static void verifyBakedAssets(Minecraft mc) {
        var models = mc.getModelManager();
        int items = 0;
        int states = 0;
        for (var item : ForgeRegistries.ITEMS.getValues()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (!id.getNamespace().equals(CreateDieselGenerators.ID))
                continue;
            check(models.getModel(new ModelResourceLocation(id, "inventory")) != models.getMissingModel(), "Missing item model: " + id);
            items++;
        }
        for (var block : ForgeRegistries.BLOCKS.getValues()) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
            if (!id.getNamespace().equals(CreateDieselGenerators.ID))
                continue;
            for (var state : block.getStateDefinition().getPossibleStates()) {
                if (state.getRenderShape() != RenderShape.MODEL)
                    continue;
                check(models.getBlockModelShaper().getBlockModel(state) != models.getMissingModel(), "Missing blockstate model: " + state);
                states++;
            }
        }
        for (var resource : mc.getResourceManager().listResources("textures", id ->
                id.getNamespace().equals(CreateDieselGenerators.ID) && id.getPath().endsWith(".png")
                        && (id.getPath().startsWith("textures/block/") || id.getPath().startsWith("textures/item/"))).keySet()) {
            ResourceLocation sprite = new ResourceLocation(resource.getNamespace(),
                    resource.getPath().substring("textures/".length(), resource.getPath().length() - 4));
            check(mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(sprite).contents().name().equals(sprite), "Texture not stitched: " + sprite);
        }
        for (MoldType mold : MoldType.types)
            check(mold.model != null && mold.model != models.getMissingModel(), "Missing registered mold model: " + mold.getId());
        checks.add(items + " item models, " + states + " blockstate models, block/item atlas sprites and mold models baked");
    }

    private static void placeRenderTargets(Minecraft mc) {
        var server = mc.getSingleplayerServer();
        var level = server.overworld();
        var player = server.getPlayerList().getPlayers().get(0);
        BlockPos origin = player.blockPosition().offset(2, 1, 2);
        int index = 0;
        for (var type : ForgeRegistries.BLOCK_ENTITY_TYPES.getValues()) {
            ResourceLocation id = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(type);
            if (!id.getNamespace().equals(CreateDieselGenerators.ID))
                continue;
            var state = ForgeRegistries.BLOCKS.getValues().stream().map(block -> block.defaultBlockState())
                    .filter(type::isValid).findFirst().orElseThrow();
            BlockPos pos = origin.offset((index % 4) * 3, 0, (index / 4) * 3);
            index++;
            if (type == CDGBlockEntityTypes.HUGE_DIESEL_ENGINE.get()) {
                state = state.setValue(HugeDieselEngineBlock.FACING, Direction.UP);
                hugePos = pos;
            }
            level.setBlock(pos.below(), type == CDGBlockEntityTypes.BASIN_LID.get()
                    ? com.simibubi.create.AllBlocks.BASIN.getDefaultState()
                    : net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
            level.setBlock(pos, state, 3);
            placed.put(pos, type);
        }
        // A perpendicular powered shaft and real diesel make the huge piston
        // exercise a nonzero time-dependent angle, not just an idle pose.
        BlockPos shaft = hugePos.above(2);
        level.setBlock(shaft, CDGBlocks.POWERED_ENGINE_SHAFT.getDefaultState()
                .setValue(BlockStateProperties.AXIS, Direction.Axis.X), 3);
        placed.put(shaft, CDGBlockEntityTypes.POWERED_ENGINE_SHAFT.get());
        level.getBlockEntity(hugePos).getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElseThrow(IllegalStateException::new)
                .fill(new FluidStack(CDGFluids.DIESEL.getSource(), 100), IFluidHandler.FluidAction.EXECUTE);
    }

    private static void verifyRendererRegistration(Minecraft mc) {
        for (var entry : placed.entrySet()) {
            var type = entry.getValue();
            if (type == CDGBlockEntityTypes.OIL_BARREL.get() || type == CDGBlockEntityTypes.ENCASED_GIRDER.get()
                    || type == CDGBlockEntityTypes.CONCRETE_ENCASED_FLUID_PIPE.get())
                continue; // These types deliberately render as blocks, not BERs.
            check(mc.getBlockEntityRenderDispatcher().getRenderer(mc.level.getBlockEntity(entry.getKey())) != null,
                    "Missing block-entity renderer: " + ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(type));
        }
        for (var type : List.of(CDGBlockEntityTypes.DIESEL_ENGINE.get(), CDGBlockEntityTypes.MODULAR_DIESEL_ENGINE.get(),
                CDGBlockEntityTypes.HUGE_DIESEL_ENGINE.get(), CDGBlockEntityTypes.POWERED_ENGINE_SHAFT.get(),
                CDGBlockEntityTypes.PUMPJACK_BEARING.get(), CDGBlockEntityTypes.PUMPJACK_CRANK.get()))
            check(InstancedRenderRegistry.canInstance(type), "Missing Flywheel instance: " + ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(type));
    }

    @SuppressWarnings("unchecked")
    private static void setBackend(BackendType type) throws ReflectiveOperationException {
        // Test-only access to the exact fields verified in the bundled 0.6.11
        // jar. This follows the same config + refresh path as /flywheel backend.
        var clientField = FlwConfig.class.getDeclaredField("client");
        clientField.setAccessible(true);
        Object client = clientField.get(FlwConfig.get());
        var backendField = client.getClass().getDeclaredField("backend");
        backendField.setAccessible(true);
        ((ForgeConfigSpec.EnumValue<BackendType>) backendField.get(client)).set(type);
        Backend.refresh();
    }

    private static void openFilterScreen(Minecraft mc) {
        var filter = CDGItems.ENTITY_FILTER.asStack();
        mc.player.setItemInHand(InteractionHand.MAIN_HAND, filter);
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        try {
            data.writeItem(filter);
            var type = CDGMenuTypes.ENTITY_FILTER.get();
            var menu = type.create(101, mc.player.getInventory(), data);
            var factory = MenuScreens.getScreenFactory(type, mc, 101, Component.literal("Entity filter smoke")).orElseThrow();
            mc.player.containerMenu = menu;
            mc.setScreen(factory.create(menu, mc.player.getInventory(), Component.literal("Entity filter smoke")));
        } finally {
            data.release();
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException(message);
    }
}
