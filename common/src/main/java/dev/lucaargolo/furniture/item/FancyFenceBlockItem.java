package dev.lucaargolo.furniture.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import dev.lucaargolo.furniture.block.FancyFenceBlock;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.client.FurnitureModClient;
import dev.lucaargolo.furniture.utils.FurnitureData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
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
import java.util.Map;
import java.util.UUID;

public class FancyFenceBlockItem extends FurnitureBlockItem{

    private static final Map<UUID, BlockPos> lastPositions = new HashMap<>();
    private static BlockPos lastLocalPosition = null;

    private final FancyFenceBlock fancyFenceBlock;

    public FancyFenceBlockItem(FancyFenceBlock block, Properties properties) {
        super(block, properties);
        this.fancyFenceBlock = block;
    }

    @Override
    @NotNull
    public InteractionResult useOn(@NotNull UseOnContext context) {
        Player player = context.getPlayer();
        if(player != null && !player.isShiftKeyDown()) {
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

    public FancyFenceBlock getFancyFenceBlock() {
        return fancyFenceBlock;
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

    public static void renderFancyFencePreview(Level level, Camera camera, PoseStack poseStack, MultiBufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        HitResult hitResult = minecraft.hitResult;
        if(player != null && level == player.level() && hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK) {
            Pair<FancyFenceBlockItem, InteractionHand> holding = getHoldingFancyFence(player);
            if(holding != null) {
                BlockPos pos = lastLocalPosition;
                if(pos != null) {
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof FancyFenceBlock) {
                        BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(player, holding.getSecond(), blockHitResult));
                        BlockPos connectedPos = context.getClickedPos();
                        Vec3 connectedLocation = context.getClickLocation();

                        FancyFenceBlock.Connecting connecting = FancyFenceBlock.Connecting.NONE;
                        Vec3i offset = pos.subtract(connectedPos);
                        for(FancyFenceBlock.Connecting c: FancyFenceBlock.Connecting.values()) {
                            if(offset.equals(c.getOffset())) {
                                connecting = c;
                            }
                        }

                        if(connecting != FancyFenceBlock.Connecting.NONE) {
                            FurnitureData data = FurnitureData.get(level, pos, state.getValue(FurnitureBlock.LAYER));

                            boolean snapToGrid = !player.isShiftKeyDown();
                            double ox = 0.0;
                            double oz = 0.0;
                            if(!snapToGrid) {
                                ox = Math.floor((connectedLocation.x - connectedPos.getX())*16.0)/16.0 - 0.5;
                                oz = Math.floor((connectedLocation.z - connectedPos.getZ())*16.0)/16.0 - 0.5;
                            }

                            Vec3 origin = new Vec3(pos.getX() + 0.5 + data.getX(), pos.getY() + 0.5, pos.getZ() + 0.5 + data.getZ());
                            Vec3 destination = new Vec3(connectedPos.getX() + 0.5 + ox, connectedPos.getY() + 0.5, connectedPos.getZ() + 0.5 + oz);

                            Vec3 direction = destination.subtract(origin);
                            direction.normalize();

                            FancyFenceBlock block = holding.getFirst().getFancyFenceBlock();
                            float angle = (float) Math.atan2(direction.x, -direction.z);
                            float distance = (float) origin.distanceTo(destination);
                            float size = block.getSize()/16f;

                            poseStack.pushPose();
                            poseStack.translate(origin.x-camera.getPosition().x-0.5, origin.y-camera.getPosition().y-0.5, origin.z-camera.getPosition().z-0.5);
                            poseStack.translate(0.5, 0.5, 0.5);
                            poseStack.mulPose(Axis.YP.rotation(-angle));
                            poseStack.scale(1f + 0.006f, 1f + 0.006f, 1f + 0.006f);
                            poseStack.translate(-0.5, -0.5, -0.5);

                            BakedModel model = minecraft.getBlockRenderer().getBlockModel(state);
                            RenderType renderType = FurnitureModClient.INSTANCE.getRenderTypeManager().hologramTranslucent(InventoryMenu.BLOCK_ATLAS);
                            VertexConsumer consumer = bufferSource.getBuffer(renderType);

                            int color = block.getStateForPlacement(context) == null || !level.getBlockState(connectedPos).canBeReplaced(context) ? 0xda3e44 : 0x5865f2;
                            int packedColor = FastColor.ARGB32.color(120, color);

                            emitSide(poseStack, consumer, model.getParticleIcon(), Direction.NORTH, (0.5f - size / 2f), 0, (0.5f + size / 2f), 1, 1f - distance - 0.5f, packedColor);
                            emitSide(poseStack, consumer, model.getParticleIcon(), Direction.SOUTH, (0.5f - size / 2f), 0, (0.5f + size / 2f), 1, 0.5f, packedColor);
                            emitSide(poseStack, consumer, model.getParticleIcon(), Direction.EAST, 0.5f, 0, 0.5f + distance, 1, (0.5f - size / 2f), packedColor);
                            emitSide(poseStack, consumer, model.getParticleIcon(), Direction.WEST, 1f - distance - 0.5f, 0, 1f - 0.5f, 1, (0.5f - size / 2f), packedColor);
                            emitSide(poseStack, consumer, model.getParticleIcon(), Direction.UP, (0.5f - size / 2f), 0.5f, (0.5f + size / 2f), distance + 0.5f, 0, packedColor);
                            emitSide(poseStack, consumer, model.getParticleIcon(), Direction.DOWN, (0.5f - size / 2f), 1f - distance - 0.5f, (0.5f + size / 2f), 1f - 0.5f, 0, packedColor);

                            poseStack.popPose();

                        }
                    }
                }
            }
        }
    }

    private static @Nullable Pair<FancyFenceBlockItem, InteractionHand> getHoldingFancyFence(LocalPlayer player) {
        ItemStack mainStack = player.getMainHandItem();
        ItemStack offStack = player.getOffhandItem();
        if(mainStack.getItem() instanceof FancyFenceBlockItem mainItem) {
            return Pair.of(mainItem, InteractionHand.MAIN_HAND);
        }else if(offStack.getItem() instanceof FancyFenceBlockItem offItem) {
            return Pair.of(offItem, InteractionHand.OFF_HAND);
        }else{
            return null;
        }
    }

    private static void emitSide(PoseStack poseStack, VertexConsumer vertexConsumer, TextureAtlasSprite sprite, Direction dir, float x1, float y1, float x2, float y2, float depthOffset, int color) {
        PoseStack.Pose pose = poseStack.last();

        int xCount = (int) Math.ceil(x2 - x1);
        int yCount = (int) Math.ceil(y2 - y1);

        float totalWidth = x2 - x1;
        float totalHeight = y2 - y1;

        float dx = totalWidth / xCount;
        float dy = totalHeight / yCount;

        for (int xi = 0; xi < xCount; xi++) {
            for (int yi = 0; yi < yCount; yi++) {
                float sx1 = x1 + xi * dx;
                float sy1 = y1 + yi * dy;
                float sx2 = sx1 + dx;
                float sy2 = sy1 + dy;

                float u0 = sprite.getU0();
                float u1 = sprite.getU0() + (sprite.getU1()-sprite.getU0())*dx;
                float v0 = sprite.getV0();
                float v1 = sprite.getV0() + (sprite.getV1()-sprite.getV0())*dy;

                emitQuad(pose, vertexConsumer, dir, sx1, sy1, sx2, sy2, depthOffset, u0, u1, v0, v1, color);
            }
        }
    }

    private static void emitQuad(PoseStack.Pose pose, VertexConsumer vertexConsumer, Direction nominalFace, float left, float bottom, float right, float top, float depth, float u0, float u1, float v0, float v1, int color) {
        switch (nominalFace) {
            case UP:
                depth = 1 - depth;
                top = 1 - top;
                bottom = 1 - bottom;

            case DOWN:
                vertexConsumer.addVertex(pose, left, depth, top).setColor(color).setUv(u0, v0).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, left, depth, bottom).setColor(color).setUv(u0, v1).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, right, depth, bottom).setColor(color).setUv(u1, v1).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, right, depth, top).setColor(color).setUv(u1, v0).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                break;

            case EAST:
                depth = 1 - depth;
                left = 1 - left;
                right = 1 - right;

            case WEST:
                vertexConsumer.addVertex(pose, depth, top, left).setColor(color).setUv(u0, v0).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, depth, bottom, left).setColor(color).setUv(u0, v1).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, depth, bottom, right).setColor(color).setUv(u1, v1).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, depth, top, right).setColor(color).setUv(u1, v0).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                break;

            case SOUTH:
                depth = 1 - depth;
                left = 1 - left;
                right = 1 - right;

            case NORTH:
                vertexConsumer.addVertex(pose, 1 - left, top, depth).setColor(color).setUv(u0, v0).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, 1 - left, bottom, depth).setColor(color).setUv(u0, v1).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, 1 - right, bottom, depth).setColor(color).setUv(u1, v1).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                vertexConsumer.addVertex(pose, 1 - right, top, depth).setColor(color).setUv(u1, v0).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, nominalFace.getStepX(), nominalFace.getStepY(), nominalFace.getStepZ());
                break;
        }

    }


}
