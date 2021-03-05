package Format.EXEDecode;

import java.io.*;
import RandomAccessFileV.*;

import dataTools.*;
import core.x86.*;
import WindowComponents.*;

public class Headers extends Data
{
  //*********************************creates the data of the MZ header***********************************

  public Descriptor readMZ(RandomAccessFileV b) throws IOException
  {
    Descriptor mz = new Descriptor( b );

    mz.String8( "SIGNATURE", 2 ); String sig = mz.value + "";
    mz.LUINT16( "Size of Last Page" );
    mz.LUINT16( "Number of 512 byte pages in file" );
    mz.LUINT16( "Number of Relocation Entries" );
    mz.LUINT16( "Header size in Paragraphs" );
    mz.LUINT16( "Minimum additional Memory required in paragraphs" );
    mz.LUINT16( "Maximum additional Memory required in paragraphs" );
    mz.LUINT16( "Initial SS relative to start of file" );
    mz.LUINT16( "Initial SP" );
    mz.LUINT16( "Checksum (unused)" );
    mz.LUINT16( "Initial IP" );
    mz.LUINT16( "Initial CS relative to start of file" );
    mz.LUINT16( "Offset within Header of Relocation Table" );
    mz.LUINT16( "Overlay Number" );
    mz.Other( "Reserved", 8 );
    mz.LUINT16( "ID" );
    mz.LUINT16( "INFO" );
    mz.Other( "Reserved", 20 );
    mz.LUINT32( "PE Header Location" ); PE = ((Integer)mz.value).longValue();

    b.addV( b.getFilePointer(), PE - mz.length, 256, PE - mz.length );

    mz.Other( "8086 16-bit", (int)(PE - mz.length) );

    mz.setEvent(this::mzInfo);

    Data.error = !sig.equals("MZ"); return( mz );
  }

  //*********************************creates the nicely styled data of the PE header***********************************

  public Descriptor readPE(RandomAccessFileV b) throws IOException
  {
    Descriptor pe = new Descriptor( b );

    //data decode to table

    pe.String8( "SIGNATURE", 4 ); byte[] sig = ( pe.value + "" ).getBytes();
    pe.LUINT16( "Machine" ); coreType = ((Short)pe.value).shortValue();
    pe.LUINT16( "Number Of Sections" ); NOS = ((Short)pe.value).shortValue();
    pe.LUINT32( "Time Date Stamp" );
    pe.LUINT32( "Pointer To Symbol Table" );
    pe.LUINT32( "Number Of Symbols" );
    pe.LUINT16( "Size Of OP Header" );
    pe.Other( "Characteristics", 2 );

    pe.setEvent(this::peInfo);
    
    Data.error = !( sig[0] == 0x50 && sig[1] == 0x45 && sig[2] == 0 && sig[3] == 0 ); return( pe );
  }

  //************************************************READ OP HEADER********************************************

  public Descriptor readOP(RandomAccessFileV b) throws IOException
  {
    Descriptor op = new Descriptor(b);

    //The OP header has different signature meanings.
    //OP = 0B 01 is a 32 bit program, and 0B 02 is a 64 bit one. Additional 01 07 is a ROM image.
    //Note I could compare numbers 267 for 32 bit, 523 for 64 bit, and 263 for a ROM image.

    op.Other( "SIGNATURE", 2 ); byte[] OPS = ( op.value + "" ).getBytes();

    is64bit = OPS[0] == 0x0B && OPS[1] == 0x02;

    op.UINT8( "Major Linker Version" );
    op.UINT8( "Minor Linker Version" );

    op.LUINT32( "Size Of Code" ); sizeOfCode = ((Integer)op.value).intValue();
    op.LUINT32( "Size Of Initialized Data" );
    op.LUINT32( "Size Of Uninitialized Data" );
    op.LUINT32( "Start Of Code." ); startOfCode = ((Integer)op.value).intValue();
    op.LUINT32( "Base Of Code" ); baseOfCode = ((Integer)op.value).intValue();

    //32 bit only.

    if(!is64bit) { op.LUINT32( "Base Of Data" ); }

    //64 bit base address.

    if(is64bit) { op.LUINT64( "Base Address" ); imageBase = ((Long)op.value).longValue(); }
    
    //32 bit base address.

    else { op.LUINT32( "Base Address" ); imageBase = ((Integer)op.value).intValue(); }

    op.LUINT32( "Section Alignment" );
    op.LUINT32( "File Alignment" );

    op.LUINT16( "Major Operating System Version" );
    op.LUINT16( "Minor Operating System Version" );
    op.LUINT16( "Major Image Version" );
    op.LUINT16( "Minor Image Version" );
    op.LUINT16( "Major Sub system Version" );
    op.LUINT16( "Minor Sub system Version" );

    op.LUINT32( "Win 32 Version Value" );
    op.LUINT32( "Size Of Image" );
    op.LUINT32( "Size Of Headers" );
    op.LUINT32( "Check Sum" );

    op.LUINT16( "Sub system" );
    op.Other( "Dll Characteristics", 2 );

    //64 bit stack.

    if(is64bit)
    {
      op.LUINT64( "Size Of Stack Reserve" );
      op.LUINT64( "Size Of Stack Commit" );
      op.LUINT64( "Size Of Heap Reserve" );
      op.LUINT64( "Size Of Heap Commit" );
    }

    //32 bit stack.

    else
    {
      op.LUINT32( "Size Of Stack Reserve" );
      op.LUINT32( "Size Of Stack Commit" );
      op.LUINT32( "Size Of Heap Reserve" );
      op.LUINT32( "Size Of Heap Commit" );
    }

    op.Other( "Loader Flags", 4 );
    op.LUINT32( "Data Directory Array Size" ); DDS = ((Integer)op.value).intValue();

    op.setEvent(this::opInfo);

    //If op header was read properly.

    Data.error = !( OPS[0] == 0x0B && OPS[1] == 0x01 ) && !is64bit; return( op );
  }

