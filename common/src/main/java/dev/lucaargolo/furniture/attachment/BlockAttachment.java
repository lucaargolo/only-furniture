package dev.lucaargolo.furniture.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class BlockAttachment implements DataAttachment<BlockAttachment> {

    private static final Codec<BlockAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockAttachment::getBlock)
    ).apply(instance, BlockAttachment::new));

    private static final StreamCodec<ByteBuf, BlockAttachment> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT.map(id -> BuiltInRegistries.BLOCK.getHolder(id).orElseThrow().value(), BuiltInRegistries.BLOCK::getId),
        BlockAttachment::getBlock,
        BlockAttachment::new
    );

    private final Block block;

    public BlockAttachment(Block block) {
        this.block = block;
    }

    public BlockAttachment() {
        this(Blocks.AIR);
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public Codec<BlockAttachment> getCodec() {
        return CODEC;
    }

    @Override
    public StreamCodec<ByteBuf, BlockAttachment> getStreamCodec() {
        return STREAM_CODEC;
    }

}