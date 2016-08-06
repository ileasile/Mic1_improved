package mic.ontko;

/*
 *
 *  ConfParser.java
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

import java.io.*;
import java.util.*;
import mic.CompPane;

/**
 * Parses a macrolanguage description file
 *
 * @author 
 *   Dan Stone (<a href="mailto:dans@ontko.com"><i>dans@ontko.com</i></a>),
 *   Ray Ontko & Co,
 *   Richmond, Indiana, US
 */
public class ConfParser {

	private static final int radix = 16;
	private static final int exit_code = 1;
	private Reader in = null;
	private int lineno;
	private String filename = null;
	private Vector instruction_set = null;
	private CompPane cpane;
	public boolean fail = false;

	public ConfParser(CompPane cpane, String filename) {
		this.filename = filename;
		this.cpane = cpane;
		try {
			try {
				in = new BufferedReader(new InputStreamReader
						(new FileInputStream(new File(System.getProperty("user.dir"), filename))));
				parse();
			} catch (FileNotFoundException ex) {
				try {
					in = new BufferedReader(new InputStreamReader
							(getClass().getResourceAsStream("resources/"+cpane.getFrame().defaultIjvmFile)));
					parse();
				} catch (FileNotFoundException ex1) {
					cpane.appendErr("File Error Loading IJVM\n"+ex1.getMessage());
					fail = true;
				}
			}
		} catch (IOException ex) {
			cpane.appendErr("IO Error Loading IJVM\n"+ex.getMessage());
			fail = true;
		}
	}

	public Vector getInstructionSet() {
		return instruction_set;
	}

	private void parse()
			throws IOException {
		instruction_set = new Vector();
		lineno = 0;
		String s = readLine();
		while (s.length() > 0) {
			StringTokenizer st = new StringTokenizer(s);
			int opcode;
			String mnemonic = null;
			try {
				if (st.hasMoreTokens()) {
					String str = st.nextToken();
					opcode = decode(str).intValue();
					if (st.hasMoreTokens()) { 
						mnemonic = st.nextToken();
						int type = (opcode == 0xC4) ? Instruction.WIDE : Instruction.NOPARAM;   // rms wide-fix
						if (st.hasMoreTokens()) {
							String param = st.nextToken();
							if (param.equalsIgnoreCase("varnum")) {
								if (st.hasMoreTokens() && st.nextToken().equalsIgnoreCase("const"))
									type = Instruction.VARNUM_CONST;
								else type = Instruction.VARNUM;
							}
							else if (param.equalsIgnoreCase("index"))
								type = Instruction.INDEX;
							else if (param.equalsIgnoreCase("label"))
								type = Instruction.LABEL;
							else if (param.equalsIgnoreCase("byte"))
								type = Instruction.BYTE;
							else if (param.equalsIgnoreCase("const"))
								type = Instruction.CONST;
							else if (param.equalsIgnoreCase("offset"))
								type = Instruction.OFFSET;
							else cpane.appendErr("Parameter type " + param + " not supported");
						}
						instruction_set.addElement(new Instruction(opcode, mnemonic, type));
					}
				}
			}
			catch (NumberFormatException nfe) {
				cpane.appendErr(" " + filename + " " + lineno + ": invalid number format for opcode");
			}
			s = readLine();
		}
	}

	/**
     decode() parses a String and creates an Integer object with the appropriate value.  
     This method performs the same function as the java.lang.Integer.decode() method. It is
     included to ensure compatability with Java 1.0 compilers.
	 */
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
		return Integer.valueOf(str);
	}    

	private String readLine()
			throws IOException {
		String s = new String();
		int ch = 0;
		while (((char)ch) != '\n' && ch > -1) {
			ch = in.read();
			if (ch == '/' && in.read() == '/') {
				ch = in.read();
				while (((char)ch) != '\n' && ch > -1)
					ch = in.read();
				lineno++;
				return s.toLowerCase() + " ";
			}
			if (ch != '\n' && ch > -1)
				s += (char)ch;
		}
		lineno++;
		return s.toLowerCase();
	}
}
