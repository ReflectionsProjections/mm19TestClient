package mm19.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;

import mm19.game.Action;
import mm19.game.Engine;
import mm19.server.API;
import mm19.server.ShipData;

import org.junit.Before;
import org.junit.Test;

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
			assertTrue(-1 != testGame.playerSet(genPlayer(),null));
			assertFalse(-1 != testGame.playerSet(genBrokenPlayer(),null));
			assertFalse(-1 != testGame.playerSet(genNoMainShip(),null));
			assertTrue(-1 != testGame.playerSet(genPlayer(),null));
			
		}
		
		@Test
		public void testFire(){
			
		}
		
		@Test
		public void testMove(){
			testGame.playerSet(genPlayer(), "A");
			testGame.playerSet(genPlayer(), "B");
			testGame.playerTurn("A", genMove());
		}
		
		@Test
		public void testSonar(){
			
		}
		
		@Test
		public void testBurst(){
			
		}
		
		@Test
		public void testTurn(){
			
		}
		public static ArrayList<ShipData> genPlayer(){
			ArrayList<ShipData> shipData = new ArrayList<ShipData>();
			shipData.add(new ShipData(0, 0, Engine.MAINSHIP, 6, 6, "H"));
			shipData.add(new ShipData(0, 1, Engine.DESTROYER, 4, 4, "H"));
			return shipData;
		}
		public static ArrayList<ShipData> genBrokenPlayer(){
			ArrayList<ShipData> shipData = new ArrayList<ShipData>();
			shipData.add(new ShipData(0, 0, Engine.MAINSHIP, 0, 0, "H"));
			shipData.add(new ShipData(0, 0, Engine.DESTROYER, 4, 4, "H"));
			return shipData;
		}
		public static ArrayList<ShipData> genNoMainShip(){
			ArrayList<ShipData> shipData = new ArrayList<ShipData>();
			shipData.add(new ShipData(0, 0, Engine.DESTROYER, 4, 4, "H"));
			return shipData;
		}
		public static ArrayList<Action> genMove(){
			ArrayList<Action> move = new ArrayList<Action>();
			move.add(new Action(1,"MH",4,5,0));
			return move;
		}
}
