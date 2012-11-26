import java.util.Random;

/**
 * So that we can work with theoretical rolls (if we get roll X then ...)
 *
 * There are 2 dice but how about when we roll doubles we think of them as being 4 dice?
 * Or merely use a partialMovesCountDown? Stored in here?
 * There's a parallel array in here marking which dice have been used so far.
 * 
 * hmmm, getUsedDiceHowMany & numOfPartialMovesAvail & allDiceAreUsed( )
 * were working more or less in parallel so I reconciled them by commenting
 * out all "usedDiceHowMany" and kept "allDiceAreUsed( )" for its handiness. 
 * The only way you can now decrease the number
 * of PartialMovesAvail is by marking a die as used (which you can do by
 * its die number or by its roll value).
 * 
 * @author (Mike Roam) 
 * @version (2011 Dec 14)
 */
public class Dice {
    /* if you add more fields here, add them to the copy constructor, also! 
    Beware deep copy if the new fields hold (pointers to) objects! */
    private int[ ] dice = new int[howManyDice];
    /* beware! Outside world talks about getUsed(1) which refers to used[0], 
     * getUsed(2) means used[1], etc */
    private boolean[ ] used = new boolean[maxMovesCount]; /* was called "used_move". 
    Too small for collection? */
    private int numOfPartialMovesAvail = 0; /* 4 for doubles, 2 otherwise. 
    Counts down as dice are used. */
    //private int usedDiceHowMany = 0; /* counts up as dice are used */

    /* old code for used: usedDice == 1 means first dice has been used
    usedDice == 2 means second dice has been used
    usedDice == 0 means no die have been used yet
    numOfPartialMovesAvail keeps a countdown of total number of moves available, 
    from 4 when doubles.  */

    private boolean rolled = false;
    private static final Random rdice = new Random(); /* random number generator, 
    gets started in constructor. */

    static final int howManyDice = Board.howManyDice; 
    /*2. gy suggests having 4 but only using 2 unless you get doubles */

    static final int maxMovesCount = Board.maxMovesCount; 
    /* 4. Bonus for getting doubles. Would be diff with more dice. */

    static final int NO_SUCH_DIE = -9; /* if someone asks unrolled dice which one 
    is highest roll */

    static final int UNROLLED = 0; /* for dice in mid-air? */
    static final int minDieVal = 1;
    static final int maxDieVal = 6;

    /**
     * Default constructor, leaves dice unrolled
     */
    public Dice( ) {
        // dice haven't rolled yet, are all zero!
        //rdice = new Random(); // random number generator
        reset( ); //turns off rolled, unrolls all dice, put usedDice & 
        // numOfPartialMovesAvail to 0
    }

    /**
     * Copy Constructor
     */
    public Dice ( Dice other) {
        for (int i=0; i<howManyDice; ++i) {
            dice[i] = other.dice[i];
        }
        for (int i=0; i<maxMovesCount; ++i) {
            used[i] = other.used[i];
        }
        numOfPartialMovesAvail = other.numOfPartialMovesAvail;
        //usedDiceHowMany = other.usedDiceHowMany;
        rolled = other.rolled;
        //rdice = other.rdice; /* not copying this, just linking to their generator. 
        // We don't need another, do we? */
    } /* Copy Constructor */

