
/**
 * BadBoardException for squawking about wrong number of pieces on board.
 * 
 * @author (Mike Roam) 
 * @version (2011 Dec 14)
 */
public class BadBoardException extends Exception {
	
    public static final long serialVersionUID = 1L; // for serializing, mike version 1
    
  public BadBoardException() {
  }

  public BadBoardException(String msg) {
    super(msg);
  }
} /* class BadBoardException */
