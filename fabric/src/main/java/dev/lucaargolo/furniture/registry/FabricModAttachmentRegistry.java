package dev.lucaargolo.furniture.registry;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.attachment.DataAttachment;
import dev.lucaargolo.furniture.attachment.DataAttachmentType;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings({"UnstableApiUsage", "unchecked"})
public class FabricModAttachmentRegistry extends ModAttachmentRegistry<AttachmentType<?>> {

    private final Map<DataAttachmentType<?>, AttachmentType<?>> registry = new HashMap<>();

    @Override
    public <A extends DataAttachment<A>> DataAttachmentType<A> register(String path, DataAttachmentType<A> type) {
        registry.put(type, AttachmentRegistry.create(FurnitureMod.id(path), (Consumer<AttachmentRegistry.Builder<A>>) builder -> {
            builder.initializer(type::create);
            if(type.isSerializable())
                builder.persistent(type.getCodec());
            if(type.isNetworkSynced())
                builder.syncWith(type.getStreamCodec(), AttachmentSyncPredicate.all());
        }));
        return type;
    }

    @Override
    public <A extends DataAttachment<A>> AttachmentType<A> get(DataAttachmentType<A> type) {
        return (AttachmentType<A>) registry.get(type);
    }

}
