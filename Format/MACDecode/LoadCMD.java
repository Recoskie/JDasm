package Format.MACDecode;

import swingIO.*;
import swingIO.tree.*;

public class LoadCMD extends Data
{
  public void load(JDNode root) throws java.io.IOException
  {
    //Remove the dummy node.

    if( App.getChildCount() > 0 ) { App.remove( 0 ); }

    //Program start address.

    long main = 0;

    //WE should divide sections by data.

    JDNode code = new JDNode( "Code Sections" );

    int cmd = 0, size = 0;

    for( int i = 0; i < loadCMD; i++ )
    {
      DTemp = new Descriptor( file );

      DTemp.LUINT32("Command"); cmd = (int)DTemp.value & 0xFF;
      DTemp.LUINT32("Size"); size = (int)DTemp.value - 8;

      if( size <= 0 ){ size = 0; }

      //Loading the programs sections and data is the main priority here, so commands 0x19, and 0x01 come first.

      if( cmd == 0x19 || cmd == 0x01 )
      {
        String segName = "", name = "";

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

        JDNode n2 = new JDNode( name + ( oSize > 0 ? "" : ".h" ), new long[] { 0, ref++ } );
        
        DTemp.setEvent( this::segInfo ); des.add( DTemp );

        if( oSize > 0 ) { n2.add( new JDNode( name + " (Data).h", new long[] { -3, address, address + vSize - 1 } ) ); }

        JDNode t;

        for( int i2 = 0; i2 < sect; i2++ )
        {
          DTemp = new Descriptor( file ); DTemp.setEvent( this::sectInfo ); des.add( DTemp );

          DTemp.String8("Section Name", 16); segName = (String)DTemp.value;
          
          t = new JDNode( DTemp.value + "", new long[] { 0, ref++ } );

          DTemp.String8("Segment Name", 16);

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
          DTemp.LUINT32("flags"); int flag = (int)DTemp.value;
          DTemp.LUINT32("Reserved");
          DTemp.LUINT32("Reserved");
          
          if( is64bit ) { DTemp.LUINT32("Reserved"); }

          //Section contains only machine code.

          if( ( flag & 0x80000000 ) != 0 )
          {
            code.add( new JDNode( name + "." + segName + "().h", new long[]{ -4, address, vSize } ) );
          }

          n2.add( t );
        }

        root.add( n2 );
      }

      //Symbol information.

      else if( cmd == 0x02 )
      {
        DTemp.LUINT32("Symbol Offset"); int off = (int)base + (int)DTemp.value;
        DTemp.LUINT32("Number of symbol"); int len = (int)DTemp.value;
        DTemp.LUINT32("String table offset"); int strOff = (int)base + (int)DTemp.value;
        DTemp.LUINT32("String table size"); int strSize = (int)DTemp.value;

        DTemp.setEvent( this::symInfo ); des.add( DTemp );
        
        JDNode n2 = new JDNode( "Symbol Table", new long[]{ 0, ref++ } ); root.add( n2 );
      
        n2.add( new JDNode( "String table.h", new long[]{ -2, strOff, strOff + strSize - 1 } ) );

        JDNode n3 = new JDNode( "Symbols", new long[]{ 0, ref++ } ); n2.add( n3 );
        
        //Begin reading all the symbols.
        
        Descriptor string;

        long t1 = file.getFilePointer(), t2 = 0;

        file.seek(off); DTemp = new Descriptor( file ); DTemp.setEvent( this::symsInfo ); des.add( DTemp );

        int id;
      
        for( int i2 = 0; i2 < len; i2++ )
        {
          id = 0;

          DTemp.Array("Symbol #" + i2 + "", is64bit ? 16 : 12 );
          DTemp.LUINT32("Name"); int name = (int)DTemp.value;
          DTemp.UINT8("Type"); int type = (byte)DTemp.value; 
          DTemp.UINT8("NSect"); int NSect = (byte)DTemp.value;
          DTemp.LUINT16("DSect");

          if( is64bit ) { DTemp.LUINT64("Symbol offset"); } else { DTemp.LUINT32("Symbol offset"); }

          if( ( type & 0x0E ) == 0x0E ) { id = NSect; }
          
          if( name != 0 )
          {
            t2 = file.getFilePointer(); file.seek( name + strOff );

            string = new Descriptor( file ); string.setEvent( this::blank );

            string.String8("Symbol name", (byte)0x00 );

            n3.add( new JDNode( string.value + ( id > 0 ? " #" + id + "" : "" ) + ".h", new long[]{ 0, ref++ }) );

            des.add( string ); file.seek( t2 );
          }
          else if( id > 0 )
          {
            n3.add( new JDNode("Symbol #" + id + ".h") );
          }
          else
          {
            n3.add( new JDNode( "null.h" ) );
          }
        }

        file.seek( t1 );
      }

      //Dynamic link edit symbol table.

      else if( cmd == 0x0B )
      {
        DTemp.LUINT32("Local sym index");
        DTemp.LUINT32("Number of local symbols");
        DTemp.LUINT32("Index to external sym");
        DTemp.LUINT32("Number of external sym");
        DTemp.LUINT32("Index to undefined sym");
        DTemp.LUINT32("Number of undefined sym");

        DTemp.LUINT32("Contents table offset"); int coff = (int)base + (int)DTemp.value;
        DTemp.LUINT32("Entries in table of contents"); int csize = (int)DTemp.value;

        DTemp.LUINT32("Offset to module table"); int moff = (int)base + (int)DTemp.value;
        DTemp.LUINT32("number of module table entries"); int msize = (int)DTemp.value;

        DTemp.LUINT32("Offset to referenced symbol table"); int roff = (int)base + (int)DTemp.value;
        DTemp.LUINT32("Number of referenced symbol table entries"); int rsize = (int)DTemp.value;

        DTemp.LUINT32("File offset to the indirect symbol table"); int indoff = (int)base + (int)DTemp.value;
        DTemp.LUINT32("Number of indirect symbol table entries"); int indsize = (int)DTemp.value;

        DTemp.LUINT32("Offset to external relocation entries"); int extoff = (int)base + (int)DTemp.value;
        DTemp.LUINT32("Number of external relocation entries"); int extsize = (int)DTemp.value;

        DTemp.LUINT32("Offset to local relocation entries"); int loff = (int)base + (int)DTemp.value;
        DTemp.LUINT32("Number of local relocation entries"); int lsize = (int)DTemp.value;

        DTemp.setEvent( this::blank ); des.add( DTemp );

        JDNode n1 = new JDNode( "Link Edit info", new long[]{ 0, ref++ } );
      
        if( csize > 0 ) { n1.add( new JDNode("Content.h", new long[]{ -2, coff, coff + csize * 4 - 1 } ) ); }
        if( msize > 0 ) { n1.add( new JDNode("Module.h", new long[]{ -2, moff, moff + msize * 4 - 1 } ) ); }
        if( rsize > 0 ) { n1.add( new JDNode("Sym Ref.h", new long[]{ -2, roff, roff + rsize * 4 - 1 } ) ); }
        if( indsize > 0 ) { n1.add( new JDNode("Indirect Sym.h", new long[]{ -2, indoff, indoff + indsize * 4 - 1 } ) ); }
        if( extsize > 0 ) { n1.add( new JDNode("Export.h", new long[]{ -2, extoff, extoff + extsize * 4 - 1 } ) ); }
        if( lsize > 0 ) { n1.add( new JDNode("Local.h", new long[]{ -2, loff, loff + lsize * 4 - 1 } ) ); }

        root.add( n1 );
      }

      //Load a link library.

      else if( cmd == 0x0C || cmd == 0x0D || cmd == 0x18 )
      {
        DTemp.LUINT32("String Offset"); int off = (int)DTemp.value;
        DTemp.LUINT32("Time date stamp");
        DTemp.LUINT32("Current version");
        DTemp.LUINT32("Compatibility version");

        if( off < 24 ){ DTemp.Other( "Other Data", off - 24 ); }

        DTemp.String8("Dynamic Library", ( size + 8 ) - off );

        DTemp.setEvent( this::dynlInfo ); des.add( DTemp );
      
        root.add( new JDNode( "Load link library.h", new long[]{ 0, ref++ } ) );
      }

      //Load a dynamic linker.

      else if( cmd == 0x0E )
      {
        DTemp.LUINT32("String Offset"); int off = (int)DTemp.value;
        DTemp.String8("Dynamic linker", ( size + 8 ) - off );

        DTemp.setEvent( this::dynInfo ); des.add( DTemp );

        root.add( new JDNode( "Dynamic linker.h", new long[]{ 0, ref++ } ) );
      }
    
      //The start of the application.

      else if( cmd == 0x28 )
      {
        DTemp.LUINT64("Programs start address"); main = file.toVirtual( base + (long)DTemp.value );
        DTemp.LUINT64("Stack memory size");

        DTemp.setEvent( this::startInfo ); des.add( DTemp );

        root.add( new JDNode( "Start Address.h", new long[]{ 0, ref++ } ) );
      }

      //The UUID.

      else if( cmd == 0x1B )
      {
        DTemp.Other("128-bit UUID", 16);

        DTemp.setEvent( this::uuidInfo ); des.add( DTemp );

        root.add( new JDNode( "APP UUID.h", new long[]{ 0, ref++ } ) );
      }

      //Link edit.

      else if( cmd == 0x1D || cmd == 0x1E || cmd == 0x26 || cmd == 0x29 || cmd == 0x2B || cmd == 0x2E || cmd == 0x33 || cmd == 0x34 )
      {
        DTemp.LUINT32("Offset"); int off = (int)base + (int)DTemp.value;
        DTemp.LUINT32("Size"); int s = (int)DTemp.value;

        DTemp.setEvent( this::blank ); des.add( DTemp );

        JDNode n1;
        
        if( cmd == 0x1D ) { n1 = new JDNode( "Code Signature", new long[]{ 0, ref++ } ); }
        else if( cmd == 0x1E ) { n1 = new JDNode( "Split info", new long[]{ 0, ref++ } ); }
        else if( cmd == 0x26) { n1 = new JDNode( "Function Starts", new long[]{ 0, ref++ } ); }
        else if( cmd == 0x29 ) { n1 = new JDNode( "Data in Code", new long[]{ 0, ref++ } ); }
        else if( cmd == 0x2B ) { n1 = new JDNode( "Code Singing", new long[]{ 0, ref++ } ); }
        else if( cmd == 0x2E ) { n1 = new JDNode( "Optimization Hints", new long[]{ 0, ref++ } ); }
        else if( cmd == 0x33 ) { n1 = new JDNode( "Exports", new long[]{ 0, ref++ } ); }
        else { n1 = new JDNode( "Chained Fixups", new long[]{ 0, ref++ } ); }

        n1.add( new JDNode("sect.h", new long[]{ -2, off, off + s - 1 } ) );

        root.add( n1 );
      }

      //Compressed link information. Note this section is easy to read and uses opcodes for the type of rebase, or symbol bind.

      else if( cmd == 0x22 )
      {
        DTemp.LUINT32("rebase offset"); int roff = (int)base + (int)DTemp.value;
        DTemp.LUINT32("rebase size"); int rsize = (int)DTemp.value;
        DTemp.LUINT32("bind offset"); int boff = (int)base + (int)DTemp.value;
        DTemp.LUINT32("bind size"); int bsize = (int)DTemp.value;
        DTemp.LUINT32("weak bind offset"); int wboff = (int)base + (int)DTemp.value;
        DTemp.LUINT32("weak bind size"); int wbsize = (int)DTemp.value;
        DTemp.LUINT32("lazy bind offset"); int lboff = (int)base + (int)DTemp.value;
        DTemp.LUINT32("lazy bind size"); int lbsize = (int)DTemp.value;
        DTemp.LUINT32("export offset"); int eoff = (int)base + (int)DTemp.value;
        DTemp.LUINT32("export size"); int esize = (int)DTemp.value;

        DTemp.setEvent( this::blank ); des.add( DTemp );

        JDNode n1 = new JDNode( "Link info", new long[]{ 0, ref++ } );

        if( rsize > 0 ){ n1.add( new JDNode("rebase.h", new long[]{ -2, roff, roff + rsize - 1 } ) ); }
        if( bsize > 0 ){ n1.add( new JDNode("bind.h", new long[]{ -2, boff, boff + bsize - 1 } ) ); }
        if( wbsize > 0 ){ n1.add( new JDNode("weak bind.h", new long[]{ -2, wboff, wboff + wbsize - 1 } ) ); }
        if( lbsize > 0 ){ n1.add( new JDNode("lazy bind.h", new long[]{ -2, lboff, lboff + lbsize - 1 } ) ); }
        if( esize > 0 ){ n1.add( new JDNode("export.h", new long[]{ -2, eoff, eoff + esize - 1 } ) ); }

        root.add( n1 );
      }

      //Minimum OS version. Specifying platform and tools.

      else if( cmd == 0x32 )
      {
        DTemp.LUINT32("Platform");
        DTemp.LUINT32("Minium OS");
        DTemp.LUINT32("SDK");
        DTemp.LUINT32("Num Tools"); int t = (int)DTemp.value;

        for( int i1 = 0; i1 < t; i1++ )
        {
          DTemp.Array("Tool #" + i1 + "", 8);
          DTemp.LUINT32("Tool type");
          DTemp.LUINT32("Tool version");
        }

        DTemp.setEvent( this::osVerInfo ); des.add( DTemp );

        root.add( new JDNode( "Min OS Version.h", new long[]{ 0, ref++ } ) );
      }

      //The old way of doing Min os version.

      else if( cmd == 0x24 || cmd == 0x25 || cmd == 0x2F || cmd == 0x30 )
      {
        DTemp.LUINT32("Min Version");
        DTemp.LUINT32("Min SDK");

        DTemp.setEvent( this::osInfo ); des.add( DTemp );

        if( cmd == 0x24 ){ root.add( new JDNode("Min MacOS version.h", new long[]{ 0, ref++ } ) ); }
        else if( cmd == 0x25 ){ root.add( new JDNode("Min iPhone Version.h", new long[]{ 0, ref++ } ) ); }
        else if( cmd == 0x2F ){ root.add( new JDNode("Min Apple TV Version.h", new long[]{ 0, ref++ } ) ); }
        if( cmd == 0x30 ){ root.add( new JDNode("Min Apple Watch Version.h", new long[]{ 0, ref++ } ) ); }
      }

      //The Source Version.

      else if( cmd == 0x2A )
      {
        DTemp.LUINT64("Source Version");
      
        DTemp.setEvent( this::sourceVerInfo ); des.add( DTemp );
      
        root.add( new JDNode( "Source Version.h", new long[]{ 0, ref++ } ) );
      }

      //An unknown command, or a command I have not added Yet.

      else
      {
        DTemp.Other("Command Data", size ); DTemp.setEvent( this::cmdInfo ); des.add( DTemp );

        root.add( new JDNode( "CMD #" + i + ".h", new long[]{ 0, ref++ } ) );
      }
    }

    //Sections that only machine code.

    if( code.getChildCount() > 0 ) { App.add( code ); }

    //The programs main entry point.

    if( main != 0 ) { App.add( new JDNode("Program Start (Machine Code).h", new long[]{ -4, main } ) ); }
  }

