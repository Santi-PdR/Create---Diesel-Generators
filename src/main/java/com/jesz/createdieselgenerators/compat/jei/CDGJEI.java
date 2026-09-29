package com.jesz.createdieselgenerators.compat.jei;

import com.google.common.collect.ImmutableList;
import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import com.jesz.createdieselgenerators.other.CDGFuelType;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.jesz.createdieselgenerators.recipes.DistillationRecipe;
import com.jesz.createdieselgenerators.recipes.RecipeRegistry;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.CreateJEI;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.ItemIcon;
import com.simibubi.create.compat.jei.SlotMover;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory.Factory;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory.Info;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.foundation.config.ConfigBase.ConfigBool;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CRecipes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

@ParametersAreNonnullByDefault
@JeiPlugin
public class CDGJEI implements IModPlugin {
   private static final ResourceLocation ID = new ResourceLocation("createdieselgenerators", "jei_plugin");
   private final List<CreateRecipeCategory<?>> allCategories = new ArrayList<>();

   public ResourceLocation getPluginUid() {
      return ID;
   }

   private void loadCategories() {
      this.allCategories.clear();
      CreateRecipeCategory<?> basin_fermenting = this.builder(BasinRecipe.class)
         .addTypedRecipes(RecipeRegistry.BASIN_FERMENTING)
         .catalyst(BlockRegistry.BASIN_LID::get)
         .catalyst(AllBlocks.BASIN::get)
         .doubleItemIcon((ItemLike)AllBlocks.BASIN.get(), (ItemLike)BlockRegistry.BASIN_LID.get())
         .emptyBackground(177, 100)
         .build("basin_fermenting", BasinFermentingCategory::new);
      CreateRecipeCategory<?> distillation = this.builder(DistillationRecipe.class)
         .addTypedRecipes(RecipeRegistry.DISTILLATION)
         .catalyst(AllBlocks.FLUID_TANK::get)
         .catalyst(ItemRegistry.DISTILLATION_CONTROLLER::get)
         .doubleItemIcon((ItemLike)AllBlocks.FLUID_TANK.get(), (ItemLike)ItemRegistry.DISTILLATION_CONTROLLER.get())
         .emptyBackground(177, 200)
         .build("distillation", DistillationCategory::new);
   }

   private <T extends Recipe<?>> CDGJEI.CategoryBuilder<T> builder(Class<? extends T> recipeClass) {
      return new CDGJEI.CategoryBuilder<>(recipeClass);
   }

   public void registerCategories(IRecipeCategoryRegistration registration) {
      registration.addRecipeCategories(new IRecipeCategory[]{new DieselEngineCategory(registration.getJeiHelpers().getGuiHelper())});
      this.loadCategories();
      registration.addRecipeCategories(this.allCategories.toArray(IRecipeCategory[]::new));
   }

   public void registerRecipes(IRecipeRegistration registration) {
      this.allCategories.forEach(c -> c.registerRecipes(registration));
      if ((Boolean)ConfigRegistry.DIESEL_ENGINE_IN_JEI.get()) {
         FuelTypeManager.tryPopulateTags();

         for (Entry<Fluid, CDGFuelType> entry : FuelTypeManager.fuelTypes.entrySet()) {
            if (entry.getKey().isSource(entry.getKey().defaultFluidState())) {
               registration.addRecipes(DieselEngineJeiRecipeType.DIESEL_COMBUSTION, ImmutableList.of(new DieselEngineJeiRecipeType(entry.getKey())));
            }
         }
      }
   }

   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      this.allCategories.forEach(c -> c.registerCatalysts(registration));
      if ((Boolean)ConfigRegistry.NORMAL_ENGINES.get()) {
         registration.addRecipeCatalyst(
            BlockRegistry.DIESEL_ENGINE.asStack(), new mezz.jei.api.recipe.RecipeType[]{DieselEngineJeiRecipeType.DIESEL_COMBUSTION}
         );
      }

      if ((Boolean)ConfigRegistry.MODULAR_ENGINES.get()) {
         registration.addRecipeCatalyst(
            BlockRegistry.MODULAR_DIESEL_ENGINE.asStack(), new mezz.jei.api.recipe.RecipeType[]{DieselEngineJeiRecipeType.DIESEL_COMBUSTION}
         );
      }

