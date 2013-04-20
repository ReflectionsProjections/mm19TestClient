package mm19.objects;

import org.json.JSONException;
import org.json.JSONObject;

public class Ship {
	public int ID;
	public int health;
	public String type;
	public int xCoord;
	public int yCoord;
	public String orientation;
	
	public Ship(int i, int h, String t, int x, int y, String o) {
		ID = i;
		health = h;
		type = t;
		xCoord = x;
		yCoord = y;
		orientation = o;
	}
	
	public Ship(JSONObject obj) throws JSONException{
		ID = obj.getInt("ID");
		health = obj.getInt("health");
		type = obj.getString("type");
		xCoord = obj.getInt("xCoord");
		yCoord = obj.getInt("yCoord");
		orientation = obj.getString("orientation");
	}
	
	public void takeDamage(int damage) {
		health -= damage;
	}
	
	public void move(int x, int y) {
		xCoord = x;
		yCoord = y;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(type.equals("M")) {
			sb.append("Main ");
		} else if (type.equals("D")) {
			sb.append("Destroyer ");
		} else if (type.equals("P")) {
			sb.append("Pilot " );
		}
		
		sb.append("Ship " + ID + " at (" + xCoord + ", " + yCoord + ")\n");
		sb.append('\t' + "Health : " + health + '\n');
		sb.append('\t' + "Orientation: " + orientation + '\n');
		
		return sb.toString();
		
	}
}
