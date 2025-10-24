package dev.lucaargolo.furniture.data;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lucaargolo.furniture.client.render.RenderHelper;
import dev.lucaargolo.furniture.utils.FurnitureUtils;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.FastColor;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LocalFurnitureData {

    private static final HashMap<ResourceKey<Level>, Long2ObjectMap<Int2IntMap>> dimensionToLevelMap = new HashMap<>();
    private static final Map<ResourceKey<Level>, Set<ChunkPos>> trackingMap = new HashMap<>();

    public static synchronized FurnitureData get(ResourceKey<Level> dimension, long regionPos, int regionLocalBlockPos) {
        Long2ObjectMap<Int2IntMap> levelMap = dimensionToLevelMap.get(dimension);
        if(levelMap != null) {
            Int2IntMap regionMap = levelMap.get(regionPos);
            if(regionMap != null) {
                int packed = regionMap.get(regionLocalBlockPos);
                if(packed != FurnitureData.DEFAULT.getPacked()) {
                    return new FurnitureData(packed);
                }
            }
        }
        return FurnitureData.DEFAULT;
    }

    public static synchronized void set(ResourceKey<Level> dimension, long regionPos, int regionLocalBlockPos, int packedFurnitureData) {
        boolean isDefault = packedFurnitureData == FurnitureData.DEFAULT.getPacked();
        Long2ObjectMap<Int2IntMap> levelMap = dimensionToLevelMap.get(dimension);
        if(levelMap != null) {
            Int2IntMap regionMap = levelMap.get(regionPos);
            if(regionMap != null) {
                if(!isDefault) {
                    regionMap.put(regionLocalBlockPos, packedFurnitureData);
                }else{
                    regionMap.remove(regionLocalBlockPos);
                }
                if(regionMap.isEmpty()) {
                    levelMap.remove(regionPos);
                }
            }else if(!isDefault) {
                Int2IntMap newRegionMap = new Int2IntOpenHashMap();
                newRegionMap.defaultReturnValue(FurnitureData.DEFAULT.getPacked());
                newRegionMap.put(regionLocalBlockPos, packedFurnitureData);
                levelMap.put(regionPos, newRegionMap);
            }
            if(levelMap.isEmpty()) {
                dimensionToLevelMap.remove(dimension);
            }
        }else if(!isDefault) {
            Long2ObjectMap<Int2IntMap> newLevelMap = new Long2ObjectOpenHashMap<>();
            Int2IntMap newRegionMap = new Int2IntOpenHashMap();
            newRegionMap.defaultReturnValue(FurnitureData.DEFAULT.getPacked());
            newRegionMap.put(regionLocalBlockPos, packedFurnitureData);
            newLevelMap.put(regionPos, newRegionMap);
            dimensionToLevelMap.put(dimension, newLevelMap);
        }
    }

    public static synchronized void put(ResourceKey<Level> dimension, long regionPos, Int2IntMap regionMap) {
        Long2ObjectMap<Int2IntMap> levelMap = dimensionToLevelMap.computeIfAbsent(dimension, k -> new Long2ObjectOpenHashMap<>());
        levelMap.put(regionPos, regionMap);
    }

    private static synchronized void remove(ResourceKey<Level> dimension, long regionPos) {
        Long2ObjectMap<Int2IntMap> levelMap = dimensionToLevelMap.get(dimension);
        if (levelMap != null) {
            levelMap.remove(regionPos);
            if(levelMap.isEmpty()) {
                dimensionToLevelMap.remove(dimension);
            }
        }
    }

    public static void watchChunk(ResourceKey<Level> dimension, ChunkPos pos) {
        trackingMap.computeIfAbsent(dimension, k -> new HashSet<>()).add(pos);
    }

    public static void unwatchChunk(ResourceKey<Level> dimension, ChunkPos pos) {
        Set<ChunkPos> trackedChunks = trackingMap.computeIfAbsent(dimension, k -> new HashSet<>());
        Set<Long> previousTrackedRegions = trackedChunks.stream().map(FurnitureUtils::chunkPosToRegionPos).collect(Collectors.toSet());
        trackedChunks.remove(pos);
        Set<Long> currentTrackedRegions = trackedChunks.stream().map(FurnitureUtils::chunkPosToRegionPos).collect(Collectors.toSet());
        for(long regionPos : previousTrackedRegions) {
            if(!currentTrackedRegions.contains(regionPos)) {
                remove(dimension, regionPos);
            }
        }
    }

    public static void unwatchWorld() {
        dimensionToLevelMap.clear();
        trackingMap.clear();
    }

    public static void renderFurnitureDataDebug(Level level, Camera camera, PoseStack poseStack, MultiBufferSource bufferSource) {
        if(!Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
            return;
        }

        Long2ObjectMap<Int2IntMap> levelMap = dimensionToLevelMap.get(level.dimension());

        if(levelMap == null) {
            return;
        }

        levelMap.forEach((regionPos, regionMap) -> {
            regionMap.forEach((regionLocalBlockPos, packedFurnitureData) -> {
                BlockPos blockPos = FurnitureUtils.regionLocalBlockPosToBlockPos(regionPos, regionLocalBlockPos);
                FurnitureData data = new FurnitureData(packedFurnitureData);
                renderFurnitureBlockDebug(blockPos, data, camera, poseStack, bufferSource);
            });
        });
    }

    private static void renderFurnitureBlockDebug(BlockPos blockPos, FurnitureData data, Camera camera, PoseStack poseStack, MultiBufferSource bufferSource) {
        Vec3 pos = Vec3.atLowerCornerOf(blockPos);

        poseStack.pushPose();
        poseStack.translate(pos.x-camera.getPosition().x, pos.y-camera.getPosition().y, pos.z-camera.getPosition().z);

        Direction toOriginal = data.getDirectionToOriginal();

        int color = toOriginal == null ? 0x00FF00 : 0xFFFF00;
        float red = FastColor.ARGB32.red(color)/255f;
        float green = FastColor.ARGB32.green(color)/255f;
        float blue = FastColor.ARGB32.blue(color)/255f;

        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(poseStack, lineConsumer, 0.001f, 0.001f, 0.001f, 0.999f, 0.999f, 0.999f, red, green, blue, 1f);
        if(toOriginal != null) {
            Vec3 vector = new Vec3(toOriginal.getStepX(), toOriginal.getStepY(), toOriginal.getStepZ()).multiply(0.5, 0.5, 0.5);
            RenderHelper.renderArrow(poseStack, lineConsumer, new Vec3(0.5, 0.5, 0.5), vector, 0f, 0f, 1f, 1f);
        }

        VertexConsumer atlasConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));
        RenderHelper.renderFilledBox(poseStack, atlasConsumer, 0.001f, 0.001f, 0.001f, 0.999f, 0.999f, 0.999f, red, green, blue, 0.3f);

        poseStack.popPose();
    }

}
