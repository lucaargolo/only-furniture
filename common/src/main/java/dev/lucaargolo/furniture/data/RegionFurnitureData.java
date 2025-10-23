package dev.lucaargolo.furniture.data;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegionFurnitureData extends SavedData {

    @SuppressWarnings("DataFlowIssue")
    protected static final Factory<RegionFurnitureData> FACTORY = new Factory<>(RegionFurnitureData::new, RegionFurnitureData::create, null);

    private final Int2ObjectMap<Long2IntMap> regionMap = new Int2ObjectOpenHashMap<>();

    @Nullable
    protected Long2IntMap get(int chunkPos) {
        return regionMap.get(chunkPos);
    }

    protected FurnitureData get(int chunkPos, long blockPos) {
        Long2IntMap chunkMap = regionMap.get(chunkPos);
        if(chunkMap != null) {
            int packed = chunkMap.get(blockPos);
            if(packed != FurnitureData.DEFAULT.getPacked()) {
                return new FurnitureData(packed);
            }
        }
        return FurnitureData.DEFAULT;
    }

    protected void set(int chunkPos, long blockPos, FurnitureData data) {
        boolean isDefault = data.equals(FurnitureData.DEFAULT);
        Long2IntMap chunkMap = regionMap.get(chunkPos);
        if(chunkMap != null) {
            if(!isDefault) {
                chunkMap.put(blockPos, data.getPacked());
            }else{
                chunkMap.remove(blockPos);
            }
            if(chunkMap.isEmpty()) {
                regionMap.remove(chunkPos);
            }
        }else if(!isDefault) {
            Long2IntMap newChunkMap = new Long2IntOpenHashMap();
            newChunkMap.defaultReturnValue(FurnitureData.DEFAULT.getPacked());
            newChunkMap.put(blockPos, data.getPacked());
            regionMap.put(chunkPos, newChunkMap);
        }
        this.setDirty();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        for (Int2ObjectMap.Entry<Long2IntMap> regionEntry : regionMap.int2ObjectEntrySet()) {
            Long2IntMap longMap = regionEntry.getValue();
            CompoundTag regionTag = new CompoundTag();

            int size = longMap.size();
            long[] keys = new long[size];
            int[] values = new int[size];

            int i = 0;
            for (Long2IntMap.Entry e : longMap.long2IntEntrySet()) {
                keys[i] = e.getLongKey();
                values[i] = e.getIntValue();
                i++;
            }

            regionTag.putLongArray("keys", keys);
            regionTag.putIntArray("values", values);
            tag.put(Integer.toString(regionEntry.getIntKey()), regionTag);
        }
        return tag;
    }

    protected static RegionFurnitureData create(CompoundTag tag, HolderLookup.Provider registries) {
        RegionFurnitureData data = new RegionFurnitureData();

        for (String key : tag.getAllKeys()) {
            int regionId = Integer.parseInt(key);
            CompoundTag regionTag = tag.getCompound(key);

            long[] keys = regionTag.getLongArray("keys");
            int[] values = regionTag.getIntArray("values");

            Long2IntMap longMap = new Long2IntOpenHashMap(keys.length);
            for (int i = 0; i < keys.length; i++) {
                longMap.put(keys[i], values[i]);
            }

            data.regionMap.put(regionId, longMap);
        }

        return data;
    }

}
