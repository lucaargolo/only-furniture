package dev.lucaargolo.furniture;


import dev.lucaargolo.furniture.client.FurnitureModClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;

@Mod(FurnitureMod.MOD_ID)
public class NeoForgeFurnitureMod extends FurnitureMod {

    private final IEventBus modBus;

    public NeoForgeFurnitureMod(IEventBus modBus) {
        this.modBus = modBus;
        this.modBus.register(this);
        this.init();
    }

    @SubscribeEvent
    public void onClientInit(FMLClientSetupEvent event) {
        this.loadPlatformClass(FurnitureModClient.class);
    }

    @SubscribeEvent
    public void onChunkSent(ChunkWatchEvent.Sent event) {
        this.onChunkPacket(event.getLevel(), event.getPlayer(), event.getPos());
    }

    @Override
    public String getPlatform() {
        return "NeoForge";
    }

    public static IEventBus getModBus() {
        return ((NeoForgeFurnitureMod) FurnitureMod.INSTANCE).modBus;
    }

}
