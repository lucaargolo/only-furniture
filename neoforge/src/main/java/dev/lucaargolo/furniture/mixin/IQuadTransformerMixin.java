package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.client.utils.FurnitureBakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IQuadTransformer.class)
public interface IQuadTransformerMixin {

    @Inject(at = @At("RETURN"), method = "copy")
    private static void furniture$copyGroupAndPivot(BakedQuad quad, CallbackInfoReturnable<BakedQuad> cir) {
        if(quad instanceof FurnitureBakedQuad original && cir.getReturnValue() instanceof FurnitureBakedQuad copy) {
            copy.furniture$setGroupName(original.furniture$getGroupName());
            copy.furniture$setPivot(original.furniture$getPivot());
        }
    }

}
