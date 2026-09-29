package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock;
import com.jesz.createdieselgenerators.blocks.HugeDieselEngineBlock;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class AnimatedDieselEngineElement extends AnimatedKinetics {
   public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
      PoseStack matrixStack = graphics.pose();
      byte enginesEnabled = (byte)(
         (DieselGeneratorBlock.EngineTypes.NORMAL.enabled() ? 1 : 0)
            + (DieselGeneratorBlock.EngineTypes.MODULAR.enabled() ? 1 : 0)
            + (DieselGeneratorBlock.EngineTypes.HUGE.enabled() ? 1 : 0)
      );
      int currentEngineIndex = AnimationTickHolder.getTicks() % 120 / 20;
      List<DieselGeneratorBlock.EngineTypes> enabledEngines = Arrays.stream(DieselGeneratorBlock.EngineTypes.values())
         .filter(DieselGeneratorBlock.EngineTypes::enabled)
         .toList();
      DieselGeneratorBlock.EngineTypes currentEngine = enabledEngines.get(currentEngineIndex % enginesEnabled);
      matrixStack.pushPose();
      matrixStack.translate(xOffset, yOffset, 100.0F);
      matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5F));
      matrixStack.mulPose(Axis.YP.rotationDegrees(112.5F));
      int scale = 25;
      if (currentEngine == DieselGeneratorBlock.EngineTypes.HUGE) {
         scale = 17;
      }

      this.blockElement(this.shaft(net.minecraft.core.Direction.Axis.X))
         .atLocal(0.0, currentEngine == DieselGeneratorBlock.EngineTypes.HUGE ? -1.25 : 0.0, 0.0)
         .rotateBlock(-getCurrentAngle() * 6.0F, 0.0, 0.0)
         .scale(scale)
         .render(graphics);
      int angle = (int)(getCurrentAngle() * 18.0F % 360.0F) / 36;
      if (currentEngine == DieselGeneratorBlock.EngineTypes.HUGE) {
         this.blockElement(PartialModels.ENGINE_PISTON_CONNECTOR)
            .atLocal(0.0, -1.25, 0.0)
            .rotateBlock(-getCurrentAngle() * 6.0F, 0.0, 0.0)
            .scale(scale)
            .render(graphics);
         this.blockElement(PartialModels.ENGINE_PISTON_LINKAGE)
            .atLocal(0.0, Math.cos(getCurrentAngle() / 30.0F * Math.PI) / 5.0 - 1.0, 0.0)
            .scale(scale)
            .render(graphics);
         this.blockElement(PartialModels.JEI_ENGINE_PISTON)
            .atLocal(0.0, Math.cos(getCurrentAngle() / 30.0F * Math.PI) / 5.0, 0.0)
            .scale(scale)
            .render(graphics);
      }

      if (currentEngine == DieselGeneratorBlock.EngineTypes.NORMAL) {
         this.blockElement(
               angle == 10
                  ? PartialModels.ENGINE_PISTONS_0
                  : (
                     angle == 9
                        ? PartialModels.ENGINE_PISTONS_1
                        : (
                           angle == 8
                              ? PartialModels.ENGINE_PISTONS_2
                              : (
                                 angle == 7
                                    ? PartialModels.ENGINE_PISTONS_3
                                    : (
                                       angle == 6
                                          ? PartialModels.ENGINE_PISTONS_4
                                          : (
                                             angle == 5
                                                ? PartialModels.ENGINE_PISTONS_4
                                                : (
                                                   angle == 4
                                                      ? PartialModels.ENGINE_PISTONS_3
                                                      : (
                                                         angle == 3
                                                            ? PartialModels.ENGINE_PISTONS_2
                                                            : (angle == 2 ? PartialModels.ENGINE_PISTONS_1 : PartialModels.ENGINE_PISTONS_0)
                                                      )
                                                )
                                          )
                                    )
                              )
                        )
                  )
            )
            .rotateBlock(0.0, 90.0, 0.0)
            .scale(scale)
            .render(graphics);
      } else if (currentEngine == DieselGeneratorBlock.EngineTypes.MODULAR) {
         this.blockElement(
               angle == 10
                  ? PartialModels.MODULAR_ENGINE_PISTONS_0
                  : (
                     angle == 9
                        ? PartialModels.MODULAR_ENGINE_PISTONS_1
                        : (
                           angle == 8
                              ? PartialModels.MODULAR_ENGINE_PISTONS_2
                              : (
                                 angle == 7
                                    ? PartialModels.MODULAR_ENGINE_PISTONS_3
                                    : (
                                       angle == 6
                                          ? PartialModels.MODULAR_ENGINE_PISTONS_4
                                          : (
                                             angle == 5
                                                ? PartialModels.MODULAR_ENGINE_PISTONS_4
                                                : (
                                                   angle == 4
                                                      ? PartialModels.MODULAR_ENGINE_PISTONS_3
                                                      : (
                                                         angle == 3
                                                            ? PartialModels.MODULAR_ENGINE_PISTONS_2
                                                            : (angle == 2 ? PartialModels.MODULAR_ENGINE_PISTONS_1 : PartialModels.MODULAR_ENGINE_PISTONS_0)
                                                      )
                                                )
                                          )
                                    )
                              )
                        )
                  )
            )
            .rotateBlock(0.0, 90.0, 0.0)
            .scale(scale)
            .render(graphics);
      }

      this.blockElement(
            currentEngine == DieselGeneratorBlock.EngineTypes.MODULAR
               ? BlockRegistry.MODULAR_DIESEL_ENGINE.getDefaultState()
               : (
                  currentEngine == DieselGeneratorBlock.EngineTypes.HUGE
                     ? (BlockState)BlockRegistry.HUGE_DIESEL_ENGINE.getDefaultState().setValue(HugeDieselEngineBlock.FACING, Direction.UP)
                     : BlockRegistry.DIESEL_ENGINE.getDefaultState()
               )
         )
         .rotateBlock(0.0, 90.0, 0.0)
         .scale(scale)
         .render(graphics);
      matrixStack.popPose();
   }
}
