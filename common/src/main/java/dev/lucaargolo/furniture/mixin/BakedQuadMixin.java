package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.client.utils.FurnitureBakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements FurnitureBakedQuad {

    @Unique
    private String furniture$groupHint = "";
    @Unique
    private Vector3f furniture$pivot = new Vector3f();

    @Override
    public String furniture$getGroupName() {
        return this.furniture$groupHint;
    }

    @Override
    public void furniture$setGroupName(String groupName) {
        this.furniture$groupHint = groupName;
    }

    @Override
    public Vector3f furniture$getPivot() {
        return this.furniture$pivot;
    }

    @Override
    public void furniture$setPivot(Vector3f pivot) {
        this.furniture$pivot = pivot;
    }

}
