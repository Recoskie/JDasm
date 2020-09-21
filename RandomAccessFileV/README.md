See release <a href="https://github.com/Recoskie/RandomAccessFileV/tree/V1.0">V1.0</a>.<br /><br />
New features are still in beta development.<br /><br />
Event handling on seek, and multiple read/writes is a new feature.<br /><br />
So far It is implemented for regular read/write, but not Virtual mapped space.

# RandomAccessFileV

Simulates a ram address space using very little ram Memory, and time on CPU.

This tool is very useful for mapping areas of an file at random positions and reading the data in order using Virtual read, or write operations.

This tool is also useful for mapping parts of an program into a simulated virtual space that is to be modified, or changed. Thus loading all the patches between. Allowing you to write directly to the patches on disk when making changes, and to the program.

# Detailed Description

Note that **RandomAccessFileV** is the same as **RandomAccessFile** except you have both a **file pointer**, and **virtual address pointer**.

The **virtual pointer** reads within the areas that are mapped in address space using **V** versions of **read/write/seek**.

Using method **addV( long FileOffset, long DataLen, long Address, long AddressLen )**. You select the positions in the file that are at the selected position. For methods **readV/writeV/seekV**.

Anything unmapped is read as **0x00 bytes** using **readV**, and can not be changed using using **writeV** unless it is an mapped byte from the file.

If the the unmapped **read**, or **write** methods are used than **file pointer** moves, and also the **Virtual pointer moves** as it is relative to the **file pointer**. This allows you to mix both mapped and unmapped IO operations.

All **RandomAcesssFile** methods are the original operations with no overrides. So there is no performance penalties.

The virtual operations use the original read/write, but seek to the next address in the map at file position by moving up one in index in the virtual address map. The **V** operations do basically **one additional compare**. Thus as read/write are called the **File pointer updates**, and is added relative to **Virtual pointer**.

**Additionally** as addresses are added. Any address that writes to the start of a address will move the start of the address in file position to number of bytes written over. Matching the position on disk to bytes written over in virtual space. Any address that writes to the end of an address will crop the end of the address -1 to the start of the added address. All addresses with less than 0 bytes will be removed. Further allowing patches and updates to be easily mapped as they are added. While making it easy to read, or write over the patches and sections accordingly to your desired modifications. Because of organization and the address removing system the **readV/writeV/seakV** operations remain blazing fast.

When using **seekV** anything less than the current virtual address will scan down in index, and anything further away in index will scan up one in index.

This is a very high performance IO system for mapping and reading fragmented positions in a straight line, and at selected virtual address locations. This system allows you to modify programs/files that are bigger than the installed ram memory in your device by reading, and writing directly to the file in an simulated address space.

# Methods.

1. Method **addV( long FileOffset, long DataLen, long Address, long AddressLen )**
    > ##### Add an address to Virtual address map.
2. Method **readV()**.
    > ##### read current byte at virtual address position. <br /> Returns byte as int.
3. Method **readV( byte[] b )**.
    > ##### Read len bytes into byte array from current virtual address pointer.
4. Method **readV( byte[] b, int off, int len )**.
    > ##### Read select len bytes into byte array at select offset to write bytes into array b at the current virtual address pointer.
5. Method **writeV( int byte )**.
    > ##### Write a byte at the current virtual address pointer.
6. Method **writeV( byte[] b )**.
    > ##### Write the byte array at the current virtual address pointer.
7. Method **writeV( byte[] b, int off, int len )**.
    > ##### Write selected offset bytes from byte array to len at the current virtual address pointer.
8. Method **seekV( long Address )**.
    > ##### Seek to a specific virtual address. <br /> Updates "file pointer", and "virtual pointer" relatively.
9. Method **getVirtualPointer()**.
    > ##### Get the current virtual address pointer position. <br /> Note "return( super.getFilePointer() + VAddress );".
10. Method **resetV()**.
    > ##### Resets the virtual address map.

Thus all methods from Random access file are extended. See [Random access file documentation](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/RandomAccessFile.html) for the rest of the supported methods.

# Sample code.

Assuming that we have the following bytes in a file named "File.bin".

```java
import java.io.*;

public class Sample
{
  public Sample()
  {
    try
    {
      //Create "File.bin", for reading, or writing.

      RandomAccessFileV V = new RandomAccessFileV( new java.io.File("File.bin"), "rw" );

      //Write some bytes.

      V.write( new byte[]{ (byte)0x11, (byte)0x22, (byte)0x33, (byte)0x44, (byte)0x55, (byte)0x66,
        (byte)0x77, (byte)0x88, (byte)0x99, (byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0xEE, (byte)0xFF } );

      //Map byte 12 as the first byte. At Virtuall address 0.
      //Byte 12, 1 byte in length across, At address 0, Thus address length 1.

      V.addV( 12, 1, 0, 1 );

      //Byte 0, 11 byte in length across, At address 1, Thus address length 11.

      V.addV( 0, 11, 1, 11 );

      //Insert byte 13 at address 9.
      //Byte 13, 1 byte in length across, At address 9, Thus address length 1.

      V.addV( 13, 1, 9, 1 );

      //Read bytes 0 to 12.

      byte[] b = new byte[12];

      V.readV( b );

      //Print byte values.

      for( int i = 0; i < b.length; System.out.print( String.format( "%1$02X", b[ i++ ] ) + "," ) );

      System.out.println("");
    }
    catch(Exception e)
    {
      //Print error.

      e.printStackTrace( System.out );
    }
  }

  public static void main( String[] args )
  {
    new Sample();
  }
}
```

In this example we read bytes in mixed order using the readV method. You can mix mapped and unmapped IO.
This tool can be used for any fragmented format task you wish, or with tables that create an file (compressed formats).
Modify this sample however you like to get a feel for how the IO system works.
# IO Event handling.

```java
import java.io.*;

public class MYCommponet extends JComponent implements IOEventListener
{
  //Reference to IO stream.
  
  private RandomAccessFileV IOStream;
  
  //The file system stream.

  public MYCommponet( RandomAccessFileV IO )
  {
    //Reference to File system stream for use in this component.
    
    IOStream = IO;
    
    //Add event listener to this component when edits, or changes are made in stream by other components.
    
    IO.addIOEventListener( this );
  }
  
  //On seeking a new position in stream.
  //Show current position in stream, or Are we in range of an bit field this component edits.
  
  public void onSeek( IOEvent e )
  {
    System.out.println("Seek!");
  }
  
  //On reading a new position in stream.
  //Show current position in stream, or Are we in range of an bit field this component edits.
  
  public void onRead( IOEvent e )
  {
    System.out.println("Read!");
  }
  
  //On writing a new position in stream.
  //Have we updated a bit field this component edits.
  
  public void onWrite( IOEvent e )
  {
    System.out.println("Write!");
  }

}
```

The IO events are designed to be triggered for read, and writes that happen outside of your editing components.<br />
This allows you to design components that update if an random read or write is generated by a outside code.<br />

# Disabling Events.

If your component does a search in the IO stream you will want to disable events till done.<br />

## First step set.<br/>
**IOStream.Events = false;**<br/>
Then after the search is done.<br />
**IOStream.Events = true;**<br />
Then call **IOStream.seek(pos);** to update the position and to trigger the seek event in all editors that are open with this file system.<br />
<br />
This is one example in which you would want to disable Events while doing IO. Then enable events when done.
<br /><br />
However If you want to watch what your search algorithm does then you do not have to set events false, but should add at least a 400 millisecond delay between each IO operation.
<br /><br />
I use a search algorithm as a good example. It could be a format decoder, or anything you can think of.
