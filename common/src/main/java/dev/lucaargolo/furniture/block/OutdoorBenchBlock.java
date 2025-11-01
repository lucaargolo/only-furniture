package dev.lucaargolo.furniture.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OutdoorBenchBlock extends FurnitureSeatBlock implements WoodBlock, MetalBlock {
    private final WoodType wood;

    public OutdoorBenchBlock(Block base, WoodType wood, VoxelShape[] shapes) {
        super(base, shapes, new Vec3(-0.5, 0.5, 0.0), new Vec3(0.5, 0.5, 0.0));
        this.wood = wood;
    }

    @Override
    public WoodType getWood() {
        return wood;
    }

    @Override
    public MetalType getMetal() {
        return MetalType.IRON;
    }

    @Override
    public WeatheringCopper.WeatherState getAge() {
        return WeatheringCopper.WeatherState.UNAFFECTED;
    }


}
