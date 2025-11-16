package dev.lucaargolo.furniture.block.impl;

import dev.lucaargolo.furniture.block.base.MetalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OutdoorBenchBlock extends WoodSeatBlock implements MetalBlock {

    private final MetalType metal;
    private final WeatheringCopper.WeatherState state;

    public OutdoorBenchBlock(Block base, MetalType metal, WeatheringCopper.WeatherState state, WoodType wood, VoxelShape[] shapes, Vec3... seats) {
        super(base, wood, shapes, seats);
        this.metal = metal;
        this.state = state;
    }

    @Override
    public MetalType getMetal() {
        return this.metal;
    }

    @Override
    public WeatheringCopper.WeatherState getAge() {
        return this.state;
    }

}
