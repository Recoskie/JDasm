import javax.swing.*;
import java.io.*;
import javax.swing.tree.*;
import java.awt.*;
import EXEDecode.*;
import RandomAccessFileV.*;
import WindowCompoents.*;

//Processor cores.

import core.x86.*; //X86.

public class EXE extends WindowCompoents implements ExploerEventListener
{
  //file system.

  public RandomAccessFileV b;

  //The disassembler.

  public static Object core;

  //The new Descriptor table allows a description of clicked data.

  public Descriptor des = new Descriptor( new Object[][] {{}}, new Object[]{} );
  public javax.swing.table.DefaultTableModel TData[] = new javax.swing.table.DefaultTableModel[5];

  //Nodes that can be added to when Adding section format readers.

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

  //public DLLImport DLL = new DLLImport();
  //public Resource RSRC = new Resource();

  public EXE() { UsedDecoder = this; out = des; out.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); }

  //plug in the separate decoders of the exe format together

  public void read( String F, RandomAccessFileV file )
  {
    b = file; data.core = new X86( b );

    //Root node is now the target file.
 
    ((DefaultTreeModel)tree.getModel()).setRoot(null);
    tree.setRootVisible(true);tree.setShowsRootHandles(true);
    DefaultMutableTreeNode root = new DefaultMutableTreeNode( F );

    //header data folder to organize exe setup information.
  
    DefaultMutableTreeNode Headers = new DefaultMutableTreeNode("Header Data");

    //Decode the setup headers.

    try
    {
      TData[0] = Header.readMZ( b );
      TData[1] = Header.readPE( b );
      TData[2] = Header.readOP( b );
      TData[3] = Header.readDataDrectory( b );
      TData[4] = Header.readSections( b );
    }
    catch(Exception e) { data.DataDirUsed = new boolean[15]; }

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

    ((DefaultTreeModel)tree.getModel()).setRoot(root); f.setVisible(true);
  }

  //Change What To Display Based on what the user clicks on

  public void elementOpen(String h)
  {
    //Start of application.
    
    if( h.equals("Program Start (Machine code).h") )
    {
      String t = "", t2 = "";

      try
      {
        b.seekV( data.startOfCode );

        //Disassembler.

        if( data.is64bit ) { ((X86)data.core).setBit( X86.x86_64 ); } else { ((X86)data.core).setBit( X86.x86_32 ); }

        long end = data.baseOfCode + data.sizeOfCode;

        //Disassemble till end, or return from application.
        //Note that more can be added here such as the jump operation.

        while( t2.indexOf("RET") != 0 && b.getVirtualPointer() < end )
        {
          t2 = ((X86)data.core).decodeInstruction(); t += ((X86)data.core).pos() + " " + t2 + "<br />";
        }

        long f = ((X86)data.core).getPos() - 1, v = ((X86)data.core).getPosV() - 1;

        b.seekV( data.startOfCode ); Virtual.setSelected( data.startOfCode, v ); Offset.setSelected( b.getFilePointer(), f );

        info( "<html>" + t + "</html>" );
      }
      catch( Exception e ) { }
    }

    //Headers must be decoded before any other part of the program can be read.

    else if( h.equals("MZ Header.h") )
    {
      Offset.setSelected( 0, data.PE - 1 );
      
      des.setType( Descriptor.MZ ); out.setModel( TData[0] );

      info("<html><p>This is the original DOS header. Which must be at the start of all windows binary files.<br /><br />Today the reserved bytes are used to locate to the new Portable executable header format.<br /><br />" +
      "However, on DOS this header still loads as the reserved bytes that locate to the PE header do nothing in DOS.<br /><br />Thus the small 16 bit binary at the end will run. " +
      "Which normally contains a small 16 bit code that prints the message that this program can not be run in DOS mode.</p></html>");
    }
    else if( h.equals("PE Header.h") )
    {
      Offset.setSelected( data.PE, data.PE + 23 );

      des.setType( Descriptor.PE ); out.setModel( TData[1] );

      info("<html><p>The PE header marks the start of the new Executable format. If the file is not loaded in DOS.<br /><br />" +
      "This header specifies the number of sections to map in virtual space. The processor type, and date of compilation.</p></html>");
    }
    else if( h.equals("OP Header.h") )
    {
      Offset.setSelected( data.PE + 24, data.is64bit ? data.PE + 135 : data.PE + 119 );

      des.setType( Descriptor.OP ); out.setModel( TData[2] );

      info("<html><p>At the end of the PE header is the start of the Optional header. However, this header is not optional.</p></html>");
    }
    else if( h.equals("Data Directory Array.h") )
    {
      long pos = data.is64bit ? data.PE + 136 : data.PE + 120;

      Offset.setSelected( pos, pos + ( ( data.DDS / 3 ) << 3 ) - 1 );

      des.setType( Descriptor.dataDirectoryArray ); out.setModel( TData[3] );

      info("<html><p>This is the Data directory array section of the OP header. Every element has a different use.<br /><br />The virtual address positions are useless without setting up the mapped sections after the array.<br /><br />" +
      "Anything that is 0, is not used.</p></html>");
    }
    else if( h.equals("Mapped SECTOINS TO RAM.h") )
    {
      long pos = ( data.is64bit ? data.PE + 136 : data.PE + 120 ) + ( ( data.DDS / 3 ) << 3 );

      Offset.setSelected( pos, pos + ( data.NOS * 40 ) - 1 );

      des.setType( Descriptor.sections ); out.setModel( TData[4] );

      info("<html><p>The PE header specifies the number of sections to read.<br /><br />Each section specifies where to read the file, and size, and at what address to place the data in virtual Memory, and size.<br /><br />" +
      "Without doing this, the data Directory Array is useless. Also the base of code in the OP header is a virtual address position. Which is the start of the programs machine code.</p></html>");
    }
    else if( h.equals("Header Data") )
    {
      long pos = ( data.is64bit ? data.PE + 136 : data.PE + 120 ) + ( ( data.DDS / 3 ) << 3 );

      Offset.setSelected( 0, pos + ( data.NOS * 40 ) - 1 );

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
        b.seekV( data.DataDir[0] );
        Virtual.setSelected( data.DataDir[0], data.DataDir[0] + data.DataDir[1] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[1] - 1 );
      }
      catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("DLL Import Table.h") )
    {
      try
      {
        b.seekV( data.DataDir[2] );
        Virtual.setSelected( data.DataDir[2], data.DataDir[2] + data.DataDir[3] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[3] - 1 );
      }
      catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Resource Files.h") )
    {
      try
      {
        b.seekV( data.DataDir[4] );
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
        b.seekV( data.DataDir[6] );
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
        b.seekV( data.DataDir[8] );
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
        b.seekV( data.DataDir[10] );
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
        b.seekV( data.DataDir[12] );
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
        b.seekV( data.DataDir[14] );
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
        b.seekV( data.DataDir[16] );
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
        b.seekV( data.DataDir[18] );
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
        b.seekV( data.DataDir[20] );
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
        b.seekV( data.DataDir[22] );
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
        b.seekV( data.DataDir[24] );
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
        b.seekV( data.DataDir[26] );
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
        b.seekV( data.DataDir[28] );
        Virtual.setSelected( data.DataDir[28], data.DataDir[28] + data.DataDir[29] - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.DataDir[29] - 1 );
      }
      catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }
  }

  //No Decoder.

  public void noDecode()
  { 
    info(""); out.setModel(new JTable( ( new Object[][] { { "NO DECODER" } } ), ( new Object[]{ "NO DECODER HAS BEN MADE YET" } ) ).getModel());
  }
}