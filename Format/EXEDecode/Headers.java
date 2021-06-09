package Format.EXEDecode;

import java.io.*;
import swingIO.*;
import swingIO.tree.JDNode;

public class Headers extends Data
{
  //*********************************creates the data of the MZ header***********************************

  public Descriptor[] readMZ( JDNode DOS ) throws IOException
  {
    Descriptor mz = new Descriptor( file ), Reloc = mz;

    String sig = "";

    int R_Size = 0; long R_Loc = 0;

    DOS.add( new JDNode( "DOS 2.0 Header.h", new long[]{ 0, 0 } ));

    mz.String8( "SIGNATURE", 2 ); sig = mz.value + "";
    mz.LUINT16( "Last 512 bytes" );
    mz.LUINT16( "512 bytes in file" );
    mz.LUINT16( "Number of Relocation Entries" ); R_Size = ((Short)mz.value).intValue();
    mz.LUINT16( "Header size" ); MZSize = ((Short)mz.value).intValue()*16;
    mz.LUINT16( "Minimum Memory" );
    mz.LUINT16( "Maximum Memory" );
    mz.LUINT16( "Initial SS relative to start of file" );
    mz.LUINT16( "Initial SP" );
    mz.LUINT16( "Checksum (unused)" );
    mz.LUINT16( "Initial IP" ); MZMain = ((Short)mz.value).intValue();
    mz.LUINT16( "Initial CS relative to start of file" ); MZMain += ((Short)mz.value).intValue() * 16;
    mz.LUINT16( "Relocations Offset" ); R_Loc = ((Short)mz.value).longValue();
    mz.LUINT16( "Overlay Number" );
    mz.Other( "Reserved", 8 );
    mz.LUINT16( "ID" );
    mz.LUINT16( "INFO" );
    mz.Other( "Reserved", 20 );
    mz.LUINT32( "PE Header Location" ); PE = ((Integer)mz.value).longValue();

    if( R_Size != 0 )
    {
      file.seek( R_Loc ); Reloc = new Descriptor( file );

      for( int i = 0; i < R_Size; i++ ) { Reloc.Array("Location #" + i + "", 4); Reloc.LUINT16("Offset"); Reloc.LUINT16("Segment"); }

      DOS.add( new JDNode( "DOS Relocations.h", new long[]{ 0, 1 } ));
    }

    DOS.add( new JDNode( "Program Start (Machine Code).h", "Dis16", new long[]{ -2, MZMain } ) );

    mz.setEvent(this::mzInfo); Reloc.setEvent(this::mzRelocInfo);

    Data.error = !sig.equals("MZ"); return( new Descriptor[] { mz, Reloc } );
  }

  //*********************************creates the nicely styled data of the PE header***********************************

  public Descriptor readPE() throws IOException
  {
    file.seek(PE); Descriptor pe = new Descriptor( file );

    //data decode to table

    pe.String8( "SIGNATURE", 4 ); byte[] sig = ( pe.value + "" ).getBytes();

    Data.DOS = !( sig[0] == 0x50 && sig[1] == 0x45 && sig[2] == 0 && sig[3] == 0 );

    pe.LUINT16( "Machine" ); coreType = ((Short)pe.value).shortValue();
    pe.LUINT16( "Number Of Sections" ); NOS = ((Short)pe.value).shortValue();
    pe.LUINT32( "Time Date Stamp" );
    pe.LUINT32( "Pointer To Symbol Table" );
    pe.LUINT32( "Number Of Symbols" );
    pe.LUINT16( "Size Of OP Header" );
    pe.LUINT16( "Characteristics" );

    pe.setEvent(this::peInfo);
    
    return( pe );
  }

  //************************************************READ OP HEADER********************************************

