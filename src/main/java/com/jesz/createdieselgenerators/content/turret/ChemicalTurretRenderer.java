package com.jesz.createdieselgenerators.content.turret;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.jesz.createdieselgenerators.content.entity_filter.EntityFilteringRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class ChemicalTurretRenderer extends KineticBlockEntityRenderer<ChemicalTurretBlockEntity> {
    public ChemicalTurretRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(ChemicalTurretBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        EntityFilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);

        BlockState state = getRenderedBlockState(be);
        RenderType type = getRenderType(be, state);
        renderRotatingBuffer(be, getRotatedModel(be, state), ms, buffer.getBuffer(type), light);

        float horizontalRotation = AngleHelper.angleLerp(partialTicks, be.oldHorizontalRotation, be.horizontalRotation);
        float verticalRotation = AngleHelper.angleLerp(partialTicks, be.oldVerticalRotation, be.verticalRotation);

        CachedBufferer.partial(CDGPartialModels.CHEMICAL_TURRET_CONNECTOR, state)
                
                .rotateY(horizontalRotation)
                
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        CachedBufferer.partial(CDGPartialModels.CHEMICAL_TURRET_BODY, state)
                
                .rotateY(horizontalRotation+180)
                
                .translate(0.5, 1.3125, 0.125)
                .rotateX(verticalRotation)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        if(be.lighterUpgrade)
            CachedBufferer.partial(CDGPartialModels.CHEMICAL_TURRET_LIGHTER, state)
                    
                    .rotateY(horizontalRotation+180)
                    
                    .translate(0.5, 1.3125, 0.125)
                    .rotateX(verticalRotation)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        CachedBufferer.partial(CDGPartialModels.CHEMICAL_TURRET_SMALL_COG, state)
                
                .rotateY(horizontalRotation+180)
                
                .translate(0.5, 1.3125, 0.125)
                .rotateX(verticalRotation)
                .rotateZ((Mth.lerp(partialTicks, AnimationTickHolder.getTicks()-1, AnimationTickHolder.getTicks()))*(be.getSpeed()/32))
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }

    @Override
    protected SuperByteBuffer getRotatedModel(ChemicalTurretBlockEntity be, BlockState state) {
        return CachedBufferer.partial(CDGPartialModels.CHEMICAL_TURRET_COG, state);
    }
}
