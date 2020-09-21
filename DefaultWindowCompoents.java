import javax.swing.*;
import java.awt.*;
import javax.swing.tree.*;

public class DefaultWindowCompoents
{
  public static JFrame f;
  public static JTree tree;
  public static Object UsedDecoder;

  public void SetDefault()
  {
    f.setLayout(new GridLayout(0,1));
    
    f.getContentPane().removeAll();

    f.add(tree);
    
    f.add(new JScrollPane(tree));
    
    tree.setShowsRootHandles(false);
    
    tree.setRootVisible(false);
    
    f.setVisible(true);
  }
}