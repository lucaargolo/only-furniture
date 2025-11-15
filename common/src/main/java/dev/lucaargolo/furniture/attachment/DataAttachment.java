package dev.lucaargolo.furniture.attachment;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public interface DataAttachment<A extends DataAttachment<A>> {

    static <A extends DataAttachment<A>> A instantiate(Class<A> attachment) {
        try {
            return attachment.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static <A extends DataAttachment<A>> boolean hasCodec(Class<A> attachment) {
        return codec(attachment) != null;
    }

    static <A extends DataAttachment<A>> Codec<A> codec(Class<A> attachment) {
        return instantiate(attachment).getCodec();
    }

    static <A extends DataAttachment<A>> boolean hasStreamCodec(Class<A> attachment) {
        return streamCodec(attachment) != null;
    }

    static <A extends DataAttachment<A>> StreamCodec<ByteBuf, A> streamCodec(Class<A> attachment) {
        return instantiate(attachment).getStreamCodec();
    }

    @Nullable
    default Codec<A> getCodec() {
        return null;
    }

    @Nullable
    default StreamCodec<ByteBuf, A> getStreamCodec() {
        return null;
    }

}