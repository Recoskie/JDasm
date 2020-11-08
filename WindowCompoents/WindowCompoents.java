package WindowCompoents;

import javax.swing.*;
import java.awt.*;
import javax.swing.tree.*;
import VHex.*;
import dataTools.*;

public class WindowCompoents
{
  //Main application Window.

  public static JFrame f;

  //File chooser menu bar.

  public static JMenuBar fcBar;

  //Binary tool options, and views.

  public static JMenuBar bdBar;

  //File chooser tree, or data from a file format reader.

  public static JTree tree;

  //Additional detailed information output. For data in table cells, or section.
  //Also disassembly output.

  public static JLabel infoData = new JLabel("");

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

    JSplitPane p1 = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, new JScrollPane( tree ), new JScrollPane( ds ) ), new JScrollPane(infoData) );

    //Binary tools.

    JPanel p2 = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.VERTICAL;
    c.anchor = GridBagConstraints.FIRST_LINE_START;

    //Hex editor view, or additional binary tools.

    c.weightx = 1; p2.add( Virtual , c );
    
    c.weightx = 1000; p2.add( Offset, c );
    
    c.weightx = 1000000; c.weighty = 1; p2.add( di, c );

    //Septate the two panels.

    f.add( new JSplitPane(JSplitPane.VERTICAL_SPLIT, p1, p2) );

    f.setJMenuBar( bdBar );
    
    f.validate();
  }

  //Hex edit mode. Binary tools only.

  public void editMode()
  {
    f.getContentPane().removeAll(); Virtual.setVisible(false);

    f.setLayout(new GridLayout(1,1));

    JPanel p1 = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.VERTICAL;
    c.anchor = GridBagConstraints.FIRST_LINE_START;

    c.weightx = 1; c.gridx = 0; p1.add( Virtual , c );
    
    c.weightx = 1000; c.gridx = 1; p1.add( Offset, c );
    
    c.weightx = 1000000; c.gridx = 2; c.weighty = 1; p1.add( di, c );

    f.add( new JScrollPane( p1, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ) );

    f.setJMenuBar(bdBar);
    
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