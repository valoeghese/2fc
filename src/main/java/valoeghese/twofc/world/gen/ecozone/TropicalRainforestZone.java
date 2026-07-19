package valoeghese.twofc.world.gen.ecozone;

import valoeghese.twofc.world.gen.generator.Generator;
import valoeghese.twofc.world.gen.generator.GroundFoliageGeneratorSettings;
import valoeghese.twofc.world.gen.generator.ScatteredOreGenerator;
import valoeghese.twofc.world.gen.generator.TreeGeneratorSettings;
import valoeghese.twofc.world.tile.Tile;

public class TropicalRainforestZone extends EcoZone {
	TropicalRainforestZone(String name, int density) {
		super(name);

		this.addGenerator(Generator.SCATTERED_ORE, ScatteredOreGenerator.EXTRA_COAL);
		int grassDensity = (int)((double)density * 1.5);
		this.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(grassDensity, grassDensity + 3, Tile.DAISY, Tile.TALLGRASS));
		this.addGenerator(Generator.TREE, new TreeGeneratorSettings(density, 3.5f, 6, 4));
		this.addGenerator(Generator.POMELO_PLANT, new TreeGeneratorSettings(0, 5.0f, 1, 1));
	}
}
