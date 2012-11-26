
/**
 * For returning scored moves from Board.bestMove(Scorer s, Player p)
 * since I'm hesitant to complicate Move with an additional score field.
 * Perhaps this shouldn't extend Move?
 * 
 * @author Mike Roam
 * @version 2012 May
 */
public class MoveWithScore extends Move {
    private Scorer myScorer = null; //usually null unless we've been scored for comparison
    private boolean hasBeenScored = false;
    private double score = 0; // should be ignored or -NAN if we're not scored
    private Board boardToScore = null;
//    private Board startingBoard = null; // who will tell us?

//     /**
//      * Constructor for objects of class MoveWithScore
//      */
//     public MoveWithScore( ) {
//         super( ); // isn't this implicit?
//     }

    /**
     * Copy (Move) Constructor for objects of class MoveWithScore
     */
    public MoveWithScore(Move newMove, Board newBoardToScore) {
        super( newMove );
        if (newBoardToScore == null) {
            throw new NullPointerException("I need a Board to evaluate");
        }
        boardToScore = newBoardToScore;
    }

    /**
     * Copy Constructor for objects of class MoveWithScore
     * Shallow copy.
     */
    public MoveWithScore(MoveWithScore other) {
        super( other );
        myScorer = other.myScorer; 
        hasBeenScored = other.hasBeenScored;
        score = other.score;
        boardToScore = other.boardToScore;
    }

    /**
     * Copy Constructor for objects of class MoveWithScore
     * Shallow copy.
     */
    public MoveWithScore(Move otherMove, Scorer newMyScorer, Board newBoardToScore) {
        //         myScorer = other.scorer; 
        //         hasBeenScored = other.hasBeenScored;
        //         score = other.score;
        super(otherMove);
        if (newBoardToScore == null) {
            throw new NullPointerException("I need a Board to evaluate");
        }
        if (newMyScorer==null) {
            throw new NullPointerException("I need non-null Scorer to figure this score");
        } 
        setScore( newMyScorer,newBoardToScore ); // memorizes this Board & Scorer
    }

    // was this meant to be a copy constructor?
//     /**
//      * Constructor for objects of class MoveWithScore
//      */
//     public MoveWithScore( Scorer myNewScorer ) {
//         super( ); // isn't this implicit?
//         if (newMyScorer==null) {
//             throw new NullPointerException("I need non-null Scorer to figure this score");
//         } 
//         setScore( ); // uses own scorer
//     }

    /**
     * returns null if we haven't been scored
     */
    public Scorer getMyScorer( ) {
        if (hasBeenScored) {
            return myScorer;
        } else {
            return null;
        }
    }

    /**
     * I'm not providing a "setHasBeenScored( )": the only way to change that
     * is by providing a score using "setScore( )".
     */
    public boolean getHasBeenScored( ) {
        return hasBeenScored;
    }

    public double getScore( ) {
        if (!hasBeenScored) {
            throw new IllegalArgumentException("this move has no score, yet.");
        }
        return score;
        // con
    }

    public double getScore(Board aBoard, Scorer aScorer, Player playerColor) {
        if (aScorer==null) {
            throw new NullPointerException("I need a Scorer to calculate a score");
        }
        if (playerColor==null) {
            throw new NullPointerException("I need a Player to calculate a score");
        }
        if (aBoard == null) {
            throw new NullPointerException("I need a Board to evaluate");
        }
        return aScorer.score( aBoard, playerColor);
    }

    /**
     * calculates a score using a different scorer.
     * Should this memorize the new score and alternate Scorer? Probably not.
     */
    public double getScore(Scorer aScorer) {
        if (aScorer==null) {
            throw new NullPointerException("I need a Scorer to calculate a score");
        }
        if (myColor==null) {
            throw new NullPointerException("I need a Player to calculate a score");
        }
        if (myBoard==null) {
            throw new NullPointerException("I need a Board to calculate a score");
        }
        return aScorer.score( myBoard, myColor);
    }

    /**
     * we're told a score and which Scorer was used to calculate the score, supposedly.
     */
    public void setScore( double newScore, Scorer newMyScorer) {
        if (newMyScorer==null) {
            //             throw new NullPointerException("I want to know who made this score");
        }
        score = newScore;
        hasBeenScored = true;
        myScorer = newMyScorer;
    }

    /**
     * with no params means use own scorer to score self on own board.
     */
    public void setScore( ) {
        setScore( myScorer );
    }

    /**
     * Gives us a Scorer and asks it to calculate a score. Handy.
     * tells us which Scorer was used to calculate the score, supposedly.
     * @param newMyScorer
     */
    public void setScore( Scorer newMyScorer) {
        if (newMyScorer==null) {
            throw new NullPointerException("I need non-null Scorer to figure this score");
        }
        if (myColor==null) {
            throw new NullPointerException("I need a Player to calculate a score");
        }
        if (myBoard==null) {
            throw new NullPointerException("I need a Board to calculate a score");
        }

        score = newMyScorer.score( myBoard, myColor );
        hasBeenScored = true;
        if (myScorer != newMyScorer) {
            // only doing anything if this is a change
            myScorer = newMyScorer;
        }
    }

    /**
     * Gives us a Board & a Scorer and asks it to calculate a score. Handy.
     * tells us which Scorer was used to calculate the score, supposedly.
     * @param newMyScorer
     */
    public void setScore( Scorer newMyScorer, Board aBoard) {
        if (newMyScorer==null) {
            throw new NullPointerException("I need non-null Scorer to figure this score");
        }
        if (myColor==null) {
            throw new NullPointerException("I need a Player to calculate a score");
        }
        if (aBoard==null) {
            throw new NullPointerException("I need a Board to calculate a score");
        }

        score = newMyScorer.score( aBoard, myColor );
        hasBeenScored = true;
        if (myScorer != newMyScorer) {
            // only doing anything if this is a change
            myScorer = newMyScorer;
        }
    }
}
