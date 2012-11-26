
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The test class PartialMoveTest.
 *
 * @author  (Mike Roam)
 * @version (2012)
 */
public class PartialMoveTest {

    Game g;
    Board b;
    Player aiColor = Game.blackP; /* playerColor */

    /**
     * Default constructor for test class PartialMoveTest
     */
    public PartialMoveTest()
    {
    }

    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    @Before
    public void setUp()
    {
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
    public void testEquals() /* hey, I had to put "test" on manually and see (at)Test, wassup */
    {
        g = new Game( false /* networked true/false */ );
        b = g.getMyBoard( );
        assertNotNull(b);
        try {
            b.make4BlotGame( ); /* black on 20 & 12 (ends at 0), whites on 16 (ends past 24) */
            assertNotNull(b);
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }

        g.setCurrentPlayer(aiColor);
        b.myDice.roll(3,3);
        // PartialMove(int newStart, int newRoll, int newEnd, Board newBoard, int newColor, int newWhichDie)
        PartialMove partialM1 = new PartialMove(20, 3, 17, b, g.getCurrentPlayer( )/*, 1*/);
        PartialMove partialM2 = new PartialMove(20, 3, 17, b, g.getCurrentPlayer( )/*, 1*/);
        System.out.println("partialM1==" + partialM1.toString( ) );
        assertTrue( partialM1.equals(partialM2));
    }
    
    @Test
    public void testIsPossible() {
        g = new Game( false /* networked true/false */ );
        b = g.getMyBoard( );
        assertNotNull(b);
        try {
            b.make4BlotGame( ); /* black on 20 & 12 (ends at 0), whites on 16 (ends past 24) */
            assertNotNull(b);
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }

        g.setCurrentPlayer(aiColor);
        b.myDice.roll(3,3);
        // PartialMove(int newStart, int newRoll, int newEnd, Board newBoard, int newColor, int newWhichDie)
        PartialMove partialM1 = new PartialMove(20, 3, 17, b, g.getCurrentPlayer( )/*, 1*/);
        PartialMove partialM2 = new PartialMove(20, 3, 17, b, g.getCurrentPlayer( )/*, 1*/);
        System.out.println("partialM1==" + partialM1.toString( ) );
        assertTrue( partialM1.equals(partialM2));
        assertTrue( partialM1.isPossible( ) );
        // now try some nonsense partials... params are start,roll,end,board,color,whichdie
        // bad start (where we aren't located)
        partialM2 = new PartialMove(21, 3, 18, b, g.getCurrentPlayer( )/*, 1*/);
        assertFalse( partialM2.isPossible( ) );
        
        // bad distance (miscalculated)
        partialM2 = new PartialMove(20, 3, 18, b, g.getCurrentPlayer( )/*, 1*/);
        assertFalse( partialM2.isPossible( ) );
        
        // bad end (blocked by protected point)
        partialM2 = new PartialMove(20, 4, 16, b, g.getCurrentPlayer( )/*, 1*/);
        assertFalse( partialM2.isPossible( ) );
    }
} /* class PartialMoveTest */