package com.jesz.createdieselgenerators.blocks;

import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.HitResult;

public class PumpjackBearingBBlock extends Block implements IWrenchable {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

   public PumpjackBearingBBlock(Properties properties) {
      super(properties);
   }

   @Override
   public InteractionResult onWrenched(BlockState state, UseOnContext context) {
      context.getLevel()
         .setBlock(
            context.getClickedPos(), (BlockState)BlockRegistry.PUMPJACK_BEARING.getDefaultState().setValue(BearingBlock.FACING, (Direction)state.getValue(FACING)), 2
         );
      return InteractionResult.SUCCESS;
   }

   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
      return BlockRegistry.PUMPJACK_BEARING.asStack();
   }

   public Item asItem() {
      return BlockRegistry.PUMPJACK_BEARING.asStack().getItem();
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }
}
