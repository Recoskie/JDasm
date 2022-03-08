package Format.MACDecode;

public class Data extends Window.Window
{
  //The descriptors explain the binary data sections as it is read.

  public static java.util.ArrayList<swingIO.Descriptor> des = new java.util.ArrayList<swingIO.Descriptor>();

  //This integer is used to keep track of the descriptors added to the list adn set to a node on the open event.

  public static int ref = 0, paths = 0;

  //A temporary descriptor holder that is used when reading data and adding descriptor to the list.

  public static swingIO.Descriptor DTemp = null;

  //In the case of a universal binary we must unload a binary before loading in another.

  public static swingIO.tree.JDNode App = null;
  public static java.util.ArrayList<swingIO.tree.JDNode> rPath = new java.util.ArrayList<swingIO.tree.JDNode>();

  //The selected application base address.

  public static long base = 0;

  //The Number of load commands.

  public static int loadCMD = 0;

  //Error when reading headers, or section.

  public static boolean error = false;

  //Check if it is 64 bit.

  public static boolean is64bit;

  //If the core is loaded.

  public static boolean coreLoaded = false;

  //The machine core type.

  public static int coreType = 0;
}
