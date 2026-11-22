package dev.swa.research;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationManager.AnimationRegistryEvent;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.exception.AssetLoadingException;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@EventBusSubscriber(modid = ConvoyResearch.MOD_ID)
public final class ArchivistEchoAnimation {
    private static AnimationAccessor<StaticAnimation> historicalGlove;

    private ArchivistEchoAnimation() {}

    @SubscribeEvent
    public static void register(AnimationRegistryEvent event) {
        event.newBuilder(ConvoyResearch.MOD_ID, builder -> historicalGlove =
                builder.nextAccessor("echo/historical_glove", accessor ->
                        new StaticAnimation(0.07F, false, accessor, Armatures.BIPED)
                                .addProperty(ClientAnimationProperties.LAYER_TYPE, Layer.LayerType.COMPOSITE_LAYER)
                                .addProperty(ClientAnimationProperties.PRIORITY, Layer.Priority.HIGH)));
    }

    static boolean play(ServerPlayer player) {
        var patch = EpicFightCapabilities.getServerPlayerPatch(player);
        if (patch == null || !patch.isEpicFightMode() || player.isPassenger() || patch.getEntityState().inaction()
                || historicalGlove == null || !historicalGlove.isPresent()) {
            return false;
        }
        try {
            if (historicalGlove.get().getTotalTime() <= 0.0F) {
                return false;
            }
        } catch (AssetLoadingException exception) {
            return false;
        }
        patch.playAnimationSynchronized(historicalGlove, 0.0F);
        return true;
    }
}
