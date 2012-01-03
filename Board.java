/***************************************************************
JBackgammon (http://jbackgammon.sf.net)
 
Copyright (C) 2002
George Vulov <georgevulov@hotmail.com>
Cody Planteen <frostgiant@msn.com>
 
revised by Mike Roam, 2011


This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
****************************************************************/

/** 
 * File: board.java
 *
 * Description: This file contains the class for the backgammon board, 
 * keeping count how many pieces of which color are on each "point",
 * and providing moving and dice rolling. 
 */

 

import java.util.Random;

public class Board
{
    int howManyOnPoint[];
    int whichColorOnPoint[];

    int white_bar = 0;
    int black_bar = 0;
    int white_bear = 0;
    int black_bear = 0;
    
    public static final int neutral = 0;
    public static final int white = 1;
    public static final int black = 2;

    boolean rolled = false;
    Random rdice; // random number generator, gets started in constructor.
    /* int dice1;
    int dice2; */
    DiceRoll myDice;

    public static final int bar = 0; /* name of a place one can move to */
    public static final int bearoff = -1; /* a move might end on here */
    public static final int howManyPoints = 24; /* points are "spikes"; stored in array 0..24 just using 1..24 */
    public static final int howManyBlots = 15;
    public static final int howManyWhiteBlots = howManyBlots; /* some might be on bar or did bear off */
    public static final int howManyBlackBlots = howManyBlots; /* some might be on bar or did bear off */
    public static final int howManyDice = 2; /* with 3 dice could we do triples of rolls if all 3 dice the same? */
    public static final int diceHighNum = 6; /* we could use funky dice! */
    
    
    /**
     * Build a new game board
     */
    public Board()
    {
        howManyOnPoint = new int[howManyPoints + 1]; 
        // 0..24?? why 25 points? Think 0th point is the bar?
        // Ah-hah: maybe it is so that we get boxes 0..24 and can say [24] instead of minusing 1
        whichColorOnPoint = new int[howManyPoints + 1];
        rdice = new Random();

        for (int i=0; i<=howManyPoints; i++) // why not start at 1??
        {
            howManyOnPoint[i] = 0;
            whichColorOnPoint[i] = neutral;
        }
        try {
            /* makeStartingBoard( ); */ 
            make2PieceGame( );
            /* makeEasyHitStartingBoard( ); */
        } catch( BadBoardException e ) {
            System.out.print("ERROR building Board: " + e );
        }
    } // board constructor
    
    
    
    
    
    /**
     * The regular starting position (15 blots of each color)
     */
    public void makeStartingBoard( )  throws BadBoardException {
        setPoint(1, /* howMany */ 2, white); /* used to be 2 */
        setPoint(6, /* howMany */ 5, black);
        setPoint(8, /* howMany */ 3, black);
        setPoint(12, /* howMany */ 5, white);
        setPoint(13, /* howMany */ 5, black);
        setPoint(17, /* howMany */ 3, white);
        setPoint(19, /* howMany */ 5, white);
        setPoint(24, /* howMany */ 2, black);
        checkForBadNumberOfBlots( white );
        checkForBadNumberOfBlots( black );
    } //  makeStartingBoard( )
    
    
    
    /**
     * Starting position in which it is easy for players to hit each other
     * (Handy for testing!)
     */
    public void makeEasyHitStartingBoard( )  throws BadBoardException {
        setPoint(1, /* howMany */ 1, white); /* used to be 2 */
        setPoint(6, /* howMany */ 1, black);
        setPoint(8, /* howMany */ 1, black);
        setPoint(12, /* howMany */ 1, white);
        setPoint(13, /* howMany */ 1, black);
        setPoint(17, /* howMany */ 1, white);
        setPoint(19, /* howMany */ 1, white);
        setPoint(24, /* howMany */ 1, black);
        black_bar = 1;
        black_bear = 10;
        white_bar = 0;
        white_bear = 11;
        checkForBadNumberOfBlots( white );
        checkForBadNumberOfBlots( black );
    } //  makeEasyHitStartingBoard( )
    
    
    
    
    /** 
     * black and white each have removed 5 pieces already...
     */
    public void makeAlmostDoneGame( ) throws BadBoardException {
        setPoint(1, /* howMany */ 2, black); /* used to be 2 */
        setPoint(5, /* howMany */ 5, black);
        setPoint(6, /* howMany */ 3, black);
        black_bear = 5;
        
        setPoint(18, /* howMany */ 3, white);
        setPoint(19, /* howMany */ 5, white);
        setPoint(24, /* howMany */ 2, white);
        white_bear = 5;
        checkForBadNumberOfBlots( white );
        checkForBadNumberOfBlots( black );
    } // makeAlmostDoneGame
    
    
     /** 
     * black and white each have removed 14 pieces already...
     * note: white ends past 24, black ends below 1.
     */
    public void make2PieceGame( ) throws BadBoardException {
        setPoint(10, /* howMany */ 1, black);
        black_bear = 14;
        
        setPoint(11, /* howMany */ 1, white);
        white_bear = 14;
        System.out.println("created board with 2 pieces");
        checkForBadNumberOfBlots( white );
        checkForBadNumberOfBlots( black );
    } // makeAlmostDoneGame
    
    

