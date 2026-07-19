package valoeghese.twofc.client.render.tile;

import valoeghese.twofc.client.render.model.ChunkMesh;
import valoeghese.twofc.client.world.ClientChunk;
import valoeghese.twofc.world.chunk.Chunk;
import valoeghese.twofc.world.tile.Tile;

import java.util.List;

/**
 * An interface for custom rendering of tiles.
 */
public interface TileRenderer {
	/**
	 * @param instance the tile to render
	 * @param layer the layer of rendered tile faces to add faces to
	 * @param tiles the array of tiles for rendering.
	 * @param chunk the chunk
	 * @param x the local chunk x
	 * @param y the local chunk y
	 * @param z the local chunk z
	 * @param meta the meta value of the tile
	 */
	void addFaces(Tile instance, List<ChunkMesh.RenderedTileFace> layer, byte[] tiles, ClientChunk chunk, int x, int y, int z, byte meta);

	/**
	 * @see Chunk#index
	 */
	static int index(int x, int y, int z) {
		return (x << 11) | (z << 7) | y;
	}
}
