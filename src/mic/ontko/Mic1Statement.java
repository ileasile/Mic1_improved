package mic.ontko;
/*
*
*  Mic1Statement.java
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

// this is used by Mic1Parse and Mic1Parser

public class Mic1Statement
{
public String label = null ;
public Mic1Instruction i = new Mic1Instruction() ;
public boolean isGoto = false ;
public boolean isIf = false ;
public boolean isMultiway = false ;
public String gotoLabel = null ; // used only if isGoto or isIf
public String elseLabel = null ; // used only if isIf
}
