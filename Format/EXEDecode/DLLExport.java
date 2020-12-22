package Format.EXEDecode;
import java.io.*;
import javax.swing.tree.*;

import dataTools.*;
import RandomAccessFileV.*;
import WindowCompoents.*;

public class DLLExport extends Data
{
  public Descriptor[] LoadExport( DefaultMutableTreeNode Export, RandomAccessFileV b ) throws IOException
  {
    java.util.LinkedList<Descriptor> des = new java.util.LinkedList<Descriptor>();

    //Descriptors.

    Descriptor Data, Str;

    //Export.

    int size = 0, asize = 0;

    long name_List = 0, ordinal_List = 0, address_List = 0, name = 0, t = 0;

    long[] loc, tloc;

    DefaultMutableTreeNode Methods, Method_loc;

    //Begin reeding, and mapping the export method locations.

    Export.add( new DefaultMutableTreeNode( "Export info.h#E,0" ) );

    Data = new Descriptor( b, true ); Data.setEvent( this::exportInfo );

    Data.LUINT32("Characteristics");
    Data.LUINT32("Time Date Stamp");
    Data.LUINT16("Major Version");
    Data.LUINT16("Minor Version");
    Data.LUINT32("Export Name location"); name = ( (Integer)Data.value ).longValue() + imageBase;
    Data.LUINT32("Base");
    Data.LUINT32("Number Of Functions"); asize = ((Integer)Data.value).intValue(); loc = new long[ asize ];
    Data.LUINT32("Number Of Names, and ordinals."); size = ((Integer)Data.value).intValue();
    Data.LUINT32("Address list location"); address_List = ((Integer)Data.value).longValue() + imageBase;
    Data.LUINT32("Method list location"); name_List = ((Integer)Data.value).longValue() + imageBase;
    Data.LUINT32("Method order location"); ordinal_List = ((Integer)Data.value).longValue() + imageBase;

    des.add( Data );

    //Read the export name.

    b.seekV( name ); Str = new Descriptor( b, true );

    Str.String8("Export Name.", ((byte)0x00));

    Methods = new DefaultMutableTreeNode( Str.value + "#E,1" ); des.add( Str ); Str.setEvent( this::strInfo );

    Methods.add( new DefaultMutableTreeNode( "Address list location.h#E,2" ) );

    //The location of each export method.
    
    b.seekV( address_List ); Data = new Descriptor( b, true ); des.add( Data ); Data.setEvent( this::AlistInfo );

    for( int i = 0; i < asize; i++ )
    {
      Data.Array( "Array Element " + i + "", 4 );
      Data.LUINT32("Method Location."); loc[i] = ((Integer)Data.value).longValue() + imageBase;
    }

    //Names are in alphabetical order. So we use a Data list for which order the methods are arranged in.

    tloc = new long[ size ]; b.seekV( ordinal_List ); Data = new Descriptor( b, true );

    for( int i = 0; i < size; i++ )
    {
      Data.Array( "Array Element " + i + "", 2 );
      Data.LUINT16("Address index"); tloc[((Short)Data.value).intValue()] = loc[i]; 
    }

    loc = tloc; tloc = null;

    Methods.add( new DefaultMutableTreeNode( "Order list location.h#E," + des.size() ) ); des.add( Data ); Data.setEvent( this::OlistInfo );

    //Names. Our sorted location list should now locate to each method.
    
    b.seekV( name_List ); Data = new Descriptor( b, true );

    Methods.insert( new DefaultMutableTreeNode( "Name list location.h#E," + des.size() ), 1 ); des.add( Data ); Data.setEvent( this::MlistInfo );

    for( int i = 0; i < size; i++ )
    {
      Data.Array( "Array Element " + i + "", 4 );
      Data.LUINT32("Method name Location."); name_List = ((Integer)Data.value).longValue() + imageBase;

      t = b.getVirtualPointer(); b.seekV( name_List );

      Str = new Descriptor( b, true ); des.add( Str ); Str.setEvent( this::mstrInfo );

      Str.String8("Name.", ((byte)0x00)); b.seekV( t );

      Method_loc = new DefaultMutableTreeNode( Str.value + "#E," + des.size() + "" );

      Method_loc.add( new DefaultMutableTreeNode( "Method location#Dis," + loc[i] + "" ) );

      Methods.add( Method_loc );

      des.add( Str );
    }

    Export.add( Methods );

    return( des.toArray( new Descriptor[ des.size() ] ) );
  }

