package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.blocks.ct.DistillationTankModel;
import com.jesz.createdieselgenerators.blocks.ct.ModularDieselEngineCTBehavior;
import com.jesz.createdieselgenerators.blocks.ct.OilBarrelCTBehavior;
import com.jesz.createdieselgenerators.contraption.DieselEngineMovementBehaviour;
import com.jesz.createdieselgenerators.contraption.PumpjackBearingBMovementBehaviour;
import com.jesz.createdieselgenerators.contraption.PumpjackHeadMovementBehaviour;
import com.jesz.createdieselgenerators.items.CanisterBlockItem;
import com.jesz.createdieselgenerators.items.MultiBlockContainerBlockItem;
import com.jesz.createdieselgenerators.other.EngineStateDisplaySource;
import com.jesz.createdieselgenerators.other.OilAmountDisplaySource;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.redstone.displayLink.AllDisplayBehaviours;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;

public class BlockRegistry {
   public static final BlockEntry<DieselGeneratorBlock> DIESEL_ENGINE = CreateDieselGenerators.REGISTRATE
      .block("diesel_engine", DieselGeneratorBlock::new)
      .properties(p -> p.mapColor(MapColor.COLOR_YELLOW))
      .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
      .onRegister(AllDisplayBehaviours.assignDataBehaviour(new EngineStateDisplaySource()))
      .properties(p -> p.noOcclusion())
      .properties(p -> p.strength(3.0F))
      .onRegister(AllMovementBehaviours.movementBehaviour(new DieselEngineMovementBehaviour()))
      .simpleItem()
      .register();
   public static final BlockEntry<LargeDieselGeneratorBlock> MODULAR_DIESEL_ENGINE = CreateDieselGenerators.REGISTRATE
      .block("large_diesel_engine", LargeDieselGeneratorBlock::new)
      .properties(p -> p.mapColor(MapColor.COLOR_YELLOW))
      .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
      .onRegister(AllDisplayBehaviours.assignDataBehaviour(new EngineStateDisplaySource()))
      .properties(p -> p.noOcclusion())
      .properties(p -> p.strength(3.0F))
      .onRegister(CreateRegistrate.connectedTextures(ModularDieselEngineCTBehavior::new))
      .onRegister(AllMovementBehaviours.movementBehaviour(new DieselEngineMovementBehaviour()))
      .simpleItem()
      .register();
   public static final BlockEntry<HugeDieselEngineBlock> HUGE_DIESEL_ENGINE = CreateDieselGenerators.REGISTRATE
      .block("huge_diesel_engine", HugeDieselEngineBlock::new)
      .properties(p -> p.mapColor(MapColor.COLOR_YELLOW))
      .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
      .onRegister(AllDisplayBehaviours.assignDataBehaviour(new EngineStateDisplaySource()))
      .properties(p -> p.noOcclusion())
      .properties(p -> p.strength(3.0F))
      .simpleItem()
      .register();
   public static final BlockEntry<PoweredEngineShaftBlock> POWERED_ENGINE_SHAFT = ((BlockBuilder)CreateDieselGenerators.REGISTRATE
         .block("powered_engine_shaft", PoweredEngineShaftBlock::new)
         .initialProperties(SharedProperties::stone)
         .properties(p -> p.mapColor(MapColor.METAL))
         .transform(TagGen.pickaxeOnly()))
      .register();
   public static final BlockEntry<BasinLidBlock> BASIN_LID = CreateDieselGenerators.REGISTRATE
      .block("basin_lid", BasinLidBlock::new)
      .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
      .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
      .properties(p -> p.noOcclusion())
      .properties(p -> p.strength(3.0F))
      .simpleItem()
      .register();
   public static final BlockEntry<PumpjackBearingBlock> PUMPJACK_BEARING = CreateDieselGenerators.REGISTRATE
      .block("pumpjack_bearing", PumpjackBearingBlock::new)
      .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
      .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
      .properties(p -> p.noOcclusion())
      .properties(p -> p.strength(3.0F))
      .simpleItem()
      .register();
   public static final BlockEntry<PumpjackHeadBlock> PUMPJACK_HEAD = ((BlockBuilder)CreateDieselGenerators.REGISTRATE
         .block("pumpjack_head", PumpjackHeadBlock::new)
         .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
         .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
         .properties(p -> p.noOcclusion())
         .properties(p -> p.strength(3.0F))
         .onRegister(AllMovementBehaviours.movementBehaviour(new PumpjackHeadMovementBehaviour())))
      .simpleItem()
      .register();
   public static final BlockEntry<PumpjackBearingBBlock> PUMPJACK_BEARING_B = CreateDieselGenerators.REGISTRATE
      .block("pumpjack_bearing_b", PumpjackBearingBBlock::new)
      .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
      .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
      .properties(p -> p.noOcclusion())
      .properties(p -> p.strength(3.0F))
      .onRegister(AllMovementBehaviours.movementBehaviour(new PumpjackBearingBMovementBehaviour()))
      .register();
   public static final BlockEntry<PumpjackHoleBlock> PUMPJACK_HOLE = CreateDieselGenerators.REGISTRATE
      .block("pumpjack_hole", PumpjackHoleBlock::new)
      .properties(p -> p.mapColor(MapColor.COLOR_ORANGE))
      .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
      .onRegister(AllDisplayBehaviours.assignDataBehaviour(new OilAmountDisplaySource()))
      .properties(p -> p.noOcclusion())
      .properties(p -> p.strength(3.0F))
      .simpleItem()
      .register();
   public static final BlockEntry<PumpjackCrankBlock> PUMPJACK_CRANK = CreateDieselGenerators.REGISTRATE
      .block("pumpjack_crank", PumpjackCrankBlock::new)
      .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
      .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
      .properties(p -> p.noOcclusion())
      .properties(p -> p.strength(3.0F))
      .simpleItem()
      .register();
   public static final BlockEntry<CanisterBlock> CANISTER = ((BlockBuilder)CreateDieselGenerators.REGISTRATE
         .block("canister", CanisterBlock::new)
         .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
         .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
         .properties(p -> p.noOcclusion())
         .properties(p -> p.strength(3.0F))
         .item(CanisterBlockItem::new)
         .transform(ModelGen.customItemModel()))
      .register();
   public static final BlockEntry<DistillationTankBlock> DISTILLATION_TANK = ((BlockBuilder)((BlockBuilder)CreateDieselGenerators.REGISTRATE
            .block("distillation_tank", DistillationTankBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(Properties::noOcclusion)
            .properties(p -> p.isRedstoneConductor((p1, p2, p3) -> true))
            .transform(TagGen.pickaxeOnly()))
         .onRegister(CreateRegistrate.blockModel(() -> DistillationTankModel::new)))
      .register();
   public static final BlockEntry<OilBarrelBlock> OIL_BARREL = ((BlockBuilder)((BlockBuilder)((BlockBuilder)CreateDieselGenerators.REGISTRATE
               .block("oil_barrel", OilBarrelBlock::new)
               .initialProperties(SharedProperties::copperMetal)
               .properties(Properties::noOcclusion)
               .properties(p -> p.isRedstoneConductor((p1, p2, p3) -> true))
               .transform(TagGen.pickaxeOnly()))
            .onRegister(CreateRegistrate.connectedTextures(OilBarrelCTBehavior::new)))
         .item((NonNullBiFunction<net.minecraft.world.level.block.Block, net.minecraft.world.item.Item.Properties, MultiBlockContainerBlockItem>)MultiBlockContainerBlockItem::new)
         .build())
      .register();
   public static final BlockEntry<RotatedPillarBlock> CHIP_WOOD_BLOCK = CreateDieselGenerators.REGISTRATE
      .block("chip_wood_block", RotatedPillarBlock::new)
      .initialProperties(() -> Blocks.OAK_PLANKS)
      .properties(p -> p)
      .simpleItem()
      .register();
   public static final BlockEntry<RotatedPillarBlock> CHIP_WOOD_BEAM = CreateDieselGenerators.REGISTRATE
      .block("chip_wood_beam", RotatedPillarBlock::new)
      .initialProperties(() -> Blocks.OAK_PLANKS)
      .properties(p -> p)
      .simpleItem()
      .register();
   public static final BlockEntry<SlabBlock> CHIP_WOOD_SLAB = CreateDieselGenerators.REGISTRATE
      .block("chip_wood_slab", SlabBlock::new)
      .initialProperties(() -> Blocks.OAK_PLANKS)
      .properties(p -> p)
      .simpleItem()
      .register();
   public static final BlockEntry<StairBlock> CHIP_WOOD_STAIRS = CreateDieselGenerators.REGISTRATE
      .block("chip_wood_stairs", p -> new StairBlock(Blocks.ANDESITE_STAIRS::defaultBlockState, p))
      .initialProperties(() -> Blocks.OAK_PLANKS)
      .properties(p -> p)
      .simpleItem()
      .register();
   public static final BlockEntry<Block> ASPHALT_BLOCK = CreateDieselGenerators.REGISTRATE
      .<Block>block("asphalt_block", Block::new)
      .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
      .properties(p -> p.sound(SoundType.STONE))
      .properties(p -> p.noOcclusion())
      .properties(p -> p.strength(3.0F))
      .properties(p -> p.speedFactor(1.25F))
      .simpleItem()
      .register();
   public static final BlockEntry<SlabBlock> ASPHALT_SLAB = CreateDieselGenerators.REGISTRATE
      .block("asphalt_slab", SlabBlock::new)
      .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
      .properties(p -> p.sound(SoundType.STONE))
      .properties(p -> p.noOcclusion())
      .properties(p -> p.strength(3.0F))
      .properties(p -> p.speedFactor(1.25F))
      .simpleItem()
      .register();
   public static final BlockEntry<StairBlock> ASPHALT_STAIRS = CreateDieselGenerators.REGISTRATE
      .block("asphalt_stairs", p -> new StairBlock(Blocks.ANDESITE_STAIRS::defaultBlockState, p))
      .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
      .properties(p -> p.sound(SoundType.STONE))
      .properties(p -> p.noOcclusion())
      .properties(p -> p.strength(3.0F))
      .properties(p -> p.speedFactor(1.25F))
      .simpleItem()
      .register();

   public static void register() {
   }
}