  private static final String offsets = "<br /><br />If this is a universal binary then the offset is added to the start of the application in this file.";

  private static final String cmdType = "The first 2 hex digits is the load command. If the last two hex digits are 80 this means the section is required for the program to run properly.<br /><br />" +
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
  "<tr><td>28</td><td>The start address for the application. Replacement for LC_UNIXTHREAD.</td></tr>" +
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

  private static final String offStr = "This is the number of bytes from the start of this command to the string that tells us the binary to load.";

  private static final String Str = "This is the string that the offset in the command locates to. The end of the string of text is defined by the remaining size of this command.<br /><br />" +
  "Values that are 00 in value after the text is to be ignored as padding bytes.";

  private static final String dateTime = "A date time stamp is in seconds. The seconds are added to the starting date \"Wed Dec 31 7:00:00PM 1969\".<br /><br />" +
  "If the time date stamp is \"37\" in value, then it is plus 37 seconds giving \"Wed Dec 31 7:00:37PM 1969\".<br /><br />Note that the start time varies based on time zone.";

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
    "<html>File position to the bytes that will be read and placed into RAM at the memory address." + offsets + "</html>",
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
    "<tr><td>00000000000000000000000000001000</td><td>This segment is protected. If the segment starts at file offset 0, the first page of the segment is not protected. All other pages of the segment are protected.</td></tr>" +
    "<tr><td>00000000000000000000000000010000</td><td>This segment is made read-only after relocations are applied if needed.</td></tr>" +
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

