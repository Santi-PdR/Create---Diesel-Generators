package com.jesz.createdieselgenerators.content.canister;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class CanisterRenderer extends SmartBlockEntityRenderer<CanisterBlockEntity> {
    public CanisterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(CanisterBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);
        if(blockEntity.capacityEnchantLevel == 0)
            return;
        CachedBufferer.block(blockEntity.getBlockState())
                
                .scale(1)
                
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
        CachedBufferer.block(blockEntity.getBlockState())
                
                .scale(1)
                
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.glint()));
    }
}
