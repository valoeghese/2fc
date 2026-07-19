package valoeghese.twofc.client.screen;

import valoeghese.twofc.client.Client2fc;
import valoeghese.twofc.client.sound.MusicSettings;
import valoeghese.scalpel.Window;

import java.util.Optional;

public abstract class Screen {
	public Screen(Client2fc game) {
		this.game = game;
	}

	protected final Client2fc game;

	public abstract void renderGUI(float lighting);
	public abstract void handleMouseInput(double dx, double dy);
	public abstract void handleKeybinds();
	public abstract void handleEscape(Window window);

	public void onFocus() {
	}

	public Optional<MusicSettings> getMusic() {
		return Optional.empty();
	}

	public boolean isPauseScreen() {
		return false;
	}
}
