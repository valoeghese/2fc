package valoeghese.twofc.world.chunk;

import valoeghese.twofc.util.maths.TilePos;

public interface TileWriter {
	void writeMeta(int x, int y, int z, byte meta);
	void writeTile(int x, int y, int z, byte tile);

	byte readTile(int x, int y, int z);
	byte readMeta(int x, int y, int z);

	boolean isInWorld(int x, int y, int z);

	int WORLD_HEIGHT = 128;
}
