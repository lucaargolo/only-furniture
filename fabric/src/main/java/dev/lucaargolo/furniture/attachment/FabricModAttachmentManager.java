package dev.lucaargolo.furniture.attachment;

import dev.lucaargolo.furniture.FurnitureMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings({"UnstableApiUsage", "unchecked"})
public class FabricModAttachmentManager extends ModAttachmentManager {

    private final Map<Class<? extends DataAttachment<?>>, AttachmentType<?>> registeredAttachments = new HashMap<>();

    @Override
    public <A extends DataAttachment<A>> void register(String path, Class<A> attachment) {
        registeredAttachments.put(attachment, AttachmentRegistry.create(FurnitureMod.id(path), (Consumer<AttachmentRegistry.Builder<A>>) builder -> {
            builder.initializer(() -> DataAttachment.instantiate(attachment));
            if(DataAttachment.hasCodec(attachment))
                builder.persistent(DataAttachment.codec(attachment));
            if(DataAttachment.hasStreamCodec(attachment))
                builder.syncWith(DataAttachment.streamCodec(attachment), AttachmentSyncPredicate.all());
        }));
    }

    @Override
    public <A extends DataAttachment<A>> @Nullable A get(ChunkAccess chunk, Class<A> attachment) {
        return chunk.getAttached((AttachmentType<A>) registeredAttachments.get(attachment));
    }

    @Override
    public <A extends DataAttachment<A>> A set(ChunkAccess chunk, A value) {
        return chunk.setAttached((AttachmentType<A>) registeredAttachments.get(value.getClass()), value);
    }

    @Override
    public <A extends DataAttachment<A>> @Nullable A get(BlockEntity entity, Class<A> attachment) {
        return entity.getAttached((AttachmentType<A>) registeredAttachments.get(attachment));
    }

    @Override
    public <A extends DataAttachment<A>> A set(BlockEntity entity, A value) {
        return entity.setAttached((AttachmentType<A>) registeredAttachments.get(value.getClass()), value);
    }

}
