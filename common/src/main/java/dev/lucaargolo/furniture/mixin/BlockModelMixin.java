package dev.lucaargolo.furniture.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import dev.lucaargolo.furniture.client.utils.FurnitureBakedQuad;
import dev.lucaargolo.furniture.client.utils.GroupedBlockModel;
import dev.lucaargolo.furniture.client.utils.ModelGroup;
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
    private ModelGroup furniture$group = null;

    @SuppressWarnings("ConstantValue")
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/SimpleBakedModel$Builder;addUnculledFace(Lnet/minecraft/client/renderer/block/model/BakedQuad;)Lnet/minecraft/client/resources/model/SimpleBakedModel$Builder;"), method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Z)Lnet/minecraft/client/resources/model/BakedModel;")
    public BakedQuad furniture$bakeGroupAndAddPivotForUnculledFace(BakedQuad value, @Local BlockElement blockElement) {
        int index = this.elements.indexOf(blockElement);
        if(index != -1 && this.furniture$group != null) {
            if(blockElement.rotation != null) {
                ((FurnitureBakedQuad) value).furniture$setPivot(blockElement.rotation.origin());
            }
            furniture$addGroupIfPresent((FurnitureBakedQuad) value, index, "", this.furniture$group.children());
        }
        return value;
    }

    @SuppressWarnings("ConstantValue")
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/SimpleBakedModel$Builder;addCulledFace(Lnet/minecraft/core/Direction;Lnet/minecraft/client/renderer/block/model/BakedQuad;)Lnet/minecraft/client/resources/model/SimpleBakedModel$Builder;"), method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Z)Lnet/minecraft/client/resources/model/BakedModel;")
    public BakedQuad furniture$bakeGroupAndAddPivotForCulledFace(BakedQuad value, @Local BlockElement blockElement) {
        int index = this.elements.indexOf(blockElement);
        if(index != -1 && this.furniture$group != null) {
            if(blockElement.rotation != null) {
                ((FurnitureBakedQuad) value).furniture$setPivot(blockElement.rotation.origin());
            }
            furniture$addGroupIfPresent((FurnitureBakedQuad) value, index, "", this.furniture$group.children());
        }
        return value;
    }

    @Override
    @Nullable
    public ModelGroup furniture$getGroup() {
        return this.furniture$group == null && this.parent != null ? ((GroupedBlockModel) this.parent).furniture$getGroup() : furniture$group;
    }

    @Override
    public void furniture$setGroup(ModelGroup group) {
        this.furniture$group = group;
    }

    @Unique
    private void furniture$addGroupIfPresent(FurnitureBakedQuad value, int index, String root, List<Either<Integer, ModelGroup>> children) {
        if(this.furniture$group != null) {
            for (Either<Integer, ModelGroup> child : children) {
                Optional<Integer> childIndex = child.left();
                Optional<ModelGroup> childGroup = child.right();
                if (childIndex.isPresent() && childIndex.get() == index) {
                    value.furniture$setGroupName(root);
                } else if (childGroup.isPresent()) {
                    String path;
                    if(root.isEmpty()) {
                        path = childGroup.get().name();
                    }else{
                        path = root + "." + childGroup.get().name();
                    }
                    furniture$addGroupIfPresent(value, index, path, childGroup.get().children());
                }
            }
        }
    }
}
