package Format.EXEDecode;

import javax.swing.JTable;

public class Data extends Window.Window
{
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

  //Data Directory Array Size.

  public static int DDS = 0;

  //the Data Directory Array

  public static long DataDir[];

  //the used elements of the data dir.

  public static boolean DataDirUsed[];

  //list of dlls used

  public static String DLL[] = new String[0];

  //list of functions used from the DLL.
  //Note the 32 bit number that locates to the method name. Is wrote over by windows after it is loaded at a address location.
  //The location that located to the method name then locates to the method.
  //A disassembled call operation that locates to the DLL name location is then a function call to the method.

  public static String FDLL[][] = new String[0][0];

  //Error when reading headers, or section.

  public static boolean error = false;

  //Processor core interface engine.

  public static core.Core core;

  //If the core is loaded.

  public static boolean coreLoaded = false;

  //The machine core type.

  public static int coreType = 0;
}