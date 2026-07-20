package valoeghese.twofc.world.gen;

import test.PanelTest;
import valoeghese.twofc.util.maths.Vec2f;
import valoeghese.twofc.world.kingdom.Voronoi;

import java.awt.*;

/**
 * Displays a heightmap of a world where 1 pixel = 4 blocks (not average, picks instead the first in the block selection)
 */
public class RiversShapeTest extends PanelTest {
	public static void main(String[] args) {
		new RiversShapeTest().start();
	}

	static long seed = 1;//new Random().nextLong();
	static Earth.Debug worldGen = new Earth.Debug(seed);

	@Override
	protected int getColour(int x, int z) {
		Vec2f centre = Voronoi.sampleVoronoi((float) x / 20, (float)z / 20, (int)(seed & (long)0xFFFFFFFF), 0.2f);
		int region = worldGen.regionType(centre);
		return Color.getHSBColor(region == 2 ? 0.55f : region == 1 ? 0.4f : 0.7f, 1.0f, 0.8f).getRGB();
	}
}
