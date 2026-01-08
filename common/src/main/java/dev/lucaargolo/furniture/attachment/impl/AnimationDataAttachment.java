package dev.lucaargolo.furniture.attachment.impl;

import dev.lucaargolo.furniture.animation.Animation;
import dev.lucaargolo.furniture.attachment.DataAttachment;
import dev.lucaargolo.furniture.attachment.DataAttachmentType;
import dev.lucaargolo.furniture.attachment.ModDataAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class AnimationDataAttachment implements DataAttachment<AnimationDataAttachment> {

    public static final StreamCodec<RegistryFriendlyByteBuf, AnimationDataAttachment> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, Animation.STREAM_CODEC),
            AnimationDataAttachment::get,
            AnimationDataAttachment::new
    );

    private final List<Animation> data;

    public AnimationDataAttachment(List<Animation> data) {
        this.data = new ArrayList<>(data);
    }

    public List<Animation> get() {
        return this.data;
    }

    private AnimationDataAttachment innerSet(Level level, BlockPos pos, BlockState state, Animation animation, boolean replace) {
        this.data.sort(Comparator.comparingInt(a -> a.life() - a.age()));
        int ageSum = 0;
        int lifeSum = 0;
        for (Animation a : this.data) {
            ageSum += a.age();
            lifeSum += a.life();
        }
        if(replace) {
            this.data.removeIf(a -> a.overlaps(animation));
        }else{
            this.data.clear();
        }

        float p = (float) ageSum / (float) lifeSum;
        float i = 1.0f - p;
        int age = replace ? Mth.floor(i * animation.life()) : 0;

        level.setBlockAndUpdate(pos, animation.applyStart(state));
        this.data.add(animation.definition().animation(age));
        return this;
    }

    public AnimationDataAttachment add(Level level, BlockPos pos, BlockState state, Animation animation) {
        level.setBlockAndUpdate(pos, animation.applyStart(state));
        this.data.add(animation);
        return this;
    }

    public AnimationDataAttachment set(Level level, BlockPos pos, BlockState state, Animation animation) {
        return innerSet(level, pos, state, animation, false);
    }

    public AnimationDataAttachment replace(Level level, BlockPos pos, BlockState state, Animation animation) {
        return innerSet(level, pos, state, animation, true);
    }

    public void tick(BlockEntity target) {
        Iterator<Animation> it = this.data.iterator();
        while (it.hasNext()) {
            Animation animation = it.next();
            if(animation.tick()) {
                Level level = target.getLevel();
                if(level != null) {
                    BlockPos pos = target.getBlockPos();
                    BlockState state = target.getBlockState();
                    level.setBlockAndUpdate(pos, animation.applyEnd(state));
                }
                it.remove();
            }
        }
        if(this.data.isEmpty()) {
            ModDataAttachments.ANIMATION_DATA.set(target, null);
        }
    }

    public Matrix4f animate(String group, Matrix4f matrix, float partialTick) {
        for(Animation animation : this.data) {
            matrix = animation.animate(group, matrix, partialTick);
        }
        return matrix;
    }
    @Override
    public DataAttachmentType<AnimationDataAttachment> getType() {
        return ModDataAttachments.ANIMATION_DATA;
    }


}
