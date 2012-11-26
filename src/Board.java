/* **************************************************************
JBackgammon (http://jbackgammon.sf.net)

Copyright (C) 2002
George Vulov <georgevulov@hotmail.com>
Cody Planteen <frostgiant@msn.com>

revised by Julien S., Joshua G., Mike Roam, 2011-2

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
 * The backgammon board, 
 * keeping count how many blots of which color are on each "point",
 * and providing moving and dice rolling. 
 */

import java.util.*;  // provides Collections

public class Board {
    /* if you add more fields here, add them to the copy constructor, also! 
    Beware deep copy if the new fields hold (pointers to) objects! */

    Game myGame = null; /* better be set up in constructor or die! */

    int howManyOnPoint[ ]; /* just for board points (1..24), Beware: numbered 1..24, NOT 0..23! */
    Player whichPlayerOnPoint[ ];  // !!?? was int

    /* These should be arrays for multicolor calling convenience! */
    int bar[ ]; // bar[0] is nothing, bar[1] is # of blots on white bar, bar[2]=black_bar;
    //     int white_bar = 0; /* how many blots on white's bar */
    //     int black_bar = 0;
    int bear[ ]; // bear[0] is nothing, bear[1] is # of blots beared off by white, bear[2]=black_bear;
    //     int white_bear = 0; /* how many blots white has "beared off" the board */
    //     int black_bear = 0;

    /*private*/ Dice myDice = new Dice( ); /* this should be private but then re-edit */

    private int selectedPointLoc = NO_MOVE_SELECTED; // Original position of blot selected for moving
    /* Whether the current player has selected a point (which would
    mean that the possible move positions are showing. */
    /*private*/ boolean aPointIsSelected = false;
    // The move possible with each dice
    // Positions: 1 - 24 = points, 1 being on the beginning of the black quarter
    // formerly 0 was black bear off and 25 was white bear off
    private int potDest1 = NO_MOVE_SELECTED;
    private int potDest2 = NO_MOVE_SELECTED; 
    // destination of moving from selectedPointLoc using dice1 & 2 respectively
    /* usedDice is now incorporated into myDice ! */

    private ArrayList<HitBlot> hitsSoFar = new ArrayList<HitBlot>( ); /* For keeping track of all 
        blots that were hit on this board so far, supposedly during just the current move.*/
        
    // point colors (PlayerColor objects are only Game.whiteP or Game.blackp or null)
    // Board isn't currently keeping track of current player. Game does.

    public static final Player neutral = null;
    public static final Player white = Game.whiteP;
    public static final Player black = Game.blackP;

    public static final int ILLEGAL_MOVE = -97;
    public static final int NO_MOVE_SELECTED = -105;
    // the following are part of Player now, as white.getBarLoc( ), white.getBearOffLoc( ), 
    // and white.getBeyondBearOffLoc( )
    //   public static final int bar = 0; /* name of a place one can move to */
    //  public static final int bearoff = -1; /* a move might end on here */

    public final static int HOW_MANY_POINTS_IN_QUADRANT = 6;
    public final static int HOW_MANY_QUADRANTS = 4;
    public static final int howManyPoints = /* 24 */HOW_MANY_POINTS_IN_QUADRANT * HOW_MANY_QUADRANTS; 
    /*points="spikes"; stored in array 0..24 using 1..24*/
    public static final int howManyPointsInFinalQuadrant = 6;
    public static final int howManyBlots = 15; // Players can have custom # of blots, for handicapping
    // these next two should be an array like bar and bear..
    public static final int howManyWhiteBlots = howManyBlots; /* some might be on bar or did bear off*/
    public static final int howManyBlackBlots = howManyBlots; /* some might be on bar or did bear off*/
    public static final int howManyDice = 2; 
    static final int maxMovesCount = 4; /* with 3 dice could we do doubles (6 maxMoves) or triples (9!) of rolls? */
    public static final int diceHighNum = 6; /* we could use funky dice! */

    /**
     * Build a new game board.
     * I was considering having another constructor that takes numeric parameter
     * of which test board you want, but realized it is not necessary: it is 
     * so easy to tell a newly constructed board to merely b.makeBoardWithoutBlots( ) 
     * or b.makeStartingBoard( ) or b.makeAlmostDoneGame( ) or b.make3BlotGame( )!
     * 
     * Can constructors throw exceptions? Shouldn't this be throwing?
     */
    public Board(Game newMyGame) {
        if (newMyGame == null) {
            throw new NullPointerException("Can't give null Game to Board Constructor."
                + "Boards HAVE to know their game.");
        } else {
            myGame = newMyGame;
        }
        howManyOnPoint = new int[howManyPoints + 1]; 
        // 0..24?? why 25 points? Think 0th point is the bar?
        // Ah-hah: maybe it is so that we get boxes 0..24 and can say [24] instead of minusing 1
        whichPlayerOnPoint = new Player[howManyPoints + 1];
        bar = new int[Game.howManyColors + 1];
        bear = new int[Game.howManyColors + 1];

        try {
            /* regular games start with "makeStartingBoard( ); */ 
            /*make3BlotGame( );   /* which first calls makeBoardWithoutBlots( ) */
            /* makeEasyHitStartingBoard( ); */
            makeStartingBoard( );
        } catch( BadBoardException e ) {
            System.out.print("ERROR building Board: " + e );
            //throw new BadBoardException("ERROR building Board: " + e );
        }
    } // board constructor

    /**
     * Copy constructor so that I can make duplicate boards
     * Usage  Board b2 = new Board( myBoard );
     * ?? Should I also implement clone( ) ??? Nah, online says bad implementation, subtle bugs.
     * "Game" is here so other Games can copy boards from this board's game
     */
    public Board(Game newMyGame, Board otherBoard ) {
        if (newMyGame == null) {
            throw new NullPointerException("Can't give null Game to Board Constructor." 
                + " Boards HAVE to know their game.");
        } else if (otherBoard == null) {
            throw new NullPointerException("Can't give null Board to Board Copy Constructor.");
        } else {
            myGame = newMyGame;
        }
        howManyOnPoint = new int[howManyPoints + 1]; // 25 slots, numbering from 0..24
        whichPlayerOnPoint = new Player[howManyPoints + 1];
        for (int i = 0; i < howManyPoints + 1; i++) {
            howManyOnPoint[i] = otherBoard.howManyOnPoint[i];
            whichPlayerOnPoint[i] = otherBoard.whichPlayerOnPoint[i];
        }

        bar = new int[ Game.howManyColors + 1 ];
        bear = new int[ Game.howManyColors + 1 ];
        // making 1 extra so that I can say bar[1] or bar[2] with 1 is white and 2 is black
        for (int i = 0; i < Game.howManyColors + 1; i++) {
            bar[i] = otherBoard.bar[i];
            bear[i] = otherBoard.bear[i];
        }

        //         white_bar = otherBoard.white_bar;    
        //         black_bar = otherBoard.black_bar;
        //         white_bear = otherBoard.white_bear;
        //         black_bear = otherBoard.black_bear;

        myDice = new Dice(otherBoard.myDice); 
        /* deep copy because dice are keeping track of dice usage */
        /* might be null if dice haven't rolled yet */
        /* note: don't have to use get when talking to myself */
        
        ArrayList<HitBlot> hitsSoFar = new ArrayList<HitBlot>(otherBoard.hitsSoFar );

        aPointIsSelected = otherBoard.aPointIsSelected;
        potDest1 = otherBoard.potDest1;
        potDest2 = otherBoard.potDest2; 
        /* destination of moving from selectedPointLoc using dice1 & 2 respectively */

        selectedPointLoc = otherBoard.selectedPointLoc; 
        // Original position of blot selected for moving
    } /* copy constructor */

    /**
     * make sure the bars and bears and points are all free of blots.
     * Should this myDice.reset( ); ?? probably
     */
    void makeBoardWithoutBlots( ) {
        for (int i=0; i<=howManyPoints; i++) { 
            /* why start at 0? For prettier pointer numbering 1..24, ignore the 0's? */
            /* could use setNumOfBlotsOnPoint(i,0,neutral); ?? */
            howManyOnPoint[i] = 0;
            whichPlayerOnPoint[i] = null /* means neutral*/;
        }
        bar[Player.WHITE] = 0;
        bar[Player.BLACK] = 0;
        bear[Player.WHITE] = 0;
        bear[Player.BLACK] = 0;
        myDice.reset( );
    } /* makeBoardWithoutBlots */

    /**
     * The regular normal starting position (15 blots of each color)
     */
    public void makeStartingBoard( )  throws BadBoardException {
        makeBoardWithoutBlots( );
        setNumOfBlotsOnPoint(1, /* howMany */ 2, white);
        setNumOfBlotsOnPoint(6, /* howMany */ 5, black);
        setNumOfBlotsOnPoint(8, /* howMany */ 3, black);
        setNumOfBlotsOnPoint(12, /* howMany */ 5, white);
        setNumOfBlotsOnPoint(13, /* howMany */ 5, black);
        setNumOfBlotsOnPoint(17, /* howMany */ 3, white);
        setNumOfBlotsOnPoint(19, /* howMany */ 5, white);
        setNumOfBlotsOnPoint(24, /* howMany */ 2, black);
        checkForBadNumberOfBlots( white );
        checkForBadNumberOfBlots( black );
    } //  makeStartingBoard( )

    /**
     * A few moves have happened, for math demo
     */
    public void makePointBuild1Board( )  throws BadBoardException {
        makeBoardWithoutBlots( );
        setNumOfBlotsOnPoint(1, /* howMany */ 2, white);
        setNumOfBlotsOnPoint(4, 1, white );
        setNumOfBlotsOnPoint(5, 1, black );
        setNumOfBlotsOnPoint(6, /* howMany */ 5, black);
        setNumOfBlotsOnPoint(8, /* howMany */ 2, black);
        setNumOfBlotsOnPoint(12, /* howMany */ 4, white);
        setNumOfBlotsOnPoint(13, /* howMany */ 5, black);
        setNumOfBlotsOnPoint(17, /* howMany */ 3, white);
        setNumOfBlotsOnPoint(19, /* howMany */ 4, white);
        setNumOfBlotsOnPoint(20, 1, white);
        setNumOfBlotsOnPoint(24, /* howMany */ 1, black);
        bar[Player.BLACK] = 1;
        checkForBadNumberOfBlots( white );
        checkForBadNumberOfBlots( black );
    } //  makeStartingBoard( )
    
    
    
    /**
     * AI isn't making the move we expect using pointBuilder
     */
    public void makePointBuildBoard( )  throws BadBoardException {
        makeBoardWithoutBlots( );
        setNumOfBlotsOnPoint(3, /* howMany */ 1, black );
        setNumOfBlotsOnPoint(4, 2, black );
        setNumOfBlotsOnPoint(5, 2, black );
        setNumOfBlotsOnPoint(6, 2, black);
        setNumOfBlotsOnPoint(8, 2, black);
        setNumOfBlotsOnPoint(9, 2, black);
        setNumOfBlotsOnPoint(12, 2, white);
        setNumOfBlotsOnPoint(16, 2, white);
        setNumOfBlotsOnPoint(17, 3, white);
        setNumOfBlotsOnPoint(18, 2, black);
        setNumOfBlotsOnPoint(19, 3, white);
        setNumOfBlotsOnPoint(20, 2, black);
        setNumOfBlotsOnPoint(21, 3, white);
        setNumOfBlotsOnPoint(22, 2, white);
        //bar[Player.WHITE] = 0;
        checkForBadNumberOfBlots( white );
        checkForBadNumberOfBlots( black );
    } //  makeStartingBoard( )

    
    
    /**
     * A few moves have happened, for math demo
     */
    public void makePointBuild2Board( )  throws BadBoardException {
        makeBoardWithoutBlots( );
        setNumOfBlotsOnPoint(4, 1, white );
        setNumOfBlotsOnPoint(5, 1, white );
        setNumOfBlotsOnPoint(6, /* howMany */ 5, black);
        setNumOfBlotsOnPoint(8, /* howMany */ 3, black);
        setNumOfBlotsOnPoint(9, /* howMany */ 1, black);
        setNumOfBlotsOnPoint(12, /* howMany */ 5, white);
        setNumOfBlotsOnPoint(13, /* howMany */ 4, black);
        setNumOfBlotsOnPoint(17, /* howMany */ 3, white);
        setNumOfBlotsOnPoint(19, /* howMany */ 5, white);
        setNumOfBlotsOnPoint(24, /* howMany */ 2, black);
        //bar[Player.WHITE] = 0;
        checkForBadNumberOfBlots( white );
        checkForBadNumberOfBlots( black );
    } //  makeStartingBoard( )

    /**
     * Symmetrical starting position in which it is easy for players to hit each other
     * white singles on 5,7,9,13
     * black singles on (absolute) 20,18,16,12 [from black POV == 5,7,9,13]
     * (Handy for testing!)
     */
    public void makeEasyHitStartingBoard( )  throws BadBoardException {
        makeBoardWithoutBlots( );
        setNumOfBlotsOnPoint(5, /* howMany */ 1, white); 
        setNumOfBlotsOnPoint(7, /* howMany */ 1, white);
        setNumOfBlotsOnPoint(9, /* howMany */ 1, white);
        setNumOfBlotsOnPoint(13, /* howMany */ 1, white);

        setNumOfBlotsOnPoint(20, /* howMany */ 1, black);
        setNumOfBlotsOnPoint(18, /* howMany */ 1, black);
        setNumOfBlotsOnPoint(16, /* howMany */ 1, black);
        setNumOfBlotsOnPoint(12, /* howMany */ 1, black);

        bear[Player.WHITE] = 11;
        bear[Player.BLACK] = 11;

        checkForBadNumberOfBlots( white );
        checkForBadNumberOfBlots( black );
    } //  makeEasyHitStartingBoard( )

    /** 
     * black has singles in quadrant 4, on points 1,4,6 (bearing off to 0)
     */
    public void makeAlmostDoneGame( ) throws BadBoardException {
        makeBoardWithoutBlots( );
        setNumOfBlotsOnPoint(1, /* howMany */ 1, black); 
        setNumOfBlotsOnPoint(4, /* howMany */ 1, black);
        setNumOfBlotsOnPoint(6, /* howMany */ 1, black);
        bear[Player.BLACK] = 12;

        setNumOfBlotsOnPoint(18, /* howMany */ 3, white);
        setNumOfBlotsOnPoint(19, /* howMany */ 3, white);
        setNumOfBlotsOnPoint(24, /* howMany */ 2, white);
        bear[Player.WHITE] = 7;

        checkForBadNumberOfBlots( white );
        checkForBadNumberOfBlots( black );
    } // makeAlmostDoneGame

    /** 
     * black has singles in quadrant 4, on points 1,3,4 (bearing off to 0)
     * white has 3 on 18 & 19, and has 2 on 24
     */
    public void makeAlmostDonerGame( ) throws BadBoardException {
        makeBoardWithoutBlots( );
        setNumOfBlotsOnPoint(1, /* howMany */ 1, black);
        setNumOfBlotsOnPoint(3, /* howMany */ 1, black);
        setNumOfBlotsOnPoint(4, /* howMany */ 1, black);
        bear[Player.BLACK] = 12;

        setNumOfBlotsOnPoint(18, /* howMany */ 3, white);
        setNumOfBlotsOnPoint(19, /* howMany */ 3, white);
        setNumOfBlotsOnPoint(24, /* howMany */ 2, white);
        bear[Player.WHITE] = 7;
        checkForBadNumberOfBlots( white );
        checkForBadNumberOfBlots( black );
    } // makeAlmostDoneGame

    /** 
     * black on 20 & 12, white on 4 (has long way to go)
     * note: white ends past 24, black ends below 1.
     * black has beared off 13 blots, white has beared off 14 blots already...
     */
    public void make3BlotGame( ) throws BadBoardException {
        makeBoardWithoutBlots( );
        setNumOfBlotsOnPoint(20, /* howMany */ 1, black);
        setNumOfBlotsOnPoint(12, 1, black);
        bear[Player.BLACK] = 13;

        setNumOfBlotsOnPoint(4, /* howMany */ 1, white);
        bear[Player.WHITE] = 14;
        System.out.println("created board with 2 black blots (on points 20,12)"
            + " and 1 white blot (on point 4)");
        checkForBadNumberOfBlots( white );
        checkForBadNumberOfBlots( black );
    } // make3BlotGame

