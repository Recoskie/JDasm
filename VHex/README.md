# VHex

Note the latest updates to [RandomAccessFileV](https://github.com/Recoskie/RandomAccessFileV) is required with event handling. In order to watch IO at system level live. Virtual space events are still in development.<br />

VHex uses [RandomAccessFileV](https://github.com/Recoskie/RandomAccessFileV) which is a powerful mapping tool for fragmented data. This component is designed to have both a simulated Virtual memory space mode, and file offset mode. For mapping and modifying binary applications. Or the virtual map can also be used to map fragmented data in disk images when doing data recovery.

# IO Event Handling.

Any read, or write is visually displayed in the VHex editor as it listens to IO events. If you use **YourStream.seek(pos)** then the hex editor will automatically scroll to position, and highlight the byte in hex editor.
<br /><br />
If you are plugging in a search algorithm into this file stream you will want to set **YourStream.Events = false;** until search is done. Then set **YourStream.Events = true;** thus calling **YourStream.seek(pos)** to display search indexes in hex editor.
<br /><br />
If you want to watch what your search algorithm does then you do not have to set events false, but should add at least a 400 millisecond delay between each IO operation.
<br /><br />
I use a search algorithm as a good example. It could be a format decoder, or anything you can think of.

```java
import javax.swing.*;
import java.awt.*;

public class Window
{
  //Main file system stream.

  RandomAccessFileV file;

  public Window()
  { 
    //Instance and setup a basic JFrame.

    JFrame frame = new JFrame( "Hex editor Component." );
    
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

    //Create the file system stream.

    try
    {
      file = new RandomAccessFileV( "Sample.bin", "rw" );
    }
    catch( java.io.IOException e )
    {
      e.printStackTrace( System.out );
    }

    //Command line input.
    
    Cmd c = new Cmd( file ); c.start();
    
    //Instance and setup VHex editor component.
    
    VHex Virtual = new VHex( file, true ), Offset = new VHex( file, false );

    //Enable, or disable text representation.

    Offset.enableText( true );
    Virtual.enableText( true );

    //Set the default flow Layout, for components.

    frame.setLayout( new GridLayout( 1, 2 ) );

    //Add the hex editor components.
    
    frame.add( Virtual ); frame.add( Offset );

    //Pack the frame to minim size of components.
    
    frame.pack();

    //Move window to center of screen.

    frame.setLocationRelativeTo( null );

    //Set the window visible.

    frame.setVisible( true );
  }

  public static void main( String[] args )
  {
    new Window();
  }
}
```

![ExampleCode](https://github.com/Recoskie/VHex/blob/master/ExampleCode.bmp)
