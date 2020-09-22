import javax.swing.*;
import java.io.*;
import javax.swing.tree.*;
import java.awt.*;
import EXEDecode.*;
import RandomAccessFileV.*;

public class EXE extends DefaultWindowCompoents implements ExploerEventListener
{
  //file system.

  public RandomAccessFileV b;

  public JTable DebugOut[] = new JTable[5];

  //plug in the executable Readers

  public static Data data = new Data();
  public Headers Header = new Headers();

  //public DLLImport DLL = new DLLImport();
  //public Resource RSRC = new Resource();

  public EXE() { UsedDecoder = this; }

  //plug in the separate decoders of the exe format together

  public void read(String F, RandomAccessFileV file )
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
      try { DebugOut[3] = Header.readDataDrectory( b ); } catch(Exception e) {  data.DataDirUsed = new boolean[16]; }
      try { DebugOut[4] = Header.readSections( b ); } catch(Exception e) { }

      DefaultMutableTreeNode MZ = new DefaultMutableTreeNode("MZ Header.h");

      Headers.add(MZ);
      Headers.add(new DefaultMutableTreeNode("PE Header.h"));
      Headers.add(new DefaultMutableTreeNode("OP Header.h"));
      Headers.add(new DefaultMutableTreeNode("Data Directory Array.h"));
      Headers.add(new DefaultMutableTreeNode("Mapped EXE SECTOINS TO RAM.h"));

      root.add( Headers );

      //Location of the export directory

      if( data.DataDirUsed[0] ) { DefaultMutableTreeNode Export = new DefaultMutableTreeNode("function Export Table.h"); root.add(Export); }

      //Location of the import directory

      if( data.DataDirUsed[1] ) { DefaultMutableTreeNode Import = new DefaultMutableTreeNode("DLL Import Table.h"); root.add(Import); }

      //Location of the resource directory

      if( data.DataDirUsed[2] ) { DefaultMutableTreeNode RE = new DefaultMutableTreeNode("Resource Files.h"); root.add(RE); }

      //Exception

      if( data.DataDirUsed[3] ) { DefaultMutableTreeNode EX = new DefaultMutableTreeNode("Exception Table.h"); root.add(EX); }

      //Security

      if( data.DataDirUsed[4] ) { DefaultMutableTreeNode Security = new DefaultMutableTreeNode("Security Level Settings.h"); root.add(Security); }

      //Relocation/Patching

      if( data.DataDirUsed[5] ) { DefaultMutableTreeNode RELOC = new DefaultMutableTreeNode("Relocation/Patching.h"); root.add(RELOC); }

      //Debug

      if( data.DataDirUsed[6] ) { DefaultMutableTreeNode DEBUG = new DefaultMutableTreeNode("DEBUG TABLE.h"); root.add(DEBUG); }

      //Description/Architecture

      if( data.DataDirUsed[7] ) { DefaultMutableTreeNode Decription = new DefaultMutableTreeNode("Description/Architecture.h"); root.add(Decription); }

      //Machine Value

      if( data.DataDirUsed[8] ) { DefaultMutableTreeNode MV = new DefaultMutableTreeNode("Machine Value.h"); root.add(MV); }

      //Thread Storage

      if( data.DataDirUsed[9] ) { DefaultMutableTreeNode TS = new DefaultMutableTreeNode("Thread Storage Lowcation.h"); root.add(TS); }

      //Load System Configuration

      if( data.DataDirUsed[10] ) { DefaultMutableTreeNode ConFIG = new DefaultMutableTreeNode("Load System Configuration.h"); root.add(ConFIG); }

      //Location of alternate import-binding director

      if( data.DataDirUsed[11] ) { DefaultMutableTreeNode BoundImport = new DefaultMutableTreeNode("Import Table of Functions inside program.h"); root.add(BoundImport); }

      //Import Address Table

      if( data.DataDirUsed[12] ) { DefaultMutableTreeNode ImportAddress = new DefaultMutableTreeNode("Import Address Setup Table.h"); root.add(ImportAddress); }

      //Delayed Imports

      if( data.DataDirUsed[13] ) { DefaultMutableTreeNode DelayImport = new DefaultMutableTreeNode("Delayed Import Table.h"); root.add(DelayImport); }

      //COM Runtime Descriptor

      if( data.DataDirUsed[14] ) { DefaultMutableTreeNode COM = new DefaultMutableTreeNode("COM Runtime Descriptor.h"); root.add(COM); }

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
    //Headers must be decoded before any other part of the program can be read.

    if( h.equals("MZ Header.h") ) { out = DebugOut[0]; }
    else if( h.equals("PE Header.h") ) { out = DebugOut[1]; }
    else if( h.equals("OP Header.h") ) { out = DebugOut[2]; }
    else if( h.equals("Data Directory Array.h") ) { out = DebugOut[3]; }
    else if( h.equals("Mapped EXE SECTOINS TO RAM.h") ) { out = DebugOut[4]; }

    //Seek virtual address position. Thus begin reading section.

    else if( h.equals("function Export Table.h") )
    {
      try{ b.seekV( data.DataDir[0] ); } catch( IOException e ) { }

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("DLL Import Table.h") )
    {
      try{ b.seekV( data.DataDir[1] ); } catch( IOException e ) {}

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Resource Files.h") )
    {
      try{ b.seekV( data.DataDir[2] ); } catch( IOException e ) {}

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Exception Table.h") )
    {
      try{ b.seekV( data.DataDir[3] ); } catch( IOException e ) {}

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Security Level Settings.h") )
    {
      try{ b.seekV( data.DataDir[4] ); } catch( IOException e ) {}

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Relocation/Patching.h") )
    {
      try{ b.seekV( data.DataDir[5] ); } catch( IOException e ) {}

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("DEBUG TABLE.h") )
    {
      try{ b.seekV( data.DataDir[6] ); } catch( IOException e ) {}

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Description/Architecture.h") )
    {
      try{ b.seekV( data.DataDir[7] ); } catch( IOException e ) {}

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Machine Value.h") )
    {
      try{ b.seekV( data.DataDir[8] ); } catch( IOException e ) {}

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Thread Storage Lowcation.h") )
    {
      try{ b.seekV( data.DataDir[9] ); } catch( IOException e ) {}

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Load System Configuration.h") )
    {
      try{ b.seekV( data.DataDir[10] ); } catch( IOException e ) {}

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Import Table of Functions inside program.h") )
    {
      try{ b.seekV( data.DataDir[11] ); } catch( IOException e ) {}

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Import Address Setup Table.h") )
    {
      try{ b.seekV( data.DataDir[12] ); } catch( IOException e ) {}

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("Delayed Import Table.h") )
    {
      try{ b.seekV( data.DataDir[13] ); } catch( IOException e ) {}

      //Decoder goes here.

      noDecode();
    }
    else if( h.equals("COM Runtime Descriptor.h") )
    {
      try{ b.seekV( data.DataDir[14] ); } catch( IOException e ) {}

      //Decoder goes here.

      noDecode();
    }

    //Update the window.

    updateWindowData();
  }

  //No Decoder.

  public void noDecode()
  { 
    out = new JTable( ( new Object[][] { { "NO DECODER" } } ), ( new Object[]{ "NO DECODER HAS BEN WORTE YET" } ) );
  }
}