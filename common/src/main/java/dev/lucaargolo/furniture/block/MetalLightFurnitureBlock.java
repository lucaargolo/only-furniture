package dev.lucaargolo.furniture.block;

import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MetalLightFurnitureBlock extends LightFurnitureBlock implements MetalBlock {

    private final MetalType metal;
    private final WeatheringCopper.WeatherState state;

    public MetalLightFurnitureBlock(Properties properties, MetalType metal, WeatheringCopper.WeatherState state, VoxelShape[] shapes) {
        super(properties, shapes);
        this.metal = metal;
        this.state = state;
    }

    public MetalLightFurnitureBlock(MetalType metal, WeatheringCopper.WeatherState state, VoxelShape[] shapes) {
        super(metal.get(state), shapes);
        this.metal = metal;
        this.state = state;
    }

    public MetalLightFurnitureBlock(MetalType metal, VoxelShape[] shapes) {
        super(metal.getBase(), shapes);
        this.metal = metal;
        this.state = WeatheringCopper.WeatherState.UNAFFECTED;
    }

    @Override
    public MetalType getMetal() {
        return metal;
    }

    @Override
    public WeatheringCopper.WeatherState getAge() {
        return state;
    }

}
