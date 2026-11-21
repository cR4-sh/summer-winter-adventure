package dev.swa.space.mixin;

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.starwarsmod.client.gui.PlanetTravelScreen", remap = false)
abstract class StarWCraftNavigationScreenMixin {
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void swa$hideDestinationButtons(CallbackInfo callback) {
        callback.cancel();
    }

    @Inject(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fillGradient(IIIIII)V", shift = At.Shift.AFTER), cancellable = true)
    private void swa$renderOnlyBackdrop(CallbackInfo callback) {
        callback.cancel();
    }

    @ModifyConstant(method = "render", constant = @Constant(stringValue = "§b◈ HYPERSPACE NAVICOMPUTER ◈"))
    private String swa$pointToFlightMap(String original) {
        return Component.translatable("swa.space.map_hint").getString();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void swa$renderOnlyHint(CallbackInfo callback) {
        callback.cancel();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void swa$disableDestinationClicks(CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(false);
    }
}
