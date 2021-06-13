package Format.ELFDecode;

import java.io.*;
import swingIO.*;
import swingIO.tree.JDNode;

public class Headers extends Data
{
  //*********************************creates the data of the ELF header***********************************

  public Descriptor readELF() throws IOException
  {
    Descriptor elf = new Descriptor( file );

    elf.String8( "SIGNATURE", 4 ); byte[] sig = ( elf.value + "" ).getBytes();
    elf.UINT8( "Is 64 bit" ); is64Bit = ((Byte)elf.value) > 1;
    elf.UINT8( "Byte Order" ); isLittle = ((Byte)elf.value) <= 1;
    elf.UINT8( "ELF Type" );
    elf.UINT8( "OS type" );
    elf.Other( "OS Version", 7 );
    elf.UINT8( "PAD" );
    
    if( isLittle )
    {
      elf.LUINT16( "File type" );
      elf.LUINT16( "Machine" ); coreType = ((Short)elf.value);
      elf.LUINT32( "ELF Version" );
    }
    else
    {
      elf.UINT16( "File type" );
      elf.UINT16( "Machine" ); coreType = ((Short)elf.value);
      elf.UINT32( "ELF Version" );
    }

    if( isLittle )
    {
      if( is64Bit )
      {
        elf.LUINT64( "Start Address" ); start = ((Long)elf.value);
        elf.LUINT64( "Program Header" ); programHeader = ((Long)elf.value);
        elf.LUINT64( "Sections" ); Sections = ((Long)elf.value);
      }
      else
      {
        elf.LUINT32( "Start Address" ); start = ((Integer)elf.value);
        elf.LUINT32( "Program Header" ); programHeader = ((Integer)elf.value);
        elf.LUINT32( "Sections" ); Sections = ((Integer)elf.value);
      }
    }
    else
    {
      if( is64Bit )
      {
        elf.UINT64( "Start Address" ); start = ((Long)elf.value);
        elf.UINT64( "Program Header" ); programHeader = ((Long)elf.value);
        elf.UINT64( "Sections" ); Sections = ((Long)elf.value);
      }
      else
      {
        elf.UINT32( "Start Address" ); start = ((Integer)elf.value);
        elf.UINT32( "Program Header" ); programHeader = ((Integer)elf.value);
        elf.UINT32( "Sections" ); Sections = ((Integer)elf.value);
      }
    }

    if( isLittle )
    {
      elf.LUINT32( "Flags" );
      elf.LUINT16( "Size of ELF header" );
      elf.LUINT16( "Program header entire size" ); elPrSize = (Short)elf.value;
      elf.LUINT16( "Entires in Program header" ); prSize = (Short)elf.value;
      elf.LUINT16( "Section header entire size" ); elSecSize = (Short)elf.value;
      elf.LUINT16( "Entries in section header" ); secSize = (short)elf.value;
      elf.LUINT16( "Section names" ); namesEl = (short)elf.value;
    }
    else
    {
      elf.UINT32( "Flags" );
      elf.UINT16( "Size of ELF header" );
      elf.UINT16( "Program header entire size" ); elPrSize = (Short)elf.value;
      elf.UINT16( "Entires in Program header" ); prSize = (Short)elf.value;
      elf.UINT16( "Section header entire size" ); elSecSize = (Short)elf.value;
      elf.UINT16( "Entries in section header" ); secSize = (short)elf.value;
      elf.UINT16( "Section names" ); namesEl = (short)elf.value;
    }

    elf.setEvent(this::elfInfo);

    //Check if ELF header was read properly.

    Data.error = !( sig[0] == 0x7F && sig[1] == 0x45 && sig[2] == 0x4C && sig[3] == 0x46 ); return( elf );
  }

  //*********************************Reads the Program header***********************************

