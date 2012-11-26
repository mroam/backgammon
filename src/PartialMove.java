/**
 * Immutable! (Only has constructor and private fields and getters.)
 * Holds a "single" part of a move.
 * (one blot moves from point or bar to point or bear off)
 * 
 * HEY: this class has a copy constructor, so if you add new fields
 * You MUST add them to the copy constructor!
 * 
 * @author (Mike Roam) 
 * @version (2012 Feb 11)
 */
public class PartialMove implements Comparable<PartialMove>
{

    private final int rollValue;  /* keeping this for reference, but creator decides and 
    specifies end location, since bearing off sometimes allows higher roll if you
    there are no pieces that can use that full higher roll */
    
    // I've made Dice objects which can tell you whichDieHasValue( ), so I hope
    // not to need whichDie anymore in PartialMove, and instead use rollValues (1..6) everywhere
//    private final int whichDie;  /* Board.doMove( ) wants to know which die you're using? */

    /* note: start and end can be 1..Board.howManyPoints(==24) or
    use Board static final ints W/B_BAR_LOC, 
    W/B_BEAR_OFF and W/B_PAST_BEAR_OFF */
    private int start = Board.ILLEGAL_MOVE ;
    private int end  = Board.ILLEGAL_MOVE; 
    private Player color = Board.neutral;
    /* private Game myGame = null; /* is this necessary? */
    private Board myBoard = null; /*For comparing the quality score of moves*/

    // public static final int bar = 0;
    // public static final int bear = -1; /* just a symbol: is it bad to mismatch board?? */

    /**
     * Constructor for objects of class MoveOne
     * Note: start + rollValue might not equal end when bearing off
     * but creator of PartialMove should use 
     * int Board.endPointMovingFrom(int start, int steps, int color)
     * to calculate end.
     * and no higher blots can use a too-high rollValue.
     * ?? might want to know what game you belong to?? or can ask the board??
     */
    public PartialMove(int newStart, int newRollValue, int newEnd, Board newBoard, Player newColor/*, int newWhichDie*/)
    /*throws BadPartialMoveException, BadBoardException*/ {
        if (newBoard == null) {
            /* throw new BadBoardException*/
            throw new NullPointerException("Moves must know the board they belong to, can't be null board");
        }
        //myGame = newGame;
        myBoard = newBoard;
        if (Board.legitStartLoc( newStart,newColor ) && Board.legitEndLoc( newEnd, newColor ) && 
        (Dice.legitDiceValue( newRollValue )) && Board.legitColor( newColor.getColor( ) )) {
            start = newStart;
            rollValue = newRollValue;
            end = newEnd;
            color = newColor;
          //  whichDie = newWhichDie;
        } else {
            String msg = "[bad PartialMove start:" + newStart + " rollValue:" + newRollValue 
                + " end:" + newEnd + " color:" + newColor /*+ " whichDie:" + newWhichDie */ + "]";
            throw new IllegalArgumentException/*BadPartialMoveException*/(msg);
        }
    } // constructor with values

    /**
     * Copy constructor for deep copy!
     * Usage  PartialMove p = new PartialMove( existingPartial );
     * ?? Should I also implement clone( ) ??? Nah, online says bad implementation, subtle bugs.
     * "Game" is( could be) here so other Games can copy boards from this board's game
     */
    public PartialMove(/* Game myNewGame; */PartialMove otherPartial, Board otherBoard ) {
        /*if (myNewGame == null) {
        throw new NullPointerException("Can't give null Game to Board Constructor." 
        + " Boards HAVE to know their game.");
        } else*/ if (otherPartial == null) {
            throw new NullPointerException("Can't give null PartialMove to PartialMove Copy Constructor.");
        } /*else {
        myGame = myNewGame;
        }*/
        rollValue = otherPartial.rollValue;
//        whichDie = otherPartial.whichDie;
        start  = otherPartial.start;
        end  = otherPartial.end; 
        color = otherPartial.color;
        /* private Game myGame = null; /* is this necessary? */
       // myBoard = otherPartial.board; /*For comparing the quality score of moves*/
       myBoard = otherBoard;
    }

