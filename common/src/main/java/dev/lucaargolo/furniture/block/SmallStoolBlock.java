package dev.lucaargolo.furniture.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SmallStoolBlock extends FurnitureSeatBlock implements WoodBlock {

    private final WoodType wood;

    public SmallStoolBlock(Block base, WoodType wood, VoxelShape[] shapes) {
        super(base, shapes, new Vec3(0.0, 0.375, 0.0));
        this.wood = wood;
    }

    @Override
    public WoodType getWood() {
        return wood;
    }

}
