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
package mouse;

/**
 * This class is used in order to invoke the win32lib.dll .It
 * @author Nikos Fotiou
 */
public class MouseCursor {
    
    /** 
     * Creates a new instance of the MouseCursor 
     */
    public MouseCursor() {
    }
    static {
       String sysName = System.getProperty("os.name").toLowerCase();
       if (sysName.indexOf("windows") != -1) {
            System.loadLibrary("win32lib");
       }else{//Linux
            System.load(System.getProperty("user.dir")+"/lin32lib.so");
       }
    }
    
   public native double[] GetCursorPos();
   
   /**
    * It is used to retreive the current cursor position
    * @return java.awt.Point that holds the cursor's position
    */
   public java.awt.Point getCursorPos(){
        double[] dim = new double[2];
        dim = GetCursorPos();
        java.awt.Point ret = new java.awt.Point();
        ret.x = (int)dim[0];
        ret.y = (int)dim[1];
        return ret;
    } 
    
}
