package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.client.utils.GroupedBlockModel;
import dev.lucaargolo.furniture.client.utils.ModelGroup;
import net.minecraft.client.renderer.block.model.BlockModel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockModel.class)
public class BlockModelMixin implements GroupedBlockModel {

    @Shadow @Nullable protected BlockModel parent;
    @Unique
    @Nullable
    private ModelGroup furniture$group = null;

    @Override
    @Nullable
    public ModelGroup furniture$getGroup() {
        return this.furniture$group == null && this.parent != null ? ((GroupedBlockModel) this.parent).furniture$getGroup() : furniture$group;
    }

    @Override
    public void furniture$setGroup(ModelGroup group) {
        this.furniture$group = group;
    }

}
