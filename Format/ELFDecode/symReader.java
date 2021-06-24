package Format.ELFDecode;

import swingIO.*;
import swingIO.tree.*;

public class symReader extends Data implements sec
{
  public Descriptor[] read() throws java.io.IOException
  {
    java.util.ArrayList<Descriptor> sym = new java.util.ArrayList<Descriptor>();

    //We only use one descriptor for each array as it is only disassemble locations.

    int ref = 0, end = 0; Descriptor loc, name;
    long name_loc = 0, strTable = 0, t = 0;

    //WE read all link library sections.

    JDNode sects = sections[5], curSec = null;

    for( int i = 0, size = sects.getChildCount(); i < size; i++ )
    {
      curSec = (JDNode)sects.getChildAt(i);

      //In this case we must know the location to the string table used in the link library section.

      if( curSec.getID().equals("Lib") ) { strTable = dynStr; }

      //Setup descriptor.

      file.seekV(curSec.getArg(1)); loc = new Descriptor( file, true );

      //Number of address locations.

      end = (int)( curSec.getArg(2) / ( is64Bit ? 24 : 16 ) );

      //Setup section node.

      curSec.setUserObject( ((String)curSec.getUserObject()).replace(".h","") );

      curSec.setArgs( new long[]{ 4, ref } ); sym.add( loc ); ref += 1;

      //Read locations.

      for( int i2 = 0; i2 < end; i2++ )
      {
        if( is64Bit )
        {
          loc.Array("Symbol el " + i2 + "", 24 );

          if( isLittle )
          {
            loc.LUINT32("Name table index"); name_loc = (int)loc.value;
            loc.UINT8("Symbol type and binding");
            loc.UINT8("Symbol visibility");
            loc.LUINT16("Section Index");
            loc.LUINT64("Symbol Address, or value");
            loc.LUINT64("Symbol size");
          }
          else
          {
            loc.UINT32("Name table index"); name_loc = (int)loc.value;
            loc.UINT8("Symbol type and binding");
            loc.UINT8("Symbol visibility");
            loc.UINT16("Section Index");
            loc.UINT64("Symbol Address, or value");
            loc.UINT64("Symbol size");
          }
        }
        else
        {
          loc.Array("Symbol " + i2 + "", 16 );

          if( isLittle )
          {
            loc.LUINT32("Name table index"); name_loc = (int)loc.value;
            loc.LUINT32("Symbol Address, or value");
            loc.LUINT32("Symbol size");
            loc.UINT8("Symbol type and binding");
            loc.UINT8("Symbol visibility");
            loc.LUINT16("Section Index");
          }
          else
          {
            loc.UINT32("Name table index"); name_loc = (int)loc.value;
            loc.UINT32("Symbol Address, or value");
            loc.UINT32("Symbol size");
            loc.UINT8("Symbol type and binding");
            loc.UINT8("Symbol visibility");
            loc.UINT16("Section Index");
          }
        }
        
        if( name_loc > 0)
        {
          t = file.getVirtualPointer();
          file.seekV( name_loc + strTable ); name = new Descriptor(file, true); name.String8("Symbol name.", (byte)0x00); sym.add( name );
          curSec.add( new JDNode( name.value + " #" + i2 + ".h", new long[]{ 4, ref } ) ); ref += 1;
          file.seekV( t );
        }
      }
    }

    return( sym.toArray( new Descriptor[ sym.size() ] ) );
  }
}
