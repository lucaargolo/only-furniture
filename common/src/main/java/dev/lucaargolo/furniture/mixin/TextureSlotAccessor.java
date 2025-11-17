package dev.lucaargolo.furniture.mixin;

import net.minecraft.data.models.model.TextureSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TextureSlot.class)
public interface TextureSlotAccessor {

    @Invoker
    static TextureSlot invokeCreate(String id) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

}
