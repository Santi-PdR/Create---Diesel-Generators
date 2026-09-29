package com.jesz.createdieselgenerators.blocks.ct;

import com.jesz.createdieselgenerators.blocks.OilBarrelBlock;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class OilBarrelCTBehavior extends ConnectedTextureBehaviour {
   @Override
   public CTSpriteShiftEntry getShift(BlockState state, Direction direction, TextureAtlasSprite sprite) {
      if (direction.getAxis() == state.getValue(OilBarrelBlock.AXIS)) {
         return SpriteShifts.OIL_BARREL_TOP;
      } else if ((state.getValue(OilBarrelBlock.AXIS) != Axis.Z || direction.getAxis() != Axis.Y) && state.getValue(OilBarrelBlock.AXIS) != Axis.Y) {
         if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.WHITE) {
            return SpriteShifts.OIL_BARREL_SIDE_WHITE;
         } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.ORANGE) {
            return SpriteShifts.OIL_BARREL_SIDE_ORANGE;
         } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.MAGENTA) {
            return SpriteShifts.OIL_BARREL_SIDE_MAGENTA;
         } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.LIGHT_BLUE) {
            return SpriteShifts.OIL_BARREL_SIDE_LIGHT_BLUE;
         } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.YELLOW) {
            return SpriteShifts.OIL_BARREL_SIDE_YELLOW;
         } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.LIME) {
            return SpriteShifts.OIL_BARREL_SIDE_LIME;
         } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.PINK) {
            return SpriteShifts.OIL_BARREL_SIDE_PINK;
         } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.GRAY) {
            return SpriteShifts.OIL_BARREL_SIDE_GRAY;
         } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.LIGHT_GRAY) {
            return SpriteShifts.OIL_BARREL_SIDE_LIGHT_GRAY;
         } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.CYAN) {
            return SpriteShifts.OIL_BARREL_SIDE_CYAN;
         } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.PURPLE) {
            return SpriteShifts.OIL_BARREL_SIDE_PURPLE;
         } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.BLUE) {
            return SpriteShifts.OIL_BARREL_SIDE_BLUE;
         } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.BROWN) {
            return SpriteShifts.OIL_BARREL_SIDE_BROWN;
         } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.GREEN) {
            return SpriteShifts.OIL_BARREL_SIDE_GREEN;
         } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.RED) {
            return SpriteShifts.OIL_BARREL_SIDE_RED;
         } else {
            return state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.BLACK
               ? SpriteShifts.OIL_BARREL_SIDE_BLACK
               : SpriteShifts.OIL_BARREL_SIDE;
         }
      } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.WHITE) {
         return SpriteShifts.OIL_BARREL_WHITE;
      } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.ORANGE) {
         return SpriteShifts.OIL_BARREL_ORANGE;
      } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.MAGENTA) {
         return SpriteShifts.OIL_BARREL_MAGENTA;
      } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.LIGHT_BLUE) {
         return SpriteShifts.OIL_BARREL_LIGHT_BLUE;
      } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.YELLOW) {
         return SpriteShifts.OIL_BARREL_YELLOW;
      } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.LIME) {
         return SpriteShifts.OIL_BARREL_LIME;
      } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.PINK) {
         return SpriteShifts.OIL_BARREL_PINK;
      } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.GRAY) {
         return SpriteShifts.OIL_BARREL_GRAY;
      } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.LIGHT_GRAY) {
         return SpriteShifts.OIL_BARREL_LIGHT_GRAY;
      } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.CYAN) {
         return SpriteShifts.OIL_BARREL_CYAN;
      } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.PURPLE) {
         return SpriteShifts.OIL_BARREL_PURPLE;
      } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.BLUE) {
         return SpriteShifts.OIL_BARREL_BLUE;
      } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.BROWN) {
         return SpriteShifts.OIL_BARREL_BROWN;
      } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.GREEN) {
         return SpriteShifts.OIL_BARREL_GREEN;
      } else if (state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.RED) {
         return SpriteShifts.OIL_BARREL_RED;
      } else {
         return state.getValue(OilBarrelBlock.OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.BLACK
            ? SpriteShifts.OIL_BARREL_BLACK
            : SpriteShifts.OIL_BARREL;
      }
   }

   @Override
   public CTType getDataType(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction direction) {
      return AllCTTypes.RECTANGLE;
   }

   @Override
   public boolean connectsTo(
      BlockState state,
      BlockState other,
      BlockAndTintGetter reader,
      BlockPos pos,
      BlockPos otherPos,
      Direction face,
      Direction primaryOffset,
      Direction secondaryOffset
   ) {
      return other.getBlock() instanceof OilBarrelBlock && ConnectivityHandler.isConnected(reader, pos, otherPos);
   }
}
