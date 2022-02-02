package Format.MACDecode;

import swingIO.*;
import swingIO.tree.*;
import javax.swing.tree.*;

import core.x86.*;

public class Headers extends Data
{
  public JDNode readMAC( JDNode app ) throws java.io.IOException
  {
    //Begin reading the MacOS header.

    DTemp = new Descriptor( file ); des.add( DTemp ); JDNode n = new JDNode("Mac Header", new long[]{ 0, ref++ } );
    
    DTemp.LUINT32("Signature");
    
    is64bit = (int)DTemp.value == -17958193;

    //Mac universal binaries have more than one loadable mac program that can be switch between.
    
    if( (int)DTemp.value == -1095041334 )
    {
      DTemp.setEvent( this::UMacHInfo );
      
      DTemp.UINT32("Binaries"); int b = (int)DTemp.value;
    
      JDNode Apps;
          
      for( int i = 0; i < b; i++ )
      {
        DTemp.Array("Mac Binary #" + i + "", 20 );
        DTemp.UINT32("CPU Type");
        DTemp.UINT32("CPU Sub Type");
        DTemp.UINT32("File Offset");
    
        Apps = new JDNode("App #" + i + "", new long[] { 1, (int)DTemp.value } ); Apps.add( new JDNode("") );
    
        n.add( Apps );
    
        DTemp.UINT32("Size");
        DTemp.UINT32("Align");
      }
    }

    //Begin reading the mac header. Note some binaries can have more than one loadable binary.

    else
    {
      //Uninitialize the loaded app before loading a new one.

      if( App != null )
      {
        //Leave only the universal mac header. There should only be one at the start of the binary.

        java.util.ArrayList<Descriptor> bd = new java.util.ArrayList<Descriptor>();

        bd.add( des.get(0) ); des = bd; ref = 1;

        des.add( DTemp ); n.setArgs( new long[]{ 0, ref++ } );

        //Remove everything read under the other App.

        App.removeAllChildren(); App.add( new JDNode( "" ) );

        //Reset the applications virtual address space.

        file.resetV(); core.clear(); ds.clear();

        //Update the tree.

        ((DefaultTreeModel)tree.getModel()).reload((TreeNode)((DefaultTreeModel)tree.getModel()).getRoot());
      }

      //Set the current node for the application that is being read.
      
      base = file.getFilePointer() - 4; App = app; DTemp.setEvent( this::MacHInfo );

      //Read the application header.

      DTemp.LUINT32("CPU Type"); coreType = ((int)DTemp.value) & 0xFF;
      DTemp.LUINT32("CPU Sub Type");
      DTemp.LUINT32("File Type");
      DTemp.LUINT32("Commands"); loadCMD = (int)DTemp.value;
      DTemp.LUINT32("Commands Size");
      DTemp.LUINT32("Flags");
    
      if( is64bit ){ DTemp.UINT32("Reserved"); }

      //Setup the processor core.

      if( ( coreType & 0xFF ) == 0x07 )
      {
        if( core == null || core.type() != 0 ) { core = new X86( file ); } else { core.setTarget( file ); }
            
        core.setBit( is64bit ? X86.x86_64 : X86.x86_32 );
                          
        core.setEvent( this::Dis ); coreLoaded = true;
      }
      else { coreLoaded = false; }
    }

    return( n );
  }

  private static final String Singatures = "<html>The MacOS binary format uses two signature types.<br /><br />" +
  "<table border='1'>" +
  "<tr><td>Hex Value</td><td>Binary Type</td></tr>" +
  "<tr><td>CE FA ED FE</td><td>32 bit binary application.</td></tr>" +
  "<tr><td>CF FA ED FE</td><td>64 bit binary application.</td></tr>" +
  "</table><br />" +
  "Signature type CA FE BA BE is used for universal binaries.<br /><br />" +
  "A universal binary has more than one binary application in the file for different core types which begin with Mac Headers as well.<br /><br />" +
  "It is not useful most of the time since majority of all systems run x86 instructions, or ARM instructions natively.</html>";

