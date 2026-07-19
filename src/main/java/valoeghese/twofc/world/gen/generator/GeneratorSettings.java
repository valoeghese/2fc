package valoeghese.twofc.world.gen.generator;

import valoeghese.twofc.world.gen.GenWorld;

import java.util.Random;

public interface GeneratorSettings {
	int getCount(GenWorld world, Random rand, int startX, int startZ);
}
