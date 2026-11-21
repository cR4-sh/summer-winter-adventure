package dev.swa.space;

import com.google.gson.Gson;
import com.starwarsmod.world.structure.PlanetaryStructureGenerator;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import me.moltea.unbound.api.AddonContext;
import me.moltea.unbound.api.UnboundAddon;
import me.moltea.unbound.api.travel.Craft;
import me.moltea.unbound.api.travel.DestinationSpec;
import me.moltea.unbound.api.travel.Travel;
import me.moltea.unbound.api.travel.TravelEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.Mod;

@Mod("swa_space")
public final class ConvoySpace implements UnboundAddon {
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
            if (world != null && world.id().getNamespace().equals(context.namespace())) {
                // StarWCraft normally initializes first-visit sites from its own travel code.
                PlanetaryStructureGenerator.checkAndGenerate(to, ship.blockPosition());
            }
        });
    }

    private record World(String id, float orbit, float angle, double radius,
                         float gravity, int primary, int secondary) {}
}
