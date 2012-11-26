import java.util.*;  // provides Collection ?

/**
 * Immutable!
 * 
 * Holds a "move", which includes 0, 1, 2, 3, or 4 partialMoves.
 * (note: the order matters sometimes: see the checkWhetherOrderMatters( ) method)
 * but if partials are possible( ) in either order, then order doesn't matter!
 * I'm not sure whether Moves and PartialMoves MUST know about their board, their context in life.
 * 
 * If this is going to be in a set, it has to implement equals & hashcode.
 * 
 * If this is going to be sorted (but how to compare moves to each other?)
 * (by seeing if their resulting boards are same arrangement of blots?)
 * it would have to "implements Comparable<Move>" and have a
 * public int compareTo(Move other) {
 * which returns negative integer, 0, or a positive integer depending on whether the 
 * "this" is less than, equal to, or greater than the other object.
 *
 * [ ]Would be nice to have a copy constructor
 * [ ]Would be nice to have a special Move that means forfeitTurn. (Or doMove
 * just uses up all our partials (perhaps none), and then ends the turn.
 * 
 * @author Julien S., Josh G., Mike Roam
 * @version (2011 Nov 15)
 */
public class Move /* implements Comparable<Move> */ {
    //private boolean cantMove = false; // stronger than wantToForfeitTurn
    //    private boolean wantToForfeitTurn = false; 
    /* Usually false. doMove still gets to
    do any partialMoves before finally forfeiting (the rest of) the turn. */
    // what other flags might I want? "win!"?

    protected Player myColor = Board.neutral; // very important: affects direction, bar, bear
    //    private final Game myGame==null; 
    protected /*final*/ ArrayList<PartialMove> myPartialMoves = null;
    protected Board myBoard = null; /*usually null. When comparing equality of moves we
    have to look at moves on a board to see whether order of partials matters. */
    /*private int howManyMoves = 0; redundant for moves.size()==0 and moves.isEmpty( )? silly?*/
    // private boolean orderMatters = false; /* we'd better decide this in a method. */
    //private int howManyBlotsAreMoving = 1; // might be 4 pieces moving with doubles!

    protected static final int maxPartialMovesInAMove = 4; 
    /* if we ever have more dice, might change */

    /**
     * Constructor for objects of class Move, receiving 2?? moves.
     * 0,1,2,3,4 are all possible since we might have rolled doubles and might be blocked.
     * 
     * partialMoves hold playerColor info, but we need the playerColor info anyway
     * if there are no partial moves (if this is a short move and/or forfeitTurn).
     */
    public Move(ArrayList<PartialMove> theNewPartials, Player myNewColor 
    /*Game myNewGame*/ /*,Board myNewStarterBoard /*Final?*/)
    /*Can't be starter board because some of the partials may have happened to it already!*/
    throws BadBoardException, BadMoveException    {
        if (theNewPartials == null) {
            myPartialMoves = new ArrayList<PartialMove>( ); // empty
        } else {
            myPartialMoves = theNewPartials;
        }
        if (Board.legitColor(myNewColor.getColor( ))) {
            myColor = myNewColor;
        } else {
            throw new BadMoveException("bogus color '" + myNewColor + "'");
        }
        //         if (myNewStarterBoard/*Game*/ == null) {
        //             throw new BadBoardException("Moves must know the board/*game*/ they belong to, "
        //             + "can't be null");
        //         }
        //        myBoard/*Game*/ = myNewStarterBoard/*Game*/;

        int listSize =  myPartialMoves.size( );
        if (listSize > maxPartialMovesInAMove) {
            throw new BadMoveException("Weird: I'm building a move that has " + listSize 
                + " partial moves, more than max allowed (" + maxPartialMovesInAMove +")!");
        }
        // orderMatters = checkWhetherOrderMatters( myNewColor, myNewStarterBoard );
    } // constructor

