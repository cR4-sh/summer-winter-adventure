package dev.swa.space;

import com.starwarsmod.combat.Anim;
import com.starwarsmod.force.ForcePowers;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationManager.AnimationBuilder;
import yesman.epicfight.api.animation.AnimationManager.AnimationRegistryEvent;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.common.AbstractAnimatorControl.Action;
import yesman.epicfight.network.server.SPAnimatorControl;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

@EventBusSubscriber(modid = "swa_space")
public final class ConvoyForceAnimations {
    private static final Map<ForcePowers, AnimationAccessor<StaticAnimation>> CASTS =
            new EnumMap<>(ForcePowers.class);
    private static final Map<Anim, ChannelClips> CHANNELS = new EnumMap<>(Anim.class);
    private static final Map<ServerPlayer, ChannelClips> ACTIVE_CHANNELS = new WeakHashMap<>();
    private static final ThreadLocal<ServerPlayer> CASTER = new ThreadLocal<>();

    private ConvoyForceAnimations() {}

    @SubscribeEvent
    public static void register(AnimationRegistryEvent event) {
        event.newBuilder("swa_space", ConvoyForceAnimations::build);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer observer)
                || !(event.getTarget() instanceof ServerPlayer caster)) {
            return;
        }
        var clips = ACTIVE_CHANNELS.get(caster);
        var patch = battlePatch(caster);
        if (clips != null && patch != null) {
            var current = patch.getServerAnimator().getPlayerFor(null).getRealAnimation();
            if (current == clips.start() || current == clips.hold()) {
                EpicFightNetworkManager.sendToPlayer(new SPAnimatorControl(Action.PLAY, current, patch, 0.0F), observer);
            }
        }
    }

    private static void build(AnimationBuilder builder) {
        for (ForcePowers power : ForcePowers.values()) {
            switch (power) {
                case TAUNT, QUICK_DRAW, SQUAD_SHIELD, THERMAL_ORDNANCE -> { }
                case CHOKE -> registerChannel(builder, power, Anim.FORCE_CHOKE);
                case LIGHTNING -> registerChannel(builder, power, Anim.FORCE_LIGHTNING);
                default -> CASTS.put(power, clip(builder, name(power), false));
            }
        }
    }

    private static void registerChannel(AnimationBuilder builder, ForcePowers power, Anim nativePose) {
        String name = name(power);
        var start = clip(builder, name + "_start", false);
        var hold = clip(builder, name + "_hold", true);
        var release = clip(builder, name + "_release", false);
        CASTS.put(power, start);
        CHANNELS.put(nativePose, new ChannelClips(start, hold, release));
    }

    private static String name(ForcePowers power) {
        return power.name().toLowerCase(Locale.ROOT);
    }

    private static AnimationAccessor<StaticAnimation> clip(AnimationBuilder builder, String name, boolean loop) {
        // A composite pose keeps the underlying locomotion and never applies root motion.
        return builder.nextAccessor("force/" + name,
                accessor -> new StaticAnimation(0.07F, loop, accessor, Armatures.BIPED)
                        .addProperty(ClientAnimationProperties.LAYER_TYPE, Layer.LayerType.COMPOSITE_LAYER)
                        .addProperty(ClientAnimationProperties.PRIORITY, Layer.Priority.HIGH));
    }

    public static AnimationAccessor<StaticAnimation> cast(ForcePowers power) {
        return CASTS.get(power);
    }

    public static ServerPlayer setCaster(ServerPlayer caster) {
        var previous = CASTER.get();
        if (caster == null) {
            CASTER.remove();
        } else {
            CASTER.set(caster);
        }
        return previous;
    }

    public static boolean replacesLegacyPose(LivingEntity fighter, Anim animation) {
        return fighter == CASTER.get() && switch (animation) {
            case FORCE_PUSH, FORCE_PULL, FORCE_CHOKE, FORCE_LIGHTNING, FORCE_CHANNEL, LEAP, SABER_THROW -> true;
            default -> false;
        };
    }

    public static ServerPlayerPatch battlePatch(ServerPlayer player) {
        return player.isAlive() && !player.isPassenger() && !player.hasDisconnected()
                ? ConvoyCombat.battlePatch(player) : null;
    }

    public static boolean beginChannel(ServerPlayer player, Anim nativePose) {
        var clips = CHANNELS.get(nativePose);
        var patch = battlePatch(player);
        if (clips == null || patch == null) {
            return false;
        }
        patch.playAnimationSynchronized(clips.start(), 0.0F);
        ACTIVE_CHANNELS.put(player, clips);
        return true;
    }

    public static boolean usesEpicChannel(ServerPlayer player) {
        return ACTIVE_CHANNELS.containsKey(player) && battlePatch(player) != null;
    }

    public static void tickChannels() {
        var channels = ACTIVE_CHANNELS.entrySet().iterator();
        while (channels.hasNext()) {
            var entry = channels.next();
            var player = entry.getKey();
            var clips = entry.getValue();
            var patch = battlePatch(player);
            if (patch == null) {
                stopChannelClips(player, clips);
                channels.remove();
                continue;
            }
            var playback = patch.getServerAnimator().getPlayerFor(null);
            if (playback.getAnimation() == clips.start()
                    && playback.getElapsedTime() >= clips.start().get().getTotalTime() - 0.07F) {
                patch.playAnimationSynchronized(clips.hold(), 0.0F);
            }
        }
    }

    public static boolean endChannel(ServerPlayer player) {
        var clips = ACTIVE_CHANNELS.remove(player);
        if (clips == null) {
            return false;
        }
        var patch = battlePatch(player);
        if (patch != null) {
            var current = patch.getServerAnimator().getPlayerFor(null).getRealAnimation();
            if (current == clips.start() || current == clips.hold()) {
                patch.playAnimationSynchronized(clips.release(), 0.0F);
                return true;
            }
        }
        // The client composite may still exist after another server animation replaces it.
        stopChannelClips(player, clips);
        return true;
    }

    private static void stopChannelClips(ServerPlayer player, ChannelClips clips) {
        var patch = EpicFightCapabilities.getServerPlayerPatch(player);
        if (patch != null && !player.hasDisconnected()) {
            patch.stopPlaying(clips.start());
            patch.stopPlaying(clips.hold());
        }
    }

    private record ChannelClips(AnimationAccessor<StaticAnimation> start,
            AnimationAccessor<StaticAnimation> hold, AnimationAccessor<StaticAnimation> release) {}
}
