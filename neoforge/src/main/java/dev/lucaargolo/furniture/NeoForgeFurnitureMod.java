package dev.lucaargolo.furniture;


import dev.lucaargolo.furniture.client.FurnitureModClient;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;

@Mod(FurnitureMod.MOD_ID)
public class NeoForgeFurnitureMod extends FurnitureMod {

    private final IEventBus modBus;

    public NeoForgeFurnitureMod(IEventBus modBus) {
        this.modBus = modBus;
        this.modBus.addListener(this::onClientInit);
        NeoForge.EVENT_BUS.addListener(this::onChunkWatch);
        NeoForge.EVENT_BUS.addListener(this::onChunkUnwatch);
        this.init();
    }

    @SubscribeEvent
    public void onClientInit(FMLClientSetupEvent event) {
        this.loadPlatformClass(FurnitureModClient.class);
    }

    @SubscribeEvent
    public void onChunkWatch(ChunkWatchEvent.Watch event) {
        this.onServerChunkWatch(event.getLevel(), event.getPlayer(), event.getPos());
    }

    @SubscribeEvent
    public void onChunkUnwatch(ChunkWatchEvent.UnWatch event) {
        this.onServerChunkUnwatch(event.getLevel(), event.getPlayer(), event.getPos());
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    public String getPlatform() {
        return "NeoForge";
    }

    public static IEventBus getModBus() {
        return ((NeoForgeFurnitureMod) FurnitureMod.INSTANCE).modBus;
    }

}
