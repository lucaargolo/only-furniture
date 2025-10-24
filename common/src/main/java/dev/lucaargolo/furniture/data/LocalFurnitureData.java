package dev.lucaargolo.furniture.data;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lucaargolo.furniture.client.render.RenderHelper;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;

public class LocalFurnitureData {

    private static final HashMap<ResourceKey<Level>, Long2ObjectMap<Long2IntMap>> map = new HashMap<>();

    public static synchronized FurnitureData get(ResourceKey<Level> dimension, long chunkPos, long blockPos) {
        Long2ObjectMap<Long2IntMap> regionMap = map.get(dimension);
        if(regionMap != null) {
            Long2IntMap chunkMap = regionMap.get(chunkPos);
            if(chunkMap != null) {
                int packed = chunkMap.get(blockPos);
                if(packed != FurnitureData.DEFAULT.getPacked()) {
                    return new FurnitureData(packed);
                }
            }
        }
        return FurnitureData.DEFAULT;
    }

    public static synchronized void set(ResourceKey<Level> dimension, long chunkPos, long blockPos, int data) {
        boolean isDefault = data == FurnitureData.DEFAULT.getPacked();
        Long2ObjectMap<Long2IntMap> regionMap = map.get(dimension);
        if(regionMap != null) {
            Long2IntMap chunkMap = regionMap.get(chunkPos);
            if(chunkMap != null) {
                if(!isDefault) {
                    chunkMap.put(blockPos, data);
                }else{
                    chunkMap.remove(blockPos);
                }
                if(chunkMap.isEmpty()) {
                    regionMap.remove(chunkPos);
                }
            }else if(!isDefault) {
                Long2IntMap newChunkMap = new Long2IntOpenHashMap();
                newChunkMap.defaultReturnValue(FurnitureData.DEFAULT.getPacked());
                newChunkMap.put(blockPos, data);
                regionMap.put(chunkPos, newChunkMap);
            }
            if(regionMap.isEmpty()) {
                map.remove(dimension);
            }
        }else if(!isDefault) {
            Long2ObjectMap<Long2IntMap> newRegionMap = new Long2ObjectOpenHashMap<>();
            Long2IntMap newChunkMap = new Long2IntOpenHashMap();
            newChunkMap.defaultReturnValue(FurnitureData.DEFAULT.getPacked());
            newChunkMap.put(blockPos, data);
            newRegionMap.put(chunkPos, newChunkMap);
            map.put(dimension, newRegionMap);
        }
    }

    public static synchronized void put(ResourceKey<Level> dimension, long chunkPos, Long2IntMap chunkData) {
        Long2ObjectMap<Long2IntMap> regionMap = map.computeIfAbsent(dimension, k -> new Long2ObjectOpenHashMap<>());
        regionMap.put(chunkPos, chunkData);
    }

    public static synchronized void remove(ResourceKey<Level> dimension, long chunkPos) {
        Long2ObjectMap<Long2IntMap> regionMap = map.get(dimension);
        if (regionMap != null) {
            regionMap.remove(chunkPos);
            if(regionMap.isEmpty()) {
                map.remove(dimension);
            }
        }
    }

    public static synchronized void clear() {
        map.clear();
    }

    public static void renderFurnitureDebug(Level level, Camera camera, PoseStack poseStack, MultiBufferSource bufferSource) {
        if(!Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
            return;
        }

        Long2ObjectMap<Long2IntMap> regionMap = map.get(level.dimension());

        if(regionMap == null) {
            return;
        }

        regionMap.forEach((packedChunkPos, chunkMap) -> {
            chunkMap.forEach((packedBlockPos, packedData) -> {
                BlockPos blockPos = BlockPos.of(packedBlockPos);
                FurnitureData data = new FurnitureData(packedData);
                drawFurniture(blockPos, data, camera, poseStack, bufferSource);
            });
        });
    }

    private static void drawFurniture(BlockPos blockPos, FurnitureData data, Camera camera, PoseStack poseStack, MultiBufferSource bufferSource) {
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
