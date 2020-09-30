package WindowCompoents;
import javax.swing.*;
import java.awt.*;

public class autoSizeScroll extends JScrollPane
{
  public autoSizeScroll( Component c ) { super( c ); }

  @Override public Dimension getMinimumSize()
  {
    return( new Dimension( 700, 40 ) );
  }

  @Override public Dimension getPreferredSize()
  {
    return( new Dimension( 700, super.getParent().getHeight() ) );
  }
}