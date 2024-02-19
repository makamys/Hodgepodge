package com.mitchej123.hodgepodge.mixins.early.minecraft.textures.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mitchej123.hodgepodge.textures.AnimationsRenderUtils;

@Mixin(RenderBlocks.class)
public class MixinRenderBlocks {

    @Shadow
    public IBlockAccess world;

    @Shadow
    public IIcon sprite;

    /**
     * @author laetansky Here where things get very tricky. We can't just mark blocks textures for update because this
     *         method gets called only when chunk render cache needs an update (that happens when a state of any block
     *         in that chunk changes). What we can do though is pass the rendered textures up to the WorldRenderer and
     *         later use it in RenderGlobal to mark textures for update and before that even sort WorldRenderers and
     *         apply Occlusion Querry (Basically that means that we will only mark those textures for update that are
     *         visible (on the viewport) at the moment)
     */
    @Inject(method = "*(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;)V", at = @At("HEAD"))
    public void hodgepodge$beforeRenderFace(Block p_147761_1_, double p_147761_2_, double p_147761_4_,
            double p_147761_6_, IIcon icon, CallbackInfo ci) {
        if (sprite != null) {
            icon = sprite;
        }

        AnimationsRenderUtils.markBlockTextureForUpdate(icon, world);
    }

    @Inject(method = "tessellateFire", at = @At("HEAD"))
    public void hodgepodge$markFireBlockAnimationForUpdate(BlockFire instance, int x, int y, int z,
            CallbackInfoReturnable<Boolean> cir) {
        AnimationsRenderUtils.markBlockTextureForUpdate(instance.get(0), world);
        AnimationsRenderUtils.markBlockTextureForUpdate(instance.get(1), world);
    }

    @ModifyVariable(
            method = "tessellateLiquid",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/client/renderer/RenderBlocks;getSprite(Lnet/minecraft/block/Block;II)Lnet/minecraft/util/IIcon;"))
    public IIcon hodgepodge$markFluidAnimationForUpdate(IIcon icon) {
        AnimationsRenderUtils.markBlockTextureForUpdate(icon, world);

        return icon;
    }
}
