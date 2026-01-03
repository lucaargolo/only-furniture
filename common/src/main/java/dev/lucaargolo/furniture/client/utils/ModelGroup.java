package dev.lucaargolo.furniture.client.utils;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public record ModelGroup(String name, Vec3 origin, List<Either<Integer, ModelGroup>> children) {

    @SuppressWarnings("ConstantValue")
    public static BakedQuad bakeGroupAndAddPivotToQuad(int index, BakedQuad value, BlockElement blockElement, ModelGroup group) {
        if(index != -1 && group != null) {
            if(blockElement.rotation != null) {
                ((FurnitureBakedQuad) value).furniture$setPivot(blockElement.rotation.origin());
            }
            bakeGroupIfPresent((FurnitureBakedQuad) value, index, "", group.children());
        }
        return value;
    }


    private static void bakeGroupIfPresent(FurnitureBakedQuad value, int index, String root, List<Either<Integer, ModelGroup>> children) {
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
                bakeGroupIfPresent(value, index, path, childGroup.get().children());
            }
        }
    }


}
