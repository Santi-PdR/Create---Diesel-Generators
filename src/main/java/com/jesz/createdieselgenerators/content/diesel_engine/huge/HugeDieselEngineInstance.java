package com.jesz.createdieselgenerators.content.diesel_engine.huge;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.materials.FlatLit;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class HugeDieselEngineInstance extends BlockEntityInstance<HugeDieselEngineBlockEntity> implements DynamicInstance {
    protected final ModelData piston = getTransformMaterial().getModel(CDGPartialModels.ENGINE_PISTON).createInstance();
    protected final ModelData connector = getTransformMaterial().getModel(CDGPartialModels.ENGINE_PISTON_CONNECTOR).createInstance();
    protected final ModelData linkage = getTransformMaterial().getModel(CDGPartialModels.ENGINE_PISTON_LINKAGE).createInstance();

    public HugeDieselEngineInstance(MaterialManager materialManager, HugeDieselEngineBlockEntity blockEntity) {
        super(materialManager, blockEntity);
    }

    @Override
    public void beginFrame() {
        Float angle = blockEntity.getTargetAngle();
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.getValue(HugeDieselEngineBlock.FACING);
        Axis facingAxis = facing.getAxis();
        if (angle == null) {
            transformed(piston, facing, false).translate(0, .53475, 0);
            linkage.setEmptyTransform();
            connector.setEmptyTransform();
            return;
        }
        PoweredEngineShaftBlockEntity shaft = blockEntity.getShaft();
        if (shaft == null) {
            transformed(piston, facing, false).translate(0, .53475, 0);
            linkage.setEmptyTransform();
            connector.setEmptyTransform();
            return;
        }
        Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
        boolean roll90 = facingAxis.isHorizontal() && axis == Axis.Y || facingAxis.isVertical() && axis == Axis.Z;
        float shaftRotation = facing == Direction.DOWN ? -90 : facing == Direction.UP ? 90 : facing == Direction.WEST ? -90 : facing == Direction.EAST ? 90 : 0;
        if (roll90)
            shaftRotation = facing == Direction.NORTH ? 180 : facing == Direction.SOUTH ? 0 : facing == Direction.EAST ? -90 : facing == Direction.WEST ? 90 : 0;
        angle += shaftRotation * Mth.DEG_TO_RAD;
        float sign = facingAxis == Axis.Y ? -1 : 1;
        float sine = Mth.sin(angle) * sign;
        float sine2 = Mth.sin(angle - Mth.HALF_PI) * sign;
        float pistonOffset = (1 - sine) / 4 + .4375f;
        transformed(piston, facing, roll90).translate(0, pistonOffset, 0);
        transformed(linkage, facing, roll90).translate(0, 1, 0).translate(0, pistonOffset, 0).translate(0, .25, .5).rotateX(sine2 * 23).translate(0, -.25, -.5);
        if (shaft.isEngineForConnectorDisplay(blockEntity.getBlockPos()))
            transformed(connector, facing, roll90).translate(0, 2, 0).rotateXRadians(-angle + Mth.HALF_PI - (facingAxis.isVertical() ? Mth.PI : 0));
        else
            connector.setEmptyTransform();
    }

    protected ModelData transformed(ModelData data, Direction facing, boolean roll90) {
        return data.loadIdentity().translate(getInstancePosition()).centre()
            .rotateY(AngleHelper.horizontalAngle(facing)).rotateX(AngleHelper.verticalAngle(facing) + 90)
            .rotateY(roll90 ? -90 : 0);
    }

    @Override public void updateLight() { relight(pos, new FlatLit[]{piston, connector, linkage}); }
    @Override protected void remove() { piston.delete(); connector.delete(); linkage.delete(); }
}
