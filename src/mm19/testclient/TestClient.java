package mm19.testclient;

import java.io.IOException;
import java.net.Socket;

import mm19.communication.Requester;
import mm19.communication.Responder;
import mm19.response.ServerResponse;

import org.json.JSONObject;

public abstract class TestClient {
	
	public static final int BOARD_WIDTH = 100;
	public static int TURN = 0;
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
	
	public void respondToServer(JSONObject obj) {
		responder.sendResponse(obj.toString());
	}
	
	/*
	 * Override these methods below
	 */
	
	/**
	 * This should prepare the initial layout of you ships, and any other pre-connecting logic
	 * @return JSONObject containing the initial layout of your ships
	 */
	public abstract JSONObject setup();
	
	/**
	 * This is the initial response, so you can set a ships health and such.
	 */
	public abstract void processInitialReponse(ServerResponse sr);
	
	/**
	 * When the server returns a 200 or 400 responseCode, it means your turn has been processed, 
	 * and it will call this function.
	 * @param sr - the server's response
	 */
	public abstract void processResponse(ServerResponse sr);
	
	/**
	 * When the server returns a 100 responseCode, it means it's your turn.
	 * @return JSONObject containing your turn
	 */
	public abstract JSONObject prepareTurn(ServerResponse sr);
	
	/**
	 * When the server sends an interrupt, it means you've timed out. This function is called to handle it.
	 * @param sr
	 * @return
	 */
	
	public abstract void handleInterrupt(ServerResponse sr);
}
