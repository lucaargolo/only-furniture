package dev.lucaargolo.furniture.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.client.FurnitureModClient;
import dev.lucaargolo.furniture.network.FurnitureRotationPayload;
import dev.lucaargolo.furniture.utils.FurnitureData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FurnitureBlockItem extends BlockItem {

    private static final RandomSource random = RandomSource.create();

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

    public static void renderFurniturePreview(Level level, Camera camera, PoseStack poseStack, MultiBufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        HitResult hitResult = minecraft.hitResult;
        if(player != null && level == player.level() && hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK) {
            Pair<FurnitureBlockItem, InteractionHand> holding = getHoldingFurniture(player);
            if(holding != null) {
                BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(player, holding.getSecond(), blockHitResult));
                BlockPos blockPos = context.getClickedPos();
                Vec3 location = context.getClickLocation();

                boolean snapToGrid = !player.isShiftKeyDown();
                double ox, oz;
                if(snapToGrid) {
                    ox = 0.5;
                    oz = 0.5;
                }else{
                    ox = Math.floor((location.x - blockPos.getX())*16.0)/16.0;
                    oz = Math.floor((location.z - blockPos.getZ())*16.0)/16.0;
                }

                Vec3 pos = new Vec3(blockPos.getX()+ox, blockPos.getY(), blockPos.getZ()+oz);

                FurnitureBlock block = holding.getFirst().getFurnitureBlock();

                poseStack.pushPose();
                poseStack.translate(pos.x-camera.getPosition().x-0.5, pos.y-camera.getPosition().y, pos.z-camera.getPosition().z-0.5);
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(Axis.YN.rotationDegrees(localRotation));
                poseStack.scale(1f + 0.005f, 1f + 0.005f, 1f + 0.005f);
                poseStack.translate(-0.5, -0.5, -0.5);

                BlockState state = block.getStateForPlacement(context);
                boolean validPlacement = state != null;
                if(state == null) {
                    state = block.defaultBlockState();
                }

                BakedModel model = minecraft.getBlockRenderer().getBlockModel(state);
                RenderType renderType = FurnitureModClient.INSTANCE.getRenderTypeManager().hologramTranslucent(InventoryMenu.BLOCK_ATLAS);
                VertexConsumer consumer = bufferSource.getBuffer(renderType);

                int color = !validPlacement || !level.getBlockState(blockPos).canBeReplaced(context) ? 0xda3e44 : 0x5865f2;
                int packedColor = FastColor.ARGB32.color(120, color);

                for (Direction direction : Direction.values()) {
                    random.setSeed(42L);
                    renderQuadList(poseStack, consumer, model.getQuads(state, direction, random), LightTexture.FULL_BRIGHT, packedColor);
                }
                random.setSeed(42L);
                renderQuadList(poseStack, consumer, model.getQuads(state, null, random), LightTexture.FULL_BRIGHT, packedColor);

                poseStack.popPose();

            }
        }
    }

    private static @Nullable Pair<FurnitureBlockItem, InteractionHand> getHoldingFurniture(LocalPlayer player) {
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

    private static void renderQuadList(PoseStack poseStack, VertexConsumer consumer, List<BakedQuad> quads, int packedLight, int packedColor) {
        for (BakedQuad bakedquad : quads) {
            consumer.putBulkData(poseStack.last(), bakedquad, FastColor.ARGB32.red(packedColor)/255f, FastColor.ARGB32.green(packedColor)/255f, FastColor.ARGB32.blue(packedColor)/255f, FastColor.ARGB32.alpha(packedColor)/255f, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }

}
