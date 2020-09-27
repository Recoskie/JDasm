package EXEDecode;
import javax.swing.*;
import WindowCompoents.*;

//The descriptor allows decoded information in headers to be explained in detail.

public class Descriptor extends JTable
{
  //Types of data.

  public static final int MZ = 0, PE = 1, OP = 2, dataDirectoryArray = 3, sections = 4;

  //The set Type.

  public static int type = -1;

  //Basic constructor.

  public Descriptor( Object[][] rows, Object[] cols ) { super( rows, cols ); }

  //Set the descriptor type.

  public void setType( int t ){ type = t; }

  //No cells are editable. Also on click/edit display detailed information of row.

  @Override public boolean isCellEditable( int row, int col )
  {
    if( type == MZ ) { MZinfo( row ); } else if( type == PE ) { PEinfo( row ); } else if( type == OP ) { OPinfo( row ); }

    else if( type == dataDirectoryArray ){ DDRinfo( row ); }

    else if( type == sections ){ Sinfo( row ); }

    //No cells are editable.
    
    return(false);
  }

  //Detailed description of the MZ header.

  public static final String res = "A section that is reserved, is skipped. So that some day the empty space may be used for something new.";
  
  public static final String stack = "The SP (stack pointer) is a place that CPU uses to store data. Each thing wrote into the stack increments the stack pointer.<br /><br />" +
  "Each thing read from the stack deincrements the stack pointer. Thus the first thing read is the last thing added to the stack.<br /><br />" +
  "The stack is used between method calls. As the stack is a convenient place to put things that function, or method uses as input.<br /><br />" +
  "It is important that the stack pointer is adjusted away from the program. So the stack does not write into the programs machine code in virtual space.";

  public static final String Instruct = "The instruction pointer is the position the CPU is set with the binary code.<br /><br />" +
  "The CPU reads the memory at the position of the instruction pointer, and does a operation.<br /><br />" +
  "Instruction pointer increments after completing a single operation. To fetch the next instruction. This repeats in a cycle.<br /><br />" +
  "The instruction pointer is built into the CPU in order to run software.";
  
  public static final String sseg = "SS (Stack segment) is a value that is multiplied by 16 plus the SP (stack pointer) to forum the stack pointer position.<br /><br />" +
  "This was done to make the address space bigger in 16 bit computers.<br /><br />" +
  "Thus 32 bit, and 64 bit systems no longer use a segment. Unless set 16 bit mode.<br /><br />";

  public static final String cseg = "CS (Code segment) is a value that is multiplied by 16 plus the IP (Instruction pointer) to forum the Instruction pointer position.<br /><br />" +
  "This was done to make the address space bigger in 16 bit computers.<br /><br />" +
  "Thus 32 bit, and 64 bit systems no longer use a segment. Unless set 16 bit mode.<br /><br />";

  public static final int[] MZsec = new int[]{0,2,4,6,8,10,12,14,16,18,20,22,24,26,28,36,38,40,60,64};
  public static final String[] MZinfo = new String[]{"<html><p>The signature must always be 4D 5A = MZ.<br /><br />" + 
  "It must be at the start of any windows binary.<br /><br />" +
  "If the file does not pass this test. Then it is corrupted.<br /><br />" + 
  "Or is a different file type disguise as a windows binary.</p></html>",
  "",
  "",
  "",
  "",
  "",
  "",
  "<html><p>" + sseg + stack + "</p></html>",
  "<html><p>" + sseg + stack + "</p></html>",
  "",
  "<html><p>" + cseg + Instruct + "</p></html>",
  "<html><p>" + cseg + Instruct + "</p></html>",
  "",
  "",
  "<html><p>" + res + "</p></html>",
  "",
  "",
  "<html><p>" + res + "</p></html>",
  "<html><p>" + res + "<br /><br />Instead of adding to DOS. Microsoft created a new system that uses the reserved section to locate to the PE header.</p></html>",
  "<html><p>This is a x86 binary that gets loaded by DOS in 16 bit. The new \"Windows\" system used the PE location to go to the new PE header.</html>"};

