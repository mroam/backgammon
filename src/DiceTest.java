
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The test class DiceTest.
 *
 * @author  (Mike Roam)
 * @version (2012 Feb)
 */
public class DiceTest
{
    /**
     * Default constructor for test class DiceRollTest
     */
    public DiceTest()
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
    public void testConstructors()
    {
        // Default constructor:

        Dice d5 = new Dice( );
        assertEquals(Dice.UNROLLED, d5.getDie(1));
        assertEquals(false, d5.getRolled( ));
        assertEquals(Dice.UNROLLED, d5.getDie(1));
        assertNotNull("uh-oh, random generator is null",d5.getRDice( ));
        assertEquals(0, d5.getNumOfPartialMovesAvail( ));
        assertEquals(false, d5.getUsedDie(1));

        // Copy constructor
        Dice d6 = new Dice(d5);
        assertNotSame(d6,d5);
        assertEquals(Dice.UNROLLED, d6.getDie(1));
        assertEquals(false, d6.getRolled( ));
        assertEquals(Dice.UNROLLED, d6.getDie(1));
        assertNotNull("uh-oh, random generator is null",d6.getRDice( ));
        assertEquals(0, d6.getNumOfPartialMovesAvail( ));
        assertEquals(false, d6.getUsedDie(1));

        // constructor with all values
        Dice d1 = new Dice(1, 2);
        assertEquals(1, d1.getDie1());
        assertEquals(2, d1.getDie2());
        /* alternative getter */
        assertEquals(1, d1.getDie(1));
        assertEquals(2, d1.getDie(2));

        Dice d2 = new Dice(Dice.UNROLLED, 0);
        assertEquals(0, d2.getDie1());
        assertEquals(0, d2.getDie2());

        Dice d3 = null;
        try {
            d3 = new Dice(-1,1); /* out of bounds, should result in null! */
        } catch (Exception e) {
            System.out.println(e);
            d3 = null;
        }
        assertNull(d3);

        try {
            d3 = new Dice(2,7); /* out of bounds, should result in null! */
        } catch (Exception e) {
            System.out.println(e);
            d3 = null;
        }
        assertNull(d3);

        Dice d4 = new Dice(3,4); 
        assertEquals(3, d4.getDie(1));
        try {
            d4 = new Dice(Dice.UNROLLED,5); /* should throw exception for one rolled, one not */
        } catch (Exception e) {
            System.out.println(e);
            d4 = null;
        }
        assertNull(d4);
    } 

    /* test the constructors */

    @Test
    public void DR() {
        Dice dr4 = new Dice(4, 5);
        assertEquals(4, dr4.getDie1());
        assertEquals("I set die2 to 5 in constructor",5, dr4.getDie(2 ) );
        assertEquals(2, dr4.getNumOfPartialMovesAvail( ));
        assertFalse(dr4.isDoubles( ));
        dr4.roll(4,4);
        assertEquals(4, dr4.getNumOfPartialMovesAvail( ));
        assertTrue(dr4.isDoubles( ));
        dr4.setUsedValue( dr4.getDie(1) ); // all dice are same in doubles
        //dr4.numOfPartialMovesAvailDecrease( );
        assertEquals(3, dr4.getNumOfPartialMovesAvail( ));      
    }

    @Test
    public void testSetDie() {
        Dice d1 = new Dice();
        assertEquals(0, d1.getNumOfPartialMovesAvail( ));
        assertFalse(d1.isDoubles( ));

        d1.setDie(1, 2);
        assertEquals("die1 was UNROLLED and then explicitly set to 2",2, d1.getDie(1));
        assertFalse(d1.getRolled( ));
        d1.setDie(2, 3);
        assertEquals("die2 was UNROLLED and then explicitly set to 3",3, d1.getDie(2));
        assertTrue(d1.getRolled( )); /* now that both dice have values, the dice should think it is rolled */
        Dice d2 = new Dice(d1);
        assertEquals("die2 copied from another dice in which it was explicitly set to 3",3, d2.getDie(2));
        Dice d3 = new Dice(3, 2);
        assertEquals("die2 constructed with 2",2, d3.getDie(2));
        try {
            d3.setDie(2,7); /* should get exception for overflow! */
            d3.setDie(2,-1); /* underflow */
            d3.setDie(3,3); /* no such dice! */
        } catch (Exception e) {
            System.out.println(e);
            d3 = null;
        }
        assertNull(d3);
    }

