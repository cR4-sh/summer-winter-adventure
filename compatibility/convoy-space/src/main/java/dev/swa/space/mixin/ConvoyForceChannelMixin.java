package dev.swa.space.mixin;

import com.starwarsmod.combat.Anim;
import com.starwarsmod.combat.Combat;
import com.starwarsmod.combat.ForceChannel;
import dev.swa.space.ConvoyForceAnimations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ForceChannel.class, remap = false)
abstract class ConvoyForceChannelMixin {
    private static final String NATIVE_POSE =
            "Lcom/starwarsmod/combat/Combat;anim(Lnet/minecraft/world/entity/LivingEntity;Lcom/starwarsmod/combat/Anim;)V";

    @Redirect(method = "input", at = @At(value = "INVOKE", target = NATIVE_POSE))
    private static void swa$startChannel(LivingEntity fighter, Anim animation) {
        if (!(fighter instanceof ServerPlayer player) || !ConvoyForceAnimations.beginChannel(player, animation)) {
            Combat.anim(fighter, animation);
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = NATIVE_POSE))
    private static void swa$keepChannelPose(LivingEntity fighter, Anim animation) {
        if (!(fighter instanceof ServerPlayer player) || !ConvoyForceAnimations.usesEpicChannel(player)) {
            Combat.anim(fighter, animation);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private static void swa$advanceChannels(ServerTickEvent.Post event, CallbackInfo callback) {
        ConvoyForceAnimations.tickChannels();
    }

    @Redirect(method = "stop", at = @At(value = "INVOKE", target = NATIVE_POSE))
    private static void swa$releaseChannel(LivingEntity fighter, Anim animation) {
        if (!(fighter instanceof ServerPlayer player) || !ConvoyForceAnimations.endChannel(player)) {
            Combat.anim(fighter, animation);
        }
    }
}