    /**
     * Constructor for objects of class Dice with only 2 dice. 
     * If we get more dice, this has to die.
     * Only accepts rolls of minDiceVal .. maxDiceVal  or Dice.UNROLLED
     * Should it accept 0's so I can pass around unrolled dice? I guess so.
     * Figures out rolled status based on the values it gets for the Dice:
     * Either both dice have values in the "rolled" range or both must be UNROLLED:
     * This will throw exception if one diceValue is UNROLLED while other is in rolled range!
     */
    public Dice(int newDie1, int newDie2/*, boolean newRolled*/) 
    /* throws IllegalArgumentException, ArrayIndexOutOfBoundsException */
    {
        if (! (howManyDice == 2)) {
            throw new IllegalArgumentException("Can't use the 2 dice constructor " 
                + "because we have " + howManyDice + " dice!");
        }
        if (! legitDiceValue( newDie1 ) ) {
            throw new IllegalArgumentException("dice1 is given bad value '" + newDie1 
                + "' not 0 and not [" + minDieVal + ".." + maxDieVal + "]");
        }
        if (! legitDiceValue( newDie2 )) {
            throw new IllegalArgumentException("dice2 is given bad value '" + newDie2 
                + "' not 0 and not [" + minDieVal + ".." + maxDieVal + "]");
        }
        if ((newDie1 != newDie2) && ((newDie1 == UNROLLED) || (newDie2 == UNROLLED))) {
            throw new IllegalArgumentException("Bad dice pair '" + newDie1 + "," + newDie2 
                +"', must both be UNROLLED or both be" + minDieVal + ".." + maxDieVal);
        }
        /* so either dice are both unrolled, or both rolled, and it makes no 
         * difference which one tells us */ 
        rolled = (newDie1 != UNROLLED);
        dice[0] = newDie1;
        dice[1] = newDie2;
        //rdice = new Random(); // random number generator, gets started in constructor.
        resetUsedDice( ); /* calls        resetNumOfPartialMovesAvail( ); */
    } /* constructor */

    /**
     * allows UNROLLED or [minDiceVal to maxDiceVal]
     */
    static boolean legitDiceValue(int dieVal) {
        return (((minDieVal<= dieVal) && (dieVal <= maxDieVal)) || (dieVal == UNROLLED));
    } /* hasLegitDiceValue( ) */

    /**
     * for specifying which DIE we're talking about, not the value on a face of a die!
     */
    static boolean legitDieNum(int dieNum) {
        return ((1 <= dieNum) && (dieNum <= howManyDice));
    }

    /**
     * convenience method.
     * Note: there is no dice0
     */   
    public int getDie1() {
        return dice[0];
    } 

    /**
     * convenience method
     * Note: there is a dice2, and there is no dice0
     */   
    public int getDie2() {
        return dice[1];
    } 

    /**
     * Dice are named "1", "2" ... up to howManyDice (there is no dice 0!)
     * corresponding to hidden array dice[0], dice[1], respectively
     */
    public int getDie( int whichDie) {
        // should we allow whichDie to be 3 or 4 when we've got doubles??
        // Probably not.
        if (! legitDieNum(whichDie)) {
            throw new IllegalArgumentException("bad die number '" + whichDie 
                + "', should be 1.." + howManyDice);
        }
        return dice[whichDie - 1]; 
    } /* getDice(int ) */

    /**
     * returns the value of the lowest die.
     * will be 0 (??) if dice unrolled?
     * See whichUnusedDieIsHighest( ) to find out which Die holds this value.
     * [ ]This ought to be reconciled with whichUnusedDieIsLowest( ) which says
     * WHICH die is lowest instead of giving its value. They could use the same code
     * and then one returns value instead of position. Ditto for highest.
     */
    public int lowestUnusedRoll( ) {
        if (!rolled) {
            return UNROLLED;
        } else if ( allDiceAreUsed( ) ) {
            return NO_SUCH_DIE;
        } else { // see highestUnusedRoll which tries to be more efficient
            // by having a special shortcut for doubles
            int lowestRoll = maxDieVal;
            for (int i=0; i<howManyDice; ++i) {
                if ((!used[i]) && (dice[i] < lowestRoll)) {
                    lowestRoll = dice[i];
                };
            }
            return lowestRoll;
        }
    } /* highestRoll( ) */

    /**
     * returns the value of the highest (roll value) die.
     * will be 0 (??) if dice unrolled?
     * See whichUnusedDieIsHighest( ) to find out which Die holds this value.
     */
    public int highestUnusedRoll( ) {
        if (!rolled) {
            return UNROLLED;
        } else if ( allDiceAreUsed( ) ) {
            return NO_SUCH_DIE;
        } else if (! isDoubles( ) ) {
            int highestRoll = 0;
            for (int i=0; i<howManyDice; ++i) {
                if ((!used[i]) && (dice[i] > highestRoll)) {
                    highestRoll = dice[i];
                };
            }
            return highestRoll;
        } else { // isDoubles( )
            return dice[0]; // they're all the same in doubles so it doesn't matter
            // getDie( whichDieIsUnused( ) );
        }
    } /* highestRoll( ) */

