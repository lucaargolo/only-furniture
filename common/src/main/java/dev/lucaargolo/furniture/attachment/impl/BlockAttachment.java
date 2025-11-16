package dev.lucaargolo.furniture.attachment.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lucaargolo.furniture.attachment.DataAttachment;
import dev.lucaargolo.furniture.attachment.DataAttachmentType;
import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;

public final class BlockAttachment implements DataAttachment<BlockAttachment> {

    public static final Codec<BlockAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockAttachment::get)
    ).apply(instance, BlockAttachment::new));

    public static final StreamCodec<ByteBuf, BlockAttachment> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT.map(id -> BuiltInRegistries.BLOCK.getHolder(id).orElseThrow().value(), BuiltInRegistries.BLOCK::getId),
            BlockAttachment::get,
            BlockAttachment::new
    );

    private Block block;

    public BlockAttachment(Block block) {
        this.block = block;
    }

    public Block get() {
        return this.block;
    }

    public BlockAttachment set(Block block) {
        this.block = block;
        return this;
    }

    @Override
    public DataAttachmentType<BlockAttachment> getType() {
        return ModDataAttachments.BLOCK;
    }

}