package Format;

import java.io.*;

import swingIO.*;
import swingIO.tree.*;
import javax.swing.tree.*;
import Format.EXEDecode.*;
import core.Core;

//Processor cores.

import core.x86.*; //X86.

public class EXE extends Data implements JDEventListener
{
  //Descriptors.

  private Descriptor[][] des = new Descriptor[17][];

  private JDNode root, MSDos = new JDNode("MZ Header", 0);
  private JDNode[] sections = new JDNode[]
  {
    new JDNode("function Export Table.h", 2),
    new JDNode("DLL Import Table.h", 3),
    new JDNode("Resource Files.h", 4),
    new JDNode("Exception Table.h", 5),
    new JDNode("Security Level Settings.h", 6),
    new JDNode("Relocations.h", 7),
    new JDNode("DEBUG TABLE.h", 8),
    new JDNode("Description/Architecture.h", 9),
    new JDNode("Machine Value.h", 10),
    new JDNode("Thread Storage Location.h", 11),
    new JDNode("Load System Configuration.h", 12),
    new JDNode("Import Table of Functions inside program.h", 13),
    new JDNode("Import Address Setup Table.h", 14),
    new JDNode("Delayed Import Table.h", 15),
    new JDNode("COM Runtime Descriptor.h", 16)
  };

  //plug in the executable Readers

  private static final Headers Header = new Headers();

  private static final sec[] Reader = new sec[] { new DLLExport(), new DLLImport(), new Resource(), null, null, null, null, null, null, null, null, null, null, null, null };

  //Section Info.

  public static final String noReader = "No reader, for this section.";
  public static final String[] SInfo = new String[]
  {
    //DOS header.
    "<html>This is the original DOS header. Which must be at the start of all windows binary files.<br /><br />Today the reserved bytes are used to locate to the new Portable executable header format.<br /><br />" +
    "However, on DOS this header still loads as the reserved bytes that locate to the PE header do nothing in DOS.<br /><br />Thus the small 16 bit binary at the end will run. " +
    "Which normally contains a small 16 bit code that prints the message that this program can not be run in DOS mode.<br /><br />Though it can be a full-fledged DOS version of the program.</html>",
    //Microsoft headers.
    "<html>The headers setup the Microsoft binary virtual space.<br /><br />Otherwise The import table can not be located.<br /><br />" +
    "Export Table can not be located.<br /><br />" +
    "Files that are included in the binary. Called Resource Files. Also can not be located.<br /><br />" +
    "Nether can the machine code Start position be located.</html>",
    //Export
    "<html>Once the headers are read, then the program is setup in virtual space.<br /><br />" +
    "The Export section is a list of names that locate to a machine code in RAM.<br /><br />" +
    "Methods can be imported by name, or by number they are in the export Address list.<br /><br />" +
    "A import table specifies which files to load to memory. If not already loaded.<br /><br />" +
    "The method list in the import table is replaced with the export locations in RAM from the other file.<br /><br />" +
    "This allows the other binary to directly run methods by using the import location as a relative address.</html>",
    //Import.
    "<html>Methods that are imported from other files using the export table section.<br /><br />" +
    "Each import file is loaded to RAM memory. Each import has two method lists.<br /><br />" +
    "The first list is wrote over in RAM with the location to each export method location.<br /><br />" +
    "This allows the binary to directly run methods without rewriting, or changing machine code.<br /><br />" +
    "It is easy to map when a method call is done in machine code.</html>",
    //Resource.
    "<html>Files that can be read within the application, or DLL. Such as pictures, images, audio files.<br /><br />The first Icon that is read is the programs ICon image.<br /><br />" +
    "Each address location is added to the start of the resource section.</html>",
    noReader, noReader,
    //Relocations.
    "<html>Relocations are used if the program is not loaded at it's preferred base Address set in the op header.<br /><br />" +
    "The difference is added to locations defined in the address list in this relocation section.<br /><br />" +
    "Relocations are not needed, for this disassembler as the program is always mapped at it's preferred base address.<br /><br />" +
    "A reader can be designed for the relocation section, but is not really necessary.<br /><br /><br />" +
    "Relocations are common in 16Bit, or 32Bit x86. However, 64bit x86 machine code uses relative addresses.<br /><br />" +
    "Relative addresses are added to the current instruction position in the binary.<br /><br />" +
    "Allowing the binary to be placed anywhere in memory without having to change the address locations.<br /><br />" +
    "It is very rare for there to be relocations, if it is a 64bit x86 binary.</html>",
    noReader, noReader, noReader, noReader, noReader, noReader, noReader, noReader, noReader
  };

