package EXEDecode;
import javax.swing.*;

//The descriptor allows decoded information in headers to be highlighted and explained.

public class Descriptor extends JTable
{
  public Descriptor( Object[][] rows, Object[] cols )
  {
    super( rows, cols );
  }

  @Override public boolean isCellEditable(int row, int column) { return(false); }
}