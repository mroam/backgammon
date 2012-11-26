import java.awt.*; // supplies the Color

/**
 * To keep track of "white" and "black" (and perhaps more colors someday)
 * Might want to be singletons someday?
 * 
 * @author (Mike Roam) 
 * @version (2012 Apr 18)
 */
public class Player {
    /* points 19-24 are white's #4 (final) quadrant*/
    public static final int startWhiteFinalQuadrant = Board.howManyPoints - (Board.howManyPointsInFinalQuadrant - 1); 
    public static final int endWhiteFinalQuadrant = Board.howManyPoints;
    public static final int endBlackFinalQuadrant = 1; /* points 1-6 are black's #4 (final) quadrant */
    public static final int startBlackFinalQuadrant = endBlackFinalQuadrant+(Board.howManyPointsInFinalQuadrant - 1); 

    public static final int NEUTRAL = 0;
    public static final int WHITE = 1;
    public static final int BLACK = 2;

    private Color javaColor = BoardPict.clr_white;
    private int color = WHITE;
    private boolean isAI = false;

    private int howManyBlotsRequired = 15; /* for handicapping could allow someplayers to have more or less */

    private int lowPoint = 1; /* black moves from 24 to 1 (0), white 1..24(25) */
    private int highPoint = 24;
    private int startOfFinalQuadrant = 19; /* white 19..24, black 6..1 */
    private int endOfFinalQuadrant = 24;
    private int barLoc = 0; /* black will be 25 */
    private int bearOffLoc = 25; /* black will be 0 */
    private int beyondBearOffLoc = 100; /* black will be -100 */

    /**
     * Constructor for objects of class Player
     * 
     * Do I need a copy constructor? Not if these are sorta singleton
     */
    public Player(int newColor, boolean newIsAI) {
        isAI = newIsAI;
        switch (newColor) {
            case WHITE: 
            color = WHITE;
            javaColor = BoardPict.clr_white;
            howManyBlotsRequired = 15;/* for handicapping could allow someplayers to have more or less */
            lowPoint = 1;
            highPoint = Board.howManyPoints;
            startOfFinalQuadrant = startWhiteFinalQuadrant; /* white 19..24, black 6..1 */
            endOfFinalQuadrant = endWhiteFinalQuadrant;
            barLoc = 0;
            bearOffLoc = 25;
            beyondBearOffLoc = 100;
            break;

            case BLACK:
            color = BLACK;
            javaColor = BoardPict.clr_black;
            howManyBlotsRequired = 15;/* for handicapping could allow someplayers to have more or less */
            lowPoint = Board.howManyPoints;
            highPoint = 1;
            startOfFinalQuadrant = startBlackFinalQuadrant; /* white 19..24, black 6..1 */
            endOfFinalQuadrant = endBlackFinalQuadrant;
            barLoc = 25;
            bearOffLoc = 0;
            beyondBearOffLoc = -100;
            break;
            
            default:
            throw new IllegalArgumentException("Player must be White (1) or Black (2), not '" + newColor + "'");
        } /* switch */
    }

    /**
     * perhaps this should instead be compared to singleton white/black??
     */
    public int getColor( ) {
        return color;
    }

    public Color getJavaColor( ) {
        return javaColor;
    }

    /**
     * for handicapping could allow someplayers to have more or less blots
     */
    public int getHowManyBlotsRequired( ) {
        return howManyBlotsRequired;
    }

    /**
     * black and white supply different values for these so that I 
     * can use a general purpose for loop
     */
    public int getLowPoint( ) {
        return lowPoint;
    }

    public int getHighPoint( ) {
        return highPoint;
    }

    /**
     * replacing the specific white and black bear off zones that Board had
     * so that I can use a single for loop for either color 
     * (incrementing using the Player.nextPoint( ) method below)
     */
    public int getStartOfFinalQuadrant( ) {
        return startOfFinalQuadrant;
    }

    public int getEndOfFinalQuadrant( ) {
        return endOfFinalQuadrant;
    }

    public int getBarLoc( ) {
        return barLoc;
    }

    public int getBearOffLoc( ) {
        return bearOffLoc;
    }

    public int getBeyondBearOffLoc( ) {
        return beyondBearOffLoc;
    }

    /**
     * Maybe the player should be linked to its AI brain if it is an AI??
     */
    public boolean getIsAI( ) {
        return isAI;
    }

    /**
     * utility for checking that a number is in the final quadrant on the board.
     * Note: only 6 points are in this quadrant, not bearOffLoc and beyondBearOffLoc!
     */
    public boolean inFinalQuadrant( int i ) {
        if (startOfFinalQuadrant <= endOfFinalQuadrant) {
            return ((startOfFinalQuadrant <= i) && (i <= endOfFinalQuadrant));
        } else {
            return ((endOfFinalQuadrant <= i) && (i <= startOfFinalQuadrant));
        }
    }

    /**
     * utility for checking that a number is in the range of numbered points on the board.
     * Note: "inclusive" range! 1 and 24 are in range but bearOffLoc and beyondBearOffLoc are NOT!
     */
    public boolean inBoardZone( int i ) {
        if (lowPoint <= highPoint) {
            return ((lowPoint <= i) && (i <= highPoint));
        } else {
            return ((highPoint <= i) && (i <= lowPoint));
        }
    }

