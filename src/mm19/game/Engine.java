package mm19.game;

import java.util.ArrayList;

import mm19.game.board.Board;
import mm19.game.board.Position;
import mm19.game.player.Player;
import mm19.game.ships.DestroyerShip;
import mm19.game.ships.MainShip;
import mm19.game.ships.PilotShip;
import mm19.game.ships.Ship;
import mm19.server.ShipData;

/**
 * @author mm19
 *
 * This will put all the pieces of the game together, and actually make things run.
 */
public class Engine{
	private Player p1;
	private Player p2;
	final private static int SHOOT = 0;
	final private static int BURST_SHOT = 1;
	final private static int SONAR = 2;
	final private static int MOVE = 3;
	final public static int DEFAULT_RESOURCES=100;
	private static final String DESTROYER = "D";
	private static final String MAINSHIP = "M";
	private static final String PILOT = "P";

	/**
	 * the constructor is called by the server (or API?) to start the game.
	 */
    public Engine(){
    	p1 = null;
    	p2 = null;
    }
	
    /**
	 * This function sets up the player's pieces on the board as specified
	 * And returns the playerID to the server so that it can refer back to it
	 */
	public int playerSet(ArrayList<ShipData> shipDatas, String playerName){//ArrayList<Ship> ships, ArrayList<Position> positions){
		
		ArrayList<Ship> ships = new ArrayList<Ship>();
		ArrayList<Position> positions = new ArrayList<Position>();
		Ship tempShip;
		Position tempPos;
		String tempType;
		for(int i = 0; i < shipDatas.size(); i++){
			tempType = shipDatas.get(i).type;
			tempShip = null;
			if(tempType.equals(DESTROYER)){
				tempShip = new DestroyerShip();
			}else if(tempType.equals(MAINSHIP)){
				tempShip = new MainShip();
			}else if(tempType.equals(MAINSHIP)){
				tempShip = new PilotShip();
			}
			if(tempShip != null){
				if(shipDatas.get(i).orientation.equals("H")){
					tempPos = new Position(shipDatas.get(i).xCoord, 
							shipDatas.get(i).yCoord, 
							Position.Orientation.HORIZONTAL);
				}else{
					tempPos = new Position(shipDatas.get(i).xCoord, 
							shipDatas.get(i).yCoord, 
							Position.Orientation.VERTICAL);
				}
				ships.add(tempShip);
				positions.add(tempPos);
			}
		}
		
		Player player=new Player(DEFAULT_RESOURCES);
		Ability.setupBoard(player, ships, positions); //TODO: could fail to setup board
		if(p1 == null) p1 = player;
		else p2 = player;
		return player.getPlayerID();
	}
	
	/**
	 * At the start of their turn, they receive resources
	 * This function attempts all of the player's chosen actions for the turn
	 * Afterwards, it tells the API to send the data back
	 */
	public void playerTurn(int playerID, ArrayList<Action> actions){
		//Check for valid playerID
		Player p=null;
		Player otherP=null;
		if(p1.getPlayerID()==playerID){
			p=p1;
			otherP=p2;
		}
		else if(p2.getPlayerID()==playerID){
			p=p2;
			otherP=p1;
		}
		if(p==null){
			//TODO: just got an invalid player ID
		}
		for(Action a: actions){ //TODO: check the return value of these functions, save to some type of array list
			switch(a.actionID){
				case SHOOT:
					Ability.shoot(p, otherP, a.shipID, a.actionXVar, a.actionYVar);
					break;
				case BURST_SHOT:
					Ability.burstShot(p, otherP, a.shipID, a.actionXVar, a.actionYVar);
					break;
				case SONAR:
					Ability.sonar(p, otherP, a.shipID, a.actionXVar, a.actionYVar);
					break;
				case MOVE:
					Ability.move(p, a.shipID, new Position(a.actionXVar, a.actionYVar, p.getBoard().getShipPosition(a.shipID).orientation));
					break;
				default:
					break;
			}
			
		}
	}

	public int getP1ID() {
		if(p1 != null) return p1.getPlayerID();
		return -1;
	}

	public int getP2ID() {
		if(p2 != null) return p2.getPlayerID();
		return -1;
	}



}
