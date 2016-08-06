package mic.ontko;

/*
*
*  IJVMAssembler.java
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

import java.util.*;
import java.io.*;
import javax.swing.JTextArea;
import mic.CompPane;
import mic.MicTranslator;

/**
* Main part of assembler.
*
* @author 
*   Dan Stone (<a href="mailto:dans@ontko.com"><i>dans@ontko.com</i></a>),
*   Ray Ontko & Co,
*   Richmond, Indiana, US
*/
public class IJVMAssembler implements Mic1Constants, MicTranslator {
 
  private InputStream in = null;
  private OutputStream out = null;
  private Hashtable ops = null;
  private Vector constants = null;
  private Vector methods = null;
  private IJVMMethod main = null;
  private Hashtable method_refs = null;
  private static int lineno;
  private boolean status;
  static final byte
    magic1 = (byte)0x1D,
    magic2 = (byte)0xEA,
    magic3 = (byte)0xDF,
    magic4 = (byte)0xAD;
  int CPP_B = CPP * 4;
  int byte_count;
  int const_count;

  private CompPane cpane;

  public IJVMAssembler(CompPane cpane, InputStream in, OutputStream out) {
    this();
    this.cpane = cpane;
    this.in = in;
    this.out = out;
  }

  public IJVMAssembler() {
    ops = new Hashtable();
    constants = new Vector();
    methods = new Vector();
    method_refs = new Hashtable();
  }

  public void setCPane(CompPane cpane) {this.cpane = cpane;}
  public void setInputStream(InputStream in) {this.in = in;}
  public void setOutputStream(OutputStream out) {this.out = out;}

  public void run() throws Exception {
    if (!init()) return;
    if (parse()) {
      try {
        generate();
	cpane.setStatus(status);
      }
      catch (IOException ioe) {
        cpane.appendErr("Exception encountered while generating code");
	cpane.setStatus(false);
      }
    }
  }

