package dev.lucaargolo.furniture.block.impl;

import dev.lucaargolo.furniture.block.base.impl.MetalFurnitureBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class KitchenSinkBlock extends MetalFurnitureBlock {

    public KitchenSinkBlock(MetalType metal, WeatheringCopper.WeatherState state, VoxelShape[] shapes) {
        super(metal, state, shapes);
    }

    public KitchenSinkBlock(MetalType metal, VoxelShape[] shapes) {
        super(metal, shapes);
    }

    public static class Weathering extends KitchenSinkBlock implements WeatheringCopper {

        public Weathering(MetalType metal, WeatherState state, VoxelShape[] shapes) {
            super(metal, state, shapes);
        }

        @Override
        protected void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
            this.changeOverTime(state, level, pos, random);
        }

        @Override
        public @NotNull WeatherState getAge() {
            return super.getAge();
        }

    }


}
