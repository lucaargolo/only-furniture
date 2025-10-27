package dev.lucaargolo.furniture.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.item.FurnitureBlockItem;
import dev.lucaargolo.furniture.mixin.LevelRendererAccessor;
import dev.lucaargolo.furniture.network.DestroyEffectsPayload;
import dev.lucaargolo.furniture.utils.FurnitureData;
import dev.lucaargolo.furniture.utils.FurnitureShape;
import dev.lucaargolo.furniture.utils.VoxelShapeUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FurnitureBlock extends Block {

    public static final IntegerProperty LAYER = IntegerProperty.create("layer", 0, 3);

    private static final Map<UUID, Float> rotations = new HashMap<>();
    private final Map<Direction, VoxelShape> shapes;

    public FurnitureBlock(Block base, VoxelShape... shapes) {
        super(BlockBehaviour.Properties.ofFullCopy(base).dynamicShape().noTerrainParticles());
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

        int layer = findAvailableLayer(level, pos, intersectingPositions);
        if(layer == -1) {
            return null;
        }

        if(calculateIntersectingDirections(pos, intersectingPositions) != null) {
            return this.defaultBlockState().setValue(LAYER, layer);
        }else {
            return null;
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
    protected void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pMovedByPiston) {
        if (!(pNewState.getBlock() instanceof FurnitureBlock)) {
            FurnitureData[] layers = FurnitureData.get(pLevel, pPos);
            int originalLayer = -1;
            Direction toOriginal = null;
            for (int layer = 0; layer < 4; layer++) {
                FurnitureData data = layers[layer];
                originalLayer = originalLayer == -1 && data.hasOriginal() ? layer : originalLayer;
                toOriginal = toOriginal == null && data.getDirectionToOriginal() != null ? data.getDirectionToOriginal() : toOriginal;
            }
            if(originalLayer != -1 && toOriginal != null) {
                onRemoveOriginalLayer(true, pLevel, pPos, pState, originalLayer);
                pLevel.setBlockAndUpdate(pPos, pLevel.getBlockState(pPos.relative(toOriginal)));
            }else {
                for (int layer = 0; layer < 4; layer++) {
                    onRemoveLayer(pLevel, pPos, pState, layer);
                }
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    private static void onRemoveLayer(Level level, BlockPos pos, BlockState state, int layer) {
        FurnitureData data = FurnitureData.get(level, pos, layer);
        if(data.hasOriginal()) {
            onRemoveOriginalLayer(true, level, pos, state, layer);
        }else{
            if(data.getDirectionToOriginal() != null) {
                Pair<FurnitureData, Vec3i> pair = FurnitureData.getOriginal(level, pos, layer);
                BlockPos originalPos = pos.offset(pair.getSecond());
                BlockState originalState = level.getBlockState(originalPos);
                FurnitureData[] originalLayers = FurnitureData.get(level, originalPos);
                boolean hasAnother = false;
                for (int intersectingLayer = 0; intersectingLayer < originalLayers.length; intersectingLayer++) {
                    if(intersectingLayer != layer) {
                        FurnitureData intersectingData = originalLayers[intersectingLayer];
                        hasAnother = hasAnother || intersectingData.hasOriginal() || intersectingData.getDirectionToOriginal() != null;
                    }
                }
                if(!hasAnother) {
                    level.setBlockAndUpdate(originalPos, Blocks.AIR.defaultBlockState());
                }else{
                    onRemoveOriginalLayer(false, level, originalPos, originalState, layer);
                    FurnitureData.set(level, originalPos, layer, FurnitureData.DEFAULT);
                }
            }
        }
        FurnitureData.set(level, pos, layer, FurnitureData.DEFAULT);
    }

    private static void onRemoveOriginalLayer(boolean alreadyRemoved, Level level, BlockPos pos, BlockState state, int layer) {
        if(level instanceof ServerLevel serverLevel) {
            FurnitureMod.INSTANCE.getPacketManager().sendToPlayersTrackingChunk(serverLevel, new ChunkPos(pos), new DestroyEffectsPayload(pos, Block.getId(state), layer));
        }
        Set<BlockPos> intersectingPositions = calculateIntersectingPositions(level, pos, layer);
        if(!alreadyRemoved) {
            intersectingPositions.add(pos);
        }
        for (BlockPos intersectingPos : intersectingPositions) {
            FurnitureData[] intersectingLayers = FurnitureData.get(level, intersectingPos);
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
                level.setBlockAndUpdate(intersectingPos, Blocks.AIR.defaultBlockState());
                FurnitureData.set(level, intersectingPos, FurnitureData.DEFAULT_LAYERS);
            }else{
                int anotherLayer = anotherLayers.getFirst();
                Pair<FurnitureData, Vec3i> pair = FurnitureData.getOriginal(level, intersectingPos, anotherLayer);
                BlockPos anotherPos = intersectingPos.offset(pair.getSecond());
                BlockState anotherState = level.getBlockState(anotherPos);
                level.setBlockAndUpdate(intersectingPos, anotherState);
                FurnitureData.set(level, intersectingPos, layer, FurnitureData.DEFAULT);
            }
        }
    }

    @Override
    protected void spawnDestroyParticles(@NotNull Level level, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state) {
        //Since we're rewriting vanilla logic, we need to overwrite this method.
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if(context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof Player player) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 lookVec = player.getLookAngle();
            Vec3 reachEnd = eyePos.add(lookVec.scale(player.blockInteractionRange()));
            FurnitureShape shape = getOriginalShape(level, pos, new ClipContext(eyePos, reachEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, context));
            if (shape != null) return shape;
        }
        return this.getCollisionShape(state, level, pos, context);
    }


    @Override
    protected @NotNull VoxelShape getCollisionShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        List<FurnitureShape> shapes = getShapes(pLevel, pPos);
        if(shapes.isEmpty()) {
            return this.shapes.get(Direction.NORTH);
        }else{
            return FurnitureData.cachedShape(pLevel, pPos, () -> {
                Iterator<FurnitureShape> iterator = shapes.iterator();
                VoxelShape shape = iterator.next().shape();
                while (iterator.hasNext()) {
                    shape = Shapes.joinUnoptimized(shape, iterator.next().shape(), BooleanOp.OR);
                }
                return shape;
            });
        }
    }

    public FurnitureShape getOriginalShape(BlockGetter level, BlockPos pos, ClipContext context) {
        List<FurnitureShape> shapes = this.getShapes(level, pos);
        if(!shapes.isEmpty()) {
            double closest = Double.MAX_VALUE;
            FurnitureShape best = null;

            for (FurnitureShape shape : shapes) {
                BlockHitResult hit = shape.clip(context.getFrom(), context.getTo(), pos);
                if (hit != null) {
                    double dist = hit.getLocation().distanceToSqr(context.getFrom());
                    if (dist < closest) {
                        closest = dist;
                        best = shape;
                    }
                }
            }

            return best;
        }
        return null;
    }

    public @NotNull List<FurnitureShape> getShapes(@NotNull BlockGetter pLevel, @NotNull BlockPos pPos) {
        List<FurnitureShape> shapes = new ArrayList<>();
        FurnitureData[] layers = FurnitureData.get(pLevel, pPos);
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

                VoxelShape originalShape = originalShapes.getOrDefault(facing, Shapes.empty()).move(toOriginal.x, toOriginal.y, toOriginal.z);
                shapes.add(new FurnitureShape(layer, originalData, originalPos, originalState, pair.getSecond(), originalShape));
            }
        }
        return shapes;
    }

    public boolean renderFurnitureOutline(Level level, Camera camera, BlockPos pos, BlockState state, PoseStack poseStack, MultiBufferSource bufferSource) {
        VoxelShape s = getShape(state, level, pos, CollisionContext.of(camera.getEntity()));
        if(s instanceof FurnitureShape shape) {
            FurnitureData data = shape.data();
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

            Vec3 offsetVec = Vec3.atLowerCornerOf(shape.offset());
            offsetVec = offsetVec.add(data.getX(), 0.0, data.getZ());
            Vec3 offsetPos = Vec3.atCenterOf(pos).add(offsetVec);

            poseStack.pushPose();
            poseStack.translate(offsetPos.x-camera.getPosition().x, offsetPos.y-camera.getPosition().y, offsetPos.z-camera.getPosition().z);
            Direction facing = Direction.fromYRot(data.getRotation() + 180);
            poseStack.mulPose(Axis.YN.rotationDegrees(facing.toYRot()));
            poseStack.mulPose(Axis.YP.rotationDegrees(data.getRotation()));

            LevelRendererAccessor.invokeRenderShape(poseStack, consumer, shape, (double)pos.getX() - offsetPos.x, (double)pos.getY() - offsetPos.y, (double)pos.getZ() - offsetPos.z, 0.0F, 0.0F, 0.0F, 0.4F);
            poseStack.popPose();
            return true;
        }
        return false;
    }

    public static void destroyEffects(ClientLevel level, BlockPos pos, BlockState state, int layer) {
        if(state.getBlock() instanceof FurnitureBlock block) {
            Optional<FurnitureShape> optional = block.getShapes(level, pos).stream().filter(s -> s.layer() == layer).findFirst();
            optional.ifPresent(shape -> {
                SoundType soundType = state.getSoundType();
                level.playLocalSound(pos, soundType.getBreakSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
                shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                    double xSize = Math.min(1.0, maxX - minX);
                    double ySize = Math.min(1.0, maxY - minY);
                    double zSize = Math.min(1.0, maxZ - minZ);

                    int xBounds = Math.max(2, Mth.ceil(xSize / 0.25));
                    int yBounds = Math.max(2, Mth.ceil(ySize / 0.25));
                    int zBounds = Math.max(2, Mth.ceil(zSize / 0.25));

                    for (int x = 0; x < xBounds; x++) {
                        for (int y = 0; y < yBounds; y++) {
                            for (int z = 0; z < zBounds; z++) {
                                double xOffset = ((double)x + 0.5) / (double)xBounds;
                                double yOffset = ((double)y + 0.5) / (double)yBounds;
                                double zOffset = ((double)z + 0.5) / (double)zBounds;
                                double xPos = xOffset * xSize + minX;
                                double yPos = yOffset * ySize + minY;
                                double zPos = zOffset * zSize + minZ;
                                TerrainParticle particle = new TerrainParticle(
                                        level, (double) pos.getX() + xPos, (double) pos.getY() + yPos, (double) pos.getZ() + zPos,
                                        xOffset - 0.5, yOffset - 0.5, zOffset - 0.5, state, pos
                                );
                                Minecraft.getInstance().particleEngine.add(particle);
                            }
                        }
                    }
                });
            });
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
    private static Map<BlockPos, Direction> calculateIntersectingDirections(BlockPos originalPos, Set<BlockPos> intersectingPositions) {
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

    private static int findAvailableLayer(Level level, BlockPos pos, Set<BlockPos> intersectingPositions) {
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

}
