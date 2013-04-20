package mm19.runner;

import mm19.testclient.TestClient;
import mm19.testclient.alex.TestClientAlex;

public class TestClientRunner {
	public static void main(String args[]) {
		TestClient tc = new TestClientAlex();
		tc.connect();
	}
}
