package com.jesz.createdieselgenerators.compat.computercraft;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.FallbackComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import java.util.function.Function;

public class CCProxy {
   private static Function<SmartBlockEntity, ? extends AbstractComputerBehaviour> fallbackFactory;
   private static Function<SmartBlockEntity, ? extends AbstractComputerBehaviour> computerFactory;

   public static void register() {
      fallbackFactory = FallbackComputerBehaviour::new;
      Mods.COMPUTERCRAFT.executeIfInstalled(() -> CCProxy::registerWithDependency);
   }

   private static void registerWithDependency() {
      computerFactory = ComputerBehaviour::new;
   }

   public static AbstractComputerBehaviour behaviour(SmartBlockEntity sbe) {
      fallbackFactory = FallbackComputerBehaviour::new;
      return computerFactory == null ? fallbackFactory.apply(sbe) : computerFactory.apply(sbe);
   }
}
