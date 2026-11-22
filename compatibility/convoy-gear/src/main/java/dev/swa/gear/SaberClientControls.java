package dev.swa.gear;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.lwjgl.glfw.GLFW;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@EventBusSubscriber(modid = GearPreview.ID, value = Dist.CLIENT)
public final class SaberClientControls {
    private static final KeyMapping TOGGLE = new KeyMapping("key.swa_gear_preview.toggle_saber",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.starwarsmod");

    private SaberClientControls() {}

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE);
    }

    public static boolean hasToggleChannel() {
        var connection = Minecraft.getInstance().getConnection();
        return connection != null && NetworkRegistry.hasChannel(connection, SaberTogglePayload.TYPE.id());
    }

    @SubscribeEvent
    public static void toggle(ClientTickEvent.Post event) {
        var client = Minecraft.getInstance();
        while (TOGGLE.consumeClick()) {
            if (client.player != null && client.screen == null && !client.player.isPassenger()
                    && hasToggleChannel() && !SaberTogglePayload.heldBlade(client.player).isEmpty()) {
                PacketDistributor.sendToServer(SaberTogglePayload.INSTANCE);
            }
        }
    }

    @SubscribeEvent
    public static void renderHint(RenderGuiLayerEvent.Post event) {
        if (!VanillaGuiLayers.CROSSHAIR.equals(event.getName())) return;
        var client = Minecraft.getInstance();
        var player = client.player;
        if (client.screen != null || player == null || !player.isAlive() || player.isPassenger()
                || !hasToggleChannel() || SaberTogglePayload.heldBlade(player).isEmpty()) return;
        var patch = EpicFightCapabilities.getLocalPlayerPatch(player);
        if (patch == null) return;

        Component cue;
        if (patch.getSkill(SkillSlots.GUARD).isActivated()) {
            cue = Component.translatable("swa_gear_preview.saber.guarding");
        } else if (!EpicFightKeyMappings.GUARD.isDown()) {
            return;
        } else if (!SaberTogglePayload.hasActiveBlade(player)) {
            cue = Component.translatable("swa_gear_preview.saber.needs_blade", TOGGLE.getTranslatedKeyMessage());
        } else if (!patch.isEpicFightMode()) {
            cue = Component.translatable("swa_gear_preview.saber.needs_battle",
                    Component.keybind("key.epicfight.switch_mode"));
        } else {
            return;
        }

        var graphics = event.getGuiGraphics();
        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() / 2 + 15;
        int halfWidth = (client.font.width(cue) + 1) / 2;
        graphics.fill(x - halfWidth - 5, y - 3, x + halfWidth + 5, y + 10, 0xA0141B23);
        graphics.drawCenteredString(client.font, cue, x, y, 0xCDEBFF);
    }
}
