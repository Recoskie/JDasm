import javax.swing.*;
import java.awt.*;
import javax.swing.tree.*;
import VHex.*;

public class DefaultWindowCompoents
{
  //Main application Window.

  public static JFrame f;

  //File chooser menu bar.

  public static JMenuBar fcBar;

  //Binary tool options, and views.

  public static JMenuBar bdBar;

  //File chooser tree, or header data.

  public static JTree tree;

  //Output of a decoded header. Set by file reader.

  public static JTable out;

  //Hex editor.

  public static VHex Virtual, Offset;

  //Once hex editor is initialized then the target is set afterwards for new files.

  public static boolean HInit = false;

  //Hex editor view options.

  public static boolean textV = true, addV = true;

  //The current file reader.

  public static Object UsedDecoder;

  //Back to file chooser.

  public void fileChooser()
  {
    f.setLayout(new GridLayout(1,1));
    
    f.getContentPane().removeAll();

    f.add(tree);
    
    f.add(new JScrollPane(tree));
    
    tree.setShowsRootHandles(false);
    
    tree.setRootVisible(false);

    f.setJMenuBar(fcBar);
    
    f.validate();
  }

  //Update window when viewing decoded data.

  public void updateWindowData()
  {
    if(addV) { f.setLayout(new GridLayout(1,4)); } else { f.setLayout(new GridLayout(1,3)); }
    
    f.getContentPane().removeAll();

    f.add(tree); f.add(new JScrollPane(tree));

    f.add(out); f.add(new JScrollPane(out));

    if( addV ) { f.add( Virtual ); } f.add( Offset );

    f.setJMenuBar(bdBar);
    
    f.validate();
  }

  //Update window.

  public void updateWindow()
  {
    if(addV) { f.setLayout(new GridLayout(1,3)); } else { f.setLayout(new GridLayout(1,2)); }
    
    f.getContentPane().removeAll();

    f.add(tree); f.add(new JScrollPane(tree));

    if( addV ) { f.add( Virtual ); } f.add( Offset );

    f.setJMenuBar(bdBar);
    
    f.validate();
  }

  //Hex edit mode. Binary tools.

  public void editMode()
  {
    if(addV) { f.setLayout(new GridLayout(1,2)); } else { f.setLayout(new GridLayout(1,1)); }
    
    f.getContentPane().removeAll();

    if( addV ) { f.add( Virtual ); } f.add( Offset );

    f.setJMenuBar(bdBar);
    
    f.validate(); f.repaint();
  }
}