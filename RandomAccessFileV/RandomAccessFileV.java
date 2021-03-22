package RandomAccessFileV;
import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import javax.swing.event.*;

public class RandomAccessFileV extends RandomAccessFile implements Runnable
{
  //My event listener list for graphical components that listen for stream update events.
  
  protected EventListenerList list = new EventListenerList();
  
  //Disable events. This is to stop graphics components from updating while doing intensive operations.
  
  public static boolean Events = false;
  
  //Trigger Position.
  
  private long TPos = 0;
  private long TPosV = 0;
  
  //Updated pos.
  
  private long pos = 0;
  private long posV = 0;
  
  //Running thread.
  
  private boolean Running = false;
  
  //Event trigger.
  
  private boolean Trigger = false;

  //Weather last command was virtual, or offset.

  private boolean TriggerV = false;
  
  //Read or write event.
  
  private boolean Read = false;
  
  //Main event thread.
  
  private Thread EventThread;

  //Test if read only.

  public static boolean readOnly = false;

  //Add and remove event listeners.

  public void addIOEventListener( IOEventListener listener )
  {
    //Event thread is created for sequential read, or write length.

    if( !EventThread.isAlive() ) { EventThread.start(); }
    
    list.add( IOEventListener.class, listener ); Events = true;
  }
  
  public void removeIOEventListener( IOEventListener listener )
  {
    list.remove( IOEventListener.class, listener );
    
    //If all event listeners are removed. Disable event thread.
    
    Running = ( list.getListenerList().length > 0 );
  }
  
  //Fire the event to all my graphics components, for editing the stream, or decoding data types.
  
  void fireIOEventSeek ( IOEvent evt )
  {
    Object[] listeners = list.getListenerList();
    
    if ( Events )
    {
      for ( int i = 0; i < listeners.length; i = i + 2 )
      {
        if ( listeners[i] == IOEventListener.class )
        {
          ((IOEventListener)listeners[i+1]).onSeek( evt );
        }
      }
    }
  }

  //This is a delayed event to find the length of the data, for sequential read or write.
  
  void fireIOEvent ( IOEvent evt )
  {
    Object[] listeners = list.getListenerList();
    
    if ( Events )
    {
      for ( int i = 0; i < listeners.length; i = i + 2 )
      {
        if ( listeners[i] == IOEventListener.class )
        {
          if( Read )
          {
            ((IOEventListener)listeners[i+1]).onRead( evt );
          }
          
          else
          {
            ((IOEventListener)listeners[i+1]).onWrite( evt );
          }
        }
      }
    }
  }
  
  //64 bit address pointer. Used by things in virtual ram address space such as program instructions, and data.
  //Note that Virtual address must be compared as unsigned.
  
  private long VAddress = 0x0000000000000000L;

  //Keep a reference of the read data.

  private static byte[] d;
  private static String o = "";
  
  //Positions of an file can be mapped into ram address space locations.
  //The file pointer can not address offsets as unsigned. So comparing file offsets as unsigned is not necessary.

  private class VRA
  {
    //General address map properties.
    //Note that Pos, Len, and FEnd do not have to be compared as unsigned, however VPos, VLen, and VEnd need to be treated as unsigned.
    
    private long Pos = 0x0000000000000000L, Len = 0x0000000000000000L, VPos = 0x0000000000000000L, VLen = 0x0000000000000000L;
    
    //File offset end position.
    
    private long FEnd = 0x0000000000000000L;
    
    //Virtual address end position. If grater than actual data the rest is 0 filled space.
    
    private long VEnd = 0x0000000000000000L;

    //If this address has mapped space.

    private boolean Maped = false;
    
    //Construct area map. Both long/int size. Note End position can match the start position as the same byte. End position is minus 1.
    
    public VRA( long Offset, long DataLen, long Address, long AddressLen )
    {
      Pos = Offset; Len = DataLen; VPos = Address; VLen = AddressLen;
      
      //Data offset length can't be higher than virtual offset length.
      
      if( Long.compareUnsigned( Len, VLen ) > 0 ){ Len = VLen; }
      
      //Calculate file offset end positions and virtual end positions.
      
      FEnd = Pos + (Len > 0 ? ( Len - 1 ) : 0); VEnd = VPos + ( VLen - 1 );

      //Set mapped.

      Maped = Len != 0;
    }
    
    //Set the end of an address when another address writes into this address.
    
    public void setEnd( long Address )
    {
      //Set end of the current address to the start of added address.
      
      VEnd = Address;
      
      //Calculate address length.
      
      VLen = ( VEnd + 1 ) - VPos;
      
      //If there still is data after the added address.
      
      Len = Long.compareUnsigned( Len, VLen ) < 0 ? Len : VLen; 
      
      //Calculate the bytes written into.
      
      FEnd = Pos + (Len > 0 ? ( Len - 1 ) : 0);

      //Set mapped.

      Maped = Len != 0;
    }
    
    //Addresses that write over the start of an address.
    
