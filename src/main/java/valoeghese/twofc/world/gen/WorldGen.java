package valoeghese.twofc.world.gen;

import valoeghese.twofc.util.Pair;
import valoeghese.twofc.util.noise.Noise;
import valoeghese.twofc.util.noise.RidgedNoise;
import valoeghese.twofc.world.GeneratorWorld;
import valoeghese.twofc.world.World;
import valoeghese.twofc.world.WorldComponent;
import valoeghese.twofc.world.chunk.Chunk;
import valoeghese.twofc.world.gen.ecozone.EcoZone;
import valoeghese.twofc.world.gen.generator.Generator;
import valoeghese.twofc.world.gen.generator.GeneratorSettings;
import valoeghese.twofc.world.tile.Tile;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class WorldGen {
	public WorldGen(long seed) {
		this.sand = new Noise(new Random(seed - 29));
		this.ecoZone = new Noise(new Random(seed + 31));
		this.cavesHorizontal = new Noise(new Random(seed + 79));
		this.cavesMain = new Noise(new Random(seed - 79));
	}

	private final Noise sand;
	private final Noise ecoZone;

	// Caves

	private final Noise cavesHorizontal;
	private final Noise cavesMain;

	public <T extends Chunk> T generateChunk(ChunkConstructor<T> constructor, World<T> parent, int chunkX, int chunkZ, Random rand) {
		byte[] tiles = new byte[16 * 16 * WorldComponent.WORLD_HEIGHT];
		byte[] meta = new byte[tiles.length];

		double[] noise = generateNoise(chunkX, chunkZ);
		shapeChunk(chunkX, chunkZ, noise, tiles, meta);
		carveCaves(chunkX, chunkZ, noise, tiles, meta);

		return constructor.create(parent, chunkX, chunkZ, tiles, meta, null);
	}

	private double[] generateNoise(int chunkX, int chunkZ) {
		int blockX = chunkX << 4;
		int blockZ = chunkZ << 4;

		double[] noise = new double[256];

		for (int x = 0; x < 16; ++x) {
			int totalX = x + blockX;

			for (int z = 0; z < 16; ++z) {
				int totalZ = z + blockZ;

				noise[x * 16 + z] = this.sampleHeight(totalX, totalZ);
			}
		}

		return noise;
	}

	private void shapeChunk(int chunkX, int chunkZ, double[] heightmap, byte[] tiles, byte[] meta) {
		int blockX = chunkX << 4;
		int blockZ = chunkZ << 4;

		for (int x = 0; x < 16; ++x) {
			int totalX = x + blockX;

			for (int z = 0; z < 16; ++z) {
				int totalZ = z + blockZ;
				EcoZone zone = getEcoZoneByPosition(totalX, totalZ);

				// ridges
				int height = (int) heightmap[x * 16 + z];

				int sandDepth = this.sampleBeaches(totalX, totalZ);

				if (height >= WorldComponent.WORLD_HEIGHT) {
					height = WorldComponent.WORLD_HEIGHT - 1; // height cap
				}

				int depth = zone.surface == Tile.SAND.id ? 2 : 1; // sand depth where sand is surface

				for (int y = 0; y < height; ++y) {
					byte toSet = y > height - depth - 1 ? zone.surface : Tile.STONE.id;

					if (toSet == zone.surface && height < 52) {
						toSet = zone.beach;
					}

					int index = Chunk.index(x, y, z);
					tiles[index] = toSet;

					if (toSet == Tile.GRASS.id && zone.isCold()) {
						meta[index] = 1;
					}
				}

				if (height < 52) {
					for (int y = height; y < 52; ++y) {
						if (y == 51 && zone.isCold()) {
							tiles[Chunk.index(x, y, z)] = Tile.ICE.id;
						} else {
							tiles[Chunk.index(x, y, z)] = Tile.WATER.id;
						}
					}
				}

				// add beaches
				if (height <= 52 + sandDepth) {
					for (int y = 51; y < height; ++y) {
						tiles[Chunk.index(x, y, z)] = zone.beach;
					}
				}
			}
		}
	}

	private void carveCaves(int chunkX, int chunkZ, double[] heightmap, byte[] tiles, byte[] meta) {
		int blockX = chunkX << 4;
		int blockZ = chunkZ << 4;

		for (int x = 0; x < 16; ++x) {
			int totalX = x + blockX;

			for (int z = 0; z < 16; ++z) {
				int totalZ = z + blockZ;

				double height = heightmap[x * 16 + z];

				double threshold = horizontalThresholdAt(cavesHorizontal.sample(totalX / 35.0, totalZ / 35.0));

				for (int y = 0; y < (int) height; y++) {
					if ((noiseFalloffAt(y, height) + (cavesMain.sample(totalX / 40.0, y / 20.0, totalZ / 40.0) * 2.5)) < threshold) {
						tiles[Chunk.index(x, y, z)] = Tile.AIR.id;
						meta[Chunk.index(x, y, z)] = 0;
					}
				}
			}
		}
	}

	private static double horizontalThresholdAt(double noise) {
		return (14.5 * noise * noise);
	}

	private static double noiseFalloffAt(int y, double maxHeight) {
		return (48.0 / y) - (48.0 / (y - maxHeight));
	}

	protected abstract double sampleHeight(double x, double z);

	public void populateChunk(GeneratorWorld world, Chunk chunk, Random rand) {
		EcoZone zone = getEcoZoneByPosition(chunk.startX, chunk.startZ);

		for (Pair<Generator, GeneratorSettings> generator : zone.getGenerators()) {
			generator.getLeft().generate(world, generator.getRight(), chunk.startX, chunk.startZ, rand);
		}
	}

	public EcoZone getEcoZoneByPosition(double x, double z) {
		return getEcoZone(ecoZone.sample(x * 0.0012, z * 0.0012), ecoZone.sample(x * 0.002 + 4.08, z * 0.002));
	}

	public EcoZone getEcoZone(double temp, double humidity) {
		if (temp < -0.39) {
			return EcoZone.TUNDRA;
		} else if (temp < -0.27) {
			if (humidity > 0.15) {
				return EcoZone.COLD_WOODLAND;
			} else {
				return EcoZone.TUNDRA;
			}
		} else if (temp < 0.27) {
			if (humidity < -0.15) {
				return EcoZone.TEMPERATE_GRASSLAND;
			} else if (humidity < 0.25) {
				return EcoZone.TEMPERATE_WOODLAND;
			} else {
				return EcoZone.TEMPERATE_RAINFOREST;
			}
		} else {
			if (humidity < -0.2) {
				return EcoZone.DESERT;
			} else if (humidity < 0.15) {
				return EcoZone.TROPICAL_STEPPE;
			} else if (humidity < 0.2) {
				return EcoZone.TROPICAL_RAINFOREST_EDGE;
			} else {
				return EcoZone.TROPICAL_RAINFOREST;
			}
		}
	}

	int sampleBeaches(int totalX, int totalZ) {
		return (int) (2.1 * sand.sample(totalX / 21.0, totalZ / 21.0));
	}

	@FunctionalInterface
	public interface ChunkConstructor<T extends Chunk> {
		T create(World<T> parent, int x, int z, byte[] tiles, byte[] meta, @Nullable int[] kingdoms);
	}
}
