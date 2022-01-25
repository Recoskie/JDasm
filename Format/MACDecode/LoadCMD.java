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

        DTemp.String8("Section Name", 16 ); name = (String)DTemp.value;

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

        DTemp.setEvent( this::dataInfo );

        JDNode n2 = new JDNode( name, new long[] { 0, ref++ } ); des.add( DTemp );

        n2.add( new JDNode( name + " (Data).h", new long[] { -3, address, address + vSize - 1 } ) );

        for( int i2 = 0; i2 < sect; i2++ )
        {
          DTemp = new Descriptor( file ); DTemp.setEvent( this::segInfo );

          DTemp.String8("Segment Name", 16);
          
          JDNode t = new JDNode( DTemp.value + "", new long[] { 0, ref++ } ); des.add( DTemp );

          DTemp.String8("Section Name", 16); name = (String)DTemp.value;

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
    cmdType, cmdSize,
    "<html>The commands values and parameters.</html>"
  };

  private static final String[] dataInfo = new String[]
  {
    cmdType, cmdSize,
    "<html>Section name. A section can be divided into smaller section called segments by segment names.</html>",
    "<html>Memory address to place the section in RAM memory.</html>",
    "<html>Number of bytes to place in RAM memory for this section.</html>",
    "<html>File position to the bytes that will be read and placed into RAM at the memory address.<br /><br />" +
    "If this is a universal binary then the offset is added to the start of the application in this file.</html>",
    "<html>The number of bytes to read from the file to be placed at the RAM address.<br /><br />" +
    "If this is smaller than the number of bytes to place in RAM for this section then the rest are 00 byte filled.<br /><br />" +
    "The section named PAGEZERO generally creates a large space of 0 for where the program is going be loaded.</html>",
    "<html>Maximum virtual memory protection.</html>",
    "<html>Initial virtual memory protection.</html>",
    "<html>Number of segment this section is broken into.</html>",
    "<html>Flags.</html>"
  };

  private static final String[] segInfo = new String[]
  {
    dataInfo[2],
    "<html>This is the section the segment belongs to. It should match the main sections name.</html>",
    dataInfo[3], dataInfo[4], dataInfo[5],
    "<html>Section alignment. Some values in the section are read using a multiply. This means things must be evenly spaced apart.<br /><br />" +
    "If we place this section somewhere else in memory we must make sure we move it in a even multiple of this number.</html>",
    "<html>Relocations offset. This is a list of addresses that access a value in this section.<br /><br />" +
    "This means if we palace this section somewhere else in memory 128 bytes away then we must use the address locations in the relocation list to add 128 bytes to the locations that access values in this section.<br /><br />" +
    "This program always loads all sections to their defined RAM address locations. The relocations are only used when the address location is already in use by another program.<br /><br />" +
    "The section must also be aligned by section alignment. See the section alignment properties for details.</html>",
    "<html>Number of relocations. This is the number of addresses in the offset to the relocation list. See relocation offset property for details.</html>",
    dataInfo[10],
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

  private void dataInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>Load a section of the program into RAM memory.</html>" );
    }
    else
    {
      info( dataInfo[i] );
    }
  }

  private void segInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>This is a segment. It labels a sections within the loaded section.</html>" );
    }
    else
    {
      info( segInfo[i] );
    }
  }
}
