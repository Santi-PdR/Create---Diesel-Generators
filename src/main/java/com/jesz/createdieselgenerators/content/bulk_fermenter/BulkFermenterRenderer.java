package com.jesz.createdieselgenerators.content.bulk_fermenter;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.item.SmartInventory;
import com.jozufozu.flywheel.util.transform.TransformStack;
import net.createmod.catnip.data.Iterate;
import com.simibubi.create.foundation.render.CachedBufferer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class BulkFermenterRenderer extends SafeBlockEntityRenderer<BulkFermenterBlockEntity> {
    public BulkFermenterRenderer(BlockEntityRendererProvider.Context context){}

    @Override
    protected void renderSafe(BulkFermenterBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        if (!be.isController())
            return;

        BlockState blockState = be.getBlockState();
        VertexConsumer vb = buffer.getBuffer(RenderType.cutout());
        ms.pushPose();
        TransformStack msr = TransformStack.cast(ms);
        msr.translate(be.getWidth() / 2f, 0.5, be.getWidth() / 2f);

        float dialPivotY = 6f / 16;
        float dialPivotZ = 8f / 16;
        ProcessingRecipe<SmartInventory> r = be.currentRecipe;

        float progress = be.currentRecipe == null ? 0 :
                (float) Mth.clamp(Mth.lerp(partialTicks, be.processingTime + Math.sqrt(be.width * be.height), be.processingTime) / be.currentRecipe.getProcessingDuration(), 0, 1);

        for (Direction d : Iterate.horizontalDirections) {
            ms.pushPose();
            CachedBufferer.partial(CDGPartialModels.BULK_FERMENTER_GAUGE, blockState)
                    .rotateY(d.toYRot())
                    
                    .translate(be.getWidth() / 2f - 6 / 16f, 0, 0)
                    .light(light)
                    .renderInto(ms, vb);
            CachedBufferer.partial(AllPartialModels.BOILER_GAUGE_DIAL, blockState)
                    .rotateY(d.toYRot())
                    
                    .translate(be.width / 2f - 6 / 16f, 0, 0)
                    .translate(0, dialPivotY, dialPivotZ)
                    .rotateX(-180 * progress + 90)
                    .translate(0, -dialPivotY, -dialPivotZ)
                    .light(light)
                    .renderInto(ms, vb);
            ms.popPose();
        }

        ms.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(BulkFermenterBlockEntity be) {
        return be.isController();
    }

}
