package valoeghese.twofc.world.gen.ecozone;

import valoeghese.twofc.world.gen.generator.Generator;
import valoeghese.twofc.world.gen.generator.GroundFoliageGeneratorSettings;
import valoeghese.twofc.world.tile.Tile;

public class DesertZone extends EcoZone {
	DesertZone() {
		super("desert", Tile.SAND, Tile.SAND);

		this.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(3, 5, Tile.CACTUS));
	}
}