    /**
     * Constructor, receiving int value specification of 2-partials
     * Need a version of this for 4 partials (doubles were rolled)?
     * @param aBoard need this for checking legality of the moves
     */
    public Move(int start1, int end1, int rollVal1, 
    int start2, int end2, int rollVal2, Board aBoard, Player playerColor) {
        // not bothering to check legitColor(playerColor) now that it is self protecting Class
        int shouldBeEnd1 = aBoard.endPointMovingFrom(start1, rollVal1, playerColor/*, this*/);
        if (end1 != shouldBeEnd1) {
            throw new IllegalArgumentException("bad end1:" + end1 + " isn't " + rollVal1 
                + " steps from start1:" + start1 + ", Should be " + shouldBeEnd1 + "?");
        }
        int shouldBeEnd2 = aBoard.endPointMovingFrom(start2, rollVal2, playerColor/*, this*/);
        if (end2 != shouldBeEnd2) {
            throw new IllegalArgumentException("bad end2:" + end2 + " isn't " + rollVal2 
                + " steps from start2:" + start2 + ", Should be " + shouldBeEnd2 + "?");
        }

        PartialMove pm1 = null;
        PartialMove pm2 = null;

        pm1 = new PartialMove( start1, rollVal1, end1, aBoard, playerColor/* dieNum*/);
        pm2 = new PartialMove( start2, rollVal2, end2, aBoard, playerColor/* dieNum*/);

        ArrayList<PartialMove> theNewPartials = new ArrayList<PartialMove>( );
        theNewPartials.add(pm1);
        theNewPartials.add(pm2);

        myPartialMoves = theNewPartials;
    }

    /**
     * copy constructor
     * does deep copy of partialMoves since other classes might adjust the original partials
     */
    public Move(Move other) {
        myColor = other.myColor;
        if (other.myPartialMoves == null) {
            myPartialMoves = null;
        } else {
            myPartialMoves = new ArrayList<PartialMove>(other.myPartialMoves);
        }
        myBoard = other.myBoard;
    }

    /**
     * boards are usually null, but should get set if we're going to compare equality of moves
     * and are considering equality in which order of partials doesn't matter.
     */
    public Board getMyBoard( ) {
        return myBoard; // null by default
    }

    /**
     * boards are usually null, but should get set if we're going to compare equality of moves
     * and are considering equality in which order of partials doesn't matter.
     */
    public void setMyBoard(Board newMyBoard ) {
        myBoard = newMyBoard; // null by default, so setting to null is a-ok
    }

    //     /**
    //      * Moves can carry the "wantToForfeitTurn" flag
    //      */
    //     public boolean getWantToForfeitTurn( ) {
    //         return wantToForfeitTurn;
    //     }
    //     
    //     /**
    //      * Moves can carry the "wantToForfeitTurn" flag
    //      */
    //     public void setWantToForfeitTurn(boolean newWantToForfeitTurn ) {
    //         wantToForfeitTurn = newWantToForfeitTurn;
    //     }

    /**
     * not a clone but a pointer. Beware!!?? 
     */
    public final ArrayList<PartialMove> getMyPartials( ) {
        return myPartialMoves; 
    }

    /**
     * See if ANY of the partialMoves are blocked or illegal.
     * Since each move changes the board, we'll use a temporary
     * board and play out the moves (In Order!) upon it.
     * NOT playing the last move!
     * Checks if the dice are rolled to values used by this move.
     */
    public boolean isPossible( Board theBoard ) {
        Board tempBoard = new Board( theBoard.myGame, theBoard );
        ArrayList<PartialMove> thePartials = this.getMyPartials();
        int howMany = thePartials.size( );
        int i = 0; // we don't have to actually do the last Partial: better not to, 
        // since it ends a move
        for (PartialMove p : thePartials) {
            PartialMove tempPartial = new PartialMove( p, tempBoard ); 
            // points the partialMove toward tempBoard
            // tempPartial.setMyBoard( tempBoard );
            if (! tempPartial.isPossible( ) ) {
                return false; // sudden death: method is over
            }
            i++;
            if (i<howMany) {
                tempBoard.doPartialMove( tempPartial );
            }
        }
        return true;
    }