      if ((Boolean)ConfigRegistry.HUGE_ENGINES.get()) {
         registration.addRecipeCatalyst(
            BlockRegistry.HUGE_DIESEL_ENGINE.asStack(), new mezz.jei.api.recipe.RecipeType[]{DieselEngineJeiRecipeType.DIESEL_COMBUSTION}
         );
      }
   }

   public void registerGuiHandlers(IGuiHandlerRegistration registration) {
      registration.addGenericGuiContainerHandler(AbstractSimiContainerScreen.class, new SlotMover());
   }

   private class CategoryBuilder<T extends Recipe<?>> {
      private final Class<? extends T> recipeClass;
      private Predicate<CRecipes> predicate = cRecipes -> true;
      private IDrawable background;
      private IDrawable icon;
      private final List<Consumer<List<T>>> recipeListConsumers = new ArrayList<>();
      private final List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>();

      public CategoryBuilder(Class<? extends T> recipeClass) {
         this.recipeClass = recipeClass;
      }

      public CDGJEI.CategoryBuilder<T> enableIf(Predicate<CRecipes> predicate) {
         this.predicate = predicate;
         return this;
      }

      public CDGJEI.CategoryBuilder<T> enableWhen(Function<CRecipes, ConfigBool> configValue) {
         this.predicate = c -> configValue.apply(c).get();
         return this;
      }

      public CDGJEI.CategoryBuilder<T> addRecipeListConsumer(Consumer<List<T>> consumer) {
         this.recipeListConsumers.add(consumer);
         return this;
      }

      public CDGJEI.CategoryBuilder<T> addRecipes(Supplier<Collection<? extends T>> collection) {
         return this.addRecipeListConsumer(recipes -> recipes.addAll(collection.get()));
      }

      public CDGJEI.CategoryBuilder<T> addAllRecipesIf(Predicate<Recipe<?>> pred) {
         return this.addRecipeListConsumer(recipes -> CreateJEI.consumeAllRecipes(recipe -> {
            if (pred.test(recipe)) {
               recipes.add((T)recipe);
            }
         }));
      }

      public CDGJEI.CategoryBuilder<T> addAllRecipesIf(Predicate<Recipe<?>> pred, Function<Recipe<?>, T> converter) {
         return this.addRecipeListConsumer(recipes -> CreateJEI.consumeAllRecipes(recipe -> {
            if (pred.test(recipe)) {
               recipes.add(converter.apply(recipe));
            }
         }));
      }

      public CDGJEI.CategoryBuilder<T> addTypedRecipes(IRecipeTypeInfo recipeTypeEntry) {
         return this.addTypedRecipes(recipeTypeEntry::getType);
      }

      public CDGJEI.CategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<? extends T>> recipeType) {
         return this.addRecipeListConsumer(recipes -> CreateJEI.consumeTypedRecipes(recipe -> recipes.add((T)recipe), recipeType.get()));
      }

      public CDGJEI.CategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<? extends T>> recipeType, Function<Recipe<?>, T> converter) {
         return this.addRecipeListConsumer(recipes -> CreateJEI.consumeTypedRecipes(recipe -> recipes.add(converter.apply(recipe)), recipeType.get()));
      }

      public CDGJEI.CategoryBuilder<T> addTypedRecipesIf(Supplier<RecipeType<? extends T>> recipeType, Predicate<Recipe<?>> pred) {
         return this.addRecipeListConsumer(recipes -> CreateJEI.consumeTypedRecipes(recipe -> {
            if (pred.test(recipe)) {
               recipes.add((T)recipe);
            }
         }, recipeType.get()));
      }

      public CDGJEI.CategoryBuilder<T> addTypedRecipesExcluding(Supplier<RecipeType<? extends T>> recipeType, Supplier<RecipeType<? extends T>> excluded) {
         return this.addRecipeListConsumer(recipes -> {
            List<Recipe<?>> excludedRecipes = CreateJEI.getTypedRecipes(excluded.get());
            CreateJEI.consumeTypedRecipes(recipe -> {
               for (Recipe<?> excludedRecipe : excludedRecipes) {
                  if (CreateJEI.doInputsMatch(recipe, excludedRecipe)) {
                     return;
                  }
               }

               recipes.add((T)recipe);
            }, recipeType.get());
         });
      }

      public CDGJEI.CategoryBuilder<T> removeRecipes(Supplier<RecipeType<? extends T>> recipeType) {
         return this.addRecipeListConsumer(recipes -> {
            List<Recipe<?>> excludedRecipes = CreateJEI.getTypedRecipes(recipeType.get());
            recipes.removeIf(recipe -> {
               for (Recipe<?> excludedRecipe : excludedRecipes) {
                  if (CreateJEI.doInputsMatch(recipe, excludedRecipe) && CreateJEI.doOutputsMatch(recipe, excludedRecipe)) {
                     return true;
                  }
               }

               return false;
            });
         });
      }

      public CDGJEI.CategoryBuilder<T> catalystStack(Supplier<ItemStack> supplier) {
         this.catalysts.add(supplier);
         return this;
      }

      public CDGJEI.CategoryBuilder<T> catalyst(Supplier<ItemLike> supplier) {
         return this.catalystStack(() -> new ItemStack(supplier.get().asItem()));
      }

      public CDGJEI.CategoryBuilder<T> icon(IDrawable icon) {
         this.icon = icon;
         return this;
      }

      public CDGJEI.CategoryBuilder<T> itemIcon(ItemLike item) {
         this.icon(new ItemIcon(() -> new ItemStack(item)));
         return this;
      }

      public CDGJEI.CategoryBuilder<T> doubleItemIcon(ItemLike item1, ItemLike item2) {
         this.icon(new DoubleItemIcon(() -> new ItemStack(item1), () -> new ItemStack(item2)));
         return this;
      }

      public CDGJEI.CategoryBuilder<T> background(IDrawable background) {
         this.background = background;
         return this;
      }

      public CDGJEI.CategoryBuilder<T> emptyBackground(int width, int height) {
         this.background(new EmptyBackground(width, height));
         return this;
      }

      public CreateRecipeCategory<T> build(String name, Factory<T> factory) {
         Supplier<List<T>> recipesSupplier;
         if (this.predicate.test(AllConfigs.server().recipes)) {
            recipesSupplier = () -> {
               List<T> recipes = new ArrayList<>();

               for (Consumer<List<T>> consumer : this.recipeListConsumers) {
                  consumer.accept(recipes);
               }

               return recipes;
            };
         } else {
            recipesSupplier = () -> Collections.emptyList();
         }

         Info<T> info = new Info<>(
            new mezz.jei.api.recipe.RecipeType(new ResourceLocation("createdieselgenerators", name), this.recipeClass),
            Components.translatable("createdieselgenerators.recipe." + name),
            this.background,
            this.icon,
            recipesSupplier,
            this.catalysts
         );
         CreateRecipeCategory<T> category = factory.create(info);
         CDGJEI.this.allCategories.add(category);
         return category;
      }
   }
}
