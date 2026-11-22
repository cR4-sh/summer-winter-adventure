package dev.swa.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.starwarsmod.item.BlasterItem;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static dev.swa.gear.GearViewGeometry.CHARCOAL;
import static dev.swa.gear.GearViewGeometry.GLOVE;
import static dev.swa.gear.GearViewGeometry.KNUCKLE;
import static dev.swa.gear.GearViewGeometry.arm;
import static dev.swa.gear.GearViewGeometry.box;
import static dev.swa.gear.GearViewGeometry.quad;

@EventBusSubscriber(modid = GearPreview.ID, value = Dist.CLIENT)
public final class RifleViewmodel {
    private static final ResourceLocation WHITE = ResourceLocation.fromNamespaceAndPath(GearPreview.ID, "textures/viewmodel_white.png");
    private static final int RECEIVER = 0x40474B;
    private static final int STEEL = 0x616A6D;
    private static final int BLACK = 0x131618;
    private static final int INDICATOR = 0xC4743A;
    private static final float MUZZLE_HEIGHT = 0.055F;
    private static final float MUZZLE_END = -0.665F;
    private static long drawStarted;
    private static long lastRifleFrame;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void render(RenderHandEvent event) {
        var client = Minecraft.getInstance();
        var player = client.player;
        if (player == null || player.isPassenger() || !client.options.getCameraType().isFirstPerson()
                || !BlasterMuzzle.isRifle(player.getMainHandItem())) {
            if (event.getHand() == InteractionHand.MAIN_HAND) drawStarted = 0;
            return;
        }
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            event.setCanceled(true);
            return;
        }
        if (!(event.getItemStack().getItem() instanceof BlasterItem blaster)
                || !BlasterMuzzle.isRifle(event.getItemStack())) {
            drawStarted = 0;
            return;
        }
        event.setCanceled(true);

        long now = Util.getMillis();
        if (drawStarted == 0 || now - lastRifleFrame > 250) drawStarted = now;
        lastRifleFrame = now;
        var shot = BlasterMuzzle.currentViewShot(blaster, now);
        float age = shot == null || shot.launch() == null ? 0F : now - shot.visibleSince();
        float side = player.getMainArm() == HumanoidArm.RIGHT ? 1F : -1F;
        // Stack data can change before the confirmed shot arrives. Only an actual draw lowers the model.
        float draw = Math.clamp((now - drawStarted) / 150F, 0F, 1F);
        float equip = shot == null ? 1F - draw * draw * (3F - 2F * draw) : 0F;

        PoseStack pose = event.getPoseStack();
        Camera camera = client.gameRenderer.getMainCamera();
        pose.pushPose();
        pose.translate(side * 0.35F, -0.365F - 0.38F * equip, -0.68F);
        pose.mulPose(Axis.YP.rotationDegrees(side * 24F));
        pose.mulPose(Axis.XP.rotationDegrees(13F - equip * 36F));
        pose.mulPose(Axis.ZP.rotationDegrees(side * -5F));
        if (side < 0F) pose.scale(-1, 1, 1);

        var socket = new Vector3f(0, MUZZLE_HEIGHT, MUZZLE_END - .005F);
        if (shot != null) {
            if (shot.launch() == null) {
                Matrix4f model = pose.last().pose();
                Vec3 muzzle = toWorld(camera, model.transformPosition(socket, new Vector3f()));
                Vec3 ahead = toWorld(camera, model.transformPosition(socket.x, socket.y, socket.z - 1F, new Vector3f()));
                BlasterMuzzle.present(shot, muzzle, ahead.subtract(muzzle).normalize(), now);
            }
            applyRecoil(pose, age);
        }

        var actualSocket = toWorld(camera, pose.last().pose().transformPosition(socket, new Vector3f()));

        VertexConsumer vertices = event.getMultiBufferSource().getBuffer(RenderType.entityCutoutNoCull(WHITE));
        int light = event.getPackedLight();
        drawRifle(pose, vertices, light);
        drawHands(pose, vertices, light);

