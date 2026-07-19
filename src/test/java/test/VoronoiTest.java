package test;

import valoeghese.twofc.util.maths.Vec2f;
import valoeghese.twofc.world.kingdom.Kingdom;
import valoeghese.twofc.world.kingdom.Voronoi;

public class VoronoiTest extends PanelTest {
	public static void main(String[] args) {
		new VoronoiTest().start();
	}

	@Override
	protected int getColour(int x, int z) {
		Vec2f val = Voronoi.sampleVoronoi((float) x / 90.0f, (float) z / 90.0f, 123, Kingdom.RELAXATION);

		int color = 0;

		if (val.squaredDist((float) x / 90.0f, (float) z / 90.0f) > 0.001f) {
			color = val.hashCode() & 0xFF;
			color <<= 8;
			color |= (val.hashCode() * 3) & 0xFF;
			color <<= 8;
			color |= (val.hashCode() * 7) & 0xFF;
		}

		return color;
	}
}
