package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.client.utils.RenderTypeHint;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
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

@Mixin(ItemBlockRenderTypes.class)
public class ItemBlockRenderTypesMixin {

    @Unique
    private static final Int2IntMap furniture$stateRenderTypeCache = new Int2IntOpenHashMap();

    @Inject(at = @At("HEAD"), method = "getChunkRenderType", cancellable = true)
    private static void furniture$getChunkRenderTypeHint(BlockState state, CallbackInfoReturnable<RenderType> cir) {
        int ordinal = furniture$stateRenderTypeCache.computeIfAbsent(Block.getId(state), ItemBlockRenderTypesMixin::furniture$computeStateRenderType);
        if(ordinal != -1) {
            cir.setReturnValue(RenderTypeHint.Type.values()[ordinal].getChunk());
        }
    }

    @Inject(at = @At("HEAD"), method = "getMovingBlockRenderType", cancellable = true)
    private static void furniture$getMovingRenderTypeHint(BlockState state, CallbackInfoReturnable<RenderType> cir) {
        int ordinal = furniture$stateRenderTypeCache.computeIfAbsent(Block.getId(state), ItemBlockRenderTypesMixin::furniture$computeStateRenderType);
        if(ordinal != -1) {
            cir.setReturnValue(RenderTypeHint.Type.values()[ordinal].getMoving());
        }
    }

    @Unique
    private static int furniture$computeStateRenderType(int stateId) {
        BlockState state = Block.stateById(stateId);
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        while (model instanceof ForwardingBakedModel forwarding) {
            model = forwarding.getWrappedModel();
        }
        if(model instanceof RenderTypeHint hint) {
            RenderTypeHint.Type type = RenderTypeHint.getRenderType(hint.furniture$getRenderTypeHint());
            if(type != null) {
                return type.ordinal();
            }
        }
        return -1;
    }

}