    /**
     * unlike lowestRoll, this tells us WHICH Unused die has the lowest value.
     */
    public int whichUnusedDieIsLowest( ) {
        if (!rolled) {
            return UNROLLED;
        } else if (allDiceAreUsed( ) ) {
            return NO_SUCH_DIE;
        } 
        if (! isDoubles( ) ) {
            int whereLowest = 0;
            int lowestRoll = maxDieVal;
            for (int i=0; i<howManyDice; ++i) {
                if ((!used[i]) && (dice[i] < lowestRoll)) {
                    whereLowest = i;
                    lowestRoll = dice[i];
                };
            }
            return whereLowest+1; /* beware OBOB! user expects these dice to be named 1 & 2 */
        } else {
            return whichDieIsUnused( );
        }
    } /* whichDieIsHighest( ) */

    /**
     * unlike highestRoll, this tells us WHICH Unused die has the highest value.
     * Will fail on doubles.
     */
    public int whichUnusedDieIsHighest( ) {
        if (!rolled) {
            return UNROLLED;
        } else if (allDiceAreUsed( ) ) {
            return NO_SUCH_DIE;
        }
        if (! isDoubles() ) {
            int whereHighest = 0;
            int highestRoll = 0;
            for (int i=0; i<howManyDice; ++i) {
                if ((!used[i]) && (dice[i] > highestRoll)) {
                    whereHighest = i;
                    highestRoll = dice[i];
                };
            }
            return whereHighest+1; /* beware OBOB! user expects these dice to be named 1 & 2 */
        } else {
            return whichDieIsUnused( );
        }
    } /* whichDieIsHighest( ) */

    /**
     * returns the first unused die number (WHICH die, not its roll value), 
     * range 1..maxMovesCount
     * working its way up from lowest numbered dice
     */
    public int whichDieIsUnused( ) {
        if (!rolled) {
            return UNROLLED;
        } else if (allDiceAreUsed( ) ) {
            return NO_SUCH_DIE;// throw new ...
        }
        int highestUsedSpot = howManyDice;
        if (isDoubles( )) {
            highestUsedSpot = maxMovesCount;
        }
        for (int i=0; i<highestUsedSpot; ++i) {
            if (!used[i]) {
                return i + 1;/* beware OBOB! user expects these dice to be named 1 & 2 */
            };
        }
        return NO_SUCH_DIE;
    }

    /**
     * This for setting a specified individual die.
     * Can't use this to set first two dice at once! Use "roll(5,6)" to do that.
     * This expects dice numbers 1 or 2 (not array indices 0,1!!)
     * A changed die is marked as unused, and the DoubletCountdown starts over from the top!
     */
    public void setDie( int whichDie, int newRoll ) {
        if (! legitDieNum(whichDie)) {
            throw new IllegalArgumentException("Can't set value of dice#'" 
                + whichDie + "', we only have dice#1.." + howManyDice);
        }
        if (! legitDiceValue(newRoll)) {
            throw new IllegalArgumentException("Bad dice value '" + newRoll 
                + "', our dice only can roll values " + minDieVal + ".." + maxDieVal);
        }
        if (dice[whichDie - 1] != newRoll) { /* okay, changing a die */
            dice[whichDie - 1] = newRoll;
            used[whichDie - 1] = false;    
            resetNumOfPartialMovesAvail( );  // in case we've acquired doubles
            if ((! rolled) && (allDiceHaveValues())) { //changing to "rolled" status!
                rolled = true;
                resetUsedDice( );  // calls resetNumOfPartialMovesAvail( );
            } // changed rolled status
        } // changed a die
    } /* setDie( ) */

