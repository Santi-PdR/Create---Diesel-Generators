package com.jesz.createdieselgenerators.ponder;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGItems;
import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.infrastructure.ponder.AllPonderTags;
import com.tterrag.registrate.util.entry.ItemProviderEntry;

public class PonderIndex {
   static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper("createdieselgenerators");

   public static void register() {
      HELPER.forComponents(CDGItems.DISTILLATION_CONTROLLER).addStoryBoard("distillation_tower", DistillationScenes::distillation);
      HELPER.forComponents(CDGBlocks.DIESEL_ENGINE).addStoryBoard("diesel_engine", DieselEngineScenes::small);
      HELPER.forComponents(CDGBlocks.DIESEL_ENGINE).addStoryBoard("engine_silencer", DieselEngineScenes::silencer);
      HELPER.forComponents(CDGBlocks.MODULAR_DIESEL_ENGINE).addStoryBoard("large_diesel_engine", DieselEngineScenes::modular);
      HELPER.forComponents(CDGBlocks.MODULAR_DIESEL_ENGINE).addStoryBoard("engine_silencer", DieselEngineScenes::silencer);
      HELPER.forComponents(CDGItems.ENGINE_SILENCER).addStoryBoard("engine_silencer", DieselEngineScenes::silencer);
      HELPER.forComponents(CDGBlocks.BASIN_LID).addStoryBoard("basin_fermenting_station", BasinScenes::basin_lid);
      HELPER.forComponents(CDGBlocks.HUGE_DIESEL_ENGINE).addStoryBoard("huge_diesel_engine", DieselEngineScenes::huge);
      HELPER.forComponents(CDGBlocks.PUMPJACK_BEARING, CDGBlocks.PUMPJACK_CRANK, CDGBlocks.PUMPJACK_HEAD)
         .addStoryBoard("pumpjack", OilScenes::pumpjack);
      PonderRegistry.TAGS
         .forTag(AllPonderTags.KINETIC_SOURCES)
         .add((ItemProviderEntry<?>)CDGBlocks.DIESEL_ENGINE)
         .add((ItemProviderEntry<?>)CDGBlocks.MODULAR_DIESEL_ENGINE)
         .add((ItemProviderEntry<?>)CDGBlocks.HUGE_DIESEL_ENGINE);
      PonderRegistry.TAGS.forTag(AllPonderTags.KINETIC_APPLIANCES).add((ItemProviderEntry<?>)CDGBlocks.BASIN_LID);
      PonderRegistry.TAGS
         .forTag(AllPonderTags.DISPLAY_SOURCES)
         .add((ItemProviderEntry<?>)CDGBlocks.DIESEL_ENGINE)
         .add((ItemProviderEntry<?>)CDGBlocks.MODULAR_DIESEL_ENGINE);
   }
}
