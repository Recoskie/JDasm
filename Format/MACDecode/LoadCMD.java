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

      if( cmd == 0x19 )
      {
        String name = "";

        DTemp.String8("Section Name", 16 ); name = (String)DTemp.value;
        DTemp.LUINT64("Address"); long address = (long)DTemp.value;
        DTemp.LUINT64("Size"); long vSize = (long)DTemp.value;
        DTemp.LUINT64("File position"); long offset = (long)DTemp.value;
        DTemp.LUINT64("Length"); long oSize = (long)DTemp.value;

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
          DTemp = new Descriptor( file );

          DTemp.String8("Section Name", 16);
          
          JDNode t = new JDNode( DTemp.value + "", new long[] { 0, ref++ } ); des.add( DTemp );

          DTemp.String8("Segment Name", 16); name =(String)DTemp.value;
          DTemp.LUINT64("Address"); address = (long)DTemp.value;
          DTemp.LUINT64("Size"); vSize = (long)DTemp.value;
          DTemp.LUINT32("Offset"); offset = (int)DTemp.value;

          file.addV(offset, vSize, address, vSize);

          t.add( new JDNode( name + ".h", new long[] { -3, address, address + vSize - 1 } ) );

          DTemp.LUINT32("Alignment");
          DTemp.LUINT32("Relocations Offset");
          DTemp.LUINT32("Relocations");
          DTemp.LUINT32("flags");
          DTemp.LUINT32("Reserved");
          DTemp.LUINT32("Reserved");
          DTemp.LUINT32("Reserved");

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

  private static final String[] dataInfo = new String[]
  {
    cmdType,
    "<html>The size of the command and all it's parameters.</html>",
    "<html>Segment name.</html>",
    "<html>Memory address to place the section in RAM memory.</html>",
    "<html>Number of bytes to place in RAM memory for this section.</html>",
    "<html>File position to the bytes that will be read and placed into RAM at the memory address.</html>",
    "<html>The number of bytes to read from the file to be placed at the RAM address.<br /><br />" +
    "If this is smaller than the number of bytes to place in RAM for this section then the rest are 00 byte filled.</html>",
    "<html>Maximum virtual memory protection.</html>",
    "<html>Initial virtual memory protection.</html>",
    "<html>Number of sections in segment.</html>",
    "<html>Flags.</html>"
  };

  private static final String[] cmdInfo = new String[]
  {
    cmdType,
    "<html>The size of the command and all it's parameters.</html>",
    "<html>The commands values and parameters.</html>"
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
      info( "<html>Command for mapping and loading the program.</html>" );
    }
    else
    {
      info( dataInfo[i] );
    }
  }
}
