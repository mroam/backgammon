
/**
 * Write a description of class Race here.
 * 
 * @author M & J & J
 * @version 2012 Mar 11
 */
public class RaceStrategy extends Strategy {

    Scorer myScorer = new RaceScorer( );

    /**
     * Constructor for objects of class Race
     */
    public RaceStrategy( ) {

    }

    /**
     * Given a board, will tell us the "best" move.
     */
    Move pickBestMove(Board aBoard, Player playerColor) {
        Move bestMove = null;
        if (aBoard.onBar(playerColor)) { // have to deal with coming in from bar!
            bestMove = pickBarMove( aBoard,playerColor ); 
        } else {
            /*System.err.println("AI should be racing to the finish, bearing off ASAP" 
            + "\nBut doesn't know how, yet. Can you help?");*/
            //        throw new IllegalArgumentException("The 'Race' Strategy doesn't work yet.");
            try {
                bestMove = aBoard.bestLegalMove( myScorer, /*partialsSoFar*/null, playerColor);
                //bestMove = aBoard.aLegalMove( playerColor , /*movesSoFar*/null);
            } catch(Exception e) {
                throw new IllegalArgumentException("had trouble making new Move\n:" + e);
            }
        }
        return bestMove;
    }

    public String toString( ) {
        return "RaceStrategy";
    }

} /* class RaceStrategy */
