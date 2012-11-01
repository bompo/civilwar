package de.redlion.rts;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import de.redlion.rts.units.Soldier;

public class Targeting {

	/*
	 * returns the closest target of the given type 0 = Soldier
	 */
	public static Soldier getNearestOfType(Soldier source, int type) {
		if (type == 0)
			return getNearestOfType(source, GameSession.getInstance().soldiers);
		else
			return null;
	}
	
	private static Soldier getNearestOfType(Soldier source, Array<Soldier> soldiers) {
		// find the closest one!
		Soldier closed = null;
		float closestDistanze = Float.MAX_VALUE;

		for (int i = 0; i < soldiers.size; i++) {
			Soldier soldier = soldiers.get(i);
			float currentDistance = source.position.dst(soldier.position);

			if (soldier.alive && source.id != soldier.id && (currentDistance < closestDistanze)) {
				//skip if ship is not targeting source ship
				if(soldier instanceof Soldier) {
					if(((Soldier) soldier).ai.target!=null && ((Soldier) soldier).ai.target.id != source.id) {
						continue;
					}
				}
				closed = soldier;
				closestDistanze = currentDistance;
			}
		}

		return closed;
	}

	/*
	 * return a random enemy of the desired type that's in range
	 * 0 = Soldier
	 */
	public static Soldier getTypeInRange(Soldier source, int type, float range) {
		if (type == 0)
			return getTypeInRange(source, GameSession.getInstance().soldiers, range);
		else
			return null;
	}

	/**
	 * return a random ship of the desired type that's in range
	 * @param source
	 * @param soldiers
	 * @param range
	 * @return
	 */
	private static Soldier getTypeInRange(Soldier source, Array<Soldier> soldiers, float range) {
		Array<Soldier> inRange = new Array<Soldier>();
		float range_squared = range * range;

		for (int i = 0; i < soldiers.size; i++) {
			Soldier soldier = soldiers.get(i);
			float currentDistance = source.position.dst(soldier.position);

			if (soldier.alive && source.id != soldier.id && (currentDistance < range_squared)) {
				inRange.add(soldier);
			}
		}

		if (inRange.size > 0) {
			return inRange.get(MathUtils.random(0, inRange.size - 1));
		} else {
			return null;
		}
	}
}
