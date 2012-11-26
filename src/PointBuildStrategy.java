
/**
 * PointBuild Strategy - tries to build protection on favorite points
 *
 * @author J^2+M
 * @version 2012 Mar 6
 */

import java.util.*; // provides Collections

public class PointBuildStrategy extends Strategy {

    // instance variables
    protected Caution myCaution; /* our mood can move from 0.0 to 1.0.
    Doesn't make any difference yet, but is a (useless) argument to
    PointBuildScorer.getSuperMegaHappyScore( ) */

    protected PointBuildScorer myScorer = null; // built in constructor

    //   Board currBoard = null;

    /**
     * Constructor for objects of class PointBuild
     */
    public PointBuildStrategy() {
        this(new Caution(0.0));
    }

    /**
     * Constructor for objects of class PointBuild
     */
    public PointBuildStrategy(Caution newCaution) {
        myCaution = new Caution(newCaution);
        myScorer = new PointBuildScorer(myCaution);
    }

    /**
     * Constructor for objects of class PointBuild
     */
    public PointBuildStrategy(double newCautiousness) {
        this(new Caution(newCautiousness)); // will be 0.0 if newC outside 0.0-1.0
    }

    /**
     * Given a board, will tell us the "best" move.
     */
    Move pickBestMove(Board aBoard, Player playerColor) {
        if (aBoard == null) {
            throw new IllegalArgumentException("Strategy can't work with null board!");
        }
        if (playerColor == null) {
            throw new IllegalArgumentException("Strategy can't work with null playerColor!");
        }
        Move bestMove = null;
        //currBoard = currentBoard; // might be null
        if (aBoard.onBar(playerColor)) { // have to deal with coming in from bar!
            bestMove = pickBarMove(aBoard, playerColor);
        } else {
            try {
                // bestMove = gatherALegitMove(currentBoard, playerColor); // Lame, not a well-picked move
                bestMove = aBoard.bestLegalMove( myScorer, /*partialsSoFar:*/null, playerColor);
            } catch(Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
        return bestMove;
        //        throw new IllegalArgumentException("The 'PointBuild' Strategy doesn't work yet.");
        // return newMove;
    }

    public Caution getMyCaution( ) {
        return myCaution;
    }

    public void setMyCaution( Caution other ){
        if (other != null) {
            myCaution = other;
        }
    }

    public void setMyCaution( double newCautiousness ) {
        myCaution = new Caution( newCautiousness ); // will be 0.0 if newC out of range 0.0-1.0
    }

    public Scorer getMyScorer( ) {
        return myScorer;
    }

    /* ----------------- Old Junk from AI, just to get this partly working */

    /**
     * was AI's main method "thinkAndPlay"
     */
    public Move gatherALegitMove(Board currentBoard, Player playerColor) throws BadBoardException, BadMoveException, BadPartialMoveException
    {
        // System.out.println("computer is thinking...");
        if (currentBoard == null) {
            throw new IllegalArgumentException("PointBuildStrategy.gatherALegitMove doesn't like " + "null board");
        }
        if (!playerColor.legitColor()) {
            throw new IllegalArgumentException("Bad color '" + playerColor + "'");
        }
        //         if (myGame.getCurrentPlayer( ) != myColor) {
        //             throw new BadBoardException("AI can't move now, it's not AI's turn!");
        //         }
        if (!currentBoard.myDice.getRolled()) {
            currentBoard.myDice.roll();
            // if onBar then calls handleBar, if can'tMove then calls forfeit
        }
        // if ( ! myGame.myBoard.onBar(myColor)) { myGame.myBoard.

        ArrayList < PartialMove > myChosenPartials = new ArrayList < PartialMove >();
        /* my final picks for pm1, pm2*/
        Board tempBoard = new Board(currentBoard.myGame, currentBoard);
        //int i = 0; // we didn't have to actually do the last Partial: was better not to,
        // when it ended a move, but now moves aren't ended except on main official board.
        //int howManyMoves = tempBoard.getMyDice( ).getNumOfPartialMovesAvail( );

        // Note: canMove( ) checks whether allDiceAreUsed( ) and if any legal moves remain.
        boolean gottaStop = false; // in case I'm getting null moves.
        while (tempBoard.canMove(playerColor) && (!gottaStop)) {
            // was while(i < howManyMoves ) {
            ArrayList < PartialMove > myMoves = tempBoard.allLegalPartialMoves(playerColor/* ,myGame*/);
            // how about trying aLegalMove( ) instead of allLegalPartialMoves( )????
            // No, as of 2012apr26 the Board.aLegalMove( ) isn't ready for primetime
            // and Board.allLegalMoves( ) doesn't have any code inside.
            //      ArrayList<Move> myMoves = myGame.myBoard.allLegalMoves( myColor/* , myGame*/);
            /* might not have any moves! */
            //System.out.println(myMoves);

            PartialMove gonnaMove = bestPartialMoveOf(currentBoard, myMoves);
            /* might be null */
            if (gonnaMove == null) {
                gottaStop = true;
            } else {
                /* Move gonnaMove = bestMoveOf( myMoves ); /* might be null */
                //System.out.println("AI will move to '" + gonnaMove + "'");
                myChosenPartials.add(gonnaMove);
                //             i++;
                //             if (i<howManyMoves) {
                tempBoard.doPartialMove(gonnaMove); // will mark a die as "used"
                //             }
            }
        }
        // okay, using currentBoard, not the damaged "tempBoard"
        return new Move(myChosenPartials, playerColor/* , currentBoard*/);
    } // gatherALegitMove()

    /**
     * for now just picks first PartialMove. This needs some major work to figure out good
     * (combo-of-partials) Move
     * Might want to compare boards using their getHowManyUnprotected( ) or  getHowManyProtected( )
     * or the new    getProtectedPointScore( )!! And should also compare pip counts since the opponent
     * might have got sent back!
     */
    public PartialMove bestPartialMoveOf(Board currentBoard, ArrayList < PartialMove > possibleMoves)/* throws BadMoveException*/
    {
        System.out.println("AI's doBestPartialMove( ) for now just picks first move. Fix!!");
        if (possibleMoves == null) {
            throw new NullPointerException("no possible Moves! Maybe just skip a turn?");
        }
        if (possibleMoves.isEmpty()) {
            return null;
        }
        PartialMove bestMove = possibleMoves.get(possibleMoves.size() - 1); /* counting from 0, like arrays! */
        System.out.println("My AI is dumb, just choosing last possible move. Will move to " + bestMove);
        return bestMove;
    } /* bestPartialMoveOf */

    /**
     * This needs some major work to figure out good (combo-of-partials) Move
     * Might want to compare boards using their getHowManyUnprotected( ) or  getHowManyProtected( )
     * or the new    getProtectedPointScore( )!! And should also compare pip counts since the
     * opponent might have got sent back!
     */
    public Move bestMoveOf(Board currentBoard, ArrayList < Move > possibleMoves)/* throws BadMoveException*/
    {
        System.out.println("AI's doBestMove( ) for now just picks first move. Fix!!");
        if (possibleMoves == null) {
            throw new NullPointerException("no possible Moves! Maybe just skip a turn?");
        }
        if (possibleMoves.isEmpty()) {
            return null;
        }
        if (myCaution.getCaution() > 0.5) {
            // be more timid
        }
        Move bestMove = possibleMoves.get(possibleMoves.size() - 1); /* counting from 0, like arrays! */
        System.out.println("My AI is dumbly choosing last possible move. Will move to " + bestMove);
        return bestMove;
    } /* bestMoveOf */
    
     public String toString( ) {
        return "PointBuildStrategy";
    }

} /* class PointBuildStrategy */
