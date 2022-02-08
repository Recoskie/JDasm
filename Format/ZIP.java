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

    int level = 0, change = 0;

    //Parse zip in 4k buffer.

    long pos = 0, pkPos = 0; int buf = 0; byte[] b = new byte[4096]; file.read(b);

    //Signature.

    int sig = 0;

    //Constants to know when the buffer needs to be updated.
  
    int sigEnd = b.length - 4; //Next 4 bytes must be available to read signature.
    int pkEnd = b.length - 30; //There must be 30 bytes left to read the PK header.

    //Old path and new path.

    String[] path, opath = new String[]{};
    boolean exists = false;

    //Zip pk entry.

    int size, strLen, extData;
    String name;

    //Data descriptor entry.

    int data = 0;
    
    //Begin reading the file header.

    long end = file.length(); while( ( pos + buf ) < end )
    {
      if( buf >= sigEnd )
      {
        pos += buf; file.seek( pos ); file.read(b); buf = 0;
      }

      sig = ( b[buf + 3] << 24 ) | ( b[buf + 2] << 16 ) | ( b[buf + 1] << 8 ) | b[buf];

      if( sig == 0x04034B50 )
      {
        data = 0; pkPos = buf + pos;

        if( buf >= pkEnd )
        {
          pos += buf; file.seek( pos ); file.read( b ); buf = 0;
        }

        size = ( ( b[buf + 21] & 0xFF ) << 24 ) | ( ( b[buf + 20] & 0xFF ) << 16 ) | ( ( b[buf + 19] & 0xFF ) << 8 ) | ( b[buf + 18] & 0xFF );

        strLen = ( ( b[buf + 27] & 0xFF ) << 8 ) | ( b[buf + 26] & 0xFF );
        extData = ( ( b[buf + 29] & 0xFF ) << 8 ) | ( b[buf + 28] & 0xFF );

        buf += 30;

        if( ( strLen + buf ) >= b.length )
        {
          pos += buf; file.seek( pos ); file.read( b ); buf = 0;
        }
        
        name = ""; for( int i = 0; i < strLen; name = name + ((char)b[buf + i++ ]) );

        buf += strLen + extData;

        path = name.split("/");

        change = 0; for( int e = Math.min(path.length,opath.length); change < e; change++ )
        {
          if( !path[change].equals(opath[change]) ){ break; }
        }

        while( change < level ) { level--; r = (JDNode)r.getParent(); }

        while( path.length > level )
        {
          //Check if node exists.

          exists = false;
          
          try
          {
            for( int e = r.getChildCount(), el = 0; el < e; el++ )
            {
              if( path[level].equals(r.getChildAt(el).toString()))
              {
                exists = true; r = (JDNode)r.getChildAt(el);
              }
            }
          } catch( Exception er ){}

          //Node does not exist.

          if(!exists) { JDNode h = new JDNode( path[ level ], new long[]{ 2, pkPos } ); r.add( h ); r = h; }

          level++;
        }

        if( size > 0 ) { r.add( new JDNode("File Data.h", new long[]{ 1, pos + buf, pos + buf + size - 1 } ) ); }
        
        opath = name.split("/");
        
        buf += size; if( buf >= sigEnd )
        {
          pos += buf; file.seek( pos ); file.read( b ); buf = 0;
        }
      }

      //The data descriptor tells us the size of the compressed data after we have read it.

      else if( sig == 0x08074B50 )
      {
        r.add( new JDNode("File Data.h", new long[]{ 1, pos + buf - data, pos + buf - 1 } ) );
        r.add( new JDNode( "Data info.h", new long[]{ 3, pos + buf } ) );

        buf += 16;
      }

      //The central directory.

      else if( sig == 0x02014b50 ){ break; }

      //The size of the riles compressed data is identified by the data descriptor after the files data.
      //This happens when the win zip program does not know the size before hand.
      //The size of the data we read before the data descriptor should match the size in the data descriptor.

      else
      {
        buf++; data++;
      }
    }

    //Set binary tree view, and enable IO system events.
      
    ((DefaultTreeModel)tree.getModel()).setRoot(root); file.Events = true;
      
    //Zip info.

    info("<html>The zip file format is used as a container for mobile (android, iPhone) applications, and java applications, and also Microsoft office documents as well as being useful for users to store files as a compressed zip file.<br /><br />" +
      "Java uses the zip format to store application files as a single file as a runnable file called an java jar.<br /><br />" +
      "Android APK applications are stored in zip files to save space, and to keep applications organized.<br /><br />" +
      "Apple iPhone IPA applications are stored in zip files to also save space and to keep applications organized.<br /><br />" +
      "Disassembling android and iPhone apps is supported by JDisassembly, but you will first need to decompress the application file.<br /><br />" +
      "Microsoft stores office document files into compressed zip files to save space and to keep pictures and models used in the office document organized as one file.<br /><br />" +
      "You can open these files using a zip program if you like and decompress all the files stored in the Android APK, or IPA, or java JAR, or microsoft docx file.</html>"
    );
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

      if( javax.swing.JOptionPane.showConfirmDialog(null, "Would you like to open this file?", null, javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.YES_OPTION )
      {
        if( main != null )
        {
          String[] paths = tree.getLeadSelectionPath().toString().split(", ");
          String path = ""; for( int i = 1, end = paths.length - 1; i < end; path += paths[i++] + "/" );
          path = path.substring(0,path.length()-1);
          main.actionPerformed( new java.awt.event.ActionEvent(this, java.awt.event.ActionEvent.ACTION_PERFORMED, "ZOpen" + path ) );
        }
      }
    }

    //Load and set descriptor to node.

    else if( e.getArg( 0 ) == 2 )
    {
      try
      {
        file.Events = false; file.seek( e.getArg(1) );

        JDNode n = (JDNode)tree.getLastSelectedPathComponent();

        DTemp = new Descriptor( file );

        des.add( DTemp ); DTemp.setEvent( this::zipInfo );

        n.setArgs( new long[]{ 0, ref++ });

        DTemp.LUINT32("Signature");
        DTemp.LUINT16("Min Version");
        DTemp.LUINT16("Flag");
        DTemp.LUINT16("Compression method");
        DTemp.LUINT16("Last Modified (Time)");
        DTemp.LUINT16("Last Modified (Date)");
        DTemp.LUINT32("CRC-32");
        DTemp.LUINT32("Compressed Size");
        DTemp.LUINT32("Uncompressed Size");
        DTemp.LUINT16("File name Length"); int flen = (short)DTemp.value;
        DTemp.LUINT16("Data felid len"); int elen = (short)DTemp.value;
        DTemp.String8("File name", flen );
        if( elen > 0 ) { DTemp.Other("Data Felid", elen ); }

        file.Events = true;

        ds.setDescriptor( DTemp );
      }
      catch(Exception er) {}
    }
    else if( e.getArg(0) == 3 )
    {
      try
      {
        file.Events = false;

        JDNode n = (JDNode)tree.getLastSelectedPathComponent();

        file.seek( e.getArg(1) ); DTemp = new Descriptor( file ); des.add( DTemp );

        DTemp.setEvent( this::dataInfo );

        DTemp.LUINT32("Signature");
        DTemp.LUINT32("CRC-32");
        DTemp.LUINT32("Compressed Size");
        DTemp.LUINT32("Uncompressed Size");

        n.setArgs( new long[]{ 0, ref++ } ); file.Events = true;

        ds.setDescriptor( DTemp );
      }
      catch( Exception er ){}
    }
  }

  //The ZIP file header.

  private static final String[] zipInfo = new String[]
  {
    "<html>50 4B 03 04 is the start of a file signature in a compressed zip file.</html>",
    "<html>Version needed to extract (minimum). The version number is convert to an decimal value.<br /><br />" +
    "in the case of version 122 it would mean 12.2v. In the case of 20 it means 2.0v.</html>",
    "<html>The flag is meant to be viewed in binary. Each of the 16 binary digits if set one signifies an setting." +
    "The table bellow shows what setting each digit implies.<br /><br />" +
    "<table border='1'>" +
    "<tr><td>Digit</td><td>Description</td></tr>" +
    "<tr><td>0000000000001000</td><td>If this bit is set, the fields CRC-32, compressed size and uncompressed size are set to zero in the local header. The correct values are put in the data descriptor immediately following the compressed data.</td></tr>" +
    "<tr><td>0000000000100000</td><td>If this bit is set, this indicates that the file is compressed patched data.</td></tr>" +
    "<tr><td>0000000001000000</td><td>Strong encryption. If this bit is set, you MUST set the version needed to extract value to at least 50 and you MUST also set bit 0. If AES encryption is used, the version needed to extract value MUST be at least 51.</td></tr>" +
    "<tr><td>0000100000000000</td><td>Language encoding flag (EFS).</td></tr>" +
    "<tr><td>0010000000000000</td><td>Set when encrypting the Central Directory.</td></tr>" +
    "</table></html>",
    "<html>Compression method.<br /><br />" +
    "<table border='1'>" +
    "<tr><td>Value</td><td>Compression</td></tr>" +
    "<tr><td>0</td><td>The file is stored (no compression).</td></tr>" +
    "<tr><td>1</td><td>The file is Shrunk.</td></tr>" +
    "<tr><td>2</td><td>The file is Reduced with compression factor 1.</td></tr>" +
    "<tr><td>3</td><td>The file is Reduced with compression factor 2.</td></tr>" +
    "<tr><td>4</td><td>The file is Reduced with compression factor 3.</td></tr>" +
    "<tr><td>5</td><td>The file is Reduced with compression factor 4.</td></tr>" +
    "<tr><td>6</td><td>The file is Imploded.</td></tr>" +
    "<tr><td>7</td><td>Reserved for Tokenizing compression algorithm.</td></tr>" +
    "<tr><td>8</td><td>The file is Deflated.</td></tr>" +
    "<tr><td>9</td><td>Enhanced Deflating using Deflate64(tm).</td></tr>" +
    "<tr><td>10</td><td>PKWARE Data Compression Library Imploding (old IBM TERSE).</td></tr>" +
    "<tr><td>11</td><td>Reserved by PKWARE.</td></tr>" +
    "<tr><td>12</td><td>File is compressed using BZIP2 algorithm.</td></tr>" +
    "<tr><td>13</td><td>Reserved by PKWARE.</td></tr>" +
    "<tr><td>14</td><td>LZMA.</td></tr>" +
    "<tr><td>15</td><td>Reserved by PKWARE.</td></tr>" +
    "<tr><td>16</td><td>IBM z/OS CMPSC Compression.</td></tr>" +
    "<tr><td>17</td><td>Reserved by PKWARE.</td></tr>" +
    "<tr><td>18</td><td>File is compressed using IBM TERSE (new).</td></tr>" +
    "<tr><td>19</td><td>IBM LZ77 z Architecture.</td></tr>" +
    "<tr><td>20</td><td>Deprecated (use method 93 for zstd).</td></tr>" +
    "<tr><td>93</td><td>Zstandard (zstd) Compression.</td></tr>" +
    "<tr><td>94</td><td>MP3 Compression.</td></tr>" +
    "<tr><td>95</td><td>XZ Compression.</td></tr>" +
    "<tr><td>96</td><td>JPEG variant.</td></tr>" +
    "<tr><td>97</td><td>WavPack compressed data.</td></tr>" +
    "<tr><td>98</td><td>PPMd version I, Rev 1.</td></tr>" +
    "<tr><td>99</td><td>AE-x encryption marker (see APPENDIX E).</td></tr>" +
    "</table></html>",
    "<html>File last modification time.</html>",
    "<html>File last modification date.</html>",
    "<html>CRC-32 of uncompressed data. This is the number of zeros that should exist in the binary file.<br /><br />" +
    "If the CRC does not match the count of zeros in binary in the file we know there is something wrong.</html>",
    "<html>Compressed size. This is the size of the data after this PK signature.</html>",
    "<html>Uncompressed file size. This is the file size after we decompress the file.<br /><br />" +
    "In some cases this is 0 as it is a folder. The extra data field defines properties for the folder.<br /><br /" +
    "Details for the breakdown of the extra data field will be added soon.</html>",
    "<html>File name length in bytes.</html>",
    "<html>Extra field length. The extra felid is useful for folder and file attributes and properties.<br /><br />" +
    "Some PK signatures have a zero size uncompressed as they only define attributes to a folder.<br /><br />" +
    "The extra data felid can also be used to extend the zip file format.</html>",
    "<html>The zip file format uses the full path to the file then name of the file.</html>",
    "<html>Extra field. Usually a set of 2 byte pairs that add additional information about the file or entire.</html>"
  };

  //Data descriptor.

  private static final String[] dataInfo = new String[]
  {
    "<html>This is the data descriptor signature. Marks the end of a compressed file data.</html>",
    "<html>CRC-32 of uncompressed data. This is the number of zeros that should exist in the binary file.<br /><br />" +
    "If the CRC does not match the count of zeros in binary in the file we know there is something wrong.</html>",
    "<html>Compressed size. This is the size of the data before this sdata signature.</html>",
    "<html>Uncompressed file size. This is the file size after we decompress the file.</html>"
  };

  public void zipInfo( int i )
  {
    if( i < 0 )
    {
      info("<html>All file in the zip begin with a PK signature. The file compressed data is right after the PK parameters.<br /><br />" +
      "The next file signature is after the compressed file size parameter.<br /><br />" +
      "In some cases a signature code (data descriptor) is used to identify the end of the compressed file data in some cases.<br /><br />" +
      "The data descriptor tells us how many bytes the compressed data is which should match the number of bytes we read after the PK parameters.<br /><br />" +
      "Most of the time only PK signatures exist and the number of bytes for the compressed file is set in the PK header.<br /><br />" +
      "The only time we do not set the compressed file size in the PK header is when we do not know the compressed file size till after the file was compressed.<br /><br />" +
      "The flag parameter can also be adjusted to signify that the data descriptor marks the end of the files data.</html>");
    }
    else
    {
      info( zipInfo[ i ] );
    }
  }

  public void dataInfo( int i )
  {
    if( i < 0 )
    {
      info("<html>In the case that the data descriptor setting is set in the PK header then the size of the compressed file was not known.<br /><br />" +
      "Instead the Data descriptor signature marks the end of the files data.<br /><br />" +
      "The Data descriptor tells us how big the compressed file is which should match the number of bytes we read before encountering the data descriptor signature.<br /><br />" +
      "The data descriptor also stores the files original size, and has an CRC count which is numbers of zero digits that should exist in the file once decompress.<br /><br />" +
      "The CRC is very important as it can be used to know if the decompressed file matches the original.</html>");
    }
    else
    {
      info( dataInfo[ i ] );
    }
  }
}