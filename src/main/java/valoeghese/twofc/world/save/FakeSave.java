package valoeghese.twofc.world.save;

import valoeghese.twofc.Game2fc;
import valoeghese.twofc.util.maths.Pos;
import valoeghese.twofc.world.chunk.Chunk;
import valoeghese.twofc.world.chunk.ChunkLoadStatus;
import valoeghese.twofc.world.GameplayWorld;
import valoeghese.twofc.world.gen.WorldGen;
import valoeghese.twofc.world.player.Item;
import valoeghese.twofc.world.player.Player;

import java.util.Iterator;
import java.util.Random;

public class FakeSave implements SaveLike {
	public FakeSave(long seed) {
		this.seed = seed;
	}

	private final long seed;
	private final Random genRand = new Random();

	@Override
	public void writeChunks(Iterator<? extends Chunk> chunks) {
	}

	@Override
	public void writeForClient(Player player, GameplayWorld world, Iterator<Item> inventory, int invSize, Pos playerPos, Pos spawnPos, long time) {
	}

	@Override
	public <T extends Chunk> void loadChunk(WorldGen worldGen, ChunkLoadingAccess<T> parent, int x, int z, WorldGen.ChunkConstructor<T> constructor, ChunkLoadStatus status) {
		this.genRand.setSeed(this.seed + 134 * x + -529 * z);
		T chunk = worldGen.generateChunk(constructor, parent, x, z, this.genRand);
		Game2fc.getInstance().runLater(() -> parent.addUpgradedChunk(chunk, status));
	}
}
