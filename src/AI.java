//import java.util.*;   // for collections

/**
 * class AI tries to think of good moves.
 * Note: "Game" is the main class of the whole project, creating a board and an AI.
 * 
 * @author (Mike Roam) 
 * @version (2012 Feb 6)
 * Gavin suggestion: that AI be an interface that can be implemented in various ways.
 */
public class AI {
    //private Player playerColor = Game.blackP; // allowing one AI to play both sides
    private Game myGame = null; /* gets set in constructor. Perhaps can be changed 
    if there are multiple boards ? */

    private Strategy myStrategy = new StartGameStrategy( );
    private int howManyTurnsSoFar = 0; // will use startGameStrat for only a few turns
    static final int startGameHowManyTurns = 1; // arbitrary

    /**
     * Constructor for objects of class AI
     */
    public AI(Game myNewGame) {
        myGame = myNewGame;
    } /* Constructor */

    //     /**
    //      * tells us the color that the AI is playing.
    //      */
    //     public Player getPlayerColor( ) {
    //         return playerColor;
    //     }

    /**
     * should there be a setMyStrategy( ) or better off letting
     * "AI.switchStrategy( )" take care of it more or less intelligently??
     */
    public Strategy getMyStrategy( ) {
        return myStrategy;
    }

    
    public int getHowManyTurnsSoFar( ) {
        return howManyTurnsSoFar;
    }
    
    public void setHowManyTurnsSoFar( int newHowMany ) {
        if (newHowMany < 0 ) {
            throw new IllegalArgumentException("No freakin' way can you set howManyTurnsSoFar to negative " 
            + newHowMany); 
        } else {
            howManyTurnsSoFar = newHowMany;
        }
    }
    /**
     * AI's main method (was called thinkAndPlay but doesn't play anymore)
     * Should this return a "Move" and somebody else handles moving?? -gy suggestion 2011
     */
    public Move chooseAMove(Board aBoard, Player playerColor) throws 
    BadBoardException, BadMoveException, BadPartialMoveException {
        // System.out.println("computer is thinking...");
        if (myGame.getCurrentPlayer( ) != playerColor) {
            throw new BadBoardException("AI can't move now, it's not AI's turn!");
        }

        ++howManyTurnsSoFar;
        if ( ! aBoard.myDice.getRolled( ) ) {
            if (aBoard == myGame.getMyBoard()) {
                myGame.doRoll( aBoard, playerColor); // includes roll & gui stuff
            } else {
                aBoard.myDice.roll( ); 
            }
            // if onBar then calls handleBar, if can'tMove then calls forfeit
        }
        myStrategy = pickAStrategy( aBoard, playerColor ); 

        Move aiMove = new Move(/*partials*/null, playerColor);
        if (! aBoard.canMove(playerColor)) {
            // hopeless
            return aiMove;
        }

        //Board temp = new Board(myGame, aBoard); //redundant: strategies work on a temp anyway
        // note: if we're on the bar, strategies are supposed to know to bring us in!
        aiMove = myStrategy.pickBestMove(aBoard/*temp*/, playerColor); 
        // does doMove care what board a move is linked to?
        //        if (! aiMove.getMyWantToForfeitTurn( )) {

        /* might be null, might not have any moves, might be blocked! */
        if ((aiMove == null) || (aiMove.getMyPartials( ).size( ) < 1) 
        || (! aiMove.isPossible(aBoard))) {
            // first Move was bad, let's try to get another from fallback...
            PointBuildStrategy pbs = new PointBuildStrategy( );
            aiMove = pbs.pickBestMove(aBoard, playerColor);
        }
        //        }
        //System.out.println(myMoves);
        System.out.println("\nDice==" + aBoard.getMyDice().shortString() + ", AI " + myStrategy 
            + " suggests " + aiMove + "");
        return aiMove;
        //        aiMove.doMove( );
    } // chooseAMove()

    /**
     * hmmm, we might get here if startGameStrategy is stuck,
     * so maybe we should switch to PointBuildStrategy.
     * 
     * hmmm, when do we switch from what to what?
     */
    public Strategy pickAStrategy(Board aBoard, Player playerColor ) {
        /* if it is ai's first move, return StartGameStrategy */
        /* if it is time for a race... */
        /* if we are 25% ahead in the pip count 
        OR if we have 5 points out of any group of 6 
        AND they are between the opponent and its bear out zone */
        // if (myStrategy.instanceOf(StartGameStrategy)) {
        Player otherColor = playerColor.theReversePlayerColor( );
        Class newStrategyClass = null;

        boolean canBear = aBoard.canBearOff(playerColor);
        boolean otherCanBear = aBoard.canBearOff(otherColor);
        boolean overlap = aBoard.playersOverlap( playerColor, otherColor);
        boolean startGameDone = (howManyTurnsSoFar > startGameHowManyTurns);

        //         if (startGameDone && (myStrategy instanceOf StartGameStrategy)) {
        //             newStrategyClass = PointBuildStrategy.class; // (/*caution:0.5?*/ );
        //         }
        if (canBear || otherCanBear || !overlap)  {
            // suggest which Strategy to use
            newStrategyClass = RaceStrategy.class;
        } else if (startGameDone) {
            newStrategyClass = PointBuildStrategy.class; // (/*caution:0.5?*/ );
        } else {
            newStrategyClass = StartGameStrategy.class;
        }
        // if (myStrategy != null) { System.out.println(myStrategy); }

        // only instantiate if we don't have the Strategy already
        if (myStrategy.getClass( ) != newStrategyClass) {
            Strategy newStrategyObject = myStrategy;
            try {
                newStrategyObject=(Strategy)newStrategyClass.newInstance( );
            } catch(Exception e) {
                // throws InstantiationException and IllegalAccessException
                System.err.println("Trouble building new '" + newStrategyClass.getName() + "': "+ e);
            }
            if (myStrategy == null) {
                System.out.println("starting strategy '" + newStrategyObject + "'");
            } else {
                System.out.println("\n*** switching strategy from " + myStrategy + " to " 
                + newStrategyObject + " *** \n");
            }
            return newStrategyObject; 
        } else {
            return myStrategy;
        }
    }
    /* class AI */ 
}