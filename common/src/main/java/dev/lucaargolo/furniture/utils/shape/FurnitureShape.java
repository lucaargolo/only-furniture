package dev.lucaargolo.furniture.utils.shape;

import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.mixin.VoxelShapeAccessor;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FurnitureShape extends VoxelShape {

    private final int layer;
    private final FurnitureData data;
    private final BlockPos pos;
    private final BlockState state;
    private final Vec3i offset;
    private final VoxelShape shape;

    public FurnitureShape(int layer, FurnitureData data, BlockPos pos, BlockState state, Vec3i offset, VoxelShape shape) {
        super(((VoxelShapeAccessor) shape).getShape());
        this.layer = layer;
        this.data = data;
        this.pos = pos;
        this.state = state;
        this.offset = offset;
        this.shape = shape;
    }

    @Override
    public @NotNull DoubleList getCoords(@NotNull Direction.Axis axis) {
        return shape.getCoords(axis);
    }

    @Override
    public @NotNull VoxelShape move(double xOffset, double yOffset, double zOffset) {
        return new FurnitureShape(layer, data, pos, state, offset, shape.move(xOffset, yOffset, zOffset));
    }

    public int layer() {
        return layer;
    }

    public FurnitureData data() {
        return data;
    }

    public BlockPos pos() {
        return pos;
    }

    public BlockState state() {
        return state;
    }

    public Vec3i offset() {
        return offset;
    }

    public VoxelShape shape() {
        return shape;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FurnitureShape) obj;
        return this.layer == that.layer &&
                Objects.equals(this.data, that.data) &&
                Objects.equals(this.pos, that.pos) &&
                Objects.equals(this.state, that.state) &&
                Objects.equals(this.offset, that.offset) &&
                Objects.equals(this.shape, that.shape);
    }

    @Override
    public int hashCode() {
        return Objects.hash(layer, data, pos, state, offset, shape);
    }

    @Override
    public @NotNull String toString() {
        return this.isEmpty() ? "EMPTY" : "FurnitureShape[" +
                "layer=" + layer + ", " +
                "data=" + data + ", " +
                "pos=" + pos + ", " +
                "state=" + state + ", " +
                "offset=" + offset + ", " +
                "shape=" + shape + ']';
    }

}