    /**
     * This could be done with the click (?? "handlePoint" ?? ) maybe? but then have to unclick?
     * 
     * This does NOT verify that any of the dice match the "rollValue" (distance moved), because we
     * might be constructing all kinds of hypothetical moves (if they do that, then we could do
     * this) without changing the dice for each move we consider.
     */
    public boolean isPossible( /*Board theBoard*/ ) {
        if ( ! Board.legitStartLoc( start, color ) ) {
            return false;
        }
        if (myBoard.getHowManyBlotsOnPoint(start, color) < 1 ) {
            return false;
        }
        /* start might be on bar, so using the fancy getColorOnPoint( ) */
        if (myBoard.getPlayerOnPoint( start,color ) != color) {
            return false;
        }
        int calculatedEnd = myBoard.endPointMovingFrom( start, rollValue, color);
        if (end != calculatedEnd) {
            return false;
        }
        // does legitEndLoc check color compatibility? probably just checks that it is not out of bounds
        if (! myBoard.canLandOn(end, color)) {
            return false;
        }

        return true;
    }

    public int getStart( ) {
        return start;
    }

    public int getEnd( ) {
        return end;
    }

    public Player getPlayerColor( ) {
        return color;
    }

    public int getRollValue( ) {
        return rollValue;
    }

//     public int getWhichDie( ) {
//         return whichDie;
//     }

    /**
     * was precise and verbose "[start:3, end:6, rollValue:3, color:[player black]]"
     * but now more concise "[[player black] 3=>6 (rollVal:3)]"
     */
    public String toString( ) {
        //return ("[start:" + start +  ", end:" + end + ", rollValue:" + rollValue 
        //+ ", color:" + color+"]");
        String startStr;
        if (start == color.getBarLoc( )) {
            startStr = "bar";
        } else {
            startStr = Integer.toString(start ); // simple number
        }
        String endStr;
        if ((end == color.getBearOffLoc( )) || (end == color.getBeyondBearOffLoc( ))) {
            endStr = "bearOff";
        } else {
            endStr = Integer.toString(end ); // simple number
        }
        return  "[" + color + " " + startStr + "=>" + endStr + " rollVal:" + rollValue + "]";
    }

    /**
     * has to check values inside PartialMoves
     */
    public boolean equals(Object other) throws ClassCastException  {
        // boolean identical = false;
        // do they have same values?
        if (!(other instanceof PartialMove)) {
            return false;
        }
        PartialMove otherPM = (PartialMove) other;
        return ((this.start==otherPM.start) && (this.rollValue==otherPM.rollValue) && (this.end==otherPM.end) 
        && (this.color==otherPM.color) /*&& (this.whichDie==otherPM.whichDie)*/); 
    } // equals( )

    public int hashCode() {
        return start + (rollValue*26) + (end * 6 * 26) + /*(whichDie * 2 * 6 * 26) +*/ (color.getColor( ) * 2 * 2 * 6 * 26);
    }

    /*
     * required function for implementing "Comparable"
     * returns negative integer, 0, or a positive integer depending on whether the 
     * "this" is less than, equal to, or greater than the other object.
     * 
     * ?? Does it matter if these are using the same dice "whichDie"????
     */
    public int compareTo(PartialMove other) {
        if (this.equals(other)) {
            return 0;
        }
        if (this.color.getColor( ) < other.color.getColor( )) {
            return -1;
        } else if (this.color.getColor( ) > other.color.getColor( )) {
            return 1;
        }
        if (this.start < other.start) {
            return -1;
        } else if (this.start > other.start) {
            return 1;
        }
        /* okay, starts are equal */
        if (this.rollValue < other.rollValue) {
            return -1;
        } else if (this.rollValue > other.rollValue) {
            return 1;
        }
        /* starts and rollValues are equal */
        if (this.end < other.end) {
            return -1;
        } else if (this.end > other.end) {
            return 1;
        } else {
            /* shouldn't get here since this implies start,rollValue & end are tied which
            the first ".equals( )" should have stopped! */
            String myMsg = "PartialMove '"+ this.toString( )+ "' isn't comparable to PartialMove '" 
            + other.toString( ) + "'";
            throw new ClassCastException(myMsg);
        }
    } // CompareTo
    // class PartialMove
}