  public EXE()
  {
    tree.setEventListener( this );

    file.Events = false;

    //Root node is now the target file.
 
    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true);tree.setShowsRootHandles(true); root = new JDNode( fc.getFileName() );

    //header data folder to organize exe setup information.
  
    JDNode Headers = new JDNode("Header Data",1);

    //Decode the setup headers.

    try
    {
      des[0] = Header.readMZ(MSDos); Headers.add(MSDos);

      des[1] = new Descriptor[4];
      
      if(!error) { des[1][0] = Header.readPE(); }
      if( !DOS )
      {
        if(!error) { des[1][1] = Header.readOP(); }
        if(!error) { des[1][2] = Header.readDataDirectory(); }
        if(!error) { des[1][3] = Header.readSections(); }
      }
      else
      {
        if( core == null || core.type() != 0 ){ core = new X86( file ); } else { core.setTarget( file ); }
        core.setEvent( this::Dis ); coreLoaded = true;
      }
    }
    catch(java.io.IOException e) { error = true; }

    if( error || DOS ) { DataDirUsed = new boolean[15]; } //Error, Or if MS-DOS binary.
    else
    {
      //Load processor core type.

      if( coreType == 0x014C || coreType == (short)0x8664 )
      {
        if( core == null || core.type() != 0 ){ core = new X86( file ); } else { core.setTarget( file ); }
        
        core.setEvent( this::Dis ); coreLoaded = true;
      }
      else
      {
        if( core == null || core.type() != 0 ){ core = new X86( file ); } else { core.setTarget( file ); }
        core.setEvent( this::Dis ); coreLoaded = false;
      }
    }

    core.setAddressMode(false); //Uses the address pointer array system. Faster lookup.

    if( !DOS )
    {
      Headers.add(new JDNode("PE Header.h", new long[]{ 1, 0 } ));
      Headers.add(new JDNode("OP Header.h", new long[]{ 1, 1 } ));
      Headers.add(new JDNode("Data Directory Array.h", new long[]{ 1, 2 } ));
      Headers.add(new JDNode("Mapped SECTIONS TO RAM.h", new long[]{ 1, 3 } ));
    }
    else
    {
      try { PE = file.length() - MZSize; } catch(IOException e) { }
    }

    root.add( Headers ); file.addV( MZSize, PE, 0, PE - MZSize );

    //Start of code.

    if( !DOS && baseOfCode != 0 )  { root.add(new JDNode( "Program Start (Machine code).h", new long[]{ -1, imageBase + startOfCode } )); }

    //Setup the sections.

    for( int i = 0; i < 15; i++ ) { if( DataDirUsed[i] ) { root.add(sections[i]); } } ((DefaultTreeModel)tree.getModel()).setRoot(root);

    file.Events = true;

    //Set the default node.

