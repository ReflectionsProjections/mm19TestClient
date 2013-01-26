package mm19.game.ships;

/**
 * @author mm19
 *
 * An abstract base class for all ships in the game
 */
public abstract class Ship {
    private static int nextID = 0;

    final private int ID;
    final private int length;

    private int health;

    /**
     * Constructor
     * Gives the instantiated Ship a unique identifier and specifies the length.
     *
     * @param length The length of the boat.
     */
    public Ship(int length, int health) {
        ID = nextID;
        nextID++;

        this.length = length;
        this.health = health;
    }

    /**
     * Reports the length of the Ship
     *
     * @return Length of Ship
     */
    public int getLength() {
        return length;
    }

    /**
     * Reports the boats unique identifier
     *
     * @return A unique identifier for the boat
     */
    public int getID() {
        return ID;
    }

    /**
     * Reports the health of the Ship
     *
     * @return Health of ship
     */
    public int getHealth() {
        return health;
    }

    /**
     * Applies damage to the boat
     *
     * @param damage Amount of damage to deal to the ship
     * @return The Ship's remaining health
     */
    public int applyDamage(int damage) {
        if (damage > 0) {
            health -= damage;
        }
        return health;
    }

    /**
     * Reports whether or not the ship has sunk
     *
     * @return True if the ship is still afloat, false if destroyed.
     */
    public boolean isAlive() {
        return (health > 0);
    }
}
