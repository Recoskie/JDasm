import javax.swing.*;
import java.io.*;
import java.awt.event.*;
import RandomAccessFileV.*;
import Window.*;
import swingIO.tree.*;
import java.awt.dnd.*;
import java.awt.datatransfer.DataFlavor;

public class app extends Window implements ActionListener, DropTargetListener, JDEventListener
{
  //Application is not Administrator by default.

  private static boolean admin = false;

  //Use the file signature codes instead of file extension.

  private byte Signature[][] = new byte[][]
  {
    new byte[] { 0x4D, 0x5A } //Microsoft binaries.
  };

  //Buffer should be set to the length of the largest signature sequence.

  private byte[] Sig = new byte[2];

  //The file to load. To begin decoding file types.

  private String DecodeAPP[] = new String[]{ "Format.EXE" };

  //Drag and drop file handling.
  
  private String df = "";

  //Create the application.

  public app( String Arg_file, boolean isDisk )
  {
    //Create GUI.

    createGUI("J-Disassembly", this, this); new DropTarget(winFrame, DnDConstants.ACTION_LINK, this, true);

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
    //Basic file path commands.
    
    if( e.getActionCommand() == "B" ) { fc.back(); }
    
    if( e.getActionCommand() == "G" ) { fc.go(); }
    
    if( e.getActionCommand() == "C" ) { fc.root(); }
    
    if( e.getActionCommand() == "H" ) { fc.home(); }
    
    if( e.getActionCommand() == "U" ) { fc.up(); }

    //Disk selector.
    
    if( e.getActionCommand() == "O" ) { if( !fc.disks() ) { javax.swing.JOptionPane.showMessageDialog(null,"Unable to Find any Disk drives on this System."); } }

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

    //Copy sleeted bytes in editor.

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
            while( pos < end ) { data += String.format( "%1$02X", file.readV() ); pos += 1; }
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
            while( pos < end ) { data += String.format( "%1$02X", file.read() ); pos += 1; }
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
    tree.setRootVisible(false); tree.setShowsRootHandles(false);

    stree.setVisible(true); ds.setVisible(false); iData.setVisible(false);
    Virtual.setVisible(false); Offset.setVisible(false); di.setVisible(false);

    winFrame.setJMenuBar(fcBar); fc.setTree( tree ); tree.singleClick = false;
  }

  public void open( JDEvent e )
  {
    //Set the IO target.

    try
    {
      if( e.getArg(0) >= 0 )
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

      //Open a diskID.

      else
      {
        winFrame.setContentPane( new JLabel( "Loading...", SwingConstants.CENTER ) );
        file = new RandomAccessDevice( e.getID(), "r" );
      }

      //Set io components to target.

      Offset.setTarget( file ); Virtual.setTarget( file ); di.setTarget( file );

      winFrame.setJMenuBar( bdBar );

      //If file format is Supported. Open in reader with binary tools.

      int I = DefaultProgram();

      //If is a recognized file.

      if( I >= 0 )
      {
        //Load file format reader.

        try
        {
          if( I >= 0 ) { Class.forName(DecodeAPP[I]).getConstructor().newInstance(); tree.singleClick = true; }

          stree.setVisible(true); ds.setVisible(true); iData.setVisible(true);
      
          Virtual.setVisible(true); Offset.setVisible(true); di.setVisible(true);
        }
        catch(Exception er) { I = -1; JOptionPane.showMessageDialog(null,"Unable to Load Format reader, For This File Format!"); }
      }

      //If it is not an recognized file, or file format reader failed. Open using data types, and hex editor.

      if( I < 0 )
      {
        stree.setVisible(false); ds.setVisible(false); iData.setVisible(false);
      
        Virtual.setVisible(false); Offset.setVisible(true); di.setVisible(true);
      }
      
      //Set back tools after disk finish loading.

      if( e.getArg(0) == -2 ) { winFrame.setContentPane( tools ); }

      //Adjust the window.

      if( winFrame.getExtendedState() != JFrame.MAXIMIZED_BOTH )
      {
        winFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); winFrame.revalidate();
      }

      SwingUtilities.invokeLater(new Runnable() 
      {
        public void run()
        {
          tools.rowMaximize(0); tools.update();
        }
      });
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

          if( Sys.promptAdmin("disk " + e.getID() ) ) { System.exit(0); }
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
          JOptionPane.showMessageDialog(null,"Unable to read disk drive.");
        }
      }

      //Back to file selection as disk, or file can not be read at all.

      Reset();
    }
  }

  //File check on drag and drop.
  
  @SuppressWarnings({"unchecked"}) public void dragOver(DropTargetDragEvent dtde)
  {
    try
    {
      java.util.List<File> f = ((java.util.List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));

      df = f.get(0).toString(); if( new File( df ).isFile() && f.size() == 1 ) { dtde.acceptDrag(DnDConstants.ACTION_LINK); } else { dtde.rejectDrag(); }
    }
    catch( Exception e ) { dtde.rejectDrag(); }
  }

  //Open file.

  public void drop(DropTargetDropEvent dtde) { fc.openFile( df ); }

  public void dropActionChanged(DropTargetDragEvent dtde) { }

  public void dragEnter(DropTargetDragEvent dtde) { }

  public void dragExit(DropTargetEvent dte) { }

  public int DefaultProgram()
  {
    boolean Valid = true; file.Events = false; try{ file.read( Sig ); file.seek(0); } catch( IOException err ) { } file.Events = true;

    for( int i1 = 0; i1 < Signature.length; i1++ )
    {
      Valid = true; for( int i2 = 0; i2 < Signature[i1].length && Valid; i2++ ) { Valid = Signature[i1][i2] == Sig[i2]; }

      if( Valid ) { return( i1 ); }
    }
      
    return( -1 );
  }
}
