
/**
 * Degree of Cautiousness
 * 
 * @author Mike Roam
 * @version 2012 May 8
 */
public class Caution
{
    // instance variables - replace the example below with your own
    private double cautiousness = 0.0;
    
    final static double cautionMinimum = 0.0;
    final static double cautionMaximum = 1.0;

    /**
     * Constructor for objects of class Caution
     */
    public Caution() {
        
    }
    
    /**
     * Constructor for objects of class Caution
     */
    public Caution(double newCaution) {
        setCaution(newCaution); // will refuse values outside 0.0 to 1.0
    }
    
    /**
     * Copy Constructor for objects of class Caution
     */
    public Caution(Caution other) {
        cautiousness = other.cautiousness;
    }

    /**
     * our cautionness mood can move from 0.0 (brave) to 1.0 (timid). 
     */
    public void setCaution(double newCaution) {
        if (( cautionMinimum <= newCaution) && (newCaution <= cautionMaximum)) {
            cautiousness = newCaution;
        } else {
            throw new IllegalArgumentException("Bad value '" + newCaution 
                + "', I only like 'caution' values " 
                + cautionMinimum + ".." + cautionMaximum);
        }
    }

    /**
     * our cautiousness mood can move from 0.0 (brave) to 1.0 (timid).
     */
    public double getCaution( ) {
        return cautiousness;
    }
    
    public String toString( ) {
        return Double.toString(cautiousness);
    }
}
