import java.util.*;  // provides Collections

/**
 * Holds ArrayList<Integer>, useful for saying "here are all the points that have moveable blots"
 * Has no privacy, is just for convenience (for testing routines)
 * 
 * Might want to have info about how many are on each of those points, someday.
 * Might want to not be integer but some kind of "Loc" class.
 * Hmmm, not thread safe?? But doesn't currently allowing changing?
 * 
 * @author (Mike Roam) 
 * @version (2012 Jan 30)
 */
public class LocList
{
    // instance variables - replace the example below with your own
    /* private */ ArrayList<Integer> myList;

    /**
     * Constructor for objects of class LocList
     */
    public LocList()
    {
        //myList = starter.clone( );// 
        myList = new ArrayList<Integer>( );
    }

    
    /**
     * get element 'y' of my list (0 is the first one, just like arrays)
     */
    public Integer get(int y)
    {
        return myList.get(y);
    }

    
    /**
     * tells us if myList is empty, duh
     */
    public boolean isEmpty()
    {
        // put your code here
        return myList.isEmpty( );
    }

    
    /**
     * how many elements in the list 
     */
    public int size()
    {
        // put your code here
        return myList.size( );
    }
    
    
    public String toString( ) {
        return myList.toString( );
    }
    
} /* class LocList */
