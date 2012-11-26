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
 * File: FixedButton.java
 *
 * A custom button class for absolute positioning of JButtons. 
 */

import javax.swing.*;
import java.awt.*;
//import java.awt.event.*;
//import java.awt.geom.*;
//import java.awt.image.*;

public class FixedButton extends JButton {

    protected int btnWidth = BoardPict.POINT_WIDTH; // 28
    protected int btnHeight = GUI_Dim.TEXT_LINE_HEIGHT; // 10

    Container myContainer;
    Game myGame;
    public static final long serialVersionUID = 1L; // version 1

    public FixedButton(Container aContentPane, Game newMyGame) {
        myContainer = aContentPane;
        myGame = newMyGame;
        myContainer.setLayout(null); // <- necessary?
        myContainer.add(this);
    } // FixedButton constructor

    /**
     * Is used to display the "move-here" buttons on the GUI, at the bottom of
     * the points that are legal move destinations for the selectedPoint.
     * Uses Game.findX(pointNum) and Game.findY(pointNum) which report the
     * coords of the specified point.
     */
    public void drawOnPoint(int point) {
        Insets in = myGame.getInsets();
        int btnLeft = myGame.findX(point) - in.left;
        int btnTop = myGame.findY(point) - in.top;
        // if point is 1..12 then stand above the line
        // First 2 quadrants (from white's point of view, as everything is)
        // have numbers sitting above, while last two have numbers hanging below
        if (point <= (Board.HOW_MANY_QUADRANTS/2) * Board.HOW_MANY_POINTS_IN_QUADRANT) {
            btnTop = myGame.findY(point) - /*10*/btnHeight - in.top;
        }
        setBounds(btnLeft,btnTop, btnWidth,btnHeight);
        setVisible(true);
        myGame.repaint();
    } // drawOnPoint

} // class FixedButton
