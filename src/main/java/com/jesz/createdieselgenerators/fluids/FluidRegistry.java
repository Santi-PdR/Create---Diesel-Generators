package com.jesz.createdieselgenerators.fluids;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;

public class FluidRegistry {
   public static final FluidEntry<Flowing> PLANT_OIL = CreateDieselGenerators.REGISTRATE
      .fluid(
         "plant_oil", new ResourceLocation("createdieselgenerators:block/plant_oil_still"), new ResourceLocation("createdieselgenerators:block/plant_oil_flow")
      )
      .lang("Plant Oil")
      .properties(b -> b.viscosity(1500).density(500))
      .fluidProperties(p -> p.levelDecreasePerBlock(2).tickRate(25).slopeFindDistance(3).explosionResistance(100.0F))
      .register();
   public static final FluidEntry<Flowing> CRUDE_OIL = CreateDieselGenerators.REGISTRATE
      .fluid(
         "crude_oil", new ResourceLocation("createdieselgenerators:block/crude_oil_still"), new ResourceLocation("createdieselgenerators:block/crude_oil_flow")
      )
      .lang("Crude Oil")
      .properties(b -> b.viscosity(1500).density(100))
      .fluidProperties(p -> p.levelDecreasePerBlock(3).tickRate(25).slopeFindDistance(2).explosionResistance(100.0F))
      .register();
   public static final FluidEntry<Flowing> BIODIESEL = CreateDieselGenerators.REGISTRATE
      .fluid(
         "biodiesel", new ResourceLocation("createdieselgenerators:block/biodiesel_still"), new ResourceLocation("createdieselgenerators:block/biodiesel_flow")
      )
      .lang("Biodiesel")
      .properties(b -> b.viscosity(1500).density(500))
      .fluidProperties(p -> p.levelDecreasePerBlock(2).tickRate(25).slopeFindDistance(3).explosionResistance(100.0F))
      .register();
   public static final FluidEntry<Flowing> DIESEL = CreateDieselGenerators.REGISTRATE
      .fluid("diesel", new ResourceLocation("createdieselgenerators:block/diesel_still"), new ResourceLocation("createdieselgenerators:block/diesel_flow"))
      .lang("Diesel")
      .properties(b -> b.viscosity(1500).density(500))
      .fluidProperties(p -> p.levelDecreasePerBlock(2).tickRate(25).slopeFindDistance(3).explosionResistance(100.0F))
      .register();
   public static final FluidEntry<Flowing> GASOLINE = CreateDieselGenerators.REGISTRATE
      .fluid(
         "gasoline", new ResourceLocation("createdieselgenerators:block/gasoline_still"), new ResourceLocation("createdieselgenerators:block/gasoline_flow")
      )
      .lang("Gasoline")
      .properties(b -> b.viscosity(1500).density(500))
      .fluidProperties(p -> p.levelDecreasePerBlock(2).tickRate(25).slopeFindDistance(3).explosionResistance(100.0F))
      .register();
   public static final FluidEntry<Flowing> ETHANOL = CreateDieselGenerators.REGISTRATE
      .fluid("ethanol", new ResourceLocation("createdieselgenerators:block/ethanol_still"), new ResourceLocation("createdieselgenerators:block/ethanol_flow"))
      .lang("Ethanol")
      .properties(b -> b.viscosity(1500).density(500))
      .fluidProperties(p -> p.levelDecreasePerBlock(2).tickRate(25).slopeFindDistance(5).explosionResistance(100.0F))
      .register();

   public static void register() {
   }
}
