package Format;

import swingIO.*;
import swingIO.tree.*;
import javax.swing.tree.*;
import Format.MACDecode.*;

public class MAC extends Data implements JDEventListener
{
  //The binary tree stores which descriptor to set from the integer "ref".

  private JDNode root;

  //Mac header reader.

  private static Headers header = new Headers();
  
  public MAC() throws java.io.IOException
  {
    tree.setEventListener( this ); file.Events = false;

    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true); tree.setShowsRootHandles(true); root = new JDNode( fc.getFileName() + ( fc.getFileName().indexOf(".") > 0 ? "" : ".exe" ), -1 );

    header.readMAC( root );

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
      JDNode root = (JDNode)tree.getLastSelectedPathComponent();

      //We do not want to reload an existing binary if already loaded.

      if( App != root )
      {
        int Offset = (int)e.getArg(1); file.Events = false;

        try { file.seek( Offset ); header.readMAC( root ); } catch(Exception er) { er.printStackTrace(); }

        file.Events = true; tree.setSelectionPath( new TreePath( App.getPath() ) ); tree.expandPath( new TreePath( App.getPath() ) );
      }
    }
  }
}