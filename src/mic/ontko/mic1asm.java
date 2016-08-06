package mic.ontko;

/*
*
*  mic1asm.java
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
// public class mic1asm

import java.net.* ;
import java.io.* ;
import java.util.* ;
import java_cup.runtime.* ;

import mic.CompPane;
import mic.MicTranslator;

/**
Reads a text file containing a Mic1 Micro-Assembly Language (MAL) 
program and produces a binary file containing Mic1 instructions, 
one 36-bit instruction per 40-bit word (the lower order 4 bits 
are not used).

@author 

Ray Ontko (<A HREF="mailto:rayo@ontko.com"><I>rayo@ontko.com</I></A>),
Ray Ontko & Co, 
Richmond, Indiana, US
*/

public class mic1asm implements Mic1Constants, MicTranslator
{

  private BufferedInputStream in = null ;
  private DataOutputStream out = null ;
  private CompPane cpane = null;

  public void setCPane(CompPane cpane) {this.cpane = cpane;}
  public void setInputStream(InputStream in) {this.in = (BufferedInputStream)in;}
  public void setOutputStream(OutputStream out) {this.out = (DataOutputStream) out;}


  public static void main( String[] args ) throws Exception
  {
    String infilename = null ;
    String outfilename = null ;
    BufferedInputStream in = null ;
    DataOutputStream out = null ;

    if ( args.length != 2 )
      {
	System.out.println( "usage: java mic1asm <infilename> <outfilename>" ) ;
	return;
      }

    infilename = args[0] ; 
    outfilename = args[1] ;

    in = new BufferedInputStream( 
				 new FileInputStream( new File( infilename ) ) ) ;
    out = new DataOutputStream( 
			       new FileOutputStream( new File( outfilename ) ) ) ;  

    //    assemble( in , out ) ;

    in.close() ;
    out.close() ;
  }

  public void run() throws Exception {
    assemble(cpane, in, out);
    in.close() ;
    out.close() ;
  }

