
/**
 * BadBoardException for squawking about wrong number of partialMoves in a Move, etc.
 * 
 * @author (Mike Roam) 
 * @version (2011 Dec 14)
 */
public class BadMoveException extends Exception {
  public BadMoveException() {
  }

  public BadMoveException(String msg) {
    super(msg);
  }
} /* class BadMoveException */
