package dev.lucaargolo.furniture.mixin;

import com.google.gson.JsonElement;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(BlockModelGenerators.class)
public interface BlockModelGeneratorsAccessor {

    @Accessor
    Consumer<BlockStateGenerator> getBlockStateOutput();

    @Accessor
    BiConsumer<ResourceLocation, Supplier<JsonElement>> getModelOutput();

    @Invoker
    void invokeCreateTrivialBlock(Block block, TextureMapping textureMapping, ModelTemplate modelTemplate);

    @Invoker
    void invokeCreateNonTemplateModelBlock(Block block);

}
