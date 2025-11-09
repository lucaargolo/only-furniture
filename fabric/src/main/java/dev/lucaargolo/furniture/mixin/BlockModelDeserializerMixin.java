package dev.lucaargolo.furniture.mixin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.lucaargolo.furniture.client.utils.RenderTypeHint;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;

@Mixin(BlockModel.Deserializer.class)
public class BlockModelDeserializerMixin {

    @Inject(at = @At("RETURN"), method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockModel;")
    public void furniture$loadRenderTypeOnDeserialize(JsonElement json, Type type, JsonDeserializationContext context, CallbackInfoReturnable<BlockModel> cir) {
        BlockModel model = cir.getReturnValue();
        JsonObject jsonObject = json.getAsJsonObject();
        if(jsonObject.has("render_type")) {
            String renderType = jsonObject.get("render_type").getAsString();
            ResourceLocation renderTypeHint = ResourceLocation.parse(renderType);
            ((RenderTypeHint) model).furniture$setRenderTypeHint(renderTypeHint);
        }
    }

}
