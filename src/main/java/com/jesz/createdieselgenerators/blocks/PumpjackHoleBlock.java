package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackHoleBlockEntity;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class PumpjackHoleBlock extends Block implements IBE<PumpjackHoleBlockEntity>, IWrenchable {
   public PumpjackHoleBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(BlockStateProperties.NORTH, true))
                  .setValue(BlockStateProperties.SOUTH, true))
               .setValue(BlockStateProperties.WEST, false))
            .setValue(BlockStateProperties.EAST, false)
      );
   }

   @Override
   public InteractionResult onWrenched(BlockState state, UseOnContext context) {
      if (context.getClickedFace().getAxis().isHorizontal()) {
         context.getLevel()
            .setBlock(
               context.getClickedPos(),
               (BlockState)state.setValue(
                  BooleanProperty.create(context.getClickedFace().getName()), !(Boolean)state.getValue(BooleanProperty.create(context.getClickedFace().getName()))
               ),
               3
            );
         this.playRotateSound(context.getLevel(), context.getClickedPos());
         return InteractionResult.SUCCESS;
      } else {
         return IWrenchable.super.onWrenched(state, context);
      }
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(
         new Property[]{
            BlockStateProperties.NORTH,
            BlockStateProperties.EAST,
            BlockStateProperties.SOUTH,
            BlockStateProperties.WEST,
            BlockStateProperties.UP,
            BlockStateProperties.DOWN
         }
      );
      super.createBlockStateDefinition(builder);
   }

   @Override
   public PumpjackHoleBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new PumpjackHoleBlockEntity(getBlockEntityType(), pos, state);
   }

   @Override
   public Class<PumpjackHoleBlockEntity> getBlockEntityClass() {
      return PumpjackHoleBlockEntity.class;
   }

   @Override
   public BlockEntityType<? extends PumpjackHoleBlockEntity> getBlockEntityType() {
      return BlockEntityRegistry.PUMPJACK_HOLE.get();
   }
}
