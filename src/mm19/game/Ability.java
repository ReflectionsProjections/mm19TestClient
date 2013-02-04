package mm19.game;

import mm19.game.board.Board;
import mm19.game.board.Position;
import mm19.game.player.Player;
import mm19.game.ships.Ship;

/**
 * @author mm19
 *         User: Eric
 *         Date: 1/29/13
 *         Time: 9:05 PM
 */
public class Ability {
    enum Type { SHOOT, BURST_SHOT, MOVE, SONAR};

    public static HitReport shoot(Player attackingPlayer, Player targetPlayer, int targetX, int targetY) {
        boolean hadResources = attackingPlayer.chargePlayer(Constants.MISSILE_COST);
        if( !hadResources ) {
            return null;
        }

        Board board = targetPlayer.getBoard();
        Ship targetShip = board.getShip(targetX, targetY);
        boolean shipHit = false;
        if( targetShip != null ) {
            shipHit = true;
            targetShip.applyDamage(Constants.MISSILE_DAMAGE);
            if( !targetShip.isAlive() ) {
                board.removeShip( targetShip.getID() );
            }
        }
        return new HitReport(targetX, targetY, shipHit, targetShip);
    }

    public static boolean move(Player player, int shipId, Position newPosition) {
        Board board = player.getBoard();
        Ship ship = board.getShip(shipId);

        boolean hadResources = player.takeResources(ship.getMoveCost());
        if ( !hadResources ) {
            return false;
        }

        boolean moveSuccessful = board.moveShip(shipId, newPosition);
        if ( !moveSuccessful ) {
            player.giveResources(ship.getMoveCost());
        }
        return moveSuccessful;

    }
}
