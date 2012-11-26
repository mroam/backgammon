

/**
 * For keeping track of which blots were hit during the course of a move.
 * Somebody (Board probably) will keep a collection of these.
 *
 * @author M,J,J
 * @version 2012 May 29
 */
public class HitBlot {
    // instance variables - replace the example below with your own
    private int wasOnPoint = Board.ILLEGAL_MOVE;
    Player whichPlayer = null;

    /**
     * Constructor for objects of class HitBlots
     */
    public HitBlot(int newWasOnPoint, Player playerColor) {
        if ( ! Board.legitStartLoc( newWasOnPoint, playerColor )) { /* checks also Player.legitColor */
            throw new IllegalArgumentException("Can't start moving from point '" + newWasOnPoint + "'");
        };
        wasOnPoint = newWasOnPoint;
        whichPlayer = playerColor;
    }

    public Player getWhichPlayer( ) {
        return whichPlayer;
    }
    
    public int getWasOnPoint( ) {
        return wasOnPoint;
    }

}
