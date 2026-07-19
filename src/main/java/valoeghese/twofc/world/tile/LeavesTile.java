package valoeghese.twofc.world.tile;

import valoeghese.twofc.util.maths.Vec2i;
import valoeghese.twofc.world.player.ItemType;
import valoeghese.twofc.world.player.Item;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Function;

public class LeavesTile extends Tile {
	public LeavesTile(String textureName, int id, float natureness) {
		super(textureName, id, natureness);
	}

	private int fruitingU = 0;
	private int fruitingV = 0;

	@Override
	public void requestUV(Function<String, Vec2i> uvs) {
		super.requestUV(uvs);
		Vec2i fruiting = uvs.apply("pomelo_leaves");
		this.fruitingU = fruiting.getX();
		this.fruitingV = fruiting.getY();
	}

	@Override
	public int getU(int faceAxis, byte meta) {
		return meta == 1 ? this.fruitingU : super.getU(faceAxis, meta);
	}

	@Override
	public int getV(int faceAxis, byte meta) {
		return meta == 1 ? this.fruitingV : super.getV(faceAxis, meta);
	}

	@Nullable
	@Override
	public Item getDrop(Random rand, byte meta) {
		if (meta == 1) {
			return new Item(ItemType.POMELO);
		} else {
			return super.getDrop(rand, meta);
		}
	}
}
