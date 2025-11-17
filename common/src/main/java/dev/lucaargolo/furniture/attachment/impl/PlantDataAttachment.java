package dev.lucaargolo.furniture.attachment.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lucaargolo.furniture.attachment.DataAttachment;
import dev.lucaargolo.furniture.attachment.DataAttachmentType;
import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

public class PlantDataAttachment implements DataAttachment<PlantDataAttachment> {

    public static final Codec<PlantDataAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING.xmap(Integer::valueOf, Object::toString), BuiltInRegistries.BLOCK.byNameCodec()).fieldOf("data").forGetter(PlantDataAttachment::get)
    ).apply(instance, PlantDataAttachment::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlantDataAttachment> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.INT, ByteBufCodecs.fromCodec(BuiltInRegistries.BLOCK.byNameCodec())),
            PlantDataAttachment::get,
            PlantDataAttachment::new
    );

    private final Map<Integer, Block> data;

    public PlantDataAttachment(Map<Integer, Block> data) {
        this.data = new HashMap<>(data);
    }

    private Map<Integer, Block> get() {
        return this.data;
    }

    public Block getBlock(int index) {
        return data.getOrDefault(index, Blocks.FLOWER_POT);
    }

    public PlantDataAttachment set(int index, Block block) {
        data.put(index, block);
        return this;
    }

    @Override
    public DataAttachmentType<PlantDataAttachment> getType() {
        return ModDataAttachments.PLANT_DATA;
    }

}
