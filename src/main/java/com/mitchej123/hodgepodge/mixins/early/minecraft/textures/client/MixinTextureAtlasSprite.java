package com.mitchej123.hodgepodge.mixins.early.minecraft.textures.client;

import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.data.AnimationMetadataSection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.mitchej123.hodgepodge.textures.IPatchedTextureAtlasSprite;

@Mixin(TextureAtlasSprite.class)
public abstract class MixinTextureAtlasSprite implements IPatchedTextureAtlasSprite {

    @Unique
    private boolean needsAnimationUpdate = false;

    @Shadow
    protected int frameTicks;
    @Shadow
    protected int frameIndex;

    @Shadow
    private AnimationMetadataSection animation;

    @Shadow
    protected List<?> frames;

    @Override
    public void markNeedsAnimationUpdate() {
        needsAnimationUpdate = true;
    }

    @Override
    public boolean needsAnimationUpdate() {
        return needsAnimationUpdate;
    }

    @Override
    public void unmarkNeedsAnimationUpdate() {
        needsAnimationUpdate = false;
    }

    @Override
    public void updateAnimationsDryRun() {
        // account for weird subclass that doesn't use the stock mechanisms for animation
        if (animation == null || frames == null) return;

        frameTicks++;
        if (frameTicks >= animation.getTime(frameIndex)) {
            int j = this.animation.getFrameCount() == 0 ? frames.size()
                    : this.animation.getFrameCount();
            this.frameIndex = (this.frameIndex + 1) % j;
            this.frameTicks = 0;
        }
    }
}
