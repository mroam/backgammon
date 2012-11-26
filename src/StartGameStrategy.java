import java.util.*;  // provides Collection ?

/**
 * StartGameStrategy is hard-wired with "best" start moves
 * (source: "Mathematics of Games & Gambling" 
 * chapter "Backgammon and other dice Diversions" pg.29)
 * 
 * Best early moves, if possible. 
 * (Dice roll: followed by black's moves (moving from 24 to 1)
 * 
 * 1,1: (8->7)2x, (6->5)2x
 * 1,2: 6->5, 13->11
 * 1,3: 6->5, 8->5
 * 1,4: 6->5, 13->9
 * 1,5: 24->23, 23->18
 * 1,6: 8->7, 13->7
 * 
 * 2,2: (24->22)2x, (22->20)2x
 * 2,3: 13->11, 13->10
 * 2,4: 6->4, 8->4
 * 2,5: 13->11, 13->8
 * 2,6: 13->11, 24->18
 * 
 * 3,3: (13->10)2x, (10->7)2x
 * 3,4: 13->10, 13->9
 * 3,5: 6->3, 8->3
 * 3,6: 13->10, 24->18
 * 
 * 4,4: (13->9)2x, (24->20)2x
 * 4,5: 13->9, 19->8
 * 4,6: 24->18, 18->14 
 * 
 * 5,5: (13->8)2x, (8->3)2x
 * 5,6  24->18,18->13
 * 
 * 6,6: (13->7)2x, (24->18)2x
 * 
 * Note: while constructing moves we assume the small move will
 * happen first unless specified (with final boolean) that big
 * move should happen first.
 * 
 * @author J & M & J 
 * @version 2012 Mar 11
 */
public class StartGameStrategy extends Strategy
{
    // instance variables: note, board & player get set up in pickBestMove( )
    private Player currPlayer = null /* means neutral */;
    private Board currBoard = null;
    private Dice currDice = null;
    boolean dice1isLow = true;
    int lowDice = 0;
    int highDice = 0;

    /**
     * Constructor for objects of class StartGame
     */
    public StartGameStrategy(/* Player newCurrPlayer, Board newCurrBoard*/) {

    }

    /**
     * @param  currentBoard is ideally ready for start of move, isn't partially moved already
     * @return     Move is a collection of partial moves
     */
    public Move pickBestMove(Board aBoard, Player playerColor) {
        if (!Board.legitColor(playerColor.getColor( ))) {
            throw new IllegalArgumentException("StartGame.pickBestMove( ) got bad playerColor '" + playerColor + "'");
        } else {
            currPlayer = playerColor;
        }
        if (aBoard == null) {
            throw new NullPointerException("StartGame.pickBestMove( ) can't analyze null board.");
        } else {
            currBoard = aBoard;
        }

        Move bestMove = null;

        currDice = currBoard.getMyDice( ); // will be clone, so we can't hurt it
        lowDice = currDice.getDie1( );
        highDice = currDice.getDie2( );

        if (aBoard.onBar(playerColor)) { // have to deal with coming in from bar!
            bestMove = pickBarMove( aBoard,playerColor ); 
        } else {

            if (lowDice > highDice) {
                dice1isLow = false;
                lowDice = currDice.getDie2( );
                highDice = currDice.getDie1( );
            }

            switch(lowDice) {
                case  1: bestMove = lowDice1(); break;
                case  2: bestMove = lowDice2(); break;
                case  3: bestMove = lowDice3(); break;
                case  4: bestMove = lowDice4(); break;
                case  5: bestMove = lowDice5(); break;
                case  6: bestMove = lowDice6(); break;
                default: 
                throw new IllegalArgumentException("StartGame.pickBestMove( ) only knows dice values 1..6!");
            }
        } // on bar or not

        return bestMove;
    }

    /**
     * This will calculate the starting moves for dice 6 & anything.
     */
    public Move lowDice6() {
        switch(highDice) {
            case  6: 
            return dealWithDoubles(lowDice);
            default: 
            throw new IllegalArgumentException("StartGame.pickBestMove( ) only knows dice values"
                + " 1..6! not '" + highDice + "'");
        }
    }

