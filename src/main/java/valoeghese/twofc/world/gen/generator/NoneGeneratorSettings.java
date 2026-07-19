package valoeghese.twofc.world.gen.generator;

import valoeghese.twofc.world.GeneratorWorld;

import java.util.Random;

public final class NoneGeneratorSettings implements GeneratorSettings {
	@Override
	public int getCount(GeneratorWorld world, Random rand, int startX, int startZ) {
		return 0;
	}

	public static final NoneGeneratorSettings INSTANCE = new NoneGeneratorSettings();
}