    /**
     * I don't want to have a setRolled( ) because I think this is better: 
     * this rolls all dice and sets rolled to true.
     * To invalidate the dice, use "reset( )"
     * Unfortunately, if receiving a networked roll, we need to set rolled to true, I guess.
     * Note: there is a version of this for setting two specific dice values "roll(int,int)" 
     * which is called by this.
     */
    public void roll( ) {
        int newDie1 = rdice.nextInt(maxDieVal) + minDieVal;
        int newDie2 = rdice.nextInt(maxDieVal) + minDieVal;
        roll(newDie1,newDie2); // this would have to pass array if I don't have 2 dice??
        if (howManyDice > 2) {
            throw new IllegalArgumentException("I don't know how to handle more than 2 dice.");
            //             for (int i=0; i<howManyDice; ++i) {
            //                 dice[i] = rdice.nextInt(maxDieVal) + minDieVal;
            //             }
        }
        //     System.out.println("I just rolled the dice and got " + this.toString( ) + "!");
    }

    /**
     * changes the (first two) dice to specified values
     */
    public void roll(int newRoll1, int newRoll2 ) {
        if (howManyDice > 2) {
            throw new IllegalArgumentException("I don't yet know how to handle" 
                + "more than 2 dice.");
        }
        if (! legitDiceValue(newRoll1)) {
            throw new IllegalArgumentException("Bad dice value '" + newRoll1 
                + "', our dice only can roll " + minDieVal + ".." + maxDieVal);
        }
        if (! legitDiceValue(newRoll2)) {
            throw new IllegalArgumentException("Bad dice value '" + newRoll1 
                + "', our dice only can roll " + minDieVal + ".." + maxDieVal);
        }

        dice[0] = newRoll1;
        dice[1] = newRoll2;
        rolled = true;
        resetUsedDice( ); // calls resetNumOfPartialMovesAvail( );
        // System.out.println("I changed the rolled dice to " + this.toString( ) + "!");
    } /* roll( ) */

    /**
     * If we want to manually set the dice values, set them all before setting "rolled" to true
     * because I will squawk if some of the dice don't have values (are UNROLLED).
     * It's probably easiest to just call roll(newDie1val, newDie2val)
     * (or do what roll(int,int) does: call roll( ) and then change some of their values.
     * 
     * Hmmm, if the dice are already rolled, this has no effect, 
     * (doesn't reset the used and numOfPartialMovesAvailcountdown)
     */
    public void setRolled(boolean newRolled) {
        if (newRolled == false) {
            rolled = false;
            reset( );
        } else if (allDiceHaveValues()) {
            if (!rolled) { /* if this is a change ... */
                rolled = true;
                resetUsedDice( ); /* calls             resetNumOfPartialMovesAvail( ); */
            }
        } else {
            throw new IllegalArgumentException("Uh-oh: dice think they are rolled but "
                + "some don't have values.");
        }
    }

    /**
     * checks for no dice are UNROLLED value
     */
    public boolean allDiceHaveValues( ) {
        boolean allDiceAreRolled = true;
        for (int i=0; i<howManyDice; ++i) {
            if (dice[i] == UNROLLED) {
                allDiceAreRolled = false;
            };
        }
        return allDiceAreRolled;
    }

    /**
     * Supposedly, when rolled is true, all dice have values from minDiceVal..maxDiceVal.
     * Use "roll( )" to roll again, use "reset( )" to blank them all out and set rolled 
     * to false.
     */
    public boolean getRolled( ) {
        if (rolled) {
            if (allDiceHaveValues( )) {
                return true;
            } else {
                throw new IllegalArgumentException("Uh-oh: dice think they are rolled " 
                    + "but some don't have values.");
            }
        }
        return rolled;
    }

    /**
     * gets the private "rdice" random number generator.
     * Why should anybody need it? I don't know, but I'm revealing it
     * for unit testing. Might be null!
     */
    public Random getRDice( ) {
        return rdice;
    }

