package dev.swa.space.mixin;

import java.util.UUID;
import me.moltea.unbound.space.beacon.Beacons;
import me.moltea.unbound.space.beacon.LandingBeacons.Beacon;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Beacons.class, remap = false)
abstract class PublicLandingBeaconMixin {
    @Unique private static final UUID swa$PORT_OWNER = UUID.fromString("14291763-2e41-4c78-91da-912fc1afe176");
    @Unique private static final BlockPos swa$TATOOINE_PORT = new BlockPos(0, 140, -12);
    @Unique private static final BlockPos swa$DEATH_STAR_PORT = new BlockPos(660, 108, 575);
    @Unique private static final String swa$HOTH_RELAY = "SWA Hoth Relay";

    @Inject(method = "admits", at = @At("HEAD"), cancellable = true)
    private static void swa$admitAtPublicPort(ServerPlayer player, Beacon beacon,
                                             CallbackInfoReturnable<Boolean> callback) {
        // Ordinary player-owned beacons keep Unbound's access rules.
        if (swa$PORT_OWNER.equals(beacon.owner())
            && (swa$TATOOINE_PORT.equals(beacon.pos()) || swa$DEATH_STAR_PORT.equals(beacon.pos())
                || swa$HOTH_RELAY.equals(beacon.keeper()))) {
            callback.setReturnValue(true);
        }
    }
}
