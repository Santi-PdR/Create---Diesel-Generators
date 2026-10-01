package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.CDGRegistries;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Monster {
    protected CreeperMixin(EntityType<? extends Monster> type, Level level) { super(type, level); }

    @Shadow public abstract void ignite();

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    public void mobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir){
        ItemStack stackInHand = player.getItemInHand(hand);
        if (!CDGItems.LIGHTER.isIn(stackInHand))
            return;
        IFluidHandlerItem fluid = stackInHand.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (fluid == null || fluid.getFluidInTank(0).isEmpty()
                || FuelType.getTypeFor(level().registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE),
                        fluid.getFluidInTank(0).getFluid()).normal().speed() == 0)
            return;
        if (!level().isClientSide) {
            fluid.drain(1, IFluidHandler.FluidAction.EXECUTE);
            ignite();
        }
        level().playSound(player, getX(), getY(), getZ(), SoundEvents.FLINTANDSTEEL_USE,
                getSoundSource(), 1.0F, random.nextFloat() * 0.4F + 0.8F);
        cir.setReturnValue(InteractionResult.sidedSuccess(level().isClientSide));
    }
}
