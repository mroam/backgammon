import java.util.*;  // provides Collection ?

/**
 * Immutable!
 * 
 * Holds a "move", which includes 0, 1, 2, 3, or 4 partialMoves.
 * (note: the order matters sometimes: see the checkWhetherOrderMatters( ) method)
 * 
 * I'm not sure whether Moves and PartialMoves MUST know about their board, their context in life.
 * 
 * If this is going to be in a set, it has to implement equals & hashcode
 * If this is going to be sorted (but how to compare moves to each other?)
 * it would have to "implements Comparable<Move>" and have a
 * public int compareTo(Move other) {
 * which returns negative integer, 0, or a positive integer depending on whether the 
 * "this" is less than, equal to, or greater than the other object.
 *
 * @author Julien S., Josh G., Mike Roam
 * @version (2011 Nov 15)
 */
public class Move /* implements Comparable<Move> */
{
    
    
    private final int myColor;
    private final JBackgammon myGame; 
    private final ArrayList<PartialMove> myPartialMoves;
    /* private int howManyMoves = 0; redundant for moves.size()==0 and moves.isEmpty( )? silly? */
    private boolean orderMatters = false; /* we'd better decide this in the constructor?? */
    //private int howManyBlotsAreMoving = 1; // might be 4 pieces moving with doubles!

    private static final int maxPartialMovesInAMove = 4; /* if we ever have more dice, might change */
    
    
    /**
     * Constructor for objects of class Move, receiving 2 moves.
     * 0,1,2,3,4 are all possible since we might have rolled doubles and might be blocked.
     */
    public Move(ArrayList<PartialMove> theNewPartials, int myNewColor, JBackgammon myNewGame)
    throws BadBoardException, BadMoveException
    {
        if (theNewPartials == null) {
            myPartialMoves = new ArrayList<PartialMove>( ); // empty
        } else {
            myPartialMoves = theNewPartials;
        }
        if (Board.legitColor(myNewColor)) {
            myColor = myNewColor;
        } else {
            throw new BadMoveException("bogus color '" + myNewColor + "'");
        }
        if (myNewGame == null) {
            throw new BadBoardException("Moves must know the game they belong to, can't be null game");
        }
        myGame = myNewGame;
        
        int listSize =  myPartialMoves.size( );
        if (listSize > maxPartialMovesInAMove) {
            System.out.println("Weird: I'm building a move that has " + listSize + " partial moves, more than max allowed (" + maxPartialMovesInAMove  +")!");
            throw new BadMoveException("Weird: I'm building a move that has " + listSize + " partial moves, more than max allowed (" + maxPartialMovesInAMove +")!");
        }
        orderMatters = checkWhetherOrderMatters( );
    } // constructor
    
    
      
    public final ArrayList<PartialMove> getMyPartials( ) {
        return myPartialMoves; /* not a clone but a pointer. Beware!!?? */
    }
  

    /**
     * has to check values inside PartialMoves
     */
    public boolean equals(Object other)
    {
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
        } else if (!orderMatters) {
            try {
                return this.hasSameValuesInDifferentOrderFrom( otherMove );
            } catch (BadMoveException e) {
                System.out.println("Bad Move Exception:" + e);
            }
        }
        return false;
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
     * Called by equal( ), which has already checked for null and size mismatch, but I'll check
     * them again in case I ever want to re-use this function.
     * 
     * Since collections can be sorted, I sort both lists of moves and then compare them in parallel.
     * (Early idea was just check containsAll in both directions, but that could give false positive 
     * if they have the same partialMoves but in different proportions! eg [2,2,3] =? [2,3,3])
     */
    private boolean hasSameValuesInDifferentOrderFrom ( Move other )  throws BadMoveException {
        if ( other == null ) {
            return false; /* null list is different than empty existing list!? */
        }
        if (this.myPartialMoves.size( ) != other.myPartialMoves.size( )) {
            return false;
        }

        List<PartialMove> myPartialsSorted = new ArrayList<PartialMove>( this.getMyPartials( ) );
        List<PartialMove> otherPartialsSorted = new ArrayList<PartialMove>( other.getMyPartials( ) );
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
     * This is called by constructor to set a flag. Won't change, will it??
     * 
     * Order of moves matters when one or more blots is coming in from the bar, or
     * when one or more blots are bearing off, or there are protected enemy points in between
     * when one piece is making multiple moves.
     * 
     * Maybe the creator would know better whether order matters??? And could just tell us?
     */
    public boolean checkWhetherOrderMatters( ) {
        boolean tempOrderMatters = false;
        for (PartialMove aPartialMove : myPartialMoves) {
            if (aPartialMove.getStart( ) == Board.bar ) {
                return true;
            }
            if (aPartialMove.getEnd( ) == Board.bearoff ) {
                return true;
            }
            /* next check if a blot is planning to move from a place it hasn't reached yet */
            if (myGame.myBoard.getColorOnPoint( aPartialMove.getStart( ) ) != aPartialMove.getColor( ) ) {
                return thisPathOnlyWorksInOneOrder( ); 
            }
        } /* for */
        System.out.println( "not really testing for the possibility of order of PartialMoves mattering, FIX! ");
        return false; // for now... 
    }
    
    
    /**
     * Tricky: called when we see that a blot is moving twice: detected by "checkWhetherOrderMatters"
     * which sees somebody moving from a place they haven't reached yet. Maybe the blot is moving 2
     * and then 4, and maybe it couldn't get there if it instead moved 4 and then 2. Check it out
     * by finding the double moving blot in the PartialMove and ask the board if the other path hits
     * Board.protectedPoint(  other color ) ... if so, this method returns TRUE;
     */
    public boolean thisPathOnlyWorksInOneOrder( ) {
        System.out.println("Move's thisPathOnlyWorksInOneOrder( ) isn't coded yet, so we don't know");
        System.out.println("whether order of steps matters for this path.");
       // throw new BadMoveException( " thisPathOnlyWorksInOneOrder( ) not written yet ");
        return false;
    } /* thisPathOnlyWorksInOneOrder( ) */
        
    
    
    public String toString( ) {
        StringBuffer temp = new StringBuffer("[" );
        for (PartialMove aPartialMove : myPartialMoves) {
            temp.append( aPartialMove.toString( ) );
            temp.append( "," );
        }
        temp.append( "]");
        return temp.toString( );
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
    
} // class Move
