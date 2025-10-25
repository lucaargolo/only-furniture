package dev.lucaargolo.furniture.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.client.FurnitureModClient;
import dev.lucaargolo.furniture.data.FurnitureData;
import dev.lucaargolo.furniture.network.FurnitureRotationPayload;
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

import java.util.List;

public class FurnitureBlockItem extends BlockItem {

    private static final RandomSource random = RandomSource.create();
    private static float localRotation = 0f;

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
        FurnitureData.set(pContext.getLevel(), pos, 0, new FurnitureData((float) (location.x - pos.getX()), (float) (location.z - pos.getZ()), FurnitureBlock.getRotation(player), null));
        return placed;
    }

    public FurnitureBlock getFurnitureBlock() {
        return furnitureBlock;
    }

    public static float getLocalRotation() {
        return localRotation;
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

                double ox = Math.floor((location.x - blockPos.getX())*16.0)/16.0;
                double oz = Math.floor((location.z - blockPos.getZ())*16.0)/16.0;

                Vec3 pos = new Vec3(blockPos.getX()+ox, blockPos.getY(), blockPos.getZ()+oz);

                poseStack.pushPose();
                poseStack.translate(pos.x-camera.getPosition().x-0.5, pos.y-camera.getPosition().y, pos.z-camera.getPosition().z-0.5);
                poseStack.translate(0.5, 0.0, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(localRotation));
                poseStack.translate(-0.5, 0.0, -0.5);

                FurnitureBlock block = holding.getFirst().getFurnitureBlock();
                BlockState state = block.defaultBlockState();
                BakedModel model = minecraft.getBlockRenderer().getBlockModel(state);

                RenderType renderType = FurnitureModClient.INSTANCE.getRenderTypeManager().hologramTranslucent(InventoryMenu.BLOCK_ATLAS);
                VertexConsumer consumer = bufferSource.getBuffer(renderType);

                int color = !level.getBlockState(blockPos).canBeReplaced(context) || block.getStateForPlacement(context) == null ? 0xda3e44 : 0x5865f2;
                int packedLight = LightTexture.FULL_BRIGHT;
                int packedColor = FastColor.ARGB32.color(120, color);

                for (Direction direction : Direction.values()) {
                    random.setSeed(42L);
                    renderQuadList(poseStack, consumer, model.getQuads(state, direction, random), packedLight, packedColor);
                }

                random.setSeed(42L);
                renderQuadList(poseStack, consumer, model.getQuads(state, null, random), packedLight, packedColor);

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
            poseStack.pushPose();
            float offset = (random.nextFloat() - 0.5f)/1000f;
            poseStack.translate(offset, offset, offset);
            consumer.putBulkData(poseStack.last(), bakedquad, FastColor.ARGB32.red(packedColor)/255f, FastColor.ARGB32.green(packedColor)/255f, FastColor.ARGB32.blue(packedColor)/255f, FastColor.ARGB32.alpha(packedColor)/255f, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }

}
