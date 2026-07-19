package valoeghese.twofc.world.save;

import valoeghese.twofc.world.chunk.Chunk;
import valoeghese.twofc.world.chunk.ChunkLoadStatus;
import valoeghese.twofc.world.kingdom.Kingdom;

import javax.annotation.Nullable;

public interface ChunkLoadingAccess<T extends Chunk> {
	boolean loadChunk(int x, int z, ChunkLoadStatus status);
	/**
	 * Gets the chunk if it is currently loaded.
	 */
	@Nullable
	Chunk getChunk(int x, int z);
	/**
	 * Gets the chunk at LIGHT stage, if it exists.
	 */
	@Nullable
	Chunk getFullChunk(int x, int z);

	Kingdom kingdomById(int kingdom, int x, int z);

	/**
	 * Places the chunk at its position.
	 * @param chunk the chunk.
	 * @param status the status of the chunk required.
	 */
	void addUpgradedChunk(T chunk, ChunkLoadStatus status);

	long getSeed();
}
