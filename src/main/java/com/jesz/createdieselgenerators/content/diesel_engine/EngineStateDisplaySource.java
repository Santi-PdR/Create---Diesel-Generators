package com.jesz.createdieselgenerators.content.diesel_engine;

import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import java.util.List;

public class EngineStateDisplaySource extends DisplaySource {
    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        GeneratingKineticBlockEntity engine = null;
        if (context.getSourceBlockEntity() instanceof DieselEngineBlockEntity diesel)
            engine = diesel;
        else if (context.getSourceBlockEntity() instanceof ModularDieselEngineBlockEntity modular)
            engine = modular.isController() ? modular : modular.getControllerBE();
        if (!(engine instanceof IEngine fuelEngine) || !fuelEngine.validFS())
            return List.of(Component.translatable("createdieselgenerators.display_source.engine_status").append(" : "),
                    Component.translatable("createdieselgenerators.display_source.idle"));
        float speed = engine instanceof DieselEngineBlockEntity diesel ? diesel.getGeneratedSpeed() : ((ModularDieselEngineBlockEntity) engine).getGeneratedSpeed();
        float stress = Math.abs(engine.calculateAddedStressCapacity() * speed);
        return List.of(Component.translatable("createdieselgenerators.display_source.engine_status").append(" : "),
                Component.translatable("createdieselgenerators.display_source.speed").append(String.valueOf(Math.abs(speed))).append(Lang.translate("generic.unit.rpm").component()),
                Component.translatable("createdieselgenerators.display_source.stress").append(String.valueOf(stress)).append(Lang.translate("generic.unit.stress").component()));
    }
}
