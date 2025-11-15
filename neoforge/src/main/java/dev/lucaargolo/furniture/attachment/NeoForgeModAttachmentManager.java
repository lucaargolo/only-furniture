package dev.lucaargolo.furniture.attachment;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.NeoForgeFurnitureMod;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class NeoForgeModAttachmentManager extends ModAttachmentManager {

    private final DeferredRegister<AttachmentType<?>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, FurnitureMod.MOD_ID);
    private final Map<Class<? extends DataAttachment<?>>, Supplier<AttachmentType<?>>> registeredAttachments = new HashMap<>();

    @Override
    public void init() {
        super.init();
        REGISTRY.register(NeoForgeFurnitureMod.getModBus());
    }

    @Override
    public <A extends DataAttachment<A>> void register(String path, Class<A> attachment) {
        registeredAttachments.put(attachment, REGISTRY.register(path, () -> {
            AttachmentType.Builder<A> builder = AttachmentType.builder(() -> DataAttachment.instantiate(attachment));
            if(DataAttachment.hasCodec(attachment))
                builder.serialize(DataAttachment.codec(attachment));
            if(DataAttachment.hasStreamCodec(attachment))
                builder.sync(DataAttachment.streamCodec(attachment));
            return builder.build();
        }));
    }

    @Override
    public <A extends DataAttachment<A>> @Nullable A get(ChunkAccess chunk, Class<A> attachment) {
        return chunk.getData((AttachmentType<A>) registeredAttachments.get(attachment).get());
    }

    @Override
    public <A extends DataAttachment<A>> A set(ChunkAccess chunk, A value) {
        return chunk.setData((AttachmentType<A>) registeredAttachments.get(value.getClass()).get(), value);
    }

    @Override
    public <A extends DataAttachment<A>> @Nullable A get(BlockEntity entity, Class<A> attachment) {
        return entity.getData((AttachmentType<A>) registeredAttachments.get(attachment).get());
    }

    @Override
    public <A extends DataAttachment<A>> A set(BlockEntity entity, A value) {
        return entity.setData((AttachmentType<A>) registeredAttachments.get(value.getClass()).get(), value);
    }

}