  private static final String[] symInfo = new String[]
  {
    cmdType, cmdSize,
    "<html>This is the file position to the symbol table." + offsets + "</html>",
    "<html>This is the number of symbols at the symbol table offset.</html>",
    "<html>This is the file position to the string table. This value is added with the name value in the symbol table to find create the file position to the name of a symbol." + offsets + "</html>",
    "<html>This is the size of the string table. If the name value is bigger than the string table size then we know we are reading outside the names and that there is something wrong.</html>"
  };

  private static final String[] symsInfo = new String[]
  {
    "<html>Array element for defining one symbol.</html>",
    "<html>The name value is added to the file position for the string table. If this value is 0 then the symbol has no name." + offsets + "</html>",
    "<html>This value is broken into tow sections. First is the flag setting. Any of the binary digits that are set one correspond to the following settings.<br /><br />" +
    "<table border='1'>" +
    "<tr><td>Digit</td><td>Setting</td></tr>" +
    "<tr><td>10000000</td><td>Symbolic debugging entry.</td></tr>" +
    "<tr><td>01000000</td><td>Symbolic debugging entry.</td></tr>" +
    "<tr><td>00100000</td><td>Symbolic debugging entry.</td></tr>" +
    "<tr><td>00010000</td><td>Private external symbol.</td></tr>" +
    "<tr><td>00000001</td><td>External symbol.</td></tr>" +
    "<tr><td>0000xxx0</td><td>The digits marked x here are used as a combination for the type setting.</td></tr>" +
    "</table><br /><br />" +
    "The type setting uses the three digits marked as x in the above table as the type setting combination. The hyphens are used to separate the tree bits for the type setting.<br /><br />" +
    "<table border='1'>" +
    "<tr><td>Combination</td><td>Setting</td></tr>" +
    "<tr><td>0000-000-0</td><td>Symbol undefined.</td></tr>" +
    "<tr><td>0000-001-0</td><td>Symbol absolute.</td></tr>" +
    "<tr><td>0000-101-0</td><td>Symbol indirect.</td></tr>" +
    "<tr><td>0000-110-0</td><td>Symbol prebound undefined.</td></tr>" +
    "<tr><td>0000-111-0</td><td>Symbol does not use the section value.</td></tr>" +
    "</table></html>",
    "<html>An integer specifying the number of the section that this symbol can be found in.<br  /><br />" +
    "If the type setting of the symbol is set to use no section, then this value is used as the symbols number name.<br /><br />" +
    "An symbol can be both referenced by a name, or by a number. If the symbol is set to use no name and this value is set also set to 0 then it means that the symbol is null (Non existent).</html>",
    "<html>The DSect value setting describes additional information about the type of symbol this is.<br /><br />" +
    "This value is broken into tow sections. First is the flag setting. Any of the binary digits that are set one correspond to the following settings.<br /><br />" +
    "<table border='1'>"+
    "<tr><td>Digit</td><td>Setting</td></tr>" +
    "<tr><td>0000000000010000</td><td>Must be set for any defined symbol that is referenced by dynamic-loader.</td></tr>" +
    "<tr><td>0000000000100000</td><td>Used by the dynamic linker at runtime.</td></tr>" +
    "<tr><td>0000000001000000</td><td>If the dynamic linker cannot find a definition for this symbol, it sets the address of this symbol to 0.</td></tr>" +
    "<tr><td>0000000010000000</td><td>If the static linker or the dynamic linker finds another definition for this symbol, the definition is ignored.</td></tr>" +
    "<tr><td>000000000000xxxx</td><td>The digits marked as x here are used for the symbol type information. See the next table for type information.</td></tr>" +
    "</table><br />" +
    "The last four binary digits are used as a combination for the symbol type which are separated by a hyphen.<br /><br />" +
    "<table border='1'>" +
    "<tr><td>Value</td><td>Description</td></tr>" +
    "<tr><td>000000000000-0000</td><td>This symbol is a reference to an external symbol.</td></tr>" +
    "<tr><td>000000000000-0001</td><td>This symbol is a reference to an external function call.</td></tr>" +
    "<tr><td>000000000000-0010</td><td>This symbol is defined in this library/program.</td></tr>" +
    "<tr><td>000000000000-0011</td><td>This symbol is defined in this module and is visible only to library/program within this shared library.</td></tr>" +
    "<tr><td>000000000000-0100</td><td>This symbol is defined in another module in this file, and is visible only to libraries/programs within this shared library.</td></tr>" +
    "<tr><td>000000000000-0101</td><td>This symbol is defined in another module in this file, is a lazy (function) symbol, and is visible only to libraries/programs within this shared library.</td></tr>" +
    "</table></html>",
    "<html>The address location that the symbol is at.</html>",
  };

