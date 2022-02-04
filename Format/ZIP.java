package Format;

import swingIO.*;
import swingIO.tree.*;
import javax.swing.tree.*;

public class ZIP extends Window.Window implements JDEventListener
{
  private JDNode root;

  private static Descriptor DTemp;

  private static int ref = 0;

  private static java.util.ArrayList<swingIO.Descriptor> des = new java.util.ArrayList<swingIO.Descriptor>();
  
  public ZIP() throws java.io.IOException
  {
    //Setup.

    tree.setEventListener( this ); file.Events = false;

    //Begin reading the ZIP file.

    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true); tree.setShowsRootHandles(true); root = new JDNode( fc.getFileName() + ( fc.getFileName().indexOf(".") > 0 ? "" : ".zip" ), -1 );

    JDNode r = root; //The current path node.

    int level = 1;
    
    //Begin reading the file header.

    long end = file.length(); while( file.getFilePointer() < end )
    {
      DTemp = new Descriptor( file ); des.add( DTemp ); DTemp.setEvent( this::zipInfo );

      //Example of reading and defining a value.

      DTemp.LUINT32("Signature"); int sig = (int)DTemp.value;

      //Old path and new path.

      String[] path, opath;

      if( sig == 0x04034B50 )
      {
        DTemp.LUINT16("Min Version");
        DTemp.LUINT16("Flag");
        DTemp.LUINT16("Compression method");
        DTemp.LUINT16("Last Modified (Time)");
        DTemp.LUINT16("Last Modified (Date)");
        DTemp.LUINT32("CRC-32");
        DTemp.LUINT32("Compressed Size"); int size = (int)DTemp.value;
        DTemp.LUINT32("Uncompressed Size");
        DTemp.LUINT16("File name Length"); int flen = (short)DTemp.value;
        DTemp.LUINT16("Data felid len"); int elen = (short)DTemp.value;
        DTemp.String8("File name", flen ); String name = (String)DTemp.value;
        DTemp.Other("Data Felid", elen );

        path = name.split("/");

        while( path.length < level ) { level--; r = (JDNode)r.getParent(); }

        while( path.length > level ) { JDNode h = new JDNode( path[ level - 1 ], new long[]{ 0, ref } ); r.add( h ); r = h; level++; }

        JDNode h = new JDNode( path[ path.length - 1 ], new long[]{ 0, ref++ } );

        if( size > 0 ) { h.add( new JDNode("File Data.h", new long[]{ 1, file.getFilePointer(), file.getFilePointer() + size - 1 } ) ); }
    
        r.add( h ); level = path.length; if( size == 0 ){ level += 1; r = h; }
        
        //opath = name.split("/"); We can path walk usually without having to compare the full path.
        
        file.skipBytes( size );
      }
      else { break; }
    }

    //Set binary tree view, and enable IO system events.
      
    ((DefaultTreeModel)tree.getModel()).setRoot(root); file.Events = true;
      
    //Set the selected node.
  
    tree.setSelectionPath( new TreePath( ((DefaultMutableTreeNode)root.getFirstChild()).getPath() ) );

    //Make it as if we clicked and opened the node.

    open( new JDEvent( this, "", new long[]{ 0, 0 } ) );
  }

  //This method is called when opening a new file format to get rid of variables and arrays needed by this format reader by
  //setting values and arrays to null. If this is not done, then program will eventually crash when loading too many files.

  public void Uninitialize() { des.clear(); ref = 0; }

  //This event is called when the user clicks on an tree node.

  public void open(JDEvent e)
  {
    //This is given to the open method when the user goes to load a new file format. It triggers the Uninitialize method.

    if( e.getID().equals("UInit") ) { Uninitialize(); }

    //When the user clicks on the "File header.h" node we will receive the array arguments associated with the node.

    else if( e.getArg(0) == 0 )
    {
      ds.setDescriptor( des.get( (int)e.getArg(1) ) );
    }

    //Select bytes Offset.

    else if( e.getArg( 0 ) == 1 )
    {
      ds.clear(); info("<html></html>");

      try { file.seek( e.getArg(1) ); Offset.setSelected( e.getArg(1), e.getArg(2) ); } catch( java.io.IOException er ) { }
    }
  }

  //The ZIP file header.

  private static final String[] zipInfo = new String[]
  {
    "<html>This is the file signature of a compressed zip file.</html>",
    "<html>Version needed to extract (minimum).</html>",
    "<html>General purpose bit flag.</html>",
    "<html>Compression method; none = 0, DEFLATE = 8.</html>",
    "<html>File last modification time.</html>",
    "<html>File last modification date.</html>",
    "<html>CRC-32 of uncompressed data. This is the number of zeros that should exist in the binary file.<br /><br />" +
    "If the CRC does not match the count of zeros in binary in the file we know there is something wrong.</html>",
    "<html>Compressed size. This is the size of the data after this PK signature.</html>",
    "<html>Uncompressed file size. This is the file size after we decompress the file.<br /><br />" +
    "In some cases this is 0 as it is a folder. The extra data field defines properties for the folder.</html>",
    "<html>File name length in bytes.</html>",
    "<html>Extra field length. The extra felid is useful for folder and file attributes and properties. For example if the file/folder should be hidden, or visible.<br /><br />" +
    "Some PK signatures have a zero size uncompressed as they only define attributes to a folder.</html>",
    "<html>The zip file format uses the full path to the file then name of the file.</html>",
    "<html>Extra field. Mainly used for file/folder properties or attributes which will be used by the OS.</html>"
  };

  public void zipInfo( int i )
  {
    if( i < 0 )
    {
      info("<html>The zip file format is used as a container for mobile, and java applications, and also Microsoft office documents as well as being useful for users to store files as a compressed zip file.<br /><br />" +
      "Java uses the zip format to store application files as a single file as a runnable file called an java jar.<br /><br />" +
      "Android APK applications are stored in zip files to save space, and to keep applications organized.<br /><br />" +
      "Apple iPhone IPA applications are stored in zip files to also save space and to keep applications organized.<br /><br />" +
      "Microsoft stores office document files into compressed zip files to save space and to keep pictures and models used in the office document organized as one file.<br /><br />" +
      "You can open these files using a zip program if you like and decompress all the files stored in the Android APK, or IPA, or java JAR, or microsoft docx file.</html>");
    }
    else
    {
      info( zipInfo[ i ] );
    }
  }
}