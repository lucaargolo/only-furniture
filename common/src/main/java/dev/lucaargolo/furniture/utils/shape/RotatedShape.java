package dev.lucaargolo.furniture.utils.shape;

import dev.lucaargolo.furniture.mixin.VoxelShapeAccessor;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RotatedShape extends VoxelShape {

    private final VoxelShape rotated;
    private final VoxelShape original;

    protected RotatedShape(VoxelShape rotated, VoxelShape original) {
        super(((VoxelShapeAccessor) rotated).getShape());
        this.rotated = rotated;
        this.original = original;
    }

    public VoxelShape getOriginal() {
        return original;
    }

    @Override
    public @NotNull DoubleList getCoords(@NotNull Direction.Axis axis) {
        return rotated.getCoords(axis);
    }

    @Override
    public @NotNull VoxelShape move(double xOffset, double yOffset, double zOffset) {
        return new RotatedShape(rotated.move(xOffset, yOffset, zOffset), original.move(xOffset, yOffset, zOffset));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RotatedShape that = (RotatedShape) o;
        return Objects.equals(rotated, that.rotated) && Objects.equals(original, that.original);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rotated, original);
    }

    @Override
    public @NotNull String toString() {
        return "RotatedShape{" +
                "rotated=" + rotated +
                ", original=" + original +
                '}';
    }
}
