package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.client.FurnitureModClient;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"), method = "onScroll", cancellable = true)
    public void furniture$onScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci) {
        if(FurnitureModClient.INSTANCE.onMouseScroll(xOffset, yOffset)) {
            ci.cancel();
        }
    }

}
