
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The test class PlayerTest.
 *
 * @author  Mike Roam
 * @version 2012 Apr 20
 */
public class PlayerTest
{
    /**
     * Default constructor for test class PlayerTest
     */
    public PlayerTest()
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
    /**
     * This test is assuming standard board: 4 quadrants with 6 points per quadrant.
     */
    public void testConstructors() {
        Player pw = new Player(Player.WHITE, false);
        assertEquals(Game.white, pw.getColor());
        assertEquals(Board.howManyPoints, pw.getEndOfFinalQuadrant());
        assertEquals(19, pw.getStartOfFinalQuadrant());
        assertEquals(Board.howManyPoints, pw.getHighPoint());
        assertEquals(1, pw.getLowPoint());
        assertEquals(false, pw.getIsAI());
        assertEquals(false, pw.inFinalQuadrant(18));
        assertEquals(true, pw.inFinalQuadrant(19));
        assertEquals(true, pw.inFinalQuadrant(22));
        assertEquals(true, pw.inFinalQuadrant(24));
        assertEquals(false, pw.inFinalQuadrant(25));
        assertEquals(false, pw.inBoardZone(0));
        assertEquals(true, pw.inBoardZone(1));
        assertEquals(true, pw.inBoardZone(18));
        assertEquals(true, pw.inBoardZone(24));
        assertEquals(false, pw.inBoardZone(25));
        assertEquals(0, pw.getBarLoc());
        assertEquals(25, pw.getBearOffLoc());
        assertEquals(100, pw.getBeyondBearOffLoc());
        assertEquals(2, pw.nextPoint(1));
        assertEquals(pw.getBearOffLoc( ), pw.nextPoint(24));
        assertEquals("white", pw.toString());
        assertEquals(true, pw.legitColor());

        // now creating a black AI, which should count downward when asked for nextPoint( )
        Player pb = new Player(Player.BLACK, true);
        assertEquals(true, pb.getIsAI());
        assertEquals(Player.BLACK, pb.getColor());
        assertEquals(1, pb.getEndOfFinalQuadrant());
        assertEquals(6, pb.getStartOfFinalQuadrant());
        assertEquals(Board.howManyPoints, pb.getLowPoint());
        assertEquals(1, pb.getHighPoint());
        assertEquals(false, pb.inFinalQuadrant(0));
        assertEquals(true, pb.inFinalQuadrant(1));
        assertEquals(true, pb.inFinalQuadrant(4));
        assertEquals(true, pb.inFinalQuadrant(6));
        assertEquals(false, pb.inFinalQuadrant(7));
        assertEquals(false, pb.inBoardZone(0));
        assertEquals(true, pb.inBoardZone(1));
        assertEquals(true, pb.inBoardZone(18));
        assertEquals(true, pb.inBoardZone(Board.howManyPoints));
        assertEquals(false, pb.inBoardZone(Board.howManyPoints+1));
        assertEquals(true, pb.legitColor());
        try {
            assertEquals(0, pb.nextPoint(1));
        } catch (Exception e) {
            System.out.println(e);
        }
        assertEquals(6, pb.nextPoint(7));
        assertEquals(23, pb.nextPoint(Board.howManyPoints));
        try {
            assertEquals(24, pb.nextPoint(25));
            // should throw exception re 25 out of bounds
        } catch (Exception e) {
            System.out.println("As expected, " + e);
        }
        try {
            assertEquals(-1, pb.nextPoint(0));
            // should throw exception re 0 out of bounds
        } catch (Exception e) {
            System.out.println("As expected, " + e);
        }
        assertEquals("black", pb.toString());
        assertEquals(25, pb.getBarLoc());
        assertEquals(0, pb.getBearOffLoc());
        assertEquals(-100, pb.getBeyondBearOffLoc());
    }
}

