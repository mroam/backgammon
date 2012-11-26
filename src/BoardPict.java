import java.awt.*; // supplies the Color

/**
 * The playing board has 4 quadrants (upper/lower, left/right)
 * with the bar standing vertically between them.
 * 
 * Colors are defined here, so they can be tweaked.
 * 
 * @author Mike Roam
 * @version 2012 Jan 30
 */
public class BoardPict {
    // Color to be used when drawing a white blot
    static final Color clr_white = new Color(200, 200, 200);
    // Color to be used when drawing a black blot
    static final Color clr_black = new Color(50, 50, 50);
    static final Color clr_darkgray = Color.DARK_GRAY;
    static final Color clr_green = new Color(0 , 150, 0);
    static final Color clr_red = Color.red;
    static final Color clr_brown = new Color(128,64,0);
    static final Color clr_bar = new Color(100, 50, 0); // dark brown
    static final Color clr_bar_white = Color.white; 

    // Color to be used when drawing a black point
    static final Color color_point_black = new Color(130, 70, 0); //  brown
    // Color to be used when drawing a white point
    static final Color color_point_white = new Color(240, 215, 100); // pale yellow

    static final int LEFT_MARGIN = 20;
    final static int BOARD_LEFT_EDGE = 0;  // how does this differ from margin? abs?

    final static int TOP_MARGIN = 60; // was in Game
    final static int BOARD_TOP_EDGE = 0;

    final static int BOARD_MIDPOINT_VERTICAL_PIXELS = 200;
    final static int BOARD_MIDPOINT_HORIZONTAL_PIXELS = 238; /*Is the bar before or after this??*/

    final static int QUADRANT_WIDTH = 190; // is this right?
    final static int QUADRANT_HEIGHT = 160;

    final static int BAR_ZONE_HEIGHT = 40; /* the bar cubes in middle of tall bar */
    final static int BAR_WIDTH = 50;
    final static int BEAR_WIDTH = 50;
    // "gutter" is the height of the horizontal zone between the upper & lower quadrants */
    final static int GUTTER_HEIGHT = 40;
    final static int BAR_MARGIN_TO_BLOT = 11; /* seems small? */
    final static int BAR_BLACK_TOP = 120;
    final static int BAR_WHITE_TOP = 200;

    final static int BOARD_WIDTH = (2*QUADRANT_WIDTH) + BAR_WIDTH; /* 430; /* should be resizeable someday */
    final static int BOARD_RIGHT_EDGE = BOARD_LEFT_EDGE + BOARD_WIDTH;
    final static int BOARD_HEIGHT = (2*QUADRANT_HEIGHT) + GUTTER_HEIGHT; /* 360; */
    final static int BOARD_BOTTOM_EDGE = BOARD_TOP_EDGE + BOARD_HEIGHT;

    final static int BLOT_WIDTH = 29;

    final static int POINT_WIDTH = 30;
    final static int POINT_MARGIN = 2; // gap between points to allow for linewidth??
    final static int POINT_HEIGHT = QUADRANT_HEIGHT;

    final static Rectangle upperLeftRect = new Rectangle(BOARD_LEFT_EDGE, BOARD_TOP_EDGE, QUADRANT_WIDTH, QUADRANT_HEIGHT);
    final static Rectangle upperRightRect = new Rectangle(BOARD_LEFT_EDGE + QUADRANT_WIDTH + BAR_WIDTH, BOARD_TOP_EDGE, 
            QUADRANT_WIDTH, QUADRANT_HEIGHT);
    final static Rectangle lowerLeftRect = new Rectangle(BOARD_LEFT_EDGE, BOARD_TOP_EDGE + QUADRANT_HEIGHT + GUTTER_HEIGHT,
            QUADRANT_WIDTH, QUADRANT_HEIGHT);
    final static Rectangle lowerRightRect = new Rectangle(BOARD_LEFT_EDGE + QUADRANT_WIDTH + BAR_WIDTH, 
            BOARD_TOP_EDGE + QUADRANT_HEIGHT + GUTTER_HEIGHT,
            QUADRANT_WIDTH, QUADRANT_HEIGHT);

    /* triangle constructor receives left,top,width,height */

    final static Rectangle barRect = new Rectangle(BOARD_LEFT_EDGE + QUADRANT_WIDTH, BOARD_TOP_EDGE, 
            BAR_WIDTH, BOARD_BOTTOM_EDGE);
            
            // temporarily drawing bearOff past right edge of board without leaving margin room for it.
    final static Rectangle bearRect = new Rectangle(BOARD_RIGHT_EDGE, BOARD_TOP_EDGE, 
            BEAR_WIDTH, BOARD_BOTTOM_EDGE);

    /**
     * Constructor for objects of class BoardPict.
     * Hmmm, could use static initializers like barRect above. 
     * If board gets resizeable many of the things above would need to merely lose the "final".
     * Beware of changing board size while a move or click is going on? 
     */
    public BoardPict() {
        //upperLeftRect = new Rectangle(BOARD_LEFT_EDGE, BOARD_TOP_EDGE, QUADRANT_WIDTH, QUADRANT_HEIGHT);
        //upperRightRect = new Rectangle(BOARD_LEFT_EDGE + QUADRANT_WIDTH + BAR_WIDTH, BOARD_TOP_EDGE, 
        //    QUADRANT_WIDTH, QUADRANT_HEIGHT);

        //lowerLeftRect = new Rectangle(BOARD_LEFT_EDGE, BOARD_TOP_EDGE + QUADRANT_HEIGHT + GUTTER_HEIGHT,
        //    QUADRANT_WIDTH, QUADRANT_HEIGHT);
        //lowerRightRect = new Rectangle(BOARD_LEFT_EDGE + QUADRANT_WIDTH + BAR_WIDTH, 
        //    BOARD_TOP_EDGE + QUADRANT_HEIGHT + GUTTER_HEIGHT,
        //    QUADRANT_WIDTH, QUADRANT_HEIGHT);

        // note: the following happens above
        // barRect = new Rectangle(BOARD_LEFT_EDGE + QUADRANT_WIDTH, BOARD_TOP_EDGE, 
        //    BAR_WIDTH, BOARD_BOTTOM_EDGE); 
    }

} /* class BoardPict */
