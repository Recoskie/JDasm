package Format;

import javax.swing.*;
import java.io.*;
import javax.swing.tree.*;
import java.awt.*;
import Format.EXEDecode.*;
import RandomAccessFileV.*;
import WindowCompoents.*;

import dataTools.*;

//Processor cores.

import core.x86.*; //X86.

public class EXE extends WindowCompoents implements ExploerEventListener
{
  //file system.

  public RandomAccessFileV b;

  //The new Descriptor table allows a description of clicked data.

  public Descriptor[] des = new Descriptor[6];

  //Data descriptor for DLL import table.

  public Descriptor[] DLL_des;

  //Nodes that can be added to when Adding section format readers.

  DefaultMutableTreeNode root;
  DefaultMutableTreeNode Export = new DefaultMutableTreeNode("function Export Table.h");
  DefaultMutableTreeNode Import = new DefaultMutableTreeNode("DLL Import Table.h");
  DefaultMutableTreeNode RE = new DefaultMutableTreeNode("Resource Files.h");
  DefaultMutableTreeNode EX = new DefaultMutableTreeNode("Exception Table.h");
  DefaultMutableTreeNode Security = new DefaultMutableTreeNode("Security Level Settings.h");
  DefaultMutableTreeNode RELOC = new DefaultMutableTreeNode("Relocation/Patching.h");
  DefaultMutableTreeNode DEBUG = new DefaultMutableTreeNode("DEBUG TABLE.h");
  DefaultMutableTreeNode Decription = new DefaultMutableTreeNode("Description/Architecture.h");
  DefaultMutableTreeNode MV = new DefaultMutableTreeNode("Machine Value.h");
  DefaultMutableTreeNode TS = new DefaultMutableTreeNode("Thread Storage Lowcation.h");
  DefaultMutableTreeNode ConFIG = new DefaultMutableTreeNode("Load System Configuration.h");
  DefaultMutableTreeNode BoundImport = new DefaultMutableTreeNode("Import Table of Functions inside program.h");
  DefaultMutableTreeNode ImportAddress = new DefaultMutableTreeNode("Import Address Setup Table.h");
  DefaultMutableTreeNode DelayImport = new DefaultMutableTreeNode("Delayed Import Table.h");
  DefaultMutableTreeNode COM = new DefaultMutableTreeNode("COM Runtime Descriptor.h");

  //plug in the executable Readers

  public static Data data = new Data();
  public Headers Header = new Headers();
  public DLLImport DLL = new DLLImport();
  
  //public Resource RSRC = new Resource();

  public EXE() { UsedDecoder = this; }

  //plug in the separate decoders of the exe format together

  public void read( String F, RandomAccessFileV file )
  {
    b = file; data.stream = file;

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
      des[0] = Header.readMZ( b );
      if(!Data.error) { des[1] = Header.readPE( b ); }
      if(!Data.error) { des[2] = Header.readOP( b ); }
      if(!Data.error) { des[3] = Header.readDataDrectory( b ); }
      if(!Data.error) { des[4] = Header.readSections( b ); }
    }
    catch(java.io.IOException e) { Data.error = true; }

    if( Data.error ) { data.DataDirUsed = new boolean[15]; } //Error.
    else
    {
      //Load processor core type.

      if( Data.coreType == 0x014C )
      {
        data.core = new X86( b ); data.core.setBit( X86.x86_32 ); data.coreLoaded = true;
      }
      else if( Data.coreType == (short)0x8664 )
      {
        data.core = new X86( b ); data.core.setBit( X86.x86_64 ); data.coreLoaded = true;
      }
    }

    Headers.add(new DefaultMutableTreeNode("MZ Header.h"));
    Headers.add(new DefaultMutableTreeNode("PE Header.h"));
    Headers.add(new DefaultMutableTreeNode("OP Header.h"));
    Headers.add(new DefaultMutableTreeNode("Data Directory Array.h"));
    Headers.add(new DefaultMutableTreeNode("Mapped SECTOINS TO RAM.h"));

    root.add( Headers );

    //Start of code.

    if( data.baseOfCode != 0 )  { root.add(new DefaultMutableTreeNode("Program Start (Machine code).h")); }

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

    if( data.DataDirUsed[7] ) { root.add(Decription); }

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

  //Change What To Display Based on what the user clicks on

