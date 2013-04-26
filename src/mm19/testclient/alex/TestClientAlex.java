package mm19.testclient.alex;

import java.util.ArrayList;
import java.util.Collection;

import mm19.objects.HitReport;
import mm19.objects.Ship;
import mm19.objects.Ship.ShipType;
import mm19.objects.ShipAction;
import mm19.response.ServerResponse;
import mm19.testclient.TestClient;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Contains logic for Alex's test client, feel free to disregard any
 * logic you don't agree with. I'll see you on the battle field. >:)
 * 
 * @author Flewp
 *
 */
public class TestClientAlex extends TestClient{
	
	private Ship[] ships;
	int resources;
	String playerToken;
	
	TestClientAlexLogic tcal;
	
	public TestClientAlex(String name) {
		super(name);
		tcal = new TestClientAlexLogic();
	}
	
	/**
	 * Making my ships impossibly hard to find.
	 */
	@Override
	public JSONObject setup() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("playerName", this.name);
			
			JSONObject mainShip = new JSONObject();
			mainShip.put("xCoord", 5);
			mainShip.put("yCoord", 5);
			mainShip.put("orientation", "H");
			obj.put("mainShip", mainShip);
			
			Collection<JSONObject> ships = new ArrayList<JSONObject>();
			for(int i = 0; i < 4; i++) {
				JSONObject ship = new JSONObject();
				
				if(i % 2 == 0) {
					ship.put("type", "D");
					ship.put("orientation", "H");
				} else {
					ship.put("type", "P");
					ship.put("orientation", "V");
				}
				
				int xCoord = (int) (Math.random()*90 + 5);
				int yCoord = (int) ((Math.random()*5) + ((i+1) * TestClient.BOARD_WIDTH/6));
				ship.put("xCoord", xCoord);
				ship.put("yCoord", yCoord);
				ships.add(ship);
			}
			
			obj.put("ships", ships);
		}
		catch(JSONException e) {
			
		}
		
		return obj;
	}
	
	/**
	 * Setting up my impenetrable defense
	 */
	@Override
	public void processInitialReponse(ServerResponse sr) {
		resources = sr.resources;
		ships = sr.ships;
		playerToken = sr.playerToken;
	}
	
	/**
	 * Process my response in O(n^2) like a boss.
	 */
	@Override
	public void processResponse(ServerResponse sr) {
		for(HitReport hr : sr.hitReport) {
			if(hr.hit) {
				System.out.println("I hit something...? :)");
			}
		}
		for(int i = 0; i < sr.ships.length; i++) {
			Ship currResponseShip = sr.ships[i];
			
			for(int j = 0; j < ships.length; j++) {
				Ship currShip = ships[j];
				
				// If we're talking about the same boat...
				if(currShip.ID == currResponseShip.ID) {
					
					currShip.health = currResponseShip.health;
					
					// You sunk my battleship!
					if(currShip.health <= 0) {
						tcal.removeShip(j);
					}
					break;
				}
			}
		}
	}

	/**
	 * Preparing my incredible offense
	 */
	@Override
	public JSONObject prepareTurn(ServerResponse sr) {
		JSONObject turnObj = new JSONObject();
		try {
			Collection<JSONObject> actions = new ArrayList<JSONObject>();
			
			for(Ship ship : ships) {
				if(ship.type == ShipType.Pilot) {
					actions.add(new ShipAction(ship.ID).toJSONObject());
				} else {
					int xCoord = (int) (Math.random()*TestClient.BOARD_WIDTH);
					int yCoord = (int) (Math.random()*TestClient.BOARD_WIDTH);
					
					ShipAction tempAction = new ShipAction(ship.ID, xCoord, yCoord, ShipAction.Action.Fire, 0);
					actions.add(tempAction.toJSONObject());
				}
			}
			turnObj.put("playerToken", playerToken);
			turnObj.put("shipActions", actions);
			return turnObj;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return turnObj;
	}

	/**
	 * wat
	 */
	@Override
	public void handleInterrupt(ServerResponse sr) {
		
	}

	/**
	 * Misc Logic
	 * @author Flewp
	 *
	 */
	private class TestClientAlexLogic {
		
		public void removeShip(int position) {
			Ship[] newShips = new Ship[ships.length - 1];
			for(int i = 0; i < ships.length; i++) {
				if(i == position) {
					i++;
				}
				newShips[i] = ships[i];
			}
			
			ships = newShips;
		}
	}
	
}
