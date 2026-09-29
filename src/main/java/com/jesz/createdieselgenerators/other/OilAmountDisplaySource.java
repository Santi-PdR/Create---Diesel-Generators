package com.jesz.createdieselgenerators.other;

import com.jesz.createdieselgenerators.blocks.entity.PumpjackHoleBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import java.util.List;
import net.minecraft.network.chat.MutableComponent;

public class OilAmountDisplaySource extends DisplaySource {
   @Override
   public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
      return context.getSourceBlockEntity() instanceof PumpjackHoleBlockEntity sourceBE
         ? List.of(
            Components.translatable("createdieselgenerators.display_source.pumpjack_hole_source").append(" : "),
            Lang.number(sourceBE.oilAmount).add(Lang.translate("generic.unit.buckets")).component()
         )
         : List.of();
   }
}
