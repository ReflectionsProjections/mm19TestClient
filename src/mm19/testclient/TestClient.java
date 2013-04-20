package mm19.testclient;

import java.io.IOException;
import java.net.Socket;

import mm19.communication.Requester;
import mm19.communication.Responder;
import mm19.response.ServerResponse;

import org.json.JSONObject;

public abstract class TestClient {
		
	public String name;
	protected Requester requester;
	protected Responder responder;
	protected Socket serverSocket;
	
	public TestClient(String n) {
		System.out.println("Creating New TestClient: " + n);
		name = n;
	}
	
	public void connect() throws TestClientException{
		// Make a new requester and get the server socket
		Socket serverSocket = connectToServer();
		
		if(serverSocket == null) {
			throw new TestClientException(name + "could not connect to the server.");
		}
		
		// Make a responder from the newly acquired socket
		requester = new Requester(this, serverSocket);
		responder = new Responder(serverSocket);
		
		// Setup the initial data to send to the server
		JSONObject obj = setup();
		responder.sendResponse(obj.toString());
		
		requester.start();
	}
	
	public Socket connectToServer() {
		try {
			System.out.println(name + " is trying to connect to the server.");

			serverSocket = new Socket("localhost", 6969);
			System.out.println(name + " connected!");
			
			return serverSocket;
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	/*
	 * Override these methods below
	 */
	public abstract JSONObject setup();
	public abstract void processResponse(ServerResponse sr);
}
