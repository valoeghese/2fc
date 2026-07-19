package valoeghese.twofc.world.gen.ecozone;

import valoeghese.twofc.world.gen.generator.Generator;
import valoeghese.twofc.world.gen.generator.GroundFoliageGeneratorSettings;
import valoeghese.twofc.world.gen.generator.TreeGeneratorSettings;
import valoeghese.twofc.world.tile.Tile;

public class TundraZone extends EcoZone {
	TundraZone() {
		super("tundra", Tile.GRASS, Tile.STONE);

		this.cold();
		this.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(0, 1, Tile.TALLGRASS));
		this.addGenerator(Generator.TREE, new TreeGeneratorSettings(-1, 0));
	}
}
