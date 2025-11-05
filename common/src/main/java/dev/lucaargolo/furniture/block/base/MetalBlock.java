package dev.lucaargolo.furniture.block.base;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;

public interface MetalBlock {

    MetalType getMetal();

    WeatheringCopper.WeatherState getAge();

    enum MetalType {
        IRON(Blocks.IRON_BLOCK),
        GOLD(Blocks.GOLD_BLOCK),
        COPPER(Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER);

        private final Block unaffected;
        private final Block exposed;
        private final Block weathered;
        private final Block oxidized;

        MetalType(Block unaffected, Block exposed, Block weathered, Block oxidized) {
            this.unaffected = unaffected;
            this.exposed = exposed;
            this.weathered = weathered;
            this.oxidized = oxidized;
        }

        MetalType(Block base) {
            this(base, base, base, base);
        }


        public Block getBase() {
            return this.unaffected;
        }

        public Block getTexture(WeatheringCopper.WeatherState state) {
            Block block = this.get(state);
            return block == Blocks.IRON_BLOCK ? Blocks.ANVIL : block;
        }

        public Block get(WeatheringCopper.WeatherState state) {
            return switch (state) {
                case UNAFFECTED -> this.unaffected;
                case EXPOSED -> this.exposed;
                case WEATHERED -> this.weathered;
                case OXIDIZED -> this.oxidized;
            };
        }

    }

}