    /**
     * Tells us if specified die has been used.
     * note: if I get doubles, I'm keeping track of 4 usable dice!
     * Users are speaking in terms of die#1 and die#2 which use our private used[0] 
     * and used[1] respectively.
     * And I'm sneaking up on the idea of coding doubles as 4 (identical) dice 1..4, 
     * so allowing up to 4 here.
     */
    public boolean getUsedDie( int newUsedDie ) {
        if ( ! ((1<= newUsedDie) && (newUsedDie <= maxMovesCount)) ) {
            throw new IllegalArgumentException("bad newUsedDie '" + newUsedDie 
                + "', should be 1.." + maxMovesCount);
        }
        return used[newUsedDie-1];
    }

    /**
     * Says how many of the dice have been 'used' so far.
     * Being replaced by getNumOfPartialMovesAvail( )
     */
    //     public int getUsedDiceHowMany(  ) {
    //         //int howMany = 0;
    //         //for (int i=0; i<maxMovesCount; ++i) { if (used[i]) { howMany++; } }
    //         return usedDiceHowMany;
    //     } /* getUsedDiceHowMany( ) */

    /**
     * Special for doubles: every die has same value, so just returning "1"
     * Be careful about using this with doubles since it won't correspond
     * to which potential moves are which.
     * Note: in the outside world the die numbers are 1,2[,3,4 for doubles]
     * but in here the arrays are numbered 0,1[,2,3].
     * 
     * @return     which die has this value. NO_SUCH_DIE (-9) if none.
     */
    public int whichDieHasValue(int rollVal) {
        if ( ! ((minDieVal<= rollVal) && (rollVal <= maxDieVal)) ) {
            throw new IllegalArgumentException("bad rollVal '" + rollVal 
                + "', should be " + minDieVal + "..." + maxDieVal);
        }
        for (int i=0; i<howManyDice; ++i) { 
            if (dice[i] == rollVal) { 
                return i+1; // beware OBOB
            } 
        }
        return NO_SUCH_DIE; // -9
    }

        /**
         * Suppose I roll 5,6 and want to say that I've used the Ò5Ó
         * unlike ÒsetUsedDieÓ, this works from roll value, Not which die youÕre talking about.
         * If we have doubles (e.g. 5,5) then it doesn't matter which we use, so we
         * can use one that isnÕt used yet OUT OF FOUR.
         * 
         * gy suggestion 2012 (heÕs full of Õem!): return a boolean indicating success.
         * hmmm, if Dice were a hashmap or something we could quick search for whichVal.
         */
        public void setUsedValue( int whichVal ) { 
            if ( ! ((minDieVal<= whichVal) && (whichVal <= maxDieVal)) ) {
                throw new IllegalArgumentException("bad whichVal '" + whichVal 
                    + "', should be " + minDieVal + "..." + maxDieVal);
            }
            if ( allDiceAreUsed( ) ) {
                throw new IllegalArgumentException("All dice are already used! " 
                    + "CanÕt setUsedValue( ), duh!");
            }
            if ( isDoubles( ) ) {
                if (whichVal == dice[0]) {
                    setUsedDie( whichDieIsUnused( ), true );
                } else {
                    throw new IllegalArgumentException("CanÕt setUsedValue(" + whichVal 
                        + "), because no dice have that value!");
                }
            } else {
                for (int i=0; i<howManyDice/*maxMovesCount*/; ++i) {
                    if (dice[i] == whichVal) {
                        if (! used[i]) {
                            setUsedDie( i+1, true); 
                            // better than "used[i] = true;" because it also 
                            // numOfPartialMovesAvail--;
                            return;
                        } // else keep looking for another
                        //throw new IllegalArgumentException("CanÕt setUsedValue("
                        // + whichVal + "), because no dice have that value!");
                        //}
                    }
                }
                //return NO_SUCH_DIE;
                throw new IllegalArgumentException("CanÕt setUsedValue(" + whichVal 
                    + "), because no unused dice have that value!");
            }
        }

        /**
         * convenience method
         */
        public boolean allDiceAreUsed( ) {
            return (numOfPartialMovesAvail < 1);
        }

