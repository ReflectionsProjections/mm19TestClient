package mm19.server;

import java.util.ArrayList;
import java.util.Timer;

import mm19.game.*;
import mm19.game.board.Position;
import mm19.game.ships.DestroyerShip;
import mm19.game.ships.MainShip;
import mm19.game.ships.PilotShip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author mm19
 * 
 *         This will tie together the server and game logic. Functions defined
 *         here are called by the server to interface properly with the game.
 * 
 */
public class API {
    //TODO: This file needs more javadoc

	private static JSONObject[] playerTurnObj;
	private static String[] playerTokens;
	private static String[] playerNames;
	private static Engine game;
	private static int ID = 0;

    /**
     * TODO Description goes here
     *
     * @return Boolean that always returns true for some reason TODO See if this can be changed to a void function
     */
    public static boolean initAPI() {
		playerTurnObj = new JSONObject[Constants.PLAYER_COUNT];
        playerTokens = new String[Constants.PLAYER_COUNT];
        playerNames = new String[Constants.PLAYER_COUNT];

        for(int i = 0; i < Constants.PLAYER_COUNT; i++) {
            playerTurnObj[i] = new JSONObject();
            playerTokens[i] = "";
            playerNames[i] = "";
        }
		game = new Engine();
		return true;
	}



    /**
     * TODO Description goes here
     *
     * @param obj TODO: This file needs more javadoc
     * @param playerToken TODO: This file needs more javadoc
     * @return TODO: This file needs more javadoc
     */
	public static boolean newData(JSONObject obj, String playerToken) {
        //TODO: Determine better name for method -Eric
		int playerID;
		String playerName;
		JSONObject mainShipJSON;
		ShipData mainShip;
		JSONArray shipsJSONArray;
		ArrayList<ShipData> ships;

		try {
            if(obj.has("playerName") && obj.has("mainShip") && obj.has("ships")) {
                playerName = obj.getString("playerName");
                mainShipJSON = (JSONObject) obj.get("mainShip");
                shipsJSONArray = (JSONArray) obj.get("ships");
            } else {
                return false;
            }

			if ( playerName != null && mainShipJSON != null && shipsJSONArray != null) {
                mainShipJSON.put("type", MainShip.IDENTIFIER);
                mainShip = initShip(mainShipJSON);

                ships = new ArrayList<ShipData>();

                //Subtract 1 since main ship initialized earlier.
                for(int i = 0; i < Constants.MAX_SHIPS-1; i++) {
                    ShipData tempData = initShip(shipsJSONArray.getJSONObject(i));

                    //TODO: Determine if tempData == null is worth not ignoring like it is now. -Eric
                    if(tempData != null) {
                        ships.add(tempData);
                    }
                }

                ships.add(mainShip);
                //TODO: Determine what playerSet does and rename method -Eric
                playerID = game.playerSet(ships, playerToken);

                if(playerID < 0 || playerID >= Constants.PLAYER_COUNT) {
                    return false;
                }

                playerNames[playerID] = playerName;
                playerTokens[playerID] = playerToken;

                writePlayer(playerID, "playerToken", playerToken);
                writePlayer(playerID, "playerName", playerName);
                writePlayer(playerID, "shipActionResults", new JSONArray());
                writePlayer(playerID, "hitReport", new JSONArray());
                writePlayer(playerID, "pingReport", new JSONArray());
                send(playerID);

                return true;
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return false;
	}

    /**
     * TODO Description goes here
     *
     * @param obj TODO: This file needs more javadoc
     * @return TODO: This file needs more javadoc
     */
	public static boolean decodeTurn(JSONObject obj) {
		// sanity check
        for (String token : playerTokens) {
            if (token.equals("")) {
                return false;
            }
        }

		ArrayList<Action> actionList;
		int currPlayerID = -1;
		int opponentID;
        String playerToken;
		try {
            if(obj.has("playerToken") && obj.has("shipActions")) {
                //TODO Create method getPlayerByToken
                playerToken = obj.getString("playerToken");
                for(int i = 0; i < Constants.PLAYER_COUNT; i++) {
                    if(playerToken.equals(playerTokens[i])) {
                        currPlayerID = i;
                        break;
                    }
                }

                if(currPlayerID == -1){
                    return false;
                }

                opponentID = Engine.getOpponentID(currPlayerID);

            } else {
                return false;
            }


            JSONArray actionListObj = obj.getJSONArray("shipActions");
            actionList = getActionList(actionListObj);

            boolean success = game.playerTurn(playerToken, actionList);

            writePlayer(currPlayerID, "playerToken", playerTokens[currPlayerID]);
            writePlayer(currPlayerID, "playerName", playerNames[currPlayerID]);
            send(currPlayerID);

            if(success){
                printTurnToLog(currPlayerID);

                Timer t = new Timer();
                ServerTimerTask.PLAYER_TO_NOTIFY = opponentID;
                //TODO replace number with constant
                t.schedule(new ServerTimerTask(), 50);
            }
            return true;

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

    /**
     * TODO Description goes here
     *
     * @param obj TODO: This file needs more javadoc
     * @return TODO: This file needs more javadoc
     */
	private static ShipData initShip(JSONObject obj) {
		int health;
		String type;
		int xCoord;
		int yCoord;
		String orientationIdentifier;

		try {
            if(obj.has("type") && obj.has("xCoord") && obj.has("yCoord") && obj.has("orientation")) {
                type = obj.getString("type");
                xCoord = obj.getInt("xCoord");
                yCoord = obj.getInt("yCoord");
                orientationIdentifier = obj.getString("orientation");

                health = -1;
                //TODO Try to find a better way to handle this
                if(type.equals(PilotShip.IDENTIFIER)) {
                    health = PilotShip.HEALTH;
                } else if(type.equals(DestroyerShip.IDENTIFIER)) {
                    health = DestroyerShip.HEALTH;
                } else if(type.equals(MainShip.IDENTIFIER)) {
                    health = MainShip.HEALTH;
                }
            } else {
                return null;
            }

			if (!type.equals("") && xCoord > -1 && xCoord < Constants.BOARD_SIZE && yCoord > -1
                    && yCoord < Constants.BOARD_SIZE && !orientationIdentifier.equals("") && health != -1) {
                Position.Orientation orientation = Position.getOrientationByIdentifier(orientationIdentifier);
                return new ShipData(health, ID++, type, xCoord, yCoord, orientation);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;

	}
	
	/**
	 * Handle setting up the next turn once this player's turn has ended.
     *
	 * @param currPlayerID TODO: This file needs more javadoc
	 */
	public static void sendTurn(int currPlayerID){
		writePlayer(currPlayerID, "playerToken", playerTokens[currPlayerID]);
		writePlayer(currPlayerID, "playerName", playerNames[currPlayerID]);
		printTurnToLog(currPlayerID);
		send(currPlayerID);

		int opponentID = Engine.getOpponentID(currPlayerID);

		Timer t = new Timer();
		ServerTimerTask.PLAYER_TO_NOTIFY = opponentID;
        //TODO Make constant for number
		t.schedule(new ServerTimerTask(), 50);
	}

	/**
     * TODO Description goes here
     *
	 * @param obj TODO: This file needs more javadoc
	 * @return A valid ShipData if the given JSONObject contains such, null otherwise
	 */
	private static ShipData getShip(JSONObject obj) {
		int health;
		int ID;
		String type;
		int xCoord;
		int yCoord;
		String orientationIdentifier;

		try {
            if(obj.has("health") && obj.has("ID") && obj.has("type") && obj.has("xCoord")
                    && obj.has("yCoord") && obj.has("orientation")) {

                health = obj.getInt("health");
                ID = obj.getInt("ID");
                type = obj.getString("type");
                xCoord = obj.getInt("xCoord");
                yCoord = obj.getInt("yCoord");
                orientationIdentifier = obj.getString("orientation");
            } else {
                return null;
            }

            //TODO: Determine why ID must not be 0.  0 seems like a reasonable value. -Eric
			if (health != 0 && ID != 0 && type.equals("") && xCoord > -1 && xCoord < Constants.BOARD_SIZE
                    && yCoord  > -1 && yCoord < Constants.BOARD_SIZE && !orientationIdentifier.equals("")) {

                Position.Orientation orientation = Position.getOrientationByIdentifier(orientationIdentifier);
                return new ShipData(health, ID, type, xCoord, yCoord, orientation);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Failure
		return null;
	}

	/**
     * TODO Description goes here
     *
	 * @param jsonArray TODO: This file needs more javadoc
	 * @return ArrayList of 20 valid ships if the given JSONArray contains
     *          such, any error will cause this function return null
	 */
	private static ArrayList<ShipData> getShipList(JSONArray jsonArray) {
        //TODO What is the significance of 20?  There are only 10 ships in the game.  Also, magic number.
		if (jsonArray.length() != 19)
			return null;
		int length = jsonArray.length();
		ArrayList<ShipData> list = new ArrayList<ShipData>();
		ShipData tempShip;
		JSONObject tempJson;
        //TODO Change to for loop
		while (length > 0) {
			length--;
			try {
				tempJson = jsonArray.getJSONObject(length);
				if ((tempShip = getShip(tempJson)) != null)
					list.add(tempShip);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return list;

	}

	/**
     * TODO Description goes here
     *
	 * @param obj TODO: This file needs more javadoc
	 * @return - returns the associated Action if the given JSON object contains
	 *         a valid Action, null otherwise
	 */
	private static Action getAction(JSONObject obj) {
		String actionID;
		int shipID;
		int actionX;
		int actionY;
		int actionExtra;

		try {
			if (obj.has("actionID") && obj.has("ID") && obj.has("actionX") && obj.has("actionY") && obj.has("actionExtra")){
				actionID = obj.getString("actionID");
                shipID = obj.getInt("ID");
                actionX = obj.getInt("actionX");
                actionY = obj.getInt("actionY");
                actionExtra = obj.getInt("actionExtra");

                Action.Type actionType = Action.getActionTypeByIdentifier(actionID);



                return new Action(shipID, actionType, actionX, actionY, actionExtra);
            }
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Failure
		return null;
	}

	/**
     * TODO Description goes here
     *
	 * @param jsonArray TODO: This file needs more javadoc
	 * @return An ArrayList of Actions if the given JSONArray contains such, null otherwise
	 */
	private static ArrayList<Action> getActionList(JSONArray jsonArray) {
		int length = jsonArray.length();
		ArrayList<Action> list = new ArrayList<Action>();
		Action tempAction;
		JSONObject tempJson;
        //TODO Change to for loop
		while (length > 0) {
			length--;
			try {
				tempJson = jsonArray.getJSONObject(length);
				if ((tempAction = getAction(tempJson)) != null)
					list.add(tempAction);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	/**
     * TODO Description goes here
     *
	 * @param status enum that tells us to write to player1, player2 or both
	 * @param ships ArrayList of current player's shipData
	 * @return - boolean indicating if write was successful
	 */
	public static boolean writePlayerShips(int status, ArrayList<ShipData> ships) {
		JSONArray shipsJson = new JSONArray();
		JSONObject tempShip;
		int length = ships.size();
        //TODO Change to for loop
		while (length > 0) {
			length--;
            //TODO Please don't assign in the if conditional...
			if ((tempShip = makeShipJSON(ships.get(length))) != null)
				shipsJson.put(tempShip);
		}

		boolean writeSuccessful = writePlayer(status, "ships", (Object) shipsJson);
		return writeSuccessful;
	}

	/**
     * TODO Description goes here
     *
	 * @param data the data of a given ship
	 * @return JSONObject containing said data
	 */
	private static JSONObject makeShipJSON(ShipData data) {
		JSONObject tempShip = new JSONObject();

		try {
			tempShip.put("health", data.health);
			tempShip.put("ID", data.ID);
			tempShip.put("type", data.type);
			tempShip.put("xCoord", data.xCoord);
			tempShip.put("yCoord", data.yCoord);
			tempShip.put("orientation", data.orientation);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return tempShip;
	}

    /**
     * TODO Description goes here
     *
     * @param status TODO: This file needs more javadoc
     * @param message TODO: This file needs more javadoc
     * @return TODO: This file needs more javadoc
     */
	public static boolean writePlayerError(int status, String message) {
		try {
			playerTurnObj[status].append("error", message);
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

    /**
     * TODO Description goes here
     *
     * @param playerID TODO: This file needs more javadoc
     * @return TODO: This file needs more javadoc
     */
	public static boolean writePlayerResponseCode(int playerID) {
		try {

            //TODO Rearrange code so there is only a since writePlayer call.
            //TODO Store result of writePlayer in well named boolean variable and return that
			if(playerTurnObj[playerID].has("error")) {
				int length = playerTurnObj[playerID].getJSONArray("error").length();
				if(length > 0) {
					return writePlayer(playerID, "responseCode", 400);
				}
				return writePlayer(playerID, "responseCode", 200);
			} else {
				playerTurnObj[playerID].put("error", new JSONArray());
				return writePlayer(playerID, "responseCode", 200);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

    /**
     * TODO Description goes here
     *
     * @param playerID TODO: This file needs more javadoc
     * @param resources TODO: This file needs more javadoc
     * @return TODO: This file needs more javadoc
     */
	public static boolean writePlayerResources(int playerID, int resources) {
		boolean writeSuccessful = writePlayer(playerID, "resources", resources);
        return writeSuccessful;
	}

	/**
     * TODO Description goes here
     *
	 * @param playerID Player to write to
	 * @param hits array list of current player hit reports
	 * @return Boolean indicating if write was successful
	 */
	public static boolean writePlayerHits(int playerID, ArrayList<HitReport> hits) {
		JSONArray hitsJson = new JSONArray();
		JSONObject tempHit;
		int length = hits.size();
        //TODO Change to for loop
		while (length > 0) {
			length--;
            //TODO Please don't assign in the if conditional...
			if ((tempHit = makeHitJSON(hits.get(length))) != null)
				hitsJson.put(tempHit);
		}

		boolean writeSuccessful = writePlayer(playerID, "hitReport", hitsJson);
		return writeSuccessful;
	}

	/**
     * TODO Description goes here
     *
	 * @param playerID Player to write to
	 * @param hits array list of current player hit reports
	 * @return Boolean that indicates if write was successful
	 */
	public static boolean writePlayerEnemyHits(int playerID, ArrayList<HitReport> hits) {
		JSONArray hitsJson = new JSONArray();
		JSONObject tempHit;
		int length = hits.size();
        //TODO Change to for loop
		while (length > 0) {
			length--;
            //TODO Please don't assign in the if conditional...
			if ((tempHit = makeHitJSON(hits.get(length))) != null)
				hitsJson.put(tempHit);
		}

		boolean writeSuccessful = writePlayer(playerID, "enemyHitReport", hitsJson);
		return writeSuccessful;
	}

	/**
     * TODO Description goes here
     *
	 * @param report the data of a given HitReport
	 * @return JSONObject containing the HitReport's data
	 */
	private static JSONObject makeHitJSON(HitReport report) {
		JSONObject tempHit = new JSONObject();
		try {
			tempHit.put("xCoord", report.x);
			tempHit.put("yCoord", report.y);
			tempHit.put("hit", report.shotSuccessful);

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return tempHit;
	}

	/**
     * TODO Description goes here
     *
	 * @param playerID Player to write to
	 * @param pings array list of current player pings
	 * @return Boolean indicating if write was successful
	 */
	public static boolean writePlayerPings(int playerID, ArrayList<SonarReport> pings) {
		JSONArray pingsJson = new JSONArray();

        //TODO Naming: tempPing...
		JSONObject tempPing;
		int length = pings.size();
        //TODO Change to for loop
		while (length > 0) {
			length--;
            //TODO Please don't assign in the if conditional...
			if ((tempPing = makePingJSON(pings.get(length))) != null)
				pingsJson.put(tempPing);
		}

        boolean writeSuccessful = writePlayer(playerID, "pingReport", pingsJson);
        return writeSuccessful;
	}

	/**
     * TODO Description goes here
     *
	 * @param ping A SonarReport
	 * @return JSONObject containing the SonarReport's data
	 */
	private static JSONObject makePingJSON(SonarReport ping) {
		JSONObject tempPing = new JSONObject();

        //TODO Naming: ping and tempPing...

		try {
			tempPing.put("distance", ping.dist);
			tempPing.put("shipID", ping.ship.getID());
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return tempPing;
	}

	/**
     * TODO Description goes here
     *
	 * @param playerID player to write to
	 * @param results array list of current action results
	 * @return Boolean indicating if write was successful
	 */
	public static boolean writePlayerResults(int playerID, ArrayList<ShipActionResult> results) {
		JSONArray resultsJson = new JSONArray();
        //TODO Naming: results and tempResult...
		JSONObject tempResult;
		int length = results.size();
        //TODO change to for loop
		while (length > 0) {
			length--;
            //TODO Please don't assign in the if conditional...
			if ((tempResult = makeResultJSON(results.get(length))) != null)
				resultsJson.put(tempResult);
		}

        boolean writeSuccessful = writePlayer(playerID, "shipActionResults", resultsJson);
	    return writeSuccessful;
	}

	/**
     * TODO Description goes here
     *
	 * @param result A ShipActionResult
	 * @return JSONObject containing the ShipActionResult's data
	 */
	private static JSONObject makeResultJSON(ShipActionResult result) {
        //TODO Rename function
        //TODO Naming: result and tempResult...
        JSONObject tempResult = new JSONObject();

		try {
			tempResult.put("ID", result.shipID);
			tempResult.put("result", result.result);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return tempResult;
	}
	
	/**
	 * Notifies the player that their turn is about to begin.
     *
	 * @param playerID Player to notify
	 */
	public static void notifyTurn(int playerID) {
        //TODO Create constants for strings and codes
		writePlayer(playerID, "error", new JSONArray());
		writePlayer(playerID, "responseCode", 100);
		writePlayer(playerID, "playerName", playerNames[playerID]);
		writePlayer(playerID, "playerToken", playerTokens[playerID]);
		writePlayer(playerID, "shipActionResults", new JSONArray());
		writePlayer(playerID, "hitReport", new JSONArray());
		
		if(!playerTurnObj[playerID].has("ships")) {
			writePlayer(playerID, "ships", new JSONArray());
		}
		if(!playerTurnObj[playerID].has("pingReport")) {
			writePlayer(playerID, "pingReport", new JSONArray());
		}
		send(playerID);
	}

	// TODO turn interuppts

	/**
     * TODO Description goes here
     *
	 * @param playerID player to write to
	 * @param string key to what we're writing
	 * @param obj object that we're writing
     * @return Boolean that indicates if write was successful
	 */
	private static boolean writePlayer(int playerID, String string, Object obj) {
        //TODO Naming: string and obj...
		try {
			playerTurnObj[playerID].put(string, obj);
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
     * TODO Description goes here
     *
	 * @param playerID Player to send stored data to
	 */
	public static void send(int playerID) {
        Server.sendPlayer(playerTurnObj[playerID], playerTokens[playerID]);
        playerTurnObj[playerID] = new JSONObject();
	}

    /**
     * TODO Description goes here
     *
     * @param playerID Player that won
     * @return Boolean that always returns true for some reason TODO See if this can be changed to a void function
     */
	public static boolean hasWon(int playerID) {
        int opponentID = Engine.getOpponentID(playerID);

        //TODO Create Constants for codes and strings
        writePlayer(playerID, "responseCode", 9001);
        writePlayer(opponentID, "responseCode", -1);
        send(playerID);
        send(opponentID);

        Server.winCondition(playerID);
        return true;
	}

    /**
     * TODO Description goes here
     *
     * @param playerID Player whose turn is sent to visualizer
     */
	public static void printTurnToLog(int playerID) {
		Server.printToVisualizerLog(playerTurnObj[playerID].toString());
	}
}
