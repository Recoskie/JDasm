package Format.MACDecode;

import swingIO.*;
import swingIO.tree.*;

public class LoadCMD extends Data
{
  public void load(JDNode n) throws java.io.IOException
  {
    long sectStart = file.getFilePointer();

    //Preparing to add the load commands.

    int cmd = 0, size = 0;

    for( int i = 0; i < loadCMD; i++ )
    {
      DTemp = new Descriptor( file );

      DTemp.LUINT32("Command"); cmd = (int)DTemp.value;
      DTemp.LUINT32("Size"); size = (int)DTemp.value - 8;

      if( size <= 0 ){ size = 0; }

      //Begin reading the command by type.

      if( cmd == 0x19 || cmd == 0x01 )
      {
        String name = "";

        DTemp.String8("Segment Name", 16 ); name = (String)DTemp.value;

        long address = 0, vSize = 0, offset = 0, oSize = 0;

        if( is64bit )
        {
          DTemp.LUINT64("Address"); address = (long)DTemp.value;
          DTemp.LUINT64("Size"); vSize = (long)DTemp.value;
          DTemp.LUINT64("File position"); offset = base + (long)DTemp.value;
          DTemp.LUINT64("Length"); oSize = (long)DTemp.value;
        }
        else
        {
          DTemp.LUINT32("Address"); address = (int)DTemp.value;
          DTemp.LUINT32("Size"); vSize = (int)DTemp.value;
          DTemp.LUINT32("File position"); offset = base + (int)DTemp.value;
          DTemp.LUINT32("Length"); oSize = (int)DTemp.value;
        }

        file.addV( offset, oSize, address, vSize );

        DTemp.LUINT32("maxvprot");
        DTemp.LUINT32("minvprot");
        DTemp.LUINT32("Number of sections"); int sect = (int)DTemp.value;
        DTemp.LUINT32("flags");

        DTemp.setEvent( this::segInfo );

        JDNode n2 = new JDNode( name + ( oSize > 0 ? "" : ".h" ), new long[] { 0, ref++ } ); des.add( DTemp );

        if( oSize > 0 ) { n2.add( new JDNode( name + " (Data).h", new long[] { -3, address, address + vSize - 1 } ) ); }

        for( int i2 = 0; i2 < sect; i2++ )
        {
          DTemp = new Descriptor( file ); DTemp.setEvent( this::sectInfo );

          DTemp.String8("Section Name", 16);
          
          JDNode t = new JDNode( DTemp.value + "", new long[] { 0, ref++ } ); des.add( DTemp );

          DTemp.String8("Segment Name", 16); name = (String)DTemp.value;

          if( is64bit )
          {
            DTemp.LUINT64("Address"); address = (long)DTemp.value;
            DTemp.LUINT64("Size"); vSize = (long)DTemp.value;
          }
          else
          {
            DTemp.LUINT32("Address"); address = (int)DTemp.value;
            DTemp.LUINT32("Size"); vSize = (int)DTemp.value;
          }

          DTemp.LUINT32("Offset"); offset = base + (int)DTemp.value;

          file.addV(offset, vSize, address, vSize);

          t.add( new JDNode( "Goto Data.h", new long[] { -3, address, address + vSize - 1 } ) );

          DTemp.LUINT32("Alignment");
          DTemp.LUINT32("Relocations Offset");
          DTemp.LUINT32("Relocations");
          DTemp.LUINT32("flags");
          DTemp.LUINT32("Reserved");
          DTemp.LUINT32("Reserved");
          
          if( is64bit ) { DTemp.LUINT32("Reserved"); }

          n2.add( t );
        }

        n.add( n2 );
      }
      else
      {
        DTemp.Other("Command Data", size ); DTemp.setEvent( this::cmdInfo ); des.add( DTemp );

        n.setArgs( new long[]{ -2, sectStart, file.getFilePointer() } );

        n.add( new JDNode( "CMD #" + i + ".h", new long[]{ 0, ref++ } ) );
      }
    }
  }

