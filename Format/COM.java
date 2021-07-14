package Format;

import swingIO.Descriptor;
import swingIO.tree.*;
import javax.swing.tree.*;

//Processor cores.

import core.x86.*; //X86.

public class COM extends Window.Window implements JDEventListener
{
  private static Descriptor blank = new Descriptor( file );

  public COM()
  {
    tree.setEventListener( this );

    //Root node is now the target file.
 
    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true); tree.setShowsRootHandles(true);
    
    JDNode root = new JDNode( fc.getFileName() );

    //header data folder to organize exe setup information.
  
    JDNode t = new JDNode("Info.h"); root.add( t ); root.add( new JDNode("Program Start (Machine code).h") );

    if( core == null || core.type() != 0 ){ core = new X86( file ); } else { core.setTarget( file ); }

    try { file.addV( 0, file.length(), 0x0100, file.length() ); } catch( Exception e ) { }

    core.setBit(X86.x86_16); core.setEvent( this::Dis );

    ((DefaultTreeModel)tree.getModel()).setRoot(root);

    tree.setSelectionPath( new TreePath( t.getPath() ) ); open( new JDEvent(this, "Info.h") );
  }

  public void open( JDEvent e )
  {
    if( e.getPath().equals("Info.h") )
    {
      ds.setDescriptor( blank );
      info("<html>DOS COM Files have no header or setup information. The program begins at the start of the file.</html>");
    }
    else
    {
      core.clear(); core.Crawl.add( 0x0100L ); core.disLoc(0, true); ds.setDescriptor( core );
    }
  }

  //Disassemble routine.

  public void Dis( int loc, boolean crawl )
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
}
