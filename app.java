import javax.swing.*;
import java.io.*;
import java.awt.event.*;
import RandomAccessFileV.*;
import Window.*;
import swingIO.tree.*;

public class app extends Window implements ActionListener, JDEventListener
{
  //Application is not Administrator by default.

  public static boolean admin = false;

  //Each integer is the file format decoder to use by file extensions.

  public int UseDecoder[] = new int[] { 0, 0, 0, 0, 0 };

  //Supported file format extensions.

  public String Supports[] = new String[] { ".exe", ".dll", ".sys", ".drv", ".ocx" };

  //The file to load. To begin decoding file types.

  public String DecodeAPP[] = new String[]{ "Format.EXE" };

  //Create the application.

  public app( String Arg_file, boolean isDisk )
  {
    //Create GUI.

    createGUI("JFH-Disassembly", this, this);

    //Display GUI.
    
    f.pack(); f.setLocationRelativeTo(null); f.setVisible(true);

    //Check open with args.

    if( Arg_file != "" )
    {
      if( isDisk ) { open( new JDEvent( this, "", "", Arg_file, -2 ) ); }
      else
      {
        open( new JDEvent( this, Arg_file ) );
      }
    }
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

    //Disk selector. Not available yet.
    //Note I need to make a disk chooser that uses the tree I built.
    
    if( e.getActionCommand() == "O" )
    {
      if( !dc.setTree( tree ) )
      {
        javax.swing.JOptionPane.showMessageDialog(null,"Unable to Find any Disk drives on this System.");
      }
    }

    //Binary tool display controls.

    else if( e.getActionCommand().equals("Toggle text View") )
    {
      textV = !textV; Offset.enableText( textV ); Virtual.enableText( textV );
    }

    else if( e.getActionCommand().equals("Toggle virtual space View") )
    {
      Virtual.setVisible(!Virtual.isVisible());
    }

    else if( e.getActionCommand().equals("Toggle offset View") )
    {
      Offset.setVisible(!Offset.isVisible());
    }

    else if( e.getActionCommand().equals("Open new File") ) { Reset(); }

    else if( e.getActionCommand().equals("Toggle Data Inspector") )
    {
      di.setVisible(!di.isVisible());
    }

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

    else if( e.getActionCommand().equals("Save as file") )
    {
      file.Events = false;

      long pos, end, t;

      byte[] buffer = new byte[4096];

      OutputStream os;

      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setDialogTitle("Save data to new file, or overwrite a file");
 
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
        file = new RandomAccessFileV( e.getPath(), "rw" );
      }
      else
      {
        f.setContentPane( new JLabel( "Loading...", SwingConstants.CENTER ) );
        file = new RandomAccessDevice( e.getID(), "r" );
        f.setContentPane( tools );
      }

      Offset.setTarget( file ); Virtual.setTarget( file ); di.setTarget( file );

      f.setJMenuBar( bdBar );

      if( I >= 0 )
      {
        stree.setVisible(true); ds.setVisible(true); iData.setVisible(true);
      
        Virtual.setVisible(true); Offset.setVisible(true); di.setVisible(true);
      }
      else
      {
        stree.setVisible(false); ds.setVisible(false); iData.setVisible(false);
      
        Virtual.setVisible(false); Offset.setVisible(true); di.setVisible(true);
      }

      f.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    catch (Exception er)
    {
      I = -1;
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

      Reset();
    }

    try
    {
      if( I >= 0 ) { Class.forName(DecodeAPP[UseDecoder[I]]).getConstructor().newInstance(); tree.singleClick = true; }
    }
    catch(Exception er)
    {
      I = -1; JOptionPane.showMessageDialog(null,"Unable to Load Format reader, For This File Format!"); Reset();
    }
  }

  public int DefaultProgram(String EX)
  {
    for( int i = 0; i < Supports.length; i++ )
    {
      if( Supports[i].equals(EX) )
      {
        return(i);
      }
    }

    return( -1 );
  }
}
