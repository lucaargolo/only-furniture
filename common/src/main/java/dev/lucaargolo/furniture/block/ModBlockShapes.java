package dev.lucaargolo.furniture.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ModBlockShapes {

    public static final VoxelShape[] EMPTY = new VoxelShape[0];

    public static final VoxelShape[] TABLE = new VoxelShape[] {
        Block.box(0, 12, 0, 16, 16, 16)
    };

    public static final VoxelShape[] TABLE_FOOT = new VoxelShape[] {
        Block.box(11, 0, 2, 14, 12, 5)
    };

    public static final VoxelShape[] COFFEE_TABLE = new VoxelShape[] {
        Block.box(0, 10, 0, 16, 12, 16)
    };

    public static final VoxelShape[] COFFEE_TABLE_FOOT = new VoxelShape[] {
        Block.box(12, 0, 2, 14, 10, 4)
    };

    public static final VoxelShape[] CHAIR = new VoxelShape[] {
        Block.box(3, 7, 12, 13, 24, 13),
        Block.box(3, 0, 3, 13, 7, 13)
    };

    public static final VoxelShape[] SMALL_STOOL = new VoxelShape[] {
        Shapes.box(0.25,0,0.25,0.75,0.25,0.75),
        Shapes.box(0.125,0.25,0.25,0.875,0.375,0.75),
        Shapes.box(0.25,0.25,0.125,0.75,0.375,0.875)
    };

    public static final VoxelShape[] OUTDOOR_BENCH = new VoxelShape[] {
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

    public static final VoxelShape[] PICNIC_BENCH = new VoxelShape[] {
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

    public static final VoxelShape[] LAMP_POST = new VoxelShape[] {
        Shapes.box(0.1875,0,0.1875,0.8125,0.125,0.8125),
        Shapes.box(0.375,0.125,0.375,0.625,0.75,0.625),
        Shapes.box(0.4375,0.75,0.4375,0.5625,2.4375,0.5625),
        Shapes.box(0.3125,2.4375,0.3125,0.6875,3,0.6875)
    };

    public static final VoxelShape[] DUAL_LAMP_POST = new VoxelShape[] {
        Shapes.box(0.1875,0,0.1875,0.8125,0.125,0.8125),
        Shapes.box(0.375,0.125,0.375,0.625,0.75,0.625),
        Shapes.box(0.4375,0.75,0.4375,0.5625,2,0.5625),
        Shapes.box(0.125,2,0.4375,0.875,2.4375,0.5625),
        Shapes.box(0,2.4375,0.3125,1,3,0.6875)
    };

    public static final VoxelShape[] TRIPLE_LAMP_POST = new VoxelShape[] {
        Shapes.box(0.1875,0,0.1875,0.8125,0.125,0.8125),
        Shapes.box(0.375,0.125,0.375,0.625,0.75,0.625),
        Shapes.box(0.4375,0.75,0.4375,0.5625,1.875,0.5625),
        Shapes.box(-0.0625,1.875,0.4375,1.0625,2.375,0.5625),
        Shapes.box(-0.1875,2.3125,0.3125,1.1875,3,0.6875)
    };

    public static final VoxelShape[] KITCHEN_SINK = new VoxelShape[] {
        Block.box(2, 0, 4, 14, 5, 14),
        Block.box(7.25, 5, 11.5, 8.75, 9.5, 12.5),
        Block.box(7.25, 7.5, 7.5, 8.75, 9.5, 8.5),
        Block.box(7.25, 9, 8, 8.75, 10, 9),
        Block.box(7.25, 9, 11, 8.75, 10, 12),
        Block.box(7.25, 9.5, 8.5, 8.75, 10.5, 11.5)
    };

    public static final VoxelShape[] KITCHEN_SINK_DROPPED = new VoxelShape[] {
        Block.box(2, 0, 4, 14, 1, 14),
        Block.box(7.25, 1, 11.5, 8.75, 5.5, 12.5),
        Block.box(7.25, 3.5, 7.5, 8.75, 5.5, 8.5),
        Block.box(7.25, 5, 8, 8.75, 6, 9),
        Block.box(7.25, 5, 11, 8.75, 6, 12),
        Block.box(7.25, 5.5, 8.5, 8.75, 6.5, 11.5)
    };

    public static final VoxelShape[] FRIDGE = new VoxelShape[] {
        Block.box(0, 0, 3, 16, 32, 16),
        Block.box(0, 1, 2, 16, 19, 3),
        Block.box(0, 20, 2, 16, 32, 3),
        Block.box(13, 11, 0, 15, 18, 2),
        Block.box(13, 21, 0, 15, 28, 2)
    };

    public static final VoxelShape[] BIG_FRIDGE = new VoxelShape[] {
        Block.box(4, 10, 0, 12, 12, 2),
        Block.box(4, 16, 0, 6, 30, 2),
        Block.box(10, 16, 0, 12, 30, 2),
        Block.box(-4, 14, 2, 20, 32, 3),
        Block.box(-4, 1, 2, 20, 13, 3),
        Block.box(-4, 0, 3, 20, 32, 16)
    };

    public static final VoxelShape[] STOVE = new VoxelShape[] {
        Block.box(0, 0, 3, 16, 14, 16),
        Block.box(0, 1, 2, 16, 14, 3),
        Block.box(3, 8, 0, 13, 9, 2),
    };

    public static final VoxelShape[] KITCHEN_COUNTER = new VoxelShape[] {
        Block.box(0, 0, 3, 16, 16, 16),
        Block.box(0, 16, 14, 16, 18, 16),
        Block.box(0, 14, 2, 16, 16, 3),
        Block.box(1, 3, 2, 15, 13, 3)
    };

    public static final VoxelShape[] KITCHEN_COUNTER_INNER = new VoxelShape[] {
        Block.box(0, 0, 3, 13, 16, 16),
        Block.box(0, 0, 0, 13, 16, 3),
        Block.box(13, 0, 3, 16, 16, 16),
        Block.box(2, 16, 14, 16, 18, 16),
        Block.box(0, 16, 14, 2, 18, 16),
        Block.box(0, 16, 0, 2, 18, 14),
        Block.box(14, 14, 2, 16, 16, 3),
        Block.box(13, 14, 2, 14, 16, 3),
        Block.box(13, 14, 0, 14, 16, 2)
    };

    public static final VoxelShape[] KITCHEN_COUNTER_OUTER = new VoxelShape[] {
        Block.box(0, 0, 3, 13, 14, 16),
        Block.box(13, 3, 4, 14, 13, 15),
        Block.box(1, 3, 2, 12, 13, 3),
        Block.box(0, 14, 2, 14, 16, 16),
        Block.box(0, 16, 14, 2, 18, 16)
    };

    public static final VoxelShape[] SOFA = new VoxelShape[] {
        Block.box(0, 0, 2, 16, 8, 14),
        Block.box(3, 8, 11, 13, 16, 14),
        Block.box(13, 8, 2, 16, 11, 14),
        Block.box(0, 8, 2, 3, 11, 14)
    };

    public static final VoxelShape[] SOFA_RIGHT = new VoxelShape[] {
        Block.box(0, 0, 2, 16, 8, 14),
        Block.box(3, 8, 11, 16, 16, 14),
        Block.box(0, 8, 2, 3, 11, 14)
    };

    public static final VoxelShape[] SOFA_LEFT = new VoxelShape[] {
        Block.box(0, 0, 2, 16, 8, 14),
        Block.box(0, 8, 11, 13, 16, 14),
        Block.box(13, 8, 2, 16, 11, 14)
    };

    public static final VoxelShape[] SOFA_CENTER = new VoxelShape[] {
        Block.box(0, 0, 2, 16, 8, 14),
        Block.box(0, 8, 11, 16, 16, 14)
    };

    public static final VoxelShape[] SOFA_INNER = new VoxelShape[] {
        Block.box(2, 0, 0, 14, 8, 2),
        Block.box(2, 8, 0, 5, 16, 11),
        Block.box(2, 0, 2, 14, 8, 14),
        Block.box(14, 0, 2, 16, 8, 14),
        Block.box(5, 8, 11, 16, 16, 14),
        Block.box(2, 8, 11, 5, 16, 14)
    };

    public static final VoxelShape[] SOFA_OUTER = new VoxelShape[] {
        Block.box(0, 0, 2, 2, 8, 14),
        Block.box(2, 0, 2, 14, 8, 14),
        Block.box(2, 0, 14, 14, 8, 16),
        Block.box(2, 8, 14, 5, 16, 16),
        Block.box(2, 8, 11, 5, 16, 14),
        Block.box(0, 8, 11, 2, 16, 14)
    };

}