    /** 
     * black on 14 & 12, white on 4 (has long way to go)
     * note: white ends past 24, black ends below 1.
     * black has beared off 13 blots, white has beared off 14 blots already...
     */
    public void make3BlotGame2( ) throws BadBoardException {
        makeBoardWithoutBlots( );
        setNumOfBlotsOnPoint(14, /* howMany */ 1, black);
        setNumOfBlotsOnPoint(12, 1, black);
        bear[Player.BLACK] = 13;

        setNumOfBlotsOnPoint(4, /* howMany */ 1, white);
        bear[Player.WHITE] = 14;
        System.out.println("created board with 2 black blots (on points 20,12)"
            + " and 1 white blot (on point 4)");
        checkForBadNumberOfBlots( white );
        checkForBadNumberOfBlots( black );
    } // make3BlotGame

    /** 
     * black on 20 & 12, whites on 16
     * note: white ends past 24, black ends below 1.
     * black & white have beared off 13 blots already...
     */
    public void make4BlotGame( ) throws BadBoardException {
        makeBoardWithoutBlots( );
        setNumOfBlotsOnPoint(20, /* howMany */ 1, black);
        setNumOfBlotsOnPoint(12, 1, black);
        bear[Player.BLACK] = 13;

        setNumOfBlotsOnPoint(16, /* howMany */ 2, white);
        bear[Player.WHITE] = 13;
        System.out.println("created board with 2 black blots (on points 20,12)"
            + " and 2 white blots (on point 16)");
        checkForBadNumberOfBlots( white );
        checkForBadNumberOfBlots( black );
    } // make4BlotGame

    /** 
     * black on bar, and 4 twice; white on bar twice and on 22 twice
     * note: white ends past 24, black ends below 1.
     */
    public void makeBusyBarGame( ) throws BadBoardException {
        makeBoardWithoutBlots( );
        setNumOfBlotsOnPoint(4, /* howMany */ 2, black);
        bar[Player.BLACK] = 1;
        bear[Player.BLACK] = 12;

        setNumOfBlotsOnPoint(22, /* howMany */ 2, white);
        bear[Player.WHITE] = 11;
        bar[Player.WHITE] = 2;

        checkForBadNumberOfBlots( white );
        checkForBadNumberOfBlots( black );
    } // make4BlotGame

    /**
     * returns a copy of the dice 
     */
    public Dice getMyDice( ) {
        Dice newDice = new Dice( myDice );
        return newDice;
    }
    
    /**
     * returns a copy, won't hurt the current
     */
    public ArrayList<HitBlot> getHitsSoFar( ) {
        return new ArrayList<HitBlot>( hitsSoFar );
    }
    
    /**
     * erase the list of blots sent to the bar
     */
    public void hitsSoFarBeEmpty( ) {
        hitsSoFar.clear( );
    }
    

    /**
     * Is called by Game.doPartialMove( ), so can't be private.
     * Moves blot from one position to another, modifying the board object.
     * Doesn't check legality of move, doesn't check whether player is on bar,
     * so shouldn't be called willy nilly!
     * 
     * Can move Blots in from bar, so is a partner with "moveToBar( )"
     * There is selectABar( )... does it overlap if function?
     * Or maybe there should be a "moveFromBar( )" that includes the middle of this
     * and is called by this if necessary. Could moveFromBar be called by others?
     * 
     * ?? Hey, this is doing action equivalent to bearOff without using Board.bearOff( ),
     * and not doing it as well as bearOff does.
     * Board.doPartialMove( ) calls moveBlot( )... either doPartialMove should call
     * bearOff( ) when necessary or this moveBlot( ) should call bearOff...
     * 
     * Game.receiveMove( ) [when networked] might be calling this.
     * Shouldn't they call bearOff( ) if necessary and legit, instead??
     * bearOff( ) doesn't call moveBlot: bearOff calls takeOneBlotOffPoint(oldPointNum);
     * which uses setNumOfBlotsOnPoint( ) internally and is equivalent to
     * setNumOfBlotsOnPoint(oldPointNum, getHowManyBlotsOnPoint(oldPointNum,playerColor) - 1,playerColor);
     */
    public void moveBlot(Player playerColor, int oldPointNum, int newPointNum, int rollValue) {
        if ( ! legitStartLoc( oldPointNum, playerColor )) { /* checks also Player.legitColor */
            throw new IllegalArgumentException("Can't start moving from point '" + oldPointNum + "'");
        }
        if ( ! legitEndLoc( newPointNum, playerColor /*,exact?:false*/ )) {
            throw new IllegalArgumentException("Can't move to point '" + newPointNum + "'");
        }
        if (oldPointNum == newPointNum) {
            return; // dud, not going to do anything
        }
        if (! canLandOn( newPointNum, playerColor )) {
            return;
        }
        // If the move is coming from a bar, remove it from the bar
        // and add it to the point

        /* doesn't selectABar( ) do this?? */
        if (oldPointNum == playerColor.getBarLoc( )) {
            selectABar( playerColor ); // doesn't actually move any blots, might not be necessary??
            comeInFromBar( playerColor, newPointNum, rollValue ); //marks dice as used
        } else if ((newPointNum == playerColor.getBearOffLoc( )) 
        || (newPointNum == playerColor.getBeyondBearOffLoc( )) ) {
            bearOff( playerColor, oldPointNum, rollValue ); //marks dice used!
            // knowing rollValue makes it easy to mark dice as used
        } else {
            int howManyBlotsOnDest =  getHowManyBlotsOnPoint(newPointNum,playerColor); /* cache */

            // Decrease the number of blots on the old point
            int howManyOnOldPoint = getHowManyBlotsOnPoint(oldPointNum, playerColor);
            if (howManyOnOldPoint > 0) {
                // next line is equiv to  "takeOneBlotOffPoint(oldPointNum)" 
                setNumOfBlotsOnPoint(oldPointNum, howManyOnOldPoint - 1, playerColor);
                if (howManyOnOldPoint==0) {
                    setNumOfBlotsOnPoint(oldPointNum, 0, neutral);
                }
            } else {
                throw new IllegalArgumentException("player '" + playerColor.toString( ) 
                    + "' can't move from point '" 
                    + oldPointNum + "' since no blots are there!");
            }
            // Increase the blots on the new point
            setNumOfBlotsOnPoint(newPointNum, howManyBlotsOnDest + 1, playerColor);
            myDice.setUsedValue(rollValue); //marks dice as used
        }
        setSelectedPointLoc(NO_MOVE_SELECTED,playerColor); // sets aPointIsSelected to false
    } /* moveBlot( ) */

    /**
     * Yes, this should know which incoming point to land on.
     * Why is there a startPointNum, isn't necessarily playerColor.getBarLoc( )
     */
    public void comeInFromBar( Player playerColor, int destPointNum, int rollValue ) {
        if ( ! legitEndLoc( destPointNum, playerColor /*,exact?:false*/ )) {
            throw new IllegalArgumentException("Can't move to point '" + destPointNum + "'");
        }
        int startPointNum = playerColor.getBarLoc( );
        if (startPointNum == destPointNum) {
            return; // dud, not going to do anything
        }
        if (! canLandOn( destPointNum, playerColor )) {
            return;
        }
        if (! onBar(playerColor) ) {
            throw new IllegalArgumentException(playerColor + " can't come in from bar, isn't on bar!");
        }

        // following could be simplified, refactored?? 
        if (startPointNum == white.getBarLoc( ) ) { // selectABar( playerColor ); 
            // selectABar( ) isn't enough, doesn't move anything
            bar[Player.WHITE]--;
        } else /*else??*/ if (startPointNum == black.getBarLoc( )) { //selectABar(playerColor);
            bar[Player.BLACK]--;
        }

        int howManyBlotsOnDest = getHowManyBlotsOnPoint(destPointNum,playerColor); /* cache */
        setNumOfBlotsOnPoint(destPointNum, howManyBlotsOnDest + 1, playerColor);
        myDice.setUsedValue(rollValue);
        setSelectedPointLoc(NO_MOVE_SELECTED,playerColor); // ^ sets aPointIsSelected to false
    } // comeInFromBar( )

    /**
     * Bear off a blot from oldPointNum.
     * Sure hope that the caller has checked legitimacy of bearing off!
     * Is called by Game.actionPerformed in response to button press, does
     * no checking!
     * This is the only caller, but Game.receiveMove( ) should maybe call this.
     * Board.moveBlot( ) is doing bearOff without calling this: maybe it ought to call this!
     * @param playerColor - instance of Player is black, white, or null when game unstarted.
     * @param oldPointNum - point we're bearing off from
     * @param rollValue - the value on the face of the dice (1..6), to help in marking dice used
     */
    public void bearOff( Player playerColor, int oldPointNum, int rollValue ) {
        if ( ! playerColor.legitColor( ) ) {
            throw new IllegalArgumentException("bad playerColor '" + playerColor.toString( ) + "'");
        }
        // might could use if (playerColor.inFinalQuadrant( oldPointNum )) { instead, should be same...
        if ( 4 != quadrantForPoint(oldPointNum, playerColor )) {
            throw new IllegalArgumentException("can't bear off from point '" + oldPointNum 
                + "' which isn't in final quadrant for player " + playerColor.toString( ));
        }
        //System.out.println(playerColor + " is starting to bear off from point " + oldPointNum);
        // Remove a blot from the old point
        takeOneBlotOffPoint(oldPointNum);
        //equiv: setNumOfBlotsOnPoint(oldPointNum, getHowManyBlotsOnPoint(oldPointNum,playerColor) - 1,playerColor);
        bear[playerColor.getColor( )]++;

        if (myGame.status.networked) {
            if (this==Game.mainBoard) {
                myGame.comm.sendMove(oldPointNum, playerColor.getBearOffLoc( ));
            }
        }

        if (this==Game.mainBoard) {
            // ??? trying to distinguish hypothetical boards from myGame's real public board
            myGame.fButton[myGame.btn_BearOff].setEnabled(false); 
        }

        boolean won = false; // gonna check whether currentPlayer just won
        if (!myGame.status.networked) {
            won = myGame.checkWin(this, playerColor);
        } else if (myGame.status.observer==false) {
            // what is observer?? Does it mean client, not-server? White or black respectively?
            won = myGame.checkWin(this, white);
        }
        if (won) {
            if (this==Game.mainBoard) {
                myGame.endPartialMove(playerColor);// Disable buttons
                myGame.resetGame(myGame.mainBoard);
            }
            return; // Do nothing if there's a winner
        }

        // Turn off focus on this point
        setSelectedPointLoc(NO_MOVE_SELECTED,playerColor); //sets aPointIsSelected = false;
        if (this == Game.mainBoard) {
            myGame.endPartialMove(playerColor); // adjusts some gui buttons
        }

        // Remove the dice we used
        // ?? might have been moving to beyondBearLoc?? 
        // or did moveBlot( ), selectAPoint( ) or endPointMovingFrom( )
        // ?? actually adjust the target to BearOffLoc after verifying needsInexact( )?

        // doPartialMove calls moveBlot who calls this bearOff. Only this can mark dice as used
        myDice.setUsedValue( rollValue );
        // if we didn't know the rollValue, we could figure it out:
        // int rollValue = whichDieValueUsedForBearOff( playerColor,oldPointNum ); //figures out which die we used and
        // marks it as used. NOTE: markADie... sets the turn done if all dice are used up

        if (this==Game.mainBoard) {
            if (myDice.allDiceAreUsed( ) ) {
                myGame.endTurn(this,playerColor, /*msg*/"");
            } else if (( myGame.getCurrentPlayer( )== playerColor) && (! canMove(playerColor) )) {
                // finally, heavier checking whether the turn is over...
                // note: myDice.setUsedValue( ), above, might have already ended the turn,
                // so first we check if we're still on the same player!
                myGame.forfeitTurn(this, playerColor); // calls endTurn( )
            }
            myGame.repaint();
        }
    } // bearOff

    /**
     * Now that bearOff( ) is told which diceRollValue is being
     * used, bearOff doesn't need to call this.
     * This has a bug 
     * Called by bearOff( ), this marks one of the dice as used,
     * and has to do some work to determine which one was used.
     */
    public int whichDieValueUsedForBearOff( Player playerColor, int oldPointNum ) {
        // hmmm, which die have we used??
        if (myDice.allDiceAreUsed( )) {
            throw new IllegalArgumentException("Whoa, how can " + playerColor 
                + " bear off from " + oldPointNum + " when all dice are used up??");
        }
        if (myDice.isDoubles( )) { 
            // doesn't matter which, they're all same,
            // so tell dice "i used value of die1". Easy!
            return myDice.getDie( 1 ); 
            //myDice.numOfPartialMovesAvailDecrease();  
        } else if ((myDice.getUsedDie(1)) || (myDice.getUsedDie(2))) {
            // if a previous move has already occurred, we are done
            if (myDice.getUsedDie(1)) {
                /* what if was already used?? Can't be: we checked above
                 * that not allDiceAreUsed and not doubles */
                return myDice.getDie(2); 
            } else {
                return myDice.getDie(1);
            }
            // below we'll...myGame.endTurn(this);
        } else if (((potDest1==playerColor.getBearOffLoc( )) 
            || (potDest1==playerColor.getBeyondBearOffLoc( )))
        && ((potDest2==playerColor.getBearOffLoc( ))  
            || (potDest2==playerColor.getBeyondBearOffLoc( )))) {
            // if you can bear off with both, use smaller dice (?? Why?)
            if ((myDice.getDie(1) <= myDice.getDie(2)) && (!myDice.getUsedDie(1))) {
                return myDice.getDie(1 );
            } else {
                return myDice.getDie(2 );
            }
            // I think these next 2 elses might be buggy:

        } else if ((potDest1==playerColor.getBearOffLoc( )) && (!myDice.getUsedDie(1))) {
            return myDice.getDie( 1 );
        } else if ((potDest2==playerColor.getBearOffLoc( )) && (!myDice.getUsedDie(2))) {
            return myDice.getDie( 2 );
        } else {
            throw new IllegalArgumentException("Whoa, I can't figure out which die would be used " 
                + "for " + playerColor  + " to bear off from " + oldPointNum + "??");
        }
    } // whichDieValueUsedForBearOff( )

    /**
     * Deal with player being on bar by marking possible escapes and forfeitTurn if there are none.
     * This is automatically called by Game.doRoll( )   if   Board.onBar(Game.current_player)
     * and doRoll otherwise checks whether a player canMove( ) and 
     * forfeitTurns for them if they can't move!
     * startTurn ought to call this if necessary
     * ?? Does AI hear this? Respond?
     */
    public void selectABar(Player playerColor) {
        if ( ! playerColor.legitColor( )) {
            throw new IllegalArgumentException("bad playerColor '" + playerColor + "'");
        }
        if ( ! onBar(playerColor)) {
            throw new IllegalArgumentException("Why selectABar for " + playerColor 
                + ", who isn't on bar!?");
        }
        int escape1;
        int escape2;

        if (playerColor==white) {
            escape1 = myDice.getDie(1);
            escape2 = myDice.getDie(2);
        } else {
            escape1 = (howManyPoints + 1) - myDice.getDie(1);
            escape2 = (howManyPoints + 1) - myDice.getDie(2);
        }

        // following should loop through ALL dice if we have more than 2
        // Can they escape?
        if ( (! myDice.getUsedDie( 1 )) && canLandOn(escape1,playerColor) ) {
            setSelectedPointLoc(playerColor.getBarLoc( ),playerColor); // sets aPointIsSelected = true;
            if (this == Game.mainBoard) {
                myGame.fButton[myGame.btn_AtPotentialMove1].drawOnPoint(escape1); // potential move 1
                myGame.fButton[myGame.btn_AtPotentialMove1].setVisible(true); 
                // show this as possible move
            }
            potDest1 = escape1;
        }
        if ( (!myDice.getUsedDie(2)) && canLandOn(escape2,playerColor) ) {
            setSelectedPointLoc(playerColor.getBarLoc( ),playerColor); // sets aPointIsSelected = true;
            if (this==Game.mainBoard) {
                myGame.fButton[myGame.btn_AtPotentialMove2].drawOnPoint(escape2); // potential move 2
                myGame.fButton[myGame.btn_AtPotentialMove2].setVisible(true);
            }
            potDest2 = escape2;
        }

        // If they can't escape then they forfeitTurn
        if (! myDice.allDiceAreUsed( )) {
            if (this==Game.mainBoard) {
                if ( (!canLandOn(escape1, playerColor)) && (!canLandOn(escape2, playerColor)) ) {
                    myGame.forfeitTurn(this, playerColor); // calls endTurn
                }
            }
        } else if (myDice.getUsedDie(1)) {
            if (this==Game.mainBoard) {
                if (!canLandOn(escape2, playerColor)) {
                    myGame.forfeitTurn(this, playerColor); // calls endTurn
                }
            }
        } else if (myDice.getUsedDie(2)) {
            if (this==Game.mainBoard) {
                if (!canLandOn(escape1, playerColor)) {
                    myGame.forfeitTurn(this, playerColor); // calls endTurn
                }
            }
        }
    } // selectABar

