
/**
 * Superclass for my exceptions so I can catch them in one grab.
 * 
 * @author Mike Roam, based upon suggestion by Gavin Y.
 * @version (a version number or a date)
 */
public class BackgammonException extends Exception {
	
    public static final long serialVersionUID = 1L; // for serializing, mike version 1

    /**
     * Constructor for objects of class BackgammonException
     */
    public BackgammonException() {
    }
    
    /**
     * Constructor for objects of class BackgammonException
     */
    public BackgammonException(String newMsg) {
        super(newMsg);
    }

    
}
