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
 * Description: This file contains the communication class and functions
 * for sending JBackgammon events. 
 */


import java.io.*;
import java.net.*;

public class Communication {

    Board myBoard = null; // visual display of backgammon board

    Socket sock; //The open socket
    boolean connected; //Whether a connection has been established or not
    int portBound; //Binding state of port: -1=error, 0=untried, 1=bound
    PrintWriter out; //The out stream on the socket
    BufferedReader in; //The in stream of the socket
    SocketListener listen; //Port listening thread
    CommunicationAdapter parent; //The parent class
    public final int PORT = 1776; //port to do communication on

    /**
     * 
     */
    public Communication(Board myNewBoard, CommunicationAdapter p) {
        if (myNewBoard == null) {
            throw new IllegalArgumentException("Communication object doesn't like null board GUI");
        }
        if (p == null) {
            throw new IllegalArgumentException("Communication object doesn't like null CommunicationAdapter");
        }
        myBoard = myNewBoard;
        sock = null;
        connected = false;
        portBound = 0;
        out = null;
        in = null;
        listen = null;
        parent = p;
    } // Communication constructore

    /**
     * //Start listening on the right port
     */
    public void listen() {
        PortListener watch = new PortListener(this, PORT);
        watch.start();
        portBound = watch.getValidBindState();
    } // listen( )

    /**
     * //Executes when a connection with another computer is opened
     */
    public void connectionEstablished(Socket s) {
        if (s != null) {
            if (! connected) {
                sock = s;
                connected=true;
                try {
                    out = new PrintWriter(sock.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                } catch (IOException e) {
                    socketerror();
                }
                listen = new SocketListener(this, in);
                listen.start();
                parent.connected();
            } // if ! connected
        } // socket != null
    } // connectionEstablished( )

    /**
     * //This connects to another computer
     */
    public void connect(String address) {
        ConnectThread temp = new ConnectThread(this, address, PORT);
        temp.start();
    } // connect( )

    /**
     *  Send information about a roll of dice
     *  "$R:int:int:"
     */
    public void sendRoll(int first, int second) {
        String packet = "$R:" + first + ":" + second + ":";
        out.println(packet);
    } // sendRoll( )

    /**
     * Send a player's move
     * "$M:int:int:"
     */
    public void sendMove(int oldpos, int newpos) {
        String packet = "$M:" + oldpos + ":" + newpos + ":";
        out.println(packet);
    } // sendMove( )

    /**
     * Send a text message to the other player
     * "$T:text"
     */
    public void sendMessage(String text) {
        String packet = "$T:" + text;
        out.println(packet);
    } // sendMessage

    /**
     * Ends the player's turn
     * "$E:"
     */
    public void sendEndTurn() {
        String packet = "$E:";
        out.println(packet);
    } // sendEndTurn( )

    /**
     * Sends a man on the bar
     * "$B:int:"
     */
    public void sendOnBar(int pointNum /*was "spike"*/) {
        String packet = "$B:" + pointNum + ":";
        out.println(packet);
    } // sendOnBar( )

    /**
     * Tells the other player they lost
     * "$L:"
     */
    public void sendLose() {
        String packet = "$L:";
        out.println(packet);
    } // sendLose( )

    /**
     * Sends a request for a new game
     * "$N:"
     */
    public void sendResetReq() {
        String packet = "$N:";
        out.println(packet);
    } // sendResetReq( )

    /**
     * Sends the response for a reset request
     * "$Y:int:"
     */
    public void sendResetResp( int reset ) {
        String packet = "$Y:" + reset + ":";
        out.println(packet);
    } // sendResetResp( )

    /**
     * was "Connected( )
     * Returns the state of the connection
     */
    public boolean isConnected( ) {
        return connected;
    } // isConnected( ) 

    /**
     *  Parses the packets received.  Possible packets are
     * "$R:int:int:"    // send diceRoll
     * "$M:int:int:"   // Send a player's move
     * "$T:text"   // text message to the other player
     * "$E:"   // Ends the player's turn
     * "$B:int:"   // Sends a man on the bar
     * "$L:"    // Tells the other player they lost
     * "$N:"     //request for a new game
     * "$Y:int:"    //response for a reset request
     */
    public void onGetPacket(String packet) {
    	String[] parts = packet.split(":",3); // put into array {$R   int    int }
    	//String temp = packet.substring(1,2);//Header byte without $
    	String header = parts[0].substring(1,2); // removes first char "$"
    	if (header.equals("R")) {  // $R:int:int:
    		int firstroll = Integer.parseInt( parts[1] );
    		// was (packet.substring(3, packet.indexOf(":", 3)));
    		int secondroll = Integer.parseInt(parts[2]);
    		// was (packet.substring(packet.indexOf(":", 3) + 1,
    		//    packet.indexOf(":", packet.indexOf(":", 3) + 1)));
    		parent.receiveRolls(myBoard, firstroll, secondroll);
        } else if (header.equals("M")) {    // $M:int:int:
        	int oldPos = Integer.parseInt(parts[1]);
        	// packet.substring(3, packet.split(":",3));
            int newPos = Integer.parseInt(parts[2]);
                        //= Integer.parseInt(packet.substring(packet.indexOf(":", 3) + 1, packet.indexOf(":",
                        //    packet.indexOf(":", 3) + 1)));
            throw new IllegalArgumentException("I don't know how to parse an M packet");
            //              int rollValue = Math.abs(newPos - oldPos); // what about bar/bear??
            // parent.receiveMove(myBoard, Board.white, oldPos, newPos, rollValue); 
            /* ?? do these params need 25-x or happened already? What about BAR_ & BEAR?? */
        } else if (header.equals("T")) {
            String temp = parts[1]; // = packet.substring(3);
            parent.receiveMessage(temp);
        } else if (header.equals("E")) {
            parent.turnFinished(myBoard);
        } else if (header.equals("B")) {
            int point = Integer.parseInt( parts[2] ); // packet.substring(3, packet.indexOf(":", 3)));
            parent.receiveBar(myBoard, point);
        } else if (header.equals("L")) {
            parent.receiveLose();
        } else if (header.equals("N")) {
            parent.receiveResetReq();
        } else if (header.equals("Y")) {
            int response = Integer.parseInt( parts[2] ); // packet.substring(3, packet.indexOf(":",3)));
            parent.receiveResetResp(response);
        } else {
            //Illegal Packet
        	throw new IllegalArgumentException("Illegal packet received '" + packet + "'");
        }
    } // onGetPacket( )

    /**
     * Gets called when there's an error writing/reading on the socket
     */
    public void socketerror() {
        connected = false;
        parent.disconnected();
    } // socketerror( )

    /**
     * Gets called when there's an error connecting
     */
    public void connrefused() {
        parent.connectionRefused();
    } // connrefused( )

} // class Communication

/** 
 * This thread listens on a port for connections
 */
class PortListener extends Thread {
    private int portBound;
    Socket sock;
    int port;
    Communication parent;

