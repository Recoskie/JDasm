package Format.RIFFDecode;

import swingIO.*;
import swingIO.tree.*;

public class ANI extends Data implements RSection
{
  public boolean init( String tag )
  {
    return( tag.equals("anih") || tag.equals("rate") || tag.equals("seq ") );
  }

  public void section( String name, long size, JDNode  node ) throws java.io.IOException
  {
    if( name.equals("anih") )
    {
      node.removeAllChildren();

      Descriptor aniHeader = new Descriptor( file ); des.add( aniHeader ); aniHeader.setEvent( this::ANIInfo );

      node.add( new JDNode( "Cursor Header.h", ref++ ) );

      if( size > 3 ) { aniHeader.LUINT32("Size"); size -= 4; }
      if( size > 3 ) { aniHeader.LUINT32("Frames"); size -= 4; }
      if( size > 3 ) { aniHeader.LUINT32("Steps"); size -= 4; }
      if( size > 3 ) { aniHeader.LUINT32("Width"); size -= 4; }
      if( size > 3 ) { aniHeader.LUINT32("Height"); size -= 4; }
      if( size > 3 ) { aniHeader.LUINT32("Bits per pixel"); size -= 4; }
      if( size > 3 ) { aniHeader.LUINT32("Planes"); size -= 4; }
      if( size > 3 ) { aniHeader.LUINT32("Picture rate"); size -= 4; }
      if( size > 3 ) { aniHeader.LUINT32("Attributes"); size -= 4; }
      if( size > 0 ) { aniHeader.Other("Extended data", (int)size ); }

      initPaths.add( 0, new javax.swing.tree.TreePath( node.getFirstLeaf().getPath() ) );
    }

    else if( name.equals("rate") )
    {
      node.removeAllChildren();

      Descriptor aniHeader = new Descriptor( file ); des.add( aniHeader ); aniHeader.setEvent( this::RateInfo );

      node.add( new JDNode( "Frame rate.h", ref++ ) );

      while( size > 3 ) { aniHeader.LUINT32("Rate"); size -= 4; }

      if( size > 0 ) { aniHeader.Other("???", (int)size ); }

      initPaths.add( new javax.swing.tree.TreePath( node.getPath() ) );
    }

    else if( name.equals("seq ") )
    {
      node.removeAllChildren();

      Descriptor aniHeader = new Descriptor( file ); des.add( aniHeader ); aniHeader.setEvent( this::SeqInfo );

      node.add( new JDNode( "Frame order.h", ref++ ) );

      while( size > 3 ) { aniHeader.LUINT32("Frame"); size -= 4; }

      if( size > 0 ) { aniHeader.Other("???", (int)size ); }

      initPaths.add( new javax.swing.tree.TreePath( node.getPath() ) );
    }
  }

  public void open( JDEvent e ) { }

  public static final String if_icon = "<br /><br />This can be 0 if Attributes specifies that images are icons.<br /><br />" +
  "If pictures are not icons then they are straight Red, Green, Blue color per pixel (bit map pictures).";

  public static final String[] ANI = new String[]
  {
    "<html>Size of this header.</html>",
    "<html>The number of pictures stored in the frame list (\"LIST (fram)\").</html>",
    "<html>Number of frames to be displayed before the animation repeats.</html>",
    "<html>Width of each picture in pixels." + if_icon + "</html>",
    "<html>Height of each picture in pixels." + if_icon + "</html>",
    "<html>Number of bits per pixel.<br /><br />Unusually 8 bits (pixel color a standard Red, Green, blue color)." + if_icon + "</html>",
    "<html>Number of color planes should be 1 as one picture in bitmap." + if_icon + "</html>",
    "<html>Default frame display rate. Note that 60 is one second, so 90 is 1 second and a half.</html>",
    "<html>Specifies if pictures are icons instead of bitmap by default, and also if there is a \"seq\" section.<br /><br />" +
    "<table border=\"1\">" +
    "<tr><td>Bit setting</td><td>Description</td></tr>" +
    "<tr><td>00000000000000000000000000000001</td><td>Pictures are ICON's instead of bitmaps.</td></tr>" +
    "<tr><td>00000000000000000000000000000010</td><td>The \"seq\" section exists for the order we wish to draw the pictures in.</td></tr>" +
    "</table><br /><br />Note that the value should be viewed in binary.</html>",
    "<html>Unknown data.</html>"
  };

  public void ANIInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>Animated cursors consist of a main header that sets the speed to go through each bitmap/icon picture in the \"LIST (fram)\".<br /><br />" +
      "There is also 2 sections that are optional.<br /><br />" +
      "The \"seq\" section lets you set the order each picture is used from the \"LIST (fram)\" section.<br /><br />" +
      "The \"rate\" section lets you set the delay between each picture.<br /><br />" +
      "They are not necessary as the frame list is in order; unless you want to use the same picture multiple times between the animation, and the rate should never have to very.</html>");
    }
    else
    {
      info( ANI[el] );
    }
  }

  public void RateInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>Lets you set the delay before the next picture is displayed.</html>");
    }
    else
    {
      info("<html>The delay before the next picture is drawn. Note that 60 is one second, so 90 is 1 second and a half.</html>");
    }
  }

  public void SeqInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>Lest you pick the order the pictures are read, and displayed from the \"LIST (fram)\".</html>");
    }
    else
    {
      info("<html>The pictures in \"LIST (fram)\" go from 0 to last picture. This number is which picture to draw from \"LIST (fram)\".</html>");
    }
  }
}
