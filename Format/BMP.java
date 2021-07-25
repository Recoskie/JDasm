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
  private int compressMode = 0;
  private boolean runLen = false;
  private boolean colorTable = false;
  private boolean topToBottom = false;

  //If we are using run length compression we have to read each line. As the line length may not all be the same.

  private long linePos = 0;
  private int curLine = -1;

  //Descriptors.

  private Descriptor[] headers = new Descriptor[3];

  //Number of lines are not known till the headers are read.

  private Descriptor[] lines;

  public BMP() throws java.io.IOException
  {
    //Setup.

    tree.setEventListener( this ); file.Events = false;

    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true); tree.setShowsRootHandles(true); root = new JDNode( fc.getFileName() + ( fc.getFileName().indexOf(".") > 0 ? "" : ".bmp" ), -1 );

    //Begin reading the BMP header.

    Descriptor bmpHeader = new Descriptor( file ); headers[0] = bmpHeader; bmpHeader.setEvent( this::BMPInfo );

    JDNode BHeader = new JDNode("BMP Header.h", 0); root.add( BHeader );

    bmpHeader.String8("Signature", 2);
    bmpHeader.LUINT32("Size of picture in bytes.");
    bmpHeader.LUINT16("Reserved");
    bmpHeader.LUINT16("Reserved");
    bmpHeader.LUINT32("Pixel colors location"); colorData = (int)bmpHeader.value; linePos = colorData;

    //Read DIB header.

    Descriptor dibHeader = new Descriptor( file ); headers[1] = dibHeader; dibHeader.setEvent( this::DIBInfo );

    JDNode DHeader = new JDNode("DIB Header.h", 1); root.add( DHeader );

    dibHeader.LUINT32("Size of DIB header"); int dibSize = (int)dibHeader.value; dibSize -= 4;

    if( dibSize == 8 )
    {
      dibHeader.LUINT16("Width in pixels"); width = (int)dibHeader.value; dibSize -= 2;
      dibHeader.LINT16("Height in pixels"); height = (int)dibHeader.value; dibSize -= 2;
    }
    else
    {
      dibHeader.LUINT32("Width in pixels"); width = (int)dibHeader.value; dibSize -= 4;
      dibHeader.LINT32("Height in pixels"); height = (int)dibHeader.value; dibSize -= 4;
    }

    if( height < 0 ) { height = -height; topToBottom = true; }
    
    dibHeader.LUINT16("The number of color planes"); dibSize -= 2;
    dibHeader.LUINT16("The number of bits per pixel"); pixel_size = ((short)dibHeader.value)/8f; dibSize -= 2;

    if( dibSize > 0 )
    {
      dibHeader.LUINT32("The compression method being used"); compressMode = (int)dibHeader.value; dibSize -= 4;

      runLen = compressMode == 1 || compressMode == 2 || compressMode == 12 || compressMode == 13;

      dibHeader.LUINT32("The size of the image"); dibSize -= 4;
      dibHeader.LUINT32("Horizontal resolution"); dibSize -= 4;
      dibHeader.LUINT32("Vertical resolution"); dibSize -= 4;
      dibHeader.LUINT32("The number of colors in the picture"); dibSize -= 4;
      dibHeader.LUINT32("The number of important colors used"); dibSize -= 4;
    }

    if( dibSize > 0 )
    {
      dibHeader.UINT32("Red Color Bits"); dibSize -= 4;
      dibHeader.UINT32("Green Color Bits"); dibSize -= 4;
      dibHeader.UINT32("Blue Color Bits"); dibSize -= 4;
      dibHeader.UINT32("Alpha Color Bits"); dibSize -= 4;
    }

    if( dibSize > 0 )
    {
      dibHeader.LUINT32("Color space type"); dibSize -= 4;

      dibHeader.Other("Color End points", 36); dibSize -= 36;

      dibHeader.LUINT32("Red Gamma"); dibSize -= 4;
      dibHeader.LUINT32("Green Gamma"); dibSize -= 4;
      dibHeader.LUINT32("Blue Gamma"); dibSize -= 4;
    }

    if( dibSize > 0 )
    {
      dibHeader.LUINT32("Intent"); dibSize -= 4;
      dibHeader.LUINT32("Profile Data Offset"); dibSize -= 4;
      dibHeader.LUINT32("Profile Size"); dibSize -= 4;
      dibHeader.LUINT32("Reserved"); dibSize -= 4;
    }
      
    if( dibSize > 0 ) { dibHeader.Other("Other Data", dibSize ); }

    headers[1] = dibHeader;

    //If the DIB header does not end at the start of the image color data.
    //Then the image uses a list of colors per pixel.

    if( colorTable = file.getFilePointer() < colorData )
    {
      root.add( new JDNode("Color table.h", 2) );

      Descriptor colors = new Descriptor( file ); headers[2] = colors; colors.setEvent( this::ColorTableInfo );

      int len = (int)(colorData - file.getFilePointer()) / 4;

      for( int i = 0; i < len; i++ ) { colors.Other("RGB color #" + i + "", 4); }
    }
    
    //Setup the lines.

    lines = new Descriptor[height];
    
    JDNode data = new JDNode( "Picture Data", 3 );
    
    if( topToBottom )
    {
      for( int i = 1, ln = 4; i <= height; i++ ) { data.add( new JDNode( "line #" + i + ".h", ln++ ) ); }
    }
    else
    {
      for( int i = height, ln = 4; i > 0; i-- ) { data.add( new JDNode( "line #" + i + ".h", ln++ ) ); }
    }

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

    else if ( (int)e.getArg(0) < 3 )
    {
      ds.setDescriptor(headers[(int)e.getArg(0)]);

      if( e.getArg(0) == 2 ) { info( "<html>The color table stores standard RGB colors only. Each color is addressable by number of bit's per pixel value.</html>" ); }
    }

    else if( e.getArg(0) == 3 )
    {
      tree.expandPath( tree.getLeadSelectionPath() );

      try
      {
        if( runLen )
        {
          file.seek( colorData ); Offset.setSelected( colorData, file.length() - 1 );
        }
        else
        {
          file.seek( colorData ); Offset.setSelected( colorData, colorData + (int)( ( width * height ) * pixel_size ) - 1 );
        }
      } catch( Exception er ) { }

      info( "<html>The color array creates the picture one line at a time per pixel.<br /><br />" + pixelArray + "</html>" ); ds.clear();
    }

    //Read an line from the picture.
    
    else
    {
      int line = (int)e.getArg(0) - 4;

      //If we are using run length compression.

      if( runLen )
      {
        //Each line must be read one at a time till line position.

        file.Events = false;

        boolean end = false; //End of line command.
        int rl = 0; //Used to check if run length 0 before adding data descriptor description.
        long t = 0; //Temporary position.

        while( curLine < line )
        {
          try
          {
            file.seek( linePos ); Descriptor pixels = new Descriptor(file); pixels.setEvent( this::PixInfo );

            end = false; while( !end )
            {
              t = file.getFilePointer(); rl = file.readByte(); file.seek(t);

              if( rl != 0 ) { pixels.UINT8("Color Run Length"); pixels.Other("pixel color", (int)( pixel_size + 0.5 )); }
              
              //Run lengths of 0 are used as commands.

              else
              {
                pixels.UINT8("Command"); t = file.getFilePointer(); rl = file.readByte(); file.seek(t);

                if( rl == 0 )
                {
                  pixels.Other("End of line",1);

                  curLine += 1; linePos = file.getFilePointer();
                    
                  lines[curLine] = pixels; end = true;
                }
                else if( rl == 1 )
                {
                  pixels.Other("End of Bit map",1);

                  curLine += 1; linePos = file.getFilePointer();
                    
                  lines[curLine] = pixels; end = true;
                }
                else if( rl == 2 )
                {
                  pixels.Other("Move Position",1); pixels.UINT8("Move to the right");

                  //It is possible to skip lines using a delta position.

                  pixels.UINT8("Move Down lines");
                  
                  if( Byte.compareUnsigned((byte)pixels.value, (byte)0) > 0 )
                  {
                    curLine += 1; linePos = file.getFilePointer();
                    
                    lines[curLine] = pixels; curLine += ((byte)pixels.value) & 0xFF;
                    
                    end = true;
                  }
                }
                else
                {
                  pixels.UINT8("Number of Pixel colors"); int i = rl & 0xFF;

                  if( pixel_size == 0.5 ){ i /= 2; } //4-bit color.

                  boolean pad = i % 2 == 1; //Must be aligned by 16 bits.

                  for( ; i > 0; i-- ) { pixels.Other("pixel color", (int)( pixel_size + 0.5 )); }

                  if( pad ){ pixels.Other("Padding", 1); }
                }
              }
            }
          }
          catch( Exception er ) { curLine += 1; er.printStackTrace(); } 
        }

        file.Events = true;
      }

      //Else line width are all the same.
      
      else
      {
        if( lines[line] == null )
        {
          file.Events = false;

          try
          {
            file.seek( colorData + (int)(line * pixel_size * width) ); Descriptor pixels = new Descriptor(file); pixels.setEvent( this::PixInfo );

            int closestByte = (int)Math.ceil(pixel_size), pxInByte = (int)( 1 / ( pixel_size - Math.floor(pixel_size) ) );

            for( int i = 1; i <= width; i += pxInByte )
            {
              pixels.Other( "pixel color #" + i + "", closestByte );
            }

            lines[line] = pixels;
          }
          catch( Exception er ) { }
        }

        file.Events = true;
      }

      //Display the read line.

      if( lines[line] != null ) { ds.setDescriptor(lines[line]); } else { info("<html>This line was skipped.</html>"); ds.clear(); }
    }
  }

  //BMP header data.

  public static final String[] BMPInfo = new String[]
  {
    "<html>The BMP header must start with BM = 42, 4D.<br /><br />If it does not pass the signature test then the picture is corrupted.</html>",
    "<html>This is the size of the bitmap file.</html>",
    "<html>Reserved, for future use. Should always be set 0.</html>",
    "<html>Reserved, for future use. Should always be set 0.</html>",
    "<html>This is the location to the pixel color data.</html>"
  };

  //Bitmap color basics.

  public static final String pixelArray = "The DIB header specifies the number of bits each pixel color is. The default is generally standard Red, Green, Blue = 24-bits/per pixel (Compression mode 0).<br /><br />" +
  "It is also important to check the compassion setting in the DIB header, for if it uses a set number of bits for Red, green, blue (Compression mode 3).<br /><br /><hr /><br />" +
  "If a color table is present, then The default is standard Red, Green, Blue 24-bits/per pixel, and also, Compression mode 3 is ignored and acts as Compression mode 0.<br /><br />" +
  "Each color in the color table is 32 bit's long and uses the three first bytes as standard Reg, green, blue color.<br /><br />" +
  "The number of bits we are using for each color is read as a number for which color we wish to use from the color table for a pixel color.";

  //Color masking info.

  public static final String colorMask = "If there is a standard RGB color table, then compression mode 3 is ignored, and number of bits for each color is ignored in the DIB header.<br /><br />" +
  "If there is no standard RGB color table and compression mode is 3, then Compression mode 3 lets you use nonstandard ranges for each color.<br /><br />" +
  "The number of bits chosen for red, green, blue in the DIB header is used as a percentage out of 255. In the case of 4-bit red 0 to 15 value. Red 15 is 255, and Red 8 is 136.<br /><br />" +
  "This is if you wish to display such a bitmap properly using graphics/video memory.";

  //Run length compression explained.

  public static final String runLength = "Bit map uses run-length compression. It can only be used with a standard RGB color table and 4, or 8 bits per pixel.<br /><br />" +
  "Compression mode 1 uses 8 bits per pixel with a color table. The first byte is how many times the color is used in a row along the line, then the next byte is which color from the color table.<br /><br />" +
  "Compression mode 2 uses 4 bits per pixel with a color table. The first byte is how many times the color is used in a row along the line, then the next byte is tow colors from the color table.<br /><br />" +
  "The 4-bit version alternates between the two colors unless you set both 4 bits in the byte to the same color.<br /><br /><hr /><br />" +
  "Any run length that is 0 in size uses the next byte as a command.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>Code</td><td>Use</td></tr>" +
  "<tr><td>00</td><td>End of line.</td></tr>" +
  "<tr><td>01</td><td>End of bitmap.</td></tr>" +
  "<tr><td>02</td><td>The next byte is read as the number of lines to skip, and the next byte is read for the starting position on that new line.</td></tr>" +
  "</table><br />" +
  "Command 02 assumes that the same colors of the current line continue for the number of lines skipped.<br /><br />" +
  "Any Command higher than 02 is used as the number of pixel colors to read.<br /><br />" +
  "In 4 bits per pixel, we divide this number by 2 as each byte is two colors. Also, if the last color that is read is not an even position, it is padded with an extra byte.";

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
    "<html>The bitmap height in pixels. It is used to know where the end of the color data is.<br /><br />" +
    "If height is negative, then the picture lines go top to bottom. If height is positive, then the picture lines go bottom to top.</html>",
    "<html>The number of color planes. Number of BMP pictures in file. Never used. It should always be set 1.</html>",
    "<html>The number of bits per pixel, which is the color depth of the image. Typical values are 1, 4, 8, 16, 24 and 32.<br /><br />" +
    "The most typical bit depth used is 24. Meaning each color is standard Red, Green, Blue per pixel.</html>",
    "<html>the compression method being used.<br /><br >" +
    "<table border=\"1\">" +
    "<tr><td>Value</td><td>Compression method.</td></tr>" +
    "<tr><td>0</td><td>No Compression. Regular Red, Green, Blue per 24-bit/pixel.</td></tr>" +
    "<tr><td>1</td><td>Run-length encoding 8-bit/pixel bitmaps.</td></tr>" +
    "<tr><td>2</td><td>Run-length encoding 4-bit/pixel bitmaps.</td></tr>" +
    "<tr><td>3</td><td>No Compression. Picture uses no color table. The specified number of bits for RED, Green, Blue, Alpha is used from the DIB header.</td></tr>" +
    "<tr><td>4</td><td>Specifies that the image is compressed using the JPEG file Interchange Format. JPEG compression trades off compression against loss; it can achieve a compression ratio of 20:1 with little noticeable loss.</td></tr>" +
    "<tr><td>5</td><td>Specifies that the image is compressed using the PNG file Interchange Format.</td></tr>" +
    "</table><br />" + colorMask,
    "<html>This is the size of the bitmap without the headers.</html>",
    "<html>The horizontal resolution, in pixels-per-meter, of the target device for the bitmap.</html>",
    "<html>The vertical resolution, in pixels-per-meter, of the target device for the bitmap.</html>",
    "<html>The number of colors in the color table that are actually used by the bitmap, or 0 to default to all colors.</html>",
    "<html>The minium number of colors that need to be read in from the start of the color table, for displaying the bitmap.<br /><br />" +
    "If this value is zero, all colors are read from start to end of color table.</html>",
    "<html>Specifies bits used for red color value. Typically this is set 00 00 FF 00.<br /><br />" +
    "If the number of bits we are using for each pixel color is 24, We then read the first 24 bits of 00 00 FF 00 is 00 00 FF.<br /><br >" +
    "The bytes are then flipped in little-endian byte order as FF 00 00, meaning the first byte is 0 to 255 Red.<br /><br /><hr /><br />" +
    "This is useful for 16 bits per color with no color table. As we can specify, 00 F8 00 00 making Red 5 bit's big.<br /><br />" +
    "The first 16 bits of 00 F8 00 00 is 00 F8. In little-endian byte order it is F8 00 = 11111 00000000000 binary. Making the first 5 binary digits the 0 to 15 red color value.<br /><br /><hr /><br />" + colorMask + "</html>",
    "<html>Specifies bits used for green color value. Typically this is set 00 FF 00 00.<br /><br />" +
    "If the number of bits we are using for each pixel color is 24, We then read the first 24 bits of 00 FF 00 00 is 00 FF 00.<br /><br >" +
    "The bytes are then flipped in little-endian byte order as 00 FF 00, meaning the second byte is 0 to 255 green.<br /><br /><hr /><br />" +
    "This is useful for 16 bits per color with no color table. As we can specify E0 07 00 00 making green 6 bit's big.<br /><br />" +
    "The first 16 bits of E0 07 00 00 is E0 07. In little-endian byte order it is 07 E0 = 00000 11111 00000 binary. Making the mid 6 binary digits 0 to 31 green color value.<br /><br /><hr /><br />" + colorMask + "</html>",
    "<html>Specifies bits used for green color value. Typically this is set FF 00 00 00.<br /><br />" +
    "If the number of bits we are using for each pixel color is 24, We then read the first 24 bits of FF 00 00 00 is FF 00 00.<br /><br >" +
    "The bytes are then flipped in little-endian byte order as 00 00 FF meaning the last byte is 0 to 255 blue.<br /><br /><hr /><br />" +
    "This is useful for 16 bits per color with no color table. As we can specify 1F 00 00 00 makings blue 5 bit's big.<br /><br />" +
    "The first 16 bits of 1F 00 00 00 is 1F 00. In little-endian byte order it is 00 1F = 0000000000 11111 binary. Making the last 5 binary digits 0 to 15 blue color value.<br /><br /><hr /><br />" + colorMask + "</html>",
    "<html>Specifies bits used, for transparent (alpha) color value. Typically this is set 00 00 00 FF hex meaning 0 to 255 byte.<br /><br />" +
    "If the number of bits we are using for each pixel color is 32, We then read the first 32 bits of FF 00 00 00 is FF 00 00 00.<br /><br >" +
    "The bytes are then flipped in little-endian byte order as 00 00 00 FF meaning the last byte is 0 to 255 Alpha, for RGBA color.<br /><br /><hr /><br />" +
    "This is useful for 16 bit's per color with no color table. As we can make Red, green, and blue 5 bits each leaving us one bit for visible, or invisible pixels.<br /><br /><hr /><br />" + colorMask + "</html>",
    "<html>Color space type.</html>",
    "<html>Color Space endpoints.</html>",
    "<html>Toned response curve for red. The first 16 bits are the unsigned integer value. The last 16 bits is the value after the decimal point.</html>",
    "<html>Toned response curve for green. The first 16 bits are the unsigned integer value. The last 16 bits is the value after the decimal point.</html>",
    "<html>Toned response curve for blue. The first 16 bits are the unsigned integer value. The last 16 bits is the value after the decimal point.</html>",
    "<html>Rendering intent.</html>",
    "<html>Offset to the start of the profile data.</html>",
    "<html>Size, in bytes, of embedded profile data.</html>",
    "<html>This member has been reserved. Its value should be set to zero.</html>"
  };

  public void DIBInfo( int el )
  {
    if( el < 0 )
    {
      info( "<html>The first value read is the DIB header size.<br /><br />" +
      "The DIB header can end at width and height settings, or can be made longer in size to use more settings and properties that are read in series.</html>" );
    }
    else
    {
      info( DIBInfo[ el ] );
    }
  }

  //bit/pixel color information.

  public void PixInfo( int el )
  {
    //Check if bit map is using run length compression.

    if( runLen ) { info( runLength ); }

    //Check if there is a color table.

    else if( colorTable )
    {
      //Pixel sizes smaller than a byte.

      if( pixel_size == 0.125 )
      {
        info( "<html>Each byte is 8 pixel colors. A binary digit of 0 is color number 0 from the color table. A binary digit of 1 is color number 1 from the color table.<br /><br />" +
        "The color table can only store standard Red, Green, Blue colors.</html>" );
      }

      //Pixel sizes smaller than a byte.

      else if( pixel_size == 0.5 )
      {
        info( "<html>Each byte is 2 pixel colors. The first hex digit is the first 0 to 15 color, and the last hex digit is 0 to 15 color.<br /><br />" +
        "The color table can only store standard Red, Green, Blue colors.</html>" );
      }

      //The other common sizes are a multiple of byte.

      else
      {
        info( "<html>The color value is a color number to use from the color table. The color table can only store standard Red, Green, Blue, colors.</html>" );
      }
    }

    //Check if the picture uses an specialized color type mask.

    else if( compressMode == 3 )
    {
      info( "<html>The DIB header specifies the number of bits to use for each RGB color.<br /><br />" +
      "Goto the DIB header, and click on Red Color bits and the other colors for a detailed description.<br /><br /><hr /><br />" +
      "Note that the bytes are in little-endian order, meaning an 11-bit pixel color E0 07 is actually 07 E0 in reverse byte order.<br /><br />" +
      "Lastly, the first 11 bits of 07E0 in binary is 00000111111. This is important to read the correct bits that are set for each color in the DIB header.<br /><br /><hr /><br />" +
      "To display bitmap images that use nonstandard bit ranges for each color, we have to divide standard red, green, blue 0 to 255 up by the number of selectable color values using the number of bits we have chosen.<br /><br />" +
      "This is if you wish to display such a bitmap properly using graphics/video memory.</html>" );
    }
    
    //Otherwise we have a few different default graphics color types.

    else if( pixel_size == 3 )
    {
      info( "<html>The 3 bytes are standard Red, Green, Blue color. Each color has a shade range of 0 to 255.<br /><br />" +
      "Note that the bytes are in little-endian order, meaning blue ends up being the first byte in reverse byte order then green and red.</html>" );
    }

    else if( pixel_size == 4 )
    {
      info( "<html>The 4 bytes are standard Red, Green, Blue, Alpha color. Each color has a shade range of 0 to 255.<br /><br />" +
      "Note the bytes are in little-endian order, meaning blue ends up being the first byte in reverse byte order then green, red, and finally Alpha.</html>" );
    }
  }

  //bit/pixel color information.

  public void ColorTableInfo( int el )
  {
    info( "<html>The 3 bytes are standard Red, Green, Blue color. Each color has a shade range of 0 to 255. The last byte is reserved, for future use.<br /><br />" +
    "The bytes are in little-endian byte order, meaning blue ends up being the first byte in reverse byte order then green, and red.</html>" );
  }
}
