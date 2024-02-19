package com.mitchej123.hodgepodge.mixins.early.minecraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mitchej123.hodgepodge.client.chat.ChatHandler;

@Mixin(GuiNewChat.class)
public class MixinGuiNewChat_CompactChat {

    @Shadow
    @Final
    private List<ChatLine> messages;

    @Shadow
    @Final
    private List<ChatLine> trimmedMessages; // drawnChatLines

    @Inject(method = "addMessage", at = @At("HEAD"))
    private void hodgepodge$compactChat(IChatComponent imsg, int chatLineId, int updateCounter, boolean refresh,
            CallbackInfo ci, @Share("deleteMessage") LocalBooleanRef deleteMessage) {
        deleteMessage.set(!refresh && ChatHandler.tryCompactMessage(imsg, this.messages) && chatLineId == 0);
    }

    @Inject(
            method = "addMessage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;isChatFocused()Z"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void hodgepodge$deletePrevious(IChatComponent imsg, int chatLineId, int updateCounter, boolean refresh,
            CallbackInfo ci, int k, int l, ChatComponentText chatcomponenttext, ArrayList<ChatComponentText> arraylist,
            @Share("deleteMessage") LocalBooleanRef deleteMessage) {
        if (!deleteMessage.get()) return;
        if (this.messages.isEmpty()) return;
        this.messages.remove(0);
        for (int i = 0; i < arraylist.size(); i++) {
            if (!this.trimmedMessages.isEmpty()) {
                this.trimmedMessages.remove(0);
            }
        }
    }

}