    /**
     * When a point is selected by user, its number is stored in selectedPointLoc
     * and aPointIsSelected becomes true.
     * While a blot is moving, this remembers its original (starting) position.
     * I'm trying to not use it much.
     */
    public int getSelectedPointLoc( ) {
        return selectedPointLoc;
    }

    /**
     * When a point is selected by user, its number is stored in selectedPointLoc
     * and aPointIsSelected becomes true.
     */
    public boolean getAPointIsSelected( ) {
        return aPointIsSelected;
    }

    /**
     * When a point is selected by user, its number is stored in selectedPointLoc
     * and aPointIsSelected becomes true. To turn off this selection, use
     * setSelectedPointLoc(NO_MOVE_SELECTED); // which also turns off "aPointIsSelected"
     * While a blot is moving, this remembers its original (starting) position??
     * Can select bar but not BearOffLoc.
     * 
     * Either this has to know which die is involved, OR anybody who calls this
     * must also call "setRelevantButtons"
     */
    public void setSelectedPointLoc(int newSelectedPointLoc, Player playerColor ) {
        if (( newSelectedPointLoc == NO_MOVE_SELECTED ) 
        || legitStartLoc( newSelectedPointLoc, myGame.getCurrentPlayer() )) {
            // okay! Written this way for easier (??) reading
        } else {
            throw new IllegalArgumentException("Can't start moving from point '" 
                +newSelectedPointLoc + "'");
        }
        // do stuff (including hide the "move here" buttons) only if this is really a change
        if (selectedPointLoc != newSelectedPointLoc) {
            selectedPointLoc = newSelectedPointLoc;
            int allPotDestsNewStatus = NO_MOVE_SELECTED;
            if ( newSelectedPointLoc == NO_MOVE_SELECTED ) {
                aPointIsSelected = false;
                allPotDestsNewStatus = NO_MOVE_SELECTED; // no move, so no potDests
            } else {
                aPointIsSelected = true; 
                allPotDestsNewStatus  = ILLEGAL_MOVE;
                // ^ a point is newly clicked: potDests are wrong until set right
            }
            for (int i = 1; i <= howManyDice; ++i) { // blank all potentials
                setPotDest(playerColor, i, allPotDestsNewStatus);
            }
            // 0 means hide both "move here" buttons
            setRelevantButtons( newSelectedPointLoc, playerColor, /* isMove1or2: */0);
        }
    } // setSelectedPointLoc

    /* set/getUsedDie was called "getUsedMove( )",  is now in myDice */
    /* set/getDoubletMovesCountdown( ) is now in myDice */

    /**
     * Of the 2 potential destinations for the selected point (selectedPointLoc), 
     * tell us the Point that is reached by dest 'whichDest'
     * 
     * potDest should be an array, or ArrayList
     * !
     * Maybe this should be part of myDice??
     */
    public int getPotDest(int whichDest) {
        if ((whichDest < 1) || (whichDest > howManyDice)) {
            throw new IllegalArgumentException("It's no good trying to talk to potential dest# '" 
                + whichDest + "', can only use 1.." + howManyDice);
        }
        if (whichDest == 1) {
            return potDest1;
        } else if (whichDest == 2) {
            return potDest2;
            //         } else if ((whichDest > 2) && (! myDice.isDoubles( ))) {
            //             throw new IllegalArgumentException("It's no good trying to talk to " + 
            //                "potential move '" + whichDest + "', can only use 1..4");
        } else {
            throw new IllegalArgumentException("getPotDest( ) doesn't know how to return potential"
                + " destination '" + whichDest + "', can only use 1..2");
        }
    } /* getPotDest( ) */

    /**
     * When a point with blot has been "selectAPoint"ed, the board remembers some
     * potDest (potential destinations) which are the legal moves from the selected place.
     * This might need to remember NO_MOVE_SELECTED, also.
     * 
     * potDest should be an array, or ArrayList!
     * This is called by 
     */
    public void setPotDest(Player playerColor, int whichDest, int newDestPointNum) {
        if ((whichDest < 0) || (whichDest > howManyDice)) {
            throw new IllegalArgumentException("It's no good trying to talk to potential dest# '" 
                + whichDest + "', can only use 1.." + howManyDice);
        }
        if ((newDestPointNum != NO_MOVE_SELECTED) && (newDestPointNum !=ILLEGAL_MOVE) 
        && (! legitEndLoc( newDestPointNum, playerColor /*,not exact*/ ) )) {
            throw new IllegalArgumentException(newDestPointNum 
                + " is not a legit potential destination");
        }
        if (whichDest == 1) {
            potDest1 = newDestPointNum;
        } else if (whichDest == 2) {
            potDest2 = newDestPointNum;
        } else {
            throw new IllegalArgumentException("setPotDest( ) doesn't know how to set potential"
                + " destination '" + whichDest + "', can only use 1..2");
        }
    }

    /**
     * When doubles, we can't ask dice which die has the value because they all do.
     * Still want to distinguish potMove1 from potMove2 for displaying buttons onscreen.
     */
    public int whichPotDest(int rollVal) {
        if (! myDice.isDoubles()) {
            return myDice.whichDieHasValue(rollVal);
        } else {
            boolean foundOne = false;
            for (int i = 1; i <= howManyDice; ++i) { // yes, 1 & 2, not zero
                // not sure about this illegal_move alternative
                if ((getPotDest(i) == NO_MOVE_SELECTED) || (getPotDest(i) == ILLEGAL_MOVE)) {
                    return i;
                }
            }
            throw new IllegalArgumentException("not enough potential move markers left to show" 
                + " move for rollVal:" + rollVal);
        }
    }

    /**
     * how many blots of this color are on the bar, waiting to come back into the game.
     */
    public int getBar( Player playerColor ) {
        if ( ! playerColor.legitColor( ) ) {
            throw new IllegalArgumentException("bad color '" + playerColor + "'");
        }
        return bar[ playerColor.getColor( ) ];
    } /* getBar( ) */

    /**
     * Tells how many blots (pieces) of a particular color are here 
     * (including on the bar, the bear, or the board).
     * There are supposed to always be 15 blots of each color in traditional backgammon. 
     * Used by "checkForBadNumberOfBlots( ) to check possible corruption of board.
     */
    public int howManyBlots(Player playerColor) {
        if ( ! playerColor.legitColor( ) ) {
            throw new IllegalArgumentException("bad color '" + playerColor + "'");
        }
        int blotCount= getBlotCountOnBoard( playerColor );
        blotCount += bar[playerColor.getColor( )] + bear[playerColor.getColor( )];
        return blotCount;
    } /* howManyBlots( ) */

    /**
     * Checks for legal number of white and black blots, compared to globals.
     */
    public void checkForBadNumberOfBlots(Player playerColor) throws BadBoardException {
        if ( ! playerColor.legitColor( ) ) {
            throw new IllegalArgumentException("bad color '" + playerColor + "'");
        }
        int howManyBlotsNow = howManyBlots(playerColor);
        if (howManyBlotsNow != playerColor.getHowManyBlotsRequired( )) {
            throw new BadBoardException("There are " + howManyBlotsNow 
                + " " + playerColor
                + " blots but legally should have " + playerColor.getHowManyBlotsRequired( ));
        }
    } /* checkForBadNumberOfBlots */

    /**
     * This will mostly be used for partial moves?
     * The biggest possible partial move is 6 (diceHighNum).
     * The smallest possible partial move is 1 (or 0? if forfeitTurn? not really a "move")
     * The biggest possible (full, non partial) move in standard backgammon is 24: doubles of 6s.
     */
    public static boolean legitStepsNum( int steps ) {
        return ( (1 <= steps) && 
            (steps <= /* diceHighNum */ (Dice.maxMovesCount * howManyDice * diceHighNum)) );
    } // legitStepsNum( )

    /**
     * This only accepts pointNumbers [1..(howManyPoints==24)], 
     * NOT BEAR, NOT BAR!! not ILLEGAL_MOVE, not NO_MOVE_SELECTED
     * If I make Classes for PointNum, StartLoc, EndLoc
     * this would want to move there.
     */
    public static boolean legitPointNum( int pointNum ) {
        return ( (1 <= pointNum) && (pointNum <= howManyPoints) );
    }

    /**
     * For deciding legality of starting place for a blot move. 1..24 or barLoc
     * Sad note: doesn't check whether the player is actually ON that point now.
     * More picky than legitEndLoc( ) since not allowing BEAR nor PAST_BEAR.
     * Good for error prevention of move math.
     * 
     * Checks the playerColor.legitColor( ) 
     * so callers don't have to if they don't want to.
     * Picky about not allowing white on BLACK_BAR, etc.
     * A blot might start on the bar or on a legit point.
     * Not sure whether point 0 is used in this implementation...
     * (See the Game.java file for rules of moves...)
     * Static so that other classes can use it without an instance.
     */
    public static boolean legitStartLoc( int pointNum, Player playerColor ) {
        if ( ! playerColor.legitColor( )) {
            throw new IllegalArgumentException("bad playerColor '" + playerColor + "'");
        }
        if ( (1 <= pointNum) && (pointNum <= howManyPoints) ) {
            return true;
        } else if (pointNum == playerColor.getBarLoc( )) {
            return true;
        } else {
            return false;
        }
    } // legitStartLoc( )

    /**
     * if legitEndLoc( ) is called without specifying boolean exact, assume NOT exact.
     */
    public static boolean legitEndLoc( int pointNum, Player playerColor) {
        return legitEndLoc( pointNum, playerColor, /*exact?:*/false);
    }

    /**
     * For verifying that there is such a place for a blot move,
     * but doesn't tell us whether that place is blocked by opponents,
     * and doesn't tell us if we're allowed to actually bear off.
     * See "canLandOn( )" for that total legality checking!
     * 
     * Picky about color since black can't be on WHITE_BAR, etc.
     * but moves can otherwise end anywhere: on points, bar, bear, past_bear,
     * so this isn't as picky as legitStartLoc( ) which doesn't allow the BEAR and PAST_BEAR.
     * 
     * Checks the Player.legitColor( playerColor.getColor( ) ) 
     * so callers don't have to if they don't want to.
     * called by canLandOn( ), among others
     */
    public static boolean legitEndLoc( int pointNum, Player playerColor, boolean exact) {
        if ( ! playerColor.legitColor( )) {
            throw new IllegalArgumentException("bad playerColor '" + playerColor + "'");
        }
        if (legitPointNum(pointNum)) {
            return true;
        }
        if (pointNum == playerColor.getBearOffLoc( )) {
            return true;
        } else if ( exact && (pointNum == playerColor.getBeyondBearOffLoc( )) ) {
            return false;
        } else if ( (!exact) && (pointNum == playerColor.getBeyondBearOffLoc( )) ) {
            return true;
        } else if (pointNum == playerColor.getBarLoc( )) {
            return true;
        } else {
            return false;
        }
    } // legitEndLoc( )

    /**
     * This does the math of moving: given a starting point & a roll distance (one die), 
     * tells the end of the move.
     * Can return ILLEGAL_MOVE if trying to bear out when not legal,
     * if trying to go past exact bear out when exact dice are required, or
     * if landing on a point protected by opponent.
     * For white, simple math in the middle of the board (endpoint = start + steps)
     * but trickier at the end since after final point is the bar.
     * Positive numbers must be used for black, too. This knows for black to use 
     * subtraction behind the scenes. I'll throw exception for negative 'steps'!
     * This just calculates but doesn't actually try to move any blots.
     * Is handy for creating partialMoves (which have start, roll, end ).
     *
     * Note: The endpoint has been checked for legality.
     * 
     * Maybe this could decide whether to return BEAR_OFF_LOC even when 
     * it is going PAST_BEAR_OFF_LOC, but
     * for now I'll return BEAR_OFF_LOC for exact bear off and PAST_BEAR_OFF_LOC if overshooting...
     */
    public/*static*/ int endPointMovingFrom(int startPoint, int steps, Player playerColor) 
    /*throws BadBoardException*/ {
        //         if (board == null) {   /* hmmm, non static call, okay with "this"? */
        //             throw new NullPointerException("board can't be null");
        //         }
        if ( ! legitStartLoc( startPoint, playerColor )) {  // also checks Player.legitColor( )
            /*BadBoardException*/
            throw new IllegalArgumentException("Can't start moving from point '" + startPoint + "'");
        }
        if ( ! legitStepsNum( steps )) {
            /*BadBoardException*/
            throw new IllegalArgumentException("Can't move bad number of steps '" + steps + "'");
        }

        int endPoint = startPoint; /* temp value in case something goes wrong */
        // The following could be done general purpose for all colors, IFF
        // Player class also had a "nextNSteps( )"!!   which would pretty much be like this
        // Here's how:
        // if (startPoint == playerColor.getBarLoc( )) {
        //      ??  endPoint = /* 0 + */ steps; 
        //       endPoint = playerColor.nextNSteps( /* start */ playerColor.getLowPoint,
        //              /* plus N steps:*/ (steps - 1)); // yes, minus 1 for black and white
        //.             // merely to achieve one less step.
        //   } else {
        //        endPoint = playerColor.nextNSteps(startPoint , steps);
        //   }

        if (playerColor == white) {
            if (startPoint == playerColor.getBarLoc( )) {
                endPoint = /* 0 + */ steps;  // zero + steps  for white, moving UP
                // might move this math into Player class someday
            } else {
                endPoint = startPoint + steps;
            }
        } else if (playerColor == black) {
            if (startPoint == playerColor.getBarLoc( )) {
                endPoint = (howManyPoints + 1) - steps; // end - steps for BLACK, moving DOWN
                // might move this math into Player class someday
            } else {
                endPoint = startPoint - steps;
            }
        }
        endPoint = fixOutOfBounds(endPoint, playerColor, this);
        // Set to constant rather than getting it implicitly by math!!
        //     if (legitPointNum(endPoint)) { /* checks in 1..24, not BAR nor BEAR nor PAST_BEAR */
        /* Easy: is 1 .. 24 so we're done, right? Unless the point is blocked... */
        /* if "canLandOn" is as good as I think it is, it is all we need, since it won't allow 
         * landing on blocked points */
        if (canLandOn(endPoint,playerColor,/*exact?:*/ true)) { 
            /* exact is the easier check, so I'll start with it. */
            return endPoint;
            /* I wasn't going to check on moves that use inexact dice, for now, ??
             * since won't the caller of this method do that? or will move blot?
             * Or should I go ahead now and decide whether to return ILLEGAL_MOVE ??
             */
        } /* not else! */
        if (canLandOn(endPoint,playerColor,/*exact?:*/ false)) { 
            /* beware recursion */
            return endPoint;
        } else {
            return ILLEGAL_MOVE;
        }
    } /* endPointMovingFrom( ) */

