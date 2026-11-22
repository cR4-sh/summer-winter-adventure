package dev.swa.gear.mixin;

import com.starwarsmod.item.BlasterItem;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.events.engine.RenderEngine;

@Mixin(RenderEngine.class)
abstract class EpicFirearmViewMixin {
    @Inject(method = "epicfight$renderHand", at = @At("HEAD"), cancellable = true)
    private void preview$useFirearmView(RenderHandEvent event, CallbackInfo callback) {
        var player = Minecraft.getInstance().player;
        if (player != null && player.getMainHandItem().getItem() instanceof BlasterItem) callback.cancel();
    }
}
