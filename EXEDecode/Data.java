package EXEDecode;
import javax.swing.JTable;

public class Data
{
  //variables used to hold dll table of functions and dll name for when user click on dll
  //the number in the name array corresponds to which table to use.
  
  public static String DLLName[] = new String[0];
  public static JTable DLLTable[] = new JTable[0];

  //PE Header Location.

  public static long PE = 0;

  //the number of mapped sections in the executable.

  public static int NOS = 0;

  //Data Directory Array Size Times 2.

  public static int DDS = 0;

  //the Data Directory Array

  public static long DataDir[];

  //the used elements of the data dir.

  public static boolean DataDirUsed[];

  //list of dlls used

  public static String DLL[] = new String[0];

  //list of functions used from the DLL

  public static String FDLL[][] = new String[0][0];

  //Methods to convert bytes to different data types.

  public String toHex( byte[] b )
  {
    String o = "";
    
    for( int i = 0; i < b.length; i++ )
    {
      o += String.format( "%1$02X", b[i] ) + " ";
    }

    return( o );
  }

  public String toText( byte[] b )
  {
    String o = "";
    
    for( int i = 0; i < b.length; i++ )
    {
      o += ((char)b[i]);
    }

    return( o );
  }

  public short toShort( byte[] b )
  {
    return( (short)( ( b[0] & 0xFF ) | ( ( b[1] << 8 ) & 0xFF00 ) ) );
  }

  public int toInt( byte[] b )
  {
    return( ( b[0] & 0xFF ) | ( (b[1] << 8) & 0xFF00 ) | ( (b[2] << 16) & 0xFF0000 ) | ( (b[3] << 24) & 0xFF000000 ) );
  }

  public long toLong( byte[] b )
  {
    return( ( (long)b[0] & 0xFFL ) | ( ((long)b[1] << 8) & 0xFF00L ) | ( ((long)b[2] << 16) & 0xFF0000L ) | ( ((long)b[3] << 24) & 0xFF000000L ) |
     ( ( (long)b[0] << 32 ) & 0xFF00000000L ) | ( ( (long)b[0] << 40 ) & 0xFF0000000000L ) | ( ( (long)b[0] << 48 ) & 0xFF000000000000L ) | ( ( (long)b[0] << 56 ) & 0xFF00000000000000L ) );
  }
}