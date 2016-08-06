package mic;

/*
*
*  Mic1Instruction.java
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

/*
Mic1Instruction

This class represents an instruction which might appear 
in the Mic-1 control store.

To Do

We should probably create a separate class for Mic1Reader
and move the read method from here to the Mic1Reader class.

Modification History

Name             Date       Comment
---------------- ---------- ----------------------------------------
Ray Ontko        1998.09.01 Created

*/

import java.io.* ;
import java.lang.* ;

public class Mic1Instruction 
{
public static final int B_MDR = 0 ;
public static final int B_PC = 1 ;
public static final int B_MBR = 2 ;
public static final int B_MBRU = 3 ;
public static final int B_SP = 4 ;
public static final int B_LV = 5 ;
public static final int B_CPP = 6 ;
public static final int B_TOS = 7 ;
public static final int B_OPC = 8 ;

public int NEXT_ADDRESS = 0 ;
public boolean JMPC = false ;
public boolean JAMN = false ;
public boolean JAMZ = false ;
public boolean SLL8 = false ;
public boolean SRA1 = false ;
public boolean F0 = false ;
public boolean F1 = false ;
public boolean ENA = false ;
public boolean ENB = false ;
public boolean INVA = false ;
public boolean INC = false ;
public boolean H = false ;
public boolean OPC = false ;
public boolean TOS = false ;
public boolean CPP = false ;
public boolean LV = false ;
public boolean SP = false ;
public boolean PC = false ;
public boolean MDR = false ;
public boolean MAR = false ;
public boolean WRITE = false ;
public boolean READ = false ;
public boolean FETCH = false ;
public int B = 0 ;

public Mic1Instruction()
{
}


public int read( InputStream in ) throws IOException
// public int read( Reader in ) throws IOException
{
int b0 ;
int b1 ;
int b2 ;
int b3 ;
int b4 ;

if ( ( b0 = in.read() ) == -1 ) return( -1 ) ;
if ( ( b1 = in.read() ) == -1 ) return( -1 ) ;
if ( ( b2 = in.read() ) == -1 ) return( -1 ) ;
if ( ( b3 = in.read() ) == -1 ) return( -1 ) ;
if ( ( b4 = in.read() ) == -1 ) return( -1 ) ;

NEXT_ADDRESS = ( b0 << 1 ) | ( ( b1 & 0x80 ) >> 7 ) ;
if ( ( b1 & 0x40 ) > 0 ) JMPC = true ;
if ( ( b1 & 0x20 ) > 0 ) JAMN = true ;
if ( ( b1 & 0x10 ) > 0 ) JAMZ = true ;
if ( ( b1 & 0x08 ) > 0 ) SLL8 = true ;
if ( ( b1 & 0x04 ) > 0 ) SRA1 = true ;
if ( ( b1 & 0x02 ) > 0 ) F0 = true ;
if ( ( b1 & 0x01 ) > 0 ) F1 = true ;
if ( ( b2 & 0x80 ) > 0 ) ENA = true ;
if ( ( b2 & 0x40 ) > 0 ) ENB = true ;
if ( ( b2 & 0x20 ) > 0 ) INVA = true ;
if ( ( b2 & 0x10 ) > 0 ) INC = true ;
if ( ( b2 & 0x08 ) > 0 ) H = true ;
if ( ( b2 & 0x04 ) > 0 ) OPC = true ;
if ( ( b2 & 0x02 ) > 0 ) TOS = true ;
if ( ( b2 & 0x01 ) > 0 ) CPP = true ;
if ( ( b3 & 0x80 ) > 0 ) LV = true ;
if ( ( b3 & 0x40 ) > 0 ) SP = true ;
if ( ( b3 & 0x20 ) > 0 ) PC = true ;
if ( ( b3 & 0x10 ) > 0 ) MDR = true ;
if ( ( b3 & 0x08 ) > 0 ) MAR = true ;
if ( ( b3 & 0x04 ) > 0 ) WRITE = true ;
if ( ( b3 & 0x02 ) > 0 ) READ = true ;
if ( ( b3 & 0x01 ) > 0 ) FETCH = true ;
B = ( b4 >> 4 ) & 0x0F ;

return( 1 ) ;
}

public void write( OutputStream out ) throws IOException
{
int b0 = 0 ;
int b1 = 0 ;
int b2 = 0 ;
int b3 = 0 ;
int b4 = 0 ;

b0 = ( NEXT_ADDRESS & 0x1FE ) >> 1 ;
b1 = ( NEXT_ADDRESS & 0x01 ) << 7 ;
if ( JMPC ) b1 = b1 | 0x40 ;
if ( JAMN ) b1 = b1 | 0x20 ;
if ( JAMZ ) b1 = b1 | 0x10 ;
if ( SLL8 ) b1 = b1 | 0x08 ;
if ( SRA1 ) b1 = b1 | 0x04 ;
if ( F0 ) b1 = b1 | 0x02 ;
if ( F1 ) b1 = b1 | 0x01 ;
if ( ENA ) b2 = b2 | 0x80 ;
if ( ENB ) b2 = b2 | 0x40 ;
if ( INVA ) b2 = b2 | 0x20 ;
if ( INC ) b2 = b2 | 0x10 ;
if ( H ) b2 = b2 | 0x08 ;
if ( OPC ) b2 = b2 | 0x04 ;
if ( TOS ) b2 = b2 | 0x02 ;
if ( CPP ) b2 = b2 | 0x01 ;
if ( LV ) b3 = b3 | 0x80 ;
if ( SP ) b3 = b3 | 0x40 ;
if ( PC ) b3 = b3 | 0x20 ;
if ( MDR ) b3 = b3 | 0x10 ;
if ( MAR ) b3 = b3 | 0x08 ;
if ( WRITE ) b3 = b3 | 0x04 ;
if ( READ ) b3 = b3 | 0x02 ;
if ( FETCH ) b3 = b3 | 0x01 ;
b4 = ( B & 0x0F ) << 4 ;

out.write( b0 ) ;
out.write( b1 ) ;
out.write( b2 ) ;
out.write( b3 ) ;
out.write( b4 ) ;
}

public String toString() 
{
String s = "" ;
String a = "H" ;
String b = "" ;
String c = "" ;

// decode the C-bus bits
if ( H )
  s = s + "H=" ;
if ( OPC )
  s = s + "OPC=" ;
if ( TOS )
  s = s + "TOS=" ;
if ( CPP )
  s = s + "CPP=" ;
if ( LV )
  s = s + "LV=" ;
if ( SP )
  s = s + "SP=" ;
if ( PC )
  s = s + "PC=" ;
if ( MDR )
  s = s + "MDR=" ;
if ( MAR )
  s = s + "MAR=" ;

// decode the B-bus bits
switch ( B )
{
case B_MDR: b = "MDR" ; break ;
case B_PC: b = "PC" ; break ;
case B_MBR: b = "MBR" ; break ;
case B_MBRU: b = "MBRU" ; break ;
case B_SP: b = "SP" ; break ;
case B_LV: b = "LV" ; break ;
case B_CPP: b = "CPP" ; break ;
case B_TOS: b = "TOS" ; break ;
case B_OPC: b = "OPC" ; break ;
// are the rest of these really no-ops?
case 9: b = "R09" ; break ;
case 10: b = "R0A" ; break ;
case 11: b = "R0B" ; break ;
case 12: b = "R0C" ; break ;
case 13: b = "R0D" ; break ;
case 14: b = "R0E" ; break ;
case 15: b = "R0F" ; break ;
}

// decode the ALU operation
if ( F0 == false && F1 == false ) // a AND b
  {
  if ( ENA )
    {
    if ( ENB )
      {
      if ( INVA )
        if ( INC )
          s = s + "((NOT " + a + ") AND " + b + ")+1";
        else
          s = s + "(NOT " + a + ") AND " + b ;
      else
        if ( INC )
          s = s + "(" + a + " AND " + b + ")+1";
        else
          s = s + a + " AND " + b ;
      }
    else
      {
      if ( INC )
        s = s + "1";
      else
        s = s + "0" ;
      }
    }
  else
    {
    if ( ENB )
      {
      if ( INVA )
        if ( INC )
          s = s + b + "+1";
        else
          s = s + b ;
      else
        if ( INC )
          s = s + "1";
        else
          s = s + "0" ;
      }
    else
      {
      if ( INC )
        s = s + "1";
      else
        s = s + "0" ;
      }
    }
  }
else if ( F0 == false && F1 == true ) // a OR b
  {
  if ( ENA )
    {
    if ( ENB )
      {
      if ( INVA )
        if ( INC )
          s = s + "((NOT " + a + ") OR " + b + ")+1";
        else
          s = s + "(NOT " + a + ") OR " + b ;
      else
        if ( INC )
          s = s + "(" + a + " OR " + b + ")+1";
        else
          s = s + a + " OR " + b ;
      }
    else
      {
      if ( INVA )
        if ( INC )
          s = s + "-" + a ;
        else
          s = s + "NOT " + a ;
      else
        if ( INC )
          s = s + a + "+1";
        else
          s = s + a ;
      }
    }
  else
    {
    if ( ENB )
      {
      if ( INVA )
        if ( INC )
          s = s + "0";
        else
          s = s + "-1" ;
      else
        if ( INC )
          s = s + b + "+1";
        else
          s = s + b ;
      }
    else
      {
      if ( INVA )
        if ( INC )
          s = s + "0";
        else
          s = s + "-1" ;
      else
        if ( INC )
          s = s + "1";
        else
          s = s + "0" ;
      }
    }
  }
else if ( F0 == true && F1 == false ) // NOT b
  {
  if ( ENB )
    if ( INC )
      s = s + "-" + b ;
    else
      s = s + "NOT " + b ;
  else
    if ( INC )
      s = s + "-1" ;
    else
      s = s + "0" ;
  }
else if ( F0 == true && F1 == true ) // a + b
  {
  if ( ENA )
    {
    if ( ENB )
      {
      if ( INVA )
        if ( INC )
          s = s + b + "-" + a ;
        else
          s = s + b + "-" + a + "-1" ;
      else
        if ( INC )
          s = s + a + "+" + b + "+1" ;
        else
          s = s + a + "+" + b ;
      }
    else
      {
      if ( INVA )
        if ( INC )
          s = s + "-" + a ;
        else
          s = s + "-" + a + "-1" ;
      else
        if ( INC )
          s = s + a + "+1" ;
        else
          s = s + a ;
      }
    }
  else
    {
    if ( ENB )
      {
      if ( INVA )
        if ( INC )
          s = s + b ;
        else
          s = s + b + "-1" ;
      else
        if ( INC )
          s = s + b + "+1" ;
        else
          s = s + b ;
      }
    else
      {
      if ( INVA )
        if ( INC )
          s = s + "0" ;
        else
          s = s + "-1" ;
      else
        if ( INC )
          s = s + "1" ;
        else
          s = s + "0" ;
      }
    }
  }

// decode the shifter operation
if ( SRA1 )
  s = s + ">>1" ;
if ( SLL8 )
  s = s + "<<8" ;


// decode the mem bits
if ( WRITE )
  s = s + ";wr" ;
if ( READ )
  s = s + ";rd" ;
if ( FETCH )
  s = s + ";fetch" ;

// decode the JAM bits/addr
if ( JAMN )
  s = "N=" + s + 
      ";if (N) goto 0x" + Integer.toHexString( NEXT_ADDRESS | 256 ).toUpperCase() + 
      "; else goto 0x" + Integer.toHexString( NEXT_ADDRESS ).toUpperCase() ;
else if ( JAMZ )
  s = "Z=" + s + 
      ";if (Z) goto 0x" + Integer.toHexString( NEXT_ADDRESS | 256 ).toUpperCase() + 
      "; else goto 0x" + Integer.toHexString( NEXT_ADDRESS ).toUpperCase() ;
else if ( JMPC )
  {
  if ( NEXT_ADDRESS == 0 )
     s = s + ";goto (MBR)" ;
  else
     s = s + ";goto (MBR OR 0x" + Integer.toHexString( NEXT_ADDRESS ).toUpperCase() + ")" ;
  }
else
  s = s + ";goto 0x" + Integer.toHexString( NEXT_ADDRESS ).toUpperCase() ;

if ( s.equals( "0" ) )
  s = "nop" ;
else if( s.startsWith( "0;" ) )
  s = s.substring( 2 ) ;

return( s ) ;
}

public static void main( String args[] )
{
Mic1Instruction i = null ;

i = new Mic1Instruction() ;
i.WRITE = true ;
System.out.println( i.toString() ) ;
}

}
