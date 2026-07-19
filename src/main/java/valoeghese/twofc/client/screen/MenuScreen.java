package valoeghese.twofc.client.screen;

import valoeghese.twofc.client.Client2fc;
import valoeghese.twofc.client.Keybinds;
import valoeghese.twofc.client.render.Textures;
import valoeghese.twofc.client.render.gui.button.Button;
import valoeghese.twofc.client.render.gui.button.TextButton;
import valoeghese.twofc.client.sound.MusicSettings;
import valoeghese.twofc.world.sound.SoundEffect;
import valoeghese.scalpel.Window;
import valoeghese.scalpel.util.GLUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class MenuScreen extends Screen {
	public MenuScreen(Client2fc game, Screen parentScreen) {
		super(game);
		this.parentScreen = parentScreen;
	}

	protected void addButton(Button button, Runnable callback) {
		this.buttons.put(button, callback);
	}

	private final Map<Button, Runnable> buttons = new HashMap<>();
	protected final Screen parentScreen;

	@Override
	public void onFocus() {
		GLUtils.enableMouse(this.game.getWindowId());
	}

	@Override
	public void handleMouseInput(double dx, double dy) {
	}

	@Override
	public void renderGUI(float lighting) {
		// blend overlay
		GLUtils.enableBlend();
		Textures.DIMMING_OVERLAY.render();
		GLUtils.disableBlend();

		// render all the buttons
		for (Button button : this.buttons.keySet()) {
			button.render();
		}
	}

	@Override
	public void handleKeybinds() {
		if (Keybinds.DESTROY.hasBeenPressed()) {
			float[] mousePositions = this.game.getWindow().getSelectedPositions();

			for (Map.Entry<Button, Runnable> actionableButtons : this.buttons.entrySet()) {
				Button button = actionableButtons.getKey();

				if (button.isCursorSelecting(mousePositions)) {
					this.game.playSound((button instanceof TextButton && ((TextButton) button).getText().matches(".*(Continue|Exit|Back).*")) ? SoundEffect.BUTTON_OK : SoundEffect.BUTTON_CLICK);
					actionableButtons.getValue().run();
					break;
				}
			}
		}
	}

	@Override
	public void handleEscape(Window window) {
		this.game.switchScreen(this.parentScreen);
	}

	@Override
	public Optional<MusicSettings> getMusic() {
		return GameScreen.GAME_MUSIC;
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}
}