    /**
     * This was called by endPointMovingFrom( ), after 
     * endPoint has been fixed to be 1..24 or BAR or BEAR or BEAR+
     * but seems redundant with canBearOffWithLargerRolls so isn't being called right now
     */
    //is this redundant from needsInexact? only if canLandOn is averse to bearing
    public /*static*/ boolean canBearOffFromPointWithRoll(int startPoint,int endPoint, 
    int roll,Player playerColor,Board board ) {
        if (! board.canBearOff( playerColor )) {
            System.out.println("blot at startpoint:" + startPoint + " can't move " + roll 
                +" steps (not allowed to bear off yet)");
            return false;
        } // else canBearOff(  )
        return (board.canBearOffWithLargerRolls(playerColor));
    }

    /**
     * This isn't called by canBearOff( ) which we call for quick disqualify check.
     * Called by "canBearOffWithLargerRolls( )" when checking that ALL of the (unused) dice 
     * are too big for any of our blots to bear off.
     */
    public boolean allUnusedDiceAreBiggerThanBearOffMoves(Player playerColor) {
        if (! myDice.getRolled() ) {
            return false;
            // throw new BadBoardException("Dice aren't rolled! Can't try to bear off!");
        }
        if ( ! playerColor.legitColor( ) ) {
            throw new IllegalArgumentException("bad playerColor color '" + playerColor + "'");
        }

        //Here's a quick disqualifier:
        if (onBar(playerColor)) {
            return false;
        }

        if (! canBearOff(playerColor) ) {
            return false;
        }
        int lowDie = myDice.lowestUnusedRoll( );

        /* Depending upon playerColor:
        look at white blots on last 6 points, see who is farthest from bearing off.
        points are 19 .. 24, with 24 closest to bearing off
        OR look at black blots on first 6 points. 
        (Black bears off toward 0, so counts backwards.) 
        Her bear off zone (final quadrant) "starts" at 1 and "ends" at 6 */
        int farthestBlotDist = 0; /* gonna count on blots on final quadrant and bear */
        int start = playerColor.getStartOfFinalQuadrant( ); /* farthest from bearing off */
        /* I could check every point in the final quadrant: _while_ playerColor.inFinalQuadrant( i )
         * but instead I'll just look from the part of the quadrant furthest from bearing until
         * the point just above lowest die (further from bear off). */
        int end = playerColor.getEndOfFinalQuadrant( ); /* last point on the board */
        // int end = playerColor.prevPoint( lowDie ); 
        // prevPoint(lowdie) gets small 1..6 which is wrong for white unless we add it to start
        // but don't be doing any adding for black! So just check the whole stupid quadrant
        int howFarFromEnd = howManyPointsInFinalQuadrant;
        // note: inRange doesn't care what order the range bounds are in: it will figure it out.
        for (int i = start; playerColor.inRange( i, start, end ); i = playerColor.nextPoint( i )) {
            // only notice the "farthestFromEnd" blot (beware points counting up/down in white/black)
            if ((getHowManyBlotsOnPoint(i, playerColor) > 0) && (farthestBlotDist == 0)) {
                farthestBlotDist = howFarFromEnd;
            }
            --howFarFromEnd;
        }
        return lowDie > farthestBlotDist;

        //int lastPlace = locOfLastBlot(playerColor); // oops, not simple since black ends at 1 while white ends at 24

    } /* allUnusedDiceAreBiggerThanBearOffMoves( ) */

    /**
     * This is called by endPointMovingFrom( ) in order
     * to make sure destinations (pointNums)
     * are re-declared as BAR and BEAR constants
     * if out-of-bounds.
     */
    public static int fixOutOfBounds (int endPoint, Player playerColor, Board board) {
        int repairedEndPoint = endPoint; /* might return unchanged if 1..24 */
        if (playerColor == white) {
            if (endPoint == howManyPoints + 1) {
                repairedEndPoint = playerColor.getBearOffLoc( );
            } else if (endPoint > (howManyPoints + 1)) {
                repairedEndPoint = playerColor.getBeyondBearOffLoc( );
            } 
        } else if (playerColor == black) {
            if (endPoint == 0) {
                repairedEndPoint = playerColor.getBearOffLoc( );
            } else if (endPoint < 0){
                repairedEndPoint = playerColor.getBeyondBearOffLoc( );
            }
        } else {
            throw new IllegalArgumentException("Bad playerColor '"+playerColor+"' in fixOutOfBounds");
        }
        return repairedEndPoint;
    } /* fixOutOfBounds */

    /**
     * Tells us if a color is legal (black, white, neutral)
     * Use "Player.legitColor( )" if you want to check for only black and white!
     * "Player.legitColor( )" used to be "Board.legitPlayerColor( )"
     * 
     * Note: the neutral, black and white in Board class are Player pointers, not ints!
     * The neutral, black and white in GAME class, however, are ints. This might be bad.
     */
    public static boolean legitColor( int color ) {
        return ( (color == Game.neutral) || (color == Game.black) || (color==Game.white) );
    } // legitColor( )

    /**
     * Utility for toString of our 3 colors (black,white,neutral)
     * Player has a version of this with just black and white.
     * Note: the neutral, black and white in Board class are Player pointers, not ints!
     * The neutral, black and white in GAME class, however, are ints. This might be bad.
     */
    public static String colorName( int color ) {
        if ( ! legitColor( color) ) {
            throw new IllegalArgumentException("bad color '" + color + "'");
        }
        if (color == Game.neutral) {
            return "neutral (no color)";
        } else if (color == Game.black) {
            return "black";
        } else if (color==Game.white) {
            return "white";
        }
        return "unknown color '" + color + "'";
    } /* colorName */

    /**
     * Tells us which color is on the specified point (Player black, white, or (null)neutral).
     * This can't be used for BAR and BEAR_OFF zones!
     * Maybe it should be allowed to do BAR and BEAR ... but would have to be given a color
     * then so that we knew which BAR/BEAR to look at. Yuck.
     */
    public Player getPlayerOnPoint(int pointNum) {
        if ( ! legitPointNum(pointNum) ) {
            throw new IllegalArgumentException("Bad pointNum '" + pointNum 
                + "'; if you're checking bar/bear, please supply color as param");
        }
        return whichPlayerOnPoint[pointNum];
    } // getPlayerOnPoint

    /**
     * Tells us which color is on the specified point (black, white, [??or neutral??]).
     * Unlike getPlayerOnPoint(ptNum) this can be used for BAR and BEAR_OFF zones!
     * If we know a color, why are we asking? Because this works for checking bar/bear.
     */
    public Player getPlayerOnPoint(int pointNum, Player playerColor) {
        if (playerColor == neutral) {
            return getPlayerOnPoint(pointNum);
        }
        /* these check for Player.legitColor, allow points 1..24 and BAR and BEAR */
        if ( (! legitStartLoc(pointNum, playerColor) ) && (! legitEndLoc(pointNum, playerColor))) {
            throw new IllegalArgumentException("Bad pointNum '" + pointNum + "'");
        }
        if (pointNum == playerColor.getBarLoc( )) {
            return playerColor;
        } else if (pointNum == playerColor.getBearOffLoc( )) { 
            return playerColor;
        } else if (pointNum == playerColor.getBeyondBearOffLoc( )) { 
            /* Need this when considering a BeyondBearOff in doPartialMove.
            Beyond bearoff will match to player's color, even though
            nobody sits on the beyondBearOffLoc. */
            return playerColor;
        } else {
            return whichPlayerOnPoint[pointNum];
        }
    } // getPlayerOnPoint

    // want to add a getJavaPlayerOnPoint for gui

    /**
     * might return 0
     * This is more specific alternative to getHowManyBlotsOnPoint(int pointNum)
     * I suppose this is okay reporting back about BAR and BEAR??
     * But there should never be any blots in the BeyondBearOffLoc so that should get exception??
     */
    public int getHowManyBlotsOnPoint(int pointNum, Player playerColor) {
        // is it a point 1..24 or bar/bear?
        if ( ! legitEndLoc(pointNum,playerColor/*,exact?:true??false??*/)) { 
            /* also checks the color for legitimacy */
            throw new IllegalArgumentException("Bad pointNum '" + pointNum + "'");
        }
        if (pointNum == playerColor.getBarLoc( )) {
            return bar[playerColor.getColor( )]; // black_bar;
        } else if ((pointNum == playerColor.getBearOffLoc( )) 
        || (pointNum == playerColor.getBeyondBearOffLoc( ))) {
            return bear[playerColor.getColor( )]; // white_bear;??
            //         } else if ((playerColor==black) && (pointNum == BLACK_BAR_LOC)) {
            //             return black_bar;
            //         } else if ((playerColor==black) 
            //         && ((pointNum == BLACK_BEAR_OFF_LOC) 
            //                   || (pointNum == BLACK_PAST_BEAR_OFF_LOC))) {
            //             return black_bear;
            //         }
        }
        /* okay, we know it is not BAR nor BEAR, so confirm playerColor */
        if (getPlayerOnPoint(pointNum) == playerColor) {            
            return howManyOnPoint[pointNum];
        } else {
            return 0;
        }
    } // getHowManyBlotsOnPoint( )

    /**
     * Tells us how many blots without specifying their color.
     * See alternative getHowManyBlotsOnPoint(int pointNum, Player playerColor)
     */
    public int getHowManyBlotsOnPoint(int pointNum) {
        if ( ! legitPointNum(pointNum)) {
            throw new IllegalArgumentException("getHowManyBlotsOnPoint got Bad pointNum '" + pointNum 
            + "': beware of calling it with bar or bear, which it can't handle. Use the version" 
            + "that also accepts playerColor arg");
        }
        return howManyOnPoint[pointNum];
    } // getHowManyBlotsOnPoint( )

    /**
     * Convenience method
     * What to do if there are no blots on the specified point? Uh-oh.
     * This is only for points on the board (1..24), and maybe bar?
     */
    public void takeOneBlotOffPoint( int startPointNum) {
        Player playerColor;
        if ( (startPointNum == white.getBarLoc( )) || (startPointNum == white.getBearOffLoc( ))
        || (startPointNum == white.getBeyondBearOffLoc( ))) {
            playerColor = white;
        } else if ((startPointNum == black.getBarLoc( )) || (startPointNum == black.getBearOffLoc( ))
        || (startPointNum == black.getBeyondBearOffLoc( ))) {
            playerColor = black;
        } else if (! legitPointNum(startPointNum)) {
            throw new IllegalArgumentException("Bad PointNum '"+startPointNum+"' in takeOneBlotOff");
        } else { /* we know it is not BAR nor BEAR nor BeyondBear */
            playerColor = getPlayerOnPoint(startPointNum);
        }
        int howMany = getHowManyBlotsOnPoint(startPointNum,playerColor);
        if (howMany < 1) {
            throw new IllegalArgumentException("Can't remove a blot from point '" 
                + startPointNum + "' which has no blots!");
        }
        setNumOfBlotsOnPoint( startPointNum, howMany - 1, playerColor);
    } /* takeOneBlotOffPoint( ) */

    /**
     * This seems partly misnamed: shouldn't it be "setNumOfBlotsOnPoint( )"??
     * Specified point will end up holding "howMany" blots of specified color. 
     * (Might be 0 blots: cleared off, neutral). 
     * In that case any color is okay: blackP, whiteP, null(means neutral).
     * 
     * Might the board be temporarily having a bad number of blots while one is moving??
     * Checking for legit END-point location, but have to treat bar special??
     * 
     * ?? We can put blots in the BearOffLoc, but not in the BeyondBearOffLoc?? Or
     * is BeyondBear OK when we need inexact dice?
     */
    public void setNumOfBlotsOnPoint(int destPointNum, int howMany, Player playerColor) {
        if (playerColor == null) {
            // okay! means neutral
        } else if ( ! legitColor( playerColor.getColor( ) ) ) { 
            /* neutral is allowed by "legitColor( )", unlike "Player.legitColor( )" */
            throw new IllegalArgumentException("Bad color '" + playerColor + "' in setNumOfBlotsOnPoint( )");
        }
        if (playerColor == null /* means neutral*/) {
            if (howMany != 0) {
                throw new IllegalArgumentException("Bad color: Can't put " + howMany 
                    + " 'neutral' blots anywhere!");
            } else {
                howManyOnPoint[destPointNum] = 0;
                whichPlayerOnPoint[destPointNum] = neutral;
            }
        } else if ( ! legitEndLoc(destPointNum, playerColor /*,exact?:??true/false??*/ )) { 
            /* also checks Player.legitColor */
            throw new IllegalArgumentException("Bad destPointNum '"+destPointNum+"' in setNumOfBlotsOnPoint( )");
        } else if ( (howMany < 0) || (howMany > howManyBlots) ) {
            /* ******* Hey, should we check that there are legit num of blots on board?? No,
            because we use this to populate starting boards, when there aren't legal number 
            of blots yet.
            But there's never any excuse for putting a negative number of blots onto a point,
            and no excuse for putting more than "howManyBlots" (aka 15) blots onto a point. */
            throw new IllegalArgumentException("Bad number '" + howMany 
                + "' of blots in setNumOfBlotsOnPoint( ), should be in range [0.." + howManyBlots + "]");
        } else if ((howMany > 0) && ( ! playerColor.legitColor( ) )) {
            throw new IllegalArgumentException("Bad color '" +playerColor.toString( )+ "' for blots");
        } else {
            /* finally getting down to work, having dealt with neutral and legitimacy issues */
            /* What if we're dealing with a BAR? Could such a thing happen??
            there is a moveToBar( ) method for hitting a blot, and comeInFromBar( ) for returning */
            /* Could we be dealing with BEAR_OFF or PAST_BEAR_OFF also?? */
            if ((destPointNum == playerColor.getBarLoc( )) && (playerColor == white)) {
                System.out.println("hmmm, weird, doing 'setNumOfBlotsOnPoint(" + destPointNum + ",/*howMany:*/" 
                    + howMany + ",/*playerColor:*/" 
                    + playerColor.toString( ) + ") for BAR but I'll give it a try.");
                bar[playerColor.getColor( )] = howMany;
            } else {
                howManyOnPoint[destPointNum] = howMany;
                if (howMany==0) {
                    whichPlayerOnPoint[destPointNum] = neutral;
                } else {
                    whichPlayerOnPoint[destPointNum] = playerColor;
                }
                if (this == Game.mainBoard) {
                    myGame.repaint( );
                }
            }
        }
    } // setNumOfBlotsOnPoint( )

    /* rollDice(), getDice1, getDice2, getDice(int),resetDice are all now in myDice */

    /**
     * Moving to "bar" from the specified point. (Checks that can't be coming from BEAR nor BAR!)
     * The point then getting set to color neutral because only a single can get sent to the bar.
     * What if more blots are there: bogus setup.
     * This is called by moveBlot when hitting an opponent, and moveBlot sets a die used.
     * There is equivalent "comeInFromBar(destPoint, playerColor )" method
     * but "moveBlot( from, to, color)" also works when the fromLoc is a bar.
     */
    public void moveToBar(int pointNum, Player bounceeColor) {
        if ( ! legitPointNum( pointNum )) { /* strong test, no BAR, no BEAR */
            throw new IllegalArgumentException("Bad pointNum '" + pointNum + "'in moveToBar!");
        }
        if ( ! bounceeColor.legitColor( ) ) { 
            throw new IllegalArgumentException("Bad playerColor '" + bounceeColor + "'in moveToBar!");
        }
        if (getPlayerOnPoint(pointNum,bounceeColor)!=bounceeColor) {
            throw new IllegalArgumentException("There's no " + bounceeColor.toString( ) 
                + " blot on point '" + pointNum + "' to move to bar!");
        }

        int howManyBlotsOnThisPoint = getHowManyBlotsOnPoint( pointNum, bounceeColor );
        if ( howManyBlotsOnThisPoint == 1 ) {
            bar[ bounceeColor.getColor( ) ]++;
            setNumOfBlotsOnPoint(pointNum, /* howMany */ 0, neutral);
            this.hitsSoFar.add(new HitBlot(pointNum, bounceeColor));
        } else {
            throw new IllegalArgumentException("Error, can only send single blot from point " 
                + pointNum + " to bar, but it has " + howManyBlotsOnThisPoint + " blots!");
        }
    } // moveToBar