  public void elementOpen(String h)
  {
    //Start of application.
    
    if( h.equals("Program Start (Machine code).h") )
    {
      if(Data.coreLoaded)
      {
        String t = "", t1 = "", t2 ="";

        try
        {
          b.seekV( data.imageBase + data.startOfCode );

          //Disassembler.

          long end = data.imageBase + data.baseOfCode + data.sizeOfCode;

          //Disassemble till end, or return from application.
          //Note that more can be added here such as the jump operation.

          while( t2.indexOf("RET") != 0 && b.getVirtualPointer() < end )
          {
            t1 = data.core.posV(); t2 = data.core.disASM(); t += t1 + " " + t2 + "<br />";
          }

          long f = b.getFilePointer() - 1, v = b.getVirtualPointer() - 1;

          b.seekV( data.imageBase + data.startOfCode ); Virtual.setSelected( data.imageBase + data.startOfCode, v ); Offset.setSelected( b.getFilePointer(), f );

          info( "<html>" + t + "</html>" );
        }
        catch( Exception e ) { }
      }
      else { noCore(); }
    }

    //Headers must be decoded before any other part of the program can be read.

    else if( h.equals("MZ Header.h") )
    {
      ds.setDescriptor( des[0] );

      info("<html><p>This is the original DOS header. Which must be at the start of all windows binary files.<br /><br />Today the reserved bytes are used to locate to the new Portable executable header format.<br /><br />" +
      "However, on DOS this header still loads as the reserved bytes that locate to the PE header do nothing in DOS.<br /><br />Thus the small 16 bit binary at the end will run. " +
      "Which normally contains a small 16 bit code that prints the message that this program can not be run in DOS mode.</p></html>");
    }
    else if( h.equals("PE Header.h") )
    {
      ds.setDescriptor( des[1] );

      info("<html><p>The PE header marks the start of the new Executable format. If the file is not loaded in DOS.<br /><br />" +
      "This header specifies the number of sections to map in virtual space. The processor type, and date of compilation.</p></html>");
    }
    else if( h.equals("OP Header.h") )
    {
      ds.setDescriptor( des[2] );

      info("<html><p>At the end of the PE header is the start of the Optional header. However, this header is not optional.</p></html>");
    }
    else if( h.equals("Data Directory Array.h") )
    {
      ds.setDescriptor( des[3] );

      info("<html><p>This is the Data directory array section of the OP header. Every element has a different use.<br /><br />The virtual address positions are useless without setting up the mapped sections after the array.<br /><br />" +
      "The virtual addresses are added to the programs \"Base Address\". The \"Base Address\" is defined by the OP header.<br /><br />Anything that is 0, is not used.</p></html>");
    }
    else if( h.equals("Mapped SECTOINS TO RAM.h") )
    {
      ds.setDescriptor( des[4] );

      info("<html><p>The PE header specifies the number of sections to read.<br /><br />Each section specifies where to read the file, and size, and at what address to place the data in virtual Memory, and size.<br /><br />" +
      "Each virtual address is added to the programs \"Base Address\". The \"Base Address\" is defined by the OP header.</p></html>");
    }
    else if( h.equals("Header Data") )
    {
      Offset.setSelected( 0, ( des[4].pos + des[4].length ) - 1 );

      info("<html><p>The headers setup the Microsoft binary virtual space.<br /><br />Otherwise The import table can not be located.<br /><br />" +
      "Export Table can not be located.<br /><br />" +
      "Files that are included in the binary. Called Resource Files. Also can not be located.<br /><br />" +
      "Nether can the machine code Start position.</p></html>");
    }

    //Seek virtual address position. Thus begin reading section.

    else if( h.equals("function Export Table.h") )
    {
      try
      {
        b.seekV(data.DataDir[0]);b.seekV(data.DataDir[0]);
        Virtual.setSelected( data.DataDir[0], data.DataDir[0] + data.DataDir[1] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[1] - 1 );
      }
      catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }
    else if( h.startsWith( "DLL Import Table", 0 ) )
    {
      try
      {
        b.seekV(data.DataDir[2]);b.seekV(data.DataDir[2]);

        //Decode import table.

        if( DLL_des == null )
        {
          b.Events = false;
          
          Import.setUserObject("DLL Import Table"); DLL_des = DLL.LoadDLLImport( b, Import );
          
          b.Events = true;
        }

        Virtual.setSelected( data.DataDir[2], data.DataDir[2] + data.DataDir[3] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[3] - 1 );
      }
      catch( IOException e ) { }

      //Update the tree.

      ((DefaultTreeModel)tree.getModel()).nodeChanged( Import ); tree.expandPath( new TreePath( Import.getPath() ) );
    }
    else if( h.equals( "DLL IMPORT ARRAY DECODE.H" ) ) { ds.setDescriptor( DLL_des[DLL_des.length-1] ); }
    else if( h.startsWith( "Function Array Decode.H", 0 ) ) { ds.setDescriptor( DLL_des[ Integer.parseInt( h.split("#")[1] ) ] ); }

    else if( h.equals("Resource Files.h") )
    {
      try
      {
        b.seekV(data.DataDir[4]);b.seekV(data.DataDir[4]);
        Virtual.setSelected( data.DataDir[4], data.DataDir[4] + data.DataDir[5] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[5] - 1 );
      }
      catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Exception Table.h") )
    {
      try
      {
        b.seekV(data.DataDir[6]);b.seekV(data.DataDir[6]);
        Virtual.setSelected( data.DataDir[6], data.DataDir[6] + data.DataDir[7] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[7] - 1 );
      }
      catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Security Level Settings.h") )
    {
      try
      {
        b.seekV(data.DataDir[8]);b.seekV(data.DataDir[8]);
        Virtual.setSelected( data.DataDir[8], data.DataDir[8] + data.DataDir[9] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[9] - 1 );
      }
      catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Relocation/Patching.h") )
    {
      try
      {
        b.seekV(data.DataDir[10]);b.seekV(data.DataDir[10]);
        Virtual.setSelected( data.DataDir[10], data.DataDir[10] + data.DataDir[11] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[11] - 1 );
      }
      catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("DEBUG TABLE.h") )
    {
      try
      {
        b.seekV(data.DataDir[12]);b.seekV(data.DataDir[12]);
        Virtual.setSelected( data.DataDir[12], data.DataDir[12] + data.DataDir[13] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[13] - 1 );
      }
      catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Description/Architecture.h") )
    {
      try
      {
        b.seekV(data.DataDir[14]);b.seekV(data.DataDir[14]);
        Virtual.setSelected( data.DataDir[14], data.DataDir[14] + data.DataDir[15] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[15] - 1 );
      }
      catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Machine Value.h") )
    {
      try
      {
        b.seekV(data.DataDir[16]);b.seekV(data.DataDir[16]);
        Virtual.setSelected( data.DataDir[16], data.DataDir[16] + data.DataDir[17] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[17] - 1 );
      }
      catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Thread Storage Lowcation.h") )
    {
      try
      {
        b.seekV(data.DataDir[18]);b.seekV(data.DataDir[18]);
        Virtual.setSelected( data.DataDir[18], data.DataDir[18] + data.DataDir[19] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[19] - 1 );
      }
      catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Load System Configuration.h") )
    {
      try
      {
        b.seekV(data.DataDir[20]);b.seekV(data.DataDir[20]);
        Virtual.setSelected( data.DataDir[20], data.DataDir[20] + data.DataDir[21] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[21] - 1 );
      }
      catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Import Table of Functions inside program.h") )
    {
      try
      {
        b.seekV(data.DataDir[22]);b.seekV(data.DataDir[22]);
        Virtual.setSelected( data.DataDir[22], data.DataDir[22] + data.DataDir[23] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[23] - 1 );
      }
      catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Import Address Setup Table.h") )
    {
      try
      {
        b.seekV(data.DataDir[24]);b.seekV(data.DataDir[24]);
        Virtual.setSelected( data.DataDir[24], data.DataDir[24] + data.DataDir[25] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[25] - 1 );
      }
      catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Delayed Import Table.h") )
    {
      try
      {
        b.seekV(data.DataDir[26]);b.seekV(data.DataDir[26]);
        Virtual.setSelected( data.DataDir[26], data.DataDir[26] + data.DataDir[27] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[27] - 1 );
      }
      catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("COM Runtime Descriptor.h") )
    {
      try
      {
        b.seekV(data.DataDir[28]);b.seekV(data.DataDir[28]);
        Virtual.setSelected( data.DataDir[28], data.DataDir[28] + data.DataDir[29] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[29] - 1 );
      }
      catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }

    else
    {
      try
      {
        long s = Long.parseLong( h.split("#")[1] ); b.seekV(s);
      }
      catch( IOException e ) { }
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
    info("<html>The processor core is not supported.</html>");
  }
}