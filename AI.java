import java.util.*;   // for collections

/**
 * class AI tries to think of good moves.
 * 
 * @author (Mike Roam) 
 * @version (a version number or a date)
 * Gavin suggestion: that AI be an interface that can be implemented in various ways.
 * 
 */
public class AI
{
    private int myColor = JBackgammon.black;
    private JBackgammon myGame = null; /* gets set in constructor. Perhaps can be changed 
        if there are multiple boards ? */

        
    /**
     * Constructor for objects of class AI
     */
    public AI(JBackgammon myNewGame)
    {
        myGame = myNewGame;
        
    } /* Constructor */
    

    /**
     * AI's main method
     * Should this return a "Move" and somebody else handles moving?? -gy suggestion 2011
     */
    public void thinkAndPlay() throws BadBoardException, BadMoveException, BadPartialMoveException
    {
        System.out.println("computer is thinking...");
        if (myGame.getCurrent_player( ) != myColor) {
            throw new BadBoardException("AI can't move now, it's not AI's turn!");
        }
        if ( myGame.myBoard.rolled != true ) {
            myGame.doRoll( );
        }
        ArrayList<Move> myMoves = myGame.allLegalMoves( myColor, myGame );
        /* might not have any moves! */
        
        Move gonnaMove = bestMoveOf( myMoves ); /* might be null */
        /* to make the move actually happen, check out JBackgammon's methods:
         superMove( ), forfeit( ), endTurn( ) */
    } // thinkAndPlay()
    
    
    
    /**
     * for now just picks first move
     */
    public Move bestMoveOf( ArrayList<Move> possibleMoves ) throws BadMoveException {
        System.out.println("AI's doBestMove( ) for now just picks first move. Fix!!");
        if ( possibleMoves == null ) {
            throw new BadMoveException("no possible Moves! Maybe just skip a turn?");
        }
        if ( possibleMoves.isEmpty( ) ) {
            return null;
        }
        Move bestMove = possibleMoves.get(0); /* 1? */
        System.out.println( "AI is dumb, just choosing first move. Will move to " + bestMove );
        return bestMove;
    } /* bestMoveOf */
    
    
    
    
} /* class AI */
