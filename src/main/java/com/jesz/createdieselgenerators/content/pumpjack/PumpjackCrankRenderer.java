package com.jesz.createdieselgenerators.content.pumpjack;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.utility.AngleHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;

public class PumpjackCrankRenderer extends ShaftRenderer<PumpjackCrankBlockEntity> {
    public PumpjackCrankRenderer(Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(PumpjackCrankBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        if (Backend.canUseInstancing(be.getLevel()))
            return;
        var state = be.getBlockState();
        float angle = AngleHelper.angleLerp(partialTicks, be.prevAngle, be.angle);
        boolean xAxis = state.getValue(HorizontalKineticBlock.HORIZONTAL_FACING).getAxis() == Direction.Axis.X;
        double radians = Math.toRadians((xAxis ? angle : -angle) + 90);
        double radius = be.crankSize.getValue() == 0 ? .8125 : 1.125;
        double sin = Math.sin(radians) * radius;
        double cos = Math.cos(radians) * radius;
        var crank = CachedBufferer.partial(be.crankSize.getValue() == 0
                ? CDGPartialModels.PUMPJACK_CRANK_SMALL : CDGPartialModels.PUMPJACK_CRANK_LARGE, state);
        var rod = CachedBufferer.partial(be.crankSize.getValue() == 0
                ? CDGPartialModels.PUMPJACK_CRANK_ROD_SMALL : CDGPartialModels.PUMPJACK_CRANK_ROD_LARGE, state);
        double rodAngle = PumpjackCrankGeometry.rodAngle(be, angle, partialTicks);
        if (xAxis) {
            crank.translate(.5, 1.25, 0).rotateZ(angle);
            rod.translate(.5, 1.25, 0).translate(cos, sin, 0).rotateZ(rodAngle);
        } else {
            crank.translate(0, 1.25, .5).rotateY(90).rotateZ(angle);
            rod.translate(0, 1.25, .5).translate(0, sin, cos).rotateY(90).rotateZ(rodAngle);
        }
        rod.light(light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
        crank.light(light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
    }
}
