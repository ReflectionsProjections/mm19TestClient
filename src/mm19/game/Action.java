package mm19.game;

public class Action {

	// Class variables
	final public Type actionID;
	final public int shipID;
	final public int actionXVar;
	final public int actionYVar;
	final public int actionExtraVar;

    public static final String SHOOT_IDENTIFIER = "F";
    public static final String BURST_SHOT_IDENTIFIER = "BS";
    public static final String SONAR_IDENTIFIER = "S";
    public static final String MOVE_HORIZONTAL_IDENTIFIER = "MH";
    public static final String MOVE_VERTICAL_IDENTIFIER = "MV";

    public enum Type {BURST_SHOT, MOVE_HORIZONTAL, MOVE_VERTICAL, SHOOT, SONAR, INVALID}

	// Variable-initializing constructor
	public Action(int shipID, Type actionID, int x, int y, int extra)
	{
		// Initialize variables
		this.shipID = shipID;
		this.actionID = actionID;
		this.actionXVar = x;
		this.actionYVar = y;
		this.actionExtraVar = extra;
	}

    public static Type getActionTypeByIdentifier(String identifier) {
        if (identifier.equals(BURST_SHOT_IDENTIFIER)) {
            return Type.BURST_SHOT;
        } else if (identifier.equals(MOVE_HORIZONTAL_IDENTIFIER)) {
            return Type.MOVE_HORIZONTAL;
        } else if (identifier.equals(MOVE_VERTICAL_IDENTIFIER)) {
            return Type.MOVE_VERTICAL;
        } else if (identifier.equals(SHOOT_IDENTIFIER)) {
            return Type.SHOOT;
        } else if (identifier.equals(SONAR_IDENTIFIER)){
            return Type.SONAR;
        } else {
            return Type.INVALID;
        }
    }

    public static String getActionIdentifierByType(Type actionType) {
        if (actionType == Type.BURST_SHOT) {
            return BURST_SHOT_IDENTIFIER;
        } else if (actionType == Type.MOVE_HORIZONTAL) {
            return MOVE_HORIZONTAL_IDENTIFIER;
        } else if (actionType == Type.MOVE_VERTICAL) {
            return MOVE_VERTICAL_IDENTIFIER;
        } else if (actionType == Type.SHOOT) {
            return SHOOT_IDENTIFIER;
        } else if (actionType == Type.SONAR){
            return SONAR_IDENTIFIER;
        } else {
            return "";
        }
    }
}
