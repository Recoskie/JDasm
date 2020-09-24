import javax.swing.*;
import java.io.*;
import javax.swing.tree.*;
import java.awt.*;
import EXEDecode.*;
import RandomAccessFileV.*;
import WindowCompoents.*;

public class EXE extends WindowCompoents implements ExploerEventListener
{
  //file system.

  public RandomAccessFileV b;

  //The new Descriptor table allows a description of clicked data.

  public Descriptor DebugOut[] = new Descriptor[5];

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

  public EXE() { UsedDecoder = this; }

  //plug in the separate decoders of the exe format together

  public void read( String F, RandomAccessFileV file )
  {
    b = file;

    //Root node is now the target file.
 
    ((DefaultTreeModel)tree.getModel()).setRoot(null);
    tree.setRootVisible(true);tree.setShowsRootHandles(true);
    DefaultMutableTreeNode root = new DefaultMutableTreeNode( F );

    //header data folder to organize exe setup information.
  
    DefaultMutableTreeNode Headers = new DefaultMutableTreeNode("Header Data");

    //Decode the setup headers.

    try
    {
      try { DebugOut[0] = Header.readMZ( b ); } catch(Exception e) { }
      try { DebugOut[1] = Header.readPE( b ); } catch(Exception e) { }
      try { DebugOut[2] = Header.readOP( b ); } catch(Exception e) { }
      try { DebugOut[3] = Header.readDataDrectory( b ); } catch(Exception e) { data.DataDirUsed = new boolean[16]; }
      try { DebugOut[4] = Header.readSections( b ); } catch(Exception e) { }

      Headers.add(new DefaultMutableTreeNode("MZ Header.h"));
      Headers.add(new DefaultMutableTreeNode("PE Header.h"));
      Headers.add(new DefaultMutableTreeNode("OP Header.h"));
      Headers.add(new DefaultMutableTreeNode("Data Directory Array.h"));
      Headers.add(new DefaultMutableTreeNode("Mapped EXE SECTOINS TO RAM.h"));

      root.add( Headers );

      //Start of code.

      if( data.baseOfCode != 0 ) { root.add(new DefaultMutableTreeNode("Program Start (Machine code).h")); }

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
    catch(Exception e)
    {
      JOptionPane.showMessageDialog(null,"Error "+e);
    }
  }

  //Change What To Display Based on what the user clicks on

  public void elementOpen(String h)
  {
    //Start of application.
    
    if( h.equals("Program Start (Machine code).h") )
    {
      try
      {
        b.seekV( data.baseOfCode );
        Virtual.setSelected( data.baseOfCode, data.baseOfCode + data.sizeOfCode - 1 );
        Offset.setSelected( b.getFilePointer(), b.getFilePointer() + data.sizeOfCode - 1 );
      }
      catch( IOException e ) { }

      //Disassembler is not ready yet.

      noDecode();
    }

    //Headers must be decoded before any other part of the program can be read.

    else if( h.equals("MZ Header.h") )
    {
      Offset.setSelected( 0, data.PE - 1 );
      
      DebugOut[0].setType( Descriptor.MZ ); out = DebugOut[0];
    }
    else if( h.equals("PE Header.h") )
    {
      Offset.setSelected( data.PE, data.PE + 23 );

      DebugOut[1].setType( Descriptor.PE ); out = DebugOut[1];
    }
    else if( h.equals("OP Header.h") )
    {
      Offset.setSelected( data.PE + 24, data.is64bit ? data.PE + 135 : data.PE + 119 );

      DebugOut[2].setType( Descriptor.OP ); out = DebugOut[2];
    }
    else if( h.equals("Data Directory Array.h") )
    {
      long pos = data.is64bit ? data.PE + 136 : data.PE + 120;

      Offset.setSelected( pos, pos + ( ( data.DDS / 3 ) << 3 ) - 1 );

      DebugOut[3].setType( Descriptor.dataDirectoryArray ); out = DebugOut[3];
    }
    else if( h.equals("Mapped EXE SECTOINS TO RAM.h") )
    {
      long pos = ( data.is64bit ? data.PE + 136 : data.PE + 120 ) + ( ( data.DDS / 3 ) << 3 );

      Offset.setSelected( pos, pos + ( data.NOS * 40 ) - 1 );

      DebugOut[4].setType( Descriptor.sections ); out = DebugOut[4];
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

    //Update the window.

    updateWindow();
  }

  //No Decoder.

  public void noDecode()
  { 
    out = new JTable( ( new Object[][] { { "NO DECODER" } } ), ( new Object[]{ "NO DECODER HAS BEN MADE YET" } ) );
    out.setEnabled(false);
  }
}