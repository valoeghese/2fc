package valoeghese.twofc.world.gen;

import valoeghese.twofc.world.GameplayWorld;

public interface SeedWorld {
	long getSeed();
	/**
	 * @return the gameplay world associated with this SeedWorld. Returns itself if the object a GameplayWorld already.
	 */
	GameplayWorld<?> getGameplayWorld();
}