    /**
     * Says how how many moves left before black blots are all "beared off".
     * 
     * E.G. suppose there is one black blot on point 1: answer is 1
     * (But white counts the other way: final move on board for white is point 24.)
     * This is equivalent to getBlackPipCount( ) and getWhitePipCount( ) but probably
     * better since multi-purpose.
     */
    public int getPipCount( Player playerColor ) {
        if (! playerColor.legitColor( ) ) {
            throw new IllegalArgumentException("bad playerColor '" + playerColor + "'");
        }
        int pipcount = 0;

        for (int i=1; i<=howManyPoints; i++) {
            if (playerColor == black) {
                pipcount += getHowManyBlotsOnPoint(i, playerColor) * i;
            } else if (playerColor == white) {
                pipcount += getHowManyBlotsOnPoint(i, white) * (25 - i);
            }           
        }
        /* blots on the bar are 25 moves away from bearing off */
        pipcount += 25 * bar[playerColor.getColor( )];

        //System.out.println(colorName(playerColor) + "'s pip count is " + pipcount);
        return pipcount;
    } // getPipCount

    /**
     * Says how how many moves left before black blots are all "beared off".
     * This is equivalent to getPipCount(black), which is probably the better thing to use.
     * 
     * E.G. suppose there is one black blot on point 1: answer is 1
     * (But white counts the other way: final move on board for white is point 24.)
     */
    public int getBlackPipCount( ) {
        return getPipCount(black);
    } // getBlackPipCount

    /**
     * Says how how many moves left before black blots are all "beared off".
     * This is equivalent to getPipCount(white), which is probably the better thing to use.
     *
     * At the start of the game we think this should be 162.
     * 
     * E.G. suppose there is one white blot on point 1: answer is 24.
     * (But black counts the other way: final move for black is point 1.)
     */
    public int getWhitePipCount( ) {
        return getPipCount(white);
    } // getWhitePipCount

    /**
     * For comparing boards, thinking that protected points on certain parts of the board
     * are more useful than on other parts of the board, and unprotected blots are in more
     * danger in some places than in others.
     * Writing this separately for white and black since they count up and down differently, 
     * unfortunately. Not sure if we're going to use this.
     */
    public double protectionScoreWhite(  ) {
        System.out.println("protectionScoreWhite is totally FAKE, fix!");
        System.err.println("protectionScoreWhite is totally FAKE, fix!");
        return 0.5;
    } /* protectionScoreWhite */

    /**
     * Looks at all points to see if there are any loner blots who aren't protected.
     * Note: they might not be in danger if no enemies are nearby.
     * Tells us how many of a color are unprotected.
     * 
     * Might be useful for comparing boards.  or might not matter....
     */
    public int getHowManyUnprotected(Player playerColor ) {
        if (! playerColor.legitColor( )) {
            throw new IllegalArgumentException("bad playerColor color '" + playerColor + "'");
        }
        int howManyUnprotected = 0;

        for (int i=1; i<=howManyPoints; i++) {
            if (solitaryBlotOnPoint( i, playerColor)) {
                /* if ((getPlayerOnPoint(i,playerColor)==color)  
                && (getHowManyBlotsOnPoint(i) == 1)) { */
                howManyUnprotected++;
                /* where they are ought to matter also */
            }
        }

        /* System.err.println("There are " + howManyUnprotected + " unprotected " 
        + playerColor + " points on the board."); */
        return howManyUnprotected;
    } // getHowManyUnprotected

    /**
     * Looks at all points to see if there are any points who are protected.
     * Tells us how many of a color are protected.
     * Ought to care about where they are but doesn't.
     * 
     * Might be useful for comparing boards.
     */
    public int getHowManyProtected(Player playerColor ) {
        if (! playerColor.legitColor( )) {
            throw new IllegalArgumentException("bad color '" + playerColor + "'");
        }

        int howManyProtected = 0;

        for (int i=1; i<=howManyPoints; i++)  {
            if (getHowManyBlotsOnPoint(i,playerColor) > 1) {
                howManyProtected++; /* where they are ought to matter also */
            }
        }

        /* System.err.println("There are " + howManyProtected + " protected " 
        + playerColor + " points on the board."); */
        return howManyProtected;
    } // getHowManyUnprotected

    /**
     * Calculate white's total danger score, by figuring
     * for every exposed white blots are there
     * And how far away are black blots that could hit them
     * AND how far are those white blots from the end?? At least by quadrant?
     * And do we care how far the exposed white blots are from eventual safety?
     * 
     * Note: white starts on 1 and ends on 25 (bear)
     */
    public double getWhiteBeHitProbability( ) {
        double whiteBeHit = 0.0;
        /* finding the exposed white blots */
        for (int pointNum=1; pointNum<=howManyPoints; pointNum++) {
            if ( solitaryBlotOnPoint( pointNum , /* color:*/ white) ) {
                double thisWhiteBeHitProb = blackCanHitPointProb( pointNum );
                int distanceFromStart = pointNum; /* bar is starting point */
                int distanceToBearOff = (howManyPoints + 1) - pointNum;
                int quadrantNumber = quadrantForPoint( pointNum, white );
                double thisProbScore = distanceFromStart * thisWhiteBeHitProb; 
                /* ?? should be linear?? */
                whiteBeHit += thisProbScore;
            }
        } 
        return whiteBeHit;
    } // getWhiteBeHitProbability( )

    /**
     * Points 1..6 are quadrant "1", 7..12 = q"2", for white, etc.
     * This works for white and black (reverse the pointNum itself before calculating black).
     * What about bar and bear? Bar is quadrant 1?? or 0?? Or don't matter for bar?
     * Well, bar is definitely not 4, since all pieces have to be in 4 to permit bearing off.
     * [ ]This would be better dividing by howManyPoints and taking floor?
     */
    public int quadrantForPoint(final int pointNum, final Player playerColor ) {
        if ( ! legitStartLoc(pointNum,playerColor)) { /* also checks Player.legitColor( ) */
            throw new IllegalArgumentException("Bad pointNum '" + pointNum + "'");
        }
        /* so now we know we've got bar or point-on-board (1..24) */
        int pointForMe = pointNum;
        if (playerColor == black) {
            pointForMe = (howManyPoints + 1) - pointNum;
        }
        if ((1<=pointForMe) && (pointForMe <= 6)) {
            return 1;
        } else if ((7<=pointForMe) && (pointForMe <= 12)) {
            return 2;
        } else if ((13<=pointForMe) && (pointForMe <= 18)) {
            return 3;
        } else if ((19<=pointForMe) && (pointForMe <= 24)) {
            return 4;
        } else {
            return 1; //or is bar quadrant "0"?
        }
    } // quadrantForPoint( )

    /**
     * for a particular point, what are the odds black can land on it.
     * Unwritten, not in use yet.
     */
    public double blackCanHitPointProb( int pointNum ) {
        if ( ! legitStartLoc(pointNum, Board.black)) { /* also checks Player.legitColor( ) */
            throw new IllegalArgumentException("Bad pointNum '" + pointNum + "'");
        }

        System.out.println("blackCanHitPointProb needs lots of work because not all rolls");
        System.out.println("work to bring dangerous blots onto us: if there is a protected");
        System.out.println("point in between then enemy can't use it as a step toward hitting");
        System.out.println("us!");
        System.out.println("Uh-oh: don't forget that black blots on bar can hit us!");
        return 0.5; // ?? obviously a fake answer
        // ??
    } // 

    /**
     * For a specific point, is there a solitary blot of color 'color' on it? 
     * (Unprotected, exposed!)
     */
    public boolean solitaryBlotOnPoint( int pointNum ,Player playerColor ) {
        if ( ! legitStartLoc(pointNum,playerColor)) { /* also checks Player.legitColor( ) */
            throw new IllegalArgumentException("Bad pointNum '" + pointNum + "'");
        }
        return (getHowManyBlotsOnPoint(pointNum, playerColor) == 1);
    } // solitaryBlotOnPoint( )

    /** 
     * For a specific point, are there two or more blots of color 'color' on it? (Protected!)
     * Another way to ask this is "if ( howMuchProtected(p, color) > 1) {..."
     */
    public boolean isProtected( int pointNum ,Player playerColor ) {
        if ( ! legitStartLoc(pointNum,playerColor)) { /* also checks Player.legitColor( ) */
            throw new IllegalArgumentException("Bad pointNum '" + pointNum + "'");
        }
        return (getHowManyBlotsOnPoint(pointNum, playerColor) > 1);
    } // isProtected( )

    /**
     * For a specific point, how many blots of color 'color' are on it? (Protected!)
     * Might return 0.
     * Equivalent to getHowManyBlotsOnPoint(pointNum, playerColor);
     */
    public int howMuchProtected( int pointNum ,Player playerColor ) {
        if ( ! legitStartLoc(pointNum,playerColor)) { /* also checks Player.legitColor( ) */
            throw new IllegalArgumentException("Bad pointNum '" + pointNum + "'");
        }
        return getHowManyBlotsOnPoint(pointNum, playerColor);
    } // howMuchProtected( )

    /**
     * Says how many blots of specified color are still on the board.
     * Doesn't seem to count any on the bar??
     * was called "getBlackOnBoard" and "getWhiteOnBoard"
     */
    public int getBlotCountOnBoard(Player playerColor ) {
        if (! playerColor.legitColor( )) {
            throw new IllegalArgumentException("bad playerColor color '" + playerColor + "'");
        }
        int sum = 0;

        for (int i=1; i<=howManyPoints; i++) {
            sum += getHowManyBlotsOnPoint(i, playerColor);
        }

        //System.out.println("There are currently " + sum + " " + colorName( playerColor) 
        //  + " blots on the board");
        return sum;
    } // getBlotCountOnBoard

    /**
     * player can't bear off until all (remaining) blots are on final 6 points (final quadrant) 
     * or farther (i.e. already beared off).
     * If onBar( ) then can't bear off, so easy quick disqualify.
     * Using the handy "getBlotCountOnBoard( playerColor );"
     */
    public boolean canBearOff(final Player playerColor) {
        if ( ! playerColor.legitColor( ) ) {
            throw new IllegalArgumentException("bad playerColor color '" + playerColor + "'");
        }

        //Here's a quick disqualifier:
        if (onBar(playerColor)) {
            return false;
        }
        int sum = 0; /* gonna count on blots on final quadrant and bear */
        int start = playerColor.getStartOfFinalQuadrant( );
        for (int i = start; playerColor.inFinalQuadrant( i ); i = playerColor.nextPoint(i) ) {
            sum += getHowManyBlotsOnPoint(i, playerColor);
        }

        if (sum==getBlotCountOnBoard( playerColor )) {
            return true;         //There are 15 blots (pieces) in backgammon
        }
        return false;
    } // canBearOff

