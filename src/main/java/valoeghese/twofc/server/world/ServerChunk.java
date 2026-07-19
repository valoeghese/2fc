package valoeghese.twofc.server.world;

import valoeghese.twofc.world.World;
import valoeghese.twofc.world.chunk.Chunk;

import javax.annotation.Nullable;

public class ServerChunk extends Chunk {
	public ServerChunk(World parent, int x, int z, byte[] tiles, byte[] meta, @Nullable int[] kingdoms) {
		super(parent, x, z, tiles, meta, kingdoms);
	}

	@Override
	public void refreshLightingMesh() {
		// TODO send packet thing
	}
}
