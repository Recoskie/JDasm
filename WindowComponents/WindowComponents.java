package WindowComponents;

import javax.swing.*;
import java.awt.*;
import VHex.*;
import dataTools.*;
import cellPane.*;

public class WindowComponents
{
  //Main application Window.

  public static JFrame f;

  //File chooser menu bar.

  public static JMenuBar fcBar;

  //Binary tool options, and views.

  public static JMenuBar bdBar;

  //Menu item actions.

  public static JPopupMenu pm;

  //File chooser tree, or data from a file format reader.

  public static JTree tree;

  //Additional detailed information output. For data in table cells, or section.
  //Also disassembly output.

  public static JTextPane infoData = new JTextPane();

  //Hex editor.

  public static VHex Virtual, Offset;

  //Data type inspector tool.

  public static dataInspector di;

  //Data descriptor tool.

  public static dataDescriptor ds;

  //Once hex editor is initialized. Then the target is set afterwards for new files.

  public static boolean HInit = false;

  //Hex editor view options.

  public static boolean textV = true;

  //The current file reader. Used for handling events to decode for the section of interest in the tree.

  public static Object UsedDecoder;

  //Back to file chooser.

  public void fileChooser()
  {
    f.getContentPane().removeAll();

    f.setLayout(new GridLayout(1,1));

    f.add(tree);
    
    f.add(new JScrollPane(tree));
    
    tree.setShowsRootHandles(false);
    
    tree.setRootVisible(false);

    f.setJMenuBar(fcBar);
    
    f.validate();
  }

  //Additional info text.

  public static void info( String s ) { infoData.setText(s); }

  //Update window when viewing decoded data.

  public void openFile()
  {
    f.getContentPane().removeAll();

    f.setLayout(new GridLayout(1,1));

    CellPane cp = new CellPane();

    //Data display tools.

    cp.add( new JScrollPane( tree ) ); cp.add( ds ); cp.add( new JScrollPane( infoData ) ); cp.rowEnd();

    //Binary tools.

    cp.add( Virtual ); cp.add( Offset ); cp.add( di ); cp.rowEnd();

    //Add the new cellPane to window.

    f.add( cp );

    f.setJMenuBar( bdBar );
    
    f.validate();
  }

  //Hex edit mode. Binary tools only.

  public void editMode()
  {
    f.getContentPane().removeAll(); Virtual.setVisible(false);

    f.setLayout(new GridLayout(1,1));

    CellPane cp = new CellPane();

    //Binary tools.

    cp.add( Virtual ); cp.add( Offset ); cp.add( di ); cp.rowEnd();

    //Add the new cellPane to window.

    f.add( cp );

    f.setJMenuBar( bdBar );
    
    f.validate();
  }

  //Loading.

  public void loading()
  {
    f.getContentPane().removeAll();

    f.setLayout( new GridLayout(1,1) );

    f.add( new JLabel( "Loading...", SwingConstants.CENTER ) );
    
    f.validate();
  }
}