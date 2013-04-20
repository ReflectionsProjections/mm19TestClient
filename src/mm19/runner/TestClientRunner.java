package mm19.runner;

import mm19.testclient.TestClient;
import mm19.testclient.TestClientException;
import mm19.testclient.alex.TestClientAlex;

public class TestClientRunner {
	public static void main(String args[]) {
		TestClient tc1 = new TestClientAlex();
		TestClient tc2 = new TestClientAlex();
		try {
			tc1.connect();
			tc2.connect();
		} catch (TestClientException e) {
			e.printStackTrace();
		}
	}
}
