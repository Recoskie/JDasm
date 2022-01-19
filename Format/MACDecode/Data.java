package Format.MACDecode;

public class Data extends Window.Window
{
  //Mach-O universal binaries.

  public static boolean Universal;

  //Error when reading headers, or section.

  public static boolean error = false;

  //Check if it is 64 bit.

  public static boolean is64bit;

  //If the core is loaded.

  public static boolean coreLoaded = false;

  //The machine core type.

  public static int coreType = 0;
}
