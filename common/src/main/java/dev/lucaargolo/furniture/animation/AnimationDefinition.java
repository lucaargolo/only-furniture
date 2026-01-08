package dev.lucaargolo.furniture.animation;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record AnimationDefinition(String group, int life, float start, float end, Easing easing, Target target, @Nullable Function<BlockState, BlockState> startState, @Nullable Function<BlockState, BlockState> endState) {

    public Animation animation() {
        return new Animation(this);
    }

    public Animation animation(int age) {
        return new Animation(this, age);
    }

    public boolean overlaps(AnimationDefinition definition) {
        return this.group.equals(definition.group) && this.target == definition.target;
    }

}
