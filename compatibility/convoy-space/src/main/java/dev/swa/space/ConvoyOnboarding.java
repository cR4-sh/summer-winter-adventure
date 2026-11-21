package dev.swa.space;

import com.starwarsmod.force.ForceData;
import com.starwarsmod.network.StarWarsNetwork;
import com.starwarsmod.quest.SagaMode;
import com.starwarsmod.world.Era;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "swa_space")
public final class ConvoyOnboarding {
    private ConvoyOnboarding() {}

    public static ForceData load(Player player) {
        ForceData data = ForceData.reloadFromPlayer(player);
        boolean hadHero = !data.getHero().isEmpty();
        data.setSagaMode(SagaMode.FREE.id);
        data.setPlayAsHeroes(false);
        data.setJoinScenes(false);
        data.setSeenIntro(true);
        data.setReplay(null);
        data.setHero("");
        if ("unselected".equals(data.getSpecies())) {
            data.setSpecies("human");
        }

        // A previously selected starter planet has no effect when loading an existing save.
        player.getPersistentData().remove("sw_journey_planet");
        data.save(player);
        if (player instanceof ServerPlayer serverPlayer) {
            if (hadHero) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer,
                    new StarWarsNetwork.HeroPayload(serverPlayer.getId(), ""));
            }
            if (!player.getPersistentData().getBoolean("swa_campaign_intro_seen")) {
                serverPlayer.sendSystemMessage(Component.translatable("swa.campaign.first_login",
                    Component.keybind("key.ftbquests.quests")));
                player.getPersistentData().putBoolean("swa_campaign_intro_seen", true);
            }
        }
        return data;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void setCampaignEra(ServerStartedEvent event) {
        if (Era.current() != Era.NEW_REPUBLIC) {
            Era.set(event.getServer(), Era.NEW_REPUBLIC, true);
        }
    }
}
