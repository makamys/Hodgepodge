package com.mitchej123.hodgepodge.client.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import com.gtnewhorizon.gtnhlib.util.AboveHotbarHUD;
import com.mitchej123.hodgepodge.client.HodgepodgeClient;
import com.mitchej123.hodgepodge.config.DebugConfig;
import com.mitchej123.hodgepodge.config.TweaksConfig;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientKeyListener {

    private static String fastBlockPlacingEnabled = StatCollector.translate("key.fastBlockPlacing.enabled");
    private static String fastBlockPlacingDisabled = StatCollector.translate("key.fastBlockPlacing.disabled");

    public static KeyBinding FastBlockPlacingKey = new KeyBinding(
            "key.fastBlockPlacing.desc",
            Keyboard.KEY_NONE,
            "key.hodgepodge.category");

    @SubscribeEvent
    public void keyPressed(InputEvent.KeyInputEvent event) {
        int key = Keyboard.getEventKey();
        boolean released = !Keyboard.getEventKeyState();
        if (released) {
            if (Minecraft.getInstance().options.debugEnabled && GuiScreen.isShiftDown()
                    && GuiScreen.isControlDown()) {
                if (key == Keyboard.KEY_N) {
                    HodgepodgeClient.animationsMode.next();
                } else if (key == Keyboard.KEY_D && DebugConfig.renderDebug) {
                    HodgepodgeClient.renderDebugMode.next();
                }
            }
        } else {
            if (FastBlockPlacingKey.consumeClick()) {
                TweaksConfig.fastBlockPlacing = !TweaksConfig.fastBlockPlacing;
                AboveHotbarHUD.renderTextAboveHotbar(
                        (TweaksConfig.fastBlockPlacing ? fastBlockPlacingEnabled : fastBlockPlacingDisabled),
                        40,
                        false,
                        false);
            }
        }
    }
}