  public Descriptor readProgram( JDNode Pr ) throws IOException
  {
    file.seek( programHeader ); Descriptor prh = new Descriptor( file ); prh.setEvent( this::prInfo );

    long offset = 0, flen = 0, virtual = 0, vlen = 0;

    int type = 0, flags = 0; //Used to organize sections.

    for( int i = 0; i < prSize; i++ )
    {
      prh.Array("Program entire " + i + "", elPrSize );

      if( isLittle )
      {
        prh.LUINT32("Type"); type = (int)prh.value;

        if( is64Bit )
        {
          prh.LUINT32("flag"); flags = (int)prh.value;
          prh.LUINT64("Offset"); offset = ((long)prh.value);
          prh.LUINT64("Virtual"); virtual = ((long)prh.value);
          prh.LUINT64("Physical Address");
          prh.LUINT64("Section size"); flen = ((long)prh.value);
          prh.LUINT64("Size in memory"); vlen = ((long)prh.value);
          prh.LUINT64("Alignment");
        }
        else
        {
          prh.LUINT32("Offset"); offset = ((int)prh.value);
          prh.LUINT32("Virtual"); virtual = ((int)prh.value);
          prh.LUINT32("Physical Address");
          prh.LUINT32("Section size"); flen = ((int)prh.value);
          prh.LUINT32("Size in memory"); vlen = ((int)prh.value);
          prh.LUINT32("flag"); flags = (int)prh.value;
          prh.LUINT32("Alignment");
        }
      }
      else
      {
        prh.UINT32("Type"); type = (int)prh.value;

        if( is64Bit )
        {
          prh.UINT32("flag"); flags = (int)prh.value;
          prh.UINT64("Offset"); offset = ((long)prh.value);
          prh.UINT64("Virtual"); virtual = ((long)prh.value);
          prh.UINT64("Physical Address");
          prh.UINT64("Section size"); flen = ((long)prh.value);
          prh.UINT64("Size in memory"); vlen = ((long)prh.value);
          prh.UINT64("Alignment");
        }
        else
        {
          prh.UINT32("Offset"); offset = ((int)prh.value);
          prh.UINT32("Virtual"); virtual = ((int)prh.value);
          prh.UINT32("Physical Address");
          prh.UINT32("Section size"); flen = ((int)prh.value);
          prh.UINT32("Size in memory"); vlen = ((int)prh.value);
          prh.UINT32("flag"); flags = (int)prh.value;
          prh.UINT32("Alignment");
        }
      }

      file.addV( offset, flen, virtual, vlen );

      Pr.add( new JDNode( "Program entire " + i + " (Data).h", new long[]{ -2, offset, virtual, flen } ) );

      //If section has runnable machine code instruction.

      if( ( flags & 1 ) == 1 ){ code.add( new JDNode("Program entire " + i + ".h", new long[]{ -1, virtual } ) ); }

      //If section is data.

      else if( type == 1 ){ data.add( new JDNode("Program entire " + i + ".h", new long[]{ -2, offset, virtual, flen } ) ); }

      //If section is link libraries.

      else if( type == 2 ){ lnk.add( new JDNode("Program entire " + i + ".h", new long[]{ 2, virtual, vlen } ) ); }
    }
      
    return( prh );
  }

  //*********************************Reads the Section header***********************************

  private class sect { long virtual, offset, size, name, flags; int type; }

