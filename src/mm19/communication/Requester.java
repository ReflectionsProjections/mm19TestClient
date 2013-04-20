package mm19.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import mm19.response.ServerResponse;
import mm19.response.ServerResponseException;
import mm19.testclient.TestClient;

import org.json.JSONException;
import org.json.JSONObject;


public class Requester extends Thread{
	private TestClient testClient;
	private Socket serverSocket;
	private BufferedReader in;
	
	public Requester(TestClient tc, Socket ss) {
		testClient = tc;
		serverSocket = ss;
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				in = new BufferedReader( new InputStreamReader(serverSocket.getInputStream()));
				String s = in.readLine();
				testClient.processResponse(new ServerResponse(new JSONObject(s)));
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
