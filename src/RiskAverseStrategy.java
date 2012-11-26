
/**
 * RiskAverse Strategy - Tries to protect points and not risk getting knocked to bar.
 * 
 * @author M & J & J
 * @version 2012 Mar 11
 */
public class RiskAverseStrategy extends Strategy
{

    /**
     * Constructor for objects of class RiskAverse
     */
    public RiskAverseStrategy()
    {
    }

    /**
     * Given a board, will tell us the "best" move.
     */
    Move pickBestMove(Board currentBoard, Player theColor) {
        throw new IllegalArgumentException("The 'RiskAverse' Strategy doesn't work yet.");
//         if (aBoard.onBar(playerColor)) { // have to deal with coming in from bar!
//             bestMove = getInFromBar( currentBoard,playerColor ); 
//         } else {
// 
//         }
//         return bestMove;

    }

     public String toString( ) {
        return "RiskAverseStrategy";
    }
} /* class RiskAverseStrategy */
