package dev.swa.space.mixin;

import com.starwarsmod.quest.CampaignDirector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CampaignDirector.class, remap = false)
abstract class StarWCraftCrawlMixin {
    @Inject(method = "crawlOnce", at = @At("HEAD"), cancellable = true)
    private static void swa$skipOpeningCrawl(CallbackInfoReturnable<Boolean> callback) {
        // Even StarWCraft Free sends its own introductory crawl after spawning.
        callback.setReturnValue(false);
    }
}
