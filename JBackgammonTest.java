

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The test class JBackgammonTest.
 *
 * @author  (your name)
 * @version (a version number or a date)
 */
public class JBackgammonTest
{
    /**
     * Default constructor for test class JBackgammonTest
     */
    public JBackgammonTest()
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
    public void testJBackgammon()
    {
        JBackgammon jb = new JBackgammon(false);
    }

    @Test
    public void test2pieceBoard()
    {
        JBackgammon jb = new JBackgammon(false);
        Board myb = jb.getMyBoard();
        assertEquals(Board.black, myb.getColorOnPoint(10));
    }
}



