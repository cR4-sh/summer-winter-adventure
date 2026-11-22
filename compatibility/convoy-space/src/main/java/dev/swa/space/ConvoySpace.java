package dev.swa.space;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import com.starwarsmod.world.MapImporter;
import com.starwarsmod.world.structure.PlanetaryStructureGenerator;
import com.starwarsmod.world.structure.StructureState;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import me.moltea.unbound.api.AddonContext;
import me.moltea.unbound.api.UnboundAddon;
import me.moltea.unbound.api.travel.Craft;
import me.moltea.unbound.api.travel.DestinationSpec;
import me.moltea.unbound.api.travel.Travel;
import me.moltea.unbound.api.travel.TravelEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod("swa_space")
@EventBusSubscriber(modid = "swa_space")
public final class ConvoySpace implements UnboundAddon {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PREPARED = "SwaFlightPrepared";

    @SubscribeEvent
    public static void preparePlanetMaps(ServerStartingEvent event) {
        for (var level : event.getServer().getAllLevels()) {
            var dimension = level.dimension().location();
            var destination = Travel.destinationOf(level.dimension());
            if (destination == null || !destination.id().getNamespace().equals("swa_space")) {
                continue;
            }
            var state = StructureState.get(level);
            if (state.extra.getBoolean(PREPARED)) {
                continue;
            }
            var anchor = MapImporter.importedSpawn(level);
            if (anchor == null && dimension.getPath().equals("hoth")) {
                // Keep the native settlement independent of the research relay and of the first pilot's approach.
                anchor = BlockPos.ZERO;
            }
            if (anchor == null) {
                continue;
            }

            // StarWCraft places entire landmarks on the server thread. Do this during world load.
            long started = System.nanoTime();
            try {
                PlanetaryStructureGenerator.checkAndGenerate(level, anchor);
                state.extra.putBoolean(PREPARED, true);
                state.setDirty();
                LOGGER.info("Prepared StarWCraft landmarks on {} in {} ms", dimension,
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
            } catch (RuntimeException error) {
                LOGGER.error("Could not prepare StarWCraft landmarks on {} during world load", dimension, error);
            }
        }
    }

    @Override
    public void register(AddonContext context) {
        var catalog = Objects.requireNonNull(ConvoySpace.class.getResourceAsStream("/swa_space_worlds.json"));
        try (var reader = new InputStreamReader(catalog, StandardCharsets.UTF_8)) {
            for (var world : new Gson().fromJson(reader, World[].class)) {
                var dimension = ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath("starwarsmod", world.id));
                var destination = new DestinationSpec(world.id)
                    .dimension(dimension)
                    .craft(Craft.HEAVY_ROCKET)
                    .shipOnly()
                    .orbit(world.orbit)
                    .gravity(world.gravity)
                    .body(world.radius, world.angle)
                    .look(15, 1200, world.primary, world.secondary)
                    .chart(context.id("textures/space/" + world.id + ".png"));
                if (world.id.equals("bespin")) {
                    destination.clouds();
                }
                Travel.addDestination(context, destination);
            }
        } catch (IOException error) {
            throw new IllegalStateException("Unable to read the Convoy planet catalog", error);
        }

        TravelEvents.SHIP_TRAVEL.register((ship, pilot, from, to, leg) -> {
            if (leg != TravelEvents.ShipLeg.ENTER_WORLD) {
                return;
            }
            var world = Travel.destinationOf(to.dimension());
            if (world != null && world.id().getNamespace().equals(context.namespace())
                && !StructureState.get(to).extra.getBoolean(PREPARED)) {
                // StarWCraft normally initializes first-visit sites from its own travel code.
                PlanetaryStructureGenerator.checkAndGenerate(to, ship.blockPosition());
            }
        });
    }

    private record World(String id, float orbit, float angle, double radius,
                         float gravity, int primary, int secondary) {}
}
