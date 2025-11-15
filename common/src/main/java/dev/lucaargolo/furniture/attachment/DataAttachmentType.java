package dev.lucaargolo.furniture.attachment;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public interface DataAttachmentType<A extends DataAttachment<A>> {

    A create();

    Class<A> getType();

    @Nullable
    Codec<A> getCodec();

    @Nullable
    StreamCodec<ByteBuf, A> getStreamCodec();

    default boolean isSerializable() {
        return this.getCodec() != null;
    }

    default boolean isNetworkSynced() {
        return this.getStreamCodec() != null;
    }

}