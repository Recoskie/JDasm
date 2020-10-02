package Format.EXEDecode;

import javax.swing.JTable;
import RandomAccessFileV.*;

public class Data
{
  //File system stream.

  public static RandomAccessFileV stream;

  //variables used to hold dll table of functions and dll name for when user click on dll
  //the number in the name array corresponds to which table to use.
  
  public static String DLLName[] = new String[0];
  public static JTable DLLTable[] = new JTable[0];

  //The PE header changes format if program is 64 bit.

  public static boolean is64bit = false;

  //PE Header Location.

  public static long PE = 0;

  //Machine code.

  public static int baseOfCode = 0, sizeOfCode = 0, startOfCode = 0;

  //Base address of the program.

  public static long imageBase = 0;

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

  //list of functions used from the DLL.
  //Note the 32 bit number that locates to the method name.
  //Is wrote over by windows after it is loaded at a address location.
  //The location that located to the method name then locates to the method.
  //A disassembled call operation that locates to the DLL string location is then a function call to the method.

  public static String FDLL[][] = new String[0][0];

  //Error when reading headers, or section.

  public static boolean error = false;

  //Different lengths of byte array, for reading different data types.

  public static final byte[] b1 = new byte[1], b2 = new byte[2], b4 = new byte[4], b8 = new byte[8];

  //Processor core interface engine.

  public static core.Core core;

  //If the core is loaded.

  public static boolean coreLoaded = false;

  //The machine core type.

  public static int coreType = 0;

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