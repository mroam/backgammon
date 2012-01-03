/***************************************************************
JBackgammon (http:// jbackgammon.sf.net)
 
Copyright (C) 2002
George Vulov <georgevulov@hotmail.com>
Cody Planteen <frostgiant@msn.com>
revised 2011 by Joshua Glasser, Julien Soros, Mike Roam

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
****************************************************************/

/**
 * File: JBackgammon.java
 *
 * Description: This file contains the guts of the main program.  All drawing,
 * control, and rule-checking occurs here. 
 */

 

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.Random;
import java.util.*;  // provides Collections


public class JBackgammon extends JFrame implements ActionListener, CommunicationAdapter
{
    static final String VERSION = "1.4";
    public static final long serialVersionUID = 1L; // mjr, version 1
    
    static final int neutral = 0;
    static final int white = 1;
    static final int black = 2;

    // Color to be used when drawing a white checker
    static final Color clr_white = new Color(200, 200, 200);
    // Color to be used when drawing a black checker
    static final Color clr_black = new Color(50, 50, 50);

    // Color to be used when drawing a white point
    static final Color point_black = new Color(130, 70, 0);
    // Color to be used when drawing a black point
    static final Color point_white = new Color(240, 215, 100);

    // Buffers used for double buffering
    BufferedImage b_bimage;
    Graphics2D g_buffer;
    static final int x_offset = 20;
    static final int y_offset = 60;
    Board myBoard = null; // this gets set up
    AI myAI;
    

    // When moving, the original position of the checker
    private int old_point;

    // The current player
    private int current_player = white;

    private int used_move = 0;
    /* used_move == 1 means first dice has been used
     * used_move == 2 means second dice has been used
     * used_move == 0 means no die have been used yet
     */

    // The move possible with each dice
    // Positions:
    // 1 - 24 = points, 1 being on the beginning of the black quarter
    //-1 = bar
    // 0 = black bear off
    // 25 = white bear off
    private int potmove1, potmove2;
    // comment in supermove( ) says "/* In networked mode: 25 = to bar, 26 = bear off */


    // If there are doublets, how many doublet moves remain
    int doublet_moves;

    // This contains some booleans about the status of the game
    Status status;

    // Class that performs the network operations
    Communication comm = null;

    // Textfield used for typing messages
    JTextField msg_input;

    // TextArea used to display messages between the players
    JTextArea msg_display;

    // Scroll pane to provide scrolling capabilities for messages
    JScrollPane msg_scrollpane;

    // The buttons the gui uses for various purposes
    FixedButton FButton[] = new FixedButton[9];
    /* FButton # ... Purpose
    * ------------------------------------------------------------
    * 0 ............ Cancel Move
    * 1 ............ Roll Dice
    * 2 ............ Bear Off
    * 3 ............ Potential Move 1
    * 4 ............ Potential Move 2
    * 5 ............ Connect (only if networked)
    * 6 ............ Send Message (only if networked)
    * 7 ............ New Game */

    // Button labels
    static final String CANCEL = "Cancel Move";
    static final String ROLL_DICE = "Roll Dice";
    static final String BEAR_OFF = "Bear Off";
    static final String MOVE1 = "M1";
    static final String MOVE2 = "M2";
    static final String CONNECT = "Connect";
    static final String SEND_MSG = "Send Message";
    static final String NEW_GAME = "New Game";
    static final String COMPUTER_MOVE = "Computer Move";
    


    /*=================================================
     * Game-related Methods 
     * ================================================*/

    private void debug_msg(String dmsg)
    {
            /*System.out.println("----------------");
            System.out.println("Breakpoint " +  dmsg);
            System.out.println("status.point_selected = " + status.point_selected + "   old_point = " + old_point);
            System.out.println("current_player = " + current_player + "   used_move = " + used_move);
            System.out.println("potmove1 = " + potmove1 + "   potmove2 = " + potmove2);
            System.out.println("doublet_moves = " + doublet_moves + " doublets = " + status.doublets);
            // System.out.println("networked = " + status.networked + "  observer = " + status.observer);
            /// System.out.println("Number of black = " + myBoard.getBlack());
            // System.out.println("Number of white = " + myBoard.getWhite());
            System.out.println("----------------");
            System.out.println();*/
    } // debug_msg


    /**
     * Selects a point and shows the possible moves
     */ 
    public void handlePoint(int point)
    {
        int xpos;
        int ypos;
        int y_comp;

        debug_msg("HandlePoint() is beginning");
        debug_data("HandlePoint called with point=",point);
        // The player cannot move the other's pieces
        if (myBoard.getColorOnPoint(point)==current_player && !status.point_selected)
        {
            // Get the possible moves from that point
            if (current_player==white)
            {
                potmove1 = point + myBoard.getDice1();
                potmove2 = point + myBoard.getDice2();

                // If the player can make no other moves, allow him
                // to bear off with rolls larger than what is needed to bear off
                if (needsInexactRolls())
                {
                    if (potmove1 > 25)
                    {
                        potmove1 = 25;
                    }
                    if (potmove2 > 25) 
                    {
                        potmove2 = 25;
                    }
                }
            } else if (current_player==black) {
                potmove1 = point - myBoard.getDice1();
                potmove2 = point - myBoard.getDice2();

                // If the player can make no other moves, allow him
                // to bear off with rolls larger than what is needed to bear off
                if (needsInexactRolls())
                {
                    if (potmove1 < 0)
                    {
                        potmove1 = 0;
                    }
                    if (potmove2 < 0)
                    {
                        potmove2 = 0;
                    }
                }
            } // player white/black

            // If a move is valid, enable the button to move to it
            if (checkFair(potmove1) && used_move!=1)
            {
                if ((potmove1<25) && (potmove1>0))
                {
                    FButton[0].setEnabled(true); // cancel move
                    FButton[3].drawOnPoint(potmove1); // potential move 1
                    status.point_selected = true;
                } else {
                    // The possible move leads to bearing off
                    FButton[0].setEnabled(true); // cancel move
                    FButton[2].setEnabled(true); // bear off
                    status.point_selected = true;
                }
            } // end if move1 is valid
            
            if (checkFair(potmove2) && used_move!=2)
            {
                if ( (potmove2<25) && (potmove2>0) )
                {
                    FButton[0].setEnabled(true); // cancel move
                    FButton[4].drawOnPoint(potmove2); // potential move 2
                    status.point_selected = true;
                } else {
                    // The possible move leads to bearing off
                    FButton[0].setEnabled(true); // cancel move
                    FButton[2].setEnabled(true); // bear off
                    status.point_selected = true;
                }
            } // if move2 is valid
            old_point = point;
        }
        debug_msg("handlePoint() is ending");
    } // handlePoint( )
    

