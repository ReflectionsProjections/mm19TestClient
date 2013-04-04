package mm19.testclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

public class Requester {
	Socket requestSocket;
	BufferedReader in;
	PrintWriter out;
	String message;
	String playerName;
	String token;
	
	Requester(String playerName) {
		this.playerName = playerName;
	}
	
	void connectToServer() {
		try {
			byte[] b = new byte[10000];
			requestSocket = new Socket("localhost", 6969);
			System.out.println("Connected to server!");
			
			
			try{ 
				JSONObject msg = new JSONObject();
				msg.put("playerName", playerName);
				String m = msg.toString();
				
				in = new BufferedReader(new InputStreamReader(requestSocket.getInputStream()) );
				String s = in.readLine();
				
				out = new PrintWriter(requestSocket.getOutputStream());
				out.flush();
				System.out.println(s);
				System.out.println(m);
				out.println(m);
				out.flush();
				s = in.readLine();
				System.out.println(s);
					
			}
			catch (JSONException e) {
				e.printStackTrace();
			}

		}
		catch(UnknownHostException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
