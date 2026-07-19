package valoeghese.twofc.world.gen.ecozone;

import valoeghese.twofc.world.gen.generator.Generator;
import valoeghese.twofc.world.gen.generator.GroundFoliageGeneratorSettings;
import valoeghese.twofc.world.gen.generator.ScatteredOreGenerator;
import valoeghese.twofc.world.gen.generator.TreeGeneratorSettings;
import valoeghese.twofc.world.tile.Tile;

public class TemperateRainforestZone extends EcoZone {
	TemperateRainforestZone() {
		super("temperate_rainforest");

		this.addGenerator(Generator.SCATTERED_ORE, ScatteredOreGenerator.EXTRA_COAL);
		this.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(6, 16, Tile.BRUNNERA, Tile.DAISY, Tile.TALLGRASS, Tile.TALLGRASS));
		this.addGenerator(Generator.TREE, new TreeGeneratorSettings(5, 2.0f, 4, 3));
		this.addGenerator(Generator.POMELO_PLANT, new TreeGeneratorSettings(0, 2.2f, 1, 1));
	}
}