  public static final String cmdType = "The first 2 hex digits is the load command. If the last two hex digits are 80 this means the section is required for the program to run properly.<br /><br />" +
  "Majority of the section types are not necessary to load or run the program.<br /><br />" +
  "<table border='1'>" +
  "<tr><td>Command</td><td>Section</td></tr>" +
  "<tr><td>01</td><td>Segment of this file to be mapped.</td></tr>" +
  "<tr><td>02</td><td>Link-edit stab symbol table info.</td></tr>" +
  "<tr><td>03</td><td>Link-edit gdb symbol table info (obsolete).</td></tr>" +
  "<tr><td>04</td><td>Thread.</td></tr>" +
  "<tr><td>05</td><td>Unix thread (includes a stack).</td></tr>" +
  "<tr><td>06</td><td>Load a specified fixed VM shared library.</td></tr>" +
  "<tr><td>07</td><td>Fixed VM shared library identification.</td></tr>" +
  "<tr><td>08</td><td>Object identification info (obsolete).</td></tr>" +
  "<tr><td>09</td><td>Fixed VM file inclusion (internal use).</td></tr>" +
  "<tr><td>0A</td><td>Prepage command (internal use).</td></tr>" +
  "<tr><td>0B</td><td>Dynamic link-edit symbol table info.</td></tr>" +
  "<tr><td>0C</td><td>Load a dynamically linked shared library.</td></tr>" +
  "<tr><td>0D</td><td>Dynamically linked shared lib ident.</td></tr>" +
  "<tr><td>0E</td><td>Load a dynamic linker.</td></tr>" +
  "<tr><td>0F</td><td>Dynamic linker identification.</td></tr>" +
  "<tr><td>10</td><td>Modules prebound for a dynamically.</td></tr>" +
  "<tr><td>11</td><td>Image routines.</td></tr>" +
  "<tr><td>12</td><td>Sub framework.</td></tr>" +
  "<tr><td>13</td><td>Sub umbrella.</td></tr>" +
  "<tr><td>14</td><td>Sub client.</td></tr>" +
  "<tr><td>15</td><td>Sub library.</td></tr>" +
  "<tr><td>16</td><td>Two-level namespace lookup hints.</td></tr>" +
  "<tr><td>17</td><td>Prebind checksum.</td></tr>" +
  "<tr><td>18</td><td>Load a dynamically linked shared library that is allowed to be missing (all symbols are weak imported).</td></tr>" +
  "<tr><td>19</td><td>64-bit segment of this file to be mapped.</td></tr>" +
  "<tr><td>1A</td><td>64-bit image routines.</td></tr>" +
  "<tr><td>1B</td><td>The uuid.</td></tr>" +
  "<tr><td>1C</td><td>Runpath additions.</td></tr>" +
  "<tr><td>1D</td><td>Local of code signature.</td></tr>" +
  "<tr><td>1E</td><td>Local of info to split segments.</td></tr>" +
  "<tr><td>1F</td><td>Load and re-export dylib.</td></tr>" +
  "<tr><td>20</td><td>Delay load of dylib until first use.</td></tr>" +
  "<tr><td>21</td><td>Encrypted segment information.</td></tr>" +
  "<tr><td>22</td><td>Compressed dyld information.</td></tr>" +
  "<tr><td>23</td><td>Load upward dylib.</td></tr>" +
  "<tr><td>24</td><td>Build for MacOSX min OS version.</td></tr>" +
  "<tr><td>25</td><td>Build for iPhoneOS min OS version.</td></tr>" +
  "<tr><td>26</td><td>Compressed table of function start addresses.</td></tr>" +
  "<tr><td>27</td><td>String for dyld to treat like environment variable.</td></tr>" +
  "<tr><td>28</td><td>Replacement for LC_UNIXTHREAD.</td></tr>" +
  "<tr><td>29</td><td>Table of non-instructions in __text.</td></tr>" +
  "<tr><td>2A</td><td>Source version used to build binary</td></tr>" +
  "<tr><td>2B</td><td>Code signing DRs copied from linked dylibs.</td></tr>" +
  "<tr><td>2C</td><td>64-bit encrypted segment information.</td></tr>" +
  "<tr><td>2D</td><td>Linker options in MH_OBJECT files.</td></tr>" +
  "<tr><td>2E</td><td>Optimization hints in MH_OBJECT files.</td></tr>" +
  "<tr><td>2F</td><td>Build for AppleTV min OS version.</td></tr>" +
  "<tr><td>30</td><td>Build for Watch min OS version.</td></tr>" +
  "<tr><td>31</td><td>Arbitrary data included within a Mach-O file.</td></tr>" +
  "<tr><td>32</td><td>Build for platform min OS version.</td></tr>" +
  "<tr><td>33</td><td>Used with linkedit_data_command, payload is trie.</td></tr>" +
  "<tr><td>34</td><td>Used with linkedit_data_command.</td></tr>" +
  "<tr><td>35</td><td>Used with fileset_entry_command.</td></tr>" +
  "</table>";

  private static final String cmdSize = "<html>The size of the command and all it's parameters.</html>";

  private static final String[] cmdInfo = new String[]
  {
    cmdType, cmdSize, "<html>The commands values and parameters.</html>"
  };

