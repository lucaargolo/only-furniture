package dev.lucaargolo.furniture.animation;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.BiFunction;

public enum Easing implements StringRepresentable {
    LINEAR((a, f) -> Mth.lerp(a.progress(f), a.definition().start(), a.definition().end())),
    EASE_IN_OUT_SINE((a, f) -> Mth.lerp(-(Mth.cos(Mth.PI * a.progress(f)) - 1f) / 2f, a.definition().start(), a.definition().end()));

    public static final StreamCodec<ByteBuf, Easing> STREAM_CODEC = ByteBufCodecs.idMapper(id -> Easing.values()[id], Easing::ordinal);

    private final BiFunction<Animation, Float, Float> function;

    Easing(BiFunction<Animation, Float, Float> function) {
        this.function = function;
    }

    public float apply(Animation animation, float partialTick) {
        return function.apply(animation, partialTick);
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
