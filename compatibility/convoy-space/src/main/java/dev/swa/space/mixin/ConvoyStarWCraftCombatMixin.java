package dev.swa.space.mixin;

import com.starwarsmod.combat.Anim;
import com.starwarsmod.combat.Combat;
import com.starwarsmod.entity.BlasterBolt;
import com.starwarsmod.item.LightsaberItem;
import dev.swa.space.ConvoyCombat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.skill.SkillSlots;

@Mixin(value = Combat.class, remap = false)
abstract class ConvoyStarWCraftCombatMixin {
    @Inject(method = "dodge", at = @At("HEAD"), cancellable = true)
    private static void swa$disableLegacyDodge(ServerPlayer player, float strafe, float forward, CallbackInfo callback) {
        callback.cancel();
    }

    @Inject(method = "setGuard", at = @At("HEAD"), cancellable = true)
    private static void swa$useEpicFightGuard(LivingEntity fighter, boolean active, CallbackInfo callback) {
        if (active && fighter instanceof ServerPlayer) {
            callback.cancel();
        }
    }

    @Redirect(method = "onSaberAttack", at = @At(value = "INVOKE", target = "Lcom/starwarsmod/combat/Combat;anim(Lnet/minecraft/world/entity/LivingEntity;Lcom/starwarsmod/combat/Anim;)V"))
    private static void swa$useEpicFightSwingAnimation(LivingEntity fighter, Anim animation) {
        if (ConvoyCombat.battlePatch(fighter) == null) {
            Combat.anim(fighter, animation);
        }
    }

    @Inject(method = "deflect", at = @At("HEAD"), cancellable = true)
    private static void swa$requireGuardForBlasterDeflection(LivingIncomingDamageEvent event,
            LivingEntity fighter, ServerLevel level, BlasterBolt bolt, CallbackInfo callback) {
        var patch = ConvoyCombat.battlePatch(fighter);
        if (fighter instanceof ServerPlayer && (patch == null || !patch.getSkill(SkillSlots.GUARD).isActivated()
                || !(fighter.getMainHandItem().getItem() instanceof LightsaberItem))) {
            callback.cancel();
        }
    }

    @Redirect(method = "deflect", at = @At(value = "INVOKE", target = "Lcom/starwarsmod/combat/Combat;isGuarding(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private static boolean swa$readEpicFightGuard(LivingEntity fighter) {
        var patch = ConvoyCombat.battlePatch(fighter);
        return patch == null ? Combat.isGuarding(fighter) : patch.getSkill(SkillSlots.GUARD).isActivated();
    }
}
