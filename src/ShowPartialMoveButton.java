/**
 * For showing PartialMoves on official GUI
 * 
 * @author Mike Roam
 * @version 2012 May 11
 * 
 * not in use anymore: replace by ShowPartialMoveLabel
 * Using a JLabel instead so I can color the start and end positions differently
 * and make them really skinny so 4 can appear side-by-side (each offset by its number).
 */

import javax.swing.*;
import java.awt.*;
//import java.awt.event.*;
//import java.awt.geom.*;
//import java.awt.image.*;

public class ShowPartialMoveButton extends JButton {
    protected int btnWidth = BoardPict.POINT_WIDTH / Board.maxMovesCount; // 28/4 == 7
    protected int btnHeight = GUI_Dim.TEXT_LINE_HEIGHT; // 10

    Container myContainer;
    Game myGame;
    int myNum = 1; // should be 1,2,3 or 4
    String myName = "nameless";
    public static final long serialVersionUID = 1L; // version 1

    /**
     * Constructor for objects of class ShowPartialMoveButton
     */
    public ShowPartialMoveButton(Container aContentPane, Game newMyGame, /*String newName,*/ int moveNum) {
        //         if (newName != null) {
        //             myName = newName;
        //         }
        if ((moveNum < 1) || (moveNum > Board.maxMovesCount)) {
            System.err.println("Weird moveNum '" + moveNum + "' in ShowPartialMoveButton constructor, should "
                + "be in range [1" + Board.maxMovesCount + "]");
        }
        myNum = moveNum; // should be 1,2,3 or 4
        myContainer = aContentPane;
        myGame = newMyGame;
        myContainer.setLayout(null); // <- necessary?
        myContainer.add(this);
        this.setEnabled(false);
        
        //Create font.
        String myFontName = this.getFont().getName(); // Default label font  
        int myFontStyle = this.getFont().getStyle(); // Default label font style  
        int myFontSize =  GUI_Dim.TEXT_SMALLER_SIZE; // currently 8?
        Font newLabelFont=new Font(myFontName, myFontStyle ,myFontSize);  

        this.setFont(newLabelFont);  //  //Set JLabel font using new created font 
        this.setText(/*myName*/Integer.toString(moveNum));
    }

    /**
     * Idea, unwritten: draws 2 buttons and puts line between them.
     * Maybe this belongs in game, near Game.showPartialMove( )
     */
    public static void drawMove(int startPt, int endPt, int moveNum ) {
        System.err.println("ShowPartialMoveButton( ) isn't written yet");
    }

    /**
     * Is used to display the PartialMoves on the GUI, somewhere on the
     * point specified. Can handle bar & bear.
     * Uses Game.findX(pointNum) and Game.findY(pointNum) which report the
     * coords of the specified point.
     */
    public void drawOnPoint(int point) {
        Insets in = myGame.getInsets();
        int btnLeft = 0;
        int btnTop = 0;

        if ( point==Game.whiteP.getBarLoc( ) ) {
            btnLeft = BoardPict.barRect.x - in.left;
            btnTop = BoardPict.TOP_MARGIN + BoardPict.BAR_WHITE_TOP - in.top;
        } else if ( point==Game.blackP.getBarLoc( ) ) {
            btnLeft = BoardPict.barRect.x - in.left;
            btnTop = BoardPict.TOP_MARGIN + BoardPict.BAR_BLACK_TOP - in.top;
        } else if ((point==Game.whiteP.getBearOffLoc( )) 
        || ( point==Game.whiteP.getBeyondBearOffLoc( ) )) {
            btnLeft = BoardPict.bearRect.x - in.left;
            btnTop = BoardPict.TOP_MARGIN + BoardPict.BAR_WHITE_TOP - in.top;

        } else if ((point==Game.blackP.getBearOffLoc( )) 
        || ( point==Game.blackP.getBeyondBearOffLoc( ) )) {
            btnLeft = BoardPict.bearRect.x - in.left;
            btnTop = BoardPict.TOP_MARGIN + BoardPict.BAR_BLACK_TOP - in.top;

        } else { // regular 1..24 board point
            btnLeft = myGame.findX(point) - in.left;
            btnTop = myGame.findY(point) + btnHeight - in.top; // going extra squidge lower
            // if point is 1..12 then stand above the line
            // First 2 quadrants (from white's point of view, as everything is)
            // have numbers sitting above, while last two have numbers hanging below
            if (point <= (Board.HOW_MANY_QUADRANTS/2) * Board.HOW_MANY_POINTS_IN_QUADRANT) {
                btnTop = myGame.findY(point)  - (2* btnHeight) - in.top; // extra squidge higher
            }
        }
        setBounds(btnLeft + (( myNum-1) * btnWidth),btnTop, btnWidth,btnHeight);
        setVisible(true);
        myGame.repaint();
    } // drawOnPoint
}