  private static final String[] osVerInfo = new String[]
  {
    cmdType, cmdSize,
    "<html>This is the platform that this binary is intended to run on.<br /><br />" +
    "<table border='1'>" +
    "<tr><td>Value</td><td>Platform</td></tr>" +
    "<tr><td>1</td><td>MacOS</td></tr>" +
    "<tr><td>2</td><td>iPhone IOS</td></tr>" +
    "<tr><td>3</td><td>Apple TV Box</td></tr>" +
    "<tr><td>4</td><td>Apple Watch</td></tr>" +
    "<tr><td>5</td><td>Bridge OS</td></tr>" +
    "<tr><td>6</td><td>Mac Catalyst</td></tr>" +
    "<tr><td>7</td><td>iPhone IOS simulator</td></tr>" +
    "<tr><td>8</td><td>Apple TV simulator</td></tr>" +
    "<tr><td>9</td><td>Apple watch simulator</td></tr>" +
    "<tr><td>10</td><td>Driver KIT</td></tr>" +
    "</table></html>",
    "<html>Minimum os version that this binary is meant to run on.<br /><br />" +
    "The version number is encoded as follows 12341212 is 1234.12.12v.<br /><br />" +
    "You then can change the hex values after each dot to a decimal value.</html>",
    "<html>This is the SDK tool set version that was used to create this binary.<br /><br />" +
    "The version number is encoded as follows 12341212 is 1234.12.12v.<br /><br />" +
    "You then can change the hex values after each dot to a decimal value.</html>",
    "<html>Number of tools used in this binary</html>",
    "<html>Array element containing the tool type and version.</html>",
    "<html>The type of tool used.<br /><br />" +
    "<table border='1'>" +
    "<tr><td>Value</td><td>Tool</td></tr>" +
    "<tr><td>1</td><td>CLANG</td></tr>" +
    "<tr><td>2</td><td>SWIFT</td></tr>" +
    "<tr><td>3</td><td>LD</td></tr>" +
    "</table></html>",
    "<html>Tool version number.</html>"
  };

