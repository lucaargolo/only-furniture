package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.client.model.GroupedElementsModel;
import dev.lucaargolo.furniture.client.utils.GroupedBlockModel;
import dev.lucaargolo.furniture.client.utils.ModelGroup;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(UnbakedGeometryHelper.class)
public class UnbakedGeometryHelperMixin {

    @SuppressWarnings("deprecation")
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/BlockModel;getElements()Ljava/util/List;"), method = "bake", cancellable = true)
    private static void furniture$bakeGroupedElementsModel(BlockModel blockModel, ModelBaker modelBaker, BlockModel owner, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, boolean guiLight3d, CallbackInfoReturnable<BakedModel> cir) {
        if(blockModel instanceof GroupedBlockModel groupedBlockModel) {
            ModelGroup group = groupedBlockModel.furniture$getGroup();
            if(group != null) {
                GroupedElementsModel elementsModel = new GroupedElementsModel(blockModel.getElements(), group);
                cir.setReturnValue(elementsModel.bake(blockModel.customData, modelBaker, spriteGetter, modelState, blockModel.getOverrides(modelBaker, owner, spriteGetter)));
            }
        }
    }

}