  private static final String CPU_type1 = "<html>This is the processor type that the binary is meant to run on.<br /><br />";
  private static final String CPU_type2 = "<table border='1'>" +
  "<tr><td>Core</td><td>Type value Hex</td></tr>" +
  "<tr><td>VAX</td><td>01</td></tr>" +
  "<tr><td>ROMP</td><td>02</td></tr>" +
  "<tr><td>NS32032</td><td>04</td></tr>" +
  "<tr><td>NS32332</td><td>05</td></tr>" +
  "<tr><td>MC680x0</td><td>06</td></tr>" +
  "<tr><td>X86</td><td>07</td></tr>" +
  "<tr><td>VAX</td><td>08</td></tr>" +
  "<tr><td>MC98000</td><td>0A</td></tr>" +
  "<tr><td>HPPA</td><td>0B</td></tr>" +
  "<tr><td>ARM</td><td>0C</td></tr>" +
  "<tr><td>MC88000</td><td>0D</td></tr>" +
  "<tr><td>SPARC</td><td>0E</td></tr>" +
  "<tr><td>I860 (big-endian)</td><td>0F</td></tr>" +
  "<tr><td>I860 (little-endian)</td><td>10</td></tr>" +
  "<tr><td>RS6000</td><td>11</td></tr>" +
  "<tr><td>POWERPC</td><td>12</td></tr>" +
  "</table><br />" +
  "The most used types are 07, and 0C which are x86, and ARM.<br /><br />" +
  "MacOS is switching from Intel x86 cores to ARM.<br /><br />" +
  "Note that MacOS and iPhone also use the same Mach-O format. iPhone used ARM way before MacOS did.</html>";

  private static final String CPU_Subx86 = "The First two Hex digit is the CPU sub type.<br /><br />" +
  "<table border='1'>" +
  "<tr><td>Hex Value.</td><td>CPU version.</td></tr>" +
  "<tr><td>03 00 00 00</td><td>All x86 cores.</td></tr>" +
  "<tr><td>04 00 00 00</td><td>Optimized for 486 or newer.</td></tr>" +
  "<tr><td>84 00 00 00</td><td>Optimized for 486SX or newer.</td></tr>" +
  "<tr><td>05 00 00 00</td><td>Optimized for 586, pentium or newer.</td></tr>" +
  "<tr><td>16 00 00 00</td><td>Optimized for Pentium professional or newer.</td></tr>" +
  "<tr><td>36 00 00 00</td><td>Optimized for Pentium M3 or newer.</td></tr>" +
  "<tr><td>56 00 00 00</td><td>Optimized for Pentium M5 or newer.</td></tr>" +
  "<tr><td>67 00 00 00</td><td>Optimized for Celeron or newer.</td></tr>" +
  "<tr><td>77 00 00 00</td><td>Optimized for Celeron Mobile.</td></tr>" +
  "<tr><td>08 00 00 00</td><td>Optimized for Pentium 3 or newer.</td></tr>" +
  "<tr><td>18 00 00 00</td><td>Optimized for Pentium 3-M or newer.</td></tr>" +
  "<tr><td>28 00 00 00</td><td>Optimized for Pentium 3-XEON or newer.</td></tr>" +
  "<tr><td>09 00 00 00</td><td>Optimized for Pentium M or newer.</td></tr>" +
  "<tr><td>0A 00 00 00</td><td>Optimized for Pentium-4 or newer.</td></tr>" +
  "<tr><td>1A 00 00 00</td><td>Optimized for Pentium-4-M or newer.</td></tr>" +
  "<tr><td>0B 00 00 00</td><td>Optimized for Itanium or newer.</td></tr>" +
  "<tr><td>1B 00 00 00</td><td>Optimized for Itanium-2 or newer.</td></tr>" +
  "<tr><td>0C 00 00 00</td><td>Optimized for XEON or newer.</td></tr>" +
  "<tr><td>1C 00 00 00</td><td>Optimized for XEON-MP or newer.</td></tr>" +
  "</table><br /><br />" +
  "Over the years Intel/AMD have added new instructions to x86 cores to perform different arithmetic operations to speed up performance.<br /><br />" +
  "The instructions used binary codes that did nothing on prior cores. This means a x86 program optimized for 486 can run on a newer core than 486.<br /><br />" +
  "Most software is compiled using no fancy instructions meaning the code is compatible to all x86 cores.<br /><br />" +
  "It is still important to test if a particular instruction does nothing, or does said arithmetic operation before the CPU is set to the programs instruction codes.";

