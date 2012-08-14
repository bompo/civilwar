package de.redlion.rts.controls;

import de.redlion.rts.Player;

public class SinglePlayerControls extends GameControls {
	
	public Player player;
	
	public SinglePlayerControls(Player player) {
		this.player = player;
	}
	
	
	@Override
	public boolean keyDown(int keycode) {
		keyDownOptions(keycode);

		keyDownPlayer(player, keycode);

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		keyUpPlayer(player, keycode);
		
		return false;
	}


}
