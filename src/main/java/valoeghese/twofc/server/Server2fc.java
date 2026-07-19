package valoeghese.twofc.server;

import valoeghese.twofc.Game2fc;
import valoeghese.twofc.server.world.ServerWorld;
import valoeghese.twofc.world.player.Player;
import valoeghese.twofc.world.sound.SoundEffect;

import javax.annotation.Nullable;

public class Server2fc extends Game2fc<ServerWorld, Player> implements Runnable {
	@Override
	public void run() {

	}

	@Override
	public void playSound(@Nullable Player toExcept, SoundEffect effect, double x, double y, double z, float volume) {
		// TODO
	}

	@Override
	public boolean isMainThread() {
		return true; // TODO
	}
}
