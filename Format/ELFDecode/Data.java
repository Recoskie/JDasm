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
  public static int elPrSize = 0;
  public static int prSize = 0;

  //The sections location.

  public static long Sections = 0;
  public static int elSecSize = 0;
  public static int secSize = 0;
  public static int namesEl = 0;

  //The machine core type.

  public static int coreType = 0;

  //Check if core type is loaded.

  public static boolean coreLoaded = false;

  //section data.

  public static sect[] st;

  //Sections can be separated by the data they contain.

  public static swingIO.tree.JDNode[] sections = new swingIO.tree.JDNode[]
  {
    new swingIO.tree.JDNode("Headers", 0), //Headers.
    new swingIO.tree.JDNode("Code Sections", 1), //Sections marked as runnable code.
    new swingIO.tree.JDNode("Link library Sections", 2), //Linked libraries method sections.
    new swingIO.tree.JDNode("String Table Sections", 3), //String table sections.
    new swingIO.tree.JDNode("Relocation Sections", 4), //Relocations to be applied if program loads in different address.
    new swingIO.tree.JDNode("Debug Sections", 5), //Relocations to be applied if program loads in different address.
    new swingIO.tree.JDNode("Local thread storage Sections", 6), //The local thread storage section.
    new swingIO.tree.JDNode("Array Sections", 7), //Array of init, fini, pre-init.
    new swingIO.tree.JDNode("Notes Sections", 8), //The section notes sections.
    new swingIO.tree.JDNode("Other Sections", 9) //Sections that are marked as straight data with no type.
  };
}