    @Test
    public void testHighestUnused()
    {
        Dice d = new Dice(4, 6);
        assertEquals(2, d.getNumOfPartialMovesAvail());
        assertEquals(6, d.highestUnusedRoll());
        assertEquals(2, d.whichUnusedDieIsHighest());
        assertEquals(4, d.lowestUnusedRoll());
        assertEquals(1, d.whichUnusedDieIsLowest());
        d.setUsedDie(2, true);
        assertEquals(4, d.highestUnusedRoll());
        assertEquals(1, d.whichUnusedDieIsHighest());
        assertEquals(4, d.lowestUnusedRoll());
        assertEquals(1, d.whichUnusedDieIsLowest());
    }

    @Test
    public void doubles()
    {
        Dice d = new Dice(5, 5);
        assertEquals(4, d.getNumOfPartialMovesAvail());
    }

    @Test
    public void doubletCountdown()
    {
        Dice d = new Dice(3, 3);
        assertEquals(true, d.isDoubles());
        assertEquals(4, d.getNumOfPartialMovesAvail());
        d.setUsedValue(3);
        assertEquals(3, d.getNumOfPartialMovesAvail());
        d.setUsedValue(3);
        assertEquals(2, d.getNumOfPartialMovesAvail());
        d.setUsedValue(3);
        assertEquals(1, d.getNumOfPartialMovesAvail());
    }

    @Test
    public void testWhichDieHasValue() {
        Dice d1 = new Dice(4, 4);
        assertEquals(1, d1.whichDieHasValue(4));
        assertEquals(Dice.NO_SUCH_DIE, d1.whichDieHasValue(5));
        d1.roll(3,5);
         assertEquals(1, d1.whichDieHasValue(3));
          assertEquals(2, d1.whichDieHasValue(5));
        assertEquals(Dice.NO_SUCH_DIE, d1.whichDieHasValue(4));
    }
       
    @Test
    public void testUsedDice()
    {
        Dice d1 = new Dice(4, 4);
        assertEquals(1, d1.whichDieIsUnused());
        assertEquals(false, d1.allDiceAreUsed());
        assertEquals(true, d1.allDiceHaveValues());
        assertEquals(4, d1.getDie(1));
        //perhaps getNumOfPartialMovesAvail should be getNumOfRollsAvail??
        assertEquals(4, d1.getNumOfPartialMovesAvail());
        assertEquals(true, d1.getRolled());
        assertEquals(4, d1.highestUnusedRoll());
        assertEquals(false, d1.getUsedDie(1));
        assertEquals(true, d1.isDoubles());
        assertEquals(1, d1.whichDieIsUnused());
        assertEquals(1, d1.whichUnusedDieIsLowest());
    }

    @Test
    public void testToString() {
        Dice d = new Dice(3, 4);
        System.out.println(d);;
        assertEquals("[3,4][values available:3,4][used:none][2 partialMovesAvail]", d.toString());
        d.setUsedDie(2, true);
        System.out.println(d);;
        assertEquals("[3,4][values available:3][used:die2][1 partialMovesAvail]", d.toString());
        d.setUsedValue(3);
        System.out.println(d);;
        assertEquals("[3,4][values available:none][used:die1,die2][0 partialMovesAvail]", d.toString());
        d.setUsedDie(2, false);
        System.out.println(d);;
        assertEquals("[3,4][values available:4][used:die1][1 partialMovesAvail]", d.toString());
        d.roll(5, 5);
        assertEquals("[5,5][values available:5,5,5,5][used:none][4 partialMovesAvail]", d.toString());
        d.setUsedValue(5);
        System.out.println(d);;
        assertEquals("[5,5][values available:5,5,5][used:die4][3 partialMovesAvail]", d.toString());
        d.setUsedDie(2, true);
        System.out.println(d);;
        assertEquals("[5,5][values available:5,5][used:die3,die4][2 partialMovesAvail]", d.toString());
        d.setUsedDie(1, true);
        System.out.println(d);;
        assertEquals("[5,5][values available:5][used:die2,die3,die4][1 partialMovesAvail]", d.toString());
    }
}



/* class DiceTest */
