package valoeghese.twofc.world.gen.generator;

import valoeghese.twofc.world.tile.Tile;

import java.util.Random;

public class GroundFoliageGeneratorSettings extends RandomCountGeneratorSettings {
	public GroundFoliageGeneratorSettings(int min, int max, Tile... tiles) {
		super(min, max);
		this.tiles = tiles;
	}

	private final Tile[] tiles;

	public Tile pickTile(Random rand) {
		return this.tiles[rand.nextInt(tiles.length)];
	}
}