    /**
     * Checks whether any blots of player1 are in the region of player2's blots.
     * In two-player game, blots on bar can come so onBar automatically means overlap.
     */
    public boolean playersOverlap( Player player1, Player player2) {
        if (player1.getColor( ) == player2.getColor( )) {
            throw new IllegalArgumentException("silly to check overlap of same colors "
                + player1 + "," + player2);
        }
        if ((onBar(player1)) || (onBar(player1))) {
            return true;
        }
        // easy for two-player game:
        if (Game.howManyColors == 2) {
            // white is going up to 24, so overlap means its last is below black's last
            return (locOfLastBlot( Game.whiteP ) < locOfLastBlot( Game.blackP ));
        } else {
            // for more than 2 players, untested
            int start1 = player1.getLowPoint( ); /* farthest from bearing off */
            int end1 = player1.getHighPoint( );
            for (int i = start1; player1.inRange( i, start1, end1 ); i = player1.nextPoint( i )) {
                if (getHowManyBlotsOnPoint(i, player2) > 0) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Where is the farthest behind blot for this player (might be bar)
     * Might be bear if all players are off!?
     * Can be used by AI.switchStrategy( ) and Board.canBearOff( ) and 
     * Board.playersDontOverlap( ) and 
     * Board.allUnusedDiceAreBiggerThanBearOffMoves( )
     */
    public int locOfLastBlot( Player playerColor) {
        if (onBar(playerColor)) {
            return playerColor.getBarLoc( );
        }
        int start = playerColor.getLowPoint( ); /* farthest from bearing off */
        int end = playerColor.getHighPoint( );
        for (int i = start; playerColor.inRange( i, start, end ); i = playerColor.nextPoint( i )) {
            // only notice the "farthestFromEnd" blot (beware points counting up/down in white/black)
            if (getHowManyBlotsOnPoint(i, playerColor) > 0) {
                return i;
            }
        }
        // wow, none found: must all be on bearOff heaven??
        return playerColor.getBearOffLoc( );
    }

    /**
     * True if specified color has any blots on the bar
     */
    public boolean onBar(Player playerColor) {
        if ( ! playerColor.legitColor( ) ) {
            throw new IllegalArgumentException("bad playerColor color '" + playerColor + "'");
        }
        return (bar[playerColor.getColor( )] > 0);
    } // onBar

    /**
     * How many blots of specified color are on its bar.
     * Related to onBar( ) which tells us (boolean) if ANY of the player's blots are on her bar
     */
    public int howManyOnBar(Player playerColor) {
        if ( ! playerColor.legitColor( ) ) {
            throw new IllegalArgumentException("bad playerColor '" + playerColor + "'");
        }
        return bar[playerColor.getColor( )];
    } /* howManyOnBar */

    /**
     * Selects a point and shows the possible moves. This used to try both
     * dice and do canMove( ), but now merely calls legalPartialMovesFromPoint( )
     * which does the testing. This saves the legal moves and potDest1 & potDest2
     * and lights them up on the GUI.
     * 
     * [ ]This should be moved onto the user interface, serves to let human specify a move
     * by "handling" a point (clicking on a point, and if there are blots on the point then
     * this does several things:
     * --lights up potDest(n) buttons at the endpoints of legal partialMoves
     * --store potDest1 and potDest2 as part of the board
     * --tell the board to remember the selectedPointLoc
     * --tell the board that "aPointIsSelected"
     * This happens when current player clicks one of her own blots: 
     * this calculates the potential moves (potDest1 & potDest2) 
     * and displays the potential move buttons on the points that we can move this blot to.
     * Memorizes the clicked upon point as "selectedPointLoc".
     * ?? Does this ensure that everybody is in from the bar?? 
     *   No, "selectABar" does that and is called by Board.doPartialMove( )
     */
    public void selectAPoint(int pointNum, Player playerColor) {
        if ( ! legitStartLoc(pointNum,playerColor)) { /* also checks Player.legitColor( ) */
            throw new IllegalArgumentException("Bad pointNum '" + pointNum + "'");
        }
        if ( onBar(playerColor) ) {
            /* throw new IllegalArgumentException("hey, " + playerColor 
            + " is on bar, so deal with that."); */
            System.err.println("hey, " + playerColor + " is on bar, deal with that first.");
            // errorBeep( );
        }
        // If a point is already selected, we can't select another w/o cancelling prev choice.
        if ((getPlayerOnPoint(pointNum,playerColor)==playerColor) && (! aPointIsSelected)) {

            ArrayList<PartialMove> legals = legalPartialMovesFromPoint(pointNum, playerColor);
            if (legals.size() < 1) { // oops, they clicked somewhere that has no legal moves
                // errorBeep( );
                setSelectedPointLoc(NO_MOVE_SELECTED,playerColor);
            } else {
                setSelectedPointLoc(pointNum,playerColor); // sets the potDests ILLEGAL for starters
                // ^ sets aPointIsSelected to true (or false if pointNum== NO_MOVE_SELECTED)
                for (PartialMove pm : legals ) { // now turn on any legal potential moves
                    //int dieNum = pm.getWhichDie( );
                    int start = pm.getStart( );
                    if (start != pointNum) {
                        System.err.println("Whoa, badness: selectAPoint( " + pointNum + "," 
                            + playerColor + ") has partialMove starting at a different point:" + start);
                        // throw new IllegalArgumentException("Whoa, badness: ....");
                    }
                    int dest = pm.getEnd( );
                    int rollVal = pm.getRollValue( );
                    int whichDie = whichPotDest(rollVal);
                    setPotDest(playerColor, whichDie, dest); // will figure out which die
                    setSelectedPointLoc( start, playerColor ); 
                    // ^ sets aPointIsSelected = true; unless start==NO_MOVE_SELECTED
                    setRelevantButtons( dest, playerColor, /* isMove1or2: */whichDie);
                    /* ^ this only changes buttons on mainboard */
                }
            }
        }
    } // selectAPoint( )

    // old selectAPoint( ) stuff, now in legalPartialMoveFromPoint( )
    //***... old        
    //int potDest1, potDest2; Don't declare, using the class fields. (Bad idea?)
    // The player cannot move the other's blots
    //        if ((getPlayerOnPoint(pointNum,playerColor)==playerColor) 
    //        && (!/*myGame.status.*/aPointIsSelected)) {
    // Get the possible destinations (including BEAR, BEAR+ (and BAR??)) 
    // when starting from that point.
    //            for (int dieNum = 1; dieNum <= howManyDice; ++dieNum) {
    //                int die = myDice.getDie(dieNum); // temp cache
    //                int dest = endPointMovingFrom(pointNum, die, playerColor/*, this*/); // might be err

    // If a move is valid, enable the button to move to it.
    //                boolean canLand = canLandOn(dest,playerColor /*,exact?: false*/);
    //                boolean dieIsUsed = myDice.getUsedDie( dieNum);
    //                if ( canLand && ( ! dieIsUsed )) {
    //                    aPointIsSelected = true;
    //                    setRelevantButtons( dest, playerColor, /* isMove1or2: */dieNum);  
    //                    /* only tweaks if mainboard */
    //                    setPotDest(playerColor, dieNum, dest);
    //                } else {
    //                    setPotDest(playerColor, dieNum, ILLEGAL_MOVE);
    //                }// end if move1 is valid
    //            }
    //            setSelectedPointLoc(pointNum); 
    //            // ^ sets aPointIsSelected to true unless pointNum==NO_MOVE_SELECTED
    //        }
    //        myGame.debug_msg("selectAPoint() is ending");

    /**
     * called by selectAPoint( ). If we're talking to the main (user visible) board
     * turns on (& off?) GUI buttons  "cancel choice", and/or the second move.
     * The isMove1or2 is for those "potential move" buttons that appear on gui.
     * 
     * @param isMove1or2 Can be 1 or 2 (for dice 1 or dice 2) or 0 means none: hide'em
     */
    public void setRelevantButtons(int potentialDest, Player playerColor, int isMove1or2 ) {
        if (this == Game.mainBoard) {
            if ((potentialDest == NO_MOVE_SELECTED) || (isMove1or2 == 0)) {
                myGame.fButton[Game.btn_AtPotentialMove1].setVisible(false);
                myGame.fButton[Game.btn_AtPotentialMove2].setVisible(false);
            } else if (legitPointNum( potentialDest )) { 
                // 1..howManyPoints, not bear, not beyondbear, not bar
                myGame.fButton[myGame.btn_CancelChoice].setEnabled(true);
                if (isMove1or2 == 1) {
                    myGame.fButton[myGame.btn_AtPotentialMove1].drawOnPoint(potentialDest);
                } else if (isMove1or2 == 2) {
                    myGame.fButton[myGame.btn_AtPotentialMove2].drawOnPoint(potentialDest);
                } else {
                    throw new IllegalArgumentException("bad isMove1or2 '" + isMove1or2 + "'");
                }
            } else if ((potentialDest == playerColor.getBearOffLoc( )) 
            || (potentialDest == playerColor.getBeyondBearOffLoc( ))) {
                myGame.fButton[myGame.btn_CancelChoice].setEnabled(true); 
                myGame.fButton[myGame.btn_BearOff].setEnabled(true);
            } else {
                // moved to bar??
            }
        }
    } // setRelevantButtons

    /**
     * Returns whether the current player can't move anywhere else
     * and needs to be able to bear off with an inexact roll 
     * BECAUSE all current dice are bigger than distance remaining for ALL blots.
     * Doesn't actually tell us which roll we can use.
     * Note: still can't use small roll to bear off when higher blots exist!
     * temporarily unprivate so I can test it...
     * was called "needsInexactRolls"
     */
    boolean canBearOffWithLargerRolls(Player playerColor) {
        /* could be private method when testing is done */
        if ( ! playerColor.legitColor( )) {
            throw new IllegalArgumentException("bad playerColor '" + playerColor + "'");
        }
        boolean canmove = false;
        // Cycle through all the points??

        if (onBar(playerColor)) { //  canBearOff( ) also checks this
            return false;
        }
        if (! canBearOff(playerColor) ) {
            return false;
        } else {
            // can bearOff, so all pieces are in final quadrant, and not on bar.
            //// checking for "canMove" might have been unnecessary AND cause of infinite recursion...
            // if (canMove(playerColor, /*exact?*/true)) {
            //        return false;
            //        // Since there is an exact move for the player (perhaps even a bear off)
            //        // then do I not need to check that no blots are on higher pointNum than 
            //        // the lowest dice??
            //    } else if (allUnusedDiceAreBiggerThanBearOffMoves(playerColor)) {
            if (allUnusedDiceAreBiggerThanBearOffMoves(playerColor)) {
                return true;
            } else {
                return false;
            }
        }
    } // canBearOffWithLargerRolls( )

    /**
     * Uses method boolean canMove() and legalPartialMovesFromPoint( )
     * both of which know that when a player is on the bar they can't move anywhere!
     * Perhaps using both is redundant.
     * legalPartialMovesFromPoint( ) takes of checking canLandOn( ).
     */
    ArrayList<PartialMove> allLegalPartialMoves( Player playerColor /*, Game myGame*/) 
    /*throws BadMoveException, BadPartialMoveException, BadBoardException*/ {       
        /* using selectAPoint( ) to find all legal moves. Might also want to check canMove( ) */
        if ( ! playerColor.legitColor( ) ) {
            throw new IllegalArgumentException("bad playerColor '" + playerColor + "'");
        }
        if ( ! canMove( playerColor ) ) {
            System.out.println( playerColor + " cannot move.");
            if (this==Game.mainBoard) {
                myGame.forfeitTurn( this, playerColor); // calls endTurn
            }
            return new ArrayList<PartialMove>( ); /* return an ArrayList that has no elements ! */
        }
        LocList myPoints = allMoveableBlotLocs(playerColor); /* might be empty */
        if (myPoints.myList.isEmpty( )) {
            System.out.println( playerColor + " has no moveable blots.");
            if (this==Game.mainBoard) {
                myGame.forfeitTurn( this,playerColor ); // ??
            }
            return new ArrayList<PartialMove>( ); /* return an ArrayList that has no elements ! */
        }
        /* System.out.println("in Board.allLegalPartialMoves( ), moveableBlotLocs==" 
        +myPoints.toString( )); */

        ArrayList<PartialMove> bunchOfPartialMoves = new ArrayList<PartialMove>( );   
        /* for storing & returning a collection of "PartialMove"s */
        /* Work through every point in myPoints */
        for (int myPoint : myPoints.myList ) {
            bunchOfPartialMoves.addAll( legalPartialMovesFromPoint(myPoint, playerColor) );
        }
        /* System.out.println("in board.allLegalPartialMoves( ), the bunchOfPartialMoves=='" 
        + bunchOfPartialMoves.toString( ) + "'"); */
        return bunchOfPartialMoves;
    } /* allLegalPartialMoves( ) */

    /**
     * Saving a list of the partial moves that can be made from a particular point.
     * Might return empty list. 
     * Called by allLegalPartialMoves( ) and by selectAPoint and canMove( )
     * Only gets called if there are blots of myColor on myPoint, supposedly.
     * This is counting on the move math calculator (endPointMovingFrom:)
     * to handle bar and bear properly, which it supposedly does.
     * 
     * Formerly was doing selectAPoint( ) to light up those "move here" buttons
     * and then storing their locations (potDest1, potDest2). Now it is the other 
     * way around: selectAPoint( ) asks here for the list of legal moves of the 
     * selected point and stores them as potDest1 & potDest2.
     * 
     * probably works for bar (uses endPointMovingFrom which is cool with bar)
     */
    public ArrayList<PartialMove> legalPartialMovesFromPoint(int startPoint, Player playerColor) {
        if ( ! Board.legitStartLoc( startPoint, playerColor )) { /* also checks Player.legitColor */
            throw new IllegalArgumentException("Can't start moving from point '" + startPoint + "'");
        }
        if (getPlayerOnPoint(startPoint,playerColor)!=playerColor) {
            // throw new IllegalArgumentException(playerColor + " can only move its own blots.");
            return null;
        }
        ArrayList<PartialMove> bunchOfPartialMoves = new ArrayList<PartialMove>( );   
        /* for storing & returning a collection of "PartialMove"s */

        // Get the possible destinations (including BEAR, BEAR+ (and BAR??)) 
        // when starting from that point.
        for (int dieNum = 1; dieNum <= howManyDice; ++dieNum) {
            int dieVal = myDice.getDie(dieNum); // temp cache
            int dest = endPointMovingFrom(startPoint, dieVal, playerColor/*, this*/); // might be err

            // If a move is valid, enable the button to move to it.
            boolean canLand = canLandOn(dest,playerColor /*,exact?: false*/); // not ILLEGAL_MOVE
            boolean dieIsUsed = myDice.getUsedDie( dieNum);
            if ( canLand && ( ! dieIsUsed )) {
                // PM expects (start, rollValue, end, Board, playerColor, whichDie) 
                PartialMove pm 
                = new PartialMove( startPoint, dieVal, dest, this, playerColor/*, dieNum*/ );
                // could it be null? Not likely: startPoint and dest have been checked repeatedly
                bunchOfPartialMoves.add(pm);
            }// end if move is valid
        } // end for all dice one at a time
        setSelectedPointLoc(startPoint,playerColor); // sets aPointIsSelected unless startPoint== NO_MOVE_SELECTED

        /* System.out.println("in legalPartialMovesFromPoint(" + startPoint + "..), the bunchOfPartials=='" 
        + bunchOfPartialMoves.toString( ) + "'"); */
        return bunchOfPartialMoves;
    } /* legalPartialMovesFromPoint( ) */

    //     // old version of legalPartialMovesFromPoint that returned the 
    //     // potDests created by selectAPoint( ).
    //        aPointIsSelected = false; // must unselect any old move choices before handling new ones!! 
    //        selectAPoint( startPoint, playerColor ); 
    //         /* will discover the potential points we can move to: potDest1, potDest2.*/
    //         /* Since this is worried about "Partial" (one-step) moves, dice doubles aren't an issue. 
    //          * There's no potDest3,4. */
    //            int endPoint1 = endPointMovingFrom(startPoint, dice1, playerColor);
    //            if (endPoint1 != ILLEGAL_MOVE) { 
    //                // equiv? to canLandOn(endPoint1, playerColor,/*exact?*/??)) {
    //                PartialMove fakePartialMove1 
    //                = new PartialMove( startPoint, dice1, endPoint1, this, playerColor, /* whichDie:*/ 1 );
    //                bunchOfPartialMoves.add(fakePartialMove1);
    //    //     } /* old legalPartialMovesFromPoint( ) */

    /**
     * Calculate just one legal move. 
     * This is just an attempt to sneak up on designing "allLegalMoves( )".
     * Not for real use! Needs to loop as long as ! allDiceUsedUp so doubles are okay.
     * Beware: might return null.  ?? Should there be a nullMove object?
     * Note: this recurses until building and returning a "complete" move, using all the dice. 
     * This is not just a partial move (which use one die).
     * 
     * ?? doesn't use the stepsSoFar, wassup, incomplete design?
     * ?? does this have 
     */
    Move aLegalMove( Player playerColor, Move stepsSoFar ) throws BadBoardException, BadMoveException {
        if ( ! playerColor.legitColor( )) {
            throw new IllegalArgumentException("bad playerColor '" + playerColor + "'");
        }
        // ?? Needs to loop as long as ! allDiceUsedUp so doubles are okay.
        ArrayList<PartialMove> partials1 = allLegalPartialMoves(playerColor );
        if (partials1.isEmpty() ) {
            return null;
        }
        PartialMove myPartial1 = partials1.get(0);

        Board partialBoard = new Board(myGame, this); 
        /* a copy of this board, including dice values */

        partialBoard.doPartialMove(myPartial1); 

        ArrayList<PartialMove> partials2 =  partialBoard.allLegalPartialMoves(playerColor);
        PartialMove myPartial2 = partials2.get(0);

        ArrayList<PartialMove> allMyPartials = new ArrayList<PartialMove>( );
        allMyPartials.add( myPartial1 );
        allMyPartials.add( myPartial2 );
        if (myDice.isDoubles( )) { // or while (partialBoard.hasmoves(playerColor))
            // get more moves
            throw new IllegalArgumentException("not done calculating doubles moves");
        }
        return new Move( allMyPartials, playerColor/*, partialBoard/* ?* myGame*/);
    } /* aLegalMove( ) */

    /**
     * My original plan was to get a collection of all legal moves so that AI
     * could compare and choose from the resulting boards. As of 5 May 2012 I'm
     * leaving allLegalMoves unwritten and instead focusing on writing this 
     * "bestLegalMove( )" that doesn't bother to save ALL of the possible tables. 
     * Instead, bestLegal can return the "best" move with the score of the resulting 
     * board, as determined by a Scorer object (or scoring function) that is passed in by AI.
     * 
     * @param myScorer will be used to calculate values for boards we reach
     * @param partialsSoFar the steps (partialMoves) in the move so far to build this board. 
     * (The caller is remembering the starting board.) Might be null if no partials so far.
     * @param bestMoveSoFar CANCELLED: each copy of this method will pick the best of
     * its children and pass back the winner. Was going to be null at the start. 
     * 
     * Since Moves are tied to a starting
     * board, Moves could conceivably hold a score field stating "the value of this move
     * when applied to startingBoard and measured with scoring function S"... ?? Then
     * the Move would have to have a boolean hasBeenScored and a Scorer. For now
     * I'm passing around the bestMoveSoFar and its corresponding score. The score of a
     * null board should be ignored, and I'm not sure how to enforce this except by
     * wiring it into the code here.
     */
    public MoveWithScore bestLegalMove(Scorer myScorer, ArrayList<PartialMove> partialsSoFar, Player playerColor) {
        /* ?? should I pass in the startingBoard, so new Move( ) can be given the original starting board, ??
        which it might want to know with respect to scoring? */
        if (!myDice.getRolled( )) {
            throw new IllegalArgumentException("best legal move needs dice to be rolled");
        }
        if (partialsSoFar == null ) {
            // not a problem: no moves so far.
            partialsSoFar = new ArrayList<PartialMove>( );
        }
        if ( ! playerColor.legitColor( )) {
            throw new IllegalArgumentException("bad playerColor '" + playerColor + "'");
        }
        if (onBar(playerColor)) {
            System.err.println("bestLegalMove( ) sees that " + playerColor + " is on bar, will try to cope.");
            // maybe this shouldn't happen??
            //throw new IllegalArgumentException("bad playerColor '" + playerColor + "'");
            Move bestMove = myGame.myAI.getMyStrategy( ).pickBarMove(this, playerColor );
            MoveWithScore bestMoveWithScore = new MoveWithScore( bestMove,/*Board:*/this ); // is copy constructor
            //double myScore = myScorer.score( this/*Board*/, playerColor );
            bestMoveWithScore.setScore(myScorer, this/*Board*/ /* was myScore*/);
            return bestMoveWithScore;
            // will recurse infinite if getInFromBar calls bestLegalMove!
        }

        ArrayList<PartialMove> copyOfPartialsSoFar = new ArrayList<PartialMove>( partialsSoFar );

        // now see if we can move any further...
        ArrayList<PartialMove> newBunchOfPartials = allLegalPartialMoves(playerColor );
        MoveWithScore bestMoveWithScoreSoFar = null ; //for comparing the results of the recursions
        /* do something with every partial move: build a new board, 
        and then if we still have more moves left, 
        ask that new board what its legal partial moves are 
        and append each pmove to newPartialsSoFar for recursive call */
        if ((newBunchOfPartials == null) || (newBunchOfPartials.size( ) < 1)) {
            // || (!canMove(playerColor) || (mydice.allDiceAreUsed( ))) // allLegalPartials will have checked
            /* End of tree branch. Seal up the partials into a move. We're expected to return
             * a bunch of Moves, so wrap the finalized move into a 1 element bunch of Moves.
             */
            double myScore = myScorer.score( this/*Board*/, playerColor );
            //            if (myScore > bestScoreSoFar ) {
            try {
                Move bestMove = new Move(copyOfPartialsSoFar, playerColor);
                bestMoveWithScoreSoFar = new MoveWithScore( bestMove, /*Board:*/this );
            } catch (Exception e) {
                throw new IllegalArgumentException("had trouble making new Move or MoveWithScore");
            }
            bestMoveWithScoreSoFar.setScore(myScorer, /*Board:*/this);
        } else {
            double bestScoreSoFar = 0;// -NAN would be better! maybe -maxDouble?
            boolean gotAScore = false;
            // each recursing children will hand back a champion MoveWithScore
            for (PartialMove partial : newBunchOfPartials) {
                Board tempBoard = new Board(myGame,this);
                tempBoard.doPartialMove(partial); // error check??
                copyOfPartialsSoFar = new ArrayList<PartialMove>( partialsSoFar );
                copyOfPartialsSoFar.add(partial); // in order, right?
                //note: should the partial be added to a history of partials
                //with history of partials starting at null when ... when new turn? or when new ai analysis?
                //         recurse to build further, getting back a bunch of (full, from start) moves
                //  will recurse as long as there are legalPartialMoves avail
                double newScore;
                MoveWithScore newMoveWithScore;
                if (tempBoard.canMove( playerColor   )) {
                    newMoveWithScore = tempBoard.bestLegalMove( myScorer, copyOfPartialsSoFar, playerColor );
                    newScore = newMoveWithScore.getScore( );
                } else {
                    Move newMove = null;
                    try {
                        newMove = new Move( copyOfPartialsSoFar, playerColor );
                        newMoveWithScore = new MoveWithScore( newMove, myScorer, tempBoard );// will calculate score
                    } catch (Exception e) {
                        throw new IllegalArgumentException("had trouble making new Move or MoveWithScore\n:"  + e);
                    }

                    newScore = myScorer.score( tempBoard, playerColor );
                }
                if (! gotAScore ) {
                    bestScoreSoFar = newScore;
                    gotAScore = true;
                    bestMoveWithScoreSoFar = newMoveWithScore;
                } else if (newScore > bestScoreSoFar) {
                    bestScoreSoFar = newScore;
                    bestMoveWithScoreSoFar = newMoveWithScore;
                }
                //Move championOfSubtree = tempBoard.bestLegalMove(myScorer, newPartialsSoFar, playerColor, );
                /* ^ SetCollection<Move>?*/
                //myLegalMoves.addAll( moreMoves ); 
                /*Note: not concatenating Moves here: partial moves were appended to thePartialsSoFar
                 * during recursion, and those were wrapped into Moves that were returned as collections
                 * of moves. Currently ArrayList, might want to be some kind of set?
                 */
                // if Board has a tree or history?: mySetOfSubsequent.add( some
            }
        }
        return bestMoveWithScoreSoFar; //
    }

    // SetCollection<Board> allLegalResultingBoards
    /**
     * returning a set, with no duplicate boardlayouts (arrangement of blots,
     * including bar and bearOff)
     * (not caring whether dice are the same, nor whether any points are selected,
     * not even caring who the current player is: I just want to know the layout
     * regardless of how we got there. As long as each board is storing a "latestMove"
     * (or better, history of (recent?) Moves) then I know how to tell the "this"
     * board what to do to get to the resulting layout.
     * 
     * Hmmm, For a moment I was thinking that only the Game has to keep the full
     * history of moves... and if we are ever looking 2 or 3 moves ahead, then
     * each resultingboard would have to keep the 2 or 3 latest moves history.
     * 
     * I need a way to thoroughly iterate through all possible sequences of partialMove 
     * leading to new board leading to new legalPartialMove, for possible 4 steps deep,
     * so I don't think I can think about one partialMove at a time... rather than keeping
     * some kind of list of all partials and all resultingBoards...
     */

    /**
     * Incomplete, untested, but might run.
     * This was intended to provide a collection of all legal moves so that AI
     * could compare and choose from the resulting boards. As of 5 May 2012 I'm
     * leaving this unwritten and instead focusing on writing a "bestLegalMove( )"
     * that doesn't bother to save ALL of the possible tables. Instead, bestLegal
     * can return the "best" move with the score of the resulting board, as
     * determined by a Scorer object (or scoring function) that is passed in by AI.
     * 
     * Note: the children must be given DUPLICATES of "partialsSoFar" so they
     * don't squabble over changing the original. Shallow copy is okay, I think,
     * since they won't be altering the PartialMove objects that it contains?
     * 
     * Uses method boolean canMove(int color) which itself uses 
     * boolean canLandOn(int newPointNum, playerColor, /*exact???)??? 
     * Beware: with doubles (4 partial moves) I think that 15 blots provides 
     * up to 15^4 possible moves = 50,625.
     * (Without doubles, there are up to 450 possible moves (2*15^2) for each pair-of-dice roll.
     * 
     * As a follow-up to this, I'm sketching allLegalResultingBoards( ) above.
     * 
     * flip-flopping between design styles: 
     * (a) recursion, appending to (copy of) partialsSoFar and returning them in a move when stuck.
     * (b) building a (global?) tree (Set of unique leaves) for later recursion.
     */
    ArrayList<Move> /*SetCollection<Move>?*/allLegalMoves( Player playerColor 
    , ArrayList<PartialMove> partialsSoFar /*,setOfMovesToAppendTo?*/) 
    /*throws BadMoveException, BadPartialMoveException, BadBoardException*/ 
    {

        System.err.println("allLegalMoves is untested & probably incomplete!!");

        if ( ! playerColor.legitColor( )) {
            throw new IllegalArgumentException("bad playerColor '" + playerColor + "'");
        }
        // keep copy of partialmoves so far to which we will add just one new partial
        ArrayList<PartialMove> newPartialsSoFar = new ArrayList<PartialMove>( partialsSoFar );

        // now see if we can move any further...
        ArrayList<PartialMove> newBunchOfPartials = allLegalPartialMoves(playerColor );
        // second try, 2012 May
        // ancient first draft code is commented out immediately below
        /* do something with every partial move: perhaps build a new board, 
        and then if we still have more moves left, ask that new board what its legal partial moves are 
        and combine them with this currPartial. */ 
        // 
        ArrayList<Move> myLegalMoves = new ArrayList<Move>/*SetCollection<Move>?*/( );
        /* ^ SetCollection<Move>?*/
        // note: leaves return a collection of Moves with only one Move inside.
        // intermediate nodes combine all the Moves from their children and hand back the collections 
        if ((newBunchOfPartials == null) || (newBunchOfPartials.size( ) < 1)) {
            // || (!canMove(playerColor) || (mydice.allDiceAreUsed( ))) // allLegalPartials will have checked
            /* End of tree branch. Seal up the partials into a move. We're expected to return
             * a bunch of Moves, so wrap the finalized move into a 1 element bunch of Moves.
             */
            try {
                Move thisMove = new Move(partialsSoFar, playerColor);
                myLegalMoves.add( thisMove ); // will be returned below

            } catch (/*BadBoard,BadMove*/Exception e) {
                System.err.println("BadBoard found while collecting allLegalMoves.");
                System.err.println(this);
                System.err.println(e);
            }
        } else {
            // the recursing children will hand back bunches of moves (sets perhaps) 
            for (PartialMove partial : newBunchOfPartials) {
                Board tempBoard = new Board(myGame,this);
                tempBoard.doPartialMove(partial); // error check??
                newPartialsSoFar.add(partial); // in order, right?
                //note: should the partial be added to a history of partials
                //with history of partials starting at null when ... when new turn? or when new ai analysis?
                //         recurse to build further, getting back a bunch of (full, from start) moves
                //  will recurse as long as there are legalPartialMoves avail
                ArrayList<Move> moreMoves = tempBoard.allLegalMoves(playerColor,newPartialsSoFar);
                /* ^ SetCollection<Move>?*/
                myLegalMoves.addAll( moreMoves ); 
                /*Note: not concatenating Moves here: partial moves were appended to thePartialsSoFar
                 * during recursion, and those were wrapped into Moves that were returned as collections
                 * of moves. Currently ArrayList, might want to be some kind of set?
                 */
                // if Board has a tree or history?: mySetOfSubsequent.add( some
            }
        }
        System.err.println("allLegalMoves is totally fake, FIX!!");
        //         throw new NullPointerException( "allLegalMoves isn't built yet.");
        return myLegalMoves; // 
    } /* allLegalMoves( ) */

    /**
     * Alternate calling overload: breaks apart fields and passes them to old doPartialMove, to
     * spare us from having to manufacture temporary PartialMoves in the button listeners.
     * But that might not be so bad??
     */
    void doPartialMove(PartialMove pm) {
        if (pm == null) {
            /* barf or just don't do empty move? The latter is friendlier... */
            throw new NullPointerException("c'mon, give me a real PartialMove to do!");
        }
        int from = pm.getStart( ); int to = pm.getEnd( ); int rollVal = pm.getRollValue( );
        Player p = pm.getPlayerColor( );
        doPartialMove(from, to, /*pm.getWhichDie( ), */ rollVal, p);
        /* ignoring roll */
    } /* doPartialMove */

    /**
     * Combination of Move.doMove and Game.doMove, which have been removed.
     * This calls Board.doPartialMove, and should save the latest move
     * for undo and so the recentMove buttons know where to display.
     */
    public void doMove(Move myMove, Player playerColor ) {
        /* doPartialMove( ) (might?) call endTurn( ) */
        int howManyPartialsDone = 0;
        for (PartialMove p : myMove.getMyPartials()) {
            this.doPartialMove( p ); // uses up dice, will end turn if allDiceAreUsed
            howManyPartialsDone++; // yes, increment now. Moves are 1,2,3,4 not 0,1,2
            if (this==Game.mainBoard) {
                myGame.showPartialMove( p, playerColor, /*partialMove#:*/howManyPartialsDone );
                myGame.endPartialMove(playerColor); // turns off selectedPoint
            }
        }
        //   doPartialMove worries about onBar and canMove (which checks allDiceAreUsed)
        // maybe better not to do the following, let some bigger boss do it??
        // No, perhaps this move can only do 0 or 1 partials & then forfeits rest of turn
        if (this==Game.mainBoard) {
            if (! getMyDice( ).allDiceAreUsed( )) { // allDice ended turn above
                myGame.forfeitTurn(this,playerColor ); // endTurn
            }
        }
    } /* doMove( ) */

    /** 
     * Does "partialMove": use one die value to move one blot. 
     * (Was originally named 'superMove'.)
     * @param fromPoint - where the blot is starting (might be 0 (white bar) or 25 (black bar))
     * @param toPoint - the new position (point number) to move to.
     * @param whichDie - which dice is being used, the first one or the second one.
     * @param rollValue - the face of the die, (when bearing off might != distance traveling)
     * 
     * calls "Board.moveBlot( )" method
     * There is a "myDice.getUsedDice( )" which says which partial moves 
     * (corresponding to which dice) have been used. 
     * myDice.isDoubles( ); is true when doubles have been rolled, 
     * and myDice.allDiceAreUsed( ) keeps track of moves countdown.
     * 
     * Note: if this is the final partial move on Game.mainBoard, 
     * this switches players by calling Game.endTurn( )!
     * This calls moveBlot( ), which checks the legality of the move.
     **/
    void doPartialMove(int fromPoint, int toPoint, /*int whichDie, */int rollValue, Player playerColor) {
        /* maybe this could be private when/if testing is ever done...*/ 
        /* In networked mode: 25 = to bar, 26 = bear off */
        if ( ! Board.legitStartLoc( fromPoint, playerColor )) { /* also checks Player.legitColor */
            throw new IllegalArgumentException("Can't start moving from point '" + fromPoint + "'");
        }
        //         if ( ! Dice.legitDieNum( whichDie )) {
        //             throw new IllegalArgumentException("Can't use Die number '" + whichDie + "'");
        //         }
        if ( ! Dice.legitDiceValue( rollValue )) {
            throw new IllegalArgumentException("Wassup? Our dice can't roll '" + rollValue + "'");
        }
        if ( ! Board.legitEndLoc( toPoint, playerColor /*,exact?:??T/F??*/ )) {
            throw new IllegalArgumentException("Can't legally move to point '" + toPoint + "'");
        }
        if (fromPoint == toPoint) {
            return; /* dud, not doing anything */
        }

        // If the new space is empty, make the move
        // Else send the opponent to the bar first
        Player destBlotColor = getPlayerOnPoint(toPoint, playerColor); 
        // supplying color because might be bear-off
        if ((destBlotColor==playerColor ) || (destBlotColor==null/* means neutral*/)) {
            /* formerly used "selectedPointLoc" field which said where the blot in motion was 
             * started from. */
            // selectAPoint(  ....  ); // unnec if moveBlot makes sure to set selectedPointLoc OR know
            moveBlot(playerColor, fromPoint /* was myBoard.getOldPoint( )*/, toPoint, rollValue);
            if ( myGame.status.networked && (!myGame.status.observer) ) {
                if (this==Game.mainBoard) {
                    myGame.comm.sendMove(fromPoint /* was myBoard.getOldPoint()*/, toPoint);
                }
            }
        } else { 
            // send the opponent to the bar first
            moveToBar(toPoint, destBlotColor);
            moveBlot(playerColor, fromPoint /*was myBoard.getOldPoint()*/, toPoint, rollValue);
            if (myGame.status.networked) {
                if (this==Game.mainBoard) {
                    myGame.comm.sendOnBar(toPoint);
                    myGame.comm.sendMove(fromPoint /* was myBoard.getOldPoint()*/, toPoint);
                }
            }
        } // end if move-to is legit (our color or neutral)
        // Turn off focus on this point, perhaps redundantly but doesn't hurt to do multiple times
        setSelectedPointLoc(NO_MOVE_SELECTED,playerColor); //sets aPointIsSelected = false;

        // blanking the following and instead gonna make sure moveBlot (or its subsidiaries
        // including comeInFromBar and bearOff) does this. Can't do it twice since we would 
        // be saying "I used one of the 6s" and then saying it again, using up another.
        //myDice.setUsedValue(rollValue); oops, already setUsedValue in // don't have to know which die it is; 
        // note: ^ this might end the move if all dice moves are used up.
        if (this==Game.mainBoard) {
            myGame.endPartialMove(playerColor); // just some gui stuff, includes repaint( )
        }

        boolean switchedPlayers = true; // hypothetical, default starter value
        if (this==Game.mainBoard) { // should turn be ended, if hasn't already?
            switchedPlayers = (playerColor != myGame.getCurrentPlayer( ));
            if (!switchedPlayers) {
                if (myDice.allDiceAreUsed( )) {
                    myGame.endTurn(this,playerColor,/*msg:*/"");
                    switchedPlayers = true;
                }
            }
            // If this still wasn't the player's last move,
            // check if he is still on the bar or if he can make more moves
            // ?? Should the following happen only for mainBoard, or any hypothetical board?
            if ( ! switchedPlayers ) {
                if (onBar(playerColor )) {
                    selectABar(playerColor); // doesn't actually move any blots
                }
                if (!canMove(playerColor )) { // <-- exception here 2012mar28
                    //if (this==Game.mainBoard) {
                    myGame.forfeitTurn(this,playerColor); // calls endTurn
                    //}
                }
            }
        }
    } // doPartialMove( )

    /** 
     * Return whether the specified player can place a blot at a certain position.
     * This checks whether moves can bear off with inexact dice.
     * See "canLandOn(.. Exact )" which only accepts precise moves matching dice roll.
     * 
     * if they don't tell us whether we have to be exact, assume NOT
     */
    public /*static*/ boolean canLandOn(int pointNum, Player playerColor) {
        return canLandOn(pointNum, playerColor, /* exact?: */ false);
        //             if (pointNum == ILLEGAL_MOVE) {
        //                 return false;
        //             }
        //             // hmmm, if (onBar(playerColor)) and this isn't 
        //                // about a point leaving the bar then false ?? */
        //             if (! legitEndLoc(pointNum, playerColor)) { /* checks Player.legitColor */
        //                 return false; 
        //                 // Or?? throw new IllegalArgumentException("bad pointNum '" +pointNum+ "'");
        //             }
        // 
        //             if (canLandOnExact(pointNum, playerColor)) { 
        // allows BEAR_OFF_LOC, not PAST_BEAR_OFF
        //                 return true;
        //             } else if ((pointNum == playerColor.getBeyondBearOffLoc( )) {
        //                 if ( canBearOff(playerColor) && canBearOffWithLargerRolls(playerColor)) {
        //                     return true;
        //                 }
        //             }
        //             return false;
    } // canLandOn

    /** 
     * Return whether the specified player can place a blot at a certain position.
     * This is supposedly specifying the endpoint of a move.
     * See "canLandOn( )" which is like this but allows for bearing off with inexact dice rolls.
     *
     * Is this checking that nobody is still on the bar waiting to come in??
     * Hmmm, this might be actually checking whether the blot on the bar can come in here,
     * so don't be too negative about there being somebody on the bar!
     * was originally called "checkFair"
     */
    public /*static*/ boolean canLandOn(int pointNum, Player playerColor, boolean exact) {
        if (pointNum == ILLEGAL_MOVE) {
            return false;
        }

        // is legitEndLoc for !exact or for both?? should work for both, allows pastBear
        // MUST allow inexact checking of EndLoc here so that we can return a false
        // instead of an exception when tentatively venturing past the end!
        // There may be a way of splitting the cases for exact and not, 
        if (! legitEndLoc(pointNum,playerColor/*, exact?:false*/)) { /* checks Player.legitColor */
            // Or?? return false; 
            throw new IllegalArgumentException("bad pointNum '" + pointNum + "'");
        }

        if (!exact) {
            // note: next line is NOT recursion because line above noticed that we're
            // using inexact checking, but will try an exact check for quick bail out.
            if (canLandOn(pointNum, playerColor, /*exact?:*/ true)) { 
                // allows BEAR_OFF_LOC, not PAST_BEAR_OFF
                return true;
            } else if (pointNum == playerColor.getBeyondBearOffLoc( )) {
                if ( canBearOff(playerColor) && canBearOffWithLargerRolls(playerColor)) {
                    return true;
                }
            }
        } else { // exact...
            if (pointNum == playerColor.getBeyondBearOffLoc( )) {
                return false;
            }

            if (pointNum == playerColor.getBearOffLoc( )) {
                if ( canBearOff(playerColor)) {
                    return true;
                } // else returns false below
            } else if (1 == getHowManyBlotsOnPoint(pointNum/*, playerColor*/)) {
                // If there is only one blot of either color, the move is legal
                // so DON'T use the color specifying form of getHowManyBlotsOnPoint( )!
                return true;
            } else {
                // If the target point is empty or has the user's own blots, the move is legal
                Player pointColor = getPlayerOnPoint(pointNum, playerColor);
                if ((pointColor==neutral) || (pointColor==playerColor )) {
                    return true;
                }
            }
        } 
        return false;
    } // canLandOnExact

    /**
     * If they don't tell us otherwise, "canMove" assumes we're NOT being exact.
     */
    public boolean canMove( Player playerColor ) {
        return canMove( playerColor, /*exact?*/ false);
    }

    /**
     * With the current rolls, can the user move anywhere?
     * Beware: this calls "canBearOffWithLargerRolls()" which can either call 
     * this canMove() or does its equivalent?? looping through all points
     * This will call canMove( with exact true ), 
     * and if stuck then try canBearOff and canBearOffWithLargerRolls
     *
     * Notes for the exact version:
     * (Later we'll worry about whether inexact rolls are allowed.)
     * Beware: the broader "canMove" calls this and then calls "canBearOffWithLargerRolls()" 
     * if necessary.
     * Watch out for either of them calling this canMoveExact() or does its equivalent?? 
     * looping through all points.
     *
     * This used to have simple overflow/underflow move math:
     *  if (playerColor==white) { move1 = point + getDie(1); move2 = point + getDie(2);
     *  } else { move1 = point - getDie(1); move2 = point - getDie(2); }
     */ 
    public boolean canMove( Player playerColor, boolean exact ) {
        if (! playerColor.legitColor( )) {
            throw new IllegalArgumentException("Bad color '" + playerColor + "'");
        }
        if (myDice.allDiceAreUsed( )) { // quick denial.
            return false;
        }
        // ?? need total movesCountdown so we can use up PartialMoves !!
        // Cycle through all the points, exiting with true as soon as we find a moveable blot
        // Easier to start off looking at the pieces that haven't moved far,
        // cause if any of them can move then we don't have to worry about 
        // whether inexact dice are allowed.
        if (onBar(playerColor)) {
            ArrayList<PartialMove> legalPms = legalPartialMovesFromPoint(playerColor.getBarLoc( ),playerColor);
            // probably okay with bar
            if (legalPms.size( ) > 0) {
                return true; // have moves to get off bar!
            } else {
                return false; // stuck on bar  :(
            }
        } else {
            int start = playerColor.getLowPoint( );
            int end = playerColor.getHighPoint( );
            for (int point = start; playerColor.inBoardZone(point); 
            point = playerColor.nextPoint( point )) {
                if (blotsOnPointCanMove(point, playerColor, exact )) {
                    return true;
                }
            }
        }
        return false;
    } // canMove( Player, exactTF)

    /**
     * Called by canMove( ) for individual points.
     * Tells us whether moveable blots _of_the_specified_color_ are on the point.
     * Isn't this partially duplicated by selectAPoint( ) and allLegalPartialMoves( )
     * or legalPartialMovesFromPoint( )??
     * 
     * probably works for bar (uses endPointMovingFrom which is cool with bar)
     */
    private boolean blotsOnPointCanMove( int pointNum, Player playerColor, boolean exact ) {
        if (pointNum == ILLEGAL_MOVE) {
            return false;
        }
        // following was checking legitEndLoc, why??
        if (! legitStartLoc(pointNum,playerColor/*,exact*/)) { /* checks Player.legitColor */
            // Or?? return false; 
            throw new IllegalArgumentException("bad pointNum '" + pointNum + "'");
        }
        // Only check points which contain the player's blots
        if (getPlayerOnPoint(pointNum,playerColor) == playerColor ) {
            for (int die = 1; die <= Dice.howManyDice; die++) { 
                // dice are numbered 1,2,.. not 0
                if (!myDice.getUsedDie(die)) {
                    int move = endPointMovingFrom(pointNum, myDice.getDie(die), playerColor/*, this*/);
                    if (move == ILLEGAL_MOVE)  {
                        continue; // jumps to next iteration of for (point ) loop
                    }
                    if (canLandOn(move, playerColor, exact)) {
                        return true;
                    }// canLandOn() only allows bearing off with exact rolls.
                    // If the player has no other option, moving with a roll greater 
                    // than needed to bear off is legal.
                    // The following might be unnecessary if "canLandOn" and 
                    // "canLandOnExact" check it
                    if (move == playerColor.getBearOffLoc( )) { 
                        return true; 
                    }
                    /* new 2012 apr 26 addition: (!exact) */
                    if ( (!exact) && (canBearOffWithLargerRolls(playerColor)) 
                    && (move==playerColor.getBeyondBearOffLoc( ))) {
                        return true;
                    }
                } // if die is available
            } // for
        } // if there are blot(s) on the point

        return false;
    } /* blotsOnPointCanMove( ) */

    /** 
     * Gives locations of moveable blots. Doesn't say how many are at each loc.
     * If I have blots on the bar, they are the only moveables!
     * Beware: black moves in negative direction
     * and old version of game coded 0 as black wanna-bear-off and 25 as white wanna-bear-off.
     * Shouldn't this encode HOW MANY moveable blots are at that location?
     * 
     * Beware?? This marks as moveable ANY blot, even if it has a wall of barricades
     */
    public LocList allMoveableBlotLocs( Player playerColor /*, Game myGame *//*, int moveDist*/ ) {
        if ( ! playerColor.legitColor( ) ) {
            throw new IllegalArgumentException("bad color '" + playerColor + "'");
        }
        LocList myMovers = new LocList( );
        if ( getBar( playerColor ) >= 1) {
            myMovers.myList.add(new Integer( playerColor.getBarLoc( ) ));
            return myMovers;
            //if (canOnlyMoveFromBar( playerColor )) {
            /* only thing we can do is move in from bar, so how to show that in a list: 
            with a code number? Or a "PointLoc" class? */
            //}
        }

        // Cycle through all the points
        for (int point = 1; point <= howManyPoints; point++) {
            // Only check points which contain the player's blots
            if (getPlayerOnPoint(point,playerColor) == playerColor) {
                myMovers.myList.add(new Integer(point));
            }
        }
        return myMovers;
    } // allMoveableBlotLocs( )

    /**
     * With doubles we can possibly move 3 blots in from bar and still have a 4th blot to move,
     * and without doubles then we can move 1 blot in from bar and still have a move left.
     * This says whether we're stuck moving ONLY blots from the bar.
     * used by "allMoveableBlotLocs( )"
     */
    private boolean canOnlyMoveFromBar( Player playerColor ) {
        if ( ! playerColor.legitColor( ) ) {
            throw new IllegalArgumentException("bad color '" + playerColor + "'");
        }

        if (myDice.getDie(1) == myDice.getDie(2)) { /* doubles! */
            return (/*myBoard.*/getBar( playerColor ) > 3);
        } else {
            return (/*myBoard.*/getBar( playerColor ) > 1);
        }
    } /* canOnlyMoveFromBar( ) */

    /**
     * For comparing just the blot locations on two boards.
     * Doesn't care about similarity of "Game", "currentPlayer", dice, nor selected points.
     * Is used by equals( ), which may or may not care about those other details.
     */  
    public boolean sameBlotLocations( Board other ) {
        // int howManyOnPoint[ ]; /* just for board points (1..24), Beware: numbered 1..24, NOT 0..23! */
        // Player whichPlayerOnPoint[ ];

        for (int i = 0; i<= howManyPoints; ++i) { // yes, "<="
            if (howManyOnPoint[i] != other.howManyOnPoint[i]) {
                return false; /* note, we're not actually using howManyOnPoint[0] */
            }
            if (whichPlayerOnPoint[i] != other.whichPlayerOnPoint[i]) {
                return false; /* note, we're not actually using howManyOnPoint[0] */
            }
        }
        // int bar[ ]; // bar[0] is nothing, bar[1] is # of blots on white bar, bar[2]=black_bar;
        // int bear[ ]; // bear[0] is nothing, bear[1] is # of blots beared off by white, bear[2]=black_bear;
        for (int i = 0; i<= Game.howManyColors; ++i) {
            if ((bar[i] != other.bar[i]) || (bear[i] != other.bear[i])) {
                return false;
            }
        }
        return true;
    } // sameBlotLocations( )

    /**
     * For comparing the layout of two boards.
     * Doesn't care whether they're from the same "Game".
     * Doesn't currently care about whether "currentPlayer" is the same?? Should it?
     * Does not care about whether dice are the same, nor whether a point is selected.
     */
    public boolean equals(Object other)    {
        if (!(other instanceof Board)) { /* takes care of null! */
            return false; 
        } else if (! sameBlotLocations( (Board)other )) {
            return false;
        }
        // Field list...
        // Dice myDice; /* irrelevant? */
        // Game myGame = null; /* irrelevant for similarity? */
        // int howManyOnPoint[howManyPoints + 1 ]; 
        /* ^ Just for board points (1..24),  Beware: numbered 1..24, NOT 0..23! */
        // Player whichPlayerOnPoint[howManyPoints + 1 ];
        // int bar[howManyColors + 1]; 
        //   ^  bar[0] is nothing, bar[1] is # of blots on white bar, bar[2]=black_bar;
        // int bear[howManyColors + 1]; 
        //   ^  bear[0] is nothing, bear[1] is # of blots beared off by white, bear[2]=black_bear;

        // int selectedPointLoc; /* irrelevant? */
        // boolean aPointIsSelected; /* irrelevant? */
        // int potDest1; /* irrelevant? */
        // int potDest2; /* irrelevant? */
        return true;
    }

    /**
     * for toString, related to "sameBlotLocations" used by equals( )
     */
    public String blotLocationsStr(int maxCharsOnLine) {
        StringBuffer tempStrBuf = new StringBuffer( );

        tempStrBuf.append("Points: ");
        int charsSoFar = 8;
        int pointStrSize = 12; // "p7:3black, " counting all pointnumbers 1..24 as double digit
        for (int p = 1 /* yes, 1*/; p<=howManyPoints; ++p) {
            if (howManyOnPoint[p] > 0) {
                if ((charsSoFar + pointStrSize) > maxCharsOnLine) { // won't fit
                    tempStrBuf.append("\n");
                    charsSoFar = 0;
                }
                tempStrBuf.append("p" + p + ":" + howManyOnPoint[p] + whichPlayerOnPoint[p] + ", ");
                charsSoFar += pointStrSize; 
            }
        }
        int size = tempStrBuf.length( );
        tempStrBuf.delete(/*start*/size-2, /*after end*/size); // remove the last comma & space
        tempStrBuf.append("\n");
        // int bar[howManyColors + 1]; 
        //   ^  bar[0] is nothing, bar[1] is # of blots on white bar, bar[2]=black_bar;
        // int bear[howManyColors + 1]; 
        //   ^  bear[0] is nothing, bear[1] is # of blots beared off by white, bear[2]=black_bear;
        for (int h = 1 /*yes,1*/; h <= Game.howManyColors; ++h) {
            tempStrBuf.append("[" + Player.colorName(h) + " bar:" + bar[h] + ", bearedOff:" + bear[h] + "]");
        }
        return tempStrBuf.toString( );
    }

    public String toString( ) {
        StringBuffer tempStrBuf = new StringBuffer( );
        tempStrBuf.append("<board>Dice:" + myDice + "\n");
        // tempStrBuf.append("Game:" + myGame + "\n"); /* might have name someday */
        // int howManyOnPoint[howManyPoints + 1 ];
        /* ^ Just for board points (1..24),  Beware: numbered 1..24, NOT 0..23! */
        // Player whichPlayerOnPoint[howManyPoints + 1 ];
        tempStrBuf.append(blotLocationsStr(/*maxCharsOnLine:*/ 70 ));
        tempStrBuf.append("\n");
        // int selectedPointLoc; /* irrelevant? */
        // boolean aPointIsSelected; /* irrelevant? */
        if (aPointIsSelected) {
            tempStrBuf.append("selectedPointLoc:" + selectedPointLoc + ",");
            // int potDest1; /* irrelevant? */
            // int potDest2; /* irrelevant? */
            for (int p = 1; p <= howManyDice; ++p) {
                tempStrBuf.append("PotDest" + p + ":" + getPotDest(p) + ",");
            }
            int size = tempStrBuf.length( );
            tempStrBuf.delete(/*start*/size-1, /*after end*/size); // remove the last comma
        } else {
            tempStrBuf.append("no point selected"); 
        }
        // tempStrBuf.append("latest move:"+latestMove);
        tempStrBuf.append("</board>");
        return tempStrBuf.toString( );
    }
    // class Board 
}