    /**
     * utility for checking that a number is in a range.
     * Note: "inclusive" range! 3 and 5 are in range "3..5"
     */
    public boolean inRange( int i , int oneEnd, int otherEnd) {
        if (oneEnd <= otherEnd) {
            return ((oneEnd <= i) && (i <= otherEnd));
        } else {
            return ((otherEnd <= i) && (i <= oneEnd));
        }
    }

    /**
     * So we can walk from starting "low" point to bear-off zone high points
     * black moves from (low) 24 to 1 (0), 
     * white moves from (low) 1 to 24 (25) 
     * 
     * @param  currPoint   must be 1..howManyPoints
     * @return     for black return --currPoint, white gets ++currPoint
     * 
     * BEWARE: use inBoardZone( ) or inFinalQuadrant( ) when looping, 
     * DON'T use < or > or == or != since we might be counting backwards!
     * 
     * Note: this might go out of bounds and return 
     * Board.WHITE_BEAR_OFF_LOC, Board.BLACK_BEAR_OFF_LOC,
     * but not Board.WHITE_PAST_BEAR_OFF_LOC, or Board.BLACK_PAST_BEAR_OFF_LOC
     */
    public int nextPoint(int currPoint) {
        if ( ! Board.legitStartLoc( currPoint, this )) { /* also checks legitPlayerColor */
            throw new IllegalArgumentException("Can't figure nextPoint( ) for player " 
                + this.toString( ) + " from point '" + currPoint + "'");
        }
        int theNextPoint;
        switch (color) {
            case WHITE: 
            theNextPoint = ++currPoint;
            if (currPoint > Board.howManyPoints) {
                theNextPoint = bearOffLoc;
            }
            break;
            case BLACK:
            theNextPoint = --currPoint;
            if (currPoint < 1) {
                theNextPoint = bearOffLoc;
            }
            break;
            default:
            throw new IllegalArgumentException("Player must be White (1) or Black (2), not '" + color + "'");
        } /* switch */
        return theNextPoint;
    }

    /**
     * So we can walk from starting "low" point to bear-off zone high points
     * black moves from (low) 24 to 1 (0), 
     * white moves from (low) 1 to 24 (25) 
     * 
     * @param  currPoint   must be 1..howManyPoints
     * @return     for black return ++currPoint, white gets --currPoint
     * 
     * BEWARE: use inBoardZone( ) or inFinalQuadrant( ) when looping, 
     * DON'T use < or > or == or != since we might be counting backwards!
     * 
     * Note: this might go out of bounds and return 
     * Board.WHITE_BEAR_OFF_LOC, Board.BLACK_BEAR_OFF_LOC,
     * but not Board.WHITE_PAST_BEAR_OFF_LOC, or Board.BLACK_PAST_BEAR_OFF_LOC
     */
    public int prevPoint(int currPoint) {
        if ( ! Board.legitStartLoc( currPoint, this )) { /* also checks legitPlayerColor */
            throw new IllegalArgumentException("Can't figure nextPoint( ) for player " 
                + this.toString( ) + " from point '" + currPoint + "'");
        }
        int theNextPoint;
        switch (color) {
            case WHITE: 
            theNextPoint = --currPoint;
            if (currPoint > Board.howManyPoints) {
                theNextPoint = bearOffLoc;
            } else if (currPoint < 1) {
                // throw new IllegalArgumentException("can't move backwards off the board");
                theNextPoint = barLoc;
            }
            break;
            case BLACK:
            theNextPoint = ++currPoint;
            if (currPoint < 1) {
                theNextPoint = bearOffLoc;
            } else if (currPoint > Board.howManyPoints) {
                // throw new IllegalArgumentException("can't move backwards off the board");
                theNextPoint = barLoc;
            }
            break;
            default:
            throw new IllegalArgumentException("Player must be White (1) or Black (2), not '" + color + "'");
        } /* switch */
        return theNextPoint;
    }

    /**
     * If given white, this returns black, and vice-versa.
     * If we ever get a game with more than 2 colors, this will die, and
     * so will its caller 'howImportantIsThisPoint( )'
     */
    public Player theReversePlayerColor( /*Player playerColor*/ ) {

        if (this /*playerColor*/ == Board.white) {
            return Board.black;
        } else if (this /*playerColor*/ == Board.black) {
            return Board.white;
        } else {
            throw new IllegalArgumentException("bad color '" + this/*playerColor*/ + "'");
        }
    } /* theReversePlayerColor( ) */

    /**
     * Utility for toString of our 2 colors (black,white)
     * Board has a version of this that includes neutral. Should this one include neutral??
     */
    public static String colorName( int color) {
        if ( ! legitColor( color ) ) {
            throw new IllegalArgumentException("bad color '" + color + "'");
        }
        /*if (color == neutral) {
        return "neutral (no color)";
        } else */ if (color == BLACK) {
            return "black";
        } else if (color==WHITE) {
            return "white";
        }
        return "unknown color '" + color + "'";
    } 

    /**
     * colorName.
     * Was wordy "[player black]" but is now "black"
     */
    public String toString( ) {
        //return "[player " + colorName(color) + "]";
        return colorName(color);
    }

    /**
     * Tells us if a Player's color is legal (only black and white currently allowed).
     * See legitColor if you're checking points, which can also be "neutral" color.
     * usage "if (player.legitColor( )) { ..."
     */
    public boolean legitColor(/*Player playerColor*/ ) {
        int itsColor = this.getColor( );
        return ( (itsColor == BLACK) || (itsColor==WHITE) );
    } // legitColor( )

    public static boolean legitColor( int color ) {
        return ( color == BLACK) || (color==WHITE);
    } // legitColor( )
}
