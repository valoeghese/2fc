package valoeghese.twofc.world.gen;

import test.PanelTest;
import valoeghese.twofc.util.Face;

import java.awt.*;

/**
 * Displays a heightmap of a world where 1 pixel = 4 blocks (not average, picks instead the first in the block selection)
 */
public class RiversShapeTest extends PanelTest {
	public static void main(String[] args) {
		new RiversShapeTest().scale(1).start();
	}

	static boolean showInflow = true;
	static long seed = 1;//new Random().nextLong();
	static Earth.Debug worldGen = new Earth.Debug(seed);

	@Override
	protected int getColour(int x_, int z_) {
		final float floodplainHue = 0.55f;
		final float mountainsHue = 0.4f;
		final float waterHue = 0.7f;

		// make most zoomed in 8x
		int x = Math.floorDiv(x_, 8);
		int z = Math.floorDiv(z_, 8);

		int rx = Math.floorDiv(x, 3);
		int rz = Math.floorDiv(z, 3);
		Earth.RegionInfo region = worldGen.riverInfo(rx, rz);

		if (region.type() == 0) {
			return Color.getHSBColor(waterHue, 1.0f, 0.8f).getRGB();
		}

		int lx = Math.floorMod(x, 3) - 1;
		int lz = Math.floorMod(z, 3) - 1;
		float hue = region.type() == 2 ? floodplainHue : mountainsHue;
		float b = 0.8f;

		float riverB = region.outflow1() == region.outflow2() ? 0.5f : 0.96f;

		int ox1 = region.outflow1() == null ? 7 : region.outflow1().getX();
		int oz1 = region.outflow1() == null ? 7 : region.outflow1().getZ();

		int ox2 = region.outflow2() == null ? 7 : region.outflow2().getX();
		int oz2 = region.outflow2() == null ? 7 : region.outflow2().getZ();

		// Paint centre and outflow directions
		if ((lx == ox1 && lz == oz1) || (lx == ox2 && lz == oz2) || (lx == 0 && lz == 0 && region.outflow1() != null)) {
			hue = waterHue;
			b = riverB;
		}
		// note: only regions with 2 outflows are coastal (point to water), so N/E/S/W regions will not have outflow2 point to this region
		if (showInflow && hue != waterHue) {
			if (lx == -1 && lz == 0) { // north
				Earth.RegionInfo regionN = worldGen.riverInfo(rx - 1, rz);
				if (regionN.outflow1() == Face.SOUTH) {
					hue = waterHue;
					b = 0.96f;
				}
			}
			else if (lx == 1 && lz == 0) { // south
				Earth.RegionInfo regionS = worldGen.riverInfo(rx + 1, rz);
				if (regionS.outflow1() == Face.NORTH) {
					hue = waterHue;
					b = 0.96f;
				}
			}
			else if (lx == 0 && lz == -1) { // east
				Earth.RegionInfo regionE = worldGen.riverInfo(rx, rz - 1);
				if (regionE.outflow1() == Face.WEST) {
					hue = waterHue;
					b = 0.96f;
				}
			}
			else if (lx == 0 && lz == 1) { // west
				Earth.RegionInfo regionW = worldGen.riverInfo(rx, rz + 1);
				if (regionW.outflow1() == Face.EAST) {
					hue = waterHue;
					b = 0.96f;
				}
			}
		}

		return Color.getHSBColor(hue, 1.0f, b).getRGB();
	}
}
