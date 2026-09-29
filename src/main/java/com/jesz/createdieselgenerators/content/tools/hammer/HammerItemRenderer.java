package com.jesz.createdieselgenerators.content.tools.hammer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.jozufozu.flywheel.util.transform.TransformStack;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class HammerItemRenderer extends CustomRenderedItemModelRenderer {
    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        Player player = Minecraft.getInstance().player;
        CompoundTag tag = stack.getOrCreateTag();
        if (transformType == ItemDisplayContext.GUI || !tag.contains("ProcessingItem") || player == null)
            renderer.render(model.getOriginalModel(), light);
        else {
            float time = ((AnimationTickHolder.getTicks() + AnimationTickHolder.getPartialTicks()) % 10) / 10;
            time -= 0.5f;

            ItemStack processingItem = ItemStack.of(tag.getCompound("ProcessingItem"));
            if (!transformType.firstPerson()) {
                boolean thirdPerson = transformType.name().startsWith("THIRD_PERSON");
                if (!thirdPerson) {
                    ms.pushPose();
                    itemRenderer.renderStatic(processingItem, ItemDisplayContext.NONE, light, overlay, ms, buffer, Minecraft.getInstance().level, 0);

                    TransformStack.cast(ms)
                            .translate(0.5, -0.2, 0)
                            .scale(0.75f, 0.75f, 1.1f)
                            
                            .rotateZ(Math.abs(time * time * time) * 300)
                            ;
                    renderer.render(model.getOriginalModel(), light);

                    ms.popPose();
                } else {
                    ms.pushPose();
                    TransformStack.cast(ms)
//                            .rotateY(90)
                            .translate(-0.2, 0.4, 0)
                            .scale(0.75f)
                            .rotateY(77);
                    itemRenderer.renderStatic(processingItem, ItemDisplayContext.NONE, light, overlay, ms, buffer, Minecraft.getInstance().level, 0);
                    ms.popPose();
                    ms.pushPose();

                    TransformStack.cast(ms)
                            .rotateY(90)
                            .translate(0, 0, -0.7)
                            .scale(0.75f, 0.75f, 1.1f)
                            .rotateY(77)
                            
                            .rotateZ(Math.abs(time * time * time) * -180 + 80)
                            ;
                    renderer.render(model.getOriginalModel(), light);
                    ms.popPose();
                }
            } else {
                boolean flip = transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
                ms.pushPose();
                TransformStack.cast(ms)
                        .translate(0, 0, flip ? -1 : 1)
                        .translate(0, 0, -0.6)
                        .rotateY(45)
                        
                        .rotateZ(Math.abs(time * time * time) * 400)
                        
                ;
                renderer.render(model.getOriginalModel(), light);
                ms.popPose();
                ms.pushPose();
                TransformStack.cast(ms)
                        .translate(0, 0, flip ? -1 : 1)
                        .translate(-0.5, 0.4, 0)
                        .translate(Math.cos((time) * -Math.PI) / -10, 0, 0)
                        .rotateY(-45)
                ;
                itemRenderer.renderStatic(processingItem, ItemDisplayContext.NONE, light, overlay, ms, buffer, Minecraft.getInstance().level, 0);
                ms.popPose();

            }
        }
    }
}