  //************************************************READ Data Directory Array********************************************
  //Each section is given in virtual address position if used. Sections that are not used have a virtual address of 0.
  //The next header defines the sections that are to be read and placed in ram memory.

  public Descriptor readDataDrectory(RandomAccessFileV b) throws IOException
  {
    //Names of the data array locations

    String[] Types=new String[] {"Export DLL FUNCTIONS Location",
      "Import DLL FUNCTIONS Location",
      "Resource Location to Files In DLL or EXE",
      "Exceptions",
      "Security",
      "Relocation",
      "Debug",
      "Description/Architecture",
      "Machine Value (MIPS GP)",
      "Thread Storage",
      "Load Configuration",
      "Bound Import DLL Function Inside EXE",
      "Import Address Table",
      "Delayed Imports",
      "COM Runtime Descriptor",
      "USED BY MS DOS EXE DLL SYS Loader"};

    //Create table data.

    Descriptor dd = new Descriptor(b);

    //The Number of data Directory array sections.

    DataDir = new long[ DDS * 2 ]; DataDirUsed = new boolean[ DDS ];

    //Create the table data.

    for( int i = 0, i2 = 0; i < DDS; i++, i2 += 2 )
    {
      dd.Array( ( i < Types.length ) ? Types[ i ] : "Unknown use", 8 );

      dd.LUINT32( "Virtual Address" ); DataDir[ i2 ] = ((Integer)dd.value).intValue();
      dd.LUINT32( "Size" ); DataDir[ i2 + 1 ] = ((Integer)dd.value).intValue();

      //Test if data Dir Is used.

      DataDirUsed[ i ] = ( DataDir[ i2 ] > 0 ) && ( DataDir[ i2 + 1 ] > 0 ); DataDir[ i2 ] += imageBase;
    }

    dd.setEvent(this::ddInfo); return( dd );
  }

  //****************************************Read the Mapped Sections of executable, or dll*******************************************
  //The PE header defines the number of sections. Without this the virtual addresses of each section in DataDrectory is useless.

