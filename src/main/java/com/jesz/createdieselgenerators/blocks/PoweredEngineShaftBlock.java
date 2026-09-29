package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlock;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class PoweredEngineShaftBlock extends PoweredShaftBlock {
   public PoweredEngineShaftBlock(Properties properties) {
      super(properties);
   }

   @Override
   public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
      return BlockEntityRegistry.POWERED_ENGINE_SHAFT.get();
   }

   public static BlockState getEquivalent(BlockState stateForPlacement) {
      return stateForPlacement.getBlock() instanceof ShaftBlock
         ? (BlockState)((BlockState)BlockRegistry.POWERED_ENGINE_SHAFT
               .getDefaultState()
               .setValue(PoweredShaftBlock.AXIS, (Axis)stateForPlacement.getValue(ShaftBlock.AXIS)))
            .setValue(WATERLOGGED, (Boolean)stateForPlacement.getValue(WATERLOGGED))
         : stateForPlacement;
   }
}
