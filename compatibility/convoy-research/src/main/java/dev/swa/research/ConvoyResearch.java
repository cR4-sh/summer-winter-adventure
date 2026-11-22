package dev.swa.research;

import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.research.ResearchEvent;
import com.leclowndu93150.thaumaturge.api.research.ResearchUnlockConditions;
import com.leclowndu93150.thaumaturge.api.research.UnlockRequirement;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.network.ClientboundOpenThaumonomiconPayload;
import dev.swa.research.LessonProgress.Comparison;
import dev.swa.research.LessonProgress.Prediction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(ConvoyResearch.MOD_ID)
@EventBusSubscriber(modid = ConvoyResearch.MOD_ID)
public final class ConvoyResearch {
    public static final String MOD_ID = "swa_research";
    public static final ResourceLocation ECHO_OBSERVATION = id("echo_observation");
    public static final ResourceLocation PSYCHOMETRY = id("psychometry");

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID);
    private static final DeferredBlock<LessonStation> STATION =
            BLOCKS.register("station", () -> new LessonStation(
                    BlockBehaviour.Properties.of().strength(-1.0F, 3_600_000.0F).noLootTable()));
    private static final DeferredHolder<AttachmentType<?>, AttachmentType<LessonProgress>> LESSON =
            ATTACHMENTS.register("lesson", () -> AttachmentType.builder(() -> LessonProgress.EMPTY)
                    .serialize(LessonProgress.CODEC)
                    .copyOnDeath()
                    .sync((holder, observer) -> holder == observer, ByteBufCodecs.fromCodec(LessonProgress.CODEC))
                    .build());
    private static final DeferredHolder<SoundEvent, SoundEvent> DOUBLE_KNOCK =
            SOUNDS.register("double_knock", () -> SoundEvent.createVariableRangeEvent(id("double_knock")));

    public ConvoyResearch(IEventBus modBus) {
        BLOCKS.register(modBus);
        ATTACHMENTS.register(modBus);
        SOUNDS.register(modBus);
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ResearchUnlockConditions.require(ECHO_OBSERVATION,
                    (player, research) -> lesson(player).heardEcho(), text("requires_echo"));
            ResearchUnlockConditions.require(PSYCHOMETRY, UnlockRequirement.completed(ECHO_OBSERVATION),
                    text("requires_observation"));
        });
    }

    public static boolean hasPsychometry(ServerPlayer player) {
        return research(player).hasCompleted(PSYCHOMETRY);
    }

    // The caller checks access to the expedition and that this position belongs to the registered site.
    public static String perform(ServerPlayer player, int x, int y, int z, InteractionHand hand, String expectedAction) {
        var level = player.serverLevel();
        var pos = new BlockPos(x, y, z);
        if (hand != InteractionHand.MAIN_HAND || !level.getServer().isSameThread()
                || !player.isAlive() || player.isSpectator() || !player.canInteractWithBlock(pos, 0)
                || !player.mayInteract(level, pos)) {
            return "invalid";
        }
        var chunkPos = new ChunkPos(pos);
        var chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
        if (chunk == null) {
            return "invalid";
        }
        var block = chunk.getBlockState(pos);
        if (!block.is(STATION.get())) {
            return "invalid";
        }
        var action = block.getValue(LessonStation.ACTION);
        if (!action.getSerializedName().equals(expectedAction)) {
            return "invalid";
        }
        if (action == LessonStation.Action.ENROLL) {
            return enroll(player);
        }
        if (action == LessonStation.Action.SHIP_BUCKLE) {
            return readBuckle(player);
        }
        if (!lesson(player).enrolled()) {
            return deny(player, "requires_enrollment");
        }
        return switch (action) {
            case CONTROL_GLOVE -> observeControl(player);
            case HISTORICAL_GLOVE -> observeHistorical(player);
            case PREDICT_MATERIAL -> predict(player, Prediction.MATERIAL);
            case PREDICT_PERSONAL_HISTORY -> predict(player, Prediction.PERSONAL_HISTORY);
            case COMPARISON_TOKEN -> compareToken(player);
            default -> throw new IllegalStateException("Unexpected lesson action " + action);
        };
    }

    @SubscribeEvent
    public static void onStageAdvanced(ResearchEvent.StageAdvanced event) {
        if (!(event.player() instanceof ServerPlayer player)) {
            return;
        }
        var lesson = lesson(player);
        if (ECHO_OBSERVATION.equals(event.research())) {
            if (!lesson.heardEcho()) {
                report(player, "requires_echo");
                event.setCanceled(true);
            }
        } else if (PSYCHOMETRY.equals(event.research())) {
            boolean allowed = hasObservation(player) && lesson.sawControl() && switch (event.newStage()) {
                case 1 -> true;
                case 2 -> lesson.prediction() != Prediction.NONE;
                case 3 -> lesson.comparison() == Comparison.VERIFIED;
                default -> false;
            };
            if (!allowed) {
                report(player, switch (event.newStage()) {
                    case 2 -> "needs_prediction";
                    case 3 -> "needs_comparison";
                    default -> "requires_both_gloves";
                });
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onCompleted(ResearchEvent.Completed event) {
        if (!(event.player() instanceof ServerPlayer player)) {
            return;
        }
        if (ECHO_OBSERVATION.equals(event.research()) && !lesson(player).heardEcho()) {
            event.setCanceled(true);
        }
        if (PSYCHOMETRY.equals(event.research())
                && (!hasObservation(player) || lesson(player).comparison() != Comparison.VERIFIED)) {
            event.setCanceled(true);
        }
    }

    private static String enroll(ServerPlayer player) {
        var lesson = lesson(player);
        if (!lesson.enrolled()) {
            save(player, lesson.enroll());
            report(player, "enrolled");
        } else if (hasPsychometry(player)) {
            report(player, "mastered");
        } else {
            // Previously revealed evidence remains visible in the journal's chat record.
            if (lesson.heardEcho()) {
                report(player, "historical_echo");
            }
            if (lesson.sawControl()) {
                report(player, "control_glove");
            }
            if (lesson.comparison() == Comparison.CONTRADICTED) {
                report(player, "contradicted");
            } else if (lesson.comparison() == Comparison.VERIFIED) {
                report(player, "verified");
            }
        }
        PacketDistributor.sendToPlayer(player, ClientboundOpenThaumonomiconPayload.INSTANCE);
        return "lesson_step";
    }

    private static String observeControl(ServerPlayer player) {
        save(player, lesson(player).observeControl());
        openComparisonStage(player);
        echo(player, "control_glove");
        return "lesson_step";
    }

    private static String observeHistorical(ServerPlayer player) {
        save(player, lesson(player).observeEcho());
        if (!hasObservation(player)) {
            unlock(player, ECHO_OBSERVATION);
            ResearchManager.advanceStage(player, ECHO_OBSERVATION);
        }
        if (hasObservation(player)) {
            unlock(player, PSYCHOMETRY);
            openComparisonStage(player);
        }
        player.playNotifySound(DOUBLE_KNOCK.get(), SoundSource.BLOCKS, 0.85F, 1.0F);
        if (!ArchivistEchoAnimation.play(player)) {
            player.swing(InteractionHand.MAIN_HAND, true);
        }
        showEcho(player, "historical_echo");
        return "historical_echo";
    }

    private static String predict(ServerPlayer player, Prediction prediction) {
        var lesson = lesson(player);
        if (!hasObservation(player) || !lesson.sawControl()) {
            return deny(player, "requires_both_gloves");
        }
        if (hasPsychometry(player) || lesson.comparison() == Comparison.VERIFIED) {
            return deny(player, "theory_already_verified");
        }
        if (lesson.prediction() != Prediction.NONE) {
            return deny(player, "prediction_already_set");
        }
        save(player, lesson.predict(prediction));
        unlock(player, PSYCHOMETRY);
        ResearchManager.setStage(player, PSYCHOMETRY, 2);
        report(player, "prediction." + prediction.getSerializedName());
        return "lesson_step";
    }

    private static String compareToken(ServerPlayer player) {
        var lesson = lesson(player);
        if (hasPsychometry(player) || lesson.comparison() == Comparison.VERIFIED) {
            echo(player, "token_echo");
            report(player, "verified");
            return "lesson_step";
        }
        if (lesson.prediction() == Prediction.NONE) {
            return deny(player, "needs_prediction");
        }
        var checked = lesson.checkToken();
        save(player, checked);
        echo(player, "token_echo");
        if (checked.comparison() == Comparison.CONTRADICTED) {
            ResearchManager.setStage(player, PSYCHOMETRY, 1);
            report(player, "contradicted");
        } else {
            ResearchManager.setStage(player, PSYCHOMETRY, 2);
            report(player, "verified");
        }
        return "lesson_step";
    }

    private static String readBuckle(ServerPlayer player) {
        if (!hasPsychometry(player)) {
            return deny(player, "buckle_locked");
        }
        echo(player, "buckle_echo");
        return "buckle_echo";
    }

    private static void openComparisonStage(ServerPlayer player) {
        if (hasObservation(player) && lesson(player).sawControl() && research(player).stageOf(PSYCHOMETRY) == 0) {
            ResearchManager.setStage(player, PSYCHOMETRY, 1);
        }
    }

    private static void unlock(ServerPlayer player, ResourceLocation entry) {
        if (!research(player).knows(entry)) {
            ResearchManager.unlock(player, entry);
        }
    }

    private static boolean hasObservation(ServerPlayer player) {
        return research(player).hasCompleted(ECHO_OBSERVATION);
    }

    private static PlayerResearch research(Player player) {
        return PlayerResearchAccess.of(player);
    }

    private static LessonProgress lesson(Player player) {
        return player.getData(LESSON);
    }

    private static void save(ServerPlayer player, LessonProgress progress) {
        if (!lesson(player).equals(progress)) {
            player.setData(LESSON, progress);
        }
    }

    private static void echo(ServerPlayer player, String key) {
        player.swing(InteractionHand.MAIN_HAND, true);
        showEcho(player, key);
    }

    private static void showEcho(ServerPlayer player, String key) {
        player.displayClientMessage(text(key), true);
        report(player, key);
    }

    private static String deny(ServerPlayer player, String key) {
        report(player, key);
        return "denied";
    }

    private static void report(ServerPlayer player, String key) {
        player.sendSystemMessage(text(key));
    }

    private static Component text(String key) {
        return Component.translatable("swa_research.lesson." + key);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
