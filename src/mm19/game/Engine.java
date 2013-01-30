package mm19.game;

import mm19.game.Constants;

/**
 * @author mm19
 *
 * This will put all the pieces of the game together, and actually make things run.
 */
public class Engine implements Runnable{

	/**
	 * the run function is called by the server (or API?) to start the game.
	 */
	@Override
    public void run(){
        System.out.println("And so it begins...");
        createBoard();
        mainLoop();
    }
	
	/**
	 * createBoard will set up the variable needed to start the game, using defaults.
	 */
	private void createBoard(){
		
	}
	
	/**
	 * This function runs the actual game.
	 * Should stop when the game ends.
	 */
	private void mainLoop(){
		while(true){
			playerTurn();
			returnData();
			playerTurn();
			returnData();
		}
		
	}
	
	/**
	 * This function will attempt all of the player's chosen actions for the turn
	 */
	private void playerTurn(){
		
	}
	
	/**
	 * This function will do the requested actions by the player
	 */
	private void playerAction(){
		
	}
	
	/**
	 * This function communicates with the server (through the API), sending data about a turn to the player about to move
	 */
	private void returnData(){
		
	}
	
	/**
	 * This function sets up the player's pieces on the board as specified
	 */
	public void playerSet(){
		
	}

}
