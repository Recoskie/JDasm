package Format.ELFDecode;

import swingIO.*;
import swingIO.tree.*;

public class relocReader extends Data implements sec
{
  public Descriptor[] read() throws java.io.IOException
  {
    java.util.LinkedList<Descriptor> des = new java.util.LinkedList<Descriptor>();

    int end = 0, rel_size = 0, ref = 0;
    
    boolean addEnds = false; //Weather it is relocations with addends, or not. 
    
    Descriptor rel;

    JDNode sects = sections[4], curSec = null;

    for( int i = 0, size = sects.getChildCount(); i < size; i++ )
    {
      curSec = (JDNode)sects.getChildAt(i);

      //In this case the relocations have addends.

      addEnds = curSec.getID().equals("Add");

      //Setup descriptor.

      file.seekV(curSec.getArg(1)); rel = new Descriptor( file, true );
      
      if( addEnds ){ rel.setEvent( this::relaInfo ); } else { rel.setEvent( this::relInfo ); }

      //Number of address locations.

      end = (int)( curSec.getArg(2) / ( rel_size = ( is64Bit ? ( addEnds ? 24 : 16 ) : ( addEnds ? 12 : 8 ) ) ) );

      //Setup section node.

      curSec.setArgs( new long[]{ 3, ref } ); des.add( rel ); ref += 1;

      //Read locations.

      for( int i2 = 0; i2 < end; i2++ )
      {
        rel.Array( "Relocation #" + i2 + "", rel_size );

        if( is64Bit )
        {
          if( isLittle )
          {
            rel.LUINT64("Address"); rel.LUINT64("Type");
            if( addEnds ) { rel.LINT64("Addend"); }
          }
          else
          {
            rel.UINT64("Address"); rel.UINT64("Type");
            if( addEnds ) { rel.INT64("Addend"); }
          }
        }
        else
        {
          if( isLittle )
          {
            rel.LUINT32("Address"); rel.LUINT32("Type");
            if( addEnds ) { rel.INT32("Addend"); }
          }
          else
          {
            rel.UINT32("Address"); rel.UINT32("Type");
            if( addEnds ) { rel.INT32("Addend"); }
          }
        }
      }
    }

    return( des.toArray( new Descriptor[ des.size() ] ) );
  }

  //Detailed description of the symbol sections.

  public static final String[] RelInfo = new String[]
  {
    "<html>An Array consisting of an address, and type, and optional Addend size.</html>",
    "<html>Address that needs to be changed if section changes address.</html>",
    "<html>The type of address.</html>",
    "<html>The Addend if any.</html>"
  };
  
  public void relaInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>All locations would be correct if the locations the ELF header specifies to put sections into RAM are not already used.</html>");
    }
    else
    {
      el = el % 4; info( RelInfo[ el ] );
    }
  }

  public void relInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>All locations would be correct if the locations the ELF header specifies to put sections into RAM are not already used.</html>");
    }
    else
    {
      el = el % 3; info( RelInfo[ el ] );
    }
  }
}
