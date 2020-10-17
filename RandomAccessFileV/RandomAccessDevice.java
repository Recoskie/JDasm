package RandomAccessFileV;
import java.io.*;

//Sector read version.

public class RandomAccessDevice extends RandomAccessFileV
{
  //Sector buffer. Sector size must be known to read disk.

  private static byte[] sector;

  //Used to keep track of position in sectors. Allowing unaligned read.

  private long TempPos = 0;

  //Base address, of sector.

  private long base = 0;

  //Data start position within sector.

  private int pos = 0;

  //Start, and end sector.

  private int sectN = 0, sectE = 0;

  //Number of readable bytes. Can be smaller, Because reached end of disk, or bad sector.

  private int r = 0;

  //Used to restore the original event state at end of method.

  private boolean e = false;

  //The size of a raw disk volume. Does not really need to be calculated in order to read a disk.

  private long size = 0;

  public RandomAccessDevice( File file, String mode ) throws FileNotFoundException
  {
    super( file, mode );
    
    //Sector size.

    try { getSectorSize(); } catch( IOException e ) { throw( new FileNotFoundException("") ); }
  }
  
  public RandomAccessDevice( String name, String mode ) throws FileNotFoundException
  {
    super( name, mode );

    //Sector size.

    try { getSectorSize(); } catch( IOException e ) { throw( new FileNotFoundException("") ); }
  }

  //Get sector size.

  private void getSectorSize() throws IOException
  {
    boolean end = false; sectN = 1; while( !end && sectN <= 0x1000 ) { sector = new byte[ sectN ]; try { super.read( sector ); end = true; } catch( IOException e ) { sectN <<= 1; } }

    if( !end ) { throw( new IOException("Disk Error!") ); } else { super.seek(0); }
  }

  //Read data at any position in sectors as a regular Random access file.
  
  @Override public int read() throws IOException
  {
    //Disable event during operation.

    super.syncR(); e = super.Events; super.Events = false;

    //Read operation.

    TempPos = super.getFilePointer(); base = ( TempPos / sector.length ) * sector.length;

    super.seek( base ); super.read( sector ); super.seek( TempPos + 1 );
      
    r = (int)sector[(int)( TempPos - base )];

    //Restore event state.
    
    super.Events = e; return( r );
  }

  @Override public int read( byte[] b ) throws IOException
  {
    //Disable event during operation.

    super.syncR(); e = super.Events; super.Events = false;

    //Sector start address.

    TempPos = super.getFilePointer(); base = ( TempPos / sector.length ) * sector.length;

    //Start of position within sector.

    pos = (int)( TempPos - base );
    
    //Number of sectors needed, for the size of the data being read.
    
    sectE = (int)( ( ( pos + b.length ) / sector.length ) + 1 );

    //Start sector and number of read bytes inti 0.

    sectN = 0; r = 0;

    //Move to Sector start address.

    super.seek( base );

    //Read and test each sector.
    
    try
    {
      while( sectN < sectE )
      {
        super.read( sector );

        while( pos < sector.length && r < b.length ){ b[ r++ ] = sector[ pos++ ]; }
        
        pos = 0; sectN += 1;
      }
    } catch( IOException er ) {}

    //Add original file potion by read operation size.
    
    super.seek( TempPos + b.length );

    //Restore event state.

    super.Events = e; return( r );
  }
  
  @Override public int read( byte[] b, int off, int len ) throws IOException
  {
    //Disable event during operation.

    super.syncR(); e = super.Events; super.Events = false;

    //Sector start address.

    TempPos = super.getFilePointer(); base = ( TempPos / sector.length ) * sector.length;

    //Start of position within sector.

    pos = (int)( TempPos - base );
    
    //Number of sectors needed, for the size of the data being read.
    
    sectE = (int)( ( ( pos + b.length ) / sector.length ) + 1 );

    //Start sector and number of read bytes inti 0.

    sectN = 0; r = off;

    //Move to Sector start address.

    super.seek( base );

    //Read and test each sector.
    
    try
    {
      while( sectN < sectE )
      {
        super.read( sector );

        while( pos < sector.length && r < len ){ b[ r++ ] = sector[ pos++ ]; }
        
        pos = 0; sectN += 1;
      }
    } catch( IOException er ) {}

    //Add original file potion by read operation size.
    
    super.seek( TempPos + b.length );

    //Restore event state.

    super.Events = e; return( r - off );
  }

  //The size of the disk.

  @Override public long length()
  {
    if( size == 0 )
    {
      try
      {
        size = super.length();
      }
      catch( IOException er )
      {
        super.Events = false; try { TempPos = super.getFilePointer(); } catch( IOException e ) { }

        //Calculate the length of a raw disk volume.

        long bit = sector.length; boolean end = false;
    
        end = false; while( bit <= 0x4000000000000000L && !end ) { try { super.seek( bit <<= 1 ); super.read( sector ); } catch( IOException e ) { bit >>= 1; size |= bit; end = true; } }

        while( bit >= sector.length )
        {
          try { super.seek( size | bit ); super.read( sector ); size |= bit; } catch( IOException e ) { }

          bit >>= 1;
        }

        size += sector.length - 1; //End of last sector.
    
        try { super.seek( TempPos ); } catch( IOException e ) { } super.Events = true;
      }
    }

    return( size );
  }
}
