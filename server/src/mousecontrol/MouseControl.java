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
package mousecontrol;

/**
 * This class is used to move the mouse cursor. It starts moving the cursor 
 * until the boolean variable cont becomes false
 * @author Nikos Fotiou
 */
public class MouseControl implements Runnable {

    private Thread t;
    private int x = 0;
    private int y = 0;
    private int step = 5;
    private java.awt.Robot robot;
    private mouse.MouseCursor mc;
    private remoteserver.RemoteServer rs;
    /**
     * Constant for the left click
     */
    public static final int LCLICK = 16;
    /**
     * Constant for the right click
     */
    public static final int RCLICK = 4;
    /**
     * Constant for the wheel click
     */
    public static final int WCLICK = 8;
    /**
     * While this variable is true the mouse cursor is beeing moved
     */
    private boolean cont = true;

    /**
     * It creates a new instance of the MouseMove
     */
    public MouseControl(remoteserver.RemoteServer rs) {
        try {
            this.rs = rs;
            robot = new java.awt.Robot();
            mc = new mouse.MouseCursor();
        } catch (Exception e) {
        }
    }

    /**
     * It performs a mouse click
     * @param button it represents to be clicked. Constants
     * LCLICK, RCLICK, WCLICK can be used 
     */
    public void mClick(int button) {
        robot.mousePress(button);
    }

    /**
     * It performs a mouse button release
     * @param button it represents to be clicked. Constants
     * LCLICK, RCLICK, WCLICK can be used 
     */
    public void mRelease(int button) {
        robot.mouseRelease(button);
        sendScreen();
    }

    /**
     * It performs a wheel movement
     * @param direction -1 for up, 1 for down 
     */
    public void mWheel(int direction) {
        robot.mouseWheel(direction);
        sendScreen();
    }

    /**
     * It controls the mouse movement.
     * In order to move the mouse the class mouse.MouseCursor is being used. This
     * class is bfound in the win32lib.jar and it uses the win32lib.dll, which is
     * a JNI dll that invokes native Windows API's that return the cursor's current
     * position.
     * @param x  is set to -1 to move cursor to the left, 1 to the right
     * @param y  is set to -1 to move cursor up, 1 down
     * 
     */
    public void moveMouse(int x, int y) {
        this.x = x;
        this.y = y;
        cont = true;
        step = 5;
        t = new Thread(this);
        t.start();
    }

    /**
     * It stops the mouse movement
     */
    public void stopMouse() {
        cont = false;
    }

    public void clikc(int button) {

    }

    /**
     * The run method of the Thread which is invoked by the MouseMove
     */
    public void run() {
        while (cont) {
            java.awt.Point point = mc.getCursorPos();
            robot.mouseMove(point.x + x * step++, point.y + y * step++);
            sendScreen();
            try{
                Thread.sleep(100);  
            }catch(Exception e){}
        }
        t = null;
    }

    /**
     * It send to the client a screenshot of the area around the mouse
     */
    public void sendScreen() {
        java.awt.Point point = mc.getCursorPos();
        int startx, starty;
        if (point.x - (int) (rs.screenx / 2) < 0) {
            startx = 0;
        } else if (point.x + (int) (rs.screenx / 2) > rs.sx) {
            startx = rs.sx - rs.screenx;
        } else {
            startx = point.x - (int) (rs.screenx / 2);
        }
        if (point.y - (int) (rs.screeny / 2) < 0) {
            starty = 0;
        } else if (point.y + (int) (rs.screeny / 2) > rs.sy) {
            starty = rs.sy - rs.screeny;
        } else {
            starty = point.y - (int) (rs.screeny / 2);
        }
        java.awt.Rectangle screenrect = new java.awt.Rectangle(startx, starty,
                rs.screenx, rs.screeny);
        
        
        java.awt.image.BufferedImage bscreen = robot.createScreenCapture(screenrect);
       
        java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
        try {
            javax.imageio.ImageIO.write(bscreen, "png", bout);
            byte[] tempimage = bout.toByteArray();
            if (rs.bluetooth.SendDataIfReady("SCRC " + tempimage.length))
                rs.bluetooth.SendData(tempimage);
        } catch (Exception e) {
            System.out.println("error" + e.toString());
        }
    }
}
