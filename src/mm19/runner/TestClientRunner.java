package mm19.runner;

import mm19.testclient.TestClient;
import mm19.testclient.TestClientException;
import mm19.testclient.dummy.TestClientDummy;


public class TestClientRunner {
	public static void main(String args[]) {
		TestClient tc1 = new TestClientDummy("SuperPwnageFest");
		TestClient tc2 = new TestClientDummy("Str4y3dF4t4l1tY");
		
		try {
			tc1.connect();
			tc2.connect();
		} catch (TestClientException e) {
			e.printStackTrace();
		}
	}
}
