package Format.ELFDecode;

import swingIO.*;
import swingIO.tree.*;

public class symReader extends Data implements sec
{
  public Descriptor[] read() throws java.io.IOException
  {
    java.util.ArrayList<Descriptor> sym = new java.util.ArrayList<Descriptor>();

    //We only use one descriptor for each array as it is only disassemble locations.

    int ref = 0, end = 0, type = 0;
    Descriptor loc, name;
    long name_loc = 0, strTable = 0, t = 0;

    //WE read all link library sections.

    JDNode sects = sections[5], curSec = null, Func = new JDNode("Functions");

    for( int i = 0, size = sects.getChildCount(); i < size; i++ )
    {
      curSec = (JDNode)sects.getChildAt(i);

      //In this case we must know the location to the string table used in the link library section.

      if( curSec.getID().equals("Lib") ) { strTable = dynStr; }

      //Setup descriptor.

      file.seekV(curSec.getArg(1)); loc = new Descriptor( file, true ); loc.setEvent( this::symInfo );

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
            loc.LUINT32("Symbol name location"); name_loc = (int)loc.value;
            loc.UINT8("Symbol type and binding"); type = (byte)loc.value;
            loc.UINT8("Symbol visibility");
            loc.LUINT16("Section Index");
            loc.LUINT64("Symbol Address");
            loc.LUINT64("Symbol size");
          }
          else
          {
            loc.UINT32("Symbol name location"); name_loc = (int)loc.value;
            loc.UINT8("Symbol type and binding"); type = (byte)loc.value;
            loc.UINT8("Symbol visibility");
            loc.UINT16("Section Index");
            loc.UINT64("Symbol Address");
            loc.UINT64("Symbol size");
          }
        }
        else
        {
          loc.Array("Symbol " + i2 + "", 16 );

          if( isLittle )
          {
            loc.LUINT32("Symbol name location"); name_loc = (int)loc.value;
            loc.LUINT32("Symbol Address");
            loc.LUINT32("Symbol size");
            loc.UINT8("Symbol type and binding"); type = (byte)loc.value;
            loc.UINT8("Symbol visibility");
            loc.LUINT16("Section Index");
          }
          else
          {
            loc.UINT32("Symbol name location"); name_loc = (int)loc.value;
            loc.UINT32("Symbol Address");
            loc.UINT32("Symbol size");
            loc.UINT8("Symbol type and binding"); type = (byte)loc.value;
            loc.UINT8("Symbol visibility");
            loc.UINT16("Section Index");
          }
        }
        
        if( name_loc > 0)
        {
          t = file.getVirtualPointer();
          file.seekV( name_loc + strTable ); name = new Descriptor(file, true); name.String8("Symbol name.", (byte)0x00); sym.add( name );
          
          if( ( type & 0xF ) == 2 ) { Func.add( new JDNode( name.value + " #" + i2 + ".h", new long[]{ 4, ref } ) ); }
          else
          {
            curSec.add( new JDNode( name.value + " #" + i2 + ".h", new long[]{ 4, ref } ) );
          }
          
          ref += 1; file.seekV( t );
        }
      }

      if( Func.getChildCount() > 0 ) { curSec.insert( Func, 0 ); }
    }

    return( sym.toArray( new Descriptor[ sym.size() ] ) );
  }

  //Detailed description of the symbol sections.

  public static final String[] SymInfo = new String[]
  {
    "<html>An Array consisting of a symbol type, name, Address, and size.</html>",
    "<html>The name location is added with the location to the string table. If the name location is 0 then the symbol has no name.</html>",
    "<html>The value is split into two. The last 4 binary digits is what the symbol is used for.<br /><br />" +
    "<table border=\"1\">" +
    "<tr><td>Value</td><td>Type</td></tr>" +
    "<tr><td>xxxx0000</td><td>Symbol type is unspecified.</td></tr>" +
    "<tr><td>xxxx0001</td><td>Symbol is data.</td></tr>" +
    "<tr><td>xxxx0010</td><td>Symbol is a code (Function).</td></tr>" +
    "<tr><td>xxxx0011</td><td>Symbol associated with a section.</td></tr>" +
    "<tr><td>xxxx0100</td><td>Symbol's name is file name.</td></tr>" +
    "<tr><td>xxxx0101</td><td>Symbol is a common data.</td></tr>" +
    "<tr><td>xxxx0110</td><td>Symbol is thread local storage data.</td></tr>" +
    "<tr><td>xxxx0111</td><td>Multi-type. May contain both code and data.</td></tr>" +
    "<tr><td>xxxx1000</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>xxxx1001</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>xxxx1010 to xxxx1100</td><td>OS specific types.</td></tr>" +
    "<tr><td>xxxx1010</td><td>Unique symbol.</td></tr>" +
    "<tr><td>xxxx1011</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>xxxx1100</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>xxxx1101 to xxxx1111</td><td>Processor specific types.</td></tr>" +
    "<tr><td>xxxx1101</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>xxxx1110</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>xxxx1111</td><td>Reserved for future use.</td></tr>" +
    "</table><br />" +
    "The first 4 binary digits is what type of binding it uses.<br /><br />" +
    "<table border=\"1\">" +
    "<tr><td>Value</td><td>Type</td></tr>" +
    "<tr><td>0000xxxx</td><td>Local symbol (used within function/block).</td></tr>" +
    "<tr><td>0001xxxx</td><td>Global symbol (used from everywhere).</td></tr>" +
    "<tr><td>0010xxxx</td><td>Weak symbol.</td></tr>" +
    "<tr><td>0011xxxx</td><td>Multi-bound-type.</td></tr>" +
    "<tr><td>0100xxxx</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>0101xxxx</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>0110xxxx</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>0111xxxx</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>1000xxxx</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>1001xxxx</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>1010xxxx to 1100xxxx</td><td>OS specific types.</td></tr>" +
    "<tr><td>1010xxxx</td><td>Unique symbol.</td></tr>" +
    "<tr><td>1011xxxx</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>1100xxxx</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>1101xxxx to 1111xxxx</td><td>Processor specific types.</td></tr>" +
    "<tr><td>1101xxxx</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>1110xxxx</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>1111xxxx</td><td>Reserved for future use.</td></tr>" +
    "</table></html>",
    "<html>Usually the symbol visibility type is 0 meaning the symbol follows the Default symbol visibility rules.<br /><br /><table border=\"1\">" +
    "<tr><td>Value</td><td>Type</td></tr>" +
    "<tr><td>0</td><td>Default symbol visibility rules.</td></tr>" +
    "<tr><td>1</td><td>Processor specific hidden.</td></tr>" +
    "<tr><td>2</td><td>Symbol unavailable in other modules.</td></tr>" +
    "<tr><td>3</td><td>Not preemptible, and not exported.</td></tr>" +
    "</table></html>",
    "<html>Section Index.</html>",
    "<html>Symbol Address.</html>",
    "<html>Symbol size.</html>"
  };
  
  public void symInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>Symbols are very easy to read, and are defined by type, and size. Symbols contain the names of function, and data.</html>");
    }
    else
    {
      el = el % 7; if( !is64Bit ) { if( el >= 2 ){ el += 3; } if( el >= 7 ){ el -= 5; } }
        
      info( SymInfo[ el ] );
    }
  }
}
