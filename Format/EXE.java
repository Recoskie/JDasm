package Format;

import java.io.*;
import swingIO.*;
import javax.swing.tree.*;
import Format.EXEDecode.*;
import RandomAccessFileV.*;
import WindowComponents.*;

//Processor cores.

import core.x86.*; //X86.

public class EXE extends Data implements ExplorerEventListener
{
  //The new Descriptor table allows a description of clicked.

  public Descriptor[] Header_des = new Descriptor[6];

  //Exportable methods.

  public Descriptor[] Export_des;

  //Data descriptor for DLL import table.

  public Descriptor[] DLL_des;

  //Data descriptor for resource reader.

  public Descriptor[] RSRC_des;

  //A blank data Descriptor.

  public Descriptor blank;

  //Nodes that can be added to when Adding section format readers.

  DefaultMutableTreeNode root;
  DefaultMutableTreeNode Export = new DefaultMutableTreeNode("function Export Table.h");
  DefaultMutableTreeNode Import = new DefaultMutableTreeNode("DLL Import Table.h");
  DefaultMutableTreeNode RE = new DefaultMutableTreeNode("Resource Files.h");
  DefaultMutableTreeNode EX = new DefaultMutableTreeNode("Exception Table.h");
  DefaultMutableTreeNode Security = new DefaultMutableTreeNode("Security Level Settings.h");
  DefaultMutableTreeNode RELOC = new DefaultMutableTreeNode("Relocations.h");
  DefaultMutableTreeNode DEBUG = new DefaultMutableTreeNode("DEBUG TABLE.h");
  DefaultMutableTreeNode Description = new DefaultMutableTreeNode("Description/Architecture.h");
  DefaultMutableTreeNode MV = new DefaultMutableTreeNode("Machine Value.h");
  DefaultMutableTreeNode TS = new DefaultMutableTreeNode("Thread Storage Location.h");
  DefaultMutableTreeNode ConFIG = new DefaultMutableTreeNode("Load System Configuration.h");
  DefaultMutableTreeNode BoundImport = new DefaultMutableTreeNode("Import Table of Functions inside program.h");
  DefaultMutableTreeNode ImportAddress = new DefaultMutableTreeNode("Import Address Setup Table.h");
  DefaultMutableTreeNode DelayImport = new DefaultMutableTreeNode("Delayed Import Table.h");
  DefaultMutableTreeNode COM = new DefaultMutableTreeNode("COM Runtime Descriptor.h");

  //plug in the executable Readers

  public Headers Header = new Headers();
  public DLLExport DLL_ex = new DLLExport();
  public DLLImport DLL = new DLLImport();
  public Resource RSRC = new Resource();

  public EXE() { UsedDecoder = this; }

  //plug in the separate decoders of the exe format together

  public void read( String F, RandomAccessFileV notUsed )
  {
    blank = new Descriptor( file );

    file.Events = false;

    //Root node is now the target file.
 
    ((DefaultTreeModel)tree.getModel()).setRoot(null);
    tree.setRootVisible(true);tree.setShowsRootHandles(true);
    root = new DefaultMutableTreeNode( F );

    //header data folder to organize exe setup information.
  
    DefaultMutableTreeNode Headers = new DefaultMutableTreeNode("Header Data");

    //Decode the setup headers.

    try
    {
      Header_des[0] = Header.readMZ( file );
      if(!error) { Header_des[1] = Header.readPE( file ); }
      if(!error) { Header_des[2] = Header.readOP( file ); }
      if(!error) { Header_des[3] = Header.readDataDrectory( file ); }
      if(!error) { Header_des[4] = Header.readSections( file ); }
    }
    catch(java.io.IOException e) { error = true; }

    if( error ) { DataDirUsed = new boolean[15]; } //Error.
    else
    {
      //Load processor core type.

      if( coreType == 0x014C )
      {
        core = new X86( file ); core.setBit( X86.x86_32 );
        
        core.setEvent( this::Dis ); coreLoaded = true;
      }
      else if( coreType == (short)0x8664 )
      {
        core = new X86( file ); core.setBit( X86.x86_64 );
        
        core.setEvent( this::Dis ); coreLoaded = true;
      }
    }

    Headers.add(new DefaultMutableTreeNode("MZ Header.h#H,0"));
    Headers.add(new DefaultMutableTreeNode("PE Header.h#H,1"));
    Headers.add(new DefaultMutableTreeNode("OP Header.h#H,2"));
    Headers.add(new DefaultMutableTreeNode("Data Directory Array.h#H,3"));
    Headers.add(new DefaultMutableTreeNode("Mapped SECTIONS TO RAM.h#H,4"));

    root.add( Headers );

    //Start of code.

    if( baseOfCode != 0 )  { root.add(new DefaultMutableTreeNode("Program Start (Machine code).h#Dis," + ( imageBase + startOfCode ) + "" )); }

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

    tree.setSelectionPath( new TreePath( Headers.getPath() ) ); elementOpen("Header Data");

    f.setVisible(true);
  }