    // moving this to Board.doMove, combined with that (unused?) Game.doMove( )
    //     /**
    //      * Game has doMove also, but AI calls this one. Which is better??
    //      * This one just calls Board.doPartialMove, never calls Board.doMove
    //      */
    //     public void doMove( Board theBoard ) {
    //         /* to make the move actually happen, check out Game's methods:
    //         superMove( ), forfeit( ). Note: superMove( ) calls endTurn( ) */
    //         for (PartialMove p : this.getMyPartials()) {
    //             //   myGame.myBoard.handlePoint( p.getStart( )   );
    //             //   myGame.doPartialMove(p );
    //             //   System.err.println("DoubletCountdown is '" + myBoard.getMyDice().toString( ) 
    //             // + "'");
    //             theBoard.doPartialMove( p ); // will end turn if allDiceAreUsed
    //         }
    // 
    //         // maybe better not to do the following, let some bigger boss do it??
    //         // No, perhaps this move can only do 0 or 1 partial & then forfeits rest of turn
    //         if (theBoard==Game.mainBoard) {
    //             if (! theBoard.getMyDice( ).allDiceAreUsed( )) { // allDice ended turn above
    //                 theBoard.myGame.forfeitTurn(theBoard,myColor ); // endTurn
    //             }
    //         }
    //     }

    /**
     * has to check values inside PartialMoves.
     * Uh-oh: context matters. On some boards, the PartialMoves will work in any
     * order so order doesn't matter _on_that_board_ but on other boards the
     * partials can only happen in a certain order since blocked in other order,
     * and then the order of partials matters when deciding equality of Moves.
     * So "equals( )" needs context (Moves must own non-null board?) or else
     * order matters by default??
     * perhaps moves have a (usually null) board, and AI could say to itself
     *    move1.setBoard(tempBoard); move2.setBoard(tempBoard); if (move1.equals(move2))
     * and orderMatters( ) checks whether boards are null...
     * 
     * Note: occasions in which order matters are
     * (1) Bar entry and then a move upon board
     * (2) Moving to an unoccupied (or enemy occupied) blot and then moving on from there
     * (3) Bringing last piece into final quadrant which then allows next move to bear off
     */
    public boolean equals(Object other)    {
        if (!(other instanceof Move)) { /* takes care of null! */
            return false; /* null list is different than empty existing list!? */
        }
        Move otherMove = (Move)other;
        if (this.myPartialMoves.size( ) != otherMove.getMyPartials( ).size( )) {
            return false;
        }
        /* first check in order */
        if ( this.hasSameValuesAs( otherMove )) {
            return true;
        } else return( resultsSame( otherMove ) );
        //} else if (!orderMatters(otherMove )) {
        //    try {
        //        return this.hasSameValuesInDifferentOrderFrom( otherMove );
        //    } catch (BadMoveException e) {
        //        System.out.println("Bad Move Exception:" + e);
        //    }
        //}
        //return false;
    } // equals( )

    /**
     * called by equals( )
     */
    private boolean hasSameValuesAs( Move other ) {
        if ( other == null ) {
            return false; /* null list is different than empty existing list!? */
        }
        return this.myPartialMoves.equals( other.myPartialMoves );
    } // hasSameValuesAs( )

    /**
     * Called by equal( ), which has already checked for null and size mismatch, but I'll 
     * check them again in case I ever want to re-use this function.
     * 
     * Since collections can be sorted, I sort both lists of moves and then compare them in 
     * parallel. (Early idea was just check containsAll in both directions, but that could 
     * give false positive if they have the same partialMoves but in different proportions! 
     * eg [2,2,3] =? [2,3,3])
     */
    private boolean hasSameValuesInDifferentOrderFrom ( Move other )  throws BadMoveException {
        if ( other == null ) {
            return false; /* null list is different than empty existing list!? */
        }
        if (this.myPartialMoves.size( ) != other.myPartialMoves.size( )) {
            return false;
        }

        List<PartialMove> myPartialsSorted = new ArrayList<PartialMove>( this.getMyPartials( ));
        List<PartialMove> otherPartialsSorted = new ArrayList<PartialMove>(other.getMyPartials());
        Collections.sort(myPartialsSorted);
        Collections.sort(otherPartialsSorted);
        int howManyPartialMoves = myPartialMoves.size( );
        for (int i = 0; i < howManyPartialMoves; ++i ) {
            if ( ! myPartialsSorted.get(0).equals(otherPartialsSorted.get(0) )) {
                return false; /* mismatch, so we're out of here! */
            }
        }
        return true; /* made it through all matching */
    } // hasSameValuesInDifferentOrderFrom( )