        if (shot != null && shot.launch() != null) {
            var launch = shot.launch();
            long flightTime = now - launch.startedAt();
            if (flightTime < BlasterMuzzle.VIEW_TRACER_MS) {
                // The shot stays in world space after leaving the barrel, even during camera recoil.
                var inversePose = new Matrix4f(pose.last().pose()).invert();
                var emissive = event.getMultiBufferSource().getBuffer(RenderType.entityTranslucentEmissive(WHITE));
                drawShot(pose, emissive, camera, inversePose, shot, launch, socket, flightTime);
                if (Boolean.getBoolean("swa.preview.traceRecoil")) {
                    var bore = pose.last().pose().transformDirection(0, 0, -1, new Vector3f());
                    var departure = toView(camera, launch.departure()).sub(toView(camera, launch.origin()));
                    System.out.printf(java.util.Locale.ROOT,
                            "QA_SMOOTH bolt=%d frame=%d angle=%.3f offset=%.5f recoil=%.3f%n",
                            shot.boltId, flightTime, Math.toDegrees(bore.angle(departure)),
                            launch.origin().distanceTo(actualSocket), recoilAmount(age));
                }
            }
        }
        pose.popPose();
    }

    private static float recoilAmount(float age) {
        float time = Math.max(0F, age - BlasterMuzzle.RECOIL_START_MS) / 39F;
        float tail = Math.clamp((BlasterMuzzle.VIEW_SHOT_MS - age) / 45F, 0F, 1F);
        return time * time * (float) Math.exp(2F - 2F * time) * tail * tail * (3F - 2F * tail);
    }

    private static void applyRecoil(PoseStack pose, float age) {
        float recoil = recoilAmount(age);
        pose.translate(0, -.08F, .025F + .072F * recoil);
        pose.mulPose(Axis.XP.rotationDegrees(1.85F * recoil));
        pose.translate(0, .08F, -.025F);
    }

    private static Vec3 toWorld(Camera camera, Vector3f point) {
        // The hand projection looks down -Z, and Minecraft's camera quaternion faces +Z.
        point.set(-point.x, point.y, -point.z);
        camera.rotation().transform(point);
        return camera.getPosition().add(point.x, point.y, point.z);
    }

    private static Vector3f toView(Camera camera, Vec3 point) {
        Vec3 offset = point.subtract(camera.getPosition());
        var view = new Vector3f((float) offset.x, (float) offset.y, (float) offset.z);
        new Quaternionf(camera.rotation()).conjugate().transform(view);
        return view.set(-view.x, view.y, -view.z);
    }

    private static void drawShot(PoseStack pose, VertexConsumer v, Camera camera, Matrix4f inverse,
            BlasterMuzzle.ViewShot shot, BlasterMuzzle.Launch launch, Vector3f socket, float age) {
        float flight = age / BlasterMuzzle.VIEW_TRACER_MS;
        float tail = Math.max(0F, (age - BlasterMuzzle.EMISSION_MS) / BlasterMuzzle.VIEW_TRACER_MS);
        float head = Math.min(1F, flight + .13F);
        int tint = ((int) (shot.red * 255) << 16) | ((int) (shot.green * 255) << 8) | (int) (shot.blue * 255);
        var start = shotPoint(camera, inverse, launch, socket, tail);
        // Build the beam along its path so the visible first segment follows the barrel axis.
        for (int i = 1; i <= 5; i++) {
            var end = shotPoint(camera, inverse, launch, socket, tail + (head - tail) * i / 5F);
            var direction = new Vector3f(end).sub(start);
            if (direction.lengthSquared() < .00001F) break;
            var side = perpendicular(direction);
            var up = new Vector3f(side).cross(direction).normalize();
            ribbon(pose, v, start, end, side, .027F, tint, .57F);
            ribbon(pose, v, start, end, up, .027F, tint, .57F);
            ribbon(pose, v, start, end, side, .010F, 0xFFF4CA, .98F);
            ribbon(pose, v, start, end, up, .010F, 0xFFF4CA, .98F);
            start = end;
        }

        if (age < BlasterMuzzle.EMISSION_MS && launch.limit() > .025F) {
            float flare = 1F - age / BlasterMuzzle.EMISSION_MS;
            float reach = Math.min(.19F, (float) launch.origin().distanceTo(launch.point(.06F)));
            var flashTip = new Vector3f(socket).add(0F, 0F, -reach);
            var horizontal = new Vector3f(1F, 0F, 0F);
            var vertical = new Vector3f(0F, 1F, 0F);
            ribbon(pose, v, socket, flashTip, horizontal, .079F, tint, .78F * flare);
            ribbon(pose, v, socket, flashTip, vertical, .079F, tint, .78F * flare);
            ribbon(pose, v, socket, flashTip, horizontal, .037F, 0xFFF9E8, flare);
            ribbon(pose, v, socket, flashTip, vertical, .037F, 0xFFF9E8, flare);
        }
    }

    private static Vector3f shotPoint(Camera camera, Matrix4f inverse, BlasterMuzzle.Launch launch,
            Vector3f socket, float progress) {
        float step = Math.min(progress, launch.limit());
        var point = inverse.transformPosition(toView(camera, launch.point(step)));
        if (step < .36F) {
            float alongBore = (float) launch.origin().distanceTo(launch.point(Math.min(step, BlasterMuzzle.STRAIGHT_FRACTION)));
            var fromBarrel = new Vector3f(socket).add(0F, 0F, -alongBore);
            float blend = Mth.clamp((step - BlasterMuzzle.STRAIGHT_FRACTION) / .12F, 0F, 1F);
            return fromBarrel.lerp(point, blend * blend * (3F - 2F * blend));
        }
        return point;
    }

    private static Vector3f perpendicular(Vector3f direction) {
        var side = new Vector3f(direction).cross(0, 1, 0);
        if (side.lengthSquared() < .0001F) side.set(direction).cross(1, 0, 0);
        return side.normalize();
    }

    private static void ribbon(PoseStack pose, VertexConsumer v, Vector3f tail, Vector3f tip, Vector3f normal,
            float width, int color, float alpha) {
        int argb = ((int) (alpha * 255) << 24) | color;
        for (var point : new Vector3f[] { new Vector3f(tail).fma(-width*.46F, normal),
                new Vector3f(tail).fma(width*.46F, normal), new Vector3f(tip).fma(width, normal),
                new Vector3f(tip).fma(-width, normal) }) {
            var transform = pose.last();
            v.addVertex(transform,point.x,point.y,point.z).setColor(argb).setUv(.5F,.5F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(transform,0,1,0);
        }
    }

    private static void drawRifle(PoseStack pose, VertexConsumer v, int light) {
        box(pose,v,light,RECEIVER, -.091F,-.028F,-.341F, .091F,.113F,.134F);
        box(pose,v,light,CHARCOAL,-.081F,.111F,-.287F, .081F,.135F,.088F);
        box(pose,v,light,BLACK,   -.084F,-.052F,-.284F, .084F,-.025F,.105F);
        box(pose,v,light,STEEL,    .091F,.047F,-.214F, .095F,.072F,-.045F);
        box(pose,v,light,CHARCOAL, .092F,.019F,-.209F, .096F,.047F,-.034F);
        box(pose,v,light,BLACK,   -.096F,.015F,-.193F,-.091F,.045F,-.025F);
        box(pose,v,light,INDICATOR,.093F,.073F,-.121F, .097F,.089F,-.079F);
        box(pose,v,light,BLACK,    .092F,.030F,-.024F, .104F,.062F,.093F);
        box(pose,v,light,STEEL,    .101F,.054F,.036F, .115F,.069F,.099F);
        box(pose,v,light,RECEIVER,-.075F,-.057F,-.199F, .076F,-.027F,-.091F);
        box(pose,v,light,CHARCOAL,-.052F,-.101F,-.191F, .053F,-.057F,-.106F);
        box(pose,v,light,STEEL,   -.048F,-.108F,-.189F, .050F,-.098F,-.108F);

        box(pose,v,light,CHARCOAL,-.067F,-.008F,-.511F, .067F,.100F,-.329F);
        box(pose,v,light,RECEIVER,-.045F,.089F,-.510F, .045F,.112F,-.339F);
        for (int slot = 0; slot < 5; slot++) {
            float z = -0.501F + slot * .033F;
            box(pose,v,light,BLACK, -.070F,.016F,z,-.063F,.056F,z+.018F);
            box(pose,v,light,BLACK,  .063F,.016F,z, .070F,.056F,z+.018F);
        }
        cylinder(pose,v,light,STEEL, .049F,-.548F,-.496F);
        cylinder(pose,v,light,CHARCOAL, .036F,-.624F,-.538F);
        cylinder(pose,v,light,RECEIVER, .053F,MUZZLE_END,-.606F);
        cylinder(pose,v,light,BLACK, .031F,MUZZLE_END+.003F,-.612F);

        box(pose,v,light,CHARCOAL,-.034F,.132F,-.164F, .034F,.173F,.058F);
        box(pose,v,light,RECEIVER,-.030F,.147F,-.251F, .030F,.184F,-.164F);
        box(pose,v,light,BLACK,   -.060F,.164F,-.226F, .060F,.251F,.047F);
        box(pose,v,light,RECEIVER,-.064F,.166F,.045F,-.044F,.254F,.062F);
        box(pose,v,light,RECEIVER, .044F,.166F,.045F, .064F,.254F,.062F);
        box(pose,v,light,RECEIVER,-.045F,.235F,.045F, .045F,.254F,.062F);
        box(pose,v,light,CHARCOAL,-.046F,.168F,.045F, .046F,.186F,.062F);
        box(pose,v,light,0x2F4A49,-.042F,.186F,.045F, .042F,.234F,.052F);

        box(pose,v,light,BLACK,-.044F,-.175F,-.040F, .044F,-.041F,.057F);
        box(pose,v,light,CHARCOAL,-.052F,-.148F,-.018F,.052F,-.137F,.070F);
        box(pose,v,light,CHARCOAL,-.052F,-.120F,-.020F,.052F,-.108F,.065F);
        box(pose,v,light,BLACK,-.034F,-.112F,-.116F,.034F,-.090F,-.039F);
        box(pose,v,light,STEEL,-.026F,-.093F,-.100F,.026F,-.051F,-.079F);
        box(pose,v,light,RECEIVER,-.065F,.024F,.127F, .065F,.093F,.224F);
        box(pose,v,light,CHARCOAL,-.087F,-.012F,.200F, .087F,.109F,.242F);
        box(pose,v,light,BLACK,-.089F,-.014F,.238F, .089F,.108F,.257F);
    }

    private static void drawHands(PoseStack pose, VertexConsumer v, int light) {
        arm(pose,v,light, new Vector3f(.327F,-.506F,.322F), new Vector3f(.079F,-.167F,.081F), 1F);
        arm(pose,v,light, new Vector3f(-.512F,-.413F,.268F), new Vector3f(-.085F,-.107F,-.370F), 1F);
        box(pose,v,light,GLOVE, .024F,-.168F,-.016F, .114F,-.058F,.112F);
        box(pose,v,light,KNUCKLE,.078F,-.124F,-.038F, .120F,-.067F,.028F);
        box(pose,v,light,GLOVE,-.039F,-.130F,-.065F, .077F,-.104F,-.023F);
        box(pose,v,light,KNUCKLE,-.030F,-.089F,-.087F, .050F,-.067F,-.040F);
        box(pose,v,light,GLOVE,-.125F,-.143F,-.466F, .028F,-.064F,-.341F);
        box(pose,v,light,KNUCKLE,-.123F,-.096F,-.481F,-.066F,-.028F,-.363F);
        box(pose,v,light,GLOVE,-.052F,-.085F,-.488F, .054F,-.050F,-.455F);
        box(pose,v,light,GLOVE,-.038F,-.070F,-.451F, .069F,-.036F,-.413F);
    }

    private static void cylinder(PoseStack p, VertexConsumer v, int light, int color, float radius,
            float front, float back) {
        for (int segment = 0; segment < 12; segment++) {
            double a = segment * Math.TAU / 12;
            double b = (segment + 1) * Math.TAU / 12;
            float ax = (float) Math.cos(a), ay = (float) Math.sin(a);
            float bx = (float) Math.cos(b), by = (float) Math.sin(b);
            float nx = (ax + bx) * .5F, ny = (ay + by) * .5F;
            quad(p,v,light,color, nx,ny,0,
                    ax*radius, MUZZLE_HEIGHT+ay*radius, front,
                    ax*radius, MUZZLE_HEIGHT+ay*radius, back,
                    bx*radius, MUZZLE_HEIGHT+by*radius, back,
                    bx*radius, MUZZLE_HEIGHT+by*radius, front);
            float bore = radius * .55F;
            quad(p,v,light,segment % 2 == 0 ? STEEL : color,0,0,-1,
                    ax*radius, MUZZLE_HEIGHT+ay*radius, front,
                    bx*radius, MUZZLE_HEIGHT+by*radius, front,
                    bx*bore, MUZZLE_HEIGHT+by*bore, front,
                    ax*bore, MUZZLE_HEIGHT+ay*bore, front);
        }
    }

}
