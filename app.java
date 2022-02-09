import javax.swing.*;
import java.io.*;
import java.util.zip.*;
import java.awt.event.*;
import RandomAccessFileV.*;
import Window.*;
import swingIO.tree.*;
import java.awt.dnd.*;
import java.awt.datatransfer.DataFlavor;
import core.x86.*;

public class app extends Window implements ActionListener, DropTargetListener, JDEventListener
{
  //Application is not Administrator by default.

  private static boolean admin = false;

  //Used to uninitialize the plugins when loading a new file.

  private static boolean UInit = false;

  //Use the file signature codes instead of file extension.

  private byte Signature[][] = new byte[][]
  {
    new byte[] { 0x4D, 0x5A }, //Microsoft binaries.
    new byte[] { 0x7F, 0x45, 0x4C, 0x46 }, //Linux/UNIX binaries.
    new byte[] { -50, -6, -19, -2 }, //32 Bit Mac/IOS binary.
    new byte[] { -49, -6, -19, -2 }, //64 Bit Mac/IOS binary.
    new byte[] { -54, -2, -70, -66 }, //Mac/IOS universal binary.
    new byte[] { 0x42, 0x4D }, //Bit map pictures.
    new byte[] { -1, -40 }, //JPEG start of image marker.
    new byte[] { 0x52, 0x49, 0x46, 0x46 }, //Multimedia RIFF file.
    new byte[] { 0x52, 0x46, 0x36, 0x34 }, //Multimedia RIFF/64 file.
    new byte[] { 0x50, 0x4B, 0x03, 0x04 } //Compressed ZIP files.
  };

  //Depending on the file format we do not need a virtual address space.

  private static boolean SignatureV[] = new boolean[]
  {
    true, true, true, true, true,
    false, false, false, false, false
  };

  //We want to keep an reference to temp files so we can delete them on opening a new files.

  private static File temp;

  //Buffer should be set to the length of the largest signature sequence.

  private byte[] Sig = new byte[4];

  //The file to load. To begin decoding file types.

  private String DecodeAPP[] = new String[]
  {
    "Format.EXE", "Format.ELF", "Format.MAC", "Format.MAC", "Format.MAC",
    "Format.BMP", "Format.JPEG", "Format.RIFF", "Format.RIFF", "Format.ZIP"
  };

  //By file extension.

  private String Extension[] = new String[]{ ".com" };

  //Depending on the file format extension we do not need a virtual address space.

  private static boolean ExtensionV[] = new boolean[] { true };

  //What file to load by file extension.

  private String DecodeAPP_EX[] = new String[]{ "Format.COM" };

  //Drag and drop file handling.
  
  private String df = "";

  //Create the application.

  public app( String Arg_file, boolean isDisk )
  {
    //Create GUI.

    createGUI("JDisassembly", this, this); new DropTarget(winFrame, DnDConstants.ACTION_LINK, this, true);

    //Display GUI.
    
    winFrame.pack(); winFrame.setLocationRelativeTo(null); winFrame.setVisible(true);

    //Check open with args.

    if( Arg_file != "" ) { if( isDisk ) { open( new JDEvent( this, "", "", Arg_file, -2 ) ); } else { fc.openFile(Arg_file); } }
  }

  public static void main( String[] args )
  {
    admin = Sys.start( app.class, args );

    //Command line argument.

    if( args.length > 1 )
    {
      if( args[0].equals("disk") ) { new app( args[1], true ); }
      else
      {
        new app( args[1], false );
      }

      return;
    }

    new app( "", false );
  }

  //handling menu item events
  
