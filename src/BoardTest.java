
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The test class BoardTest.
 *
 * @author  (Mike Roam)
 * @version (2012 Jan, Feb)
 */
public class BoardTest {

    Game g;
    Board b;
    Player aiColor = Game.blackP; /* playerColor */

    /**
     * Default constructor for test class BoardTest
     */
    public BoardTest() {
    }

    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    @Before
    public void setUp() {
        g = new Game(/*networked*/false,/*testing*/true);
    }

    /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    @After
    public void tearDown() {
    }

    @Test//(expected=IllegalArgumentException.class)
    /**
     * the gui board shows up to two potential moves after a point is clicked
     * numbered 1 & 2 corresponding to which die has the rollValue that gets there.
     * We're not keeping track of dieNum anymore so trying to determine by rollVal
     * which die we're talking about
     */
    public void testWhichPotMove2( ) {
        b = g.getMyBoard();
        b.myDice.roll(5,3);
        //try { // nonexistent die
        int whatever = b.whichPotDest( /*rollValue:*/6 );
        // System.err.println("whatever should be -9 (Dice.NO_SUCH_DIE) and is " + whatever);
        assertEquals(Dice.NO_SUCH_DIE,whatever);
        //             ^ should throw IllegalArgumentException
        // Oops, actually returns -9  (Dice.NO_SUCH_DIE)
        //     assertEquals(1, b.whichPotDest( /*rollValue:*/6 ));
        //} catch(Exception e) {

    }

    @Test
    /**
     * the gui board shows up to two potential moves after a point is clicked
     * numbered 1 & 2 corresponding to which die has the rollValue that gets there.
     * We're not keeping track of dieNum anymore so trying to determine by rollVal
     * which die we're talking about
     */
    public void testWhichPotMove( ) {

        b = g.getMyBoard();
        b.myDice.roll(5,3);
        assertEquals(1, b.whichPotDest( /*rollValue:*/5 ));
        assertEquals(2, b.whichPotDest( /*rollValue:*/3 ));
        assertEquals(Dice.NO_SUCH_DIE, b.whichPotDest( /*rollValue:*/6 ));
        //try { // nonexistent die: tested in WhichPotMove2
        //    assertEquals(1, b.whichPotDest( /*rollValue:*/5 ));
        //} catch(Exception e) {  }
        b.myDice.roll(6,6);
        assertEquals(1, b.whichPotDest( /* rollValue:*/6 ));
    }

    @Test
    /**
     * first test of Board.bestLegalMove( )
     * don't have to test for coming in from bar because Strategies know that
     * they have to get in from bar... but ?? maybe bestLegalMove should
     * know about bar??
     */
    public void testBestLegalMove1( ) {
        b = g.getMyBoard();
        try {
            /* black on 14 & 12 heading for 0; white on 4 (has long way to go)*/
            b.make3BlotGame2( );
            assertNotNull(b);
        } catch(Exception e) { /* isn't there a way to test without catching excetions? */
            fail(e.toString( ));
        }
        g.setCurrentPlayer(aiColor);
        b.myDice.roll(4,2); 
        /* Hoping to contrive an obvious "best" pointbuilding move of 14->10 & 12->10.
         * Then might move white close enough so that the obvious best move -- depending
         * upon the value of putting opponent on bar -- will instead be to send one
         * black blot on a long trip to hit that white blot.
         */
        PointBuildScorer pbs = new PointBuildScorer( /* cautiousness:0.5?*/ );
        Move/*WithScore*/ best = b.bestLegalMove(/*Scorer*/pbs, /*partialsSoFar*/ null, aiColor );
        // compare it to hand built move. Is my board.equals( ) ready for primetime
        // as does Move.equals work when comparing Moves that have same result but
        // different order of partials?
        Move builtByHand = new Move(/*start1:*/12,/*end1:*/10,/*rollVal1:*/2,
                /*start2:*/14,/*end2:*/10,/*rollVal2:*/4, b, aiColor);
        assertEquals(builtByHand,best);
        b.doMove(best,aiColor);
        System.err.println("we should test more complicated boards for bestLegalMove!");
    }

