package mm19.runner;

import mm19.testclient.TestClient;

public class TestClientRunner {
	public static void main(String args[]) {
		TestClient tc = new TestClient("Alex");
		tc.connect();

	}
}
