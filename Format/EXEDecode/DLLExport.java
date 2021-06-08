package Format.EXEDecode;
import java.io.*;
import swingIO.*;
import swingIO.tree.*;

public class DLLExport extends Data implements sec
{
  public Descriptor[] read( JDNode Export ) throws IOException
  {
    java.util.LinkedList<Descriptor> des = new java.util.LinkedList<Descriptor>();

    //Descriptors.

    Descriptor Data, Str;

    //Export.

    int ref = 0;

    int size = 0, asize = 0, base = 0;

    long name_List = 0, ordinal_List = 0, address_List = 0, name = 0, t = 0;

    //Regular address list. May be bigger than the method name list.

    long[] loc;
    
    //Each method name specifies which address to use from the address list.

    int[] order;

    //The order list does not have to use addresses in order from the address list for each named method.
    //Named is set true for each address in address list that is named.

    boolean[] named;

    JDNode Methods, Method_loc;

    //Begin reeding, and mapping the export method locations.

    Export.add( new JDNode( "Export info.h", new long[]{ 2, ref } )); ref += 1;

    Data = new Descriptor( file, true ); Data.setEvent( this::exportInfo );

    Data.LUINT32("Characteristics");
    Data.LUINT32("Time Date Stamp");
    Data.LUINT16("Major Version");
    Data.LUINT16("Minor Version");
    Data.LUINT32("Export Name location"); name = ( (Integer)Data.value ).longValue() + imageBase;
    Data.LUINT32("Base"); base = ((Integer)Data.value).intValue() - 1;
    Data.LUINT32("Number Of Functions");
    
    asize = ((Integer)Data.value).intValue(); //Number of addresses.
    
    //Address list. Can be bigger than the order list and name list.

    loc = new long[ asize ];
    
    //Addresses that are named.

    named = new boolean[ asize ];
    
    Data.LUINT32("Number Of Names, and ordinals.");
    
    //The method list and the order list match in size, for which address to use for each named method.

    size = ((Integer)Data.value).intValue(); order = new int[ size ];

    //The three list that forum the export table.
    
    Data.LUINT32("Address list location"); address_List = ((Integer)Data.value).longValue() + imageBase;
    Data.LUINT32("Method list location"); name_List = ((Integer)Data.value).longValue() + imageBase;
    Data.LUINT32("Method order location"); ordinal_List = ((Integer)Data.value).longValue() + imageBase;

    des.add( Data );

    //Read the export binary file name.

    file.seekV( name ); Str = new Descriptor( file, true );

    Str.String8("Export Name.", ((byte)0x00));

    Methods = new JDNode( Str.value.toString(), new long[]{ 2, ref } ); des.add( Str ); Str.setEvent( this::strInfo ); ref += 1;

    Methods.add( new JDNode( "Address list location.h", new long[]{ 2, ref } )); ref += 1;

    //The Address list.
    
    file.seekV( address_List ); Data = new Descriptor( file, true ); des.add( Data ); Data.setEvent( this::AlistInfo );

    for( int i = 0; i < asize; i++ )
    {
      Data.Array( "Array Element " + i + "", 4 );
      Data.LUINT32("Method Location."); loc[i] = ((Integer)Data.value).longValue() + imageBase;
    }

    //Put the address locations in order to each name using the ordinal list.

    file.seekV( ordinal_List ); Data = new Descriptor( file, true );

    for( int i = 0; i < size; i++ )
    {
      Data.Array( "Array Element " + i + "", 2 );
      Data.LUINT16("Address index");

      order[i] = ( ((Short)Data.value).intValue() & 0xFFFF ); named[ order[i] + base ] = true;
    }

    Methods.add( new JDNode( "Order list location.h", new long[]{ 2, ref } )); des.add( Data ); Data.setEvent( this::OlistInfo ); ref += 1;

    //Names. Our sorted location list should now locate to each named method.
    
    file.seekV( name_List ); Data = new Descriptor( file, true );

    Methods.insert( new JDNode( "Name list location.h", "E", new long[]{ 2, ref } ), 1 ); des.add( Data ); Data.setEvent( this::MlistInfo ); ref += 1;

    //The named methods.

    for( int i = 0; i < size; i++ )
    {
      Data.Array( "Array Element " + i + "", 4 );
      Data.LUINT32("Method name Location."); name_List = ((Integer)Data.value).longValue() + imageBase;

      t = file.getVirtualPointer(); file.seekV( name_List );

      Str = new Descriptor( file, true ); des.add( Str ); Str.setEvent( this::mstrInfo );

      Str.String8("Name.", ((byte)0x00)); file.seekV( t );

      Method_loc = new JDNode( Str.value + "() #" + order[i], new long[]{ 2, ref } ); ref += 1;

      if( loc[ order[i] + base ] > imageBase )
      {
        Method_loc.add( new JDNode( "Goto Location.h", new long[]{ -4, loc[ order[i] + base ], loc[ order[i] + base ] } ) );
        Method_loc.add( new JDNode( "Disassemble Location.h", new long[]{ -1, loc[ order[i] + base ] } ) );
      }
      else
      {
        Method_loc.add( new JDNode( "No Data" ) );
      }

      Methods.add( Method_loc );
    }

    //Methods that are not named.

    for( int i = 0; i < asize; i++ )
    {
      if( !named[i] )
      {
        Method_loc = new JDNode( "No_Name() #" + i + "#" );

        if( loc[i] > imageBase )
        {
          Method_loc.add( new JDNode( "Goto Location.h", new long[]{ -4, loc[i], loc[i] } ) );
          Method_loc.add( new JDNode( "Disassemble Location.h", new long[]{ -1, loc[i] } ) );
        }
        else
        {
          Method_loc.add( new JDNode( "No Data" ) );
        }

        Methods.add( Method_loc );
      }
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
    "<html>Base is usually set one = zero.<br /><br />If base is 3, then it is 2 in value.<br /><br />" +
    "Base is added to the address list. It allows us to skip addresses at the start of the address list.</html>",
    "<html>Number of address locations in address list.<br /><br />Can be bigger than \"named methods, and order list\"." +
    "As some methods can only be imported by number they are in the address list.</html>",
    "<html>Size of named methods, and order list.</html>",
    "<html>Location to the address list.</html>",
    "<html>Location to the Method list names.</html>",
    "<html>The order each method name is in. Method names, and order list are the same in length.<br /><br />The order number tells us which address to use from address list plus Base.<br /><br />" +
    "On modern compilers the order values should go from first to last in order.</html>"
  };

  public void exportInfo( int el )
  {
    if( el < 0 )
    {
      info( "<html>The export section has a name location that should be the file name.<br /><br />" +
      "The Export section uses three lists locations.<br /><br />The name list, and order list match in length.<br /><br />" +
      "The method names are sorted in alphabetical order.<br /><br />The order list is the original order before sorting the names.<br /><br />" +
      "If method 5 moved to the start of the names list. The first value in the order list then would be 5.<br /><br />" +
      "This would mean the fifth address in the address list is then the methods location.<br /><br />" +
      "Today compilers sort both the address list, and method names.<br /><br >So the order list generally goes in order from 0 to last address.</html>" );
    }
    else
    {
      info( ExportInfo[ el ] );
    }
  }

  public void AlistInfo( int el )
  {
    info( "<html>Location to each method.<br /><br />Method name list might not match the address order.<br /><br />Which is why we have both a name list, and order list.</html>" );
  }

  public void MlistInfo( int el )
  {
    info( "<html>The locations to each method name.</html>" );
  }

  public void OlistInfo( int el )
  {
    info( "<html>The order each method name is in plus base.<br /><br />Generally goes in order.<br /><br />If addresses are sorted along with the method names in alphabetical order.</html>" );
  }

  public void strInfo( int el )
  {
    info( "<html>The export name location. The name ends with code 00 hex.</html>" );
  }

  public void mstrInfo( int el )
  {
    info( "<html>Location to the export method name, from the method name list. The name ends with code 00 hex.<br /><br />" +
    "The order number is next to the name.<br /><br />The order number is which address from the address list is the methods location.<br /><br />" +
    "Methods can be imported by both number in the address list, or by name.<br /><br />In some cases some export methods are not given names.</html>" );
  }
}
