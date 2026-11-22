package dev.swa.gear;

import org.joml.Vector3f;

final class SaberArmRig {
    private static final float UPPER_LENGTH = .365F;
    private static final float FOREARM_LENGTH = .325F;
    private static final float REACH = .655F;
    private static final float REACH_BLEND = .09F;

    private SaberArmRig() {}

    static Vector3f fitMount(Vector3f desired, Contact main, Contact support) {
        Vector3f center = main.mountCenter();
        if (support != null) center.lerp(support.mountCenter(), .5F);
        var direction = new Vector3f(desired).sub(center);
        float distance = direction.length();
        if (distance < .001F) return new Vector3f(desired);
        direction.div(distance);

        float limit = main.reachFrom(center, direction);
        if (support != null) limit = Math.min(limit, support.reachFrom(center, direction));
        // Roll off reach gradually so an animated wrist doesn't stop abruptly at the limit.
        float blendWidth = Math.min(REACH_BLEND, limit * .5F);
        float blendStart = limit - blendWidth;
        if (distance <= blendStart) return new Vector3f(desired);
        float fitted = blendStart + blendWidth * (1F - (float) Math.exp(-(distance - blendStart) / blendWidth));
        return center.fma(fitted, direction);
    }

    static Arm solve(Vector3f mount, Contact contact) {
        var wrist = new Vector3f(mount).add(contact.wristOffset);
        var upperAxis = new Vector3f(wrist).sub(contact.shoulder);
        float distance = upperAxis.length();
        if (distance < .0001F) upperAxis.set(0F, 0F, -1F);
        else upperAxis.div(distance);
        float span = Math.clamp(distance, UPPER_LENGTH - FOREARM_LENGTH + .001F, REACH);
        float reach = (UPPER_LENGTH * UPPER_LENGTH - FOREARM_LENGTH * FOREARM_LENGTH + span * span) / (2F * span);
        float height = (float) Math.sqrt(Math.max(0F, UPPER_LENGTH * UPPER_LENGTH - reach * reach));
        var bend = new Vector3f(contact.elbowPole).fma(-contact.elbowPole.dot(upperAxis), upperAxis);
        if (bend.lengthSquared() < .0001F) {
            bend.set(0F, 0F, 1F).fma(-upperAxis.z, upperAxis);
        }
        var elbow = new Vector3f(contact.shoulder).fma(reach, upperAxis).fma(height, bend.normalize());
        return new Arm(contact.shoulder, elbow, wrist);
    }

    record Contact(Vector3f shoulder, Vector3f wristOffset, Vector3f elbowPole) {
        private Vector3f mountCenter() {
            return new Vector3f(shoulder).sub(wristOffset);
        }

        private float reachFrom(Vector3f origin, Vector3f direction) {
            var offset = new Vector3f(origin).sub(mountCenter());
            float projection = offset.dot(direction);
            return (float) Math.sqrt(Math.max(0F, projection * projection + REACH * REACH - offset.lengthSquared()))
                    - projection;
        }
    }

    record Arm(Vector3f shoulder, Vector3f elbow, Vector3f wrist) {}
}