  public IJVMAssembler(String infile, String outfile) {
    try {
      lineno = 0;
      in = new BufferedInputStream(new FileInputStream(infile));
      ops = new Hashtable();
      constants = new Vector();
      methods = new Vector();
      method_refs = new Hashtable();
      init();
    }
    catch (Exception e) {
      System.out.println("Error opening file " + infile);
    }
    try {
      if (parse()) {
	out = new FileOutputStream(outfile);
	generate();
      }
    }
    catch (IOException e) {
      System.out.println("Error opening file " + outfile);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
    * init() initializes the ops Vector, which contains descriptions of 
    * each assembly language instruction--mnemonic, opcode, parameters.
    * This information comes from the file <code>ijvm.conf</code>, which
    * can be customized to suit the requirements of an altered instruction
    * set or simulator architecture.
    */
  private boolean init() {
    ops.clear();
    ConfParser cp = new ConfParser(cpane, cpane.getFrame().ijvmFile);
    if (cp.fail) return false;
    Vector ins = cp.getInstructionSet();
    for (int i = 0; i < ins.size(); i++) {
      Instruction op = (Instruction)ins.elementAt(i);
      if (op.getMnemonic() != null)
	ops.put(op.getMnemonic(), op);
      else cpane.appendErr("\n Error: null instruction mnemonic, opcode: " + op.getOpcode());
    }
    byte_count = 0;
    const_count = 0;
    return true;
  }

  public boolean getStatus() {
    return status;
  }

  private boolean parse() {
    status = true;
    boolean const_parsed = false;
    boolean main_parsed = false;
    String line = readLine();
    while (true) {
      while (line != null && line.trim().length() == 0)
	line = readLine();
      if (line == null) 
	break;
      else {
	line = line.trim();
	if (line.equals(".constant")) {
	  if (main_parsed) {
	    error("Constants must be defined before method definitions");
	    status = false;
	  }
	  else {
	    if (!const_parsed) {
	      status = parseConstants() && status;
	      const_parsed = true;
	    }
	    else {
	      error("Constants already declared");
	      status = false;
	    }
	  }
	}
	else if (line.equals(".main")) {
	  if (main_parsed) {
	    error("Method main already declared");
	    status = false;
	    //	    line = readLine();
	  }
	  else {
	    status = parseMain() && status;
	    main_parsed = true;
	  }
	}
	else if (line.startsWith(".method")) {
	  if (!main_parsed) {
	    error("main method must be defined before other methods");
	    status = false;
	  }
	  else 
	    status = parseMethod(line) && status;
	}
	else {
	  error("Unexpected directive: " + line);
	  status = false;
	  //	  line = readLine();
	}
      }
      line = readLine();
    }
    status = linkMethods() && status;
    return status;
  }

  private boolean parseConstants() {
    boolean status = true;
    String line = readLine();
    StringTokenizer st = null;
    while (line != null && !line.equals(".end-constant")) {
      if (line.trim().length() > 0) {
	st = new StringTokenizer(line);
	String name = st.nextToken();
	if (!st.hasMoreTokens()) {
	  error("Constant must have a value");
	  status = false;
	}
	else {
	  Integer value = decode(st.nextToken());
	  constants.addElement(new IJVMConstant(name, value.intValue()));
	}
      }
      line = readLine();
    }
    if (line == null) {
      error("Unexpected end of file");
      status = false;
    }
    return status;
  }
  
  private boolean parseMain() {
    main = new IJVMMethod("main", ops, constants, in, lineno, cpane);
    if (main.getParameterCount() > 1) {
      error("main may not have parameters");
      lineno = main.getLineno();
      return false;
    }
    lineno = main.getLineno();
    return main.getStatus();
  }

  private boolean parseMethod(String line) {
    boolean status = true;
    String name_params = line.substring(7); // strip off ".method", remainder is method name & parameters
    if (name_params.trim().length() == 0) {
      error("Method must be named");
      status = false;
    }
    else {
      IJVMMethod method = new IJVMMethod(name_params, ops, constants, in, lineno, cpane);
      status = method.getStatus();
      lineno = method.getLineno();
      methods.addElement(method);
    }
    return status;
  }

  private boolean linkMethods() {
    byte_count = main.getByteCount();
    boolean status = true;
    for (int i = 0; i < methods.size(); i++) {
      IJVMMethod method = (IJVMMethod)methods.elementAt(i);
      IJVMConstant constant = new IJVMConstant(method.getName(), byte_count);
      constants.addElement(constant);
      method_refs.put(method.getName(), new Integer(constants.indexOf(constant)));
      byte_count = byte_count + method.getByteCount() + 4;  // four bytes for param & local var count
    }
    for (int i = 0; i < methods.size(); i++) 
      status = ((IJVMMethod)methods.elementAt(i)).linkMethods(method_refs) && status;
    status = main.linkMethods(method_refs) && status;
    return status;
  }

  private void generate() throws IOException {
    out.write(magic1);
    out.write(magic2);
    out.write(magic3);
    out.write(magic4);
    out.write(CPP_B >> 24);
    out.write(CPP_B >> 16);
    out.write(CPP_B >> 8);
    out.write(CPP_B);
    out.write((constants.size()*4) >> 24);
    out.write((constants.size()*4) >> 16);
    out.write((constants.size()*4) >> 8);
    out.write(constants.size()*4);
    for (int i = 0; i < constants.size(); i++) {
      IJVMConstant con = (IJVMConstant)constants.elementAt(i);
	out.write(con.getValue() >> 24);
	out.write(con.getValue() >> 16);
	out.write(con.getValue() >> 8);
	out.write(con.getValue());
    }
    out.write(0);
    out.write(0);
    out.write(0);
    out.write(0);
    out.write(byte_count >> 24);
    out.write(byte_count >> 16);
    out.write(byte_count >> 8);
    out.write(byte_count);
    main.generate(out);
    for (int i = 0; i < methods.size(); i++) {
      IJVMMethod method = (IJVMMethod)methods.elementAt(i);
      out.write(method.getParameterCount() >> 8);
      out.write(method.getParameterCount());
      out.write(method.getVarnumCount() >> 8);
      out.write(method.getVarnumCount());
      method.generate(out);
    }
    out.close();
  }

  private void error(String msg) {
    cpane.appendErr(lineno + ": " + msg);
  }

  private String readLine() {
    int ctemp;
    String s = new String();
    try {
      int ch = 0;
      while (((char)ch) != '\n' && ch > -1) {
        ch = in.read();
        if (ch == -1 && s.length() == 0) {
          return null;
        }
        if (ch == '/') {
          ctemp = in.read();
          if ((char)ctemp == '/') {
            ch = in.read();
            while (((char)ch) != '\n' && ch > -1)
              ch = in.read();
            lineno++;
            return s + " ";
          }
          else if (ch != 13)
            s = s + ((char)ch) + Character.toLowerCase((char)ctemp);
        }
        if (ch != '\n' && ch > -1 && ch != 13) {
          if ((char)ch == '\'') {
            ctemp = in.read();
            s = s + (char)ch + (char)ctemp;
          }
          else
            s = s + Character.toLowerCase((char)ch);
        }
      }
      lineno++;
      return s;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return s;
  }

  public void writeSymbols(PrintStream dbo) {
    int byte_count = main.getByteCount();

    dbo.println("ijvm-debug 1");
    dbo.println("method main 0 " + byte_count);

    main.writeSymbols(dbo);

    for(int i = 0; i < methods.size(); i++) {
      IJVMMethod ijvmm = (IJVMMethod)methods.elementAt(i);
      dbo.println("method " + ijvmm.getName() + " " + byte_count + " " +
		  (ijvmm.getByteCount() + 4));
      byte_count += ijvmm.getByteCount() + 4;
      ijvmm.writeSymbols(dbo);
    }
    
    for(int i = 0; i < constants.size(); i++) {
      IJVMConstant ijvmc = (IJVMConstant)constants.elementAt(i);
      dbo.println("constant " + i + " " + ijvmc.getName());
    }
  
  }

  private Integer decode(String str) throws NumberFormatException {
    if (str.startsWith("0x") || str.startsWith("0X")) {
      return Integer.valueOf(str.substring(2), 16);
    }
    if (str.startsWith("#")) {
      return Integer.valueOf(str.substring(1), 16);
    }
    if (str.startsWith("0") && str.length() > 1) {
      return Integer.valueOf(str.substring(1), 8);
    }
    if (str.charAt(0) == '\'' && str.length() > 1) {
      return new Integer((int)str.charAt(1));
    }
    return Integer.valueOf(str);
  }    

}
