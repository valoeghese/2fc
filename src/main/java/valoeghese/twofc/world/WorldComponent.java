package valoeghese.twofc.world;

import valoeghese.twofc.util.maths.TilePos;
import valoeghese.twofc.world.chunk.TileWriter;
import valoeghese.twofc.world.kingdom.Kingdom;
import valoeghese.twofc.world.player.Player;
import valoeghese.twofc.world.tile.Tile;

import java.util.function.Predicate;

public interface WorldComponent extends TileWriter {
	// Height and Position
	int getHeight(int x, int z, Predicate<Tile> solid);

	default int getHeight(int x, int z) {
		return this.getHeight(x, z, Tile::shouldRender);
	}
	default boolean isInWorld(TilePos pos) {
		return this.isInWorld(pos.x, pos.y, pos.z);
	}

	// Seed
	long getSeed();

	// Overloads R/W
	default void writeTile(TilePos pos, byte tile) {
		this.writeTile(pos.x, pos.y, pos.z, tile);
	}
	default void writeMeta(TilePos pos, byte tile) {
		this.writeMeta(pos.x, pos.y, pos.z, tile);
	}

	default byte readTile(TilePos pos) {
		return this.readTile(pos.x, pos.y, pos.z);
	}
	default byte readMeta(TilePos pos) {
		return this.readMeta(pos.x, pos.y, pos.z);
	}

	// Kingdom
	Kingdom getKingdom(int x, int z);
	int getKingdomId(int x, int z);

	// ?
	void destroy();

	default void updateChunkOf(Player player) {
	}

	/**
	 * @return the gameplay world associated with this world component. Returns itself if the object a GameplayWorld already.
	 */
	World<?> getGameplayWorld();
}