  public void MZinfo( int row )
  {
    //Select Bytes.

    WindowCompoents.Offset.setSelected( MZsec[row], row == 19 ? Data.PE - 1 : MZsec[row+1] - 1 );

    //No description outputs yet.

    WindowCompoents.info( MZinfo[ row ] );
  }

  //Detailed description of the PE header.

  public static final int[] PEsec = new int[]{0,4,6,8,12,16,20,22,24};
  public static final String[] PEinfo = new String[]{"<html><p>The PE header must start with PE = 50 45 00 00.<br /><br />If it does not pass the signature test then the windows binary is corrupted.</p></html>",
  "<html><p>Windows does not translate binary to match other cores. It sets a core to the start of the program if CPU is compatible.<br /><br /><table border='1'>" +
  "<tr><td>Value</td><td>Type</td></tr>" +
  "<tr><td>4C 01</td><td>Intel 386</td></tr>" +
  "<tr><td>64 86</td><td>Intel x64, and AMD x64</td></tr>" +
  "<tr><td>62 01</td><td>MIPS R3000</td></tr>" +
  "<tr><td>68 01</td><td>MIPS R10000</td></tr>" +
  "<tr><td>69 01</td><td>MIPS little endian WCI v2</td></tr>" +
  "<tr><td>83 01</td><td>old Alpha AXP</td></tr>" +
  "<tr><td>84 01</td><td>Alpha AXP</td></tr>" +
  "<tr><td>A2 01</td><td>Hitachi SH3</td></tr>" +
  "<tr><td>A3 01</td><td>Hitachi SH3 DSP</td></tr>" +
  "<tr><td>A6 01</td><td>Hitachi SH4</td></tr>" +
  "<tr><td>A8 01</td><td>Hitachi SH5</td></tr>" +
  "<tr><td>C0 01</td><td>ARM little endian</td></tr>" +
  "<tr><td>C2 01</td><td>Thumb</td></tr>" +
  "<tr><td>C4 01</td><td>ARMv7 (Thumb-2)</td></tr>" +
  "<tr><td>D3 01</td><td>Matsushita AM33</td></tr>" +
  "<tr><td>F0 01</td><td>PowerPC little endian</td></tr>" +
  "<tr><td>F1 01</td><td>PowerPC with floating point support</td></tr>" +
  "<tr><td>F2 01</td><td>PowerPC 64-bit little endian</td></tr>" +
  "<tr><td>00 02</td><td>Intel IA64</td></tr>" +
  "<tr><td>66 02</td><td>MIPS16</td></tr>" +
  "<tr><td>68 02</td><td>Motorola 68000 series</td></tr>" +
  "<tr><td>84 02</td><td>Alpha AXP 64-bit</td></tr>" +
  "<tr><td>66 03</td><td>MIPS with FPU</td></tr>" +
  "<tr><td>66 04</td><td>MIPS16 with FPU</td></tr>" +
  "<tr><td>BC 0E</td><td>EFI Byte Code</td></tr>" +
  "<tr><td>41 90</td><td>Mitsubishi M32R little endian</td></tr>" +
  "<tr><td>64 AA</td><td>ARM64 little endian</td></tr>" +
  "<tr><td>EE C0</td><td>clr pure MSIL</td></tr>" +
  "</table><br />Generally Windows is wrote in x86 machine code. So the only two settings you will ever see used are.<br /><br />" +
  "4C 01 = Intel 386 is 32 bit x86 machine code.<br />64 86 = Intel x64, and AMD x64 is 64 bit x86 machine code.<br /><br />A 64 bit x86 core can run 32 bit by setting operation size 32 bits when running code.<br /><br />" +
  "However a 32 bit x86 core can not be forced to do 64 bit in length operations. Even though the machine code is the same.<br /><br />" +
  "There is also windows RT. Which RT is a ARM core compilation of windows. In which case you might see Machine ARM.</p></html>",
  "<html><p>This is the number of sections to read after the OP header. In the \"Mapped SECTOINS TO RAM\".<br /><br />" +
  "The sections specify a position to read the file, and virtual address to place the section, from the windows binary in RAM.</p></html>",
  "",
  "",
  "",
  "",
  "",
  ""};