  private static final String CPU_SubARM = "The First two Hex digit is the CPU sub type.<br /><br />" +
  "The last two hex digits are used for capability settings on arm64e platforms (Experimental).<br /><br />" +
  "The last two hex digits can be ignored on all ARM cores without encountering problems.<br /><br />" +
  "<table border='1'>" +
  "<tr><td>Hex Value.</td><td>CPU version.</td></tr>" +
  "<tr><td>00 00 00 00</td><td>All ARM cores.</td></tr>" +
  "<tr><td>01 00 00 00</td><td>Optimized for ARM-A500 ARCH or newer.</td></tr>" +
  "<tr><td>02 00 00 00</td><td>Optimized for ARM-A500 or newer.</td></tr>" +
  "<tr><td>03 00 00 00</td><td>Optimized for ARM-A440 or newer.</td></tr>" +
  "<tr><td>04 00 00 00</td><td>Optimized for ARM-M4 or newer.</td></tr>" +
  "<tr><td>05 00 00 00</td><td>Optimized for ARM-V4T or newer.</td></tr>" +
  "<tr><td>06 00 00 00</td><td>Optimized for ARM-V6 or newer.</td></tr>" +
  "<tr><td>07 00 00 00</td><td>Optimized for ARM-V5TEJ or newer.</td></tr>" +
  "<tr><td>08 00 00 00</td><td>Optimized for ARM-XSCALE or newer.</td></tr>" +
  "<tr><td>09 00 00 00</td><td>Optimized for ARM-V7 or newer.</td></tr>" +
  "<tr><td>0A 00 00 00</td><td>Optimized for ARM-V7F (Cortex A9) or newer.</td></tr>" +
  "<tr><td>0B 00 00 00</td><td>Optimized for ARM-V7S (Swift) or newer.</td></tr>" +
  "<tr><td>0C 00 00 00</td><td>Optimized for ARM-V7K (Kirkwood40) or newer.</td></tr>" +
  "<tr><td>0D 00 00 00</td><td>Optimized for ARM-V8 or newer.</td></tr>" +
  "<tr><td>0E 00 00 00</td><td>Optimized for ARM-V6M or newer.</td></tr>" +
  "<tr><td>0F 00 00 00</td><td>Optimized for ARM-V7M or newer.</td></tr>" +
  "<tr><td>10 00 00 00</td><td>Optimized for ARM-V7EM or newer.</td></tr>" +
  "</table><br /><br />" +
  "Over the years ARM has grow a lot with new instructions to perform different arithmetic operations to speed up performance.<br /><br />" +
  "The instructions used binary codes that did nothing on prior cores. This means a ARM-V6 optimized program can run on a newer core like ARM-V8.<br /><br />" +
  "In the case the all type no fancy instructions are used meaning the code is compatible to all ARM cores.<br /><br />" +
  "It is still important to test if a particular instruction does nothing, or does said arithmetic operation before the CPU is set to the programs instruction codes.";

