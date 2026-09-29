package com.jesz.createdieselgenerators.content.pumpjack;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.materials.FlatLit;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityInstance;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec2;

public class PumpjackCrankInstance extends KineticBlockEntityInstance<PumpjackCrankBlockEntity> implements DynamicInstance {
    final ModelData crank = getTransformMaterial().getModel(CDGPartialModels.PUMPJACK_CRANK_SMALL).createInstance();
    final ModelData rod = getTransformMaterial().getModel(CDGPartialModels.PUMPJACK_CRANK_ROD_SMALL).createInstance();
    final ModelData largeCrank = getTransformMaterial().getModel(CDGPartialModels.PUMPJACK_CRANK_LARGE).createInstance();
    final ModelData largeRod = getTransformMaterial().getModel(CDGPartialModels.PUMPJACK_CRANK_ROD_LARGE).createInstance();
    final RotatingData shaft = setup(getRotatingMaterial().getModel(shaft()).createInstance());

    public PumpjackCrankInstance(MaterialManager materialManager, PumpjackCrankBlockEntity blockEntity) { super(materialManager, blockEntity); }

    @Override public void beginFrame() {
        float angle = AngleHelper.angleLerp(AnimationTickHolder.getPartialTicks(), blockEntity.prevAngle, blockEntity.angle);
        boolean xAxis = blockState.getValue(HorizontalKineticBlock.HORIZONTAL_FACING).getAxis() == Axis.X;
        double v = ((xAxis ? angle : -angle) + 90) / 180 * Math.PI;
        double radius = blockEntity.crankSize.getValue() == 0 ? .8125 : 1.125;
        double sin = Math.sin(v) * radius, cos = Math.cos(v) * radius;
        boolean large = blockEntity.crankSize.getValue() != 0;
        PoseStack pose = new PoseStack();
        TransformStack transform = TransformStack.cast(pose);
        transform.translate(getInstancePosition());
        if (xAxis) transform.translate(.5, 1.25, 0).rotateZ(angle);
        else transform.translate(0, 1.25, .5).rotateY(90).rotateZ(angle);
        (large ? largeCrank : crank).setTransform(pose);
        (large ? crank : largeCrank).setEmptyTransform();
        pose = new PoseStack();
        transform = TransformStack.cast(pose);
        transform.translate(getInstancePosition());
        if (xAxis) transform.translate(.5, 1.25, 0).translate(cos, sin, 0).rotateZ(Math.atan2(-1000 - sin - 1.25 - pos.getY(), -cos - .5) * 180 / Math.PI - 90);
        else transform.translate(0, 1.25, .5).translate(0, sin, cos).rotateY(90).rotateZ(Math.atan2(-cos - .5, -1000 - sin - 1.25 - pos.getY()) * 180 / Math.PI);
        (large ? largeRod : rod).setTransform(pose);
        (large ? rod : largeRod).setEmptyTransform();
    }

    @Override public void update() { updateRotation(shaft); }
    @Override public void updateLight() { relight(pos, new FlatLit[]{shaft, crank, rod, largeCrank, largeRod}); }
    @Override public void remove() { shaft.delete(); crank.delete(); rod.delete(); largeCrank.delete(); largeRod.delete(); }
}
