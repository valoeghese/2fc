package valoeghese.twofc.world.tile;

import valoeghese.twofc.client.render.tile.TileRenderer;
import valoeghese.twofc.client.render.tile.TorchRenderer;
import valoeghese.twofc.world.player.ItemType;

import javax.annotation.Nullable;

public class TorchTile extends Tile {
	public TorchTile(String textureName, int id, float natureness) {
		super(textureName, id, natureness);
	}

	@Nullable
	@Override
	public TileRenderer getCustomTileRenderer() {
		return TorchRenderer.INSTANCE;
	}

	@Nullable
	@Override
	public ItemType delegateItem() {
		return ItemType.TORCH;
	}
}
