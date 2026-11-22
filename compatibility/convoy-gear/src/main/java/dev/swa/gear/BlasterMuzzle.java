package dev.swa.gear;

import com.starwarsmod.entity.BlasterBolt;
import com.starwarsmod.item.BlasterItem;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class BlasterMuzzle {
    private static final ResourceLocation RIFLE = ResourceLocation.fromNamespaceAndPath("starwarsmod", "blaster_rifle");
    private static final double MERGE_DISTANCE = 7.0;
    public static final float STRAIGHT_FRACTION = .24F;
    public static final int EMISSION_MS = 38;
    private static final Map<Integer, Trace> TRACES = new HashMap<>();
    private static WeakReference<ClientLevel> activeLevel = new WeakReference<>(null);
    private static ViewShot lastViewShot;
    public static final int VIEW_TRACER_MS = 82;
    public static final int RECOIL_START_MS = 20;
    public static final int VIEW_SHOT_MS = 190;
    private static final int EQUIP_RECOVERY_MS = 350;

    private BlasterMuzzle() {}

    public static boolean isRifle(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(RIFLE);
    }

    static void receive(BlasterShotPayload shot) {
        var game = Minecraft.getInstance();
        ClientLevel level = game.level;
        bind(level);
        if (level == null || game.player == null) return;
        long now = Util.getMillis();
        if (firstPerson(game) && isRifle(game.player.getMainHandItem())
                && game.player.getMainHandItem().getItem() instanceof BlasterItem item) {
            TRACES.entrySet().removeIf(entry -> level.getGameTime() > entry.getValue().expires());
            Vec3 aimPoint = clipEnd(level, game, shot.origin(), shot.origin().add(shot.direction().scale(MERGE_DISTANCE)));
            lastViewShot = new ViewShot(shot.boltId(), item, aimPoint, now, shot.red(), shot.green(), shot.blue());
            TRACES.put(shot.boltId(), new Trace(shot.origin(), shot.direction(), level.getGameTime() + 5, now));
            return;
        }
        Vec3 muzzle = clipEnd(level, game, shot.origin(), shot.fallbackMuzzle());
        flash(level, new DustParticleOptions(new Vector3f(shot.red(), shot.green(), shot.blue()), 1.2F),
                muzzle, 6, 0.04, 0.0);
        flash(level, ParticleTypes.SMALL_FLAME, muzzle, 2, 0.02, 0.01);
        flash(level, ParticleTypes.SMOKE, muzzle, 2, 0.03, 0.01);
        if (shot.critical()) flash(level, ParticleTypes.CRIT, muzzle, 8, 0.1, 0.2);
    }

    public static boolean hideWorldBolt(BlasterBolt bolt) {
        var game = Minecraft.getInstance();
        if (game.level == null || !firstPerson(game)) return false;
        Trace trace = game.level == activeLevel.get() ? TRACES.get(bolt.getId()) : null;
        if (trace != null && game.level.getGameTime() <= trace.expires()) {
            double distance = bolt.position().subtract(trace.origin()).dot(trace.direction());
            return Util.getMillis() - trace.startMillis() < VIEW_TRACER_MS || distance < MERGE_DISTANCE - 1;
        }
        // The spawn packet and shot acknowledgement need not be handled in the same frame.
        return bolt.getOwner() == game.player && isRifle(game.player.getMainHandItem()) && bolt.tickCount <= 2
                && bolt.position().distanceToSqr(game.player.getEyePosition()) < MERGE_DISTANCE * MERGE_DISTANCE;
    }

    private static Vec3 clipEnd(ClientLevel level, Minecraft game, Vec3 from, Vec3 to) {
        if (from.distanceToSqr(to) < 1.0E-8) {
            return to;
        }
        var hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, game.player));
        return hit.getType() == HitResult.Type.MISS ? to : hit.getLocation();
    }

    private static void flash(ClientLevel level, ParticleOptions particle, Vec3 origin, int count, double scatter,
            double speed) {
        var random = level.getRandom();
        for (int i = 0; i < count; i++) {
            level.addParticle(particle, origin.x + random.nextGaussian() * scatter,
                    origin.y + random.nextGaussian() * scatter, origin.z + random.nextGaussian() * scatter,
                    random.nextGaussian() * speed, random.nextGaussian() * speed, random.nextGaussian() * speed);
        }
    }

    private static boolean firstPerson(Minecraft game) {
        return game.player != null && game.getCameraEntity() == game.player && game.options.getCameraType().isFirstPerson();
    }

    private static void bind(ClientLevel level) {
        if (level != activeLevel.get()) {
            activeLevel = new WeakReference<>(level);
            lastViewShot = null;
            TRACES.clear();
        }
    }

    public static ViewShot currentViewShot(BlasterItem item, long now) {
        return lastViewShot != null && lastViewShot.item == item
                && now - lastViewShot.visibleSince() <= EQUIP_RECOVERY_MS ? lastViewShot : null;
    }

    public static void present(ViewShot shot, Vec3 muzzle, Vec3 bore, long now) {
        if (shot.launch != null) return;
        var game = Minecraft.getInstance();
        Trace trace = TRACES.get(shot.boltId);
        if (trace == null || game.level == null) return;
        Vec3 clearMuzzle = clipEnd(game.level, game, trace.origin(), muzzle);
        if (clearMuzzle.distanceToSqr(muzzle) >= 0.0025) {
            // A wall moved into the space between the camera and muzzle since the last frame.
            lastViewShot = null;
            TRACES.remove(shot.boltId);
            return;
        }
        Vec3 end = clipEnd(game.level, game, muzzle, shot.aimPoint);
        double distance = muzzle.distanceTo(end);
        Vec3 departure = muzzle.add(bore.scale(Math.min(.85, distance * .25)));
        Vec3 departureControl = departure.add(bore.scale(Math.min(.50, distance * .15)));
        Vec3 arrival = end.subtract(trace.direction().scale(Math.min(1.15, distance * .30)));
        var launch = new Launch(muzzle, departure, departureControl, arrival, end, 1F, now);
        Vec3 previous = muzzle;
        // The near tracer blends onto the authoritative ray without crossing nearby cover.
        for (int i = 1; i <= 8; i++) {
            float fraction = i / 8F;
            Vec3 next = launch.point(fraction);
            Vec3 clear = clipEnd(game.level, game, previous, next);
            if (clear.distanceToSqr(next) > 1E-6) {
                double length = previous.distanceTo(next);
                float limit = (i - 1 + (float) (previous.distanceTo(clear) / length)) / 8F;
                launch = new Launch(muzzle, departure, departureControl, arrival, end, limit, now);
                break;
            }
            previous = next;
        }
        shot.launch = launch;
        TRACES.put(shot.boltId, new Trace(trace.origin(), trace.direction(), trace.expires(), now));
    }

    public static final class ViewShot {
        public final int boltId;
        public final Vec3 aimPoint;
        public final float red;
        public final float green;
        public final float blue;
        private final BlasterItem item;
        private final long receivedAt;
        private Launch launch;

        private ViewShot(int boltId, BlasterItem item, Vec3 aimPoint, long receivedAt,
                float red, float green, float blue) {
            this.boltId = boltId;
            this.item = item;
            this.aimPoint = aimPoint;
            this.receivedAt = receivedAt;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public Launch launch() {
            return launch;
        }

        public long visibleSince() {
            return launch == null ? receivedAt : launch.startedAt();
        }
    }

    public record Launch(Vec3 origin, Vec3 departure, Vec3 departureControl, Vec3 arrival, Vec3 end, float limit,
            long startedAt) {
        public Vec3 point(float fraction) {
            double step = Math.clamp(fraction, 0F, limit);
            if (step <= STRAIGHT_FRACTION) return origin.lerp(departure, step / STRAIGHT_FRACTION);
            double t = (step - STRAIGHT_FRACTION) / (1 - STRAIGHT_FRACTION);
            double remaining = 1 - t;
            return departure.scale(remaining * remaining * remaining)
                    .add(departureControl.scale(3 * remaining * remaining * t))
                    .add(arrival.scale(3 * remaining * t * t))
                    .add(end.scale(t * t * t));
        }
    }

    private record Trace(Vec3 origin, Vec3 direction, long expires, long startMillis) {}
}