  public void actionPerformed(ActionEvent e)
  {
    //Open file within zip.
  
    if( e.getActionCommand().startsWith("ZOpen") )
    {
      String file = e.getActionCommand(); file = file.substring(5,file.length());

      Reset(); open(new JDEvent( this, "", file.substring(file.lastIndexOf("."), file.length()), file, 1 )); return;
    }

    //Basic file path commands.
    
    if( e.getActionCommand() == "B" ) { fc.back(); }
    
    if( e.getActionCommand() == "G" ) { fc.go(); }
    
    if( e.getActionCommand() == "C" ) { fc.root(); }
    
    if( e.getActionCommand() == "H" ) { fc.home(); }
    
    if( e.getActionCommand() == "U" ) { fc.up(); }

    //Disk selector.
    
    if( e.getActionCommand() == "O" ) { if( !fc.disks( !admin ) ) { javax.swing.JOptionPane.showMessageDialog(null,"Unable to Find any Disk drives on this System."); } }

    //Disassemble boot program.
    
    if( e.getActionCommand() == "boot" )
    {
      ds.setVisible(true); iData.setVisible(true); tools.update();

      if( core == null || core.type() != 0 ){ core = new X86( file ); } else { core.setTarget( file ); }

      disEnd = 512L; core.setBit(X86.x86_16); core.setEvent( this::Dis );

      core.clear(); core.Crawl.add( 0L ); Dis( core.Crawl.get(0), true );

      tools.rowMaximize(0);
    }

    //Binary tool display controls.

    else if( e.getActionCommand().equals("Toggle text View") ) { Offset.enableText( !Offset.showText() ); Virtual.enableText( !Virtual.showText() ); tools.update(); }

    else if( e.getActionCommand().equals("Toggle virtual space View") ) { Virtual.setVisible(!Virtual.isVisible()); tools.update(); }

    else if( e.getActionCommand().equals("Toggle offset View") ) { Offset.setVisible(!Offset.isVisible()); tools.update(); }

    else if( e.getActionCommand().equals("Open new File") ) { Reset(); }

    else if( e.getActionCommand().equals("Toggle Data Inspector") ) { di.setVisible(!di.isVisible()); tools.update(); }

    else if( e.getActionCommand().equals("Goto Offset") )
    {
      try
      {
        file.seek( Long.parseUnsignedLong( JOptionPane.showInputDialog("Enter Offset to seek (HEX): "), 16 ) );
      }
      catch( Exception err )
      {
        JOptionPane.showMessageDialog( null, "Bad Input. Hex only." );
      }
    }

    else if( e.getActionCommand().equals("Goto Virtual") )
    {
      try
      {
        file.seekV( Long.parseUnsignedLong( JOptionPane.showInputDialog("Enter Offset to seek (HEX): "), 16 ) );
      }
      catch( Exception err )
      {
        JOptionPane.showMessageDialog( null, "Bad Input. Hex only." );
      }
    }

    //Copy selected bytes in editor.

    else if( e.getActionCommand().startsWith("CP") )
    {
      file.Events = false;

      String data = "";

      long pos, end, t;

      boolean raw = e.getActionCommand().equals("CPR");

      try
      {
        java.awt.datatransfer.Clipboard c = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();

        java.awt.datatransfer.StringSelection s;

        if( pm.getInvoker() == Virtual )
        {
          pos = Virtual.selectPos(); end = Virtual.selectEnd() + 1; t = file.getVirtualPointer();

          file.seekV(pos);

          if( raw )
          {
            while( pos < end ) { data += (char)file.readV(); pos += 1; }
          }
          else
          {
            while( pos < end ) { data += String.format( "%1$02X", file.readV() & 0xFF ); pos += 1; }
          }

          file.seekV( t );
        }
        else
        {
          pos = Offset.selectPos(); end = Offset.selectEnd() + 1; t = file.getFilePointer();

          file.seek(pos);

          if( raw )
          {
            while( pos < end ) { data += (char)file.read(); pos += 1; }
          }
          else
          {
            while( pos < end ) { data += String.format( "%1$02X", file.read() & 0xFF ); pos += 1; }
          }

          file.seek(pos);
        }
      
        s = new java.awt.datatransfer.StringSelection( data ); c.setContents( s, s );
      }
      catch( IOException err ) {}

      file.Events = true;
    }

    //Save selected bytes as file.

    else if( e.getActionCommand().equals("Save as file") )
    {
      file.Events = false;

      long pos, end, t;

      byte[] buffer = new byte[4096];

      OutputStream os;

      JFileChooser fileChooser = new JFileChooser(); fileChooser.setDialogTitle("Save data to new file, or overwrite a file");
 
      int userSelection = fileChooser.showSaveDialog( winFrame );
 
      if (userSelection == JFileChooser.APPROVE_OPTION)
      {
        try
        {
          os = new FileOutputStream( fileChooser.getSelectedFile() );
      
          if( pm.getInvoker() == Virtual )
          {
            pos = Virtual.selectPos(); end = Virtual.selectEnd() + 1; t = file.getVirtualPointer();

            file.seekV( pos );

            while ( pos < end )
            {
              file.readV( buffer );

              if( ( pos + 4096 ) < end ) { os.write( buffer ); }
              else
              {
                os.write( buffer, 0, (int)( end - pos ) );
              }

              pos += 4096;
            }
          
            os.close(); file.seekV( t );
          }
          else
          {
            pos = Offset.selectPos(); end = Offset.selectEnd() + 1; t = file.getFilePointer();

            file.seek( pos );

            while ( pos < end )
            {
              file.read( buffer );

              if( ( pos + 4096 ) < end ) { os.write( buffer ); }
              else
              {
                os.write( buffer, 0, (int)( end - pos ) );
              }

              pos += 4096;
            }
          
            os.close(); file.seek( t );
          }
        }
        catch( IOException err )
        {
          JOptionPane.showMessageDialog( null, "Unable to save file at location." );
        }
      }

      file.Events = true;
    }
  }

