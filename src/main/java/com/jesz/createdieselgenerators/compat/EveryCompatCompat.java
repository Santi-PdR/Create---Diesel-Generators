package com.jesz.createdieselgenerators.compat;

import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType.Finder;

public class EveryCompatCompat {
   public static void init() {
      BlockSetAPI.addBlockTypeFinder(WoodType.class, Finder.simple("createdieselgenerators", "chip_wood", "chip_wood_block", "chip_wood_beam"));
   }
}
