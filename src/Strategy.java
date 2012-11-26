
/**
 * Abstract class Strategy - 
 * lists the methods that have to be implemented by any Game Strategy we write
 * 
 * @author Mike Roam
 * @version 2012 Mar 6
 */

import java.util.*;  // provides Collections

public abstract class Strategy {

    /**
     * Given a board, will tell us the "best" move.
     * Note: if we're on the bar, our only possible first partialmove is to get in 
     * from bar, so pickBestMove had better do that first!
     */
    abstract Move pickBestMove(Board currentBoard, Player theColor);

    /**
     * First get in from the bar, then fill the rest of the move with first
     * of the available moves.
     * This can be used by any of the Strategies.
     * Not very strategic since it just uses first available moves
     * for in-from-bar and then while canMove !!??
     */
    public Move pickBarMove(Board aBoard, Player playerColor ) {
        if (! aBoard.onBar(playerColor)) { 
            System.err.println("Wassup trying to come in from Bar for " + playerColor
                + ", who isn't on bar!");
            return pickBestMove(  aBoard, playerColor ); // calls for the concrete subclass to do its thing
        } else if (! aBoard.canMove(playerColor) ) {
            return null;
        }

        Move bestMove = null;
        // the following will be the partialMoves we want, in order
        ArrayList<PartialMove> chosenPartials = new ArrayList<PartialMove>( );
        PartialMove pm = null;
        ArrayList<PartialMove> bunchOfNewPartials = null; // various lists we'll pick from

        Board tempBoard = new Board(/*Game:*/ aBoard.myGame,aBoard); // redundant

        while ((tempBoard.onBar(playerColor)) && (! tempBoard.getMyDice().allDiceAreUsed( ))
        && (tempBoard.canMove(playerColor))) {
            bunchOfNewPartials = tempBoard.allLegalPartialMoves( playerColor );
            if (bunchOfNewPartials.size() > 0) {
                pm = bunchOfNewPartials.get(0); // ?? should be more strategic about coming in!!
                chosenPartials.add(pm);
                tempBoard.doPartialMove(pm);
            } else {  // should this be commented out??
                try {
                    return new Move(chosenPartials, playerColor/*,aBoard*/ ); // might have 0 or 1 partials
                } catch(Exception e) {
                    throw new IllegalArgumentException("trouble making Move with board '" + tempBoard + "':\n" + e);
                }
            }
        }

        // still have more partialMoves avail? 
        if ((tempBoard.canMove(playerColor)) && (! tempBoard.getMyDice().allDiceAreUsed( ))) {
            bunchOfNewPartials = tempBoard.allLegalPartialMoves( playerColor );
            if (bunchOfNewPartials.size() > 0) {
                Move restOfMove = this.pickBestMove(tempBoard, playerColor); // uses current Strategy subclass
                //pm = bunchOfNewPartials.get(0); // ?? should be more strategic about moves after coming in!!
                chosenPartials.addAll(restOfMove.getMyPartials());
                //tempBoard.doPartialMove(pm);
//            } else {  // should this be commented out??
//                try {
//                    return new Move(chosenPartials, playerColor/*,aBoard*/ ); // might have 0 or 1 partials
//                    //    throw new IllegalArgumentException("sorry, I don't know how to get more"
//                    //       + "partialMoves now that I'm in from bar");
//                } catch(Exception e) {
//                    throw new IllegalArgumentException("trouble making Move with board '" + tempBoard + "':\n" + e);
//                }
            }
        }

        try {
            bestMove = new Move(chosenPartials, playerColor/*,aBoard*/ );
            //Move(ArrayList<PartialMove> theNewPartials, int myNewColor, 
            //     Board myNewStarterBoard /*Final?*/)
        } catch(Exception e) {
            throw new IllegalArgumentException("trouble making Move with board '" + tempBoard + "':\n" + e);
        }
        System.out.println("\nDice==" + aBoard.getMyDice().shortString() 
        + ", AI is trying to come in from bar, suggests " + bestMove + "");
        return bestMove;
    }

}

