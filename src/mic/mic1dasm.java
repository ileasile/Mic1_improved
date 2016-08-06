package mic;

import java.io.* ;
import java.util.* ;

public class mic1dasm
{

public static void main ( String args[] ) 
{
String iname = "t.mic1" ;
String oname = "t.mal" ;

System.out.println( "mic1dasm V0.0" ) ;

// get the input file name from the command line
if ( args.length > 0 )
  iname = args[0] ;
// System.out.println( "iname = " + iname ) ;

// get the output file name from the command line
if ( args.length > 1 )
  oname = args[1] ;
// System.out.println( "oname = " + oname ) ;

// open the input file
try
  {
  //#ifdef jdk_1_0
  FileInputStream f = new FileInputStream( iname ) ;
  DataInputStream in = new DataInputStream( f ) ;
  //#else
  // FileReader f = new FileReader( iname ) ;
  // BufferedReader in = new BufferedReader( f ) ;
  //#endif
  try
    {
    int c;
    int magic = 0 ;
    c = in.read( ) ;
    magic = c ;
    c = in.read( ) ;
    magic *= 256 ;
    magic += c ;
    c = in.read( ) ;
    magic *= 256 ;
    magic += c ;
    c = in.read( ) ;
    magic *= 256 ;
    magic += c ;
    if ( magic != 0x12345678 )
      {
      System.out.println( "error: magic number not found" ) ;
      System.out.println( "input file may not contain mic1 microcode" ) ;
      }
    else
      {  
      Mic1Instruction i = new Mic1Instruction() ;
      for( int a = 0 ; i.read( in ) != - 1 ; a ++ )
        {
        System.out.println( "0x" + Integer.toHexString(a) + 
          ": " + i.toString() ) ;
        i = new Mic1Instruction() ;
        }
      }
    }
  catch ( IOException e )
    {
    }

  // close the input file
  try
    {
    in.close() ;
    }
  catch( IOException e )
    {
    }
  }
catch( FileNotFoundException e )
  {
  System.out.println( "error: file not found" ) ;
  }

// assign the physical addresses

// open the output file
// generate the code
// close the output file
}

}
