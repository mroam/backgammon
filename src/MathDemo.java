

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The test class MathDemo.
 *
 * @author  (your name)
 * @version (a version number or a date)
 */
public class MathDemo {

    Game g;
    Board b;
    Player aiColor = Game.blackP; /* playerColor */

    /**
     * Default constructor for test class MathDemo
     */
    public MathDemo() {
    }

    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    @Before
    public void setUp() {
        g = new Game(/*networked*/false,/*testing*/false);
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
    public void testPointBuild1( ) {
        b = g.getMyBoard();
        try {
            /* black on 14 & 12 heading for 0; white on 4 (has long way to go)*/
            b.makePointBuild1Board( );
            assertNotNull(b);
        } catch(Exception e) { /* isn't there a way to test without catching excetions? */
            fail(e.toString( ));
        }
        g.setCurrentPlayer(aiColor);
        g.myAI.setHowManyTurnsSoFar(4); // so AI uses pointBuilderStrategy
        b.myDice.roll(4,3);
    }
    
    
    @Test
    public void testPointBuild2( ) {
        b = g.getMyBoard();
        try {
            /* black on 14 & 12 heading for 0; white on 4 (has long way to go)*/
            b.makePointBuild2Board( );
            assertNotNull(b);
        } catch(Exception e) { /* isn't there a way to test without catching excetions? */
            fail(e.toString( ));
        }
        g.setCurrentPlayer(aiColor);
        g.myAI.setHowManyTurnsSoFar(4); // so AI uses pointBuilderStrategy
        b.myDice.roll(4,3);
    }
}
