package dev.swa.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;

final class ArmorMesh extends ModelPart.Cube {
    private final List<Face> faces;
    private final boolean rigid;

    private ArmorMesh(List<Face> faces, boolean rigid) {
        super(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, 1, 1, Set.of());
        this.faces = List.copyOf(faces);
        this.rigid = rigid;
    }

    boolean isRigid() {
        return rigid;
    }

    @Override
    public void compile(PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, int tint) {
        var normal = new Vector3f();
        var point = new Vector3f();
        for (Face face : faces) {
            pose.transformNormal(face.normal(), normal);
            float u = face.material().ordinal() * ArmorModels.MATERIAL_WIDTH + 5;
            paint(pose, consumer, face.a(), point, normal, u, 4, light, overlay, tint);
            paint(pose, consumer, face.b(), point, normal, u, 25, light, overlay, tint);
            paint(pose, consumer, face.c(), point, normal, u + 23, 25, light, overlay, tint);
            paint(pose, consumer, face.d(), point, normal, u + 23, 4, light, overlay, tint);
        }
    }

    private static void paint(PoseStack.Pose pose, VertexConsumer consumer, Point vertex,
            Vector3f position, Vector3f normal, float u, float v, int light, int overlay, int tint) {
        pose.pose().transformPosition(vertex.x() / 16, vertex.y() / 16, vertex.z() / 16, position);
        consumer.addVertex(position.x, position.y, position.z, tint, u / ArmorModels.ATLAS_WIDTH,
                v / ArmorModels.ATLAS_HEIGHT, overlay, light, normal.x, normal.y, normal.z);
    }

    record Point(float x, float y, float z) {
        Vector3f difference(Point origin) {
            return new Vector3f(x - origin.x, y - origin.y, z - origin.z);
        }
    }

    private record Face(ArmorModels.Material material, Point a, Point b, Point c, Point d, Vector3f normal) {}

    static final class Builder {
        private final List<Face> faces = new ArrayList<>();
        private boolean rigid;

        Builder rigid() {
            rigid = true;
            return this;
        }

        Builder quad(ArmorModels.Material material, Point a, Point b, Point c, Point d) {
            Vector3f normal = b.difference(a).cross(c.difference(a));
            if (normal.lengthSquared() > 1e-7F) {
                faces.add(new Face(material, a, b, c, d, normal.normalize()));
            }
            return this;
        }

        Builder triangle(ArmorModels.Material material, Point a, Point b, Point c) {
            return quad(material, a, b, c, c);
        }

        Builder strip(ArmorModels.Material material, Point[] above, Point[] below, boolean wrap) {
            int count = wrap ? above.length : above.length - 1;
            for (int i = 0; i < count; i++) {
                int next = (i + 1) % above.length;
                quad(material, above[i], below[i], below[next], above[next]);
            }
            return this;
        }

        ArmorMesh build() {
            return new ArmorMesh(faces, rigid);
        }
    }
}
