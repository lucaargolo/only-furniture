package dev.lucaargolo.furniture.item;

import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.client.FurnitureModClient;
import dev.lucaargolo.furniture.data.FurnitureData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
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

    private final FurnitureBlock furnitureBlock;

    public FurnitureBlockItem(FurnitureBlock pBlock, Properties pProperties) {
        super(pBlock, pProperties);
        this.furnitureBlock = pBlock;
    }

    @Override
    protected boolean placeBlock(@NotNull BlockPlaceContext pContext, @NotNull BlockState pState) {
        boolean placed = super.placeBlock(pContext, pState);
        Player player = pContext.getPlayer();
        BlockPos pos = pContext.getClickedPos();
        Vec3 location = pContext.getClickLocation();
        FurnitureData.set(pContext.getLevel(), pos, new FurnitureData((float) (location.x - pos.getX()), (float) (location.z - pos.getZ()), getRotation(player)));
        return placed;
    }

    public FurnitureBlock getFurnitureBlock() {
        return furnitureBlock;
    }

    public static float getRotation(@Nullable Player player) {
        return player != null ? player.level().isClientSide ? getLocalRotation() : rotations.getOrDefault(player.getUUID(), 0f) : 0f;
    }

    private static float getLocalRotation() {
        return FurnitureModClient.INSTANCE.getFurnitureRotation();
    }

    public static void setRotation(ServerPlayer player, float rotation) {
        rotations.put(player.getUUID(), rotation);
    }

}
