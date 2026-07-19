package valoeghese.twofc.world.gen;

import test.Dummy2fc;
import test.PanelTest;
import valoeghese.twofc.util.maths.Vec2f;
import valoeghese.twofc.util.maths.Vec2i;
import valoeghese.twofc.world.gen.generator.CityGenerator;
import valoeghese.twofc.world.gen.generator.Generator;
import valoeghese.twofc.world.kingdom.Kingdom;
import valoeghese.twofc.world.kingdom.KingdomIDMapper;
import valoeghese.twofc.world.kingdom.Voronoi;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Displays a heightmap of a world where 1 pixel = 4 blocks (not average, picks instead the first in the block selection)
 */
public class KingdomTest extends PanelTest implements KingdomIDMapper {
	public static void main(String[] args) {
		Dummy2fc.use();
		new KingdomTest().scale(6).start();
		//IntList kingdomList = new IntArrayList(kingdoms);
		//Collections.sort(kingdomList);
		//System.out.println(kingdomList);
	}

	static final long seed = new Random().nextLong();
	private static final Map<Vec2f, Kingdom> KINGDOMS = new HashMap<>();
	//private static IntSet kingdoms = new IntArraySet();

	@Override
	public final Kingdom kingdomById(Vec2f voronoi) {
		return KINGDOMS.computeIfAbsent(voronoi, v -> new Kingdom(seed, v.id(), v));
	}

	@Override
	protected int getColour(int x, int z) {
		Kingdom k = this.kingdomById(Voronoi.sampleVoronoi(x / Kingdom.SCALE, z / Kingdom.SCALE, (int) seed, Kingdom.RELAXATION));
		//kingdoms.add(k.id);

		Vec2i centre = k.getCityCentre(); // get city centre
		int dist = centre.manhattan(x, z);

		if (dist > Generator.OVERWORLD_CITY_SIZE + 5) {
			if (CityGenerator.isOnPath(this, x, z, k, k.getCityCentre(), (int) seed)) {
				return Color.WHITE.getRGB();
			}
		} else if (dist >= Generator.OVERWORLD_CITY_SIZE) {
			return Color.DARK_GRAY.getRGB(); // dark grey to represent the walls
		}

		double hue = (double) k.id / (double) Integer.MAX_VALUE;
		return Color.getHSBColor((float) hue, 0.8f, 1.0f).getRGB();
	}
}
