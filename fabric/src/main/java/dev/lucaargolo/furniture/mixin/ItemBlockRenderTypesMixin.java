package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.client.utils.RenderTypeProvider;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ConcurrentHashMap;

@Mixin(ItemBlockRenderTypes.class)
public class ItemBlockRenderTypesMixin {

    @Unique
    private static final ConcurrentHashMap<Integer, Integer> furniture$stateRenderTypeCache = new ConcurrentHashMap<>();

    @Inject(at = @At("HEAD"), method = "getChunkRenderType", cancellable = true)
    private static void furniture$getChunkRenderTypeHint(BlockState state, CallbackInfoReturnable<RenderType> cir) {
        int id = Block.getId(state);
        if(id != 0) {
            int ordinal = furniture$stateRenderTypeCache.computeIfAbsent(id, ItemBlockRenderTypesMixin::furniture$computeStateRenderType);
            if (ordinal != -1) {
                cir.setReturnValue(RenderTypeProvider.Type.values()[ordinal].getChunk());
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "getMovingBlockRenderType", cancellable = true)
    private static void furniture$getMovingRenderTypeHint(BlockState state, CallbackInfoReturnable<RenderType> cir) {
        int id = Block.getId(state);
        if(id != 0) {
            int ordinal = furniture$stateRenderTypeCache.computeIfAbsent(id, ItemBlockRenderTypesMixin::furniture$computeStateRenderType);
            if(ordinal != -1) {
                cir.setReturnValue(RenderTypeProvider.Type.values()[ordinal].getMoving());
            }
        }
    }

    @Unique
    private static int furniture$computeStateRenderType(int stateId) {
        BlockState state = Block.stateById(stateId);
        if(!state.isAir()) {
            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
            while (model instanceof ForwardingBakedModel forwarding) {
                model = forwarding.getWrappedModel();
            }
            if (model instanceof RenderTypeProvider hint) {
                RenderTypeProvider.Type type = RenderTypeProvider.getRenderType(hint.furniture$getRenderType());
                if (type != null) {
                    return type.ordinal();
                }
            }
        }
        return -1;
    }

}
