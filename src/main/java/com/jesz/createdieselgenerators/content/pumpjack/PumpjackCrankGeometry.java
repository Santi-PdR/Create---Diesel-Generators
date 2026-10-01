package com.jesz.createdieselgenerators.content.pumpjack;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import net.minecraft.core.Direction;

/** Shared by the Flywheel instance and the non-instanced renderer. */
public final class PumpjackCrankGeometry {
    private PumpjackCrankGeometry() {}

    public static double rodAngle(PumpjackCrankBlockEntity be, float angle, float partialTicks) {
        boolean xAxis = be.getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING).getAxis() == Direction.Axis.X;
        double radians = Math.toRadians((xAxis ? angle : -angle) + 90);
        double radius = be.crankSize.getValue() == 0 ? .8125 : 1.125;
        double sin = Math.sin(radians) * radius;
        double cos = Math.cos(radians) * radius;
        double targetHorizontal = xAxis ? be.getBlockPos().getX() : be.getBlockPos().getZ();
        double targetY = -1000;
        if (be.bearingPos != null) {
            PumpjackBearingBlockEntity bearing = be.bearing.get();
            float bearingAngle = bearing == null ? 0 : bearing.getInterpolatedAngle(partialTicks);
            double bearingRadians = Math.toRadians(xAxis ? bearingAngle : -bearingAngle);
            var location = be.crankBearingLocation;
            targetHorizontal = location.x * Math.cos(bearingRadians) - location.y * Math.sin(bearingRadians)
                    + .5 + (xAxis ? be.bearingPos.getX() : be.bearingPos.getZ());
            targetY = location.x * Math.sin(bearingRadians) + location.y * Math.cos(bearingRadians)
                    + .5 + be.bearingPos.getY();
        }
        double deltaY = targetY - sin - 1.25 - be.getBlockPos().getY();
        double deltaHorizontal = targetHorizontal - cos - .5
                - (xAxis ? be.getBlockPos().getX() : be.getBlockPos().getZ());
        return xAxis ? Math.toDegrees(Math.atan2(deltaY, deltaHorizontal)) - 90
                : Math.toDegrees(Math.atan2(deltaHorizontal, deltaY));
    }
}
