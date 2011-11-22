/*
Copyright (C) 2008 Nikolaos Fotiou
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package system;

import javax.bluetooth.*;
import javax.microedition.io.*;
import java.io.*;
import remoteserver.RemoteServer;

/**
 * This class handles all bluetooth activity
 * @author Nikos Fotiou
 */
public class Bluetooth implements Runnable 
{
    final UUID uuid = new UUID(                         //the uid of the service, it has to be unique,
            "27012f0c68af4fbf8dbe6bbaf7aa432a", false); //it can be generated randomly
    final String name = "Echo Server";                  //the name of the service
    final String url = "btspp://localhost:" + uuid      //the service url
            + ";name=" + name + ";authenticate=false;encrypt=false;";
    DataOutputStream dout;
    DataInputStream din;
    LocalDevice local = null;
    StreamConnectionNotifier server = null;
    StreamConnection conn = null;
    RemoteServer rs;
    boolean remoteReady = true; //it holds whether client is ready to receive data

    public Bluetooth(RemoteServer rs) 
    {
        try 
        {
            this.rs = rs;
            local = LocalDevice.getLocalDevice();
            local.setDiscoverable(DiscoveryAgent.GIAC);
            server = (StreamConnectionNotifier) Connector.open(url);
        }
        catch (Exception e) 
        {
            rs.displayMessage("Exception Occured, \nMake sure bluetooth adapter is connected," +
                              " \nPlease restart\nException decription:\n" + e.toString());
        }
    }

    /**
     *It listens for incoming connections 
     */
    public void listen() 
    {
        try 
        {
            Thread t = new Thread(this);
            t.start();
        }
        catch (Exception e) 
        {
            System.out.println("Exception " + e.toString());
        }
    }

    /**
     *It closes the bluetooth connection 
     */
    public void close() 
    {
        try 
        {
            conn.close();
            server.close();
        } 
        catch (Exception e) 
        {
            System.out.println("Exception " + e.toString());
        }
    }

    /**
     * It listens for incoming connections and commands 
     */
    public void run() 
    {
        boolean displayOnce = true;
        boolean connectionBroken = false;
        
        try 
        {
            boolean listening = true;
            boolean firstTime = true;
            String cmd = "";
            while (true) 
            {
                try 
                {
                    /* first establish connection [07.01.10, SBe] */
                    if (listening) 
                    {
                        if(connectionBroken)
                            server = (StreamConnectionNotifier) Connector.open(url);
                        
                        conn = server.acceptAndOpen();
                        
                        din = new DataInputStream(conn.openInputStream());
                        dout = new DataOutputStream(conn.openOutputStream());
                        rs.displayMessage("Client Connected...");
                        
                       if(connectionBroken)
                          // part 2 of the proximity sensor [07.01.10, SBe]
                            rs.handleAppResume();
                        
                        listening = false;
                        firstTime = false;
                        
                        connectionBroken = false;
                        displayOnce      = true;
                    } 
                    /* get the command and process it [07.01.10, SBe] */
                    else 
                    {
                        cmd = din.readUTF();
                        System.out.println("Received: " + cmd);
                        
                        if (cmd.equals("ACK"))
                            remoteReady = true;
                        
                        rs.cmdReceived(cmd);
                    }
                } 
                /* handle disconnected client [07.01.10, SBe] */
                catch (Exception e) 
                {   
                    System.out.println("Exception");
                    if(displayOnce)
                    {
                        System.out.println("Exception " + e.toString());
                        rs.displayMessage("Client Disconnected");
                        rs.displayMessage("Waiting for new connection");
              
                        // part 1 of the proximity sensor [07.01.10, SBe]
                        rs.handleAppShutdown();
                        close();
                        
                        if (firstTime) 
                        {
                            rs.displayMessage("Exception Occured, \nMake sure bluetooth adapter is connected, \nPlease restart\nException decription:\n" + e.toString());
                            return;
                        }
                        
                        connectionBroken = true;
                        displayOnce = false;
                    }
                    
                    listening = true;
                    Thread.sleep(10);
                }
            }
        } 
        catch (Exception e) 
        {
            rs.displayMessage("Exception Occured, \nMake sure bluetooth adapter is connected, \nPlease restart\nException decription:\n" + e.toString());
        }
    }

    /**
     * It sends a String of data
     * @param data the data to send
     */
    public void SendData(String data) 
    {
        try 
        {
            dout.writeUTF(data);
            dout.flush();
        } 
        catch (Exception e) 
        {
            rs.displayMessage("Exception while sending data " + e.toString());
        }
    }
    
    /**
     * It sends a String of data only if remote client is ready
     * @param data the data to send
     * @return true if data was send successfylly else false
     */
    public boolean SendDataIfReady(String data) 
    {
        boolean result = false;
        try 
        {
            if(remoteReady)
            {
                remoteReady = false;
                dout.writeUTF(data);
                dout.flush();
                result = true;
            }
        } 
        catch (Exception e) 
        {
            rs.displayMessage("Exception while sending data " + e.toString());
        }
         return result;
    }

    /**
     * It sends a byte[] of data
     * @param data the data to send
     */
    public void SendData(byte[] data) 
    {
        try 
        {
            dout.write(data, 0, data.length);
            dout.flush();
        } 
        catch (Exception e) 
        {
            rs.displayMessage("Exception while sending data " + e.toString());
        }
    }

    /**
     * It sends a byte[] of data only if remote client is ready
     * @param data the data to send
     * @return true if data was send successfylly else false
     */
    public boolean SendDataIfReady(byte[] data) 
    {
        boolean result = false;
        try 
        {
            if (remoteReady) 
            {                
                remoteReady = false;
                dout.write(data, 0, data.length);
                dout.flush();
                result = true;
            }
            
        } 
        catch (Exception e) 
        {
            rs.displayMessage("Exception while sending data " + e.toString());
        }
        return result;
    }
}