    @Test
    /**
     * Testing the ability to detect "can Bear Off" with or without needing to use
     * inexact (overly large) dice moves.
     */
    public void testFarthestDice( ) {
        b = g.getMyBoard();
        assertNotNull(b);
        try {
            b.makeAlmostDoneGame( );
            /* black just has singles in quadrant 4, on points 1,4,6 (bearing off to 0) */
            b.takeOneBlotOffPoint(6);
            b.setNumOfBlotsOnPoint(7,1,aiColor);
            //move the piece on 6 to 7 so bearoff is illegal, for testing
            assertNotNull(b);
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }
        g.setCurrentPlayer(aiColor);
        b.myDice.roll(5,6 );
        assertFalse(b.canBearOff(aiColor));
        assertFalse(b.allUnusedDiceAreBiggerThanBearOffMoves(aiColor ));

        //move the piece on 7 to 6 where it needs 6 to go off, so bearoff is legal now
        b.takeOneBlotOffPoint(7);
        b.setNumOfBlotsOnPoint(6,1,aiColor);
        assertTrue(b.canBearOff(aiColor));
        assertFalse(b.allUnusedDiceAreBiggerThanBearOffMoves(aiColor ));
        assertFalse(b.canBearOffWithLargerRolls(aiColor));

        b.takeOneBlotOffPoint(6);
        b.setNumOfBlotsOnPoint(2,1,aiColor); // so now blots are on 4,2,1
        assertTrue(b.canBearOff(aiColor));
        assertTrue(b.canBearOff(aiColor));
        assertTrue(b.allUnusedDiceAreBiggerThanBearOffMoves(aiColor ));
        assertTrue(b.canBearOffWithLargerRolls(aiColor));

        // now test for white. Current board has 3 white on 18 (final quadrant is 19..24)
        // and 3 white on 19, and 2 white on 24 (final point).
        g.setCurrentPlayer(Board.white);
        b.myDice.roll(5,6); // die1 rollValue==5, die2 rollValue==6

        assertFalse(b.canBearOff(Board.white));
        assertFalse(b.allUnusedDiceAreBiggerThanBearOffMoves(Board.white ));

        //move the pieces on 18 to 19 where they need 6 to go off, so bearoff is legal now
        b.setNumOfBlotsOnPoint(18,0,Board.white);
        b.setNumOfBlotsOnPoint(19,6,Board.white);
        assertTrue(b.canBearOff(Board.white));
        assertFalse(b.allUnusedDiceAreBiggerThanBearOffMoves(Board.white ));
        assertFalse(b.canBearOffWithLargerRolls(Board.white));

        //move the pieces on 19 to 22 where they need 3 to go off, so inexact legal now
        b.setNumOfBlotsOnPoint(19,0,Board.white);
        b.setNumOfBlotsOnPoint(22,6,Board.white); // so now blots are on 20 and 24
        assertTrue(b.canBearOff(Board.white));
        assertTrue(b.allUnusedDiceAreBiggerThanBearOffMoves(Board.white ));
        assertTrue(b.canBearOffWithLargerRolls(Board.white));
        // now test doubles??
        b.myDice.roll(6,6); // die1 rollValue==6, die2 rollValue==6
        assertTrue(b.canBearOff(Board.white));
        assertTrue(b.allUnusedDiceAreBiggerThanBearOffMoves(Board.white ));
        assertTrue(b.canBearOffWithLargerRolls(Board.white));

        // now test non doubles again
        b.myDice.roll(5,6); // die1 rollValue==5, die2 rollValue==6
        assertTrue(b.canBearOff(Board.white));
        assertTrue(b.allUnusedDiceAreBiggerThanBearOffMoves(Board.white ));
        assertTrue(b.canBearOffWithLargerRolls(Board.white));
    }

