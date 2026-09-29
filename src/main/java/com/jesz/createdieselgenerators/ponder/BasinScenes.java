package com.jesz.createdieselgenerators.ponder;

import com.jesz.createdieselgenerators.content.basin_lid.BasinLidBlock;
import com.jesz.createdieselgenerators.CDGFluids;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.utility.Pointing;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class BasinScenes {
   public static void basin_lid(SceneBuilder scene, SceneBuildingUtil util) {
      scene.title("basin_fermenting_station", "Setting up a Basin Fermenting Station");
      scene.configureBasePlate(0, 0, 3);
      scene.showBasePlate();
      Selection basinSection = util.select.position(1, 1, 1);
      Selection basinLidSection = util.select.position(1, 2, 1);
      scene.idle(15);
      scene.world.showSection(basinSection, Direction.DOWN);
      scene.world.showSection(basinLidSection, Direction.DOWN);
      scene.overlay
         .showText(50)
         .attachKeyFrame()
         .text("Basin Lids allow you to create Ethanol.")
         .pointAt(util.vector.blockSurface(util.grid.at(1, 1, 1), Direction.NORTH))
         .placeNearTarget();
      scene.idle(60);
      scene.overlay
         .showText(30)
         .attachKeyFrame()
         .text("Give it some Sugar and Bone Meal...")
         .pointAt(util.vector.blockSurface(util.grid.at(1, 1, 1), Direction.NORTH))
         .placeNearTarget();
      scene.idle(30);
      scene.world.modifyBlock(util.grid.at(1, 2, 1), s -> (BlockState)s.setValue(BasinLidBlock.ON_A_BASIN, false), false);
      scene.world.hideSection(basinLidSection, Direction.UP);
      scene.idle(10);
      scene.overlay.showControls(new InputWindowElement(util.vector.topOf(1, 1, 1), Pointing.DOWN).withItem(new ItemStack(Items.SUGAR)), 20);
      scene.idle(30);
      scene.overlay.showControls(new InputWindowElement(util.vector.topOf(1, 1, 1), Pointing.DOWN).withItem(new ItemStack(Items.BONE_MEAL)), 20);
      scene.idle(30);
      scene.world.showSection(basinLidSection, Direction.DOWN);
      scene.world.modifyBlock(util.grid.at(1, 2, 1), s -> (BlockState)s.setValue(BasinLidBlock.ON_A_BASIN, false), false);
      scene.idle(20);
      scene.world.modifyBlock(util.grid.at(1, 2, 1), s -> (BlockState)s.setValue(BasinLidBlock.ON_A_BASIN, true), false);
      scene.idle(50);
      scene.overlay
         .showText(30)
         .attachKeyFrame()
         .text("... Ethanol will be created.")
         .pointAt(util.vector.blockSurface(util.grid.at(1, 1, 1), Direction.NORTH))
         .placeNearTarget();
      scene.idle(40);
      scene.world.showSection(util.select.fromTo(3, 0, 1, 4, 1, 1), Direction.SOUTH);
      scene.world.showSection(util.select.position(2, 1, 1), Direction.SOUTH);
      scene.idle(20);
      scene.world.modifyKineticSpeed(util.select.position(3, 0, 0), s -> 16.0F);
      scene.world.modifyKineticSpeed(util.select.position(3, 1, 1), s -> -16.0F);
      scene.idle(10);
      FluidStack content = new FluidStack(CDGFluids.ETHANOL.getSource(), 50);
      scene.world.modifyBlockEntity(util.grid.at(4, 0, 1), FluidTankBlockEntity.class, be -> be.getTankInventory().fill(content, FluidAction.EXECUTE));
      scene.idle(60);
   }
}
