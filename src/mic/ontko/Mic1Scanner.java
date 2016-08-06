package mic.ontko;
/*
*
*  Mic1Scanner.java
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

// Simple mic1asm scanner class

import java_cup.runtime.*;
import java.io.* ;

public class Mic1Scanner {

private static StreamTokenizer st = null ; 

public static void init( StreamTokenizer st )
{
Mic1Scanner.st = st ;
}

public static Symbol next_token() throws java.io.IOException
{
int t = st.nextToken() ;
if ( t ==  StreamTokenizer.TT_WORD )
  {
  // System.out.println( "token = " + st.sval ) ;
  // we can replace much of the following with a hash table if desired
  if ( st.sval.equals( "0" ) ) return new Symbol(Mic1Symbol.ZERO) ;
  if ( st.sval.equals( "1" ) ) return new Symbol(Mic1Symbol.ONE) ;
  if ( st.sval.equals( "8" ) ) return new Symbol(Mic1Symbol.EIGHT) ;
  if ( st.sval.equals( "rd" ) ) return new Symbol(Mic1Symbol.rd) ;
  if ( st.sval.equals( "wr" ) ) return new Symbol(Mic1Symbol.wr) ;
  if ( st.sval.equals( "fetch" ) ) return new Symbol(Mic1Symbol.fetch) ;
  if ( st.sval.equals( "if" ) ) return new Symbol(Mic1Symbol.IF) ;
  if ( st.sval.equals( "else" ) ) return new Symbol(Mic1Symbol.ELSE) ;
  if ( st.sval.equals( "goto" ) ) return new Symbol(Mic1Symbol.GOTO) ;
  if ( st.sval.equals( "nop" ) ) return new Symbol(Mic1Symbol.NOP) ;
  if ( st.sval.equals( "N" ) ) return new Symbol(Mic1Symbol.N) ;
  if ( st.sval.equals( "Z" ) ) return new Symbol(Mic1Symbol.Z) ;
  if ( st.sval.equals( "MBR" ) ) return new Symbol(Mic1Symbol.MBR) ;
  if ( st.sval.equals( "MAR" ) ) return new Symbol(Mic1Symbol.MAR) ;
  if ( st.sval.equals( "MDR" ) ) return new Symbol(Mic1Symbol.MDR) ;
  if ( st.sval.equals( "PC" ) ) return new Symbol(Mic1Symbol.PC) ;
  if ( st.sval.equals( "SP" ) ) return new Symbol(Mic1Symbol.SP) ;
  if ( st.sval.equals( "LV" ) ) return new Symbol(Mic1Symbol.LV) ;
  if ( st.sval.equals( "CPP" ) ) return new Symbol(Mic1Symbol.CPP) ;
  if ( st.sval.equals( "TOS" ) ) return new Symbol(Mic1Symbol.TOS) ;
  if ( st.sval.equals( "OPC" ) ) return new Symbol(Mic1Symbol.OPC) ;
  if ( st.sval.equals( "H" ) ) return new Symbol(Mic1Symbol.H) ;
  if ( st.sval.equals( "MBRU" ) ) return new Symbol(Mic1Symbol.MBRU) ;
  if ( st.sval.equals( "OR" ) ) return new Symbol(Mic1Symbol.OR) ;
  if ( st.sval.equals( "AND" ) ) return new Symbol(Mic1Symbol.AND) ;
  if ( st.sval.equals( "NOT" ) ) return new Symbol(Mic1Symbol.NOT) ;
  if ( st.sval.equals( ".label" ) ) return new Symbol(Mic1Symbol.DOTLABEL) ;
  if ( st.sval.equals( ".default" ) ) return new Symbol(Mic1Symbol.DOTDEFAULT) ;
  if ( st.sval.startsWith( "0x" ) || st.sval.startsWith( "0X" ) )
     {
     return new Symbol(Mic1Symbol.address, Integer.valueOf(st.sval.substring(2),16));
     }
  // else
  return new Symbol(Mic1Symbol.label,new String(st.sval));
  }
if ( t == StreamTokenizer.TT_EOL ) return new Symbol(Mic1Symbol.EOL) ;
if ( t == StreamTokenizer.TT_EOF ) return new Symbol(Mic1Symbol.EOF) ;
// System.out.println( "char = " + t ) ;
switch ( (char)t )
  {
  case ';': return new Symbol(Mic1Symbol.SEMI);
  case '+': return new Symbol(Mic1Symbol.PLUS);
  case '-': return new Symbol(Mic1Symbol.MINUS);
  case '<': return new Symbol(Mic1Symbol.LESSTHAN);
  case '=': return new Symbol(Mic1Symbol.EQUALS);
  case '>': return new Symbol(Mic1Symbol.GREATERTHAN);
  case '(': return new Symbol(Mic1Symbol.LPAREN);
  case ')': return new Symbol(Mic1Symbol.RPAREN);
  default :
     System.out.println( st.lineno() + ": unexpected character " + t ) ; 
     break ;
  }
return new Symbol(Mic1Symbol.error) ;
}

}
