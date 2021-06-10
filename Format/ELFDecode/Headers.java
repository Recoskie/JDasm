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

  public Descriptor readProgram() throws IOException
  {
    file.seek( programHeader ); Descriptor prh = new Descriptor( file );

    for( int i = 0; i < prSize; i++ )
    {
      prh.Array("Program entire " + i + "", elPrSize );

      if( isLittle )
      {
        prh.LUINT32("Type");

        if( is64Bit )
        {
          prh.LUINT32("flag 64");
          prh.LUINT64("Offset");
          prh.LUINT64("Virtual");
          prh.LUINT64("Physical Address");
          prh.LUINT64("Section size");
          prh.LUINT64("Size in memory");
          prh.LUINT64("Alignment");
        }
        else
        {
          prh.LUINT32("Offset");
          prh.LUINT32("Virtual");
          prh.LUINT32("Physical Address");
          prh.LUINT32("Section size");
          prh.LUINT32("Size in memory");
          prh.LUINT32("flag 32");
          prh.LUINT32("Alignment");
        }
      }
      else
      {
        prh.UINT32("Type");

        if( is64Bit )
        {
          prh.UINT32("flag 64");
          prh.UINT64("Offset");
          prh.UINT64("Virtual");
          prh.UINT64("Physical Address");
          prh.UINT64("Section size");
          prh.UINT64("Size in memory");
          prh.UINT64("Alignment");
        }
        else
        {
          prh.UINT32("Offset");
          prh.UINT32("Virtual");
          prh.UINT32("Physical Address");
          prh.UINT32("Section size");
          prh.UINT32("Size in memory");
          prh.UINT32("flag 32");
          prh.UINT32("Alignment");
        }
      }
    }
      
    return( prh );
  }

  //*********************************Reads the Section header***********************************

  class sect
  {
    long virtual, offset, size, name;

    public sect()
    {

    }
  }

  public Descriptor[] readSections( JDNode Sec ) throws IOException
  {
    JDNode tNode;

    sect s;

    java.util.LinkedList<Descriptor> des = new java.util.LinkedList<Descriptor>();
    java.util.LinkedList<sect> st = new java.util.LinkedList<sect>();

    Descriptor sec, Name;

    //Now we dump all sections excluding the name section we just dumped.

    file.seek( Sections ); sec = new Descriptor( file ); des.add( sec );

    for( int i = 0; i < secSize; i++ )
    {
      s = new sect();

      sec.Array("Section entire " + i + "", elSecSize );

      if( is64Bit )
      {
        if( isLittle )
        {
          sec.LUINT32("Entire Name Location"); s.name = (Integer)sec.value;
          sec.LUINT32("Section Type");
          sec.LUINT64("flag 64");
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
          sec.UINT32("Entire Name Location"); s.name = (Integer)sec.value;
          sec.UINT32("Section Type");
          sec.UINT64("flag 64");
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
          sec.LUINT32("Entire Name Location"); s.name = (Integer)sec.value;
          sec.LUINT32("Section Type");
          sec.LUINT32("flag 32");
          sec.LUINT32("Virtual"); s.virtual = ((Integer)sec.value).longValue();
          sec.LUINT32("Offset"); s.offset = ((Integer)sec.value).longValue();
          sec.LUINT32("Section Size"); s.size = ((Integer)sec.value).longValue();
          sec.LUINT32("LINK");
          sec.LUINT32("INFO");
          sec.LUINT32("Alignment");
          sec.LUINT32("Entire Size");
        }
        else
        {
          sec.UINT32("Entire Name Location"); s.name = (Integer)sec.value;
          sec.UINT32("Section Type");
          sec.UINT32("flag 64");
          sec.UINT32("Virtual"); s.virtual = ((Integer)sec.value).longValue();
          sec.UINT32("Offset"); s.offset = ((Integer)sec.value).longValue();
          sec.UINT32("Section Size"); s.size = ((Integer)sec.value).longValue();
          sec.UINT32("LINK");
          sec.UINT32("INFO");
          sec.UINT32("Alignment");
          sec.UINT32("Entire Size");
        }
      }

      file.addV( s.offset, s.size, s.virtual, s.size ); st.add(s);
    }

    //Create nodes for section data and names.

    for( int i = 0, i2 = 1; i < secSize; i++ )
    {
      s = st.get(i);
      
      if( s.name == 0 )
      {
        Sec.add( new JDNode( "No Name"+".h" ) );
      }
      else
      {
        file.seekV(s.name); Name = new Descriptor(file,true);
      
        Name.String8("Section name location", (byte)0x00); des.add(Name);

        tNode = new JDNode( Name.value + "", new long[]{ 1, i2 } ); tNode.add( new JDNode( "Section Data.h", new long[]{ -2, s.offset, s.virtual, s.size } ) );
      
        Sec.add( tNode ); i2 += 1;
      }
    }
      
    return( des.toArray( new Descriptor[ des.size() ] ) );
  }

  //Detailed description of the MZ header.

  public static final String[] ELFInfo = new String[]{"<html>The signature must always be 7F, 45, 4C, 46 = ELF.<br /><br />" + 
  "It must be at the start of any unix/linux binary.<br /><br />" +
  "If the file does not pass this test. Then it is corrupted.</html>",
  "<html>This byte is set 1 for 32 bit, or is set 2 for 64 bit.<br /><br />" +
  "Locations are read as 64 bit numbers instead of 32 bit numbers if 64 bit. This changes the ELF header size.<br /><br />" +
  "This also changes the section header size, and program header size.</html>",
  "<html>This byte is set 1, for little endian byte order, or is set 2 for big endian byte order.<br /><br />" +
  "This affects interpretation of multi-byte fields.</html>",
  "<html>Usually set to 1 for the original and current version of ELF.</html>",
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
  "<html>Application File type.</html>",
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
  "<html>Usually Set to 1 for the original version of ELF.</html>",
  "<html>This is the Virtual address that the program starts at.</html>",
  "<html>Location to the program information header.</html>",
  "<html>Location to the section header.</html>",
  "<html>Interpretation of this field depends on the CPU.</html>",
  "<html>The size of this ELF header.</html>",
  "<html>The size of each entry in the program header.<br /><br />Multiplying this by how many entries gives us the size of the program header.<br /><br />" +
  "These tow properties are used to read the Program header.</html>",
  "<html>The Number of entry in the program Header.<br /><br />Multiplying this by the size of each entire gives us the size of the program header.<br /><br />" +
  "These tow properties are used to read the Program header.</html>",
  "<html>The size of each entry in the section header.<br /><br />Multiplying this by how many entries gives us the size of the section header.<br /><br />" +
  "These tow properties are used to read the Section header.</html>",
  "<html>The Number of entry in the section Header.<br /><br />Multiplying this by the size of each entire gives us the size of the section header.<br /><br />" +
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
}
