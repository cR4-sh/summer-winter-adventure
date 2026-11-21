package dev.swa.space.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.ssnec.encounter.client.SpawnControlClient", remap = false)
abstract class ConvoyDirectorKeysMixin {
    @ModifyConstant(method = "<clinit>", constant = {@Constant(intValue = 75), @Constant(intValue = 297)})
    private static int swa$makeAdminShortcutsOptIn(int previousKey) {
        return -1; // K opens Epic Fight skills and F8 opens Unbound's system map.
    }
}
