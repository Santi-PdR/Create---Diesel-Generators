package com.jesz.createdieselgenerators.blocks.renderer;

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
      Direction facing = (Direction)this.blockState.getValue(BlockStateProperties.FACING);
      this.rotationAxis = Axis.of(
         Direction.get(AxisDirection.POSITIVE, ((Direction)blockEntity.getBlockState().getValue(DirectionalKineticBlock.FACING)).getAxis()).step()
      );
      this.blockOrientation = getBlockStateOrientation(facing);
      PartialModel top = blockEntity.isWoodenTop() ? AllPartialModels.BEARING_TOP_WOODEN : AllPartialModels.BEARING_TOP;
      this.topInstance = this.getOrientedMaterial().getModel(top, this.blockState).createInstance();
      this.topInstance.setPosition(this.getInstancePosition()).setRotation(this.blockOrientation);
   }

   @Override
   public void beginFrame() {
      float interpolatedAngle = this.blockEntity.getInterpolatedAngle(AnimationTickHolder.getPartialTicks() - 1.0F);
      Quaternionf rot = this.rotationAxis.rotationDegrees(interpolatedAngle);
      rot.mul(this.blockOrientation);
      this.topInstance.setRotation(rot);
   }

   @Override
   public void updateLight() {
      super.updateLight();
      this.relight(this.pos, new FlatLit[]{this.topInstance});
   }

   @Override
   public void remove() {
      this.topInstance.delete();
   }

   static Quaternionf getBlockStateOrientation(Direction facing) {
      Quaternionf orientation;
      if (facing.getAxis().isHorizontal()) {
         orientation = Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(facing.getOpposite()));
      } else {
         orientation = new Quaternionf();
      }

      orientation.mul(Axis.XP.rotationDegrees(-90.0F - AngleHelper.verticalAngle(facing)));
      return orientation;
   }
}
