package mm19.testclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Requester {
	Socket requestSocket;
	ObjectOutputStream out;
	ObjectInputStream in;
	String message;
	
	Requester() {
		
	}
	
	void connectToServer() {
		try {
			requestSocket = new Socket("localhost", 6969);
			System.out.println("Connected to server!");
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			
			do {
				try{ 
					message = (String)in.readObject();
					System.out.println(message);
				}
				catch(ClassNotFoundException e) {
					e.printStackTrace();
				}
			} while(!message.equals("bye"));
		}
		catch(UnknownHostException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
