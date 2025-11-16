package dev.lucaargolo.furniture.item;

import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.FurnitureData;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.mixin.BlockItemAccessor;
import dev.lucaargolo.furniture.network.FurnitureRotationPayload;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FurnitureBlockItem extends BlockItem {

    private static final Map<UUID, Float> rotations = new HashMap<>();
    private static float localRotation = 0f;

    private final FurnitureBlock furnitureBlock;

    public FurnitureBlockItem(FurnitureBlock block, Properties properties) {
        super(block, properties);
        this.furnitureBlock = block;
    }

    public FurnitureBlock getFurnitureBlock() {
        return furnitureBlock;
    }

    @Override
    public @NotNull InteractionResult place(BlockPlaceContext context) {
        if (!this.getBlock().isEnabled(context.getLevel().enabledFeatures())) {
            return InteractionResult.FAIL;
        } else if (!context.canPlace()) {
            return InteractionResult.FAIL;
        } else {
            BlockPlaceContext updatedContext = this.updatePlacementContext(context);
            if (updatedContext == null) {
                return InteractionResult.FAIL;
            } else {
                Pair<BlockState, Integer> pair = this.getPlacementStateAndLayer(updatedContext);
                if (pair.getFirst() == null || pair.getSecond() == -1) {
                    return InteractionResult.FAIL;
                } else if (!this.placeBlock(updatedContext, pair.getFirst(), pair.getSecond())) {
                    return InteractionResult.FAIL;
                } else {
                    BlockPos pos = updatedContext.getClickedPos();
                    Level level = updatedContext.getLevel();
                    Player player = updatedContext.getPlayer();
                    ItemStack stack = updatedContext.getItemInHand();
                    BlockState state = level.getBlockState(pos);
                    if (state.is(pair.getFirst().getBlock())) {
                        state = ((BlockItemAccessor) this).invokeUpdateBlockStateFromTag(pos, level, stack, state);
                        this.updateCustomBlockEntityTag(pos, level, player, stack, state);
                        BlockItemAccessor.invokeUpdateBlockEntityComponents(level, pos, stack);
                        state.getBlock().setPlacedBy(level, pos, state, player, stack);
                        if (player instanceof ServerPlayer) {
                            CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, pos, stack);
                        }
                    }

                    SoundType soundtype = state.getSoundType();
                    level.playSound(player, pos, this.getPlaceSound(state), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, state));
                    stack.consume(1, player);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
    }

    protected boolean placeBlock(BlockPlaceContext context, BlockState state, int layer) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        level.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE);

        if(level.getBlockState(pos) == state) {
            FurnitureData.set(context.getLevel(), context.getClickedPos(), layer, this.furnitureBlock.getFurnitureDataForPlacement(context));
            FurnitureMod.updateBlock(context.getLevel(), context.getClickedPos());
            return true;
        }else {
            return false;
        }
    }

    private Pair<BlockState, Integer> getPlacementStateAndLayer(BlockPlaceContext context) {
        FurnitureData data = this.getFurnitureBlock().getFurnitureDataForPlacement(context);
        Pair<BlockState, Integer> pair = this.getFurnitureBlock().getStateAndLayerForPlacement(context, data);
        return pair.getFirst() != null && this.canPlace(context, pair.getFirst()) ? pair : Pair.of(null, -1);
    }

    public static float getRotation(@Nullable Player player) {
        return player != null ? player.level().isClientSide ? localRotation : rotations.getOrDefault(player.getUUID(), 0f) : 0f;
    }

    public static void setRotation(ServerPlayer player, float rotation) {
        rotations.put(player.getUUID(), rotation);
    }

    public static boolean rotateFurniture(LocalPlayer player, double delta) {
        Pair<FurnitureBlockItem, InteractionHand> holding = getHoldingFurniture(player);
        if(holding != null) {
            if(holding.getFirst().getFurnitureBlock().isWallBlock()) {
                delta *= -1;
            }
            localRotation += Mth.sign(delta)*22.5f;
            if(localRotation >= 360.0f) {
                localRotation -= 360.0f;
            }
            if(localRotation < 0f) {
                localRotation += 360.0f;
            }
            FurnitureMod.getPacketManager().sendToServer(new FurnitureRotationPayload(localRotation));
            return true;
        }else{
            return false;
        }
    }

    public static @Nullable Pair<FurnitureBlockItem, InteractionHand> getHoldingFurniture(LocalPlayer player) {
        ItemStack mainStack = player.getMainHandItem();
        ItemStack offStack = player.getOffhandItem();
        if(mainStack.getItem() instanceof FurnitureBlockItem mainItem) {
            return Pair.of(mainItem, InteractionHand.MAIN_HAND);
        }else if(offStack.getItem() instanceof FurnitureBlockItem offItem) {
            return Pair.of(offItem, InteractionHand.OFF_HAND);
        }else{
            return null;
        }
    }

}
