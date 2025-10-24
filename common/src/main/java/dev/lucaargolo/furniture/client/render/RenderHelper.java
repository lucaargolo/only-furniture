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

        //Render cube
        vertexConsumer.addVertex(pose.pose(), maxX, minY, maxZ).setColor(red, green, blue, alpha).setUv(sprite.getU1(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), maxX, maxY, maxZ).setColor(red, green, blue, alpha).setUv(sprite.getU1(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), minX, maxY, maxZ).setColor(red, green, blue, alpha).setUv(sprite.getU0(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), minX, minY, maxZ).setColor(red, green, blue, alpha).setUv(sprite.getU0(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());

        vertexConsumer.addVertex(pose.pose(), minX, minY, minZ).setColor(red, green, blue, alpha).setUv(sprite.getU0(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), minX, maxY, minZ).setColor(red, green, blue, alpha).setUv(sprite.getU0(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), maxX, maxY, minZ).setColor(red, green, blue, alpha).setUv(sprite.getU1(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), maxX, minY, minZ).setColor(red, green, blue, alpha).setUv(sprite.getU1(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());

        vertexConsumer.addVertex(pose.pose(), minX, minY, maxZ).setColor(red, green, blue, alpha).setUv(sprite.getU0(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), minX, maxY, maxZ).setColor(red, green, blue, alpha).setUv(sprite.getU0(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), minX, maxY, minZ).setColor(red, green, blue, alpha).setUv(sprite.getU1(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), minX, minY, minZ).setColor(red, green, blue, alpha).setUv(sprite.getU1(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());

        vertexConsumer.addVertex(pose.pose(), maxX, maxY, minZ).setColor(red, green, blue, alpha).setUv(sprite.getU0(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), maxX, maxY, maxZ).setColor(red, green, blue, alpha).setUv(sprite.getU0(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), maxX, minY, maxZ).setColor(red, green, blue, alpha).setUv(sprite.getU1(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), maxX, minY, minZ).setColor(red, green, blue, alpha).setUv(sprite.getU1(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());

        vertexConsumer.addVertex(pose.pose(), minX, maxY, maxZ).setColor(red, green, blue, alpha).setUv(sprite.getU0(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), maxX, maxY, maxZ).setColor(red, green, blue, alpha).setUv(sprite.getU0(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), maxX, maxY, minZ).setColor(red, green, blue, alpha).setUv(sprite.getU1(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), minX, maxY, minZ).setColor(red, green, blue, alpha).setUv(sprite.getU1(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());

        vertexConsumer.addVertex(pose.pose(), minX, minY, maxZ).setColor(red, green, blue, alpha).setUv(sprite.getU0(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), minX, minY, minZ).setColor(red, green, blue, alpha).setUv(sprite.getU0(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), maxX, minY, minZ).setColor(red, green, blue, alpha).setUv(sprite.getU1(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
        vertexConsumer.addVertex(pose.pose(), maxX, minY, maxZ).setColor(red, green, blue, alpha).setUv(sprite.getU1(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, normal.getX(), normal.getY(), normal.getZ());
    }

    public static void renderArrow(PoseStack pPoseStack, VertexConsumer pBuffer, Vec3 pStartPos, Vec3 pVector, float red, float green, float blue, float alpha) {
        renderVector(pPoseStack, pBuffer, pStartPos, pVector, red, green, blue, alpha);

        Vec3 endPos = pStartPos.add(pVector);
        double headLength = pVector.length() * 0.2; // 20% of the vector length for arrowhead
        Vec3 shaftDir = pVector.normalize();
        Vec3 headBase = endPos.subtract(shaftDir.scale(headLength));

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 side = shaftDir.cross(up);
        if (side.lengthSqr() == 0) {
            side = new Vec3(1, 0, 0);
        }
        side = side.normalize().scale(headLength * 0.5);
        Vec3 upDir = shaftDir.cross(side).normalize().scale(headLength * 0.5);

        Vec3 leftHead = headBase.add(side);
        Vec3 rightHead = headBase.subtract(side);
        Vec3 upHead = headBase.add(upDir);
        Vec3 downHead = headBase.subtract(upDir);

        renderVector(pPoseStack, pBuffer, endPos, leftHead.subtract(endPos), red, green, blue, alpha);
        renderVector(pPoseStack, pBuffer, endPos, rightHead.subtract(endPos), red, green, blue, alpha);
        renderVector(pPoseStack, pBuffer, endPos, upHead.subtract(endPos), red, green, blue, alpha);
        renderVector(pPoseStack, pBuffer, endPos, downHead.subtract(endPos), red, green, blue, alpha);
    }

    public static void renderVector(PoseStack pPoseStack, VertexConsumer pBuffer, Vec3 pStartPos, Vec3 pVector, float red, float green, float blue, float alpha) {
        PoseStack.Pose pose = pPoseStack.last();
        float minX = (float) pStartPos.x;
        float minY = (float) pStartPos.y;
        float minZ = (float) pStartPos.z;
        float maxX = (float)(pStartPos.x() + pVector.x);
        float maxY = (float)(pStartPos.y() + pVector.y);
        float maxZ = (float)(pStartPos.z() + pVector.z);
        pBuffer.addVertex(pose, minX, minY, minZ).setColor(red, green, blue, alpha).setNormal(pose, (float) pVector.x, (float) pVector.y, (float) pVector.z);
        pBuffer.addVertex(pose, maxX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(pose, (float) pVector.x, (float) pVector.y, (float) pVector.z);
    }

}