    public void setStart( long Address )
    {
      //Add Data offset to bytes written over at start of address.

      Pos += Address - VPos;
        
      //Move Virtual address start to end of address.
        
      VPos = Address;
        
      //Recalculate length between the new end position.
        
      Len = Pos > FEnd ? 0 : ( FEnd + 1 ) - Pos;
      
      if( Len == 0 ) { Pos = 0; }

      VLen = Long.compareUnsigned( VPos, VEnd ) > 0 ? 0 : ( VEnd + 1 ) - VPos;

      //Set mapped.

      Maped = Len != 0;
    }
    
    //String Representation for address space.
    
    public String toString()
    {
      return( "-----------------------------------------------------------------------------------------------------------------------\r\n" +
              "File(Offset)=" + String.format( "%1$016X", Pos ) + "---FileEnd(Offset)=" + String.format( "%1$016X", FEnd ) + "\r\n" + 
              "Start(Address)=" + String.format( "%1$016X", VPos ) + "---End(Address)=" + String.format( "%1$016X", VEnd ) + "\r\n" +
              "VLength=" + String.format( "%1$016X", VLen ) + "---FLength=" + String.format( "%1$016X", Len ) + "\r\n" + 
              "-----------------------------------------------------------------------------------------------------------------------" );
    }
  }
  
  //The mapped addresses.
  //Length zero is not address 0. Length 0 implies no data. Thus length is one byte less than the start address 0, and end position address.
  //So to fix this. The address space is then split in half with two addresses 0x8000000000000000 in length.
  
  private java.util.ArrayList<VRA> Map = new java.util.ArrayList<VRA>(Arrays.asList(
    new VRA( 0, 0, 0, 0x8000000000000000L ),
    new VRA( 0, 0, 0x8000000000000000L, 0x8000000000000000L )
  ));
  
  //The virtual address that the current virtual address pointer is in range of.
  
  private VRA curVra = Map.get(0);
  
  //Speeds up search. By going up or down from current virtual address.
  
  private int Index = 0;
  
  //Map.size() is slower than storing the mapped address space size.
  
  private int MSize = 2;
  
  //Construct the reader using an file, or disk drive.
  
  public RandomAccessFileV( File file, String mode ) throws FileNotFoundException { super( file, mode ); EventThread = new Thread(this); this.readOnly = mode.indexOf("w") < 0; }
  
  public RandomAccessFileV( String name, String mode ) throws FileNotFoundException { super( name, mode ); EventThread = new Thread(this); this.readOnly = mode.indexOf("w") < 0; }
  
  //Temporary data. This is so that components that are dependent on this file system can be used without a target file.
  
  private static File TFile;
  
  private static File mkf() throws IOException { TFile = File.createTempFile("random",".tmp"); TFile.deleteOnExit(); return( TFile ); }
  
  public RandomAccessFileV( byte[] data ) throws IOException
  {
    super( mkf(), "rw" ); EventThread = new Thread(this); this.readOnly = false; super.write( data );
    
    addV( 0, data.length, 0, data.length );
    
    TFile.delete();
  }
  
  public RandomAccessFileV( byte[] data, long Address ) throws IOException
  {
    super( mkf(), "rw" ); EventThread = new Thread(this); this.readOnly = false; super.write( data );
    
    addV( 0, (long)data.length, Address, (long)data.length );
    
    TFile.delete();
  }

  //Check if position is maped.

  public boolean isMaped()
  {
    if( curVra.Maped )
    {
      Events = false; try { seekV(getVirtualPointer()); } catch( IOException e ) { } Events = true;

      return( curVra.Maped );
    }

    return( false );
  }

  //Reset the Virtual ram map.
  
  public void resetV()
  {
    Map.clear();
    
    Map.add( new VRA( 0, 0, 0, 0x8000000000000000L ) ); Map.add( new VRA( 0, 0, 0x8000000000000000L, 0x8000000000000000L ) );
    
    MSize = 2; Index = 0; VAddress = 0;
    
    curVra = Map.get(0);
  }
  
  //Get the virtual address pointer. Relative to the File offset pointer.
  
  public long getVirtualPointer() throws IOException { return( super.getFilePointer() + VAddress ); }

  //Add an virtual address.
  
