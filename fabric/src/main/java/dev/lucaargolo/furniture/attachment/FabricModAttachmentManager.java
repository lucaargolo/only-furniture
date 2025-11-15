package dev.lucaargolo.furniture.attachment;

import dev.lucaargolo.furniture.FurnitureMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings({"UnstableApiUsage", "unchecked"})
public class FabricModAttachmentManager extends ModAttachmentManager {

    private final Map<DataAttachmentType<?>, AttachmentType<?>> registry = new HashMap<>();
    private final Map<Class<?>, DataAttachmentType<?>> classRegistry = new HashMap<>();

    @Override
    public <A extends DataAttachment<A>> DataAttachmentType<A> getType(Class<A> type) {
        return (DataAttachmentType<A>) classRegistry.get(type);
    }

    @Override
    public <A extends DataAttachment<A>> void registerType(String path, DataAttachmentType<A> type) {
        registry.put(type, AttachmentRegistry.create(FurnitureMod.id(path), (Consumer<AttachmentRegistry.Builder<A>>) builder -> {
            builder.initializer(type::create);
            if(type.isSerializable())
                builder.persistent(type.getCodec());
            if(type.isNetworkSynced())
                builder.syncWith(type.getStreamCodec(), AttachmentSyncPredicate.all());
        }));
        classRegistry.put(type.getType(), type);
    }

    @Override
    public <A extends DataAttachment<A>> @Nullable A get(Object target, DataAttachmentType<A> type) {
        return ((AttachmentTarget) target).getAttached((AttachmentType<A>) registry.get(type));
    }

    @Override
    public <A extends DataAttachment<A>> A set(Object target, DataAttachmentType<A> type, A value) {
        return ((AttachmentTarget) target).setAttached((AttachmentType<A>) registry.get(type), value);
    }

}
