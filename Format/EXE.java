package Format;

import javax.swing.*;
import java.io.*;
import javax.swing.tree.*;
import java.awt.*;
import Format.EXEDecode.*;
import RandomAccessFileV.*;
import WindowComponents.*;

import dataTools.*;

//Processor cores.

import core.x86.*; //X86.

public class EXE extends WindowComponents implements ExploerEventListener
{
  //file system.

  public RandomAccessFileV b;

  //The new Descriptor table allows a description of clicked data.

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

  public static Data data = new Data();
  public Headers Header = new Headers();
  public DLLExport DLL_ex = new DLLExport();
  public DLLImport DLL = new DLLImport();
  public Resource RSRC = new Resource();

  public EXE() { UsedDecoder = this; }

  //plug in the separate decoders of the exe format together

  public void read( String F, RandomAccessFileV file )
  {
    b = file; data.stream = file; blank = new Descriptor( file );

    data.stream.Events = false;

    //Root node is now the target file.
 
    ((DefaultTreeModel)tree.getModel()).setRoot(null);
    tree.setRootVisible(true);tree.setShowsRootHandles(true);
    root = new DefaultMutableTreeNode( F );

    //header data folder to organize exe setup information.
  
    DefaultMutableTreeNode Headers = new DefaultMutableTreeNode("Header Data");

    //Decode the setup headers.

    try
    {
      Header_des[0] = Header.readMZ( b );
      if(!Data.error) { Header_des[1] = Header.readPE( b ); }
      if(!Data.error) { Header_des[2] = Header.readOP( b ); }
      if(!Data.error) { Header_des[3] = Header.readDataDrectory( b ); }
      if(!Data.error) { Header_des[4] = Header.readSections( b ); }
    }
    catch(java.io.IOException e) { Data.error = true; }

    if( Data.error ) { data.DataDirUsed = new boolean[15]; } //Error.
    else
    {
      //Load processor core type.

      if( Data.coreType == 0x014C )
      {
        data.core = new X86( b ); data.core.setBit( X86.x86_32 );
        
        data.core.setEvent( this::Dis ); data.coreLoaded = true;
      }
      else if( Data.coreType == (short)0x8664 )
      {
        data.core = new X86( b ); data.core.setBit( X86.x86_64 );
        
        data.core.setEvent( this::Dis ); data.coreLoaded = true;
      }
    }

    Headers.add(new DefaultMutableTreeNode("MZ Header.h#H,0"));
    Headers.add(new DefaultMutableTreeNode("PE Header.h#H,1"));
    Headers.add(new DefaultMutableTreeNode("OP Header.h#H,2"));
    Headers.add(new DefaultMutableTreeNode("Data Directory Array.h#H,3"));
    Headers.add(new DefaultMutableTreeNode("Mapped SECTIONS TO RAM.h#H,4"));

    root.add( Headers );

    //Start of code.

    if( data.baseOfCode != 0 )  { root.add(new DefaultMutableTreeNode("Program Start (Machine code).h#Dis," + ( data.imageBase + data.startOfCode ) + "" )); }

    //Location of the export directory

    if( data.DataDirUsed[0] ) { root.add(Export); }

    //Location of the import directory

    if( data.DataDirUsed[1] ) { root.add(Import); }

    //Location of the resource directory

    if( data.DataDirUsed[2] ) { root.add(RE); }

    //Exception

    if( data.DataDirUsed[3] ) { root.add(EX); }

    //Security

    if( data.DataDirUsed[4] ) { root.add(Security); }

    //Relocation/Patching

    if( data.DataDirUsed[5] ) { root.add(RELOC); }

    //Debug

    if( data.DataDirUsed[6] ) { root.add(DEBUG); }

    //Description/Architecture

    if( data.DataDirUsed[7] ) { root.add(Description); }

    //Machine Value

    if( data.DataDirUsed[8] ) { root.add(MV); }

    //Thread Storage

    if( data.DataDirUsed[9] ) { root.add(TS); }

    //Load System Configuration

    if( data.DataDirUsed[10] ) { root.add(ConFIG); }

    //Location of alternate import-binding director

    if( data.DataDirUsed[11] ) { root.add(BoundImport); }

    //Import Address Table

    if( data.DataDirUsed[12] ) { root.add(ImportAddress); }

    //Delayed Imports

    if( data.DataDirUsed[13] ) { root.add(DelayImport); }

    //COM Runtime Descriptor

    if( data.DataDirUsed[14] ) { root.add(COM); }

    ((DefaultTreeModel)tree.getModel()).setRoot(root);

    data.stream.Events = true;

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

        if( Data.coreLoaded )
        {
          data.core.locations.clear(); data.core.data_off.clear(); data.core.code.clear();

          data.core.locations.add( Long.parseLong( type[1] ) );

          Dis( data.core.locations.get(0) ); ds.setDescriptor( data.core );
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
          b.seekV( Long.parseLong( type[1] ) );
        }
        catch( IOException e ) { }
      }

      //Select virtual offset.

