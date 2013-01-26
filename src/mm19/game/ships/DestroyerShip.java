package mm19.game.ships;

import mm19.game.Constants;

/**
 * 
 * @author mm19
 * 
 * Destroyers can fire and move.
 * Length: 4
 *
 */
public class DestroyerShip extends Ship{
    //Private Constants
    final private static int HEALTH_MULTIPLIER = 3;

    //Public Constants
    final public static int HEALTH = HEALTH_MULTIPLIER * Constants.MISSILE_DAMAGE;
    final public static int LENGTH = 4;

    /**
     * Constructor
     * Sets length and health using parent constructor
     */
	public DestroyerShip(){
		super(LENGTH, HEALTH);
	}
}
