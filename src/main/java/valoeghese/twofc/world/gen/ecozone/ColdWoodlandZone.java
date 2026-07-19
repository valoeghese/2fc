package valoeghese.twofc.world.gen.ecozone;

import valoeghese.twofc.world.gen.generator.Generator;
import valoeghese.twofc.world.gen.generator.GroundFoliageGeneratorSettings;
import valoeghese.twofc.world.gen.generator.ScatteredOreGenerator;
import valoeghese.twofc.world.gen.generator.TreeGeneratorSettings;
import valoeghese.twofc.world.tile.Tile;

public class ColdWoodlandZone extends EcoZone {
	ColdWoodlandZone() {
		super("cold_woodland", Tile.GRASS, Tile.STONE);

		this.cold();
		this.addGenerator(Generator.SCATTERED_ORE, ScatteredOreGenerator.EXTRA_COAL);
		this.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(0, 2, Tile.TALLGRASS));
		this.addGenerator(Generator.TREE, new TreeGeneratorSettings(2, 1.0f));
	}
}
