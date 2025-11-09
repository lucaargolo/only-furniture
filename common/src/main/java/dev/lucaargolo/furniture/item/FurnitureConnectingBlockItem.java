package dev.lucaargolo.furniture.item;

import dev.lucaargolo.furniture.block.FurnitureConnectingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FurnitureConnectingBlockItem extends FurnitureBlockItem{

    private static final Map<UUID, BlockPos> lastPositions = new HashMap<>();
    private static BlockPos lastLocalPosition = null;

    private final FurnitureConnectingBlock block;

    public FurnitureConnectingBlockItem(FurnitureConnectingBlock block, Properties properties) {
        super(block, properties);
        this.block = block;
    }

    @Override
    public FurnitureConnectingBlock getFurnitureBlock() {
        return this.block;
    }

    @Override
    @NotNull
    public InteractionResult useOn(@NotNull UseOnContext context) {
        if(this.block.getType().isDependentOnLastPosition()) {
            Level level = context.getLevel();
            BlockPos clickedPos = context.getClickedPos();
            BlockState clickedState = level.getBlockState(clickedPos);
            Player player = context.getPlayer();
            if(player != null && !player.isShiftKeyDown() && clickedState.is(this.block.getConnecting())) {
                BlockPos lastPosition = FurnitureConnectingBlockItem.getLastPosition(player);
                BooleanProperty propertyToConnect = lastPosition != null ? manuallyConnectNeighbors(level, lastPosition, clickedPos, clickedState) : null;
                if(propertyToConnect != null) {
                    BlockState lastState = level.getBlockState(lastPosition);
                    level.setBlockAndUpdate(lastPosition, lastState.cycle(propertyToConnect));
                }else {
                    if (player instanceof ServerPlayer serverPlayer) {
                        FurnitureConnectingBlockItem.setLastPosition(serverPlayer, clickedPos);
                    } else if (level.isClientSide) {
                        lastLocalPosition = clickedPos;
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(context);
    }

    @Nullable
    public BooleanProperty manuallyConnectNeighbors(Level level, BlockPos lastPosition, BlockPos clickedPos, BlockState clickedState) {
        BlockState lastState = level.getBlockState(lastPosition);
        if(clickedState.is(this.block.getConnecting()) && lastState.is(this.block.getConnecting())) {
            BooleanProperty clickedProperty = this.block.getType().getProperty(lastPosition.subtract(clickedPos));
            BooleanProperty lastProperty = this.block.getType().getProperty(clickedPos.subtract(lastPosition));
            if (clickedProperty != null && lastProperty != null) {
                boolean clicked = clickedState.getValue(clickedProperty);
                boolean last = lastState.getValue(lastProperty);
                if(clicked == last) {
                    return lastProperty;
                }
            }
        }
        return null;
    }

    @Override
    protected boolean placeBlock(@NotNull BlockPlaceContext pContext, @NotNull BlockState pState) {
        boolean placed = super.placeBlock(pContext, pState);
        if(placed) {
            Player player = pContext.getPlayer();
            Level level = pContext.getLevel();
            BlockPos pos = pContext.getClickedPos();
            if(player instanceof ServerPlayer serverPlayer) {
                FurnitureConnectingBlockItem.setLastPosition(serverPlayer, pos);
            }else if(level.isClientSide){
                lastLocalPosition = pos;
            }
        }
        return placed;
    }

    @Nullable
    public static BlockPos getLastPosition(@Nullable Player player) {
        return player != null ? player.level().isClientSide ? lastLocalPosition : lastPositions.get(player.getUUID()) : null;
    }

    public static void setLastPosition(ServerPlayer player, @Nullable BlockPos position) {
        if(position == null) {
            lastPositions.remove(player.getUUID());
        }else{
            lastPositions.put(player.getUUID(), position);
        }
    }

}
