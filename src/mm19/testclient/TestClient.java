package mm19.testclient;

public class TestClient {

	public TestClient() {
		
	}
	
	public void run() {
		System.out.println("Trying to connect to server.");
		
		Requester r = new Requester("Team Pwnage");
		r.connectToServer();
	}
}