    /**
     * This will calculate the starting moves for dice 5 & anything.
     */
    public Move lowDice5() {
       switch(highDice) {
            case  5: 
            return dealWithDoubles(lowDice);
            case  6: 
            // hmmm, the low move should be the 5, but we care about the ORDER of the moves!!
            // if (currentPlayer == Game.blackP) {
            return new Move(/*start1*/24,/*end1*/18,/*rollVal1*/6,  /*start2*/18,/*end2*/13,/*rollVal2*/5, 
            currBoard,currPlayer); // making high rollval move first!
            // } else { return new Move(1,7,6,  7,12,5, currBoard,currPlayer); }
            default: 
            throw new IllegalArgumentException("StartGame.pickBestMove( ) only knows dice values"
                + " 1..6! not '" + highDice + "'");
        }
    }

    /**
     * This will calculate the starting moves for dice 4 & anything.
     */
    public Move lowDice4() {
        switch(highDice) {
            case  4: 
            return dealWithDoubles(lowDice);
            case  5: 
            return new Move(/*start1*/13,/*end1*/9,/*rollVal1*/4,  /*start2*/13,/*end2*/8,/*rollVal2*/5, 
            currBoard,currPlayer);
            //return buildMove(/*startLow*/13,/*endLow*/9,  /*startHigh*/13,/*endHigh*/8);
            case  6: 
            return new Move(24,18,6,      18,14, 4,  currBoard,currPlayer); // make high rollval move first!
            //return buildMove(/*startLow*/18,/*endLow*/14,  /*startHigh*/24,/*endHigh*/18, /*makeTheHighMoveFirst */true);
            default: 
            throw new IllegalArgumentException("StartGame.pickBestMove( ) only knows dice values"
                + " 1..6! not '" + highDice + "'");
        }
    }

    /**
     * This will calculate the starting moves for dice 3 & anything.
     */
    public Move lowDice3() {
        switch(highDice) {
            case  3: 
            return dealWithDoubles(lowDice);
            case  4: 
            return new Move(/*start1*/13,/*end1*/10,/*rollVal1*/3,  /*start2*/13,/*end2*/9,/*rollVal2*/4, 
            currBoard,currPlayer);
            //return buildMove(/*startLow*/13,/*endLow*/10,  /*startHigh*/13,/*endHigh*/9);
            case  5: 
            return new Move(6,3, 3,    8,3, 5, currBoard,currPlayer);
            //return buildMove(/*startLow*/6,/*endLow*/3,  /*startHigh*/8,/*endHigh*/3);
            case  6: 
            return new Move(13,10, 3,    24,18, 6, currBoard,currPlayer);
            //return buildMove(/*startLow*/13,/*endLow*/10,  /*startHigh*/24,/*endHigh*/18);
            default: 
            throw new IllegalArgumentException("StartGame.pickBestMove( ) only knows dice values"
                + " 1..6! not '" + highDice + "'");
        }
    }

    /**
     * This will calculate the starting moves for dice 2 & anything.
     */
    public Move lowDice2() {
        switch(highDice) {
            case  2:  
            return dealWithDoubles(lowDice);
            case  3:
            return new Move(/*start1*/13,/*end1*/11,/*rollVal1*/2,  /*start2*/13,/*end2*/10,/*rollVal2*/3, currBoard,currPlayer);
            //return buildMove(/*startLow*/13,/*endLow*/11,  /*startHigh*/13,/*endHigh*/10);
            case  4: 
            return new Move(6,4, 2,      8,4, 4, currBoard,currPlayer);
            //return buildMove(/*startLow*/6,/*endLow*/4,  /*startHigh*/8,/*endHigh*/4);
            case  5: 
            return new Move(13,11, 2,     13,8, 5, currBoard,currPlayer);
            //return buildMove(/*startLow*/13,/*endLow*/11,  /*startHigh*/13,/*endHigh*/8);
            case  6: 
            return new Move(13,11, 2,     24,18, 6, currBoard,currPlayer);
            //return buildMove(/*startLow*/13,/*endLow*/11,  /*startHigh*/24,/*endHigh*/18);
            default: 
            throw new IllegalArgumentException("StartGame.pickBestMove( ) only knows dice values"
                + " 1..6! not '" + highDice + "'");
        }
    }

