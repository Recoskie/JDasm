package Format;

import swingIO.*;
import swingIO.tree.*;
import javax.swing.tree.*;

public class JPEG extends Window.Window implements JDEventListener
{
  private java.util.LinkedList<Descriptor> des = new java.util.LinkedList<Descriptor>();

  private int ref = 0;

  private JDNode root;
  
  public JPEG() throws java.io.IOException
  {
    //Setup.

    tree.setEventListener( this ); file.Events = false;

    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true); tree.setShowsRootHandles(true); root = new JDNode( fc.getFileName(), -1 );
    
    //Begin reading the JPEG signatures/markers.

    Descriptor jpg = new Descriptor(file); des.add(jpg);

    JDNode h = new JDNode("JPEG Data", ref++); root.add( h );

    jpg.Other("Signature", 2);

    //Read the jpeg markers. All markers start with a 0xFF = -1 code.

    int nx = 0, size = 0, type = 0;

    //Set nx to the marker code if there is an marker.

    long t = file.getFilePointer(); file.read(1); nx = file.toByte(); file.seek(t);

    //Read all the markers and define the data of known marker types.

    while( nx == -1 )
    {
      Descriptor marker = new Descriptor(file); des.add(marker);
  
      marker.UINT8("Maker Code");
      marker.UINT8("Marker type"); type = ((byte)marker.value) & 0xFF;
      marker.UINT16("Maker size"); size = (short)marker.value - 2;

      //Decode the marker if it is a known type.

      if( !decodeMarker( type, size, h ) )
      {
        //Define the markers data if it is not a known maker type.

        marker.Other("Maker Data", size);
      }

      //Read the next byte to check if there is another marker.

      t = file.getFilePointer(); file.read(1); nx = file.toByte(); file.seek(t);
    }

    //Setup headers.
    
    ((DefaultTreeModel)tree.getModel()).setRoot(root); file.Events = true;

    //Set the first node.

    tree.setSelectionPath( new TreePath( h.getPath() ) ); open( new JDEvent( this, "", 0 ) );
  }

  private boolean decodeMarker( int type, int size, JDNode marker )
  {
    if( type == 0xC0 )
    {
      marker.add( new JDNode("Start Of Frame.h", ref++) );
    }
    else if( type == 0xC4 )
    {
      marker.add( new JDNode("Define Huffman Tables.h", ref++) );
    }
    else if( type == 0xDA )
    {
      marker.add( new JDNode("Start Of Scan.h", ref++) );
    }
    else if( type == 0xDB )
    {
      marker.add( new JDNode("Define Quantization Tables.h", ref++) );
    }
    else if( ( type & 0xF0 ) == 0xE0 )
    {
      marker.add( new JDNode("Application (specific).h", ref++) );
    }
    else if( type == 0xFE )
    {
      marker.add( new JDNode("Comment.h", ref++) );
    }
    else
    {
      marker.add( new JDNode("Marker.h", ref++) );
    }

    return( false );
  }

  public void Uninitialize() { des.clear(); ref = 0; }

  public void open( JDEvent e )
  {
    if( e.getID().equals("UInit") ) { Uninitialize(); }

    else if( e.getArg(0) >= 0 )
    {
      tree.expandPath( tree.getLeadSelectionPath() );

      ds.setDescriptor(des.get((int)e.getArg(0)));
    }
    else
    {

    }
  }
}
