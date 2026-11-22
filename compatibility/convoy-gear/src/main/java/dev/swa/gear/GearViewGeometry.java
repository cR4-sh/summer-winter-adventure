package dev.swa.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Quaternionf;
import org.joml.Vector3f;

final class GearViewGeometry {
    static final int CHARCOAL = 0x202529;
    static final int CLOTH = 0x272D30;
    static final int CLOTH_SEAM = 0x484E4F;
    static final int GLOVE = 0x303637;
    static final int KNUCKLE = 0x484D4B;

    private GearViewGeometry() {}

    static void arm(PoseStack pose, VertexConsumer v, int light, Vector3f elbow, Vector3f wrist, float width) {
        Vector3f forward = new Vector3f(elbow).sub(wrist);
        float length = forward.length();
        forward.div(length);
        pose.pushPose();
        pose.translate(wrist.x, wrist.y, wrist.z);
        pose.mulPose(new Quaternionf().rotationTo(0, 0, 1, forward.x, forward.y, forward.z));
        pose.scale(width, width, 1F);
        box(pose,v,light,CLOTH,-.071F,-.078F,.019F,.071F,.078F,length);
        box(pose,v,light,CLOTH,-.080F,-.087F,length*.31F,.080F,.087F,length);
        box(pose,v,light,CLOTH_SEAM,-.073F,-.079F,.042F,.073F,.079F,.061F);
        box(pose,v,light,CHARCOAL,-.076F,-.082F,-.020F,.076F,.082F,.031F);
        box(pose,v,light,GLOVE,-.065F,.073F,length*.24F,.045F,.091F,length*.73F);
        box(pose,v,light,CLOTH_SEAM,-.074F,.075F,length*.45F,-.063F,.091F,length*.94F);
        pose.popPose();
    }

    static void upperSleeve(PoseStack pose, VertexConsumer v, int light, Vector3f shoulder, Vector3f elbow) {
        var offset = new Vector3f(shoulder).sub(elbow);
        float length = offset.length();
        pose.pushPose();
        pose.translate(elbow.x, elbow.y, elbow.z);
        pose.mulPose(new Quaternionf().rotationTo(0, 0, 1, offset.x / length, offset.y / length, offset.z / length));
        box(pose, v, light, CLOTH, -.063F, -.069F, -.036F, .063F, .069F, length);
        box(pose, v, light, CLOTH_SEAM, -.064F, -.071F, .038F, .064F, .071F, .061F);
        pose.popPose();
    }

    static void box(PoseStack p, VertexConsumer v, int light, int c,
            float x0, float y0, float z0, float x1, float y1, float z1) {
        quad(p,v,light,c, 0,0,-1, x0,y0,z0, x0,y1,z0, x1,y1,z0, x1,y0,z0);
        quad(p,v,light,c, 0,0,1,  x1,y0,z1, x1,y1,z1, x0,y1,z1, x0,y0,z1);
        quad(p,v,light,c,-1,0,0,  x0,y0,z1, x0,y1,z1, x0,y1,z0, x0,y0,z0);
        quad(p,v,light,c, 1,0,0,  x1,y0,z0, x1,y1,z0, x1,y1,z1, x1,y0,z1);
        quad(p,v,light,c, 0,-1,0, x0,y0,z1, x0,y0,z0, x1,y0,z0, x1,y0,z1);
        quad(p,v,light,c, 0,1,0,  x0,y1,z0, x0,y1,z1, x1,y1,z1, x1,y1,z0);
    }

    static void quad(PoseStack pose, VertexConsumer consumer, int light, int color,
            float nx, float ny, float nz, float... xyz) {
        PoseStack.Pose transform = pose.last();
        for (int index = 0; index < xyz.length; index += 3) {
            consumer.addVertex(transform, xyz[index], xyz[index+1], xyz[index+2])
                    .setColor(color | 0xFF000000).setUv(.5F,.5F).setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light).setNormal(transform,nx,ny,nz);
        }
    }
}
