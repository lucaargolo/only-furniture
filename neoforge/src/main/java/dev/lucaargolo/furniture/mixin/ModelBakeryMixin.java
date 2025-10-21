package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.client.NeoForgeFurnitureModClient;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelBakery.class)
public class ModelBakeryMixin {

    @Inject(at = @At("HEAD"), method = "getModel", cancellable = true)
    public void loadBlockModel(ResourceLocation location, CallbackInfoReturnable<UnbakedModel> cir) {
        UnbakedModel model = NeoForgeFurnitureModClient.INSTANCE.getModelReplacement(location);
        if(model != null) {
            cir.setReturnValue(model);
        }
    }
    
}
