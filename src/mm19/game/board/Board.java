package mm19.game.board;

import mm19.game.ships.Ship;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author mm19
 *
 * The entity which keeps track of positions of ships.
 */
public class Board {
    final public static int DEFAULT_WIDTH = 100;
    final public static int DEFAULT_HEIGHT = 100;

    private Tile[][] tiles;
    private int width;
    private int height;

    private HashMap<Integer, ShipData> ships = new HashMap<Integer, ShipData>();

    /**
     * Constructor
     * Initializes board with default width and height.
     */
    public Board() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Constructor.  Takes a width and a height and initializes the double Tile array.
     *
     * @param boardWidth  Width of the Board to create
     * @param boardHeight Height of the Board to create
     */
    public Board(int boardWidth, int boardHeight) {
        width = boardWidth;
        height = boardHeight;
        tiles = new Tile[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = new Tile();
            }
        }
    }

    /**
     * Returns the width of the Board
     *
     * @return Width of the Board
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the Board
     *
     * @return Height of the Board
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns a reference to a Ship if it occupies coordinate (x,y) on the board.
     *
     * @param x X Coordinate
     * @param y Y Coordinate
     * @return A Ship if one exists at the position, null otherwise
     */
    public Ship getShip(int x, int y) {
        if (inBounds(x, y) && isOccupied(x, y)) {
            return tiles[x][y].getShip();
        } else {
            return null;
        }
    }

    /**
     * Returns references to all ships on the board within an ArrayList.
     *
     * @return An ArrayList containing all ships.
     */
    public ArrayList<Ship> getShips() {
        ShipData[] shipData = ships.values().toArray(new ShipData[0]);
        ArrayList<Ship> shipArray = new ArrayList<Ship>();
        for (int i = 0; i < shipData.length; i++) {
            shipArray.add(shipData[i].getShip());
        }
        return shipArray;
    }

    /**
     * Searches the Ship storage for the given shipID
     *
     * @param shipID ID of the Ship to search for
     * @return The Ship if it is on the board, null otherwise
     */
    public Ship getShip(int shipID) {
        ShipData shipData = ships.get(shipID);
        return shipData.getShip();
    }

    /**
     * Searches the Ship storage for the given shipID and returns the
     * corresponding Ship's position
     *
     * @param shipID ID of the Ship to search for.
     * @return A Ship's position if it is on the board, null otherwise
     */
    public Position getShipPosition(int shipID) {
        ShipData shipData = ships.get(shipID);
        return shipData.getPosition();
    }

    /**
     * Searches the Ship storage for the provided Ship and returns the
     * corresponding Ship's position
     *
     * @param ship A reference to the Ship to lookup the position of.
     * @return A Ship's position if it is on the board, null otherwise
     */
    public Position getShipPosition(Ship ship) {
        return getShipPosition(ship.getID());
    }

    /**
     * Attempts to place a Ship on the board.
     *
     * @param ship        The Ship to place
     * @param shipX       X coordinate to place Ship at
     * @param shipY       Y coordinate to place Ship at
     * @param orientation Orientation of the Ship
     * @return True if the ship could be placed, false otherwise.
     */
    public boolean placeShip(Ship ship, int shipX, int shipY, Position.Orientation orientation) {
        if (!canPlaceShip(ship, shipX, shipY, orientation))
            return false;

        //Store Ship's data in hash table
        ShipData shipData = new ShipData();
        shipData.setShip(ship);
        shipData.setPosition(new Position(shipX, shipY, orientation));
        ships.put(ship.getID(), shipData);

        int x = shipX;
        int y = shipY;
        for (int i = 0; i < ship.getLength(); i++) {
            if (orientation == Position.Orientation.VERTICAL) {
                y = shipY - i;
            } else {
                x = shipX - i;
            }
            tiles[x][y].setShip(ship);
        }
        return true;
    }

    /**
     * Attempts to place a Ship on the board.
     *
     * @param ship     The Ship to place
     * @param position A Position object indicating how the ship should be placed.
     * @return True if the ship could be placed, false otherwise.
     */
    public boolean placeShip(Ship ship, Position position) {
        return placeShip(ship, position.x, position.y, position.orientation);
    }

    /**
     * Removes the ship occupying the given x,y coordinate and returns a reference to the ship.
     *
     * @param x X Coordinate
     * @param y Y Coordinate
     * @return A reference to the Ship removed, null if no Ship could be removed.
     */
    public Ship removeShip(int x, int y) {
        if (!inBounds(x, y) || !isOccupied(x, y))
            return null;

        Ship ship = tiles[x][y].getShip();
        Position shipPosition = getShipPosition(ship);
        int shipX = shipPosition.x;
        int shipY = shipPosition.y;
        Position.Orientation orientation = shipPosition.orientation;

        for (int i = 0; i < ship.getLength(); i++) {
            if (orientation == Position.Orientation.VERTICAL) {
                y = shipY - i;
            } else {
                x = shipX - i;
            }
            tiles[x][y].removeShip();
        }

        //Remove from hash table
        ships.remove(ship.getID());

        return ship;
    }

    /**
     * Attempts to move a ship from one position to another by first removing the ship
     * from the board, checking if the requested position is available and then placing
     * the ship.  If the position was not available, it is placed back in its original
     * position.
     *
     * @param ship        The Ship to move
     * @param x           X coordinate to move Ship to
     * @param y           Y coordinate to move Ship to
     * @param orientation New orientation for Ship
     * @return True if Ship could be moved, false otherwise
     */
    public boolean moveShip(Ship ship, int x, int y, Position.Orientation orientation) {
        Position currentPosition = getShipPosition(ship);
        removeShip(currentPosition.x, currentPosition.y);

        if (canPlaceShip(ship, x, y, orientation)) {
            placeShip(ship, x, y, orientation);
            return true;
        } else {
            placeShip(ship, currentPosition.x, currentPosition.y, currentPosition.orientation);
            return false;
        }
    }

    /**
     * Resets the board to it's post-constructor state by removing all ships.
     */
    public void reset() {
        ShipData[] shipData = ships.values().toArray(new ShipData[0]);
        for (int i = 0; i < shipData.length; i++) {
            Position shipPosition = shipData[i].getPosition();
            removeShip(shipPosition.x, shipPosition.y);
        }
    }

    /**
     * Reports the number of ships that exist on the board.
     *
     * @return Number of Ships on the board.
     */
    public int shipCount() {
        return ships.size();
    }

    /**
     * Reports if a Ship can be placed on the Board
     *
     * @param ship        The Ship to place
     * @param shipX       X coordinate of Ship
     * @param shipY       Y coordinate of Ship
     * @param orientation Orientation of the ship
     * @return Returns true if the ship can be placed, false otherwise
     */
    private boolean canPlaceShip(Ship ship, int shipX, int shipY, Position.Orientation orientation) {
        int x = shipX;
        int y = shipY;
        for (int i = 0; i < ship.getLength(); i++) {
            if (orientation == Position.Orientation.VERTICAL) {
                y = shipY - i;
            } else {
                x = shipX - i;
            }
            if (!inBounds(x, y) || isOccupied(x, y)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Given an (x,y) coordinate, determines if there is a corresponding board location.
     *
     * @param x X Coordinate
     * @param y Y Coordinate
     * @return True if cell is within bounds of double Tile array, false if not.
     */
    private boolean inBounds(int x, int y) {
        return (x >= 0) && (x < width) && (y >= 0) && (y < height);
    }

    /**
     * Given an (x,y) coordinate, reports if the board location is occupied
     *
     * @param x X Coordinate
     * @param y Y Coordinate
     * @return True is an object occupies Tile, false otherwise
     */
    private boolean isOccupied(int x, int y) {
        return tiles[x][y].isOccupied();
    }

    /**
     * @author mm19
     *         <p/>
     *         The entities which make up the board
     */
    private class Tile {

        private boolean occupied = false;

        /**
         * Reference to the ship occupying this Tile.
         */
        private Ship currentShip = null;


        /**
         * Default Tile constructor
         */
        public Tile() {

        }

        /**
         * @return true if Tile is occupied, false if not.
         */
        public boolean isOccupied() {
            return occupied;
        }

        /**
         * Sets a reference to the ship currently occupying this Tile
         *
         * @param ship
         */
        public void setShip(Ship ship) {
            currentShip = ship;
            occupied = true;
        }

        /**
         * @return Returns a reference to the current ship.
         */
        public Ship getShip() {
            return currentShip;
        }

        /**
         * Removes a reference to the ship currently occupying this Tile
         */
        public void removeShip() {
            currentShip = null;
            occupied = false;
        }
    }

    /**
     * @author mm19
     *         <p/>
     *         Private class for storage of data relevent to a ship on a board
     */
    private class ShipData {
        private Ship ship;
        private Position position;

        public Ship getShip() {
            return ship;
        }

        public void setShip(Ship ship) {
            this.ship = ship;
        }

        public Position getPosition() {
            return position;
        }

        public void setPosition(Position position) {
            this.position = position;
        }
    }
}





