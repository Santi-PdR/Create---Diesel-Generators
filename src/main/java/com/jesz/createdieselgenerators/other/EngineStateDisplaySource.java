package com.jesz.createdieselgenerators.other;

import com.jesz.createdieselgenerators.blocks.entity.DieselGeneratorBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.LargeDieselGeneratorBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.utility.Components;
import java.util.List;
import net.minecraft.network.chat.MutableComponent;

public class EngineStateDisplaySource extends DisplaySource {
   @Override
   public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
      if (context.getSourceBlockEntity() instanceof DieselGeneratorBlockEntity sourceBE) {
         return sourceBE.validFuel
            ? List.of(
               Components.translatable("createdieselgenerators.display_source.engine_status").append(" : "),
               Components.translatable("createdieselgenerators.display_source.speed")
                  .append(Math.abs(sourceBE.getGeneratedSpeed()) + Components.translatable("create.generic.unit.rpm").toString()),
               Components.translatable("createdieselgenerators.display_source.stress")
                  .append(
                     Math.abs(sourceBE.calculateAddedStressCapacity() * sourceBE.getGeneratedSpeed())
                        + Components.translatable("create.generic.unit.stress").toString()
                  )
            )
            : List.of(
               Components.translatable("createdieselgenerators.display_source.engine_status").append(" : "),
               Components.translatable("createdieselgenerators.display_source.idle")
            );
      } else if (context.getSourceBlockEntity() instanceof LargeDieselGeneratorBlockEntity sourceBE) {
         LargeDieselGeneratorBlockEntity frontEngine = sourceBE.frontEngine.get();
         return frontEngine != null && frontEngine.validFuel
            ? List.of(
               Components.translatable("createdieselgenerators.display_source.engine_status").append(" : "),
               Components.translatable("createdieselgenerators.display_source.speed")
                  .append(Math.abs(frontEngine.getGeneratedSpeed()) + Components.translatable("create.generic.unit.rpm").toString()),
               Components.translatable("createdieselgenerators.display_source.stress")
                  .append(
                     Math.abs(frontEngine.calculateAddedStressCapacity() * frontEngine.getGeneratedSpeed())
                        + Components.translatable("create.generic.unit.stress").toString()
                  )
            )
            : List.of(
               Components.translatable("createdieselgenerators.display_source.engine_status").append(" : "),
               Components.translatable("createdieselgenerators.display_source.idle")
            );
      } else {
         return List.of();
      }
   }
}
