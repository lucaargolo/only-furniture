package dev.lucaargolo.furniture.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.data.FurnitureData;
import dev.lucaargolo.furniture.item.FurnitureBlockItem;
import dev.lucaargolo.furniture.mixin.LevelRendererAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
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

    private final VoxelShape shape;

    public FurnitureBlock(Block base, VoxelShape... shapes) {
        super(BlockBehaviour.Properties.ofFullCopy(base).noOcclusion());
        VoxelShape shape = Shapes.empty();
        for (VoxelShape s : shapes) {
            shape = Shapes.join(shape, s, BooleanOp.OR);
        }
        this.shape = shape;
    }

    @Override
    public void setPlacedBy(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @Nullable LivingEntity pPlacer, @NotNull ItemStack pStack) {
        FurnitureData data = FurnitureData.get(pLevel, pPos);
        AABB bounds = this.shape.bounds().deflate(0.001).move(pPos).move(data.getX(), 0, data.getZ());
        List<BlockPos> intersectingPositions = BlockPos.betweenClosedStream(bounds).filter(p -> !p.equals(pPos)).map(BlockPos::new).toList();
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
        AABB bounds = this.shape.bounds().deflate(0.001).move(pos).move(data.getX(), 0, data.getZ());
        List<BlockPos> intersectingPositions = BlockPos.betweenClosedStream(bounds).filter(p -> !p.equals(pos)).map(BlockPos::new).toList();

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
                List<BlockPos> intersectingPositions = calculateIntersectingPositions(pLevel, pPos);
                for (BlockPos intersectingPos : intersectingPositions) {
                    pLevel.setBlockAndUpdate(intersectingPos, Blocks.AIR.defaultBlockState());
                }
            }else{
                Pair<FurnitureData, Vec3i> pair = getOriginal(pLevel, pPos);
                BlockPos pos = pPos.offset(pair.getSecond());
                pLevel.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
            FurnitureData.set(pLevel, pPos, FurnitureData.DEFAULT);
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        Pair<FurnitureData, Vec3i> pair = getOriginal(pLevel, pPos);
        FurnitureData data = pair.getFirst();
        Vec3 toOriginal = Vec3.atLowerCornerOf(pair.getSecond());
        toOriginal = toOriginal.add(data.getX(), 0.0, data.getZ());
        return shape.move(toOriginal.x, toOriginal.y, toOriginal.z);
    }

    public static float getRotation(@Nullable Player player) {
        return player != null ? player.level().isClientSide ? FurnitureBlockItem.getLocalRotation() : rotations.getOrDefault(player.getUUID(), 0f) : 0f;
    }

    public static void setRotation(ServerPlayer player, float rotation) {
        rotations.put(player.getUUID(), rotation);
    }

    public static List<BlockPos> calculateIntersectingPositions(Level level, BlockPos originalPos) {
        return calculateIntersectingPositions(level, originalPos, new ArrayList<>());
    }

    public static List<BlockPos> calculateIntersectingPositions(Level level, BlockPos originalPos, List<BlockPos> intersectingPositions) {
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

    @Nullable
    public static Map<BlockPos, Direction> calculateIntersectingDirections(BlockPos originalPos, List<BlockPos> intersectingPositions) {
        Map<BlockPos, Direction> intersectingDirections = new HashMap<>();
        Queue<BlockPos> queue = new LinkedList<>();
        Map<BlockPos, Direction> firstMoveMap = new HashMap<>();

        queue.add(originalPos);
        firstMoveMap.put(originalPos, null);

        Set<BlockPos> visited = new HashSet<>();
        visited.add(originalPos);

        while (!queue.isEmpty()) {
            BlockPos cur = queue.poll();
            Direction rootDir = firstMoveMap.get(cur);

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = cur.relative(dir);

                if (!visited.contains(neighbor) && intersectingPositions.contains(neighbor)) {
                    Direction directionTowardOrigin = (rootDir == null) ? dir.getOpposite() : rootDir;

                    firstMoveMap.put(neighbor, directionTowardOrigin);
                    intersectingDirections.put(neighbor, directionTowardOrigin);

                    queue.add(neighbor);
                    visited.add(neighbor);
                }
            }
        }

        for (BlockPos pos : intersectingPositions) {
            if (!intersectingDirections.containsKey(pos)) {
                return null;
            }
        }

        return intersectingDirections;
    }

    public static boolean renderFurnitureOutline(LevelRendererAccessor levelRenderer, Camera camera, BlockPos pos, BlockState state, PoseStack poseStack, MultiBufferSource bufferSource) {
        if(state.getBlock() instanceof FurnitureBlock) {
            Pair<FurnitureData, Vec3i> pair = getOriginal(levelRenderer.getLevel(), pos);
            FurnitureData data = pair.getFirst();
            if(data.getRotation() != 0f) {
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

                Vec3 offsetVec = Vec3.atLowerCornerOf(pair.getSecond());
                offsetVec = offsetVec.add(data.getX(), 0.0, data.getZ());
                Vec3 offsetPos = Vec3.atCenterOf(pos).add(offsetVec);

                poseStack.pushPose();
                poseStack.translate(offsetPos.x-camera.getPosition().x, offsetPos.y-camera.getPosition().y, offsetPos.z-camera.getPosition().z);
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

    private static Pair<FurnitureData, Vec3i> getOriginal(BlockGetter level, BlockPos pos) {
        FurnitureData data = FurnitureData.get(level, pos);
        Vec3i toOriginal = Vec3i.ZERO;
        Set<BlockPos> positions = new HashSet<>();
        while (data.getDirectionToOriginal() != null && !positions.contains(pos)) {
            positions.add(pos);
            Direction direction = data.getDirectionToOriginal();
            pos = pos.relative(direction);
            toOriginal = toOriginal.relative(direction);
            data = FurnitureData.get(level, pos);
        }
        return Pair.of(data, toOriginal);
    }

}
