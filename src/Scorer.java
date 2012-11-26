
/**
* Abstract class Scorer - so various AIs can score boards differently
*
* @author Mike Roam
* @version 2012 May 8
*/
public abstract class Scorer {

    public final static double valueOfBearedOffPiece = 1000.0; /* for convenience of math & testing */
    public final static double penaltyOfBarPiece = -100.0;/* for convenience of math & testing */

    /**
    *
    * @param myBoard      Which board to look at. Scorer probably need not care whose
    * turn it is nor whether any points are currently selected.
    * @param  playerColor    Wo is looking at the board--white will think differently
    * about the goodness of a board than black will!
    * @return      how the board looks to the Scorer, using player's point of view
    */
    abstract public double score(Board myBoard, Player playerColor);
}