      else if( type[0].equals("Sv") )
      {
        try
        {
          b.seekV( Long.parseLong( type[1] ) );

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
        b.seekV( data.DataDir[0] ); b.seekV( data.DataDir[0] );

        //Read Exportable methods.

        if( Export_des == null )
        {
          b.Events = false;
          
          Export.setUserObject("function Export Table"); Export_des = DLL_ex.LoadExport( Export, b );
          
          b.Events = true;

          //Update the tree.

          ((DefaultTreeModel)tree.getModel()).nodeChanged( Export ); tree.expandPath( new TreePath( Export.getPath() ) );
        }

        Virtual.setSelected( data.DataDir[0], data.DataDir[0] + data.DataDir[1] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[1] - 1 );
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
        b.seekV( data.DataDir[2] ); b.seekV( data.DataDir[2] );

        //Decode import table.

        if( DLL_des == null )
        {
          b.Events = false;
          
          Import.setUserObject("DLL Import Table"); DLL_des = DLL.LoadDLLImport( Import, b );
          
          b.Events = true;

          //Update the tree.

          ((DefaultTreeModel)tree.getModel()).nodeChanged( Import ); tree.expandPath( new TreePath( Import.getPath() ) );
        }

        Virtual.setSelected( data.DataDir[2], data.DataDir[2] + data.DataDir[3] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[3] - 1 );
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
        b.seekV( data.DataDir[4] );

        //Read Resource files.

        if( RSRC_des == null )
        {
          b.Events = false;
          
          RE.setUserObject("Resource Files"); RSRC_des = RSRC.readResource( RE, b );
          
          b.Events = true;

          //Update the tree.

          ((DefaultTreeModel)tree.getModel()).nodeChanged( RE ); tree.expandPath( new TreePath( RE.getPath() ) );
        }

        Virtual.setSelected( data.DataDir[4], data.DataDir[4] + data.DataDir[5] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[5] - 1 );
      }
      catch( IOException e ) { }

      info("<html>Files that can be read within the application, or DLL. Such as pictures, images, audio files.<br /><br />The first Icon that is read is the programs ICon image.<br /><br />" +
      "Each address location is added to the start of the resource section.</html>");
    }

    else if( h.equals("Exception Table.h") )
    {
      try
      {
        b.seekV(data.DataDir[6]);
        Virtual.setSelected( data.DataDir[6], data.DataDir[6] + data.DataDir[7] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[7] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Security Level Settings.h") )
    {
      try
      {
        b.seekV(data.DataDir[8]);
        Virtual.setSelected( data.DataDir[8], data.DataDir[8] + data.DataDir[9] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[9] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Relocations.h") )
    {
      try
      {
        b.seekV(data.DataDir[10]);
        Virtual.setSelected( data.DataDir[10], data.DataDir[10] + data.DataDir[11] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[11] - 1 );
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
        b.seekV(data.DataDir[12]);
        Virtual.setSelected( data.DataDir[12], data.DataDir[12] + data.DataDir[13] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[13] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Description/Architecture.h") )
    {
      try
      {
        b.seekV(data.DataDir[14]);
        Virtual.setSelected( data.DataDir[14], data.DataDir[14] + data.DataDir[15] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[15] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Machine Value.h") )
    {
      try
      {
        b.seekV(data.DataDir[16]);
        Virtual.setSelected( data.DataDir[16], data.DataDir[16] + data.DataDir[17] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[17] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Thread Storage Location.h") )
    {
      try
      {
        b.seekV(data.DataDir[18]);
        Virtual.setSelected( data.DataDir[18], data.DataDir[18] + data.DataDir[19] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[19] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Load System Configuration.h") )
    {
      try
      {
        b.seekV(data.DataDir[20]);
        Virtual.setSelected( data.DataDir[20], data.DataDir[20] + data.DataDir[21] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[21] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Import Table of Functions inside program.h") )
    {
      try
      {
        b.seekV(data.DataDir[22]);
        Virtual.setSelected( data.DataDir[22], data.DataDir[22] + data.DataDir[23] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[23] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Import Address Setup Table.h") )
    {
      try
      {
        b.seekV(data.DataDir[24]);
        Virtual.setSelected( data.DataDir[24], data.DataDir[24] + data.DataDir[25] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[25] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("Delayed Import Table.h") )
    {
      try
      {
        b.seekV(data.DataDir[26]);
        Virtual.setSelected( data.DataDir[26], data.DataDir[26] + data.DataDir[27] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[27] - 1 );
      }
      catch( IOException e ) { }

      noDecode();
    }
    else if( h.equals("COM Runtime Descriptor.h") )
    {
      try
      {
        b.seekV(data.DataDir[28]);
        Virtual.setSelected( data.DataDir[28], data.DataDir[28] + data.DataDir[29] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[29] - 1 );
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
      b.seekV( loc );

      long floc = b.getFilePointer();

      String d = data.core.disASM_Code();

      info( "<html>" + d + "</html>" );

      Virtual.setSelected( loc, b.getVirtualPointer() - 1 ); Offset.setSelected( floc, b.getFilePointer() - 1 );

      ds.setDescriptor( data.core );
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