  private static final String[] MacHeaderInfo = new String[]
  {
    Singatures,
    CPU_type1 + "The first two hex digits is the CPU type.<br /><br />" +
    "The last two hex digits are 01 for 64 bit, and 02 for 32 bit version of the core.<br /><br />" + CPU_type2,
    "<html>The CPU sub type is used to specify features the core should have support for as the code is optimized for a particular core or newer.<br /><br />" +
    "Meaning some earlier cores may encounter operation codes that do nothing that are usable in newer version of the core.</html>",
    "<html>How the file is intended to be used.<br /><br />" +
    "<table border='1'>" +
    "<tr><td>File Type Value</td><td>Description</td></tr>" +
    "<tr><td>1</td><td>Relocatable object file.</td></tr>" +
    "<tr><td>2</td><td>Demand paged executable file.</td></tr>" +
    "<tr><td>3</td><td>Fixed VM shared library file.</td></tr>" +
    "<tr><td>4</td><td>Core file.</td></tr>" +
    "<tr><td>5</td><td>Preloaded executable file.</td></tr>" +
    "<tr><td>6</td><td>Dynamicly bound shared library file.</td></tr>" +
    "<tr><td>7</td><td>Dynamic link editor.</td></tr>" +
    "<tr><td>8</td><td>Dynamicly bound bundle file.</td></tr>" +
    "<tr><td>9</td><td>Shared library stub for static linking only, no section contents.</td></tr>" +
    "<tr><td>10</td><td>Companion file with only debug sections.</td></tr>" +
    "<tr><td>11</td><td>x86_64 kexts.</td></tr>" +
    "<tr><td>12</td><td>a file composed of other Mach-Os to be run in the same userspace sharing a single linkedit.</td></tr>" +
    "</table></html>",
    "<html>Number of load commands.</html>",
    "<html>The size of all the load commands.</html>",
    "<html>The flags settings should be viewed in binary.<br /><br />" +
    "The value 00000000000000000000000000010000 means the file has it's dynamic undefined references prebound.<br /><br />" +
    "More than one binary digit can be set for more than one setting, or information about the file.<br /><br />" +
    "The table bellow show the break down of what bits have to be set for each setting.<br /><br />" +
    "<table border=\"1\">" +
    "<tr><td>00000000000000000000000000000001</td><td>The object file has no undefined references.</td></tr>" +
    "<tr><td>00000000000000000000000000000010</td><td>The object file is the output of an incremental link against a base file and can't be link edited again.</td></tr>" +
    "<tr><td>00000000000000000000000000000100</td><td>The object file is input for the dynamic linker and can't be staticly link edited again.</td></tr>" +
    "<tr><td>00000000000000000000000000001000</td><td>The object file's undefined references are bound by the dynamic linker when loaded.</td></tr>" +
    "<tr><td>00000000000000000000000000010000</td><td>The file has its dynamic undefined references prebound.</td></tr>" +
    "<tr><td>00000000000000000000000000100000</td><td>The file has its read-only and read-write segments split.</td></tr>" +
    "<tr><td>00000000000000000000000001000000</td><td>The shared library init routine is to be run lazily via catching memory faults to its writeable segments (obsolete).</td></tr>" +
    "<tr><td>00000000000000000000000010000000</td><td>The image is using two-level name space bindings.</td></tr>" +
    "<tr><td>00000000000000000000000100000000</td><td>The executable is forcing all images to use flat name space bindings.</td></tr>" +
    "<tr><td>00000000000000000000001000000000</td><td>This umbrella guarantees no multiple defintions of symbols in its sub-images so the two-level namespace hints can always be used.</td></tr>" +
    "<tr><td>00000000000000000000010000000000</td><td>Do not have dyld notify the prebinding agent about this executable.</td></tr>" +
    "<tr><td>00000000000000000000100000000000</td><td>The binary is not prebound but can have its prebinding redone. only used when MH_PREBOUND is not set.</td></tr>" +
    "<tr><td>00000000000000000001000000000000</td><td>Indicates that this binary binds to all two-level namespace modules of its dependent libraries.</td></tr>" +
    "<tr><td>00000000000000000010000000000000</td><td>Safe to divide up the sections into sub-sections via symbols for dead code stripping.</td></tr>" +
    "<tr><td>00000000000000000100000000000000</td><td>The binary has been canonicalized via the unprebind operation.</td></tr>" +
    "<tr><td>00000000000000001000000000000000</td><td>The final linked image contains external weak symbols.</td></tr>" +
    "<tr><td>00000000000000010000000000000000</td><td>The final linked image uses weak symbols.</td></tr>" +
    "<tr><td>00000000000000100000000000000000</td><td>When this bit is set, all stacks in the task will be given stack execution privilege.</td></tr>" +
    "<tr><td>00000000000001000000000000000000</td><td>When this bit is set, the binary declares it is safe for use in processes with uid zero.</td></tr>" +
    "<tr><td>00000000000010000000000000000000</td><td>When this bit is set, the binary declares it is safe for use in processes when UGID is true.</td></tr>" +
    "<tr><td>00000000000100000000000000000000</td><td>When this bit is set on a dylib, the static linker does not need to examine dependent dylibs to see if any are re-exported.</td></tr>" +
    "<tr><td>00000000001000000000000000000000</td><td>When this bit is set, the OS will load the main executable at a random address.</td></tr>" +
    "<tr><td>00000000010000000000000000000000</td><td>Only for use on dylibs. When linking against a dylib that has this bit set, the static linker will automatically not create a load command to the dylib if no symbols are being referenced from the dylib.</td></tr>" +
    "<tr><td>00000000100000000000000000000000</td><td>Contains a section of type S_THREAD_LOCAL_VARIABLES.</td></tr>" +
    "<tr><td>00000001000000000000000000000000</td><td>When this bit is set, the OS will run the main executable with a non-executable heap even on platforms (e.g. i386) that don't require it.</td></tr>" +
    "<tr><td>00000010000000000000000000000000</td><td>The code was linked for use in an application.</td></tr>" +
    "<tr><td>00000100000000000000000000000000</td><td>The external symbols listed in the nlist symbol table do not include all the symbols listed in the dyld info.</td></tr>" +
    "<tr><td>00001000000000000000000000000000</td><td>Allow LC_MIN_VERSION_MACOS and LC_BUILD_VERSION load commands with the platforms macOS, macCatalyst, iOSSimulator, tvOSSimulator and watchOSSimulator.</td></tr>" +
    "<tr><td>10000000000000000000000000000000</td><td>Only for use on dylibs. When this bit is set, the dylib is part of the dyld shared cache, rather than loose in the filesystem.</td></tr>" +
    "<tr><td>0xxx0000000000000000000000000000</td><td>The digits marked with \"x\" have no use yet, and are reserved for future use.</td></tr>" +
    "</table></html>",
    "<html>This is reserved for use with 64 bit programs in the future.</html>"
  };

