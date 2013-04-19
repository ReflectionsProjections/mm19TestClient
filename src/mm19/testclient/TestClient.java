package mm19.testclient;

public class TestClient {
		
	private String name;
	private Requester requester;
	
	public TestClient(String n) {
		System.out.println("Creating New TestClient: " + n);
		name = n;
		requester = new Requester(name);
	}
	
	public void connect() {
		requester.connectToServer();
	}
}
