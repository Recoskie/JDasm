package EXEDecode;
import javax.swing.*;
import WindowCompoents.*;

//The descriptor allows decoded information in headers to be explained in detail.

public class Descriptor extends JTable
{
  //Types of data.

  public static final int MZ = 0, PE = 1, OP = 2, dataDirectoryArray = 3, sections = 4;

  //The set Type.

  public static int type = -1;

  //Basic constructor.

  public Descriptor( Object[][] rows, Object[] cols ) { super( rows, cols ); }

  //Set the descriptor type.

  public void setType( int t ){ type = t; }

  //No cells are editable. Also on click/edit display detailed information of row.

  @Override public boolean isCellEditable( int row, int col )
  {
    if( type == MZ ) { MZinfo( row ); }

    //No cells are editable.
    
    return(false);
  }

  //Detailed description of the MZ header.

  public static final int[] MZsec = new int[]{0,2,4,6,8,10,12,14,16,18,20,22,24,26,28,36,38,40,60,64};

  public void MZinfo( int row )
  {
    //Select Bytes.

    WindowCompoents.Offset.setSelected( MZsec[row], row == 19 ? Data.PE - 1 : MZsec[row+1] - 1 );

    //No description outputs yet.

  }
}