package Format.MACDecode;

public class Data extends Window.Window
{
  //In the case of a universal binary we must unload a binary before loading in another.

  public static swingIO.tree.JDNode App;

  //Error when reading headers, or section.

  public static boolean error = false;

  //Check if it is 64 bit.

  public static boolean is64bit;

  //If the core is loaded.

  public static boolean coreLoaded = false;

  //The machine core type.

  public static int coreType = 0;
}
