package dev.lucaargolo.furniture.animation;

import com.mojang.math.Axis;
import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import dev.lucaargolo.furniture.attachment.impl.AnimationDataAttachment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class Animation {

    public static final StreamCodec<RegistryFriendlyByteBuf, Animation> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(ModAnimations.REGISTRY_KEY),
            Animation::definition,
            ByteBufCodecs.VAR_INT,
            Animation::age,
            Animation::new
    );

    private final AnimationDefinition definition;

    private int age;
    private int lastAge;

    protected Animation(AnimationDefinition definition, int age) {
        this.definition = definition;
        this.age = age;
        this.lastAge = age;
    }

    protected Animation(AnimationDefinition definition) {
        this(definition, 0);
    }

    public AnimationDefinition definition() {
        return this.definition;
    }

    public int life() {
        return this.definition.life();
    }

    public int age() {
        return this.age;
    }

    public boolean tick() {
        this.lastAge = this.age;
        return ++this.age > this.life();
    }

    public BlockState applyStart(BlockState state) {
        return this.definition.startState() != null ? this.definition.startState().apply(state) : state;
    }

    public BlockState applyEnd(BlockState state) {
        return this.definition.endState() != null ? this.definition.endState().apply(state) : state;
    }

    public Matrix4f animate(String group, Matrix4f matrix, float partialTick) {
        if(group.startsWith(this.definition.group())) {
            float value = this.definition.easing().apply(this, partialTick);;
            return switch (this.definition.target()) {
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
        return this.definition.overlaps(animation.definition);
    }

    public float progress(float partialTick) {
        float p = Mth.lerp(partialTick, this.lastAge, this.age);
        return Mth.clamp(p / this.definition.life(), 0f, 1f);
    }

    public static <T extends BlockEntity> BlockEntityTicker<T> ticker() {
        return (level, pos, state, blockEntity) -> {
            AnimationDataAttachment data = ModDataAttachments.ANIMATION_DATA.get(blockEntity);
            if(data != null) {
                data.tick(blockEntity);
            }
        };
    }

}