  public void addV( long Offset, long DataLen, long Address, long AddressLen ) 
  {
    VRA Add = new VRA( Offset, DataLen, Address, AddressLen );
    VRA Cmp = null;
    
    //The numerical range the address lines up to in index in the address map.
    
    int e = 0;
    
    //fixes lap over ERROR. When splitting an address.
    
    boolean sw = true;

    //The address is split into two between the end, and start to follow regular sequential write.

    if( Long.compareUnsigned( Add.VPos + ( Add.VLen - 1 ), Add.VPos ) < 0 )
    {
      //Data before the end of address space.

      long len = ( 0xFFFFFFFFFFFFFFFFL - Add.VPos ) + 1;

      //Data in first address.

      long len1 = Add.Len <= len ? Add.Len : len;

      //Remaining Data in address 0 plus.

      long len2 = Add.Len - len; len2 = len2 < 0 ? 0 : len2;

      //Adjust address space.

      addV( Add.Pos, len1, Add.VPos, len ); Add = new VRA( Add.Pos + len1, len2, 0, Add.VLen - len );
    }

    //Write in alignment.
    
    for( int i = 0; i < MSize; i++ )
    {
      Cmp = Map.get( i );

      //Remove any address that is overwritten.

      if( Long.compareUnsigned( Add.VPos, Cmp.VPos ) <= 0 && Long.compareUnsigned( Add.VEnd, Cmp.VEnd ) >= 0 )
      {
        Map.remove( i ); i -= 1; MSize -= 1;
      }
      
      //If the added address writes to the end, or in the Middle of an address.
      
      else if( Long.compareUnsigned( Add.VPos, Cmp.VEnd ) <= 0 && Long.compareUnsigned( Add.VPos, Cmp.VPos ) > 0 && sw )
      {
        //Address range position.
        
        e = i + 1;
        
        //If the added address does not write to the end of the address then add it to the next element.
        //Set sw. So the end start position can be adjusted on split address.
        
        if( Long.compareUnsigned( Cmp.VEnd, Add.VEnd ) > 0 )
        {
          sw = false; Map.add( e, new VRA( Cmp.Pos, Cmp.Len, Cmp.VPos, Cmp.VLen ) ); MSize++;
        }
        
        //Set end of the current address to the start of added address.
        
        Cmp.setEnd( Add.VPos - 1 );
      }
      
      //If added Address writes to the start of Address.
      
      else if( Long.compareUnsigned( Add.VPos, Cmp.VPos ) <= 0 && Long.compareUnsigned( Add.VEnd, Cmp.VPos ) >= 0 || !sw )
      {
        //Address range position.
        
        e = i;
        
        //Add Data offset to bytes written over at start of address.
        
        Cmp.setStart( Add.VEnd + 1 ); sw = true;
      }
    }
    
    //Add address in order to it's position in range.
    
    Map.add( e, Add ); MSize++;
  }
  
  //Adjust the Virtual offset pointer relative to the mapped virtual ram address and file pointer.
  
  public void seekV( long Address ) throws IOException
  {
    syncV();

    long r = 0;

    //If address is in range of current address index.
    
    if( Long.compareUnsigned( Address, curVra.VPos ) >= 0 && Long.compareUnsigned( Address, curVra.VEnd ) <= 0 )
    {
      r = Address - curVra.VPos; if( Long.compareUnsigned( r, curVra.Len ) >= 0 ) { r = curVra.Len; }
      
      if( curVra.Len > 0 ) { super.seek( r + curVra.Pos ); }
      
      VAddress = Address - super.getFilePointer();
    }
    
    //If address is grater than the next vra iterate up in indexes.
    
    else if( Long.compareUnsigned( Address, curVra.VEnd ) >= 0 || Index == -1 )
    {
      VRA e = null;
      
      for( int n = Index + 1; n < MSize; n++ )
      {
        e = Map.get( n );
        
        if( Long.compareUnsigned( Address, e.VPos ) >= 0 && Long.compareUnsigned( Address, e.VEnd ) <= 0 )
        {
          Index = n; curVra = e;

          r = Address - e.VPos; if( Long.compareUnsigned( r, e.Len ) >= 0 ) { r = e.Len; }
          
          if( curVra.Len > 0 ) { super.seek( r + e.Pos ); }
          
          VAddress = Address - super.getFilePointer();

          fireIOEventSeek( new IOEvent( this, super.getFilePointer(), 0, getVirtualPointer(), 0, TriggerV ) );
          
          return;
        }
      }
    }
    
    //else iterate down in indexes.
    
    else if( Long.compareUnsigned( Address, curVra.VPos ) <= 0 )
    {
      VRA e = null;
      
      for( int n = Index - 1; n > -1; n-- )
      {
        e = Map.get( n );
        
        if( Long.compareUnsigned( Address, e.VPos ) >= 0 && Long.compareUnsigned( Address, e.VEnd ) <= 0 )
        {
          Index = n; curVra = e;

          r = Address - e.VPos; if( Long.compareUnsigned( r, e.Len ) >= 0 ) { r = e.Len; }
          
          if( curVra.Len > 0 ) { super.seek( r + e.Pos ); }
          
          VAddress = Address - super.getFilePointer();

          fireIOEventSeek( new IOEvent( this, super.getFilePointer(), 0, getVirtualPointer(), 0, TriggerV ) );
          
          return;
        }
      }
    }
    
    fireIOEventSeek( new IOEvent( this, super.getFilePointer(), 0, getVirtualPointer(), 0, TriggerV ) );
  }
  
  public int readV() throws IOException
  {
    syncRV(); 

    //Seek address if outside current address space.
    
    if( Long.compareUnsigned( getVirtualPointer(), curVra.VEnd ) > 0 ) { seekV( getVirtualPointer() ); }

    //Undefined byte.

    if( curVra.Len <= 0 ) { VAddress += 1; return( -1 ); }
    
    //Read in current offset. If any data to be read.
    
    else if( super.getFilePointer() >= curVra.Pos && super.getFilePointer() <= curVra.FEnd ) { return( super.read() ); }
    
    //No data then 0 space.
    
    VAddress += 1; return( 0 );
  }
  
  //Read len bytes from current virtual offset pointer. Stop at undefined bytes.
  
