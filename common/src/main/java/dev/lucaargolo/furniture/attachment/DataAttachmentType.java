package dev.lucaargolo.furniture.attachment;

import com.mojang.serialization.Codec;
import dev.lucaargolo.furniture.FurnitureMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DataAttachmentType<A extends DataAttachment<A>> {

    private final Class<A> type;
    private final Supplier<A> supplier;

    @Nullable
    private final Codec<A> codec;
    @Nullable
    private final StreamCodec<RegistryFriendlyByteBuf, A> streamCodec;

    private DataAttachmentType(Class<A> type, Supplier<A> supplier, @Nullable Codec<A> codec, @Nullable StreamCodec<RegistryFriendlyByteBuf, A> streamCodec) {
        this.type = type;
        this.supplier = supplier;
        this.codec = codec;
        this.streamCodec = streamCodec;
    }

    public A create() {
        return supplier.get();
    }

    public Class<A> getType() {
        return type;
    }

    public @Nullable Codec<A> getCodec() {
        return codec;
    }

    public @Nullable StreamCodec<RegistryFriendlyByteBuf, A> getStreamCodec() {
        return streamCodec;
    }

    public boolean isSerializable() {
        return this.getCodec() != null;
    }

    public boolean isNetworkSynced() {
        return this.getStreamCodec() != null;
    }

    @Nullable
    public A get(Object target) {
        return FurnitureMod.getAttachmentManager().get(target, this);
    }

    public void set(Object target, A value) {
        FurnitureMod.getAttachmentManager().set(target, this, value);
    }

    public A getOrCreate(Object target) {
        return FurnitureMod.getAttachmentManager().getOrCreate(target, this);
    }

    static <A extends DataAttachment<A>> DataAttachmentType<A> of(Class<A> type, Supplier<A> supplier) {
        return of(type, supplier, null, null);
    }

    static <A extends DataAttachment<A>> DataAttachmentType<A> of(Class<A> type, Supplier<A> supplier, @Nullable Codec<A> codec) {
        return of(type, supplier, codec, null);
    }

    static <A extends DataAttachment<A>> DataAttachmentType<A> of(Class<A> type, Supplier<A> supplier, @Nullable StreamCodec<RegistryFriendlyByteBuf, A> streamCodec) {
        return of(type, supplier, null, streamCodec);
    }

    static <A extends DataAttachment<A>> DataAttachmentType<A> of(Class<A> type, Supplier<A> supplier, @Nullable Codec<A> codec, @Nullable StreamCodec<RegistryFriendlyByteBuf, A> streamCodec) {
        return new DataAttachmentType<>(type, supplier, codec, streamCodec);
    }

}