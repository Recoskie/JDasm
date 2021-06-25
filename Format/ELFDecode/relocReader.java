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

      curSec.setUserObject( ((String)curSec.getUserObject()).replace(".h","") ); curSec.setArgs( new long[]{ 3, ref } ); des.add( rel ); ref += 1;

      //Read locations.

      long type = 0, pos = 0;
      
      int sym = 0;
      
      sym_pos = new long[ sym_names.length ];

      for( int i2 = 0; i2 < end; i2++ )
      {
        rel.Array( "Relocation #" + i2 + "", rel_size );

        if( is64Bit )
        {
          if( isLittle )
          {
            rel.LUINT64("Address"); pos = (long)rel.value;
            rel.LUINT64("Type"); type = (long)rel.value; sym = (int)(type >> 32);
            if( addEnds ) { rel.LINT64("Addend"); }
          }
          else
          {
            rel.UINT64("Address"); pos = (long)rel.value;
            rel.UINT64("Type"); type = (long)rel.value; sym = (int)(type >> 32);
            if( addEnds ) { rel.INT64("Addend"); }
          }
        }
        else
        {
          if( isLittle )
          {
            rel.LUINT32("Address"); pos = (int)rel.value;
            rel.LUINT32("Type"); type = (int)rel.value; sym = (int)(type >> 8);
            if( addEnds ) { rel.INT32("Addend"); }
          }
          else
          {
            rel.UINT32("Address"); pos = (int)rel.value;
            rel.UINT32("Type"); type = (int)rel.value; sym = (int)(type >> 8);
            if( addEnds ) { rel.INT32("Addend"); }
          }
        }

        //Add symbol position in global pointer table.
        //This allows us to map the dynamically loaded symbols.

        if( is64Bit )
        {
          sym_pos[ sym ] = pos;

          curSec.add( new JDNode( sym_names[ sym ] + ".h", new long[]{ -3, pos, 8 } ) );
        }
        else
        {
          sym_pos[ sym ] = pos;

          curSec.add( new JDNode( sym_names[ sym ] + ".h", new long[]{ -3, pos, 4 } ) );
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
      info("<html>All locations would be correct if the locations the ELF header specifies to put sections into RAM are not already used.<br /><br />" +
      "All relocations generally adjust the locations in the global pointer table sections \".got\", and \".got.plt\".</html>");
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
      info("<html>All locations would be correct if the locations the ELF header specifies to put sections into RAM are not already used.<br /><br />" +
      "All relocations generally adjust the locations in the global pointer table sections \".got\", and \".got.plt\".</html>");
    }
    else
    {
      el = el % 3; info( RelInfo[ el ] );
    }
  }
}
