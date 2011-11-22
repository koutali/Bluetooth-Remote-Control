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

package remoteserver;

import gui.RemoteServerGUI;
import mousecontrol.MouseControl;
import keyboardcontrol.KeyboardControl;
import system.Bluetooth;

/**
 * The server of the remote control
 * it receives the commands from the mobile phone
 * @author Nikos Fotiou
 */
public class RemoteServer 
{
    public int screenx  = -1;   //the width of the mobile phone screen
    public int screeny  = -1;   //the height of the mobile phone screen
    public int sx       = -1;   //the server screen width
    public int sy       = -1;   //the server screen height
    
    MouseControl mouse;
    KeyboardControl keyboard;
    RemoteServerGUI gui;
    public Bluetooth bluetooth;

    /* variables for proximity server [07.01.10, SBe] */
    boolean appMode           = false;
    boolean isWinampRunning   = false;
    boolean isWMPlayerRunning = false;
    boolean isCtrlPressed     = false;
    
    boolean wasWinampRunning   = false;
    boolean wasWMPlayerRunning = false;
    
    /* key codes for application handling [07.01.10, SBe] */
    final int CTRL_KEY_NUM = 17;
    final int S_KEY_NUM    = 83;
    final int P_KEY_NUM    = 80;
    final int V_KEY_NUM    = 86;
    final int X_KEY_NUM    = 88;
    final int TWO_KEY_NUM  = 32;

    /**
     * It creates a new GUI for the remote control server,
     * it initiates the Bluetooth stuck and it waits until
     * it receives a command. Upon the command is received
     * its being parsed and the appropriate action is being 
     * taken.
     */
    public RemoteServer() 
    {
        try 
        {
            gui = new RemoteServerGUI();
            mouse = new MouseControl(this);
            keyboard = new KeyboardControl();
            java.awt.Dimension sdimension = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            gui.setSize(250, 200);
            sx = sdimension.width;
            sy = sdimension.height;
            gui.setLocation(sdimension.width - 250, sdimension.height - 250);
            gui.setVisible(true);
            gui.jScrollPane1.setLocation(1, 1);
            gui.jScrollPane1.setSize(245, 150);
            gui.InfoLabel.setText("Setting device to be discoverable...");

            /*--------- Initialising the Bluetooth Stuck -----------*/
            bluetooth = new Bluetooth(this);            
            gui.InfoLabel.setText("Start advertising service...");

            /*--------- Listening for incoming connections ----------*/
            gui.InfoLabel.setText("Waiting for incoming connection...");
            bluetooth.listen();
        } 
        catch (Exception e) 
        {
            gui.InfoLabel.setText("Exception Occured, \nMake sure bluetooth adapter is connected, \nPlease restart\nException decription:\n" + e.toString());
        }
    }
    
    /* this function shuts down an application that is controlled by our application
     * [07.01.10, SBe]
     */
    public void handleAppShutdown()
    {
        if(appMode)
        {
            if(isWinampRunning)
            {
                keyboard.keyPress(V_KEY_NUM);
                keyboard.keyRelease(V_KEY_NUM);
                
                isWinampRunning  = false;
                wasWinampRunning = true;
            }
            
            if(isWMPlayerRunning)
            {
                keyboard.keyPress(CTRL_KEY_NUM);                
                keyboard.keyPress(S_KEY_NUM);               
                
                keyboard.keyRelease(CTRL_KEY_NUM);
                keyboard.keyRelease(S_KEY_NUM);
                
                isWMPlayerRunning  = false;
                wasWMPlayerRunning = true;
                
               System.out.println("handleAppShutdown isWMPlayerRunning");
            }
        }
    }
    
    /* this function resumes an application that is being controlled by our software
     * [07.01.10, SBe]
     */
    public void handleAppResume()
    {
        System.out.println("handleAppResume");
        
        if(appMode)
        {
            System.out.println("handleAppResume appmode");
            
            if(wasWinampRunning)
            {
                keyboard.keyPress(X_KEY_NUM);
                keyboard.keyRelease(X_KEY_NUM);
                
                isWinampRunning  = true;
                wasWinampRunning = false;
            }
            
            if(wasWMPlayerRunning)
            {
                keyboard.keyPress(CTRL_KEY_NUM);                
                keyboard.keyPress(P_KEY_NUM);
                
                keyboard.keyRelease(CTRL_KEY_NUM);
                keyboard.keyRelease(P_KEY_NUM);
                
                isWMPlayerRunning  = true;
                wasWMPlayerRunning = false;
                
                System.out.println("handleAppResume isWMPlayerRunning");
            }
        }
    }
    
