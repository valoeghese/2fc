package valoeghese.twofc.world;

import valoeghese.twofc.util.maths.TilePos;
import valoeghese.twofc.world.gen.GenWorld;
import valoeghese.twofc.world.kingdom.Kingdom;
import valoeghese.twofc.world.player.Player;
import valoeghese.twofc.world.tile.Tile;

public interface TileAccess extends GenWorld {
	default void writeTile(TilePos pos, byte tile) {
		this.writeTile(pos.x, pos.y, pos.z, tile);
	}
	default byte readTile(TilePos pos) {
		return this.readTile(pos.x, pos.y, pos.z);
	}
	default byte readMeta(TilePos pos) {
		return this.readMeta(pos.x, pos.y, pos.z);
	}

	default int getHeight(int x, int z) {
		return this.getHeight(x, z, Tile::shouldRender);
	}

	Kingdom getKingdom(int x, int z);
	int getKingdomId(int x, int z);

	default boolean isInWorld(TilePos pos) {
		return this.isInWorld(pos.x, pos.y, pos.z);
	}

	void destroy();

	default void updateChunkOf(Player player) {
	}
}