  //Detailed description.

  public static final String res = "A section that is reserved, is skipped. So that some day the empty space may be used for something new.";

  public static final String Ver = "Major, and Minor are put together to forum the version number.<br /><br />Example.<br /><br />Major version = 5<br /><br />Minor version = 12<br /><br />Would mean version 5.12V.";

  public static final String[] ExportInfo = new String[] { "<html>Characteristics are reserved, for future use.<br /><br />" + res + "</html>",
    "<html>A date time stamp is in seconds. The seconds are added to the starting date \"Wed Dec 31 7:00:00PM 1969\".<br /><br />" +
    "If the time date stamp is \"37\" in value, then it is plus 37 seconds giving \"Wed Dec 31 7:00:37PM 1969\".</html>",
    "<html>" + Ver + "</html>",
    "<html>" + Ver + "</html>",
    "<html>Location to the export file name.<br /><br />Should be the name of the binary file.</html>",
    "<html>Base is usually 1.<br /><br />If base is larger than one. Then Base is added with the order numbers.<br /><br />For which address is the location of a method name.<br /><br />" +
    "In very rare cases the address list is bigger.<br /><br />The first few addresses are skipped using Base.<br /><br />As some methods can only be imported by ordinal number.</html>",
    "<html>Number of address locations in address list.<br /><br />Can be bigger than named methods.<br /><br />With base set bigger than 1 to skip the first few addresses.<br /><br />" +
    "As some methods can only be imported by ordinal number.</html>",
    "<html>Size of named methods, and order list.</html>",
    "<html>Location to the address list.</html>",
    "<html>Location to the Method list names.</html>",
    "<html>The order each method name is in. Method names, and order list are the same in length.<br /><br />The order number tells us which address to use, for which name in the address list.<br /><br />" +
    "On modern compilers the order values should go from first to last in order.</html>"
  };

  public void exportInfo( int el )
  {
    if( el < 0 )
    {
      WindowCompoents.info( "<html>The export section has a name location that should be the file name.<br /><br />" +
      "Export section uses three lists.<br /><br />The name list, and order list match in length.<br /><br />" +
      "The method names are sorted in alphabetical order.<br /><br />The order list shows the original order before sorting the names.<br /><br />" +
      "If method 5 moved to the start of the names list. The first value in the order list then would be 5.<br /><br />" +
      "This would mean the fifth address in the address list is then the methods location.<br /><br />" +
      "Today compilers sort both the address list, and method names.<br /><br >So the order list generally goes in order from 0 to last address.</html>" );
    }
    else
    {
      WindowCompoents.info( ExportInfo[ el ] );
    }
  }

  public void AlistInfo( int el )
  {
    WindowCompoents.info( "<html>Location to each method.<br /><br />Method name list might not match the address order.<br /><br />Which is why we have both a name list, and order list.</html>" );
  }

  public void MlistInfo( int el )
  {
    WindowCompoents.info( "<html>The locations to each method name.</html>" );
  }

  public void OlistInfo( int el )
  {
    WindowCompoents.info( "<html>The order each method name is in.<br /><br />Generally goes in order.<br /><br />If addresses are sorted along with the method names in alphabetical order.</html>" );
  }

  public void strInfo( int el )
  {
    WindowCompoents.info( "<html>The export name location. The name ends with code 00 hex.</html>" );
  }

  public void mstrInfo( int el )
  {
    WindowCompoents.info( "<html>Location to the export method name, from the method name list. The name ends with code 00 hex.</html>" );
  }
}
