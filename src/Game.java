/* **************************************************************
JBackgammon (http://jbackgammon.sf.net)
note: "http://jbackgammon.sf.net" no longer exists but jumps to 
"http://jbackgammon.sourceforge.net/" which is just a placeholder: 
no code, no working links, no info.
??possible alternate "http://djbackgammon.sourceforge.net/" 
has no source code, might not be java, 
and gives credit to different developer "David le Roux"

Copyright (C) 2002
George Vulov <georgevulov@hotmail.com>
Cody Planteen <frostgiant@msn.com>
revised 2011-12 by Joshua G., Julien S., Mike Roam

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
 * File: Game.java (was JBackgammon.java)
 *
 * Description: This file contains the guts of the main program.  
 * All drawing, control, and rule-checking occurs here.
 * A "Game" has a board, and the board has dice. 
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.Random;
//import java.util.*;  // provides Collections

public class Game extends JFrame implements ActionListener, CommunicationAdapter {
    static final String VERSION = "1.4";
    public static final long serialVersionUID = 1L; // mjr, version 1

    static private boolean testing = false; // set in constructor

    // point colors (player colors are only white & black)
    /* Beware: Player has a duplicate list of these colors which better be identical! */
    static final int neutral = 0;
    static final int white = 1;
    static final int black = 2;
    static final int howManyColors = 2;

    static final int game_unstarted = 3;

    // now in BoardPict: static final int LEFT_MARGIN = 20;
    // now in BoardPict: static final int TOP_MARGIN = 60;
    /* For analysis and fun, can show the point numbers on the board... */
    protected boolean drawPointNumbers = true; 

    // Buffers for double buffering
    BufferedImage b_bimage;
    Graphics2D gBuffer;
    BoardPict myBoardPict = new BoardPict( /* could receive board size param someday! */);

    // note: I'm making the mainBoard static so that they can
    // be reached from anywhere, but this restricts a game to having just one
    // of each: one official board, one official current player, and one AI.
    // This may want to change if a game has multiple AI's competing.
    // Maybe I could have a Tournament class which holds multiple independent Games,
    // and in that case Board, AI, currPlayer could NOT be static (unique) anymore.
    public static Board mainBoard = null; // this gets set up in constructor or die
    AI myAI;  // perhaps this should be owned by the AI player if there is one.
    public static final Player whiteP =  new Player( white, /*isAI?:*/ false /*,this?*/);
    // = null; 
    /* is it better to be creating these in constructor since they might need to 
     * be given a link to their game?? or their AI??
     */
    public static final Player blackP = new Player( black,/*isAI:*/true /*,this?,myAI?*/);
    //null;
    private Player currentPlayer = null; /* null means game_unstarted; */

    // This contains some booleans about the status of the game
    Status status = null;

    Communication comm = null;    // performs the network operations
    JTextField msg_input = null;    // for displaying messages (during network game?)
    JTextArea msg_display = null;    // display messages between the players
    JScrollPane msg_scrollpane = null;    // for scrolling messages

    // The buttons the gui uses for various purposes.
    ShowPartialMoveLabel/*Button*/[ ] recentMoveEnd 
    = new ShowPartialMoveLabel/*Button*/[Board.maxMovesCount /*4, for doubles*/];
    ShowPartialMoveLabel/*Button*/[ ] recentMoveStart 
    = new ShowPartialMoveLabel/*Button*/[Board.maxMovesCount /*4, for doubles*/];
    // Bummer: the buttons that show legal available moves on the board
    // are part of this, rather than being part of the Board.
    FixedButton fButton[] = new FixedButton[10]; /* array of buttons 0..9 */

    static final int btn_CancelChoice = 0;
    static final int btn_RollDice = 1;
    static final int btn_BearOff = 2;
    static final int btn_AtPotentialMove1 = 3;
    static final int btn_AtPotentialMove2 = 4;
    static final int btn_Connect = 5; /* only if networked */
    static final int btn_SendMessage = 6; /* only if networked */
    static final int btn_NewGame = 7;
    static final int btn_AIMove = 8;
    static final int btn_ForfeitMove = 9;

    // Button labels
    static final String CANCEL = "Cancel Choice";
    static final String ROLL_DICE = "Roll Dice";
    static final String BEAR_OFF = "Bear Off";
    static final String MOVE1 = "M1";
    static final String MOVE2 = "M2";
    static final String CONNECT = "Connect";
    static final String SEND_MSG = "Send Message";
    static final String NEW_GAME = "New Game";
    static final String AI_MOVE = "AI Move";
    static final String FORFEIT_MOVE = "Forfeit Move";

    static final int GUI_WIDTH = 202;
    /* GUI fits in the game BOARD_HEIGHT, sitting next to board */
    static final int BOARD_PADDING = 120;
    static final int MESSAGE_HEIGHT = 80; /* only when networked */

    /**
     * Game class constructor
     * assumes you're playing against AI.
     * (Use the Game(boolean) constructor if you want to set up network game)
     */
    public Game() {
        this(/*networked:*/false,/*testing:*/false);  // merely call fancier constructor
    }

    /**
     * Game class constructor
     * When running tests, I don't want the JOptionPane to put up dialog
     * boxes that require mouse click to dismiss.
     * The testing boolean is final, so can only be set at startup!
     */
    public Game(boolean networkTF) {
        this(/*networked:*/false,/*testing:*/false );  // merely call fancier constructor
    }

    /**
     * Game class constructor
     * Sets title bar, size, shows the window, and does the GUI
     */
    public Game(boolean networkTF /* networked true/false */, boolean newTesting) {
        //         whiteP = new Player( white, /* isAI? */ false);
        //         blackP = new Player( black, /* isAI? */ true);
        testing = newTesting;
        setTitle("JBackgammon");
        setResizable(false); 
        /* someday this can be resizable when all dimensions are relative */
        status = new Status();
        mainBoard = new Board(this); 
        /* change this to mainBoard or something like that !!?? */
        myAI = new AI( this);
        status.networked = networkTF;

        addMouseListener(new MyMouseListener(this));

        // Call pack() since otherwise getInsets() does not work until the frame is shown
        pack();

        Container theGui = getContentPane( );
        for (int i=0; i < fButton.length; i++) {
            /* create all the original group buttons */
            fButton[i] = new FixedButton(theGui, this);
        }

        for (int i = 0; i < Board.maxMovesCount; ++i) {
            // note, counting 0,1,.. but labelled 1,2,..
            recentMoveStart[i] = new ShowPartialMoveLabel/*Button*/(theGui, this, (i+1), BoardPict.clr_green);
            recentMoveEnd[i] = new ShowPartialMoveLabel/*Button*/(theGui, this, (i+1), BoardPict.clr_red);
        }
        hidePartialMoves( );

        if (status.networked) {
            setupNetworking( );
            setSize(myBoardPict.BOARD_WIDTH + GUI_WIDTH/*632*/
            , myBoardPict.BOARD_HEIGHT + BOARD_PADDING + MESSAGE_HEIGHT /*560*/);
        } else {
            setSize(myBoardPict.BOARD_WIDTH + GUI_WIDTH/*632*/
            , myBoardPict.BOARD_HEIGHT + BOARD_PADDING);
        }

        // Set up double buffering
        b_bimage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        gBuffer = b_bimage.createGraphics();

        setupGUI();
        setVisible(true); // was the deprecated "show()";
    } // Game( ) constructor

    /**
     * Called by Game(boolean) constructor if we're networking
     */
    private void setupNetworking( ) {
        comm = new Communication(mainBoard, (CommunicationAdapter)this);
        comm.listen();
        // Set up the window for messaging
        getRootPane().setDefaultButton(fButton[btn_SendMessage]);
        msg_input = new JTextField();
        getContentPane().add(msg_input);
        msg_display = new JTextArea();
        msg_scrollpane = new JScrollPane(msg_display);
        msg_scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(msg_scrollpane);
    } /* setupNetworking( ) */

    /**
     * Is called by Board, so can't be private.
     */
    public void debug_msg(String dmsg) {
        /*System.out.println("----------------");
        System.out.println("Breakpoint " +  dmsg);
        System.out.println("status.aPointIsSelected = " + status.aPointIsSelected 
        + "   selectedPointLoc = " + selectedPointLoc);
        System.out.println("currentPlayer = " + currentPlayer.toString( ) 
        + "   Dice = " + getMyBoard( ).myDice);
        System.out.println("potDest1 = " + potDest1 + "   potDest2 = " + potDest2);
        System.out.println("doublet_moves = " + doublet_moves + " doublets = " 
        + getMyBoard().myDice.isDoubles( ));
        // System.out.println("networked = " + status.networked + "  observer = " 
        //   + status.observer);
        /// System.out.println("Number of black = " + myBoard.getBlack());
        // System.out.println("Number of white = " + myBoard.getWhite());
        System.out.println("----------------");
        System.out.println();*/
    } // debug_msg

    /** 
     * Looks good but does anybody call this, or instead use Move.doMove??
     * 
     * calls "Board.doPartialMove( )" method
     * There is a "getUsedDie( )" (in myBoard().myDice) which says which which dice have 
     * been used.
     * (aBoard.myDice.isDoubles( ) is true when doubles have been rolled, 
     * and Board.myDice.numOfPartialMovesAvail( ) keeps track of 4 moves countdown)
     * 
     * Note: this switches players by calling game.endTurn( )!
     */
    /*not private for testing */ 
    //     void doMove(Move myMove, Board aBoard,Player playerColor) {
    //         debug_msg("doMove()");
    //         int howManyPartialsDone = 0;
    //         ArrayList<PartialMove> myPartials = myMove.getMyPartials( );
    //         for (PartialMove myPartial : myPartials ) {
    //             aBoard.doPartialMove( myPartial ); // marks the die as used??
    //             howManyPartialsDone++;
    // 
    //             // doPartialMove() marks the die as used...note: can only mark values once!
    //             // aBoard.myDice.setUsedValue( myPartial.getRollValue( ) );
    //             //note: getRollValue( ) tells the VALUE of the die, not WHICH die
    //             // Turn off focus on this point
    //             endPartialMove(playerColor);
    //             repaint();
    //             // this better use up all the dice rolls, but not try to do too many!
    //         }
    // 
    //         //boolean switchedplayers = true; // hypothetical
    //         if ((aBoard.myDice.allDiceAreUsed( )) || (!canMove(playerColor))){
    //             endTurn(aBoard,currentPlayer);
    //             //} else { switchedplayers = false; }
    //         }
    // 
    //         // If this wasn't the player's last partialMove,
    //         // check if he is still on the bar or if he can make more partialMoves
    //         //if ( ! switchedplayers ) {
    //         //    if (aBoard.onBar(playerColor )) {
    //         //        aBoard.selectABar(playerColor);
    //         //    }
    //         //    if (!aBoard.canMove(playerColor )) {
    //         //        forfeitTurn(aBoard,playerColor);
    //         //    }
    //         //}
    //     } // doMove( )

    /**
     * Forfeit the current player's turn.
     * Is called by Board, so can't be private.
     */
    public void forfeitTurn(Board aBoard, Player playerColor) {
        String msg;
        if ( aBoard.canMove(playerColor) ) {
            msg = "\nWeird, " + playerColor + " canMove (usable diceRoll(s): " 
            + aBoard.getMyDice( ).diceRollValuesAvailable( ) + ") but forfeits a turn.";
        } else {
            msg = "\n" + playerColor + " is stuck, forfeits a turn. (unused diceRoll(s): " 
            + aBoard.getMyDice( ).diceRollValuesAvailable( ) + ")";
        }
        // JOptionPane.showMessageDialog(this, msg);
        endTurn(aBoard, playerColor, msg);
        repaint();
    } // forfeitTurn( )

    /**
     * Checks if there is a winner
     * If there is one, displays appropriate message.
     * Return true if there was a winner, false otherwise
     */
    public boolean checkWin(Board aBoard, Player playerColor)    {
        String msg;

        if ( (playerColor==whiteP) && (!status.networked) ) {
            msg = "White wins";
        } else if ((playerColor==blackP) && (!status.networked)) {
            msg = "Black wins";
        } else {
            msg = "You win!";
        }

        if (aBoard.bear[playerColor.getColor( )]==Board.howManyBlots) {
            if (status.networked) {
                comm.sendLose();
            }
            repaint();
            if (!Game.testing) { 
                JOptionPane.showMessageDialog(this, msg);
            }
            return true;
        }

        return false;
    } // checkWin( )

    /** 
     * Roll the dice for the current player.
     * If current player is on the bar then this calls "aBoard.selectABar( )"
     * If the current player can't move, this calls "Game.forfeitTurn( )"
     * This is stronger than merely telling a board's dice to roll, which doesn't
     * check for bar nor canMove
     * 
     * [ ]This is smarter than Board.roll. Should Board.roll( ) get its brains? 
     */
    public void doRoll(Board aBoard, Player playerColor) {
        hidePartialMoves( ); // hides the buttons that showed the prev move
        aBoard.myDice.roll(); // randomValues
        /* Dice keep count of how many have been "used" (of 2, or 4 with doubles). */
        /* Call Dice's setUsedDie( ) or setUsedValue( ) after using a die. */

        if (status.networked) {
            comm.sendRoll(aBoard.myDice.getDie(1), aBoard.myDice.getDie(2));
        }

        // Turn off roll dice button, will supposedly be turned on when next turn starts
        fButton[btn_RollDice].setEnabled(false); // roll dice

        if (playerColor == null/* means game_unstarted*/) {
            /* Dice 1 is white, Dice 2 is black; high player starts using these 2. 
            In case of tie, roll again until no tie. So a first move can never be doubles!*/
            /* maybe this should wait for the players to roll again? */
            while (aBoard.myDice.isDoubles( ) ) {
                aBoard.myDice.roll( );
                drawCurrentDice(aBoard ); //calls repaint();??
            }
            if (aBoard.myDice.getDie(1) > aBoard.myDice.getDie(2)) {
                currentPlayer = whiteP;
                playerColor = currentPlayer;
            } else {
                currentPlayer = blackP;
                playerColor = currentPlayer;
                fButton[btn_AIMove].setEnabled(playerColor == blackP);
            }
        }
        aBoard.setSelectedPointLoc( Board.NO_MOVE_SELECTED, playerColor ); 
        // Check if the player is on the bar and deal with that right away before 
        // player tries to move.
        // Does AI know to hear this selectABar??
        if (aBoard.onBar(playerColor)) {
            aBoard.selectABar(playerColor);// lights up potential moves, doesn't do them
        } else if ( ! aBoard.canMove(playerColor) ) {
            forfeitTurn(aBoard,playerColor);
        }
        repaint(); 
    } // doRoll( )

    /**
     * This could selectA more than 2 players with slight modification...
     * is caller also doing startTurn??
     * This is a toggle: shouldn't it get a parameter?
     */
    public void changePlayerFrom(Board aBoard, Player playerColor) {
        if (currentPlayer != playerColor) {
            System.err.println("weird, currentPlayer is " + currentPlayer 
                + " while " + playerColor + " is being told to changePlayer");
        } else { // okay, change hasn't already happened, can happen.
            if (currentPlayer == whiteP) {
                currentPlayer = blackP;
            } else {
                currentPlayer = whiteP;
            }
            aBoard.setSelectedPointLoc( Board.NO_MOVE_SELECTED, playerColor );
            // oops: tempting to call startTurn( ) here, but recursion loop?, since
            // endTurn() calls changePlayer( ), and endTurn( ) also calls startTurn( ); 
            // and startTurn  sets "fButton[btn_RollDice].setEnabled(true);"
            // Now that we're only changing sometimes, could we safely startTurn?
            if (myAI != null) {
                fButton[btn_AIMove].setEnabled(currentPlayer == blackP);
            }
            repaint();   
        }

    } /* changePlayer */

    /**
     * End the current player's turn and start the turn
     * of the other player.
     * Perhaps redundant that "currentPlayer" is specified ...
     * Is called by Board, so it can't be private.
     * Calls startTurn( ), which sets  "fButton[btn_RollDice].setEnabled(true);"
     */
    public void endTurn(Board aBoard,Player playerColor, String additionalMessage) {
        if (additionalMessage == null) {
            additionalMessage = "";
        }
        String msg;
        if (currentPlayer != playerColor) {
            throw new IllegalArgumentException("Whoa, why ending turn of " + playerColor 
                + " when currentPlayer is " + currentPlayer + "?");
        } else { // currentPlayer == playerColor
            changePlayerFrom(aBoard,playerColor ); /* disables "AI Move" btn if necessary */
        }
        aBoard.myDice.reset();
        /* calls resetUsedDice(  ),  sets rolled to false and countdown to 0 */
        //         fButton[btn_NewGame].setEnabled(false); // why??
        aBoard.setSelectedPointLoc( Board.NO_MOVE_SELECTED, playerColor );

        if (!status.networked) {
            msg = playerColor + "'s turn is now over.  Please switch players.";
        } else {
            msg = playerColor + "'s turn is now over.";
            comm.sendEndTurn();
            status.observer = true;
        }

        if (!Game.testing) { 
            // annoying and useless message?
            // JOptionPane.showMessageDialog(this, msg + additionalMessage);
        }
        if (!status.networked) {
            startTurn(playerColor); // has "fButton[btn_RollDice].setEnabled(true);"
        }
        repaint();
    } // endTurn

    /**
     * Begins a player's turn
     */
    private void startTurn(Player playerColor) {
        mainBoard.setSelectedPointLoc( Board.NO_MOVE_SELECTED, playerColor ); 
        // makes aPointIsSelected be false
        // Enable roll dice and new game buttons
        fButton[btn_RollDice].setEnabled(true);
        fButton[btn_NewGame].setEnabled(true);
        if (status.networked && !status.observer) {
            Toolkit.getDefaultToolkit().beep();
            if (!Game.testing) { 
                // annoying and useless message?
                // JOptionPane.showMessageDialog(this, "It is now your turn");
            }
        }
        mainBoard.hitsSoFarBeEmpty( ); // keeping list of blots knocked to bar in this move
    } // startTurn( )

    /** 
     * This is for Partial Move
     * Remove focus from a certain point which has been selected
     * This allows the player to select a new point.
     * called by Board, so can't be private.
     * Disables the CancelChoice button because there's not
     * a tentative partial move to cancel anymore!
     */
    public void endPartialMove(Player playerColor) {
        mainBoard.setSelectedPointLoc( Board.NO_MOVE_SELECTED, playerColor ); 
        // makes aPointIsSelected be false
        // Board should take take care of this! Does it??

        // Disable potential move buttons, which ought to be part of board someday
        fButton[btn_AtPotentialMove1].setVisible(false); // potential move 1
        fButton[btn_AtPotentialMove2].setVisible(false); // potential move 2
        // Disable "Cancel Move" button
        fButton[btn_CancelChoice].setEnabled(false); // cancel move
        repaint( );
    } // endPartialMove( )

    /**
     * Returns the game's mainboard, which (currently) is the only board
     * with gui display (View) and gui buttons (Controller). Other boards
     * are created to test hypothetical moves but don't have V & C and
     * any moves upon those hypothetical boards are not official moves
     * of the game.
     * [ ]Games ought to keep a history of official moves!
     */
    public Board getMyBoard( ) {
        return mainBoard;
    }

    /** 
     * returns (point to) currentPlayer or null means game unstarted
     * formerly returned int white = 1; black = 2; (shouldn't ever have neutral = 0;)
     */
    public Player /*PlayerColor*/ getCurrentPlayer( ) {
        return currentPlayer;
    }

    /**
     * note: beware overlapping duties hardcoded into startTurn( ), endTurn( ), ??
     * Should this acknowledge a change somehow? Shouldn't roll dice, I guess.
     */
    public void setCurrentPlayer(Player newPlayerColor ) {
        /* only change if currentPlayer will become different from before */
        /* might be null if starting a new game... */
        if (currentPlayer != newPlayerColor) {
            currentPlayer = newPlayerColor;
            if (myAI != null) { /* redundant from changePlayer() */
                fButton[btn_AIMove].setEnabled( currentPlayer == blackP );
            }
            startTurn(newPlayerColor ); // makes selectedPoint clear, etc
            repaint( );
        }
    } 

    /**
     *  Initialize the GUI
     *   Sets up all the buttons
     */
    public void setupGUI() {
        int left = GUI_Dim.BTN_LEFT_EDGE; /* 475 when board is 430 wide */
        int width = GUI_Dim.BTN_WIDTH; /* 135 */
        int height = GUI_Dim.BTN_HEIGHT; /* 25 */
        int currTop = GUI_Dim.TOP_OF_BUTTON_ZONE;
        fButton[btn_RollDice].setBounds(left, currTop, width, height);
        fButton[btn_RollDice].setVisible(true);
        fButton[btn_RollDice].setText(ROLL_DICE);
        fButton[btn_RollDice].addActionListener(this);
        fButton[btn_RollDice].setEnabled(true);
        currTop += (GUI_Dim.BTN_HEIGHT + GUI_Dim.BTN_MARGIN);

        fButton[btn_BearOff].setBounds(left, currTop, width, height);
        fButton[btn_BearOff].setVisible(true);
        fButton[btn_BearOff].setText(BEAR_OFF);
        fButton[btn_BearOff].addActionListener(this);
        fButton[btn_BearOff].setEnabled(false);
        currTop += (GUI_Dim.BTN_HEIGHT + GUI_Dim.BTN_MARGIN);

        fButton[btn_CancelChoice].setBounds(left, currTop, width, height);
        fButton[btn_CancelChoice].setVisible(true);
        fButton[btn_CancelChoice].setText(CANCEL);
        fButton[btn_CancelChoice].addActionListener(this);
        fButton[btn_CancelChoice].setEnabled(false);
        currTop += (GUI_Dim.BTN_HEIGHT + GUI_Dim.BTN_MARGIN);

        fButton[btn_ForfeitMove].setBounds(left, currTop, width, height);
        fButton[btn_ForfeitMove].setVisible(true);
        fButton[btn_ForfeitMove].setText(FORFEIT_MOVE);
        fButton[btn_ForfeitMove].addActionListener(this);
        fButton[btn_ForfeitMove].setEnabled(true);
        currTop += (GUI_Dim.BTN_HEIGHT + GUI_Dim.BTN_MARGIN);

        fButton[btn_AIMove].setBounds(left, currTop, width, height);
        fButton[btn_AIMove].setVisible(true);
        fButton[btn_AIMove].setText(AI_MOVE);
        fButton[btn_AIMove].addActionListener(this);
        fButton[btn_AIMove].setEnabled(true);
        currTop += (GUI_Dim.BTN_HEIGHT + GUI_Dim.BTN_MARGIN);

        currTop += 4; // gap so the newGame button is further away, less likely to press

        fButton[btn_NewGame].setBounds(left, currTop, width, height);
        fButton[btn_NewGame].setVisible(true);
        fButton[btn_NewGame].setText(NEW_GAME);
        fButton[btn_NewGame].addActionListener(this);
        fButton[btn_NewGame].setEnabled(true);
        currTop += (GUI_Dim.BTN_HEIGHT + GUI_Dim.BTN_MARGIN);

        // potential move 1. Are these coords so it is hiding off the right side?
        fButton[btn_AtPotentialMove1].setBounds(650, 490, 9, 10); // LTWH
        fButton[btn_AtPotentialMove1].setVisible(true);
        fButton[btn_AtPotentialMove1].setText(MOVE1);
        fButton[btn_AtPotentialMove1].addActionListener(this);
        fButton[btn_AtPotentialMove1].setEnabled(true);

        // potential move 2. Are these coords so it is hiding off the right side?
        fButton[btn_AtPotentialMove2].setBounds(750, 490, 9, 10); //LTWH
        fButton[btn_AtPotentialMove2].setVisible(true);
        fButton[btn_AtPotentialMove2].setText(MOVE2);
        fButton[btn_AtPotentialMove2].addActionListener(this);
        fButton[btn_AtPotentialMove2].setEnabled(true);
        currTop += (GUI_Dim.BTN_HEIGHT + GUI_Dim.BTN_MARGIN);

        if (status.networked) {
            fButton[btn_Connect].setBounds(left, currTop, width, height);
            fButton[btn_Connect].setVisible(true);
            fButton[btn_Connect].setText(CONNECT);
            fButton[btn_Connect].addActionListener(this);
            fButton[btn_Connect].setEnabled(true);
            currTop += (GUI_Dim.BTN_HEIGHT + GUI_Dim.BTN_MARGIN);

            // rest of these gui elements aren't particularly adjusted 
            // to GUI_Dim dimensions...
            fButton[btn_SendMessage].setBounds(left, 
                BoardPict.TOP_MARGIN+getInsets().top + 412, width, height);
            fButton[btn_SendMessage].setVisible(true);
            fButton[btn_SendMessage].setText(SEND_MSG);
            fButton[btn_SendMessage].addActionListener(this);
            fButton[btn_SendMessage].setEnabled(false);

            fButton[btn_RollDice].setEnabled(false);
            fButton[btn_NewGame].setEnabled(false); // why false??

            msg_input.setBounds(BoardPict.LEFT_MARGIN - getInsets().left, 
                BoardPict.TOP_MARGIN + getInsets().top + 412, 
                450, height);

            msg_scrollpane.setBounds(BoardPict.LEFT_MARGIN - getInsets().left, 
                BoardPict.TOP_MARGIN + getInsets().top + 327, 
                593, 80);
            msg_display.setEditable(false);
            msg_display.setLineWrap(true);
            msg_display.setWrapStyleWord(true);
        }
    } // setupGUI

    /**
     *  Connect to another Game for network play
     */
    public void connect() {
        String input_ip;
        input_ip = JOptionPane.showInputDialog("Enter computer name or IP address");
        fButton[btn_Connect].setEnabled(false); // connect
        if (input_ip != null) {
            if ( (comm.portBound == 1 ) 
            && ( (input_ip.equalsIgnoreCase("localhost")) 
                || (input_ip.equals("127.0.0.1")) ) )
            {
                if (!Game.testing) { 
                    JOptionPane.showMessageDialog(this, "Game can't connect to input_ip '" 
                        +  input_ip + ", which is same instance of itself.");
                }
                fButton[btn_Connect].setEnabled(true);
            } else {
                status.clicker = true;
                comm.connect(input_ip);
            }
        } else { // The user canceled, re-enable the connect button
            fButton[btn_Connect].setEnabled(true);
        }
    } // connect( )

    /**
     *  Method to send a message through a JOptionPane to the other user
     */
    public void sendMessage() {
        String message = msg_input.getText();
        if (message.length() > 0) {
            comm.sendMessage(message);
            msg_display.append("White player: " + message + '\n');
            // Scroll the text area to the bottom
            msg_display.scrollRectToVisible(new Rectangle(0, msg_display.getHeight(), 1, 1));
        }
        msg_input.setText("");
    } // sendMessage( )

    /*=================================================
     * Network Methods 
     * ================================================*/

    /**
     * The remote network player has won
     */
    public void receiveLose() {
        fButton[btn_NewGame].setEnabled(true);
        if (!Game.testing) { 
            JOptionPane.showMessageDialog(this, "You lose!");
        }
    } // receiveLose( )

    /**
     * The remote player is forfeiting a turn
     */
    public void receiveForfeitTurn( ) {
        forfeitTurn( mainBoard, currentPlayer );
    }

    /**
     *  Connection lost, reset the board for a new game
     */
    public void disconnected() {
        if (!Game.testing) {
            JOptionPane.showMessageDialog(this, "Network connection lost!");
        }
        // Allow the person to connect to someone else
        fButton[btn_Connect].setEnabled(true);
        // Reset the order of connecting
        status.clicker = false;
        // Start listening for connections again
        comm.listen();
        resetGame(mainBoard);
    } // disconnected( )

    /**
     * Implementing the "connectionRefused( )" method of interface CommunicationAdapter
     * Which says what to do if we could not connect to an ip
     */
    public void connectionRefused() {
        if (!Game.testing) { 
            JOptionPane.showMessageDialog(this, "Connection refused.\n\nMake sure the " 
                +   "computer name/IP is correct\n" 
                + "and that the destination is running Game in networked mode.");
        }
        status.clicker = false;
        fButton[btn_Connect].setEnabled(true);
    } // connectionRefused( )

    /**
     *  The network player has rolled the dice, display them
     */
    public void receiveRolls(Board aBoard,int i, int j) {
        currentPlayer = blackP;
        // alt syntax: 
        //aBoard.myDice.setDie(1,i);
        //aBoard.myDice.setDie(2,j);
        aBoard.myDice.roll(i,j);
        aBoard.myDice.setRolled(true);
        repaint();
    } // receiverolls( )

    /**
     * The local (non-network) player got sent to the bar, so update the board.
     * Apparently point is a number from the point of view of the opponent?
     * Apparently remote network player is always black on our screen, while
     * perhaps white on her screen??
     * So that's why we're doing the 25 - point thing??
     */
    public void receiveBar(Board aBoard,int point) {
        /* int destPointNum, int howMany, int color/* not merely playerColor*/
        // bogus, this doesn't do any error checking, should call something...
        aBoard.setNumOfBlotsOnPoint((Board.howManyPoints+1) - point, /*howMany:*/0, 
            /*color:*/null /* means neutral*/);
        aBoard.bar[ Game.whiteP.getColor( ) ]++;
        repaint();
    } // receivebar

    /**
     * The network player requested a new game, and wants a response
     */ 
    public void receiveResetReq() {
        int reset = JOptionPane.showConfirmDialog(this, 
                "The network player has requested a new game.\nDo you want to accept?",
                "New Game Request",JOptionPane.YES_NO_OPTION);
        comm.sendResetResp(reset);
        if ( reset == JOptionPane.YES_OPTION ) {
            resetGame(mainBoard);
        }
    } // receiveResetReq

    /**
     * The network player responded to a new game request, process the results
     */ 
    public void receiveResetResp( int resp ) {
        if (resp == JOptionPane.NO_OPTION) {
            if (!Game.testing) { 
                JOptionPane.showMessageDialog(this, "Request for new game denied.");
            }
        } else {
            if (!Game.testing) { 
                JOptionPane.showMessageDialog(this, "Request for new game accepted.");
            }
            resetGame(mainBoard);
            Random r = new Random();
            boolean goesfirst = r.nextBoolean();
            if (goesfirst) {
                status.observer = false;
                startTurn(currentPlayer);
            } else {
                status.observer = true;
                comm.sendEndTurn();
            }
        }
    } // receiveResetResp( )

    /**
     * The network player has moved, update the board.
     * Apparently remote network player is always black on our screen, while
     * perhaps white on her screen?? (which explains why her moves are getting (25 - X)).
     * Apparently oldpos value -1 used to mean coming in from bar, and 26 meant bearing off?
     * Are the constants being handed in properly for BAR_ and BEAR?  Probably not.
     * This isn't using any of the improved selectAPoint and moveBlot code yet.
     */
    public void receiveMove(Board aBoard,Player playerColor, int oldpos,int newpos,int rollValue) {
        /* throw New IllegalArgumentException("network moving needs complete overhaul"); */
        if ( ! Board.legitStartLoc( oldpos, playerColor )) { // checks the playerColor
            throw new IllegalArgumentException("point '" +oldpos
                +"' isn't a legal starting place");
        }
        if ( ! Board.legitEndLoc( newpos, playerColor )) {
            throw new IllegalArgumentException("Can't legally move to point '" +newpos+"'");
        }
        if ( this != null ) {
            throw new IllegalArgumentException("receiveMove doesn't know whether to" +
                "call movePoint( ) or ??");
        } else {
            // shouldn't this just call moveBlot or doPartialMove or something like that
            // rather than re-inventing all this bar/bear/point movement??
            if (playerColor == blackP) {
                oldpos = (Board.howManyPoints + 1) - oldpos;
                newpos = (Board.howManyPoints + 1) - newpos;
            }
            if ( (1 <= oldpos) && (oldpos<= Board.howManyPoints) 
            && (1<=newpos) && (newpos<=Board.howManyPoints) ) {
                aBoard.moveBlot(playerColor /* white*/, oldpos, newpos, rollValue);
                repaint();
            } else if (newpos==playerColor.getBearOffLoc( ) ) /*||getBeyondBearOffLoc?? */{
                /* Calling board.bearOff( ) to make sure everything is handled */
                aBoard.bearOff(playerColor, oldpos, rollValue);
                /* instead of */
                //aBoard.bear[playerColor.getColor( ) ]++;
                //aBoard.takeOneBlotOffPoint( oldpos /* newPos?or just for black??*/);

                /* which was instead of */
                //myBoard.setNumOfBlotsOnPoint(/*point*/oldpos, 
                //    /*howMany:*/myBoard.getHowManyBlotsOnPoint(oldpos) - 1, 
                //    playerColor/*white*/);
                repaint();
            } else if (oldpos==playerColor.getBarLoc( ) ) {
                // bogus: this doesn't do any error checking. Should call something ??
                aBoard.bar[playerColor.getColor( ) ]--;
                int howMany = aBoard.getHowManyBlotsOnPoint(newpos + 1, playerColor/*white*/);
                aBoard.setNumOfBlotsOnPoint(/*point*/newpos, howMany,playerColor);
                repaint();
            }

        } /* end of sudden death */
    } // receiveMove( )

    /**
     * The network player has sent an instant message. Display it
     */
    public void receiveMessage(String message) {
        msg_display.append("Black player: " + message + '\n');
        // Scroll the text area to the bottom
        msg_display.scrollRectToVisible(new 
            Rectangle(0, msg_display.getHeight(), 1, 1));
    } // receiveMessage( )

    /** 
     * Connection with an instance of Game successfully established.
     * Start the game. "The client initiating the connection decides 
     * who goes first." 
     */
    public void connected() {
        fButton[btn_Connect].setEnabled(false);
        fButton[btn_SendMessage].setEnabled(true);

        if (status.clicker) {
            Random r = new Random();
            boolean goesfirst = r.nextBoolean();
            if (goesfirst) {
                status.observer = false;
                startTurn(currentPlayer);
            } else {
                status.observer = true;
                comm.sendEndTurn();
            }
        } else {
            status.observer = true;
        }
        repaint();
    } // connected( )

    /** The network player has finished his turn.
     * Start the local player's turn
     * Is "local" always white, or randomly decided in connected( )??
     */
    public void turnFinished(Board aBoard) {
        status.observer = false;
        currentPlayer = whiteP;

        aBoard.myDice.reset(); /* sets rolled = false; */
        fButton[btn_RollDice].setEnabled(true);
        startTurn(currentPlayer); // turns selectedPoint off, 
    } // turnFinished( )

    /**
     * called from actionPerformed by gui when user presses
     * one of the potentialmove buttons
     */
    public void buttonCausesPartialMove(int move1or2) {
        // better not allow 3 or 4, even with doubles
        if (! mainBoard.getAPointIsSelected( )) {
            System.err.println("How is it that you're moving but no point is selected??");
            return;
        }
        int fromPoint = mainBoard.getSelectedPointLoc();
        int toPoint = mainBoard.getPotDest(move1or2);
        int rollValue = mainBoard.myDice.getDie( move1or2 );
        //This could be trouble with doubles since saying (re-)use dice 1 isn't sayable yet.
        // Have to adjust dice so that setUsed( ) while in doubles can mark any free die as 
        // used? or merely tell the dice, this is the VALUE we're using up.
        PartialMove pm = new PartialMove( fromPoint, rollValue, toPoint, mainBoard, currentPlayer);
        //mainBoard.doPartialMove(fromPoint, toPoint, rollValue, currentPlayer);
        mainBoard.doPartialMove(pm);
        showPartialMove(pm, currentPlayer, 0); /*note: final arg means is this the first partialMove
        we've played for this player's turn, or the second or 3rd or 4th. 0 means we don't know which
        partial it is, so just reveal the lowest invisible */ 
    }

    /**
     * Puts visible buttons on start and end of the move.
     * Should it do them in the order of dice and potMove,
     * or in the order of actually being done <--probably better.
     * (Counting 0,1,2 but named 1,2,3...).
     * @param moveNum either explicit 1,2,3,4 OR 0 means use lowest invisible buttons.
     * I.E. caller doesn't know which move this is and wants us to figure it out.
     */
    public void showPartialMove( PartialMove p, Player playerColor, int moveNum ) {
        int start = p.getStart( ); // might be bar 
        int end = p.getEnd( ); // might be bear
        if (moveNum ==0) { //I'm willing to let 
            //use lowest numbered invisible pair of buttons
            for (int i = 0; i < Board.maxMovesCount; ++i) {
                if ((! recentMoveStart[i].isVisible( )) && (moveNum==0) ) {
                    moveNum = i+1; // 
                }
            }
            if (moveNum == 0 ) {
                System.err.println("I didn't find a lowest invisible move, so using 4");
                moveNum = 4;
            }
        } else if ((moveNum < 0) || (moveNum > Board.maxMovesCount)) {
            throw new IllegalArgumentException("can't show move#" + moveNum 
                + "! I only have 1.." + Board.maxMovesCount);
        }

        recentMoveStart[moveNum-1].drawOnPoint(start); // can handle bar
        recentMoveStart[moveNum-1].setVisible(true);

        recentMoveEnd[moveNum-1].drawOnPoint(end); // can handle bear
        recentMoveEnd[moveNum-1].setVisible(true);

        repaint( );
    }

    /**
     * would be nice to have a specific hidePartialMove(x) method,
     * especially for doing undo...
     */
    public void hidePartialMoves( ) {
        for (int i = 0; i < Board.maxMovesCount; ++i) {
            recentMoveStart[i].setVisible(false);
            recentMoveEnd[i].setVisible(false);
        }
        repaint( );
    }

    /**
     * Cancelling a choice of point, saying "I don't really want to move from here after all"
     */
    public void doCancel( Board aBoard, Player playerColor) {
        mainBoard.setSelectedPointLoc(Board.NO_MOVE_SELECTED,playerColor); 
        // makes aPointIsSelected = false;
        fButton[btn_CancelChoice].setEnabled(false);
        fButton[btn_BearOff].setEnabled(false);
        fButton[btn_AtPotentialMove1].setVisible(false);
        fButton[btn_AtPotentialMove2].setVisible(false);
        repaint();
    }

    /*=================================================
     * Overridden Methods 
     * (constructor wants boolean re networked? true/false)
     * 
     * all of this big "if" should be separate listeners,
     * perhaps anonymous, attached to appropriate buttons.
     * 
     * Note: many of these are talking to "mainBoard" which
     * is the public display (MVC) of the most visible board,
     * but isn't necessarily the temp board that is trying
     * out temporary moves when the AI is playing with itself.
     * ================================================*/

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(ROLL_DICE)) {
            doRoll(mainBoard, currentPlayer);
        } else if (e.getActionCommand().equals(CANCEL)) {
            doCancel(mainBoard, currentPlayer);
        } else if (e.getActionCommand().equals(BEAR_OFF)) {
            int bearOffFrom = mainBoard.getSelectedPointLoc( );
            int rollValue = mainBoard.whichDieValueUsedForBearOff(currentPlayer,bearOffFrom);
            mainBoard.bearOff(currentPlayer, bearOffFrom, rollValue);
        } else if (e.getActionCommand().equals(MOVE1)) {
            buttonCausesPartialMove( 1 );
        } else if (e.getActionCommand().equals(MOVE2)) {
            buttonCausesPartialMove( 2 );
        } else if (e.getActionCommand().equals(AI_MOVE)) {
            Move suggestion = null;
            try {
                suggestion = myAI.chooseAMove(mainBoard, currentPlayer );
                mainBoard.doMove(suggestion,currentPlayer ); // does this endTurn?
            } catch(Exception ex) {
                ex.printStackTrace( System.err );
                //StackTraceElement[] getStackTrace()
                System.out.println("AI.chooseAMove( ) had exception: \n" + ex);
            }
        } else if (e.getActionCommand( ).equals(FORFEIT_MOVE)) {
            forfeitTurn( mainBoard, currentPlayer );
        } else if (e.getActionCommand().equals(SEND_MSG)) {
            sendMessage();
        } else if (e.getActionCommand().equals(CONNECT)) {
            connect();
        } else if (e.getActionCommand().equals(NEW_GAME)) {
            if ( status.networked ) {
                int conf = JOptionPane.showConfirmDialog(this, "Send new game request?", 
                        "New Game", JOptionPane.YES_NO_OPTION);
                if ( conf == JOptionPane.YES_OPTION ) {
                    // FIXME: should check for network connection(?)
                    comm.sendResetReq();
                }
            } else {
                int conf = JOptionPane.showConfirmDialog(this, "Start a new game?", 
                        "New Game", JOptionPane.YES_NO_OPTION);
                if ( conf == JOptionPane.YES_OPTION ) {
                    resetGame(mainBoard); // <-- this isn't working?
                }
            } // if t/f networked
        } // if newgame
    } // actionPerformed( )

    /**
     * Talking to "mainBoard" which
     * is the public display (MVC) of the most visible board,
     * but isn't necessarily the temp board that is trying
     * out temporary moves when the AI is playing with itself.
     */
    public void paint(Graphics g) {
        // Cast the Graphics to a Graphics2D so actual drawing methods
        // are available
        Graphics2D screen = (Graphics2D) g;
        gBuffer.clearRect(0, 0, getWidth( ), getHeight( ));
        drawBoard( mainBoard );
        drawBar( mainBoard);
        drawBlots( mainBoard );
        drawBearStats( mainBoard );
        drawPipStats( mainBoard);
        drawBoardScore( mainBoard);
        drawCurrentPlayer( mainBoard );

        //if (mainBoard.myDice.getRolled()) { // 
        drawCurrentDice( mainBoard );
        //}

        if ( (status.networked) && (! comm.isConnected() ) ) {
            putString("Waiting for connection...", /*X:*/15, /*Y:*/50, 
                BoardPict.clr_red, GUI_Dim.TEXT_BIGGER_SIZE);
        }

        // Blit the buffer onto the screen
        screen.drawImage(b_bimage, null, 0, 0);

        fButton[btn_CancelChoice].repaint( );
        fButton[btn_RollDice].repaint( );
        fButton[btn_BearOff].repaint( );
        fButton[btn_AtPotentialMove1].repaint( );
        fButton[btn_AtPotentialMove2].repaint( );
        fButton[btn_NewGame].repaint( );
        fButton[btn_AIMove].repaint( );
        fButton[btn_ForfeitMove].repaint( );

        if (status.networked) {
            fButton[btn_Connect].repaint();
            fButton[btn_SendMessage].repaint();
            msg_input.repaint();
            msg_scrollpane.repaint();
        }

        paintPartialMoveButtons( );
    } // paint( )

    public void paintPartialMoveButtons( ) {
        for (int i = 0; i < Board.maxMovesCount; ++i) {
            recentMoveStart[i].repaint( );
            recentMoveEnd[i].repaint( ); 
        }
    }

    public static void main(String args[]) {
        JFrame f = new JFrame("Main Menu");
        f.setResizable(false);

        f.addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            }
        );
        JPanel pane = new JPanel();
        pane.setBorder(BorderFactory.createEmptyBorder( /* TLRB */ 20, 20, 20, 20) );
        /* TLRB means "top, left, bot, right" */
        pane.setLayout(new GridLayout(/* rows (height) */ 0, /* cols (width) */ 1));
        JLabel l1 = new JLabel("JBackgammon v" + VERSION);
        JLabel l2 = new JLabel("started by Cody Planteen and George Vulov");
        /* JLabel l3 = new JLabel("http:// jbackgammon.sf.net"); Dead link*/
        pane.add(l1);
        pane.add(l2);
        /*   pane.add(l3); */

        JButton ButtonA = new JButton("1P vs. 2P (same computer)");
        ButtonA.addActionListener(new MainMenuListener(f, /* networked */ false));
        JButton ButtonB = new JButton("1P vs. 2P (network)");
        ButtonB.addActionListener(new MainMenuListener(f, /* networked */ true));
        pane.add(ButtonA);
        pane.add(ButtonB);
        f.getContentPane().add(pane);

        f.pack();
        f.setVisible(true);
    } // main( )

    /*=================================================
     * Drawing Methods 
     * ================================================*/

    /** 
     * Gets the X coordinate of the (center of?) the specified point 
     * (aka "column" or "spike"), for displaying blots and the "move here"
     * buttons which will be shown just below the point of possible destinations.
     */
    public int findX(int point) {
        // standard board has 6 points per quadrant
        int endOfQuadrant1 = 1*Board.HOW_MANY_POINTS_IN_QUADRANT;
        int endOfQuadrant2 = 2*Board.HOW_MANY_POINTS_IN_QUADRANT;
        int endOfQuadrant3 = 3*Board.HOW_MANY_POINTS_IN_QUADRANT;
        int endOfQuadrant4 = 4*Board.HOW_MANY_POINTS_IN_QUADRANT;
        int pointWidth = BoardPict.POINT_WIDTH + BoardPict.POINT_MARGIN;
        if (point <= endOfQuadrant1) { /* quadrant one is 1..6 (for white, natch) */
            return BoardPict.LEFT_MARGIN + /*401*/ BoardPict.BOARD_WIDTH - (pointWidth* point );
            // return BoardPict.LEFT_MARGIN + 401 - (32*(point - 1));
        }
        if (point <= endOfQuadrant2)  { /* quadrant two is 7..12  */
            return BoardPict.LEFT_MARGIN + BoardPict.QUADRANT_WIDTH - (pointWidth * (point - endOfQuadrant1));
            // return BoardPict.LEFT_MARGIN + 161 - (32*(point - 7));
        }
        if (point <= endOfQuadrant3) { /* quadrant three is 13..18  */
            return BoardPict.LEFT_MARGIN + 1 + (pointWidth*(point - (endOfQuadrant2+1)));
            // return BoardPict.LEFT_MARGIN + 1 + (32*(point - 13));
        }
        if (point <= endOfQuadrant4) { /* quadrant four is 19..24  */
            return BoardPict.LEFT_MARGIN + BoardPict.QUADRANT_WIDTH + BoardPict.BAR_WIDTH 
            + (pointWidth*(point - (endOfQuadrant3+1)));
            //return BoardPict.LEFT_MARGIN + 241 + (pointWidth*(point - 19));
        }
        return -1; // WTF??
    } // findX( )

    /** 
     * Gets the Y coordinate (??) of the bottom of the triangle of the specified point 
     * (aka "column" or "spike"), for displaying blots and "move here" buttons which
     * will be just below the point of possible destinations.
     */
    public int findY(int point) {
        if (point <= ((Board.HOW_MANY_QUADRANTS/2)*Board.HOW_MANY_POINTS_IN_QUADRANT)) { 
            /* points 1..12 are in top half of board */
            return BoardPict.TOP_MARGIN;
        } else if (point <= (Board.HOW_MANY_QUADRANTS*Board.HOW_MANY_POINTS_IN_QUADRANT)) { 
            /* points 13..24 are in lower half of board */
            return BoardPict.TOP_MARGIN + /*361*/ BoardPict.BOARD_HEIGHT + 1;
        }
        return -1; // wtf??
    } // findY( )

    /* shouldn't be final if board is resizable */
    final static int left = GUI_Dim.BTN_LEFT_EDGE; /* 475 */
    final static int furtherleft = GUI_Dim.LEFT_EDGE; /* 455 */
    final static int bearTop = GUI_Dim.STATS_TOP_MARGIN; /* 30 */
    final static int textLineHeight = GUI_Dim.TEXT_LINE_HEIGHT; /* 18 */

    /**
     * Announce how many pieces each player beared off so far
     */
    public void drawBearStats(Board aBoard) {
        String m1, m2;
        m1 = "White Pieces Beared Off: " + aBoard.bear[Player.WHITE];
        m2 = "Black Pieces Beared Off: " + aBoard.bear[Player.BLACK];

        gBuffer.setColor(BoardPict.clr_black);
        gBuffer.fill(
            new Rectangle2D.Double(left, bearTop, GUI_Dim.GUI_WIDTH, 2*textLineHeight) );

        putString(m1, /*X:*/furtherleft, /*Y:*/bearTop + textLineHeight, 
            BoardPict.clr_white, GUI_Dim.TEXT_FONT_SIZE);
        putString(m2, /*X:*/furtherleft, /*Y:*/bearTop + 2*textLineHeight, 
            BoardPict.clr_white, GUI_Dim.TEXT_FONT_SIZE);
    } // drawBearStats( )

    public void drawPipStats(Board aBoard) {
        String m1, m2;
        m1 = "White Pip count: " + aBoard.getPipCount( whiteP );
        m2 = "Black Pip count: " + aBoard.getPipCount( blackP );

        gBuffer.setColor(BoardPict.clr_black);
        gBuffer.fill(new Rectangle2D.Double(furtherleft, bearTop + 2*textLineHeight
            , GUI_Dim.GUI_WIDTH, 2*textLineHeight));

        putString(m1, /*X:*/furtherleft, /*Y:*/bearTop + 3*textLineHeight, 
            BoardPict.clr_white, GUI_Dim.TEXT_FONT_SIZE);
        putString(m2, /*X:*/furtherleft, /*Y:*/bearTop + 4*textLineHeight, 
            BoardPict.clr_white, GUI_Dim.TEXT_FONT_SIZE);
    } // drawPipStats( )

    /**
     * Currently turned off since score comes from one of the strategies now 
     * rather than directly from the board
     */
    public void drawBoardScore(Board aBoard) {
        //     String m1, m2;
        //     m1 = "White Board Score: " + myBoard.superMegaHappyScore(myAI.getCautious( ), 
        //           white );
        //     m2 = "Black Board Score: " + myBoard.superMegaHappyScore(myAI.getCautious( ), 
        //           black );
        //     gBuffer.setColor(BoardPict.clr_black);
        //     gBuffer.fill(new Rectangle2D.Double(furtherleft, bearTop + 4*textLineHeight
        //        , GUI_Dim.GUI_WIDTH, 2*textLineHeight));
        // 
        //     putString(m1, /*X:*/furtherleft, /*Y:*/bearTop + 5*textLineHeight, 
        //         BoardPict.clr_white, 
        //         GUI_Dim.TEXT_FONT_SIZE);
        //     putString(m2, /*X:*/furtherleft, /*Y:*/bearTop + 6*textLineHeight, 
        //         BoardPict.clr_white, 
        //         GUI_Dim.TEXT_FONT_SIZE);
    } // drawBoardScore( )

    /**
     * Puts their name on board ("white" "black" "game_unstarted")
     */
    public void drawCurrentPlayer( Board aBoard ) {
        String m1 = "Current Player: " + nameOf(currentPlayer );
        String m2;
        if (myAI != null) {
            m2 = "AI: " + nameOf(blackP);
        } else {
            m2 = "AI is null";
        }
        gBuffer.setColor(BoardPict.clr_darkgray);
        gBuffer.fill(new Rectangle2D.Double(furtherleft, bearTop + 6*textLineHeight
            , GUI_Dim.GUI_WIDTH, 1*textLineHeight));

        putString(m1, /*X:*/furtherleft, /*Y:*/bearTop + 7*textLineHeight, 
            BoardPict.clr_white, GUI_Dim.TEXT_FONT_SIZE);
        putString(m2, /*X:*/furtherleft, /*Y:*/bearTop + 8*textLineHeight, 
            BoardPict.clr_white, GUI_Dim.TEXT_FONT_SIZE);
    }

    public String nameOf(Player playerColor ) {
        if (playerColor == null) {
            return "game unstarted";
        } else {
            return playerColor.toString( );
        }
    } 

    /* gets nameOf( )  [currentPlayerName] */

    private void putString(String message, int x, int y, Color c, int fontsize) {
        gBuffer.setFont(new Font("Arial", Font.BOLD, fontsize));
        gBuffer.setColor(c);
        gBuffer.drawString(message, x, y);
    } // putString( )

    /**
     * Driver, organizes data and color before calling general purpose "drawDice"
     * [ ]For formal game start we would want to draw one black die and one white die.
     */
    private void drawCurrentDice(Board aBoard ) {
        int[ ] diceXcoord = { GUI_Dim.DICE1_LEFT, GUI_Dim.DICE2_LEFT };
        int diceTop = GUI_Dim.DICE_TOP;
        // set default colors for white
        Color[ ] diceColor = { myBoardPict.clr_white, myBoardPict.clr_white };
        Color[ ] dotColor = { myBoardPict.clr_black, myBoardPict.clr_black };
        if (currentPlayer==blackP) {
            diceColor[0] = diceColor[1] = myBoardPict.clr_black;
            dotColor[0] = dotColor[1] = myBoardPict.clr_white;
        } else if (currentPlayer==null/* means game_unstarted*/ ) {
            // one white die, one black die,
            // but this might never be seen since doRoll chooses a player before calling this
            diceColor[1] = myBoardPict.clr_black;
            dotColor[1] = myBoardPict.clr_white;
        }
        for (int i=1; i <= Board.howManyDice; ++i) {
            // note: getDie(i) might be 0 "not rolled"... just draw blank dice
            drawDie(aBoard, aBoard.myDice.getDie(i),diceXcoord[i-1]/*beware obob!*/, 
                diceTop, diceColor[i-1], dotColor[i-1], aBoard.myDice.getUsedDie(i)/* yes, i*/);
        }
    } /* drawCurrentDice( ) */

    /**
     * Called by "drawCurrentDice( )"
     * x,y specify the upper left corner of the die image
     * note: gBuffer is Graphics2D
     * @param roll might be 0 (Dice.UNROLLED), in which case draw blank face die
     */
    private void drawDie(Board aBoard, int roll, int x, int y, Color dicecolor, 
    Color dotcolor, boolean isUsed) {
        int diceSize = GUI_Dim.DICE_SIZE; /* 25 */
        int dotSize = GUI_Dim.DOT_SIZE; /* 4 */
        int leftX = GUI_Dim.DICE_MARGIN; /* 2 */
        int topY = leftX;   /* 2 */
        int midX = (diceSize / 2) - GUI_Dim.DICE_MARGIN; /* 11 */
        int midY = midX;
        int rightX = 2 + (2 * (midX - leftX));
        /* trying to evenly space. was 19, 20 ugly, trying 22 */
        int lowY = rightX;

        gBuffer.setColor(dicecolor);
        gBuffer.fill(new Rectangle2D.Double(x, y, diceSize, diceSize ));
        gBuffer.setColor(dotcolor);

        switch(roll) { // draw the dots
            case 0: // blank face unrolled die
            break;
            case 1:
            gBuffer.fill(new Rectangle2D.Double(x+midX, y+midY, dotSize, dotSize));
            break;
            case 2:
            gBuffer.fill(new Rectangle2D.Double(x+leftX, y+topY, dotSize, dotSize));
            gBuffer.fill(new Rectangle2D.Double(x+rightX, y+lowY, dotSize, dotSize));
            break;
            case 3:
            gBuffer.fill(new Rectangle2D.Double(x+leftX, y+topY, dotSize, dotSize));
            gBuffer.fill(new Rectangle2D.Double(x+midX, y+midY, dotSize, dotSize));
            gBuffer.fill(new Rectangle2D.Double(x+rightX, y+lowY, dotSize, dotSize));
            break;
            case 4:
            gBuffer.fill(new Rectangle2D.Double(x+leftX, y+topY, dotSize, dotSize));
            gBuffer.fill(new Rectangle2D.Double(x+rightX, y+lowY, dotSize, dotSize));
            gBuffer.fill(new Rectangle2D.Double(x+rightX, y+topY, dotSize, dotSize));
            gBuffer.fill(new Rectangle2D.Double(x+leftX, y+lowY, dotSize, dotSize));
            break;
            case 5:
            gBuffer.fill(new Rectangle2D.Double(x+leftX, y+topY, dotSize, dotSize));
            gBuffer.fill(new Rectangle2D.Double(x+rightX, y+lowY, dotSize, dotSize));
            gBuffer.fill(new Rectangle2D.Double(x+rightX, y+topY, dotSize, dotSize));
            gBuffer.fill(new Rectangle2D.Double(x+leftX, y+lowY, dotSize, dotSize));
            gBuffer.fill(new Rectangle2D.Double(x+midX, y+midY, dotSize, dotSize));
            break;
            case 6:
            gBuffer.fill(new Rectangle2D.Double(x+leftX, y+topY, dotSize, dotSize));
            gBuffer.fill(new Rectangle2D.Double(x+rightX, y+lowY, dotSize, dotSize));
            gBuffer.fill(new Rectangle2D.Double(x+rightX, y+topY, dotSize, dotSize));
            gBuffer.fill(new Rectangle2D.Double(x+leftX, y+lowY, dotSize, dotSize));
            gBuffer.fill(new Rectangle2D.Double(x+leftX, y+midY, dotSize, dotSize));
            gBuffer.fill(new Rectangle2D.Double(x+rightX, y+midY, dotSize, dotSize));
            break;
        }
        if (isUsed) {
            gBuffer.setColor(Color.red);
            int extra = 2;
            gBuffer.drawLine(x-extra,y-extra, x+diceSize+extra,y+diceSize+extra);
            gBuffer.drawLine(x-extra,y+diceSize+extra, x+diceSize+extra,y-extra);
        } else {
            gBuffer.setColor(Color.green);
            gBuffer.drawLine(x,y, x+diceSize,y);
            gBuffer.drawLine(x,y+diceSize, x+diceSize,y+diceSize);
            //gBuffer.fill(new Rectangle2D.Double(x,y+diceSize+2, diceSize, dotSize));
        }
    } // drawDice( )

    /**
     * drawTriangle: Draws a triangle with the point facing downward, 
     * x,y gives left corner coordinates and a number for color.
     * Draws red border around a selected point.
     * uses: status, gBuffer, selectedPointLoc 
     */
    private void drawTriangle(Board aBoard, int x, int y, int point_color) {
        if (point_color==white) {
            gBuffer.setColor(myBoardPict.color_point_white);
        } else {
            gBuffer.setColor(myBoardPict.color_point_black);
        }

        int[ ] myXs = new int[3]; 
        /* re-written in attempt to fix bluej's "cannot parse" but still "cannot parse" */
        myXs[0] = x; myXs[1] = x + myBoardPict.POINT_WIDTH/2; 
        myXs[2] = x + myBoardPict.POINT_WIDTH;
        int[ ] myYs = new int[3]; //= new int[] { y, y + myBoardPict.POINT_HEIGHT, y};
        myYs[0] = y; myYs[1] = y + myBoardPict.POINT_HEIGHT; myYs[2] = y;

        Polygon tri = new Polygon(myXs, myYs, /*howManyVertices*/3);

        gBuffer.fillPolygon(tri);
        if ((aBoard.aPointIsSelected) && (aBoard.getSelectedPointLoc( ) == getPointNum(x,y))) {
            debug_data("TRI: Calling getPointNum",0);          
            gBuffer.setColor(BoardPict.clr_red);
            debug_data("TRI: selectedPointLoc = ",aBoard.getSelectedPointLoc( ) );
        }
        gBuffer.drawPolygon(tri);
    } // drawTriangle( )

    /**
     * drawTriangleRev: Draws a triangle with the point facing upward,
     * x,y gives left corner coordinates and a number for color.
     * If a point on the gui board is "selected" by mouse, selectedPointLoc gets
     * drawn with red border. (Sometimes?)
     * uses: status, gBuffer, selectedPointLoc 
     */
    private void drawTriangleRev(Board aBoard, int x, int y, int point_color) {
        if (point_color==neutral) {
            gBuffer.setColor(myBoardPict.color_point_white);
        } else {
            gBuffer.setColor(myBoardPict.color_point_black);
        }

        int[ ] myXs = new int[3]; 
        // new int[] { x, x + myBoardPict.POINT_WIDTH/2, x + myBoardPict.POINT_WIDTH};
        myXs[0] = x; myXs[1] = x + myBoardPict.POINT_WIDTH/2; 
        myXs[2] = x + myBoardPict.POINT_WIDTH;
        int[ ] myYs = new int[3]; // = new int[] { y, y - myBoardPict.POINT_HEIGHT, y};
        myYs[0] = y; myYs[1] = y - myBoardPict.POINT_HEIGHT; myYs[2] = y;

        Polygon tri = new Polygon(myXs, myYs, /*howManyVertices*/3);
        gBuffer.fillPolygon(tri);
        if ((aBoard.aPointIsSelected) && (aBoard.getSelectedPointLoc( ) == getPointNum(x,y))) {
            debug_data("DEBUG: drawTriangleRev: Calling getPointNum",0);
            gBuffer.setColor(BoardPict.clr_red);
            debug_data("drawTriangleRev: selectedPointLoc = ", 
                aBoard.getSelectedPointLoc( ) );
        }
        gBuffer.drawPolygon(tri);
    } // drawTriangleRev

    /**
     * Draws the Game board onto the buffer
     */
    private void drawBoard(Board aBoard) {
        // Set the green color
        gBuffer.setColor(BoardPict.clr_green);

        // Draw the two ("A" left & "B" right) halves of the board
        int leftA = BoardPict.LEFT_MARGIN;
        int quadwidth = BoardPict.QUADRANT_WIDTH /*+ 2/*192*/;
        int pointWidth=(BoardPict.POINT_WIDTH+BoardPict.POINT_MARGIN)/*32*/;
        int leftBar = leftA + quadwidth; /*+2/*192*/;
        int leftB = leftBar + BoardPict.BAR_WIDTH; /*238*/
        int top = BoardPict.TOP_MARGIN;
        int height = BoardPict.BOARD_HEIGHT/*360*/;
        int bottom = top + height /*360*/;
        Rectangle2D.Double halfBoardA = new Rectangle2D.Double(leftA, top, quadwidth , height);
        Rectangle2D.Double halfBoardB = new Rectangle2D.Double(leftB, top, quadwidth, height);

        gBuffer.draw(halfBoardA);
        gBuffer.fill(halfBoardA);
        gBuffer.draw(halfBoardB);
        gBuffer.fill(halfBoardB);

        // Draw the bar
        gBuffer.setColor(BoardPict.clr_brown); /* brown? */
        // couldn't following use already calculated Rectangle BoardPict.barRect?
        // but rect2D constructor can't accept Rectangle as param, so would have to unbox
        // or have BoardPict build a Rectangle2D from the start.
        Rectangle2D.Double bar = new 
            Rectangle2D.Double(leftBar, top, BoardPict.BAR_WIDTH /*-4/*46*/, height);
        gBuffer.draw(bar);
        gBuffer.fill(bar);

        gBuffer.setColor(BoardPict.color_point_white);
        int point_color = white; // for toggling 

        // Draw the points, four at a time
        for (int i=0; i<=quadwidth-10/*180*/; i+=pointWidth) {
            if (point_color == neutral) {
                point_color = white;
            } else {
                point_color = neutral;
            }

            // upper left...
            drawTriangle(aBoard,leftA+i, top, point_color);
            // lower left (drawTriRev uses reverse color, I think)
            drawTriangleRev(aBoard,leftA+i, bottom, point_color);

            // upper right...
            drawTriangle(aBoard, leftB+i, top, point_color);
            // lower right (drawTriRev uses reverse color, I think)
            drawTriangleRev(aBoard, leftB+i, bottom, point_color);
        }
        if (drawPointNumbers) { drawNumbersOnPoints( );}
    } // drawBoard( )

    private void drawNumbersOnPoints( ) {
        int scootchX = BoardPict.POINT_WIDTH / 3; // don't go all the way to middle
        int lineHeight = GUI_Dim.TEXT_LINE_HEIGHT; // slightly larger than font Height
        int fontSize = GUI_Dim.TEXT_FONT_SIZE;
        int descender = lineHeight - fontSize;
        int howManyPoints = Board.HOW_MANY_POINTS_IN_QUADRANT;
        int howManyPointsOnHalfBoard = howManyPoints * (Board.HOW_MANY_QUADRANTS / 2);
        for (int p = 1; p<= Board.howManyPoints; ++p) {
            // first 2 quadrants (from white's point of view, as everything is)
            // Have numbers sitting above, while last two have numbers hanging below
            if (p <= howManyPointsOnHalfBoard) { // upper
                putString(String.valueOf(p), /*X:*/findX(p) + scootchX, /*Y:*/findY(p) - descender, 
                    BoardPict.clr_green, fontSize);
            } else {
                putString(String.valueOf(p), /*X:*/findX(p) + scootchX, 
                    /*Y:*/findY(p) + fontSize /* lineHeight?*/, 
                    BoardPict.clr_green, fontSize);
            }
        }
    } 

    /* drawNumbersOnPoints */

    private void drawBar(Board aBoard) {
        gBuffer.setColor(BoardPict.clr_bar); /* dark-brown? */
        int left = BoardPict.LEFT_MARGIN + BoardPict.barRect.x + 2 /*192*/;
        int topBlack = BoardPict.TOP_MARGIN + BoardPict.BAR_BLACK_TOP; /* topmarg + 120 */
        int topWhite = BoardPict.TOP_MARGIN + BoardPict.BAR_WHITE_TOP; /* topmarg + 200 */
        gBuffer.drawRect(left,topBlack/*?*/,BoardPict.BAR_WIDTH - 4,BoardPict.BAR_ZONE_HEIGHT);
        gBuffer.fill(new Rectangle2D.Double(left, topBlack, BoardPict.BAR_WIDTH - 4, 
                BoardPict.BAR_ZONE_HEIGHT));
        gBuffer.fill(new Rectangle2D.Double(left, topWhite, BoardPict.BAR_WIDTH - 4, 
                BoardPict.BAR_ZONE_HEIGHT));

        gBuffer.setColor(BoardPict.clr_bar_white);
        gBuffer.fill(new Rectangle2D.Double(left, (topBlack+topWhite)/2, 
                BoardPict.BAR_WIDTH - 3, BoardPict.BAR_ZONE_HEIGHT));
        left = BoardPict.LEFT_MARGIN + BoardPict.barRect.x + BoardPict.BAR_MARGIN_TO_BLOT; /* 201 */
        int blotSize = BoardPict.BLOT_WIDTH; /* 29 */

        if (aBoard.onBar(blackP)) {
            gBuffer.setColor(myBoardPict.clr_black);
            gBuffer.fill(new Ellipse2D.Double(left, topBlack + 5, blotSize, blotSize));
            if (aBoard.bar[Player.BLACK] > 1) {
                putString(String.valueOf(aBoard.bar[Player.BLACK]), 
                    /*X:*/left+(BoardPict.BAR_WIDTH/3), 
                    /*Y:*/topBlack + (BoardPict.BAR_ZONE_HEIGHT / 2), 
                    BoardPict.clr_red, GUI_Dim.TEXT_BIGGER_SIZE);
            }
        }

        if (aBoard.onBar(whiteP)) {
            gBuffer.setColor(myBoardPict.clr_white);
            gBuffer.fill(new Ellipse2D.Double(left, topWhite + 5, blotSize, blotSize));
            if (aBoard.bar[Player.WHITE] > 1) {
                putString(String.valueOf(aBoard.bar[Player.WHITE]), 
                    /*X:*/left+(BoardPict.BAR_WIDTH/3), 
                    /*Y:*/topWhite + (BoardPict.BAR_ZONE_HEIGHT / 2), 
                    BoardPict.clr_red, GUI_Dim.TEXT_BIGGER_SIZE);
            }
        }

    } // drawBar( )

    /**
     * Attempt to show the recent move. 
     * paint( ) will call drawBlots which will probably obliterate this, unless
     * instead of using paint to highlight we instead have (two) buttons like the 
     * moveHere buttons, showing where the latest moves were!
     * @param playerColor - having this lets us possibly highlight bar (or bear!)
     * @param howMany - probably just one in most cases, might be 2
     */
    //     public void highlightBlot(Board aBoard,Player playerColor,int pointNum, int howMany) {
    //         show buttons on endpoints of latest move.
    //         better idea, save latest Move and mark all its partial
    //     }

    /**
     * Draw all blots of current board. If more than 5 are on a point,
     * just draw the first 5 and show a number of how many are actually there.
     */
    private void drawBlots(Board aBoard) {
        //debug_msg("drawBlots()");
        int blotSize = BoardPict.BLOT_WIDTH; /* 29 */
        int blotSizePlus = BoardPict.BLOT_WIDTH + 1; // 30, for padding
        int topOfUpperBlotStack = BoardPict.TOP_MARGIN + BoardPict.BOARD_TOP_EDGE 
            + BoardPict.QUADRANT_HEIGHT;
        int topOfLowerBlotStack = /*Y:255*/BoardPict.TOP_MARGIN + BoardPict.BOARD_TOP_EDGE 
            + BoardPict.QUADRANT_HEIGHT + BoardPict.GUTTER_HEIGHT;

        for (int point=1; point<=12; point++) {
            int howManyBlots = aBoard.getHowManyBlotsOnPoint(point);
            int howManyBlotsToDraw = 5; // draw 0..5 blots
            if (howManyBlots < 5) { 
                howManyBlotsToDraw = howManyBlots; 
            }

            for (int i=0; i<howManyBlotsToDraw; i++) {
                gBuffer.setColor( aBoard.getPlayerOnPoint(point).getJavaColor( ) );
                gBuffer.fill(new Ellipse2D.Double(findX(point), findY(point) + i*blotSize, 
                        blotSize, blotSize));
            }
            if (howManyBlots > 5) {
                /* note: findX( ) can return -1 if it doesn't know the point */
                putString(String.valueOf(howManyBlots), 
                    /*X:*/findX(point)+ (blotSize/3), topOfUpperBlotStack, 
                    BoardPict.clr_red, GUI_Dim.TEXT_BIGGER_SIZE);
            }
        } // for point 1..12

        for (int point=13; point<=24; point++) {
            int howManyBlots = aBoard.getHowManyBlotsOnPoint(point);
            int howManyBlotsToDraw = 5; // draw 0..5 blots
            if (howManyBlots < 5) { 
                howManyBlotsToDraw = howManyBlots; 
            }

            for (int i=0; i<howManyBlotsToDraw; i++) {
                gBuffer.setColor( aBoard.getPlayerOnPoint(point).getJavaColor( ) );
                gBuffer.fill(new Ellipse2D.Double(findX(point), 
                        findY(point) - ((i+1)*blotSize), 
                        blotSize, blotSize));
            }
            if (howManyBlots>5) {
                putString(String.valueOf(howManyBlots), 
                    /*X:*/findX(point)+(blotSize/3), topOfLowerBlotStack, 
                    BoardPict.clr_red, GUI_Dim.TEXT_BIGGER_SIZE);
            }
        } // for point 13..24
    } // drawBlots( )

    /**
     * This seems to figure out which point on display board is touched by the 
     * x,y int coordinates.
     * Might be bar, and in that case I have to know who the current player is.
     */
    public int getPointNum(int point_x, int point_y) {
        boolean leftHalf=true;
        boolean topHalf=true;
        int i=1;

        debug_data("point_x = ",point_x);
        debug_data("point_y = ",point_y);
        // Find which portion of the board the click occurred in
        if (point_y >= BoardPict.BOARD_MIDPOINT_VERTICAL_PIXELS) {
            topHalf = false;
        }

        if (point_x >= BoardPict.BOARD_MIDPOINT_HORIZONTAL_PIXELS) {
            point_x -=BoardPict.BOARD_MIDPOINT_HORIZONTAL_PIXELS;
            debug_data("point_x changed to ", point_x);
            leftHalf = false;
        }
        /* debug_data("half = ", half);
        debug_data("quad = ", quad); */
        int pointWidthWithSpace = (BoardPict.POINT_WIDTH + 2); /* 32 */
        // Find how many times we can subtract pointWidthWithSpace from the position
        // while remaining positive
        for ( i=1; point_x >= pointWidthWithSpace; point_x -= pointWidthWithSpace) {
            i++;
        }

        // Compensate for top/bottom and left/right
        // quadrant I is 1..6, quad II is 7..12
        if (topHalf) {
            if (leftHalf) {
                i = (6-i) + 7;
            } else {
                i = (6-i) + 1;
            }
        } else { /* bottom half. quad III is 13..18, quad IV is 19..24 */
            if (leftHalf)  {
                i += 12;
            } else {
                i += 18;
            }
        }
        // Useful debug statements
        debug_data("getPointNum returns ",i);
        return i;
    } // getPointNum( )

    public void debug_data( String msg, int data) {
        /*
        System.out.print("DEBUG: ");
        System.out.print(msg);
        System.out.println(data);
         */
    } // debug_data( )

    /**
     * Set up a new game
     */
    public void resetGame(Board aBoard) {
        // System.out.println("GAME RESET WAS HIT");
        // Reset Game data /
        aBoard.myDice.reset( ); /* puts to unrolled, unused, countdown=0 */ 
        aBoard.setSelectedPointLoc( Board.NO_MOVE_SELECTED, currentPlayer ); 
        // makes aPointIsSelected be false
        currentPlayer = null; /* means game_unstarted; /* = white; */

        // Reset buttons
        fButton[btn_CancelChoice].setEnabled(false);
        fButton[btn_RollDice].setEnabled(true); /* was false, why? 
        Was this why new game didn't work? */
        fButton[btn_BearOff].setEnabled(false);
        fButton[btn_NewGame].setEnabled(false);
        fButton[btn_AtPotentialMove1].setVisible(false);
        fButton[btn_AtPotentialMove2].setVisible(false);

        // Re-create the board... (why??)
        mainBoard = new Board(this);

        // Have the Status object reset game values, keep network value
        status.newGame(); // doesn't do much

        repaint();
    } // resetGame( )
    //class Game
}