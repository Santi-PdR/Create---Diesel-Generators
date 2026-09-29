package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.fluids.FluidRegistry;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreativeTab {
   private static final DeferredRegister<CreativeModeTab> TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "createdieselgenerators");
   public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = TAB_REGISTER.register(
      "cdg_creative_tab",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.cdg_creative_tab"))
         .icon(BlockRegistry.DIESEL_ENGINE::asStack)
         .displayItems((pParameters, output) -> {
            output.accept((ItemLike)ItemRegistry.ENGINE_PISTON.get());
            output.accept((ItemLike)ItemRegistry.ENGINE_SILENCER.get());
            output.accept((ItemLike)ItemRegistry.ENGINE_TURBO.get());
            output.accept((ItemLike)BlockRegistry.DIESEL_ENGINE.get());
            output.accept((ItemLike)BlockRegistry.MODULAR_DIESEL_ENGINE.get());
            output.accept((ItemLike)BlockRegistry.HUGE_DIESEL_ENGINE.get());
            output.accept((ItemLike)ItemRegistry.DISTILLATION_CONTROLLER.get());
            output.accept((ItemLike)ItemRegistry.OIL_SCANNER.get());
            output.accept((ItemLike)BlockRegistry.PUMPJACK_BEARING.get());
            output.accept((ItemLike)BlockRegistry.PUMPJACK_CRANK.get());
            output.accept((ItemLike)BlockRegistry.PUMPJACK_HEAD.get());
            output.accept((ItemLike)ItemRegistry.WOOD_CHIPS.get());
            output.accept((ItemLike)BlockRegistry.CHIP_WOOD_BEAM.get());
            output.accept((ItemLike)BlockRegistry.CHIP_WOOD_BLOCK.get());
            output.accept((ItemLike)BlockRegistry.CHIP_WOOD_STAIRS.get());
            output.accept((ItemLike)BlockRegistry.CHIP_WOOD_SLAB.get());
            output.accept((ItemLike)BlockRegistry.CANISTER.get());
            output.accept((ItemLike)BlockRegistry.OIL_BARREL.get());
            output.accept((ItemLike)BlockRegistry.BASIN_LID.get());
            output.accept((ItemLike)BlockRegistry.ASPHALT_BLOCK.get());
            output.accept((ItemLike)BlockRegistry.ASPHALT_STAIRS.get());
            output.accept((ItemLike)BlockRegistry.ASPHALT_SLAB.get());
            output.accept((ItemLike)FluidRegistry.CRUDE_OIL.getBucket().get());
            output.accept((ItemLike)FluidRegistry.BIODIESEL.getBucket().get());
            output.accept((ItemLike)FluidRegistry.DIESEL.getBucket().get());
            output.accept((ItemLike)FluidRegistry.GASOLINE.getBucket().get());
            output.accept((ItemLike)FluidRegistry.PLANT_OIL.getBucket().get());
            output.accept((ItemLike)FluidRegistry.ETHANOL.getBucket().get());
            output.accept((ItemLike)ItemRegistry.KELP_HANDLE.get());
            output.accept((ItemLike)ItemRegistry.LIGHTER.get());
            output.accept((ItemLike)ItemRegistry.CHEMICAL_SPRAYER.get());
            output.accept((ItemLike)ItemRegistry.CHEMICAL_SPRAYER_LIGHTER.get());
         })
         .build()
   );

   public static void register(IEventBus modEventBus) {
      TAB_REGISTER.register(modEventBus);
   }
}
