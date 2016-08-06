package mic.ontko;

/*
 *
 *  IJVMMethod.java
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
import mic.CompPane;

/**
 * Parses a method, stores information about parameters, local variables,
 * and instructions, and writes this information to an output stream.
 *
 * @author 
 *   Dan Stone (<a href="mailto:dans@ontko.com"><i>dans@ontko.com</i></a>),
 *   Ray Ontko & Co,
 *   Richmond, Indiana, US
 */
public class IJVMMethod {

	private String name = null;
	private String params = null;
	private String end_method = null;
	private int byte_count;
	private Vector code = null;
	private Vector varnums = null;
	private Vector constants = null;
	private Hashtable labels = null;
	private Hashtable ops = null;
	private InputStream in = null;
	private int lineno;
	private boolean status;
	private int param_count;
	private int var_count;
	private CompPane cpane;
	private boolean wide_flg = false;  // rms - wide fix

	public IJVMMethod(String name_params, Hashtable ops, Vector constants, InputStream in, int lineno, CompPane cpane) {
		this.ops = ops;
		this.constants = constants;
		this.in = in;
		this.lineno = lineno;
		this.cpane = cpane;
		code = new Vector();
		labels = new Hashtable();
		varnums = new Vector();
		byte_count = 0;
		var_count = 0;
		status = true;
		if (name_params.equals("main")) {
			param_count = 0; // main has no parameters
			name = "main";
			end_method = ".end-main";
		}
		else {
			param_count = 1; // OBJREF always counts as a parameter
			varnums.addElement("LINK PTR"); // OBJREF gets overwritten with the link-pointer, needed for IRETURN
			end_method = ".end-method";
			if (name_params.indexOf('(') < 0) {
				error(lineno, "Invalid method declaration: " + name_params +
						"\n  must contain ()");
				status = false;
			}
			else {
				name = name_params.substring(0,name_params.indexOf('(')).trim();
				params = name_params.substring(name_params.indexOf('(') + 1).trim();
				parseParameters();
			}
		}
		parse();

	}

	public int getByteCount() {
		return byte_count;
	}

	public Vector getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public int getLineno() {
		return lineno;
	}

	public boolean getStatus() {
		return status;
	}

	public int getParameterCount() {
		return param_count;
	}

	public int getVarnumCount() {
		return var_count;
	}

	private void parse() {
		String line = readLine();
		while (line != null && line.trim().length() == 0)
			line = readLine();
		if (line.trim().equals(".var")) {
			parseVarnums();
			line = readLine();
			while (line != null && line.trim().length() == 0)
				line = readLine();
		}
		while (line != null && !line.trim().equals(end_method)) {
			parseInstruction(line.trim());
			line = readLine();
		}
		linkLabels();
	}

	private void parseParameters() {
		if (params.indexOf(')') < 0) {
			error(lineno, "Missing ')' in method declaration");
			status = false;
		}
		else {
			char ch = ' ';
			while (ch != ')') {
				String param = "";
				ch = params.charAt(0);
				params = params.substring(1);
				while (ch != ',' && ch != ')') {
					param = param + ch;
					ch = params.charAt(0);
					params = params.substring(1);
				}
				if (param.trim().length() > 0) {
					varnums.addElement(param.trim());
					param_count++;
				}
			}
		}
	}

	private void parseVarnums() {
		String line = readLine();
		while (!line.trim().equals(".end-var")) {
			if (line == null) {
				error(lineno, "Unexpected end of file");
				status = false;
				break;
			}
			varnums.addElement(line.trim());
			var_count++;
			line = readLine();
		}
	}