        /**
         * Tell a specific die (by die#, not by value) that it has been used.
         * If this is news (if the die is not already marked used) then this decreases
         * numOfPartialMovesAvail (and formerly increased usedDiceHowMany).
         * 
         * doMove and doPartialMove might be calling this when they know which die they're using,
         * but now that there is a setUsedValue( ) method, they could call setUsedValue( ) instead
         * of this setUsedDie( ), without caring which die holds the used value. Handy!
         */
        public void setUsedDie(int newUsedDie, boolean newUsedTF) {
            /* Beware! Outside users talk about die1 and die2 while in here 
             * we say used[0] & used[1] */
            if ( ! ((0<= newUsedDie) && (newUsedDie <= maxMovesCount)) ) {
                throw new IllegalArgumentException("bad newUsedDie '" + newUsedDie 
                    + "', should be 0, 1, or 2");
            }
            if ( isDoubles( ) ) { // does not matter which die we use up?
                if (used[numOfPartialMovesAvail-1] != newUsedTF) {
                    used[numOfPartialMovesAvail-1] = newUsedTF;
                    if (newUsedTF) {
                        //usedDiceHowMany++;
                        numOfPartialMovesAvail--;
                    } else {
                        //usedDiceHowMany--;
                        numOfPartialMovesAvail++;
                    }
                }   
            } else if (used[newUsedDie-1] != newUsedTF) { // change!
                used[newUsedDie-1] = newUsedTF;
                if (newUsedTF) {
                    //usedDiceHowMany++;
                    numOfPartialMovesAvail--;
                } else {
                    //usedDiceHowMany--;
                    numOfPartialMovesAvail++;
                }
            }
        } /* setUsedDie( ) */

        /**
         * Puts the dice into unrolled state without dice values, ready for new roll.
         */
        public void reset( ) {
            for (int i=0; i<howManyDice; ++i) {
                dice[i] = UNROLLED;
            }
            rolled = false;
            resetUsedDice( ); /* calls   resetNumOfPartialMovesAvail( ); */
        }

        /**
         * Tells all the dice that they are unused.
         * (For doubles, tells four of them, otherwise just 2)
         * This also resetNumOfPartialMovesAvail( ) so don't make it in turn call us!
         */
        public void resetUsedDice( ) {

            for (int i=0; i<maxMovesCount; ++i) {
                used[i] = false;
            }
            if (! isDoubles() ) { /* disallow the higher dice */
                for (int i=howManyDice; i<maxMovesCount; ++i) {
                    used[i] = true;
                }
            }
            //usedDiceHowMany = 0;
            resetNumOfPartialMovesAvail( );
        }

        public boolean isDoubles( ) {
            return ((dice[0] == dice[1]) && (dice[0] != 0));
        } /* isDoubles */

        /**
         * Tells the dice that one of the doublet rolls has been used.
         * Can only be used when dice are doubles, 
         * Otherwise use "setUsedDie(int whichDie, true);" to mark a move as used!
         * 
         * New approach, 2012apr25: whether you have doubles or not, just tell
         * dice that they're used, which will cause numOfPartialMovesAvail to decrease
         * and DON'T let anybody else decrease it. Making this uncallable to force
         * all calls to become syntax error so I can find and replace them.
         */
        //     public void was_numOfPartialMovesAvailDecrease( ) /* was doubletCountdown( ) */{
        //         if (! isDoubles( )) {
        //             System.err.print("'numOfPartialMovesAvailDecrease( )' can only be used");
        //             System.err.print(" when dice are doubles. 
        //             Otherwise use 'setUsedDie(int whichDie, true);'");
        //             System.err.println("to mark a move as used!");
        //             throw new IllegalArgumentException("numOfPartialMovesAvailDecrease( ) 
        //             called when not doubles rolled");
        //         }
        //         ?? So was just marking off the dice one by one??
        //         setUsedDie(usedDiceHowMany,true); // causes numOfPartialMovesAvail--
        //         if ( numOfPartialMovesAvail < 0 ) {
        //             throw new IllegalArgumentException("bad: numOfPartialMovesAvail went negative!");
        //         }
        //     }

        public int getNumOfPartialMovesAvail() {
            return numOfPartialMovesAvail;
        }

