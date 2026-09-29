package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.blocks.renderer.BasinLidRenderer;
import com.jesz.createdieselgenerators.blocks.renderer.DieselGeneratorRenderer;
import com.jesz.createdieselgenerators.blocks.renderer.DistillationTankRenderer;
import com.jesz.createdieselgenerators.blocks.renderer.HugeDieselEngineInstance;
import com.jesz.createdieselgenerators.blocks.renderer.HugeDieselEngineRenderer;
import com.jesz.createdieselgenerators.blocks.renderer.LargeDieselGeneratorRenderer;
import com.jesz.createdieselgenerators.blocks.renderer.NoShaftBearingInstance;
import com.jesz.createdieselgenerators.blocks.renderer.NoShaftBearingRenderer;
import com.jesz.createdieselgenerators.blocks.renderer.PumpjackCrankInstance;
import com.jesz.createdieselgenerators.blocks.renderer.PumpjackCrankRenderer;
import com.jesz.createdieselgenerators.blocks.renderer.PumpjackHoleRenderer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftInstance;
import com.simibubi.create.content.kinetics.base.SingleRotatingInstance;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityRegistry {
   public static final BlockEntityEntry<DieselGeneratorBlockEntity> DIESEL_ENGINE = CreateDieselGenerators.REGISTRATE
      .blockEntity("diesel_engine_tile_entity", DieselGeneratorBlockEntity::new)
      .instance(() -> ShaftInstance::new)
      .validBlocks(new NonNullSupplier[]{BlockRegistry.DIESEL_ENGINE})
      .renderer(renderer(DieselGeneratorRenderer::new))
      .register();
   public static final BlockEntityEntry<LargeDieselGeneratorBlockEntity> LARGE_DIESEL_ENGINE = CreateDieselGenerators.REGISTRATE
      .blockEntity("large_diesel_engine_tile_entity", LargeDieselGeneratorBlockEntity::new)
      .instance(() -> ShaftInstance::new)
      .validBlocks(new NonNullSupplier[]{BlockRegistry.MODULAR_DIESEL_ENGINE})
      .renderer(renderer(LargeDieselGeneratorRenderer::new))
      .register();
   public static final BlockEntityEntry<HugeDieselEngineBlockEntity> HUGE_DIESEL_ENGINE = CreateDieselGenerators.REGISTRATE
      .blockEntity("huge_diesel_engine_block_entity", HugeDieselEngineBlockEntity::new)
      .instance(() -> HugeDieselEngineInstance::new)
      .validBlocks(new NonNullSupplier[]{BlockRegistry.HUGE_DIESEL_ENGINE})
      .renderer(renderer(HugeDieselEngineRenderer::new))
      .register();
   public static final BlockEntityEntry<PoweredEngineShaftBlockEntity> POWERED_ENGINE_SHAFT = CreateDieselGenerators.REGISTRATE
      .blockEntity("powered_engine_shaft_block_entity", PoweredEngineShaftBlockEntity::new)
      .instance(() -> SingleRotatingInstance::new, false)
      .validBlocks(new NonNullSupplier[]{BlockRegistry.POWERED_ENGINE_SHAFT})
      .renderer(renderer(KineticBlockEntityRenderer::new))
      .register();
   public static final BlockEntityEntry<BasinLidBlockEntity> BASIN_LID = CreateDieselGenerators.REGISTRATE
      .blockEntity("basin_lid_tile_entity", BasinLidBlockEntity::new)
      .validBlocks(new NonNullSupplier[]{BlockRegistry.BASIN_LID})
      .renderer(renderer(BasinLidRenderer::new))
      .register();
   public static final BlockEntityEntry<PumpjackBearingBlockEntity> PUMPJACK_BEARING = CreateDieselGenerators.REGISTRATE
      .blockEntity("pumpjack_bearing_block_entity", PumpjackBearingBlockEntity::new)
      .instance(() -> NoShaftBearingInstance::new)
      .validBlocks(new NonNullSupplier[]{BlockRegistry.PUMPJACK_BEARING})
      .renderer(renderer(NoShaftBearingRenderer::new))
      .register();
   public static final BlockEntityEntry<CanisterBlockEntity> CANISTER = CreateDieselGenerators.REGISTRATE
      .blockEntity("canister_block_entity", CanisterBlockEntity::new)
      .validBlocks(new NonNullSupplier[]{BlockRegistry.CANISTER})
      .register();
   public static final BlockEntityEntry<DistillationTankBlockEntity> DISTILLATION_TANK = CreateDieselGenerators.REGISTRATE
      .blockEntity("distillation_tank_block_entity", DistillationTankBlockEntity::new)
      .validBlocks(new NonNullSupplier[]{BlockRegistry.DISTILLATION_TANK})
      .renderer(renderer(DistillationTankRenderer::new))
      .register();
   public static final BlockEntityEntry<OilBarrelBlockEntity> OIL_BARREL = CreateDieselGenerators.REGISTRATE
      .blockEntity("oil_barrel_block_entity", OilBarrelBlockEntity::new)
      .validBlocks(new NonNullSupplier[]{BlockRegistry.OIL_BARREL})
      .register();
   public static final BlockEntityEntry<PumpjackHoleBlockEntity> PUMPJACK_HOLE = CreateDieselGenerators.REGISTRATE
      .blockEntity("pumpjack_hole_block_entity", PumpjackHoleBlockEntity::new)
      .validBlocks(new NonNullSupplier[]{BlockRegistry.PUMPJACK_HOLE})
      .renderer(renderer(PumpjackHoleRenderer::new))
      .register();
   public static final BlockEntityEntry<PumpjackCrankBlockEntity> PUMPJACK_CRANK = CreateDieselGenerators.REGISTRATE
      .blockEntity("pumpjack_crank_block_entity", PumpjackCrankBlockEntity::new)
      .instance(() -> PumpjackCrankInstance::new)
      .validBlocks(new NonNullSupplier[]{BlockRegistry.PUMPJACK_CRANK})
      .renderer(renderer(PumpjackCrankRenderer::new))
      .register();

   public static void register() {
   }

   private static <T extends BlockEntity> NonNullSupplier<NonNullFunction<Context, BlockEntityRenderer<? super T>>> renderer(
         NonNullFunction<Context, BlockEntityRenderer<? super T>> factory) {
      return () -> factory;
   }
}
