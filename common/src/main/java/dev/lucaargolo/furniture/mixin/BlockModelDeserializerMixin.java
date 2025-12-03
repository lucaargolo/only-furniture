package dev.lucaargolo.furniture.mixin;

import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import dev.lucaargolo.furniture.client.utils.GroupedBlockModel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Mixin(BlockModel.Deserializer.class)
public class BlockModelDeserializerMixin {

    @Inject(at = @At("RETURN"), method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockModel;")
    public void furniture$loadRenderTypeOnDeserialize(JsonElement json, Type type, JsonDeserializationContext context, CallbackInfoReturnable<BlockModel> cir) {
        BlockModel model = cir.getReturnValue();
        JsonObject jsonObject = json.getAsJsonObject();
        if(jsonObject.has("groups")) {
            GroupedBlockModel.Group root = new GroupedBlockModel.Group("root", Vec3.ZERO, furniture$computeGroups(jsonObject.getAsJsonArray("groups")));
            ((GroupedBlockModel) model).furniture$setGroupHint(root);
        }
    }

    @Unique
    private static List<Either<Integer, GroupedBlockModel.Group>> furniture$computeGroups(JsonArray jsonArray) {
        List<Either<Integer, GroupedBlockModel.Group>> list = new ArrayList<>();
        for(JsonElement element : jsonArray) {
            Either<Integer, GroupedBlockModel.Group> either;
            if(element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                JsonPrimitive name = object.getAsJsonPrimitive("name");
                JsonArray origin = object.getAsJsonArray("origin");
                JsonArray children = object.getAsJsonArray("children");

                GroupedBlockModel.Group group = new GroupedBlockModel.Group(
                        name.getAsString(),
                        new Vec3(origin.get(0).getAsDouble(), origin.get(1).getAsDouble(), origin.get(2).getAsDouble()),
                        furniture$computeGroups(children)
                );
                either = Either.right(group);
            }else {
                either = Either.left(element.getAsInt());
            }
            list.add(either);
        }
        return list;
    }

}
