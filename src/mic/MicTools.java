/*  $Id$
 *
 *  MicTools.java
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

import java.applet.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 * tools
 * 
 * @author
 *   Richard M. Salter (<a href="mailto:rms@cs.oberlin.edu"><i>rms@cs.oberlin.edu</i></a>),
 *   Oberlin College
 *   Oberlin, OH 44074
 */
public class MicTools {
  private static Hashtable langTable, aluTable = new Hashtable();
  public static int wideOp = 0;
  final static ALUCombo[] alucombos = {
    new ALUCombo((byte)0x18, "A"),
    new ALUCombo((byte)0x14, "B"),
    new ALUCombo((byte)0x1a, "~A"),
    new ALUCombo((byte)0x2c, "~B"),
    new ALUCombo((byte)0x3c, "A + B"),
    new ALUCombo((byte)0x3d, "A + B + 1"),
    new ALUCombo((byte)0x39, "A + 1"),
    new ALUCombo((byte)0x35, "B + 1"),
    new ALUCombo((byte)0x3f, "B - A"),
    new ALUCombo((byte)0x36, "B - 1"),
    new ALUCombo((byte)0x3b, "-A"),
    new ALUCombo((byte)0x0c, "A and B"),
    new ALUCombo((byte)0x1c, "A or B"),
    new ALUCombo((byte)0x10, "0"),
    new ALUCombo((byte)0x11, "1"),
    new ALUCombo((byte)0x12, "-1"),
  };

  static {
    for (int i = 0; i < alucombos.length; i++) 
      aluTable.put(new Byte(alucombos[i].key), alucombos[i]);
  }

  static String formatHex(int x, int n) {
    String tmp = Integer.toHexString(x);
    if (tmp.length() > n) tmp = tmp.substring(tmp.length()-n);
    else {
      int top = tmp.length();
      for (int i = 0; i < n - top; i++) tmp = "0" + tmp;
    }
    return tmp;
  }

  public static void initIJVM(InputStream conf_file) 
    throws IOException {
    langTable = new Hashtable();
    String s;
    ConfParser cp = new ConfParser(conf_file);
    Vector ins = cp.getInstructionSet();
    for (Iterator it = ins.iterator(); it.hasNext();) {
      Instruction instr = (Instruction)it.next();
      byte opcode = (byte)instr.getOpcode();
      String mnemonic = instr.getMnemonic().toUpperCase();
      int type = instr.getType();
      boolean branch = (type == Instruction.LABEL) || (type == Instruction.OFFSET) || (type == Instruction.INDEX);
      int data = (type == Instruction.BYTE || type == Instruction.VARNUM) ? 1 : (type == Instruction.VARNUM_CONST) ? 2 : 0;
      langTable.put(new Byte(opcode), new IJVMOpInfo(opcode, mnemonic, branch, data, type==Instruction.WIDE));
      if (type == Instruction.WIDE) wideOp = opcode & 0xff;    // wide fix
//        System.out.println(opcode+" "+mnemonic+" "+branch+" "+data+" "+type);
    }
  }

  public static String aluInfo(byte b) {
    ALUCombo aluc = (ALUCombo)aluTable.get(new Byte(b));
    if (aluc == null) return "";
    else return aluc.info;
  }

  static boolean wide_flg = false;

  public static String[] disAssemble(byte[] prog) {
    Vector ans = new Vector();
    wide_flg = false;
    short pc = 0;
    for (int i = 0; i < prog.length; i++) {
      byte opCode = prog[i];
      IJVMOpInfo info = (IJVMOpInfo)langTable.get(new Byte(opCode));
      if (info == null) {
	ans.add("Error");
	continue;
      }
      if (info.branch) {
	int offset = ((int)prog[i+1] & 0xff) << 8 | ((int)prog[i+2] & 0xff);
	i += 2;
	ans.add(info.mnemonic+" "+formatHex((short)offset, 4));
	ans.add(new String(""));
	ans.add(new String(""));
	continue;
      } 
      if (info.data > 0) {
	if (wide_flg && info.data == 1) {                                 //  wide fix
	  int offset = ((int)prog[i+1] & 0xff) << 8 | ((int)prog[i+2] & 0xff);
	  i += 2;
	  ans.add(info.mnemonic+" "+formatHex((short)offset, 4));
	  ans.add(new String(""));
	  ans.add(new String(""));
	  wide_flg = info.wide;
	  continue;
	}
	StringBuffer line =
	  new StringBuffer(info.mnemonic+((info.data == 0) ? "" : " "));
	for (int j = 0; j < info.data; j++) {
	  int num = (int)prog[++i];
	  line.append(formatHex(num, 2) + " ");
	}
	ans.add(line.toString());
	for (int j = 0; j < info.data; j++) ans.add(new String(""));
	continue;
      }
      wide_flg = info.wide;
      ans.add(new String(info.mnemonic));
    }
    String[] tmp = new String[0];
    tmp = (String[])ans.toArray(tmp);
    return tmp;
  }
}

class ALUCombo {
  byte key;
  String info;

  public ALUCombo(byte key, String info) {
    this.key = key;
    this.info = info;
  }
}

class IJVMOpInfo {
  byte opCode;
  String mnemonic;
  boolean branch, wide;
  int data;

  public IJVMOpInfo(byte opCode, String mnemonic, boolean branch, int data, boolean wide) {
    this.opCode = opCode;
    this.mnemonic = mnemonic;
    this.branch = branch;
    this.data = data;
    this.wide = wide;
  }
}
