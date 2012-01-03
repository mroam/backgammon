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

/**
 * File: Communication.java
 *
 * Description: This file contains the communication adapter for the
 * backgammon-specific events. 
 */

 

public interface CommunicationAdapter
{
    /** 
     * Gets called when dice rolls are received 
     */
    public void receiveRolls(int i, int j);


    /** 
     * Gets called when a move is received 
     */
    public void receiveMove(int oldpos, int newpos);


    /** 
     * Gets called when the other player clicks "New Game" 
     */
    public void receiveResetReq();


    /** 
     * Gets called when the other player responds to "New Game" 
     */
    public void receiveResetResp(int resp);


    /** 
     * Gets called when a text message is received 
     */
    public void receiveMessage(String message);


    /** 
     * Gets called when a connection is established 
     */
    public void connected();


    /** 
     * Gets called when the other player's turn is over 
     */
    public void turnFinished();


    /** 
     * Sends a man to the bar 
     */
    public void receiveBar(int spike);


    /** 
     * Gets called when a socket error occurs 
     */
    public void disconnected();


    /** 
     * Gets called when the player loses the game 
     */
    public void receiveLose();


    /** 
     * Gets called when a connection fails to be established 
     */
    public void connectionRefused();

} // interface CommunicationAdapter
