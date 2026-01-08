package dev.lucaargolo.furniture.animation;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum Target implements StringRepresentable {
    ROTATE_X,
    ROTATE_Y,
    ROTATE_Z,
    SCALE_X,
    SCALE_Y,
    SCALE_Z,
    POSITION_X,
    POSITION_Y,
    POSITION_Z;

    public static final StreamCodec<ByteBuf, Target> STREAM_CODEC = ByteBufCodecs.idMapper(id -> Target.values()[id], Target::ordinal);

    @Override
    public @NotNull String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}