package mm19.game.ships;

import mm19.game.Ability;

/**
 * @author mm19
 *
 * Destroyers can fire and move.
 * Length: 4
 */
public class DestroyerShip extends Ship {
    //Private Constants
    final private static int HEALTH_MULTIPLIER = 4;

    //Public Constants
    final public static int HEALTH = HEALTH_MULTIPLIER * Ability.MISSILE_DAMAGE;
    final public static int LENGTH = 4;
    final public static boolean CAN_SHOOT = true;
    final public static boolean CAN_GENERATE_RESOURCES = false;
    final public static int RESOURCES_GENERATED = 0;
    final public static boolean CAN_MOVE = true;
    final public static boolean CAN_BURST_SHOT = true;
    final public static boolean CAN_SONAR = false;
    final public static String IDENTIFIER = "D";

    /**
     * Constructor
     * Sets length and health using parent constructor
     */
    public DestroyerShip() {
        super(LENGTH, HEALTH);
    }


    /**
     * Indicates whether or not the Ship can fire at positions on the board.
     *
     * @return True if the Ship can fire, false otherwise.
     */
    @Override
    public boolean canShoot() {
        return CAN_SHOOT;
    }

    /**
     * Indicates whether or not the Ship can generate resources.
     *
     * @return True if the Ship can generate resources, false otherwise
     */
    @Override
    public boolean canGenerateResources() {
        return CAN_GENERATE_RESOURCES;
    }

    /**
     * Gets the number of resource points generated by the Ship each turn
     *
     * @return Amount of resources generated by ship
     */
    @Override
    public int getResources() {
        return RESOURCES_GENERATED;
    }

    /**
     * Indicates whether or not the Ship can move to a new location on the board.
     *
     * @return True if the Ship can move, false otherwise
     */
    @Override
    public boolean canMove() {
        return CAN_MOVE;
    }

    /**
     * Indicates whether or not the Ship can use the Burst Shot special ability
     *
     * @return True if the Ship can use burst shot, false otherwise.
     */
    @Override
    public boolean canBurstShot() {
        return CAN_BURST_SHOT;
    }

    /**
     * Indicates whether or not the Ship can use the Sonar ability
     *
     * @return True if the Ship can use sonar, false otherwise.
     */
    @Override
    public boolean canSonar() {
        return CAN_SONAR;
    }

    /**
     * Gets the identifier for the current ship
     *
     * @return The Ship's identifier
     */
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
}