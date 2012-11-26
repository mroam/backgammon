
/**
 * BadBoardException for squawking about wrong number of partialMoves in a Move, etc.
 * 
 * @author (Mike Roam) 
 * @version (2011 Dec 14)
 */
public class BadMoveException extends BackgammonException {
	
    public static final long serialVersionUID = 1L; // for serializing, mike version 1
  public BadMoveException() {
  }

  public BadMoveException(String msg) {
    super(msg);
  }
} /* class BadMoveException */
