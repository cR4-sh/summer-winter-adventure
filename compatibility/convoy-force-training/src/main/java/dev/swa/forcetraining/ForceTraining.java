package dev.swa.forcetraining;

import com.starwarsmod.force.ForceData;
import com.starwarsmod.force.ForcePowers;
import com.starwarsmod.force.PlayerClass;
import com.starwarsmod.force.Skill;
import com.starwarsmod.network.StarWarsNetwork.ForceDataPayload;
import dev.swa.space.ConvoyForceAnimations;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mod(ForceTraining.ID)
@EventBusSubscriber(modid = ForceTraining.ID)
public final class ForceTraining {
    public static final String ID = "swa_force_training";
    private static final String WORLD = "Тренировка Силы";
    private static final String INITIALIZED = "swa_force_training_ready";
    private static final Set<ForceData> TRAINING_DATA =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private static boolean isTraining(ServerPlayer player) {
        return !player.serverLevel().getServer().isDedicatedServer()
                && WORLD.equals(player.serverLevel().getServer().getWorldData().getLevelName());
    }

    static List<ForcePowers> powers() {
        return Arrays.stream(ForcePowers.values()).filter(power -> ConvoyForceAnimations.cast(power) != null).toList();
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isTraining(player)) prepare(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        TRAINING_DATA.remove(ForceData.get(event.getEntity()));
    }

    static void allowAllPowers(ForceData data) {
        TRAINING_DATA.add(data);
    }

    public static boolean bypassProgression(ForceData data, ForcePowers power) {
        return TRAINING_DATA.contains(data) && power != null && ConvoyForceAnimations.cast(power) != null;
    }

    static void ensurePowers(ForceData data) {
        var available = powers();
        boolean missing = !data.getTrialPowers().containsAll(available)
                || available.stream().map(Skill::unlocking).anyMatch(skill -> skill != null && !data.has(skill));
        if (missing) teachPowers(data);
        allowAllPowers(data);
    }

    private static void teachPowers(ForceData data) {
        var learned = new HashSet<>(data.getSkills());
        var tag = data.serializeNBT();
        var free = new HashSet<>(Arrays.asList(tag.getString("FreeSkills").split(",")));
        free.remove("");
        for (ForcePowers power : powers()) {
            data.grantTrialPower(power);
            for (Skill skill = Skill.unlocking(power); skill != null; skill = skill.requires) {
                if (learned.add(skill)) free.add(skill.name());
            }
        }
        tag.putString("TrialPowers", data.getTrialPowers().stream().map(ForcePowers::name).sorted().collect(Collectors.joining(",")));
        tag.putString("Skills", learned.stream().map(Skill::name).sorted().collect(Collectors.joining(",")));
        tag.putString("FreeSkills", free.stream().sorted().collect(Collectors.joining(",")));
        data.deserializeNBT(tag);
        data.setLevel(Math.max(data.getLevel(), powers().stream().mapToInt(power -> power.requiredLevel).max().orElse(1)));
        allowAllPowers(data);
    }

