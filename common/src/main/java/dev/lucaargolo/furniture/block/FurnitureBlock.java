package dev.lucaargolo.furniture.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.data.FurnitureData;
import dev.lucaargolo.furniture.item.FurnitureBlockItem;
import dev.lucaargolo.furniture.mixin.LevelRendererAccessor;
import dev.lucaargolo.furniture.utils.VoxelShapeUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FurnitureBlock extends Block {

    private static final Map<UUID, Float> rotations = new HashMap<>();
    private final Map<Direction, VoxelShape> shapes;

    public FurnitureBlock(Block base, VoxelShape... shapes) {
        super(BlockBehaviour.Properties.ofFullCopy(base).noOcclusion());
        VoxelShape shape = Shapes.empty();
        for (VoxelShape s : shapes) {
            shape = Shapes.join(shape, s, BooleanOp.OR);
        }
        ImmutableMap.Builder<Direction, VoxelShape> builder = ImmutableMap.builder();
        builder.put(Direction.NORTH, shape);
        builder.put(Direction.EAST, VoxelShapeUtils.rotate(shape, Direction.EAST));
        builder.put(Direction.SOUTH, VoxelShapeUtils.rotate(shape, Direction.SOUTH));
        builder.put(Direction.WEST, VoxelShapeUtils.rotate(shape, Direction.WEST));
        this.shapes = builder.build();
    }

    @Override
    public void setPlacedBy(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @Nullable LivingEntity pPlacer, @NotNull ItemStack pStack) {
        FurnitureData data = FurnitureData.get(pLevel, pPos);
        Direction facing = Direction.fromYRot(data.getRotation() + 180);

        VoxelShape shape = this.shapes.getOrDefault(facing, Shapes.empty());
        Set<BlockPos> intersectingPositions = calculateIntersectingPositions(pPos, shape, Vec3.atLowerCornerOf(pPos).add(data.getX(), 0, data.getZ()));

        Map<BlockPos, Direction> intersectingDirections = calculateIntersectingDirections(pPos, intersectingPositions);
        if(intersectingDirections != null) {
            for(BlockPos intersectingPos: intersectingPositions) {
                if(!pPos.equals(intersectingPos)) {
                    FurnitureData intersectingData = new FurnitureData(data.getX(), data.getZ(), data.getRotation(), intersectingDirections.get(intersectingPos));
                    FurnitureData.set(pLevel, intersectingPos, intersectingData);
                    pLevel.setBlockAndUpdate(intersectingPos, this.defaultBlockState());
                }
            }
        }else{
            //This should technically never be reached since calculateIntersectingDirections is already validated in getStateForPlacement, but we never know
            FurnitureMod.LOG.error("Invalid furniture intersection at {} ({})", pPos, intersectingPositions);
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        Vec3 location = pContext.getClickLocation();
        Player player = pContext.getPlayer();

        FurnitureData data = new FurnitureData((float) (location.x - pos.getX()), (float) (location.z - pos.getZ()), getRotation(player), null);
        Direction facing = Direction.fromYRot(data.getRotation() + 180);

        VoxelShape shape = this.shapes.getOrDefault(facing, Shapes.empty());
        Set<BlockPos> intersectingPositions = calculateIntersectingPositions(pos, shape, Vec3.atLowerCornerOf(pos).add(data.getX(), 0, data.getZ()));

        for(BlockPos intersectingPos: intersectingPositions) {
            BlockState intersectingState = level.getBlockState(intersectingPos);
            if(!intersectingState.canBeReplaced(pContext)) {
                return null;
            }
        }

        if(calculateIntersectingDirections(pos, intersectingPositions) != null) {
            return this.defaultBlockState();
        }else {
            return null;
        }
    }

    @Override
    protected void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pMovedByPiston) {
        if (!pState.is(pNewState.getBlock())) {
            FurnitureData data = FurnitureData.get(pLevel, pPos);
            if(data.getDirectionToOriginal() == null) {
                Set<BlockPos> intersectingPositions = calculateIntersectingPositions(pLevel, pPos);
                for (BlockPos intersectingPos : intersectingPositions) {
                    pLevel.setBlockAndUpdate(intersectingPos, Blocks.AIR.defaultBlockState());
                }
            }else{
                Pair<FurnitureData, Vec3i> pair = FurnitureData.getOriginal(pLevel, pPos);
                BlockPos pos = pPos.offset(pair.getSecond());
                pLevel.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
            FurnitureData.set(pLevel, pPos, FurnitureData.DEFAULT);
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        Pair<FurnitureData, Vec3i> pair = FurnitureData.getOriginal(pLevel, pPos);
        FurnitureData data = pair.getFirst();
        Vec3 toOriginal = Vec3.atLowerCornerOf(pair.getSecond());
        toOriginal = toOriginal.add(data.getX(), 0.0, data.getZ());
        Direction facing = Direction.fromYRot(data.getRotation() + 180);
        return this.shapes.getOrDefault(facing, Shapes.empty()).move(toOriginal.x, toOriginal.y, toOriginal.z);
    }

    public static float getRotation(@Nullable Player player) {
        return player != null ? player.level().isClientSide ? FurnitureBlockItem.getLocalRotation() : rotations.getOrDefault(player.getUUID(), 0f) : 0f;
    }

    public static void setRotation(ServerPlayer player, float rotation) {
        rotations.put(player.getUUID(), rotation);
    }

    private static Set<BlockPos> calculateIntersectingPositions(Level level, BlockPos originalPos) {
        return calculateIntersectingPositions(level, originalPos, new HashSet<>());
    }

    private static Set<BlockPos> calculateIntersectingPositions(Level level, BlockPos originalPos, Set<BlockPos> intersectingPositions) {
        for(Direction direction : Direction.values()) {
            BlockPos relativePos = originalPos.relative(direction);
            if(!intersectingPositions.contains(relativePos)) {
                FurnitureData data = FurnitureData.get(level, relativePos);
                if(data.getDirectionToOriginal() == direction.getOpposite()) {
                    intersectingPositions.add(relativePos);
                    calculateIntersectingPositions(level, relativePos, intersectingPositions);
                }
            }
        }
        return intersectingPositions;
    }

    private static Set<BlockPos> calculateIntersectingPositions(BlockPos originalPos, VoxelShape shape, Vec3 offset) {
        Set<BlockPos> positions = new HashSet<>();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (AABB box : shape.toAabbs()) {
            box = box.deflate(0.001).move(offset);

            for (int x = Mth.floor(box.minX); x < Mth.ceil(box.maxX); x++) {
                for (int y = Mth.floor(box.minY); y < Mth.ceil(box.maxY); y++) {
                    for (int z = Mth.floor(box.minZ); z < Mth.ceil(box.maxZ); z++) {
                        pos.set(x, y, z);
                        if(!pos.equals(originalPos)) {
                            positions.add(pos.immutable());
                        }
                    }
                }
            }
        }

        return positions;
    }

    @Nullable
    public static Map<BlockPos, Direction> calculateIntersectingDirections(BlockPos originalPos, Set<BlockPos> intersectingPositions) {
        Map<BlockPos, Direction> result = new HashMap<>();

        for (BlockPos pos : intersectingPositions) {
            if (pos.equals(originalPos)) {
                result.put(pos, null);
                continue;
            }

            int dx = originalPos.getX() - pos.getX();
            int dz = originalPos.getZ() - pos.getZ();
            int dy = originalPos.getY() - pos.getY();

            Direction directionToOriginal = null;
            if (dx > 0) directionToOriginal = Direction.EAST;
            else if (dx < 0) directionToOriginal = Direction.WEST;
            else if (dz > 0) directionToOriginal = Direction.SOUTH;
            else if (dz < 0) directionToOriginal = Direction.NORTH;
            else if (dy > 0) directionToOriginal = Direction.UP;
            else if (dy < 0) directionToOriginal = Direction.DOWN;

            if(directionToOriginal != null) {
                result.put(pos, directionToOriginal);
            }
        }

        for (BlockPos p : intersectingPositions) {
            if (!result.containsKey(p)) return null;
        }

        return result;
    }

    public static boolean renderFurnitureOutline(LevelRendererAccessor levelRenderer, Camera camera, BlockPos pos, BlockState state, PoseStack poseStack, MultiBufferSource bufferSource) {
        if(state.getBlock() instanceof FurnitureBlock) {
            Pair<FurnitureData, Vec3i> pair = FurnitureData.getOriginal(levelRenderer.getLevel(), pos);
            FurnitureData data = pair.getFirst();
            if(data.getRotation() != 0) {
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

                Vec3 offsetVec = Vec3.atLowerCornerOf(pair.getSecond());
                offsetVec = offsetVec.add(data.getX(), 0.0, data.getZ());
                Vec3 offsetPos = Vec3.atCenterOf(pos).add(offsetVec);

                poseStack.pushPose();
                poseStack.translate(offsetPos.x-camera.getPosition().x, offsetPos.y-camera.getPosition().y, offsetPos.z-camera.getPosition().z);
                Direction facing = Direction.fromYRot(data.getRotation() + 180);
                poseStack.mulPose(Axis.YN.rotationDegrees(facing.toYRot()));
                poseStack.mulPose(Axis.YP.rotationDegrees(data.getRotation()));

                renderHitOutline(levelRenderer, poseStack, consumer, camera.getEntity(), offsetPos.x, offsetPos.y, offsetPos.z, pos, state);
                poseStack.popPose();
                return true;
            }
        }
        return false;
    }

    private static void renderHitOutline(LevelRendererAccessor levelRenderer, PoseStack pPoseStack, VertexConsumer pConsumer, Entity pEntity, double pCamX, double pCamY, double pCamZ, BlockPos pPos, BlockState pState) {
        LevelRendererAccessor.invokeRenderShape(pPoseStack, pConsumer, pState.getShape(levelRenderer.getLevel(), pPos, CollisionContext.of(pEntity)), (double)pPos.getX() - pCamX, (double)pPos.getY() - pCamY, (double)pPos.getZ() - pCamZ, 0.0F, 0.0F, 0.0F, 0.4F);
    }

}