    @Test
    public void testCanMoveExact(){

        b = g.getMyBoard();
        assertNotNull(b);
        try {
            b.make3BlotGame( ); /* black on 20 & 12 ends at 0, white on 4 ends past 24 */
            assertNotNull(b);
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }

        g.setCurrentPlayer(aiColor);
        b.myDice.roll(5,6 ); // die1 rollValue==5, die2 rollValue==6
        assertTrue(b.solitaryBlotOnPoint(12, aiColor));
        assertTrue(b.canLandOn(12, aiColor));
        assertTrue( b.canMove(aiColor, /*exact?*/ true));
    }

    @Test
    public void testCanMove(){

        b = g.getMyBoard();
        assertNotNull(b);
        try {
            b.make3BlotGame( ); /* black on 20 & 12 ends at 0, white on 4 ends past 24 */
            assertNotNull(b);
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }

        g.setCurrentPlayer(aiColor);
        b.myDice.roll( );  // get random values
        assertTrue(b.solitaryBlotOnPoint(12, aiColor));
        assertTrue(b.canLandOn(12, aiColor));
        assertTrue( b.canMove(aiColor));
    }

    @Test
    /**
     * testing ai points in mid board
     */
    public void testSelectAPoint1()  {

        b = g.getMyBoard();
        assertNotNull(b);
        try {
            b.make3BlotGame( );/* black on 20 & 12 ends at 0, white on 4 ends past 24 */
            assertNotNull(b);
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }

        g.setCurrentPlayer(aiColor);
        b.myDice.setDie(/*whichDie:*/1,/*rollValue:*/1); 
        b.myDice.setDie(/*whichDie:*/2,/*rollValue:*/6);
        // alternative syntax: b1.myDice.roll(1,6); // die1 rollValue==1, die2 rollValue==6

        assertTrue( b.solitaryBlotOnPoint(12, aiColor));
        assertTrue( b.canLandOn(12, aiColor, /*exact?:*/true));
        assertTrue( b.canLandOn(12, aiColor, /*exact?:*/false));
        assertTrue( b.canLandOn(12, aiColor/*, exact?:false*/)); 
        assertTrue( b.canMove(aiColor));
        assertEquals(1, b.getHowManyBlotsOnPoint(12));
        b.selectAPoint(12, aiColor);
        assertEquals(11, b.getPotDest(1));
        assertEquals(6, b.getPotDest(2));
        b.doPartialMove(12,11,/*whichDie:1*/ /*rollValue:*/1,aiColor);
        assertTrue( b.solitaryBlotOnPoint(11, aiColor));
        b.selectAPoint(11, aiColor);
        b.doPartialMove(20,14,/*whichDie:2*/ /*rollValue:*/6,aiColor);
        assertTrue( b.solitaryBlotOnPoint(11, aiColor));
        assertTrue( b.solitaryBlotOnPoint(14, aiColor));
    }

    @Test
    /**
     * testing ai points in end game. testSelectAPoint3 is identical except
     * it tries to use the "BearOff( )" command instead of doPartialMove( )
     */
    public void testSelectAPoint2()  {

        b = g.getMyBoard();
        assertNotNull(b);
        try {
            b.makeAlmostDoneGame( );
            /* black just has singles in quadrant 4, on points 1,4,6 (bearing off to 0) */
            assertNotNull(b);
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }

        g.setCurrentPlayer(aiColor);
        b.myDice.roll(5,6); // die1 rollValue==5, die2 rollValue==6
        // alternative syntax:
        // b.myDice.setDie(/*whichDie:*/1,/*rollValue:*/5);
        // b.setDie(/*whichDie:*/2,/*rollValue:*/6);
        assertTrue( b.solitaryBlotOnPoint(6, aiColor));
        assertTrue( b.canLandOn(6, aiColor,/*exact?:*/true));
        assertTrue( b.canLandOn(6, aiColor));
        assertTrue( b.canMove(aiColor));
        assertEquals(1, b.getHowManyBlotsOnPoint(6));
        b.selectAPoint(6, aiColor);
        assertEquals(1, b.getPotDest(1));
        assertEquals(Board.black.getBearOffLoc(), b.getPotDest(2));
        // was interesting bug: moving from point 6 to 6 caused another blot to appear
        int bboff = aiColor.getBearOffLoc( );
        b.doPartialMove(6,bboff,/*whichDie:2*/ /*rollValue*/6, aiColor);

        // so now the blot has beared off from 6, right?
        assertEquals(13, b.bear[Player.BLACK]);

        assertFalse(b.solitaryBlotOnPoint(6, aiColor));
        assertTrue(b.canBearOffWithLargerRolls(aiColor)); // <-- is this miscalculating??
        b.selectAPoint(4, aiColor);
        //b.bearOff( aiColor ); // <--this worked
        //ah-hah, doPartialMove( to bearOff ) might not be as smart as bearOff
        //and forget about doPartialMove( to beyondBearOff ) ??
        b.doPartialMove(4,bboff,/*whichDie:1*/ /*rollValue:*/5,aiColor);
        assertEquals(0, b.getHowManyBlotsOnPoint(4));
        assertEquals(14, b.bear[Player.BLACK]);
    }

