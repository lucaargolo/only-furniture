package dev.lucaargolo.furniture.block.base.impl;

import dev.lucaargolo.furniture.block.base.MetalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MetalStoneFurnitureBlock extends StoneFurnitureBlock implements MetalBlock {

    private final MetalType metal;
    private final WeatheringCopper.WeatherState state;

    public MetalStoneFurnitureBlock(Block base, MetalBlock.MetalType metal, WeatheringCopper.WeatherState state, StoneType stone, VoxelShape[] shapes) {
        super(base, stone, shapes);
        this.metal = metal;
        this.state = state;
    }

    public MetalStoneFurnitureBlock(MetalBlock.MetalType metal, WeatheringCopper.WeatherState state, StoneType stone, VoxelShape[] shapes) {
        this(metal.get(state), metal, state, stone, shapes);
    }

    public MetalStoneFurnitureBlock(MetalBlock.MetalType metal, StoneType stone, VoxelShape[] shapes) {
        this(metal, WeatheringCopper.WeatherState.UNAFFECTED, stone, shapes);
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
