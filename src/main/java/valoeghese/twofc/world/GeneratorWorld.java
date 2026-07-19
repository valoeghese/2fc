package valoeghese.twofc.world;

import valoeghese.twofc.util.noise.Noise;
import valoeghese.twofc.world.chunk.Chunk;
import valoeghese.twofc.world.chunk.OverflowChunk;
import valoeghese.twofc.world.chunk.TileWriter;
import valoeghese.twofc.world.kingdom.Kingdom;
import valoeghese.twofc.world.tile.Tile;

import java.util.Random;
import java.util.function.Predicate;

/**
 * Read inside the generating chunk, and generate in a buffer outside the generating chunk.
 */
public class GeneratorWorld implements WorldComponent {
    public GeneratorWorld(Chunk chunk) {
        this.world = chunk.getGameplayWorld();
        this.noise = new Noise(new Random(world.getSeed()));

        this.chunk = chunk;
        final int x = chunk.x;
        final int z = chunk.z;

        this.buffer = new TileWriter[][] {
                { new OverflowChunk(x - 1, z - 1), new OverflowChunk(x, z - 1), new OverflowChunk(x + 1, z - 1) },
                { new OverflowChunk(x - 1, z), chunk, new OverflowChunk(x + 1, z) },
                { new OverflowChunk(x - 1, z + 1), new OverflowChunk(x, z + 1), chunk, new OverflowChunk(x + 1, z + 1) }
        };

        this.x = x;
        this.z = z;
    }

    private final Chunk chunk;
    private final int x;
    private final int z;
    private final TileWriter[][] buffer;
    private final World<?> world;
    private final Noise noise;

    @Override
    public long getSeed() {
        return this.world.getSeed();
    }

    TileWriter getWritable(int x, int z) {
        return this.buffer[x - this.x + 1][z - this.z + 1];
    }

    public double sampleNoise(double x, double y) {
        return this.noise.sample(x, y, 0);
    }

    @Override
    public boolean isInWorld(int x, int y, int z) {
        return this.world.isInWorld(x, y, z);
    }

    @Override
    public void writeTile(int x, int y, int z, byte tile) {
        // removed canPlaceAt check for more direct writing. any "canPlaceAt" stuff should be done directly in the generator.
        this.getWritable(x >> 4, z >> 4).writeTile(x & 0xF, y, z & 0xF, tile);
    }

    @Override
    public void writeMeta(int x, int y, int z, byte meta) {
        this.getWritable(x >> 4, z >> 4).writeMeta(x & 0xF, y, z & 0xF, meta);
    }

    @Override
    public byte readTile(int x, int y, int z) {
        return this.getWritable(x >> 4, z >> 4).readTile(x & 0xF, y, z & 0xF);
    }

    @Override
    public byte readMeta(int x, int y, int z) {
        return this.getWritable(x >> 4, z >> 4).readMeta(x & 0xF, y, z & 0xF);
    }

    @Override
    public int getHeight(int x, int z, Predicate<Tile> solid) {
        return this.chunk.getHeight(x & 0xF, z & 0xF, solid);
    }

    @Override
    public Kingdom getKingdom(int x, int z) {
        return chunk.getKingdom(x & 0xF, z & 0xF);
    }

    @Override
    public int getKingdomId(int x, int z) {
        return chunk.getKingdomId(x & 0xF, z & 0xF);
    }

    @Override
    public void destroy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public World<?> getGameplayWorld() {
        return this.world;
    }
}
