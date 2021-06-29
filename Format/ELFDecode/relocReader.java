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

    JDNode sects = sections[5], curSec = null;

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

      curSec.setUserObject( ((String)curSec.getUserObject()).replace(".h","") ); curSec.setArgs( new long[]{ 4, ref } ); des.add( rel ); ref += 1;

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
            rel.LUINT64("Type"); type = (long)rel.value; sym = (int)(type >> 32); type = type & 0xFFFFFFFFl;
            if( addEnds ) { rel.LINT64("Addend"); }
          }
          else
          {
            rel.UINT64("Address"); pos = (long)rel.value;
            rel.UINT64("Type"); type = (long)rel.value; sym = (int)(type >> 32); type = type & 0xFFFFFFFFl;
            if( addEnds ) { rel.INT64("Addend"); }
          }
        }
        else
        {
          if( isLittle )
          {
            rel.LUINT32("Address"); pos = (int)rel.value;
            rel.LUINT32("Type"); type = (int)rel.value; sym = (int)(type >> 8); type = type & 0xFF;
            if( addEnds ) { rel.INT32("Addend"); }
          }
          else
          {
            rel.UINT32("Address"); pos = (int)rel.value;
            rel.UINT32("Type"); type = (int)rel.value; sym = (int)(type >> 8); type = type & 0xFF;
            if( addEnds ) { rel.INT32("Addend"); }
          }
        }

        //Add symbol position in global pointer table.
        //This allows us to map the dynamically loaded symbols, and other data.

        if( coreType == 62 ) //X86-64
        {
          if ( type == 6 || type == 7 )
          {
            sym_pos[ sym ] = pos;

            core.mapped_pos.add(pos); core.mapped_pos.add(pos + 8); core.mapped_loc.add( sym_names[sym] );

            curSec.add( new JDNode( sym_names[ sym ] + ".h", new long[]{ -3, pos, 8 } ) );
          }
        }

        else if( coreType == 3 ) //X86-32
        {
          if ( type == 6 || type == 7 )
          {
            sym_pos[ sym ] = pos;

            core.mapped_pos.add(pos); core.mapped_pos.add(pos + 4); core.mapped_loc.add( sym_names[sym] );

            curSec.add( new JDNode( sym_names[ sym ] + ".h", new long[]{ -3, pos, 4 } ) );
          }
        }

        //Generically define the symbols, for other CPU types.

        else
        {
          sym_pos[ sym ] = pos;

          //Note relocations can be different sizes other than the CPU bit size depending on what the relocation is calculation of.

          curSec.add( new JDNode( sym_names[ sym ] + ".h", new long[]{ -3, pos, is64Bit ? 8 : 4 } ) );
        }
      }
    }

    return( des.toArray( new Descriptor[ des.size() ] ) );
  }

  //Detailed description of the symbol sections.

  public static final String[] RelInfo = new String[]
  {
    "<html>An Array consisting of an address, symbol, and type of relocation, and optional Addend size.</html>",
    "<html>Address to be set to link library location, or address needs be adjust relative to section position.<br /><br />" +
    "See the relocation type for details.</html>", "",
    "<html>The Addend if any.</html>"
  };

  public static final String relType64 = "<html>Each relocation type has a symbol number and type of relocation.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>Value.</td><td>Decoding.</td></tr>" +
  "<tr><td>00000009 00000007</td><td>Symbol = 9, Type = 7.</td></tr>" +
  "<tr><td>00000002 00000006</td><td>Symbol = 2, Type = 6.</td></tr>" +
  "</table><br />" +
  "In hex the value is 16 digits long. The first 8 digits is the Symbol, and the last 8 digits is the Type.<br /><br />" +
  "The symbol could be a name of a link library function, and the type could specify to place the address.<br /><br />" +
  "Note that you will want to view these under the data inspector in hex.<br /><br /><hr /><br />";

  public static final String relType32 = "<html>Each relocation type has a symbol number and type of relocation.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>Value.</td><td>Decoding.</td></tr>" +
  "<tr><td>000009 07</td><td>Symbol = 9, Type = 7.</td></tr>" +
  "<tr><td>000002 06</td><td>Symbol = 2, Type = 6.</td></tr>" +
  "</table><br />" +
  "In hex the value is 8 digits long. The first 6 digits is the Symbol, and the last 8 digits is the Type.<br /><br />" +
  "The symbol could be a name of a link library function, and the symbol type could specify to place the address.<br /><br />" +
  "Take note that you will want to view these under the data inspector in hex.<br /><br /><hr /><br />";

  public static final String rel8664 = "The tow most important types are 6, and 7. Type 7 tell the dynamic linker where to place an function call location.<br /><br />" +
  "The address usually locates to sections called \".got.plt\", this allows us to map dynamically loaded function calls.<br /><br />" +
  "Type 6 locates to section \".got\" which is used as a data location. Such as arrays and strings, and other things.<br /><br /><hr /><br />" +
  "The machine code in section \".plt.got\" reads the values placed in sections \".got,plt\" then jumps CPU to function.</html>";

  public static final String rel386 = "The tow most important types are 6, and 7. Type 7 tell the dynamic linker where to place an function call location.<br /><br />" +
  "The address usually locates to sections called \".got.plt\", this allows us to map dynamically loaded function calls.<br /><br />" +
  "Type 6 locates to section \".got\" which is used as a data location. Such as arrays and strings, and other things.<br /><br /><hr /><br />" +
  "The machine code in section \".plt.got\" reads the values placed in sections \".got.plt\" then jumps CPU to function.</html>";
  
  public void relaInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>All locations would be correct if the locations the ELF header specifies to put sections into RAM are not already used.<br /><br />" +
      "Also relocations setup the locations in the global pointer table sections \".got\", and \".got.plt\", for dynamically loaded methods.</html>");
    }
    else
    {
      el = el % 4;
      
      if( el == 2 )
      {
        if( coreType == 62 ) { info( relType64 + rel8664 ); } else if( coreType == 3 ) { info( relType32 + rel386 ); }
      }
      else { info( RelInfo[ el ] ); }
    }
  }

  public void relInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>All locations would be correct if the locations the ELF header specifies to put sections into RAM are not already used.<br /><br />" +
      "Also relocations setup the locations in the global pointer table sections \".got\", and \".got.plt\", for dynamically loaded methods.</html>");
    }
    else
    {
      el = el % 3;

      if( el == 2 )
      {
        if( coreType == 62 ) { info( relType64 + rel8664 ); } else if( coreType == 3 ) { info( relType32 + rel386 ); }
      }
      else { info( RelInfo[ el ] ); }
    }
  }
}