    if( DOS )
    {
      tree.setSelectionPath( new TreePath( ((DefaultMutableTreeNode)MSDos.getFirstChild()).getPath() ) ); open( new JDEvent( this, "", 0 ) );
    }
    else
    {
      tree.setSelectionPath( new TreePath( Headers.getPath() ) ); open( new JDEvent( this, "", 1 ) );
    }
  }


  public void Uninitialize()
  {
    des = new Descriptor[17][];

    DataDir = null; DataDirUsed = null; DLL = null; FDLL = null; DLLName = null; DLLTable = null;

    core.mapped_loc.clear(); core.mapped_pos.clear();
  }

  //Change What To Display Based on what the user clicks on.

  public void open( JDEvent e )
  {
    if( e.getID().equals("UInit") ) { Uninitialize(); }

    //Load a new section.

    else if( e.getArgs().length == 1 )
    {
      int el = (int)e.getArg(0); info(SInfo[el]); el -= 2;
      
      if( el < 0 ) { Offset.setSelected( 0, ( DOS || el == -2 ) ? MZSize - 1 : ( des[1][3].pos + des[1][3].length ) - 1 ); }
      else
      {
        try
        {
          file.seekV( DataDir[ el << 1 ] );

          //Read Section.

          if( des[el+2] == null && Reader[el] != null )
          {
            file.Events = false;
        
            sections[el].setUserObject(sections[el].getUserObject().toString().replace(".h","")); des[el+2] = Reader[el].read( sections[el] );
        
            file.Events = true;

            //Update the tree.

            ((DefaultTreeModel)tree.getModel()).nodeChanged( sections[el] ); tree.expandPath( new TreePath( sections[el].getPath() ) );
          }

          el <<= 1;

          Virtual.setSelected( DataDir[el], DataDir[el] + DataDir[el+1] - 1 );
          Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[el+1] - 1 );
        }
        catch( IOException er ) { }
      }
    }
    else if( e.getArg(0) >= 0 ) { ds.setDescriptor(des[(int)e.getArg(0)][(int)e.getArg(1)]); return; }

    //Negative values are commands.

    else if( e.getArg(0) < 0 )
    {
      //Start Disassembling instructions.
    
      if( (DosMode = e.getArg(0) == -2) || e.getArg(0) == -1 )
      {
        //If import table is not loaded. It should be loaded to map method calls.

        if( !DosMode && des[3] == null ) { open( new JDEvent( this, "", 3 ) ); }

        //Begin disassembly.

        if( coreLoaded || DosMode )
        {
          if( DosMode ) { core.setBit(X86.x86_16); } else { core.setBit( is64bit ? X86.x86_64 : X86.x86_32 ); }

          core.locations.clear(); core.data_off.clear(); core.code.clear();

          core.locations.add( e.getArg(1) );

          core.disLoc(0); ds.setDescriptor( core ); return;
        }
        else { try{ file.seekV( e.getArg(1) ); Virtual.setSelected( e.getArg(1), e.getArg(1) ); } catch(Exception er) { } noCore(); }
      }

      //Offset.

      else if( e.getArg(0) == -3 ) { try { file.seekV( e.getArg(1) ); } catch( IOException er ) { } }

      //Select virtual offset.

      else if( e.getArg(0) == -4 ) { try { file.seekV( e.getArg(1) ); Virtual.setSelected( e.getArg(1), e.getArg(2) ); } catch( IOException er ) { } }
    }

    ds.clear();
  }

  //Disassembler.

  public void Dis( long loc )
  {
    //If we are taking apart a Dos application.

    if(DosMode)
    {
      try
      {
        //Disassemble till end, or return from application.
        //Note that more can be added here such as the jump operation.

        file.seekV( loc );

        String t1 = "", t2 = "", t = "";

        int Dos_exit = 0;

        while( true )
        {
          t1 = core.posV(); t2 = core.disASM();
          
          if( Dos_exit == 0 && ( t2.startsWith("MOV AX,4C") || t2.startsWith("MOV AH,4C") ) ) { Dos_exit = 1; }
          else if( Dos_exit == 1 && ( t2.indexOf("AX,") > 0 || t2.indexOf("AH,") > 0 ) ) { Dos_exit = 0; }
          if( Dos_exit == 1 && t2.equals("INT 21") ) { Dos_exit = 2; }
          
          t += t1 + " " + t2 + "<br />";

          if( Dos_exit == 2 || t2.startsWith("RET") || t2.startsWith("JMP") || t2.equals("INT 20") ) { break; }
        }

        info( "<html>" + t + "</html>" ); core.clean(loc, file.getVirtualPointer());

        long pos = file.getFilePointer() - 1, posV = file.getVirtualPointer() - 1;
        
        file.seekV( loc ); Virtual.setSelectedEnd( posV ); Offset.setSelectedEnd( pos );

        ds.setDescriptor( core );
      }
      catch( Exception e ) { e.printStackTrace(); }
    }
    else
    {
      try
      {
        file.seekV( loc );

        long floc = file.getFilePointer();

        String d = core.disASM_Code();

        info( "<html>" + d + "</html>" );

        Virtual.setSelected( loc, file.getVirtualPointer() - 1 ); Offset.setSelected( floc, file.getFilePointer() - 1 );

        ds.setDescriptor( core );
      }
      catch( IOException e ) { e.printStackTrace(); }
    }
  }

  //Error while reading file.

  public void errDecode()
  { 
    info("<html>Error occurred while reading this header.</html>");
  }

  //Processor core is not supported.

  public void noCore()
  { 
    info("<html>The processor core engine is not supported.</html>");
  }
}