    /**
     * This will calculate the starting moves for dice 1 & anything.
     */
    public Move lowDice1() {
        switch(highDice) {
            case  1:
            return dealWithDoubles(lowDice);
            case  2: //suppose the best move for 1,2 is point5 moves 1 to 4, point6 moves 2 to 4
            return new Move(/*start1*/6,/*end1*/5,/*rollVal1*/1,  /*start2*/13,/*end2*/11,/*rollVal2*/2, currBoard,currPlayer);
            //return buildMove(/*startLow*/6,/*endLow*/5,  /*start2*/13,/*end2*/11);
            case  3:
            return new Move(6,5, 1,      8,5, 3, currBoard,currPlayer);
            //return buildMove(/*startLow*/6,/*endLow*/5,  /*start2*/8,/*end2*/5);
            case  4:
            return new Move(6,5, 1,      13,9, 4, currBoard,currPlayer);
            //return buildMove(/*startLow*/6,/*endLow*/5,  /*start2*/13,/*end2*/9);
            case  5:
            return new Move(24,23, 1,      23,18, 5, currBoard,currPlayer);
            //return buildMove(/*startLow*/24,/*endLow*/23,  /*start2*/23,/*end2*/18);
            case  6:
            return new Move(8,7, 1,      13,7, 6, currBoard,currPlayer);
            //return buildMove(/*startLow*/8,/*endLow*/7,  /*start2*/13,/*end2*/7);
            default:
            throw new IllegalArgumentException("StartGame.pickBestMove( ) only knows dice values"
                + " 1..6! not '" + highDice + "'");
        }
    }

    /**
     * This will happen if order of small die move and then large die move.
     * It it HAS to move in the reverse order, call this with boolean makeTheHighMoveFirst = true.
     */
    public Move oldBuildMove(int startLow, int endLow, int startHigh, int endHigh) {
        return oldBuildMove(startLow, endLow, startHigh, endHigh,/*makeTheHighMoveFirst */ false);
    }

    /**
     * The order of these moves might matter!
     * This could move into Class Move as a convenience class if dice1isLow were a param OR
     * if this received rollVals instead of hardwiring die numbers 1 & 2 in the first 8 lines.
     * 
     * This is relatively clunky and unnecessary now that I have a new Move Constructor
     * which receives (start1,end1,rollVal1,  start2,end2,rollVal2, aBoard, playerColor);
     */
    public Move oldBuildMove(int startLow, int endLow, int startHigh, int endHigh, boolean makeTheHighMoveFirst ) {
        Move bestMove = null;        
        PartialMove pm0 = null;
        PartialMove pm1 = null;

        pm0 = new PartialMove( startLow, lowDice, endLow, currBoard, currPlayer);
        pm1 = new PartialMove( startHigh, highDice, endHigh, currBoard, currPlayer);
        //          if (dice1isLow) { // 1,2
        //             pm0 = new PartialMove( startLow, lowDice, endLow, currBoard, currPlayer, /*whichDie:*/1);
        //             pm1 = new PartialMove( startHigh, highDice, endHigh, currBoard, currPlayer, /*whichDie:*/2);
        //         } else { // 2,1
        //             pm0 = new PartialMove( startLow, lowDice, endLow, currBoard, currPlayer, /*whichDie:*/2); //<--2!!
        //             pm1 = new PartialMove( startHigh, highDice, endHigh, currBoard, currPlayer, /*whichDie:*/1);
        //         }

        ArrayList<PartialMove> theNewPartials = new ArrayList<PartialMove>( );
        if (makeTheHighMoveFirst) {
            theNewPartials.add(pm1);
            theNewPartials.add(pm0);
        } else {
            theNewPartials.add(pm0);
            theNewPartials.add(pm1);
        }
        try {
            bestMove = new Move(theNewPartials, currPlayer/*,currBoard*/ );
            //Move(ArrayList<PartialMove> theNewPartials, int myNewColor, Board myNewStarterBoard /*Final?*/)
        } catch(Exception e) {
            throw new IllegalArgumentException(e);
        }
        return bestMove;
    }

