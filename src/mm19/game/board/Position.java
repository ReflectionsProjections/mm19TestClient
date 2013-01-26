package mm19.game.board;

/**
 *
 * @author mm19
 *
 * An immutable data object for positions
 *
 */
public class Position {
    // Enum for orientation
    public static enum Orientation { HORIZONTAL, VERTICAL }

    public final int x;
    public final int y;
    public final Orientation orientation;

    /**
     * Constructor for positions.
     * @param x
     * @param y
     * @param orientation
     */
    public Position(int x, int y, Orientation orientation){
        this.x = x;
        this.y = y;
        this.orientation = orientation;
    }
}
