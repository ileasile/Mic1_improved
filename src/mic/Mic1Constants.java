package mic;

/* $Id: Mic1Constants.java,v 1.2 2005/02/22 05:28:25 RMS Exp $
*
*  Mic1Constants.java
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
* This is an interface that has any shared constants, such as 
* magic numbers for binary files, initial addresses of SP,CPP,LV, etc.
*
* @author 
*   Dan Stone (<a href="mailto:dans@ontko.com"><i>dans@ontko.com</i></a>),
*   Ray Ontko & Co,
*   Richmond, Indiana, US
*/
public interface Mic1Constants {

  /** Four byte "magic number" for .mic1 binary files */
  public static final int
    mic1_magic1 = 0x12,
    mic1_magic2 = 0x34,
    mic1_magic3 = 0x56,
    mic1_magic4 = 0x78;

  /** Four byte "magic number" for .ijvm binary files */
  public static final int
    magic1 = 0x1D,
    magic2 = 0xEA,
    magic3 = 0xDF,
    magic4 = 0xAD;

  /** Word address of the constant pool pointer */
  public static final int CPP = 0x4000;

  /** Byte address of the constant pool pointer */
  public static final int CPP_B = 0x10000;

  /** Word address of the stack pointer */
  public static final int SP = 0x8000;

  /** Byte address of the stack pointer */
  public static final int SP_B = 0x20000;

  /** Word address of the local variable frame */
  public static final int LV = 0xc000;

  /** Word address of the local variable frame */
  public static final int LV_B = 0x30000;

  /** Size in bytes of main memory */
  public static final int MEM_MAX = 0x40000;
}