  private static final String[] segInfo = new String[]
  {
    cmdType, cmdSize,
    "<html>Segment name. A Segment can be divided into smaller section names.</html>",
    "<html>Memory address to place the Segment in RAM memory.</html>",
    "<html>Number of bytes to place in RAM memory for this Segment.</html>",
    "<html>File position to the bytes that will be read and placed into RAM at the memory address.<br /><br />" +
    "If this is a universal binary then the offset is added to the start of the application in this file.</html>",
    "<html>The number of bytes to read from the file to be placed at the RAM address.<br /><br />" +
    "If this is smaller than the number of bytes to place in RAM for this Segment then the rest are 00 byte filled.<br /><br />" +
    "The segment named PAGEZERO generally creates a large space of 0 for where the program is going be loaded.</html>",
    "<html>Maximum virtual memory protection.</html>",
    "<html>Initial virtual memory protection.</html>",
    "<html>Number of sections that this segment is broken into.</html>",
    "<html>The flag setting should be viewed in binary. Each binary digit that is set 1 in value is a setting that is active.<br /><br />" +
    "The Table bellow shows what each setting is for this section.<br /><br />" +
    "<table border='1'>" +
    "<tr><td>Binary digit</td><td>Active Setting</td></tr>" +
    "<tr><td>00000000000000000000000000000001</td><td>The file contents for this segment is for the high part of the VM space, the low part is zero filled (for stacks in core files).</td></tr>" +
    "<tr><td>00000000000000000000000000000010</td><td>This segment is the VM that is allocated by a fixed VM library, for overlap checking in the link editor.</td></tr>" +
    "<tr><td>00000000000000000000000000000100</td><td>This segment has nothing that was relocated in it and nothing relocated to it, that is it maybe safely replaced without relocation.</td></tr>" +
    "<tr><td>00000000000000000000000000001000</td><td>This segment is protected. If the segment starts at file offset 0, the first page of the segment is not protected.  All other pages of the segment are protected.</td></tr>" +
    "<tr><td>00000000000000000000000000010000</td><td>This segment is made read-only after fixups.</td></tr>" +
    "</table></html>"
  };

  private static final String[] sectInfo = new String[]
  {
    "<html>This is the name given to a section of data in this segment.</html>",
    "<html>This is the the segment that this section belongs to. It should match the main data segment name.</html>",
    segInfo[3], segInfo[4], segInfo[5],
    "<html>Section alignment. Some values in the section are read using a multiply such as an grouping of elements that are 2 bytes each (multiple is then 2). This means things must be evenly spaced apart.<br /><br />" +
    "If we place this section somewhere else in memory we must make sure we move it in a even multiple of this number.</html>",
    "<html>Relocations offset. This is a list of addresses that locate to addresses that access a value in this section. If there is none it is set 0.<br /><br />" +
    "The offsets in the Relocations are wrote to if we move this section somewhere else in memory like 128 bytes away then we must add 128 bytes to the locations that access values in this section.<br /><br />" +
    "This program always loads all sections to their defined RAM address locations. The relocations are only used when the address location is already in use by another program.<br /><br />" +
    "The section must also be aligned by section alignment. See the Alignment property of this section for details.</html>",
    "<html>Number of relocations. This is the number of addresses in the relocation list. See relocation offset property for details.</html>",
    "<html>The first two hex digits is the section type. The reaming 24 binary digits after the first hex digit is 24 flag settings.<br /><br />" +
    "<table border='1'>" +
    "<tr><td>Hex value</td><td>Section type</td></tr>" +
    "<tr><td>06</td><td>Section with only non-lazy symbol pointers.</td></tr>" +
    "<tr><td>07</td><td>Section with only lazy symbol pointers.</td></tr>" +
    "<tr><td>08</td><td>Section with only symbol stubs, byte size of stub in the reserved2 field.</td></tr>" +
    "<tr><td>09</td><td>Section with only function pointers for initialization.</td></tr>" +
    "<tr><td>0A</td><td>Section with only function pointers for termination.</td></tr>" +
    "<tr><td>0B</td><td>Section contains symbols that are to be coalesced</td></tr>" +
    "<tr><td>0C</td><td>Zero fill on demand section (that can be larger than 4 gigabytes).</td></tr>" +
    "<tr><td>0D</td><td>Section with only pairs of function pointers for interposing.</td></tr>" +
    "<tr><td>0E</td><td>Section with only 16 byte literals.</td></tr>" +
    "<tr><td>0F</td><td>Section contains DTrace Object Format.</td></tr>" +
    "<tr><td>10</td><td>Section with only lazy symbol pointers to lazy loaded dylibs.</td></tr>" +
    "<tr><td>11</td><td>Template of initial values for TLVs.</td></tr>" +
    "<tr><td>12</td><td>Template of initial values for TLVs with zero fill.</td></tr>" +
    "<tr><td>13</td><td>TLV descriptors.</td></tr>" +
    "<tr><td>14</td><td>Pointers to TLV descriptors.</td></tr>" +
    "<tr><td>15</td><td>Functions to call to initialize TLV values.</td></tr>" +
    "<tr><td>16</td><td>32-bit offsets to initializers.</td></tr>" +
    "</table><br /><br />" +
    "The flag settings are intended to be viewed in binary. Each setting that is active is a binary digit that is set 1. The following bits are used for the sections attributes.<br /><br />" +
    "<table border='1'>" +
    "<tr><td>10000000000000000000000000000000</td><td>Section contains only true machine instructions.</td></tr>" +
    "<tr><td>01000000000000000000000000000000</td><td>Section contains coalesced symbols that are not to be in a ranlib table of contents.</td></tr>" +
    "<tr><td>00100000000000000000000000000000</td><td>Ok to strip static symbols in this section in files with the MH_DYLDLINK flag.</td></tr>" +
    "<tr><td>00010000000000000000000000000000</td><td>No dead stripping.</td></tr>" +
    "<tr><td>00001000000000000000000000000000</td><td>blocks are live if they reference live blocks.</td></tr>" +
    "<tr><td>00000100000000000000000000000000</td><td>Used with i386 code stubs written on by dyld.</td></tr>" +
    "<tr><td>00000010000000000000000000000000</td><td>A debug section.</td></tr>" +
    "<tr><td>00000000000000000000010000000000</td><td>Section contains some machine instructions.</td></tr>" +
    "<tr><td>00000000000000000000001000000000</td><td>Section has external relocation entries.</td></tr>" +
    "<tr><td>00000000000000000000000100000000</td><td>Section has local relocation entries.</td></tr>" +
    "</table></html>",
    "<html>Reserved for future use (for offset or index).</html>",
    "<html>Reserved for future use (for count or sizeof).</html>",
    "<html>Reserved for future use (for use on 64 bit programs only).</html>"
  };

