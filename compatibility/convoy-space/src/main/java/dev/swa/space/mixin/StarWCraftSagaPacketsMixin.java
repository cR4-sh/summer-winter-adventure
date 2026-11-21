package dev.swa.space.mixin;

import com.starwarsmod.network.StarWarsNetwork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = StarWarsNetwork.class, remap = false)
abstract class StarWCraftSagaPacketsMixin {
    @Inject(method = {"lambda$register$54", "lambda$register$52", "lambda$register$50"},
        at = @At("HEAD"), cancellable = true, require = 3)
    private static void swa$ignoreLegacyCampaignChoices(CallbackInfo callback) {
        // These are the queued SagaMode, Replay and SagaOption handlers in 1.4.0.
        callback.cancel();
    }
}
