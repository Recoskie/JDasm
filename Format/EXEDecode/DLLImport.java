package Format.EXEDecode;
import java.io.*;
import javax.swing.tree.*;

import dataTools.*;
import RandomAccessFileV.*;
import WindowCompoents.*;

public class DLLImport extends Data
{
  public Descriptor[] LoadDLLImport( RandomAccessFileV b, DefaultMutableTreeNode IMPORT ) throws IOException
  {
    //get the physical address to data directory array links to dll import table

    java.util.LinkedList<Descriptor> des = new java.util.LinkedList<Descriptor>();

    System.out.println( "DLL RVA ARRAY POSITION " + b.getVirtualPointer() + "" );

    //for dll names, and function list.

    IMPORT.add( new DefaultMutableTreeNode( "DLL IMPORT ARRAY DECODE.H" ) );

    Descriptor DLLArray = new Descriptor( b, true );
    Descriptor FuncArray;

    int d1 = 0, d2 = 0, d3 = 0, d4 = 0, d5 = 1, DLLS = 0, code = 0;

    long t = 0, t2 = 0;

    String o = "";

    long pos= 1;

    DefaultMutableTreeNode DLLFunc;

    while( ( d1 | d2 | d3 | d4 | d5 ) != 0 )
    {
      DLLArray.Array( "Array Element " + DLLS + "", 20 );
      DLLArray.LUINT32("Original Array DLL Functions"); d1 = ((Integer)DLLArray.value).intValue(); //Location to function list.
      DLLArray.LUINT32("Time Date Stamp"); d2 = ((Integer)DLLArray.value).intValue();
      DLLArray.LUINT32("Forward Chain"); d3 = ((Integer)DLLArray.value).intValue();
      DLLArray.LUINT32("DLL Name Location"); d4 = ((Integer)DLLArray.value).intValue(); //The name of the library.
      DLLArray.LUINT32("DLL Functions"); d5 = ((Integer)DLLArray.value).intValue();

      //DLL Name.

      t = b.getVirtualPointer(); b.seekV( d4 + imageBase );

      //Read the name.

      o = ""; code = 1; while( code != 0 ){ code = b.read(); if( code != 0 ) { o += (char)code; } }

      //Load Function list.

      DLLFunc = new DefaultMutableTreeNode( o + "#" + ( d4  + imageBase ) );
      
      if( ( d1 | d2 | d3 | d4 | d5 ) != 0 )
      {
        //Function list location.

        b.seekV( d1 + imageBase ); FuncArray = new Descriptor( b, true );

        DLLFunc.add( new DefaultMutableTreeNode( "Function Array Decode.H#" + ( DLLS ) ) );
        
        d1 = 0; pos = 1;

        while( pos != 0 )
        {
          //Function name location.

          FuncArray.Array( "Array Element " + d1 + "", 8 );

          if( is64bit ) { FuncArray.LUINT64("Location to ASCII Function Import Name"); pos = ((Long)FuncArray.value).longValue(); }
          else { FuncArray.LUINT32("Location to ASCII Function Import Name"); pos = ((Integer)FuncArray.value).intValue(); }
          
          d1 += 1;

          if( pos != 0 )
          {
            t2 = b.getVirtualPointer();

            //Read Hint ID, and Function name.

            b.seekV( pos + imageBase ); b.read(2); o = ""; code = 1; while( code != 0 ){ code = b.read(); if( code != 0 ) { o += (char)code; } }

            DLLFunc.add( new DefaultMutableTreeNode( o + "().dll#"+ ( pos  + imageBase ) ) );

            b.seekV( t2 );
          }

          //If past 60 methods/functions then something really went wrong.

          if( d1 > 60 ){ break; }
        }

        des.add( FuncArray ); IMPORT.add( DLLFunc );
      }
      
      DLLS++; b.seekV(t);

      //If 60 DLL's then something really went wrong.
      
      if( DLLS > 60 ){ break; }
    }

    DLLArray.setEvent( this::arrayInfo ); des.add( DLLArray );
    
    
    return( des.toArray( new Descriptor[ des.size() ] ) );
  }

  //Detailed description DLL.

  public static final String[] Arrayinfo = new String[] { "<html>Array element consisting of A DLL name, and tow Lists for which methods.</html>",
    "<html></html>",
    "<html></html>",
    "<html></html>",
    "<html></html>",
    "<html></html>",
    "<html></html>"
  };

  public void arrayInfo( int el )
  {
    WindowCompoents.info( Arrayinfo[ el % 6 ] );
  }
}
