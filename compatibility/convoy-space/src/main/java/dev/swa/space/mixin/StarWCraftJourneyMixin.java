package dev.swa.space.mixin;

import com.starwarsmod.force.Journey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Journey.class, remap = false)
abstract class StarWCraftJourneyMixin {
    @Inject(method = {"start", "tick"}, at = @At("HEAD"), cancellable = true, require = 2)
    private static void swa$disableLegacyJourneyStart(CallbackInfo callback) {
        // Skip both the new setup packet and any deferred starter-planet transfer left in a save.
        callback.cancel();
    }
}
