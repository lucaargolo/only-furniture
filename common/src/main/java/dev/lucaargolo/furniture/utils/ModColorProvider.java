package dev.lucaargolo.furniture.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface ModColorProvider {

    @FunctionalInterface
    interface Block {
        int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex);
    }

    @FunctionalInterface
    interface Item {
        int getColor(ItemStack stack, int tintIndex);
    }

}
