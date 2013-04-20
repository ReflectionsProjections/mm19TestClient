package mm19.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import mm19.response.ServerResponse;
import mm19.response.ServerResponseException;
import mm19.testclient.TestClient;

import org.json.JSONException;
import org.json.JSONObject;


public class Requester implements Runnable{
	private TestClient tc;
	private Socket requestSocket;
	private BufferedReader in;
	private PrintWriter out;
	private String message;
	private String playerName;
	
	public Requester(TestClient tc) {
		
		playerName = tc.name;
	}
	
	public ServerResponse connectToServer(JSONObject obj) {
		try {
			System.out.println(playerName + " is trying to connect to the server.");

			requestSocket = new Socket("localhost", 6969);
			System.out.println(playerName + " connected!");
			
			out = new PrintWriter(requestSocket.getOutputStream(), true);
			out.flush();
			
			in = new BufferedReader( new InputStreamReader(requestSocket.getInputStream()));
			
			message = obj.toString();
			System.out.println(message);
			
			out.println(message);
			
			String s = in.readLine();
			ServerResponse sr = new ServerResponse(new JSONObject(s));
			System.out.println(sr.toString());
			
			return sr;
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ServerResponseException e) {
			e.printStackTrace();
		}
		System.out.println(playerName + " has been authenticated!");
		
		return null;
	}

	@Override
	public void run() {
		while(true) {
			try {
				in = new BufferedReader( new InputStreamReader(requestSocket.getInputStream()));
				String s = in.readLine();
				tc.processResponse(new ServerResponse(new JSONObject(s)));
			}
			catch(UnknownHostException e) {
				e.printStackTrace();
				break;
			}
			catch(IOException e) {
				e.printStackTrace();
				break;
			} catch (ServerResponseException e) {
				e.printStackTrace();
				break;
			} catch (JSONException e) {
				e.printStackTrace();
				break;
			}
		}
	}
}
