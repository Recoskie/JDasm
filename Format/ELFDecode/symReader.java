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
    "<html>The value is split into two. The last hex digits is what the symbol is used for.<br /><br />" +
    "<table border=\"1\">" +
    "<tr><td>Value (Hex)</td><td>Type</td></tr>" +
    "<tr><td>x0</td><td>Symbol type is unspecified.</td></tr>" +
    "<tr><td>x1</td><td>Symbol is data.</td></tr>" +
    "<tr><td>x2</td><td>Symbol is a code (Function).</td></tr>" +
    "<tr><td>x3</td><td>Symbol associated with a section.</td></tr>" +
    "<tr><td>x4</td><td>Symbol's name is file name.</td></tr>" +
    "<tr><td>x5</td><td>Symbol is a common data.</td></tr>" +
    "<tr><td>x6</td><td>Symbol is thread local storage data.</td></tr>" +
    "<tr><td>x7</td><td>Multi-type. May contain both code and data.</td></tr>" +
    "<tr><td>x8</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>x9</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>xA to xC</td><td>OS specific types.</td></tr>" +
    "<tr><td>xA</td><td>Unique symbol.</td></tr>" +
    "<tr><td>xB</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>xC</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>xD to xF</td><td>Processor specific types.</td></tr>" +
    "<tr><td>xD</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>xE</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>xF</td><td>Reserved for future use.</td></tr>" +
    "</table><br />" +
    "The first hex digits is what type of binding it uses.<br /><br />" +
    "<table border=\"1\">" +
    "<tr><td>Value</td><td>Type</td></tr>" +
    "<tr><td>0x</td><td>Local symbol (used within function/block).</td></tr>" +
    "<tr><td>1x</td><td>Global symbol (used from everywhere).</td></tr>" +
    "<tr><td>2x</td><td>Weak symbol.</td></tr>" +
    "<tr><td>3x</td><td>Multi-bound-type.</td></tr>" +
    "<tr><td>4x</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>5x</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>6x</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>7x</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>8x</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>9x</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>Ax to Cx</td><td>OS specific types.</td></tr>" +
    "<tr><td>Ax</td><td>Unique symbol.</td></tr>" +
    "<tr><td>Bx</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>Cx</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>Dx to Fx</td><td>Processor specific types.</td></tr>" +
    "<tr><td>Dx</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>Ex</td><td>Reserved for future use.</td></tr>" +
    "<tr><td>Fx</td><td>Reserved for future use.</td></tr>" +
    "</table></html>",
    "<html>Usually the symbol visibility type is 0. Meaning the symbol follows the Default symbol visibility rules.<br /><br /><table border=\"1\">" +
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
