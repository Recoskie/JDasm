package RandomAccessFileV;
import java.io.*;

//Sector read version.

public class RandomAccessFileVS extends RandomAccessFileV
{
  private static byte[] buf = new byte[512];
  private long TPos = 0;
  private long base = 0;

  public RandomAccessFileVS( File file, String mode ) throws FileNotFoundException { super( file, mode ); }
  
  public RandomAccessFileVS( String name, String mode ) throws FileNotFoundException { super( name, mode ); }
  
  //Temporary data. This is so that components that are dependent on this file system can be used without a target file.
  
  public RandomAccessFileVS( byte[] data ) throws IOException { super( data ); }
  
  public RandomAccessFileVS( byte[] data, long Address ) throws IOException { super( data, Address ); }

  //Read and write events.
  
  @Override public int read() throws IOException
  {
    super.Events = false;

    TPos = super.getFilePointer(); base = ( TPos / 512 ) * 512;

    super.seek( base ); super.read( buf ); super.seek( TPos + 1 );

    super.Events = true;
    
    return( (int)buf[(int)( TPos - base )] );
  }
  
  @Override public int read( byte[] b ) throws IOException
  {
    super.Events = false;

    TPos = super.getFilePointer(); base = ( TPos / 512 ) * 512;

    buf = new byte[(int)( ( b.length / 512 ) + 1 ) * 512];

    super.seek( base );
    
    int r = super.read( buf ); super.seek( TPos + b.length );

    for(int s = (int)( TPos - base ), e = (int)( TPos + b.length ), i = 0; s < e; b[i++] = buf[s++] );

    buf = new byte[512];

    super.Events = true;
    
    return( r );
  }
  
  @Override public int read( byte[] b, int off, int len ) throws IOException
  {
    super.Events = false;

    TPos = super.getFilePointer(); base = ( TPos / 512 ) * 512;

    buf = new byte[(int)( ( b.length / 512 ) + 1 ) * 512];

    super.seek( base ); int r = super.read( buf ); super.seek( TPos + b.length );

    for(int s = (int)( TPos - base ) + off, e = (int)( TPos + off ) + len, i = 0; s < e; b[i++] = buf[s++] );

    buf = new byte[512];

    super.Events = true;
    
    return( r );
  }
}
