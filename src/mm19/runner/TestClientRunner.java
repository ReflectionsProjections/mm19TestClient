package mm19.runner;

import mm19.testclient.TestClient;
import mm19.testclient.TestClientException;
import mm19.testclient.alex.TestClientAlex;

public class TestClientRunner {
	public static void main(String args[]) {
		TestClient tc1 = new TestClientAlex("SuperPwnageFest");
		TestClient tc2 = new TestClientAlex("Str4y3dF4t4l1tY");
		
		try {
			tc1.connect();
			tc2.connect();
		} catch (TestClientException e) {
			e.printStackTrace();
		}
	}
}