	private void parseInstruction(String line) {
		if (line.length() > 0) {
			StringTokenizer st = new StringTokenizer(line);
			String mnemonic = st.nextToken();
			if (mnemonic.indexOf(':') > -1) {
				String label = mnemonic.substring(0, mnemonic.indexOf(':'));
				labels.put(label, new Integer(byte_count));
				mnemonic = mnemonic.substring(mnemonic.indexOf(':') + 1);
				if (mnemonic.trim().length() == 0) {
					if (!st.hasMoreTokens()) 
						return;
					else
						mnemonic = st.nextToken();
				}
			}
			Instruction instruction = (Instruction)ops.get(mnemonic);
			IJVMInstruction inst = null;
			if (instruction == null) {
				error(lineno, "Invalid instruction: " + mnemonic);
				status = false;
			}
			else {
				inst = new IJVMInstruction(instruction, byte_count, lineno);
				// parse parameters
				if (st.hasMoreTokens()) {
					String const_name = null;
					int const_index;
					switch (inst.getType()) {
					case Instruction.WIDE:
					case Instruction.NOPARAM : 
						error(lineno, "Instruction takes no parameters");
						status = false;
						break; 
					case Instruction.BYTE :
						inst.setParameter(decode(st.nextToken()).intValue());
						code.addElement(inst);
						byte_count += 2;
						break;
					case Instruction.VARNUM :
						String varname = st.nextToken();
						if (varnums.indexOf(varname) < 0){
							error(lineno, "Undeclared variable: " + varname);
							status = false;
						}
						else {
							inst.setParameter(varnums.indexOf(varname));
							code.addElement(inst);
							byte_count += 2;
							if (wide_flg) byte_count++;
						}
						break;
					case Instruction.LABEL : 
						String label = st.nextToken();
						inst.setLabel(label);
						code.addElement(inst);
						byte_count += 3;
						break;
					case Instruction.OFFSET :
						const_name = st.nextToken();
						inst.setLabel(const_name);
						code.addElement(inst);
						byte_count += 3;
						break;
					case Instruction.VARNUM_CONST :
						varname = st.nextToken();
						if (varnums.indexOf(varname) < 0) {
							error(lineno, "Undeclared variable: " + varname);
							status = false;
						}
						else {
							inst.setParameter(varnums.indexOf(varname));
							inst.setParameter2(decode(st.nextToken()).intValue());
							code.addElement(inst);
							byte_count += 3;
							break;
						}
						break;
					case Instruction.CONST :
					case Instruction.INDEX :
						const_name = st.nextToken();
						if (const_name.startsWith("=")) {
							int const_value = decode(const_name.substring(1)).intValue();
							int const_count = constants.size();
							constants.addElement(new IJVMConstant(const_name, const_value));
							inst.setParameter(const_count);
							code.addElement(inst);
							byte_count += 3;
						}
						else {
							const_index = findConstant(const_name);
							if (const_index >= 0) {
								inst.setParameter(const_index);
								code.addElement(inst);
								byte_count += 3;
							}
							else 
								error(lineno, "Constant not declared: " + const_name);
						}
						break;
					default :
					}
				}
				else if (inst.getType() == Instruction.NOPARAM || inst.getType() == Instruction.WIDE) {
					code.addElement(inst);
					byte_count++;
				}
				else
					error(lineno, "Parameter(s) expected");
				inst.setWide(wide_flg);
				wide_flg = (inst.getType() == Instruction.WIDE); // rms wide-fix
			}
		}
	}

	private void linkLabels() {
		for (int i = 0; i < code.size(); i++) {
			IJVMInstruction inst = (IJVMInstruction)code.elementAt(i);
			if (inst.getType() == Instruction.LABEL) {
				Integer offset = (Integer)labels.get(inst.getLabel());
				if (offset == null) {
					error(inst.getLineno(),"Invalid goto label: " + inst.getLabel());
					status = false;
				}
				else 
					inst.setParameter(offset.intValue() - inst.getAddress());
			}
		}
	}

	public boolean linkMethods(Hashtable method_refs) {
		status = true;
		for (int i = 0; i < code.size(); i++) {
			IJVMInstruction inst = (IJVMInstruction)code.elementAt(i);
			if (inst.getType() == Instruction.OFFSET) {
				String meth_name = inst.getLabel();
				Integer offset = (Integer)method_refs.get(meth_name);
				if (offset == null) {
					error(inst.getLineno(),"Method " + meth_name + " is not defined");
					status = false;
				}
				else 
					inst.setParameter(offset.intValue());
			}
		}
		return status;
	}

	public void generate(OutputStream out) throws IOException {
		for (int i = 0; i < code.size(); i++) {
			IJVMInstruction inst = (IJVMInstruction)code.elementAt(i);
			out.write(inst.getOpcode());
			switch (inst.getType()) {
			case Instruction.LABEL : 
				int address = inst.getParameter();
				out.write((address >> 8) & 255);
				out.write(inst.getParameter());
				break;
			case Instruction.NOPARAM :
			case Instruction.WIDE :
				break;
			case Instruction.BYTE :
			case Instruction.VARNUM : 
				if (inst.isWide()) {
					int param = inst.getParameter();
					out.write((param >> 8) & 255);
					out.write(inst.getParameter());
				} else
					out.write(inst.getParameter());
				break;
			case Instruction.OFFSET :
			case Instruction.INDEX :
			case Instruction.CONST :
				out.write(inst.getParameter() >> 8);
				out.write(inst.getParameter());
				break;
			case Instruction.VARNUM_CONST :
				out.write(inst.getParameter());
				out.write(inst.getParameter2());
				break;
			default :
			}
		}
	}

	public void writeSymbols(PrintStream dbo) {
		for (int i = 0; i < varnums.size(); i++) {
			dbo.println("variable " + name + " " + i + " " + varnums.elementAt(i));
		}
	}

	private int findConstant(String const_name) {
		for (int i = 0; i < constants.size(); i++) {
			if (((IJVMConstant)constants.elementAt(i)).getName().equals(const_name))
				return i;
		}
		return -1;
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


	private void error(int line, String msg) {
		cpane.appendErr(line + ": " + msg);
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
