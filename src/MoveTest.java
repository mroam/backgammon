
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;  // provides Collection
/**
 * The test class MoveTest.
 *
 * @author  Mike Roam
 * @version 2012 Mar 25
 */
public class MoveTest {

    Game g;
    Board b;
    Player aiColor = Game.blackP; /* playerColor */

    /**
     * Default constructor for test class MoveTest
     */
    public MoveTest()
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
        g = new Game(false);
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

    /**
     * Not sure whether equals will work when partialMoves are in different order:
     * should only be equals if the moves can work in either order.
     * Impossible moves are equal only if partialMoves are in_same_order.
     * 
     * Note: occasions in which order matters are
     * (1) Bar entry and then a move upon board
     * (2) Moving to an unoccupied (or enemy occupied) blot and then moving on from there
     * (3) Bringing last piece into final quadrant which then allows next move to bear off
     */
    @Test
    public void testEquals( ) {
        b = g.getMyBoard( ); // standard starting board
        try {
            /* black on 14 & 12 heading for 0; white on 4 (has long way to go)*/
            b.make3BlotGame2( );
            assertNotNull(b);
        } catch(Exception e) { /* isn't there a way to test without catching exceptions? */
            fail(e.toString( ));
        }
        try {
            // Move constructor throws BadMoveException & BadBoardException
            // Move(/*start1: end1:* rollVal1:*/ /*s2: e2: r2:*/4, b, aiColor)
            Move m1 = new Move(12,10,2, 14,10,4,  b,aiColor);
            Move mNull = new Move(/*partials:*/null, aiColor);
            Move m1SameOrder = new Move(12,10,2,  14,10,4,  b,aiColor);
            Move m1DiffOrder = new Move(14,10,4,  12,10,2,  b,aiColor);
            Move m1DiffPartial = new Move(12,11,1,  14,10,4,  b,aiColor);
            // standard JUnit does not have "assertNotEquals"
            assertFalse(m1.equals(mNull));
            assertEquals(m1,m1SameOrder);

            // now try a possible move with partials in different order
            assertEquals(m1,m1DiffOrder);
            assertTrue(! m1.equals(m1DiffPartial)); // <Move.equals is buggish!
        } catch (Exception e) {
            System.err.println("trouble making a Move: " + e);
        }
    }

    @Test
    public void testAIMakingImpossibleMove( ) {
        /* [ ]after moving all black blots from 13 earlier, StartGameStrategy responded to roll of 4,3
        with its prefab choice, and isPossible( ) didn't catch the problem or wasn't called.
        isPossible passes some tests, so maybe AI didn't call it:
        Dice==4,3, AI StartGameStrategy suggests [[black 13=>10 rollVal:3],[black 13=>9 rollVal:4]]
        AI thinkAndPlay had exception: java.lang.IllegalArgumentException: 
        player 'black' can't move from point '13' since no blots are there!
         */
        b = g.getMyBoard( ); // standard starting board, has 5 blacks on 13
        Move m1 = new Move(13,11,2, 13,11,2,  b,aiColor);

        g.setCurrentPlayer(aiColor);
        b.myDice.roll(2,2);
        assertTrue(m1.isPossible( b )); // has to have dice available

        b.myDice.roll(2,2);
        b.moveBlot(aiColor, 13, 11, 2);
        b.moveBlot(aiColor, 13, 11, 2);
        b.moveBlot(aiColor, 13, 11, 2);
        b.moveBlot(aiColor, 13, 11, 2);

        g.setCurrentPlayer(aiColor);
        b.myDice.roll(2,1);
        b.moveBlot(aiColor, 13, 11, 2);
        b.moveBlot(aiColor, 11, 10, 1);

        assertEquals( 0, b.getHowManyBlotsOnPoint(13));
        g.setCurrentPlayer(aiColor);
        b.myDice.roll(4,3); /* alternative syntax:b1.myDice.roll(1,2) */

        // okay, so the board is set for a 4,3
        // will ai return that impossible move?
        StartGameStrategy sg = new StartGameStrategy( );
        Move best = sg.pickBestMove(b,aiColor);
        System.out.println(best);

        // this is the (currently impossible) default move suggested by startGameStrategy
        m1 = new Move(13,10,3,  13,9,4,  b,aiColor);
        assertEquals(m1,best);
        try {
            // Move constructor throws BadMoveException & BadBoardException
            best = g.myAI.chooseAMove(b, aiColor); // should be different than m1 
        } catch(Exception e) {
            fail(e.toString( ));
        }
        if (m1.equals(best)) {
            System.err.println("uh-oh, AI is still suggesting that not currently possible move");
            System.err.println("or --more likely-- the Move.equals( ) is still malfunctioning");
            assertFalse(m1.equals(best));
        } else {
            // got another move to try
        }
        b.doMove(best,aiColor);
    }

    @Test
    public void testIsPossible( ) {
        b = g.getMyBoard( ); // standard starting board, has 5 blacks on 13
        Move m1 = new Move(13,11,2, 13,11,2,  b,aiColor);

        g.setCurrentPlayer(aiColor);
        b.myDice.roll(2,2);
        assertTrue(m1.isPossible( b )); // has to have dice available

        b.myDice.roll(2,2);
        b.moveBlot(aiColor, 13, 11, 2);
        b.moveBlot(aiColor, 13, 11, 2);
        b.moveBlot(aiColor, 13, 11, 2);
        b.moveBlot(aiColor, 13, 11, 2);

        g.setCurrentPlayer(aiColor);
        b.myDice.roll(2,1);
        b.moveBlot(aiColor, 13, 11, 2);
        b.moveBlot(aiColor, 11, 10, 1);

        assertEquals( 0, b.getHowManyBlotsOnPoint(13));
        g.setCurrentPlayer(aiColor);
        b.myDice.roll(4,3); /* alternative syntax:b1.myDice.roll(1,2) */
        m1 = new Move(13,10,3, 13,9,4,  b,aiColor);
        assertFalse(m1.isPossible( b ));
    }

    @Test
    public void testMove() {
        /* old test, not working anymore, maybe it needs specific board
        probably has issue with the game needing a first roll of dice
        to choose a currentPlayer */

        b = g.getMyBoard( );
        assertNotNull(b);
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
        assertTrue( best.isPossible(b ) );
        ArrayList<PartialMove> partials = best.getMyPartials();
        assertEquals(4, partials.size( ) );

        b.doMove(best,aiColor);
        assertEquals(0, b.myDice.getNumOfPartialMovesAvail( ) );
    } /* testMove( ) */

} /* Class MoveTest*/