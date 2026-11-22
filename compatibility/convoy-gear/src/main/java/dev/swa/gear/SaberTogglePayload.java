package dev.swa.gear;

import com.starwarsmod.item.LightsaberItem;
import com.starwarsmod.registry.ModSounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@EventBusSubscriber(modid = GearPreview.ID)
public record SaberTogglePayload() implements CustomPacketPayload {
    public static final Type<SaberTogglePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GearPreview.ID, "toggle_saber"));
    public static final SaberTogglePayload INSTANCE = new SaberTogglePayload();
    private static final StreamCodec<RegistryFriendlyByteBuf, SaberTogglePayload> CODEC = StreamCodec.unit(INSTANCE);

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToServer(TYPE, CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) toggle(player);
        });
    }

    public static ItemStack heldBlade(Player player) {
        ItemStack main = player.getMainHandItem();
        ItemStack offhand = player.getOffhandItem();
        if (main.getItem() instanceof LightsaberItem) return main;
        if (offhand.getItem() instanceof LightsaberItem) return offhand;
        return ItemStack.EMPTY;
    }

    public static boolean hasActiveBlade(Player player) {
        var blade = heldBlade(player);
        return !blade.isEmpty() && LightsaberItem.isActive(blade);
    }

    public static boolean usesHybridFire(Player player, ItemStack stack) {
        return player.isShiftKeyDown() && stack.getItem() instanceof LightsaberItem saber
                && saber.getEffect() == LightsaberItem.Effect.BLASTER && LightsaberItem.isActive(stack);
    }

    private static void toggle(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || player.isPassenger()) return;
        ItemStack blade = heldBlade(player);
        if (blade.isEmpty() || player.getCooldowns().isOnCooldown(blade.getItem())) return;

        var patch = EpicFightCapabilities.getServerPlayerPatch(player);
        if (patch != null && patch.getSkill(SkillSlots.GUARD).isActivated()) {
            player.displayClientMessage(Component.translatable("swa_gear_preview.saber.release_guard"), true);
            return;
        }

        boolean activate = !LightsaberItem.isActive(blade);
        var level = player.serverLevel();
        LightsaberItem.setActive(blade, activate, level.getGameTime());
        var offhand = player.getOffhandItem();
        if (offhand != blade && offhand.getItem() instanceof LightsaberItem) {
            LightsaberItem.setActive(offhand, activate, level.getGameTime());
        }
        player.getCooldowns().addCooldown(blade.getItem(), 4);
        var sound = activate ? ModSounds.LIGHTSABER_OPEN : ModSounds.LIGHTSABER_CLOSE;
        level.playSound(null, player.blockPosition(), sound.get(), SoundSource.PLAYERS, 1F, 1F);
        String message = activate ? "swa_gear_preview.saber.ignited" : "swa_gear_preview.saber.extinguished";
        player.displayClientMessage(Component.translatable(message), true);
    }

    @Override
    public Type<SaberTogglePayload> type() {
        return TYPE;
    }
}
