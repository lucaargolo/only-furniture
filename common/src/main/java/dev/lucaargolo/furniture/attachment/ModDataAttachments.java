package dev.lucaargolo.furniture.attachment;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.attachment.impl.ChunkFurnitureDataAttachment;
import dev.lucaargolo.furniture.attachment.impl.PlantDataAttachment;
import dev.lucaargolo.furniture.attachment.impl.StorageDataAttachment;
import dev.lucaargolo.furniture.registry.ModAttachmentRegistry;

import java.util.Map;

public class ModDataAttachments {

    public static final ModAttachmentRegistry<?> REGISTRY = FurnitureMod.attachmentRegistry();

    public static final DataAttachmentType<ChunkFurnitureDataAttachment> CHUNK_FURNITURE_DATA = REGISTRY.register("chunk_furniture_data", DataAttachmentType.of(
            ChunkFurnitureDataAttachment.class,
            () -> new ChunkFurnitureDataAttachment(Map.of()),
            ChunkFurnitureDataAttachment.CODEC,
            ChunkFurnitureDataAttachment.STREAM_CODEC
    ));

    public static final DataAttachmentType<PlantDataAttachment> PLANT_DATA = REGISTRY.register("plant_data", DataAttachmentType.of(
            PlantDataAttachment.class,
            () -> new PlantDataAttachment(Map.of()),
            PlantDataAttachment.CODEC,
            PlantDataAttachment.STREAM_CODEC
    ));

    public static final DataAttachmentType<StorageDataAttachment> STORAGE_DATA = REGISTRY.register("storage_data", DataAttachmentType.of(
            StorageDataAttachment.class,
            () -> new StorageDataAttachment(Map.of()),
            StorageDataAttachment.CODEC
    ));


}
