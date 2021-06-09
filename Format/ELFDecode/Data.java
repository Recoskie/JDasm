package Format.ELFDecode;

public class Data extends Window.Window
{
  //An error occurred while read the ELF binary file.
  
  public static boolean error = false;

  //Weather the byte felids are little or big endian.

  public static boolean isLittle = true;

  //File is 64 bits.

  public static boolean is64Bit = false;

  //The start of the program.

  public static long start = 0;

  //Program information.

  public static long programHeader = 0;

  //The sections location.

  public static long Sections = 0;

  //The machine core type.

  public static int coreType = 0;

  //Check if core type is loaded.

  public static boolean coreLoaded = false;
}