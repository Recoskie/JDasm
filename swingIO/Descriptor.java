package swingIO;

import RandomAccessFileV.*;

import java.util.*;

public class Descriptor
{
  //data.

  public LinkedList<String[]> data = new LinkedList<String[]>();
  public LinkedList<Integer> type = new LinkedList<Integer>();
  public LinkedList<Integer> rpos = new LinkedList<Integer>();
  public LinkedList<Integer> apos = new LinkedList<Integer>();

  public int length = 0; //Total length of data.
  public long pos = 0; //Position of data.
  
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
    if( b != null )
    {
      Virtual = V;
    
      try { pos = V ? b.getVirtualPointer() : b.getFilePointer(); } catch(java.io.IOException e) { }

      rpos.add( 0 ); apos.add( 0 );
    
      IOStream = b;
    }
  }

  //Defined data types.

  public void UINT8( String use ) throws java.io.IOException
  {
    IOStream.read(1); value = IOStream.toByte();

    data.add(new String[]{ use, IOStream.toHex(), ( ( ((Byte)value).shortValue() ) & 0xFF ) + "" } );
    
    length += 1; type.add( 2 ); rpos.add( length ); apos.add( 0 ); rows += 1;
  }

  public void UINT16( String use ) throws java.io.IOException
  {
    IOStream.read(2); value = IOStream.toShort();

    data.add(new String[]{ use, IOStream.toHex(), ( ( ((Short)value).intValue() ) & 0xFFFF ) + "" } );
    
    length += 2; type.add( 4 ); rpos.add( length ); apos.add( 0 ); rows += 1;
  }

  public void UINT32( String use ) throws java.io.IOException
  {
    IOStream.read(4); value = IOStream.toInt();

    data.add(new String[]{ use, IOStream.toHex(), ( ( ((Integer)value).longValue() ) & 0xFFFFFFFFL ) + "" } );
    
    length += 4; type.add( 6 ); rpos.add( length ); apos.add( 0 ); rows += 1;
  }

  public void LUINT16( String use ) throws java.io.IOException
  {
    IOStream.read(2); value = IOStream.toLShort();

    data.add(new String[]{ use, IOStream.toHex(), ( ( ((Short)value).intValue() ) & 0xFFFF ) + "" } );
    
    length += 2; type.add( 4 ); rpos.add( length ); apos.add( 0 ); rows += 1;
  }

  public void LUINT32( String use ) throws java.io.IOException
  {
    IOStream.read(4); value = IOStream.toLInt();

    data.add(new String[]{ use, IOStream.toHex(), ( ( ((Integer)value).longValue() ) & 0xFFFFFFFFL ) + "" } );
    
    length += 4; type.add( 6 ); rpos.add( length ); apos.add( 0 ); rows += 1;
  }

  public void LUINT64( String use ) throws java.io.IOException
  {
    IOStream.read(8); value = IOStream.toLLong();

    data.add(new String[]{ use, IOStream.toHex(), Long.toUnsignedString( ((Long)value).longValue() ) } );
    
    length += 8; type.add( 8 ); rpos.add( length ); apos.add( 0 ); rows += 1;
  }

  public void INT8( String use ) throws java.io.IOException
  {
    IOStream.read(1); value = IOStream.toByte();

    data.add(new String[]{ use, IOStream.toHex(), ((Byte)value) + "" } );
    
    length += 1; type.add( 1 ); rpos.add( length ); apos.add( 0 ); rows += 1;
  }

  public void INT16( String use ) throws java.io.IOException
  {
    IOStream.read(2); value = IOStream.toShort();

    data.add(new String[]{ use, IOStream.toHex(), ((Short)value) + "" } );
    
    length += 2; type.add( 3 ); rpos.add( length ); apos.add( 0 ); rows += 1;
  }

  public void INT32( String use ) throws java.io.IOException
  {
    IOStream.read(4); value = IOStream.toInt();

    data.add(new String[]{ use, IOStream.toHex(), ((Integer)value) + "" } );
    
    length += 4; type.add( 5 ); rpos.add( length ); apos.add( 0 ); rows += 1;
  }

  public void LINT16( String use ) throws java.io.IOException
  {
    IOStream.read(2); value = IOStream.toLShort();

    data.add(new String[]{ use, IOStream.toHex(), ((Short)value) + "" } );
    
    length += 2; type.add( 3 ); rpos.add( length ); apos.add( 0 ); rows += 1;
  }

  public void LINT32( String use ) throws java.io.IOException
  {
    IOStream.read(4); value = IOStream.toLInt();

    data.add(new String[]{ use, IOStream.toHex(), ((Integer)value) + "" } );
    
    length += 4; type.add( 5 ); rpos.add( length ); apos.add( 0 ); rows += 1;
  }

  public void LINT64( String use ) throws java.io.IOException
  {
    IOStream.read(8); value = IOStream.toLLong();

    data.add(new String[]{ use, IOStream.toHex(), ((Long)value) + "" } );
    
    length += 8; type.add( 7 ); rpos.add( length ); apos.add( 0 ); rows += 1;
  }

  //String data.

  public void String8( String use, int len ) throws java.io.IOException
  {
    IOStream.read( len ); value = IOStream.toText8();

    data.add(new String[]{ use, IOStream.toHex(), value + "" } );
    
    length += len; rpos.add( length ); apos.add( 0 );
    
    type.add( 13 ); rows += 1;
  }

  public void String16( String use, int len ) throws java.io.IOException
  {
    len <<= 1; IOStream.read( len ); value = IOStream.toText16();

    data.add(new String[]{ use, IOStream.toHex(), value + "" } );
    
    length += len; rpos.add( length ); apos.add( 0 );
    
    type.add( 14 ); rows += 1;
  }

  public void LString16( String use, int len ) throws java.io.IOException
  {
    len <<= 1; IOStream.read( len ); value = IOStream.toLText16();

    data.add(new String[]{ use, IOStream.toHex(), value + "" } );
    
    length += len; rpos.add( length ); apos.add( 0 );
    
    type.add( 14 ); rows += 1;
  }

  //Read a string till termination code.

  int code = 0; String s = "", h = "";

  public void String8( String use, byte c ) throws java.io.IOException
  {
    code = ~c; s = ""; h = "";

    while ( code != c ) { code = IOStream.read(); if( code != 0 ) { s += (char)code; h += String.format( "%1$02X", code ) + " "; } }; value = s;

    data.add(new String[]{ use, h, value + "" } );
    
    length += s.length(); rpos.add( length ); apos.add( 0 );
    
    type.add( 13 ); rows += 1;
  }

  //Array type.

  public void Array( String use, int len ) throws java.io.IOException
  {
    data.add(new String[]{ use, "No Data", "No value" } );
    
    rpos.add( length ); apos.add( length + len );
    
    type.add( 16 ); rows += 1;
  }

  //Other type.

  public void Other( String use, int len ) throws java.io.IOException
  {
    IOStream.read( len ); value = IOStream.toText8();

    data.add(new String[]{ use, IOStream.toHex(), "No value" } );
    
    length += len; rpos.add( length ); apos.add( 0 );
    
    type.add( 15 ); rows += 1;
  }

  //goto data by row.

  public void loc( int row ) { try { if( Virtual ) { IOStream.seekV( pos + rpos.get(row) ); } else { IOStream.seek( pos + rpos.get(row) ); } } catch( java.io.IOException e ) { } }

  //Sets the method that is called when user clicks a data type.

  public void setEvent( java.util.function.IntConsumer e ) { Event = e; }

  //stud Event that does nothing.

  public void stud(int el){}

  //The total length of the data.

  public int length()
  {
    return( length );
  }
}