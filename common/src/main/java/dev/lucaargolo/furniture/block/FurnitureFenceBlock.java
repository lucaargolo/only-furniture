package dev.lucaargolo.furniture.block;

import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.block.base.WoodBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FurnitureFenceBlock extends FurnitureConnectingBlock {

    private final float size;

    public FurnitureFenceBlock(Block base, TagKey<Block> connecting, float size) {
        super(base, new VoxelShape[]{
            Block.box(8.0-(size/2.0), 0, 8.0-(size/2.0), 8.0+(size/2.0), 16, 8.0+(size/2.0))
        }, connecting);
        this.size = size;
    }

    public float getSize() {
        return size;
    }

    @Override
    public VoxelShape getShapeForData(BlockGetter level, BlockPos pos, BlockState state, FurnitureData data) {
        VoxelShape original = super.getShapeForData(level, pos, state, data);
        List<VoxelShape> shapes = new ArrayList<>();
        shapes.add(original);
        List<Vec3i> offsets = this.getType().getOffsets();
        for(Vec3i offset : offsets) {
            BooleanProperty property = this.getType().getProperty(offset);
            BooleanProperty oppositeProperty = this.getType().getProperty(offset.multiply(-1));

            BlockPos connectedPos = pos.offset(offset);
            BlockState connectedState = level.getBlockState(connectedPos);
            if(connectedState.is(this.getConnecting()) && ((property != null && state.getValue(property)) || (oppositeProperty != null && connectedState.getValue(oppositeProperty)))) {
                FurnitureData connectedData = FurnitureData.get(level, connectedPos, state.getValue(FurnitureBlock.LAYER));

                Vector3f origin = new Vector3f(0f, 0f, 0f);
                Vector3f destination = new Vector3f(
                    offset.getX() + connectedData.getX(connectedState) - data.getX(state),
                    offset.getY() + connectedData.getY(connectedState) - data.getY(state),
                    offset.getZ() + connectedData.getZ(connectedState) - data.getZ(state)
                );

                float maxDistance = (8 - size/2f)/16f;
                float fullDistance = origin.distance(destination);
                float distance = Math.min(fullDistance, maxDistance);

                Vector3f direction = new Vector3f(destination);
                if (fullDistance > 0f) {
                    direction.mul(distance);
                }

                float steps = distance / (size/16f);

                for (int i = 0; i <= steps*3; i++) {
                    float t = i / steps;
                    Vector3f point = new Vector3f();
                    origin.lerp(direction, t/3f, point);
                    shapes.add(original.move(point.x, point.y, point.z));
                }
            }
        }
        if(shapes.size() == 1) {
            return shapes.getFirst();
        }else {
            Iterator<VoxelShape> iterator = shapes.iterator();
            VoxelShape shape = iterator.next();
            while (iterator.hasNext()) {
                shape = Shapes.joinUnoptimized(shape, iterator.next(), BooleanOp.OR);
            }
            return shape;
        }
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.FENCE;
    }

    public static class Hedge extends FurnitureFenceBlock implements WoodBlock.LeafBlock {

        private final WoodType wood;

        public Hedge(Block base, TagKey<Block> connecting, WoodType wood, float size) {
            super(base, connecting, size);
            this.wood = wood;
        }

        @Override
        public WoodType getWood() {
            return wood;
        }

    }

}