    @Test
    /**
     * testing ai points in end game. testSelectAPoint3 is identical except
     * it tries to use the "BearOff( )" command instead of doPartialMove( )
     */
    public void testSelectAPoint3()  {

        b = g.getMyBoard();
        assertNotNull(b);
        try {
            b.makeAlmostDoneGame( );
            /* black just has singles in quadrant 4, on points 1,4,6 (bearing off to 0) */
            assertNotNull(b);
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }

        g.setCurrentPlayer(aiColor);
        b.myDice.roll(5,6); // die1 rollValue==5, die2 rollValue==6
        // alternative syntax:
        // b1.myDice.setDie(/*whichDie:*/1,/*rollValue:*/5);
        //setDie(/*whichDie:*/2,/*rollValue:*/6);
        assertTrue( b.solitaryBlotOnPoint(6, aiColor));
        assertTrue( b.canLandOn(6, aiColor,/*exact?:*/true));
        assertTrue( b.canLandOn(6, aiColor));
        assertTrue( b.canMove(aiColor));
        assertEquals(1, b.getHowManyBlotsOnPoint(6));
        b.selectAPoint(6, aiColor);
        assertEquals(1, b.getPotDest(1));
        assertEquals(Board.black.getBearOffLoc(), b.getPotDest(2));
        // was interesting bug: moving from point 6 to 6 caused another blot to appear
        b.bearOff( aiColor, /*oldpoint*/6, /*rollValue*/6 ); 
        // was:
        // b.doPartialMove(/*from:*/6, /*to:*/Board.black.getBearOffLoc( ),
        //      /*whichDie:*/2,/*rollValue*/6, aiColor);

        // so now the blot has beared off from 6, right?
        assertEquals(13, b.bear[Player.BLACK]);

        assertFalse(b.solitaryBlotOnPoint(6, aiColor));
        assertTrue(b.canBearOffWithLargerRolls(aiColor)); // <-- is this miscalculating??
        // b.selectAPoint(4, aiColor); // not necessary to select since bearOff now says where from
        //ah-hah, doPartialMove( to bearOff ) might not be as smart as bearOff
        //and forget about doPartialMove( to beyondBearOff ) ??
        b.bearOff( aiColor,/*oldpoint*/4,/*rollValue*/5 ); 
        // was:
        //doPartialMove(4,aiColor.getBearOffLoc( ),/*whichDie:*/1,/*rollValue:*/5,aiColor);
        // did this ^ work?

        assertEquals(0, b.getHowManyBlotsOnPoint(4));
        assertEquals(14, b.bear[Player.BLACK]);
    }

