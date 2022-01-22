package Format.MACDecode;

import swingIO.*;
import swingIO.tree.*;
import javax.swing.tree.*;

public class Headers extends Data
{
  public void readMAC(JDNode n) throws java.io.IOException
  {
    //Begin reading the MacOS header.

    DTemp = new Descriptor( file ); n.removeAllChildren();
    
    DTemp.LUINT32("Signature");
    
    is64bit = (int)DTemp.value == -17958193;

    //Mac universal binaries have more than one loadable mac program that can be switch between.
    
    if( (int)DTemp.value == -1095041334 )
    {
      DTemp.setEvent( this::UMacHInfo );
      
      DTemp.UINT32("Binaries"); int b = (int)DTemp.value;
    
      JDNode Apps;
          
      for( int i = 0; i < b; i++ )
      {
        DTemp.Array("Mac Binary #" + i + "", 20 );
        DTemp.UINT32("CPU Type");
        DTemp.UINT32("CPU Sub Type");
        DTemp.UINT32("File Offset");
    
        Apps = new JDNode("App #" + i + "", new long[] { 1, (int)DTemp.value } ); Apps.add( new JDNode("") );
    
        n.add( Apps );
    
        DTemp.UINT32("Size");
        DTemp.UINT32("Align");
      }
    }

    //Begin reading the mac header. Note some binaries can have more than one loadable binary.

    else
    {
      //Uninitialize the loaded app before loading a new one.

      if( App != null )
      {
        //Leave only the universal mac header. There should only be one at the start of the binary.

        java.util.ArrayList<Descriptor> bd = new java.util.ArrayList<Descriptor>();

        bd.add( des.get(0) ); des = bd; ref = 1;

        //Remove everything read under the other App.

        App.removeAllChildren(); App.add( new JDNode( "" ) );

        //Reset the applications virtual address space.

        file.resetV();

        //Update the tree.

        ((DefaultTreeModel)tree.getModel()).reload((TreeNode)((DefaultTreeModel)tree.getModel()).getRoot());
      }

      //Set the current node for the application that is being read.
      
      App = n; DTemp.setEvent( this::MacHInfo );

      //Read the application header.

      DTemp.LUINT32("CPU Type"); coreType = ((int)DTemp.value) & 0xFF;
      DTemp.UINT32("CPU Sub Type");
      DTemp.LUINT32("File Type");
      DTemp.LUINT32("Commands");
      DTemp.LUINT32("Commands Size");
      DTemp.LUINT32("Flags");
    
      if( is64bit ){ DTemp.UINT32("Reserved"); }
    }

    n.insert( new JDNode("Mac Header.h", new long[]{ 0, ref++ } ), 0 ); des.add( DTemp ); DTemp = null;
  }

  private static final String Singatures = "<html>The MacOS binary format uses two signature types.<br /><br />" +
  "<table border='1'>" +
  "<tr><td>Hex Value</td><td>Binary Type</td></tr>" +
  "<tr><td>CE FA ED FE</td><td>32 bit binary application.</td></tr>" +
  "<tr><td>CF FA ED FE</td><td>64 bit binary application.</td></tr>" +
  "</table><br />" +
  "Signature type CA FE BA BE is used for universal binaries.<br /><br />" +
  "A universal binary has more than one binary application in the file for different core types which begin with Mac Headers as well.<br /><br />" +
  "It is not useful most of the time since majority of all systems run x86 instructions, or ARM instructions natively.</html>";

  private static final String CPU_type1 = "<html>This is the processor type that the binary is meant to run on.<br /><br />";
  private static final String CPU_type2 = "<table border='1'>" +
  "<tr><td>Core</td><td>Type value Hex</td></tr>" +
  "<tr><td>VAX</td><td>01</td></tr>" +
  "<tr><td>MC680x0</td><td>06</td></tr>" +
  "<tr><td>X86</td><td>07</td></tr>" +
  "<tr><td>MC98000</td><td>0A</td></tr>" +
  "<tr><td>HPPA</td><td>0B</td></tr>" +
  "<tr><td>ARM</td><td>0C</td></tr>" +
  "<tr><td>MC88000</td><td>0D</td></tr>" +
  "<tr><td>SPARC</td><td>0E</td></tr>" +
  "<tr><td>I860</td><td>0F</td></tr>" +
  "<tr><td>POWERPC</td><td>12</td></tr>" +
  "</table><br />" +
  "The most used types are 07, and 0C which are x86, and ARM.<br /><br />" +
  "MacOS is switching from Intel x86 cores to ARM.<br /><br />" +
  "Note that MacOS and iPhone also use the same Mach-O format. iPhone used ARM way before MacOS did.</html>";

  private static final String[] MacHeaderInfo = new String[]
  {
    Singatures,
    CPU_type1 + "The first two hex digits is the CPU type.<br /><br />" +
    "The last two hex digits are 01 for 64 bit, and 00 for 32 bit version of the core.<br /><br />" + CPU_type2,
    "<html>The CPU sub type is used to specify features the core supports.</html>",
    "<html>File Type.</html>",
    "<html>Number of load commands.</html>",
    "<html>The size of all the load commands.</html>",
    "<html>Flag settings.</html>",
    "<html>This is reserved for use with 64 bit programs in the future.</html>"
  };

  public void MacHInfo( int i )
  {
    if( i < 0 )
    {
      info("<html>The MacOS header identifies the type of core the machine code binary is intended to run on.<br /><br />" +
      "It also specifies the number of loading commands to map and load the binary into memory.</html>");
    }
    else
    {
      info( MacHeaderInfo[ i ] );
    }
  }

  private static final String[] UMacHeaderInfo = new String[]
  {
    Singatures,
    "<html>Number of binaries in the universal binary.</html>",
    "<html>Binary application information.</html>",
    CPU_type1 + "The last two hex digits is the CPU type.<br /><br />" +
    "The first two hex digits are 01 for 64 bit, and 00 for 32 bit version of the core.<br /><br />" + CPU_type2,
    "<html>The CPU sub type is used to specify features the core supports.</html>",
    "<html>File position to application.</html>",
    "<html>The size of the application in the file.</html>",
    "<html>Section alignment in power of 2.</html>"
  };

  public void UMacHInfo( int i )
  {
    if( i < 0 )
    {
      info("<html>The MacOS header identifies the type of core the machine code binary is intended to run on.<br /><br />" +
      "It also specifies the number of loading commands to map and load the binary into memory.</html>");
    }
    else
    {
      info( i > 1 ? UMacHeaderInfo[ ( ( i - 2 ) % 6 ) + 2 ] : UMacHeaderInfo[ i ] );
    }
  }
}
