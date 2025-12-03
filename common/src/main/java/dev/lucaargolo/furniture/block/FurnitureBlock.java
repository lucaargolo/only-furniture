package dev.lucaargolo.furniture.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.base.LightBlock;
import dev.lucaargolo.furniture.block.behaviour.Behaviour;
import dev.lucaargolo.furniture.block.entity.FurnitureBlockEntity;
import dev.lucaargolo.furniture.block.entity.ModBlockEntityTypes;
import dev.lucaargolo.furniture.item.FurnitureBlockItem;
import dev.lucaargolo.furniture.network.DestroyEffectsPayload;
import dev.lucaargolo.furniture.utils.Rotation;
import dev.lucaargolo.furniture.utils.shape.FurnitureShape;
import dev.lucaargolo.furniture.utils.shape.ShapeUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.IntStream;

public class FurnitureBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    protected final Map<Pair<Direction, Rotation>, VoxelShape> shapes;
    protected final Behaviour<?>[] behaviours;

    public FurnitureBlock(Block base, VoxelShape[] shapes, Behaviour<?>... behaviours) {
        super(furnitureProperties(base));
        this.shapes = computeVoxelShapes(shapes, this.isWallBlock());
        this.behaviours = behaviours;
        BlockState state = this.stateDefinition.any();
        if(this.isWallBlock()) {
            state = state.setValue(FACING, Direction.NORTH);
        }
        this.registerDefaultState(state);
    }

    public FurnitureBlock(Block base, VoxelShape[] shapes) {
        this(base, shapes, new Behaviour[0]);
    }

    @Override
    public void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        if(this.isWallBlock()) {
            builder.add(FACING);
        }
    }

    public boolean isWallBlock() {
        return false;
    }

    public final Behaviour<?>[] getBehaviours() {
        return this.behaviours;
    }

    @SuppressWarnings("unchecked")
    public final <I extends Behaviour<I>> I[] getBehaviours(Class<I> type) {
        return Arrays.stream(this.getBehaviours())
                .filter(type::isInstance)
                .map(type::cast)
                .toArray(size -> (I[]) Array.newInstance(type, size));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        if(Arrays.stream(this.behaviours).anyMatch(Behaviour::isBlockEntityNeeded)) {
            return new FurnitureBlockEntity(pos, state);
        }else {
            return null;
        }
    }

    public boolean shouldRenderBlockEntity(BlockGetter level, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        FurnitureData data = FurnitureData.getOriginal(level, pos);
        if (!FurnitureMod.getInstance().isFakePlayer(player) && level.mayInteract(player, pos) && data.hasOriginal()) {
            ItemStack stack = player.getMainHandItem();
            //This is needed for facilitating putting furnitures on top of each other.
            if(stack.getItem() instanceof FurnitureBlockItem || stack.is(Items.DEBUG_STICK)) {
                return InteractionResult.PASS;
            }

            List<Pair<Integer, Behaviour<?>>> sortedBehaviours = IntStream.range(0, this.behaviours.length).boxed()
                    .map(i -> Pair.<Integer, Behaviour<?>>of(i, computePositionedBehaviour(pos, state, data, this.behaviours[i])))
                    .sorted(Comparator.comparingDouble(i -> i.getSecond().pos().distanceToSqr(hitResult.getLocation())))
                    .toList();

            Optional<FurnitureBlockEntity> optional = level.getBlockEntity(pos, ModBlockEntityTypes.FURNITURE.get());
            for(Pair<Integer, Behaviour<?>> pair : sortedBehaviours) {
                int index = pair.getFirst();
                Behaviour<?> behaviour = pair.getSecond();
                if((optional.isPresent() || !behaviour.isBlockEntityNeeded()) && behaviour.interact(level, pos, state, optional.orElse(null), player, index)) {
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean canBeReplaced(@NotNull BlockState state, @NotNull BlockPlaceContext useContext) {
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
    public final @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext pContext) {
        return getStateAndLayerForPlacement(pContext, getFurnitureDataForPlacement(pContext)).getFirst();
    }

    public final FurnitureData getFurnitureDataForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Vec3 location = context.getClickLocation();
        Player player = context.getPlayer();

        boolean snapToGrid = player == null || !player.isShiftKeyDown();
        float ox = 0.5f;
        float oy = 0.5f;
        float oz = 0.5f;
        if(!snapToGrid) {
            ox = (float) (location.x - pos.getX());
            oy = (float) (location.y - pos.getY());
            oz = (float) (location.z - pos.getZ());
        }

        Direction facing = context.getHorizontalDirection().getOpposite();
        float x = this.isWallBlock() && facing.getAxis() == Direction.Axis.X ? oy : ox;
        float z = this.isWallBlock() && facing.getAxis() == Direction.Axis.Z ? oy : oz;
        return new FurnitureData(x, z, FurnitureBlockItem.getRotation(player), FurnitureData.Type.ORIGINAL);
    }

    public final Pair<BlockState, Integer> getStateAndLayerForPlacement(BlockPlaceContext context, FurnitureData data) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = this.defaultBlockState();
        if(this.isWallBlock()) {
            state = state.setValue(FACING, context.getHorizontalDirection().getOpposite());
        }

        VoxelShape shape = this.getShapeForFurnitureWithOffset(level, pos, state, data, -1);
        List<BlockPos> intersectingPositions = calculateIntersectingPositionsFromShape(pos, shape, Vec3.atLowerCornerOf(pos));
        intersectingPositions.add(pos);

        for(BlockPos intersectingPos: intersectingPositions) {
            BlockState intersectingState = level.getBlockState(intersectingPos);
            if(!intersectingState.canBeReplaced(context)) {
                return Pair.of(null, -1);
            }else if(intersectingState.getBlock() instanceof FurnitureBlock) {
                VoxelShape intersectingShape = intersectingState.getShape(level, intersectingPos).move(
                        intersectingPos.getX()-pos.getX(),
                        intersectingPos.getY()-pos.getY(),
                        intersectingPos.getZ()-pos.getZ()
                );
                boolean collides = Shapes.joinIsNotEmpty(shape, intersectingShape, BooleanOp.AND);
                if(collides) {
                    return Pair.of(null, -1);
                }
            }
        }

        int layer = calculateAvailableLayer(level, pos, intersectingPositions);
        if(layer == -1) {
            return Pair.of(null, -1);
        }

        if(calculateIntersectingDirections(pos, intersectingPositions) != null) {
            return Pair.of(computeStateForData(level, pos, state, data, context), layer);
        }else {
            return Pair.of(null, -1);
        }
    }

    @Override
    public void setPlacedBy(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @Nullable LivingEntity pPlacer, @NotNull ItemStack pStack) {
        Pair<FurnitureData, Integer> pair = FurnitureData.getOriginalAndLayer(pLevel, pPos);
        FurnitureData data = pair.getFirst();
        int layer = pair.getSecond();

        VoxelShape shape = this.getShapeForFurnitureWithOffset(pLevel, pPos, pState, data, layer);
        List<BlockPos> intersectingPositions = calculateIntersectingPositionsFromShape(pPos, shape, Vec3.atLowerCornerOf(pPos));

        Map<BlockPos, Direction> intersectingDirections = Objects.requireNonNull(calculateIntersectingDirections(pPos, intersectingPositions));
        for(BlockPos intersectingPos: intersectingPositions) {
            FurnitureData intersectingData = new FurnitureData(data.x(), data.z(), data.rotation(), intersectingDirections.get(intersectingPos));
            FurnitureData.set(pLevel, intersectingPos, layer, intersectingData);
            BlockState intersectingState = pLevel.getBlockState(intersectingPos);
            if(!(intersectingState.getBlock() instanceof FurnitureBlock)) {
                pLevel.setBlockAndUpdate(intersectingPos, pState);
            }
        }
    }

    @Override
    public void onRemove(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pMovedByPiston) {
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
                this.onRemoveLayer((ServerLevel) pLevel, pPos, pState, originalLayer);
                pLevel.setBlockAndUpdate(pPos, pLevel.getBlockState(pPos.relative(toOriginal)));
            }else {
                for (int layer = 0; layer < 4; layer++) {
                    this.onRemoveLayer((ServerLevel) pLevel, pPos, pState, layer);
                }
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    private void onRemoveLayer(ServerLevel level, BlockPos pos, BlockState state, int layer) {
        FurnitureData data = FurnitureData.get(level, pos, layer);
        if(data.hasOriginal()) {
            FurnitureMod.getPacketManager().sendToPlayersTrackingChunk(level, new ChunkPos(pos), new DestroyEffectsPayload(pos, Block.getId(state), data.getPacked()));
            this.onRemoveOriginalLayer(true, level, pos, state, layer);
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
                    this.onRemoveOriginalLayer(false, level, originalPos, originalState, layer);
                    FurnitureData.set(level, originalPos, layer, FurnitureData.DEFAULT);
                }
            }
        }
        FurnitureData.set(level, pos, layer, FurnitureData.DEFAULT);
    }

    private void onRemoveOriginalLayer(boolean alreadyRemoved, Level level, BlockPos pos, BlockState state, int layer) {
        Optional<FurnitureBlockEntity> optional = level.getBlockEntity(pos, ModBlockEntityTypes.FURNITURE.get());
        Behaviour<?>[] behaviours = this.getBehaviours();
        for(int index = 0; index < behaviours.length; index++) {
            Behaviour<?> behaviour = behaviours[index];
            if(optional.isPresent() || !behaviour.isBlockEntityNeeded()) {
                behaviour.remove(level, pos, state, optional.orElse(null), index);
            }
        }
        List<BlockPos> intersectingPositions = calculateIntersectingPositionsInLevel(level, pos, layer);
        if(!alreadyRemoved) {
            intersectingPositions.add(pos);
        }
        for (BlockPos intersectingPos : intersectingPositions) {
            FurnitureData[] intersectingLayers = FurnitureData.get(level, intersectingPos);
            IntList anotherLayers = new IntArrayList();
            boolean hasOriginal = false;
            for (int intersectingLayer = 0; intersectingLayer < intersectingLayers.length; intersectingLayer++) {
                if(intersectingLayer != layer) {
                    FurnitureData intersectingData = intersectingLayers[intersectingLayer];
                    if(intersectingData.hasOriginal()) {
                        hasOriginal = true;
                    }else if(intersectingData.getDirectionToOriginal() != null) {
                        anotherLayers.add(intersectingLayer);
                    }
                }
            }
            if(!hasOriginal && anotherLayers.isEmpty()) {
                level.setBlockAndUpdate(intersectingPos, Blocks.AIR.defaultBlockState());
                FurnitureData.set(level, intersectingPos, FurnitureData.DEFAULT_LAYERS);
            }else {
                if(!hasOriginal) {
                    int anotherLayer = anotherLayers.getFirst();
                    Pair<FurnitureData, Vec3i> pair = FurnitureData.getOriginal(level, intersectingPos, anotherLayer);
                    BlockPos anotherPos = intersectingPos.offset(pair.getSecond());
                    BlockState anotherState = level.getBlockState(anotherPos);
                    level.setBlockAndUpdate(intersectingPos, anotherState);
                }
                FurnitureData.set(level, intersectingPos, layer, FurnitureData.DEFAULT);
            }
        }
    }

    @Override
    protected final void spawnDestroyParticles(@NotNull Level level, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state) {
        //Since we're rewriting vanilla logic, we need to overwrite this method.
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if(level instanceof Level) {
            FurnitureData.clearShapeCache((Level) level, pos);
        }
        Pair<FurnitureData, Integer> pair = FurnitureData.getOriginalAndLayer(level, pos);
        return computeStateForData(level, pos, state, pair.getFirst(), null);
    }

    protected BlockState computeStateForData(LevelAccessor level, BlockPos pos, BlockState state, FurnitureData data, @Nullable BlockPlaceContext context) {
        return state;
    }

    @Override
    public final @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
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
    protected final @NotNull VoxelShape getCollisionShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return FurnitureData.cachedShape(pLevel, pPos, () -> {
            List<FurnitureShape> shapes = this.getAllShapes(pLevel, pPos);
            if(shapes.isEmpty()) {
                return Shapes.empty();
            }else if(shapes.size() == 1) {
                return shapes.getFirst();
            }else {
                Iterator<FurnitureShape> iterator = shapes.iterator();
                VoxelShape shape = iterator.next().shape();
                while (iterator.hasNext()) {
                    shape = Shapes.joinUnoptimized(shape, iterator.next().shape(), BooleanOp.OR);
                }
                return shape;
            }
        });
    }

    public final FurnitureShape getOriginalShape(BlockGetter level, BlockPos pos, ClipContext context) {
        List<FurnitureShape> shapes = this.getAllShapes(level, pos);
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

    private @NotNull List<FurnitureShape> getAllShapes(@NotNull BlockGetter pLevel, @NotNull BlockPos pPos) {
        List<FurnitureShape> shapes = new ArrayList<>();
        FurnitureData[] layers = FurnitureData.get(pLevel, pPos);
        for(int layer = 0; layer < layers.length; layer++) {
            FurnitureData data = layers[layer];
            if(data.hasOriginal() || data.getDirectionToOriginal() != null) {
                Pair<FurnitureData, Vec3i> pair = FurnitureData.getOriginal(pLevel, pPos, layer);
                FurnitureData originalData = pair.getFirst();
                BlockPos originalPos = pPos.offset(pair.getSecond());
                BlockState originalState = pLevel.getBlockState(originalPos);
                if(originalState.getBlock() instanceof FurnitureBlock originalBlock) {
                    Vec3 toOriginal = Vec3.atLowerCornerOf(pair.getSecond());
                    VoxelShape originalShape = originalBlock.getShapeForFurnitureWithOffset(pLevel, originalPos, originalState, originalData, layer, toOriginal);
                    shapes.add(new FurnitureShape(layer, originalData, originalPos, originalState, pair.getSecond(), originalShape));
                }
            }
        }
        return shapes;
    }

    private VoxelShape getShapeForFurnitureWithOffset(BlockGetter level, BlockPos pos, BlockState state, FurnitureData data, int layer) {
        return this.getShapeForFurnitureWithOffset(level, pos, state, data, layer, Vec3.ZERO);
    }

    @NotNull
    private VoxelShape getShapeForFurnitureWithOffset(BlockGetter level, BlockPos pos, BlockState state, FurnitureData data, int layer, Vec3 offset) {
        offset = offset.add(data.getX(state), data.getY(state), data.getZ(state));
        return this.getShapeForFurniture(level, pos, state, data, layer).move(offset.x, offset.y, offset.z);
    }

    public VoxelShape getShapeForFurniture(BlockGetter level, BlockPos pos, BlockState state, FurnitureData data, int layer) {
        Direction facing = data.getFacing(state);
        Rotation rotation = data.getRotation();
        return this.shapes.get(Pair.of(facing, rotation));
    }

    @Override
    protected final boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType pathComputationType) {
        return false;
    }

    public static BlockBehaviour.Properties furnitureProperties(Block base) {
        return BlockBehaviour.Properties.ofFullCopy(base)
                .lightLevel(state -> state.getBlock() instanceof LightBlock lightBlock ? lightBlock.getLight(state) : 0)
                .pushReaction(PushReaction.BLOCK)
                .noTerrainParticles()
                .randomTicks();
    }

    public static Map<Pair<Direction, Rotation>, VoxelShape> computeVoxelShapes(VoxelShape[] shapes, boolean isWallBlock) {
        VoxelShape northShape = Shapes.empty();
        for (VoxelShape s : shapes) {
            northShape = Shapes.or(northShape, s);
        }
        VoxelShape eastShape = ShapeUtils.rotateY(northShape, Direction.EAST);
        VoxelShape southShape = ShapeUtils.rotateY(northShape, Direction.SOUTH);
        VoxelShape westShape = ShapeUtils.rotateY(northShape, Direction.WEST);
        ImmutableMap.Builder<Pair<Direction, Rotation>, VoxelShape> builder = ImmutableMap.builder();
        if(isWallBlock) {
            for (Rotation rotation : Rotation.values()) {
                builder.put(Pair.of(Direction.NORTH, rotation), ShapeUtils.rotate(northShape, Direction.Axis.Z, -rotation.getAngle()));
                builder.put(Pair.of(Direction.EAST, rotation), ShapeUtils.rotate(eastShape, Direction.Axis.X, rotation.getAngle()));
                builder.put(Pair.of(Direction.SOUTH, rotation), ShapeUtils.rotate(southShape, Direction.Axis.Z, rotation.getAngle()));
                builder.put(Pair.of(Direction.WEST, rotation), ShapeUtils.rotate(westShape, Direction.Axis.X, -rotation.getAngle()));
            }
        }else{
            builder.put(Pair.of(Direction.NORTH, Rotation.R0), northShape);
            builder.put(Pair.of(Direction.EAST, Rotation.R90), eastShape);
            builder.put(Pair.of(Direction.SOUTH, Rotation.R180), southShape);
            builder.put(Pair.of(Direction.WEST, Rotation.R270), westShape);
        }
        return builder.build();
    }

    public static Behaviour<?> computePositionedBehaviour(BlockPos pos, BlockState state, FurnitureData data, Behaviour<?> behaviour) {
        Vec3 position = Vec3.atCenterOf(pos).add(data.getX(state), data.getY(state), data.getZ(state));
        Quaternionf rotation = data.getRotation(state);
        Matrix4f transform = new Matrix4f().rotate(rotation);
        Vector4f offset = new Vector4f(behaviour.pos().toVector3f(), 1f);
        offset.mul(transform);
        return behaviour.positioned(position.add(offset.x, offset.y, offset.z));
    }

    private static List<BlockPos> calculateIntersectingPositionsInLevel(Level level, BlockPos originalPos, int layer) {
        return calculateIntersectingPositionsInLevel(level, originalPos, layer, new ArrayList<>());
    }

    private static List<BlockPos> calculateIntersectingPositionsInLevel(Level level, BlockPos originalPos, int layer, List<BlockPos> intersectingPositions) {
        for(Direction direction : Direction.values()) {
            BlockPos relativePos = originalPos.relative(direction);
            if(!intersectingPositions.contains(relativePos)) {
                FurnitureData data = FurnitureData.get(level, relativePos, layer);
                if(data.getDirectionToOriginal() == direction.getOpposite()) {
                    intersectingPositions.add(relativePos);
                    calculateIntersectingPositionsInLevel(level, relativePos, layer, intersectingPositions);
                }
            }
        }
        return intersectingPositions;
    }

    private static List<BlockPos> calculateIntersectingPositionsFromShape(BlockPos originalPos, VoxelShape shape, Vec3 offset) {
        List<BlockPos> positions = new ArrayList<>();

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
        positions.sort(Comparator.comparingDouble(p -> p.distSqr(originalPos)));

        return positions;
    }

    @Nullable
    private static Map<BlockPos, Direction> calculateIntersectingDirections(BlockPos originalPos, List<BlockPos> intersectingPositions) {
        Map<BlockPos, Direction> result = new HashMap<>();
        result.put(originalPos, null);

        for (BlockPos pos : intersectingPositions) {
            if (pos.equals(originalPos)) {
                continue;
            }

            BlockPos nearestToOriginal = null;
            double minDistance = Double.MAX_VALUE;

            for (BlockPos known : result.keySet()) {
                double dist = pos.distSqr(known);
                if (Math.abs(dist) <= 1 && dist != 0) {
                    if (dist < minDistance) {
                        nearestToOriginal = known;
                    }
                }
            }

            if (nearestToOriginal == null) {
                return null;
            }

            Vec3i v = nearestToOriginal.subtract(pos);
            result.put(pos, Direction.getNearest(v.getX(), v.getY(), v.getZ()));
        }

        for (BlockPos p : intersectingPositions) {
            if (!result.containsKey(p)) return null;
        }

        return result;
    }

    private static int calculateAvailableLayer(Level level, BlockPos pos, List<BlockPos> intersectingPositions) {
        List<BlockPos> allPositions = new ArrayList<>(intersectingPositions);
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
