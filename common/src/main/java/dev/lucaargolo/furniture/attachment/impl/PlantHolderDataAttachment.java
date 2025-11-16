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
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class PlantHolderDataAttachment implements DataAttachment<PlantHolderDataAttachment> {

    public static final Codec<PlantHolderDataAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(BuiltInRegistries.BLOCK.byNameCodec()).fieldOf("data").forGetter(PlantHolderDataAttachment::get)
    ).apply(instance, PlantHolderDataAttachment::new));

    public static final StreamCodec<ByteBuf, PlantHolderDataAttachment> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.fromCodec(BuiltInRegistries.BLOCK.byNameCodec())),
            PlantHolderDataAttachment::get,
            PlantHolderDataAttachment::new
    );

    private final List<Block> data;

    public PlantHolderDataAttachment(List<Block> data) {
        this.data = new ArrayList<>(data);
    }

    private List<Block> get() {
        return data;
    }

    public Block getBlock(int index) {
        return (index >= 0 && index < data.size()) ? data.get(index) : Blocks.FLOWER_POT;
    }

    public PlantHolderDataAttachment set(int index, Block block) {
        if(index == data.size()) {
            data.add(block);
        }else if(index < data.size()) {
            data.set(index, block);
        }
        return this;
    }

    public int size() {
        return data.size();
    }

    @Override
    public DataAttachmentType<PlantHolderDataAttachment> getType() {
        return ModDataAttachments.PLANT_HOLDER_DATA;
    }

}
