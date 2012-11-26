
/**
 * Write a description of class RaceScorer here.
 * 
 * @author Mike Roam
 * @version 2012 May 14
 */
public class RaceScorer extends Scorer {

    /**
     * Constructor for objects of class RaceScorer
     */
    public RaceScorer() {

    }

    /**
     * Using pip count, mostly, but also looking at how far back my 
     * last player is for tie-breaker (since pip count isn't going to 
     * vary from Move to Move except when bearing off. Maybe I could
     * have some kind of weighted average of "how many are far behind"??
     * 
     * This score is suitable for comparison to other RaceScorers, but
     * is not normalized to be comparable to other Scorers
     */
    public double score(Board myBoard, Player playerColor) {
        double lastBlotScore = 0;
        int locOfSlowPoke = myBoard.locOfLastBlot( playerColor );
        int howManySlowPokes = myBoard.getHowManyBlotsOnPoint(locOfSlowPoke,playerColor);
        if (Game.howManyColors != 2) {
            throw new IllegalArgumentException("I don't know how to score" 
                + " more than 2 colors for race, yet.");
        } else {
            // first worry about scootching up the caboose (last blot(s))
            if (playerColor == Game.blackP) {
                // black is trying to get to 0, so lets reverse his 
                // position so we can use same calculations as white
                locOfSlowPoke = Board.howManyPoints - locOfSlowPoke; 
            }
            // now score the last blot(s), here are some examples
            // 3 blots on 7 (ugh, far behind) => score 2.3
            // 1 blot on 7 (ugh, but not as bad as 3blots on 7)=> 7
            // 1 blot on 20 (best of these) => 20
            lastBlotScore = locOfSlowPoke / howManySlowPokes;

            int pipCount = myBoard.getPipCount(playerColor);
            // most alternative moves will have the same pip count
            // EXCEPT when bearing off. The lower pip count, the better,
            // perhaps exaggerate its effect. How much?
            int where = playerColor.getBearOffLoc( );
            int howManyBearedOff = myBoard.getHowManyBlotsOnPoint(where,playerColor);
            //The more beared off the better, by far!
            double finalScore = lastBlotScore - pipCount 
                + howManyBearedOff * Scorer.valueOfBearedOffPiece; // 
            return finalScore;
        }
    } // score( )

    /* class RaceScorer */
}
