package com.jesz.createdieselgenerators.content.diesel_engine.huge;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class HugeDieselEngineRenderer extends SafeBlockEntityRenderer<HugeDieselEngineBlockEntity> {
    public HugeDieselEngineRenderer(Context context) {}

    @Override protected void renderSafe(HugeDieselEngineBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
        if (Backend.canUseInstancing(be.getLevel())) return;
        Float angle = be.getTargetAngle();
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(HugeDieselEngineBlock.FACING);
        VertexConsumer vb = buffers.getBuffer(RenderType.solid());
        if (angle == null) {
            transformed(CDGPartialModels.ENGINE_PISTON, state, facing, false).translate(0, .53475, 0).light(light).renderInto(ms, vb);
            return;
        }
        PoweredEngineShaftBlockEntity shaft = be.getShaft();
        if (shaft == null) {
            transformed(CDGPartialModels.ENGINE_PISTON, state, facing, false).translate(0, .53475, 0).light(light).renderInto(ms, vb);
            return;
        }
        Axis facingAxis = facing.getAxis();
        Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
        boolean roll90 = facingAxis.isHorizontal() && axis == Axis.Y || facingAxis.isVertical() && axis == Axis.Z;
        float rotation = facing == Direction.DOWN ? -90 : facing == Direction.UP ? 90 : facing == Direction.WEST ? -90 : facing == Direction.EAST ? 90 : 0;
        if (roll90) rotation = facing == Direction.NORTH ? 180 : facing == Direction.SOUTH ? 0 : facing == Direction.EAST ? -90 : facing == Direction.WEST ? 90 : 0;
        angle += rotation * Mth.DEG_TO_RAD;
        float sign = facingAxis == Axis.Y ? -1 : 1;
        float sine = Mth.sin(angle) * sign;
        float sine2 = Mth.sin(angle - Mth.HALF_PI) * sign;
        float piston = (1 - sine) / 4 + .4375f;
        transformed(CDGPartialModels.ENGINE_PISTON, state, facing, roll90).translate(0, piston, 0).light(light).renderInto(ms, vb);
        transformed(CDGPartialModels.ENGINE_PISTON_LINKAGE, state, facing, roll90).translate(0, 1, 0).translate(0, piston, 0).translate(0, .25, .5).rotateX(sine2 * 23).translate(0, -.25, -.5).light(light).renderInto(ms, vb);
        if (shaft.isEngineForConnectorDisplay(be.getBlockPos()))
            transformed(CDGPartialModels.ENGINE_PISTON_CONNECTOR, state, facing, roll90).translate(0, 2, 0).rotateXRadians(-angle + Mth.HALF_PI - (facingAxis.isVertical() ? Mth.PI : 0)).light(light).renderInto(ms, vb);
    }

    private SuperByteBuffer transformed(PartialModel model, BlockState state, Direction facing, boolean roll90) {
        return CachedBufferer.partial(model, state).centre().rotateY(AngleHelper.horizontalAngle(facing)).rotateX(AngleHelper.verticalAngle(facing) + 90).rotateY(roll90 ? -90 : 0);
    }
    @Override public int getViewDistance() { return 128; }
}
