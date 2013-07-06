package mm19.server;

import java.util.TimerTask;

import mm19.api.API;

public class ServerTimerTask extends TimerTask{
	public static int PLAYER_TO_NOTIFY = 0;
	@Override
	public void run() {
		API.notifyTurn(PLAYER_TO_NOTIFY);
	}

}
