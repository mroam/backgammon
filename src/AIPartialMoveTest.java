

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The test class AIPartialMoveTest.
 *
 * @author  Mike Roam
 * @version 2012
 */
public class AIPartialMoveTest {
    private Game g;
    private Board b;
    private LocList ll;
    private Player aiColor = Game.blackP;
    private AI ai;
    private java.util.ArrayList<PartialMove> pm;
//    private PartialMove pmove;
    
    
    

    /**
     * Default constructor for test class AIPartialMoveTest
     */
    public AIPartialMoveTest()
    {
    }

    
    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    @Before
    public void setUp() {
        g = new Game(false);
        b = g.getMyBoard();
        try {
            b.makeEasyHitStartingBoard( );
            /* white singles on 5,7,9,13
             * black singles on (absolute) 20, 18,16,12 
             * [from black POV == 5,7,9,13]
             */
            assertNotNull(b);
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }
        g.setCurrentPlayer(aiColor);
        b.myDice.roll(3,5);
        ll = b.allMoveableBlotLocs(aiColor);
        assertNotNull(ll);
        ai = new AI(g);
        //try {
            pm = b.allLegalPartialMoves(aiColor); /* this is doing "handlePoint" */
            assertNotNull(pm);
            PointBuildStrategy pbs = new PointBuildStrategy(/* cautious */0.7);
            assertNotNull(pbs);
            PartialMove pmove = pbs.bestPartialMoveOf(b,pm);
            assertNotNull(pmove);
            g.getMyBoard( ).doPartialMove(pmove);
        //} catch(Exception e) {
        //    System.err.println(e);
        //}
    } /* setUp( ) */

    
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
    public void testDoPartial()
    {
        java.lang.Integer Int1 = ll.get(0);
        assertNotNull(Int1);
        //assertEquals(12, Int1.intValue()); ??
    }


    @Test
    public void AIPartial2()
    {
        //try {
            java.util.ArrayList<PartialMove> pm2 = b.allLegalPartialMoves(aiColor);
            assertNotNull(pm2);
            PointBuildStrategy pbs = new PointBuildStrategy(/* cautious */0.7);
            assertNotNull(pbs);
            PartialMove pmove2 = pbs.bestPartialMoveOf(b,pm2);
            assertNotNull(pmove2);
            b.doPartialMove(pmove2);
        //} catch(Exception e) {
        //    System.err.println(e);
        //}
    }
    
    
}
 /* class AIPartialMoveTest */
