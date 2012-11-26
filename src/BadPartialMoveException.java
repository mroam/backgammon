
/**
 * BadPartialMoveException for squawking about negative moves or bad start or end points.
 * 
 * @author (Mike Roam) 
 * @version (2011 Dec 15)
 */
public class BadPartialMoveException extends BackgammonException {
	
    public static final long serialVersionUID = 1L; // for serializing, mike version 1
    
  public BadPartialMoveException() {
  }

  public BadPartialMoveException(String msg) {
    super(msg);
  }
} /* class BadPartialMoveException */
