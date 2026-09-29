package com.jesz.createdieselgenerators.recipes;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeFactory;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.foundation.utility.Lang;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public enum RecipeRegistry implements IRecipeTypeInfo {
   BASIN_FERMENTING(BasinFermentingRecipe::new),
   DISTILLATION(DistillationRecipe::new);

   private final ResourceLocation id = new ResourceLocation("createdieselgenerators");
   private final RegistryObject<RecipeSerializer<?>> serializerObject;
   @Nullable
   private final RegistryObject<RecipeType<?>> typeObject;
   private final Supplier<RecipeType<?>> type;

   private RecipeRegistry(Supplier<RecipeSerializer<?>> serializerSupplier) {
      String name = Lang.asId(this.name());
      this.serializerObject = RecipeRegistry.Registers.SERIALIZER_REGISTER.register(name, serializerSupplier);
      this.typeObject = RecipeRegistry.Registers.TYPE_REGISTER.register(name, () -> RecipeType.simple(this.id));
      this.type = this.typeObject;
   }

   private RecipeRegistry(ProcessingRecipeFactory<?> processingFactory) {
      this(() -> new ProcessingRecipeSerializer<>(processingFactory));
   }

   public static void register(IEventBus modEventBus) {
      RecipeRegistry.Registers.SERIALIZER_REGISTER.register(modEventBus);
      RecipeRegistry.Registers.TYPE_REGISTER.register(modEventBus);
   }

   @Override
   public ResourceLocation getId() {
      return this.id;
   }

   @Override
   public <T extends RecipeSerializer<?>> T getSerializer() {
      return (T)this.serializerObject.get();
   }

   @Override
   public <T extends RecipeType<?>> T getType() {
      return (T)this.type.get();
   }

   private static class Registers {
      private static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER = DeferredRegister.create(
         ForgeRegistries.RECIPE_SERIALIZERS, "createdieselgenerators"
      );
      private static final DeferredRegister<RecipeType<?>> TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, "createdieselgenerators");
   }
}
