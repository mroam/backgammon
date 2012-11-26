
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The test class AITest.
 *
 * @author  Mike Roam
 * @version 2012 Apr 26
 */
public class AITest {

    Game g;
    Board b;
    Player aiColor = Game.blackP; /* playerColor */

    /**
     * Default constructor for test class AITest
     */
    public AITest() {
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

    @Test
    /**
     * black on bar, and 4 twice; white on bar twice and on 22 twice
     * note: white ends past 24, black ends below 1.
     * Impossible to get back in if black rolls 3,3 or white rolls 4,4
     */
    public void testPointBuild() {
        b = g.getMyBoard();
        try {
            b.makePointBuildBoard();
            /* Why is AI breaking point 6 instead of point 9 or 18?  */
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }
        g.setCurrentPlayer(Board.black);
        b.myDice.roll(3, 5);
        g.myAI.setHowManyTurnsSoFar(2); // so PointBuilder happens, not startGame

        try {
            Move suggestion = g.myAI.chooseAMove(b,aiColor );
            b.doMove( suggestion, aiColor );
        } catch(Exception ex) {
            ex.printStackTrace( System.err );
            //StackTraceElement[] getStackTrace()
            System.out.println("AI.chooseAMove( ) had exception: \n" + ex);
        }
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

        try {
            Move suggestion = g.myAI.chooseAMove(b,aiColor );
            b.doMove( suggestion, aiColor );
        } catch(Exception ex) {
            ex.printStackTrace( System.err );
            //StackTraceElement[] getStackTrace()
            System.out.println("AI.chooseAMove( ) had exception: \n" + ex);
        }
    }

    @Test
    /**
     * black on bar, and 4 twice; white on bar twice and on 22 twice
     * note: white ends past 24, black ends below 1.
     * Impossible to get back in if black rolls 3,3 or white rolls 4,4
     */
    public void testBar2() {
        g = new Game();
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
        b.myDice.roll(3, 3);

        try {
            Move suggestion = g.myAI.chooseAMove(b,aiColor );//might have 0 or 1 partials
            b.doMove( suggestion, aiColor );
        } catch(Exception ex) {
            ex.printStackTrace( System.err );
            //StackTraceElement[] getStackTrace()
            System.out.println("AI.chooseAMove( ) had exception: \n" + ex);
        }
    }

    @Test
    /**
     * testing ai points in end game. Scenario only allows bearing off:
     * will AI realize that and do it?
     */
    public void testBearoff() {
        g = new Game(false);
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
        try {
            Move suggestion = g.myAI.chooseAMove(b,aiColor );
            b.doMove( suggestion, aiColor );
        } catch(Exception ex) {
            ex.printStackTrace( System.err );
            //StackTraceElement[] getStackTrace()
            System.out.println("AI.chooseAMove( ) had exception: \n" + ex);
        }
    }
}
