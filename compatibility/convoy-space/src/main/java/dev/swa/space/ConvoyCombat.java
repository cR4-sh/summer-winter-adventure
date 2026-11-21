package dev.swa.space;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.starwarsmod.item.LightsaberItem;
import com.starwarsmod.registry.ModSounds;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.server.commands.PlayerSkillCommand;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

@EventBusSubscriber(modid = "swa_space")
public final class ConvoyCombat {
    private static final String TRAINED = "swa_combat_started";

    private ConvoyCombat() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFirstLogin(PlayerEvent.PlayerLoggedInEvent event) throws CommandSyntaxException {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.getTags().contains(TRAINED)) {
            return;
        }
        ServerPlayerPatch patch = EpicFightCapabilities.getServerPlayerPatch(player);
        if (patch == null) {
            return;
        }
        var source = player.createCommandSourceStack().withSuppressedOutput();
        if (patch.getSkill(SkillSlots.GUARD).isEmpty()) {
            PlayerSkillCommand.addSkill(source, List.of(player), SkillSlots.GUARD, EpicFightSkills.GUARD);
        }
        if (patch.getSkill(SkillSlots.DODGE).isEmpty()) {
            PlayerSkillCommand.addSkill(source, List.of(player), SkillSlots.DODGE, EpicFightSkills.ROLL);
        }
        patch.toEpicFightMode(true);
        player.addTag(TRAINED);
    }

    public static ServerPlayerPatch battlePatch(LivingEntity fighter) {
        if (fighter instanceof ServerPlayer player) {
            ServerPlayerPatch patch = EpicFightCapabilities.getServerPlayerPatch(player);
            if (patch != null && patch.isEpicFightMode()) {
                return patch;
            }
        }
        return null;
    }

    public static void igniteSaber(ServerPlayer player) {
        var stack = player.getMainHandItem();
        if (stack.getItem() instanceof LightsaberItem && !LightsaberItem.isActive(stack)) {
            var level = player.serverLevel();
            LightsaberItem.setActive(stack, true, level.getGameTime());
            level.playSound(null, player.blockPosition(), ModSounds.LIGHTSABER_OPEN.get(), SoundSource.PLAYERS, 1, 1);
        }
    }
}
