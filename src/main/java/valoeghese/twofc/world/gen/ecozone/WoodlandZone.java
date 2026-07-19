package valoeghese.twofc.world.gen.ecozone;

import valoeghese.twofc.world.gen.generator.Generator;
import valoeghese.twofc.world.gen.generator.GroundFoliageGeneratorSettings;
import valoeghese.twofc.world.gen.generator.TreeGeneratorSettings;
import valoeghese.twofc.world.tile.Tile;

public class WoodlandZone extends EcoZone {
	WoodlandZone() {
		super("woodland");

		this.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(6, 8, Tile.BRUNNERA, Tile.DAISY, Tile.TALLGRASS));
		this.addGenerator(Generator.TREE, new TreeGeneratorSettings(2, 2.0f));
	}
}
