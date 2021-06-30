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

  public static final String basic = "<html>All locations would be correct if the locations the ELF header specifies to put sections into RAM are not already used.<br /><br />" +
  "Also relocations setup the locations in the global pointer table sections \".got\", and \".got.plt\", for dynamically loaded methods.<br /><br /><hr /><br />";

  public static final String relType64 = "Each relocation type has a symbol number and type of relocation.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>Value.</td><td>Decoding.</td></tr>" +
  "<tr><td>00000009 00000007</td><td>Symbol = 9, Type = 7.</td></tr>" +
  "<tr><td>00000002 00000006</td><td>Symbol = 2, Type = 6.</td></tr>" +
  "</table><br />" +
  "In hex the value is 16 digits long. The first 8 digits is the Symbol, and the last 8 digits is the Type.<br /><br />" +
  "The symbol could be a name of a link library function, and the type can specify to place the address at the relocation address.<br /><br />" +
  "Note that you will want to view these under the data inspector in hex.<br /><br /><hr /><br />";

  public static final String relType32 = "Each relocation type has a symbol number and type of relocation.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>Value.</td><td>Decoding.</td></tr>" +
  "<tr><td>000009 07</td><td>Symbol = 9, Type = 7.</td></tr>" +
  "<tr><td>000002 06</td><td>Symbol = 2, Type = 6.</td></tr>" +
  "</table><br />" +
  "In hex the value is 8 digits long. The first 6 digits is the Symbol, and the last 8 digits is the Type.<br /><br />" +
  "The symbol could be a name of a link library function, and the type can specify to place the address at the relocation address.<br /><br />" +
  "Take note that you will want to view these under the data inspector in hex.<br /><br /><hr /><br />";

  public static final String rel8664 = "The tow most important types are 6, and 7.<br /><br /><strong>Type 7</strong> tell the dynamic linker where to place an function call location.<br /><br />" +
  "The address usually locates to sections called \".got.plt\", this allows us to map dynamically loaded function calls.<br /><br />" +
  "<strong>Type 6</strong> locates to section \".got\" which is used as a data location. Such as arrays and strings, and other things.<br /><br /><hr /><br />" +
  "The machine code in section \".plt.got\" reads the values placed in sections \".got,plt\" then jumps CPU to function.<br /><br /><hr /><br />" +
  "The full listing of all reallocation types are bellow. Note that only an handful are generally used.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>Type Value (Hex).</td><td>Address locates to.</td></tr>" +
  "<tr><td>00000000</td><td>No relocation.</td></tr>" +
  "<tr><td>00000001</td><td>Direct 64 bit.</td></tr>" +
  "<tr><td>00000002</td><td>PC relative 32 bit signed.</td></tr>" +
  "<tr><td>00000003</td><td>32 bit GOT entry.</td></tr>" +
  "<tr><td>00000004</td><td>32 bit PLT address.</td></tr>" +
  "<tr><td>00000005</td><td>Copy symbol at runtime.</td></tr>" +
  "<tr><td>00000006</td><td>Create GOT entry.</td></tr>" +
  "<tr><td>00000007</td><td>Create PLT entry.</td></tr>" +
  "<tr><td>00000008</td><td>Adjust by program base.</td></tr>" +
  "<tr><td>00000009</td><td>32 bit signed PC relative offset to GOT.</td></tr>" +
  "<tr><td>0000000A</td><td>Direct 32 bit zero extended.</td></tr>" +
  "<tr><td>0000000B</td><td>Direct 32 bit sign extended.</td></tr>" +
  "<tr><td>0000000C</td><td>Direct 16 bit zero extended.</td></tr>" +
  "<tr><td>0000000D</td><td>16 bit sign extended pc relative.</td></tr>" +
  "<tr><td>0000000E</td><td>Direct 8 bit sign extended.</td></tr>" +
  "<tr><td>0000000F</td><td>8 bit sign extended pc relative.</td></tr>" +
  "<tr><td>00000010</td><td>ID of module containing symbol.</td></tr>" +
  "<tr><td>00000011</td><td>Offset in module's TLS block.</td></tr>" +
  "<tr><td>00000012</td><td>Offset in initial TLS block.</td></tr>" +
  "<tr><td>00000013</td><td>32 bit signed PC relative offset to two GOT entries for GD symbol.</td></tr>" +
  "<tr><td>00000014</td><td>32 bit signed PC relative offset to two GOT entries for LD symbol.</td></tr>" +
  "<tr><td>00000015</td><td>Offset in TLS block.</td></tr>" +
  "<tr><td>00000016</td><td>32 bit signed PC relative offset to GOT entry for IE symbol.</td></tr>" +
  "<tr><td>00000017</td><td>Offset in initial TLS block.</td></tr>" +
  "<tr><td>00000018</td><td>PC relative 64 bit.</td></tr>" +
  "<tr><td>00000019</td><td>64 bit offset to GOT.</td></tr>" +
  "<tr><td>0000001A</td><td>32 bit signed pc relative offset to GOT.</td></tr>" +
  "<tr><td>0000001B</td><td>64-bit GOT entry offset.</td></tr>" +
  "<tr><td>0000001C</td><td>64-bit PC relative offset to GOT entry.</td></tr>" +
  "<tr><td>0000001D</td><td>64-bit PC relative offset to GOT.</td></tr>" +
  "<tr><td>0000001E</td><td>like GOT64, says PLT entry needed.</td></tr>" +
  "<tr><td>0000001F</td><td>64-bit GOT relative offset to PLT entry.</td></tr>" +
  "<tr><td>00000020</td><td>Size of symbol plus 32-bit addend.</td></tr>" +
  "<tr><td>00000021</td><td>Size of symbol plus 64-bit addend.</td></tr>" +
  "<tr><td>00000022</td><td>GOT offset for TLS descriptor.</td></tr>" +
  "<tr><td>00000023</td><td>Marker for call through TLS descriptor.</td></tr>" +
  "<tr><td>00000024</td><td>TLS descriptor.</td></tr>" +
  "<tr><td>00000025</td><td>Adjust indirectly by program base.</td></tr>" +
  "<tr><td>00000026</td><td>64-bit adjust by program base.</td></tr>" +
  "<tr><td>00000027</td><td>Reserved was R_X86_64_PC32_BND.</td></tr>" +
  "<tr><td>00000028</td><td>Reserved was R_X86_64_PLT32_BND.</td></tr>" +
  "<tr><td>00000029</td><td>Load from 32 bit signed pc relative offset to GOT entry without REX prefix, relaxable.</td></tr>" +
  "<tr><td>0000002A</td><td>Load from 32 bit signed pc relative offset to GOT entry with REX prefix, relaxable.</td></tr>" +
  "</table></html>";

  public static final String rel386 = "The tow most important types are 6, and 7.<br /><br /><strong>Type 7</strong> tell the dynamic linker where to place an function call location.<br /><br />" +
  "The address usually locates to sections called \".got.plt\", this allows us to map dynamically loaded function calls.<br /><br />" +
  "<strong>Type 6</strong> locates to section \".got\" which is used as a data location. Such as arrays and strings, and other things.<br /><br /><hr /><br />" +
  "The machine code in section \".plt.got\" reads the values placed in sections \".got.plt\" then jumps CPU to function.<br /><br /><hr /><br />" +
  "The full listing of all reallocation types are bellow. Note that only an handful are generally used.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>Type Value (Hex).</td><td>Address locates to.</td></tr>" +
  "<tr><td>00</td><td>No relocation.</td></tr>" +
  "<tr><td>01</td><td>Direct 32 bit.</td></tr>" +
  "<tr><td>02</td><td>PC relative 32 bit.</td></tr>" +
  "<tr><td>03</td><td>32 bit GOT entry.</td></tr>" +
  "<tr><td>04</td><td>32 bit PLT address.</td></tr>" +
  "<tr><td>05</td><td>Copy symbol at runtime.</td></tr>" +
  "<tr><td>06</td><td>Create GOT entry.</td></tr>" +
  "<tr><td>07</td><td>Create PLT entry.</td></tr>" +
  "<tr><td>08</td><td>Adjust by program base.</td></tr>" +
  "<tr><td>09</td><td>32 bit offset to GOT.</td></tr>" +
  "<tr><td>0A</td><td>32 bit PC relative offset to GOT.</td></tr>" +
  "<tr><td>0B</td><td>R_386_32PLT</td></tr>" +
  "<tr><td>0E</td><td>Offset in static TLS block.</td></tr>" +
  "<tr><td>0F</td><td>Address of GOT entry for static TLS block offset.</td></tr>" +
  "<tr><td>10</td><td>GOT entry for static TLS block offset.</td></tr>" +
  "<tr><td>11</td><td>Offset relative to static TLS block.</td></tr>" +
  "<tr><td>12</td><td>Direct 32 bit for GNU version of general dynamic thread local data.</td></tr>" +
  "<tr><td>13</td><td>Direct 32 bit for GNU version of local dynamic thread local data in LE code.</td></tr>" +
  "<tr><td>14</td><td>R_386_16</td></tr>" +
  "<tr><td>15</td><td>R_386_PC16</td></tr>" +
  "<tr><td>16</td><td>R_386_8</td></tr>" +
  "<tr><td>17</td><td>R_386_PC8</td></tr>" +
  "<tr><td>18</td><td>Direct 32 bit for general dynamic thread local data.</td></tr>" +
  "<tr><td>19</td><td>Tag for pushl in GD TLS code.</td></tr>" +
  "<tr><td>1A</td><td>Relocation for call to __tls_get_addr().</td></tr>" +
  "<tr><td>1B</td><td>Tag for popl in GD TLS code.</td></tr>" +
  "<tr><td>1C</td><td>Direct 32 bit for local dynamic thread local data in LE code.</td></tr>" +
  "<tr><td>1D</td><td>Tag for pushl in LDM TLS code.</td></tr>" +
  "<tr><td>1E</td><td>Relocation for call to __tls_get_addr() in LDM code.</td></tr>" +
  "<tr><td>1F</td><td>Tag for popl in LDM TLS code.</td></tr>" +
  "<tr><td>20</td><td>Offset relative to TLS block</td></tr>" +
  "<tr><td>21</td><td>GOT entry for negated static TLS block offset.</td></tr>" +
  "<tr><td>22</td><td>Negated offset relative to static TLS block.</td></tr>" +
  "<tr><td>23</td><td>ID of module containing symbol.</td></tr>" +
  "<tr><td>24</td><td>Offset in TLS block.</td></tr>" +
  "<tr><td>25</td><td>Negated offset in static TLS block.</td></tr>" +
  "<tr><td>26</td><td>32-bit symbol size.</td></tr>" +
  "<tr><td>27</td><td>GOT offset for TLS descriptor.</td></tr>" +
  "<tr><td>28</td><td>Marker of call through TLS descriptor for relaxation.</td></tr>" +
  "<tr><td>29</td><td>TLS descriptor containing pointer to code and to argument, returning the TLS offset for the symbol.</td></tr>" +
  "<tr><td>2A</td><td>Adjust indirectly by program base.</td></tr>" +
  "<tr><td>2B</td><td>Load from 32 bit GOT entry, relaxable.</td></tr>" +
  "</table></html>";

  private static String t = "";
  
  public void relaInfo( int el )
  {
    if( el < 0 ) { t = basic; el = 2; } else { t = "<html>"; }

    el = el % 4;
      
    if( el == 2 )
    {
      if( coreType == 62 ) { info( t + relType64 + rel8664 ); } else if( coreType == 3 ) { info( t + relType32 + rel386 ); }
    }
    else { info( RelInfo[ el ] ); }
  }

  public void relInfo( int el )
  {
    if( el < 0 ) { t = basic; el = 2; } else{ t = "<html>"; }

    el = el % 3;
      
    if( el == 2 )
    {
      if( coreType == 62 ) { info( t + relType64 + rel8664 ); } else if( coreType == 3 ) { info( t + relType32 + rel386 ); }
    }
    else { info( RelInfo[ el ] ); }
  }
}
