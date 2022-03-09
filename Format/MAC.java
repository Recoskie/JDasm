package Format;

import swingIO.*;
import swingIO.tree.*;
import javax.swing.tree.*;
import Format.MACDecode.*;

public class MAC extends Data implements JDEventListener
{
  //The binary tree stores which descriptor to set from the format readers.

  private JDNode root;

  //Mac header reader.

  private static Headers header = new Headers();
  private static LoadCMD commands = new LoadCMD();
  private static linkEdit ledit = new linkEdit();
  
  public MAC() throws java.io.IOException
  {
    tree.setEventListener( this ); file.Events = false;

    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true); tree.setShowsRootHandles(true); root = new JDNode( fc.getFileName() + ( fc.getFileName().indexOf(".") > 0 ? "" : ".exe" ), -1 );

    //Load the application header.

    JDNode h = header.readMAC( root ); if( App != null ) { commands.load( h ); } root.insert( h, 0 );

    //Set binary tree view, and enable IO system events.
      
    ((DefaultTreeModel)tree.getModel()).setRoot(root); file.Events = true;

    //Set the selected node.
  
    tree.setSelectionPath( new TreePath( ((DefaultMutableTreeNode)root.getFirstChild()).getPath() ) );

    //Make it as if we clicked and opened the node.

    open( new JDEvent( this, "", new long[]{ 0x4000000000000000L, 0 } ) );
  }

  public void Uninitialize() { des = new java.util.ArrayList<Descriptor>(); ref = 0; DTemp = null; App = null; rPath.clear(); segment.clear(); paths = 0; }

  public void open(JDEvent e)
  {
    long val = e.getArg(0);

    boolean cmd = ( val & 0x8000000000000000L ) != 0, expandNode = ( val & 0x4000000000000000L ) != 0;

    int arg = (int)val, CMDinfo = ( arg >> 8 ) & 0xFF; arg &= 0xFF;

    if( e.getID().equals("UInit") ) { Uninitialize(); }

    else if( cmd )
    {
      ds.clear();

      //Navigate to an node.

      if( arg == 5 )
      {
        tree.setSelectionPath( new TreePath( rPath.get((int)e.getArg(1)).getPath() ) );
        tree.expandPath(tree.getLeadSelectionPath()); tree.scrollPathToVisible(tree.getLeadSelectionPath().getParentPath());
        open( new JDEvent( this, "", ((JDNode)tree.getLastSelectedPathComponent()).getArgs() ) ); return;
      }

      //Begin disassembling the program.

      else if( arg == 4 )
      {
        if( coreLoaded )
        {
          core.clear();
          
          if( e.getArgs().length == 2 )
          {
            core.Crawl.add( e.getArg(1) ); core.disLoc(0, true);
          }
          else
          {
            core.Linear.add( e.getArg(1) ); core.Linear.add( e.getArg(2) ); core.disLoc(0, false);
          }

          ds.setDescriptor( core );
        }
        else
        {
          try { file.seekV( e.getArg(1) ); } catch( java.io.IOException er ) {  }
          
          info("<html>The processor core architecture type has not been added to JDisassembly yet.</html>");
        }

        return;
      }
      
      //Select bytes in virtual space.

      else if( arg == 3 )
      {
        try
        {
          file.seekV( e.getArg(1) );
          Virtual.setSelected( e.getArg(1), e.getArg(2) );
          Offset.setSelected( file.getFilePointer(), file.getFilePointer() + e.getArg(2) - e.getArg(1) );
        }
        catch( java.io.IOException er ) { }
      }
      
      //Select bytes Offset.
      
      else if( arg == 2 )
      {
        try { file.seek( e.getArg(1) ); Offset.setSelected( e.getArg(1), e.getArg(2) ); } catch( java.io.IOException er ) { }
      }

      info( MInfo[CMDinfo] );
    }

    //Command 0 sets a descriptor for a section of data in the binary tree.

    else if( arg == 0 ) { try{ ds.setDescriptor( des.get( (int)e.getArg( 1 ) ) ); } catch( Exception er ){} }

    //Open application header within universal binaries.

    else if( arg == 1 )
    {
      ds.clear(); info("<html></html>");

      JDNode root = (JDNode)tree.getLastSelectedPathComponent();

      //We do not want to reload an existing binary if already loaded.

      if( App != root )
      {
        int Offset = (int)e.getArg(1); file.Events = false;

        //We can switch between binaries in a universal binary.

        try
        {
          //Load the main application header.

          file.seek( Offset ); JDNode h = header.readMAC( root );

          //Begin loading the program with load commands.

          commands.load( h ); root.insert( h, 0 );
        }
        catch(Exception er) { er.printStackTrace(); }

        file.Events = true; tree.setSelectionPath( new TreePath( App.getPath() ) ); tree.expandPath( new TreePath( App.getPath() ) );
      }
    }

    //Open compressed link edit info (rebase).

    else if( arg == 2 ) { ledit.rebase( e.getArg(1), e.getArg(2) ); }
  
    //Open compressed link edit info (bind).

    else if( arg == 3 ) { ledit.bind( e.getArg(1), e.getArg(2) ); }

    //Display the binding actions.

    else if( arg == 4 )
    {
      file.Events = false; bind[] b = ledit.bindSyms( e.getArg(1), e.getArg(2) ); file.Events = true;

      try { file.seek( e.getArg(1) ); Offset.setSelected( e.getArg(1), e.getArg(2) ); } catch( java.io.IOException er ) { }

      String out = "<table border='1'><tr><td>Set address.</td><td>Export method.</td></tr>";

      if( is64bit ) { for( int i = 0; i < b.length; i++ ) { out += "<tr><td>" + String.format( "%1$016X", b[i].loc ) + "</td><td>" + b[i].name + "</td></tr>"; } }
      else { for( int i = 0; i < b.length; i++ ) { out += "<tr><td>" + String.format( "%1$08X", b[i].loc ) + "</td><td>" + b[i].name + "</td></tr>"; } }

      info( out + "</table>" );
    }

    //Expand node on click.

    if( expandNode ) { tree.expandPath(tree.getLeadSelectionPath()); }
  }

  //Some nodes should require a small exploration as they carry out an command like selecting bytes.
  //Thus the commands do not have an descriptor describing the data.

  private static final String[] MInfo = new String[]
  {
    "<html></html>",
    "<html>The binding information uses opcodes which tells us which method to locate in the export section of another binary which we set to the location of each pointer.<br /><br />" +
    "The pointers are not stored in with the link information section. There are two sections loaded into RAM using load commands that have a flag setting of 6 (Bind pointers), 7 (Lazy bind pointers).<br /><br />" +
    "The locations are the positions in memory of which the program will call the method.<br /><br />" +
    "The pointers node takes you to the load command for the pointers, and the opcodes node shows how to read the method names linked to per pointer.<br /><br />" +
    "The actions node shows the information without the opcodes and the address at which the method is called in the program from the pointers section.</html>"
  };
}