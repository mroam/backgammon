
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The test class PointBuildScorerTest.
 *
 * @author  Mike Roam
 * @version 2012 May 9
 */
public class PointBuildScorerTest
{
    Game g = null;
    Board b = null;
    Player aiColor = Game.blackP; /* playerColor */

    /**
     * Default constructor for test class PointBuildStrategyTest
     */
    public PointBuildScorerTest()
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
    public void testSuperMegaHappyScore1() {
        g = new Game(false);
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
        //PointBuildStrategy pb = new PointBuildStrategy( );
        //PointBuildScorer ps = pb.getMyScorer( );
        PointBuildScorer ps = new PointBuildScorer( );

        assertEquals(5.25, ps.howImportantIsThisPoint(b,20, aiColor, ps.getMyCaution( )), 
            /*how close?*/0.01);
        assertEquals(13006.75, ps.getAllPointScore(b,aiColor, new Caution(0.5)), 
            /*how close?*/0.01);
        assertEquals(4.25, ps.howImportantIsThisPoint(b,4, Board.white, new Caution(0.5)), 
            /*how close?*/0.01);
        assertEquals(14004.25, ps.getAllPointScore(b,Board.white, new Caution(0.5)), 
            /*how close?*/0.01);
        assertEquals(-997.5, ps.superMegaHappyScore(b,ps.getMyCaution( ), aiColor ), 
            /*how close?*/0.01);
        System.out.println(ps.superMegaHappyScore(b,ps.getMyCaution( ), aiColor ));
        /* let's test a new score for another board layout...*/
    }
}
