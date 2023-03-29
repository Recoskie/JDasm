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

    long size = 0;
    int strLen, extData, cLen;
    byte[] bn; String name;

    //Data descriptor entry.

    int data = 0, dir = 1;

    //Central directory.
  
    JDNode c = new JDNode("Central Directory", "dir");
    
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
        pkPos = buf + pos;

        if( data > 0 ) { r.add( new JDNode( "File Data.h", new long[]{ 1, pkPos - data, pkPos - 1 } ) ); data = 0; }

        if( buf >= pkEnd )
        {
          pos += buf; file.seek( pos ); file.read( b ); buf = 0;
        }

        size = ( ( ( b[buf + 21] & 0xFF ) << 24 ) | ( ( b[buf + 20] & 0xFF ) << 16 ) | ( ( b[buf + 19] & 0xFF ) << 8 ) | ( b[buf + 18] & 0xFF ) ) & 0xFFFFFFFFl;

        if( size == 0xFFFFFFFFl ){ size = 0; } //This is not allowed. If this happens it is a ZIP64 signature.

        strLen = ( ( b[buf + 27] & 0xFF ) << 8 ) | ( b[buf + 26] & 0xFF );
        extData = ( ( b[buf + 29] & 0xFF ) << 8 ) | ( b[buf + 28] & 0xFF );

        buf += 30;

        if( ( strLen + buf ) >= b.length )
        {
          pos += buf; file.seek( pos ); file.read( b ); buf = 0;
        }
        
        bn = new byte[strLen]; for( int i = 0; i < strLen; bn[i] = b[ buf + i++ ] ); name = new String( bn, java.nio.charset.StandardCharsets.UTF_8 );

        buf += strLen;

        if( ( extData + buf ) >= b.length )
        {
          pos += buf; file.seek( pos ); file.read( b ); buf = 0;
        }

        //Check for zip64 compressed file size.

        extData += buf - 4; while( buf < extData )
        {
          if( b[buf] == 1 && b[buf + 1] == 0 && ( ( ( b[buf+3] & 0xFF ) << 8 ) | ( b[buf + 2] & 0xFF ) ) >= 16 )
          {
            size = ( ( b[buf + 15] & 0xFF ) << 56 ) | ( ( b[buf + 14] & 0xFF ) << 48 ) | ( ( b[buf + 13] & 0xFF ) << 40 ) | ( ( b[buf + 12] & 0xFF ) << 32 ) |
            ( ( b[buf + 11] & 0xFF ) << 24 ) | ( ( b[buf + 10] & 0xFF ) << 16 ) | ( ( b[buf + 9] & 0xFF ) << 8 ) | ( b[buf + 8] & 0xFF ); buf = extData;
          }
          else { buf += ( ( ( b[buf + 3] & 0xFF ) << 8 ) | ( b[buf + 2] & 0xFF ) ) + 4; }
        }

        buf = extData + 4; path = name.split("/");

        change = 0; for( int e = path.length > opath.length ? opath.length : path.length ; change < e; change++ )
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
        
        opath = name.split("/"); buf += size; data += size;
      }

      //The data descriptor tells us the size of the compressed data after we have read it.

      else if( sig == 0x08074B50 )
      {
        r.add( new JDNode( "File Data.h", new long[]{ 1, pos + buf - data, pos + buf - 1 } ) );
        r.add( new JDNode( "Data info.h", new long[]{ 3, pos + buf } ) );

        buf += 16; data = 0;
      }

      //The central directory.

      else if( sig == 0x02014B50 )
      {
        if( data > 0 ) { r.add( new JDNode( "File Data.h", new long[]{ 1, pkPos - data, pkPos - 1 } ) ); data = 0; }

        if( ( buf + 46 ) > b.length )
        {
          pos += buf; file.seek( pos ); file.read( b ); buf = 0;
        }

        strLen = ( ( b[buf + 29] & 0xFF ) << 8 ) | ( b[buf + 28] & 0xFF );
        extData = ( ( b[buf + 31] & 0xFF ) << 8 ) | ( b[buf + 30] & 0xFF );
        cLen = ( ( b[buf + 33] & 0xFF ) << 8 ) | ( b[buf + 32] & 0xFF );

        c.add( new JDNode( "Directory #" + (dir++) + ".h", new long[]{ 4, pos + buf } ) );

        buf += 46 + strLen + extData + cLen;
      }
      else if( sig == 0x06064B50 )
      {
        if( ( buf + 12 ) > b.length )
        {
          pos += buf; file.seek( pos ); file.read( b ); buf = 0;
        }

        extData = ( ( b[buf + 11] & 0xFF ) << 56 ) | ( ( b[buf + 10] & 0xFF ) << 48 ) | ( ( b[buf + 9] & 0xFF ) << 40 ) | ( ( b[buf + 8] & 0xFF ) << 32 ) |
        ( ( b[buf + 7] & 0xFF ) << 24 ) | ( ( b[buf + 6] & 0xFF ) << 16 ) | ( ( b[buf + 5] & 0xFF ) << 8 ) | ( b[buf + 4] & 0xFF );
        
        c.add( new JDNode("Directory End64.h", new long[]{ 6, pos + buf }) ); buf += ( extData - 44 ) + 56;
      }
      else if( sig == 0x07064B50 )
      {
        c.add( new JDNode("Directory Loc64.h", new long[]{ 7, pos + buf }) ); buf += 20;
      }
      else if( sig == 0x06054B50 )
      {
        if( ( buf + 22 ) > b.length )
        {
          pos += buf; file.seek( pos ); file.read( b ); buf = 0;
        }

        cLen = ( ( b[buf + 21] & 0xFF ) << 8 ) | ( b[buf + 20] & 0xFF );

        c.add( new JDNode("Directory End.h", new long[]{ 5, pos + buf }) );

        buf += cLen + 22;
      }

      //The size of the files compressed data is identified by the data descriptor after the files data.
      //This happens when the win zip program does not know the size before hand.
      //The size of the data we read before the data descriptor should match the size in the data descriptor.

      else
      {
        buf++; data++;
      }
    }

    if( c.getChildCount() > 0 ){ root.add( c ); }

    //Set binary tree view, and enable IO system events.
      
    ((DefaultTreeModel)tree.getModel()).setRoot(root); file.Events = true;
      
    //Zip info.

    ds.clear();

    info("<html>The zip file format is used as a container for mobile (android, iPhone) applications, and java applications, and also Microsoft office documents as well as being useful for users to store files as a compressed zip file.<br /><br />" +
      "Java uses the zip format to store application files as a single file as a runnable file called an java jar.<br /><br />" +
      "Android APK applications are stored in zip files to save space, and to keep applications organized.<br /><br />" +
      "Apple iPhone IPA applications are stored in zip files to also save space and to keep applications organized.<br /><br />" +
      "Disassembling android and iPhone apps is supported by JDisassembly, but you will first need to find the application file in the IPA, or APK file.<br /><br />" +
      "Microsoft stores office document files into compressed zip files to save space and to keep pictures and models used in the office document organized as one file.</html>"
    );
  }

  //This method is called when opening a new file format to get rid of variables and arrays needed by this format reader by
  //setting values and arrays to null. If this is not done, then program will eventually crash when loading too many files.

  public void Uninitialize() { des.clear(); ref = 0; }

  //Decode the extended data field information.

  public void extendedData()
  {
    file.Events = false;

    byte[] d = new byte[]{}; String out = "<html>Extra data field is a set of 2 byte pairs (code pair type) with a value that specifies the number of bytes to read.<br /><br />" +
    "The extra data field adds additional information about the file or entire, or extends values.<br /><br />";

    out += "<table border='1'><tr><td>Description</td><td>Hex</td><td>Value</td></tr>";

    //Get the data.

    try { file.seek( Offset.selectPos() ); d = new byte[ (int)(Offset.selectEnd() - file.getFilePointer()) + 1 ]; file.read(d); } catch( java.io.IOException er ) { }

    //Analyze the data.

    int pos = 0, end = d.length - 3; int CMD = 0; String Hex = ""; long val = 0; while( pos < end )
    {
      CMD = ( ( d[pos + 1] & 0xFF ) << 8 ) | ( d[pos] & 0xFF );
      Hex = String.format("%1$02X", d[pos] ) + " " + String.format("%1$02X", d[pos + 1] );

      //The unix time date stamp.

      if( CMD == 0x5455 )
      {
        out += "<tr><td>Unix Time Date stamps (0x" + String.format("%1$04X", CMD ) + ").</td><td>" + Hex + "</td><td>" + CMD + "</td></tr>";

        int size = ( ( d[pos + 3] & 0xFF ) << 8 ) | ( d[pos + 2] & 0xFF ); Hex = String.format("%1$02X", d[pos + 2] ) + " " + String.format("%1$02X", d[pos + 3] );

        out += "<tr><td>Time stamp len.</td><td>" + Hex + "</td><td>" + ( size > 28 ? "Error (" + size + " > 13)" : size ) + "</td></tr>"; size = size > 13 ? 0 : size; pos += 4;

        if( size > 0 )
        {
          val = d[pos] & 0xFF; Hex = String.format("%1$02X", d[pos] );

          out += "<tr><td>Flag settings.</td><td>" + Hex + "</td><td>" + val + "</td></tr>"; pos += 1; size -= 1;
        }

        if( size > 0 )
        {
          val = ( ( d[pos + 3] & 0xFF ) << 24 ) | ( ( d[pos + 2] & 0xFF ) << 16 ) | ( ( d[pos + 1] & 0xFF ) << 8 ) | ( d[pos] & 0xFF );
          Hex = String.format("%1$02X", d[pos] ) + " " + String.format("%1$02X", d[pos + 1] )+ " " + String.format("%1$02X", d[pos + 2] )+ " " + String.format("%1$02X", d[pos + 3] );

          out += "<tr><td>Last Modification.</td><td>" + Hex + "</td><td>" + val + "</td></tr>"; pos += 4; size -= 4;
        }

        if( size > 0 )
        {
          val = ( ( d[pos + 3] & 0xFF ) << 24 ) | ( ( d[pos + 2] & 0xFF ) << 16 ) | ( ( d[pos + 1] & 0xFF ) << 8 ) | ( d[pos] & 0xFF );
          Hex = String.format("%1$02X", d[pos] ) + " " + String.format("%1$02X", d[pos + 1] )+ " " + String.format("%1$02X", d[pos + 2] )+ " " + String.format("%1$02X", d[pos + 3] );

          out += "<tr><td>Last accessed.</td><td>" + Hex + "</td><td>" + val + "</td></tr>"; pos += 4; size -= 4;
        }

        if( size > 0 )
        {
          val = ( ( d[pos + 3] & 0xFF ) << 24 ) | ( ( d[pos + 2] & 0xFF ) << 16 ) | ( ( d[pos + 1] & 0xFF ) << 8 ) | ( d[pos] & 0xFF );
          Hex = String.format("%1$02X", d[pos] ) + " " + String.format("%1$02X", d[pos + 1] )+ " " + String.format("%1$02X", d[pos + 2] )+ " " + String.format("%1$02X", d[pos + 3] );

          out += "<tr><td>Creation time.</td><td>" + Hex + "</td><td>" + val + "</td></tr>"; pos += 4; size -= 4;
        }
      }

      //Compressed file attributes as 64 bit fields.

      else if( CMD == 0x0001 )
      {
        out += "<tr><td>zip64 (0x" + String.format("%1$04X", CMD ) + ").</td><td>" + Hex + "</td><td>" + CMD + "</td></tr>";

        int size = ( ( d[pos + 3] & 0xFF ) << 8 ) | ( d[pos + 2] & 0xFF );
        Hex = String.format("%1$02X", d[pos + 2] ) + " " + String.format("%1$02X", d[pos + 3] );

        out += "<tr><td>zip64 len.</td><td>" + Hex + "</td><td>" + ( size > 28 ? "Error (" + size + " > 28)" : size ) + "</td></tr>"; size = size > 28 ? 0 : size; pos += 4;

        if( size > 0 )
        {
          val = ( ( d[pos + 7] & 0xFF ) << 56 ) | ( ( d[pos + 6] & 0xFF ) << 48 ) | ( ( d[pos + 5] & 0xFF ) << 40 ) | ( ( d[pos + 4] & 0xFF ) << 32 ) |
          ( ( d[pos + 3] & 0xFF ) << 24 ) | ( ( d[pos + 2] & 0xFF ) << 16 ) | ( ( d[pos + 1] & 0xFF ) << 8 ) | ( d[pos] & 0xFF );
          Hex = String.format("%1$02X", d[pos] ) + " " + String.format("%1$02X", d[pos + 1] )+ " " + String.format("%1$02X", d[pos + 2] )+ " " + String.format("%1$02X", d[pos + 3] )
          + " " + String.format("%1$02X", d[pos + 4] )+ " " + String.format("%1$02X", d[pos + 5] )+ " " + String.format("%1$02X", d[pos + 6] )+ " " + String.format("%1$02X", d[pos + 7] );

          out += "<tr><td>Uncompressed file size.</td><td>" + Hex + "</td><td>" + val + "</td></tr>"; pos += 8; size -= 8;
        }

        if( size > 0 )
        {
          val = ( ( d[pos + 7] & 0xFF ) << 56 ) | ( ( d[pos + 6] & 0xFF ) << 48 ) | ( ( d[pos + 5] & 0xFF ) << 40 ) | ( ( d[pos + 4] & 0xFF ) << 32 ) |
          ( ( d[pos + 3] & 0xFF ) << 24 ) | ( ( d[pos + 2] & 0xFF ) << 16 ) | ( ( d[pos + 1] & 0xFF ) << 8 ) | ( d[pos] & 0xFF );
          Hex = String.format("%1$02X", d[pos] ) + " " + String.format("%1$02X", d[pos + 1] )+ " " + String.format("%1$02X", d[pos + 2] )+ " " + String.format("%1$02X", d[pos + 3] )
          + " " + String.format("%1$02X", d[pos + 4] )+ " " + String.format("%1$02X", d[pos + 5] )+ " " + String.format("%1$02X", d[pos + 6] )+ " " + String.format("%1$02X", d[pos + 7] );

          out += "<tr><td>Size of compressed data.</td><td>" + Hex + "</td><td>" + val + "</td></tr>"; pos += 8; size -= 8;
        }

        if( size > 0 )
        {
          val = ( ( d[pos + 7] & 0xFF ) << 56 ) | ( ( d[pos + 6] & 0xFF ) << 48 ) | ( ( d[pos + 5] & 0xFF ) << 40 ) | ( ( d[pos + 4] & 0xFF ) << 32 ) |
          ( ( d[pos + 3] & 0xFF ) << 24 ) | ( ( d[pos + 2] & 0xFF ) << 16 ) | ( ( d[pos + 1] & 0xFF ) << 8 ) | ( d[pos] & 0xFF );
          Hex = String.format("%1$02X", d[pos] ) + " " + String.format("%1$02X", d[pos + 1] )+ " " + String.format("%1$02X", d[pos + 2] )+ " " + String.format("%1$02X", d[pos + 3] )
          + " " + String.format("%1$02X", d[pos + 4] )+ " " + String.format("%1$02X", d[pos + 5] )+ " " + String.format("%1$02X", d[pos + 6] )+ " " + String.format("%1$02X", d[pos + 7] );

          out += "<tr><td>Offset to File signature.</td><td>" + Hex + "</td><td>" + val + "</td></tr>"; pos += 8; size -= 8;
        }

        if( size > 0 )
        {
          val = ( ( d[pos + 3] & 0xFF ) << 24 ) | ( ( d[pos + 2] & 0xFF ) << 16 ) | ( ( d[pos + 1] & 0xFF ) << 8 ) | ( d[pos] & 0xFF );
          Hex = String.format("%1$02X", d[pos] ) + " " + String.format("%1$02X", d[pos + 1] )+ " " + String.format("%1$02X", d[pos + 2] )+ " " + String.format("%1$02X", d[pos + 3] );

          out += "<tr><td>Offset to File signature.</td><td>" + Hex + "</td><td>" + val + "</td></tr>"; pos += 4; size -= 4;
        }
      }
      else if( CMD == 0x0000 )
      {
        while( CMD == 0x0000 && pos < d.length )
        {
          out += "<tr><td>Padding (0x0000).</td><td>" + String.format("%1$02X", CMD & 0xFF ) + "</td><td>Unused.</td></tr>";
          CMD = d[pos++];
        }
      }
      else
      {
        out += "<tr><td>Unknown (0x" + String.format("%1$04X", CMD ) + ").</td><td>" + Hex + "</td><td>" + CMD + "</td></tr>";

        int size = ( ( d[pos + 3] & 0xFF ) << 8 ) | ( d[pos + 2] & 0xFF ); Hex = String.format("%1$02X", d[pos + 2] ) + " " + String.format("%1$02X", d[pos + 3] );

        out += "<tr><td>Unknown data len.</td><td>" + Hex + "</td><td>" + size + "</td></tr>"; pos += 4;

        if( size > 0 )
        {
          Hex = ""; while( size > 0 ) { Hex += String.format("%1$02X", d[pos] ) + ( size > 1 ? " " : "" ); size -= 1; pos += 1; }

          out += "<tr><td>Unknown data.</td><td>" + Hex + "</td><td>?</td></tr>";
        }
      }
    }

    if( pos < d.length )
    {
      Hex = ""; int size = d.length - pos; while( size > 0 ) { Hex += String.format("%1$02X", d[pos] ) + ( size > 1 ? " " : "" ); size -= 1; pos += 1; }
      out += "<tr><td>Bad Data.</td><td>" + Hex + "</td><td>?</td></tr>";
    }

    //Display the result.

    file.Events = true; info( out + "</table></html>" );
  }

  //This event is called when the user clicks on an tree node.

  public void open(JDEvent e)
  {
    //This is given to the open method when the user goes to load a new file format. It triggers the Uninitialize method.

    if( e.getID().equals("UInit") ) { Uninitialize(); }

    //Data directory array.

    else if( e.getID().equals("dir") ){ info( cDir ); }

    //When the user clicks on the node we will receive the array arguments associated with the node.

    else if( e.getArg(0) == 0 ) { ds.setDescriptor( des.get( (int)e.getArg(1) ) ); }

    //Select file data bytes.

    else if( e.getArg( 0 ) == 1 )
    {
      ds.clear(); info("<html></html>");

      try { file.seek( e.getArg(1) ); Offset.setSelected( e.getArg(1), e.getArg(2) ); } catch( java.io.IOException er ) { }

      if( javax.swing.JOptionPane.showConfirmDialog(null, "Would you like to open this file?", null, javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.YES_OPTION )
      {
        if( main != null )
        {
          String[] paths = tree.getLeadSelectionPath().toString().split(", ");
          String path = ""; for( int i = 1, end = paths.length - 1; i < end; path += paths[i++] + ( i < end ? "/" : "" ) );
          main.actionPerformed( new java.awt.event.ActionEvent(this, java.awt.event.ActionEvent.ACTION_PERFORMED, "ZOpen" + path ) );
        }
      }
    }

    //Load and set descriptor to node.

    else if( e.getArg( 0 ) >= 2 )
    {
      try
      { 
        file.Events = false;
        
        JDNode n = (JDNode)tree.getLastSelectedPathComponent(); n.setArgs( new long[]{ 0, ref++ });
        
        file.seek( e.getArg(1) ); DTemp = new Descriptor( file ); des.add( DTemp );

        if( e.getArg( 0 ) == 2 )
        {
          DTemp.setEvent( this::zipInfo ); 

          DTemp.Other("Signature", 4);
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
        }
        else if( e.getArg(0) == 3 )
        {
          DTemp.setEvent( this::dataInfo );

          DTemp.Other("Signature", 4);
          DTemp.LUINT32("CRC-32");
          DTemp.LUINT32("Compressed Size");
          DTemp.LUINT32("Uncompressed Size");
        }
    
        //Load and set descriptor to node.

        else if( e.getArg( 0 ) == 4 )
        {
          DTemp.setEvent( this::dirInfo );
    
          DTemp.Other("Signature", 4);
          DTemp.LUINT16("Version used");
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
          DTemp.LUINT16("Comment len"); int clen = (short)DTemp.value;
          DTemp.LUINT16("Disk Number");
          DTemp.LUINT16("Internal attributes");
          DTemp.LUINT32("External attributes");
          DTemp.LUINT32("Offset");
          DTemp.String8("File name", flen );
          if( elen > 0 ) { DTemp.Other("Data Felid", elen ); }
          if( clen > 0 ) { DTemp.String8("Comment", clen ); }
        }

        //End of zip directory.

        else if( e.getArg( 0 ) == 5 )
        {
          DTemp.setEvent( this::endInfo );
    
          DTemp.Other("Signature", 4);
          DTemp.LUINT16("Disk Number");
          DTemp.LUINT16("Disks");
          DTemp.LUINT16("Directory");
          DTemp.LUINT16("Directories");
          DTemp.LUINT32("Directory size");
          DTemp.LUINT32("Directory offset");
          DTemp.LUINT16("Comment size"); int clen = (short)DTemp.value;
          if( clen > 0 ) { DTemp.String8("Comment", clen ); }
        }

        //End of zip directory 64.

        else if( e.getArg( 0 ) == 6 )
        {
          DTemp.setEvent( this::end64Info );

          DTemp.Other("Signature", 4);
          DTemp.LUINT64("End 64 Size"); long end = (long)DTemp.value - 44;
          DTemp.LUINT16("Version");
          DTemp.LUINT16("Min Version");
          DTemp.LUINT32("Disk Number");
          DTemp.LUINT32("Disks");
          DTemp.LUINT64("Directory");
          DTemp.LUINT64("Directories");
          DTemp.LUINT64("Directory size");
          DTemp.LUINT64("Directory offset");
          if( end > 0 ) { DTemp.Other("Extra Field", (int)end); }
        }

        //zip directory location 64.

        else if( e.getArg( 0 ) == 7 )
        {
          DTemp.setEvent( this::loc64Info );

          DTemp.Other("Signature", 4);
          DTemp.LUINT32("Disks 64");
          DTemp.LUINT64("Dir End64 Offset");
          DTemp.LUINT32("Disks");
        }

        file.Events = true; ds.setDescriptor( DTemp );
      }
      catch(Exception er) { }
    }
  }

  //Multi pat file storage detail.

  private static final String multiPartZip = "<br /><br />We should always start at disk 0 and decompress the data into the file then move to disk 1 and so on adding data to the file.<br /><br />" +
  "This feature is only used in multi-part file storage.</html>";

  //Central directory.

  private static final String cDir = "<html>The central directory Has a copy of each file signature in this file and the location to each file signature.<br /><br />" +
  "The central directory has some additional attributes that can be used to add comments to files.<br /><br />" +
  "The central directory tells us which disk we are on, and allows us to do multi part zip files as well which is not included in the file signatures.<br /><br />" +
  "It is recommend that we read the central directory first and locate the file signatures using the offset given to the file signature.<br /><br />" +
  "This is because if we read only the file signatures we do not know if it is a multi-part file zip, or if more than one file signature exists for the same file and only one is the latest version of the file.</html>";

  //Explain how CRC32 is used.

  private static final String crc32 = "<html>The CRC32 value is used to detect changes in the binary file. It extends the accuracy of a checksum check.<br /><br />" +
  "A regular checksum adds the bytes together in the file. When the checksum does not match, we know a few bytes values (0 to 255) in the file have changed.<br />" +
  "The checksum fails to detect errors in data in which an equal amount of change is added and subtracted to byte values across the data.<br />" +
  "The probability of this happening is minimal, but there is a way we can extend this check.<br /><br />" +
  "Instead, the CRC uses xor to detect changes in binary digits in the file. When we xor two binary numbers the same, we end up with zero, then any binary digits that are not the same stay.<br /><br />" +
  "PK-zip uses the carefully selected binary bit pattern 1_0000_0100_1100_0001_0001_1101_1011_0111 as the CRC.<br /><br />" +
  "The first binary digit that is one in the binary data is lined up with the first binary digit that is one in the CRC bit pattern. When xor, it cancels out this first binary digit. The remaining binary data is xor until we have fewer binary digits than 32 bits.<br /><br />" +
  "The remaining 32 bits become the CRC32 value. When we decompress a file, we do the same thing with the decompressed data and subtract our remaining 32 bits at the end with our CRC32 value. If the result is not zero, then a change was made in the binary data.<br /><br />" +
  "Unlike a checksum, the CRC can detect significant, equal changes or the tiniest change made across the file that caused the binary digits to flip and to continue a different bit pattern while performing xor across the data.<br /><br />" +
  "The CRC32 can tell us if the data does not match what is expected but does not tell us where the errors in the data are or how to correct them.</html>";

  //The ZIP file header.

  private static final String[] zipInfo = new String[]
  {
    "<html>50 4B 03 04 is the start of a file (signature) in a compressed zip file.</html>",
    "<html>Version of zip used to create the file. The version number is convert to an decimal value.<br /><br />" +
    "in the case of version 122 it would mean 12.2v. In the case of 20 it means 2.0v.</html>",
    "<html>Version needed to extract (minimum). The version number is convert to an decimal value.<br /><br />" +
    "in the case of version 122 it would mean 12.2v. In the case of 20 it means 2.0v.</html>",
    "<html>The flag is meant to be viewed in binary. Each of the 16 binary digits if set one signifies an setting." +
    "The table bellow shows what setting each digit implies.<br /><br />" +
    "<table border='1'>" +
    "<tr><td>Digit</td><td>Description</td></tr>" +
    "<tr><td>0000000000001000</td><td>If this bit is set, the fields CRC-32, compressed size and uncompressed size are set to zero in the local header. The correct values are put in the data descriptor after the compressed data.</td></tr>" +
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
    "<tr><td>99</td><td>AE-x encryption marker.</td></tr>" +
    "</table></html>",
    "<html>File last modification time.</html>",
    "<html>File last modification date.</html>",
    crc32,
    "<html>Compressed size. This is the size of the data after this PK signature.<br /><br />" +
    "After the compressed data should be another PK signature.<br /><br />" +
    "If the value is FF FF FF FF hex then the value is stored using a 64 bit number under the extra data field.</html>",
    "<html>Uncompressed file size. This is the file size after we decompress the file. In some cases this is 0 as it is a folder.<br /><br />" +
    "If the value is FF FF FF FF hex then the value is stored using a 64 bit number under the extra data field.</html>",
    "<html>File name length in bytes.</html>",
    "<html>Extra field length in bytes. The extra felid is useful for extending the file attributes and properties.<br /><br />" +
    "The extra data felid is used to extend the zip file format.</html>",
    "<html>Comment length in bytes.</html>",
    "<html>Disk Number." + multiPartZip,
    "<html>Internal attributes.</html>",
    "<html>External attributes.</html>",
    "<html>File signature location.</html>",
    "<html>The zip file format uses the full path to the file then name of the file.</html>"
  };

  //Data descriptor.

  private static final String[] dataInfo = new String[]
  {
    "<html>This is the data descriptor signature. Marks the end of a compressed file data.</html>",
    crc32,
    "<html>Compressed size. This is the size of the data before this sdata signature.</html>",
    "<html>Uncompressed file size. This is the file size after we decompress the file.</html>"
  };

  private static final String[] loc64Info = new String[]
  {
    "<html>Zip64 end of central dir locator signature.</html>",
    "<html>Total number of disks that use zip 64 signatures." + multiPartZip,
    "<html>The offset to the zip64 end of central directory record. If this does not match the location the signature was read then there is most likely file corruption.</html>",
    "<html>Total number of disks that this zip file data is split into." + multiPartZip
  };

  private static final String[] endInfo = new String[]
  {
    "<html>End of central dir signature.</html>",
    "<html>The size of this signature.<br /><br />" +
    "If this is set larger than 44 bytes then the rest of the bytes after the signature are used as an extended data failed.<br /><br />" +
    "The extended data field is reserved for future use.</html>",
    zipInfo[1], zipInfo[2],
    "<html>The current disk number." + multiPartZip,
    "<html>Number of disk with the same central directory." + multiPartZip,
    "<html>Total number of entries in the central directory on this disk." + multiPartZip,
    "<html>Total number of entries in the central directory across all disks." + multiPartZip,
    "<html>Size of the central directory in this file.</html>",
    "<html>Offset to start location for the central directory in this file.</html>",
    "<html>File user comment length.</html>",
    "<html>File user comment.</html>"
  };

  public void zipInfo( int i )
  {
    i = i > 0 ? ( i > 10 ? i + 6 : i + 1 ) : i;

    if( i < 0 )
    {
      info("<html>All files in the zip begin with a PK signature. The file compressed data is right after the PK parameters.<br /><br />" +
      "The next file signature is after the compressed file size parameter.<br /><br />" +
      "In some cases a signature code (data descriptor) is used to identify the end of the compressed file data in some cases.<br /><br />" +
      "The data descriptor tells us how many bytes the compressed data is which should match the number of bytes we read after the PK parameters.<br /><br />" +
      "Most of the time only PK signatures exist and the number of bytes for the compressed file is set in the PK header.<br /><br />" +
      "The only time we do not set the compressed file size in the PK header is when we do not know the compressed file size till after the file was compressed.<br /><br />" +
      "The flag parameter can also be adjusted to signify that the data descriptor marks the end of the files data.</html>");
    }
    else if( i == 18 )
    {
      extendedData();
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
      "The data descriptor also stores the files original size, and has an CRC count.<br /><br />" +
      "The CRC is very important as it can be used to know if the decompressed file matches the original.</html>");
    }
    else
    {
      info( dataInfo[ i ] );
    }
  }

  public void dirInfo( int i )
  {
    if( i < 0 )
    {
      info( cDir );
    }
    else if( i == 0 )
    {
      info("<html>50 4B 01 02 is the start of a file signature in the central directory in an zip file.</html>");
    }
    else if( i == 18 )
    {
      extendedData();
    }
    else
    {
      info( zipInfo[ i ] );
    }
  }

  public void loc64Info( int i )
  {
    if( i < 0 )
    {
      info("<html>This is used as the location to the directory end 64 signature. It is not needed, but is used to check for errors while reading.<br /><br />" +
      "If existent and the Directory end 64 signature that was read does not match the offset then there is most likely data corruption.</html>");
    }
    else
    {
      info( loc64Info[i] );
    }
  }

  public void end64Info( int i )
  {
    if( i < 0 )
    {
      info("<html>The end of zip may specify more than one zip file as a disk." + multiPartZip);
    }
    else
    {
      info( i < 10 ? endInfo[i] : "<html>The extra data field is reserved for future use.</html>" );
    }
  }

  public void endInfo( int i )
  {
    if( i < 0 )
    {
      info("<html>The end of zip may specify more than one zip file as a disk." + multiPartZip);
    }
    else
    {
      info( endInfo[ i > 0 ? i + 3 : i ] );
    }
  }
}