package valoeghese.twofc.world.save;

import valoeghese.twofc.util.maths.Pos;
import valoeghese.twofc.world.chunk.Chunk;
import valoeghese.twofc.world.chunk.ChunkLoadStatus;
import valoeghese.twofc.world.World;
import valoeghese.twofc.world.gen.WorldGen;
import valoeghese.twofc.world.player.Item;
import valoeghese.twofc.world.player.Player;

import java.util.Iterator;

public interface SaveLike {
	void writeChunks(Iterator<? extends Chunk> chunks);
	void writeForClient(Player player, World world, Iterator<Item> inventory, int invSize, Pos playerPos, Pos spawnPos, long time);
	<T extends Chunk> void loadChunk(WorldGen worldGen, ChunkLoadingAccess<T> parent, int x, int z, WorldGen.ChunkConstructor<T> constructor, ChunkLoadStatus status);
}
