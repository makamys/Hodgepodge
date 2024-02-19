package com.mitchej123.hodgepodge.mixins.early.minecraft;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.Container;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.mitchej123.hodgepodge.Compat;

@Mixin(InventoryEffectRenderer.class)
public abstract class MixinInventoryEffectRenderer_PotionEffectRendering extends GuiContainer {

    @Shadow
    private void drawStatusEffects() {}

    /**
     * @author Alexdoru
     * @reason Fix the bug that renders the potion effects above the tooltips from items in NEI Fix the vanilla bug that
     *         doesn't render the potion effects that you get while your inventory is opened
     */
    @Overwrite
    public void render(int mouseX, int mouseY, float partialTicks) {
        boolean leftPanelHidden = !Compat.isNeiLeftPanelVisible();
        if (leftPanelHidden) {
            super.render(mouseX, mouseY, partialTicks);
        }
        if (!this.minecraft.player.getStatusEffects().isEmpty()) {
            this.drawStatusEffects();
        }
        if (leftPanelHidden) {
            return;
        }
        super.render(mouseX, mouseY, partialTicks);
    }

    /* Forced to have constructor matching super */
    private MixinInventoryEffectRenderer_PotionEffectRendering(Container p_i1089_1_) {
        super(p_i1089_1_);
    }
}
