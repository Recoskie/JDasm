package Format;

import swingIO.*;
import swingIO.tree.*;
import javax.swing.tree.*;

public class BMP extends Window.Window implements JDEventListener
{
  private JDNode root;
  private int width = 0, height = 0;
  private float pixel_size = 0f;
  private int colorData = 0;

  //Descriptors.

  private Descriptor[] headers = new Descriptor[2];

  //Number of lines are not known till the headers are read.

  private Descriptor[] lines;

  public BMP() throws java.io.IOException
  {
    //Setup.

    tree.setEventListener( this ); file.Events = false;

    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true); tree.setShowsRootHandles(true); root = new JDNode( fc.getFileName() + ( fc.getFileName().indexOf(".") > 0 ? "" : ".bmp" ), -1 );

    //Begin reading the BMP header.

    Descriptor bmp_header = new Descriptor( file ); headers[0] = bmp_header;

    JDNode BHeader = new JDNode("BMP Header.h", 0); root.add( BHeader );

    bmp_header.String8("Signature", 2);
    bmp_header.LUINT32("Size of picture in bytes.");
    bmp_header.LUINT16("Reserved");
    bmp_header.LUINT16("Reserved");
    bmp_header.LUINT32("Pixel colors location"); colorData = (int)bmp_header.value;

    //Read DIB header.

    Descriptor dib_header = new Descriptor( file ); headers[1] = dib_header;

    JDNode DHeader = new JDNode("DIB Header.h", 1); root.add( DHeader );

    dib_header.LUINT32("Size of DIB header"); int dib_size = (int)dib_header.value; dib_size -= 4;
    dib_header.LUINT32("Width in pixels"); width = (int)dib_header.value; dib_size -= 4;
    dib_header.LUINT32("Height in pixels"); height = (int)dib_header.value; dib_size -= 4;
    
    if( dib_size > 4 )
    {
      dib_header.LUINT16("The number of color planes"); dib_size -= 2;
      dib_header.LUINT16("The number of bits per pixel"); pixel_size = ((short)dib_header.value)/8f; dib_size -= 2;
      dib_header.LUINT32("The compression method being used"); dib_size -= 4;
      dib_header.LUINT32("The size of the image"); dib_size -= 4;
      dib_header.LUINT32("Horizontal resolution"); dib_size -= 4;
      dib_header.LUINT32("Vertical resolution"); dib_size -= 4;
      dib_header.LUINT32("The number of colors in the picture"); dib_size -= 4;
      dib_header.LUINT32("The number of important colors used"); dib_size -= 4;

      if( dib_size > 0 )
      {
        dib_header.LUINT16("Horizontal and Vertical unit resolution"); dib_size -= 2;
        dib_header.LUINT16("Padding"); dib_size -= 2;
        dib_header.LUINT16("Pixel order"); dib_size -= 2;
        dib_header.LUINT16("Halftoning algorithm"); dib_size -= 2;
        dib_header.LUINT32("Halftoning parameter 1"); dib_size -= 4;
        dib_header.LUINT32("Halftoning parameter 2"); dib_size -= 4;
        dib_header.LUINT32("Color table encoding"); dib_size -= 4;
        dib_header.LUINT32("Application defined identifier"); dib_size -= 4;
      }
      
      if( dib_size > 0 ) { dib_header.Other("Other Data", dib_size ); }
    }
    else if( dib_size > 0 )
    {
      dib_header.Other("Other Data", dib_size );
    }

    headers[1] = dib_header;
    
    //Setup the lines.

    lines = new Descriptor[height];
    
    JDNode data = new JDNode( "Picture Data", 2 );
    
    for( int i = 1; i <= height; i++ ) { data.add( new JDNode( "line #" + i + ".h", ( i + 2 ) ) ); }

    root.add( data ); Virtual.setVisible(false); tools.update();

    //Decode the setup headers.
    
    ((DefaultTreeModel)tree.getModel()).setRoot(root); file.Events = true;

    //Set the default node.

    tree.setSelectionPath( new TreePath( BHeader.getPath() ) ); open( new JDEvent( this, "", 0 ) );
  }

  public void Uninitialize() { headers = new Descriptor[2]; lines = null; }

  public void open( JDEvent e )
  {
    if( e.getID().equals("UInit") ) { Uninitialize(); }

    else if ( (int)e.getArg(0) < 2 ) { ds.setDescriptor(headers[(int)e.getArg(0)]); }

    else if( e.getArg(0) == 2 )
    {
      tree.expandPath( tree.getLeadSelectionPath() );

      try { file.seek( colorData ); } catch( Exception er ) { }

      Offset.setSelected( colorData, colorData + (int)( ( width * height ) * pixel_size ) - 1 );

      info("The color data that creates the picture."); ds.clear();
    }
    
    //Read an line from the picture.
    
    else
    {
      int line = (int)e.getArg(0) - 3;

      if( lines[line] == null )
      {
        try
        {
          file.seek( colorData + (int)(line * pixel_size * width) ); Descriptor pixels = new Descriptor(file);

          for( int i = 1; i <= width; i++ ) { pixels.Other("pixel color #" + i + "",(int)pixel_size); }

          lines[line] = pixels;
        }
        catch( Exception er ) { }
      }
        
      ds.setDescriptor(lines[line]);
    }
  }
}
