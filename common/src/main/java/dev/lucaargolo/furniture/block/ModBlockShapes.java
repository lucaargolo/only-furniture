package dev.lucaargolo.furniture.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ModBlockShapes {

    public static VoxelShape[] CHAIR = new VoxelShape[] {
        Block.box(3, 7, 12, 13, 24, 13),
        Block.box(3, 0, 3, 13, 7, 13)
    };

    public static VoxelShape[] COFFEE_TABLE = new VoxelShape[] {
        Shapes.box(0.125,0,0.125,0.875,0.625,0.875),
        Shapes.box(0,0.625,0.125,1,0.75,0.875),
        Shapes.box(0.125,0.625,0,0.875,0.75,1)
    };

    public static VoxelShape[] SMALL_STOOL = new VoxelShape[] {
        Shapes.box(0.25,0,0.25,0.75,0.25,0.75),
        Shapes.box(0.125,0.25,0.25,0.875,0.375,0.75),
        Shapes.box(0.25,0.25,0.125,0.75,0.375,0.875)
    };

    public static VoxelShape[] OUTDOOR_BENCH = new VoxelShape[] {
        Block.box(-8, 0, 2, -6, 2, 14),
        Block.box(22, 0, 2, 24, 2, 14),
        Block.box(22, 2, 5, 24, 12, 11),
        Block.box(-8, 2, 5, -6, 12, 11),
        Block.box(-8, 7, 11, -6, 12, 14),
        Block.box(22, 7, 11, 24, 12, 14),
        Block.box(-6, 7, 13, 22, 12, 14),
        Block.box(-8, 11, 14, 24, 16, 15),
        Block.box(-8, 15, 15, 24, 16, 16),
        Block.box(-6, 3, 7, 22, 4, 9),
        Block.box(-8, 7, 4, 24, 8, 13),
        Block.box(-8, 6, 2, 24, 7, 5)
    };

    public static VoxelShape[] PICNIC_BENCH = new VoxelShape[] {
        Block.box(26, 0, 18, 28, 2, 21),
        Block.box(-16, 6, -12, 32, 8, -4),
        Block.box(-16, 6, 20, 32, 8, 28),
        Block.box(-14, 4, 22, 30, 6, 26),
        Block.box(-14, 4, -10, 30, 6, -6),
        Block.box(28, 4, -6, 30, 6, 22),
        Block.box(-14, 4, -6, -12, 6, 22),
        Block.box(-16, 14, -4, 32, 16, 20),
        Block.box(-12, 12, 1, 28, 14, 15),
        Block.box(-12, 0, -5, -10, 2, -2),
        Block.box(-12, 2, -4, -10, 4, -1),
        Block.box(-12, 4, -3, -10, 6, 0),
        Block.box(-12, 6, -2, -10, 8, 1),
        Block.box(-12, 10, 0, -10, 12, 3),
        Block.box(-12, 8, -1, -10, 10, 2),
        Block.box(26, 8, -1, 28, 10, 2),
        Block.box(26, 10, 0, 28, 12, 3),
        Block.box(26, 6, -2, 28, 8, 1),
        Block.box(26, 4, -3, 28, 6, 0),
        Block.box(26, 2, -4, 28, 4, -1),
        Block.box(26, 0, -5, 28, 2, -2),
        Block.box(-12, 8, 14, -10, 10, 17),
        Block.box(-12, 10, 13, -10, 12, 16),
        Block.box(-12, 6, 15, -10, 8, 18),
        Block.box(-12, 4, 16, -10, 6, 19),
        Block.box(-12, 2, 17, -10, 4, 20),
        Block.box(-12, 0, 18, -10, 2, 21),
        Block.box(26, 8, 14, 28, 10, 17),
        Block.box(26, 10, 13, 28, 12, 16),
        Block.box(26, 6, 15, 28, 8, 18),
        Block.box(26, 4, 16, 28, 6, 19),
        Block.box(26, 2, 17, 28, 4, 20)
    };

    public static VoxelShape[] LAMP_POST = new VoxelShape[] {
        Shapes.box(0.1875,0,0.1875,0.8125,0.125,0.8125),
        Shapes.box(0.375,0.125,0.375,0.625,0.75,0.625),
        Shapes.box(0.4375,0.75,0.4375,0.5625,2.4375,0.5625),
        Shapes.box(0.3125,2.4375,0.3125,0.6875,3,0.6875)
    };

    public static VoxelShape[] DUAL_LAMP_POST = new VoxelShape[] {
        Shapes.box(0.1875,0,0.1875,0.8125,0.125,0.8125),
        Shapes.box(0.375,0.125,0.375,0.625,0.75,0.625),
        Shapes.box(0.4375,0.75,0.4375,0.5625,2,0.5625),
        Shapes.box(0.125,2,0.4375,0.875,2.4375,0.5625),
        Shapes.box(0,2.4375,0.3125,1,3,0.6875)
    };

    public static VoxelShape[] TRIPLE_LAMP_POST = new VoxelShape[] {
        Shapes.box(0.1875,0,0.1875,0.8125,0.125,0.8125),
        Shapes.box(0.375,0.125,0.375,0.625,0.75,0.625),
        Shapes.box(0.4375,0.75,0.4375,0.5625,1.875,0.5625),
        Shapes.box(-0.0625,1.875,0.4375,1.0625,2.375,0.5625),
        Shapes.box(-0.1875,2.3125,0.3125,1.1875,3,0.6875)
    };

}
