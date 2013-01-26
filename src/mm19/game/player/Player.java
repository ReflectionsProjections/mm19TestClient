package mm19.game.player;

import java.util.ArrayList;

import mm19.game.board.Board;
import mm19.game.board.Position;
import mm19.game.ships.Ship;

/**
 * 
 * @author mm19
 * 
 * This will tie together the Board and Ship objects together, and keep track of 
 * the state of a player in the game.
 *
 */
public class Player {
	
	private boolean isAlive;
	private int resources;
	private Board board;
	private final int playerId;
	private boolean canSpecial;
	
	public Player(int res, int pId){
		playerId = pId;
		isAlive = true;
		resources = res;
		canSpecial = true;
		
	}

    /*
     * TODO: Eric made an assumption that ship Positions could be passed in as a parallel ArrayList.
     * If you would prefer a different implementation, please change this method accordingly.
     * If this implementation is fine, please remove this TODO.
     */
    /**
     * Attempts to place the player's starting ships in their requested positions.
     *
     * @param ships An ArrayList containing the ships to place
     * @param positions An ArrayList containing Positions indicating where the ships should be placed.
     * @return The player's Board if the placements were successful, null otherwise
     */
	public Board setMap(ArrayList<Ship> ships, ArrayList<Position> positions){
		for(int i = 0; i < ships.size(); i++){
            Ship ship = ships.get(i);
            Position position = positions.get(i);

            boolean shipPlaced = board.placeShip(ship, position);

			if(!shipPlaced)
			{
				board.reset();
				return null;
			}
		}
		return board;
	}
	
	private boolean isDead() {
		if(board.shipCount() == 0)
			return true;
		return false;
	}
	

}
