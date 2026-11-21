package dev.swa.space.mixin;

import com.starwarsmod.force.ForceData;
import com.starwarsmod.quest.Quest;
import com.starwarsmod.quest.QuestManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.starwarsmod.client.gui.ForceHudOverlay", remap = false)
abstract class StarWCraftTutorialHudMixin {
    @Redirect(method = "renderTracker", at = @At(value = "INVOKE", target =
        "Lcom/starwarsmod/quest/QuestManager;nextStep(Lcom/starwarsmod/force/ForceData;)Lcom/starwarsmod/quest/Quest;"))
    private static Quest swa$hideStarterTutorial(ForceData data) {
        Quest quest = QuestManager.nextStep(data);
        return quest != null && quest.id().startsWith("tut_") ? null : quest;
    }
}