    private static void prepare(ServerPlayer player) {
        if (!isTraining(player)) return;
        var data = ForceData.get(player);
        data.setPlayerClass(PlayerClass.GUARDIAN);
        ensurePowers(data);
        data.setEnergy(data.getMaxEnergy());
        data.setCooldown(0);
        PacketDistributor.sendToPlayer(player, new ForceDataPayload(data.serializeNBT()));
        var patch = EpicFightCapabilities.getServerPlayerPatch(player);
        if (patch != null) patch.toEpicFightMode(true);
        if (!player.getPersistentData().getBoolean(INITIALIZED)) {
            var server = player.serverLevel().getServer();
            var commands = server.getCommands();
            var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
            // Prepare the same practice platform in an empty world, too.
            var arena = server.overworld();
            for (int chunkX : new int[] {-1, 0}) {
                for (int chunkZ : new int[] {0, 1}) {
                    arena.getChunk(chunkX, chunkZ);
                }
            }
            commands.performPrefixedCommand(source, "execute in minecraft:overworld run fill -8 189 0 8 189 18 minecraft:smooth_stone");
            commands.performPrefixedCommand(source, "execute in minecraft:overworld run fill -8 190 0 8 196 18 minecraft:air");
            commands.performPrefixedCommand(source, "execute in minecraft:overworld run tp @s 0.5 190 8.7 0 0");
            source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
            commands.performPrefixedCommand(source, "item replace entity @s hotbar.0 with starwarsmod:blue_lightsaber");
            commands.performPrefixedCommand(source, "item replace entity @s hotbar.1 with starwarsmod:blaster_rifle");
            commands.performPrefixedCommand(source, "item replace entity @s weapon.offhand with minecraft:air");
            commands.performPrefixedCommand(source, "gamerule doDaylightCycle false");
            commands.performPrefixedCommand(source, "gamerule doMobSpawning false");
            commands.performPrefixedCommand(source, "time set noon");
            commands.performPrefixedCommand(source, "difficulty normal");
            summonTargets(player);
            player.getInventory().selected = 0;
            player.setGameMode(GameType.SURVIVAL);
            player.setHealth(player.getMaxHealth() - 4);
            player.getFoodData().setFoodLevel(20);
            player.getPersistentData().putBoolean(INITIALIZED, true);
        }
        player.sendSystemMessage(Component.literal("Тренировка Силы · 17 приёмов изучены"));
        player.sendSystemMessage(Component.literal("I — выбор приёма, V — применить, /forceanim all — посмотреть анимации."));
        System.out.println("SWA_FORCE_TRAINING_READY powers=" + powers().size() + " unlocked="
                + data.getUnlockedPowers().stream().filter(powers()::contains).count() + " usable="
                + powers().stream().filter(power -> power.canUse(data)).count());
    }

    private static void summonTargets(ServerPlayer player) {
        var commands = player.serverLevel().getServer().getCommands();
        var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        commands.performPrefixedCommand(source, "kill @e[tag=swa_force_training_target]");
        for (int x : new int[] {-3, 0, 3}) {
            double z = x == 0 ? 13.5 : 14.7;
            commands.performPrefixedCommand(source, "summon minecraft:husk " + (x + .5) + " 190 " + z
                    + " {NoAI:1b,Silent:1b,PersistenceRequired:1b,Health:160.0f,Tags:[\"swa_force_training_target\"],"
                    + "attributes:[{id:\"minecraft:generic.max_health\",base:160.0d}],CustomName:'{\"text\":\"Тренировочная цель\"}'}");
        }
    }

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent event) {
        var root = Commands.literal("forceanim").requires(source -> source.getEntity() instanceof ServerPlayer player && isTraining(player));
        root.then(Commands.literal("all").executes(context -> preview(context.getSource().getPlayerOrException(), -1)));
        root.then(Commands.literal("stop").executes(context -> {
            preview(context.getSource().getPlayerOrException(), -2);
            return 1;
        }));
        root.then(Commands.literal("targets").executes(context -> {
            summonTargets(context.getSource().getPlayerOrException());
            return 1;
        }));
        for (ForcePowers power : powers()) {
            root.then(Commands.literal(power.name().toLowerCase(Locale.ROOT)).executes(context ->
                    preview(context.getSource().getPlayerOrException(), power.ordinal())));
        }
        event.getDispatcher().register(root);
    }

    private static int preview(ServerPlayer player, int selection) {
        PacketDistributor.sendToPlayer(player, new TrainingPreviewPayload(selection));
        return 1;
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !isTraining(player)) return;
        if (player.tickCount % 20 == 0) {
            var data = ForceData.get(player);
            ensurePowers(data);
            data.setEnergy(data.getMaxEnergy());
            if (data.getCooldown() > 30) data.setCooldown(30);
            PacketDistributor.sendToPlayer(player, new ForceDataPayload(data.serializeNBT()));
        }
    }
}
