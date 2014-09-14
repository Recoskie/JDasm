package EXEDecode;
import javax.swing.JTable;

public class Data
{
//varibles used to hold dll table of functions and dll name for when user click on dll
//the number in the name array corasponds to wich table to use
public static String DLLName[]=new String[0];
public static JTable DLLTable[]=new JTable[0];

//PE Header Lowcation
public static long PE=0;

//the number of maped sections in the executable
public static int NOS=0;

//Data Drectory Array Size Times 2
public static int DDS=0;

//the Data Drectory Array
public static long DataDir[];

//the used elements of the data dir
public static boolean DataDirUsed[];

//list of dlls used
public static String DLL[]=new String[0];

//list of functions used for the DLL
public static String FDLL[][]=new String[0][0];

//store the used DataDir Addresses And there Phisical Addresses Only and Size
//public static long DataDirPhisical[]=new long[0];

}