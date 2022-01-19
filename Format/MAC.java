package Format;

import swingIO.*;
import swingIO.tree.*;
import javax.swing.tree.*;
import Format.MACDecode.*;

public class MAC extends Data implements JDEventListener
{
  //The binary tree stores which descriptor to set from the integer "ref".

  private JDNode root;

  //A temporary descriptor holder that is used when reading data and adding descriptor to the list.

  private Descriptor DTemp;

  //The descriptors explain the binary data sections as it is read.

  private static java.util.ArrayList<Descriptor> des = new java.util.ArrayList<Descriptor>();

  //This integer is used to keep track of the descriptors added to the list adn set to a node on the open event.

  private static int ref = 0;
  
  public MAC() throws java.io.IOException
  {
    tree.setEventListener( this ); file.Events = false;

    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true); tree.setShowsRootHandles(true); root = new JDNode( fc.getFileName() + ( fc.getFileName().indexOf(".") > 0 ? "" : ".exe" ), -1 );
    
    //Begin reading the file header.

    DTemp = new Descriptor( file );

    root.add( new JDNode("Mac Header.h", new long[]{ 0, ref++ } ) ); des.add( DTemp );

    //Begin reading the MacOS header.

    DTemp.LUINT32("Signature"); int sig = (int)DTemp.value;

    Universal = sig == -1095041334; is64bit = sig == -17958193;

    DTemp.setEvent( Universal ? this::UMacHInfo : this::MacHInfo );

    if( !Universal )
    {
      DTemp.UINT32("CPU Type"); coreType = (int)DTemp.value;
      DTemp.UINT32("CPU Sub Type");
      DTemp.LUINT32("File Type");
      DTemp.LUINT32("Commands");
      DTemp.LUINT32("Commands Size");
      DTemp.LUINT32("Flags");

      if( is64bit ){ DTemp.UINT32("Reserved"); }
    }
    else
    {
      DTemp.UINT32("Binaries"); int b = (int)DTemp.value;

      JDNode App;
      
      for( int i = 0; i < b; i++ )
      {
        DTemp.Array("Mac Binary #" + i + "", 20 );
        DTemp.UINT32("CPU Type");
        DTemp.UINT32("CPU Sub Type");
        DTemp.UINT32("File Offset");

        App = new JDNode("App #" + i + "", new long[] { 1, (int)DTemp.value } ); App.add( new JDNode("xxxxxxxxxxxxxxxxxx") );

        root.add( App );

        DTemp.UINT32("Size");
        DTemp.UINT32("Align");
      }
    }

    //Set binary tree view, and enable IO system events.
      
    ((DefaultTreeModel)tree.getModel()).setRoot(root); file.Events = true;

    //Set the selected node.
  
    tree.setSelectionPath( new TreePath( ((DefaultMutableTreeNode)root.getFirstChild()).getPath() ) );

    //Make it as if we clicked and opened the node.

    open( new JDEvent( this, "", new long[]{ 0, 0 } ) );
  }

  public void Uninitialize() { des = new java.util.ArrayList<Descriptor>(); ref = 0; DTemp = null; }

  public void open(JDEvent e)
  {
    if( e.getID().equals("UInit") ) { Uninitialize(); }

    //Command 0 sets a descriptor for a section of data in the binary tree.

    if( e.getArg( 0 ) == 0 ) { ds.setDescriptor( des.get( (int)e.getArg( 1 ) ) ); }

    //Open application header within universal binaries.

    if( e.getArg( 0 ) == 1 )
    {
      JDNode root = (JDNode)tree.getLastSelectedPathComponent(), node = new JDNode("");
      
      if( root.getChildCount() > 0 ){ node = (JDNode)root.getFirstChild(); }

      int Offset = (int)e.getArg(1);

      file.Events = false;

      try
      {
        file.seek( Offset ); DTemp = new Descriptor( file );

        node.setUserObject("Mac Header.h"); node.setArgs( new long[]{ 0, ref++ } ); des.add( DTemp );

        //Begin reading the MacOS header.

        DTemp.LUINT32("Signature"); int sig = (int)DTemp.value;

        is64bit = sig == -17958193;

        DTemp.setEvent( this::MacHInfo );

        DTemp.LUINT32("CPU Type"); coreType = (int)DTemp.value;
        DTemp.LUINT32("CPU Sub Type");
        DTemp.LUINT32("File Type");
        DTemp.LUINT32("Commands");
        DTemp.LUINT32("Commands Size");
        DTemp.LUINT32("Flags");

        if( is64bit ){ DTemp.LUINT32("Reserved"); }
      }
      catch(Exception er) { er.printStackTrace(); }

      file.Events = true; tree.expandPath( tree.getLeadSelectionPath() );
    }
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
  "The most used types are 7, and 12 which are x86, and ARM.<br /><br />" +
  "MacOS is switching from Intel x86 cores to ARM.<br /><br />" +
  "Note that MacOS and iPhone also use the same Mach-O format and that iPhone used ARM way before MacOS did.</html>";

  private static final String[] MacHeaderInfo = new String[]
  {
    Singatures,
    CPU_type1 + "The first hex digit is the CPU type. This is because the integer is in little endian byte order.<br /><br />" + CPU_type2,
    "<html>CPU Sub type.</html>",
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
    CPU_type1 + "The last hex digit is the CPU type.  This is because the integer is in big endian byte order.<br /><br />" + CPU_type2,
    "<html>CPU Sub type.</html>",
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