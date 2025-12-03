package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.client.utils.RenderTypeProvider;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SimpleBakedModel.class)
public class SimpleBakedModelMixin implements RenderTypeProvider {

    @Unique
    @Nullable
    private ResourceLocation furniture$renderTypeHint = null;

    @Override
    public @Nullable ResourceLocation furniture$getRenderType() {
        return this.furniture$renderTypeHint;
    }

    @Override
    public void furniture$setRenderType(ResourceLocation renderTypeHint) {
        this.furniture$renderTypeHint = renderTypeHint;
    }

}
