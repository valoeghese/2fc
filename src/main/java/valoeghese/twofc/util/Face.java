package valoeghese.twofc.util;

import valoeghese.twofc.util.maths.Pos;
import valoeghese.twofc.util.maths.TilePos;

import java.util.function.UnaryOperator;

public enum Face implements UnaryOperator<TilePos> {
	WEST(0, 0, 1),
	EAST(0, 0, -1),
	SOUTH(1, 0, 0),
	NORTH(-1, 0, 0),
	UP(0, 1, 0),
	DOWN(0, -1, 0);

	Face (int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	private final int x;
	private final int y;
	private final int z;

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	@Override
	public TilePos apply(TilePos original) {
		return original.ofAdded(this.x, this.y, this.z);
	}

	public Pos half() {
		return new Pos((double) this.x * 0.5, (double) this.y * 0.5, (double) this.z * 0.5);
	}

	public Face reverse() {
		switch (this) {
		case WEST:
			return EAST;
		case EAST:
			return WEST;
		case SOUTH:
			return NORTH;
		case NORTH:
			return SOUTH;
		case UP:
			return DOWN;
		case DOWN:
			return UP;
		default: // muri desu
			return null;
		}
	}
}
