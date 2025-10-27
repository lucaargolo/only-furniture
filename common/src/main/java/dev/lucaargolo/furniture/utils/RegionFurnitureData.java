package dev.lucaargolo.furniture.utils;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.network.RegionFurnitureDataPayload;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RegionFurnitureData extends SavedData {

    @SuppressWarnings("DataFlowIssue")
    private static final Factory<RegionFurnitureData> factory = new Factory<>(RegionFurnitureData::new, RegionFurnitureData::create, null);
    private static final Map<ResourceKey<Level>, Map<UUID, Set<ChunkPos>>> playerTrackingMap = new HashMap<>();

    private final Int2LongMap regionMap = new Int2LongOpenHashMap();
    private final Int2ObjectMap<VoxelShape> cachedShapes = new Int2ObjectOpenHashMap<>();

    public RegionFurnitureData() {
        regionMap.defaultReturnValue(FurnitureData.DEFAULT_PACKED_LAYERS);
    }

    public VoxelShape cachedShape(int regionLocalBlockPos, Supplier<VoxelShape> shapeSupplier) {
        return cachedShapes.computeIfAbsent(regionLocalBlockPos, key -> shapeSupplier.get());
    }

    public FurnitureData[] get(int regionLocalBlockPos) {
        long packed = regionMap.get(regionLocalBlockPos);
        if(packed != FurnitureData.DEFAULT_PACKED_LAYERS) {
            return FurnitureUtils.unpackFurnitureDataLayers(packed);
        }
        return FurnitureData.DEFAULT_LAYERS.clone();
    }

    public void set(int regionLocalBlockPos, FurnitureData[] layers) {
        cachedShapes.remove(regionLocalBlockPos);

        long newPacked = FurnitureUtils.packFurnitureDataLayers(layers);
        boolean isDefault = newPacked == FurnitureData.DEFAULT_PACKED_LAYERS;

        long packed = regionMap.get(regionLocalBlockPos);
        if(packed != FurnitureData.DEFAULT_PACKED_LAYERS || !isDefault) {
            packed = newPacked;
            if(packed != FurnitureData.DEFAULT_PACKED_LAYERS) {
                regionMap.put(regionLocalBlockPos, packed);
            }else{
                regionMap.remove(regionLocalBlockPos);
            }
            this.setDirty();
        }
    }

    public static void watchChunk(ServerLevel level, ServerPlayer player, ChunkPos pos) {
        Set<ChunkPos> trackedChunks = playerTrackingMap.computeIfAbsent(level.dimension(), k -> new HashMap<>()).computeIfAbsent(player.getUUID(), u -> new HashSet<>());
        Set<Long> trackedRegions = trackedChunks.stream().map(FurnitureUtils::chunkPosToRegionPos).collect(Collectors.toSet());
        long regionPos = FurnitureUtils.chunkPosToRegionPos(pos);
        if(!trackedRegions.contains(regionPos)) {
            syncRegion(level, player, regionPos);
        }
        trackedChunks.add(pos);
    }

    public static void unwatchChunk(ServerLevel level, ServerPlayer player, ChunkPos pos) {
        Map<UUID, Set<ChunkPos>> levelMap = playerTrackingMap.get(level.dimension());
        if(levelMap != null) {
            Set<ChunkPos> trackedChunks = levelMap.get(player.getUUID());
            if(trackedChunks != null) {
                trackedChunks.remove(pos);
                if(trackedChunks.isEmpty()) {
                    levelMap.remove(player.getUUID());
                }
            }
        }
    }

    private static void syncRegion(ServerLevel level, ServerPlayer player, long regionPos) {
        RegionFurnitureData data = get(level, FurnitureUtils.regionKey(regionPos));
        FurnitureMod.INSTANCE.getPacketManager().sendToPlayer(player, new RegionFurnitureDataPayload(level.dimension(), regionPos, data.regionMap));
    }

    public static RegionFurnitureData get(ServerLevel level, String key) {
        return level.getDataStorage().computeIfAbsent(RegionFurnitureData.factory, key);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        int size = regionMap.size();
        int[] keys = new int[size];
        long[] values = new long[size];

        int i = 0;
        for (Int2LongMap.Entry e : regionMap.int2LongEntrySet()) {
            keys[i] = e.getIntKey();
            values[i] = e.getLongValue();
            i++;
        }

        tag.putIntArray("keys", keys);
        tag.putLongArray("values", values);
        return tag;
    }

    private static RegionFurnitureData create(CompoundTag tag, HolderLookup.Provider registries) {
        RegionFurnitureData data = new RegionFurnitureData();

        int[] keys = tag.getIntArray("keys");
        long[] values = tag.getLongArray("values");

        for (int i = 0; i < keys.length; i++) {
            data.regionMap.put(keys[i], values[i]);
        }

        return data;
    }

    public static void sendToPlayersTrackingRegion(ServerLevel level, long regionPos, CustomPacketPayload payload) {
        Map<UUID, Set<ChunkPos>> levelMap = playerTrackingMap.get(level.dimension());
        if(levelMap != null) {
            level.getPlayers(player -> levelMap.get(player.getUUID()).stream().map(FurnitureUtils::chunkPosToRegionPos).anyMatch(p -> p.equals(regionPos))).forEach(player -> {
                FurnitureMod.INSTANCE.getPacketManager().sendToPlayer(player, payload);
            });
        }
    }

}
