package RandomAccessFileV;
import java.io.*;
import java.util.*;
import javax.swing.event.*;

public class RandomAccessFileV extends RandomAccessFile implements Runnable
{
  //My event listener list for graphical components that listen for stream update events.
  
  protected EventListenerList list = new EventListenerList();
  
  //Disable events. This is to stop graphics components from updating while doing intensive operations.
  
  public boolean Events = false;
  
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
  
  //Read or write event.
  
  private boolean Read = false;
  
  //Main event thread.
  
  private Thread EventThread;

  //Add and remove event listeners.

  public void addIOEventListener( IOEventListener listener )
  {
    //Event thread is created for sequential read, or write length.
    
    if( !Running ) { EventThread = new Thread(this); EventThread.start(); }
    
    list.add( IOEventListener.class, listener ); Events = true;
  }
  
  public void removeIOEventListener( IOEventListener listener )
  {
    list.remove( IOEventListener.class, listener );
    
    //If all event listeners are removed. Disable event thread.
    
    Running = ( list.getListenerList().length > 0 ); Events = false;
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
    
    //Construct area map. Both long/int size. Note End position can match the start position as the same byte. End position is minus 1.
    
    public VRA( long Offset, long DataLen, long Address, long AddressLen )
    {
      Pos = Offset; Len = DataLen; VPos = Address; VLen = AddressLen;
      
      //Data offset length can't be higher than virtual offset length.
      
      if( Long.compareUnsigned( Len, VLen ) > 0 ){ Len = VLen; }
      
      //Calculate file offset end positions and virtual end positions.
      
      FEnd = Pos + (Len > 0 ? ( Len - 1 ) : 0); VEnd = VPos + ( VLen - 1 );
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
  
  public RandomAccessFileV( File file, String mode ) throws FileNotFoundException { super( file, mode ); }
  
  public RandomAccessFileV( String name, String mode ) throws FileNotFoundException { super( name, mode ); }
  
  //Temporary data. This is so that components that are dependent on this file system can be used without a target file.
  
  private static File TFile;
  
  private static File mkf() throws IOException { TFile = File.createTempFile("",".tmp"); TFile.deleteOnExit(); return( TFile ); }
  
  public RandomAccessFileV( byte[] data ) throws IOException
  {
    super( mkf(), "r" ); super.write( data );
    
    addV( 0, data.length, 0, data.length );
    
    TFile.delete();
  }
  
  public RandomAccessFileV( byte[] data, long Address ) throws IOException
  {
    super( mkf(), "r" ); super.write( data );
    
    addV( 0, (long)data.length, Address, (long)data.length );
    
    TFile.delete();
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
    while( Events && Trigger ) { EventThread.interrupt(); }

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
          
          return;
        }
      }
    }
    
