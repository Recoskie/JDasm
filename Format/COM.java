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

    try { file.addV( 0, file.length(), 0x0100, file.length() + 0x0100 ); } catch( Exception e ) { }

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
      core.locations.clear(); core.data_off.clear(); core.code.clear();

      core.locations.add( 0x0100L ); core.disLoc(0); ds.setDescriptor( core );
    }
  }
}
