package mm19.server;

/**
 * Thread runner for individual client requests.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;

import org.json.JSONObject;

public class RequestRunnable implements Runnable {

	protected Socket clientSocket = null;
	protected API mAPI = null;
	protected ObjectInputStream in = null;
	protected ObjectOutputStream out = null;
	private boolean authed;
	
	public RequestRunnable(Socket clientSocket, API api) {
		this.clientSocket = clientSocket;
		api = mAPI;
		authed = false;
		
		try {
			in = new ObjectInputStream(clientSocket.getInputStream());
			out = new ObjectOutputStream(clientSocket.getOutputStream());
		}
		catch(Exception e) {
			Server.serverLog.log(Level.WARNING, "Error intializing input/output stream for client's socket.");
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
		while(true) {
			if(!authed) {
				send("Please send initial authentication information");
				
			}
			
			if(!send(""))
				break;
		}
			
			
		// Close up the client socket
			
		Server.serverLog.log(Level.INFO, "Dropping Player");
		
		if(out != null) {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(in != null) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
	}
	
	private boolean send(String data) {
		boolean successful = true;
		try {
			out.write(data.getBytes());
			out.flush();
		} catch (IOException e) {
			Server.serverLog.log(Level.WARNING, "Error sending message:\n" + data);
			successful = false;
			e.printStackTrace();
		}
		return successful;
		
	}
	
	private JSONObject receive() {
		return null;
	}
	
}

