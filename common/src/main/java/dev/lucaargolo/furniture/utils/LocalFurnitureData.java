package dev.lucaargolo.furniture.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lucaargolo.furniture.block.FurnitureSeatBlock;
import dev.lucaargolo.furniture.client.render.RenderHelper;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LocalFurnitureData {

    private static final Map<ResourceKey<Level>, Set<ChunkPos>> trackingMap = new HashMap<>();

    private static final HashMap<ResourceKey<Level>, Long2ObjectMap<Int2LongMap>> dimensionToLevelMap = new HashMap<>();
    private static final HashMap<ResourceKey<Level>, Long2ObjectMap<Int2ObjectMap<VoxelShape>>> cachedShapes = new HashMap<>();

    public static synchronized VoxelShape cachedShape(ResourceKey<Level> dimension, long regionPos, int regionLocalBlockPos, Supplier<VoxelShape> shapeSupplier) {
        Long2ObjectMap<Int2ObjectMap<VoxelShape>> shapeLevelMap = cachedShapes.computeIfAbsent(dimension, k -> new Long2ObjectOpenHashMap<>());
        Int2ObjectMap<VoxelShape> shapeRegionMap = shapeLevelMap.computeIfAbsent(regionPos, k -> new Int2ObjectOpenHashMap<>());
        return shapeRegionMap.computeIfAbsent(regionLocalBlockPos, k -> shapeSupplier.get());
    }

    public static synchronized FurnitureData[] get(ResourceKey<Level> dimension, long regionPos, int regionLocalBlockPos) {
        Long2ObjectMap<Int2LongMap> levelMap = dimensionToLevelMap.get(dimension);
        if(levelMap != null) {
            Int2LongMap regionMap = levelMap.get(regionPos);
            if(regionMap != null) {
                long packed = regionMap.get(regionLocalBlockPos);
                if(packed != FurnitureData.DEFAULT_PACKED_LAYERS) {
                    return FurnitureUtils.unpackFurnitureDataLayers(packed);
                }
            }
        }
        return FurnitureData.DEFAULT_LAYERS.clone();
    }

    public static synchronized void set(ResourceKey<Level> dimension, long regionPos, int regionLocalBlockPos, FurnitureData[] layers) {
        Long2ObjectMap<Int2ObjectMap<VoxelShape>> shapeLevelMap = cachedShapes.get(dimension);
        if(shapeLevelMap != null) {
            Int2ObjectMap<VoxelShape> shapeRegionMap = shapeLevelMap.get(regionPos);
            if(shapeRegionMap != null) {
                shapeRegionMap.remove(regionLocalBlockPos);
            }
        }

        long newPacked = FurnitureUtils.packFurnitureDataLayers(layers);
        boolean isDefault = newPacked == FurnitureData.DEFAULT_PACKED_LAYERS;

        Long2ObjectMap<Int2LongMap> levelMap = dimensionToLevelMap.get(dimension);
        if(levelMap != null) {
            Int2LongMap regionMap = levelMap.get(regionPos);
            if(regionMap != null) {
                long packed = regionMap.get(regionLocalBlockPos);
                if(packed != FurnitureData.DEFAULT_PACKED_LAYERS || !isDefault) {
                    packed = newPacked;
                    if(packed != FurnitureData.DEFAULT_PACKED_LAYERS) {
                        regionMap.put(regionLocalBlockPos, packed);
                    }else{
                        regionMap.remove(regionLocalBlockPos);
                    }
                    if(regionMap.isEmpty()) {
                        levelMap.remove(regionPos);
                    }
                }
            }else if(!isDefault) {
                Int2LongMap newRegionMap = new Int2LongOpenHashMap();
                newRegionMap.defaultReturnValue(FurnitureData.DEFAULT_PACKED_LAYERS);
                newRegionMap.put(regionLocalBlockPos, newPacked);
                levelMap.put(regionPos, newRegionMap);
            }
            if(levelMap.isEmpty()) {
                dimensionToLevelMap.remove(dimension);
            }
        }else if(!isDefault) {
            Long2ObjectMap<Int2LongMap> newLevelMap = new Long2ObjectOpenHashMap<>();
            Int2LongMap newRegionMap = new Int2LongOpenHashMap();
            newRegionMap.defaultReturnValue(FurnitureData.DEFAULT_PACKED_LAYERS);
            newRegionMap.put(regionLocalBlockPos, newPacked);
            newLevelMap.put(regionPos, newRegionMap);
            dimensionToLevelMap.put(dimension, newLevelMap);
        }
    }

    public static synchronized void put(ResourceKey<Level> dimension, long regionPos, Int2LongMap regionMap) {
        Long2ObjectMap<Int2LongMap> levelMap = dimensionToLevelMap.computeIfAbsent(dimension, k -> new Long2ObjectOpenHashMap<>());
        levelMap.put(regionPos, regionMap);
    }

    private static synchronized void remove(ResourceKey<Level> dimension, long regionPos) {
        Long2ObjectMap<Int2LongMap> levelMap = dimensionToLevelMap.get(dimension);
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

        Long2ObjectMap<Int2LongMap> levelMap = dimensionToLevelMap.get(level.dimension());

        if(levelMap == null) {
            return;
        }

        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderType.lines());
        levelMap.forEach((regionPos, regionMap) -> {
            regionMap.forEach((regionLocalBlockPos, packedFurnitureData) -> {
                BlockPos blockPos = FurnitureUtils.regionLocalBlockPosToBlockPos(regionPos, regionLocalBlockPos);
                FurnitureData[] layers = FurnitureUtils.unpackFurnitureDataLayers(packedFurnitureData);
                for(int layer = 0; layer < layers.length; layer++) {
                    renderFurnitureBlockDebug(level, blockPos, layers[layer], camera, poseStack, lineConsumer, layer == 0 ? 0xFFFF00 : layer == 1 ? 0xFF00FF : layer == 2 ? 0x00FFFF : 0x00FF00);
                }

                IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
                if(server != null && camera.getEntity().isShiftKeyDown()) {
                    renderServerFurnitureShapeDebug(server, level, blockPos, camera, poseStack, lineConsumer, regionPos, regionLocalBlockPos);
                }else{
                    renderClientFurnitureShapeDebug(level, regionPos, regionLocalBlockPos, blockPos, camera, poseStack, lineConsumer);
                }
            });
        });
    }

    private static void renderServerFurnitureShapeDebug(IntegratedServer server, Level level, BlockPos blockPos, Camera camera, PoseStack poseStack, VertexConsumer lineConsumer, Long regionPos, Integer regionLocalBlockPos) {
        Vec3 pos = Vec3.atLowerCornerOf(blockPos);
        poseStack.pushPose();
        poseStack.translate(pos.x- camera.getPosition().x, pos.y- camera.getPosition().y, pos.z- camera.getPosition().z);
        ServerLevel serverLevel = server.getLevel(level.dimension());
        if(serverLevel != null) {
            VoxelShape shape = RegionFurnitureData.get(serverLevel, FurnitureUtils.regionKey(regionPos)).getCachedShape(regionLocalBlockPos);
            if(shape != null) {
                shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                    LevelRenderer.renderLineBox(poseStack, lineConsumer, minX, minY, minZ, maxX, maxY, maxZ, 0f, 0f, 1f, 1f);
                });
            }
        }
        poseStack.popPose();
    }

    private static void renderClientFurnitureShapeDebug(Level level, long regionPos, int regionLocalBlockPos, BlockPos blockPos, Camera camera, PoseStack poseStack, VertexConsumer lineConsumer) {
        Vec3 pos = Vec3.atLowerCornerOf(blockPos);
        poseStack.pushPose();
        poseStack.translate(pos.x- camera.getPosition().x, pos.y- camera.getPosition().y, pos.z- camera.getPosition().z);
        Long2ObjectMap<Int2ObjectMap<VoxelShape>> shapeLevelMap = cachedShapes.get(level.dimension());
        if(shapeLevelMap != null) {
            Int2ObjectMap<VoxelShape> shapeRegionMap = shapeLevelMap.get(regionPos);
            if(shapeRegionMap != null) {
                VoxelShape shape = shapeRegionMap.get(regionLocalBlockPos);
                if(shape != null) {
                    shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                        LevelRenderer.renderLineBox(poseStack, lineConsumer, minX, minY, minZ, maxX, maxY, maxZ, 1f, 1f, 1f, 1f);
                    });
                }
            }
        }
        poseStack.popPose();
    }

    private static void renderFurnitureBlockDebug(Level level, BlockPos blockPos, FurnitureData data, Camera camera, PoseStack poseStack, VertexConsumer lineConsumer, int color) {
        Vec3 pos = Vec3.atLowerCornerOf(blockPos);

        poseStack.pushPose();
        poseStack.translate(pos.x-camera.getPosition().x, pos.y-camera.getPosition().y, pos.z-camera.getPosition().z);

        Direction toOriginal = data.getDirectionToOriginal();

        float red = FastColor.ARGB32.red(color)/255f;
        float green = FastColor.ARGB32.green(color)/255f;
        float blue = FastColor.ARGB32.blue(color)/255f;

        if(data.hasOriginal()) {
            BlockState state = level.getBlockState(blockPos);
            if(state.getBlock() instanceof FurnitureSeatBlock block) {
                Vec3[] seats = block.getSeats();
                for(int i = 0; i < seats.length; i++) {
                    Vec3 position = block.getPositionForSeat(data, blockPos, i).subtract(pos);
                    AABB bounds = AABB.ofSize(position, 0.1, 0.1, 0.1);
                    LevelRenderer.renderLineBox(poseStack, lineConsumer, bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY, bounds.maxZ, 1f, 0f, 0f, 1f);
                }
            }
        }

        if(data.hasOriginal() || toOriginal != null) {
            LevelRenderer.renderLineBox(poseStack, lineConsumer, 0.001f, 0.001f, 0.001f, 0.999f, 0.999f, 0.999f, red, green, blue, 1f);
            if(toOriginal != null) {
                Vec3 vector = new Vec3(toOriginal.getStepX(), toOriginal.getStepY(), toOriginal.getStepZ()).multiply(0.5, 0.5, 0.5);
                RenderHelper.renderArrow(poseStack, lineConsumer, new Vec3(0.5, 0.5, 0.5), vector, red, green, blue, 1f);
            }
            if(data.hasOriginal()) {
                RenderHelper.renderCrossedCube(poseStack, lineConsumer, 0.001f, 0.001f, 0.001f, 0.999f, 0.999f, 0.999f, red, green, blue, 1f);
            }
        }

        poseStack.popPose();
    }

}
