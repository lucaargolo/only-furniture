package dev.lucaargolo.furniture.registry;

import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.NeoForgeFurnitureMod;
import dev.lucaargolo.furniture.attachment.DataAttachment;
import dev.lucaargolo.furniture.attachment.DataAttachmentType;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class NeoForgeModAttachmentRegistry extends ModAttachmentRegistry<AttachmentType<?>> {

    private final DeferredRegister<AttachmentType<?>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, FurnitureMod.MOD_ID);

    private final Map<DataAttachmentType<?>, Supplier<AttachmentType<?>>> registry = new HashMap<>();

    @Override
    public void init() {
        super.init();
        REGISTRY.register(NeoForgeFurnitureMod.getModBus());
    }

    @Override
    public <A extends DataAttachment<A>> DataAttachmentType<A> register(String path, DataAttachmentType<A> type) {
        registry.put(type, REGISTRY.register(path, () -> {
            AttachmentType.Builder<A> builder = AttachmentType.builder(type::create);
            if(type.isSerializable())
                builder.serialize(Objects.requireNonNull(type.getCodec()));
            if(type.isNetworkSynced())
                builder.sync(Objects.requireNonNull(type.getStreamCodec()));
            return builder.build();
        }));
        return type;
    }

    @Override
    public <A extends DataAttachment<A>> AttachmentType<A> get(DataAttachmentType<A> type) {
        return (AttachmentType<A>) registry.get(type).get();
    }

}
