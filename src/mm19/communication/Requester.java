package mm19.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import mm19.response.ServerResponse;
import mm19.response.ServerResponseException;
import mm19.testclient.TestClient;
import mm19.testclient.TestClientException;

import org.json.JSONException;
import org.json.JSONObject;


public class Requester extends Thread{
	private TestClient testClient;
	private Socket serverSocket;
	private BufferedReader in;
	private boolean init;
	public Requester(TestClient tc, Socket ss) {
		testClient = tc;
		serverSocket = ss;
		init = false;
	}

	@Override
	public void run(){
		while(true) {
			try {
				// Prepare the input stream
				in = new BufferedReader( new InputStreamReader(serverSocket.getInputStream()));
				
				// Block until the server sends you something
				String s = in.readLine();
				
				// Formulate a ServerResponse object from the server's response
				ServerResponse sr = new ServerResponse(new JSONObject(s));
				// Call the appropriate method.
				if(sr.responseCode == 100) {
					System.out.println(testClient.name + "'s turn");
					JSONObject obj = testClient.prepareTurn(sr);
					testClient.respondToServer(obj);
				}
				else if(sr.responseCode == 200 || sr.resources == 400) {
					if(!init) {
						testClient.processInitialReponse(sr);
					} else {
						testClient.processResponse(sr);
					}
				}
				else if(sr.responseCode == 418) {
					testClient.handleInterrupt(sr);
				}
				else {
					throw new TestClientException("Unrecognized responseCode " + sr.responseCode);
				}
			}
			catch(UnknownHostException e) {
				e.printStackTrace();
				break;
			}
			catch(IOException e) {
				e.printStackTrace();
				break;
			} catch (ServerResponseException e) {
				e.printStackTrace();
				break;
			} catch (JSONException e) {
				e.printStackTrace();
				break;
			} catch (TestClientException e) {
				// It's probably best not to break if the server sent you an invalid responseCode,
				// just silently catch for now.
			}
		}
	}
}