  public void PEinfo( int row )
  {
    //Select Bytes.

    WindowCompoents.Offset.setSelected( Data.PE + PEsec[row], Data.PE + PEsec[row+1] - 1 );

    //No description outputs yet.

    WindowCompoents.info( PEinfo[ row ] );
  }

  //Detailed description of the OP header.

  public static final int[] OP32sec = new int[]{24,26,27,28,32,36,40,44,48,52,56,60,64,66,68,70,72,74,76,80,84,88,92,94,96,100,104,108,112,116,120};
  public static final int[] OP64sec = new int[]{24,26,27,28,32,36,40,44,48,52,56,60,64,66,68,70,72,74,76,80,84,88,92,94,96,104,112,120,128,132,136};
  
  public static final String[] OPinfo = new String[]{"<html><p>The Optional header has three different possible signatures.<br /><br />" +
  "0B 01 = 32 Bit binary.<br /><br />0B 02 = 64 Bit binary<br /><br />07 01 = ROM Image file.<br /><br />" +
  "The only time the OP header changes format is the 64 bit version of the Header.<br /><br />" +
  "If this section does not test true, for any of the three signatures, then the file is corrupted.</p></html>",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "<html><p>Data Directory Array can be made bigger than it's default size 16.<br /><br />Which allows for more features to be added to the windows application format.<p></html>"};

  public void OPinfo( int row )
  {
    //Select Bytes.

    WindowCompoents.Offset.setSelected( Data.PE + ( Data.is64bit ? OP64sec[row] : OP32sec[row] ), Data.PE + ( Data.is64bit ? OP64sec[row+1] : OP32sec[row+1] ) - 1 );

    //No description outputs yet.

    WindowCompoents.info( OPinfo[ row ] );
  }

  //Detailed description of the data Directory Array.

  public void DDRinfo( int row )
  {
    int pos = (int)Data.PE + ( Data.is64bit ? 136 : 120 ); pos += ( row / 3 ) << 3;

    //Select Bytes.

    int end = row % 3;

    if( end == 0 ) { end = pos + 7; }

    if( end == 1 ) { end = pos + 3; }

    if( end == 2 ) { pos += 4; end = pos + 3; }

    WindowCompoents.Offset.setSelected( pos, end );

    //No extra information possible.

    WindowCompoents.info( "" );
  }

  //Detailed description of the sections to RAM memory.

  public static final String[] Sinfo = new String[]{"<html><p>The 8 bytes can be given any text based name you like. It is not used for anything by the system.<br /><br />" +
  "The names can be very deceiving. As x86 compilers can compile out the code section giving it a \".text\" name.<br /><br />" +
  "Don't worry about the names. The data Directory Array defines what each section is after it is in virtual space.<br /><br />" +
  "Thus the PE header marks the machine code in it's \"Base of code\" value. Which is a virtual address position.</p></html>",
  "",
  "",
  "",
  "",
  "<html><p>" + res + "</p></html>",
  ""};

  public void Sinfo( int row )
  {
    int pos = (int)Data.PE + ( Data.is64bit ? 136 : 120 ); pos += ( Data.DDS / 3 ) << 3;

    //Select Bytes.

    int end = row % 7; WindowCompoents.info( Sinfo[ end ] );
    
    pos += ( row / 7 ) * 40;

    if( end == 0 ){ end = pos + 7; }
    else if( end < 5 ) { end -= 1; pos += 8 + (end << 2); end = pos + 3; }
    else if( end == 5 ) { pos += 24; end = pos + 11; }
    else if( end == 6 ) { pos += 36; end = pos + 3; }

    WindowCompoents.Offset.setSelected( pos, end );
  }
}