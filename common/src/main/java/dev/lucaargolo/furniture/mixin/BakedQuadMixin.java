package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.client.utils.GroupedBakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements GroupedBakedQuad {

    @Unique
    @Nullable
    private String furniture$groupHint = null;

    @Override
    @Nullable
    public String furniture$getGroupName() {
        return this.furniture$groupHint;
    }

    @Override
    public void furniture$setGroupName(String groupName) {
        this.furniture$groupHint = groupName;
    }

}
