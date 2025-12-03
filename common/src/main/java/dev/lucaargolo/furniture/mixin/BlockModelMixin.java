package dev.lucaargolo.furniture.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import dev.lucaargolo.furniture.client.utils.GroupedBakedQuad;
import dev.lucaargolo.furniture.client.utils.GroupedBlockModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;
import java.util.Optional;

@Mixin(BlockModel.class)
public class BlockModelMixin implements GroupedBlockModel {

    @Shadow @Nullable protected BlockModel parent;
    @Shadow @Final private List<BlockElement> elements;

    @Unique
    @Nullable
    private Group furniture$groupHint = null;

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/SimpleBakedModel$Builder;addUnculledFace(Lnet/minecraft/client/renderer/block/model/BakedQuad;)Lnet/minecraft/client/resources/model/SimpleBakedModel$Builder;"), method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Z)Lnet/minecraft/client/resources/model/BakedModel;")
    public BakedQuad furniture$bakeGroupHintForUnculledFace(BakedQuad value, @Local BlockElement blockElement) {
        int index = this.elements.indexOf(blockElement);
        if(index != -1 && this.furniture$groupHint != null) {
            furniture$addGroupHintIfPresent((GroupedBakedQuad) value, index, "", this.furniture$groupHint.children());
        }
        return value;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/SimpleBakedModel$Builder;addCulledFace(Lnet/minecraft/core/Direction;Lnet/minecraft/client/renderer/block/model/BakedQuad;)Lnet/minecraft/client/resources/model/SimpleBakedModel$Builder;"), method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Z)Lnet/minecraft/client/resources/model/BakedModel;")
    public BakedQuad furniture$bakeGroupHintForCulledFace(BakedQuad value, @Local BlockElement blockElement) {
        int index = this.elements.indexOf(blockElement);
        if(index != -1 && this.furniture$groupHint != null) {
            furniture$addGroupHintIfPresent((GroupedBakedQuad) value, index, "", this.furniture$groupHint.children());
        }
        return value;
    }

    @Override
    @Nullable
    public Group furniture$getGroupHint() {
        return this.furniture$groupHint == null && this.parent != null ? ((GroupedBlockModel) this.parent).furniture$getGroupHint() : furniture$groupHint;
    }

    @Override
    public void furniture$setGroupHint(Group groupHint) {
        this.furniture$groupHint = groupHint;
    }

    @Unique
    private void furniture$addGroupHintIfPresent(GroupedBakedQuad value, int index, String root, List<Either<Integer, Group>> children) {
        if(this.furniture$groupHint != null) {
            for (Either<Integer, Group> child : children) {
                Optional<Integer> childIndex = child.left();
                Optional<Group> childGroup = child.right();
                if (childIndex.isPresent() && childIndex.get() == index) {
                    value.furniture$setGroupHint(root);
                } else if (childGroup.isPresent()) {
                    String path;
                    if(root.isEmpty()) {
                        path = childGroup.get().name();
                    }else{
                        path = root + "." + childGroup.get().name();
                    }
                    furniture$addGroupHintIfPresent(value, index, path, childGroup.get().children());
                }
            }
        }
    }
}
