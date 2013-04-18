package mm19.testclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONException;
import org.json.JSONObject;


public class Requester implements Runnable{
	private Socket requestSocket;
	private BufferedReader in;
	private PrintWriter out;
	private String message;
	private String playerName;
	
	Requester(String pn) {
		playerName = pn;

	}
	
	public boolean connectToServer() {
		try {
			System.out.println(playerName + " is trying to connect to the server.");

			requestSocket = new Socket("localhost", 6969);
			System.out.println(playerName + " connected!");
			
			out = new PrintWriter(requestSocket.getOutputStream(), true);
			out.flush();
			
			in = new BufferedReader( new InputStreamReader(requestSocket.getInputStream()));
			
			JSONObject obj = new JSONObject();
			
			obj.put("playerName", playerName);
			
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
			message = obj.toString();
			System.out.println(message);
			
			out.println(message);
			
			String s = in.readLine();
			System.out.println(s);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(playerName + " has been authenticated!");
		
		return true;
	}

	@Override
	public void run() {
		/*
		if(connectToServer()) {
			 
		}
		*/
		try {
			requestSocket = new Socket("localhost", 6969);
			System.out.println("Connected to server!");

		}
		catch(UnknownHostException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
