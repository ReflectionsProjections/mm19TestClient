package mm19.server;

import java.util.ArrayList;
import java.util.Timer;

import mm19.game.*;
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

    //TODO: Determine better name for method -Eric
	public static boolean newData(JSONObject obj, String playerToken) {
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
                mainShipJSON.put("type", "M");
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
                t.schedule(new ServerTimerTask(), 50);
            }
            return true;

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static ShipData initShip(JSONObject obj) {
		int health;
		String type;
		int xCoord;
		int yCoord;
		String orientation;

		try {
            if(obj.has("type") && obj.has("xCoord") && obj.has("yCoord") && obj.has("orientation")) {
                type = obj.getString("type");
                xCoord = obj.getInt("xCoord");
                yCoord = obj.getInt("yCoord");
                orientation = obj.getString("orientation");

                health = -1;
                if(type.equals("P")) {
                    health = PilotShip.HEALTH;
                } else if(type.equals("D")) {
                    health = DestroyerShip.HEALTH;
                } else if(type.equals("M")) {
                    health = MainShip.HEALTH;
                }
            } else {
                return null;
            }

			if (!type.equals("") && xCoord > -1 && xCoord < Constants.BOARD_SIZE && yCoord > -1
                    && yCoord < Constants.BOARD_SIZE && !orientation.equals("") && health != -1) {

                // Success
                return new ShipData(health, ID++, type, xCoord, yCoord, orientation);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Failure
		return null;

	}
	
	/**
	 * Handle setting up the next turn once this player's turn has ended.
	 * @param currPlayerID
	 */
	public static void sendTurn(int currPlayerID){
		writePlayer(currPlayerID, "playerToken", playerTokens[currPlayerID]);
		writePlayer(currPlayerID, "playerName", playerNames[currPlayerID]);
		printTurnToLog(currPlayerID);
		send(currPlayerID);
		
		int opponentID = 1;
		if (currPlayerID == 0) opponentID = 1;
		if (currPlayerID == 1) opponentID = 0;
		
		Timer t = new Timer();
		ServerTimerTask.PLAYER_TO_NOTIFY = opponentID;
		t.schedule(new ServerTimerTask(), 50);
	}

	/**
	 * @param obj
	 * @return returns a valid ship if the given JSONObject contains such, null
	 *         otherwise
	 */
	private static ShipData getShip(JSONObject obj) {
		int health;
		int ID;
		String type;
		int xCoord;
		int yCoord;
		String orientation;

		try {
            if(obj.has("health") && obj.has("ID") && obj.has("type") && obj.has("xCoord")
                    && obj.has("yCoord") && obj.has("orientation")) {

                health = obj.getInt("health");
                ID = obj.getInt("ID");
                type = obj.getString("type");
                xCoord = obj.getInt("xCoord");
                yCoord = obj.getInt("yCoord");
                orientation = obj.getString("orientation");
            } else {
                return null;
            }

            //TODO: Determine why ID must not be 0. -Eric
			if (health != 0 && ID != 0 && type.equals("") && xCoord > -1 && xCoord < Constants.BOARD_SIZE
                    && yCoord  > -1 && yCoord < Constants.BOARD_SIZE && !orientation.equals("")) {

                // Success
                return new ShipData(health, ID, type, xCoord, yCoord, orientation);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Failure
		return null;
	}

	/**
	 * @param jsonArray
	 * @return returns the full arrayList of 20 valid ships if the given
	 *         JSONArray contains such, any error will cause this function
	 *         return null
	 */
	private static ArrayList<ShipData> getShipList(JSONArray jsonArray) {

		if (jsonArray.length() != 19)
			return null;
		int length = jsonArray.length();
		ArrayList<ShipData> list = new ArrayList<ShipData>();
		ShipData tempShip;
		JSONObject tempJson;
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
	 * @param obj
	 * @return - returns the associated Action in the given JSON object contains
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
                return new Action(shipID, actionID, actionX, actionY, actionExtra);
            }
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Failure
		return null;
	}

	/**
	 * @param jsonArray
	 * @return returns the valid List of Actions if the given JSONarray contains
	 *         such, and null otherwise
	 */
	private static ArrayList<Action> getActionList(JSONArray jsonArray) {
		int length = jsonArray.length();
		ArrayList<Action> list = new ArrayList<Action>();
		Action tempAction;
		JSONObject tempJson;
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
	 * @param status
	 *            - enum that tells us to write to player1, player2 or both
	 * @param ships
	 *            - array list of current player ships/status
	 * @return - true if successful write
	 */
	public static boolean writePlayerShips(int status, ArrayList<ShipData> ships) {
		JSONArray shipsJson = new JSONArray();
		JSONObject tempShip;
		int length = ships.size();
		while (length > 0) {
			length--;
			if ((tempShip = makeShipJSON(ships.get(length))) != null)
				shipsJson.put(tempShip);
		}
		if (writePlayer(status, "ships", (Object) shipsJson))
			return true;
		return false;
	}

	/**
	 * @param data
	 *            - the data of a given ship
	 * @return - a jsonobject containing said data
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
	
	public static boolean writePlayerError(int status, String message) {
		try {
			playerTurnObj[status].append("error", message);
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static boolean writePlayerResponseCode(int status) {
		try {
			if(playerTurnObj[status].has("error")) {
				int length = playerTurnObj[status].getJSONArray("error").length();
				if(length > 0) {
					return writePlayer(status, "responseCode", 400);
				}
				return writePlayer(status, "responseCode", 200);
			}
			else {
				playerTurnObj[status].put("error", new JSONArray());
				return writePlayer(status, "responseCode", 200);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean writePlayerResources(int status, int resources) {
		return writePlayer(status, "resources", status);
	}

	/**
	 * @param status
	 *            - enum that tells us to write to player1, player2 or both
	 * @param hits
	 *            - array list of current player hit reports
	 * @return - true if successful write
	 */
	public static boolean writePlayerHits(int status, ArrayList<HitReport> hits) {
		JSONArray hitsJson = new JSONArray();
		JSONObject tempHit;
		int length = hits.size();
		while (length > 0) {
			length--;
			if ((tempHit = makeHitJSON(hits.get(length))) != null)
				hitsJson.put(tempHit);
		}
		if (writePlayer(status, "hitReport", hitsJson))
			return true;
		return false;
	}

	/**
	 * @param status
	 *            - enum that tells us to write to player1, player2 or both
	 * @param hits
	 *            - array list of current player hit reports
	 * @return - true if sucessful write
	 */
	public static boolean writePlayerEnemyHits(int status, ArrayList<HitReport> hits) {
		JSONArray hitsJson = new JSONArray();
		JSONObject tempHit;
		int length = hits.size();
		while (length > 0) {
			length--;
			if ((tempHit = makeHitJSON(hits.get(length))) != null)
				hitsJson.put(tempHit);
		}
		if (writePlayer(status, "enemyHitReport", hitsJson))
			return true;
		return false;
	}

	/**
	 * @param report
	 *            - the data of a given hitreport
	 * @return - a jsonobject containing said data
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
	 * @param status
	 *            - enum that tells us to write to player1, player2 or both
	 * @param pings
	 *            - array list of current player pings
	 * @return - true if sucessful write
	 */
	public static boolean writePlayerPings(int status, ArrayList<SonarReport> pings) {
		JSONArray pingsJson = new JSONArray();
		JSONObject tempPing;
		int length = pings.size();
		while (length > 0) {
			length--;
			if ((tempPing = makePingJSON(pings.get(length))) != null)
				pingsJson.put(tempPing);
		}
		if (writePlayer(status, "pingReport", pingsJson))
			return true;

		return false;
	}

	/**
	 * @param ping
	 *            - the data of a given ping
	 * @return - a jsonobject containing said data
	 */
	private static JSONObject makePingJSON(SonarReport ping) {
		JSONObject tempPing = new JSONObject();

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
	 * @param status
	 *            - enum that tells us to write to player1, player2 or both
	 * @param results
	 *            - array list of current action results
	 * @return - true if sucessful write
	 */
	public static boolean writePlayerResults(int status,
			ArrayList<ShipActionResult> results) {
		JSONArray resultsJson = new JSONArray();
		JSONObject tempResult;
		int length = results.size();
		while (length > 0) {
			length--;
			if ((tempResult = makeResultJSON(results.get(length))) != null)
				resultsJson.put(tempResult);
		}
		if (writePlayer(status, "shipActionResults", (Object) resultsJson))
			return true;

		return false;
	}

	/**
	 * @param result
	 *            - the data of a given result
	 * @return - a jsonobject containing said data
	 */
	private static JSONObject makeResultJSON(ShipActionResult result) {
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
	 * @param status
	 */
	public static void notifyTurn(int status) {
		writePlayer(status, "error", new JSONArray());
		writePlayer(status, "responseCode", 100);
		writePlayer(status, "playerName", playerNames[status]);
		writePlayer(status, "playerToken", playerTokens[status]);
		writePlayer(status, "shipActionResults", new JSONArray());
		writePlayer(status, "hitReport", new JSONArray());
		
		if(!playerTurnObj[status].has("ships")) {
			writePlayer(status, "ships", new JSONArray());
		}
		if(!playerTurnObj[status].has("pingReport")) {
			writePlayer(status, "pingReport", new JSONArray());
		}
		send(status);
	}

	// TODO turn interuppts
	// TODO status enum creation
	/**
	 * @param status
	 *            - enum that tells us to write to player1, player2 or both
	 * @param string
	 *            - key to what we're writing
	 * @param obj
	 *            - object that we're writing
	 */
	private static boolean writePlayer(int status, String string, Object obj) {
        //TODO: Generalize to remove code duplication
		try {
			switch (status) {
			case 0: // append to player 1
				playerTurnObj[status].put(string, obj);
				break;
			case 1: // append to player 2
				playerTurnObj[status].put(string, obj);
				break;
			case 2: // append to both
				playerTurnObj[0].put(string, obj);
				playerTurnObj[1].put(string, obj);
				break;
			default:
				return false;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @param status
	 *            - enum that tells us to write to player1, player2 or both
	 * @return - true if successful send
	 */
	
	public static boolean send(int status) {
		//TODO: Generalize to remove code duplication
		switch (status) {
            // send to player 1
            case 0:
                Server.sendPlayer(playerTurnObj[status], playerTokens[status]);
                playerTurnObj[status] = new JSONObject();
                break;

            // send to player 2
            case 1:
                Server.sendPlayer(playerTurnObj[status], playerTokens[status]);
                playerTurnObj[status] = new JSONObject();
                break;

            // append to both
            case 2:
                Server.sendPlayer(playerTurnObj[0], playerTokens[0]);
                playerTurnObj[0] = new JSONObject();
                Server.sendPlayer(playerTurnObj[1], playerTokens[1]);
                playerTurnObj[1] = new JSONObject();
                break;

            default:
                return false;
		}
		
		return true;
	}

	public static boolean hasWon(int playerID) {
        int opponentID = Engine.getOpponentID(playerID);

        writePlayer(playerID, "responseCode", 9001);
        writePlayer(opponentID, "responseCode", -1);
        send(playerID);
        send(opponentID);

        Server.winCondition(playerID);
        return true;
	}

	public static void printTurnToLog(int playerID) {
		Server.printToVisualizerLog(playerTurnObj[playerID].toString());
	}
}
