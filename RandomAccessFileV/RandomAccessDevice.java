package RandomAccessFileV;
import java.io.*;

//Sector read version.

public class RandomAccessDevice extends RandomAccessFileV implements Runnable
{
  //One has to wait, for the disk to be measured. Error is in-case of error.

  private boolean init = false, error = false, run = false;

  //Sector buffer.

  private static byte[] buf;

  //Sector size.

  private static int sector = 1;

  //Used to keep track of position in sectors. Allowing unaligned read.

  private long TempPos = 0;

  //Base of address in sector.

  private long base = 0;

  //number of bytes read.

  private int r = 0;

  //Used to restore the original event state at end of method.

  private boolean e = false;

  //The size of a raw disk volume.

  private long size = 0;

  public RandomAccessDevice( File file, String mode ) throws FileNotFoundException
  {
    super( file, mode ); try { new Thread(this).start(); } catch( Exception e ) { }
  }
  
  public RandomAccessDevice( String name, String mode ) throws FileNotFoundException
  {
    super( name, mode ); try { new Thread(this).start(); } catch( Exception e ) { }
  }

  //Read data at any position in sectors as a regular Random access file.
  
  @Override public int read() throws IOException
  {
    //Disable event during operation.

    super.syncR(); e = super.Events; super.Events = false;

    //Read operation.

    TempPos = super.getFilePointer(); base = ( TempPos / sector ) * sector;

    super.seek( base ); super.read( buf ); super.seek( TempPos + 1 );
      
    r = (int)buf[(int)( TempPos - base )];

    //Restore event state.
    
    super.Events = e; return( r );
  }
  
  @Override public int read( byte[] b ) throws IOException
  {
    //Disable event during operation.

    super.syncR(); e = super.Events; super.Events = false;

    //Read operation.

    TempPos = super.getFilePointer(); base = ( TempPos / sector ) * sector;
    
    buf = new byte[(int)( ( ( ( TempPos - base ) + b.length ) / sector ) + 1 ) * sector];

    super.seek( base ); r = super.read( buf ); super.seek( TempPos + b.length );

    for(int s = (int)( TempPos - base ), e = (int)( s + b.length - 1 ), i = 0; s <= e; b[i++] = buf[s++] );

    buf = new byte[sector];

    //Restore event state.

    super.Events = e; return( r );
  }
  
  @Override public int read( byte[] b, int off, int len ) throws IOException
  {
    //Disable event during operation.

    super.syncR(); e = super.Events; super.Events = false;

    //Read operation.

    TempPos = super.getFilePointer(); base = ( TempPos / sector ) * sector;

    buf = new byte[(int)( ( ( ( TempPos - base ) + b.length ) / sector ) + 1 ) * sector];

    super.seek( base ); r = super.read( buf ); super.seek( TempPos + b.length );

    for(int s = (int)( TempPos - base ) + off, e = (int)( s ) + ( len - 1 ), i = 0; s <= e; b[i++] = buf[s++] );

    buf = new byte[sector];

    //Restore event state.

    super.Events = e; return( r );
  }

  //The size of the disk.

  @Override public long length() { return( size ); }

  //Wait till device is ready.

  @Override public void ready() throws IOException
  {
    while( !init || error ) { try{ Thread.sleep( 70 ); } catch(InterruptedException e) { } }
    
    if( error ){ throw( new IOException("Disk Error.") ); }
  }

  //Disk information. Note blank, or corrupted disks do not have disk information, so it is important to run this for all disks and media.

  public void run()
  {
    if( !run )
    {
      run = true; super.Events = false;

      //Sector size.

      boolean end = false; while( !end && sector <= 0x1000 ) { buf = new byte[sector]; try { super.read( buf ); end = true; } catch( IOException e ) { sector <<= 1; } }

      //Calculate the length of a raw disk volume.

      if( end )
      {
        try { super.seek(0); } catch( IOException e ) {  }

        long bit = 0x4000000000000000L;
    
        while( bit >= sector )
        {
          try { super.seek( size | bit ); super.read( buf ); size |= bit; } catch( IOException e ) { }

          bit >>= 1;
        }

        size += sector - 1; //End of last sector.

        try { super.seek(0); } catch( IOException e ) { }
      
        init = true;
      }
      else { error = true; }

      super.Events = true;
    }
  }
}