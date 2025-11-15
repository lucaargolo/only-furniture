package dev.lucaargolo.furniture.attachment;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.NeoForgeFurnitureMod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class NeoForgeModAttachmentManager extends ModAttachmentManager {

    private final DeferredRegister<AttachmentType<?>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, FurnitureMod.MOD_ID);

    private final Map<DataAttachmentType<?>, Supplier<AttachmentType<?>>> registry = new HashMap<>();
    private final Map<Class<?>, DataAttachmentType<?>> classRegistry = new HashMap<>();

    @Override
    public void init() {
        super.init();
        REGISTRY.register(NeoForgeFurnitureMod.getModBus());
    }

    @Override
    public <A extends DataAttachment<A>> DataAttachmentType<A> getType(Class<A> type) {
        return (DataAttachmentType<A>) classRegistry.get(type);
    }

    @Override
    public <A extends DataAttachment<A>> void registerType(String path, DataAttachmentType<A> type) {
        registry.put(type, REGISTRY.register(path, () -> {
            AttachmentType.Builder<A> builder = AttachmentType.builder(type::create);
            if(type.isSerializable())
                builder.serialize(Objects.requireNonNull(type.getCodec()));
            if(type.isNetworkSynced())
                builder.sync(Objects.requireNonNull(type.getStreamCodec()));
            return builder.build();
        }));
        classRegistry.put(type.getType(), type);
    }

    @Override
    public <A extends DataAttachment<A>> @Nullable A get(Object target, DataAttachmentType<A> type) {
        return ((IAttachmentHolder) target).getData((AttachmentType<A>) registry.get(type).get());
    }

    @Override
    public <A extends DataAttachment<A>> A set(Object target, DataAttachmentType<A> type, A value) {
        if(value != null) {
            return ((IAttachmentHolder) target).setData((AttachmentType<A>) registry.get(type).get(), value);
        }else{
            return ((IAttachmentHolder) target).removeData((AttachmentType<A>) registry.get(type).get());
        }
    }

}