  public Descriptor readOP() throws IOException
  {
    Descriptor op = new Descriptor(file);

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
    op.LUINT16( "Major binary Version" );
    op.LUINT16( "Minor binary Version" );
    op.LUINT16( "Major Sub system Version" );
    op.LUINT16( "Minor Sub system Version" );

    op.LUINT32( "Win 32 Version Value" );
    op.LUINT32( "Size Of binary" );
    op.LUINT32( "Size Of Headers" );
    op.LUINT32( "Check Sum" );

    op.LUINT16( "Sub system" );
    op.LUINT16( "Dll Characteristics" );

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

    op.LUINT32( "Loader Flags" );
    op.LUINT32( "Data Directory Array Size" ); DDS = ((Integer)op.value).intValue();

    op.setEvent(this::opInfo);

    //If op header was read properly.

    Data.error = !( OPS[0] == 0x0B && OPS[1] == 0x01 ) && !is64bit; return( op );
  }

  //************************************************READ Data Directory Array********************************************
  //Each section is given in virtual address position if used. Sections that are not used have a virtual address of 0.
  //The next header defines the sections that are to be read and placed in ram memory.

  public Descriptor readDataDirectory() throws IOException
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

    Descriptor dd = new Descriptor(file);

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

  public Descriptor readSections() throws IOException
  {    
    long virtualSize = 0, virtualOffset = 0, size = 0, offset = 0;

    //Create table data.

    Descriptor sd = new Descriptor(file);

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

      sd.LUINT32( "Section flags" );

      //Add virtual address to IO system.

      file.addV( offset, size, virtualOffset + imageBase, virtualSize );
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

  public static final String MZReloc = "The DOS relocations are a list of 16 bit numbers. The numbers are Offsets that are added to by the position the program is put in memory.<br /><br />" +
  "In 16 bit MS-DOS, this allowed more than one program to be loaded.<br /><br />" +
  "Relocations are common in 16Bit, or 32Bit x86. However, 64bit x86 machine code uses relative addresses.";

  public static final String FSize = "Both \"Last 512 bytes\", and \"512 bytes in file\" are used to calculate the MS-DOS binary size.<br /><br />";

  public static final String[] MZInfo = new String[]{"<html>The signature must always be 4D 5A = MZ.<br /><br />" + 
  "It must be at the start of any windows binary.<br /><br />" +
  "If the file does not pass this test. Then it is corrupted.<br /><br />" + 
  "Or is a different file type disguise as a windows binary.</html>",
  "<html>"+ FSize + "If this value is zero, that means the entire last multiple of 512 is used (i.e. the effective value is 512).</html>",
  "<html>" + FSize + "The size of the program in 512 bytes. Subtract this value by 1, multiple by 512, and add \"Last 512 bytes\".</html>",
  "<html>Number of relocation entries stored after the header. May be zero.<br /><br />" + MZReloc + "</html>",
  "<html>The size of this MZ header. Multiply this value by 16 to get it's actual size.<br /><br />" +
  "The program begins just after the header, and this field can be used to calculate the appropriate file offset.<br /><br />" +
  "Note that the header size includes the relocation entries.</html>",
  "<html>Multiply this value by 16 for the minium amount of memory this application needs.<br /><br />The program can't be loaded if there isn't at least this much memory available to it.</html>",
  "<html>Multiply this value by 16, for additional memory.<br /><br />Normally, the OS reserves all the remaining conventional memory for your program, but you can limit it with this field.</html>",
  "<html>" + sseg + stack + "</html>",
  "<html>" + sseg + stack + "</html>",
  "<html>If set properly, the 16-bit sum of all words in the file should be zero.<br /><br />Usually, this isn't filled in.</html>",
  "<html>" + cseg + Instruct + "</html>",
  "<html>" + cseg + Instruct + "</html>",
  "<html>Offset of the first relocation item in the file.<br /><br />" + MZReloc + "</html>",
  "<html>Normally zero, meaning that it's the main program.</html>",
  "<html>" + res + "</html>",
  "",
  "",
  "<html>" + res + "</html>",
  "<html>" + res + "<br /><br />Instead of adding to DOS. Microsoft created a new system that uses the reserved section to locate to the PE header.</html>"};

  public void mzInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>This is the original DOS header. Which must be at the start of all windows binary files.<br /><br />Today the reserved bytes are used to locate to the new Portable executable header format.<br /><br />" +
        "However, on DOS this header still loads as the reserved bytes that locate to the PE header do nothing in DOS.<br /><br />Thus the small 16 bit binary at the end will run. " +
        "Which normally contains a small 16 bit code that prints the message that this program can not be run in DOS mode.</html>");
    }
    else
    {
      info( MZInfo[ el ] );
    }
  }

  //Detailed description of the MZ Relocations.

  public void mzRelocInfo( int el )
  {
    info("<html>Segment is multiplied by 16 plus the offset to forum the address location.<br /><br />" +
    "If the program can not load at it's set location in MZ header. Then the difference is added to the defined locations in the relocation list.<br /><br />" +
    "The segment register is always part of the address in 16bit x86. A Segment allowed us to use more than 64 kilobytes of memory.<br /><br />" +
    "The segment also worked as a way of separating data, and programs in memory. Segment is 0 plus an offset, for programs smaller than 64 kilobytes in size.</html>");
  }

  //Detailed description of the PE header.

  public static final String symbols = "Lines of code are changed to machine code. Symbols are line numbers relative to the generated machine code start-end positions.<br /><br />It allows us to see our source code line number when a problem happens in the binary file CPU instructions.<br /><br />";

  public static final String Debug = "This value should be zero for an binary, because debugging information is usually removed.<br /><br />Takes up extra space, and makes it even easier to reconstruct the original source code.";

  public static final String[] PEInfo = new String[]{"<html>The PE header must start with PE = 50 45 00 00.<br /><br />If it does not pass the signature test then the windows binary is corrupted.</html>",
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
  "<html>The Date this binary was created.<br /><br />The date time stamp is in seconds. The seconds are added to the starting date \"00:00 January 1, 1970\".<br /><br />" +
  "If the time date stamp is \"37\" in value, then it is plus 37 second giving \"00:37 January 1, 1970\".<br /><br />" +
  "The time date stamp is defined in UTC time, so it may be a day different in time, or few hours different depending on your time zone.</html>",
  "<html>" + symbols + "The file offset of the symbol table, or zero if no symbol table is present.<br /><br />"+ Debug +"</html>",
  "<html>" + symbols + "The number of entries in the symbol table.<br /><br />This data can be used to locate the string table, which immediately follows the symbol table.<br /><br />" + Debug + "</html>",
  "<html>The size of the optional header. Which is read after the PE header.</html>",
  "<html>The flags that indicate the attributes of the file.<br /><br />" +
  "Each binary digit that is set 1 represents a setting.<br /><br />" +
  "The binary value 0001000000100000 is the tow settings \"Application can handle > 2-GB addresses.\", and \"The binary file is a system file, not a user program.\".<br /><br />" +
  "Set data inspector to binary, and use the following table to adjust the settings, or to read them.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>Value</td><td>Use</td></tr>" +
  "<tr><td>0000000000000001</td><td>Windows CE, and Microsoft Windows NT and later. This indicates that the file does not contain base relocations and must therefore be loaded at its preferred base address.</td></tr>" +
  "<tr><td>0000000000000010</td><td>This indicates that the binary file is valid and can be run. If this flag is not set, it indicates a linker error.</td></tr>" +
  "<tr><td>0000000000000100</td><td>Debug line numbers have been removed. This flag is deprecated and should be zero.</td></tr>" +
  "<tr><td>0000000000001000</td><td>Debug symbol table entries have been removed. This flag is deprecated and should be zero.</td></tr>" +
  "<tr><td>0000000000010000</td><td>Aggressively trim working set. This flag is deprecated for Windows 2000 and later and must be zero. Obsolete.</td></tr>" +
  "<tr><td>0000000000100000</td><td>Application can handle bigger than 2-GB addresses.</td></tr>" +
  "<tr><td>0000000001000000</td><td>This flag is reserved for future use.</td></tr>" +
  "<tr><td>0000000010000000</td><td>Binary is little endian instead of big endian. This flag is deprecated and should be zero.</td></tr>" +
  "<tr><td>0000000100000000</td><td>Machine is based on a 32-bit-word architecture.</td></tr>" +
  "<tr><td>0000001000000000</td><td>Debugging information is removed from the binary file.</td></tr>" +
  "<tr><td>0000010000000000</td><td>If the binary is running on removable media, then copy it to the swap file.</td></tr>" +
  "<tr><td>0000100000000000</td><td>If the binary is running on network, then copy it to the swap file.</td></tr>" +
  "<tr><td>0001000000000000</td><td>The binary file is a system file, not a user program.</td></tr>" +
  "<tr><td>0010000000000000</td><td>The binary file is a DLL file. Such files are considered executable files for almost all purposes, although they cannot be directly run.</td></tr>" +
  "<tr><td>0100000000000000</td><td>The file should be run only on a uniprocessor machine.</td></tr>" +
  "<tr><td>1000000000000000</td><td>Binary is big endian instead of little endian. This flag is deprecated and should be zero.</td></tr>" +
  "</table></html>"
  };

  public void peInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>The PE header marks the start of the new Executable format. If the file is not loaded in DOS.<br /><br />" +
        "This header specifies the number of sections to map in virtual space. The processor type, and date of compilation.</html>");
    }
    else { info( PEInfo[ el ] ); }
  }

  //Detailed description of the OP header.

  public static final String Ver = "Major, and Minor are put together to forum the version number.<br /><br />Example.<br /><br />Major version = 5<br /><br />Minor version = 12<br /><br />Would mean version 5.12V.";
  
  public static final String[] OPInfo = new String[]{"<html>The Optional header has three different possible signatures.<br /><br />" +
  "0B 01 = 32 Bit binary.<br /><br />0B 02 = 64 Bit binary<br /><br />07 01 = ROM Image file.<br /><br />" +
  "The only time the OP header changes format is the 64 bit version of the Header.<br /><br />" +
  "If this section does not test true, for any of the three signatures, then the file is corrupted.</html>",
  "<html>" + Ver + "<br /><br />The linker links the sections together into a EXE, or DLL.</html>",
  "<html>" + Ver + "<br /><br />The linker links the sections together into a EXE, or DLL.</html>",
  "<html>Adding this to \"Base of code\" marks the end of the machine code. Plus the \"Base Address\".</html>",
  "<html>The size of the initialized data section, or the sum of all such sections if there are multiple data sections.</html>",
  "<html>The size of the uninitialized data section (BSS), or the sum of all such sections if there are multiple BSS sections.</html>",
  "<html>Start of the binaries machine code in virtual space. Plus the \"Base Address\".</html>",
  "<html>The beginning of the machine code section. Plus the \"Base Address\".<br /><br />The start position does not have to be at the very start of the machine code section.</html>",
  "<html>The Data section is a safe spot to put results from operations without writing over program machine code.<br /><br />In code these are called variables.</html>",
  "<html>Base address is added to all virtual addresses.<br /><br />It is the preferred address to load the mapped sections in RAM from this file.<br /><br />Windows may add to this number to space programs apart in virtual space.</html>",
  "<html>The alignment (in bytes) of sections when they are loaded into memory. It must be greater than or equal to FileAlignment. The default is the page size for the architecture.</html>",
  "<html>The alignment factor (in bytes) that is used to align the raw data of sections in the binary file.<br /><br />The value should be a power of 2 between 512 and 64 K, inclusive.<br /><br />" +
  "The default is 512. If the SectionAlignment is less than the architecture's page size, then FileAlignment must match SectionAlignment.</html>",
  "<html>" + Ver + "<br /><br />The version number of the required operating system.</html>",
  "<html>" + Ver + "<br /><br />The version number of the required operating system.</html>",
  "<html>" + Ver + "<br /><br />The version number of this file.</html>",
  "<html>" + Ver + "<br /><br />The version number of this file.</html>",
  "<html>" + Ver + "<br /><br />The subsystem version.</html>",
  "<html>" + Ver + "<br /><br />The subsystem version.</html>",
  "<html>Reserved for future use, must be set zero.<br /><br />" + res + "</html>",
  "<html>The size of this file.</html>",
  "<html>The size of the headers, for setting up the virtual space of this binary. Excluding the rest of the data.</html>",
  "<html>The algorithm for computing the checksum is incorporated into IMAGHELP.DLL.<br /><br />" +
  "The following are checked for validation at load time: all drivers, any DLL loaded at boot time, and any DLL that is loaded into a critical Windows process.</html>",
  "<html>The subsystem does not change how the application runs.<br /><br />" +
  "It is compiler specific identifers. It makes it easy to identify the intended purpose of the binary file, or where it came from.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>Value</td><td>Use</td></tr>" +
  "<tr><td>00 00</td><td>An unknown subsystem.</td></tr>" +
  "<tr><td>01 00</td><td>Device drivers and native Windows processes.</td></tr>" +
  "<tr><td>02 00</td><td>The Windows graphical user interface (GUI) subsystem.</td></tr>" +
  "<tr><td>03 00</td><td>The Windows character subsystem.</td></tr>" +
  "<tr><td>05 00</td><td>The OS/2 character subsystem.</td></tr>" +
  "<tr><td>07 00</td><td>The Posix character subsystem.</td></tr>" +
  "<tr><td>08 00</td><td>Native Win9x driver.</td></tr>" +
  "<tr><td>09 00</td><td>Windows CE.</td></tr>" +
  "<tr><td>0A 00</td><td>An Extensible Firmware Interface (EFI) application.</td></tr>" +
  "<tr><td>0B 00</td><td>An EFI driver with boot services.</td></tr>" +
  "<tr><td>0C 00</td><td>An EFI driver with run-time services.</td></tr>" +
  "<tr><td>0D 00</td><td>An EFI ROM image.</td></tr>" +
  "<tr><td>0E 00</td><td>XBOX</td></tr>" +
  "<tr><td>0F 00</td><td>Windows boot application.</td></tr>" +
  "</table>",
  "Each binary digit that is set 1 represents a setting.<br /><br />" +
  "The binary value 0010000100000000 is the tow settings \"A WDM driver\", and \"binary is NX compatible\".<br /><br />" +
  " Set data inspector to binary, and use the following table to adjust the settings, or to read them.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>Value</td><td>Use</td></tr>" +
  "<tr><td>0000000000000001</td><td>Reserved for future use, must be set zero.</td></tr>" +
  "<tr><td>0000000000000010</td><td>Reserved for future use, must be set zero.</td></tr>" +
  "<tr><td>0000000000000100</td><td>Reserved for future use, must be set zero.</td></tr>" +
  "<tr><td>0000000000001000</td><td>Reserved for future use, must be set zero.</td></tr>" +
  "<tr><td>0000000000100000</td><td>Binary can handle a high entropy 64-bit virtual address space.</td></tr>" +
  "<tr><td>0000000001000000</td><td>DLL can be relocated at load time.</td></tr>" +
  "<tr><td>0000000010000000</td><td>Code Integrity checks are enforced.</td></tr>" +
  "<tr><td>0000000100000000</td><td>Binary is NX compatible.</td></tr>" +
  "<tr><td>0000001000000000</td><td>Isolation aware, but do not isolate the binary.</td></tr>" +
  "<tr><td>0000010000000000</td><td>Does not use structured exception (SE) handling. No SE handler may be called in this binary.</td></tr>" +
  "<tr><td>0000100000000000</td><td>Do not bind the binary.</td></tr>" +
  "<tr><td>0001000000000000</td><td>Binary must execute in an AppContainer.</td></tr>" +
  "<tr><td>0010000000000000</td><td>A WDM driver.</td></tr>" +
  "<tr><td>0100000000000000</td><td>Binary supports Control Flow Guard.</td></tr>" +
  "<tr><td>1000000000000000</td><td>Terminal Server aware.</td></tr>" +
  "</table>",
  "<html>The size of the stack to reserve. Only SizeOfStackCommit is committed; the rest is made available one page at a time until the reserve size is reached.</html>",
  "<html>The size of the stack to commit.</html>",
  "<html>The size of the local heap space to reserve. Only SizeOfHeapCommit is committed; the rest is made available one page at a time until the reserve size is reached.</html>",
  "<html>The size of the local heap space to commit.</html>",
  "<html>Reserved for future use, must be set zero.<br /><br />" + res + "</html>",
  "<html>Data Directory Array can be made bigger than it's default size 16.<br /><br />Which allows for more features to be added to the windows application format.</html>"};

  public void opInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>At the end of the PE header is the start of the Optional header. However, this header is not optional.</html>");
    }
    else { info( OPInfo[ el >= 8 && is64bit ? el + 1 : el ] ); }
  }

  //Detailed description of the data Directory Array.

  public static final String[] DDInfo = new String[]{"<html>Array element consisting of two 32 bit values.</html>",
  "<html>Virtual Address of section.<br /><br />Plus the programs base address. The Base address is defined in OP header.</html>",
  "<html>Size of section data.</html>"};

  public void ddInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>This is the Data directory array section of the OP header. Every element has a different use.<br /><br />The virtual address positions are useless without setting up the mapped sections after the array.<br /><br />" +
        "The virtual addresses are added to the programs \"Base Address\". The \"Base Address\" is defined by the OP header.<br /><br />Anything that is 0, is not used.</html>");
    }
    else { info( DDInfo[ el % 3 ] ); }
  }

  //Detailed description of the sections to RAM memory.

  public static final String[] SInfo = new String[]{"<html>Array element consisting of A section name, and some 32 bit values, for the location to put the data in memory.</html>",
  "<html>The 8 bytes can be given any text based name you like. It is not used for anything by the system.<br /><br />" +
  "The names can be very deceiving. As x86 compilers can compile out the code section giving it a \".text\" name.<br /><br />" +
  "Don't worry about the names. The data Directory Array defines what each section is after it is in virtual space.<br /><br />" +
  "Thus the OP header marks the machine code in it's \"Start of code\" value. Which is a virtual address position.</html>",
  "<html>Number of bytes to put in virtual space. This reflects the sections actual size.<br /><br />As number of bytes read from file may be padded by the linker that linked the section together.</html>",
  "<html>The virtual address is added to the programs \"Base Address\".<br /><br />The programs \"Base Address\" is defined by the OP header.</html>",
  "<html>Number of bytes to read from file.<br /><br />The number of bytes read, may not all be put in RAM. If Number of bytes to put in virtual space is smaller.<br /><br />This happens, because sections are aligned in multiples by the linker.</html>",
  "<html>The position of the file to read.</html>",
  "<html>" + res + "</html>",
  "Each binary digit that is set 1 represents a setting except the \"Align data\" setting.<br /><br />" +
  "The binary value 00000100001100000000000000000000 is the tow settings \"Align data on a 4-byte boundary\", and \"The section cannot be cached\".<br /><br />" +
  "There can only be one \"Align data\" setting, as it is a number combination.<br /><br />It is used during compiling your binary in order to line up the sections in even multiples.<br /><br />" +
  "The alignment setting is not used by the actual binary, or DLL.<br /><br />" +
  "Set data inspector to binary, and use the following table to adjust the settings, or to read them.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>Value</td><td>Use</td></tr>" +
  "<tr><td>00000000000000000000000000000001</td><td>Reserved for future use.</td></tr>" +
  "<tr><td>00000000000000000000000000000010</td><td>Reserved for future use.</td></tr>" +
  "<tr><td>00000000000000000000000000000100</td><td>Reserved for future use.</td></tr>" +
  "<tr><td>00000000000000000000000000001000</td><td>The section should not be padded to the next boundary. This flag is obsolete and is replaced by \"Align data\". This is valid only for object files.</td></tr>" +
  "<tr><td>00000000000000000000000000010000</td><td>Reserved for future use.</td></tr>" +
  "<tr><td>00000000000000000000000000100000</td><td>The section contains executable code.</td></tr>" +
  "<tr><td>00000000000000000000000001000000</td><td>The section contains initialized data.</td></tr>" +
  "<tr><td>00000000000000000000000010000000</td><td>The section contains uninitialized data.</td></tr>" +
  "<tr><td>00000000000000000000000100000000</td><td>Reserved for future use.</td></tr>" +
  "<tr><td>00000000000000000000001000000000</td><td>The section contains comments or other information. The .drectve section has this type. This is valid for object files only.</td></tr>" +
  "<tr><td>00000000000000000000010000000000</td><td>Reserved for future use.</td></tr>" +
  "<tr><td>00000000000000000000100000000000</td><td>The section will not become part of the binary. This is valid only for object files.</td></tr>" +
  "<tr><td>00000000000000000001000000000000</td><td>The section contains COMDAT data.</td></tr>" +
  "<tr><td>00000000000000000010000000000000</td><td>The section contains data referenced through the global pointer (GP).</td></tr>" +
  "<tr><td>00000000000000000100000000000000</td><td>Reserved for future use.</td></tr>" +
  "<tr><td>00000000000000001000000000000000</td><td>Reserved for future use.</td></tr>" +
  "<tr><td>00000000000000010000000000000000</td><td>Reserved for future use.</td></tr>" +
  "<tr><td>00000000000000100000000000000000</td><td>Reserved for future use.</td></tr>" +
  "<tr><td>00000000000001000000000000000000</td><td>Align data on a 1-byte boundary. Valid only for object files.</td></tr>" +
  "<tr><td>00000000000010000000000000000000</td><td>Align data on a 2-byte boundary. Valid only for object files.</td></tr>" +
  "<tr><td>00000000000011000000000000000000</td><td>Align data on a 4-byte boundary. Valid only for object files.</td></tr>" +
  "<tr><td>00000000000100000000000000000000</td><td>Align data on an 8-byte boundary. Valid only for object files.</td></tr>" +
  "<tr><td>00000000000101000000000000000000</td><td>Align data on a 16-byte boundary. Valid only for object files.</td></tr>" +
  "<tr><td>00000000000110000000000000000000</td><td>Align data on a 32-byte boundary. Valid only for object files.</td></tr>" +
  "<tr><td>00000000000111000000000000000000</td><td>Align data on a 64-byte boundary. Valid only for object files.</td></tr>" +
  "<tr><td>00000000001000000000000000000000</td><td>Align data on a 128-byte boundary. Valid only for object files.</td></tr>" +
  "<tr><td>00000000001001000000000000000000</td><td>Align data on a 256-byte boundary. Valid only for object files.</td></tr>" +
  "<tr><td>00000000001010000000000000000000</td><td>Align data on a 512-byte boundary. Valid only for object files.</td></tr>" +
  "<tr><td>00000000001011000000000000000000</td><td>Align data on a 1024-byte boundary. Valid only for object files.</td></tr>" +
  "<tr><td>00000000001100000000000000000000</td><td>Align data on a 2048-byte boundary. Valid only for object files.</td></tr>" +
  "<tr><td>00000000001101000000000000000000</td><td>Align data on a 4096-byte boundary. Valid only for object files.</td></tr>" +
  "<tr><td>00000000001110000000000000000000</td><td>Align data on an 8192-byte boundary. Valid only for object files.</td></tr>" +
  "<tr><td>00000001000000000000000000000000</td><td>The section contains extended relocations.</td></tr>" +
  "<tr><td>00000010000000000000000000000000</td><td>The section can be discarded as needed.</td></tr>" +
  "<tr><td>00000100000000000000000000000000</td><td>The section cannot be cached.</td></tr>" +
  "<tr><td>00001000000000000000000000000000</td><td>The section is not pageable.</td></tr>" +
  "<tr><td>00010000000000000000000000000000</td><td>The section can be shared in memory.</td></tr>" +
  "<tr><td>00100000000000000000000000000000</td><td>The section can be executed as code.</td></tr>" +
  "<tr><td>01000000000000000000000000000000</td><td>The section can be read.</td></tr>" +
  "<tr><td>10000000000000000000000000000000</td><td>The section can be written to.</td></tr>" +
  "</table>"
  };

  public void sdInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>Number of sections to read was defined in the OP header.<br /><br />The virtual address positions are useless without setting up the mapped sections.<br /><br />" +
        "The virtual addresses are added to the programs \"Base Address\". The \"Base Address\" is defined by the OP header.</html>");
    }
    else { info( SInfo[ el % 8 ] ); }
  }
}