    /**
     * Tells how many blots (pieces) of a particular color are here (on the bar, the bear, or the board).
     * There are supposed to always be 15 blots of each color. 
     * Used by "checkForBadNumberOfBlots( ) to check possible corruption of board.
     */
    public int howManyBlots(int theColor) {
        if ( ! legitColor(theColor) ) {
            throw new IllegalArgumentException("bad color '" + theColor + "'");
        }
        int blotCount= 0;
        if (theColor == white) {
            blotCount = white_bar + white_bear + getWhiteOnBoard( );
        } else {
            blotCount = black_bar + black_bear + getBlackOnBoard( );
        }
        // for (int p = 1; p <= howManyPoints; ++p) {
        //    if ( getColorOnPoint( p ) == theColor ) {
        //        System.out.println("found one");
        //        blotCount = blotCount + getHowManyBlotsOnPoint( p );
        //    }
        //} /* for */
        return blotCount;
    } /* howManyBlots( ) */
    
    
    /**
     * Checks for legal number of white and black blots.
     */
    public void checkForBadNumberOfBlots(int theColor) throws BadBoardException {
        if ( ! legitColor(theColor) ) {
            throw new IllegalArgumentException("bad color '" + theColor + "'");
        }
        if ( theColor == white ) {
            int howManyWhites = howManyBlots(white);
            if (howManyWhites != howManyWhiteBlots) {
                throw new BadBoardException("There are " + howManyWhites + " white blots but should be " + howManyWhiteBlots);
            }
        } else {
            int howManyBlacks = howManyBlots(black);
            if (howManyBlacks != howManyBlackBlots) {
                throw new BadBoardException("There are " + howManyBlacks + " black blots but should be " + howManyBlackBlots);
            }
        }
    } /* checkForBadNumberOfBlots */
    
    
    
