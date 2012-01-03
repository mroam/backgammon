/***************************************************************
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

/* File: tablaMouseListener.java
 *
 * Description: This file contains the mouse listener class and identifies
 * which spike the mouse click occured on. */

 

import java.awt.event.*;

public class tablaMouseListener extends MouseAdapter
{
    JBackgammon parent;

    public tablaMouseListener(JBackgammon p)
    {
        //Find where the main JBackgammon class is so we can use methods from it
        parent = p;
    } // tablaMouseListener( ) constructor


    public void mouseReleased(MouseEvent e)
    {
        //Adjust values as if the board was set in the top left corner at (0,0)
        int m_x = e.getX() - JBackgammon.x_offset;
        int m_y = e.getY() - JBackgammon.y_offset;
        
        //We only want to check clicks within the bounds of the playing board
        //   (0 <= x <= 190) OR (240 <= x <= 430) AND
        //   (0 <= y <= 160) OR (200 <= y <= 360)
        if( ( ((m_x>=0) && (m_x<=191)) || ((m_x>=238) && (m_x<=430)) ) &&
                ( ((m_y>=0) && (m_y<=160)) || ((m_y>=200) && (m_y<=360)) ) &&
                parent.myBoard.rolled && !parent.status.observer )
            parent.handlePoint(parent.getPointNum(m_x,m_y));
    } // mouseReleased( )

} // class tablaMouseListener