    /** 
     * Handle moving from one point to another
     * new_move - the new position to move to
     * move - which dice is being used, the first one or the second one
     */
    private void superMove(int new_move, int move)
    {
        /* In networked mode:
         * 25 = to bar
         * 26 = bear off */
        debug_msg("superMove()");

        boolean switchedplayers = true;

        // If the new space is empty, make the move
        // Else send the opponent on the bar first
        if ((myBoard.getColorOnPoint(new_move)==current_player) || (myBoard.getColorOnPoint(new_move)==neutral))
        {
            move(current_player, old_point, new_move);
            if ( status.networked && (!status.observer) )
            {
                comm.sendmove(old_point, new_move);
            }
        } else { // send the opponent on the bar first
            myBoard.moveToBar(new_move);

            move(current_player, old_point, new_move);

            if (status.networked)
            {
                comm.sendonbar(new_move);
                comm.sendmove(old_point, new_move);
            }
        } // end if move-to is legit (our color or neutral)

        if (!status.doublets)
        {
            // If a move has been made previously,
            // this is the second move, end the player's turn
            if (used_move==1 || used_move==2)
            {
                endTurn();
            } else {
                switchedplayers = false;
                used_move = move;
            }
        } else if (status.doublets) {
            doublet_moves--;
            if (doublet_moves==0)
            {
                endTurn();
            } else {
                switchedplayers = false;
            }
        }

        // Turn off focus on this point
        endMove();
        repaint();
        
        // If this wasn't the player's last move,
        // check if he is still on the bar or if he can make more moves
        if ( ! switchedplayers )
        {
            if (myBoard.onBar(current_player))
            {
                handleBar();
            }
            if (!canMove(current_player))
            {
                forfeit();
            }
        }
    } // superMove( )



    /**
    * Bear off a checker from the current point
    */
    private void bearOff()
    {
        // Remove a checker from the old point
        myBoard.setPoint(old_point, myBoard.getHowManyBlotsOnPoint(old_point)-1, current_player);
        if (current_player==white)
        {
            myBoard.white_bear++;
        } else {
            myBoard.black_bear++;
        }

        if (status.networked)
        {
            comm.sendmove(old_point, 26);
        }

        FButton[2].setEnabled(false);

        boolean won = false; // did someone win
        if (!status.networked)
        {
            won = checkWin(current_player);
        }
        if (status.networked&&(status.observer==false))
        {
            won = checkWin(white);
        }
        if (won)
        {
                endMove();// Disable buttons
                return;// Do nothing if there's a winner
        }

        // Remove the dice we used
        if (!status.doublets)
        {
            // Since a previous move has already occured, we are done
            if (used_move==1 || used_move==2)
            {
                endTurn();
            } else {
                // if you can bear off with both, use smaller dice
                if (((potmove1==25)||(potmove1==0)) && ((potmove2==25)||(potmove2==0)))
                {
                    if (myBoard.getDice1() > myBoard.getDice2())
                    {
                        used_move = 2;
                    } else {
                        used_move = 1;
                    }
                } else if ((potmove1==25)||(potmove1==0)) {
                    used_move = 1;
                } else if ((potmove2==25)||(potmove2==0)) {
                    used_move = 2;
                }
            }
        } else if (status.doublets) {
            doublet_moves--;
            if (doublet_moves==0)
            {
                endTurn();
            }
        }

        // Turn off focus on this point
        endMove();
        repaint();

        if ( ! canMove(current_player) ) 
        {
            forfeit();
        }
    } // bearOff


    /**
    * Handle someone being on the bar
    * Mark possible escapes and forfeit if there are none
    */
    private void handleBar()
    {
        int escape1;
        int escape2;

        if (current_player==white)
        {
            escape1 = myBoard.getDice1();
            escape2 = myBoard.getDice2();
        } else {
            escape1 = 25-myBoard.getDice1();
            escape2 = 25-myBoard.getDice2();
        }

        // Can they escape?
        if ( (used_move!=1) && checkFair(escape1) )
        {
            FButton[3].drawOnPoint(escape1); // potential move 1
            FButton[3].setVisible(true);
            potmove1 = escape1;
            old_point = -1;
            status.point_selected = true;
        }
        if ( (used_move!=2) && checkFair(escape2) )
        {
            FButton[4].drawOnPoint(escape2); // potential move 2
            FButton[4].setVisible(true);
            potmove2 = escape2;
            old_point = -1;
            status.point_selected = true;
        }

        // Nope? Then they forfeit
        if (used_move==0)
        {
            if ( (!checkFair(escape1)) && (!checkFair(escape2)) )
            {
                forfeit();
            }
        } else if (used_move==1)
        {
            if (!checkFair(escape2))
            {
                forfeit();
            }
        } else if (used_move==2)
        {
            if (!checkFair(escape1))
            {
                forfeit();
            }
        }
    } // handleBar



    /**
    * Forfeit the current player's turn
    */
    private void forfeit()
    {
        String msg = "You are stuck, you forfeit your turn.";
        JOptionPane.showMessageDialog(this, msg);
        endTurn();
        repaint();
    } // forfeit( )


    /**
    * Checks if there is a winner
    * If there is one, displays appropriate message
    * Return true if there was a winner, false otherwise
    */
    private boolean checkWin(int color)
    {
        String msg;

        if ( (color==white) && (!status.networked) )
        {
            msg = "White wins";
        } else if ((color==black)&&(!status.networked)) {
            msg = "Black wins";
        } else {
            msg = "You win!";
        }

        if (color==white)
        {
            if (myBoard.white_bear==15)
            {
                if (status.networked)
                {
                    comm.sendlose();
                }
                repaint();
                JOptionPane.showMessageDialog(this, msg);
                return true;
            }
        }

        if (color==black)
        {
            if (myBoard.black_bear==15)
            {
                if (status.networked)
                {
                    comm.sendlose();
                }
                repaint();
                JOptionPane.showMessageDialog(this, msg);
                return true;
            }
        }
        return false;
    } // checkWin( )


    // Roll the dice for the current player
    public void doRoll()
    {
        myBoard.rollDice();
        
        if (status.networked)
        {
            comm.sendroll(myBoard.getDice1(), myBoard.getDice2());
        }

        if (myBoard.getDice1()==myBoard.getDice2())
        {
            status.doublets = true;
            doublet_moves = 4;
        } else {
            status.doublets = false;
        }

        // Turn off roll dice button
        FButton[1].setEnabled(false); // roll dice

        repaint();

        // Check if the player is on the bar
        if (myBoard.onBar(current_player))
        {
            handleBar();
        } else if ( ! canMove(current_player) ) {
            forfeit();
        }
    } // doRoll( )


