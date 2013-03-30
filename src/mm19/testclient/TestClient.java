package mm19.testclient;

public class TestClient {

		public static void run(String[] args) {
			System.out.println("Trying to connect to server.");
			
			Requester r = new Requester();
			r.connectToServer();
		}
}