  public int readV( byte[] b ) throws IOException
  {
    syncRV(); int Pos = 0, n = 0;

    //Seek address if outside current address space.
    
    if( Long.compareUnsigned( getVirtualPointer(), curVra.VEnd ) > 0 ) { seekV( getVirtualPointer() ); }

    if( curVra.Len <= 0 ){ return( -1 ); }
    
    //Start reading.
    
    while( Pos < b.length )
    {
      //Read in current file offset.
      
      if( super.getFilePointer() >= curVra.Pos && super.getFilePointer() <= curVra.FEnd && curVra.Len > 0 )
      {
        //Number of bytes that can be read from current area.
        
        n = (int)Math.min( ( curVra.FEnd + 1 ) - super.getFilePointer(), b.length );
        
        super.read( b, Pos, n ); Pos += n;
      }
      
      //Else stop at undefined space.
      
      else
      {
        //Zero space before the end of address.

        n = (int)Math.min( ( curVra.VEnd - getVirtualPointer() ) + 1, b.length - Pos ); VAddress += n;

        for( int i = Pos + n; Pos < i; b[Pos++] = (byte)0x00 );
        
        //If next virtual address contains data. Continue reading.

        seekV( getVirtualPointer() ); if( curVra.Len <= 0 ) { return( Pos ); }
      }
    }
    
    return( Pos );
  }
  
  //Read len bytes at offset to len from current virtual offset pointer.
  
  public int readV( byte[] b, int off, int len ) throws IOException
  {
    syncRV(); int Pos = off, n = 0; len += off;

    //Seek address if outside current address space.
    
    if( Long.compareUnsigned( getVirtualPointer(), curVra.VEnd ) >= 0 ) { seekV( getVirtualPointer() ); }

    if( curVra.Len <= 0 ){ return( -1 ); }
    
    //Start reading.
    
    while( Pos < len )
    {
      //Read in current offset.
      
      if( super.getFilePointer() >= curVra.Pos && super.getFilePointer() <= curVra.FEnd && curVra.Len > 0 )
      {
        //Number of bytes that can be read from current area.
        
        n = (int)Math.min( ( curVra.FEnd + 1 ) - super.getFilePointer(), len );
        
        super.read( b, Pos, n ); Pos += n;
      }

      //Else stop at undefined space.
      
      else
      {
        //Zero space before the end of address.

        n = (int)Math.min( ( curVra.VEnd - getVirtualPointer() ) + 1, b.length - Pos );
        
        VAddress += n;

        for( int i = Pos + n; Pos < i; b[Pos++] = (byte)0x00 );
        
        //If next virtual address contains data. Continue reading.

        seekV( getVirtualPointer() ); if( curVra.Len <= 0 ){ return( Pos - off ); }
      }
    }
    
    return( Pos - off );
  }

  //New read method stores read bytes. Allowing the read data to be changed to different types.
  
  public int readV( int len ) throws IOException { d = new byte[len]; return( readV( d ) ); } 

  //Next Virtual address with data.

  public long endV()
  {
    try
    {
      return( ( Map.get( Index ).VEnd - getVirtualPointer() ) + 1 );
    }
    catch( Exception e ) { }

    return( 0 );
  }
  
  //Write an byte at Virtual address pointer if mapped.
  
  public void writeV( int b ) throws IOException
  {
    syncWV();

    //Seek address if outside current address space.
    
    if( Long.compareUnsigned( getVirtualPointer(), curVra.VEnd ) > 0 ) { seekV( getVirtualPointer() ); }
    
    //Write the byte if in range of address.
    
    if( super.getFilePointer() >= curVra.Pos && super.getFilePointer() <= curVra.FEnd ) { super.write( b ); return; }
    
    //Move virtual pointer.
    
    VAddress++;
  }
  
  //Write set of byte at Virtual address pointer to only mapped bytes.
  
  public void writeV( byte[] b ) throws IOException
  {
    syncWV(); int Pos = 0, n = 0;
    
    //Seek address if outside current address space.
    
    if( Long.compareUnsigned( getVirtualPointer(), curVra.VEnd ) > 0 ) { seekV( getVirtualPointer() ); }
    
    //Start Writing.
    
    while( Pos < b.length )
    {
      //Write in current offset.
      
      if( super.getFilePointer() >= curVra.Pos && super.getFilePointer() <= curVra.FEnd && curVra.Len > 0 )
      {
        //Number of bytes that can be written in current area.
        
        n = (int)Math.min( ( curVra.FEnd + 1 ) - super.getFilePointer(), b.length );
        
        super.write( b, Pos, n ); Pos += n;
      }
      
      //Else 0 space. Skip n to Next address.
      
      else
      {
        n = (int)( curVra.VLen - ( super.getFilePointer() - curVra.Pos ) );
        
        if( n < 0 || curVra.Len <= 0 ) { n = b.length - Pos; }
        
        VAddress += n; Pos += n;
        
        seekV( getVirtualPointer() );
      }
    }
  }
  
  //Write len bytes at offset to len from current virtual offset pointer.
  
