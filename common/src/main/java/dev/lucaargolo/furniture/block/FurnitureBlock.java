package dev.lucaargolo.furniture.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.item.FurnitureBlockItem;
import dev.lucaargolo.furniture.mixin.LevelRendererAccessor;
import dev.lucaargolo.furniture.utils.FurnitureData;
import dev.lucaargolo.furniture.utils.VoxelShapeUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
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

    public static final IntegerProperty LAYER = IntegerProperty.create("layer", 0, 3);

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
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        builder.add(LAYER);
    }

    @Override
    protected boolean canBeReplaced(@NotNull BlockState state, @NotNull BlockPlaceContext useContext) {
        Level level = useContext.getLevel();
        BlockPos pos = useContext.getClickedPos();
        ItemStack stack = useContext.getItemInHand();
        Item item = stack.getItem();
        if (item instanceof FurnitureBlockItem) {
            FurnitureData[] layers = FurnitureData.get(level, pos);
            for (FurnitureData data : layers) {
                if(data.hasOriginal()) {
                    return false;
                }
            }
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void setPlacedBy(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @Nullable LivingEntity pPlacer, @NotNull ItemStack pStack) {
        FurnitureData[] layers = FurnitureData.get(pLevel, pPos);
        int layer = -1;
        for(int i = 0; i < layers.length; i++) {
            if(layers[i].hasOriginal()) {
                layer = i;
                break;
            }
        }
        FurnitureData data = layers[layer];
        Direction facing = Direction.fromYRot(data.getRotation() + 180);

        VoxelShape shape = this.shapes.getOrDefault(facing, Shapes.empty());
        Set<BlockPos> intersectingPositions = calculateIntersectingPositions(pPos, shape, Vec3.atLowerCornerOf(pPos).add(data.getX(), 0, data.getZ()));

        Map<BlockPos, Direction> intersectingDirections = calculateIntersectingDirections(pPos, intersectingPositions);
        if(intersectingDirections != null) {
            for(BlockPos intersectingPos: intersectingPositions) {
                if(!pPos.equals(intersectingPos)) {
                    FurnitureData intersectingData = new FurnitureData(data.getX(), data.getZ(), data.getRotation(), intersectingDirections.get(intersectingPos), false);
                    FurnitureData.set(pLevel, intersectingPos, layer, intersectingData);
                    BlockState intersectingState = pLevel.getBlockState(intersectingPos);
                    if(!(intersectingState.getBlock() instanceof FurnitureBlock)) {
                        pLevel.setBlockAndUpdate(intersectingPos, this.defaultBlockState().setValue(LAYER, layer));
                    }
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

        FurnitureData data = new FurnitureData((float) (location.x - pos.getX()), (float) (location.z - pos.getZ()), getRotation(player), null, true);
        Direction facing = Direction.fromYRot(data.getRotation() + 180);

        VoxelShape shape = this.shapes.getOrDefault(facing, Shapes.empty());
        Set<BlockPos> intersectingPositions = calculateIntersectingPositions(pos, shape, Vec3.atLowerCornerOf(pos).add(data.getX(), 0, data.getZ()));

        for(BlockPos intersectingPos: intersectingPositions) {
            BlockState intersectingState = level.getBlockState(intersectingPos);
            if(!intersectingState.canBeReplaced(pContext)) {
                return null;
            }
        }

        int layer = calculateAvailableLayer(level, pos, intersectingPositions);
        if(layer == -1) {
            return null;
        }

        if(calculateIntersectingDirections(pos, intersectingPositions) != null) {
            return this.defaultBlockState().setValue(LAYER, layer);
        }else {
            return null;
        }
    }

    private int calculateAvailableLayer(Level level, BlockPos pos, Set<BlockPos> intersectingPositions) {
        Set<BlockPos> allPositions = new HashSet<>(intersectingPositions);
        allPositions.add(pos);

        IntSet commonLayers = null;

        for (BlockPos currentPos : allPositions) {
            FurnitureData[] layers = FurnitureData.get(level, currentPos);
            IntSet availableLayers = new IntArraySet();

            for (int i = layers.length - 1; i >= 0; i--) {
                if (!layers[i].hasOriginal() && layers[i].getDirectionToOriginal() == null) {
                    availableLayers.add(i);
                }
            }

            if (commonLayers == null) {
                commonLayers = availableLayers;
            } else {
                commonLayers.retainAll(availableLayers);
            }

            if (commonLayers.isEmpty()) {
                return -1;
            }
        }

        return commonLayers.intStream().min().orElse(-1);
    }

    @Override
    protected void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pMovedByPiston) {
        if (!(pNewState.getBlock() instanceof FurnitureBlock)) {
            FurnitureData[] layers = FurnitureData.get(pLevel, pPos);
            for (int layer = 0; layer < layers.length; layer++) {
                FurnitureData data = layers[layer];
                if(data.hasOriginal()) {
                    onRemoveOriginal(true, pLevel, pPos, layer);
                }else{
                    //TODO: Theoretically we need to check which layer was hit but that logic will be done somewhere else.
                    if(data.getDirectionToOriginal() != null) {
                        Pair<FurnitureData, Vec3i> pair = FurnitureData.getOriginal(pLevel, pPos, layer);
                        BlockPos originalPos = pPos.offset(pair.getSecond());
                        FurnitureData[] originalLayers = FurnitureData.get(pLevel, originalPos);
                        boolean hasAnother = false;
                        for (int intersectingLayer = 0; intersectingLayer < originalLayers.length; intersectingLayer++) {
                            if(intersectingLayer != layer) {
                                FurnitureData intersectingData = originalLayers[intersectingLayer];
                                hasAnother = hasAnother || intersectingData.hasOriginal() || intersectingData.getDirectionToOriginal() != null;
                            }
                        }
                        if(!hasAnother) {
                            pLevel.setBlockAndUpdate(originalPos, Blocks.AIR.defaultBlockState());
                        }else{
                            onRemoveOriginal(false, pLevel, originalPos, layer);
                            FurnitureData.set(pLevel, originalPos, layer, FurnitureData.DEFAULT);
                        }
                    }
                }
            }
            FurnitureData.set(pLevel, pPos, FurnitureData.DEFAULT_LAYERS);
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    private static void onRemoveOriginal(boolean originalRemoved, @NotNull Level pLevel, @NotNull BlockPos originalPos, int layer) {
        Set<BlockPos> intersectingPositions = calculateIntersectingPositions(pLevel, originalPos, layer);
        if(!originalRemoved) {
            intersectingPositions.add(originalPos);
        }
        for (BlockPos intersectingPos : intersectingPositions) {
            FurnitureData[] intersectingLayers = FurnitureData.get(pLevel, intersectingPos);
            IntList anotherLayers = new IntArrayList();
            for (int intersectingLayer = 0; intersectingLayer < intersectingLayers.length; intersectingLayer++) {
                if(intersectingLayer != layer) {
                    FurnitureData intersectingData = intersectingLayers[intersectingLayer];
                    if(intersectingData.hasOriginal() || intersectingData.getDirectionToOriginal() != null) {
                        anotherLayers.add(intersectingLayer);
                    }
                }
            }
            if(anotherLayers.isEmpty()) {
                pLevel.setBlockAndUpdate(intersectingPos, Blocks.AIR.defaultBlockState());
                FurnitureData.set(pLevel, intersectingPos, FurnitureData.DEFAULT_LAYERS);
            }else{
                if(!originalRemoved) {
                    int anotherLayer = anotherLayers.getFirst();
                    Pair<FurnitureData, Vec3i> pair = FurnitureData.getOriginal(pLevel, intersectingPos, anotherLayer);
                    BlockPos anotherPos = intersectingPos.offset(pair.getSecond());
                    BlockState anotherState = pLevel.getBlockState(anotherPos);
                    pLevel.setBlockAndUpdate(originalPos, anotherState);
                }
                FurnitureData.set(pLevel, intersectingPos, layer, FurnitureData.DEFAULT);
            }
        }
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        FurnitureData[] layers = FurnitureData.get(pLevel, pPos);
        List<VoxelShape> shapes = new ArrayList<>();
        for(int layer = 0; layer < layers.length; layer++) {
            FurnitureData data = layers[layer];
            if(data.hasOriginal() || data.getDirectionToOriginal() != null) {
                Pair<FurnitureData, Vec3i> pair = FurnitureData.getOriginal(pLevel, pPos, layer);
                FurnitureData originalData = pair.getFirst();
                BlockPos originalPos = pPos.offset(pair.getSecond());
                BlockState originalState = pLevel.getBlockState(originalPos);
                Map<Direction, VoxelShape> originalShapes = this.shapes;
                if(originalState.getBlock() instanceof FurnitureBlock originalBlock) {
                    originalShapes = originalBlock.shapes;
                }

                Vec3 toOriginal = Vec3.atLowerCornerOf(pair.getSecond());
                toOriginal = toOriginal.add(originalData.getX(), 0.0, originalData.getZ());
                Direction facing = Direction.fromYRot(originalData.getRotation() + 180);

                shapes.add(originalShapes.getOrDefault(facing, Shapes.empty()).move(toOriginal.x, toOriginal.y, toOriginal.z));
            }
        }
        if(shapes.isEmpty()) {
            return this.shapes.get(Direction.NORTH);
        }else{
            VoxelShape shape = shapes.removeFirst();
            while (!shapes.isEmpty()) {
                shape = Shapes.joinUnoptimized(shape, shapes.removeFirst(), BooleanOp.OR);
            }
            return shape;
        }
    }

    public static float getRotation(@Nullable Player player) {
        return player != null ? player.level().isClientSide ? FurnitureBlockItem.getLocalRotation() : rotations.getOrDefault(player.getUUID(), 0f) : 0f;
    }

    public static void setRotation(ServerPlayer player, float rotation) {
        rotations.put(player.getUUID(), rotation);
    }

    private static Set<BlockPos> calculateIntersectingPositions(Level level, BlockPos originalPos, int layer) {
        return calculateIntersectingPositions(level, originalPos, layer, new HashSet<>());
    }

    private static Set<BlockPos> calculateIntersectingPositions(Level level, BlockPos originalPos, int layer, Set<BlockPos> intersectingPositions) {
        for(Direction direction : Direction.values()) {
            BlockPos relativePos = originalPos.relative(direction);
            if(!intersectingPositions.contains(relativePos)) {
                FurnitureData data = FurnitureData.get(level, relativePos, layer);
                if(data.getDirectionToOriginal() == direction.getOpposite()) {
                    intersectingPositions.add(relativePos);
                    calculateIntersectingPositions(level, relativePos, layer, intersectingPositions);
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
//        if(state.getBlock() instanceof FurnitureBlock) {
//            //TODO: Socorro
//            Pair<FurnitureData, Vec3i> pair = FurnitureData.getOriginal(levelRenderer.getLevel(), pos, 0);
//            FurnitureData data = pair.getFirst();
//            if(data.getRotation() != 0) {
//                VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
//
//                Vec3 offsetVec = Vec3.atLowerCornerOf(pair.getSecond());
//                offsetVec = offsetVec.add(data.getX(), 0.0, data.getZ());
//                Vec3 offsetPos = Vec3.atCenterOf(pos).add(offsetVec);
//
//                poseStack.pushPose();
//                poseStack.translate(offsetPos.x-camera.getPosition().x, offsetPos.y-camera.getPosition().y, offsetPos.z-camera.getPosition().z);
//                Direction facing = Direction.fromYRot(data.getRotation() + 180);
//                poseStack.mulPose(Axis.YN.rotationDegrees(facing.toYRot()));
//                poseStack.mulPose(Axis.YP.rotationDegrees(data.getRotation()));
//
//                renderHitOutline(levelRenderer, poseStack, consumer, camera.getEntity(), offsetPos.x, offsetPos.y, offsetPos.z, pos, state);
//                poseStack.popPose();
//                return true;
//            }
//        }
        return false;
    }

    private static void renderHitOutline(LevelRendererAccessor levelRenderer, PoseStack pPoseStack, VertexConsumer pConsumer, Entity pEntity, double pCamX, double pCamY, double pCamZ, BlockPos pPos, BlockState pState) {
        LevelRendererAccessor.invokeRenderShape(pPoseStack, pConsumer, pState.getShape(levelRenderer.getLevel(), pPos, CollisionContext.of(pEntity)), (double)pPos.getX() - pCamX, (double)pPos.getY() - pCamY, (double)pPos.getZ() - pCamZ, 0.0F, 0.0F, 0.0F, 0.4F);
    }

}
