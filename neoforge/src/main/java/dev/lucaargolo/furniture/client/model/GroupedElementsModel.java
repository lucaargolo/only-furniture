package dev.lucaargolo.furniture.client.model;

import dev.lucaargolo.furniture.client.utils.ModelGroup;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.IModelBuilder;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.SimpleUnbakedGeometry;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class GroupedElementsModel extends SimpleUnbakedGeometry<GroupedElementsModel> {

    private final List<BlockElement> elements;
    private final ModelGroup modelGroup;

    public GroupedElementsModel(List<BlockElement> elements, ModelGroup modelGroup) {
        this.elements = elements;
        this.modelGroup = modelGroup;
    }

    @Override
    protected void addQuads(@NotNull IGeometryBakingContext context, @NotNull IModelBuilder<?> modelBuilder, @NotNull ModelBaker baker, @NotNull Function<Material, TextureAtlasSprite> spriteGetter, @NotNull ModelState modelState) {
        var rootTransform = context.getRootTransform();
        if (!rootTransform.isIdentity()) {
            modelState = UnbakedGeometryHelper.composeRootTransformIntoModelState(modelState, rootTransform);
        }

        for (int index = 0; index < this.elements.size(); index++) {
            BlockElement element = this.elements.get(index);
            for (Direction direction : element.faces.keySet()) {
                var face = element.faces.get(direction);
                var sprite = spriteGetter.apply(context.getMaterial(face.texture()));
                var quad = BlockModel.bakeFace(element, face, sprite, direction, modelState);
                ModelGroup.bakeGroupAndAddPivotToQuad(index, quad, element, this.modelGroup);

                if (face.cullForDirection() == null)
                    modelBuilder.addUnculledFace(quad);
                else
                    modelBuilder.addCulledFace(modelState.getRotation().rotateTransform(face.cullForDirection()), quad);
            }
        }
    }

}
