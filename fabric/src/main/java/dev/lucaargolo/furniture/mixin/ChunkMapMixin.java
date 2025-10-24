package dev.lucaargolo.furniture.mixin;

import dev.lucaargolo.furniture.FurnitureMod;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {

    @Inject(at = @At("TAIL"), method = "markChunkPendingToSend(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/chunk/LevelChunk;)V")
    private static void furniture$watchChunk(ServerPlayer player, LevelChunk chunk, CallbackInfo ci) {
        FurnitureMod.INSTANCE.onServerChunkWatch((ServerLevel) chunk.getLevel(), player, chunk.getPos());
    }

    @Inject(at = @At("HEAD"), method = "dropChunk")
    private static void furniture$unwatchChunk(ServerPlayer player, ChunkPos chunkPos, CallbackInfo ci) {
        //Technically this breaks Immersive Portals compatibility, since we're assuming the server level is the one where the player is.
        FurnitureMod.INSTANCE.onServerChunkUnwatch(player.serverLevel(), player, chunkPos);
    }

}
