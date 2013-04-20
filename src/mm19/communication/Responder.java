package mm19.communication;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Responder {
	private Socket serverSocket;
	
	public Responder(Socket s) {
		serverSocket = s;
	}
	
	public boolean sendResponse(String message) {
		PrintWriter out;
		try {
			out = new PrintWriter(serverSocket.getOutputStream(), true);
			out.flush();
			out.println(message);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
}
