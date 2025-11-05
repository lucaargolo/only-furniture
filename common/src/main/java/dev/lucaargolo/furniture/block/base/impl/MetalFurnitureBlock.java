package dev.lucaargolo.furniture.block.base.impl;

import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.block.base.MetalBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MetalFurnitureBlock extends FurnitureBlock implements MetalBlock {

    private final MetalType metal;
    private final WeatheringCopper.WeatherState state;

    public MetalFurnitureBlock(MetalType metal, WeatheringCopper.WeatherState state, VoxelShape[] shapes) {
        super(metal.get(state), shapes);
        this.metal = metal;
        this.state = state;
    }

    public MetalFurnitureBlock(MetalType metal, VoxelShape[] shapes) {
        super(metal.getBase(), shapes);
        this.metal = metal;
        this.state = WeatheringCopper.WeatherState.UNAFFECTED;
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