    // End the current player's turn and start the turn
    // of the other player
    // [ ]probably have to make this public someday, Julien's idea!!
    private void endTurn()
    {
        String msg;
        // Change player
        if (current_player == white)
        {
            current_player = black;
        } else {
            current_player = white;
        }

        // Reset vars, turn off new game button
        used_move = 0;
        myBoard.resetDice();
        myBoard.rolled = false;
        FButton[7].setEnabled(false); // new game
        
        repaint();

        if (!status.networked)
        {
            msg = "Your turn is now over.  Please switch players.";
        } else {
            msg = "Your turn is now over.";
        }

        if (status.networked)
        {
            comm.sendendturn();
            status.observer = true;
        }

        JOptionPane.showMessageDialog(this, msg);

        if (!status.networked)
        {
            startTurn();
        }
            repaint();
    } // endTurn


    // Begins a player's turn
    private void startTurn()
    {
        // Enable roll dice and new game buttons
        FButton[1].setEnabled(true); // roll dice
        FButton[7].setEnabled(true); // new game
        if (status.networked && !status.observer)
        {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "It is now your turn");
        }
    } // startTurn( )


    // Remove focus from a sertain point which has been selected
    // This allows the player to select a new point
    private void endMove()
    {
        status.point_selected = false;
        // Disable potential move buttons
        FButton[3].setVisible(false); // potential move 1
        FButton[4].setVisible(false); // potential move 2
        // Disable Cancel move button
        FButton[0].setEnabled(false); // cancel move
    } // endMove( )

    
    public Board getMyBoard( ) {
        return myBoard;
    }
    
    
    public int getCurrent_player( ) {
        return current_player;
    }
    
    
    
    
    // Which of my pieces are moveable?
    private ArrayList<int> allMoveablePieces( int theColor, JBackgammon myGame )
    {
    	ArrayList<PointLoc> myMovers = new ArrayList<PointLoc>( );
    	if (canOnlyMoveFromBar) {
    		/* only thing we can do is move in from bar,
    		  so how to show that in a list: with a code number? Or a "PointLoc" class?
    		  */
    		  
    		  return
    	}
        int move1, move2;
        // Cycle through all the points
        for (int point = 1; point <=24; point++)
        {
            // Only check points which contain the player's pieces
            if (myBoard.getColorOnPoint(point) == theColor)
            {
                if (theColor==white)
                {
                    move1 = point + myBoard.getDice1();
                    move2 = point + myBoard.getDice2();
                }
                else
                {
                    move1 = point - myBoard.getDice1();
                    move2 = point - myBoard.getDice2();
                }
                if ( (checkFair(move1) && used_move != 1) || (checkFair(move2) && used_move != 2))
                {
                    return true;
                }

                // checkFair() only allows bearing off with exact rolls.
                // If the player has no other option, moving with a roll 
                // greater than needed to bear off is legal
                // White's bearOff move is 25, Black's is 0 
                else if (needsInexactRolls() && (move1 > 25 || move1 < 0 || move2 > 25 || move2 < 0))
                {
                    return true;
                }
            }
        }
        return false;
    } // allMoveablePieces( )
    
    
    /**
    * With doubles we can possibly move 3 pieces in from bar and still have a 4th piece to move,
    * and without doubles then we can move 1 piece in from bar and still have a move left.
    * This says whether we're stuck moving ONLY pieces from the bar.
    * used by "allMoveablePieces( )"
    */
    private boolean canOnlyMoveFromBar( int theColor, JBackgammon myGame ) {
    	if (myBoard.getDice1() == myBoard.getDice2()) { /* doubles! */
    		return ((myGame.getBar( theColor ) > 3);
    	} else {
    		return ((myGame.getBar( theColor ) > 1);
    	}
    } /* canOnlyMoveFromBar( ) */
    
    
    /**
     * Will use methods (below)
     * private boolean checkFair(int new_pos)
     * private boolean canMove(int color)
     * 
     */
    ArrayList<Move> allLegalMoves( int theColor, JBackgammon myGame) throws BadMoveException, BadPartialMoveException, BadBoardException {
        System.out.println("allLegalMoves is totally fake, FIX!!");
        
        /* look at the JBackgammon.handlePoint( ) using every point to find all legal moves. Might also want to check canMove( ) */
        /* for every point ...
         *     if it has a theColor piece... */
        if ( ! canMove( theColor ) ) {
        	return new ArrayList<Move>( ); /*  I will return an ArrayList that has no elements ! */
        }
        ArrayList<int> myPoints = allMoveablePieces(theColor,myGame); /* might be empty 
        
        int newPoint1Start = 10; /* simple game with 2 players starts with black on point 10 */
        handlePoint( newPoint1Start ); /* will discover the potential points we can move to: potmove1, potmove2 WHAT about doubles? potmove3,4?? */
        int endPointA = potmove1; // better check not zero!
        int endPointB = potmove2;
        int dice1 = myBoard.getDice1();
        int dice2 = myBoard.getDice2();

        int newPoint2Start = 0; /* pretending that the first guy is doing both moves */
        int newPoint3Start = 0; 
        int newPoint4Start = 0;
        
        /* Each "move" is a collection of partial moves */
        int endPoint1 = Board.endPointMovingFrom(newPoint1Start, dice1, theColor, myGame.getMyBoard( ));
        System.out.println("for starting at " + newPoint1Start + " with roll:" + dice1 + " estimated move to:" + endPoint1);
        PartialMove fakePartialMove1 = new PartialMove( newPoint1Start, dice1, endPoint1, myGame, theColor );
        
        int endPoint2 = Board.endPointMovingFrom(newPoint2Start, dice2, theColor, myGame.getMyBoard( ));
        System.out.println("for starting at " + newPoint2Start + " with roll:" + dice2 + " estimated move to:" + endPoint2);
        PartialMove fakePartialMove2 = new PartialMove( newPoint2Start, dice2, endPoint2, myGame, theColor );
        
        ArrayList<PartialMove> temp = new ArrayList<PartialMove>();
        temp.add(fakePartialMove1);
        temp.add(fakePartialMove2);
        Move fakeMove1 = new Move(temp, theColor, myGame);
        
        /* building move2 */
        int endPoint3 = Board.endPointMovingFrom(newPoint3Start, dice1, theColor, myGame.getMyBoard( ));
        PartialMove fakePartialMove3 = new PartialMove( newPoint3Start, dice1, endPoint3, myGame, theColor );
        int endPoint4 = Board.endPointMovingFrom(newPoint4Start, dice2, theColor, myGame.getMyBoard( ));
        PartialMove fakePartialMove4 = new PartialMove( newPoint4Start, dice2, endPoint4, myGame, theColor  );
       
        temp.clear( );
        temp.add(fakePartialMove3);
        temp.add(fakePartialMove4);
        Move fakeMove2 = new Move(temp, theColor, myGame);
        
        /* planning to return a collection of "Move"s */
        ArrayList<Move> bunchOfMoves = new ArrayList<Move>( );
        bunchOfMoves.add(fakeMove1);
        bunchOfMoves.add(fakeMove2);
        return bunchOfMoves;
    } /* allLegalMoves( ) */
    

    // Return whether the current player can place a checker
    // at a certain position
    private boolean checkFair(int new_pos)
    {
        debug_msg("checkFair()");

        // Only positions 0 through 25 are valid moves
        if (new_pos > 25 || new_pos < 0)
        {
            return false;
        }

        // Positions 0 and 25 are bearing off
        if ((new_pos == 25) || (new_pos == 0))
        {
            if (myBoard.canBearOff(current_player))
            {
                return true;
            } else {
                return false;
            }
        }
        else
        {
            // If there is only one checker, the move is legal
            if (1 == myBoard.getHowManyBlotsOnPoint(new_pos))
            {
                return true;
            }
            // If the point is empty or has the user's own checkers, the move is legal
            int pointColor = myBoard.getColorOnPoint(new_pos);
            if ((pointColor==neutral) || (pointColor==current_player))
            {
                return true;
            }
        }

        return false;
    } // checkFair



    // With the current rolls, can the user move anywhere?
    // used to just use "current_player" to determine color
    private boolean canMove( int color)
    {
    	
        int move1, move2;
        // Cycle through all the points
        for (int point = 1; point <=24; point++)
        {
            // Only check points which contain the player's pieces
            if (myBoard.getColorOnPoint(point) == current_player)
            {
                if (color==white)
                {
                    move1 = point + myBoard.getDice1();
                    move2 = point + myBoard.getDice2();
                }
                else
                {
                    move1 = point-myBoard.getDice1();
                    move2 = point-myBoard.getDice2();
                }
                if ( (checkFair(move1) && used_move != 1) || (checkFair(move2) && used_move != 2))
                {
                    return true;
                }

                // checkFair() only allows bearing off with exact rolls.
                // If the player has no other option, moving with a roll 
                // greater than needed to bear off is legal
                // White's bearOff move is 25, Black's is 0 
                else if (needsInexactRolls() && (move1 > 25 || move1 < 0 || move2 > 25 || move2 < 0))
                {
                    return true;
                }
            }
        }
        return false;
    } // canMove( )


    // Returns whether the current player can't move anywhere else
    // and needs to be able to bear off with an inexact roll
    private boolean needsInexactRolls()
    {
        boolean canmove = false;
        int move1, move2;
        // Cycle through all the points
        for (int point = 1; point <=24; point++)
        {
            // Only check points which contain the player's pieces
            if (myBoard.getColorOnPoint(point) == current_player)
            {
                if (current_player==white)
                {
                    move1 = point+myBoard.getDice1();
                    move2 = point+myBoard.getDice2();
                } else {
                    move1 = point-myBoard.getDice1();
                    move2 = point-myBoard.getDice2();
                }
                if ( (checkFair(move1) && used_move != 1) || (checkFair(move2) && used_move != 2))
                {
                    canmove = true;
                }
            }
        }
        if (!canmove && myBoard.canBearOff(current_player))
        {
            return true;
        } else {
            return false;
        }
    } // needsInexactRolls( )


    // Moves checker from one position to another,
    // modifying the board object
    public void move(int color, int old_pos, int new_pos)
    {
        // If the move is coming from a bar, remove it from the bar
        // and add it to the point
        if (old_pos==-1)
        {
            if (color==white)
            {
                myBoard.white_bar--;
            } else {
                myBoard.black_bar--;
            }
            myBoard.setPoint(new_pos, myBoard.getHowManyBlotsOnPoint(new_pos)+1, color);
        } else {
            // Move is coming from another point
            // Decrease the checkers in the old point
            myBoard.setPoint(old_pos, myBoard.getHowManyBlotsOnPoint(old_pos)-1, color);
            if (myBoard.getHowManyBlotsOnPoint(old_pos)==0)
                    myBoard.setPoint(old_pos, 0, neutral);
            // Increase the checkers on the new point
            myBoard.setPoint(new_pos, myBoard.getHowManyBlotsOnPoint(new_pos)+1, color);
        }
    }


    // Initialize the GUI
    // Sets up all the buttons
    public void setupGUI()
    {
        /* FButton # ... Purpose
    * ------------------------------------------------------------
    * 0 ............ Cancel Move
    * 1 ............ Roll Dice
    * 2 ............ Bear Off
    * 3 ............ Potential Move 1
    * 4 ............ Potential Move 2
    * 5 ............ Connect (only if networked)
    * 6 ............ Send Message (only if networked)
    * 7 ............ New Game */
        FButton[0].setBounds(475, 355, 135, 25); // cancel move
        FButton[0].setVisible(true);
        FButton[0].setText(CANCEL);
        FButton[0].addActionListener(this);
        FButton[0].setEnabled(false);

        FButton[1].setBounds(475, 320, 135, 25); // roll dice
        FButton[1].setVisible(true);
        FButton[1].setText(ROLL_DICE);
        FButton[1].addActionListener(this);
        FButton[1].setEnabled(true);

        FButton[2].setBounds(475, 285, 135, 25); // bear off
        FButton[2].setVisible(true);
        FButton[2].setText(BEAR_OFF);
        FButton[2].addActionListener(this);
        FButton[2].setEnabled(false);

        FButton[3].setBounds(650, 490, 9, 10); // potential move 1
        FButton[3].setVisible(true);
        FButton[3].setText(MOVE1);
        FButton[3].addActionListener(this);
        FButton[3].setEnabled(true);

        FButton[4].setBounds(750, 490, 9, 10); // potential move 2
        FButton[4].setVisible(true);
        FButton[4].setText(MOVE2);
        FButton[4].addActionListener(this);
        FButton[4].setEnabled(true);

        FButton[7].setBounds(475, 250, 135, 25); // new game
        FButton[7].setVisible(true);
        FButton[7].setText(NEW_GAME);
        FButton[7].addActionListener(this);
        FButton[7].setEnabled(true);
        
        FButton[8].setBounds(475, 380, 135, 25); // computer move
        FButton[8].setVisible(true);
        FButton[8].setText(COMPUTER_MOVE);
        FButton[8].addActionListener(this);
        FButton[8].setEnabled(true);

        if (status.networked)
        {
            FButton[5].setBounds(475, 225, 135, 25); // connect
            FButton[5].setVisible(true);
            FButton[5].setText(CONNECT);
            FButton[5].addActionListener(this);
            FButton[5].setEnabled(true);

            FButton[6].setBounds(475, y_offset + getInsets().top + 412, 135, 25); // send message
            FButton[6].setVisible(true);
            FButton[6].setText(SEND_MSG);
            FButton[6].addActionListener(this);
            FButton[6].setEnabled(false);

            FButton[1].setEnabled(false); // roll dice
            FButton[7].setEnabled(false); // new game

            msg_input.setBounds(x_offset - getInsets().left, y_offset + getInsets().top + 412, 450, 25);

            msg_scrollpane.setBounds(x_offset - getInsets().left, y_offset + getInsets().top + 327, 593, 80);
            msg_display.setEditable(false);
            msg_display.setLineWrap(true);
            msg_display.setWrapStyleWord(true);
        }
    } // setupGUI


    // Connect to another JBackgammon for network play
    public void connect()
    {
        String input_ip;
        input_ip = JOptionPane.showInputDialog("Enter computer name or IP address");
        FButton[5].setEnabled(false); // connect
        if (input_ip!=null)
        {
            if ( (comm.portBound == 1 ) &&
                (input_ip.equalsIgnoreCase("localhost") || 
                 input_ip.equals("127.0.0.1")) )
            {
                JOptionPane.showMessageDialog(this,
                        "Jbackgammon cannot connect to the same instance of itself");
                FButton[5].setEnabled(true);
            }
            else
            {
                status.clicker = true;
                comm.connect(input_ip);
            }
        } else { // The user canceled, re-enable the connect button
            FButton[5].setEnabled(true);
        }
    } // connect( )


    // Method to send a message through a JOptionPane to the other user
    public void sendMessage()
    {
        String message = msg_input.getText();
        if (message.length()>0)
        {
            comm.sendmessage(message);
            msg_display.append("White player: " + message + '\n');
            // Scroll the text area to the bottom
            msg_display.scrollRectToVisible(new Rectangle(0, msg_display.getHeight(), 1, 1));
        }
        msg_input.setText("");
    } // sendMessage( )


    /*=================================================
     * Network Methods 
     * ================================================*/

    // The network player has won
    public void receiveLose()
    {
        FButton[7].setEnabled(true);
        JOptionPane.showMessageDialog(this, "You lose!");
    } // receiveLose( )


    // Connection lost, reset the board for a new game
    public void disconnected()
    {
        JOptionPane.showMessageDialog(this, "Network connection lost!");
        // Allow the person to connect to someone else
        FButton[5].setEnabled(true);
        // Reset the order of connecting
        status.clicker = false;
        // Start listening for connections again
        comm.listen();
        resetGame();
    } // disconnected( )


    /**
     * Implementing the "connectionRefused( )" method of interface CommunicationAdapter
     * Which says what to do if we could not connect to an ip
     */
    public void connectionRefused()
    {
        JOptionPane.showMessageDialog(this, "Connection refused.\n\nMake sure the computer name/IP is correct\nand that the destination is running JBackgammon in networked mode.");
        status.clicker = false;
        FButton[5].setEnabled(true);
    } // connectionRefused( )


    // The network player has rolled the dice, display them
    public void receiveRolls(int i, int j)
    {
        current_player = black;
        myBoard.setDice(i, j);
        myBoard.rolled = true;
        repaint();
    } // receiverolls( )


    // The non-network player got sent to the bar, update the board
    public void receiveBar(int point)
    {
        myBoard.setPoint(25 - point, 0, neutral);
        myBoard.white_bar++;
        repaint();
    } // receivebar


    // The network player requested a new game, get a response
    public void receiveResetReq()
    {
        int reset = JOptionPane.showConfirmDialog(this, 
            "The network player has requested a new game.\nDo you want to accept?",
            "New Game Request",JOptionPane.YES_NO_OPTION);
        comm.sendResetResp(reset);
        if ( reset == JOptionPane.YES_OPTION )
        {
            resetGame();
        }
    } // receiveResetReq


    // The network player responded to a new game request, process the results
    public void receiveResetResp( int resp )
    {
        if (resp == JOptionPane.NO_OPTION)
        {
            JOptionPane.showMessageDialog(this, "Request for new game denied.");
        }
        else
        {
            JOptionPane.showMessageDialog(this, "Request for new game accepted.");
            resetGame();
            Random r = new Random();
            boolean goesfirst = r.nextBoolean();
            if (goesfirst)
            {
                status.observer = false;
                startTurn();
            } else {
                status.observer = true;
                comm.sendendturn();
            }
        }
    } // receiveResetResp( )


    /**
    * The network player has moved, update the board.
    * ?? Is network player always black??
    */
    public void receiveMove(int oldpos, int newpos)
    {
        if ( (oldpos>0) && (oldpos<25) && (newpos>0) && (newpos<25) )
        {
            move(black, (25 - oldpos), (25 - newpos));
            repaint();
        } else if (newpos==26) {
            myBoard.black_bear++;
            myBoard.setPoint(/*point*/(25 - oldpos), /*howMany*/myBoard.getHowManyBlotsOnPoint((25 - oldpos)) - 1, black);
            repaint();
        } else if (oldpos==-1) {
            myBoard.black_bar--;
            myBoard.setPoint(/*point*/(25-newpos), /*howMany*/myBoard.getHowManyBlotsOnPoint((25-newpos))+1, black);
            repaint();
        }
    } // receiveMove( )


    // The network player has sent an instant message. Display it
    public void receiveMessage(String message)
    {
        msg_display.append("Black player: " + message + '\n');
        // Scroll the text area to the bottom
        msg_display.scrollRectToVisible(new Rectangle(0, msg_display.getHeight(), 1, 1));
    } // receiveMessage( )


    // Connection with an instance of JBackgammon successfully established
    // Start the game
    public void connected()
    {
        FButton[5].setEnabled(false);
        FButton[6].setEnabled(true);

        // The client initiating the connection
        // decides who goes first
        if (status.clicker)
        {
            Random r = new Random();
            boolean goesfirst = r.nextBoolean();
            if (goesfirst)
            {
                status.observer = false;
                startTurn();
            } else {
                status.observer = true;
                comm.sendendturn();
            }
        } else {
            status.observer = true;
        }
        repaint();
    } // connected( )


    // The network player has finished his turn.
    // Start the local player's turn
    public void turnFinished()
    {
        status.observer = false;
        current_player = white;

        myBoard.resetDice();
        myBoard.rolled = false;
        startTurn();
    } // turnFinished( )


    /*=================================================
     * Overridden Methods 
     * (constructor wants boolean re networked? true/false)
     * ================================================*/

    /**
    * JBackgammon class constructor
    * Sets title bar, size, shows the window, and does the GUI
    */
    public JBackgammon(boolean n /* networked true/false */)
    {
        setTitle("JBackgammon");
        setResizable(false);
        status = new Status();
        myBoard = new Board();
        myAI = new AI( this);
        status.networked = n;

        addMouseListener(new tablaMouseListener(this));

        // Call pack() since otherwise getItsets() does not work until the frame is shown
        pack();

        for (int i=0; i < FButton.length; i++)
        {
            FButton[i] = new FixedButton(getContentPane(), this);
        }

        if (status.networked)
        {
            comm = new Communication((CommunicationAdapter)this);
            comm.listen();
            setSize(632, 560);
            // Set up the window for messaging
            getRootPane().setDefaultButton(FButton[6]);
            msg_input = new JTextField();
            getContentPane().add(msg_input);
            msg_display = new JTextArea();
            msg_scrollpane = new JScrollPane(msg_display);
            msg_scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            getContentPane().add(msg_scrollpane);
        } else {
            setSize(632, 480);
        }

        // Set up double buffering
        b_bimage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        g_buffer = b_bimage.createGraphics();

        setupGUI();
        setVisible(true); // was the deprecated "show()";
    } // JBackgammon( ) constructor


    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals(ROLL_DICE))
        {
            doRoll();
        } else if (e.getActionCommand().equals(CANCEL)) {
            status.point_selected = false;
            FButton[0].setEnabled(false);
            FButton[2].setEnabled(false);
            FButton[3].setVisible(false);
            FButton[4].setVisible(false);
            repaint();
        } else if (e.getActionCommand().equals(BEAR_OFF)) {
            bearOff();
        } else if (e.getActionCommand().equals(MOVE1)) {
            superMove(potmove1, 1);
        } else if (e.getActionCommand().equals(SEND_MSG)) {
            sendMessage();
        } else if (e.getActionCommand().equals(MOVE2)) {
            superMove(potmove2, 2);
        } else if (e.getActionCommand().equals(CONNECT)) {
            connect();
        } else if (e.getActionCommand().equals(COMPUTER_MOVE)) {
            try {
                myAI.thinkAndPlay();
            } catch(Exception ex) {
                System.out.println("AI error: " + ex);
            }
        } else if (e.getActionCommand().equals(NEW_GAME)) {
            if ( status.networked )
            {
                int conf = JOptionPane.showConfirmDialog(this,
                    "Send new game request?", "New Game",
                    JOptionPane.YES_NO_OPTION);
                if ( conf == JOptionPane.YES_OPTION )
                {
                    // FIXME: should check for network connection(?)
                    comm.sendResetReq();
                }
            } else {
                int conf = JOptionPane.showConfirmDialog(this,
                            "Start a new game?", "New Game",
                            JOptionPane.YES_NO_OPTION);
                if ( conf == JOptionPane.YES_OPTION )
                {
                    resetGame();
                }
            } // if t/f networked
        } // if newgame
    } // actionPerformed( )


    public void paint(Graphics g)
    {
        // Cast the Graphics to a Graphics2D so actual drawing methods
        // are available
        Graphics2D screen = (Graphics2D) g;
        g_buffer.clearRect(0, 0, getWidth( ), getHeight( ));
        drawBoard( );
        drawBar( );
        drawMen( );
        drawBearStats( );
        drawPipStats( );

        if (myBoard.rolled)
        {
            if (current_player==white)
            {
                drawDice(myBoard.getDice1(), 479, 200, Color.WHITE, Color.BLACK);
                drawDice(myBoard.getDice2(), 529, 200, Color.WHITE, Color.BLACK);
            }
            else
            {
                drawDice(myBoard.getDice1(), 479, 200, clr_black, Color.WHITE);
                drawDice(myBoard.getDice2(), 529, 200, clr_black, Color.WHITE);
            }
        }

        if ( (status.networked) && (! comm.isConnected() ) )
        {
            putString("Waiting for connection...", 15, 50, Color.RED, 15);
        }
        
        // Blit the buffer onto the screen
        screen.drawImage(b_bimage, null, 0, 0);

        FButton[0].repaint();
        FButton[1].repaint();
        FButton[2].repaint();
        FButton[3].repaint();
        FButton[4].repaint();
        FButton[7].repaint();
        FButton[8].repaint();

        if (status.networked)
        {
            FButton[5].repaint();
            FButton[6].repaint();
            msg_input.repaint();
            msg_scrollpane.repaint();
        }
    } // paint( )


    public static void main(String args[])
    {
        JButton ButtonA = new JButton("1P vs. 2P (same computer)");
        JButton ButtonB = new JButton("1P vs. 2P (network)");
        JFrame f = new JFrame("Main Menu");
        JLabel l1 = new JLabel("JBackgammon v" + VERSION);
        JLabel l2 = new JLabel("by Cody Planteen and George Vulov");
        JLabel l3 = new JLabel("http:// jbackgammon.sf.net");

        f.setResizable(false);

        f.addWindowListener(
            new WindowAdapter()
            {
                public void windowClosing(WindowEvent e)
                {
                    System.exit(0);
                }
            }
        );

        ButtonA.addActionListener(new MainMenuListener(f, /* networked */ false));
        ButtonB.addActionListener(new MainMenuListener(f, /* networked */ true));

        JPanel pane = new JPanel();
        pane.setBorder(BorderFactory.createEmptyBorder( /* TLRB */ 20, 20, 20, 20) );
        /* TLRB means "top, left, bot, right" */
        pane.setLayout(new GridLayout(/* rows (height) */ 0, /* cols (width) */ 1));
        pane.add(l1);
        pane.add(l2);
        pane.add(l3);
        pane.add(ButtonA);
        pane.add(ButtonB);
        f.getContentPane().add(pane);
        f.pack();
        f.setVisible(true); // was the deprecated "f.show();"
    } // main( )


    /*=================================================
     * Drawing Methods 
     * ================================================*/

     /** 
      * Gets the X coordinate of the specified point (aka "column" or "spike")
      */
    public int findX(int point)
    {
        if (point<=6)
        {
            return x_offset + 401 - (32*(point-1));
        }
        if (point<=12) 
        {
            return x_offset + 161 - (32*(point-7));
        }
        if (point<=18)
        {
            return x_offset + 1 + (32*(point-13));
        }
        if (point<=24) 
        {
            return x_offset + 241 + (32*(point-19));
        }
        return -1;
    } // findX( )


    public int findY(int point)
    {
        if (point<=12)
        {
            return y_offset;
        }
        if (point<=24)
        {
            return y_offset+361;
        }
        return -1;
    } // findY( )



    public void drawPipStats()
    {
        String m1, m2;
        m1 = "White Pip count: " + myBoard.getWhitePipCount( );
        m2 = "Black Pip count: " + myBoard.getBlackPipCount( );

        g_buffer.setColor(Color.DARK_GRAY);
        g_buffer.fill(new Rectangle2D.Double(/*left*/455, /*top*/168, /*width*/160, /*height*/30));

        putString(m1, 455, 180, Color.WHITE, 12);
        putString(m2, 455, 195, Color.WHITE, 12);
    } // drawPipStats( )




    public void drawBearStats()
    {
        String m1, m2;
        m1 = "White Pieces Beared Off: " + myBoard.white_bear;
        m2 = "Black Pieces Beared Off: " + myBoard.black_bear;

        g_buffer.setColor(Color.BLACK);
        g_buffer.fill(new Rectangle2D.Double(475, 130, 150, 30));

        putString(m1, 455, 150, Color.WHITE, 12);
        putString(m2, 455, 165, Color.WHITE, 12);
    } // drawBearStats( )


    private void putString(String message, int x, int y, Color c, int size)
    {
        g_buffer.setFont(new Font("Arial", Font.BOLD, size));
        g_buffer.setColor(c);
        g_buffer.drawString(message, x, y);
    } // putString( )


    private void drawDice(int roll, int x, int y, Color dicecolor, Color dotcolor)
    {
        g_buffer.setColor(dicecolor);
        g_buffer.fill(new Rectangle2D.Double(x, y, 25, 25));

        g_buffer.setColor(dotcolor);

        switch(roll)
        {
        case 1:
            g_buffer.fill(new Rectangle2D.Double(x+11, y+11, 4, 4));
            break;
        case 2:
            g_buffer.fill(new Rectangle2D.Double(x+2, y+2, 4, 4));
            g_buffer.fill(new Rectangle2D.Double(x+19, y+19, 4, 4));
            break;
        case 3:
            g_buffer.fill(new Rectangle2D.Double(x+2, y+2, 4, 4));
            g_buffer.fill(new Rectangle2D.Double(x+11, y+11, 4, 4));
            g_buffer.fill(new Rectangle2D.Double(x+19, y+19, 4, 4));
            break;
        case 4:
            g_buffer.fill(new Rectangle2D.Double(x+2, y+2, 4, 4));
            g_buffer.fill(new Rectangle2D.Double(x+19, y+19, 4, 4));
            g_buffer.fill(new Rectangle2D.Double(x+19, y+2, 4, 4));
            g_buffer.fill(new Rectangle2D.Double(x+2, y+19, 4, 4));
            break;
        case 5:
            g_buffer.fill(new Rectangle2D.Double(x+2, y+2, 4, 4));
            g_buffer.fill(new Rectangle2D.Double(x+19, y+19, 4, 4));
            g_buffer.fill(new Rectangle2D.Double(x+19, y+2, 4, 4));
            g_buffer.fill(new Rectangle2D.Double(x+2, y+19, 4, 4));
            g_buffer.fill(new Rectangle2D.Double(x+11, y+11, 4, 4));
            break;
        case 6:
            g_buffer.fill(new Rectangle2D.Double(x+2, y+2, 4, 4));
            g_buffer.fill(new Rectangle2D.Double(x+19, y+19, 4, 4));
            g_buffer.fill(new Rectangle2D.Double(x+19, y+2, 4, 4));
            g_buffer.fill(new Rectangle2D.Double(x+2, y+19, 4, 4));
            g_buffer.fill(new Rectangle2D.Double(x+2, y+11, 4, 4));
            g_buffer.fill(new Rectangle2D.Double(x+19, y+11, 4, 4));
            break;
        }
    } // drawDice( )


    /**
    * drawTriangle: Draws a triangle with the point facing downward, 
    * takes in left corner coordinates and a number for color 
    * hooks: status, g_buffer, old_point 
    */
    private void drawTriangle(int x, int y, int point_color)
    {
        if (point_color==1)
        {
            g_buffer.setColor(point_white);
        } else {
            g_buffer.setColor(point_black);
        }

        Polygon tri = new Polygon(new int[] { x, x+15, x+30},  new int[] { y, y+160, y},  3);
        g_buffer.fillPolygon(tri);
        if (status.point_selected)
        {
            debug_data("TRI: Calling getPointNum",0);
            if (old_point == getPointNum(x,y))
            {
                g_buffer.setColor(Color.RED);
                debug_data("TRI: old_point = ",old_point);
            }
        }
        g_buffer.drawPolygon(tri);
    } // drawTriangle( )


    /**
    * drawTriangleRev: Draws a triangle with the point facing downward,
    * takes in left corner coordinates and a number for color
    * hooks: status, g_buffer, old_point 
    */
    private void drawTriangleRev(int x, int y, int point_color)
    {
        if (point_color==0)
        {
            g_buffer.setColor(point_white);
        } else {
            g_buffer.setColor(point_black);
        }

        Polygon tri = new Polygon(new int[]{x,x+15,x+30},  new int[]{y,y-160,y},  3);
        g_buffer.fillPolygon(tri);
        if (status.point_selected)
        {
            debug_data("DEBUG: TRIREV: Calling getPointNum",0);
            if (old_point == getPointNum(x,y))
            {
                g_buffer.setColor(Color.RED);
                debug_data("TRIREV: old_point = ",old_point);
            }
        }
        g_buffer.drawPolygon(tri);
    } // drawTriangleRev


    // Draws the JBackgammon board onto the buffer
    private void drawBoard()
    {
        // Set the green color
        g_buffer.setColor(new Color(0 , 150, 0));

        // Draw the two halves of the board
        Rectangle2D.Double halfBoardA = new Rectangle2D.Double(x_offset, y_offset, 192, 360);
        Rectangle2D.Double halfBoardB = new Rectangle2D.Double(x_offset+238, y_offset, 192, 360);

        g_buffer.draw(halfBoardA);
        g_buffer.fill(halfBoardA);
        g_buffer.draw(halfBoardB);
        g_buffer.fill(halfBoardB);

        // Draw the bar
        g_buffer.setColor(new Color(128,64,0));
        Rectangle2D.Double bar = new Rectangle2D.Double(x_offset+192, y_offset, 46, 360);
        g_buffer.draw(bar);
        g_buffer.fill(bar);

        g_buffer.setColor(Color.WHITE);
        int point_color = 0;

        // Draw the points
        for(int i=0;i<=180;i+=32)
        {
            if (point_color==1)
            {
                point_color = 0;
            } else {
                point_color = 1;
            }

            drawTriangle(x_offset+i, y_offset, point_color);
            drawTriangleRev(x_offset+i, y_offset+360, point_color);

            drawTriangle(x_offset+240+i, y_offset, point_color);
            drawTriangleRev(x_offset+240+i, y_offset+360, point_color);
        }
        debug_data("FINISHED THE SPIKES ",0);
    } // drawBoard( )


    private void drawBar()
    {
        g_buffer.setColor(new Color(100, 50, 0));
        g_buffer.drawRect(x_offset+192,y_offset+120,46,40);
        g_buffer.fill(new Rectangle2D.Double(x_offset+192, y_offset+120, 46, 40));
        g_buffer.fill(new Rectangle2D.Double(x_offset+192, y_offset+200, 46, 40));

        g_buffer.setColor(Color.WHITE);
        g_buffer.fill(new Rectangle2D.Double(x_offset+192, y_offset+160, 47, 40));

        if (myBoard.onBar(white))
        {
            g_buffer.setColor(clr_white);
            g_buffer.fill(new Ellipse2D.Double(x_offset+201, y_offset+205, 29, 29));
            if (myBoard.white_bar>1)
            {
                putString(String.valueOf(myBoard.white_bar), 232, 285, Color.RED, 15);
            }
        }

        if (myBoard.onBar(black))
        {
            g_buffer.setColor(clr_black);
            g_buffer.fill(new Ellipse2D.Double(x_offset+201, y_offset+125, 29, 29));
            if (myBoard.black_bar>1)
            {
                putString(String.valueOf(myBoard.black_bar), 232, 205, Color.RED, 15);
            }
        }
    } // drawBar( )


    private void drawMen()
    {
        debug_msg("drawMen()");
        for (int point=1; point<=12; point++)
        {
            if ( (myBoard.getHowManyBlotsOnPoint(point)>0) && (myBoard.getHowManyBlotsOnPoint(point)<6) )
            {
                for (int i=0; i<myBoard.getHowManyBlotsOnPoint(point); i++)
                {
                    if (myBoard.getColorOnPoint(point)==white) 
                    {
                        g_buffer.setColor(clr_white);
                    } else {
                        g_buffer.setColor(clr_black);
                    }
                    g_buffer.fill(new Ellipse2D.Double(findX(point), findY(point) + i*30, 29, 29));
                }
            }
            if (myBoard.getHowManyBlotsOnPoint(point)>5)
            {
                for (int i=0; i<5; i++)
                {
                    if (myBoard.getColorOnPoint(point)==white)
                    {
                        g_buffer.setColor(clr_white);
                    } else {
                        g_buffer.setColor(clr_black);
                    }
                    g_buffer.fill(new Ellipse2D.Double(findX(point), findY(point) + i*30, 29, 29));
                }
                putString(String.valueOf(myBoard.getHowManyBlotsOnPoint(point)), findX(point)+10, 235, Color.RED, 15);
            }
        } // for point 1..12

        for (int point=13; point<=24; point++)
        {
            if ((myBoard.getHowManyBlotsOnPoint(point)>0) && (myBoard.getHowManyBlotsOnPoint(point)<6))
            {
                for (int i=0; i<myBoard.getHowManyBlotsOnPoint(point); i++)
                {
                    if (myBoard.getColorOnPoint(point)==white)
                    {
                        g_buffer.setColor(clr_white);
                    } else {
                        g_buffer.setColor(clr_black);
                    }
                    g_buffer.fill(new Ellipse2D.Double(findX(point), findY(point) - 30 - i*30, 29, 29));
                }
            }
            if (myBoard.getHowManyBlotsOnPoint(point)>5)
            {
                for(int i=0; i<5; i++)
                {
                    if (myBoard.getColorOnPoint(point)==white)
                    {
                        g_buffer.setColor(clr_white);
                    } else {
                        g_buffer.setColor(clr_black);
                    }
                    g_buffer.fill(new Ellipse2D.Double(findX(point), findY(point) - 30 - i*30, 29, 29));
                }
                putString(String.valueOf(myBoard.getHowManyBlotsOnPoint(point)), findX(point)+10, 255, Color.RED, 15);
            }
        } // for point 13..24
    } // drawMen( )


    public int getPointNum(int point_x, int point_y)
    {
        int quad=0;
        int half=0;
        int i=1;

        debug_data("point_x = ",point_x);
        debug_data("point_y = ",point_y);
        // Find which portion of the board the click occurred in
        if (point_y>=200)
        {
            half = 1;
        }

        if (point_x>=238)
        {
            point_x -= 238;
            debug_data("point_x changed to ",point_x);
            quad = 1;
        }
        debug_data("half = ",half);
        debug_data("quad = ",quad);
        // Find how many times we can subtract 32 from the position
        // while remaining positive
        for ( i=1; point_x >= 32; point_x-=32)
        {
            i++;
        }

        // Compensate for top/bottom and left/right
        if (half==0)
        {
            if (quad==0)
            {
                i = (6-i) + 7;
            } else {
                i = (6-i) + 1;
            }
        } else {
            if (quad==0) 
            {
                i += 12;
            } else {
                i += 18;
            }
        }
        // Useful debug statements
        debug_data("getPointNum returns ",i);
        return i;
    } // getPointNum( )


    public void debug_data( String msg, int data)
    {
        /*
            System.out.print("DEBUG: ");
            System.out.print(msg);
            System.out.println(data);
        */
    } // debug_data( )


    public void resetGame()
    {
        // System.out.println("GAME RESET WAS HIT");
        // Reset JBackgammon data /
        used_move = 0;
        old_point = 0;
        current_player = white;
        doublet_moves = 0;

         /* FButton # ... Purpose
    * ------------------------------------------------------------
    * 0 ............ Cancel Move
    * 1 ............ Roll Dice
    * 2 ............ Bear Off
    * 3 ............ Potential Move 1
    * 4 ............ Potential Move 2
    * 5 ............ Connect (only if networked)
    * 6 ............ Send Message (only if networked)
    * 7 ............ New Game */
    
        // Reset buttons
        FButton[0].setEnabled(false);
        FButton[1].setEnabled(false);
        FButton[2].setEnabled(false);
        FButton[7].setEnabled(false);
        FButton[3].setVisible(false);
        FButton[4].setVisible(false);

        // Re-create the board
        myBoard = new Board();

        // Have the Status object reset game values, keep network value
        status.newGame();

        repaint();
    } // resetGame( )

} // class JBackgammon