    /**
     * This is called by equals( ) to compare to another move.
     * 
     * Order of moves might matter
     * --when only some of the blots are coming in from the bar...
     * --?when one or more blots are bearing off, 
     *-- when there are protected enemy points in between,
     * --when one piece is making multiple moves.
     * 
     * The creator can't know whether order matters unless they're sure what board
     * is the context?? And could just tell us?
     * 
     * Or maybe order should always matter? Nah, but we might have to test on (copies of)
     * our board to see if the results are the same... meaning boards would need an equality
     * test.
     */
    public boolean resultsSame( Move otherMove /*Player playerColor, Board aBoard*/ ) {
        // draft 2:
        if ((myBoard == null) /*|| (otherMove.getMyBoard( ) == null)*/) {
            return true; // I need board context(s?) to compare alternatives
        }
        Board temp = new Board(myBoard.myGame, myBoard);
        // try the moves of this, and the other Move
        temp.doMove( this, myColor);
        // hmmm, trying the moves on (copies of) the SAME board, but should I try each on its own?
        // or perhaps if either has a board, use that one??
        Board tempOther = new Board(myBoard.myGame, myBoard); // ?? <-- new Board(otherMove.getMyBoard( ));
        tempOther.doMove( otherMove, myColor );
        return temp.sameBlotLocations(tempOther);

        // draft 1:
        //         boolean tempOrderMatters = false;
        //         for (PartialMove aPartialMove : myPartialMoves) {
        //             // wrong: we could have multiple inFromBar that could happen in any order
        //             if ( aPartialMove.getStart( ) == myColor.getBarLoc( ) ) {
        //                 return true;
        //             }
        //             if ( aPartialMove.getEnd( ) == my.getBearOffLoc( ) ) {
        //                 return true;
        //             }
        //             /* next check if a blot is planning to move from a place it hasn't reached yet */
        //             if (aBoard.getPlayerOnPoint( aPartialMove.getStart( ),playerColor ) 
        //                     != aPartialMove.getPlayerColor( ) ) {
        //                 return thisPathOnlyWorksInOneOrder( ); 
        //             }
        //         } /* for */

        //throw new IllegalArgumentException( "not properly testing for the relevance of order of"
        //    + " PartialMoves mattering, FIX! ");
        //         return false; // for now... 
    }

    /**
     * Tricky: called when we see that a blot is moving twice: detected by 
     * "checkWhetherOrderMatters" which sees somebody moving from a place they haven't 
     * reached yet. Maybe the blot is moving 2 and then 4, and maybe it couldn't get 
     * there if it instead moved 4 and then 2. Check it out by finding the double moving 
     * blot in the PartialMove and ask the board if the other path hits
     * Board.protectedPoint(  other color ) ... if so, this method returns TRUE;
     */
    public boolean thisPathOnlyWorksInOneOrder( ) {
        //  System.out.println("Move's thisPathOnlyWorksInOneOrder( ) isn't coded yet," 
        // + " so we don't know");
        //  System.out.println("whether order of steps matters for this path.");
        // throw new BadMoveException( " thisPathOnlyWorksInOneOrder( ) not written yet ");
        return false;
    } 

    /* thisPathOnlyWorksInOneOrder( ) */

    public String toString( ) {
        StringBuffer tempStrBuf = new StringBuffer("[" );
        for (PartialMove aPartialMove : myPartialMoves) {
            tempStrBuf.append( aPartialMove.toString( ) );
            tempStrBuf.append( "," );
        }
        int size = tempStrBuf.length( );
        tempStrBuf.delete(/*start*/size-1, /*after end*/size); // remove the last comma
        tempStrBuf.append( "]");
        return tempStrBuf.toString( );
    }

    public int hashCode( ) {
        // equal Moves have to have equal hashCodes!!
        int hash = 0;
        for (PartialMove aPartialMove : myPartialMoves) {
            hash = hash * aPartialMove.hashCode( );
        }
        System.out.println("Move's hashCode isn't really calculating. FIX!!");
        return hash; /* how about product or sum of all hashcodes? */
    }
    // class Move
}