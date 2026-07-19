package valoeghese.twofc.world.gen.generator;

import valoeghese.twofc.world.GeneratorWorld;

import java.util.Random;

public interface GeneratorSettings {
	int getCount(GeneratorWorld world, Random rand, int startX, int startZ);
}
