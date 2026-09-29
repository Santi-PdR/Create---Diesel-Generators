package com.jesz.createdieselgenerators.compat.jei;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraftforge.fluids.FluidStack;

final class JEICompat {
    private JEICompat() {}

    static void addFluidSlot(IRecipeLayoutBuilder builder, int x, int y, FluidIngredient ingredient) {
        ((IRecipeSlotBuilder) builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
                .addIngredients(ForgeTypes.FLUID_STACK,
                        CreateRecipeCategory.withImprovedVisibility(ingredient.getMatchingFluidStacks())))
                .addTooltipCallback(CreateRecipeCategory.addFluidTooltip(ingredient.getRequiredAmount()));
    }

    static void addFluidSlot(IRecipeLayoutBuilder builder, int x, int y, FluidStack fluid) {
        ((IRecipeSlotBuilder) builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
                .addIngredient(ForgeTypes.FLUID_STACK, CreateRecipeCategory.withImprovedVisibility(fluid)))
                .addTooltipCallback(CreateRecipeCategory.addFluidTooltip(fluid.getAmount()));
    }
}
