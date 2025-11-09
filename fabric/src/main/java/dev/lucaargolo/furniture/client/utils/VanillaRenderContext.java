package dev.lucaargolo.furniture.client.utils;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.LinkedBlockingDeque;

public class VanillaRenderContext implements RenderContext {

    private static final VanillaRenderContext INSTANCE = new VanillaRenderContext();

    private final LinkedBlockingDeque<QuadTransform> stack = new LinkedBlockingDeque<>();
    private final QuadEmitter emitter = new QuadEmitter() {
        final Vector3f[] position = new Vector3f[4];
        final Vector3f[] normal = new Vector3f[4];
        final Vector2f[] uv = new Vector2f[4];
        Direction nominalFace = null;

        @Override
        public QuadEmitter pos(int vertexIndex, float x, float y, float z) {
            this.position[vertexIndex] = new Vector3f(x, y, z);
            return this;
        }

        @Override
        public QuadEmitter normal(int vertexIndex, float x, float y, float z) {
            this.normal[vertexIndex] = new Vector3f(x, y, z);
            return this;
        }

        @Override
        public QuadEmitter uv(int vertexIndex, float u, float v) {
            this.uv[vertexIndex] = new Vector2f(u, v);
            return this;
        }

        @Override
        public QuadEmitter lightmap(int vertexIndex, int lightmap) {
            return this;
        }

        @Override
        public QuadEmitter color(int vertexIndex, int color) {
            return this;
        }

        @Override
        public QuadEmitter spriteBake(TextureAtlasSprite sprite, int bakeFlags) {
            final float uMin = sprite.getU0();
            final float uSpan = sprite.getU1() - uMin;
            final float vMin = sprite.getV0();
            final float vSpan = sprite.getV1() - vMin;

            for (int i = 0; i < 4; i++) {
                this.uv(i, uMin + this.u(i) * uSpan, vMin + this.v(i) * vSpan);
            }
            return null;
        }


        @Override
        public QuadEmitter cullFace(@Nullable Direction face) {
            return this;
        }

        @Override
        public QuadEmitter nominalFace(@Nullable Direction face) {
            this.nominalFace = face;
            return this;
        }

        @Override
        public QuadEmitter material(RenderMaterial material) {
            return this;
        }

        @Override
        public QuadEmitter colorIndex(int colorIndex) {
            return this;
        }

        @Override
        public QuadEmitter tag(int tag) {
            return this;
        }

        @Override
        public QuadEmitter copyFrom(QuadView quad) {
            for(int i = 0; i < 4; i++) {
                quad.copyPos(i, this.position[i]);
                if(quad.hasNormal(i)) {
                    quad.copyNormal(i, this.normal[i]);
                } else {
                    this.normal[i] = new Vector3f();
                }
                quad.copyUv(i, this.uv[i]);
            }
            return this;
        }

        @Override
        public QuadEmitter fromVanilla(int[] quadData, int startIndex) {
            try (MemoryStack memoryStack = MemoryStack.stackPush()) {
                ByteBuffer byteBuffer = memoryStack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
                IntBuffer intBuffer = byteBuffer.asIntBuffer();

                for (int l = 0; l < 4; l++) {
                    intBuffer.clear();
                    intBuffer.put(quadData, l * 8, 8);
                    float x = byteBuffer.getFloat(0);
                    float y = byteBuffer.getFloat(4);
                    float z = byteBuffer.getFloat(8);

                    float u = byteBuffer.getFloat(16);
                    float v = byteBuffer.getFloat(20);

                    this.position[l] = new Vector3f(x, y, z);
                    this.normal[l] = new Vector3f(nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                    this.uv[l] = new Vector2f(u, v);
                }
            }
            return this;
        }

        @Override
        public QuadEmitter fromVanilla(BakedQuad quad, RenderMaterial material, @Nullable Direction cullFace) {
            int[] quadData = quad.getVertices();
            this.nominalFace = quad.getDirection();
            return fromVanilla(quadData, 0);
        }

        @Override
        public QuadEmitter emit() {
            PoseStack.Pose pose = poseStack.last();
            stack.forEach(transform -> transform.transform(this));
            for (int i = 0; i < 4; i++) {
                consumer.addVertex(pose, position[i]).setColor(packedColor).setUv(uv[i].x, uv[i].y).setLight(packedLight).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, normal[i].x, normal[i].y, normal[i].z);
            }
            return this;
        }

        @Override
        public float x(int vertexIndex) {
            return this.position[vertexIndex].x;
        }

        @Override
        public float y(int vertexIndex) {
            return this.position[vertexIndex].y;
        }

        @Override
        public float z(int vertexIndex) {
            return this.position[vertexIndex].z;
        }

        @Override
        public float posByIndex(int vertexIndex, int coordinateIndex) {
            return this.position[vertexIndex].get(coordinateIndex);
        }

        @Override
        public Vector3f copyPos(int vertexIndex, @Nullable Vector3f target) {
            if (target == null) {
                target = new Vector3f();
            }

            target.set(this.position[vertexIndex]);
            return target;
        }

        @Override
        public int color(int vertexIndex) {
            return packedColor;
        }

        @Override
        public float u(int vertexIndex) {
            return this.uv[vertexIndex].x;
        }

        @Override
        public float v(int vertexIndex) {
            return this.uv[vertexIndex].y;
        }

        @Override
        public Vector2f copyUv(int vertexIndex, @Nullable Vector2f target) {
            if (target == null) {
                target = new Vector2f();
            }

            target.set(this.uv[vertexIndex]);
            return target;
        }

        @Override
        public int lightmap(int vertexIndex) {
            return packedLight;
        }

        @Override
        public boolean hasNormal(int vertexIndex) {
            return true;
        }

        @Override
        public float normalX(int vertexIndex) {
            return this.normal[vertexIndex].x;
        }

        @Override
        public float normalY(int vertexIndex) {
            return this.normal[vertexIndex].y;
        }

        @Override
        public float normalZ(int vertexIndex) {
            return this.normal[vertexIndex].z;
        }

        @Override
        public Vector3f copyNormal(int vertexIndex, @Nullable Vector3f target) {
            if (target == null) {
                target = new Vector3f();
            }

            target.set(this.normal[vertexIndex]);
            return target;
        }

        @Override
        public @Nullable Direction cullFace() {
            return null;
        }

        @Override
        public @NotNull Direction lightFace() {
            return nominalFace;
        }

        @Override
        public @Nullable Direction nominalFace() {
            return nominalFace;
        }

        @Override
        public Vector3f faceNormal() {
            return new Vector3f(this.nominalFace.getStepX(), this.nominalFace.getStepY(), this.nominalFace.getStepZ());
        }

        @Override
        public RenderMaterial material() {
            throw new IllegalStateException("Vanilla context has no render material");
        }

        @Override
        public int colorIndex() {
            return 0;
        }

        @Override
        public int tag() {
            return 0;
        }

        @Override
        public void toVanilla(int[] target, int targetIndex) {

        }
    };

    private PoseStack poseStack = null;
    private VertexConsumer consumer = null;
    private int packedLight = LightTexture.FULL_BRIGHT;
    private int packedColor = 0x0;

    @Override
    public QuadEmitter getEmitter() {
        return emitter;
    }

    @Override
    public void pushTransform(QuadTransform transform) {
        stack.push(transform);
    }

    @Override
    public void popTransform() {
        stack.pop();
    }

    @Override
    @SuppressWarnings("removal")
    public BakedModelConsumer bakedModelConsumer() {
        throw new IllegalStateException("Baked model consumer is not supported");
    }

    public static VanillaRenderContext of(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedColor) {
        INSTANCE.poseStack = poseStack;
        INSTANCE.consumer = consumer;
        INSTANCE.packedLight = packedLight;
        INSTANCE.packedColor = packedColor;
        return INSTANCE;
    }

}
