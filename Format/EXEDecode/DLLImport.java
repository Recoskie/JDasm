package Format.EXEDecode;
import java.io.*;
import javax.swing.tree.*;

import dataTools.*;
import RandomAccessFileV.*;
import WindowCompoents.*;

public class DLLImport extends Data
{
  public Descriptor[] LoadDLLImport( DefaultMutableTreeNode IMPORT, RandomAccessFileV b ) throws IOException
  {
    //get the physical address to data directory array links to dll import table

    java.util.LinkedList<Descriptor> des = new java.util.LinkedList<Descriptor>();

    //for dll names, and function list.

    IMPORT.add( new DefaultMutableTreeNode( "DLL Import Array Decode.h#D,0" ) );

    Descriptor DLLArray = new Descriptor( b, true );
    Descriptor DLLName, FuncArray1, FuncArray2, Method;

    int d1 = 0, d2 = 0, d3 = 0, d4 = 0, d5 = 1, ref = 1, dllEl = 0;

    long t = 0, t2 = 0, pos = 1;

    DefaultMutableTreeNode DLLFunc;

    while( ( d1 | d2 | d3 | d4 | d5 ) != 0 )
    {
      //Note that there are two lists of import functions.
      //The two lists should match. If not then the import table is corrupted.

      DLLArray.Array( "Array Element " + dllEl + "", 20 );
      DLLArray.LUINT32("DLL Array Functions Location 1"); d1 = ((Integer)DLLArray.value).intValue(); //Location to function list.
      DLLArray.LUINT32("Time Date Stamp"); d2 = ((Integer)DLLArray.value).intValue();
      DLLArray.LUINT32("Forward Chain"); d3 = ((Integer)DLLArray.value).intValue();
      DLLArray.LUINT32("DLL Name Location"); d4 = ((Integer)DLLArray.value).intValue(); //The name of the library.
      DLLArray.LUINT32("DLL Array Functions Location 2"); d5 = ((Integer)DLLArray.value).intValue(); //Location to function list.

      //DLL Name.

      t = b.getVirtualPointer(); b.seekV( d4 + imageBase );

      //Read the name.

      DLLName = new Descriptor( b, true ); DLLName.String8("DLL Name", (byte)0x00 ); DLLName.setEvent( this::dllInfo );

      //Load the two Function list.

      DLLFunc = new DefaultMutableTreeNode( DLLName.value + "#D," + ref ); des.add( DLLName ); ref += 1;
      
      if( ( d1 | d2 | d3 | d4 | d5 ) != 0 )
      {
        //Function list First location.

        b.seekV( d1 + imageBase ); FuncArray1 = new Descriptor( b, true );
        
        d1 = 0; pos = 1;

        while( pos != 0 )
        {
          //Function locations list 1.

          FuncArray1.Array( "Array Element " + d1 + "", is64bit ? 8 : 4 );

          if( is64bit ) { FuncArray1.LINT64("Import Name Location, or Index"); pos = ((Long)FuncArray1.value).longValue(); }
          else { FuncArray1.LINT32("Import Name Location, or Index"); pos = ((Integer)FuncArray1.value).intValue(); }
          
          d1 += 1;
        }

        //Function location list 2.

        b.seekV( d5 + imageBase ); FuncArray2 = new Descriptor( b, true );
        
        d1 = 0; pos = 1;

        while( pos != 0 )
        {
          //Function name location.

          FuncArray2.Array( "Array Element " + d1 + "", is64bit ? 8 : 4 );

          if( is64bit ) { FuncArray2.LINT64("Import Name Location, or Index"); pos = ((Long)FuncArray2.value).longValue(); }
          else { FuncArray2.LINT32("Import Name Location, or Index"); pos = ((Integer)FuncArray2.value).intValue(); }
          
          d1 += 1;

          //If pos is < 0 then it is a import using export address list number.

          if( pos < 0 )
          {
            DLLFunc.add( new DefaultMutableTreeNode( "No_Name() #" + ( pos & 0xFFFF ) + ".dll#" ) );
          }

          //Else grater than 0, and not zero. Then it is a named import method. 

          else if ( pos != 0 )
          {
            t2 = b.getVirtualPointer();

            //Read HInit ID, and Function name.

            b.seekV( pos + imageBase ); Method = new Descriptor( b, true ); Method.LUINT16("HInit"); Method.String8( "Method name", (byte)0x00 );
            
            Method.setEvent( this::methodInfo ); des.add( Method );

            DLLFunc.add( new DefaultMutableTreeNode( Method.value + "().dll#D,"+ ref ) ); ref += 1; b.seekV(t2);
          }
        }

        DLLFunc.insert( new DefaultMutableTreeNode( "Function Array Decode 1.h#D," + ref ), 0 );

        FuncArray1.setEvent( this::funcInfo ); des.add( FuncArray1 ); ref += 1;
        
        DLLFunc.insert( new DefaultMutableTreeNode( "Function Array Decode 2.h#D," + ref ), 1 );
        
        FuncArray2.setEvent( this::funcInfo ); des.add( FuncArray2 ); ref += 1;

        IMPORT.add( DLLFunc );
      }
      
      dllEl++; b.seekV(t);

      //If 60 DLL's then something really went wrong.
      
      if( dllEl > 60 ){ break; }
    }

    DLLArray.setEvent( this::arrayInfo ); des.add( 0, DLLArray );
    
    return( des.toArray( new Descriptor[ des.size() ] ) );
  }

