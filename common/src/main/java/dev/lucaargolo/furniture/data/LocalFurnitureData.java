package dev.lucaargolo.furniture.data;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

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

}