        /**
         * Sets the numOfPartialMovesAvail to a specified value, unlike
         * resetNumOfPartialMovesAvail( ) which sets it to 2 or 4 as appropriate
         * (and 0 if the dice aren't rolled).
         */
        public void setNumOfPartialMovesAvail(int newNumOfPartialMovesAvail) {
            System.err.println("hi, who is doing setNumOfPartialMovesAvail( )?");
            Thread.dumpStack( ); // sends it to standard error

            if (! (( 0 <= newNumOfPartialMovesAvail) 
                && (newNumOfPartialMovesAvail <= maxMovesCount))) {
                throw new IllegalArgumentException("bad newNumOfPartialMovesAvail '" 
                    + newNumOfPartialMovesAvail + "', can only be 0.." + maxMovesCount);
            }
            numOfPartialMovesAvail = newNumOfPartialMovesAvail;
        }

        /**
         * restarts the countdown, so don't use this willy nilly!
         * Is called by resetUsedDice, so don't call it!
         */
        private void resetNumOfPartialMovesAvail( ) {
            if (! rolled) {
                numOfPartialMovesAvail = 0;
            } else if ( isDoubles( ) ) /* or rolledBonus( ) */ {
                numOfPartialMovesAvail = maxMovesCount;
            } else {
                numOfPartialMovesAvail = howManyDice;
            }
        }

        /**
         * for printing out info about current board,
         * will be list of unused rollValues (not dice nums:
         * e.g. if die1==5 and die2==6, this returns 5,6 if none are used).
         */
        public String diceRollValuesAvailable( ) {
            if (numOfPartialMovesAvail <= 0) {
                return "none";
            }
            StringBuffer tempStrBuf = new StringBuffer(/*"["*/);
            if (isDoubles( )) { // all die are same
                for (int i=0; i<numOfPartialMovesAvail; ++i) {
                    tempStrBuf.append( dice[0] ); // all same
                    if (i+1<numOfPartialMovesAvail) { tempStrBuf.append(",");}
                }
            } else {
                for (int i=0; i<howManyDice; ++i) {
                    if (! used[i]) {
                        tempStrBuf.append( dice[i] + ",");
                    }
                }
                int size = tempStrBuf.length( );
                tempStrBuf.delete(/*start*/size-1, /*after end*/size); // remove the last comma
            }
            //tempStrBuf.append("]");
            return tempStrBuf.toString( );
        }

        /**
         * returns the dice roll values (only 2)
         * and tells which dice have been used, numbered 1,2,3,4 not 0,1,2,3
         * ?? Will this malfunction on doubles?
         */
        public String toString( ) {
            // StringBuffer temp = new StringBuffer("[");
            // for (int i=0; i<(howManyDice-1); ++i) { temp.append(dice[i] + ","; }
            // temp.append(dice[howManyDice-1] + "]");
            // return temp.toString( );
            StringBuffer tempStrBuf = new StringBuffer("[values available:" + diceRollValuesAvailable( ) + "]");
            tempStrBuf.append("[used:");
            int howManyUsableDice = howManyDice;
            if (isDoubles( )) {
                howManyUsableDice = maxMovesCount;
            }
            if (numOfPartialMovesAvail == howManyUsableDice) {
                tempStrBuf.append("none");
            } else {
                for (int i=0; i<howManyUsableDice; ++i) {
                    if (used[i]) {
                        tempStrBuf.append("die" + (i+1) + ",");
                    }
                }
                int size = tempStrBuf.length( );
                tempStrBuf.delete(/*start*/size-1, /*after end*/size); // remove the last comma
            }
            tempStrBuf.append("]");
            String numOfPartialMovesAvailString = "[" + numOfPartialMovesAvail 
                + " partialMovesAvail]";
            return "[" + dice[0] + "," + dice[1] + "]" + tempStrBuf.toString( ) 
            + numOfPartialMovesAvailString;
        }
        
        /**
         * merely returns 3,1  or 2,2 unlike toString which returns all kinds of info
         * including how many partialMoves remaining and which dice are used
         */
        public String shortString( ) {
            return "" + dice[0] + "," + dice[1];
        }

        /* class Dice */}
