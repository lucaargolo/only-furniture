package dev.lucaargolo.furniture.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import dev.lucaargolo.furniture.FurnitureMod;
import dev.lucaargolo.furniture.block.FurnitureBlock;
import dev.lucaargolo.furniture.client.model.ModModelManager;
import dev.lucaargolo.furniture.client.render.ModRenderTypeManager;
import dev.lucaargolo.furniture.client.render.ModShaderManager;
import dev.lucaargolo.furniture.item.FurnitureBlockItem;
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
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class FurnitureModClient {

    private static final RandomSource RANDOM = RandomSource.create();

    public static FurnitureModClient INSTANCE;

    private final MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(new ByteBufferBuilder(128));

    private final ModModelManager modelManager = FurnitureMod.INSTANCE.loadPlatformClass(ModModelManager.class);
    private final ModShaderManager shaderManager = FurnitureMod.INSTANCE.loadPlatformClass(ModShaderManager.class);
    private final ModRenderTypeManager renderTypeManager = FurnitureMod.INSTANCE.loadPlatformClass(ModRenderTypeManager.class);

    private float furnitureRotation = 0f;

    public final void init() {
        INSTANCE = this;
        modelManager.init();
        shaderManager.init();
    }

    public final ModModelManager getModelManager() {
        return modelManager;
    }

    public ModShaderManager getShaderManager() {
        return shaderManager;
    }

    public ModRenderTypeManager getRenderTypeManager() {
        return renderTypeManager;
    }

    public float getFurnitureRotation() {
        return furnitureRotation;
    }

    public final boolean onMouseScroll(double deltaX, double deltaY) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if(player != null) {
            Pair<FurnitureBlockItem, InteractionHand> holding = getHoldingFurniture(player);
            if(holding != null) {
                furnitureRotation += Mth.sign(deltaY)*22.5f;
                if(furnitureRotation > 360.0f) {
                    furnitureRotation -= 360.0f;
                }
                if(furnitureRotation < 0f) {
                    furnitureRotation += 360.0f;
                }
                FurnitureMod.INSTANCE.getPacketManager().sendToServer(new FurnitureRotationPayload(furnitureRotation));
                return true;
            }
        }
        return false;
    }

    public final void renderFurniturePreview(PoseStack poseStack) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();

        HitResult hitResult = minecraft.hitResult;
        LocalPlayer player = minecraft.player;
        if(hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK && player != null) {
            Pair<FurnitureBlockItem, InteractionHand> holding = getHoldingFurniture(player);
            if(holding != null) {
                Vec3 pos = getHologramPosition(blockHitResult, player, holding.getSecond());

                poseStack.pushPose();
                poseStack.translate(pos.x-camera.getPosition().x-0.5, pos.y-camera.getPosition().y, pos.z-camera.getPosition().z-0.5);
                poseStack.translate(0.5, 0.0, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(furnitureRotation));
                poseStack.translate(-0.5, 0.0, -0.5);


                FurnitureBlock block = holding.getFirst().getFurnitureBlock();
                BlockState state = block.defaultBlockState();
                BakedModel model = minecraft.getBlockRenderer().getBlockModel(state);

                RenderType renderType = FurnitureModClient.INSTANCE.getRenderTypeManager().hologramTranslucent(InventoryMenu.BLOCK_ATLAS);
                VertexConsumer consumer = bufferSource.getBuffer(renderType);

                int packedLight = LightTexture.FULL_BRIGHT;
                int packedColor = FastColor.ARGB32.color(120, 0x5865f2);

                for (Direction direction : Direction.values()) {
                    RANDOM.setSeed(42L);
                    renderQuadList(poseStack, consumer, model.getQuads(state, direction, RANDOM), packedLight, packedColor);
                }

                RANDOM.setSeed(42L);
                renderQuadList(poseStack, consumer, model.getQuads(state, null, RANDOM), packedLight, packedColor);

                poseStack.popPose();

            }
        }

        bufferSource.endBatch();
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


    private static @NotNull Vec3 getHologramPosition(BlockHitResult blockHitResult, LocalPlayer player, InteractionHand hand) {
        BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(player, hand, blockHitResult));
        BlockPos pos = context.getClickedPos();
        Vec3 location = context.getClickLocation();

        double ox = Math.floor((location.x - pos.getX())*16.0)/16.0;
        double oz = Math.floor((location.z - pos.getZ())*16.0)/16.0;

        return new Vec3(pos.getX()+ox, pos.getY(), pos.getZ()+oz);
    }

    private static void renderQuadList(PoseStack poseStack, VertexConsumer consumer, List<BakedQuad> quads, int packedLight, int packedColor) {
        for (BakedQuad bakedquad : quads) {
            poseStack.pushPose();
            float offset = (RANDOM.nextFloat() - 0.5f)/1000f;
            poseStack.translate(offset, offset, offset);
            consumer.putBulkData(poseStack.last(), bakedquad, FastColor.ARGB32.red(packedColor)/255f, FastColor.ARGB32.green(packedColor)/255f, FastColor.ARGB32.blue(packedColor)/255f, FastColor.ARGB32.alpha(packedColor)/255f, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }

}
