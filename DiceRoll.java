
/**
 * So that we can work with theoretical rolls (if we get roll X then ...)
 * 
 * @author (Mike Roam) 
 * @version (2011 Dec 14)
 */
public class DiceRoll
{
    // instance variables - replace the example below with your own
    int dice1 = 0;
    int dice2 = 0;
    static final int nullDice = 0; /* for dice in mid-air? */
    static final int minDiceVal = 1;
    static final int maxDiceVal = 6;

    
    /**
     * Constructor for objects of class DiceRoll
     */
    public DiceRoll(int newDice1, int newDice2) throws IllegalArgumentException
    {
        if (hasLegitDiceValue( newDice1 ) ) {
            dice1 = newDice1;
        } else {
            String myMsg = "dice1 is given bad value '" + newDice1 + "' not 0 and not [" + minDiceVal + ".." + maxDiceVal + "]";
            throw new IllegalArgumentException(myMsg);
        }
                
        if (hasLegitDiceValue( newDice2 ) ) {
            dice2 = newDice2;
        } else {
            String myMsg = "dice2 is given bad value '" + newDice2 + "' not 0 and not [" + minDiceVal + ".." + maxDiceVal + "]";
           throw new IllegalArgumentException(myMsg);
        }
    } /* constructor */

    
    /**
     * allows nullDice (0) or minDiceVal to maxDiceVal
     */
    static boolean hasLegitDiceValue(int diceVal) {
        if ((diceVal < minDiceVal) || (diceVal > maxDiceVal)) {
            if (diceVal == nullDice) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    } /* hasLegitDiceValue( ) */
    
    
    public int getDice1()
    {
        return dice1;
    } 

    
    public int getDice2()
    {
        return dice2;
    } 
    
    
    public boolean isDoubles( ) {
        if ( hasLegitDiceValue( dice1 ) && (dice1 != nullDice)) {
            return dice1 == dice2;
        } else {
            return false;
        }
    } /* isDoubles */
    
    
    public String toString( ) {
        /* if (dice1 == nullDice) { */
        return dice1 + "," + dice2;
    }
} /* class DiceRoll */
