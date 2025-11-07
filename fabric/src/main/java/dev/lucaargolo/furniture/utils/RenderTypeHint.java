package dev.lucaargolo.furniture.utils;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface RenderTypeHint {

    @Nullable
    ResourceLocation furniture$getRenderTypeHint();

    void furniture$setRenderTypeHint(ResourceLocation renderTypeHint);

    @Nullable
    static Type getRenderType(ResourceLocation renderTypeHint) {
        for (Type type : Type.values()) {
            if(type.location.equals(renderTypeHint)) {
                return type;
            }
        }
        return null;
    }

    enum Type {
        SOLID(ResourceLocation.withDefaultNamespace("solid"), RenderType.solid()),
        CUTOUT(ResourceLocation.withDefaultNamespace("cutout"), RenderType.cutout()),
        CUTOUT_MIPPED(ResourceLocation.withDefaultNamespace("cutout_mipped"), RenderType.cutoutMipped()),
        TRANSLUCENT(ResourceLocation.withDefaultNamespace("translucent"), RenderType.translucent(), RenderType.translucentMovingBlock()),
        TRIPWIRE(ResourceLocation.withDefaultNamespace("tripwire"), RenderType.tripwire());

        private final ResourceLocation location;
        private final RenderType chunk;
        private final RenderType moving;

        Type(ResourceLocation location, RenderType chunk) {
            this(location, chunk, chunk);
        }

       Type(ResourceLocation location, RenderType chunk, RenderType moving) {
            this.location = location;
            this.chunk = chunk;
            this.moving = moving;
        }

        public RenderType getChunk() {
            return chunk;
        }

        public RenderType getMoving() {
            return moving;
        }
    }

}
