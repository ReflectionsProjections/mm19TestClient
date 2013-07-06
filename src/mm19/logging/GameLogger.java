package mm19.logging;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import mm19.game.board.Board;

import org.json.JSONException;
import org.json.JSONObject;

//
// makes a new logger to write to a file if the url is not valid then
// it will become a dummy logger
//
public class GameLogger {
	FileWriter logFile;
	BufferedWriter bw;
	public GameLogger(String LogUrl) {
		try {
			this.logFile = new FileWriter(LogUrl);
			bw = new BufferedWriter(logFile);
			
			// Logging the initial line for the visualizer
			JSONObject obj = new JSONObject();
			obj.put("size",Board.DEFAULT_WIDTH );
			log(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
			System.out
					.println("Invalid log URL given, or bad JSON, no information will be saved!");
			logFile = null;
		}
		catch (JSONException e) {
			e.printStackTrace();
			System.out
					.println("Invalid log URL given, or bad JSON, no information will be saved!");
			logFile = null;
		}

	}

	// print string to file
	public void log(String outPut) {
		if (logFile == null) {
			return;
		}
		try {
			bw.write(outPut);
			bw.write("\n");
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"WTF the file is open but it failed to write");
		}

	}
	public void close()
	{
		try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	 protected void finalize() {
	        close();
	        System.out.println("error you forgot to close the logfile handler");
	    }
}
