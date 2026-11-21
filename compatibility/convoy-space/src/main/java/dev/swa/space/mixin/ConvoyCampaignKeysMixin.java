package dev.swa.space.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "dev.ftb.mods.ftbquests.client.FTBQuestsKeyMappings", remap = false)
abstract class ConvoyCampaignKeysMixin {
    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = -1, ordinal = 0))
    private static int swa$openCampaignWithJ(int unboundKey) {
        return 74; // GLFW_KEY_J.
    }
}
