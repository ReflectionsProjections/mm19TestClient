package mm19.game.ships;

import mm19.game.Constants;

/**
 * @author mm19
 *
 * Pilot ships can only move, but gather resources.
 * Length: 2
 */
public class PilotShip extends Ship {
    //Private Constants
    final private static int HEALTH_MULTIPLIER = 2;

    //Public Constants
    final public static int HEALTH = HEALTH_MULTIPLIER * Constants.MISSILE_DAMAGE;
    final public static int LENGTH = 2;

    /**
     * Constructor
     * Sets length using parent constructor
     * TODO: Also set health using the parent constructor
     */
    public PilotShip() {
        super(LENGTH, HEALTH);
    }
}
