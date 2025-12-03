package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.client.utils.RenderTypeProvider;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(BlockModel.class)
public class FabricBlockModelMixin implements RenderTypeProvider {

    @Shadow @Nullable protected BlockModel parent;

    @Unique
    @Nullable
    private ResourceLocation furniture$renderTypeHint = null;

    @Inject(at = @At("RETURN"), method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Z)Lnet/minecraft/client/resources/model/BakedModel;")
    public void furniture$bakeRenderTypeHint(ModelBaker baker, BlockModel model, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state, boolean guiLight3d, CallbackInfoReturnable<BakedModel> cir) {
        if(cir.getReturnValue() instanceof RenderTypeProvider baked) {
            baked.furniture$setRenderType(this.furniture$getRenderType());
        }
    }

    @Unique
    @Override
    @Nullable
    public ResourceLocation furniture$getRenderType() {
        return this.furniture$renderTypeHint == null && this.parent != null ? ((RenderTypeProvider) this.parent).furniture$getRenderType() : furniture$renderTypeHint;
    }

    @Unique
    @Override
    public void furniture$setRenderType(ResourceLocation renderTypeHint) {
        this.furniture$renderTypeHint = renderTypeHint;
    }

}
