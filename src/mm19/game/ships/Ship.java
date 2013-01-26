package mm19.game.ships;

/**
 * 
 * @author mm19
 * 
 * An abstract base class for all ships in the game
 *
 */
public abstract class Ship {

	// x,y passed to cstructor - cstructor sets other vars on super class
	
	private final int uId; 	    // [Presumption] Type of ship
	private final int boatLength;   // Length of the boat (boats are always of width 1)
	private int health;			    // Health of the boat
	private boolean isAlive = true; // Whether or not the ship is alive

	// Constructor
	public Ship(int uID, int boatLength) // x, y, orientation - g/s
	{
		this.uId = uID;
		this.boatLength = boatLength;
	}
	
	public int getLength()
	{
		return this.boatLength;
	}
	
	public int getID()
	{
		return this.uId;
	}
	
	// ---- Damage ----
	public int applyDamage(int damage)
	{
		// If damage is <= 0, nothing happens
		this.health -= Math.max(damage, 0);
		
		// Update isAlive
		this.isAlive = (this.health > 0);
		
		// Return health
		return this.health;
	}
}
