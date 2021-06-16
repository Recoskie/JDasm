package Format.ELFDecode;

import java.io.*;
import swingIO.*;
import swingIO.tree.*;

public class libReader extends Data implements sec
{
  private class libInfo { long type = 0, value = 0; }

  public Descriptor[] read() throws IOException
  {
    //get the physical address to data directory array links to dll import table

    java.util.LinkedList<Descriptor> des = new java.util.LinkedList<Descriptor>();
    java.util.LinkedList<libInfo> lib = new java.util.LinkedList<libInfo>();

    Descriptor LInfo, Name;

    //Reference to current data Descriptor.

    int ref = 0;

    //Link information is defined by types.

    libInfo el = new libInfo();

    //Size of lib info.

    int LSize = 0;

    //Base address locations.

    long names_loc = 0;

    //WE read all link library sections.

    JDNode sects = sections[0], curSec = null;

    for( int i = 0, size = sects.getChildCount(); i < size; i++ )
    {
      lib.clear(); el.type = -1; LSize = 0; curSec = (JDNode)sects.getChildAt(i);

      //Setup descriptor.

      file.seekV(0); file.seekV(curSec.getArg(1));
      
      LInfo = new Descriptor( file, true );

      //Setup section node.

      curSec.setUserObject( ((String)curSec.getUserObject()).replace(".h","") );

      curSec.setArgs( new long[]{ 2, ref } ); des.add( LInfo );  ref += 1;

      //Read section.

      while( el.type != 0 )
      {
        el = new libInfo();

        if( is64Bit )
        {
          if( isLittle )
          {
            LInfo.Array("Link info " + LSize + "", 16);
            LInfo.LUINT64("Type"); el.type = (long)LInfo.value;
            LInfo.LUINT64("Value"); el.value = (long)LInfo.value;
          }
          else
          {
            LInfo.Array("Link info " + LSize + "", 16);
            LInfo.UINT64("Type"); el.type = (long)LInfo.value;
            LInfo.UINT64("Value"); el.value = (long)LInfo.value;
          }
        }
        else
        {
          if( isLittle )
          {
            LInfo.Array("Link info " + LSize + "", 8);
            LInfo.LUINT32("Type"); el.type = (long)LInfo.value;
            LInfo.LUINT32("Value"); el.value = (long)LInfo.value;
          }
          else
          {
            LInfo.Array("Link info " + LSize + "", 8);
            LInfo.UINT32("Type"); el.type = (long)LInfo.value;
            LInfo.UINT32("Value"); el.value = (long)LInfo.value;
          }
        }

        if( el.type == 5 ){ names_loc = el.value; }

        lib.add(el); LSize += 1;
      }

      //Read over types, and define the link libraries.

      for( int i2 = 0; i2 < LSize; i2++ )
      {
        el = lib.get(i2);

        //Needed link library name.

        if( el.type == 1 )
        {
          //Experiencing bugs with seekV. The method seekV needs to be fixed.
        
          /*file.seekV( names_loc + el.value ); Name = new Descriptor( file, true );

          Name.String8("Link Library name.", (byte)0x00 );
          
          curSec.add( new JDNode( (String)Name.value + ".h", new long[]{ 2, ref } ) );
          
          des.add( Name ); ref += 1;*/

          curSec.add( new JDNode( "Link library name location.h", new long[]{ -3, names_loc + el.value, 1 } ) );
        }
      }
    }
    
    return( des.toArray( new Descriptor[ des.size() ] ) );
  }
}
