import java.util.*;  // provides Collections

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The test class RaceStrategyTest.
 *
 * @author  Mike Roam
 * @version 2012 May 15
 */
public class RaceStrategyTest {

    Game g = null;
    Board b = null;
    Player aiColor = Game.blackP; /* playerColor */

    /**
     * Default constructor for test class RaceStrategyTest
     */
    public RaceStrategyTest() {
    }

    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    @Before
    public void setUp() {
        g = new Game(/*network*/false,/*testing*/true);
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
     * Sometimes this test works, but sometimes maybe not??
     */
    public void testRace1() {
        g = new Game(/*network*/false,/*testing*/false); //<-I want dialogs for this test
        b = g.getMyBoard();
        assertNotNull(b);
        try {
            b.make3BlotGame( );/* black on 20 & 12 ends at 0, white on 4 ends past 24 */
            assertNotNull(b);
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }

        RaceStrategy rs = new RaceStrategy( ); // builds its own RaceScorer
        g.setCurrentPlayer(aiColor);
        b.myDice.roll(5,3);
        try {
            Move suggestion = rs.pickBestMove(b,aiColor );
            b.doMove( suggestion, aiColor );
        } catch(Exception ex) {
            ex.printStackTrace( System.err );
            //StackTraceElement[] getStackTrace()
            System.out.println("RaceStrategyTest had exception: \n" + ex);
        }
        System.out.println("testRace1 is done");
        g.setCurrentPlayer(Game.whiteP);
        b.myDice.roll(5,2);
        b.selectAPoint(4, Game.whiteP);
        System.out.println("I clicked blot on 4: do both the 5 move and 2 move show up??");
        
        ArrayList<PartialMove> pm = b.legalPartialMovesFromPoint(Game.whiteP.getBarLoc( ),Game.whiteP);
        assertNotNull(pm);
        assertEquals(2, pm.size( ));
    }
}
