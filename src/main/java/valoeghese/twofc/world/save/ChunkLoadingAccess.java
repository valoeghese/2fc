package valoeghese.twofc.world.save;

import valoeghese.twofc.world.chunk.Chunk;
import valoeghese.twofc.world.ChunkAccess;
import valoeghese.twofc.world.chunk.ChunkLoadStatus;

public interface ChunkLoadingAccess<T extends Chunk> extends ChunkAccess {
	/**
	 * Places the chunk at its position.
	 * @param chunk the chunk.
	 * @param status the status of the chunk required.
	 */
	void addUpgradedChunk(T chunk, ChunkLoadStatus status);
}
