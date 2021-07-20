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

    Descriptor bmp_header = new Descriptor( file ); headers[0] = bmp_header; bmp_header.setEvent( this::BMPInfo );

    JDNode BHeader = new JDNode("BMP Header.h", 0); root.add( BHeader );

    bmp_header.String8("Signature", 2);
    bmp_header.LUINT32("Size of picture in bytes.");
    bmp_header.LUINT16("Reserved");
    bmp_header.LUINT16("Reserved");
    bmp_header.LUINT32("Pixel colors location"); colorData = (int)bmp_header.value;

    //Read DIB header.

    Descriptor dib_header = new Descriptor( file ); headers[1] = dib_header; dib_header.setEvent( this::DIBInfo );

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

      info("The color data that creates the picture. Most bit maps are RED, Green, Blue color per pixel."); ds.clear();
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

  //BMP header data.

  public static final String[] BMPInfo = new String[]
  {
    "<html>The BMP header must start with BM = 42, 4D.<br /><br />If it does not pass the signature test then the picture is corrupted.</html>",
    "<html>This is the size of the bitmap file.</html>",
    "<html>Value depends on the application that creates the image, if created manually can be 0.</html>",
    "<html>Value depends on the application that creates the image, if created manually can be 0.</html>",
    "<html>This is the location to the pixel color data.<br /><br />" +
    "The DIB header specifies the number of bits each pixel is. The default is generally Red, Green, Blue per 24-bits/pixel.<br /><br />" +
    "It is also important to check if the picture uses compassion setting other than 0 in DIB header, for if it uses subtractive colors instead of reg, green blue, or each color starts with a byte specifying a run length using run length compression.</html>"
  };

  public void BMPInfo( int el )
  {
    if( el < 0 )
    {
      info( "<html>The BIT map header signature is checked to ensures it is a BIT map picture.<br /><br />" +
      "The bit map header specifies the size of the file, and the location to the picture color data.</html>" );
    }
    else
    {
      info( BMPInfo[ el ] );
    }
  }

  //BMP DIB header data.

  public static final String[] DIBInfo = new String[]
  {
    "<html>The length of the DIB header. The DIB has optional settings and is variable in length.</html>",
    "<html>The bitmap width in pixels. It is used to know the end of each line in the color data.</html>",
    "<html>The bitmap height in pixels. It is used to know where the end of the color data is.</html>",
    "<html>The number of color planes. Number of BMP pictures in file. Never used. It should always be set 1.</html>",
    "<html>The number of bits per pixel, which is the color depth of the image. Typical values are 1, 4, 8, 16, 24 and 32.<br /><br />" +
    "The most typical bit depth used is 24. Meaning each color is Red, Green, Blue per pixel.</html>",
    "<html>the compression method being used.<br /><br >" +
    "<table border=\"1\">" +
    "<tr><td>Value</td><td>Compression method.</td></tr>" +
    "<tr><td>0</td><td>No Compression. Regular Red, Green, Blue per 24-bit/pixel.</td></tr>" +
    "<tr><td>1</td><td>Run-length encoding 8-bit/pixel bitmaps.</td></tr>" +
    "<tr><td>2</td><td>Run-length encoding 4-bit/pixel bitmaps.</td></tr>" +
    "<tr><td>3</td><td>No Compression. Uses Red, Green, Blue, Alpha per 32-bit/pixel.</td></tr>" +
    "<tr><td>4</td><td>The image is a JPEG image.</td></tr>" +
    "<tr><td>5</td><td>The image is a PNG image.</td></tr>" +
    "<tr><td>11</td><td>No compression. Uses CMYK Subtractive colors.</td></tr>" +
    "<tr><td>12</td><td>Run-length encoding. Uses CMYK Subtractive colors per 8-bit/pixel.</td></tr>" +
    "<tr><td>13</td><td>Run-length encoding. Uses CMYK Subtractive colors per 4-bit/pixel.</td></tr>" +
    "</table></html>",
    "<html>This is the size of the bitmap without the headers; a dummy 0 can be given for regular Red, Green, Blue per 24-bit/pixel bitmaps.</html>",
    "<html>The horizontal resolution of the image. (pixel per metre, signed integer).</html>",
    "<html>The vertical resolution of the image. (pixel per metre, signed integer).</html>",
    "<html>The number of colors in the color palette, or 0 to default to 2^n.</html>",
    "<html>The number of important colors used, or 0 when every color is important; generally ignored.</html>",
    "<html>An enumerated value specifying the units for the horizontal and vertical resolutions (offsets 38 and 42). The only defined value is 0, meaning pixels per metre.</html>",
    "<html>Padding is ignored and should be zero.</html>",
    "<html>An enumerated value indicating the direction in which the bits fill the bitmap. The only defined value is 0, meaning the origin is the lower-left corner. Bits fill from left-to-right, then bottom-to-top.<br /><br />" +
    "Note that Windows bitmaps (which don't include this field) can also specify an upper-left origin (bits fill from left-to-right, then top-to-bottom) by using a negative value for the image height</html>",
    "<html>An enumerated value indicating a halftoning algorithm that should be used when rendering the image.</html>",
    "<html>Halftoning parameter 1 (offset 64) is the percentage of error damping. 100 indicates no damping. 0 indicates that errors are not diffused.</html>",
    "<html>Halftoning parameters 1 and 2 (offsets 64 and 68, respectively) represent the X and Y dimensions, in pixels, respectively, of the halftoning pattern used.</html>",
    "<html>Halftoning parameters 1 and 2 (offsets 64 and 68, respectively) represent the X and Y dimensions, in pixels, respectively, of the halftoning pattern used.</html>",
    "<html>An enumerated value indicating the color encoding for each entry in the color table. The only defined value is 0, indicating RGB.</html>",
    "<html>An application-defined identifier. Not used for image rendering.</html>",
    "<html>Unknown settings.</html>"
  };

  public void DIBInfo( int el )
  {
    if( el < 0 )
    {
      info( "<html>The first value read is the DIB header size.<br /><br />" +
      "The DIB header can end at width and height, or be longer in size and use more settings and properties that are read in series.<br /><br />" +
      "The settings go in order, and ends at size of the DIB header.</html>" );
    }
    else
    {
      info( DIBInfo[ el ] );
    }
  }
}
