package dev.swa.space.mixin;

import com.hbm.client.Keybinds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;

@Mixin(value = ControlEngine.class, remap = false)
abstract class ConvoyEpicFightFirearmKeysMixin {
    @Shadow private boolean attackLightPressToggle;
    @Shadow private boolean weaponInnatePressToggle;
    @Shadow private int weaponInnatePressCounter;
    @Shadow private SkillSlot reservedOrHoldingSkillSlot;

    @Shadow public abstract void releaseAllServedKeys();

    @Inject(method = "handleEpicFightKeyMappings", at = @At("HEAD"))
    private void swa$clearMeleeInputWhenSwitchingToAFirearm(CallbackInfo callback) {
        boolean attack = swa$usesFirearmInput(EpicFightInputAction.ATTACK);
        boolean innate = swa$usesFirearmInput(EpicFightInputAction.WEAPON_INNATE_SKILL);
        if (attack) attackLightPressToggle = false;
        if (attack || innate) {
            weaponInnatePressToggle = false;
            weaponInnatePressCounter = 0;
        }
        if ((attack && reservedOrHoldingSkillSlot == SkillSlots.COMBO_ATTACKS)
                || (innate && reservedOrHoldingSkillSlot == SkillSlots.WEAPON_INNATE)) {
            releaseAllServedKeys();
        }
    }

    @Inject(method = "maybeAttack", at = @At("HEAD"), cancellable = true)
    private void swa$yieldAttackToTheFirearm(CallbackInfo callback) {
        if (swa$usesFirearmInput(EpicFightInputAction.ATTACK)) callback.cancel();
    }

    @Inject(method = "handleSeparateWeaponInnateSkill", at = @At("HEAD"), cancellable = true)
    private void swa$yieldInnateToTheFirearm(CallbackInfo callback) {
        if (swa$usesFirearmInput(EpicFightInputAction.WEAPON_INNATE_SKILL)) callback.cancel();
    }

    @Inject(method = "maybeGuard", at = @At("HEAD"), cancellable = true)
    private void swa$yieldGuardToTheFirearm(CallbackInfo callback) {
        if (swa$usesFirearmInput(EpicFightInputAction.GUARD)) callback.cancel();
    }

    @Unique
    private static boolean swa$usesFirearmInput(EpicFightInputAction action) {
        return Keybinds.isGunHeld() && Keybinds.isGunKey(action.keyMapping());
    }
}
