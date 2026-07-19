package valoeghese.twofc.world.gen.ecozone;

import valoeghese.twofc.world.gen.generator.Generator;
import valoeghese.twofc.world.gen.generator.GroundFoliageGeneratorSettings;
import valoeghese.twofc.world.gen.generator.TreeGeneratorSettings;
import valoeghese.twofc.world.tile.Tile;

public class GrasslandZone extends EcoZone {
	GrasslandZone() {
		super("grassland");

		this.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(14, 20, Tile.DAISY, Tile.DANDELION, Tile.TALLGRASS, Tile.TALLGRASS, Tile.TALLGRASS));
		this.addGenerator(Generator.TREE, new TreeGeneratorSettings(0, 1.5f));
	}
}
