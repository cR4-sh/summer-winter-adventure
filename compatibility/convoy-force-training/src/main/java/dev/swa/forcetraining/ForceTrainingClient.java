package dev.swa.forcetraining;

import com.starwarsmod.force.ForceData;
import com.starwarsmod.force.ForcePowers;
import dev.swa.space.ConvoyForceAnimations;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@EventBusSubscriber(modid = ForceTraining.ID, value = Dist.CLIENT)
public final class ForceTrainingClient {
    private static LocalPlayer shown;
    private static List<ForcePowers> queue = List.of();
    private static int index;
    private static int phase;
    private static long next;
    private static AssetAccessor<? extends StaticAnimation> active;

    static boolean isTraining(Minecraft game) {
        var server = game.getSingleplayerServer();
        return server != null && server.getWorldData().getLevelName().equals("Тренировка Силы");
    }

    public static void preview(int selection) {
        var game = Minecraft.getInstance();
        if (game.player == null || !isTraining(game)) return;
        var patch = EpicFightCapabilities.getLocalPlayerPatch(game.player);
        if (patch == null) return;
        stop(patch.getAnimator());
        if (selection == -2) {
            game.player.displayClientMessage(Component.literal("Просмотр остановлен"), true);
            return;
        }
        if (selection == -1) queue = ForceTraining.powers();
        else if (selection >= 0 && selection < ForcePowers.values().length) queue = List.of(ForcePowers.values()[selection]);
        if (queue.isEmpty()) return;
        patch.toEpicFightMode(true);
        game.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
        next = game.level.getGameTime() + 5;
    }

    private static void stop(yesman.epicfight.api.animation.Animator animator) {
        if (active != null) animator.stopPlaying(active);
        active = null;
        queue = List.of();
        index = 0;
        phase = 0;
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var game = Minecraft.getInstance();
        var player = game.player;
        if (player == null || !isTraining(game)) {
            shown = null;
            active = null;
            queue = List.of();
            index = phase = 0;
            return;
        }
        ForceTraining.ensurePowers(ForceData.get(player));
        var patch = EpicFightCapabilities.getLocalPlayerPatch(player);
        if (patch == null) return;
        if (player != shown) {
            shown = player;
            game.options.pauseOnLostFocus = false;
            game.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            patch.toEpicFightMode(true);
        }
        if (game.screen != null || queue.isEmpty() || game.level.getGameTime() < next) return;
        var animator = patch.getAnimator();
        if (index >= queue.size()) {
            stop(animator);
            player.displayClientMessage(Component.literal("Просмотр завершён"), true);
            return;
        }

        var power = queue.get(index);
        boolean channel = power == ForcePowers.CHOKE || power == ForcePowers.LIGHTNING;
        long time = game.level.getGameTime();
        String path = power.name().toLowerCase(Locale.ROOT);
        if (!channel || phase == 0) {
            player.displayClientMessage(Component.literal((index + 1) + "/" + queue.size() + " · " + power.displayName), true);
            active = ConvoyForceAnimations.cast(power);
            animator.playAnimation(active, 0F);
            if (channel) {
                phase = 1;
                next = time + Math.max(1, Math.round(active.get().getTotalTime() * 20F) - 1);
            } else {
                index++;
                next = time + 40;
            }
        } else if (phase == 1) {
            active = AnimationManager.byKey("swa_space:force/" + path + "_hold");
            animator.playAnimation(active, 0F);
            phase = 2;
            next = time + 26;
        } else {
            active = AnimationManager.byKey("swa_space:force/" + path + "_release");
            animator.playAnimation(active, 0F);
            phase = 0;
            index++;
            next = time + 24;
        }
        System.out.println("SWA_FORCE_PREVIEW_CLIENT " + active.get().getRegistryName());
    }
}
