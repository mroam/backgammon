
/**
 * Immutable!
 * Holds a "single" part of a move.
 * (one blot moves from point or bar to point or bear off)
 * 
 * @author (Mike Roam) 
 * @version (2011 dec 6)
 */
public class PartialMove implements Comparable<PartialMove>
{
    // instance variables - replace the example below with your own
    private final int roll;  /* keeping this for reference, but creator decides and 
    specifies end location, since bearing off sometimes allows higher roll if you
    there are no pieces that can use that full higher roll */
    
    private final int start; /* note: 0 means from bar */
    private final int end; 
    private final int color;
    private final JBackgammon myGame;
    
    public static final int bar = 0;
    public static final int bear = -1; /* just a symbol: is it bad to mismatch board?? */
    
    /**
     * Constructor for objects of class MoveOne
     * Note: start + roll might not equal end when bearing off
     * but creator of PartialMove should use static function
     * int Board.endPointMovingFrom(int start, int steps, int color, Board board)
     * to calculate end.
     * and no higher blots can use a too-high roll.
     */
    public PartialMove(int newStart, int newRoll, int newEnd, JBackgammon newGame, int newColor) throws BadPartialMoveException, BadBoardException
    {
        if (newGame == null) {
            throw new BadBoardException("Moves must know the game they belong to, can't be null game");
        }
        myGame = newGame;
        if (Board.legitStartPointNum( newStart ) && Board.legitEndPointNum( newEnd ) && 
              (Board.legitRoll( newRoll )) && Board.legitColor( newColor )) {
          start = newStart;
          roll = newRoll;
          end = newEnd;
          color = newColor;
        } else {
            String msg = "bad PartialMove start:" + newStart + " roll:" + newRoll + " end:" + newEnd + " color:" + newColor;
            throw new BadPartialMoveException(msg);
        }
    } // constructor with values
    
    
    public int getStart( ) {
        return start;
    }
    
    
    public int getEnd( ) {
        return end;
    }
    
    
    public int getColor( ) {
        return color;
    }
    
    
    public int getRoll( ) {
        return roll;
    }
    
    
    public String toString( ) {
        return ("start:" + start + ",roll:" + roll + ",end:" + end + "color:" + color);
    }
    

    /**
     * has to check values inside PartialMoves
     */
    public boolean equals(Object other) throws ClassCastException
    {
        // boolean identical = false;
        // do they have same values?
        if (!(other instanceof PartialMove)) {
            return false;
        }
        PartialMove otherPM = (PartialMove) other;
        return ((this.start==otherPM.start) && (this.roll==otherPM.roll) && (this.end==otherPM.end)); 
    } // equals( )
    
    
    public int hashCode() {
        return start * roll * end;
    }
    
    
    /*
     * required function for implementing "Comparable"
     * returns negative integer, 0, or a positive integer depending on whether the 
     * "this" is less than, equal to, or greater than the other object
     */
    public int compareTo(PartialMove other) {
        if (this.equals(other)) {
            return 0;
        }
        if (this.start < other.start) {
            return -1;
        } else if (this.start > other.start) {
            return 1;
        }
        /* okay, starts are equal */
        if (this.roll < other.roll) {
            return -1;
        } else if (this.roll > other.roll) {
            return 1;
        }
        /* starts and rolls are equal */
        if (this.end < other.end) {
            return -1;
        } else if (this.end > other.end) {
            return 1;
        } else {
            /* shouldn't get here since this implies start,roll & end are tied which
             the first ".equals( )" should have stopped! */
             String myMsg = "PartialMove '"+ this.toString( )+ "' isn't comparable to PartialMove '" + other.toString( ) + "'";
             throw new ClassCastException(myMsg);
        }
    } // CompareTo
    
} // class MoveOne
