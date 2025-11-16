package dev.lucaargolo.furniture.attachment;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.attachment.impl.BlockAttachment;
import dev.lucaargolo.furniture.attachment.impl.ChunkFurnitureDataAttachment;
import dev.lucaargolo.furniture.attachment.impl.PlantHolderDataAttachment;
import dev.lucaargolo.furniture.registry.ModAttachmentRegistry;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;

public class ModDataAttachments {

    public static ModAttachmentRegistry<?> REGISTRY = FurnitureMod.INSTANCE.attachmentRegistry();

    public static DataAttachmentType<BlockAttachment> BLOCK = REGISTRY.register("block", DataAttachmentType.of(
            BlockAttachment.class,
            () -> new BlockAttachment(Blocks.AIR),
            BlockAttachment.CODEC,
            BlockAttachment.STREAM_CODEC
    ));

    public static DataAttachmentType<ChunkFurnitureDataAttachment> CHUNK_FURNITURE_DATA = REGISTRY.register("chunk_furniture_data", DataAttachmentType.of(
            ChunkFurnitureDataAttachment.class,
            () -> new ChunkFurnitureDataAttachment(Map.of()),
            ChunkFurnitureDataAttachment.CODEC,
            ChunkFurnitureDataAttachment.STREAM_CODEC
    ));

    public static DataAttachmentType<PlantHolderDataAttachment> PLANT_HOLDER_DATA = REGISTRY.register("plant_holder_data", DataAttachmentType.of(
            PlantHolderDataAttachment.class,
            () -> new PlantHolderDataAttachment(List.of()),
            PlantHolderDataAttachment.CODEC,
            PlantHolderDataAttachment.STREAM_CODEC
    ));

}
