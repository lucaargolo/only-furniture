package dev.lucaargolo.furniture.utils;

import com.mojang.datafixers.util.Function7;
import com.mojang.math.Axis;
import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.attachment.impl.AnimationDataAttachment;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Animation {

    public static final StreamCodec<ByteBuf, Animation> STREAM_CODEC = compositeStreamCodec(
        ByteBufCodecs.STRING_UTF8, a -> a.group,
        ByteBufCodecs.VAR_INT, a -> a.progress,
        ByteBufCodecs.VAR_INT, a -> a.duration,
        ByteBufCodecs.FLOAT, a -> a.start,
        ByteBufCodecs.FLOAT, a -> a.end,
        Easing.STREAM_CODEC, a -> a.easing,
        Type.STREAM_CODEC,   a -> a.type,
        (a, b, c, d, e, f, g) -> new Animation(a, b, c, d, e, f, g, state -> state, state -> state)
    );

    private final String group;
    private final int duration;
    private final float start;
    private final float end;
    private final Easing easing;
    private final Type type;

    @Nullable
    private final Function<BlockState, BlockState> startState;
    @Nullable
    private final Function<BlockState, BlockState> endState;

    private int progress;
    private int lastProgress;

    public Animation(String group, int progress, int duration, float start, float end, Easing easing, Type type, @Nullable Function<BlockState, BlockState> startState, @Nullable Function<BlockState, BlockState> endState) {
        this.group = group;
        this.progress = progress;
        this.lastProgress = progress;
        this.duration = duration;
        this.start = start;
        this.end = end;
        this.easing = easing;
        this.type = type;
        this.startState = startState;
        this.endState = endState;
    }

    public Animation(String group, int duration, float start, float end, Easing easing, Type type, @Nullable Function<BlockState, BlockState> startState, @Nullable Function<BlockState, BlockState> endState) {
        this(group, 0, duration, start, end, easing, type, startState, endState);
    }

    public int progress() {
        return progress;
    }

    public int duration() {
        return duration;
    }

    public Animation copy(int progress) {
        return new Animation(this.group, progress, this.duration, this.start, this.end, this.easing, this.type, this.startState, this.endState);
    }

    public Animation copy() {
        return new Animation(this.group, this.progress, this.duration, this.start, this.end, this.easing, this.type, this.startState, this.endState);
    }

    public boolean process() {
        this.lastProgress = progress;
        return ++this.progress > this.duration;
    }

    public BlockState applyStart(BlockState state) {
        return this.startState != null ? this.startState.apply(state) : state;
    }

    public BlockState applyEnd(BlockState state) {
        return this.endState != null ? this.endState.apply(state) : state;
    }

    public Matrix4f animate(String group, Matrix4f matrix, float partialTick) {
        if(group.startsWith(this.group)) {
            float value = this.value(partialTick);
            return switch (this.type) {
                case ROTATE_X -> matrix.rotate(Axis.XP.rotationDegrees(value));
                case ROTATE_Y -> matrix.rotate(Axis.YP.rotationDegrees(value));
                case ROTATE_Z -> matrix.rotate(Axis.ZP.rotationDegrees(value));
                case SCALE_X -> matrix.scale(value, 1f, 1f);
                case SCALE_Y -> matrix.scale(1f, value, 1f);
                case SCALE_Z -> matrix.scale(1f, 1f, value);
                case POSITION_X -> matrix.translate(value, 0f, 0f);
                case POSITION_Y -> matrix.translate(0f, value, 0f);
                case POSITION_Z -> matrix.translate(0f, 0f, value);
            };
        }else{
            return matrix;
        }
    }

    public boolean overlaps(Animation animation) {
        return this.group.equals(animation.group) && this.type == animation.type;
    }

    private float value(float partialTick) {
        return this.easing.apply(this, partialTick);
    }

    private float progress(float partialTick) {
        float p = Mth.lerp(partialTick, this.lastProgress, this.progress);
        return Mth.clamp(p / this.duration, 0f, 1f);
    }

    public static <T extends BlockEntity> BlockEntityTicker<T> ticker() {
        return (level, pos, state, blockEntity) -> {
            AnimationDataAttachment data = ModDataAttachments.ANIMATION_DATA.get(blockEntity);
            if(data != null) {
                data.tick(blockEntity);
            }
        };
    }

    public enum Easing implements StringRepresentable {
        LINEAR((a, f) -> Mth.lerp(a.progress(f), a.start, a.end)),
        EASE_IN_OUT_SINE((a, f) -> Mth.lerp(-(Mth.cos(Mth.PI * a.progress(f)) - 1f) / 2f, a.start, a.end));

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
