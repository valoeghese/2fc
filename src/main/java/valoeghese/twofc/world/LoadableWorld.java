package valoeghese.twofc.world;

import valoeghese.twofc.util.maths.ChunkPos;
import valoeghese.twofc.world.chunk.Chunk;

import java.util.function.Consumer;

public interface LoadableWorld extends WorldComponent {
	void chunkLoad(ChunkPos centrePos);
	ChunkPos getSpawnPos();
	void scheduleForChunk(long chunkPos, Consumer<Chunk> callback, String taskName);
}
