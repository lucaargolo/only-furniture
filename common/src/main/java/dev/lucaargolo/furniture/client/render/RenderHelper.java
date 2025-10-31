package dev.lucaargolo.furniture.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;

public class RenderHelper {

    public static void renderFilledBox(PoseStack poseStack, VertexConsumer vertexConsumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float red, float green, float blue, float alpha) {
        PoseStack.Pose pose = poseStack.last();
        var normal = Direction.NORTH.getNormal();
        var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ResourceLocation.withDefaultNamespace("block/white_concrete"));

        float u0 = sprite.getU0(), u1 = sprite.getU1();
        float v0 = sprite.getV0(), v1 = sprite.getV1();

        float[][] vertices = {
                {maxX, minY, maxZ, u1, v0}, {maxX, maxY, maxZ, u1, v1}, {minX, maxY, maxZ, u0, v1}, {minX, minY, maxZ, u0, v0},
                {minX, minY, minZ, u0, v0}, {minX, maxY, minZ, u0, v1}, {maxX, maxY, minZ, u1, v1}, {maxX, minY, minZ, u1, v0},
                {minX, minY, maxZ, u0, v0}, {minX, maxY, maxZ, u0, v1}, {minX, maxY, minZ, u1, v1}, {minX, minY, minZ, u1, v0},
                {maxX, maxY, minZ, u0, v0}, {maxX, maxY, maxZ, u0, v1}, {maxX, minY, maxZ, u1, v1}, {maxX, minY, minZ, u1, v0},
                {minX, maxY, maxZ, u0, v0}, {maxX, maxY, maxZ, u0, v1}, {maxX, maxY, minZ, u1, v1}, {minX, maxY, minZ, u1, v0},
                {minX, minY, maxZ, u0, v0}, {minX, minY, minZ, u0, v1}, {maxX, minY, minZ, u1, v1}, {maxX, minY, maxZ, u1, v0}
        };

        for (float[] v : vertices) {
            vertexConsumer.addVertex(pose.pose(), v[0], v[1], v[2])
                    .setColor(red, green, blue, alpha)
                    .setUv(v[3], v[4])
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        }
    }

    public static void renderCrossedCube(PoseStack poseStack, VertexConsumer consumer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float red, float green, float blue, float alpha) {
        PoseStack.Pose pose = poseStack.last();
        double[][] lines = {
                {minX, minY, maxZ, maxX, maxY, maxZ, 0, 0, 1},
                {minX, maxY, maxZ, maxX, minY, maxZ, 0, 0, 1},
                {minX, minY, minZ, maxX, maxY, minZ, 0, 0, -1},
                {minX, maxY, minZ, maxX, minY, minZ, 0, 0, -1},
                {minX, minY, minZ, minX, maxY, maxZ, -1, 0, 0},
                {minX, minY, maxZ, minX, maxY, minZ, -1, 0, 0},
                {maxX, minY, minZ, maxX, maxY, maxZ, 1, 0, 0},
                {maxX, minY, maxZ, maxX, maxY, minZ, 1, 0, 0},
                {minX, maxY, minZ, maxX, maxY, maxZ, 0, 1, 0},
                {minX, maxY, maxZ, maxX, maxY, minZ, 0, 1, 0},
                {minX, minY, minZ, maxX, minY, maxZ, 0, -1, 0},
                {minX, minY, maxZ, maxX, minY, minZ, 0, -1, 0}
        };

        for (double[] l : lines) {
            drawLine(consumer, pose, l[0], l[1], l[2], l[3], l[4], l[5], (float) l[6], (float) l[7], (float) l[8], red, green, blue, alpha);
        }
    }

    public static void renderArrow(PoseStack poseStack, VertexConsumer consumer, Vec3 start, Vec3 vector, float red, float green, float blue, float alpha) {
        drawLine(consumer, poseStack.last(), start.x, start.y, start.z, start.x + vector.x, start.y + vector.y, start.z + vector.z, (float) vector.x, (float) vector.y, (float) vector.z, red, green, blue, alpha);

        Vec3 end = start.add(vector);
        double headLen = vector.length() * 0.2;
        Vec3 dir = vector.normalize();
        Vec3 base = end.subtract(dir.scale(headLen));

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 side = dir.cross(up);
        if (side.lengthSqr() == 0) side = new Vec3(1, 0, 0);

        side = side.normalize().scale(headLen * 0.5);
        Vec3 upDir = dir.cross(side).normalize().scale(headLen * 0.5);

        Vec3[] heads = {base.add(side), base.subtract(side), base.add(upDir), base.subtract(upDir)};

        for (Vec3 h : heads) {
            drawLine(consumer, poseStack.last(), end.x, end.y, end.z, h.x, h.y, h.z, (float) (h.x - end.x), (float) (h.y - end.y), (float) (h.z - end.z), red, green, blue, alpha);
        }
    }

    private static void drawLine(VertexConsumer consumer, PoseStack.Pose pose, double x1, double y1, double z1, double x2, double y2, double z2, float nx, float ny, float nz, float r, float g, float b, float a) {
        consumer.addVertex(pose, (float) x1, (float) y1, (float) z1)
                .setColor(r, g, b, a)
                .setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, (float) x2, (float) y2, (float) z2)
                .setColor(r, g, b, a)
                .setNormal(pose, nx, ny, nz);
    }

}
