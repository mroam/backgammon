/* **************************************************************
JBackgammon (http://jbackgammon.sf.net)

Copyright (C) 2002
George Vulov <georgevulov@hotmail.com>
Cody Planteen <frostgiant@msn.com>

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
 * File: tablaMouseListener.java
 *
 * The mouse listener class which identifies
 * which point the mouse click occured on. 
 */

import java.awt.event.*;
public class MyMouseListener extends MouseAdapter {
    Game myGame;

    public MyMouseListener(Game newMyGame) {
        //Find where the main Game class is so we can use methods from it
        myGame = newMyGame;
    } // MyMouseListener( ) constructor

    public void mouseReleased(MouseEvent e) {
        //Adjust values as if the board was set in the top left corner at (0,0)
        int mx = e.getX() - BoardPict.LEFT_MARGIN;
        int my = e.getY() - BoardPict.TOP_MARGIN;

        boolean hitInPlayableArea = false;
        if (BoardPict.upperLeftRect.contains(mx,my)) { hitInPlayableArea = true; }
        if (BoardPict.upperRightRect.contains(mx,my)) { hitInPlayableArea = true; }
        if (BoardPict.lowerLeftRect.contains(mx,my)) { hitInPlayableArea = true; }
        if (BoardPict.lowerRightRect.contains(mx,my)) { hitInPlayableArea = true; }

        if (BoardPict.barRect.contains(mx,my)) { // clicked on bar
            if (myGame.getMyBoard( ).myDice.getRolled( )) {
                myGame.getMyBoard( ).selectABar( myGame.getCurrentPlayer( ) );
            } else {
                System.out.println("you've got to roll the dice before moving");
            }

        } else if (hitInPlayableArea && !myGame.status.observer ) {
            if (myGame.getMyBoard( ).myDice.getRolled( )) {
                myGame.getMyBoard( ).selectAPoint( myGame.getPointNum(mx,my), myGame.getCurrentPlayer( ) );
            } else {
                System.out.println("you've got to roll the dice before moving");
            }
        } else {
            // beep( );  // clicked on non-active area
        }
    } // mouseReleased( )

} // class MyMouseListener