    public PortListener(Communication p, int prt) {
        port = prt;
        parent = p;
        portBound = 0; // Untried value
    } // PortListener( ) constructor

    /**
     * 
     */
    public synchronized int getValidBindState() {
        while(portBound == 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {

            }
        }
        return portBound;
    } // getValidBindState( )

    public synchronized void setBindState( int newState ) {
        portBound = newState;
        this.notify();
    } // setBindState( )

    public void run() {
        ServerSocket serv = null;
        try {
            serv = new ServerSocket(port);
        } catch (UnknownHostException e) {
            setBindState(-2);
            return; //System.out.println("Unknown Host");
        } catch (BindException e) {
            setBindState(-1);
            //System.out.println(e.getMessage());
            return;
        } catch (IOException e) {
            setBindState(-3);
            return; //System.out.println("I/O error");
        }
        setBindState(1);
        Socket sock = null;
        try {
            sock = serv.accept();
            serv.close();
        } catch (IOException e) {
            return;
        }
        parent.connectionEstablished(sock);
    } // run( )

} // class PortListener

/**
 * Once a connection is established, this thread listens for packets
 */
class SocketListener extends Thread
{
    BufferedReader in;
    Communication parent;

    public SocketListener(Communication p, BufferedReader i) {
        parent = p;
        in = i;
    } // SocketListener( ) constructor

    public void run() {
        String input = null;
        while (true) {
            try {
                input = in.readLine();
            } catch (IOException e) {
                parent.socketerror();
                return;
            }

            if (input == null) {
                parent.socketerror();
                return;
            }

            parent.onGetPacket(input);
            input = null;
        }
    } // run( )

} // class SocketListener

/**
 * This thread connects to a specified computer
 */
class ConnectThread extends Thread {
    Communication parent;
    String address;
    int port;

    public ConnectThread(Communication p, String a, int prt) {
        parent = p;
        address = a;
        port = prt;
    } // ConnectThread( ) constructor

    public void run() {
        Socket s=null;
        try {
            s = new Socket(address, port);
        } catch (IOException e) {
            parent.connrefused();
            return;
        }
        parent.connectionEstablished(s);
    } // run( )

} // class ConnectThread