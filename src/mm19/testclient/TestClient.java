package mm19.testclient;

import mm19.communication.Requester;
import mm19.response.ServerResponse;

import org.json.JSONObject;

public abstract class TestClient {
		
	public String name;
	protected Requester requester;
	
	public TestClient(String n) {
		System.out.println("Creating New TestClient: " + n);
		name = n;
		requester = new Requester(this);
	}
	
	public void connect() {
		JSONObject obj = setup();
		ServerResponse sr = requester.connectToServer(obj);
		processInitialResponse(sr);
	}
	
	/*
	 * Override these methods below
	 */
	public abstract JSONObject setup();
	public abstract void processInitialResponse(ServerResponse sr);
	public abstract void processResponse(ServerResponse sr);
}
