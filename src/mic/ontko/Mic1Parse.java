package mic.ontko;
/*
*
*  Mic1Parse.java
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

import java.util.* ;

public class Mic1Parse
{
private static int MAX_INSTRUCTIONS = 512 ;

public Hashtable labels = new Hashtable( MAX_INSTRUCTIONS ) ;
public int statementCount = 0 ;
public Mic1Statement statements[] = new Mic1Statement[MAX_INSTRUCTIONS] ;
public Mic1Statement defaultStatement = null ;

public void add_label( String label , Integer address )
{
labels.put( label , address ) ;
}

public void add_statement( Mic1Statement statement )
{
statements[ statementCount ] = statement ;
statementCount ++ ;
}

}
