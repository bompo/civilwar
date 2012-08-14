package de.redlion.rts.opponent;

import com.badlogic.gdx.math.Vector3;

import de.redlion.rts.controls.PlayerTwoControlMappings;

public class LocalOpponent extends Opponent {
	
	public LocalOpponent(SIDE side,boolean service) {
		super(side, service);
		this.input = new PlayerTwoControlMappings();
	}

	public void update(Vector3 playerposition) {
		super.update();
	}

}
