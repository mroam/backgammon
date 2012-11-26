
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;  // provides Collection
/**
 * The test class StartGameTest.
 *
 * @author  Mike Roam
 * @version 2012 Mar 28
 */
public class StartGameTest {

    Game g;
    Board b;
    Player aiColor = Game.blackP; /* playerColor */

    /**
     * Default constructor for test class StartGameTest
     */
    public StartGameTest()
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
    public void testGameStart11()
    {
        g = new Game(false);
        b = g.getMyBoard();
        try {
            b.makeStartingBoard( );/* regular game */
            assertNotNull(b);
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }

        g.setCurrentPlayer(aiColor);
        b.myDice.roll(1,1); /* alternative syntax:b1.myDice.roll(1,2) */
        assertEquals(4, b.myDice.getNumOfPartialMovesAvail( ) );

        StartGameStrategy sg = new StartGameStrategy( );
        Move best = sg.pickBestMove(b,aiColor);
        System.out.println(best);
        ArrayList<PartialMove> partials = best.getMyPartials();
        assertEquals(4, partials.size( ) );
        assertTrue(best.isPossible( b));
        b.doMove(best,aiColor );
        assertTrue(b.solitaryBlotOnPoint(8, aiColor));
        // //         assertEquals(true, b.canLandOnExact(12, aiColor));
        // //         assertEquals(true, b.canLandOn(12, aiColor));
        // //         assertEquals(true, b.canMove(aiColor));
        assertEquals(2, b.getHowManyBlotsOnPoint(5));
        assertEquals(2, b.getHowManyBlotsOnPoint(7));
        // //         b.handlePoint(12, aiColor);
        // //         assertEquals(11, b.getPotDest(1));
        // //         assertEquals(6, b.getPotDest(2));
        // //         b.doPartialMove(12,11,/*whichDie:*/1,aiColor);
        // //         assertEquals(true, b.solitaryBlotOnPoint(11, aiColor));
        // //         b.handlePoint(11, aiColor);
        // //         b.doPartialMove(20,14,/*whichDie:*/2,aiColor);
        // //         assertEquals(true, b.solitaryBlotOnPoint(11, aiColor));
        // //        assertEquals(true, b.solitaryBlotOnPoint(14, aiColor));
    }
    
    
    @Test
    public void testGameStart56()
    {
        g = new Game(false);
        b = g.getMyBoard();
        try {
            b.makeStartingBoard( );/* regular game */
            assertNotNull(b);
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }

        g.setCurrentPlayer(aiColor);
        b.myDice.roll(6,5); /* alternative syntax:b1.myDice.roll(1,2) */
        assertEquals(2, b.myDice.getNumOfPartialMovesAvail( ) );

        StartGameStrategy sg = new StartGameStrategy( );
        Move best = sg.pickBestMove(b,aiColor);
        System.out.println(best);
        assertTrue(best.isPossible(b ));
        ArrayList<PartialMove> partials = best.getMyPartials();
        assertEquals(2, partials.size( ) );
        b.doMove(best,aiColor );
        // //         assertEquals(true, b.solitaryBlotOnPoint(12, aiColor));
        // //         assertEquals(true, b.canLandOnExact(12, aiColor));
        // //         assertEquals(true, b.canLandOn(12, aiColor));
        // //         assertEquals(true, b.canMove(aiColor));
        // //         assertEquals(1, b.getHowManyBlotsOnPoint(12));
        // //         b.handlePoint(12, aiColor);
        // //         assertEquals(11, b.getPotDest(1));
        // //         assertEquals(6, b.getPotDest(2));
        // //         b.doPartialMove(12,11,/*whichDie:*/1,aiColor);
        // //         assertEquals(true, b.solitaryBlotOnPoint(11, aiColor));
        // //         b.handlePoint(11, aiColor);
        // //         b.doPartialMove(20,14,/*whichDie:*/2,aiColor);
        // //         assertEquals(true, b.solitaryBlotOnPoint(11, aiColor));
        // //        assertEquals(true, b.solitaryBlotOnPoint(14, aiColor));
    }

    @Test
    public void testGameStartStrategyChatty()
    {
        g = new Game(false);
        b = g.getMyBoard();

        g.setCurrentPlayer(aiColor);
        int die1 = 0;
        int die2 = 0;
        while ( die1 != 9 ) {
            die1 = askForDiceAndPickAIMove( );
        } // while
    }

    /**
     * This is used by testGameStartStrategyChatty( )
     */
    public int askForDiceAndPickAIMove( ) {
        try {
            b.makeStartingBoard( );/* regular game */
            assertNotNull(b);
        } catch(Exception e) {
            /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }
        System.out.println("Tell me the AI's dice roll" 
        + " as 2 integers with space between, followed by return:");
        System.out.println("PS: Type a 9 to end this test");
        Scanner sc = new Scanner(System.in);
        int die1 = sc.nextInt();
        int die2 = 0;

        if (die1 != 9 ) {
            die2 = sc.nextInt( );
            b.myDice.roll(die1,die2);
            g.repaint( );

            StartGameStrategy sg = new StartGameStrategy( );
            Move best = sg.pickBestMove(b,aiColor);
            System.out.println(best);
            ArrayList<PartialMove> partials = best.getMyPartials();
            if (die1 == die2) {
                assertEquals(4, partials.size( ) );
            } else {
                assertEquals(2, partials.size( ) );
            }
        }
        return die1;
    }
}