    @Test
    /**
     * testing points in end game
     */
    public void testNeedsInexact()  {

        b = g.getMyBoard();
        assertNotNull(b);
        try {
            b.makeAlmostDonerGame( );
            /* black has singles in quadrant 4, on points 1,3,4 (bearing off to 0) */
            assertNotNull(b);
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }

        g.setCurrentPlayer(aiColor);
        b.myDice.roll(6,4); // die1 rollValue==6, die2 rollValue==4
        // alternative syntax: 
        // b1.myDice.setDie(/*whichDie:*/1,/*rollValue:*/5); 
        // setDie(/*whichDie:*/2,/*rollValue:*/6);
        assertTrue( b.solitaryBlotOnPoint(4, aiColor));
        assertTrue( b.canMove(aiColor, /*exact?*/ true));
        assertTrue( b.canLandOn(Board.black.getBearOffLoc( ), aiColor, /*exact?*/ true));
        assertTrue( b.canLandOn(Board.black.getBearOffLoc( ), aiColor, /*exact?*/ false));
        assertTrue( b.canLandOn(Board.black.getBearOffLoc( ), aiColor/*, exact? false*/));
        assertFalse( b.canBearOffWithLargerRolls(aiColor));
        assertTrue( b.canMove(aiColor));
        assertEquals(1, b.getHowManyBlotsOnPoint(4));
        b.selectAPoint(4, aiColor);
        assertEquals(Board.ILLEGAL_MOVE, b.getPotDest(1)); 
        // can't use the 6 because 4 would work exact!
        assertEquals(Board.black.getBearOffLoc( ), b.getPotDest(2));
        //
        b.doPartialMove(4,Board.black.getBearOffLoc( ),/*whichDie:2*/ /*rollValue:*/4,aiColor);
        // so now the blot has beared off from 4, right?

        assertFalse(b.solitaryBlotOnPoint(4, aiColor));
        assertTrue(b.solitaryBlotOnPoint(3, aiColor));
        assertTrue(b.canBearOffWithLargerRolls(aiColor));
        b.selectAPoint(3, aiColor);
        assertEquals(Board.black.getBeyondBearOffLoc( ), b.getPotDest(1)); // can now use the 6
        assertEquals(Board.ILLEGAL_MOVE, b.getPotDest(2));

        b.doPartialMove(/*start:*/3,/*end:*/Board.black.getBeyondBearOffLoc( )
        ,/*whichDie:1*/ /*rollValue:*/6,aiColor);
        assertEquals(0, b.getHowManyBlotsOnPoint(3));
        assertEquals(14, b.bear[Player.BLACK]);
    }

    @Test
    /**
     * testing ai points in end game, doesn't do as much as testSelectAPoint2
     * Note: I'm focusing more on testNeedsInexact( ) for now, 2012apr17
     */
    public void testBearoff() {

        b = g.getMyBoard();
        assertNotNull(b);
        try {
            b.makeAlmostDonerGame( );
            /* black just has singles in quadrant 4, on points 1,3,4 (bearing off to 0) */
            assertNotNull(b);
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }

        g.setCurrentPlayer(aiColor);
        b.myDice.roll(5,6); // die1 rollValue==5, die2 rollValue==6
        //alternative syntax: 
        // b1.myDice.setDie(/*whichDie:*/1,/*rollValue:*/5); 
        // setDie(/*whichDie:*/2,/*rollValue:*/6);
        assertTrue( b.solitaryBlotOnPoint(4, aiColor));
        assertEquals(1, b.getHowManyBlotsOnPoint(4));
        /* okay, single blot on 4, with dice rolls of 5 & 6. Ought to be able to bear out.*/
        assertTrue(b.canBearOffWithLargerRolls(aiColor));
        //         ok: following was bad test... the question shouldn't be "canLandOn" since 
        //            black can land on blackBearOffLoc and blackBeyondBearOffLoc,
        //         the question should be canMove(  +- exact )
        //   assertFalse( b.canLandOn(Board.black.getBearOffLoc( ), aiColor, /*exact?*/ true));
        //   assertFalse( b.canLandOn(Board.black.getBearOffLoc( ), aiColor, /*exact?*/ false));
        //   assertFalse( b.canLandOn(Board.black.getBearOffLoc( ), aiColor/*, exact? false*/));
        assertTrue( b.canMove(aiColor/*,exact==false*/) );
        assertTrue( b.canMove(aiColor, /*exact?:*/false) );
        assertFalse( b.canMove(aiColor, /*exact?:*/true) );
        b.selectAPoint(4, aiColor);
        assertEquals(Board.black.getBeyondBearOffLoc( ), b.getPotDest(1)); //should it be just Bear??
        assertEquals(Board.black.getBeyondBearOffLoc( ), b.getPotDest(2));
    }

