package dev.lucaargolo.furniture.mixin.sodium;

import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelSlice.class)
public interface LevelSliceAccessor {

    @Accessor
    ClientLevel getLevel();

}
