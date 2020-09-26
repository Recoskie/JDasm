package WindowCompoents;
import javax.swing.*;
import java.awt.*;
import javax.swing.tree.*;
import VHex.*;

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

  //Output of a decoded section of a file format in table. Set by file reader.

  public static JTable out;

  //Additional detailed information output. For data in table cells, or section.

  public static JLabel infoData = new JLabel("");

  //Hex editor.

  public static VHex Virtual, Offset;

  //Once hex editor is initialized. Then the target is set afterwards for new files.

  public static boolean HInit = false;

  //Hex editor view options.

  public static boolean textV = true, addV = true;

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

    JSplitPane p1 = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, new JScrollPane( tree ), new JScrollPane( out ) ), infoData );

    //Binary tools.

    JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));

    //Hex editor view, or additional binary tools.

    p2.add( Virtual ); p2.add( Offset );

    //Septate the two panels.

    f.add( new JSplitPane(JSplitPane.VERTICAL_SPLIT, p1, p2) );

    f.setJMenuBar( bdBar );
    
    f.validate();
  }

  //Hex edit mode. Binary tools only.

  public void editMode()
  {
    f.getContentPane().removeAll();

    if( addV ) { f.setLayout(new GridLayout(1,2)); } else { f.setLayout(new GridLayout(1,1)); }

    if( addV ) { f.add( Virtual ); } f.add( Offset );

    f.setJMenuBar(bdBar);
    
    f.validate(); f.repaint();
  }
}