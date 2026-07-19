package valoeghese.twofc.world.gen.generator;

import valoeghese.twofc.world.GeneratorWorld;

import java.util.Random;

public class RandomCountGeneratorSettings implements GeneratorSettings {
	public RandomCountGeneratorSettings(int min, int max) {
		this.min = min;
		this.dCount = max - min + 1;
	}

	private final int min;
	private final int dCount;

	@Override
	public int getCount(GeneratorWorld world, Random rand, int startX, int startZ) {
		return this.min + rand.nextInt(this.dCount);
	}
}
