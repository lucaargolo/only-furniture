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
        super(metal.get(state), wood, shapes);
        this.metal = metal;
        this.state = state;
    }

    public MetalWoodFurnitureBlock(MetalType metal, WoodType wood, VoxelShape[] shapes) {
        super(metal.getBase(), wood, shapes);
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
