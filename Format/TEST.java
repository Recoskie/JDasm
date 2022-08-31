package Format;

import swingIO.*;
import swingIO.tree.*;
import javax.swing.tree.*;

public class TEST extends Window.Window implements JDEventListener
{
  private JDNode root;
  private Descriptor[] headers = new Descriptor[2];
  
  public TEST() throws java.io.IOException
  {
    //Setup.

    tree.setEventListener( this ); file.Events = false;

    //Change ".xxx" to your file formats file extension.

    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true); tree.setShowsRootHandles(true); root = new JDNode( fc.getFileName() + ( fc.getFileName().indexOf(".") > 0 ? "" : ".xxx" ), -1 );
    
    //Begin reading the file header.

    headers[0] = new Descriptor( new dataType[]
    {
      new dataType("Value 1", Descriptor.Int32 ),
      new dataType("Value 2", Descriptor.Int32 ),
      new dataType("Value 3", Descriptor.Int32 ),
      new dataType("Value 4", Descriptor.Int32 ),
      new dataType("Value 5", Descriptor.Int32 ),
      new dataType("Value 6", Descriptor.Int32 ),
      new dataType("Value 7", Descriptor.Int32 ),
      new dataType("Value 8", Descriptor.Int32 ),
      new dataType("Value 9", Descriptor.Int32 ),
      new dataType("Value 10", Descriptor.Int32 ),
      new dataType("Value 11", Descriptor.Int32 ),
      new dataType("Value 12", Descriptor.Int32 ),
      new dataType("Value 13", Descriptor.Int32 ),
      new dataType("Value 14", Descriptor.Int32 ),
      new dataType("Value 15", Descriptor.Int32 ),
      new dataType("Value 16", Descriptor.Int32 ),
      new dataType("Value 17", Descriptor.Int32 ),
      new dataType("Value 18", Descriptor.Int32 ),
      new dataType("Value 19", Descriptor.Int32 ),
      new dataType("Value 20", Descriptor.Int32 ),
      new dataType("Value 21", Descriptor.Int32 ),
      new dataType("Value 22", Descriptor.Int32 ),
      new dataType("Value 23", Descriptor.Int32 ),
      new dataType("Value 24", Descriptor.Int32 ),
      new dataType("Value 25", Descriptor.Int32 ),
      new dataType("Value 26", Descriptor.Int32 ),
      new dataType("This is a long description!", Descriptor.Int32 ),
      new dataType("Value 28", Descriptor.Int32 ),
      new dataType("Value 29", Descriptor.Int32 ),
      new dataType("Value 30", Descriptor.Int32 ),
      new dataType("Value 31", Descriptor.Int32 ),
      new dataType("Value 32", Descriptor.Int64 ),
      new dataType("Value 33", Descriptor.Int32 ),
      new dataType("Value 34", Descriptor.Int32 ),
      new dataType("Value 35", Descriptor.Int32 ),
      new dataType("Value 36", Descriptor.Int32 )
    } );

    //Begin reading the file header.

    headers[1] = new Descriptor( new dataType[]
    {
      new dataType("Value 1", Descriptor.Int32 ),
      new dataType("Value 2", Descriptor.Int32 ),
      new dataType("Value 3", Descriptor.Int32 ),
      new dataType("Value 4", Descriptor.Int32 )
    } );
    
    headers[0].setEvent( this::infoExample ); headers[1].setEvent( this::infoExample );

    JDNode FHeader1 = new JDNode("File Header1.h", new long[]{ 0, 0x0000 } ); root.add( FHeader1 );

    JDNode FHeader2 = new JDNode("File Header2.h", new long[]{ 1, 0x0100 } ); root.add( FHeader2 );
      
    tools.update(); //Update the window and its tools.
      
    ((DefaultTreeModel)tree.getModel()).setRoot(root); file.Events = true;

    //Set the selected node.
  
    tree.setSelectionPath( new TreePath( FHeader1.getPath() ) );

    //Make it as if we clicked and opened the node.

    open( new JDEvent( this, "", new long[]{ 0, 0x0000 } ) );
  }

  public void Uninitialize() { }

  //This event is called when the user clicks on an tree node.

  public void open(JDEvent e)
  {
    int el = (int)e.getArg(0); long offset = e.getArg(1);

    if( e.getID().equals("UInit") ) { Uninitialize(); }

    if( el >= 0 )
    {
      headers[el].pos = offset;
      
      Offset.setSelected(offset, offset + headers[el].length() - 1);
      
      ds.setDescriptor( headers[el] );
    }
  }

  private static final String[] infoEx = new String[]
  {
    "<html>This is an example Property.</html>",
    "<html>This is the second value read by the descriptor.</html>"
  };

  public void infoExample( int index )
  {
    if( index < 0 )
    {
      info("You just set this descriptor, but did not click on any values read in the descriptor.");
    }
    else if( index < 2 )
    {
      info( infoEx[ index ] );
    }
    else { info( "Value = " + (index + 1) + "" ); }
  }
}