package dev.lucaargolo.furniture.attachment.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class PlantHolderDataAttachment implements DataAttachment<PlantHolderDataAttachment> {

    public static final Codec<Vec3> VEC3_STRING_CODEC = Codec.STRING.comapFlatMap(s -> {
        try {
            String[] parts = s.split(",");
            if (parts.length != 3) return DataResult.error(() -> "Expected 3 parts for Vec3");
            return DataResult.success(new Vec3(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2])));
        } catch (Exception e) {
            return DataResult.error(e::getMessage);
        }
    }, vec -> vec.x + "," + vec.y + "," + vec.z);

    private static final StreamCodec<ByteBuf, Vec3> POS_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Vec3::x,
            ByteBufCodecs.DOUBLE, Vec3::y,
            ByteBufCodecs.DOUBLE, Vec3::z,
            Vec3::new
    );

    private static final StreamCodec<ByteBuf, Block> BLOCK_STREAM_CODEC = ByteBufCodecs.fromCodec(BuiltInRegistries.BLOCK.byNameCodec());

    public static final Codec<PlantHolderDataAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(VEC3_STRING_CODEC, BuiltInRegistries.BLOCK.byNameCodec()).fieldOf("data").forGetter(PlantHolderDataAttachment::get)
    ).apply(instance, PlantHolderDataAttachment::new));

    public static final StreamCodec<ByteBuf, PlantHolderDataAttachment> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(i -> new HashMap<>(), POS_STREAM_CODEC, BLOCK_STREAM_CODEC),
            PlantHolderDataAttachment::get,
            PlantHolderDataAttachment::new
    );

    private final Map<Vec3, Block> data;

    public PlantHolderDataAttachment(Map<Vec3, Block> data) {
        this.data = new HashMap<>(data);
    }

    private Map<Vec3, Block> get() {
        return data;
    }

    @NotNull
    public Block get(Vec3 pos) {
        return data.getOrDefault(pos, Blocks.AIR);
    }

    public PlantHolderDataAttachment set(Vec3 pos, Block block) {
        data.put(pos, block);
        return this;
    }

    public PlantHolderDataAttachment remove(Vec3 pos) {
        data.remove(pos);
        return this;
    }

    public void forEach(BiConsumer<Vec3, Block> consumer) {
        data.forEach(consumer);
    }

    @Override
    public DataAttachmentType<PlantHolderDataAttachment> getType() {
        return ModDataAttachments.PLANT_HOLDER_DATA;
    }

}