  /**
     assembles a Mic1 Micro-Assembly Language program which it reads 
     from in, and writes to out.
  */
  public static void assemble(CompPane cpane, BufferedInputStream in , DataOutputStream out )
  {

    int t ;
    int c ;
    Reader r = new BufferedReader(new InputStreamReader(in));
    StreamTokenizer st = new StreamTokenizer(r);

    st.resetSyntax() ;
    st.slashSlashComments( true ) ;
    st.whitespaceChars( 0 , ' ' ) ;
    st.eolIsSignificant( true ) ;
    st.wordChars( '.' , '.' ) ;
    st.wordChars( '_' , '_' ) ;
    st.wordChars( 'A' , 'Z' ) ;
    st.wordChars( 'a' , 'z' ) ;
    st.wordChars( '0' , '9' ) ;

    Mic1Scanner.init( st ) ;
    Mic1Parser p = new Mic1Parser() ;
    try
      {
	// write magic number to output file
	out.write(mic1_magic1);
	out.write(mic1_magic2);
	out.write(mic1_magic3);
	out.write(mic1_magic4);

	boolean errorFound = false ;
	Symbol s = p.parse();

	String labels[] = new String[512] ;
	Mic1Parse mic1parse = (Mic1Parse)s.value ;

	// add the labels from the hash table to our labels array to
	// verify that no two labels are assigned to the same address
	Enumeration e = mic1parse.labels.keys() ; 
	while ( e.hasMoreElements() )
	  {
	    String l = (String)e.nextElement() ;
	    Integer address = (Integer)mic1parse.labels.get(l) ;
	    int a = address.intValue() ;
	    // we should check to make sure labels[a] is null
	    // else error: duplicate labels for this address
	    if ( labels[a] == null )
	      labels[a] = l ;
	    else
	      {
		cpane.appendErr( "error: duplicate .label definitions " + 
				    l + " and " + labels[a] + 
				    " cannot share the same target address 0x" + Integer.toHexString(a) ) ;
		errorFound = true ;
	      }
	  }

	// System.out.println( "statementCount = " + mic1parse.statementCount ) ;
	// for( int i = 0 ; i < mic1parse.statementCount ; i++ )
	//   System.out.println( "  " + i + " " + mic1parse.statements[i].label ) ;

	// verify that all statement labels are unique
	for( int i = 0 ; i < mic1parse.statementCount - 1 ; i ++ )
	  for( int j = i + 1 ; j < mic1parse.statementCount ; j ++ )
	    if( mic1parse.statements[i].label.equals(
						     mic1parse.statements[j].label ) )
	      {
		cpane.appendErr( "error: statements " + i + " and " + j +
				    " cannot have the same label " + mic1parse.statements[i].label ) ;
		errorFound = true ;
	      } 

	// give a goto to each statement which does not name its successor
	for( int i = 0 ; i < mic1parse.statementCount ; i ++ )
	  {
	    if( ! ( mic1parse.statements[i].isIf ||
		    mic1parse.statements[i].isGoto ||
		    mic1parse.statements[i].isMultiway ) )
	      {
		if ( i + 1 < mic1parse.statementCount )
		  {
		    mic1parse.statements[i].gotoLabel = mic1parse.statements[i+1].label ;
		    mic1parse.statements[i].isGoto = true ;
		  }
		else
		  {
		    cpane.appendErr( "error: final statement must have goto or if" ) ;
		    errorFound = true ;
		  }
	      }
	  }

	// verify that default statement exists
	if( mic1parse.defaultStatement == null )
	  {
	    cpane.appendErr( "error: missing .default statement" ) ;
	    errorFound = true ;
	  }
	else
	  // verify that default statment has goto
	  if( ! ( mic1parse.defaultStatement.isIf ||
		  mic1parse.defaultStatement.isGoto ||
		  mic1parse.defaultStatement.isMultiway ) )
	    {
	      cpane.appendErr( "error: default statement must have goto or if" ) ;
	      errorFound = true ;
	    }

	// make a pass to assign locations for all the isIf target pairs
	//
	// we should verify that all labels which participate in if statements
	// are always paired similarly (i.e., the "then" and "else" targets must 
	// always match anytime either appears in an if/else).
	int a = 0 ;
	for( int i = 0 ; i < mic1parse.statementCount ; i ++ )
	  {
	    if( mic1parse.statements[i].isIf )
	      {
		boolean pairOk = true ;
		for( int j = i + 1 ; j < mic1parse.statementCount ; j ++ )
		  {
		    if( mic1parse.statements[j].isIf )
		      {
			if( ( mic1parse.statements[i].gotoLabel.equals( 
								       mic1parse.statements[j].gotoLabel ) &&
			      ! mic1parse.statements[i].elseLabel.equals(
									 mic1parse.statements[j].elseLabel ) ) ||
			    ( mic1parse.statements[i].elseLabel.equals( 
								       mic1parse.statements[j].elseLabel ) &&
			      ! mic1parse.statements[i].gotoLabel.equals(
									 mic1parse.statements[j].gotoLabel ) ) )
			  {
			    cpane.appendErr( "error: if statements " + i + " and " + j + 
						" contain mismatched goto targets" ) ;
			    errorFound = true ;
			    pairOk = false ;
			  }
			else if( mic1parse.statements[i].gotoLabel.equals(
									  mic1parse.statements[j].elseLabel ) ||
				 mic1parse.statements[i].elseLabel.equals(
									  mic1parse.statements[j].gotoLabel ) )
			  {
			    cpane.appendErr( "error: if statements " + i + " and " + j + 
						" cannot both use a label as a 'then' and 'else' goto target" ) ;
			    errorFound = true ;
			    pairOk = false ;
			  }
		      }
		    // also, check against the default statement if it has an if
		    if( mic1parse.defaultStatement.isIf )
		      {
			if( ( mic1parse.statements[i].gotoLabel.equals( 
								       mic1parse.defaultStatement.gotoLabel ) &&
			      ! mic1parse.statements[i].elseLabel.equals(
									 mic1parse.defaultStatement.elseLabel ) ) ||
			    ( mic1parse.statements[i].elseLabel.equals( 
								       mic1parse.defaultStatement.elseLabel ) &&
			      ! mic1parse.statements[i].gotoLabel.equals(
									 mic1parse.defaultStatement.gotoLabel ) ) )
			  {
			    cpane.appendErr( "error: if statements " + i + 
						" and the .default statement" + 
						" contain mismatched goto targets" ) ;
			    errorFound = true ;
			    pairOk = false ;
			  }
			else if( mic1parse.statements[i].gotoLabel.equals(
									  mic1parse.defaultStatement.elseLabel ) ||
				 mic1parse.statements[i].elseLabel.equals(
									  mic1parse.defaultStatement.gotoLabel ) )
			  {
			    cpane.appendErr( "error: if statements " + i + 
						" and the .default statement" + 
						" cannot both use a label as a 'then' and 'else' goto target" ) ;
			    errorFound = true ;
			    pairOk = false ;
			  }
		      }
		  }
		if( pairOk )
		  {
		    // look for the elseLabel to see if it has an address
		    Integer address = (Integer)mic1parse.labels.get( 
								    mic1parse.statements[i].elseLabel ) ;
		    // if the elseLabel does not have a value (in the labels hash table), 
		    // find the next available pair of addresses (by looking in the array),
		    // and give both the gotoLabel and the elseLabel values
		    // by assigning them in the labels array and labels hash table.
		    // if there are no free pairs, error
		    if( address == null )
		      {
			// find the first available address
			while( a < 256 )
			  {
			    if( labels[a] == null && labels[a+256] == null )
			      break ;
			    a ++ ;
			  }
			if( a < 256 )
			  {
			    address = new Integer(a) ;
			    mic1parse.add_label( mic1parse.statements[i].gotoLabel , 
						 new Integer(a+256) ) ;
			    labels[a+256] = mic1parse.statements[i].gotoLabel ;
			    mic1parse.add_label( mic1parse.statements[i].elseLabel , 
						 address ) ;
			    labels[a] = mic1parse.statements[i].elseLabel ;
			  }
			else
			  {
			    cpane.appendErr( 
					       "error: no room for if/else labels in statement " + i ) ;
			    errorFound = true ;
			  }
		      }
		    // update the NEXT_ADDRESS field for the statement.
		    if( ! errorFound )
		      mic1parse.statements[i].i.NEXT_ADDRESS = address.intValue() ;
		  }
	      }
	  }
	if( mic1parse.defaultStatement.isIf )
	  {
	    // look for the elseLabel to see if it has an address
	    Integer address = (Integer)mic1parse.labels.get( 
							    mic1parse.defaultStatement.elseLabel ) ;
	    // if the elseLabel does not have a value (in the labels hash table), 
	    // find the next available pair of addresses (by looking in the array),
	    // and give both the gotoLabel and the elseLabel values
	    // by assigning them in the labels array and labels hash table.
	    // if there are no free pairs, error
	    if( address == null )
	      {
		// find the first available address
		while( a < 256 )
		  {
		    if( labels[a] == null && labels[a+256] == null )
		      break ;
		    a ++ ;
		  }
		if( a < 256 )
		  {
		    address = new Integer(a) ;
		    mic1parse.add_label( mic1parse.defaultStatement.gotoLabel , 
					 new Integer(a+256) ) ;
		    labels[a+256] = mic1parse.defaultStatement.gotoLabel ;
		    mic1parse.add_label( mic1parse.defaultStatement.elseLabel , 
					 address ) ;
		    labels[a] = mic1parse.defaultStatement.elseLabel ;
		  }
		else
		  {
		    cpane.appendErr( 
				       "error: no room for if/else labels in .default statement " ) ;
		    errorFound = true ;
		  }
	      }
	    // update the NEXT_ADDRESS field for the statement.
	    if( ! errorFound )
	      mic1parse.defaultStatement.i.NEXT_ADDRESS = address.intValue() ;
	  }

	a = 0 ;
	// make a pass to assign locations for all the isGoto targets
	for( int i = 0 ; i < mic1parse.statementCount ; i ++ )
	  {
	    if( mic1parse.statements[i].isGoto )
	      {
		// check to see if the label already has an address. 
		Integer address = (Integer)mic1parse.labels.get( 
								mic1parse.statements[i].gotoLabel ) ;
		// If not, find the next available free address and assign
		// the value to the label in the array and the hash table.
		if( address == null )
		  {
		    for( ; labels[a] != null && a < 512 ; a ++ )
		      // find the first available address
		      ;
		    if( a < 512 )
		      {
			address = new Integer(a) ;
			mic1parse.add_label( mic1parse.statements[i].gotoLabel ,
					     address ) ;
			labels[a] = mic1parse.statements[i].gotoLabel ;
		      }
		    else
		      {
			cpane.appendErr( "error: no room for label in statement " + i ) ;
			errorFound = true ;
		      }
		  }
		// update the NEXT_ADDRESS field for the statement.
		if( ! errorFound )
		  mic1parse.statements[i].i.NEXT_ADDRESS = address.intValue() ;
	      }
	  }
	// fix the label in the default statement if it is a goto
	if( mic1parse.defaultStatement.isGoto )
	  {
	    // check to see if the label already has an address. 
	    Integer address = (Integer)mic1parse.labels.get( 
							    mic1parse.defaultStatement.gotoLabel ) ;
	    // If not, find the next available free address and assign
	    // the value to the label in the array and the hash table.
	    if( address == null )
	      {
		for( ; labels[a] != null && a < 512 ; a ++ )
		  // find the first available address
		  ;
		if( a < 512 )
		  {
		    address = new Integer(a) ;
		    mic1parse.add_label( mic1parse.defaultStatement.gotoLabel ,
					 address ) ;
		    labels[a] = mic1parse.defaultStatement.gotoLabel ;
		  }
		else
		  {
		    cpane.appendErr( "error: no room for lable in default statement" ) ;
		    errorFound = true ;
		  }
	      }
	    // update the NEXT_ADDRESS field for the statement.
	    if( ! errorFound )
	      mic1parse.defaultStatement.i.NEXT_ADDRESS = address.intValue() ;
	  }
 
	// by now, all labels which are used by statements are assigned to 
	// addresses. the labels are in the hash table with their assigned addresses.
	// we can use it to sequence through the statements and place them
	// into the control store.
	Mic1Instruction controlstore[] = new Mic1Instruction[512] ;
	if( ! errorFound )
	  {
	    for( int i = 0 ; i < mic1parse.statementCount ; i ++ )
	      {
		String label = mic1parse.statements[i].label ;
		int address = ((Integer)mic1parse.labels.get( label )).intValue() ;
		if( controlstore[address] == null )
		  controlstore[address] = mic1parse.statements[i].i ;
		else
		  {
		    // it might be better to check for uniqueness in a different pass
		    cpane.appendErr( "internal error: statement " + i + 
					"and some other statement are both mapped to address 0x" + 
					Integer.toHexString(address) ) ;
		    errorFound = true ;
		  }
	      }
	    // fill any empty spaces with the .default instruction
	    for( int i = 0 ; i < 512 ; i ++ )
	      if( controlstore[i] == null )
		controlstore[i] = mic1parse.defaultStatement.i ;
	  }

	// write the results to an output file
	if( ! errorFound )
	  {
	    for( int i = 0 ; i < 512 ; i ++ )
	      {
		// System.out.println( "0x" + Integer.toHexString(i) + ": " +
		//   controlstore[i].toString() ) ;
		controlstore[i].write(out) ;
	      }
	  }
	else
	  {
	    cpane.appendErr( "errors found; no output file written" ) ;
	  }

      }
    catch (Exception e) 
      {
	cpane.appendErr( "error: fatal syntax error at line " + st.lineno() ) ;
	cpane.appendErr( e.toString() );
	/* do cleanup here - - possibly rethrow e */
      } 

  }

}
