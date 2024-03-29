package Format.MACDecode;

import javax.swing.JOptionPane;

import swingIO.*;
import swingIO.tree.*;

public class LoadCMD extends Data
{
  public void load(JDNode root) throws java.io.IOException
  {
    //Remove the dummy node. Note that any sections located to by load commands should be processed after the load commands.

    if( App.getChildCount() > 0 ) { App.remove( 0 ); }

    //Program start address. Defined by load command 0x28.

    long main = 0;

    //When we dump sections to RAM memory we must pay attention to sections that are pointer lists that call linkable methods from other binaries.
    //Sections with flag settings 6 to 8 as they are used by the indirect symbol table. The order the symbols go in is the order each pointer is loaded in.
    
    int bind = -1, lazyBind = -1, studs = -1;

    //The symbol table defines address location in the file with names. The location can be a function, or variable name.
    //Some symbols do not locates to a set of data, or method/function in the file as they are set 0 address location.
    //Such symbols define the library the symbol can be found in which the address in that symbol in the other file is copied to this symbol.
    //The symbol table is used by the indirect symbol table to tell us which symbols to set to pointers that the machine code reads and jumps to the method in the other binary.

    int symOff = -1, symSize = 0, strOff = 0, strSize = 0;

    //The indirect symbol table tells us which symbol by symbol number in the symbol list is each pointer across the pointer sections in the binary.
    //These symbols often have address locations of other symbols copied into them from other binaries.

    int indoff = -1, indsize = 0;
    
    //Modern Mach binaries do not use the indirect symbol table, but use a new compressed table that define the exact location of each pointer and method.
    //It is normal for mach binaries to still have both. In which case we skip the indirect symbol table and map only the symbols with addresses not set 0.
    //We then load in the pointers using the new format.

    int boff = -1, wboff = -1, lboff = -1, bsize = 0, wbsize = 0, lbsize = 0;

    //WE should divide sections by data.

    JDNode code = new JDNode( "Code Sections", new long[]{ 0x8000000000000000L } );

    int cmd = 0, size = 0, ordinal = 1;

    try
    {
      for( int i = 0; i < loadCMD && loadCMDSize > 0; i++ )
      {
        DTemp = new Descriptor( file );

        DTemp.LUINT32("Command"); cmd = (int)DTemp.value & 0xFF;
        DTemp.LUINT32("Size"); size = (int)DTemp.value; loadCMDSize -= size; size -= 8;

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

          DTemp.LUINT32("maxvprot");
          DTemp.LUINT32("minvprot");
          DTemp.LUINT32("Number of sections"); int sect = (int)DTemp.value;
          DTemp.LUINT32("flags");

          JDNode n2 = new JDNode( name + " (Seg=" + segment.size() + ")" + ( oSize > 0 ? "" : ".h" ), new long[] { 0, ref++ } );

          file.addV( offset, oSize, address, vSize ); segment.add( address );
        
          DTemp.setEvent( this::segInfo ); des.add( DTemp );

          if( oSize > 0 ) { n2.add( new JDNode( name + " (Data).h", new long[] { 0x8000000000000003L, address, address + vSize - 1 } ) ); }

          for( int i2 = 0; i2 < sect; i2++ )
          {
            DTemp = new Descriptor( file ); DTemp.setEvent( this::sectInfo ); des.add( DTemp );

            DTemp.String8("Section Name", 16); segName = (String)DTemp.value;
          
            JDNode t = new JDNode( DTemp.value + " (Sect=" + ( sections.size() + 1 ) + ")", new long[] { 0, ref++ } );

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

            file.addV(offset, vSize, address, vSize); sections.add( address );

            t.add( new JDNode( "Goto Data.h", new long[] { 0x8000000000000003L, address, address + vSize - 1 } ) );

            DTemp.LUINT32("Alignment");
            DTemp.LUINT32("Relocations Offset");
            DTemp.LUINT32("Relocations");
            DTemp.LUINT32("flags"); int flag = (int)DTemp.value;
            DTemp.LUINT32("Reserved1");
            DTemp.LUINT32("Reserved2"); int stud_size = (int)DTemp.value;
          
            if( is64bit ) { DTemp.LUINT32("Reserved3"); }

            //Section contains only machine code.

            if( ( flag & 0x80000000 ) != 0 )
            {
              JDNode c = new JDNode("Disassemble.h", new long[]{ 0x8000000000000004L, address, vSize } ); rPath.add( c );
              t.add( c ); code.add( new JDNode( name + "." + segName + "().h", new long[]{ 0x8000000000000005L, paths++ } ) );
            }

            //Sections we want to be able to navigate too.

            flag &= 0xFF;
          
            if( flag == 6 ) { rPath.add( t ); bind = paths++; ptr.add( new Pointers(address, vSize, is64bit ? 8 : 4, bind)); }
            else if( flag == 7 ) { rPath.add( t ); lazyBind = paths++; ptr.add( new Pointers(address, vSize, is64bit ? 8 : 4, lazyBind) ); }
            else if( flag == 8 ) { rPath.add( t ); studs = paths++; ptr.add( new Pointers(address, vSize, stud_size, studs) ); }

            n2.add( t );
          }

          root.add( n2 );
        }

        //Encrypted data segment.

        else if( cmd == 0x21 || cmd == 0x2C )
        {
          DTemp.LUINT32("Data Offset"); int enOff = (int)base + (int)DTemp.value;
          DTemp.LUINT32("Data Size"); int enSize = (int)DTemp.value;
          DTemp.LUINT32("ID Number");

          DTemp.setEvent( this::enDataInfo ); des.add( DTemp );
        
          JDNode n = new JDNode( "Encrypted Data", new long[]{ 0, ref++ } ); root.add( n );
      
          n.add( new JDNode( "Data.h", new long[]{ 0x8000000000000002L, enOff, enOff + enSize - 1 } ) );
        }

        //Symbol information.

        else if( cmd == 0x02 )
        {
          DTemp.LUINT32("Symbol Offset"); symOff = (int)base + (int)DTemp.value;
          DTemp.LUINT32("Number of symbol"); symSize = (int)DTemp.value;
          DTemp.LUINT32("String table offset"); strOff = (int)base + (int)DTemp.value;
          DTemp.LUINT32("String table size"); strSize = (int)DTemp.value;

          DTemp.setEvent( this::symDataInfo ); des.add( DTemp );
        
          JDNode n = new JDNode( "Symbol Table", new long[]{ 0, ref++ } ); root.add( n );
      
          n.add( new JDNode( "String table.h", new long[]{ 0x8000000000000002L, strOff, strOff + strSize - 1 } ) );

          JDNode n1 = new JDNode( "Symbols", new long[]{ 8, symOff, symSize, strOff } ); n1.add( new JDNode("Dummy") ); n.add( n1 );

          symSize = is64bit ? symSize << 4 : ( ( symSize << 2 ) + ( symSize << 3 ) ); //Each symbol is 12 bytes if 32 bit and is 16 bytes if 64 bit.
        }

        //Dynamic link edit symbol table.

        else if( cmd == 0x0B )
        {
          DTemp.LUINT32("Local sym num");
          DTemp.LUINT32("Number of local symbols");
          DTemp.LUINT32("External sym num");
          DTemp.LUINT32("Number of external sym");
          DTemp.LUINT32("Undefined sym num");
          DTemp.LUINT32("Number of undefined sym");

          DTemp.LUINT32("Contents table offset"); int coff = (int)base + (int)DTemp.value;
          DTemp.LUINT32("Content table entries"); int csize = (int)DTemp.value;

          csize <<= 3; //two 32 bit integers. Symbol number and module number.

          DTemp.LUINT32("Module table offset"); int moff = (int)base + (int)DTemp.value;
          DTemp.LUINT32("Module table entries"); int msize = (int)DTemp.value;

          msize = ( msize << 2 ) + ( msize << 4 ) + ( msize << 5 ); //52 bytes per module.

          DTemp.LUINT32("Offset to referenced symbol table"); int roff = (int)base + (int)DTemp.value;
          DTemp.LUINT32("Number of referenced symbol table entries"); int rsize = (int)DTemp.value;

          rsize <<= 2; //32 bit Symbol number with a flag setting specifying external referenced symbols.

          DTemp.LUINT32("Indirect symbol table offset"); indoff = (int)base + (int)DTemp.value;
          DTemp.LUINT32("Indirect symbol table entries"); indsize = (int)DTemp.value;

          indsize <<= 2; //Each indirect entire is a 32 bit number specifying which symbol to map.

          DTemp.LUINT32("External relocation offset"); int extoff = (int)base + (int)DTemp.value;
          DTemp.LUINT32("External relocation entries"); int extsize = (int)DTemp.value;

          extsize <<= 3; //Packed byte encoding 64 bits each.

          DTemp.LUINT32("Local relocation offset"); int loff = (int)base + (int)DTemp.value;
          DTemp.LUINT32("Local relocation entries"); int lsize = (int)DTemp.value;

          lsize <<= 3; //Packed byte encoding 64 bits each.

          DTemp.setEvent( this::symInfo ); des.add( DTemp );

          JDNode n1 = new JDNode( "Symbol info", new long[]{ 0, ref++ } );
      
          if( csize > 0 ) { n1.add( new JDNode("Content Table.h", new long[]{ 0x8000000000000002L, coff, coff + csize - 1 } ) ); }
          if( msize > 0 ) { n1.add( new JDNode("Modules.h", new long[]{ 0x8000000000000002L, moff, moff + msize - 1 } ) ); }
          if( rsize > 0 ) { n1.add( new JDNode("External Ref.h", new long[]{ 0x8000000000000002L, roff, roff + rsize - 1 } ) ); }
          if( indsize > 0 )
          {
            JDNode n = new JDNode("Indirect Symbols", new long[]{ 9, symOff, symSize, strOff, strSize, indoff, indsize } );

            Pointers p; for( int i2 = 0; i2 < ptr.size(); i2++ )
            {
              p = ptr.get(i2); n.add( new JDNode( rPath.get( p.node ).getUserObject().toString() + ".h", new long[]{ 0x8000000000000005L, p.node } ) );
            }

            n1.add( n );
          }
          if( extsize > 0 ) { n1.add( new JDNode("External Relocations.h", new long[]{ 0x8000000000000002L, extoff, extoff + extsize - 1 } ) ); }
          if( lsize > 0 ) { n1.add( new JDNode("Local Relocations.h", new long[]{ 0x8000000000000002L, loff, loff + lsize - 1 } ) ); }

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
      
          root.add( new JDNode( "Load link library (Ordinal=" + ordinal++ + ").h", new long[]{ 0, ref++ } ) );
        }

        //Load a dynamic linker/Set run paths.

        else if( cmd == 0x0E || cmd == 0x1C )
        {
          DTemp.LUINT32("String Offset"); int off = (int)DTemp.value;
          DTemp.String8( cmd == 0x0E ? "Dynamic linker" : "Run Path", ( size + 8 ) - off );

          DTemp.setEvent( cmd == 0x0E ? this::dynInfo : this::rPathInfo ); des.add( DTemp );

          root.add( new JDNode( cmd == 0x0E ? "Dynamic linker.h" : "Run Path.h", new long[]{ 0, ref++ } ) );
        }
    
        //The start of the application.

        else if( cmd == 0x28 )
        {
          DTemp.LUINT64("Programs start address"); main = file.toVirtual( base + (long)DTemp.value );
          DTemp.LUINT64("Stack memory size");

          DTemp.setEvent( this::startInfo ); des.add( DTemp );

          root.add( new JDNode( "Start Address.h", new long[]{ 0, ref++ } ) );
        }
    
        //Thread save state. Command only supports x86, and ARM cores.

        else if( cmd == 0x04 || cmd == 0x05 )
        {
          long t = file.getFilePointer(); file.read(4); int type = file.toLInt() - 1; file.seek( t );
          int reg = 0, rs = 0;
          boolean start = false;

          String[] regs = new String[]{}; int[] rSize = new int[]{};

          if( coreType == 7 && type >= 0 && type < 25 )
          {
            regs = threadStates.x86regs[type]; rSize = threadStates.x86size[type];

            if( rSize.length > regs.length )
            {
              rs = is64bit ? rSize[2] : rSize[1]; regs = threadStates.x86regs[rs]; rSize = threadStates.x86size[rs];
            }
          }
          else if( coreType == 12 && type >= 0 && type < 27 )
          {
            regs = threadStates.ARMregs[type]; rSize = threadStates.ARMsize[type]; 
          }

          if( regs.length > 0 ) 
          {
            DTemp.LUINT32(regs[reg++]); DTemp.LUINT32("Count");

            size -= 8; while( reg < rSize.length && ( size - ( rs = rSize[reg] ) ) >= 0 )
            {
              start = rs < 0; if( start ) { rs = -rs; }

              if( rs == 4 ){ DTemp.LUINT32(regs[reg]); if( start ) { main = (int)DTemp.value; } }
              else if( rs == 8 ){ DTemp.LUINT64(regs[reg]); if( start ) { main = (long)DTemp.value; } }
              else if( rs == 2 ){ DTemp.LUINT16(regs[reg]); }
              else if( rs == 1 ){ DTemp.INT8(regs[reg]); }
              else { DTemp.Other(regs[reg], rs); }

              size -= rs; reg++;
            }

            if( size > 0 ){ DTemp.Other("???", size ); }
          }
          else
          {
            DTemp.LUINT32(( coreType == 7 || coreType == 12 ) ? "BAD Thread Type" : "BAD CPU");
            DTemp.LUINT32("Count"); DTemp.Other("???", size);
          }
      
          DTemp.setEvent( this::stateInfo ); des.add( DTemp );
      
          root.add( new JDNode( "Thread State.h", new long[]{ 0, ref++ } ) );
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

          JDNode n1, tm = new JDNode("sect.h", new long[]{ 0x8000000000000002L, off, off + s - 1 } );
        
          if( cmd == 0x1D ) { n1 = new JDNode( "Code Signature", new long[]{ 0, ref++ } ); }
          else if( cmd == 0x1E ) { n1 = new JDNode( "Split info", new long[]{ 0, ref++ } ); }
          else if( cmd == 0x26) { n1 = new JDNode( "Function Starts", new long[]{ 0, ref++ } ); }
          else if( cmd == 0x29 ) { n1 = new JDNode( "Data in Code", new long[]{ 0, ref++ } ); }
          else if( cmd == 0x2B ) { n1 = new JDNode( "Code Singing", new long[]{ 0, ref++ } ); }
          else if( cmd == 0x2E ) { n1 = new JDNode( "Optimization Hints", new long[]{ 0, ref++ } ); }
          else if( cmd == 0x33 )
          {
            n1 =  new JDNode("Export CMD",  new long[]{ 0, ref++ } );
            tm = new JDNode("Export", new long[]{ 0x0306L, off, off + s - 1 } );
            tm.add( new JDNode( "Dummy.h" ) );
          }
          else { n1 = new JDNode( "Chained Fixups", new long[]{ 0, ref++ } ); }

          n1.add( tm ); root.add( n1 );
        }

        //The linking and method call setup information.

        else if( cmd == 0x22 )
        {
          DTemp.LUINT32("rebase offset"); int roff = (int)base + (int)DTemp.value;
          DTemp.LUINT32("rebase size"); int rsize = (int)DTemp.value;
          DTemp.LUINT32("bind offset"); boff = (int)base + (int)DTemp.value;
          DTemp.LUINT32("bind size"); bsize = (int)DTemp.value;
          DTemp.LUINT32("weak bind offset"); wboff = (int)base + (int)DTemp.value;
          DTemp.LUINT32("weak bind size"); wbsize = (int)DTemp.value;
          DTemp.LUINT32("lazy bind offset"); lboff = (int)base + (int)DTemp.value;
          DTemp.LUINT32("lazy bind size"); lbsize = (int)DTemp.value;
          DTemp.LUINT32("export offset"); int eoff = (int)base + (int)DTemp.value;
          DTemp.LUINT32("export size"); int esize = (int)DTemp.value;

          DTemp.setEvent( this::dynLoaderInfo ); des.add( DTemp );

          JDNode n1 = new JDNode( "Link library setup", new long[]{ 0, ref++ } ), tm;

          if( rsize > 0 )
          {
            tm =  new JDNode("rebase", new long[]{ 0x8000000000000102L, roff, roff + rsize - 1 } );

            tm.add( new JDNode( "Opcodes.h", new long[]{ 2, roff, roff + rsize } ) );
            tm.add( new JDNode( "Actions.h", new long[]{ 3, roff, roff + rsize } ) );
            n1.add( tm );
          }
        
          if( bsize > 0 )
          {
            tm = new JDNode("bind", new long[]{ 0x8000000000000202L, boff, boff + bsize - 1 } );

            if( bind >= 0 ) { tm.add( new JDNode( "Pointers.h", new long[]{ 0x8000000000000005L, bind } ) ); }

            tm.add( new JDNode( "Opcodes.h", new long[]{ 4, boff, boff + bsize } ) );
            tm.add( new JDNode( "Actions.h", new long[]{ 5, boff, boff + bsize } ) );
            n1.add( tm );
          }
        
          if( wbsize > 0 )
          {
            tm = new JDNode("week bind", new long[]{ 0x8000000000000202L, wboff, wboff + wbsize - 1 } );

            tm.add( new JDNode( "Opcodes.h", new long[]{ 4, wboff, wboff + wbsize } ) );
            tm.add( new JDNode( "Actions.h", new long[]{ 5, wboff, wboff + wbsize } ) );
            n1.add( tm );
          }

          if( lbsize > 0 )
          {
            tm = new JDNode("lazy bind", new long[]{ 0x8000000000000202L, lboff, lboff + lbsize - 1 } );

            if( lazyBind >= 0 ) { tm.add( new JDNode( "Pointers.h", new long[]{ 0x8000000000000005L, lazyBind } ) ); }

            tm.add( new JDNode( "Opcodes.h", new long[]{ 4, lboff, lboff + lbsize } ) );
            tm.add( new JDNode( "Actions.h", new long[]{ 5, lboff, lboff + lbsize } ) );
            n1.add( tm );
          }
        
          if( esize > 0 ) { tm =  new JDNode("Export", new long[]{ 0x0306L, eoff, eoff + esize - 1 } ); tm.add( new JDNode( "Dummy" ) ); n1.add( tm ); }

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
    }
    catch( Exception err )
    {
      JOptionPane.showMessageDialog(null, "Encountered an Bad Load command.\r\n\r\nFile may be corrupted!");
    }

    if( loadCMDSize < 0 ) { JOptionPane.showMessageDialog(null, "Error load commands extend past load commands size.\r\n\r\nFile may be corrupted!"); }

    //Load in symbols and methods.

    if( core != null )
    {
      //Modern Mack format.

      if( wboff > 0 ) { linkEdit.bindSyms( wboff, wboff + wbsize, true ); }
      if( lboff > 0 ) { linkEdit.bindSyms( lboff, lboff + lbsize, true ); }
      if( boff > 0 ) { linkEdit.bindSyms( boff, boff + bsize, true ); }

      //We only map the methods by symbol table if the modern compressed linker format is not present == -1.

      linkEdit.mapSyms( symOff, symSize, strOff, strSize, indoff, indsize, ( wboff | lboff | boff ) == -1 );
    }

    //Sections that only have machine code.

    if( code.getChildCount() > 0 ) { App.add( code ); }

    //The programs main entry point.

    if( main != 0 ) { App.add( new JDNode("Program Start (Machine Code).h", new long[]{ 0x8000000000000004L, main } ) ); }
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

  private static final String vmPort = "This controls the virtual memory access permissions in the CPU to prevent programs from doing whatever they want while running.<br /><br />" +
  "The flag settings are intended to be viewed in binary. Each setting that is active is a binary digit that is set 1. The following bits are used for the sections attributes.<br /><br />" +
  "<table border='1'>" +
  "<tr><td>00000000000000000000000000000001</td><td>The section allows the CPU to read bytes in RAM in this section (Read setting).</td></tr>" +
  "<tr><td>00000000000000000000000000000010</td><td>The section allows the CPU to write bytes in RAM to this section (Write setting).</td></tr>" +
  "<tr><td>00000000000000000000000000000100</td><td>The section allows the CPU to start running code (Execute setting).</td></tr>" +
  "</table><br />" +
  "The settings must be set properly for each section of the program when loaded to memory otherwise the program will crash as the CPU, and RAM mem has the feature built in.<br /><br />" +
  "Manually changing or setting these bits can enable or disable hardware based security while program is running.";

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
    "<html>Maximum virtual memory protection.<br /><br />" + vmPort + "</html>",
    "<html>Initial virtual memory protection.<br /><br />" + vmPort + "</html>",
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

  private static final String[] enDataInfo = new String[]
  {
    cmdType, cmdSize,
    "<html>Location to encrypted data.</html>",
    "<html>The size of the encrypted data.</html>",
    "<html>Data ID number.</html>"
  };

  private static final String[] symDataInfo = new String[]
  {
    cmdType, cmdSize,
    "<html>This is the file position to the symbol table." + offsets + "</html>",
    "<html>This is the number of symbols at the symbol table offset.</html>",
    "<html>This is the file position to the string table. This value is added with the name value in the symbol table to find create the file position to the name of a symbol." + offsets + "</html>",
    "<html>This is the size of the string table. If the name value is bigger than the string table size then we know we are reading outside the names and that there is something wrong.</html>"
  };

  private static final String[] symInfo = new String[]
  {
    cmdType, cmdSize,
    "<html>The first symbol number in the symbol table that defines local symbols.<br /><br />" +
    "Local symbols are generally things like debug symbols that store line numbers for address positions in code.</html>",
    "<html>This is how many local symbols. The local symbols are grouped together one after another in the symbol table.</html>",
    "<html>The first symbol number in the symbol table that defines external symbols.<br /><br />" +
    "External symbols are locations in the program you can call as a method/function, or read data/value.</html>",
    "<html>This is how many external symbols. The external symbols are grouped together one after another in the symbol table.</html>",
    "<html>The first symbol number in the symbol table that defines undefined symbols.<br /><br />" +
    "Undefined symbols are symbols that do not exist in this binary that have to be found as an external symbol in a link library.</html>",
    "<html>This is how many undefined symbols. The undefined symbols are grouped together one after another in the symbol table.</html>",
    "<html>File offset to table of contents.</html>",
    "<html>Number of entries in table of contents.</html>",
    "<html>File offset to module table.</html>",
    "<html>Number of module table entries.</html>",
    "<html>Offset to referenced symbol table.</html>",
    "<html>Number of referenced symbol table entries.</html>",
    "<html>File offset to the indirect symbol table.</html>",
    "<html>Number of indirect symbol table entries.</html>",
    "<html>Offset to external relocation entries.</html>",
    "<html>Number of external relocation entries.</html>",
    "<html>Offset to local relocation entries.</html>",
    "<html>Number of local relocation entries.</html>"
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

  private static final String[] rPathInfo = new String[]
  {
    cmdType, cmdSize,
    "<html>" + offStr + "</html>",
    "<html>" + Str + " This tells us the path to use in looking for files in this application.</html>"
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

  private static final String[] dynLoaderInfo = new String[]
  {
    cmdType, cmdSize,
    "<html>rebase offset</html>",
    "<html>rebase size</html>",
    "<html>bind offset</html>",
    "<html>bind size</html>",
    "<html>weak bind offset</html>",
    "<html>weak bind size</html>",
    "<html>lazy bind offset</html>",
    "<html>lazy bind size</html>",
    "<html>export offset</html>",
    "<html>export size</html>"
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

  private static final String[] stateInfo = new String[]
  {
    cmdType, cmdSize,
    "<html>The save state of the Processor.</html>",
    "<html>Count is number of 32 bits numbers. Note x86 has variable length registers so we pad some of them with reserved bytes to keep the count a multiple of 4 bytes.</html>"
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
      "Segment data locations can be referenced by name, or segment load command number. The first segment load command starts at 0 and we increment upward per segment load command.<br /><br />" +
      "The link library section, if there is one uses the segment number as the location to a pointer plus an defined offset.<br /><br />" +
      "<table border='1'>" +
      "<tr><td>__PAGEZERO</td><td>Fills the area the program is going to load into RAM memory with zeros.</td></tr>" +
      "<tr><td>__TEXT</td><td>The tradition UNIX text segment. Contains machine code, and data types or strings.</td></tr>" +
      "<tr><td>__DATA</td><td>The real initialized data section. Data that should be loaded before program starts.</td></tr>" +
      "<tr><td>__OBJC</td><td>Objective-C runtime segment.</td></tr>" +
      "<tr><td>__ICON</td><td>The icon segment.</td></tr>" +
      "<tr><td>__LINKEDIT</td><td>Contains raw data used by the dynamic linker: symbol/string/relocation table entries.</td></tr>" +
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
      "Sections can be referenced by segment num/name, then section name, or only by section number. The first section number is 1 and we increment upward per section across load command.<br /><br />" +
      "By starting at section 1 allows us to use section number 0 as no section. This is important if we want to define a callable method, but know it is not part of the program.<br /><br />" +
      "Instead we use the symbol table to define the method name and which link library the method can be found in using the library load command number (ordinal).<br /><br />" +
      "The symbol table uses section numbers to tell us which part of the program the symbol can be found if not set 0.<br /><br />" +
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
      "</table><br />It is important to always read the flag setting to know what type of section the data is, as the names can be set to whatever we like actually.</html>" );
    }
    else
    {
      info( sectInfo[i] );
    }
  }

  private void enDataInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>The encrypted data segment uses your device's unique encryption key to encode data that can only be read on this device, such as securely storing a password or user info in the app.<br /><br />" +
      "Copying this app to another device may make this data unreadable and will be overwritten by the other device's unique encryption key, also resetting the data.</html>" );
    }
    else
    {
      info( enDataInfo[i] );
    }
  }

