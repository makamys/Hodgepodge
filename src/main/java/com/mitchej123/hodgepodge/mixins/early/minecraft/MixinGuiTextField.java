package com.mitchej123.hodgepodge.mixins.early.minecraft;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiTextField.class)
public abstract class MixinGuiTextField {

    @Shadow
    private boolean editable;

    @Shadow
    private boolean focused;

    @Shadow
    public abstract void write(String p_146191_1_);

    @Shadow
    public abstract void setCursorToEnd();

    @Shadow
    public abstract void setSelectionEnd(int p_146199_1_);

    @Shadow
    public abstract String getSelectedText();

    @Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
    private void hodgepodge$addMacCommandKeyShortcuts(char typedChar, int eventKey,
            CallbackInfoReturnable<Boolean> cir) {
        if (this.focused && GuiScreen.isControlDown()) {
            if (eventKey == Keyboard.KEY_V) {
                if (this.editable) {
                    this.write(GuiScreen.getClipboard());
                    cir.setReturnValue(true);
                }
            } else if (eventKey == Keyboard.KEY_C) {
                GuiScreen.setClipboard(this.getSelectedText());
                cir.setReturnValue(true);
            } else if (eventKey == Keyboard.KEY_A) {
                this.setCursorToEnd();
                this.setSelectionEnd(0);
                cir.setReturnValue(true);
            } else if (eventKey == Keyboard.KEY_X) {
                GuiScreen.setClipboard(this.getSelectedText());
                if (this.editable) {
                    this.write("");
                }
                cir.setReturnValue(true);
            }
        }
    }
}
