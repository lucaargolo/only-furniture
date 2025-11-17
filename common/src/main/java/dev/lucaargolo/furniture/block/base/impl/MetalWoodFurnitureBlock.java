package dev.lucaargolo.furniture.block.base.impl;

import dev.lucaargolo.furniture.block.base.MetalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MetalWoodFurnitureBlock extends WoodFurnitureBlock implements MetalBlock {

    private final MetalType metal;
    private final WeatheringCopper.WeatherState state;

    public MetalWoodFurnitureBlock(Block base, MetalType metal, WeatheringCopper.WeatherState state, WoodType wood, VoxelShape[] shapes) {
        super(base, wood, shapes);
        this.metal = metal;
        this.state = state;
    }

    public MetalWoodFurnitureBlock(MetalType metal, WeatheringCopper.WeatherState state, WoodType wood, VoxelShape[] shapes) {
        this(metal.get(state), metal, state, wood, shapes);
    }

    public MetalWoodFurnitureBlock(MetalType metal, WoodType wood, VoxelShape[] shapes) {
        this(metal, WeatheringCopper.WeatherState.UNAFFECTED, wood, shapes);
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
