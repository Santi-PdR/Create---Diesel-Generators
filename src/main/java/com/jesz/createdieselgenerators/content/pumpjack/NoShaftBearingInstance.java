package com.jesz.createdieselgenerators.content.pumpjack;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.materials.FlatLit;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.bearing.IBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Quaternionf;

public class NoShaftBearingInstance<B extends KineticBlockEntity & IBearingBlockEntity> extends BlockEntityInstance<B> implements DynamicInstance {
    final OrientedData topInstance;
    final Axis rotationAxis;
    final Quaternionf blockOrientation;

    public NoShaftBearingInstance(MaterialManager materialManager, B blockEntity) {
        super(materialManager, blockEntity);
        Direction facing = blockState.getValue(BlockStateProperties.FACING);
        rotationAxis = Axis.of(Direction.get(AxisDirection.POSITIVE, blockEntity.getBlockState().getValue(DirectionalKineticBlock.FACING).getAxis()).step());
        blockOrientation = getBlockStateOrientation(facing);
        PartialModel top = blockEntity.isWoodenTop() ? AllPartialModels.BEARING_TOP_WOODEN : AllPartialModels.BEARING_TOP;
        topInstance = getOrientedMaterial().getModel(top, blockState).createInstance();
        topInstance.setPosition(getInstancePosition()).setRotation(blockOrientation);
    }

    @Override public void beginFrame() {
        float angle = blockEntity.getInterpolatedAngle(AnimationTickHolder.getPartialTicks() - 1);
        Quaternionf rot = rotationAxis.rotationDegrees(angle);
        rot.mul(blockOrientation);
        topInstance.setRotation(rot);
    }
    @Override public void updateLight() { super.updateLight(); relight(pos, new FlatLit[]{topInstance}); }
    @Override public void remove() { topInstance.delete(); }

    static Quaternionf getBlockStateOrientation(Direction facing) {
        Quaternionf orientation = facing.getAxis().isHorizontal() ? Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(facing.getOpposite())) : new Quaternionf();
        orientation.mul(Axis.XP.rotationDegrees(-90 - AngleHelper.verticalAngle(facing)));
        return orientation;
    }
}