  //Set back to file chooser, and hide editor components till file is opened.

  public void Reset()
  {
    if( UInit ) { tree.fireOpenEvent( new JDEvent( this, "", "", "UInit", 0 ) ); System.gc(); UInit = false; }

    tree.setRootVisible(false); tree.setShowsRootHandles(false);

    stree.setVisible(true); ds.setVisible(false); iData.setVisible(false);
    Virtual.setVisible(false); Offset.setVisible(false); di.setVisible(false);

    winFrame.setJMenuBar(fcBar); fc.setTree( tree ); tree.singleClick = false;

    if( bdBar.getMenuCount() > 2 ) { bdBar.remove(BootSector); }

    if( core != null ) { core.setEvent( this::Dis ); } disEnd = null;
  
    if( temp != null && temp.exists() ) { temp.delete(); }
  }

  /*************************************************************************************
  Main handler for opening and reading file types.
  *************************************************************************************/

  public void open( JDEvent e )
  {
    if( UInit ) { tree.fireOpenEvent( new JDEvent( this, "", "", "UInit", 0 ) ); System.gc(); UInit = false; }

    //Set the IO target.

    try
    {
      if( e.getArg(0) >= 0 )
      {
        if( e.getArg(0) == 0 )
        {
          file = new RandomAccessFileV( e.getPath(), "r" );

          //Check if user has write privilege.

          try { file = new RandomAccessFileV( e.getPath(), "rw" ); } catch ( Exception er )
          {
            //If not admin ask user if they wish to try to gain write privilege run as admin.

            if( !admin && JOptionPane.showConfirmDialog(null, "To write to this file, you will have to run as admin.\r\n\r\n" +
            "Hit \"no\" if you want to open the file in \"read only\" mode.\r\n\r\n" +
            "Open file as admin?", null, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
              if( Sys.promptAdmin("file " + e.getPath() ) ) { System.exit(0); }
            }

            //If already running as admin file can only be read.

            if( admin ) { JOptionPane.showMessageDialog(null, "File can only be read."); }
          }
        }
      
        //open zip.

        else
        {
          String zfile = e.getID(), zip = fc.getFilePath() + fc.getFileName();

          java.util.zip.ZipInputStream z = new ZipInputStream( new FileInputStream( zip ) );

          java.util.zip.ZipEntry zd;
        
          boolean err = true;
        
          temp = File.createTempFile("random", ".tmp"); temp.deleteOnExit();

          byte[] buffer = new byte[4096];

          while( ( zd = z.getNextEntry() ) != null )
          {
            if( zfile.equals(zd.getName() ) )
            {
              BufferedOutputStream data = new BufferedOutputStream( new FileOutputStream( temp ), buffer.length);

              int len; while ((len = z.read(buffer)) > 0) { data.write(buffer, 0, len); }

              data.flush(); data.close(); file = new RandomAccessFileV(temp, "r"); fc.setFileName(zfile);
              
              err = false; break;
            }
          }

          if( err ) { JOptionPane.showMessageDialog(null, "Cant open zip file!"); return; }
        }
      }

      //Open a diskID.

      else
      {
        file = new RandomAccessDevice( e.getID(), "r" );

        bdBar.add(BootSector);
      }

      //Set io components to target.

      Offset.setTarget( file ); Virtual.setTarget( file ); di.setTarget( file );

      winFrame.setJMenuBar( bdBar );

      //If file format is Supported. Open in reader with binary tools.

      int I = DefaultProgram(), E = -1; if( I < 0 ) { E = I = ExtensionOnly(e.getExtension()); }

      //If is a recognized file.

      if( I >= 0 )
      {
        //Load file format reader.

        try
        {
          if( E >= 0 ) { Class.forName(DecodeAPP_EX[E]).getConstructor().newInstance(); }
          else if( I >= 0 ) { Class.forName(DecodeAPP[I]).getConstructor().newInstance(); }

          UInit = true; tree.singleClick = true;

          stree.setVisible(true); ds.setVisible(true); iData.setVisible(true);
      
          if( I > 0 ) { Virtual.setVisible( E == -1 ? SignatureV[ I ] : ExtensionV[ E ] ); }
          
          Offset.setVisible(true); di.setVisible(true);
        }
        catch(Exception er) { er.printStackTrace(); I = -1; JOptionPane.showMessageDialog(null,"Unable to Load Format reader, For This File Format!"); }
      }

      //If it is not an recognized file, or file format reader failed. Open using data types, and hex editor.

      if( I < 0 )
      {
        stree.setVisible(false); ds.setVisible(false); iData.setVisible(false);
      
        Virtual.setVisible(false); Offset.setVisible(true); di.setVisible(true);
      }

      //Adjust the window.

      if( winFrame.getExtendedState() != JFrame.MAXIMIZED_BOTH ) { winFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); }
      
      tools.update(); tools.rowMaximize(0);
    }

    //Failed to read file, or disk.

    catch (Exception er)
    {
      //Prompt user if they wish to run as admin to open disk or file.

      if(!admin)
      {
        if( e.getArg(0) >= 0 )
        {
          JOptionPane.showMessageDialog(null,"Need Administrative privilege to read this file, or File is open by another process.");

          if( Sys.promptAdmin("file " + e.getPath() ) ) { System.exit(0); }
        }
        else
        {
          JOptionPane.showMessageDialog(null,"Need Administrative privilege to read disk drives.");

          if( Sys.promptAdmin("disk " + e.getID() ) ) { System.exit(0); } else { winFrame.setContentPane( tools ); winFrame.setJMenuBar( fcBar ); winFrame.revalidate(); }
        }
      }

      //Already running as admin. Then disk or file can not be read at all.

      else
      {
        if( e.getArg(0) >= 0 )
        {
          JOptionPane.showMessageDialog(null,"File is open by another process. You can make a copy of the file, and put it somewhere else to open it.");
        }
        else
        {
          fc.disks( false );

          if( Sys.mac )
          {
            JOptionPane.showMessageDialog(null,"Only Readable disk dives are displayed when running as Administrator.\r\n\r\n" +
            "Some disks can not be read directly unless you disable SIP protection on macOS.\r\n" +
            "Do not disable SIP protection unless you are disconnected from the internet as it makes your mac vulnerable.");
          }
          else
          {
            JOptionPane.showMessageDialog(null,"Unable to read disk drive.");
          }
          
          return;
        }
      }

      //Back to file selection as disk, or file can not be read at all.

      Reset();
    }
  }


