package dev.lucaargolo.furniture;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class FabricFurnitureDataManager extends FurnitureDataManager {

    private final AttachmentType<ChunkFurnitureData> CHUNK_FURNITURE_DATA = AttachmentRegistry.create(
        FurnitureMod.id("chunk_furniture_data"), builder -> builder
            .initializer(ChunkFurnitureData::new)
            .persistent(ChunkFurnitureData.CODEC)
            .syncWith(ChunkFurnitureData.STREAM_CODEC, AttachmentSyncPredicate.all())
    );

    @Override
    public FurnitureData[] get(LevelReader level, BlockPos pos) {
        ChunkAccess chunk = level.getChunk(pos);
        return chunk.getAttachedOrCreate(CHUNK_FURNITURE_DATA).get(pos);
    }

    @Override
    public void set(LevelReader level, BlockPos pos, FurnitureData[] layers) {
        ChunkAccess chunk = level.getChunk(pos);
        ChunkFurnitureData data = chunk.getAttachedOrCreate(CHUNK_FURNITURE_DATA);
        data.set(pos, layers);
        chunk.setAttached(CHUNK_FURNITURE_DATA, data);
    }

    @Override
    public VoxelShape cachedShape(LevelReader level, BlockPos pos, Supplier<VoxelShape> shapeSupplier) {
        ChunkAccess chunk = level.getChunk(pos);
        return chunk.getAttachedOrCreate(CHUNK_FURNITURE_DATA).cachedShape(pos, shapeSupplier);
    }

    @Override
    protected ChunkFurnitureData getChunkData(LevelReader level, ChunkPos chunkPos) {
        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);
        return chunk.getAttachedOrCreate(CHUNK_FURNITURE_DATA);
    }

}
