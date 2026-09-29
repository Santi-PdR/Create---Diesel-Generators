package com.jesz.createdieselgenerators.contraption;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.PumpjackBearingBBlock;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackBearingBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackHoleBlockEntity;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class PumpjackHeadMovementBehaviour implements MovementBehaviour {
   BlockPos holePos;
   BlockPos headPos;

   @Nullable
   @Override
   public ItemStack canBeDisabledVia(MovementContext context) {
      return null;
   }

   @Override
   public boolean isActive(MovementContext context) {
      if (!(context.contraption instanceof BearingContraption)) {
         return false;
      } else {
         return ((BearingContraption)context.contraption).getFacing().getAxis() != Axis.Y
               && ((Direction)context.state.getValue(PumpjackBearingBBlock.FACING)).getAxis()
                  == ((BearingContraption)context.contraption).getFacing().getClockWise().getAxis()
            ? context.world.getBlockEntity(context.contraption.anchor.relative(((BearingContraption)context.contraption).getFacing().getOpposite())) instanceof PumpjackBearingBlockEntity
            : false;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {
      BlockPos hole = NbtUtils.readBlockPos(context.data.getCompound("HolePos"));
      if (context.world.getBlockEntity(hole) instanceof PumpjackHoleBlockEntity) {
         PumpjackBearingBlockEntity bearing = null;
         if (context.world.getBlockEntity(context.contraption.anchor.relative(((BearingContraption)context.contraption).getFacing().getOpposite())) instanceof PumpjackBearingBlockEntity be
            )
          {
            bearing = be;
         }

         if (bearing != null) {
            SuperByteBuffer cover = CachedBufferer.partial(PartialModels.PUMPJACK_ROPE, context.state);
            if (((BearingContraption)context.contraption).getFacing().getOpposite().getAxis() == Axis.X) {
               double zDst = context.position.z - hole.getZ() - 0.5;
               double yDst = context.position.y - hole.getY() - 0.8F;
               float distanceFromHole = (float)Math.sqrt(zDst * zDst + yDst * yDst);
               double angle = -((ControlledContraptionEntity)context.contraption.entity).getAngle(AnimationTickHolder.getPartialTicks())
                  - 180.0 * Math.atan2(yDst, zDst) / Math.PI
                  + 90.0;
               PoseStack ms = matrices.getModel();
               cover.transform(ms)
                  .translate(0.5, 0.5, 0.5)
                  .rotateX(angle)
                  .scale(1.0F, distanceFromHole, 1.0F)
                  .light(matrices.getWorld(), ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld))
                  .renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderType.cutoutMipped()));
            } else {
               double xDst = context.position.x - hole.getX() - 0.5;
               double yDst = context.position.y - hole.getY() - 0.8F;
               float distanceFromHole = (float)Math.sqrt(xDst * xDst + yDst * yDst);
               double angle = -((ControlledContraptionEntity)context.contraption.entity).getAngle(AnimationTickHolder.getPartialTicks())
                  + 180.0 * Math.atan2(yDst, xDst) / Math.PI
                  - 90.0;
               PoseStack ms = matrices.getModel();
               cover.transform(ms)
                  .translate(0.5, 0.5, 0.5)
                  .rotateZ(angle)
                  .scale(1.0F, distanceFromHole, 1.0F)
                  .light(matrices.getWorld(), ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld))
                  .renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderType.cutoutMipped()));
            }
         }
      }
   }

   @Override
   public void tick(MovementContext context) {
      MovementBehaviour.super.tick(context);
      PumpjackBearingBlockEntity bearing = null;
      if (context.world.getBlockEntity(context.contraption.anchor.relative(((BearingContraption)context.contraption).getFacing().getOpposite())) instanceof PumpjackBearingBlockEntity be
         )
       {
         bearing = be;
      }

      if (bearing != null) {
         this.headPos = new BlockPos(
            context.contraption.anchor.getX() + context.localPos.getX(),
            context.contraption.anchor.getY() + context.localPos.getY(),
            context.contraption.anchor.getZ() + context.localPos.getZ()
         );
         this.holePos = this.headPos;

         for (int i = 0; i < 32; i++) {
            if (context.world.getBlockEntity(this.holePos) instanceof PumpjackHoleBlockEntity phbe) {
               break;
            }

            this.holePos = this.holePos.below();
         }

         if (context.world.getBlockEntity(this.holePos) instanceof PumpjackHoleBlockEntity holeBE && bearing.crankSpeed >= 8.0F) {
            holeBE.headPos = ((Direction)bearing.getBlockState().getValue(BlockStateProperties.FACING)).getAxis() == Axis.X
               ? context.localPos.getZ()
               : context.localPos.getX();
            holeBE.bearingPos = ((Direction)bearing.getBlockState().getValue(BlockStateProperties.FACING)).getAxis() == Axis.X
               ? bearing.bearingBPos.getZ()
               : bearing.bearingBPos.getX();
            if ((bearing.crankAngle + 180.0F) % 360.0F < (context.data.getFloat("OldCrankAngle") + 180.0F) % 360.0F) {
               holeBE.tickFluid(bearing.isLarge);
            }
         }

         context.data.putFloat("OldCrankAngle", bearing.crankAngle);
         context.data.put("HolePos", NbtUtils.writeBlockPos(this.holePos));
      }
   }
}