  public Descriptor[] readSections( JDNode Sec ) throws IOException
  {
    java.util.LinkedList<Descriptor> des = new java.util.LinkedList<Descriptor>();
    java.util.LinkedList<sect> st = new java.util.LinkedList<sect>();

    Descriptor sec, Name;

    JDNode tNode;

    sect s;

    //Now we dump all sections to create The ELF Virtual space.

    file.seek( Sections ); sec = new Descriptor( file ); des.add( sec ); sec.setEvent( this::secInfo );

    for( int i = 0; i < secSize; i++ )
    {
      s = new sect(); sec.Array("Section entire " + i + "", elSecSize );

      if( is64Bit )
      {
        if( isLittle )
        {
          sec.LUINT32("Entire Name Location"); s.name = (int)sec.value;
          sec.LUINT32("Section Type"); s.type = (int)sec.value;
          sec.LUINT64("flags"); s.flags = (long)sec.value;
          sec.LUINT64("Virtual"); s.virtual = (long)sec.value;
          sec.LUINT64("Offset"); s.offset = (long)sec.value;
          sec.LUINT64("Section Size"); s.size = (long)sec.value;
          sec.LUINT32("LINK");
          sec.LUINT32("INFO");
          sec.LUINT64("Alignment");
          sec.LUINT64("Entire Size");
        }
        else
        {
          sec.UINT32("Entire Name Location"); s.name = (int)sec.value;
          sec.UINT32("Section Type"); s.type = (int)sec.value;
          sec.UINT64("flags"); s.flags = (long)sec.value;
          sec.UINT64("Virtual"); s.virtual = (long)sec.value;
          sec.UINT64("Offset"); s.offset = (long)sec.value;
          sec.UINT64("Section Size"); s.size = (long)sec.value;
          sec.UINT32("LINK");
          sec.UINT32("INFO");
          sec.UINT64("Alignment");
          sec.UINT64("Entire Size");
        }
      }
      else
      {
        if( isLittle )
        {
          sec.LUINT32("Entire Name Location"); s.name = (int)sec.value;
          sec.LUINT32("Section Type"); s.type = (int)sec.value;
          sec.LUINT32("flags"); s.flags = (int)sec.value;
          sec.LUINT32("Virtual"); s.virtual = (int)sec.value;
          sec.LUINT32("Offset"); s.offset = (int)sec.value;
          sec.LUINT32("Section Size"); s.size = (int)sec.value;
          sec.LUINT32("LINK");
          sec.LUINT32("INFO");
          sec.LUINT32("Alignment");
          sec.LUINT32("Entire Size");
        }
        else
        {
          sec.UINT32("Entire Name Location"); s.name = (int)sec.value;
          sec.UINT32("Section Type"); s.type = (int)sec.value;
          sec.UINT32("flags"); s.flags = (int)sec.value;
          sec.UINT32("Virtual"); s.virtual = (int)sec.value;
          sec.UINT32("Offset"); s.offset = (int)sec.value;
          sec.UINT32("Section Size"); s.size = (int)sec.value;
          sec.UINT32("LINK");
          sec.UINT32("INFO");
          sec.UINT32("Alignment");
          sec.UINT32("Entire Size");
        }
      }

      file.addV( s.offset, s.size, s.virtual, s.size ); st.add(s);
    }

    //Create nodes for section data, and names.

    for( int i = 0, i2 = 1; i < secSize; i++ )
    {
      s = st.get(i);
      
      if( s.name == 0 )
      {
        tNode = new JDNode( "No Name #" + i + ( s.size == 0 ? ".h" : "" ) );

        if( s.size > 0 ) { tNode.add( new JDNode( "Section Data.h", new long[]{ -2, s.offset, s.virtual, s.size } ) ); }

        Sec.add( tNode );
      }
      else
      {
        file.seekV(s.name); Name = new Descriptor(file,true); Name.setEvent( this::secName );
      
        Name.String8("Section name location", (byte)0x00); des.add(Name);

        tNode = new JDNode( Name.value + " #" + i + ( s.size == 0 ? ".h" : "" ), new long[]{ 1, i2 } );
        
        if( s.size > 0 ) { tNode.add( new JDNode( "Section Data.h", new long[]{ -2, s.offset, s.virtual, s.size } ) ); }
      
        Sec.add( tNode );
        
        //If section has runnable machine code instruction.

        if( ( s.flags & 4 ) == 4 ){ code.add( new JDNode( Name.value + ".h", new long[]{ -1, s.virtual } ) ); }

        //Program data.

        else if( s.type == 1 ){ data.add( new JDNode(Name.value + ".h", new long[]{ -2, s.offset, s.virtual, s.size } ) ); }

        //If section is link libraries.

        else if( s.type == 6 ){ lnk.add( new JDNode( Name.value + ".h", new long[]{ 2, s.virtual, s.size } ) ); }

        //Relocations.

        else if( s.type == 4 || s.type == 9 ){ reloc.add( new JDNode(Name.value + ".h", new long[]{ -2, s.offset, s.virtual, s.size } ) ); }

        //Debug.

        else if( s.type == 2 ){ debug.add( new JDNode(Name.value + ".h", new long[]{ -2, s.offset, s.virtual, s.size } ) ); }
        
        i2 += 1;
      }
    }
      
    return( des.toArray( new Descriptor[ des.size() ] ) );
  }

  //Detailed description of the ELF header.

