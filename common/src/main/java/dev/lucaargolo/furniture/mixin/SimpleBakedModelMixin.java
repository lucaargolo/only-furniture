package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.client.utils.GroupedModel;
import dev.lucaargolo.furniture.client.utils.ModelGroup;
import net.minecraft.client.resources.model.SimpleBakedModel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SimpleBakedModel.class)
public class SimpleBakedModelMixin implements GroupedModel {

    @Unique
    @Nullable
    private ModelGroup furniture$group = null;

    @Override
    public @Nullable ModelGroup furniture$getGroup() {
        return this.furniture$group;
    }

    @Override
    public void furniture$setGroup(ModelGroup group) {
        this.furniture$group = group;
    }
}
