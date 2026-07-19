package valoeghese.twofc.server.world;

import valoeghese.twofc.world.chunk.Chunk;
import valoeghese.twofc.world.GameplayWorld;
import valoeghese.twofc.world.gen.WorldGen;
import valoeghese.twofc.world.save.Save;

import javax.annotation.Nullable;

public class ServerWorld extends GameplayWorld<ServerChunk> {
	public ServerWorld(@Nullable Save save, long seed, int size, WorldGen.ChunkConstructor<ServerChunk> constructor) {
		super(save, seed, size, constructor);
	}

	@Override
	protected void onChunkRemove(Chunk c) {
	}
}
