package dev.lucaargolo.furniture.utils.shape;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.concurrent.atomic.AtomicReference;

public class ShapeUtils {

    public static RotatedShape rotateY(VoxelShape shape, Direction to) {
        return rotateY(shape, Direction.NORTH, to);
    }

    public static RotatedShape rotateY(VoxelShape shape, Direction from, Direction to) {
        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        return rotate(shape, Direction.Axis.Y, times * 90);
    }

    public static RotatedShape rotate(VoxelShape shape, Direction.Axis axis, int angle) {
        boolean counterClockWise = angle < 0;
        angle = Math.abs(angle);
        int times = Math.floorMod(angle / 90, 4);

        VoxelShape rotated = shape;
        for (int i = 0; i < times; i++) {
            AtomicReference<VoxelShape> aux = new AtomicReference<>(Shapes.empty());
            rotated.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                switch (axis) {
                    case X -> {
                        if (counterClockWise) {
                            // Rotating around X means Y/Z swap
                            aux.set(Shapes.or(aux.get(), Shapes.create(minX, minZ, 1 - maxY, maxX, maxZ, 1 - minY)));
                        } else {
                            aux.set(Shapes.or(aux.get(), Shapes.create(minX, 1 - maxZ, minY, maxX, 1 - minZ, maxY)));
                        }
                    }
                    case Y -> {
                        if (counterClockWise) {
                            // Rotating around Y means X/Z swap
                            aux.set(Shapes.or(aux.get(), Shapes.create(minZ, minY, 1 - maxX, maxZ, maxY, 1 - minX)));
                        } else {
                            aux.set(Shapes.or(aux.get(), Shapes.create(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
                        }
                    }
                    case Z -> {
                        if (counterClockWise) {
                            // Rotating around Z means X/Y swap
                            aux.set(Shapes.or(aux.get(), Shapes.create(minY, 1 - maxX, minZ, maxY, 1 - minX, maxZ)));
                        } else {
                            aux.set(Shapes.or(aux.get(), Shapes.create(1 - maxY, minX, minZ, 1 - minY, maxX, maxZ)));
                        }
                    }
                }
            });
            rotated = aux.get();
        }

        return new RotatedShape(rotated, shape);
    }


}