  private void cmdInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>Command for mapping and loading the program.</html>" );
    }
    else
    {
      info( cmdInfo[i] );
    }
  }

  private void segInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>Load a section of the program into RAM memory. Sections generally have standard segment name for their use.<br /><br />" +
      "<table border='1'>" +
      "<tr><td>__PAGEZERO</td><td>Fills the area the program is going to load into RAM memory with zeros.</td></tr>" +
      "<tr><td>__TEXT</td><td>The tradition UNIX text segment. Contains machine code, and data types or strings.</td></tr>" +
      "<tr><td>__DATA</td><td>The real initialized data section. Data that should be loaded before program starts.</td></tr>" +
      "<tr><td>__OBJC</td><td>Objective-C runtime segment.</td></tr>" +
      "<tr><td>__ICON</td><td>The icon segment.</td></tr>" +
      "<tr><td>__LINKEDIT</td><td>The segment containing all structs.</td></tr>" +
      "<tr><td>__UNIXSTACK</td><td>The unix stack segment.</td></tr>" +
      "<tr><td>__IMPORT</td><td>The segment for the self (dyld).</td></tr>" +
      "</table></html>" );
    }
    else
    {
      info( segInfo[i] );
    }
  }

  private void sectInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>This is a section. It labels a section within the loaded data segment.<br /><br />" +
      "<table border='1'>" +
      "<tr><td>__text</td><td>Contains the processor instructions of the program.</td></tr>" +
      "<tr><td>__fvmlib_init0</td><td>the fvmlib initialization section</td></tr>" +
      "<tr><td>__fvmlib_init1</td><td>the section following the fvmlib initialization section</td></tr>" +
      "<tr><td>__data</td><td>The real data that should be initialized before the program starts.</td></tr>" +
      "<tr><td>__bss</td><td>Section of data to set all 0 before program starts (uninitialized data).</td></tr>" +
      "<tr><td>__common</td><td>The section common symbols are in.</td></tr>" +
      "<tr><td>__symbol_table</td><td>The symbol table.</td></tr>" +
      "<tr><td>__module_info</td><td>Module information.</td></tr>" +
      "<tr><td>__selector_strs</td><td>String table.</td></tr>" +
      "<tr><td>__selector_refs</td><td>String table references.</td></tr>" +
      "<tr><td>__header</td><td>The icon headers.</td></tr>" +
      "<tr><td>__tiff</td><td>The icons in tiff format.</td></tr>" +
      "</table></html>" );
    }
    else
    {
      info( sectInfo[i] );
    }
  }
}