  public static final String[] ELFInfo = new String[]{"<html>The signature must always be 7F, 45, 4C, 46 = ELF.<br /><br />" + 
  "It must be at the start of any unix/linux binary.<br /><br />" +
  "If the file does not pass this test. Then it is corrupted.</html>",
  "<html>This byte is set 1 for 32 bit, or is set 2 for 64 bit.<br /><br />" +
  "Locations are read as 64 bit numbers instead of 32 bit numbers if 64 bit. This changes the ELF header size.<br /><br />" +
  "This also changes the section header size, and program header size.</html>",
  "<html>This byte is set 1, for little endian byte order, or is set 2 for big endian byte order.<br /><br />" +
  "This affects interpretation of multi-byte fields.</html>",
  "<html>Usually set to 1 for the original and current version of ELF.<br /><br />" +
  "Higher version numbers may be eventually added.<br /><br />Note 0 is an invalid setting.</html>",
  "<html>Identifies the target operating system (It is often set to 0 regardless of the target platform).<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>Value</td><td>Operating System</td></tr>" +
  "<tr><td>00</td><td>System V</td></tr>" +
  "<tr><td>01</td><td>HP-UX</td></tr>" +
  "<tr><td>02</td><td>NetBSD</td></tr>" +
  "<tr><td>03</td><td>Linux</td></tr>" +
  "<tr><td>04</td><td>GNU Hurd</td></tr>" +
  "<tr><td>06</td><td>Solaris</td></tr>" +
  "<tr><td>07</td><td>AIX</td></tr>" +
  "<tr><td>08</td><td>IRIX</td></tr>" +
  "<tr><td>09</td><td>FreeBSD</td></tr>" +
  "<tr><td>0A</td><td>Tru64</td></tr>" +
  "<tr><td>0B</td><td>Novell Modesto</td></tr>" +
  "<tr><td>0C</td><td>OpenBSD</td></tr>" +
  "<tr><td>0D</td><td>OpenVMS</td></tr>" +
  "<tr><td>0E</td><td>NonStop Kernel</td></tr>" +
  "<tr><td>0F</td><td>AROS</td></tr>" +
  "<tr><td>10</td><td>Fenix OS</td></tr>" +
  "<tr><td>11</td><td>CloudABI</td></tr>" +
  "<tr><td>12</td><td>Stratus Technologies OpenVOS</td></tr>" +
  "</table></html>",
  "<html>The intended version of the OS this EFL is meant to run on.</html>",
  "<html>Currently unused, should be zero.</html>",
  "<html>Application File type.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>Value</td><td>Type</td></tr>" +
  "<tr><td>0000</td><td>An unknown type.</td></tr>" +
  "<tr><td>0001</td><td>A relocatable file.</td></tr>" +
  "<tr><td>0002</td><td>An executable file.</td></tr>" +
  "<tr><td>0003</td><td>A shared object.</td></tr>" +
  "<tr><td>0004</td><td>A core file.</td></tr>" +
  "<tr><td>FE00</td><td>Operating system specific.</td></tr>" +
  "<tr><td>FEFF</td><td>Operating system specific.</td></tr>" +
  "<tr><td>FF00</td><td>Processor specific.</td></tr>" +
  "<tr><td>FFFF</td><td>Processor specific.</td></tr>" +
  "</table>" +
  "</html>",
  "<html>The processor type the binary is meant to run natively on. Majority of linux/unix systems are Intel/AMD x86.<br /><br />" +
  "The tow settings you will see the most are 003E = 64bit x86, and 0003 = 32bit x86.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>Value</td><td>CPU</td></tr>" +
  "<tr><td>0001</td><td>AT&T WE 32100</td></tr>" +
  "<tr><td>0002</td><td>SPARC</td></tr>" +
  "<tr><td>0003</td><td>x86</td></tr>" +
  "<tr><td>0004</td><td>Motorola 68000 (M68k)</td></tr>" +
  "<tr><td>0005</td><td>Motorola 88000 (M88k)</td></tr>" +
  "<tr><td>0006</td><td>Intel MCU</td></tr>" +
  "<tr><td>0007</td><td>Intel 80860</td></tr>" +
  "<tr><td>0008</td><td>MIPS</td></tr>" +
  "<tr><td>0009</td><td>IBM_System/370</td></tr>" +
  "<tr><td>000A</td><td>MIPS RS3000 Little-endian</td></tr>" +
  "<tr><td>000E</td><td>Hewlett-Packard PA-RISC</td></tr>" +
  "<tr><td>0013</td><td>Intel 80960</td></tr>" +
  "<tr><td>0014</td><td>PowerPC</td></tr>" +
  "<tr><td>0015</td><td>PowerPC (64-bit)</td></tr>" +
  "<tr><td>0016</td><td>S390, including S390x</td></tr>" +
  "<tr><td>0017</td><td>IBM SPU/SPC</td></tr>" +
  "<tr><td>0024</td><td>NEC V800</td></tr>" +
  "<tr><td>0025</td><td>Fujitsu FR20</td></tr>" +
  "<tr><td>0026</td><td>TRW RH-32</td></tr>" +
  "<tr><td>0027</td><td>Motorola RCE</td></tr>" +
  "<tr><td>0028</td><td>ARM (up to ARMv7/Aarch32)</td></tr>" +
  "<tr><td>0029</td><td>Digital Alpha</td></tr>" +
  "<tr><td>002A</td><td>SuperH</td></tr>" +
  "<tr><td>002B</td><td>SPARC Version 9</td></tr>" +
  "<tr><td>002C</td><td>Siemens TriCore embedded processor</td></tr>" +
  "<tr><td>002D</td><td>Argonaut RISC Core</td></tr>" +
  "<tr><td>002E</td><td>Hitachi H8/300</td></tr>" +
  "<tr><td>002F</td><td>Hitachi H8/300H</td></tr>" +
  "<tr><td>0030</td><td>Hitachi H8S</td></tr>" +
  "<tr><td>0031</td><td>Hitachi H8/500</td></tr>" +
  "<tr><td>0032</td><td>IA-64</td></tr>" +
  "<tr><td>0033</td><td>Stanford MIPS-X</td></tr>" +
  "<tr><td>0034</td><td>Motorola ColdFire</td></tr>" +
  "<tr><td>0035</td><td>Motorola M68HC12</td></tr>" +
  "<tr><td>0036</td><td>Fujitsu MMA Multimedia Accelerator</td></tr>" +
  "<tr><td>0037</td><td>Semen PCP</td></tr>" +
  "<tr><td>0038</td><td>Sony nCPU embedded RISC processor</td></tr>" +
  "<tr><td>0039</td><td>Denso NDR1 microprocessor</td></tr>" +
  "<tr><td>003A</td><td>Motorola Star*Core processor</td></tr>" +
  "<tr><td>003B</td><td>Toyota ME16 processor</td></tr>" +
  "<tr><td>003C</td><td>STMicroelectronics ST100 processor</td></tr>" +
  "<tr><td>003D</td><td>Advanced Logic Corp. TinyJ embedded processor family</td></tr>" +
  "<tr><td>003E</td><td>AMD x86-64</td></tr>" +
  "<tr><td>008C</td><td>TMS320C6000 Family</td></tr>" +
  "<tr><td>00B7</td><td>ARM 64-bits (ARMv8/Aarch64)</td></tr>" +
  "<tr><td>00F3</td><td>RISC-V</td></tr>" +
  "<tr><td>00F7</td><td>Berkeley Packet Filter</td></tr>" +
  "<tr><td>0101</td><td>WDC 65C816</td></tr>" +
  "</table></html>",
  "<html>Usually Set to 1 for the original version of ELF.<br /><br />Note 0 is an invalid setting.</html>",
  "<html>This is the Virtual address that the program starts at.</html>",
  "<html>Location to the program header.<br /><br />" +
  "The program header sets up the link libraries. Dumps data used by the program. Also defines sections that must run before calling the program start address.</html>",
  "<html>Location to the section header.<br /><br />" +
  "The section header maps all sections of the program even program header entires with actual names.<br /><br />" +
  "The section header maps other things like debugging information. Which is not really necessary to make the program runnable.<br /><br />" +
  "The section header defines additional sections, of the program, while the program header are sections that are executed and run before the program starts.</html>",
  "<html>Interpretation of this field depends on the CPU.</html>",
  "<html>The size of this ELF header.</html>",
  "<html>The size of each entry in the program header.<br /><br />Multiplying this by how many entries gives us the size of the program header.<br /><br />" +
  "These tow properties are used to read the Program header.</html>",
  "<html>The Number of entries in the program Header.<br /><br />Multiplying this by the size of each entire gives us the size of the program header.<br /><br />" +
  "These tow properties are used to read the Program header.</html>",
  "<html>The size of each entry in the section header.<br /><br />Multiplying this by how many entries gives us the size of the section header.<br /><br />" +
  "These tow properties are used to read the Section header.</html>",
  "<html>The Number of entries in the section Header.<br /><br />Multiplying this by the size of each entire gives us the size of the section header.<br /><br />" +
  "These tow properties are used to read the Section header.</html>",
  "<html>The section header entire that contains the section names.</html>"
};

