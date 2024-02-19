package com.mitchej123.hodgepodge.mixins.early.minecraft;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.block.BlockRedstoneTorch;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = BlockRedstoneTorch.class, remap = false)
public class MixinBlockRedstoneTorch {

    @Shadow
    private static Map RECENT_TOGGLES = new WeakHashMap<>();
}
