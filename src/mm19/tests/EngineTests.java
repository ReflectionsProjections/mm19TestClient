package mm19.tests;

import java.util.ArrayList;

import mm19.TestUtilities;
import mm19.game.board.Position;
import mm19.game.player.Player;
import mm19.game.ships.DestroyerShip;
import mm19.game.ships.MainShip;
import mm19.game.ships.PilotShip;
import mm19.game.ships.Ship;
import org.junit.Before;
import org.junit.Test;

import mm19.game.Action;
import mm19.game.Engine;
import mm19.server.API;
import mm19.server.ShipData;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

// Note: please run all tests individually, due to the nondeterministic 
// nature of junit, some may fail to to junit presets, not failure in the 
// function - the errors stem from ship ID wrangling

//TODO Please replace the magic numbers in this file with Constants or named variables.
public class EngineTests {
		Engine testGame;
		API testapi;
		@Before 
		public void method()
		{
			testapi = mock(API.class);
			testGame = new Engine();
		}
		
		@Test
		public void testConstructor(){
		    testGame = new Engine();
			assertTrue(-1 != testGame.playerSet(genPlayer(),null));
			assertFalse(-1 != testGame.playerSet(genBrokenPlayer(),null));
			assertFalse(-1 != testGame.playerSet(genNoMainShip(),null));
			assertTrue(-1 != testGame.playerSet(genPlayer(),null));
			assertTrue(-1 != testGame.playerSet(genPlayer(),null));
			
		}
		
		@Test
		public void testFire(){
		    testGame = new Engine();
		    testGame.playerSet(genPlayer(), "A");
            testGame.playerSet(genPlayer(), "B");
            testGame.playerTurn("A", genFire()); // should hit 
            testGame.playerTurn("A", genFire()); // should 1 hit 1 not enough money
            testGame.playerTurn("A", genFire()); // should any hit not enough money
            testGame.playerTurn("A", genFire()); // should 1 hit kill 1 not enough money
            testGame.playerTurn("A", genFire()); // should 1 hit miss 1 not enough money
		}
		
		@Test
		public void testMove(){

		    testGame = new Engine();
			testGame.playerSet(genPlayer(), "A");
			testGame.playerSet(genPlayer(), "B");
			testGame.playerTurn("A", genMove()); //Cannot move Destroyer
			testGame.playerTurn("A", new ArrayList<Action>()); //wait
			testGame.playerTurn("A", new ArrayList<Action>()); //wait
			testGame.playerTurn("A", genMove()); //CAN move Destroyer

		}
		
		@Test
		public void testSonar(){
		    testGame = new Engine();
		    testGame.playerSet(genPlayer(), "A");
            testGame.playerSet(genPlayer(), "B");
            testGame.playerTurn("A", genSonar()); // should return positive
		}
		
		@Test
		public void testBurst(){
		    testGame = new Engine();
            testGame.playerSet(genPlayer(), "A");
            testGame.playerSet(genPlayer(), "B");
            testGame.playerTurn("A", genBurst()); // not enough money
            testGame.playerTurn("A", genBurst()); // should go boom
            testGame.playerTurn("A", genBurst()); // not enough money
            testGame.playerTurn("A", genBurst()); // not enough money
			
		}
		
		public static ArrayList<ShipData> genPlayer(){
			ArrayList<ShipData> shipData = new ArrayList<ShipData>();
			shipData.add(new ShipData(0, 0, MainShip.IDENTIFIER, 6, 6, Position.Orientation.HORIZONTAL));

			shipData.add(new ShipData(0, 1, DestroyerShip.IDENTIFIER, 4, 4, Position.Orientation.HORIZONTAL));
			shipData.add(new ShipData(0, 2, PilotShip.IDENTIFIER, 2, 2, Position.Orientation.HORIZONTAL));

			return shipData;
		}
		public static ArrayList<ShipData> genBrokenPlayer(){
			ArrayList<ShipData> shipData = new ArrayList<ShipData>();
			shipData.add(new ShipData(0, 0, MainShip.IDENTIFIER, 0, 0, Position.Orientation.HORIZONTAL));
			shipData.add(new ShipData(0, 0, DestroyerShip.IDENTIFIER, 4, 4, Position.Orientation.HORIZONTAL));
			return shipData;
		}
		public static ArrayList<ShipData> genNoMainShip(){
			ArrayList<ShipData> shipData = new ArrayList<ShipData>();
			shipData.add(new ShipData(0, 0, DestroyerShip.IDENTIFIER, 4, 4, Position.Orientation.HORIZONTAL));
			return shipData;
		}

		public static ArrayList<Action> genMove(){
			ArrayList<Action> move = new ArrayList<Action>();
			//the first number in the Action(s) here are magic dependent on the known placement w/n the board
			//and this one is wrong though move works
			move.add(new Action(1, Action.Type.MOVE_HORIZONTAL, 4, 5, 0));
			                                    
			return move;
		}
		
		public static ArrayList<Action> genFire(){
            ArrayList<Action> move = new ArrayList<Action>();
            //the first number in the Action(s) here are magic dependent on the known placement w/n the board
            move.add(new Action(0, Action.Type.SHOOT, 4, 4, 0));
            move.add(new Action(1, Action.Type.SHOOT, 4, 4, 0));
            return move;
        }
		public static ArrayList<Action> genSonar(){
            ArrayList<Action> move = new ArrayList<Action>();
            //the first number in the Action(s) here are magic dependent on the known placement w/n the board
            move.add(new Action(23, Action.Type.SONAR, 4, 4, 0));
            return move;
        }
		public static ArrayList<Action> genBurst(){
            ArrayList<Action> move = new ArrayList<Action>();
            //the first number in the Action(s) here are magic dependent on the known placement w/n the board
            move.add(new Action(19, Action.Type.BURST_SHOT, 4, 4, 0));
            return move;
        }


    @Test
    public void testBreakTie() {
        Player player1 = TestUtilities.initializePlayer();
        Player player2 = TestUtilities.initializePlayer();

        //All things equal, player in second argument wins.
        assertEquals(player2, Engine.breakTie(player1, player2));
        assertEquals(player1, Engine.breakTie(player2, player1));

        //Resource count has precedence over equal conditions.
        player2.takeResources(1);
        assertEquals(player1, Engine.breakTie(player1, player2));
        assertEquals(player1, Engine.breakTie(player2, player1));

        //Health of resource generating ships has precedence over resource count.
        Ship damagedShip = null;
        ArrayList<Ship> ships = player1.getBoard().getShips();
        for( Ship ship : ships ){
            if(ship.canGenerateResources()){
                damagedShip = ship;
                damagedShip.applyDamage(1);
                break;
            }
        }
        assertNotNull("BrokenTest: Player1 has no resource generating ships", damagedShip);

        assertEquals(player2, Engine.breakTie(player2, player1));
        assertEquals(player2, Engine.breakTie(player1, player2));
    }

}