    /** 
     * A blot might start on the bar or on a legit point.
    * Not sure whether point 0 is used in this implementation...
    * (See the JBackgammon.java file for rules of moves...)
    * Static so that other classes can use it without an instance, is my hope.
    */
    public static boolean legitStartPointNum( int pointNum ) {
        if ( (pointNum > -1) && (pointNum <= howManyPoints) )
        {
            return true;
        } else {
            return false;
        }
    } // legitStartPointNum( )
    
    
    /**
     * The biggest possible move in standard backgammon is 24: doubles of 6s
     */
    public static boolean legitStepsNum( int steps ) {
        if ( (steps > 0) && (steps <= (2 * howManyDice * diceHighNum)) )
        {
            return true;
        } else {
            return false;
        }
    } // legitStartPointNum( )
    
    
    /**
     * For black, use subtraction!?? I'll throw exception for negative steps!
     * For white, simple math in the middle of the board (endpoint = start + steps)
     * but trickier at the end since after final point is the bar.
     * This just calculates but doesn't actually try to move any pieces.
     * Is handy for creating partialMoves (which have start, roll, end )
     */
    public static int endPointMovingFrom( int startPoint, int steps, int color, Board board) throws BadBoardException {
    	if (board == null) {   /* hmmm, non static call, okay with "this"? */
    		throw new NullPointerException("board can't be null");
    	}
        if ( ! legitStartPointNum( startPoint )) {
            throw new BadBoardException("Can't start moving from point '" + startPoint + "'");
        }
        if ( ! legitStepsNum( steps )) {
            throw new BadBoardException("Can't move bad number of steps '" + startPoint + "'");
        }
        int endPoint = startPoint; /* temp value in case something goes wrong */
        if ( ! legitColor( color ) || (color==neutral)) {
        	throw new BadBoardException("bogus color '" + color + "'");
        } else if (color == white ) {
			 endPoint = startPoint + steps;
			if ( endPoint > howManyPoints ) {
				if (board.canBearOff( white )) {
					endPoint = bearoff;
				} else {
					endPoint = startPoint; /* can't move?? */
					System.out.println("blot at startpoint:" + startPoint + " can't move " + steps +" steps (not allowed to bear off yet)");
				}
			}
        } else if (color == black) {
        	endPoint = startPoint - steps; /* MINUS */
        	if ( endPoint < 1 ) {
				if (board.canBearOff( black )) {
					endPoint = bearoff;
				} else {
					endPoint = startPoint; /* can't move?? */
					System.out.println("blot at startpoint:" + startPoint + " can't move " + steps +" steps (not allowed to bear off yet)");
				}
			}
        }
        return endPoint;
    } /* end Point Moving From */
    
    
    /** 
     * A blot might end its move by bearing off, going to place that we'll call bearoff
    * Not sure whether point 0 is used in this implementation...
    * (See the JBackgammon.java file for rules of moves...)
    * Static so that other classes can use it without an instance, is my hope.
    */
    public static boolean legitEndPointNum( int pointNum ) {
        if ( pointNum == bearoff ) {
            return true;
        } else if ( (pointNum > -1) && (pointNum <= howManyPoints) )
        {
            return true;
        } else {
            return false;
        }
    } // legitEndPointNum( )
    
    
    
    /**
     * Elsewhere I allow 0 for dice not in use. Is this bad??
     */
    public static boolean legitRoll( int roll ) {
        return (( roll > 0) && (roll < 7));
    }

    
    /**
     * Tells us if a color is legal
     */
    public static boolean legitColor( int color ) {
        if ( (color == neutral) || (color == black) || (color==white) )
        {
            return true;
        } else {
            return false;
        }
    } // legitColor( )
    

    /**
     * Tells us which color is on the specified point (black, white, or neutral)
     */
    public int getColorOnPoint(int pointNum)
    {
        if ( legitStartPointNum(pointNum) )
        {
            return whichColorOnPoint[pointNum];
        } else {
            System.out.println("Bad pointNum '" + pointNum + "' in getColorOnPoint( )");
            return 0;
        }
    } // getColorOnPoint

    
    public int getHowManyBlotsOnPoint(int pointNum)
    {
        if ( ! legitStartPointNum(pointNum)) {
            System.out.println("Bad pointNum '" + pointNum + "' in getHowManyBlotsOnPoint( )");
            return 0;
        } else {
            return howManyOnPoint[pointNum];
        }
    } // getHowManyBlotsOnPoint( )






    /**
     * Specified point gets a number of pieces of specified color. 
     * Might the board be temporarily having a bad number of pieces while one is moving??
     */
    public void setPoint(int pointNum, int howMany, int color)
    {
        if ( ! legitStartPointNum(pointNum) ) {
            System.out.println("Bad pointNum '" + pointNum + "' in setPoint( )");
        } else if ( (howMany < 0) || (howMany > howManyBlots) ) {
        
        
        /* ******* Hey, shouldn't we check that there aren't too many pieces ?? ****/
        
            System.out.println("Bad howMany blots '" + howMany + "' in setPoint( )");
        } else if ( ! legitColor( color ) ) {
            System.out.println("Bad color '" + color + "' in setPoint( )");
        } else {
            howManyOnPoint[pointNum] = howMany;
            if (howMany==0) 
            {
                color=neutral;
            }
            whichColorOnPoint[pointNum] = color;
        } // legitPointNum
    } // setPoint( )
    

    public void rollDice()
    {
        int dice1 = rdice.nextInt(6) + 1;
        int dice2 = rdice.nextInt(6) + 1;
        rolled = true;
        myDice = new DiceRoll(dice1,dice2);
        System.out.println("I just rolled the dice and got " + myDice + "!");
    } // rollDice
    