    fireIOEventSeek( new IOEvent( this, super.getFilePointer(), 0, getVirtualPointer(), 0, curVra.Len != 0 ) );
  }
  
  public int readV() throws IOException
  {
    //Trigger writing event.
    
    while( Events && Trigger && !Read ) { EventThread.interrupt(); }
    
    //Start read event tracing.
    
    if( Events && !Trigger ) { TPos = super.getFilePointer(); Read = true; Trigger = true; }

    //Begin read operation.

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
    //Trigger writing event.
    
    while( Events && Trigger && !Read ) { EventThread.interrupt(); }
    
    //Start read event tracing.
    
    if( Events && !Trigger ) { TPos = super.getFilePointer(); TPosV = getVirtualPointer(); Read = true; Trigger = true; }

    //Begin read operation.

    int Pos = 0, n = 0;

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
    //Trigger writing event.
    
    while( Events && Trigger && !Read ) { EventThread.interrupt(); }
    
    //Start read event tracing.
    
    if( Events && !Trigger ) { TPos = super.getFilePointer(); TPosV = getVirtualPointer(); Read = true; Trigger = true; }

    //Begin read operation.

    int Pos = off, n = 0; len += off;

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
    //Trigger read event.
    
    while( Events && Trigger && Read ) { EventThread.interrupt(); }
    
    //Start write event tracing.
    
    if( Events && !Trigger ) { TPos = super.getFilePointer(); Read = false; Trigger = true; }

    //begin writing.

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
    //Trigger read event.
    
    while( Events && Trigger && Read ) { EventThread.interrupt(); }
    
    //Start write event tracing.
    
    if( Events && !Trigger ) { TPos = super.getFilePointer(); TPosV = getVirtualPointer(); Read = false; Trigger = true; }

    //begin writing.

    int Pos = 0, n = 0;
    
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
    //Trigger read event.
    
    while( Events && Trigger && Read ) { EventThread.interrupt(); }
    
    //Start write event tracing.
    
    if( Events && !Trigger ) { TPos = super.getFilePointer(); TPosV = getVirtualPointer(); Read = false; Trigger = true; }

    //begin writing.

    int Pos = off, n = 0; len += off;
    
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
  
  //fire seek event.
  
  @Override public void seek( long Offset ) throws IOException
  {
    while( Events && Trigger ) { EventThread.interrupt(); }
    
    super.seek( Offset ); fireIOEventSeek( new IOEvent( this, Offset, 0, getVirtualPointer(), 0, curVra.Len != 0 ) );
  }
  
  //Seek. Same as seek, but is a little faster of a read ahead trick.
  
  @Override public int skipBytes( int n ) throws IOException
  {
    while( Events && Trigger ) { EventThread.interrupt(); }
    
    int b = super.skipBytes( n );
    
    fireIOEventSeek( new IOEvent( this, super.getFilePointer(), 0,  getVirtualPointer(), 0, curVra.Len != 0 ) );
    
    return( b );
  }
  
  //Read and write events.
  
  @Override public int read() throws IOException
  {
    //Trigger writing event.
    
    while( Events && Trigger && !Read ) { EventThread.interrupt(); }
    
    //Start read event tracing.
    
    if( Events && !Trigger ) { TPos = super.getFilePointer(); Read = true; Trigger = true; }
    
    return( super.read() );
  }
  
  @Override public int read( byte[] b ) throws IOException
  {
    //Trigger writing event.
    
    while( Events && Trigger && !Read ) { EventThread.interrupt(); }
    
    //Start read event tracing.
    
    if( Events && !Trigger ) { TPos = super.getFilePointer(); Read = true; Trigger = true; }
    
    return( super.read( b ) );
  }
  
  @Override public int read( byte[] b, int off, int len ) throws IOException
  {
    //Trigger writing event.
    
    while( Events && Trigger && !Read ) { EventThread.interrupt(); }
    
    //Start read event tracing.
    
    if( Events && !Trigger ) { TPos = super.getFilePointer(); Read = true; Trigger = true; }
    
    return( super.read( b, off, len ) );
  }

  
  @Override public void write( int b ) throws IOException
  {
    //Trigger read event.
    
    while( Events && Trigger && Read ) { EventThread.interrupt(); }
    
    //Start write event tracing.
    
    if( Events && !Trigger ) { TPos = super.getFilePointer(); Read = false; Trigger = true; }
    
    super.write( b );
  }
  
  @Override public void write( byte[] b ) throws IOException
  {
    //Trigger read event.
    
    while( Events && Trigger && Read ) { EventThread.interrupt(); }
    
    //Start write event tracing.
    
    if( Events && !Trigger ) { TPos = super.getFilePointer(); Read = false; Trigger = true; }
    
    super.write( b );
  }
  
  @Override public void write( byte[] b, int off, int len ) throws IOException
  {
    //Trigger read event.
    
    while( Events && Trigger && Read ) { EventThread.interrupt(); }
    
    //Start write event tracing.
    
    if( Events && !Trigger ) { TPos = super.getFilePointer(); Read = false; Trigger = true; }
    
    super.write( b, off, len );
  }
  
  //Debug The address mapped memory.
  
  public void Debug()
  {
    String s = "";
    
    for( int i = 0; i < MSize; s += Map.get( i++ ) + "\r\n" );
    
    System.out.println( s );
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
        
        if( Trigger )
        {
          try
          {
            if( pos == super.getFilePointer() )
            {
              fireIOEvent( new IOEvent( this, TPos, pos - 1, TPosV, posV - 1, curVra.Len != 0 ) );
              Trigger = false;
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
