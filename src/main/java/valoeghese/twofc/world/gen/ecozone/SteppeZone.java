package valoeghese.twofc.world.gen.ecozone;

import valoeghese.twofc.world.gen.generator.Generator;
import valoeghese.twofc.world.gen.generator.GroundFoliageGeneratorSettings;
import valoeghese.twofc.world.gen.generator.TreeGeneratorSettings;
import valoeghese.twofc.world.tile.Tile;

public class SteppeZone extends EcoZone {
	SteppeZone() {
		super("steppe");

		this.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(20, 32, Tile.TALLGRASS));
		this.addGenerator(Generator.TREE, new TreeGeneratorSettings(0, 1.75f, 1, 1));
	}
}