  public void writeV( byte[] b, int off, int len ) throws IOException
  {
    syncWV(); int Pos = off, n = 0; len += off;
    
    //Seek address if outside current address space.
    
    if( Long.compareUnsigned( getVirtualPointer(), curVra.VEnd ) > 0 ) { seekV( getVirtualPointer() ); }
    
    //Start writing.
    
    while( Pos < len )
    {
      //Write in current offset.
      
      if( super.getFilePointer() >= curVra.Pos && super.getFilePointer() <= curVra.FEnd && curVra.Len > 0 )
      {
        //Number of bytes that can be written in current area.
        
        n = (int)Math.min( ( curVra.FEnd + 1 ) - super.getFilePointer(), len );
        
        super.write( b, Pos, n ); Pos += n;
      }
      
      //Else 0 space. Skip n to Next address.
      
      else
      {
        n = (int)( curVra.VLen - ( super.getFilePointer() - curVra.Pos ) );
        
        if( n < 0 || curVra.Len <= 0 ) { n = len - Pos; }
        
        VAddress += n; Pos += n;
        
        seekV( getVirtualPointer() );
      }
    }
  }

  //New write method writes stored bytes.
  
  public void writeV( int len, int off ) throws IOException { syncWV(); writeV( d, off, len );  } 
  
  //fire seek event.
  
  @Override public void seek( long Offset ) throws IOException
  {
    sync(); super.seek( Offset ); fireIOEventSeek( new IOEvent( this, Offset, 0, getVirtualPointer(), 0, TriggerV ) );
  }
  
  //Seek. Same as seek, but is a little faster of a read ahead trick.
  
  @Override public int skipBytes( int n ) throws IOException
  {
    sync(); int b = super.skipBytes( n );
    
    fireIOEventSeek( new IOEvent( this, super.getFilePointer(), 0,  getVirtualPointer(), 0, TriggerV ) );
    
    return( b );
  }
  
  //New read method stores read bytes. Allowing the read data to be changed to different types.
  
  public int read( int len ) throws IOException { syncR(); d = new byte[len]; return( read( d ) ); } 

  //Default read methods.

  @Override public int read() throws IOException { syncR(); return( super.read() ); }
  
  @Override public int read( byte[] b ) throws IOException { syncR(); return( super.read( b ) ); }
  
  @Override public int read( byte[] b, int off, int len ) throws IOException { syncR(); return( super.read( b, off, len ) ); }

  //New write method. Allowing the read data to be written.

  public void write( int off, int len ) throws IOException { syncW(); super.write( d, off, len ); } 

  //Default write methods.
  
  @Override public void write( int b ) throws IOException { syncW(); super.write( b ); }
  
  @Override public void write( byte[] b ) throws IOException { syncW(); super.write( b ); }
  
  @Override public void write( byte[] b, int off, int len ) throws IOException { syncW(); super.write( b, off, len ); }

  //Methods to convert read data to different types.

  public String toHex()
  {
    o = ""; for( int i = 0; i < d.length; i++ )
    {
      o += String.format( "%1$02X", d[i] ) + " ";
    }

    return( o );
  }

  public String toHex(int off, int end)
  {
    o = ""; for( int i = off; i < end; i++ )
    {
      o += String.format( "%1$02X", d[i] ) + " ";
    }

    return( o );
  }

  public String toText8() { return( new String( d, StandardCharsets.UTF_8 ) ); }

  public String toText8( int off, int end ) { return( new String( d, StandardCharsets.UTF_8 ).substring( off, end ) ); }

  public String toText16() { return( new String( d, StandardCharsets.UTF_16BE ) ); }

  public String toText16( int off, int end ) { return( new String( d, StandardCharsets.UTF_16BE ).substring( off, end ) ); }

  public String toLText16() { return( new String( d, StandardCharsets.UTF_16LE ) ); }

  public String toLText16( int off, int end ) { return( new String( d, StandardCharsets.UTF_16LE ).substring( off, end ) ); }

  public boolean toBoolean() { return( d[0] == (byte)0xFF ); }

  public boolean toBoolean( int off ) { return( d[off] == (byte)0xFF ); }

  public byte toByte() { return( d[0] ); }

  public byte toByte( int off ) { return( d[off] ); }

  public short toShort() { return( (short)( ( d[1] & 0xFF ) | ( ( d[0] << 8 ) & 0xFF00 ) ) ); }

  public short toShort( int off ) { return( (short)( ( d[off + 1] & 0xFF ) | ( ( d[off] << 8 ) & 0xFF00 ) ) ); }

  public short toLShort() { return( (short)( ( d[0] & 0xFF ) | ( ( d[1] << 8 ) & 0xFF00 ) ) ); }

  public short toLShort( int off ) { return( (short)( ( d[off] & 0xFF ) | ( ( d[off + 1] << 8 ) & 0xFF00 ) ) ); }

  public int toInt() { return( ( d[3] & 0xFF ) | ( (d[2] << 8) & 0xFF00 ) | ( (d[1] << 16) & 0xFF0000 ) | ( (d[0] << 24) & 0xFF000000 ) ); }

  public int toInt( int off ) { return( ( d[off + 3] & 0xFF ) | ( (d[off + 2] << 8) & 0xFF00 ) | ( (d[off + 1] << 16) & 0xFF0000 ) | ( (d[off] << 24) & 0xFF000000 ) ); }

