package dev.lucaargolo.furniture.item;

import dev.lucaargolo.furniture.block.FancyFenceBlock;
import dev.lucaargolo.furniture.block.FurnitureConnectingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
    @NotNull
    public InteractionResult useOn(@NotNull UseOnContext context) {
        Player player = context.getPlayer();
        if(this.block.getType().isDependentOnLastPosition() && player != null) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);
            if(state.getBlock() instanceof FancyFenceBlock) {
                if(player instanceof ServerPlayer serverPlayer) {
                    setLastPosition(serverPlayer, pos);
                }else if(level.isClientSide){
                    lastLocalPosition = pos;
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(context);
    }

    @Override
    protected boolean placeBlock(@NotNull BlockPlaceContext pContext, @NotNull BlockState pState) {
        boolean placed = super.placeBlock(pContext, pState);
        if(placed) {
            Player player = pContext.getPlayer();
            Level level = pContext.getLevel();
            BlockPos pos = pContext.getClickedPos();
            if(player instanceof ServerPlayer serverPlayer) {
                setLastPosition(serverPlayer, pos);
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
