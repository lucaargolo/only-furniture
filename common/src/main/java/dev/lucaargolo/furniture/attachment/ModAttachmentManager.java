package dev.lucaargolo.furniture.attachment;

import com.mojang.serialization.Codec;
import dev.lucaargolo.furniture.attachment.impl.BlockAttachment;
import dev.lucaargolo.furniture.attachment.impl.ChunkFurnitureDataAttachment;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public abstract class ModAttachmentManager {

    public void init() {
        registerType("block", type(
            () -> new BlockAttachment(Blocks.AIR),
            BlockAttachment.class,
            BlockAttachment.CODEC,
            BlockAttachment.STREAM_CODEC)
        );
        registerType("chunk_furniture_data", type(
            () -> new ChunkFurnitureDataAttachment(Map.of()),
            ChunkFurnitureDataAttachment.class,
            ChunkFurnitureDataAttachment.CODEC,
            ChunkFurnitureDataAttachment.STREAM_CODEC)
        );
    }

    public abstract <A extends DataAttachment<A>> DataAttachmentType<A> getType(Class<A> type);

    public abstract <A extends DataAttachment<A>> void registerType(String path, DataAttachmentType<A> type);

    @Nullable
    public abstract <A extends DataAttachment<A>> A get(Object target, DataAttachmentType<A> type);

    public abstract <A extends DataAttachment<A>> A set(Object target, DataAttachmentType<A> type, A value);

    public <A extends DataAttachment<A>> A getOrCreate(Object target, DataAttachmentType<A> type) {
        A a = get(target, type);
        if(a == null) {
            a = type.create();
            set(target, type, a);
        }
        return a;
    }

    public <A extends DataAttachment<A>> A get(Object target, Class<A> type) {
        return get(target, getType(type));
    }

    public <A extends DataAttachment<A>> A set(Object target, A value) {
        return set(target, value.getType(), value);
    }

    public <A extends DataAttachment<A>> A getOrCreate(Object target, Class<A> type) {
        return getOrCreate(target, getType(type));
    }

    protected static <A extends DataAttachment<A>> DataAttachmentType<A> type(Supplier<A> supplier, Class<A> type, @Nullable Codec<A> codec, @Nullable StreamCodec<ByteBuf, A> streamCodec) {
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
            public @Nullable StreamCodec<ByteBuf, A> getStreamCodec() {
                return streamCodec;
            }
        };
    }

}