  public int toLInt() { return( ( d[0] & 0xFF ) | ( (d[1] << 8) & 0xFF00 ) | ( (d[2] << 16) & 0xFF0000 ) | ( (d[3] << 24) & 0xFF000000 ) ); }

  public int toLInt( int off ) { return( ( d[off] & 0xFF ) | ( (d[off + 1] << 8) & 0xFF00 ) | ( (d[off + 2] << 16) & 0xFF0000 ) | ( (d[off + 3] << 24) & 0xFF000000 ) ); }

  public long toLong()
  {
    return( ( (long)d[7] & 0xFFL ) | ( ((long)d[6] << 8) & 0xFF00L ) | ( ((long)d[5] << 16) & 0xFF0000L ) | ( ((long)d[4] << 24) & 0xFF000000L ) |
     ( ( (long)d[3] << 32 ) & 0xFF00000000L ) | ( ( (long)d[2] << 40 ) & 0xFF0000000000L ) | ( ( (long)d[1] << 48 ) & 0xFF000000000000L ) | ( ( (long)d[0] << 56 ) & 0xFF00000000000000L ) );
  }

  public long toLong( int off )
  {
    return( ( (long)d[off + 7] & 0xFFL ) | ( ((long)d[off + 6] << 8) & 0xFF00L ) | ( ((long)d[off + 5] << 16) & 0xFF0000L ) | ( ((long)d[off + 4] << 24) & 0xFF000000L ) |
     ( ( (long)d[off + 3] << 32 ) & 0xFF00000000L ) | ( ( (long)d[off + 2] << 40 ) & 0xFF0000000000L ) | ( ( (long)d[off + 1] << 48 ) & 0xFF000000000000L ) | ( ( (long)d[off] << 56 ) & 0xFF00000000000000L ) );
  }

  public long toLLong()
  {
    return( ( (long)d[0] & 0xFFL ) | ( ((long)d[1] << 8) & 0xFF00L ) | ( ((long)d[2] << 16) & 0xFF0000L ) | ( ((long)d[3] << 24) & 0xFF000000L ) |
     ( ( (long)d[4] << 32 ) & 0xFF00000000L ) | ( ( (long)d[5] << 40 ) & 0xFF0000000000L ) | ( ( (long)d[6] << 48 ) & 0xFF000000000000L ) | ( ( (long)d[7] << 56 ) & 0xFF00000000000000L ) );
  }

  public long toLLong( int off )
  {
    return( ( (long)d[off] & 0xFFL ) | ( ((long)d[off + 1] << 8) & 0xFF00L ) | ( ((long)d[off + 2] << 16) & 0xFF0000L ) | ( ((long)d[off + 3] << 24) & 0xFF000000L ) |
     ( ( (long)d[off + 4] << 32 ) & 0xFF00000000L ) | ( ( (long)d[off + 5] << 40 ) & 0xFF0000000000L ) | ( ( (long)d[off + 6] << 48 ) & 0xFF000000000000L ) | ( ( (long)d[off + 7] << 56 ) & 0xFF00000000000000L ) );
  }

  public float toFloat() { return( Float.intBitsToFloat( toInt() ) ); }

  public float toFloat( int off ) { return( Float.intBitsToFloat( toInt( off ) ) ); }

  public float toLFloat() { return( Float.intBitsToFloat( toLInt() ) ); }

  public float toLFloat( int off ) { return( Float.intBitsToFloat( toLInt( off ) ) ); }

  public double toDouble() { return( Double.longBitsToDouble( toLong() ) ); }

  public double toDouble( int off ) { return( Double.longBitsToDouble( toLong( off ) ) ); }

  public double toLDouble() { return( Double.longBitsToDouble( toLLong() ) ); }

  public double toLDouble( int off ) { return( Double.longBitsToDouble( toLLong( off ) ) ); }

  public char toChar8() { return( (char)d[0] ); }

  public char toChar8( int off ) { return( (char)d[off] ); }

  public char toChar16() { return( (char)( d[1] | ( d[0] << 8 ) ) ); }

  public char toChar16( int off ) { return( (char)( d[off + 1] | ( d[off] << 8 ) ) ); }

  public char toLChar16() { return( (char)( d[0] | ( d[1] << 8 ) ) ); }

  public char toLChar16( int off ) { return( (char)( d[off] | ( d[off + 1] << 8 ) ) ); }

  public byte[] toBytes() { return( d ); }

  //Methods to change read data.

  public void modText8( String s ) {  }

  public void modText8( String s, int off ) {  }

  public void modText16( String s ) {  }

  public void modText16( String s, int off ) {  }

  public void modLText16( String s ) {  }

  public void modLText16( String s, int off ) {  }

  public void modBoolean( boolean b ) { d[0] = b ? (byte)-1 : (byte)0; }

  public void modBoolean( boolean b, int off ) { d[off] = b ? (byte)-1 : (byte)0; }

  public void modByte( byte b ) { d[0] = b; }

  public void modByte( byte b, int off ) { d[off] = b; }

  public void modShort( short s ) { d[0] = (byte)(s&0xFF); d[1] = (byte)((s>>8)&0xFF); }

  public void modShort( short s, int off ) { d[ off ] = (byte)(s&0xFF); d[ off + 1 ] = (byte)((s>>8)&0xFF); }