  public void MacHInfo( int i )
  {
    if( i < 0 )
    {
      tree.expandPath( tree.getSelectionPath() );
      info("<html>The MacOS header identifies the type of core the machine code binary is intended to run on.<br /><br />" +
      "It also specifies the number of loading commands to map and load the binary into memory.</html>"); return;
    }
    else if ( i == 2 )
    {
      if( coreType == 7 )
      {
        info( CPU_Subx86 ); return;
      }
      else if( coreType == 12 )
      {
        info( CPU_SubARM ); return;
      }
    }

    info( MacHeaderInfo[ i ] );
  }

  private static final String[] UMacHeaderInfo = new String[]
  {
    Singatures,
    "<html>Number of binaries in the universal binary.</html>",
    "<html>Binary application information.</html>",
    CPU_type1 + "The last two hex digits is the CPU type.<br /><br />" +
    "The first two hex digits are 01 for 64 bit, and 02 for 32 bit version of the core.<br /><br />" + CPU_type2,
    "<html>The CPU sub type is used to specify features the core should have support for as the code is optimized for a particular core or newer.<br /><br />" +
    "Meaning some earlier cores may encounter operation codes that do nothing that are usable in newer version of the core.</html>",
    "<html>File position to application.</html>",
    "<html>The size of the application in the file.</html>",
    "<html>Section alignment in power of 2.</html>"
  };

  public void UMacHInfo( int i )
  {
    if( i < 0 )
    {
      tree.expandPath( tree.getSelectionPath() );
      info("<html>The MacOS header identifies the type of core the machine code binary is intended to run on.<br /><br />" +
      "It also specifies the number of loading commands to map and load the binary into memory.</html>");
    }
    else
    {
      info( i > 1 ? UMacHeaderInfo[ ( ( i - 2 ) % 6 ) + 2 ] : UMacHeaderInfo[ i ] );
    }
  }
}
