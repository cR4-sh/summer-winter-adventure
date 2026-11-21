package dev.swa.space.mixin;

import com.starwarsmod.force.ForceData;
import com.starwarsmod.network.StarWarsNetwork;
import com.starwarsmod.quest.FilmCampaign;
import com.starwarsmod.quest.Quest;
import com.starwarsmod.quest.QuestManager;
import dev.swa.space.LegacyStarWCraftTravel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = StarWarsNetwork.class, remap = false)
abstract class StarWCraftNavigationMixin {
    @Inject(method = "lambda$handleTravel$96", at = @At("HEAD"), cancellable = true)
    private static void swa$useUnbound(IPayloadContext context, StarWarsNetwork.TravelPlanetPayload payload, CallbackInfo callback) {
        if (context.player() instanceof ServerPlayer player) {
            if ("stage_current".equals(payload.planet()) && swa$isLocalStage(player)) {
                return;
            }
            LegacyStarWCraftTravel.showNavigationHint(player);
        }
        callback.cancel();
    }

    @Unique
    private static boolean swa$isLocalStage(ServerPlayer player) {
        Quest quest = QuestManager.nextStep(ForceData.get(player));
        FilmCampaign.Chapter chapter = quest == null ? null : FilmCampaign.chapter(quest.id());
        String destination = chapter == null ? null : chapter.where().dimension();
        return destination == null || destination.equals(player.serverLevel().dimension().location().toString());
    }
}
