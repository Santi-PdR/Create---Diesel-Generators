package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelBlockEntity;
import com.simibubi.create.content.contraptions.MountedFluidStorage;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MountedFluidStorage.class)
public abstract class MountedFluidStorageMixin {
    @Shadow(remap = false) protected abstract void onFluidStackChanged(FluidStack stack);

    @Inject(method = "canUseAsStorage(Lnet/minecraft/world/level/block/entity/BlockEntity;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void canUseAsStorage(BlockEntity be, CallbackInfoReturnable<Boolean> cir) {
        if (be instanceof OilBarrelBlockEntity oil && oil.isController())
            cir.setReturnValue(true);
    }

    @Inject(method = "createMountedTank(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lcom/simibubi/create/foundation/fluid/SmartFluidTank;", at = @At("HEAD"), cancellable = true, remap = false)
    private void createMountedTank(BlockEntity be, CallbackInfoReturnable<SmartFluidTank> cir) {
        if (be instanceof OilBarrelBlockEntity oil)
            cir.setReturnValue(new SmartFluidTank(oil.getTotalTankSize() * OilBarrelBlockEntity.getCapacityMultiplier(), this::onFluidStackChanged));
    }
}
