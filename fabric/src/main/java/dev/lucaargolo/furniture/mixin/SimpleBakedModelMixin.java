package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.client.utils.RenderTypeHint;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SimpleBakedModel.class)
public class SimpleBakedModelMixin implements RenderTypeHint {

    @Unique
    @Nullable
    private ResourceLocation furniture$renderTypeHint = null;

    @Override
    public @Nullable ResourceLocation furniture$getRenderTypeHint() {
        return this.furniture$renderTypeHint;
    }

    @Override
    public void furniture$setRenderTypeHint(ResourceLocation renderTypeHint) {
        this.furniture$renderTypeHint = renderTypeHint;
    }

}
