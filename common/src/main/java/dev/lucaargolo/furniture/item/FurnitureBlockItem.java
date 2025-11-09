package dev.lucaargolo.furniture.item;

import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.network.FurnitureRotationPayload;
import dev.lucaargolo.furniture.utils.FurnitureData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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

    @Override
    protected boolean placeBlock(@NotNull BlockPlaceContext pContext, @NotNull BlockState pState) {
        Player player = pContext.getPlayer();
        BlockPos pos = pContext.getClickedPos();
        Vec3 location = pContext.getClickLocation();

        boolean snapToGrid = player == null || !player.isShiftKeyDown();
        float ox, oz;
        if(snapToGrid) {
            ox = 0.5f;
            oz = 0.5f;
        }else{
            ox = (float) (location.x - pos.getX());
            oz = (float) (location.z - pos.getZ());
        }

        FurnitureData.set(pContext.getLevel(), pos, pState.getValue(FurnitureBlock.LAYER), new FurnitureData(ox, oz, getRotation(player), null, true));
        boolean placed = super.placeBlock(pContext, pState);
        if(!placed) {
            FurnitureData.set(pContext.getLevel(), pos, pState.getValue(FurnitureBlock.LAYER), FurnitureData.DEFAULT);
        }
        return placed;
    }

    public FurnitureBlock getFurnitureBlock() {
        return furnitureBlock;
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
            localRotation += Mth.sign(delta)*22.5f;
            if(localRotation >= 360.0f) {
                localRotation -= 360.0f;
            }
            if(localRotation < 0f) {
                localRotation += 360.0f;
            }
            FurnitureMod.INSTANCE.getPacketManager().sendToServer(new FurnitureRotationPayload(localRotation));
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
