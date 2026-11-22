package dev.swa.gear.mixin;

import dev.swa.gear.StarWarsArmor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
abstract class PlayerHelmetMixin {
    @Inject(method = "setModelProperties", at = @At("TAIL"))
    private void preview$hideHeadUnderVaderHelmet(AbstractClientPlayer player, CallbackInfo callback) {
        if (StarWarsArmor.coversHead(player)) {
            var model = ((PlayerRenderer) (Object) this).getModel();
            model.head.visible = false;
            model.hat.visible = false;
        }
    }
}
