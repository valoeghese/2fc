package valoeghese.twofc.world.gen.generator;

import valoeghese.twofc.world.gen.GenWorld;

import java.util.Random;

public final class NoneGeneratorSettings implements GeneratorSettings {
	@Override
	public int getCount(GenWorld world, Random rand, int startX, int startZ) {
		return 0;
	}

	public static final NoneGeneratorSettings INSTANCE = new NoneGeneratorSettings();
}
