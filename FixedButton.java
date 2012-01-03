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

/* File: FixedButton.java
 *
 * Description: This file contains the custom button class for absolute
 * positioning of JButtons. */

 

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;

public class FixedButton  extends JButton
{
	Container content;
	JBackgammon parent;
	public static final long serialVersionUID = 1L; // version 1
	
	
	public FixedButton(Container c, JBackgammon p)
	{
		content = c;
		parent = p;
		content.setLayout(null);
		content.add(this);
	} // FixedButton constructor
	
	
	public void drawOnPoint(int point)
	{
		Insets in = parent.getInsets();
	
		if (point > 12) 
		{
			setBounds(parent.findX(point) - in.left, parent.findY(point) - in.top, 28, 10);
		} else {
			setBounds(parent.findX(point) - in.left, parent.findY(point) - 10 - in.top, 28, 10);
		}
		setVisible(true);
		parent.repaint();
	} // drawOnPoint

} // class FixedButton