  private void rPathInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>We can specify the default location to look for files. This allows us to not have to use the full file path location for querying files.</html>" );
    }
    else
    {
      info( rPathInfo[i] );
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
      info( "<html>This is a binary we wish to load. Every binary has a symbol table that gives parts of the program names for address locations.<br /><br />" +
      "Not all symbols locate to a section of code, and are defined by type. We can define symbols that use a library ordinal to spefiy which binary the external symbol can be found in.<br /><br />" +
      "The symbol types are explained in detail in the \"symbol table\". Additionaly mondern Mach binariyes use a new command called \"link library setup\".<br /><br />" +
      "The command \"link library setup\" section should be used to setup methods/functions if present instead of the symbol table.<br /><br />" +
      "A Mach binary may inculde both sets of information to maintain compatibility to older systems.<br /><br />" +
      "The symbols that need to be set are a jump or call operation which are to be set to the location of an exportable method from another binary. We call these jumps and calls stubs.<br /><br />" +
      "It is the dynamic linkers job to make sure our symbols that need to be binded locate to the exportable symbols when the processor hits the call and jump instructions that read the value.</html>" );
    }
    else
    {
      info( dynlInfo[i] );
    }
  }

  private void dynLoaderInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>How the modern dyld linker works. First, we must define which binaries we want to link using the load link library commands.<br /><br />" +
      "The export section defines a method name and location to where the machine code starts for the method in the binary.<br /><br />" +
      "Each link library we load is given a number starting from 1 incrementing upward. The ordinal number is used to specify which link library the method is in.<br /><br />" +
      "Additionally, each export section is combined into one large list that can be used to look up the address location of a function/method name.<br /><br />" +
      "The ordinal speeds the lookup process by telling the linker where to start looking in the list.<br /><br />" +
      "A week method in the week bind section can be replaced by another method if existent in another link library giving us some flexibility to customize method calls.<br /><br />" +
      "The mac programs machine code loads a number from a section called a pointer list and uses it as the location to call the function/method.<br /><br />" +
      "The section usually named \"__stubs\" reads the location of a pointer and uses the read number as the location to the method.<br /><br />" +
      "This way, the programs machine code never has to be touched in setting the locations to methods in another program.<br /><br />" +
      "The bind section tells us which locations to set to a method from another binary file export section.<br /><br />" +
      "The lazy bind section does not need each method set to the export method of another binary as the pointers locate to a section usually named \"__stub_helper\" which calls the method \"dyld_stub_binder()\".<br /><br />" +
      "\"dyld_stub_binder()\" sets the location to the pointer, and calls the method. Any method/function call to the same lazy pointer in other parts of machine code then locates straight to the method.<br /><br />" +
      "The lazy bind section only loads methods in as they are needed by locating to a small code in stud helper. The bind section must include \"dyld_stub_binder()\" as it is needed before the program starts.<br /><br />" +
      "The rebase section is used if the program is not placed at its pre-calculated address locations defined in the section load commands as it is occupied by another program.<br /><br />" +
      "The rebase section adjusts the locations in the lazy bind section as they locate to a pre-calculated position in a section usually named \"__stub_helper\" which calls the method \"dyld_stub_binder()\".<br /><br />" +
      "If the program is offset by 50 bytes then every lazy pointer must be added by 50 to match the original position of the stud helper machine code instruction location.</html>" );
    }
    else
    {
      info( dynLoaderInfo[i] );
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

  private void symDataInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>The symbols define the method calls and function calls in a mac binary, exportable functions/methods, and data.<br /><br />" +
      "The only time we load in link library methods using the \"symbol table\", and \"Symbol info\" is if there is no \"Link library setup\" section that uses the modern dyld linker format.<br /><br />" +
      "A modern Mach binary may keep only the debug symbols such as line numbers relative to machine code position, and locations of variable names.<br /><br />" +
      "Some Mach binaries may include everything in the symbol table to maintain backwards compatibility.</html>" );
    }
    else
    {
      info( symDataInfo[i] );
    }
  }

  private void symInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>The original set of symbol information in the \"Symbol table\" which contains the symbol names, and type must also be present when this load command is present.<br /><br />" +
      "This section uses the \"symbol table\" by symbol number. The \"symbol table\" is structured to go in order by symbol types Local, External, and Undeifnined.<br /><br />" +
      "This section defines where symbols start in the \"Symbol table\" as types Local, External, and undefined. It specifies how many are grouped together in the \"Symbol table\".<br /><br />" +
      "Undefined symbols are symbols that do not exist in any section of this binary, but exist in a link library.<br /><br />" +
      "Each link library we load is given a number starting from 1 incrementing upward. The ordinal number is used to specify which link library the method is in.<br /><br />" +
      "We handle undefined symbols by reading their ordinal number in the \"Symbol table\" and looking for the external symbol in the other binary.<br /><br />" +
      "External symbols tell us where to read the \"symbol table\" to find number of external symbols. We use this to quickly check a binary for an external symbol for our undefined symbols.<br /><br />" +
      "Local symbols are used for address locations in the programs code is a particular line number in the source code file. Used only for debugging code.<br /><br />" +
      "Lastly the \"Indirect symbol\" table tells us which undefined symbols by number that call method/function for each pointer in the program, and jump table.<br /><br />" +
      "The only time we load in link library methods using the \"symbol table\", and \"Symbol info\" is if there is no \"Link library setup\" section that uses the modern dyld linker format.<br /><br />" +
      "A modern Mach binary may keep only the debug symbols such as line numbers relative to machine code position, and locations of variable names.<br /><br />" +
      "Some Mach binaries may include everything in the symbol table to maintain backwards compatibility.</html>" );
    }
    else
    {
      info( symInfo[i] );
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

  private void stateInfo( int i )
  {
    if( i < 0 )
    {
      info("<html>The thread state command stores the state of the CPU inculding the processor instruction position register.<br /><br />" +
      "Some older software use the thread state command to define the programs start address.<br /><br />" +
      "It is recommended to use the new main entry point command to define the start address of the program.</html>");
    }
    else if( i < 4 )
    {
      info( stateInfo[i] );
    }
    else { info("<html>Value to set to an processor register.</html>"); }
  }

  private void blank( int i )
  {
    info("<html>No information on this section or its properties yet.</html>");
  }
}