  //The preferred method is to check file types by signature, however some files may not contain a header, or file signature.
  //Files that can not be recognized by file signature are put under the method "ExtensionOnly(String ex)".

  public int DefaultProgram()
  {
    boolean Valid = true; file.Events = false; try{ file.read( Sig ); file.seek(0); } catch( IOException err ) { } file.Events = true;

    for( int i1 = 0; i1 < Signature.length; i1++ )
    {
      Valid = true; for( int i2 = 0; i2 < Signature[i1].length && Valid; i2++ ) { Valid = Signature[i1][i2] == Sig[i2]; }

      if( Valid )
      {
        //Mac signature collides with the java signature. It is easy to tell the two apparat.

        if( i1 == 4 )
        {
          try
          {
            file.Events = false; file.seek( 4 ); file.read(4); int check = file.toInt(); file.seek(0); file.Events = true;
          
            //The Oldest java version number starts at 45 and higher.
            //In a MacOS universal binary there is no way we are going to store 45 or more binaries in one file.

            if( check >= 45 ) { i1 = -1; }
          }
          catch (IOException e) { }
        }

        return( i1 );
      }
    }
      
    return( -1 );
  }

  //Some formats have no headers. They can only be recognized by file extension.

  public int ExtensionOnly(String ex) { for( int i = 0; i < Extension.length; i++ ) { if( ex.equals(Extension[i]) ) { return( i ); } } return( -1 ); }

  //File check on drag and drop.
  
  @SuppressWarnings({"unchecked"}) public void dragOver(DropTargetDragEvent dtde)
  {
    try
    {
      java.util.List<File> f = ((java.util.List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));

      //Note macOS always returns null no matter which data flavour we use.
      //Drag and drop is broken on macOS. I do not know if this will be fixed in later version of java environment.

      if( f != null )
      {
        df = f.get(0).toString(); if( new File( df ).isFile() && f.size() == 1 ) { dtde.acceptDrag(DnDConstants.ACTION_LINK); } else { dtde.rejectDrag(); }
      }
      else { dtde.rejectDrag(); }
    }
    catch( Exception e ) { dtde.rejectDrag(); }
  }

  //Open file.

  public void drop(DropTargetDropEvent dtde) { fc.openFile( df ); }

  public void dropActionChanged(DropTargetDragEvent dtde) { }

  public void dragEnter(DropTargetDragEvent dtde) { }

  public void dragExit(DropTargetEvent dte) { }
}
