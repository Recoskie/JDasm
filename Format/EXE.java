package Format;

import java.io.*;

import swingIO.*;
import swingIO.tree.*;
import javax.swing.tree.*;
import Format.EXEDecode.*;

//Processor cores.

import core.x86.*; //X86.

public class EXE extends Data implements JDEventListener
{
  //Dos header.

  public Descriptor[] DOS_des = new Descriptor[2];

  //The new Descriptor table allows a description of clicked.

  public Descriptor[] Header_des = new Descriptor[5];

  //Exportable methods.

  public Descriptor[] Export_des;

  //Data descriptor for DLL import table.

  public Descriptor[] DLL_des;

  //Data descriptor for resource reader.

  public Descriptor[] RSRC_des;

  //Nodes that can be added to when Adding section format readers.

  JDNode root;
  JDNode MSDos = new JDNode("MZ Header", -1);
  JDNode Export = new JDNode("function Export Table.h", -1);
  JDNode Import = new JDNode("DLL Import Table.h", -1);
  JDNode RE = new JDNode("Resource Files.h", -1);
  JDNode EX = new JDNode("Exception Table.h", -1);
  JDNode Security = new JDNode("Security Level Settings.h", -1);
  JDNode RELOC = new JDNode("Relocations.h", -1);
  JDNode DEBUG = new JDNode("DEBUG TABLE.h", -1);
  JDNode Description = new JDNode("Description/Architecture.h", -1);
  JDNode MV = new JDNode("Machine Value.h", -1);
  JDNode TS = new JDNode("Thread Storage Location.h", -1);
  JDNode ConFIG = new JDNode("Load System Configuration.h", -1);
  JDNode BoundImport = new JDNode("Import Table of Functions inside program.h", -1);
  JDNode ImportAddress = new JDNode("Import Address Setup Table.h", -1);
  JDNode DelayImport = new JDNode("Delayed Import Table.h", -1);
  JDNode COM = new JDNode("COM Runtime Descriptor.h", -1);

  //plug in the executable Readers

  public Headers Header = new Headers();
  public DLLExport DLL_ex = new DLLExport();
  public DLLImport DLL = new DLLImport();
  public Resource RSRC = new Resource();

  public EXE()
  {
    tree.setEventListener( this );

    file.Events = false;

    //Root node is now the target file.
 
    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true);tree.setShowsRootHandles(true); root = new JDNode( fc.getFileName() );

    //header data folder to organize exe setup information.
  
    JDNode Headers = new JDNode("Header Data", -1);

    //Decode the setup headers.

    try
    {
      DOS_des = Header.readMZ(MSDos); Headers.add(MSDos);
      
      if(!error) { Header_des[0] = Header.readPE(); }
      if( !DOS )
      {
        if(!error) { Header_des[1] = Header.readOP(); }
        if(!error) { Header_des[2] = Header.readDataDirectory(); }
        if(!error) { Header_des[3] = Header.readSections(); }
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
    }

    if( !DOS )
    {
      Headers.add(new JDNode("PE Header.h", "H", 0));
      Headers.add(new JDNode("OP Header.h", "H", 1));
      Headers.add(new JDNode("Data Directory Array.h", "H", 2));
      Headers.add(new JDNode("Mapped SECTIONS TO RAM.h", "H", 3));
    }
    else
    {
      try { PE = file.length() - MZSize; } catch(IOException e) { }
    }

    root.add( Headers ); file.addV( MZSize, PE, 0, PE - MZSize );

    //Start of code.

    if( !DOS && baseOfCode != 0 )  { root.add(new JDNode( "Program Start (Machine code).h", "Dis", imageBase + startOfCode ) ); }

    //Location of the export directory

    if( DataDirUsed[0] ) { root.add(Export); }

    //Location of the import directory

    if( DataDirUsed[1] ) { root.add(Import); }

    //Location of the resource directory

    if( DataDirUsed[2] ) { root.add(RE); }

    //Exception

    if( DataDirUsed[3] ) { root.add(EX); }

    //Security

    if( DataDirUsed[4] ) { root.add(Security); }

    //Relocation/Patching

    if( DataDirUsed[5] ) { root.add(RELOC); }

    //Debug

    if( DataDirUsed[6] ) { root.add(DEBUG); }

    //Description/Architecture

    if( DataDirUsed[7] ) { root.add(Description); }

    //Machine Value

    if( DataDirUsed[8] ) { root.add(MV); }

    //Thread Storage

    if( DataDirUsed[9] ) { root.add(TS); }

    //Load System Configuration

    if( DataDirUsed[10] ) { root.add(ConFIG); }

    //Location of alternate import-binding director

    if( DataDirUsed[11] ) { root.add(BoundImport); }

    //Import Address Table

    if( DataDirUsed[12] ) { root.add(ImportAddress); }

    //Delayed Imports

    if( DataDirUsed[13] ) { root.add(DelayImport); }

    //COM Runtime Descriptor

    if( DataDirUsed[14] ) { root.add(COM); }

    ((DefaultTreeModel)tree.getModel()).setRoot(root);

    file.Events = true;

    if( DOS )
    {
      tree.setSelectionPath( new TreePath( ((DefaultMutableTreeNode)MSDos.getFirstChild()).getPath() ) ); open( new JDEvent( this, "", "", "M", 0 ) );
    }
    else
    {
      tree.setSelectionPath( new TreePath( Headers.getPath() ) ); open( new JDEvent( this, "Header Data", -1 ) );
    }
  }

