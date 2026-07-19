package valoeghese.twofc.client.screen;

import valoeghese.twofc.client.Client2fc;
import valoeghese.twofc.client.Keybinds;
import valoeghese.twofc.client.render.gui.Text;
import valoeghese.twofc.client.render.gui.collection.WorldSave;
import valoeghese.twofc.client.sound.MusicPiece;
import valoeghese.twofc.client.sound.MusicSettings;
import valoeghese.twofc.world.sound.SoundEffect;
import valoeghese.scalpel.Window;
import valoeghese.twofc.client.render.gui.GUI;
import valoeghese.scalpel.util.GLUtils;

import java.util.List;
import java.util.Optional;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class TitleScreen extends DelegatingScreen {
	public TitleScreen(Client2fc game) {
		super(game);
		this.delegates.add(new MainTitleScreen(game, this));
		this.delegates.add(new SelectWorldScreen(game, this));
		this.switchScreen(0);
	}

	@Override
	public void handleEscape(Window window) {
		switch (this.getSelectedIndex()) {
		case 0:
			glfwSetWindowShouldClose(window.id, true);
			break;
		case 1:
			this.switchScreen(0);
			break;
		}
	}

	@Override
	public void handleMouseInput(double dx, double dy) {
		super.handleMouseInput(dx, dy);
	}

	@Override
	public Optional<MusicSettings> getMusic() {
		return TITLE_MUSIC;
	}

	private static final Optional<MusicSettings> TITLE_MUSIC = Optional.of(MusicSettings.createFixed(List.of(MusicPiece.MAIN_THEME), 10, 200));

	private static class MainTitleScreen extends Screen {
		public MainTitleScreen(Client2fc game, DelegatingScreen parent) {
			super(game);
			this.titleText = new Text("Click to start.", -0.85f, 0.5f, 2.2f);
			this.parent = parent;
		}

		private final GUI titleText;
		private final DelegatingScreen parent;

		@Override
		public void onFocus() {
			GLUtils.disableMouse(this.game.getWindowId());
		}

		@Override
		public void renderGUI(float lighting) {
			this.titleText.render();
		}

		@Override
		public void handleMouseInput(double dx, double dy) {
		}

		@Override
		public void handleKeybinds() {
			if (Keybinds.DESTROY.hasBeenPressed()) {
				this.game.playSound(SoundEffect.BUTTON_CLICK);
				this.parent.switchScreen(1);
			}
		}

		@Override
		public void handleEscape(Window window) {
		}
	}

	private static class SelectWorldScreen extends Screen {
		public SelectWorldScreen(Client2fc game, DelegatingScreen parent) {
			super(game);
			this.saveGUI = new WorldSave("Save 1", "Save 2", "Save 3");
			this.parent = parent;
		}

		private final WorldSave saveGUI;
		private final DelegatingScreen parent;

		@Override
		public void onFocus() {
			GLUtils.enableMouse(this.game.getWindowId());
		}

		@Override
		public void renderGUI(float lighting) {
			this.saveGUI.render();
		}

		@Override
		public void handleMouseInput(double dx, double dy) {
		}

		@Override
		public void handleKeybinds() {
			if (Keybinds.DESTROY.hasBeenPressed()) {
				Window window = this.game.getWindow();
				float[] positions = window.getSelectedPositions();
				int selected = this.saveGUI.getSelected(positions[0], positions[1]);

				if (selected != -1) {
					++selected;
					this.parent.switchScreen(0);
					this.game.switchScreen(this.game.gameScreen);
					this.game.playSound(SoundEffect.BUTTON_CLICK);
					this.game.createWorld("save_" + selected);
//					this.game.generateSpawnChunks();
				}
			}
		}

		@Override
		public void handleEscape(Window window) {
		}
	}
}