package mm19.server;

import mm19.game.board.Position;

/*
 * This Ship Data class is a java representation  of the JSON the server will 
 * send detailing each ship.
 */
public class ShipData {
	final public int health;
	final public int ID;
	final public String type;
	final public int xCoord;
	final public int yCoord;
	final public Position.Orientation orientation;
	
	public ShipData(int health, int ID, String type,int xCoord, int yCoord,Position.Orientation orientation){
		this.health = health;
		this.ID = ID;
		this.type = type;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.orientation = orientation;
	}
}