  //Change What To Display Based on what the user clicks on.

  public void open( JDEvent e )
  {
    //Data descriptors, for data structures.

    if( e.getArg(0) >= 0 )
    {
      //Start Disassembling instructions.
    
      if( e.getID().startsWith("Dis") )
      {
        //If import table is not loaded. It should be loaded to map method calls.

        if( !(DosMode = e.getID().length() > 3) && DLL_des == null ) { open( new JDEvent( this, "DLL Import Table", -1 ) ); }

        //Begin disassembly.

        if( coreLoaded )
        {
          if( DosMode ) { core.setBit(X86.x86_16); } else { core.setBit( is64bit ? X86.x86_64 : X86.x86_32 ); }

          core.locations.clear(); core.data_off.clear(); core.code.clear();

          core.locations.add( e.getArg(0) );

          Dis( core.locations.get(0) ); ds.setDescriptor( core );
        }
        else { noCore(); }
      }

      //Dos Header

      else if( e.getID().equals("M") )
      {
        ds.setDescriptor( DOS_des[ (int)e.getArg(0) ] );
      }

      //Headers.

      else if( e.getID().equals("H") )
      {
        ds.setDescriptor( Header_des[ (int)e.getArg(0) ] );
      }

      //Export data structures.

      else if( e.getID().equals("E") )
      {
        ds.setDescriptor( Export_des[ (int)e.getArg(0) ] );
      }

      //DLL import data structures.

      else if( e.getID().equals("D") )
      {
        ds.setDescriptor( DLL_des[ (int)e.getArg(0) ] );
      }

      //Resource reader data structures.

      else if( e.getID().equals("R") )
      {
        ds.setDescriptor( RSRC_des[ (int)e.getArg(0) ] );
      }

      //Offset.

      else if( e.getID().equals("O") )
      {
        try
        {
          file.seekV( e.getArg(0) );
        }
        catch( IOException er ) { }
      }

      //Select virtual offset.

      else if( e.getID().equals("Sv") )
      {
        try
        {
          file.seekV( e.getArg(0) );

          Virtual.setSelected( e.getArg(0), e.getArg(1) );
        }
        catch( IOException er ) { }
      }

      return;
    }

    //Headers must be decoded before any other part of the program can be read.

    else if( e.getPath().equals("MZ Header") )
    {
      Offset.setSelected( 0, MZSize - 1 );
    
      info("<html>This is the original DOS header. Which must be at the start of all windows binary files.<br /><br />Today the reserved bytes are used to locate to the new Portable executable header format.<br /><br />" +
      "However, on DOS this header still loads as the reserved bytes that locate to the PE header do nothing in DOS.<br /><br />Thus the small 16 bit binary at the end will run. " +
      "Which normally contains a small 16 bit code that prints the message that this program can not be run in DOS mode.</html>");
    }

    //Headers must be decoded before any other part of the program can be read.

    else if( e.getPath().equals("Header Data") )
    {
      Offset.setSelected( 0, DOS ? MZSize - 1 : ( Header_des[3].pos + Header_des[3].length ) - 1 );

      info("<html>The headers setup the Microsoft binary virtual space.<br /><br />Otherwise The import table can not be located.<br /><br />" +
      "Export Table can not be located.<br /><br />" +
      "Files that are included in the binary. Called Resource Files. Also can not be located.<br /><br />" +
      "Nether can the machine code Start position be located.</html>");
    }

    //Seek virtual address position. Thus begin reading section.

    else if( e.getPath().startsWith( "function Export Table", 0 ) )
    {
      try
      {
        file.seekV( DataDir[0] ); file.seekV( DataDir[0] );

        //Read Exportable methods.

        if( Export_des == null )
        {
          file.Events = false;
          
          Export.setUserObject("function Export Table"); Export_des = DLL_ex.LoadExport( Export );
          
          file.Events = true;

          //Update the tree.

          ((DefaultTreeModel)tree.getModel()).nodeChanged( Export ); tree.expandPath( new TreePath( Export.getPath() ) );
        }

        Virtual.setSelected( DataDir[0], DataDir[0] + DataDir[1] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[1] - 1 );
      }
      catch( IOException er ) { }

      info("<html>Once the headers are read, then the program is setup in virtual space.<br /><br />" +
      "The Export section is a list of names that locate to a machine code in RAM.<br /><br />" +
      "Methods can be imported by name, or by number they are in the export Address list.<br /><br />" +
      "A import table specifies which files to load to memory. If not already loaded.<br /><br />" +
      "The method list in the import table is replaced with the export locations in RAM from the other file.<br /><br />" +
      "This allows the other binary to directly run methods by using the import location as a relative address.</html>");
    }

    else if( e.getPath().startsWith( "DLL Import Table", 0 ) )
    {
      try
      {
        file.seekV( DataDir[2] ); file.seekV( DataDir[2] );

        //Decode import table.

        if( DLL_des == null )
        {
          file.Events = false;
          
          Import.setUserObject("DLL Import Table"); DLL_des = DLL.LoadDLLImport( Import );
          
          file.Events = true;

          //Update the tree.

          ((DefaultTreeModel)tree.getModel()).nodeChanged( Import ); tree.expandPath( new TreePath( Import.getPath() ) );
        }

        Virtual.setSelected( DataDir[2], DataDir[2] + DataDir[3] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[3] - 1 );
      }
      catch( IOException er ) { }

      info("<html>Methods that are imported from other files using the export table section.<br /><br />" +
      "Each import file is loaded to RAM memory. Each import has two method lists.<br /><br />" +
      "The first list is wrote over in RAM with the location to each export method location.<br /><br />" +
      "This allows the binary to directly run methods without rewriting, or changing machine code.<br /><br />" +
      "It is easy to map when a method call is done in machine code.</html>");
    }

    else if( e.getPath().startsWith( "Resource Files", 0 ) )
    {
      try
      {
        file.seekV( DataDir[4] );

        //Read Resource files.

        if( RSRC_des == null )
        {
          file.Events = false;
          
          RE.setUserObject("Resource Files"); RSRC_des = RSRC.readResource( RE );
          
          file.Events = true;

          //Update the tree.

          ((DefaultTreeModel)tree.getModel()).nodeChanged( RE ); tree.expandPath( new TreePath( RE.getPath() ) );
        }

        Virtual.setSelected( DataDir[4], DataDir[4] + DataDir[5] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[5] - 1 );
      }
      catch( IOException er ) { }

      info("<html>Files that can be read within the application, or DLL. Such as pictures, images, audio files.<br /><br />The first Icon that is read is the programs ICon image.<br /><br />" +
      "Each address location is added to the start of the resource section.</html>");
    }

    else if( e.getPath().equals("Exception Table.h") )
    {
      try
      {
        file.seekV(DataDir[6]);
        Virtual.setSelected( DataDir[6], DataDir[6] + DataDir[7] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[7] - 1 );
      }
      catch( IOException er ) { }

      noDecode();
    }
    else if( e.getPath().equals("Security Level Settings.h") )
    {
      try
      {
        file.seekV(DataDir[8]);
        Virtual.setSelected( DataDir[8], DataDir[8] + DataDir[9] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[9] - 1 );
      }
      catch( IOException er ) { }

      noDecode();
    }
    else if( e.getPath().equals("Relocations.h") )
    {
      try
      {
        file.seekV(DataDir[10]);
        Virtual.setSelected( DataDir[10], DataDir[10] + DataDir[11] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[11] - 1 );
      }
      catch( IOException er ) { }

      info("<html>Relocations are used if the program is not loaded at it's preferred base Address set in the op header.<br /><br />" +
      "The difference is added to locations defined in the address list in this relocation section.<br /><br />" +
      "Relocations are not needed, for this disassembler as the program is always mapped at it's preferred base address.<br /><br />" +
      "A reader can be designed for the relocation section, but is not really necessary.<br /><br /><br /><br />" +
      "Relocations are common in 16Bit, or 32Bit x86. However, 64bit x86 machine code uses relative addresses.<br /><br />" +
      "Relative addresses are added to the current instruction position in the binary.<br /><br />" +
      "Allowing the binary to be placed anywhere in memory without having to change the address locations.<br /><br />" +
      "It is very rare for there to be relocations, if it is a 64bit x86 binary.</html>");
    }
    else if( e.getPath().equals("DEBUG TABLE.h") )
    {
      try
      {
        file.seekV(DataDir[12]);
        Virtual.setSelected( DataDir[12], DataDir[12] + DataDir[13] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[13] - 1 );
      }
      catch( IOException er ) { }

      noDecode();
    }
    else if( e.getPath().equals("Description/Architecture.h") )
    {
      try
      {
        file.seekV(DataDir[14]);
        Virtual.setSelected( DataDir[14], DataDir[14] + DataDir[15] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[15] - 1 );
      }
      catch( IOException er ) { }

      noDecode();
    }
    else if( e.getPath().equals("Machine Value.h") )
    {
      try
      {
        file.seekV(DataDir[16]);
        Virtual.setSelected( DataDir[16], DataDir[16] + DataDir[17] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[17] - 1 );
      }
      catch( IOException er ) { }

      noDecode();
    }
    else if( e.getPath().equals("Thread Storage Location.h") )
    {
      try
      {
        file.seekV(DataDir[18]);
        Virtual.setSelected( DataDir[18], DataDir[18] + DataDir[19] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[19] - 1 );
      }
      catch( IOException er ) { }

      noDecode();
    }
    else if( e.getPath().equals("Load System Configuration.h") )
    {
      try
      {
        file.seekV(DataDir[20]);
        Virtual.setSelected( DataDir[20], DataDir[20] + DataDir[21] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[21] - 1 );
      }
      catch( IOException er ) { }

      noDecode();
    }
    else if( e.getPath().equals("Import Table of Functions inside program.h") )
    {
      try
      {
        file.seekV(DataDir[22]);
        Virtual.setSelected( DataDir[22], DataDir[22] + DataDir[23] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[23] - 1 );
      }
      catch( IOException er ) { }

      noDecode();
    }
    else if( e.getPath().equals("Import Address Setup Table.h") )
    {
      try
      {
        file.seekV(DataDir[24]);
        Virtual.setSelected( DataDir[24], DataDir[24] + DataDir[25] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[25] - 1 );
      }
      catch( IOException er ) { }

      noDecode();
    }
    else if( e.getPath().equals("Delayed Import Table.h") )
    {
      try
      {
        file.seekV(DataDir[26]);
        Virtual.setSelected( DataDir[26], DataDir[26] + DataDir[27] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[27] - 1 );
      }
      catch( IOException er ) { }

      noDecode();
    }
    else if( e.getPath().equals("COM Runtime Descriptor.h") )
    {
      try
      {
        file.seekV(DataDir[28]);
        Virtual.setSelected( DataDir[28], DataDir[28] + DataDir[29] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[29] - 1 );
      }
      catch( IOException er ) { }

      noDecode();
    }
    else { info(""); }

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

  //No Decoder.

  public void noDecode()
  { 
    info("<html>No reader for this section yet.</html>");
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