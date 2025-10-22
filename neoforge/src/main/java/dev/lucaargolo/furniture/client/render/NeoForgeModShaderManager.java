package dev.lucaargolo.furniture.client.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import dev.lucaargolo.furniture.NeoForgeFurnitureMod;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class NeoForgeModShaderManager extends ModShaderManager {

    private final Map<ResourceLocation, Pair<VertexFormat, Consumer<ShaderInstance>>> toRegister = new HashMap<>();

    @Override
    public void init() {
        super.init();
        NeoForgeFurnitureMod.getModBus().register(this);
    }

    @Override
    public void registerShader(ResourceLocation location, VertexFormat vertexFormat, Consumer<ShaderInstance> consumer) {
        toRegister.put(location, Pair.of(vertexFormat, consumer));
    }

    @SubscribeEvent
    public void registerShaders(RegisterShadersEvent event) throws IOException {
        for (Map.Entry<ResourceLocation, Pair<VertexFormat, Consumer<ShaderInstance>>> entry : toRegister.entrySet()) {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), entry.getKey(), entry.getValue().getLeft()), entry.getValue().getRight());
        }
    }

}
