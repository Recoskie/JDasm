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
  
  public MAC() throws java.io.IOException
  {
    tree.setEventListener( this ); file.Events = false;

    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true); tree.setShowsRootHandles(true); root = new JDNode( fc.getFileName() + ( fc.getFileName().indexOf(".") > 0 ? "" : ".exe" ), -1 );

    //Load the application header.

    header.readMAC( root );

    //If it is the main application header then load the binary using the load commands.

    if( App != null )
    {
      JDNode cmd = new JDNode("Load Commands"); commands.load( cmd ); root.add( cmd );
    }

    //Set binary tree view, and enable IO system events.
      
    ((DefaultTreeModel)tree.getModel()).setRoot(root); file.Events = true;

    //Set the selected node.
  
    tree.setSelectionPath( new TreePath( ((DefaultMutableTreeNode)root.getFirstChild()).getPath() ) );

    //Make it as if we clicked and opened the node.

    open( new JDEvent( this, "", new long[]{ 0, 0 } ) );
  }

  public void Uninitialize() { des = new java.util.ArrayList<Descriptor>(); ref = 0; DTemp = null; App = null; }

  public void open(JDEvent e)
  {
    if( e.getID().equals("UInit") ) { Uninitialize(); }

    else if( e.getArg(0) < 0 )
    {
      ds.clear();

      //Select bytes in virtual space.

      if( e.getArg( 0 ) == -3 )
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

      else if( e.getArg( 0 ) == -2 )
      {
        if( tree.getLastSelectedPathComponent().toString().equals("Load Commands") )
        {
          tree.expandPath( tree.getSelectionPath() );
        
          info("The load commands tell us what each section of the binary is and where to put sections into virtual address space.");
        }

        try { file.seek( e.getArg(1) ); Offset.setSelected( e.getArg(1), e.getArg(2) ); } catch( java.io.IOException er ) { }
      }
    }

    //Command 0 sets a descriptor for a section of data in the binary tree.

    else if( e.getArg( 0 ) == 0 ) { ds.setDescriptor( des.get( (int)e.getArg( 1 ) ) ); }

    //Open application header within universal binaries.

    else if( e.getArg( 0 ) == 1 )
    {
      JDNode root = (JDNode)tree.getLastSelectedPathComponent();

      //We do not want to reload an existing binary if already loaded.

      if( App != root )
      {
        int Offset = (int)e.getArg(1); file.Events = false;

        //We can switch between binaries in a universal binary.

        try
        {
          //Load the main application header.

          file.seek( Offset ); header.readMAC( root );

          //Begin loading the program with load commands.

          JDNode cmd = new JDNode("Load Commands"); commands.load( cmd ); root.add( cmd );
        }
        catch(Exception er) { er.printStackTrace(); }

        file.Events = true; tree.setSelectionPath( new TreePath( App.getPath() ) ); tree.expandPath( new TreePath( App.getPath() ) );
      }
    }
  }
}