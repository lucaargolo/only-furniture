package dev.lucaargolo.furniture.mixin;

import com.google.gson.JsonObject;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net.minecraft.client.renderer.block.model.BlockElement$Deserializer")
public class BlockElementDeserializerMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lorg/joml/Vector3f;x()F", ordinal = 0, remap = false), method = "getTo", locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void getTo(JsonObject pJson, CallbackInfoReturnable<Vector3f> cir, Vector3f vector3f) {
        cir.setReturnValue(vector3f);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lorg/joml/Vector3f;x()F", ordinal = 0, remap = false), method = "getFrom", locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void getFrom(JsonObject pJson, CallbackInfoReturnable<Vector3f> cir, Vector3f vector3f) {
        cir.setReturnValue(vector3f);
    }

}
