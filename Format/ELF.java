package Format;

import swingIO.*;
import swingIO.tree.*;
import javax.swing.tree.*;
import Format.ELFDecode.*;

public class ELF extends Data implements JDEventListener
{
  //Descriptors.

  private Descriptor[][] des = new Descriptor[1][];

  private JDNode root;

  //ELF reader plugins.

  private static Headers header = new Headers(); 

  //Read the ELF binary.

  public ELF()
  {
    tree.setEventListener( this );

    file.Events = false;

    //Root node is now the target file.
 
    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true); tree.setShowsRootHandles(true); root = new JDNode( fc.getFileName() + ".exe" );

    //The ELF header.
  
    JDNode ELFHeader = new JDNode( "ELF Header.h", new long[]{ 0, 0 } );
    JDNode PHeader = new JDNode( "Program Header.h", new long[]{ 0, 1 } );
    JDNode SECHeader = new JDNode( "Section Header.h", new long[]{ 0, 2 } );
    
    des[0] = new Descriptor[3];

    try
    {
      des[0][0] = header.readELF();
      //des[0][1] = header.readProgram();
      //des[0][2] = header.readSections();
    }
    catch(Exception e) { Data.error = true; }

    if( !Data.error )
    {
      //Decode the setup headers.
    
      root.add(ELFHeader); ((DefaultTreeModel)tree.getModel()).setRoot(root);

      file.Events = true;

      //Set the default node.

      tree.setSelectionPath( new TreePath( ELFHeader.getPath() ) ); open( new JDEvent( this, "", new long[]{ 0, 0 } ) );
    }
    else{ file.Events = true; }
  }

  public void open( JDEvent e )
  {
    if( e.getArgs().length > 1 ) { ds.setDescriptor( des[ (int)e.getArg(0) ][ (int)e.getArg(1) ] ); }
  }
}
