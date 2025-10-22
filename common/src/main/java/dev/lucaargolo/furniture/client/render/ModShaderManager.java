package dev.lucaargolo.furniture.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.lucaargolo.furniture.FurnitureMod;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public abstract class ModShaderManager {

    public static ShaderInstance HOLOGRAM_TRANSLUCENT_SHADER;

    public void init() {
        registerShader(FurnitureMod.id("hologram_translucent"), DefaultVertexFormat.NEW_ENTITY, instance -> {
            HOLOGRAM_TRANSLUCENT_SHADER = instance;
        });
    }

    public abstract void registerShader(ResourceLocation location, VertexFormat vertexFormat, Consumer<ShaderInstance> consumer);

}