    /**
     * Only six possible doubles to deal with.
     * Note: starting board has whites on point 1 (2 of 'em), 12 (5), 17 (3), 19 (5)
     * blacks on points 6 (5), 8 (3), 13 (5), 24 (2)
     * 
     * This is rigged to only deal with black.
     */
    Move dealWithDoubles(int theRoll) {
        switch(theRoll) {
            case  1: 
            // gonna move 2 blots from (black's) 8 to 7, & 2 blots from 6 to 5
            //  note: the way our board is numbered, 
            // for white this will be 2 from 17 to 18 and 2 from 19 to 20
            return move2And2(/*start1*/8,/*end1*/7,/*rollVal1*/1,  /*start2*/6,/*end2*/5, /*rollVal2*/1);

            case 2:  // bug? was coded with both go 1->3 and then from 3->5
            return move2And2(/*start1*/24,/*end1*/22,/*rollVal1*/2, /*start2*/22,/*end2*/20, /*rollVal2*/2);

            case 3:
            return move2And2(/*start1*/13,/*end1*/10,/*rollVal1*/3, /*start2*/10,/*end2*/7, /*rollVal2*/3);

            case 4: 
            return move2And2(/*start1*/13,/*end1*/9,/*rollVal1*/4, /*start2*/24,/*end2*/20, /*rollVal2*/4);

            case 5:
            return move2And2(/*start1*/13,/*end1*/8,/*rollVal1*/5, /*start2*/8,/*end2*/3, /*rollVal2*/5);

            case 6:
            return move2And2(/*start1*/13,/*end1*/7,/*rollVal1*/6, /*start2*/24,/*end2*/18, /*rollVal2*/6);

            default:
            throw new IllegalArgumentException("Bad dice value '" + theRoll + "', should be 1..6!!!!");
        }
    }

    /**
     * This was briefly called "dealWithDoublesOf1" but now is
     * more general purpose for dealing with all doubles. Assumes
     * you're moving two blots from (point) start1 to end1, and moving 
     * another 2 blots from start2 to end2.
     * 
     * This could probably be put into Move class as a convenience method.
     */
    Move move2And2(int start1, int end1, int rollVal1,  int start2, int end2, int rollVal2) {
        Move bestMove = null;
        //PartialMove Constructor takes // PM(start, rollVal, end, Board, Player, whichDie)
        PartialMove pm0 = new PartialMove( start1, currBoard.getMyDice( ).getDie(1), end1, currBoard, currPlayer/*, 1*/);
        PartialMove pm1 = new PartialMove( start1, currBoard.getMyDice( ).getDie(1), end1, currBoard, currPlayer/*, 1*/);
        PartialMove pm2 = new PartialMove( start2, currBoard.getMyDice( ).getDie(2), end2, currBoard, currPlayer/*, 2*/);
        PartialMove pm3 = new PartialMove( start2, currBoard.getMyDice( ).getDie(2), end2, currBoard, currPlayer/*, 2*/);

        ArrayList<PartialMove> theNewPartials = new ArrayList<PartialMove>( );
        theNewPartials.add(pm0);
        theNewPartials.add(pm1);
        theNewPartials.add(pm2);
        theNewPartials.add(pm3);
        try {
            bestMove = new Move(theNewPartials, currPlayer/*, currBoard*/ );
            //Move(ArrayList<PartialMove> theNewPartials, int myNewColor, Board myNewStarterBoard /*Final?*/)
        } catch(Exception e) {
            throw new IllegalArgumentException(e);
        }
        return bestMove;
    } /* move2And2( ) */
    
    public String toString( ) {
        return "StartGameStrategy";
    }
} /* class StartGameStrategy */
