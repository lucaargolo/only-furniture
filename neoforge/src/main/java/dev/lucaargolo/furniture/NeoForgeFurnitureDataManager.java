package dev.lucaargolo.furniture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class NeoForgeFurnitureDataManager extends FurnitureDataManager {

    private final DeferredRegister<AttachmentType<?>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, FurnitureMod.MOD_ID);

    private final Supplier<AttachmentType<ChunkData>> CHUNK_FURNITURE_DATA = REGISTRY.register("chunk_furniture_data", () -> AttachmentType
        .builder(ChunkData::new)
        .serialize(ChunkData.CODEC)
        .sync(ChunkData.STREAM_CODEC)
        .build()
    );

    @Override
    public void init() {
        REGISTRY.register(NeoForgeFurnitureMod.getModBus());
    }

    @Override
    public FurnitureData[] get(LevelReader level, BlockPos pos) {
        ChunkAccess chunk = level.getChunk(pos);
        return chunk.getData(CHUNK_FURNITURE_DATA).get(pos);
    }

    @Override
    public void set(LevelReader level, BlockPos pos, FurnitureData[] layers) {
        ChunkAccess chunk = level.getChunk(pos);
        ChunkData data = chunk.getData(CHUNK_FURNITURE_DATA);
        data.set(pos, layers);
        chunk.setData(CHUNK_FURNITURE_DATA, data);
    }

    @Override
    public VoxelShape cachedShape(LevelReader level, BlockPos pos, Supplier<VoxelShape> shapeSupplier) {
        ChunkAccess chunk = level.getChunk(pos);
        return chunk.getData(CHUNK_FURNITURE_DATA).cachedShape(pos, shapeSupplier);
    }

    @Override
    public ChunkData getChunkData(LevelReader level, ChunkPos chunkPos) {
        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);
        return chunk.getData(CHUNK_FURNITURE_DATA);
    }

}