  public void modLShort( short s ) { d[1] = (byte)(s&0xFF); d[0] = (byte)((s>>8)&0xFF);  }

  public void modLShort( short s, int off ) { d[1] = (byte)(s&0xFF); d[0] = (byte)((s>>8)&0xFF); }

  public void modInt( int i ) { d[0] = (byte)(i&0xFF); d[1] = (byte)((i>>8)&0xFF); d[2] = (byte)((i>>16)&0xFF); d[3] = (byte)((i>>24)&0xFF); }

  public void modInt( int i, int off ) { d[ off ] = (byte)(i&0xFF); d[ off + 1 ] = (byte)((i>>8)&0xFF); d[ off + 2 ] = (byte)((i>>16)&0xFF); d[ off + 3 ] = (byte)((i>>24)&0xFF); }

  public void modLInt( int i ) { d[3] = (byte)(i&0xFF); d[2] = (byte)((i>>8)&0xFF); d[1] = (byte)((i>>16)&0xFF); d[0] = (byte)((i>>24)&0xFF); }

  public void modLInt( int i, int off ) { d[ off + 3 ] = (byte)(i&0xFF); d[ off + 2 ] = (byte)((i>>8)&0xFF); d[ off + 1 ] = (byte)((i>>16)&0xFF); d[ off ] = (byte)((i>>24)&0xFF); }

  public void modLong( long l ) { d[0] = (byte)(l&0xFF); d[1] = (byte)((l>>8)&0xFF); d[2] = (byte)((l>>16)&0xFF); d[3] = (byte)((l>>24)&0xFF); d[4] = (byte)((l>>32)&0xFF); d[5] = (byte)((l>>40)&0xFF); d[6] = (byte)((l>>48)&0xFF); d[7] = (byte)((l>>56)&0xFF); }

  public void modLong( long l, int off ) { d[ off ] = (byte)(l&0xFF); d[ off + 1 ] = (byte)((l>>8)&0xFF); d[ off + 2 ] = (byte)((l>>16)&0xFF); d[ off + 3 ] = (byte)((l>>24)&0xFF); d[ off + 4 ] = (byte)((l>>32)&0xFF); d[ off + 5 ] = (byte)((l>>40)&0xFF); d[ off + 6 ] = (byte)((l>>48)&0xFF); d[ off + 7 ] = (byte)((l>>56)&0xFF); }

  public void modLLong( long l ) { d[7] = (byte)(l&0xFF); d[6] = (byte)((l>>8)&0xFF); d[5] = (byte)((l>>16)&0xFF); d[4] = (byte)((l>>24)&0xFF); d[3] = (byte)((l>>32)&0xFF); d[2] = (byte)((l>>40)&0xFF); d[1] = (byte)((l>>48)&0xFF); d[0] = (byte)((l>>56)&0xFF); }

  public void modLLong( long l, int off ) { d[ off + 7 ] = (byte)(l&0xFF); d[ off + 6 ] = (byte)((l>>8)&0xFF); d[ off + 5 ] = (byte)((l>>16)&0xFF); d[ off + 4 ] = (byte)((l>>24)&0xFF); d[ off + 3 ] = (byte)((l>>32)&0xFF); d[ off + 2 ] = (byte)((l>>40)&0xFF); d[ off + 1 ] = (byte)((l>>48)&0xFF); d[ off ] = (byte)((l>>56)&0xFF); }

  public void modFloat( float ff ) { int f = Float.floatToIntBits(ff); d[0] = (byte)(f&0xFF); d[1] = (byte)((f>>8)&0xFF); d[2] = (byte)((f>>16)&0xFF); d[3] = (byte)((f>>24)&0xFF); }

  public void modFloat( float ff, int off ) { int f = Float.floatToIntBits(ff); d[ off ] = (byte)(f&0xFF); d[ off + 1 ] = (byte)((f>>8)&0xFF); d[ off + 2 ] = (byte)((f>>16)&0xFF); d[ off + 3 ] = (byte)((f>>24)&0xFF); }

  public void modLFloat( float ff ) { int f = Float.floatToIntBits(ff); d[3] = (byte)(f&0xFF); d[2] = (byte)((f>>8)&0xFF); d[1] = (byte)((f>>16)&0xFF); d[0] = (byte)((f>>24)&0xFF); }

  public void modLFloat( float ff, int off ) { int f = Float.floatToIntBits(ff); d[ off + 3 ] = (byte)(f&0xFF); d[ off + 2 ] = (byte)((f>>8)&0xFF); d[ off + 1 ] = (byte)((f>>16)&0xFF); d[ off ] = (byte)((f>>24)&0xFF); }

  public void modDouble( double dd ) { long D = Double.doubleToLongBits(dd); d[0] = (byte)(D&0xFF); d[1] = (byte)((D>>8)&0xFF); d[2] = (byte)((D>>16)&0xFF); d[3] = (byte)((D>>24)&0xFF); d[4] = (byte)((D>>32)&0xFF); d[5] = (byte)((D>>40)&0xFF); d[6] = (byte)((D>>48)&0xFF); d[7] = (byte)((D>>56)&0xFF); }

