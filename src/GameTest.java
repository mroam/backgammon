
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The test class GameTest.
 *
 * @author  Mike Roam
 * @version 2012 Jan
 */
public class GameTest
{
    Game g;
    Board b;
    Player aiColor = Game.blackP; /* playerColor */

    /**
     * Default constructor for test class JBackgammonTest
     */
    public GameTest()
    {
    }

    /**
     * Sets up the test fixture.
     *
     * Called before EVERY test case method.
     */
    @Before
    public void setUp()
    {
        g = new Game(/*networked:*/false,/*testing:*/true);
    }

    /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    @After
    public void tearDown()
    {
    }


    @Test
    public void testGame() {
        
        g.setCurrentPlayer(g.blackP);
        assertEquals(Board.black, g.getCurrentPlayer( ) );
    }

    @Test
    public void testAlmostDoneGame( ) {
        g = new Game();
        b = g.getMyBoard();
        assertNotNull(b);
        try {
            b.makeAlmostDoneGame( );
            g.setCurrentPlayer(aiColor);
            b.myDice.roll(3,4);
            assertTrue(b.canBearOff(aiColor));
            assertFalse(b.onBar(aiColor));
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }
    }

    @Test
    public void test3BlotBoard() {
        g = new Game();
        b = g.getMyBoard();
        try {
            b.make3BlotGame( );/* black on 20 & 12 ends at 0, white on 4 ends past 24 */
            assertEquals(aiColor, b.getPlayerOnPoint(12));
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }
    }

   
    @Test
    public void testBoardAllMoveable() {
        g = new Game();
        b = g.getMyBoard();
        assertNotNull(b);
        b.myDice.roll( );
        LocList ll = b.allMoveableBlotLocs( Board.white );
        System.out.println(ll.myList);
    }


    @Test
    public void testGameBoardDoPartialMove2() {
        g = new Game(false);
        b = g.getMyBoard();
        b.myDice.roll();

        try {
            b.make3BlotGame( );/* black on 20 & 12 ends at 0, white on 4 ends past 24 */
        } catch(Exception e) {
            fail(e.toString( ));
        }
        assertNotNull(b.allMoveableBlotLocs(Board.white));
        LocList ll1 = b.allMoveableBlotLocs(Board.white);
        assertNotNull(ll1);
        assertEquals(1, ll1.size());
        g.setCurrentPlayer(aiColor);
        LocList ll2 = b.allMoveableBlotLocs(Board.black);
        assertNotNull(ll2);
        assertEquals(2, ll2.size());
    }


    @Test
    public void testAI() {
        g = new Game(false);
        b = g.getMyBoard();
        assertNotNull(b);
        g.setCurrentPlayer(aiColor);
        assertEquals(aiColor,g.getCurrentPlayer( ));
    }

    @Test
    public void testBlackMoveDice3n6() {
        g = new Game(false);
        b = g.getMyBoard();
        try {
            b.make3BlotGame( );/* black on 20 & 12 ends at 0, white on 4 ends past 24 */
            assertNotNull(b);
            b.myDice.roll(3, 6);
            g.setCurrentPlayer(aiColor);
            LocList ll1 = b.allMoveableBlotLocs(aiColor);
            assertNotNull(ll1);
            assertEquals("[12, 20]", ll1.toString());
            java.util.ArrayList<PartialMove> allpm1 = b.allLegalPartialMoves(aiColor);
            assertNotNull(allpm1);
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }
    }

    @Test
    public void testStartGameStrategy( ) {
        g = new Game();
        b = g.getMyBoard();
        assertNotNull(b);
        b.myDice.roll(1,1);
        //        System.out.println(ll.myList);
    }

    /* class GameTest */
}
