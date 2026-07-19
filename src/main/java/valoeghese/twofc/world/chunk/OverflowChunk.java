package valoeghese.twofc.world.chunk;

import it.unimi.dsi.fastutil.ints.Int2ByteArrayMap;
import valoeghese.twofc.util.maths.TilePos;

public final class OverflowChunk implements TileWriter {
	public OverflowChunk(int x, int z) {
		this.x = x;
		this.z = z;
	}

	public final int x;
	public final int z;

	@Override
	public boolean isInWorld(int x, int y, int z) {
		return new TilePos(x, y, z).isValidForChunk();
	}

	@Override
	public void writeMeta(int x, int y, int z, byte meta) {
		this.meta.put(Chunk.index(x, y, z), meta);
	}

	@Override
	public void writeTile(int x, int y, int z, byte tile) {
		this.tiles.put(Chunk.index(x, y, z), tile);
	}

	@Override
	public byte readTile(int x, int y, int z) {
		return this.tiles.getOrDefault(Chunk.index(x, y, z), (byte)0);
	}

	@Override
	public byte readMeta(int x, int y, int z) {
		return this.meta.getOrDefault(Chunk.index(x, y, z), (byte)0);
	}

	public void appendToChunk(byte[] tiles, byte[] meta) {
		// they expect 0 meta when they do operations on an OverflowChunk.
		for (Int2ByteArrayMap.Entry entry : this.tiles.int2ByteEntrySet()) {
			int pos = entry.getIntKey();
			tiles[pos] = entry.getByteValue();
			meta[pos] = this.meta.remove(pos);
		}

		// any remaining
		for (Int2ByteArrayMap.Entry entry : this.meta.int2ByteEntrySet()) {
			meta[entry.getIntKey()] = entry.getByteValue();
		}
	}

	private Int2ByteArrayMap tiles = new Int2ByteArrayMap(48);
	private Int2ByteArrayMap meta = new Int2ByteArrayMap(48);
}
