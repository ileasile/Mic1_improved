package mic.ontko;

/*
*
*  IJVMInstruction.java
*
*  mic1 microarchitecture simulator 
*  Copyright (C) 1999, Prentice-Hall, Inc. 
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
*  You should have received a copy of the GNU General Public License along with 
*  this program; if not, write to: 
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

/**
* Class that stores the instruction, address, and parameters for a
* given line in a .jas file.
*
* @author 
*   Dan Stone (<a href="mailto:dans@ontko.com"><i>dans@ontko.com</i></a>),
*   Ray Ontko & Co,
*   Richmond, Indiana, US
*/
public class IJVMInstruction {

  private Instruction instruction = null;
  private int address;
  private int lineno;
  private String label = null;
  private int param;
  private int param2;
  private boolean wide = false;  // rms wide-fix

  public IJVMInstruction() {}
  public IJVMInstruction(Instruction instruction) {
    this.instruction = instruction;
  }
  public IJVMInstruction(Instruction instruction, int address) {
    this.instruction = instruction;
    this.address = address;
  }
  public IJVMInstruction(Instruction instruction, int address, int lineno) {
    this.instruction = instruction;
    this.address = address;
    this.lineno = lineno;
  }
  public IJVMInstruction(Instruction instruciton, int address, String label) {
    this.instruction = instruction;
    this.address = address;
    this.label = label;
  }

  public Instruction getInstruction() {
    return instruction;
  }

  public int getAddress() {
    return address;
  }

  public void setParameter(int param) {
    this.param = param;
  }

  public int getParameter() {
    return param;
  }

  public void setParameter2(int param2) {
    this.param2 = param2;
  }

  public int getParameter2() {
    return param2;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public int getLineno() {
    return lineno;
  }

  public int getType() {
    return instruction.getType();
  }

  public int getOpcode() {
    return instruction.getOpcode();
  }

  // rms wide fix

  public void setWide(boolean wide_flg) {
    wide = wide_flg;
  }

  public boolean isWide() {
    return wide;
  }

  //  public String toString() { // overrides Object.toString()
  //   String s = Integer.toHexString("0x" + instruction.getOpcode());
  //  if (getType() > 0 && getType() <= Instruction.VARNUM) 
  //    s += (" 0x" + Integer.toHexString(param));
  //  else if (getType() > Instruction.VARNUM) {
  //    int 
}
