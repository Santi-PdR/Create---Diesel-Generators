package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.DistillationTankBlock;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.recipes.DistillationRecipe;
import com.jesz.createdieselgenerators.recipes.RecipeRegistry;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.tank.BoilerHeaters;
import com.simibubi.create.content.fluids.tank.FluidTankBlock.Shape;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer.Fluid;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class DistillationTankBlockEntity extends SmartBlockEntity implements Fluid, IHaveGoggleInformation {
   private static final int MAX_SIZE = 3;
   public float progress;
   public int heat;
   protected LazyOptional<IFluidHandler> fluidCapability;
   protected boolean forceFluidLevelUpdate;
   public FluidTank tankInventory;
   protected BlockPos controller;
   protected BlockPos lastKnownPos;
   protected boolean updateConnectivity;
   public boolean window;
   protected int luminosity;
   protected int width;
   protected int height;
   protected BlockPos bottomCPos;
   private static final int SYNC_RATE = 8;
   protected int syncCooldown;
   protected boolean queuedSync;
   private LerpedFloat fluidLevel;
   public boolean hasDistillationC;
   int processingTime = -1;
   DistillationRecipe currentRecipe;

   public DistillationTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
      this.tankInventory = this.createInventory();
      this.fluidCapability = LazyOptional.of(() -> this.tankInventory);
      this.forceFluidLevelUpdate = true;
      this.updateConnectivity = false;
      this.window = false;
      this.height = 1;
      this.width = 1;
      this.refreshCapability();
   }

   private BlockPos getBottomControllerPos() {
      if (this.isBottom()) {
         return this.getController();
      } else {
         return this.level.getBlockEntity(this.getBlockPos().below()) instanceof DistillationTankBlockEntity be ? be.getBottomControllerPos() : this.getController();
      }
   }

   protected SmartFluidTank createInventory() {
      return new SmartFluidTank(getCapacityMultiplier(), this::onFluidStackChanged);
   }

   public int getTemperature() {
      int width = this.getControllerBE().width;
      int heatN = 0;
      if (this.getController() != null && width != 0) {
         for (int xOffset = 0; xOffset < width; xOffset++) {
            for (int zOffset = 0; zOffset < width; zOffset++) {
               BlockPos pos = this.getController().offset(xOffset, -1, zOffset);
               BlockState blockState = this.level.getBlockState(pos);
               float heat = BoilerHeaters.getActiveHeat(this.level, pos, blockState);
               heatN += (int)heat;
            }
         }

         return Mth.clamp(heatN / (width * width), 0, 100);
      } else {
         return 0;
      }
   }

   public void updateConnectivity() {
      this.updateConnectivity = false;
      if (!this.level.isClientSide) {
         if (this.isController()) {
            ConnectivityHandler.formMulti(this);
         }
      }
   }

   private void startProcessing() {
      if (this.currentRecipe != null) {
         this.processingTime = this.currentRecipe.getProcessingDuration();
         if (!this.level.isClientSide) {
            this.sendData();
         }
      }
   }

   @Override
   public void tick() {
      this.bottomCPos = this.getBottomControllerPos();
      if (this.isController() && this.isBottom()) {
         if (this.processingTime > -1 && this.currentRecipe != null) {
            boolean canFill = true;
            int i = 0;

            while (i < this.currentRecipe.getFluidResults().size() * ConfigRegistry.DISTILLATION_LEVEL_HEIGHT.get()) {
               if (this.level.getBlockEntity(this.getBlockPos().above(i + 1)) instanceof DistillationTankBlockEntity be) {
                  if (be.getControllerBE().width != this.getControllerBE().width) {
                     canFill = false;
                     break;
                  }

                  if (i % (Integer)ConfigRegistry.DISTILLATION_LEVEL_HEIGHT.get() == 0
                     && be.getTank(0).getFluidAmount()
                        <= getCapacityMultiplier() * this.width * this.width
                           - ((FluidStack)this.currentRecipe.getFluidResults().get(i / (Integer)ConfigRegistry.DISTILLATION_LEVEL_HEIGHT.get())).getAmount()) {
                     i++;
                     continue;
                  }

                  canFill = false;
                  break;
               }

               canFill = false;
               break;
            }

            if (canFill) {
               this.processingTime = this.processingTime
                  - Mth.clamp(ConfigRegistry.DISTILLATION_WIDE_TANK_FASTER.get() ? this.width * this.width : 1, 1, this.processingTime);
            }

            if (this.tankInventory.getFluid().getAmount() < ((FluidIngredient)this.currentRecipe.getFluidIngredients().get(0)).getRequiredAmount()
               || this.getHeat(this.currentRecipe.getRequiredHeat()) > this.heat) {
               this.currentRecipe = null;
               this.processingTime = -1;
               this.onFluidStackChanged(this.tankInventory.getFluid());
            }
         }

         if (this.processingTime == 0 && this.currentRecipe != null) {
            if (this.tankInventory.getFluid().getAmount() >= ((FluidIngredient)this.currentRecipe.getFluidIngredients().get(0)).getRequiredAmount()
               && this.getHeat(this.currentRecipe.getRequiredHeat()) <= this.heat) {
               this.tankInventory.drain(((FluidIngredient)this.currentRecipe.getFluidIngredients().get(0)).getRequiredAmount(), FluidAction.EXECUTE);
               if (this.currentRecipe != null) {
                  for (int i = 0; i < this.currentRecipe.getFluidResults().size() * ConfigRegistry.DISTILLATION_LEVEL_HEIGHT.get(); i++) {
                     if (!(this.level.getBlockEntity(this.getBlockPos().above(i + 1)) instanceof DistillationTankBlockEntity be)
                        || be.getControllerBE().width != this.getControllerBE().width) {
                        break;
                     }

                     if (i % (Integer)ConfigRegistry.DISTILLATION_LEVEL_HEIGHT.get() == 0) {
                        be.tankInventory
                           .fill(
                              (FluidStack)this.currentRecipe.getFluidResults().get(i / (Integer)ConfigRegistry.DISTILLATION_LEVEL_HEIGHT.get()),
                              FluidAction.EXECUTE
                           );
                     }
                  }
               }
            }

            this.currentRecipe = null;
            this.processingTime = -1;
            this.onFluidStackChanged(this.tankInventory.getFluid());
         }

         if (this.currentRecipe == null || this.width != 0 && this.currentRecipe.getProcessingDuration() != 0) {
            this.progress = this.currentRecipe != null
               ? this.processingTime / ((float)this.currentRecipe.getProcessingDuration() / (this.width * this.width))
               : 0.0F;
         }
      }

      super.tick();
      if (this.syncCooldown > 0) {
         this.syncCooldown--;
         if (this.syncCooldown == 0 && this.queuedSync) {
            this.sendData();
         }
      }

      if (this.lastKnownPos == null) {
         this.lastKnownPos = this.getBlockPos();
      } else if (!this.lastKnownPos.equals(this.worldPosition) && this.worldPosition != null) {
         this.onPositionChanged();
         return;
      }

      if (this.updateConnectivity) {
         this.updateConnectivity();
      }

      if (this.fluidLevel != null) {
         this.fluidLevel.tickChaser();
      }
   }

   @Override
   public BlockPos getLastKnownPos() {
      return this.lastKnownPos;
   }

   @Override
   public boolean isController() {
      return this.controller == null
         || this.worldPosition.getX() == this.controller.getX()
            && this.worldPosition.getY() == this.controller.getY()
            && this.worldPosition.getZ() == this.controller.getZ();
   }

   @Override
   public void initialize() {
      super.initialize();
      this.updateTemperature();
      List<Recipe<?>> r = this.getMatchingRecipes();
      if (!r.isEmpty()) {
         this.currentRecipe = (DistillationRecipe)r.get(0);
         if (this.processingTime <= 0) {
            this.startProcessing();
         }
      }

      this.sendData();
      if (this.level.isClientSide) {
         this.invalidateRenderBoundingBox();
      }
   }

   private void onPositionChanged() {
      this.removeController(true);
      this.lastKnownPos = this.worldPosition;
   }

   protected List<Recipe<?>> getMatchingRecipes() {
      List<Recipe<?>> list = RecipeFinder.get(new Object(), this.level, recipe -> recipe.getType() == RecipeRegistry.DISTILLATION.getType());
      return list.stream()
         .filter(
            r -> !((DistillationRecipe)r).getFluidIngredients().isEmpty()
               && ((FluidIngredient)((DistillationRecipe)r).getFluidIngredients().get(0)).getMatchingFluidStacks().contains(this.tankInventory.getFluid())
               && ((FluidIngredient)((DistillationRecipe)r).getFluidIngredients().get(0)).getRequiredAmount() <= this.tankInventory.getFluidAmount()
               && this.getHeat(((DistillationRecipe)r).getRequiredHeat()) <= this.heat
         )
         .collect(Collectors.toList());
   }

   int getHeat(HeatCondition heatCondition) {
      if (heatCondition == HeatCondition.SUPERHEATED) {
         return 2;
      } else {
         return heatCondition == HeatCondition.HEATED ? 1 : 0;
      }
   }

   protected void onFluidStackChanged(FluidStack newFluidStack) {
      if (this.hasLevel()) {
         if (this.processingTime <= -1) {
            List<Recipe<?>> r = this.getMatchingRecipes();
            if (!r.isEmpty()) {
               this.currentRecipe = (DistillationRecipe)r.get(0);
               this.startProcessing();
            } else {
               this.currentRecipe = null;
            }
         }

         FluidType attributes = newFluidStack.getFluid().getFluidType();
         int luminosity = (int)(attributes.getLightLevel(newFluidStack) / 1.2F);
         boolean reversed = attributes.isLighterThanAir();
         int maxY = (int)(this.getFillState() * this.height + 1.0F);

         for (int yOffset = 0; yOffset < this.height; yOffset++) {
            boolean isBright = reversed ? this.height - yOffset <= maxY : yOffset < maxY;
            int actualLuminosity = isBright ? luminosity : (luminosity > 0 ? 1 : 0);

            for (int xOffset = 0; xOffset < this.width; xOffset++) {
               for (int zOffset = 0; zOffset < this.width; zOffset++) {
                  BlockPos pos = this.worldPosition.offset(xOffset, yOffset, zOffset);
                  DistillationTankBlockEntity tankAt = ConnectivityHandler.partAt(this.getType(), this.level, pos);
                  if (tankAt != null) {
                     this.level.updateNeighbourForOutputSignal(pos, tankAt.getBlockState().getBlock());
                     if (tankAt.luminosity != actualLuminosity) {
                        tankAt.setLuminosity(actualLuminosity);
                     }
                  }
               }
            }
         }

         if (!this.level.isClientSide) {
            this.setChanged();
            this.sendData();
         }

         if (this.isVirtual()) {
            if (this.fluidLevel == null) {
               this.fluidLevel = LerpedFloat.linear().startWithValue(this.getFillState());
            }

            this.fluidLevel.chase(this.getFillState(), 0.5, Chaser.EXP);
         }
      }
   }

   protected void setLuminosity(int luminosity) {
      if (!this.level.isClientSide) {
         if (this.luminosity != luminosity) {
            this.luminosity = luminosity;
            this.sendData();
         }
      }
   }

   public DistillationTankBlockEntity getControllerBE() {
      if (this.isController()) {
         return this;
      } else {
         BlockEntity blockEntity = this.level.getBlockEntity(this.controller);
         return blockEntity instanceof DistillationTankBlockEntity ? (DistillationTankBlockEntity)blockEntity : null;
      }
   }

   public void applyFluidTankSize(int blocks) {
      this.tankInventory.setCapacity(blocks * getCapacityMultiplier());
      int overflow = this.tankInventory.getFluidAmount() - this.tankInventory.getCapacity();
      if (overflow > 0) {
         this.tankInventory.drain(overflow, FluidAction.EXECUTE);
      }

      this.forceFluidLevelUpdate = true;
   }

   @Override
   public void removeController(boolean keepFluids) {
      if (!this.level.isClientSide) {
         this.updateConnectivity = true;
         if (!keepFluids) {
            this.applyFluidTankSize(1);
         }

         this.controller = null;
         this.width = 1;
         this.height = 1;
         this.onFluidStackChanged(this.tankInventory.getFluid());
         BlockState state = this.getBlockState();
         if (DistillationTankBlock.isTank(state)) {
            state = (BlockState)state.setValue(DistillationTankBlock.BOTTOM, true);
            state = (BlockState)state.setValue(DistillationTankBlock.TOP, true);
            state = (BlockState)state.setValue(DistillationTankBlock.SHAPE, this.window ? Shape.WINDOW : Shape.PLAIN);
            this.getLevel().setBlock(this.worldPosition, state, 6);
         }

         this.refreshCapability();
         this.setChanged();
         this.sendData();
      }
   }

   public void toggleWindows() {
      DistillationTankBlockEntity be = this.getControllerBE();
      if (be != null) {
         be.setWindows(!be.window);
      }
   }

   @Override
   public void sendData() {
      if (this.syncCooldown > 0) {
         this.queuedSync = true;
      } else {
         super.sendData();
         this.queuedSync = false;
         this.syncCooldown = 8;
      }
   }

   public void setWindows(boolean window) {
      if (!window
         || (Integer)ConfigRegistry.DISTILLATION_LEVEL_HEIGHT.get() == 1
         || (this.getBottomControllerPos().getY() + 1 - this.worldPosition.getY()) % (Integer)ConfigRegistry.DISTILLATION_LEVEL_HEIGHT.get() == 0) {
         this.window = window;

         for (int yOffset = 0; yOffset < this.height; yOffset++) {
            for (int xOffset = 0; xOffset < this.width; xOffset++) {
               for (int zOffset = 0; zOffset < this.width; zOffset++) {
                  BlockPos pos = this.worldPosition.offset(xOffset, yOffset, zOffset);
                  BlockState blockState = this.level.getBlockState(pos);
                  if (DistillationTankBlock.isTank(blockState)) {
                     Shape shape = Shape.PLAIN;
                     if (window) {
                        if (this.width == 1) {
                           shape = Shape.WINDOW;
                        }

                        if (this.width == 2) {
                           shape = xOffset == 0 ? (zOffset == 0 ? Shape.WINDOW_NW : Shape.WINDOW_SW) : (zOffset == 0 ? Shape.WINDOW_NE : Shape.WINDOW_SE);
                        }

                        if (this.width == 3 && Math.abs(xOffset - zOffset) == 1) {
                           shape = Shape.WINDOW;
                        }
                     }

                     this.level.setBlock(pos, (BlockState)blockState.setValue(DistillationTankBlock.SHAPE, shape), 22);
                     this.level.getChunkSource().getLightEngine().checkBlock(pos);
                  }
               }
            }
         }
      }
   }

   @Override
   public void setController(BlockPos controller) {
      if (!this.level.isClientSide || this.isVirtual()) {
         if (!controller.equals(this.controller)) {
            this.controller = controller;
            this.refreshCapability();
            this.setChanged();
            this.sendData();
         }
      }
   }

   private void refreshCapability() {
      LazyOptional<IFluidHandler> oldCap = this.fluidCapability;
      this.fluidCapability = LazyOptional.of(() -> this.handlerForCapability());
      oldCap.invalidate();
   }

   private IFluidHandler handlerForCapability() {
      return (IFluidHandler)(this.isController()
         ? this.tankInventory
         : (this.getControllerBE() != null ? this.getControllerBE().handlerForCapability() : new FluidTank(0)));
   }

   @Override
   public BlockPos getController() {
      return this.isController() ? this.worldPosition : this.controller;
   }

   @Override
   protected AABB createRenderBoundingBox() {
      return this.isController() ? super.createRenderBoundingBox().expandTowards(this.width - 1, this.height - 1, this.width - 1) : super.createRenderBoundingBox();
   }

   @Override
   public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
      if ((Integer)ConfigRegistry.DISTILLATION_LEVEL_HEIGHT.get() != 1
         && (this.getBottomControllerPos().getY() + 1 - this.worldPosition.getY()) % (Integer)ConfigRegistry.DISTILLATION_LEVEL_HEIGHT.get() != 0) {
         return false;
      } else {
         DistillationTankBlockEntity controllerBE = this.getControllerBE();
         return controllerBE == null
            ? false
            : this.containedFluidTooltip(tooltip, isPlayerSneaking, controllerBE.getCapability(ForgeCapabilities.FLUID_HANDLER));
      }
   }

   @Override
   protected void read(CompoundTag compound, boolean clientPacket) {
      super.read(compound, clientPacket);
      this.hasDistillationC = compound.getBoolean("HasDistillationC");
      BlockPos controllerBefore = this.controller;
      int prevSize = this.width;
      int prevHeight = this.height;
      int prevLum = this.luminosity;
      this.updateConnectivity = compound.contains("Uninitialized");
      this.luminosity = compound.getInt("Luminosity");
      this.controller = null;
      this.lastKnownPos = null;
      if (compound.contains("LastKnownPos")) {
         this.lastKnownPos = NbtUtils.readBlockPos(compound.getCompound("LastKnownPos"));
      }

      if (compound.contains("Controller")) {
         this.controller = NbtUtils.readBlockPos(compound.getCompound("Controller"));
      }

      if (this.isController()) {
         this.window = compound.getBoolean("Window");
         this.width = compound.getInt("Size");
         this.height = compound.getInt("Height");
         this.tankInventory.setCapacity(this.getTotalTankSize() * getCapacityMultiplier());
         this.tankInventory.readFromNBT(compound.getCompound("TankContent"));
         if (this.tankInventory.getSpace() < 0) {
            this.tankInventory.drain(-this.tankInventory.getSpace(), FluidAction.EXECUTE);
         }
      }

      if (compound.contains("ForceFluidLevel") || this.fluidLevel == null) {
         this.fluidLevel = LerpedFloat.linear().startWithValue(this.getFillState());
      }

      if (clientPacket) {
         boolean changeOfController = controllerBefore == null ? this.controller != null : !controllerBefore.equals(this.controller);
         if (changeOfController || prevSize != this.width || prevHeight != this.height) {
            if (this.hasLevel()) {
               this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 16);
            }

            if (this.isController()) {
               this.tankInventory.setCapacity(getCapacityMultiplier() * this.getTotalTankSize());
            }

            this.invalidateRenderBoundingBox();
         }

         if (this.isController()) {
            float fillState = this.getFillState();
            if (compound.contains("ForceFluidLevel") || this.fluidLevel == null) {
               this.fluidLevel = LerpedFloat.linear().startWithValue(fillState);
            }

            this.fluidLevel.chase(fillState, 0.5, Chaser.EXP);
            this.processingTime = compound.getInt("Progress");
         }

         if (this.luminosity != prevLum && this.hasLevel()) {
            this.level.getChunkSource().getLightEngine().checkBlock(this.worldPosition);
         }

         if (compound.contains("LazySync")) {
            this.fluidLevel.chase(this.fluidLevel.getChaseTarget(), 0.125, Chaser.EXP);
         }

         this.updateTemperature();
         List<Recipe<?>> r = this.getMatchingRecipes();
         if (!r.isEmpty()) {
            this.currentRecipe = (DistillationRecipe)r.get(0);
            if (this.processingTime <= 0) {
               this.startProcessing();
            }
         }
      }
   }

   public float getFillState() {
      return (float)this.tankInventory.getFluidAmount() / this.tankInventory.getCapacity();
   }

   @Override
   public void write(CompoundTag compound, boolean clientPacket) {
      compound.putBoolean("HasDistillationC", this.hasDistillationC);
      if (this.updateConnectivity) {
         compound.putBoolean("Uninitialized", true);
      }

      if (this.lastKnownPos != null) {
         compound.put("LastKnownPos", NbtUtils.writeBlockPos(this.lastKnownPos));
      }

      if (!this.isController()) {
         compound.put("Controller", NbtUtils.writeBlockPos(this.controller));
      }

      if (this.isController()) {
         compound.putBoolean("Window", this.window);
         compound.put("TankContent", this.tankInventory.writeToNBT(new CompoundTag()));
         compound.putInt("Size", this.width);
         compound.putInt("Height", this.height);
         compound.putInt("Progress", this.processingTime);
      }

      compound.putInt("Luminosity", this.luminosity);
      super.write(compound, clientPacket);
      if (clientPacket) {
         if (this.forceFluidLevelUpdate) {
            compound.putBoolean("ForceFluidLevel", true);
         }

         if (this.queuedSync) {
            compound.putBoolean("LazySync", true);
         }

         this.forceFluidLevelUpdate = false;
      }
   }

   @Nonnull
   public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
      if ((Integer)ConfigRegistry.DISTILLATION_LEVEL_HEIGHT.get() != 1
         && (this.getBottomControllerPos().getY() + 1 - this.worldPosition.getY()) % (Integer)ConfigRegistry.DISTILLATION_LEVEL_HEIGHT.get() != 0) {
         return super.getCapability(cap, side);
      } else {
         if (!this.fluidCapability.isPresent()) {
            this.refreshCapability();
         }

         return cap == ForgeCapabilities.FLUID_HANDLER ? this.fluidCapability.cast() : super.getCapability(cap, side);
      }
   }

   @Override
   public void invalidate() {
      super.invalidate();
   }

   @Override
   public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
   }

   public int getTotalTankSize() {
      return this.width * this.width;
   }

   public static int getCapacityMultiplier() {
      return 500;
   }

   public LerpedFloat getFluidLevel() {
      return this.fluidLevel;
   }

   @Override
   public void preventConnectivityUpdate() {
      this.updateConnectivity = false;
   }

   @Override
   public void notifyMultiUpdated() {
      BlockState state = this.getBlockState();
      if (DistillationTankBlock.isTank(state)) {
         state = (BlockState)state.setValue(DistillationTankBlock.BOTTOM, this.getBottomConnectivity());
         state = (BlockState)state.setValue(DistillationTankBlock.TOP, this.getTopConnectivity());
         this.level.setBlock(this.getBlockPos(), state, 6);
      }

      if (this.isController()) {
         this.setWindows(this.window);
      }

      this.onFluidStackChanged(this.tankInventory.getFluid());
      this.setChanged();
   }

   private boolean getBottomConnectivity() {
      if (this.level.getBlockEntity(this.getBlockPos().below()) instanceof DistillationTankBlockEntity be) {
         DistillationTankBlockEntity otherControllerBE = be.getControllerBE();
         if (otherControllerBE != null) {
            DistillationTankBlockEntity controllerBE = this.getControllerBE();
            if (controllerBE != null && controllerBE.getBlockPos().below().equals(otherControllerBE.getBlockPos())) {
               return controllerBE.getWidth() != otherControllerBE.getWidth();
            }
         }
      }

      return true;
   }

   private boolean getTopConnectivity() {
      if (this.level.getBlockEntity(this.getBlockPos().above()) instanceof DistillationTankBlockEntity be) {
         DistillationTankBlockEntity otherControllerBE = be.getControllerBE();
         if (otherControllerBE != null) {
            DistillationTankBlockEntity controllerBE = this.getControllerBE();
            if (controllerBE != null && controllerBE.getBlockPos().above().equals(otherControllerBE.getBlockPos())) {
               return controllerBE.getWidth() != otherControllerBE.getWidth();
            }
         }
      }

      return true;
   }

   @Override
   public void setExtraData(@Nullable Object data) {
      if (data instanceof Boolean) {
         this.window = (Boolean)data;
      }
   }

   @Nullable
   @Override
   public Object getExtraData() {
      return this.window;
   }

   @Override
   public Object modifyExtraData(Object data) {
      return data instanceof Boolean windows ? windows | this.window : data;
   }

   @Override
   public Axis getMainConnectionAxis() {
      return Axis.Y;
   }

   @Override
   public int getMaxLength(Axis longAxis, int width) {
      return longAxis == Axis.Y ? 1 : this.getMaxWidth();
   }

   @Override
   public int getMaxWidth() {
      return 3;
   }

   @Override
   public int getHeight() {
      return this.height;
   }

   @Override
   public void setHeight(int height) {
      this.height = height;
   }

   @Override
   public int getWidth() {
      return this.width;
   }

   @Override
   public void setWidth(int width) {
      this.width = width;
   }

   @Override
   public boolean hasTank() {
      return true;
   }

   @Override
   public int getTankSize(int tank) {
      return getCapacityMultiplier();
   }

   @Override
   public void setTankSize(int tank, int blocks) {
      this.applyFluidTankSize(blocks);
   }

   @Override
   public IFluidTank getTank(int tank) {
      return this.tankInventory;
   }

   @Override
   public FluidStack getFluid(int tank) {
      return this.tankInventory.getFluid().copy();
   }

   public void updateVerticalMulti() {
      BlockState state = this.getBlockState();
      if (DistillationTankBlock.isTank(state)) {
         state = (BlockState)state.setValue(DistillationTankBlock.BOTTOM, this.getBottomConnectivity());
         state = (BlockState)state.setValue(DistillationTankBlock.TOP, this.getTopConnectivity());
         if (state != this.getBlockState()) {
            this.level.setBlock(this.getBlockPos(), state, 3);
         }
      }

      if (this.level.getBlockEntity(this.getBlockPos().below()) instanceof DistillationTankBlockEntity be) {
         be.updateVerticalMulti();
      }
   }

   public boolean isBottom() {
      return !(this.level.getBlockEntity(this.getBlockPos().below()) instanceof DistillationTankBlockEntity be && be.getWidth() == this.getWidth());
   }

   public void updateTemperature() {
      if (this.isBottom()) {
         if (this.isController()) {
            this.heat = this.getTemperature();
            this.sendData();
            if (this.processingTime <= -1) {
               List<Recipe<?>> r = this.getMatchingRecipes();
               if (!r.isEmpty()) {
                  this.currentRecipe = (DistillationRecipe)r.get(0);
                  this.startProcessing();
               } else {
                  this.currentRecipe = null;
               }
            }
         } else {
            DistillationTankBlockEntity be = this.getControllerBE();
            if (be != null) {
               be.updateTemperature();
            }
         }
      }
   }
}