    @Test
    public void testTakeOneBlotOffPoint( ) {

        b = g.getMyBoard();
        assertNotNull(b);
        try {
            b.makeAlmostDoneGame( );
            /* black just has singles in quadrant 4, on points 1,4,6 (bearing off to 0) */
            assertNotNull(b);
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }

        g.setCurrentPlayer(aiColor);
        b.myDice.roll(5,6); 
        // alternative syntax: 
        // b.myDice.setDie(/*whichDie:*/1,/*rollValue:*/5);
        // b.setDie(/*whichDie:*/2,/*rollValue:*/6);
        assertTrue( b.solitaryBlotOnPoint(6, aiColor));
        b.takeOneBlotOffPoint(6);
        assertEquals(0, b.getHowManyBlotsOnPoint(6));
    }

    @Test
    public void testLegitEndLoc( ) {
        assertTrue(Board.legitEndLoc(15,aiColor));
        assertFalse(Board.legitEndLoc(Board.ILLEGAL_MOVE,aiColor));
        assertFalse(Board.legitEndLoc(Board.howManyPoints + 3,aiColor));
        assertFalse(Board.legitEndLoc(-3,aiColor));
    }

    @Test
    /**
     * black on bar, and 4 twice; white on bar twice and on 22 twice
     * note: white ends past 24, black ends below 1.
     * Impossible to get back in if black rolls 3,3 or white rolls 4,4
     */
    public void testBar() {

        b = g.getMyBoard();
        try {
            b.makeBusyBarGame();
            /* black on bar, and 4 twice; white on bar twice and on 22 twice
             * note: white ends past 24, black ends below 1.
             * Impossible to get back in if black rolls 3,3 or white rolls 4,4  */
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }
        assertTrue( b.onBar(Board.white));
        assertTrue( b.onBar(Board.black));
        g.setCurrentPlayer(Board.black);
        b.myDice.roll(3, 4);

        b.selectABar( Board.black ); // oops, this doesn't move anything
        // need equiv for bar: b.bearOff( Board.black,mainBoard.getSelectedPointLoc( ));
        // after bringing black in from the bar, using the 4
        int bbar = Board.black.getBarLoc( );
        b.moveBlot( Board.black, /*from*/bbar, /*to:*/21, /*rollVal:*/4 ); 
        // ^ equiv to barOn(Board.black, 21); // ?
        assertEquals(1, b.getHowManyBlotsOnPoint(21, aiColor));
        assertFalse(b.onBar(Board.black));
        b.selectAPoint(21, Board.black);
        assertTrue( b.canMove(Board.black));

        b.moveBlot( Board.black, /*from*/21, /*to*/b.getPotDest(1),/*rollVal:*/3 ); 
        // turn will end itself, right?
        b.myDice.roll(4,4);
        // now white is stuck, can't come in, should forfeit! Who is responsible for 
        // noticing that we're stuck?
        // and then bring in another & check
        // for now I've installed a "forfeitMove" button to be pressed by hand
        //assertEquals(false, b.onBar(Board.black));
    }

    @Test
    public void testOverlap( ) {
        b = g.getMyBoard();
        assertNotNull(b);
        assertTrue(b.playersOverlap(Game.blackP, Game.whiteP));
        try {
            b.makeAlmostDonerGame( );
        } catch( Exception e) {
            fail(e.toString( ));
        }
        /* black has singles in quadrant 4, on points 1,3,4 (bearing off to 0)
        white has 3 on 18 & 19, and has 2 on 24 */
        assertFalse(b.playersOverlap(Game.blackP, Game.whiteP));
    }
}

/* class BoardTest */