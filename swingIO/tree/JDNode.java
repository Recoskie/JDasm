package swingIO.tree;

import javax.swing.tree.DefaultMutableTreeNode;

public class JDNode extends DefaultMutableTreeNode
{
  //Additional settable properties, for advanced event handselling.

  private String id = "";
  private long[] args = new long[]{0};

  //Construct the node.
  
  public JDNode( String name ) { super( name ); }

  public JDNode( String name, String ID ){ super( name ); id = ID; }

  public JDNode( String name, long Args ){ super( name ); args = new long[]{Args}; }

  public JDNode( String name, long[] Args ){ super( name ); args = Args; }

  public JDNode( String name, String ID, long Args ){ super( name ); id = ID; args = new long[]{Args}; }

  public JDNode( String name, String ID, long[] Args ){ super( name ); id = ID; args = Args; }

  //Get the additional properties.

  public String getID() { return( id ); }

  public long[] getArgs() { return( args ); }

  public long getArg( int el ){ return( args[el] ); }
}