

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The test class DiceRollTest.
 *
 * @author  (your name)
 * @version (a version number or a date)
 */
public class DiceRollTest
{
    /**
     * Default constructor for test class DiceRollTest
     */
    public DiceRollTest()
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
    public void testDiceRoll()
    {
        DiceRoll dr = new DiceRoll(1, 2);
        assertEquals(1, dr.getDice1());
        assertEquals(2, dr.getDice2());
        
        DiceRoll dr2 = new DiceRoll(0, 0);
        assertEquals(0, dr2.getDice1());
        assertEquals(0, dr2.getDice2());
        
        DiceRoll dr3;
        try {
            dr3 = new DiceRoll(-1,1); /* out of bounds, should result in null! */
        } catch (Exception e) {
            System.out.println(e);
            dr3 = null;
        }
        assertEquals(null, dr3);
        
        try {
            dr3 = new DiceRoll(2,7); /* out of bounds, should result in null! */
        } catch (Exception e) {
            System.out.println(e);
            dr3 = null;
        }
        assertEquals(null, dr3);

    } /* test the constructor */
    

    @Test
    public void DR()
    {
        DiceRoll dr4 = new DiceRoll(4, 5);
        assertEquals(4, dr4.getDice1());
    }
}
 /* class DiceRollTest */

