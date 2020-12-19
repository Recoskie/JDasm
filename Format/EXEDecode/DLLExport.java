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

    Data = new Descriptor( b, true );

    Data.LUINT32("Characteristics");
    Data.LUINT32("Time Date Stamp");
    Data.LUINT16("Major Version");
    Data.LUINT16("Minor Version");
    Data.LUINT32("Export Name location"); name = ( (Integer)Data.value ).longValue() + imageBase;
    Data.LUINT32("Base");
    Data.LUINT32("Number Of Functions"); asize = ((Integer)Data.value).intValue(); loc = new long[ asize ];
    Data.LUINT32("Number Of Names"); size = ((Integer)Data.value).intValue();
    Data.LUINT32("Address Of Functions"); address_List = ((Integer)Data.value).longValue() + imageBase;
    Data.LUINT32("Address Of Names"); name_List = ((Integer)Data.value).longValue() + imageBase;
    Data.LUINT32("Address Of Ordinals"); ordinal_List = ((Integer)Data.value).longValue() + imageBase;

    des.add( Data );

    //Read the export name.

    b.seekV( name ); Str = new Descriptor( b, true );

    Str.String8("Export Name.", ((byte)0x00));

    Methods = new DefaultMutableTreeNode( Str.value + "#E,1" ); des.add( Str );

    Methods.add( new DefaultMutableTreeNode( "Method Address list.h#E,2" ) );

    //The location of each export method.
    
    b.seekV( address_List ); Data = new Descriptor( b, true ); des.add( Data );

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

    Methods.add( new DefaultMutableTreeNode( "Method Address list Order.h#E," + des.size() ) ); des.add( Data );

    //Names. Our sorted location list should now locate to each method.
    
    b.seekV( name_List ); Data = new Descriptor( b, true );

    Methods.add( new DefaultMutableTreeNode( "Method name Location.h#E," + des.size() ) ); des.add( Data );

    for( int i = 0; i < size; i++ )
    {
      Data.Array( "Array Element " + i + "", 4 );
      Data.LUINT32("Method name Location."); name_List = ((Integer)Data.value).longValue() + imageBase;

      t = b.getVirtualPointer(); b.seekV( name_List );

      Str = new Descriptor( b, true ); des.add( Str );

      Str.String8("Name.", ((byte)0x00)); b.seekV( t );

      Method_loc = new DefaultMutableTreeNode( Str.value + "#E," + des.size() + "" );

      Method_loc.add( new DefaultMutableTreeNode( "Method location#O," + loc[i] + "" ) );

      Methods.add( Method_loc );

      des.add( Str );
    }

    Export.add( Methods );

    return( des.toArray( new Descriptor[ des.size() ] ) );
  }
}
