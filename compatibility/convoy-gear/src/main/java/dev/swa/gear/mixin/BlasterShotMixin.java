package dev.swa.gear.mixin;

import com.starwarsmod.entity.BlasterBolt;
import com.starwarsmod.force.PlayerClass;
import com.starwarsmod.item.BlasterItem;
import com.starwarsmod.item.PowerCellItem;
import dev.swa.gear.BlasterShotPayload;
import dev.swa.gear.GearPreview;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = BlasterItem.class, remap = false)
abstract class BlasterShotMixin {
    @Redirect(method = "fireBlaster", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private int preview$originalFlashForObservers(ServerLevel level, ParticleOptions particle, double x, double y,
            double z, int count, double spreadX, double spreadY, double spreadZ, double speed,
            ServerLevel ignored, Player shooter, ItemStack stack, float multiplier) {
        if (!(shooter instanceof ServerPlayer owner) || owner.getMainHandItem() != stack
                || !BlasterShotPayload.supported(owner)) {
            return level.sendParticles(particle, x, y, z, count, spreadX, spreadY, spreadZ, speed);
        }
        int sent = 0;
        for (ServerPlayer observer : level.players()) {
            if (observer != owner && level.sendParticles(observer, particle, false, x, y, z, count,
                    spreadX, spreadY, spreadZ, speed)) {
                sent++;
            }
        }
        return sent;
    }

    @Redirect(method = "fireBlaster", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void preview$useWireSample(ServerLevel level, Player excluded, double x, double y, double z,
            SoundEvent sound, SoundSource source, float volume, float pitch) {
        level.playSound(excluded, x, y, z, GearPreview.BLASTER_SHOT.get(), source, volume * .45F, pitch);
    }

    @Inject(method = "fireBlaster", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void preview$sendAcceptedMuzzle(ServerLevel level, Player shooter, ItemStack stack, float multiplier,
            CallbackInfo callback, PowerCellItem.Cell cell, float damage, boolean scoped, PlayerClass playerClass,
            boolean critical, BlasterBolt bolt, Vec3 look, float spread, Vec3 right, Vec3 muzzle, Vector3f tint) {
        if (bolt.isAddedToLevel() && shooter instanceof ServerPlayer owner && owner.getMainHandItem() == stack
                && BlasterShotPayload.supported(owner)) {
            PacketDistributor.sendToPlayer(owner, new BlasterShotPayload(bolt.getId(), bolt.position(),
                    bolt.getDeltaMovement().normalize(), muzzle, tint.x, tint.y, tint.z, critical));
        }
    }
}
