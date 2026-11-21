package dev.swa.space;

import com.starwarsmod.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = "swa_space")
public final class LegacyStarWCraftTravel {
    private static final StackWalker STACK = StackWalker.getInstance();

    private LegacyStarWCraftTravel() {}

    public static void showNavigationHint(Player player) {
        player.displayClientMessage(Component.translatable("swa.space.use_unbound"), true);
    }

    @SubscribeEvent
    public static void guardScriptedTravel(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ResourceKey<Level> origin = player.serverLevel().dimension();
        ResourceKey<Level> destination = event.getDimension();
        if (origin.equals(destination)) {
            return;
        }
        // Covers legacy scripted scenes that call ServerPlayer.teleportTo directly.
        // Native Unbound travel, vanilla portals and operator commands use their own callers.
        boolean initiatedByStarWCraft = STACK.walk(frames -> frames
                .map(StackWalker.StackFrame::getClassName)
                .filter(name -> name.startsWith("com.starwarsmod.") || name.startsWith("me.moltea.unbound."))
                .findFirst().orElse("").startsWith("com.starwarsmod."));
        if (initiatedByStarWCraft) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void recoverPlayerFromLegacyOrbit(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.serverLevel().dimension().equals(ModDimensions.SPACE)) {
            return;
        }
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        BlockPos spawn = overworld.getSharedSpawnPos();
        player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.fallDistance = 0.0F;
        player.sendSystemMessage(Component.translatable("swa.space.recovered"));
    }
}