    public int getDice1()
    {
        return myDice.getDice1( );
    } // getDice1

    
    public int getDice2()
    {
        return myDice.getDice2( );
    } // getDice2
    
    
    /**
    * Put both dice back to 0
    */
    public void resetDice()
    {
        myDice = new DiceRoll(0,0);
        rolled = false;
    } // resetDice
    

    /**
    * Moving to "bar" from the specified point.
    * Why is the point then getting set to color neutral?
    * Oh: because only a single can get sent to the bar.
    * What if more pieces are there: bogus setup
    */
    public void moveToBar(int pointNum)
    {
        if ( ! legitStartPointNum(pointNum)) {
            System.out.println("Bad pointNum '" + pointNum + "' in setPoint( )");
        } else {
            if (getColorOnPoint(pointNum)==white) 
            {
                white_bar++;
            } else {
                black_bar++; 
            }
            int howManyBlotsOnThisPoint = getHowManyBlotsOnPoint( pointNum );
            if ( howManyBlotsOnThisPoint == 1 ) {
                System.out.println("When does point " + pointNum + " lose the blot that is going to the bar?");
                setPoint(pointNum, /* howMany */ 0, neutral);
            } else {
                System.out.println("Error, how can blot move to bar from point " + pointNum + " which has " + howManyBlotsOnThisPoint + " blots??");
            }
        } // legitPointNum
    } // moveToBar


    /**
    * Says how how many moves left before black blots are all "beared off".
    * 
    * E.G. suppose there is one black blot on point 1: answer is 1
    * (But white counts the other way: final move on board for white is point 24.)
    */
    public int getBlackPipCount( )
    {
        int pipcount = 0;

        for (int i=1; i<=24; i++)   /* used to be i<25 */
        {
            if (getColorOnPoint(i)==black) 
            {
                pipcount += getHowManyBlotsOnPoint(i) * i;
            }
        }
        /* blots on the bar are 25 moves away from bearing off */
        pipcount += black_bar * 25; 

        System.out.println("Black pip count is " + pipcount);
        return pipcount;
    } // getBlackPipCount
    
    
    
    /**
    * Says how how many moves left before black blots are all "beared off".
    * 
    * At the start of the game we think this should be 162.
    * 
    * E.G. suppose there is one white blot on point 1: answer is 24.
    * (But black counts the other way: final move for black is point 1.)
    */
    public int getWhitePipCount( )
    {
        int pipcount = 0;

        for (int i=1; i<=24; i++)   /* used to be i<25 */
        {
            if (getColorOnPoint(i)==white) 
            {
                pipcount += getHowManyBlotsOnPoint(i) * (25 - i);
            }
        }
                /* blots on the bar are 25 moves away from bearing off */
        pipcount += white_bar * 25; 

        System.out.println("White pip count is " + pipcount);
        return pipcount;
    } // getWhitePipCount


    
    /*
     * Calculate white's total danger score, by figuring
     * for every exposed white pieces are there
     * And how far away are black pieces that could hit them
     * AND how far are those white pieces from the end?? At least by quadrant?
     * And do we care how far the exposed white pieces are from eventual safety?
     * 
     * Note: white starts on 1 and ends on 25 (bear)
     * 
     */
    public double getWhiteBeHitProbability( ) {
        double whiteBeHit = 0.0;
        /* finding the exposed white pieces */
        for (int pointNum=1; pointNum<=24; pointNum++) {
            if ( solitaryWhiteBlotOnPoint( pointNum ) ) {
                double thisWhiteBeHitProb = blackCanHitPointProb( pointNum );
                int distanceFromStart = pointNum; /* bar is starting point */
                int distanceToBearOff = 25 - pointNum;
                int quadrantNumber = whatIsTheWhiteQuadrantForPoint( pointNum );
                double thisProbScore = distanceFromStart * thisWhiteBeHitProb; /* ?? /* should this be linear?? */
                whiteBeHit += thisProbScore;
            }
        } 
        return whiteBeHit;
    } // getWhiteBeHitProbability( )

    
    /**
     * White starts on 1 and ends on 25.
     * What are its 4 quadrants called? Perhaps 1..6 is q1, 7..12 = q2, etc?
     * What about bar and bear?
     * Bar is quadrant 1?? Or Quadrants don't matter for bar
     */
    public int whatIsTheWhiteQuadrantForPoint( int pointNum ) throws ArrayIndexOutOfBoundsException {
        if ((pointNum >= 0) && (pointNum <= 6)) {
            return 1;
        } else if ((pointNum >= 7) && (pointNum <= 12)) {
            return 2;
        } else if ((pointNum >= 13) && (pointNum <= 18)) {
            return 3;
        } else if ((pointNum >= 19) && (pointNum <= 24)) {
            return 4;
        } else if ( ! legitStartPointNum(pointNum)) {
                System.out.println("Bad pointNum '" + pointNum + "' in whiteQuadrantForPoint( )");
                throw new ArrayIndexOutOfBoundsException( "Bad pointNum '" + pointNum + "' in whiteQuadrantForPoint( )" );
        } else {
            return 1; // bad
        }
    } // whiteQuadrantForPoint( )
    
    
    /**
     * for a particular point, what are the odds black can land on it
     */
    public double blackCanHitPointProb( int point ) {
        System.out.println("blackCanHitPointProb needs lots of work because not all rolls");
        System.out.println("work to bring dangerous blots onto us: if there is a protected");
        System.out.println("point in between then enemy can't use it as a step toward hitting us!");
        System.out.println("Uh-oh: don't forget that black pieces on bar can hit us!");
        return 0.5; // ?? obviously a fake answer
       // ??
    } // 
    

