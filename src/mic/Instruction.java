/* $Id: Instruction.java,v 1.2 2005/02/22 05:28:24 RMS Exp $
 *
 *  Instruction.java
 *
 *  Mic1MMV microarchitecture compiler/simulator 
 *  Copyright (C) 2005, Prentice-Hall, Inc. 
 * 
 *  This program is free software; you can redistribute it and/or modify 
 *  it under the terms of the GNU General Public License as published by 
 *  the Free Software Foundation; either version 2 of the License, or 
 *  (at your option) any later version. 
 * 
 *  This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General 
 *  Public License for more details. 
 * 
 *  You should have received a copy of the GNU General Public License 
 *  along with this program; if not, write to: 
 * 
 *    Free Software Foundation, Inc. 
 *    59 Temple Place - Suite 330 
 *    Boston, MA 02111-1307, USA. 
 * 
 *  A copy of the GPL is available online the GNU web site: 
 * 
 *    http://www.gnu.org/copyleft/gpl.html
 * 
 */ 

package mic;

/**
 * Internal instruction format
 * 
*/
public class Instruction {

  public static final int NOPARAM = 0;
  public static final int BYTE = 1;
  public static final int CONST = 2;
  public static final int VARNUM = 3;
  public static final int LABEL = 4;
  public static final int OFFSET = 5;
  public static final int INDEX = 6;
  public static final int VARNUM_CONST = 7;
  public static final int WIDE = 8;   // wide fix
  private int opcode;
  private String mnemonic = null;
  private int type;

  public Instruction() {}

  public Instruction(int opcode, String mnemonic, int type) {
    this.opcode = opcode;
    this.mnemonic = mnemonic;
    this.type = type;
  }

  public void setOpcode(int opcode) {
    this.opcode = opcode;
  }

  public void setMnemonic(String mnemonic) {
    this.mnemonic = mnemonic;
  }

  public void setType(int type) {
    this.type = type;
  }

  public int getOpcode() {
    return opcode;
  }

  public String getMnemonic() {
    return mnemonic;
  }

  public int getType() {
    return type;
  }
}