  public void modDouble( double dd, int off ) { long D = Double.doubleToLongBits(dd); d[ off ] = (byte)(D&0xFF); d[ off + 1 ] = (byte)((D>>8)&0xFF); d[ off + 2 ] = (byte)((D>>16)&0xFF); d[ off + 3 ] = (byte)((D>>24)&0xFF); d[ off + 4 ] = (byte)((D>>32)&0xFF); d[ off + 5 ] = (byte)((D>>40)&0xFF); d[ off + 6 ] = (byte)((D>>48)&0xFF); d[ off + 7 ] = (byte)((D>>56)&0xFF); }

  public void modLDouble( double dd ) { long D = Double.doubleToLongBits(dd); d[7] = (byte)(D&0xFF); d[6] = (byte)((D>>8)&0xFF); d[5] = (byte)((D>>16)&0xFF); d[4] = (byte)((D>>24)&0xFF); d[3] = (byte)((D>>32)&0xFF); d[2] = (byte)((D>>40)&0xFF); d[1] = (byte)((D>>48)&0xFF); d[0] = (byte)((D>>56)&0xFF); }

  public void modLDouble( double dd, int off ) { long D = Double.doubleToLongBits(dd); d[ off + 7 ] = (byte)(D&0xFF); d[ off + 6 ] = (byte)((D>>8)&0xFF); d[ off + 5 ] = (byte)((D>>16)&0xFF); d[ off + 4 ] = (byte)((D>>24)&0xFF); d[ off + 3 ] = (byte)((D>>32)&0xFF); d[ off + 2 ] = (byte)((D>>40)&0xFF); d[ off + 1 ] = (byte)((D>>48)&0xFF); d[ off ] = (byte)((D>>56)&0xFF); }

  public void modChar8( char c ) { d[0]=(byte)c; }

  public void modChar8( char c, int off ) { d[off]=(byte)c; }

  public void modChar16( char c ) { short s = (short)c; d[0] = (byte)(s&0xFF); d[1] = (byte)((s>>8)&0xFF); }

  public void modChar16( char c, int off ) { short s = (short)c; d[ off ] = (byte)(s&0xFF); d[ off + 1 ] = (byte)((s>>8)&0xFF); }

  public void modLChar16( char c ) { short s = (short)c; d[1] = (byte)(s&0xFF); d[0] = (byte)((s>>8)&0xFF); }

  public void modLChar16( char c, int off ) { short s = (short)c; d[ off + 1 ] = (byte)(s&0xFF); d[ off ] = (byte)((s>>8)&0xFF); }
  
  //Debug The address mapped memory.
  
  public void Debug()
  {
    String s = "";
    
    for( int i = 0; i < MSize; s += Map.get( i++ ) + "\r\n" );
    
    System.out.println( s );
  }

  //Event synchronization.

  public final void sync()
  {
    TriggerV = false; while( Events && Trigger ) { EventThread.interrupt(); }
  }

  public final void syncV()
  {
    TriggerV = true; while( Events && Trigger ) { EventThread.interrupt(); }
  }

  public final void syncR() throws IOException
  {
    //Trigger writing event.
    
    TriggerV = false; while( Events && Trigger && !Read ) { EventThread.interrupt(); }
    
    //Start read event tracing.
    
    if( Events && !Trigger ) { TPos = super.getFilePointer(); Read = true; Trigger = true; }
  }

  public final void syncW() throws IOException
  {
    //Trigger read event.
    
    TriggerV = false; while( Events && Trigger && Read ) { EventThread.interrupt(); }
    
    //Start write event tracing.
    
    if( Events && !Trigger ) { TPos = super.getFilePointer(); Read = false; Trigger = true; }
  }

  public final void syncRV() throws IOException
  {
    //Trigger writing event.
    
    TriggerV = true; while( Events && Trigger && !Read ) { EventThread.interrupt(); }
    
    //Start read event tracing.
    
    if( Events && !Trigger ) { TPos = super.getFilePointer(); TPosV = getVirtualPointer(); Read = true; Trigger = true; }
  }

  public final void syncWV() throws IOException
  {
    //Trigger read event.
    
    TriggerV = true; while( Events && Trigger && Read ) { EventThread.interrupt(); }
    
    //Start write event tracing.
    
    if( Events && !Trigger ) { TPos = super.getFilePointer(); TPosV = getVirtualPointer(); Read = false; Trigger = true; }
  }
  
  //Main Event thread.
  
  public void run()
  {
    if( !Running ) //Run once.
    {
      Running = true;
      
      while( Running )
      {
        //If read, or write is triggered.
        
        if( Trigger && Events )
        {
          try
          {
            if( pos == super.getFilePointer() )
            {
              fireIOEvent( new IOEvent( this, TPos, pos - 1, TPosV, posV - 1, TriggerV ) ); Trigger = false;
            }
            else{ pos = super.getFilePointer(); posV = getVirtualPointer(); }
          }
          catch( IOException e ) { e.printStackTrace(); }
        }
        
        //Fire event right away if interrupted, by a different IO event.
        
        try{ Thread.sleep( 70 ); } catch(InterruptedException e) { }
      }
    }
  }
}