  //Detailed description DLL.

  public static final String ListInfo = "<html>The location to a list, of which methods to import from the DLL export table.<br /><br />" + 
    "There are two lists That are in different locations, but should locate to the same method names.</html>";

  public static final String[] Arrayinfo = new String[] { "<html>Array elements consisting of A DLL name location, and tow List locations.<br /><br />" + 
    "Two lists are used, for which methods to import from the DLL.<br /><br />The lists should match. If they do not, then the import table is corrupted.<br /><br />" +
    "The first Element that has no locations, and is all zeros is the end of the DLL import table.</html>",
    ListInfo,
    "<html>A date time stamp is in seconds. The seconds are added to the starting date \"Wed Dec 31 7:00:00PM 1969\".<br /><br />" +
    "If the time date stamp is \"37\" in value, then it is plus 37 seconds giving \"Wed Dec 31 7:00:37PM 1969\".</html>",
    "<html>Forward Chain is a list of methods that must be loaded from the DLL file before the import methods may be usable.<br /><br />" +
    "This is only if the method we want to use depends on other methods. This is all zeros if not used.</html>",
    "<html>The location of the DLL name to start importing methods from it's export table.</html>",
    ListInfo
  };

  public void arrayInfo( int el )
  {
    el = el < 0 ? 0 : el; WindowCompoents.info( Arrayinfo[ el % 6 ] );
  }

  public void dllInfo( int el )
  {
    WindowCompoents.info( "<html>The DLL name location. The end of each name ends with code 00 hex.<br /><br />Each DLL Array element contains a DLL Name location, and tow method list locations.</html>" );
  }

  public void funcInfo( int el )
  {
    WindowCompoents.info( "<html>Locations to each method name, or by address list index.<br /><br />" +
    "If the location is positive, it then locates to a method name.<br /><br />However, if the location is negative, then the sing is removed.<br /><br />It then imports by address list index.<br /><br />" +
    "The export section has a name list, and address list.<br /><br />Each name specifies which index in the address list.<br /><br />" +
    "This means we can lookup a name, for which address in the address list, or directly use it's address list number.<br /><br />" +
    "The first location that is 0 is the end of the list.<br /><br />Each DLL Array element contains a DLL Name location, and tow method list locations.<br /><br />" +
    "The tow method lists should locate to the same method names, or indexes.<br /><br />If they do not match then there might be something wrong with the import table.</html>" );
  }

  public void methodInfo( int el )
  {
    WindowCompoents.info( "<html>Each method name location contains a HInit value, and then its name. The end of each method name ends with code 00 hex.</html>" );
  }
}
