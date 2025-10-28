package dev.lucaargolo.furniture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class WeatheringMetalLightFurnitureBlock extends MetalLightFurnitureBlock implements WeatheringCopper {

    public WeatheringMetalLightFurnitureBlock(MetalType metal, WeatherState state, VoxelShape[] shapes) {
        super(Properties.ofFullCopy(metal.get(state)).lightLevel(s -> 15).dynamicShape().noTerrainParticles().randomTicks(), metal, state, shapes);
    }

    @Override
    protected void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        this.changeOverTime(state, level, pos, random);
    }

}
