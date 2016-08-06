package mic;

/* $Id: IJVMLoader.java,v 1.2 2005/02/22 05:28:24 RMS Exp $
*
*  IJVMLoader.java
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

/**
* Loads an IJVM program into memory.
*
* @author 
*   Dan Stone (<a href="mailto:dans@ontko.com"><i>dans@ontko.com</i></a>),
*   Ray Ontko & Co,
*   Richmond, Indiana, US
*/
public class IJVMLoader implements Mic1Constants {

  BufferedInputStream in = null;
  String filename;
  byte program[];
  boolean eof;

  public IJVMLoader(String filename) throws BadMagicNumberException, IOException {
    this.filename = filename;
    eof = false;
    program = new byte[MEM_MAX];
    in = new BufferedInputStream(new FileInputStream(filename));
    validateFile();
    while (!eof) readBlock();
  }

  public byte[] getProgram() {
    return program;
  }

  private void validateFile() throws BadMagicNumberException, IOException {
    if (in.read() != magic1 || in.read() != magic2 || in.read() != magic3 || in.read() != magic4) 
      throw new BadMagicNumberException();
  }

  private void readBlock() {
    try {
      int b = read();
      if (!eof) {
	int origin = (b << 24) + (read() << 16) +(read() <<8) + read();
	int byte_count = (read() << 24) + (read() << 16) +(read() <<8) + read();
	for (int i = 0; i < byte_count; i++) {
	  program[origin + i] = (byte)read();
	}
      }
    }
    catch (Exception ioe) {
    }
  }

  private int read() throws IOException{
    int i = in.read();
    if (i == -1)
      eof = true;
    return i;
  }

  public static void main(String args[]) throws Exception{
    IJVMLoader re = new IJVMLoader(args[0]);
  }
}
