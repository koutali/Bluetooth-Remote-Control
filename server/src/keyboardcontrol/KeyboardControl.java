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

package keyboardcontrol;
/**
 * This class is used to control the keyboard
 * @author Nikos Fotiou
 */
public class KeyboardControl {
	private java.awt.Robot robot;
	
	/**
	 * It creates a new instance of KeyboardControl
	 */
	public KeyboardControl(){
		try{
            robot = new java.awt.Robot();
        }catch(Exception e){}
	}
	
	/**
	 * It press a key on the keyboard
	 * @param keyCode the code of the key to be pressed
	 */
	public void keyPress(int keyCode){
		robot.keyPress(keyCode);
	}
	
	/**
	 * It releases a key on the keyboard
	 * @param keyCode the code of the key to be released
	 */
	public void keyRelease(int keyCode){
		robot.keyRelease(keyCode);
	}
}