  //Change What To Display Based on what the user clicks on.

  public void elementOpen(String h)
  {
    //Data descriptors, for data structures.

    if( h.indexOf("#") > 0 )
    {
      String[] type = h.substring( h.lastIndexOf("#") + 1, h.length() ).split(",");

      //Start Disassembling instructions.
    
      if( type[0].equals("Dis") )
      {
        //If import table is not loaded. It should be loaded to map method calls.

        if( DLL_des == null ) { elementOpen("DLL Import Table"); }

        //Begin disassembly.

        if( coreLoaded )
        {
          core.locations.clear(); core.data_off.clear(); core.code.clear();

          core.locations.add( Long.parseLong( type[1] ) );

          Dis( core.locations.get(0) ); ds.setDescriptor( core );
        }
        else { noCore(); }
      }

      //Headers.

      else if( type[0].equals("H") )
      {
        ds.setDescriptor( Header_des[ Integer.parseInt( type[1] ) ] );
      }

      //Export data structures.

      else if( type[0].equals("E") )
      {
        ds.setDescriptor( Export_des[ Integer.parseInt( type[1] ) ] );
      }

      //DLL import data structures.

      else if( type[0].equals("D") )
      {
        ds.setDescriptor( DLL_des[ Integer.parseInt( type[1] ) ] );
      }

      //Resource reader data structures.

      else if( type[0].equals("R") )
      {
        ds.setDescriptor( RSRC_des[ Integer.parseInt( type[1] ) ] );
      }

      //Offset.

      else if( type[0].equals("O") )
      {
        try
        {
          file.seekV( Long.parseLong( type[1] ) );
        }
        catch( IOException e ) { }
      }

      //Select virtual offset.

      else if( type[0].equals("Sv") )
      {
        try
        {
          file.seekV( Long.parseLong( type[1] ) );

          Virtual.setSelected( Long.parseLong( type[1] ), Long.parseLong( type[2] ) );
        }
        catch( IOException e ) { }
      }

      return;
    }

    //Headers must be decoded before any other part of the program can be read.

    else if( h.equals("Header Data") )
    {
      Offset.setSelected( 0, ( Header_des[4].pos + Header_des[4].length ) - 1 );

      info("<html>The headers setup the Microsoft binary virtual space.<br /><br />Otherwise The import table can not be located.<br /><br />" +
      "Export Table can not be located.<br /><br />" +
      "Files that are included in the binary. Called Resource Files. Also can not be located.<br /><br />" +
      "Nether can the machine code Start position.</html>");
    }

    //Seek virtual address position. Thus begin reading section.

    else if( h.startsWith( "function Export Table", 0 ) )
    {
      try
      {
        file.seekV( DataDir[0] ); file.seekV( DataDir[0] );

        //Read Exportable methods.

        if( Export_des == null )
        {
          file.Events = false;
          
          Export.setUserObject("function Export Table"); Export_des = DLL_ex.LoadExport( Export, file );
          
          file.Events = true;

          //Update the tree.

          ((DefaultTreeModel)tree.getModel()).nodeChanged( Export ); tree.expandPath( new TreePath( Export.getPath() ) );
        }

        Virtual.setSelected( DataDir[0], DataDir[0] + DataDir[1] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[1] - 1 );
      }
      catch( IOException e ) { }

      info("<html>Once the headers are read, then the program is setup in virtual space.<br /><br />" +
      "The Export section is a list of names that locate to a machine code in RAM.<br /><br />" +
      "Methods can be imported by name, or by number they are in the export Address list.<br /><br />" +
      "A import table specifies which files to load to memory. If not already loaded.<br /><br />" +
      "The method list in the import table is replaced with the export locations in RAM from the other file.<br /><br />" +
      "This allows the other binary to directly run methods by using the import location as a relative address.</html>");
    }

    else if( h.startsWith( "DLL Import Table", 0 ) )
    {
      try
      {
        file.seekV( DataDir[2] ); file.seekV( DataDir[2] );

        //Decode import table.

        if( DLL_des == null )
        {
          file.Events = false;
          
          Import.setUserObject("DLL Import Table"); DLL_des = DLL.LoadDLLImport( Import, file );
          
          file.Events = true;

          //Update the tree.

          ((DefaultTreeModel)tree.getModel()).nodeChanged( Import ); tree.expandPath( new TreePath( Import.getPath() ) );
        }

        Virtual.setSelected( DataDir[2], DataDir[2] + DataDir[3] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[3] - 1 );
      }
      catch( IOException e ) { }

      info("<html>Methods that are imported from other files using the export table section.<br /><br />" +
      "Each import file is loaded to RAM memory. Each import has two method lists.<br /><br />" +
      "The first list is wrote over in RAM with the location to each export method location.<br /><br />" +
      "This allows the binary to directly run methods without rewriting, or changing machine code.<br /><br />" +
      "It is easy to map when a method call is done in machine code.</html>");
    }

    else if( h.startsWith( "Resource Files", 0 ) )
    {
      try
      {
        file.seekV( DataDir[4] );

        //Read Resource files.

        if( RSRC_des == null )
        {
          file.Events = false;
          
          RE.setUserObject("Resource Files"); RSRC_des = RSRC.readResource( RE, file );
          
          file.Events = true;

          //Update the tree.

          ((DefaultTreeModel)tree.getModel()).nodeChanged( RE ); tree.expandPath( new TreePath( RE.getPath() ) );
        }

        Virtual.setSelected( DataDir[4], DataDir[4] + DataDir[5] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[5] - 1 );
      }
      catch( IOException e ) { }

      info("<html>Files that can be read within the application, or DLL. Such as pictures, images, audio files.<br /><br />The first Icon that is read is the programs ICon image.<br /><br />" +
      "Each address location is added to the start of the resource section.</html>");
    }

    else if( h.equals("Exception Table.h") )
    {
      try
      {
        file.seekV(DataDir[6]);
        Virtual.setSelected( DataDir[6], DataDir[6] + DataDir[7] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[7] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Security Level Settings.h") )
    {
      try
      {
        file.seekV(DataDir[8]);
        Virtual.setSelected( DataDir[8], DataDir[8] + DataDir[9] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[9] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Relocations.h") )
    {
      try
      {
        file.seekV(DataDir[10]);
        Virtual.setSelected( DataDir[10], DataDir[10] + DataDir[11] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[11] - 1 );
      }
      catch( IOException e ) { }

      info("<html>Relocations are used if the program is not loaded at it's preferred base Address set in the op header.<br /><br />" +
      "The difference is added to locations defined in the address list in this relocation section.<br /><br />" +
      "Relocations are not needed, for this disassembler as the program is always mapped at it's preferred base address.<br /><br />" +
      "A reader can be designed for the relocation section, but is not really necessary.</html>");
    }
    else if( h.equals("DEBUG TABLE.h") )
    {
      try
      {
        file.seekV(DataDir[12]);
        Virtual.setSelected( DataDir[12], DataDir[12] + DataDir[13] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[13] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Description/Architecture.h") )
    {
      try
      {
        file.seekV(DataDir[14]);
        Virtual.setSelected( DataDir[14], DataDir[14] + DataDir[15] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[15] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Machine Value.h") )
    {
      try
      {
        file.seekV(DataDir[16]);
        Virtual.setSelected( DataDir[16], DataDir[16] + DataDir[17] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[17] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Thread Storage Location.h") )
    {
      try
      {
        file.seekV(DataDir[18]);
        Virtual.setSelected( DataDir[18], DataDir[18] + DataDir[19] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[19] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Load System Configuration.h") )
    {
      try
      {
        file.seekV(DataDir[20]);
        Virtual.setSelected( DataDir[20], DataDir[20] + DataDir[21] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[21] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Import Table of Functions inside program.h") )
    {
      try
      {
        file.seekV(DataDir[22]);
        Virtual.setSelected( DataDir[22], DataDir[22] + DataDir[23] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[23] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Import Address Setup Table.h") )
    {
      try
      {
        file.seekV(DataDir[24]);
        Virtual.setSelected( DataDir[24], DataDir[24] + DataDir[25] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[25] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Delayed Import Table.h") )
    {
      try
      {
        file.seekV(DataDir[26]);
        Virtual.setSelected( DataDir[26], DataDir[26] + DataDir[27] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[27] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("COM Runtime Descriptor.h") )
    {
      try
      {
        file.seekV(DataDir[28]);
        Virtual.setSelected( DataDir[28], DataDir[28] + DataDir[29] - 1 );
        Offset.setSelected( file.getFilePointer(), file.getFilePointer() + DataDir[29] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else { info(""); }

    ds.clear( blank );
  }

  //Disassembler.

  public void Dis( long loc )
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
    catch( IOException e ) { }
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