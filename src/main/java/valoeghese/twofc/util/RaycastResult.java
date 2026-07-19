package valoeghese.twofc.util;

import valoeghese.twofc.util.maths.TilePos;

public class RaycastResult {
	public RaycastResult(TilePos pos, Face face) {
		this.pos = pos;
		this.face = face;
	}

	public final TilePos pos;
	public final Face face;
}
