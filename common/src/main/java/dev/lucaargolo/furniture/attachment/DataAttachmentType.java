package dev.lucaargolo.furniture.attachment;

import com.mojang.serialization.Codec;
import dev.lucaargolo.furniture.FurnitureMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface DataAttachmentType<A extends DataAttachment<A>> {

    A create();

    Class<A> getType();

    @Nullable
    Codec<A> getCodec();

    @Nullable
    StreamCodec<RegistryFriendlyByteBuf, A> getStreamCodec();

    default boolean isSerializable() {
        return this.getCodec() != null;
    }

    default boolean isNetworkSynced() {
        return this.getStreamCodec() != null;
    }

    @Nullable
    default A get(Object target) {
        return FurnitureMod.getAttachmentManager().get(target, this);
    }

    default void set(Object target, A value) {
        FurnitureMod.getAttachmentManager().set(target, this, value);
    }

    default A getOrCreate(Object target) {
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
        return new DataAttachmentType<>() {
            @Override
            public A create() {
                return supplier.get();
            }

            @Override
            public Class<A> getType() {
                return type;
            }

            @Override
            public @Nullable Codec<A> getCodec() {
                return codec;
            }

            @Override
            public @Nullable StreamCodec<RegistryFriendlyByteBuf, A> getStreamCodec() {
                return streamCodec;
            }
        };
    }

}