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

      file.seekV(curSec.getArg(1)); LInfo = new Descriptor( file, true ); LInfo.setEvent( this::libInfo );

      //Setup section node.

      curSec.setUserObject( ((String)curSec.getUserObject()).replace(".h","") );

      curSec.setArgs( new long[]{ 2, ref } ); des.add( LInfo );  ref += 1;

      //Read section.

      while( el.type != 0 )
      {
        el = new libInfo(); LInfo.Array("Link info " + LSize + "", is64Bit ? 16 : 8 );

        if( is64Bit )
        {
          if( isLittle )
          {
            LInfo.LUINT64("Type"); el.type = (long)LInfo.value;
            LInfo.LUINT64("Value"); el.value = (long)LInfo.value;
          }
          else
          {
            LInfo.UINT64("Type"); el.type = (long)LInfo.value;
            LInfo.UINT64("Value"); el.value = (long)LInfo.value;
          }
        }
        else
        {
          if( isLittle )
          {
            LInfo.LUINT32("Type"); el.type = (int)LInfo.value;
            LInfo.LUINT32("Value"); el.value = (int)LInfo.value;
          }
          else
          {
            LInfo.UINT32("Type"); el.type = (int)LInfo.value;
            LInfo.UINT32("Value"); el.value = (int)LInfo.value;
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
          file.seekV( names_loc + el.value ); Name = new Descriptor( file, true );

          Name.String8("Link Library name.", (byte)0x00 ); Name.setEvent( this::nameInfo );
          
          curSec.add( new JDNode( (String)Name.value + " #" + i2 + ".h", new long[]{ 2, ref } ) );
          
          des.add( Name ); ref += 1;
        }
      }
    }
    
    return( des.toArray( new Descriptor[ des.size() ] ) );
  }

  //Detailed description of the link library sections.

  public static final String[] lInfo = new String[]
  {
    "<html>Array element consisting of a type, and value.</html>",
    "<html>" +
    "The value after type can be an address location, an Address plus a base address, a size value, or Other.<br /><br />" +
    "The types \"Address of\" = \"symbol hash table\", \"string table\", \"symbol table\". Are very important as they define base addresses.<br /><br />" +
    "The value after \"Name of needed library\" is not exactly a full address location. It needs to add the address, for \"string table\", for the name location, of the link library.<br /><br />" +
    "This means the value that comes after type 5 is added with all type 1 values to find the address to the library name.<br /><br />" +
    "<table border=\"1\">" +
    "<tr><td>Type</td><td>Operation</td></tr>" +
    "<tr><td>0</td><td>Marks end of dynamic section.</td></tr>" +
    "<tr><td>1</td><td>Name of needed library.</td></tr>" +
    "<tr><td>2</td><td>Size in bytes of PLT Relocations.</td></tr>" +
    "<tr><td>3</td><td>Processor defined value.</td></tr>" +
    "<tr><td>4</td><td>Address of symbol hash table.</td></tr>" +
    "<tr><td>5</td><td>Address of string table.</td></tr>" +
    "<tr><td>6</td><td>Address of symbol table.</td></tr>" +
    "<tr><td>7</td><td>Address of Relocation with addends.</td></tr>" +
    "<tr><td>8</td><td>Total size of Relocation with addends.</td></tr>" +
    "<tr><td>9</td><td>Size of one Relocation with addends.</td></tr>" +
    "<tr><td>10</td><td>Size of string table.</td></tr>" +
    "<tr><td>11</td><td>Size of one symbol table entry.</td></tr>" +
    "<tr><td>12</td><td>Address of init function.</td></tr>" +
    "<tr><td>13</td><td>Address of termination function.</td></tr>" +
    "<tr><td>14</td><td>Name of shared object.</td></tr>" +
    "<tr><td>15</td><td>Library search path (deprecated).</td></tr>" +
    "<tr><td>16</td><td>Start symbol search.</td></tr>" +
    "<tr><td>17</td><td>Address of Relocations.</td></tr>" +
    "<tr><td>18</td><td>Total size of Rel Relocations.</td></tr>" +
    "<tr><td>19</td><td>Size of one Relocation.</td></tr>" +
    "<tr><td>20</td><td>Type of Relocation in PLT.</td></tr>" +
    "<tr><td>21</td><td>For debugging; unspecified.</td></tr>" +
    "<tr><td>22</td><td>Relocations might modify .text.</td></tr>" +
    "<tr><td>23</td><td>Address to PLT Relocations.</td></tr>" +
    "<tr><td>24</td><td>Process relocations of object.</td></tr>" +
    "<tr><td>25</td><td>Array with addresses of init fct.</td></tr>" +
    "<tr><td>26</td><td>Array with addresses of fini fct.</td></tr>" +
    "<tr><td>27</td><td>Size in bytes of init Array.</td></tr>" +
    "<tr><td>28</td><td>Size in bytes of fini Array.</td></tr>" +
    "<tr><td>29</td><td>Library search path.</td></tr>" +
    "<tr><td>30</td><td>Flags for the object being loaded.</td></tr>" +
    "<tr><td>31</td><td>Start of encoded range.</td></tr>" +
    "<tr><td>32</td><td>Array with addresses of pre-init fct.</td></tr>" +
    "<tr><td>33</td><td>Size in bytes, for pre-init Array.</td></tr>" +
    "<tr><td>34</td><td>Address of SYMTAB_SHNDX section.</td></tr>" +
    "<tr><td>35</td><td>Number used.</td></tr>" +
    "<tr><td>6000000D to 6FFFFFFF</td><td>OS specific.</td></tr>" +
    "<tr><td>70000000 to 7FFFFFFF</td><td>Processor specific.</td></tr>" +
    "</html>",
    "<html>What this value is used for is determined by it's type setting.</html>"
  };

  public void libInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>The dynamic link library section consists of many different types that define the needed link libraries, and more.</html>");
    }
    else
    {
      info( lInfo[ el % 3 ] );
    }
  }

  //Enquired link libraries.

  public void nameInfo( int el )
  {
    info("<html>Each link library name end with 00 hex.</html>");
  }
}