    /**
     * For a specific point, is there a solitary white piece on it? (Unprotected, exposed!)
     */
    public boolean solitaryWhiteBlotOnPoint( int pointNum ) {
        if ((getColorOnPoint(pointNum)==white)  && (getHowManyBlotsOnPoint(pointNum) == 1)) {
            return true;
        } else {
            return false;
        }
    } // solitaryWhiteBlotOnPoint( )
    
    
    
    /**
    * Says how many black pieces are still on the board.
    * Doesn't seem to count any on the bar??
    */
    public int getBlackOnBoard( )
    {
        int sum = 0;

        for (int i=1; i<=24; i++)   /* used to be i<25 */
        {
            if (getColorOnPoint(i)==black) 
            {
                sum += getHowManyBlotsOnPoint(i);
            }
        }

        System.out.println("There are currently " + sum + " black blots on the board");
        return sum;
    } // getBlack


    /**
    * Says how many white pieces are still on the board.
    */
    public int getWhiteOnBoard()
    {
        int sum = 0;

        for (int i=1; i<=24; i++)
        {
            if (getColorOnPoint(i)==white) 
            {
                sum += getHowManyBlotsOnPoint(i);
            }
        }

        return sum;
    } // getWhite


    /**
    * apparently pieces can't bear off until all 15 are on final 6 points?
    */
    public boolean canBearOff(int color)
    {
        int sum = 0;

        if (color==white)
        {   // add up the white pieces on last 6 points
            for (int i=19; i<=24; i++)
                {
                    if (getColorOnPoint(i)==white) 
                    {
                        sum += getHowManyBlotsOnPoint(i);
                    }
                }
                sum = sum + white_bear;
        } else if (color==black) {
            // add up black pieces on first 6 points
            for (int i=1; i<=6; i++)
            {
                if (getColorOnPoint(i)==black) 
                {
                    sum += getHowManyBlotsOnPoint(i);
                }
            }
            sum = sum + black_bear;
        } else {
            System.out.println("bad color param '"+ color + "' in canBearOff( )");
        }

        if (sum==howManyBlots) {
            return true;         //There are 15 blots" (pieces) in backgammon
        }
        return false;
    } // canBearOff
    
    
    /**
     * True if specified color has any blots on the bar
     */
    public boolean onBar(int color)
    {
        if (color==white)
        {
            if (white_bar>0) 
            {
                return true;
            } else {
                return false;
            }
        } else if (color==black) {
            if (black_bar>0) 
            {
                return true;
            } else {
                return false;
            }
        }

        return false;
    } // onBar
    

    public void setDice(int roll1, int roll2)
    {
        if (DiceRoll.hasLegitDiceValue(roll1) && DiceRoll.hasLegitDiceValue(roll2)) {
            myDice = new DiceRoll( roll1, roll2 );
        }
    } // setDice

} // class Board
