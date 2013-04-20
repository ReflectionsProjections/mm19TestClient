package mm19.testclient.alex;

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONException;
import org.json.JSONObject;

import mm19.response.ServerResponse;
import mm19.testclient.TestClient;

public class TestClientAlex extends TestClient{
	
	public TestClientAlex() {
		super("SuperPwnageFest");
	}
	
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
				
				ship.put("xCoord", (i+1)*10);
				ship.put("yCoord", (i+1)*10);
				ships.add(ship);
			}
			
			obj.put("ships", ships);
		}
		catch(JSONException e) {
			
		}
		
		return obj;
	}
	
	@Override
	public void processResponse(ServerResponse sr) {
		System.out.println(sr.toString());
	}
	
	
}
