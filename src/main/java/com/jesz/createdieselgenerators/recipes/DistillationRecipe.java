package com.jesz.createdieselgenerators.recipes;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.foundation.item.SmartInventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DistillationRecipe extends ProcessingRecipe<SmartInventory> {
   public DistillationRecipe(ProcessingRecipeParams params) {
      super(RecipeRegistry.DISTILLATION, params);
   }

   @Override
   protected int getMaxInputCount() {
      return 0;
   }

   @Override
   protected int getMaxOutputCount() {
      return 0;
   }

   @Override
   protected int getMaxFluidInputCount() {
      return 1;
   }

   @Override
   protected int getMaxFluidOutputCount() {
      return 6;
   }

   @Override
   protected boolean canRequireHeat() {
      return true;
   }

   @Override
   protected boolean canSpecifyDuration() {
      return true;
   }

   public boolean matches(SmartInventory p_44002_, Level p_44003_) {
      return false;
   }

   @Override
   public net.minecraft.world.item.crafting.RecipeType<?> getType() {
      return RecipeRegistry.DISTILLATION.getType();
   }

   @Override
   public net.minecraft.world.item.crafting.RecipeSerializer<?> getSerializer() {
      return RecipeRegistry.DISTILLATION.getSerializer();
   }

   @Override
   public ResourceLocation getId() {
      return new ResourceLocation("createdieselgenerators", "distillation");
   }

   @Override
   public ItemStack getResultItem(RegistryAccess registryAccess) {
      return ItemStack.EMPTY;
   }

   @Override
   public boolean canCraftInDimensions(int width, int height) {
      return false;
   }

   @Override
   public ItemStack assemble(SmartInventory inventory, RegistryAccess registryAccess) {
      return ItemStack.EMPTY;
   }
}