  private static final String[] dynInfo = new String[]
  {
    cmdType, cmdSize,
    "<html>" + offStr + "</html>",
    "<html>" + Str + " This tells us the dynamic linker to use in loading link libraries in this application.</html>"
  };

  private static final String[] dynlInfo = new String[]
  {
    cmdType, cmdSize,
    "<html>" + offStr + "</html>",
    "<html>Library's build time stamp (sometimes is not set).<br /><br />" + dateTime + "</html>",
    "<html>This was the library's current version number when this application was built.</html>",
    "<html>The Library must at least be updated to the compatibility version number or later in order to run this application.</html>",
    "<html>" + Str + " This tells us the binary to start loading. The dynamic linker links the symbols into our programs symbols.</html>"
  };

  private static final String[] sourceVerInfo = new String[]
  {
    cmdType, cmdSize,
    "<html>The version value is meant to be read in binary. The 64 binary digits are separated as follows:<br /><br />" +
    "000000000000000000000000.0000000000.0000000000.0000000000.0000000000<br /><br />" +
    "You then can change each of the separated binary numbers into decimal numbers followed by a dot to forum the source version number.</html>"
  };

  private static final String[] uuidInfo = new String[]
  {
    cmdType, cmdSize,
    "<html>This is a randomly generated number that can be used to uniquely indemnify your application.</html>"
  };

