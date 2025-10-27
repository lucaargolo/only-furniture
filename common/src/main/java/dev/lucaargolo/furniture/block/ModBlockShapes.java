package dev.lucaargolo.furniture.block;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ModBlockShapes {

    public static VoxelShape[] SMALL_TABLE = new VoxelShape[] {
        Shapes.box(0.125,0,0.125,0.875,0.625,0.875),
        Shapes.box(0,0.625,0.125,1,0.75,0.875),
        Shapes.box(0.125,0.625,0,0.875,0.75,1)
    };

    public static VoxelShape[] SMALL_STOOL = new VoxelShape[] {
        Shapes.box(0.25,0,0.25,0.75,0.25,0.75),
        Shapes.box(0.125,0.25,0.25,0.875,0.375,0.75),
        Shapes.box(0.25,0.25,0.125,0.75,0.375,0.875)
    };

    public static VoxelShape[] LAMP_POST = new VoxelShape[] {
            Shapes.box(0.1875,0,0.1875,0.8125,0.125,0.8125),
            Shapes.box(0.375,0.125,0.375,0.625,0.75,0.625),
            Shapes.box(0.4375,0.75,0.4375,0.5625,2,0.5625),
            Shapes.box(0.125,2,0.4375,0.875,2.4375,0.5625),
            Shapes.box(0,2.4375,0.3125,1,3,0.6875)
    };

}
