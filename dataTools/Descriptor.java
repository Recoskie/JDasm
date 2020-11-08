package dataTools;

import RandomAccessFileV.*;

public class Descriptor
{
  public String[][] data = new String[50][3];
  public int[] type = new int[50];
  public int[] rpos = new int[50];
  public int[] apos = new int[50];

  public int length = 0; //Total length of data.
  private long pos = 0; //Position of data.
  
  public int rows = 0; //Number of rows added, or types.

  public Object value; //Used to restive a value that is added.

  private boolean Virtual = false; //Is data virtual space.
  private RandomAccessFileV IOStream;

  //Method that is called when user clicks a element.

  public java.util.function.IntConsumer Event = this::stud;

  //Descriptor data model constructor.

  public Descriptor( RandomAccessFileV b ) { this( b, false ); }

  public Descriptor( RandomAccessFileV b, boolean V )
  {
    Virtual = V;
    
    try { pos = V ? b.getVirtualPointer() : b.getFilePointer(); } catch(java.io.IOException e) { }
    
    IOStream = b;
  }

  //Defined data types.

  public void UINT8( String use ) throws java.io.IOException
  {
    IOStream.read(1); value = IOStream.toByte();

    data[rows][0] = use; data[rows][1] = IOStream.toHex(); data[rows][2] = ( ( ((Byte)value).shortValue() ) & 0xFF ) + "";
    
    length += 1; type[rows] = 2; rpos[rows + 1] = length; rows += 1;
  }

  public void UINT16( String use ) throws java.io.IOException
  {
    IOStream.read(2); value = IOStream.toShort();

    data[rows][0] = use; data[rows][1] = IOStream.toHex(); data[rows][2] = ( ( ((Short)value).intValue() ) & 0xFFFF ) + "";
    
    length += 2; type[rows] = 4; rpos[rows + 1] = length; rows += 1;
  }

  public void UINT32( String use ) throws java.io.IOException
  {
    IOStream.read(4); value = IOStream.toInt();

    data[rows][0] = use; data[rows][1] = IOStream.toHex(); data[rows][2] = ( ( ((Integer)value).longValue() ) & 0xFFFFFFFF ) + "";
    
    length += 4; type[rows] = 6; rpos[rows + 1] = length; rows += 1;
  }

  public void LUINT16( String use ) throws java.io.IOException
  {
    IOStream.read(2); value = IOStream.toLShort();

    data[rows][0] = use; data[rows][1] = IOStream.toHex(); data[rows][2] = ( ( ((Short)value).intValue() ) & 0xFFFF ) + "";
    
    length += 2; type[rows] = 4; rpos[rows + 1] = length; rows += 1;
  }

  public void LUINT32( String use ) throws java.io.IOException
  {
    IOStream.read(4); value = IOStream.toLInt();

    data[rows][0] = use; data[rows][1] = IOStream.toHex(); data[rows][2] = ( ( ((Integer)value).longValue() ) & 0xFFFFFFFF ) + "";
    
    length += 4; type[rows] = 6; rpos[rows + 1] = length; rows += 1;
  }

  public void LUINT64( String use ) throws java.io.IOException
  {
    IOStream.read(8); value = IOStream.toLLong();

    data[rows][0] = use; data[rows][1] = IOStream.toHex(); data[rows][2] = Long.toUnsignedString( ((Long)value).longValue() );
    
    length += 4; type[rows] = 6; rpos[rows + 1] = length; rows += 1;
  }

  //String data.

  public void String8( String use, int len ) throws java.io.IOException
  {
    IOStream.read( len ); value = IOStream.toText8();

    data[rows][0] = use; data[rows][1] = IOStream.toHex(); data[rows][2] = value + "";
    
    length += len; rpos[rows + 1] = length;
    
    type[rows] = 13; rows += 1;
  }

  public void String16( String use, int len ) throws java.io.IOException
  {
    IOStream.read( len ); value = IOStream.toText16();

    data[rows][0] = use; data[rows][1] = IOStream.toHex(); data[rows][2] = value + "";
    
    length += len; rpos[rows + 1] = length;
    
    type[rows] = 14; rows += 1;
  }

  public void LString16( String use, int len ) throws java.io.IOException
  {
    IOStream.read( len ); value = IOStream.toLText16();

    data[rows][0] = use; data[rows][1] = IOStream.toHex(); data[rows][2] = value + "";
    
    length += len; rpos[rows + 1] = length;
    
    type[rows] = 14; rows += 1;
  }

  //Array type.

  public void Array( String use, int len ) throws java.io.IOException
  {
    data[rows][0] = use; data[rows][1] = "No Data"; data[rows][2] = "No value";
    
    rpos[rows + 1] = length; apos[rows + 1] = length + len;
    
    type[rows] = 16; rows += 1;
  }

  //Other type.

  public void Other( String use, int len ) throws java.io.IOException
  {
    IOStream.read( len ); value = IOStream.toText8();

    data[rows][0] = use; data[rows][1] = IOStream.toHex(); data[rows][2] = "No value";
    
    length += len; rpos[rows + 1] = length;
    
    type[rows] = 15; rows += 1;
  }

  //goto data by row.

  public void loc( int row ) { try { if( Virtual ) { IOStream.seekV( pos + rpos[row] ); } else { IOStream.seek( pos + rpos[row] ); } } catch( java.io.IOException e ) { } }

  //Sets the method that is called when user clicks a data type.

  public void setEvent( java.util.function.IntConsumer e ) { Event = e; }

  //stud Event that does nothing.

  public void stud(int el){}

  //The total length of the data.

  public int length()
  {
    return( data.length );
  }
}