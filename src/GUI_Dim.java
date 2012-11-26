import java.awt.*;

/**
 * The GUI basically has a column of buttons that should be done in a JPane
 * 
 * @author Mike Roam
 * @version 2012 Feb 3
 */
public class GUI_Dim
{
      /* shouldn't be static if the GUI becomes resizable */
    final static int MARGIN = 25; // space between playing board and command gui
    final static int LEFT_EDGE = BoardPict.BOARD_RIGHT_EDGE + MARGIN; /* 430 + 25 */
    final static int STATS_TOP_MARGIN = 30; /* 30 */
    
    final static int TEXT_SMALLER_SIZE = 10;
    final static int TEXT_FONT_SIZE = 12;
    final static int TEXT_LINE_HEIGHT = 14;
    final static int TEXT_BIGGER_SIZE = 15;
    final static int TEXT_BIGGER_LINE_HEIGHT = 18; /* 18 */

    
    final static int GUI_WIDTH = 150; /* ? */
    final static int GUI_HEIGHT = BoardPict.BOARD_HEIGHT; /* 360 */
    
        // Dice pictures
    static final int DOT_SIZE = 4;
    static final int DICE_SIZE = 25; // width & height
    static final int DICE_MARGIN = 2; /* how close the dots are to the edge of the dice face */

    final static int DICE1_LEFT = LEFT_EDGE + 24; /* 479 */
    final static int DICE2_LEFT = DICE1_LEFT + 50;
    final static int DICE_TOP = 200;
    
    final static int TOP_OF_BUTTON_ZONE = 230;
     final static int BTN_LEFT_EDGE = LEFT_EDGE + 20; /* 475. should be resizeable someday */
    final static int BTN_WIDTH = 135;
    final static int BTN_HEIGHT = 25;
    final static int BTN_MARGIN = 0; // space between buttons
   
    /* triangle constructor receives left,top,width,height  <-- huh?? */
    final static Rectangle guiRect = new Rectangle(LEFT_EDGE, BoardPict.BOARD_TOP_EDGE, GUI_WIDTH, GUI_HEIGHT);

    
} /* class GUI_Dim */

