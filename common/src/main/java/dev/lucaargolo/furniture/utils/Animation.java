package dev.lucaargolo.furniture.utils;

import com.mojang.datafixers.util.Function7;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Function;

public class Animation {

    public static final StreamCodec<ByteBuf, Animation> STREAM_CODEC = Animation.compositeStreamCodec(
        ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), a -> a.state,
        ByteBufCodecs.VAR_INT, a -> a.progress,
        ByteBufCodecs.VAR_INT, a -> a.duration,
        ByteBufCodecs.FLOAT, a -> a.start,
        ByteBufCodecs.FLOAT, a -> a.end,
        Easing.STREAM_CODEC, a -> a.easing,
        Type.STREAM_CODEC,   a -> a.type,
        Animation::new
    );

    private final BlockState state;
    private final int duration;
    private final float start;
    private final float end;
    private final Easing easing;
    private final Type type;

    private int progress;

    public Animation(BlockState state, int progress, int duration, float start, float end, Easing easing, Type type) {
        this.state = state;
        this.progress = progress;
        this.duration = duration;
        this.start = start;
        this.end = end;
        this.easing = easing;
        this.type = type;
    }

    public Animation(BlockState state, int duration, float start, float end, Easing easing, Type type) {
        this(state, 0, duration, start, end, easing, type);
    }

    public Type type() {
        return type;
    }

    public boolean tick() {
        return ++this.progress > this.duration;
    }

    public float value() {
        return this.easing.apply(this);
    }

    private float p() {
        return Mth.clamp((float) this.progress / this.duration, 0f, 1f);
    }

    public enum Easing implements StringRepresentable {
        LINEAR(a -> Mth.lerp(a.p(), a.start, a.end)),
        EASE_IN_OUT_SINE(a -> Mth.lerp(-(Mth.cos(Mth.PI * a.p()) - 1f) / 2f, a.start, a.end));

        public static final StreamCodec<ByteBuf, Easing> STREAM_CODEC = ByteBufCodecs.idMapper(id -> Easing.values()[id], Easing::ordinal);

        private final Function<Animation, Float> function;

        Easing(Function<Animation, Float> function) {
            this.function = function;
        }

        public float apply(Animation animation) {
            return function.apply(animation);
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public enum Type implements StringRepresentable {
        ROTATE_X,
        ROTATE_Y,
        ROTATE_Z,
        SCALE_X,
        SCALE_Y,
        SCALE_Z,
        POSITION_X,
        POSITION_Y,
        POSITION_Z;

        public static final StreamCodec<ByteBuf, Type> STREAM_CODEC = ByteBufCodecs.idMapper(id -> Type.values()[id], Type::ordinal);

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    private static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> compositeStreamCodec(
            final StreamCodec<? super B, T1> codec1, final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2, final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3, final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4, final Function<C, T4> getter4,
            final StreamCodec<? super B, T5> codec5, final Function<C, T5> getter5,
            final StreamCodec<? super B, T6> codec6, final Function<C, T6> getter6,
            final StreamCodec<? super B, T7> codec7, final Function<C, T7> getter7,
            final Function7<T1, T2, T3, T4, T5, T6, T7, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public @NotNull C decode(@NotNull B buf) {
                T1 t1 = codec1.decode(buf);
                T2 t2 = codec2.decode(buf);
                T3 t3 = codec3.decode(buf);
                T4 t4 = codec4.decode(buf);
                T5 t5 = codec5.decode(buf);
                T6 t6 = codec6.decode(buf);
                T7 t7 = codec7.decode(buf);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7);
            }

            @Override
            public void encode(@NotNull B buf, @NotNull C data) {
                codec1.encode(buf, getter1.apply(data));
                codec2.encode(buf, getter2.apply(data));
                codec3.encode(buf, getter3.apply(data));
                codec4.encode(buf, getter4.apply(data));
                codec5.encode(buf, getter5.apply(data));
                codec6.encode(buf, getter6.apply(data));
                codec7.encode(buf, getter7.apply(data));
            }
        };
    }

}
