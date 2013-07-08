package mm19.server;

/**
 * MechMania XIX server.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import mm19.api.API;
import mm19.api.PlayerTurn;
import mm19.exceptions.APIException;
import mm19.game.Constants;
import mm19.logging.GameLogger;

import org.jasypt.salt.RandomSaltGenerator;
import org.jasypt.util.text.BasicTextEncryptor;
import org.json.JSONObject;

public class Server {
	// Static constant logger declaration
	public static final Logger serverLog = Logger.getLogger(Server.class
			.getName());

	// API
	private static API api;
	// Server constants for now, configurable later
	public static final int PORT = 6969;

	public static final int MAX_THREADS = 8;
	public static final String LOG_PATH = "server_log.txt";
	public static final Level LOG_LEVEL = Level.INFO;

	// Bookkeeping
	private static boolean[] connected;
	private static int playersConnected;

	private static GameLogger visualizerLog = null;
	private static String visualizerLogURL = "log.out";

	// Sockets
	private static ServerSocket socket = null;
	private static Socket[] clientSockets;

	// Concurrent stuff
	private static ExecutorService threadPool = null;
	private static boolean starting = false;

	// For communication with the connecting client
	private static BufferedReader in;
	private static PrintWriter out;

	// Security
	private static String[] playerToken;
	private static BasicTextEncryptor bte;

	// Interrupts
	public static final int TURN_TIME_LIMIT = 10000;
	private static Timer interruptTimer;

	public static void main(String[] args) {

		// Set up the server, including logging and socket to listen on
		boolean success = initServer();

		if (!success) {
			serverLog.log(Level.SEVERE,
					"Fatal error: unable to start server. Bailing out.");
			System.exit(1);
		}

		visualizerLog = new GameLogger(Server.visualizerLogURL);

		// Run the server until the game ends
		run();

		// End the server and clean up

	}

	private static boolean initServer() {

		interruptTimer = new Timer();
		clientSockets = new Socket[Constants.PLAYER_COUNT];

		playerToken = new String[Constants.PLAYER_COUNT];
		for (int i = 0; i < playerToken.length; i++) {
			playerToken[i] = "";
		}

		connected = new boolean[Constants.PLAYER_COUNT];
		for (int i = 0; i < connected.length; i++) {
			connected[i] = false;
		}

		bte = new BasicTextEncryptor();
		bte.setPassword(new String((new RandomSaltGenerator().generateSalt(10))));

		// TODO: Set up logging to a file
		serverLog.setLevel(LOG_LEVEL);

		// Set up the socket
		try {
			socket = new ServerSocket(PORT);
			serverLog.log(Level.INFO, "Server listening on port: " + PORT);
		} catch (IOException e) {
			serverLog.log(Level.SEVERE, "Failed to open socket on port: "
					+ PORT, e);
			return false;
		}

		// Set up the thread pool
		threadPool = Executors.newFixedThreadPool(MAX_THREADS);

		return true;
	}

	private static void run() {
		int currPlayerID = -1;
		serverLog.log(Level.INFO, "Starting server run loop");
		starting = true;
		Socket clientSocket = null;
		// Long polling and connecting/authenticating new players as they
		// connect.
		while (starting) {
			try {
				// Listen for a new connection
				clientSocket = socket.accept();

				// players are already connected
				currPlayerID = getValidPlayerID();
				if (currPlayerID == -1) {
					serverLog.log(Level.WARNING,
							"The server needs to be restarted");
					continue;
				}

				serverLog.log(Level.INFO, "Connection received.");
				serverLog.log(Level.INFO,
						"Waiting on player for new player data.");

				in = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));
				out = new PrintWriter(clientSocket.getOutputStream(), true);

				// Blocks until it gets a response from the client, hopefully a
				// JSONObject with a playerName key.
				String s = in.readLine();

				serverLog.log(Level.INFO, "Received player info");

				String token = new String(
						(new RandomSaltGenerator().generateSalt(20)));

				try {
					api.addPlayer(s, encrypt(token));

					serverLog.log(Level.INFO,
							"Successfully initialized player!");
					clientSockets[currPlayerID] = clientSocket;
					playerToken[currPlayerID] = token;
					connected[currPlayerID] = true;

					serverLog.log(Level.INFO, "Players connected: "
							+ ++playersConnected);

					RequestRunnable task = new RequestRunnable(clientSocket,
							playerToken[currPlayerID], currPlayerID);
					threadPool.execute(task);

					if (api.getStarted()) {
						startGame();
					}

				} catch (APIException e) {
					serverLog
							.log(Level.INFO,
									"Couldn't initialize ships for player, dropping connection");
					PlayerTurn turn = new PlayerTurn();
					turn.addError(e.getMessage());
					out.println(turn.toJSON());
					out.flush();
					continue;
				}
			} catch (IOException e) {
				serverLog.log(Level.SEVERE, "Unexpected error accepting "
						+ "client connection.", e);
			}
		}

	}

	private static int getValidPlayerID() {
		if (!connected[0])
			return 0;
		else if (!connected[1])
			return 1;

		return -1;
	}

	// Returns 0 if player 1 is authenticated, returns 1 if player 2 is
	// authenticated, returns -1 if neither.
	private static int authenticate(String token) {
		if (connected[0]) {
			if (bte.decrypt(token).equals(playerToken[0])) {
				return 0;
			}
		}
		if (connected[1]) {
			if (bte.decrypt(token).equals(playerToken[1])) {
				return 1;
			}
		}
		return -1;
	}

	private static void startGame() {
		PlayerTurn turn = api.getPlayerTurn(0);
		turn.setNotify();
		sendToPlayer(turn.toJSON(), playerToken[0]);
		turn.resetTurn();

		ServerInterruptTask.PLAYER_TO_INTERRUPT = 0;
		interruptTimer.schedule(new ServerInterruptTask(), TURN_TIME_LIMIT);
	}

	private static String encrypt(String s) {
		return bte.encrypt(s);
	}

	public static synchronized void sendToPlayer(JSONObject json, String token) {
		// Authenticate the player.
		int playerID = authenticate(token);

		if (playerID == -1) {
			serverLog
					.log(Level.WARNING,
							"Couldn't authenticate player when trying to send a message");
			return;
		}

		try {
			out = new PrintWriter(clientSockets[playerID].getOutputStream(),
					true);
			out.println(json);
			out.flush();

		} catch (IOException e) {
			serverLog.log(Level.WARNING, "Error communicating with player "
					+ playerID);
			e.printStackTrace();
		}
	}

	public static synchronized void submitTurn(JSONObject obj, String token) {
		int playerID = authenticate(token);
		if (playerID == -1) {
			serverLog.log(Level.WARNING,
					"Player not authenticated when trying to send to API");
			return;
		}

		// Rejecting the player's request because it's not his turn.
		if (playerID != api.getCurrPlayerID()) {
			PlayerTurn turn = api.getPlayerTurn(playerID);
			turn.addError("It is not your turn!");
			turn.setError();
			sendToPlayer(turn.toJSON(), token);
			turn.resetTurn();
			return;
		}

		interruptTimer.cancel();
		interruptTimer.purge();

		int opponentID = api.getCurrOpponentID();

		// Once you call sendToAPI, "playerID" and "opponentID" are switched,
		// since the turn switches, which is why playerID and opponentID are
		// preserved beforehand.
		sendToAPI(obj, playerID);

		PlayerTurn playerTurn = api.getPlayerTurn(playerID);
		PlayerTurn opponentTurn = api.getPlayerTurn(opponentID);

		sendToPlayer(playerTurn.toJSON(), playerToken[playerID]);
		sendToPlayer(opponentTurn.toJSON(), playerToken[opponentID]);

		playerTurn.resetTurn();
		opponentTurn.resetTurn();

		// Set the new player to interrupt.
		ServerInterruptTask.PLAYER_TO_INTERRUPT = api.getCurrPlayerID();
		interruptTimer.schedule(new ServerInterruptTask(), TURN_TIME_LIMIT);
	}

	private static void sendToAPI(JSONObject obj, int playerID) {
		try {
			api.processTurn(obj, playerID);
		} catch (APIException e) {
			api.getPlayerTurn(playerID).addError(e.getMessage());
		}
	}

	public static synchronized void interruptPlayer(int playerID) {
		int opponentID = api.getCurrOpponentID();

		// This will switch the player/opponent IDs
		api.notifyInterrupt();

		PlayerTurn turn = api.getPlayerTurn(playerID);
		turn.setInterrupt();
		sendToPlayer(turn.toJSON(), playerToken[playerID]);
		turn.resetTurn();

		turn = api.getPlayerTurn(opponentID);
		turn.setNotify();
		sendToPlayer(turn.toJSON(), playerToken[opponentID]);
		turn.resetTurn();

		ServerInterruptTask.PLAYER_TO_INTERRUPT = api.getCurrPlayerID();
		interruptTimer.schedule(new ServerInterruptTask(), TURN_TIME_LIMIT);
	}

	public static void printToVisualizerLog(String string) {
		visualizerLog.log(string);
	}
}
