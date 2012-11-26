
/**
 * More sophisticated than PointBuildScorer in also worrying about
 * the existing odds that an unprotected point can actually be hit.
 * Can use most of PointBuildScorer's methods.
 * Will have to know howManyDice and the possible roll values.
 * 
 * @author Mike Roam
 * @version 2012 May
 */
public class PointBuildOddsScorer extends PointBuildScorer
{
    // instance variables - replace the example below with your own
    private int x;

    /**
     * Constructor for objects of class PointBuildOddsScorer
     */
    public PointBuildOddsScorer()
    {
        // initialise instance variables
        throw new IllegalArgumentException("not coded yet");
    }

    /**
     * An example of a method - replace this comment with your own
     * 
     * @param  y   a sample parameter for a method
     * @return     the sum of x and y 
     */
    public int sampleMethod(int y)
    {
        // put your code here
        return x + y;
    }
}
