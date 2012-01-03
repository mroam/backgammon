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

/* File: Communication.java
 *
 * Description: This file contains the communication class and functions
 * for sending JBackgammon events. 
 */



import java.io.*;
import java.net.*;

public class Communication
{
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
	public Communication(CommunicationAdapter p)
	{
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
	public void listen()
	{
		PortListener watch = new PortListener(this, PORT);
		watch.start();
		portBound = watch.getValidBindState();
	} // listen( )


	/**
	* //Executes when a connection with another computer is opened
	*/
    public void connectionEstablished(Socket s)
    {
        if (s != null)
        {
            if (! connected)
            {
                sock = s;
                connected=true;
                try
                {
                    out = new PrintWriter(sock.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                }
                catch (IOException e)
                {
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
    public void connect(String address)
    {
        ConnectThread temp = new ConnectThread(this, address, PORT);
        temp.start();
    } // connect( )


	/**
	*  Send information about a roll of dice
	*/
    public void sendroll(int first, int second)
    {
        String packet = "$R:" + first + ":" + second + ":";
        out.println(packet);
    } // sendroll( )


    /**
    * Send a player's move
    */
    public void sendmove(int oldpos, int newpos)
    {
        String packet = "$M:" + oldpos + ":" + newpos + ":";
        out.println(packet);
    } // sendmove( )


    /**
    * Send a text message to the other player
    */
    public void sendmessage(String text)
    {
        String packet = "$T:" + text;
        out.println(packet);
    } // sendmessage


    /**
    * Ends the player's turn
    */
    public void sendendturn()
    {
        String packet = "$E:";
        out.println(packet);
    } // sendendturn( )


    /**
    * Sends a man on the bar
    */
    public void sendonbar(int spike)
    {
        String packet = "$B:" + spike + ":";
        out.println(packet);
    } // sendonbar( )


    public void sendlose()
    {//Tells the other player they lost
        String packet = "$L:";
        out.println(packet);
    } // sendlose( )


    public void sendResetReq()
    { //Sends a request for a new game
        String packet = "$N:";
        out.println(packet);
    } // sendResetReq( )


	/**
	* Sends the response for a reset request
	*/
    public void sendResetResp( int reset )
    {
        String packet = "$Y:" + reset + ":";
        out.println(packet);
    } // sendResetResp( )


	/**
	* was "Connected( )
	* Returns the state of the connection
	*/
    public boolean isConnected()
    {
        return connected;
    } // isConnected( ) 


	/**
	*  This parses the packets received
	*/
    public void onGetPacket(String packet)
    {
        String temp = packet.substring(1,2);//Header byte
        if (temp.equals("R"))
        {
            int firstroll = Integer.parseInt(packet.substring(3, packet.indexOf(":", 3)));
            int secondroll = Integer.parseInt(packet.substring(packet.indexOf(":", 3) + 1,
                              packet.indexOf(":", packet.indexOf(":", 3) + 1)));
            parent.receiverolls(firstroll, secondroll);
        }
        else if (temp.equals("M")) {
            int oldpos = Integer.parseInt(packet.substring(3, packet.indexOf(":", 3)));
            int newpos = Integer.parseInt(packet.substring(packet.indexOf(":", 3) + 1, packet.indexOf(":",
                              packet.indexOf(":", 3) + 1)));
            parent.receivemove(oldpos, newpos);
        } else if (temp.equals("T")) {
            temp = packet.substring(3);
            parent.receivemessage(temp);
        } else if (temp.equals("E")) {
            parent.turnfinished();
        } else if (temp.equals("B")) {
            int spike = Integer.parseInt(packet.substring(3, packet.indexOf(":", 3)));
            parent.receivebar(spike);
        } else if (temp.equals("L")) {
            parent.receivelose();
        } else if (temp.equals("N")) {
            parent.receiveResetReq();
        } else if (temp.equals("Y")) {
            int response = Integer.parseInt(packet.substring(3, packet.indexOf(":",3)));
            parent.receiveResetResp(response);
        } else {
            //Illegal Packet
        }
    } // onGetPacket( )


    /**
    * Gets called when there's an error writing/reading on the socket
    */
    public void socketerror()
    {
        connected = false;
        parent.disconnected();
    } // socketerror( )


    /**
    * Gets called when there's an error connecting
    */
    public void connrefused()
    {
        parent.connectionrefused();
    } // connrefused( )
    
} // class Communication


/** 
* This thread listens on a port for connections
*/
class PortListener extends Thread
{
    private int portBound;
    Socket sock;
    int port;
    Communication parent;
    
    
    public PortListener(Communication p, int prt)
    {
        port = prt;
        parent = p;
        portBound = 0; // Untried value
    } // PortListener( ) constructor


    /**
    * 
    */
    public synchronized int getValidBindState()
    {
        while(portBound == 0)
        {
            try {
            	this.wait();
            } catch (InterruptedException e) {
            
            }
        }
        return portBound;
    } // getValidBindState( )


    public synchronized void setBindState( int newState )
    {
        portBound = newState;
        this.notify();
    } // setBindState( )


    public void run()
    {
        ServerSocket serv = null;
        try
        {
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
        try
        {
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

    public SocketListener(Communication p, BufferedReader i)
    {
        parent = p;
        in = i;
    } // SocketListener( ) constructor


    public void run()
    {
        String input = null;
        while (true)
        {
            try
            {
                input = in.readLine();
            } catch (IOException e) {
                parent.socketerror();
                return;
            }

            if (input == null)
            {
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
class ConnectThread extends Thread
{
    Communication parent;
    String address;
    int port;

    public ConnectThread(Communication p, String a, int prt)
    {
        parent = p;
        address = a;
        port = prt;
    } // ConnectThread( ) constructor


    public void run()
    {
        Socket s=null;
        try
        {
            s = new Socket(address, port);
        }
        catch (IOException e)
        {
            parent.connrefused();
            return;
        }
        parent.connectionEstablished(s);
    } // run( )

} // class ConnectThread