package dev.swa.gear;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.client.mesh.HumanoidMesh;

@EventBusSubscriber(modid = GearPreview.ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class StarWarsArmor {
    private static final String[] SLOTS = {"helmet", "chestplate", "leggings", "boots"};
    private static final Map<Item, Set> ITEMS = new HashMap<>();

    enum Set {
        BOBA_FETT, CLONE_TROOPER, DEATH_TROOPER, INQUISITOR, JEDI, MANDALORIAN,
        PLASTOID, REBEL_PILOT, ROYAL_GUARD, SANDTROOPER, SCAVENGER, SCOUT, SCRAP,
        SITH, SNOWTROOPER, STORMTROOPER, TEMPLE_GUARD, VADER;

        final String id = name().toLowerCase(java.util.Locale.ROOT);
        final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(GearPreview.ID,
                id.equals("stormtrooper") ? "textures/trooper.png" : "textures/armor/" + id + ".png");
    }

    private StarWarsArmor() {}

    @SubscribeEvent
    public static void register(RegisterClientExtensionsEvent event) {
        HumanoidModelBaker.registerNewTransformer(new EpicArmorModel());
        for (Set set : Set.values()) {
            var extension = new ArmorExtension(set);
            for (String slot : SLOTS) {
                var id = ResourceLocation.fromNamespaceAndPath("starwarsmod", set.id + "_" + slot);
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item instanceof ArmorItem) {
                    event.registerItem(extension, item);
                    ITEMS.put(item, set);
                }
            }
        }
        EpicFightClientEventHooks.Render.PREPARE_MODEL_TO_RENDER.registerEvent(render -> {
            if (render.getMesh() instanceof HumanoidMesh mesh
                    && coversHead(render.getEntityPatch().getOriginal())) {
                mesh.head.setHidden(true);
                if (mesh.hat != null) mesh.hat.setHidden(true);
            }
        }, GearPreview.ID);
    }

    public static boolean coversHead(LivingEntity wearer) {
        return set(wearer.getItemBySlot(EquipmentSlot.HEAD)) == Set.VADER;
    }

    public static ResourceLocation texture(ItemStack stack) {
        Set set = set(stack);
        return set == null ? null : set.texture;
    }

    static Set set(ItemStack stack) {
        return ITEMS.get(stack.getItem());
    }

    static HumanoidModel<LivingEntity> model(Set set, boolean inner) {
        return set == Set.STORMTROOPER ? TrooperArmor.model(inner) : ArmorModels.model(set, inner);
    }

    private static final class ArmorExtension implements IClientItemExtensions {
        private final Set set;
        private HumanoidModel<LivingEntity> outer;
        private HumanoidModel<LivingEntity> inner;

        private ArmorExtension(Set set) {
            this.set = set;
        }

        @Override
        public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                HumanoidModel<?> original) {
            if (outer == null) {
                outer = model(set, false);
                inner = model(set, true);
            }
            return slot == EquipmentSlot.LEGS ? inner : outer;
        }
    }
}
