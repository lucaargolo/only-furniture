package dev.lucaargolo.furniture.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.block.FurnitureFenceBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FurnitureFenceBakedModel extends FurnitureBakedModel {

    private static final ModelProperty<Map<Pair<Vec3i, Integer>, Pair<BlockState, FurnitureData>>> NEIGHBOR_DATA_PROPERTY = new ModelProperty<>();

    public FurnitureFenceBakedModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData modelData, @Nullable RenderType renderType) {
        List<BakedQuad> quads = new ArrayList<>(super.getQuads(state, side, rand, modelData, renderType));

        BlockPos pos = modelData.get(POS_PROPERTY);
        FurnitureData data = modelData.get(DATA_PROPERTY);
        Map<Pair<Vec3i, Integer>, Pair<BlockState, FurnitureData>> neighborDataMap = modelData.get(NEIGHBOR_DATA_PROPERTY);

        if(state != null && pos != null && data != null && neighborDataMap != null) {
            PoseStack stack = new PoseStack();
            QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
            consumer.setTintIndex(0);
            consumer.setShade(true);

            Vector3f origin = new Vector3f(pos.getX() + 0.5f + data.getX(state), pos.getY() + 0.5f + data.getY(state), pos.getZ() + 0.5f + data.getZ(state));

            neighborDataMap.forEach((neighborOffsetPair, neighborDataPair) -> {
                Vec3i neighborOffset = neighborOffsetPair.getFirst();
                int offsetIndex = neighborOffsetPair.getSecond();
                BlockPos neighborPos = pos.offset(neighborOffset);
                BlockState neighborState = neighborDataPair.getFirst();
                FurnitureData neighborData = neighborDataPair.getSecond();

                Vector3f destination = new Vector3f(neighborPos.getX() + 0.5f + neighborData.getX(neighborState), neighborPos.getY() + 0.5f + neighborData.getY(neighborState), neighborPos.getZ() + 0.5f + neighborData.getZ(neighborState));
                Vector3f direction = new Vector3f(destination).sub(origin);
                direction.normalize();

                float angle = (float) Math.atan2(direction.x, -direction.z);
                float distance = origin.distance(destination);
                float size = ((FurnitureFenceBlock) state.getBlock()).getSize()/16f;

                float offset = ((((pos.getX() & 1) << 2) | ((pos.getY() & 1) << 1) | (pos.getZ() & 1)) - 3.5f) * 0.001f;
                Matrix4f transform = new Matrix4f()
                        .translate(data.getX(state), data.getY(state), data.getZ(state))
                        .translate(0.5f, 0.5f, 0.5f)
                        .rotate(Axis.YN.rotation(angle))
                        .scale(1f - (offsetIndex * offset), 1f - (offsetIndex * offset), 1f - (offsetIndex * offset))
                        .translate(-0.5f, -0.5f, -0.5f);
                Transformation transformation = new Transformation(transform);
                IQuadTransformer transformer = QuadTransformers.applying(transformation);

                Integer color = modelData.get(COLOR);
                int packedColor = FastColor.ARGB32.color(255, color != null ? color : 0xFFFFFF);

                List<BakedQuad> fenceQuads = new ArrayList<>();
                fenceQuads.addAll(emitSide(stack, consumer, this.getParticleIcon(), Direction.NORTH, (0.5f - size / 2f), 0, (0.5f + size / 2f), 1, 1f - distance - 0.5f, packedColor));
                fenceQuads.addAll(emitSide(stack, consumer, this.getParticleIcon(), Direction.SOUTH, (0.5f - size / 2f), 0, (0.5f + size / 2f), 1, 0.5f, packedColor));
                fenceQuads.addAll(emitSide(stack, consumer, this.getParticleIcon(), Direction.EAST, 0.5f, 0, 0.5f + distance, 1, (0.5f - size / 2f), packedColor));
                fenceQuads.addAll(emitSide(stack, consumer, this.getParticleIcon(), Direction.WEST, 1f - distance - 0.5f, 0, 1f - 0.5f, 1, (0.5f - size / 2f), packedColor));
                fenceQuads.addAll(emitSide(stack, consumer, this.getParticleIcon(), Direction.UP, (0.5f - size / 2f), 0.5f, (0.5f + size / 2f), distance + 0.5f, 0, packedColor));
                fenceQuads.addAll(emitSide(stack, consumer, this.getParticleIcon(), Direction.DOWN, (0.5f - size / 2f), 1f - distance - 0.5f, (0.5f + size / 2f), 1f - 0.5f, 0, packedColor));

                quads.addAll(transformer.process(fenceQuads));
            });
        }
        return quads;
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        modelData = super.getModelData(level, pos, state, modelData);
        FurnitureData data = modelData.get(DATA_PROPERTY);
        if(data != null && data.hasOriginal() && state.getBlock() instanceof FurnitureFenceBlock furniture) {
            Map<Pair<Vec3i, Integer>, Pair<BlockState, FurnitureData>> neighborDataMap = new HashMap<>();
            List<Vec3i> offsets = furniture.getType().getOffsets();
            for (int index = 0; index < offsets.size(); index++) {
                Vec3i neighborOffset = offsets.get(index);
                if (furniture.isOffsetConnected(state, neighborOffset)) {
                    BlockPos neighborPos = pos.offset(neighborOffset);
                    BlockState neighborState = level.getBlockState(neighborPos);
                    if(neighborState.getBlock() instanceof FurnitureFenceBlock) {
                        FurnitureData neighborData = FurnitureData.getOriginal(level, neighborPos);
                        if (neighborData.hasOriginal()) {
                            neighborDataMap.put(Pair.of(neighborOffset, index), Pair.of(neighborState, neighborData));
                        }
                    }
                }
            }
            return modelData.derive().with(NEIGHBOR_DATA_PROPERTY, neighborDataMap).build();
        }
        return modelData;
    }


    private static List<BakedQuad> emitSide(PoseStack poseStack, QuadBakingVertexConsumer vertexConsumer, TextureAtlasSprite sprite, Direction dir, float x1, float y1, float x2, float y2, float depthOffset, int packedColor) {
        List<BakedQuad> quads = new ArrayList<>();
        PoseStack.Pose pose = poseStack.last();

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

                float u0 = sprite.getU0();
                float u1 = sprite.getU0() + (sprite.getU1()-sprite.getU0())*dx;
                float v0 = sprite.getV0();
                float v1 = sprite.getV0() + (sprite.getV1()-sprite.getV0())*dy;

                quads.addAll(emitQuad(pose, vertexConsumer, dir, sx1, sy1, sx2, sy2, depthOffset, u0, u1, v0, v1, packedColor));
            }
        }
        return quads;
    }

    private static List<BakedQuad> emitQuad(PoseStack.Pose pose, QuadBakingVertexConsumer vertexConsumer, Direction nominalFace, float left, float bottom, float right, float top, float depth, float u0, float u1, float v0, float v1, int packedColor) {
        List<BakedQuad> quads = new ArrayList<>();
        switch (nominalFace) {
            case UP:
                depth = 1 - depth;
                top = 1 - top;
                bottom = 1 - bottom;

            case DOWN:
                vertexConsumer.setDirection(nominalFace);
                vertexConsumer.addVertex(pose, left, depth, top).setColor(packedColor).setUv(u0, v0).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, left, depth, bottom).setColor(packedColor).setUv(u0, v1).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, right, depth, bottom).setColor(packedColor).setUv(u1, v1).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, right, depth, top).setColor(packedColor).setUv(u1, v0).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                quads.add(vertexConsumer.bakeQuad());
                break;

            case EAST:
                depth = 1 - depth;
                left = 1 - left;
                right = 1 - right;

            case WEST:
                vertexConsumer.setDirection(nominalFace);
                vertexConsumer.addVertex(pose, depth, top, left).setColor(packedColor).setUv(u0, v0).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, depth, bottom, left).setColor(packedColor).setUv(u0, v1).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, depth, bottom, right).setColor(packedColor).setUv(u1, v1).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, depth, top, right).setColor(packedColor).setUv(u1, v0).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                quads.add(vertexConsumer.bakeQuad());
                break;

            case SOUTH:
                depth = 1 - depth;
                left = 1 - left;
                right = 1 - right;

            case NORTH:
                vertexConsumer.setDirection(nominalFace);
                vertexConsumer.addVertex(pose, 1 - left, top, depth).setColor(packedColor).setUv(u0, v0).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, 1 - left, bottom, depth).setColor(packedColor).setUv(u0, v1).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, 1 - right, bottom, depth).setColor(packedColor).setUv(u1, v1).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, 1 - right, top, depth).setColor(packedColor).setUv(u1, v0).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                quads.add(vertexConsumer.bakeQuad());
                break;
        }
        return quads;
    }
}