  public void elfInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>All Linux, and unix systems use ELF files, for dumping loading machine code programs in memory.</html>");
    }
    else
    {
      info( ELFInfo[ el ] );
    }
  }

  //Detailed description of the program header.

  public static final String[] PrInfo = new String[]{"<html>A program entire consisting of a location, and size of data, and type of data.<br /><br />" +
  "These sections specify important information. Such as dynamic link libraries needed to do a operation or function.<br /><br />" +
  "It also is used to load data into memory. The flag setting can specify if the data can be read, or wrote to while the program is running.<br /><br />" +
  "The flag settings can also specify if the section has runnable processor instructions with read, or write privileges.</html>",
  "<html>This value specifies the kind of data that is in this program section.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>Value</td><td>Data type</td></tr>" +
  "<tr><td>00000000</td><td>Program header table entry unused</td></tr>" +
  "<tr><td>00000001</td><td>Loadable segment (Data used by program instructions).</td></tr>" +
  "<tr><td>00000002</td><td>Dynamic linking information.</td></tr>" +
  "<tr><td>00000003</td><td>Interpreter information.</td></tr>" +
  "<tr><td>00000004</td><td>Auxiliary information.</td></tr>" +
  "<tr><td>00000005</td><td>Reserved, for future use.</td></tr>" +
  "<tr><td>00000006</td><td>Section contains the program header itself.</td></tr>" +
  "<tr><td>00000007</td><td>Thread-Local Storage.</td></tr>" +
  "<tr><td>6474E550</td><td>GCC .eh_frame_hdr.</td></tr>" +
  "<tr><td>6474E551</td><td>Indicates stack executability.</td></tr>" +
  "<tr><td>6474E552</td><td>Read-only after relocation.</td></tr>" +
  "<tr><td>60000000 to 6FFFFFFF</td><td>Reserved, for operating system.</td></tr>" +
  "<tr><td>70000000 to 7FFFFFFF</td><td>Reserved, for processor specific.</td></tr>" +
  "</table>",
  "<html>The flags value should be viewed in binary.<br /><br />" +
  "The value 00001111111100000000000000000011 means CPU can run it's bytes as instruction, is writable, and is OS specific.<br /><br />" +
  "The table bellow show the break down of what bits have to be set for each setting.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>00000000000000000000000000000001</td><td>This section can be run directly on CPU.</td></tr>" +
  "<tr><td>00000000000000000000000000000010</td><td>It is legal to write to this section.</td></tr>" +
  "<tr><td>00000000000000000000000000000100</td><td>It is legal to read bytes from the section.</td></tr>" +
  "<tr><td>00001111111100000000000000000000</td><td>OS specific.</td></tr>" +
  "<tr><td>11110000000000000000000000000000</td><td>Processor specific.</td></tr>" +
  "</table></html>",
  "<html>Start position for the data in File.</html>",
  "<html>The position to put the data in virtual space.</html>",
  "<html>On systems where physical address is relevant (no RAM).</html>",
  "<html>The size of the section from the Start position for the data in File..</html>",
  "<html>Size in memory. If this is bigger than the size read in file. The remaining data is set 0.</html>",
  "<html>0 and 1 specify no alignment. Otherwise should be a power of 2.</html>"
};

  public void prInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>The program header specifies dynamic link libraries needed to do a operation, or function.<br /><br />" +
      "It also is used to load data into memory, and to run small sections of code before calling the start address defined in the ELF header.<br /><br />" +
      "The flag settings can also specify if the section should be run immediately, because it has runnable processor instructions.<br /><br />" +
      "The flag settings can specify read, and write privileges of sections while the program is running.<br /><br />" +
      "The program header usually maps the \".init\" code section, and runs it before the program starts.</html>");
    }
    else
    {
      if(!is64Bit){ if( el >= 2 ) { el+=1; } el = ( el == 8 ? 2 : ( el > 8 ? el - 1 : el) ); } info( PrInfo[ el % 9 ] );
    }
  }

  //Detailed description of the Section header.

  public static final String[] SecInfo = new String[]{"<html>An array of sections containing different kinds of data.</html>",
  "<html>The Virtual address location to this sections name.</html>",
  "<html>This specifies the type of data is in this section.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>Value</td><td>Type</td></tr>" +
  "<tr><td>00000000</td><td>No data type specified.</td></tr>" +
  "<tr><td>00000001</td><td>Program data.</td></tr>" +
  "<tr><td>00000002</td><td>Symbol table.</td></tr>" +
  "<tr><td>00000003</td><td>String table.</td></tr>" +
  "<tr><td>00000004</td><td>Relocation entries with addends.</td></tr>" +
  "<tr><td>00000005</td><td>Symbol hash table.</td></tr>" +
  "<tr><td>00000006</td><td>Dynamic linking information.</td></tr>" +
  "<tr><td>00000007</td><td>Notes.</td></tr>" +
  "<tr><td>00000008</td><td>Program space with no data (bss).</td></tr>" +
  "<tr><td>00000009</td><td>Relocation entries, no addends.</td></tr>" +
  "<tr><td>0000000B</td><td>Dynamic linker symbol table.</td></tr>" +
  "<tr><td>0000000E</td><td>Array of constructors.</td></tr>" +
  "<tr><td>0000000F</td><td>Array of destructors.</td></tr>" +
  "<tr><td>00000010</td><td>Array of pre-constructors.</td></tr>" +
  "<tr><td>00000011</td><td>Section group.</td></tr>" +
  "<tr><td>00000012</td><td>Extended section indices.</td></tr>" +
  "<tr><td>60000000</td><td>Start OS-specific.</td></tr>" +
  "<tr><td>6FFFFFF5</td><td>Object attributes.</td></tr>" +
  "<tr><td>6FFFFFF6</td><td>GNU-style hash table.</td></tr>" +
  "<tr><td>6FFFFFF7</td><td>Prelink library list.</td></tr>" +
  "<tr><td>6FFFFFF8</td><td>Checksum for DSO content.</td></tr>" +
  "<tr><td>6FFFFFFD</td><td>Version definition section.</td></tr>" +
  "<tr><td>6FFFFFFE</td><td>Version needs section.</td></tr>" +
  "<tr><td>6FFFFFFF</td><td>Version symbol table.</td></tr>" +
  "<tr><td>70000000 to 7FFFFFFF</td><td>Processor specific.</td></tr>" +
  "<tr><td>80000000 to 8FFFFFFF</td><td>Application specific.</td></tr>" +
  "</table></html>",
  "<html>The flags value should be viewed in binary.<br /><br />" +
  "The value 00001111111100000000000000000101 means CPU can run it's bytes as instruction, is writable, and is OS specific.<br /><br />" +
  "The table bellow show the break down of what bits have to be set for each setting.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>00000000000000000000000000000001</td><td>It is legal to write to this section.</td></tr>" +
  "<tr><td>00000000000000000000000000000010</td><td>Occupies memory during execution.</td></tr>" +
  "<tr><td>00000000000000000000000000000100</td><td>This section can be run directly on CPU.</td></tr>" +
  "<tr><td>00000000000000000000000000010000</td><td>Might be merged.</td></tr>" +
  "<tr><td>00000000000000000000000000100000</td><td>Contains nul-terminated strings.</td></tr>" +
  "<tr><td>00000000000000000000000001000000</td><td>`sh_info' contains SHT index.</td></tr>" +
  "<tr><td>00000000000000000000000010000000</td><td>Preserve order after combining.</td></tr>" +
  "<tr><td>00000000000000000000000100000000</td><td>Non-standard OS specific handling required.</td></tr>" +
  "<tr><td>00000000000000000000001000000000</td><td>Section is member of a group.</td></tr>" +
  "<tr><td>00000000000000000000010000000000</td><td>Section holds local thread data.</td></tr>" +
  "<tr><td>00000000000000000000100000000000</td><td>Section with compressed data.</td></tr>" +
  "<tr><td>00001111111100000000000000000000</td><td>OS specific.</td></tr>" +
  "<tr><td>11110000000000000000000000000000</td><td>Processor specific.</td></tr>" +
  "</table></html>",
  "<html>The virtual address to put the section.</html>",
  "<html>The offset of section in the file.</html>",
  "<html>The size of the section to read and put in virtual space.</html>",
  "<html></html>",
  "<html></html>",
  "<html>0 and 1 specify no alignment. Otherwise should be a power of 2.</html>",
  "<html>Contains the size, in bytes, of each entry, for sections that contain fixed-size entries. Otherwise, this field contains zero.</html>"
};

  public void secInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>The section header defines the rest of the information of the program.<br /><br />" +
      "The sections in the section header are not run right away before the binary starts like the program header.<br /><br />" +
      "The section header can have relocations sections. Also symbol tables, for debuggers.<br /><br />" +
      "Also weather the section has processor instructions (callable functions, or methods), or is writable.<br /><br />" +
      "The section header may dump sections that have previously been run, or used by the program header such as the \".init\" section.<br /><br />" +
      "The sections have a name. In which the program header had no names.<br /><br />" +
      "Each section has a type setting, for what type of data it has. Similar to the program header.<br /><br />" +
      "After all sections are placed in memory. The defined start address is called that is defined in the ELF header.</html>");
    }
    else
    {
      info( SecInfo[ el % 11 ] );
    }
  }

  public void secName( int el )
  {
    info("<html>Sections are given default names by compilers, for what they are used for.<br /><br />" +
    "Bellow is a list of section names and what they are may be used for.<br /><br >" +
    "Also take note that a sections name does not have to reflect it's intended operation all the time." +
    "As the section type, and flag settings identify what the section does.<br /><br />" +
    "<table border=\"1\">"+
    "<tr><td>Section Name.</td><td>Use</td></tr>" +
    "<tr><td>.shstrtab</td><td>This section holds section names.</td></tr>" +
    "<tr><td>.init</td><td>This section holds executable instructions, for program initialization. Section is run before calling the main program entry point.</td></tr>" +
    "<tr><td>.text</td><td>This section holds executable instructions, for the main program entry.</td></tr>" +
    "<tr><td>.fini</td><td>This section holds executable instructions, for ending the program (exit/taskkill).</td></tr>" +
    "<tr><td>.note</td><td>This section holds various notes.</td></tr>" +
    "<tr><td>.rodata1</td><td>This section holds read-only data.</td></tr>" +
    "<tr><td>.bss</td><td>This section holds uninitialized data. The system initializes the data with zeros when the program begins to run.</td></tr>" +
    "<tr><td>.data</td><td>This section holds initialize data that contribute to the program's memory.</td></tr>" +
    "<tr><td>.data1</td><td>This section holds initialize data that contribute to the program's memory.</td></tr>" +
    "<tr><td>.line</td><td>This section holds line number information for symbolic debugging, which describes the correspondence between the program source and the machine code.</td></tr>" +
    "<tr><td>.comment</td><td>This section holds version control information.</td></tr>" +
    "<tr><td>.ctors</td><td>This section holds initialized pointers to the C++ constructor functions.</td></tr>" +
    "<tr><td>.debug</td><td>This section holds information for symbolic debugging.</td></tr>" +
    "<tr><td>.dtors</td><td>This section holds initialized pointers to the C++ destructor functions.</td></tr>" +
    "<tr><td>.dynamic</td><td>This section holds dynamic linking information.</td></tr>" +
    "<tr><td>.dynstr</td><td>This section holds strings needed for dynamic linking.</td></tr>" +
    "<tr><td>.dynsym</td><td>This section holds the dynamic linking symbol table.</td></tr>" +
    "<tr><td>.gnu.version</td><td>This section holds the version symbol array.</td></tr>" +
    "<tr><td>.gnu.version_d</td><td>This section holds the version symbol definitions array.</td></tr>" +
    "<tr><td>.gnu.version_r</td><td>This section holds the version symbol needed elements array.</td></tr>" +
    "<tr><td>.got</td><td>This section holds the global offset table.</td></tr>" +
    "<tr><td>.hash</td><td>This section holds a symbol hash table.</td></tr>" +
    "<tr><td>.interp</td><td>This section holds the pathname of a program interpreter.</td></tr>" +
    "<tr><td>.note.ABI-tag</td><td>This section is used to declare the expected run-time of the ELF image. It may include the operating system name and its run-time versions.</td></tr>" +
    "<tr><td>.note.gnu.build-id</td><td>This section is used to hold an ID that uniquely identifies the contents of the ELF image.  Different files with the same build ID should contain the same executable content.</td></tr>" +
    "<tr><td>.note.GNU-stack</td><td>This section is used in Linux object files for declaring stack attributes.</td></tr>" +
    "<tr><td>.note.openbsd.ident</td><td>OpenBSD native executables usually contain this section to identify themselves so the kernel can bypass any compatibility ELF binary emulation tests when loading the file.</td></tr>" +
    "<tr><td>.plt</td><td>This section holds the procedure linkage table.</td></tr>" +
    "<tr><td>.strtab</td><td>This section holds strings, most commonly the strings that represent the names associated with symbol table entries.</td></tr>" +
    "<tr><td>.symtab</td><td>This section holds a symbol table.</td></tr>" +
    "</table>" +
    "</html>");
  }
}
