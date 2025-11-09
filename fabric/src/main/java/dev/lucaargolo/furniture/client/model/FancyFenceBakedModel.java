package dev.lucaargolo.furniture.client.model;

import com.mojang.math.Axis;
import dev.lucaargolo.furniture.block.FancyFenceBlock;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.utils.FurnitureData;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.function.Supplier;

public class FancyFenceBakedModel extends FurnitureBakedModel {

    public FancyFenceBakedModel(BakedModel wrapped) {
        super(wrapped);
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context, FurnitureData data) {
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context, data);
        if(!RendererAccess.INSTANCE.hasRenderer()) {
            return;
        }
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        assert renderer != null;
        if(state.getBlock() instanceof FancyFenceBlock furniture) {
            List<Vec3i> offsets = furniture.getType().getOffsets();
            for (int index = 0; index < offsets.size(); index++) {
                Vec3i neighborOffset = offsets.get(index);
                if (furniture.isOffsetConnected(state, neighborOffset)) {
                    BlockPos neighborPos = pos.offset(neighborOffset);
                    BlockState neighborState = blockView.getBlockState(neighborPos);
                    if(neighborState.getBlock() instanceof FancyFenceBlock) {
                        FurnitureData connectedData = FurnitureData.get(blockView, neighborPos, neighborState.getValue(FurnitureBlock.LAYER));
                        if (connectedData.hasOriginal()) {
                            Vector3f origin = new Vector3f(pos.getX() + 0.5f + data.getX(), pos.getY() + 0.5f, pos.getZ() + 0.5f + data.getZ());
                            Vector3f destination = new Vector3f(neighborPos.getX() + 0.5f + connectedData.getX(), neighborPos.getY() + 0.5f, neighborPos.getZ() + 0.5f + connectedData.getZ());

                            Vector3f direction = new Vector3f(destination).sub(origin);
                            direction.normalize();

                            float angle = (float) Math.atan2(direction.x, -direction.z);
                            float distance = origin.distance(destination);
                            float size = ((FancyFenceBlock) state.getBlock()).getSize() / 16f;

                            float offset = ((((pos.getX() & 1) << 2) | ((pos.getY() & 1) << 1) | (pos.getZ() & 1)) - 3.5f) * 0.001f;
                            Matrix4f transform = new Matrix4f()
                                    .translate(data.getX(), 0f, data.getZ())
                                    .translate(0.5f, 0.5f, 0.5f)
                                    .rotate(Axis.YP.rotation(-angle))
                                    .scale(1f - (index * offset), 1f - (index * offset), 1f - (index * offset))
                                    .translate(-0.5f, -0.5f, -0.5f);

                            context.pushTransform((quad) -> {
                                for (int i = 0; i < 4; i++) {
                                    Vector4f vector = new Vector4f(quad.x(i), quad.y(i), quad.z(i), 1.0f);
                                    vector.mul(transform);
                                    quad.pos(i, vector.x, vector.y, vector.z);
                                }
                                return true;
                            });

                            QuadEmitter emitter = context.getEmitter();

                            int color = Minecraft.getInstance().getBlockColors().getColor(state, blockView, pos, 0);
                            int packedColor = FastColor.ARGB32.color(255, color);

                            emitSide(emitter, this.getParticleIcon(), Direction.NORTH, (0.5f - size / 2f), 0, (0.5f + size / 2f), 1, 1f - distance - 0.5f, packedColor);
                            emitSide(emitter, this.getParticleIcon(), Direction.SOUTH, (0.5f - size / 2f), 0, (0.5f + size / 2f), 1, 0.5f, packedColor);
                            emitSide(emitter, this.getParticleIcon(), Direction.EAST, 0.5f, 0, 0.5f + distance, 1, (0.5f - size / 2f), packedColor);
                            emitSide(emitter, this.getParticleIcon(), Direction.WEST, 1f - distance - 0.5f, 0, 1f - 0.5f, 1, (0.5f - size / 2f), packedColor);
                            emitSide(emitter, this.getParticleIcon(), Direction.UP, (0.5f - size / 2f), 0.5f, (0.5f + size / 2f), distance + 0.5f, 0, packedColor);
                            emitSide(emitter, this.getParticleIcon(), Direction.DOWN, (0.5f - size / 2f), 1f - distance - 0.5f, (0.5f + size / 2f), 1f - 0.5f, 0, packedColor);

                            context.popTransform();
                        }
                    }
                }
            }
        }
    }

    private static void emitSide(QuadEmitter emitter, TextureAtlasSprite sprite, Direction dir, float x1, float y1, float x2, float y2, float depthOffset, int color) {
        int xCount = (int) Math.ceil(x2 - x1);
        int yCount = (int) Math.ceil(y2 - y1);

        float totalWidth = x2 - x1;
        float totalHeight = y2 - y1;

        float dx = totalWidth / xCount;
        float dy = totalHeight / yCount;

        for (int xi = 0; xi < xCount; xi++) {
            for (int yi = 0; yi < yCount; yi++) {
                float sx1 = x1 + xi * dx;
                float sy1 = y1 + yi * dy;
                float sx2 = sx1 + dx;
                float sy2 = sy1 + dy;

                emitter.square(dir, sx1, sy1, sx2, sy2, depthOffset);

                emitter.uv(0, 0, 0);
                emitter.uv(1, 0, dy);
                emitter.uv(2, dx, dy);
                emitter.uv(3, dx, 0);

                emitter.color(color, color, color, color);
                emitter.spriteBake(sprite, MutableQuadView.BAKE_NORMALIZED);
                emitter.emit();
            }
        }
    }
}
