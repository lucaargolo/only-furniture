package dev.lucaargolo.furniture.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public abstract class ModRenderTypeManager extends RenderType {

    protected ModRenderTypeManager() {
        super("", DefaultVertexFormat.BLIT_SCREEN, VertexFormat.Mode.LINES, 0, false, false, () -> {}, () -> {});
    }

    private final Function<ResourceLocation, RenderType> HOLOGRAM_TRANSLUCENT = Util.memoize(location ->
            createComposite("hologram_translucent", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true,
                    RenderType.CompositeState.builder()
                            .setShaderState(new ShaderStateShard(() -> ModShaderManager.HOLOGRAM_TRANSLUCENT_SHADER))
                            .setTextureState(new RenderStateShard.TextureStateShard(location, false, false))
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setLightmapState(LIGHTMAP)
                            .setOverlayState(OVERLAY)
                            .setCullState(NO_CULL)
                            .createCompositeState(true)
            )
    );

    public final RenderType hologramTranslucent(ResourceLocation id) {
        return HOLOGRAM_TRANSLUCENT.apply(id);
    }

    protected abstract RenderType createComposite(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, CompositeState state);

}