    /**
     * It is used by system.Bluetooth whenever a
     * command arrives
     * @param cmd the command arrived
     */
    public void cmdReceived(String cmd)
    {   
        /*------ Parse the command -------*/
        
        /* parse to determine which mode the application is running and
         * to determine which application is being controlled if the application is 
         * running in application control mode 
         * [07.01.10, SBe]
         */
        if (cmd.startsWith("K")) 
        { 
            //Key pressed
            int scode = Integer.parseInt(cmd.substring(1));

            /* after each press, reset isCtrlPressed variable [07.01.10, SBe] */
            if(isCtrlPressed)
            {
                /* there are two types of command: the ones detected by one command
                 * (winamp) anf the ones detected by two commands that follow each other
                 * (wm player and the ctrl + 2 command used to detect application mode)
                 * 
                 * the start and stop of an application (winamp or wm player) is only controlled
                 * by key strokes since it is possible that the user controlled the application
                 * with our software, switched to some other task and switched back to the 
                 * application he was controlling. if we handle the boolean variables that are
                 * responsible for the handling of the application in some other way, we may not 
                 * be able to cover that step.
                 *
                 * appMode parameter is reset for all cases when we handle something else than 
                 * key strokes
                 * [07.01.10, SBe] 
                 */
                switch(scode)
                {
                    case TWO_KEY_NUM:
                        appMode       = true;
                        isCtrlPressed = false;
                        break;
                            
                    case S_KEY_NUM:
                        isWMPlayerRunning = false;
                        isCtrlPressed     = false;
                        break;
                        
                    case P_KEY_NUM:
                        isWMPlayerRunning = true;
                        isCtrlPressed     = false;
                        break;
                        
                    default:
                        isCtrlPressed = false;
                        break;
                }
            }
            else
            {
                switch(scode)
                {                   
                    case CTRL_KEY_NUM:
                        isCtrlPressed = true;
                        break;
                        
                    case V_KEY_NUM:
                        isCtrlPressed   = false;
                        isWinampRunning = false;
                        break;
                        
                    case X_KEY_NUM:
                        isCtrlPressed   = false;
                        isWinampRunning = true;
                        break;

                    default:
                        isCtrlPressed   = false;
                        break;
                }
            }
            
            keyboard.keyPress(scode);
        }
        else if (cmd.startsWith("SK")) 
        {
            //Key released
            String scode = cmd.substring(2);
            keyboard.keyRelease(Integer.parseInt(scode));
        }
        else
        {
            appMode = false;
            if (cmd.startsWith("run")) 
            {
                //Invoke a run command
                appMode = false;

                String command = cmd.substring(4);
                try 
                {
                    String sysName = System.getProperty("os.name");
                    if (sysName.indexOf("windows") != -1) 
                    {
                        Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "start", command});
                    }
                    else     
                        Runtime.getRuntime().exec(command); //Linux           
                } 
                catch (Exception e) 
                {}
            }
        
            if (cmd.startsWith("msg")) 
            {
                //Send message has been send
                appMode = false;

                String message = cmd.substring(4);
                gui.showMsgbox(message);
            }

            if(cmd.startsWith("DIM")) 
            {
                //The screen simensions
                appMode = false;

                int ypos = cmd.indexOf("Y");
                screenx = Integer.parseInt(cmd.substring(3, ypos));
                screeny = Integer.parseInt(cmd.substring(ypos+1));
            }

            if (cmd.equalsIgnoreCase("applist")) 
            {	     
               //Send the application list
               try
               {
                   util.SendAppList.startSending(this);
               }
               catch(Exception e)
               {
                   gui.InfoLabel.setText("Exception Occured " + e.toString());
               }
            }
            
            if (cmd.equalsIgnoreCase("CLOSE")) 
            {
                bluetooth.close();                      //close connection
            }

            if (cmd.equalsIgnoreCase("MUP"))
                mouse.moveMouse(0, -1);                 //Move mouse up
            if (cmd.equalsIgnoreCase("MDOWN"))        
                mouse.moveMouse(0, 1);		        //Move mouse down
            if (cmd.equalsIgnoreCase("MLEFT"))        
                mouse.moveMouse(-1, 0);		        //Move mouse left
            if (cmd.equalsIgnoreCase("MRIGHT")) 
                mouse.moveMouse(1, 0);			//Move mouse right
            if (cmd.equalsIgnoreCase("SM")) 
                mouse.stopMouse();			//Stop moving mouse 
            if (cmd.equalsIgnoreCase("MLCLICK")) 
                mouse.mClick(MouseControl.LCLICK);      //Mouse left click pressed
            if (cmd.equalsIgnoreCase("SMLCLICK")) 
                mouse.mRelease(MouseControl.LCLICK);    //Mouse left click released		
            if (cmd.equalsIgnoreCase("MRCLICK")) 
                mouse.mClick(MouseControl.RCLICK);      //Mouse right click pressed
            if (cmd.equalsIgnoreCase("SMRCLICK")) 
                mouse.mRelease(MouseControl.RCLICK);    //Mouse right click released
            if (cmd.equalsIgnoreCase("MWPRESS")) 
                mouse.mClick(MouseControl.WCLICK);      //Mouse wheel pressed
            if (cmd.equalsIgnoreCase("SMWPRESS")) 
                mouse.mRelease(MouseControl.WCLICK);    //Mouse wheel released
            if (cmd.equalsIgnoreCase("MWUP")) 
                mouse.mWheel(-1);			//Move wheel up
            if (cmd.equalsIgnoreCase("MWDOWN"))
                mouse.mWheel(1);                        //Move wheel down
        }
    }
    
    /**
     * It displays a message in the TextArea
     * @param msg the message to be displayed
     */
    public void displayMessage(String msg) 
    {
        gui.InfoLabel.setText(msg);
    }

    /**
     * The entry point of the program
     */
    public static void main(String args[]) 
    {
        RemoteServer echoserver = new RemoteServer();
    }
}