  public Descriptor readSections(RandomAccessFileV b) throws IOException
  {
    byte[] bd = new byte[ 12 ];
    
    long virtualSize = 0, virtualOffset = 0, size = 0, offset = 0;

    //Create table data.

    Descriptor sd = new Descriptor(b);

    for( int i = 0; i < NOS; i++ )
    {
      sd.Array( "Section Array element " + i + "", 40 );

      //Section name.
      
      sd.String8( "Section Name", 8 );

      //Virtual address.

      sd.LUINT32( "Section Size Loaded In Ram" ); virtualSize = ((Integer)sd.value).intValue();
      sd.LUINT32( "Where to Store Bytes in Ram"); virtualOffset = ((Integer)sd.value).intValue();
      sd.LUINT32( "Byte length to read from EXE file"); size = ((Integer)sd.value).intValue();
      sd.LUINT32( "Position to Start Reading EXE" ); offset = ((Integer)sd.value).intValue();

      //Reserved section.

      sd.Other( "Reserved", 12 );

      //Section FLAGS.

      sd.Other( "Section flags", 4 );

      //Add virtual address to IO system.

      b.addV( offset, size, virtualOffset + imageBase, virtualSize );
    }

    sd.setEvent(this::sdInfo); return(sd);
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

  public static final String[] MZinfo = new String[]{"<html>The signature must always be 4D 5A = MZ.<br /><br />" + 
  "It must be at the start of any windows binary.<br /><br />" +
  "If the file does not pass this test. Then it is corrupted.<br /><br />" + 
  "Or is a different file type disguise as a windows binary.</html>",
  "",
  "",
  "",
  "",
  "",
  "",
  "<html>" + sseg + stack + "</html>",
  "<html>" + sseg + stack + "</html>",
  "",
  "<html>" + cseg + Instruct + "</html>",
  "<html>" + cseg + Instruct + "</html>",
  "",
  "",
  "<html>" + res + "</html>",
  "",
  "",
  "<html>" + res + "</html>",
  "<html>" + res + "<br /><br />Instead of adding to DOS. Microsoft created a new system that uses the reserved section to locate to the PE header.</html>",
  "This is a x86 binary that gets loaded by DOS in 16 bit.<br /><br />The new \"Windows\" system used the PE location to go to the new PE header.<br /><br />"};

  public void mzInfo( int el )
  {
    if( el < 0 )
    {
      WindowComponents.info("<html>This is the original DOS header. Which must be at the start of all windows binary files.<br /><br />Today the reserved bytes are used to locate to the new Portable executable header format.<br /><br />" +
        "However, on DOS this header still loads as the reserved bytes that locate to the PE header do nothing in DOS.<br /><br />Thus the small 16 bit binary at the end will run. " +
        "Which normally contains a small 16 bit code that prints the message that this program can not be run in DOS mode.</html>");
    }

    //Disassemble 16 bit section.

    if( el == 19 )
    {
      String t = "", t1 = "", t2 = "";

      int Dos_exit = 0; //DOS has a exit code.

      //16 bit x86 DOS.

      X86 temp = new X86( Data.stream ); temp.setBit( X86.x86_16 );

      temp.setSeg( (short)0x0010 ); //Sets the code segment.

      try
      {
        //Disassemble till end, or return from application.
        //Note that more can be added here such as the jump operation.

        Data.stream.seekV( 256 );

        while( Data.stream.getFilePointer() < Data.PE )
        {
          t1 = temp.posV(); t2 = temp.disASM();
          
          if( Dos_exit == 0 && t2.equals("MOV AX,4C01") ) { Dos_exit = 1; }
          else if( Dos_exit == 1 && t2.equals("INT 21") ) { Dos_exit = 2; }
          
          t += t1 + " " + t2 + "<br />";

          if( Dos_exit == 2 ) { break; }
        }

        long pos = Data.stream.getFilePointer() - 1, posV = Data.stream.getVirtualPointer() - 1;
        
        Data.stream.seekV( 256 );
        WindowComponents.Virtual.setSelectedEnd( posV ); WindowComponents.Offset.setSelectedEnd( pos );
        
        WindowComponents.info( "<html>" + MZinfo[ el ] + t + "</html>" );
      }
      catch( Exception e ) { }
    }

    else
    {
      WindowComponents.info( MZinfo[ el ] );
    }
  }

  //Detailed description of the PE header.

  public static final String[] PEinfo = new String[]{"<html>The PE header must start with PE = 50 45 00 00.<br /><br />If it does not pass the signature test then the windows binary is corrupted.</html>",
  "<html>Windows does not translate binary to match other cores. It sets a core to the start of the program if CPU is compatible.<br /><br /><table border='1'>" +
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
  "There is also windows RT. Which RT is a ARM core compilation of windows. In which case you might see Machine ARM.</html>",
  "<html>This is the number of sections to read after the OP header. In the \"Mapped SECTIONS TO RAM\".<br /><br />" +
  "The sections specify a position to read the file, and virtual address to place the section, from the windows binary in RAM.</html>",
  "<html>A date time stamp is in seconds. The seconds are added to the starting date \"Wed Dec 31 7:00:00PM 1969\".<br /><br />" +
  "If the time date stamp is \"37\" in value, then it is plus 37 second giving \"Wed Dec 31 7:00:37PM 1969\".</html>",
  "",
  "",
  "",
  "",
  ""};

  public void peInfo( int el )
  {
    if( el < 0 )
    {
      WindowComponents.info("<html>The PE header marks the start of the new Executable format. If the file is not loaded in DOS.<br /><br />" +
        "This header specifies the number of sections to map in virtual space. The processor type, and date of compilation.</html>");
    }
    else { WindowComponents.info( PEinfo[ el ] ); }
  }

  //Detailed description of the OP header.

  public static final String Ver = "Major, and Minor are put together to forum the version number.<br /><br />Example.<br /><br />Major version = 5<br /><br />Minor version = 12<br /><br />Would mean version 5.12V.";
  
  public static final String[] OPinfo = new String[]{"<html>The Optional header has three different possible signatures.<br /><br />" +
  "0B 01 = 32 Bit binary.<br /><br />0B 02 = 64 Bit binary<br /><br />07 01 = ROM Image file.<br /><br />" +
  "The only time the OP header changes format is the 64 bit version of the Header.<br /><br />" +
  "If this section does not test true, for any of the three signatures, then the file is corrupted.</html>",
  "<html>" + Ver + "<br /><br />The linker links the sections together into a EXE, or DLL.</html>",
  "<html>" + Ver + "<br /><br />The linker links the sections together into a EXE, or DLL.</html>",
  "<html>Adding this to \"Base of code\" marks the end of the machine code. Plus the \"Base Address\".</html>",
  "",
  "",
  "<html>Start of the binaries machine code in virtual space. Plus the \"Base Address\".</html>",
  "<html>The beginning of the machine code section. Plus the \"Base Address\".<br /><br />The start position does not have to be at the very start of the machine code section.</html>",
  "<html>The Data section is a safe spot to put results from operations without writing over program machine code.<br /><br />In code these are called variables.</html>",
  "<html>Base address is added to all virtual addresses.<br /><br />It is the preferred address to load the mapped sections in RAM from this file.<br /><br />Windows may add to this number to space programs apart in virtual space.</html>",
  "",
  "",
  "<html>" + Ver + "<br /><br />The version number of the required operating system.</html>",
  "<html>" + Ver + "<br /><br />The version number of the required operating system.</html>",
  "<html>" + Ver + "<br /><br />The version number of this file.</html>",
  "<html>" + Ver + "<br /><br />The version number of this file.</html>",
  "<html>" + Ver + "<br /><br />The subsystem version.</html>",
  "<html>" + Ver + "<br /><br />The subsystem version.</html>",
  "<html>The win 32 Value has never been used.</html>",
  "<html>The size of this file.</html>",
  "<html>The size of the headers, for setting up the virtual space of this binary. Excluding the rest of the data.</html>",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "",
  "<html>Data Directory Array can be made bigger than it's default size 16.<br /><br />Which allows for more features to be added to the windows application format.</html>"};

  public void opInfo( int el )
  {
    if( el < 0 )
    {
      WindowComponents.info("<html>At the end of the PE header is the start of the Optional header. However, this header is not optional.</html>");
    }
    else { WindowComponents.info( OPinfo[ el >= 8 && is64bit ? el + 1 : el ] ); }
  }

  //Detailed description of the data Directory Array.

  public static final String[] DDinfo = new String[]{"<html>Array element consisting of two 32 bit values.</html>",
  "<html>Virtual Address of section.<br /><br />Plus the programs base address. The Base address is defined in OP header.</html>",
  "<html>Size of section data.</html>"};

  public void ddInfo( int el )
  {
    if( el < 0 )
    {
      WindowComponents.info("<html>This is the Data directory array section of the OP header. Every element has a different use.<br /><br />The virtual address positions are useless without setting up the mapped sections after the array.<br /><br />" +
        "The virtual addresses are added to the programs \"Base Address\". The \"Base Address\" is defined by the OP header.<br /><br />Anything that is 0, is not used.</html>");
    }
    else { WindowComponents.info( DDinfo[ el % 3 ] ); }
  }

  //Detailed description of the sections to RAM memory.

  public static final String[] Sinfo = new String[]{"<html>Array element consisting of A section name, and some 32 bit values, for the location to put the data in memory.</html>",
  "<html>The 8 bytes can be given any text based name you like. It is not used for anything by the system.<br /><br />" +
  "The names can be very deceiving. As x86 compilers can compile out the code section giving it a \".text\" name.<br /><br />" +
  "Don't worry about the names. The data Directory Array defines what each section is after it is in virtual space.<br /><br />" +
  "Thus the OP header marks the machine code in it's \"Start of code\" value. Which is a virtual address position.</html>",
  "<html>Number of bytes to put in virtual space. This reflects the sections actual size.<br /><br />As number of bytes read from file may be padded by the linker that linked the section together.</html>",
  "<html>The virtual address is added to the programs \"Base Address\".<br /><br />The programs \"Base Address\" is defined by the OP header.</html>",
  "<html>Number of bytes to read from file.<br /><br />The number of bytes read, may not all be put in RAM. If Number of bytes to put in virtual space is smaller.<br /><br />This happens, because sections are aligned in multiples by the linker.</html>",
  "<html>The position of the file to read.</html>",
  "<html>" + res + "</html>",
  ""};

  public void sdInfo( int el )
  {
    if( el < 0 )
    {
      WindowComponents.info("<html>Number of sections to read was defined in the OP header.<br /><br />The virtual address positions are useless without setting up the mapped sections.<br /><br />" +
        "The virtual addresses are added to the programs \"Base Address\". The \"Base Address\" is defined by the OP header.</html>");
    }
    else { WindowComponents.info( Sinfo[ el % 8 ] ); }
  }
}