  private static final String[] startInfo = new String[]
  {
    cmdType, cmdSize,
    "<html>The file offset to the programs start Address.</html>",
    "<html>Stack memory size.</html>"
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

  private void dynInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>We can specify the tool we wish to use to link in methods from other binaries into this binary.</html>" );
    }
    else
    {
      info( dynInfo[i] );
    }
  }

  private void dynlInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>This is a binary we wish to load. Every binary has a symbol table that gives parts of the program names by locations.<br /><br />" +
      "Not all symbols locate to a section of code. There is a command called \"link info\" that defines which symbols are exportable and which ones need to be binded to another binary.<br /><br />" +
      "The symbols that need to be binded are a jump or call operation which are to be set to the location of a exportable method from another binary. We call these jumps and calls studs.<br /><br />" +
      "It is the dynamic linkers job to make sure our symbols that need to be binded locate to the exportable symbols when the processor hits the call and jump instructions that read the value.</html>" );
    }
    else
    {
      info( dynlInfo[i] );
    }
  }

  private void uuidInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>The UUID is a randomly generated number that can be used to identify your application.</html>" );
    }
    else
    {
      info( uuidInfo[i] );
    }
  }

  private void osVerInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>Specifies the OS that this binary is meant to run on.<br /><br />" +
      "Also defines the minium version of the OS this binary is meant to run on, and tools used to compile it.</html>" );
    }
    else
    {
      info( osVerInfo[ i >= 6 ? ( ( i - 6 ) % 3 ) + 6 : i ] );
    }
  }

  private void osInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>Defines the minium version of the OS this binary is meant to run on.</html>" );
    }
    else
    {
      info( osVerInfo[ i >= 2 ? i + 1 : i ] );
    }
  }

  private void sourceVerInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>Defines the source version for the application.</html>" );
    }
    else
    {
      info( sourceVerInfo[ i ] );
    }
  }

  private void symInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>The symbols define the method calls and function calls in a mac binary.</html>" );
    }
    else
    {
      info( symInfo[i] );
    }
  }

  private void symsInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>This is the symbol array.</html>" );
    }
    else
    {
      info( symsInfo[ i % 6 ] );
    }
  }

  private void startInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>This is the location to the beginning of the program.</html>" );
    }
    else
    {
      info( startInfo[i] );
    }
  }

  private void blank( int i )
  {
    info("<html>No information on this section or its properties yet.</html>");
  }
}
