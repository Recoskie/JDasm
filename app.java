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

  //Each integer is the file format decoder to use by file extensions.

  private int UseDecoder[] = new int[] { 0, 0, 0, 0, 0 };

  //Supported file format extensions.

  private String Supports[] = new String[] { ".exe", ".dll", ".sys", ".drv", ".ocx" };

  //The file to load. To begin decoding file types.

  private String DecodeAPP[] = new String[]{ "Format.EXE" };

  //Drag and drop file handling.
  
  private String df = "";

  //Create the application.

  public app( String Arg_file, boolean isDisk )
  {
    //Create GUI.

    createGUI("JFH-Disassembly", this, this); new DropTarget(f, DnDConstants.ACTION_LINK, this, true);

    //Display GUI.
    
    f.pack(); f.setLocationRelativeTo(null); f.setVisible(true);

    //Adjust minium col widths.

    tools.setColMinium(0, 300);

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

    else if( e.getActionCommand().equals("Toggle text View") ) { textV = !textV; Offset.enableText( textV ); Virtual.enableText( textV ); }

    else if( e.getActionCommand().equals("Toggle virtual space View") ) { Virtual.setVisible(!Virtual.isVisible()); }

    else if( e.getActionCommand().equals("Toggle offset View") ) { Offset.setVisible(!Offset.isVisible()); }

    else if( e.getActionCommand().equals("Open new File") ) { Reset(); }

    else if( e.getActionCommand().equals("Toggle Data Inspector") ) { di.setVisible(!di.isVisible()); }

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
 
      int userSelection = fileChooser.showSaveDialog( f );
 
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

    f.setJMenuBar(fcBar); fc.setTree( tree ); tree.singleClick = false;
  }

  public void open( JDEvent e )
  {
    //If file format is Supported. Open in reader with binary tools.

    int I = DefaultProgram( e.getExtension() );

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
        f.setContentPane( new JLabel( "Loading...", SwingConstants.CENTER ) ); file = new RandomAccessDevice( e.getID(), "r" );
      }

      //Set io components to target.

      Offset.setTarget( file ); Virtual.setTarget( file ); di.setTarget( file );

      f.setJMenuBar( bdBar );

      //If is a recognized file. Set all editor/data components active.

      if( I >= 0 )
      {
        stree.setVisible(true); ds.setVisible(true); iData.setVisible(true);
      
        Virtual.setVisible(true); Offset.setVisible(true); di.setVisible(true);
      }

      //Is not recognized file. Open using data types, and hex editor.

      else
      {
        stree.setVisible(false); ds.setVisible(false); iData.setVisible(false);
      
        Virtual.setVisible(false); Offset.setVisible(true); di.setVisible(true);
      }

      //Set back tools after disk finish loading.

      if( e.getArg(0) == -2 ) { f.setContentPane( tools ); }

      //Adjust the window.

      f.setExtendedState(JFrame.MAXIMIZED_BOTH); try { tools.rowMaximize(0); } catch( Exception er ) {}
    }

    //Failed to read file, or disk.

    catch (Exception er)
    {
      I = -1;

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

    //Load file format reader.

    try
    {
      if( I >= 0 ) { Class.forName(DecodeAPP[UseDecoder[I]]).getConstructor().newInstance(); tree.singleClick = true; }
    }
    catch(Exception er)
    {
      I = -1; JOptionPane.showMessageDialog(null,"Unable to Load Format reader, For This File Format!"); Reset();
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

  public void drop(DropTargetDropEvent dtde)
  {
    open( new JDEvent( this, df, df.indexOf(".") > 0 ? df.substring( df.lastIndexOf("."), df.length() ) : "", "", 0 ) );
  }

  public void dropActionChanged(DropTargetDragEvent dtde) { }

  public void dragEnter(DropTargetDragEvent dtde) { }

  public void dragExit(DropTargetEvent dte) { }

  public int DefaultProgram(String EX) { for( int i = 0; i < Supports.length; i++ ) { if( Supports[i].equals(EX) ) { return(i); } } return( -1 ); }
}
