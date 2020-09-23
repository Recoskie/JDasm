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
    if( type == MZ ) { MZinfo( row ); } else if( type == PE ) { PEinfo( row ); } else if( type == OP ) { OPinfo( row ); }

    else if( type == dataDirectoryArray ){ DDRinfo( row ); }

    else if( type == sections ){ Sinfo( row ); }

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

  //Detailed description of the PE header.

  public static final int[] PEsec = new int[]{0,4,6,8,12,16,20,22,24};

  public void PEinfo( int row )
  {
    //Select Bytes.

    WindowCompoents.Offset.setSelected( Data.PE + PEsec[row], Data.PE + PEsec[row+1] - 1 );

    //No description outputs yet.

  }

  //Detailed description of the OP header.

  public static final int[] OP32sec = new int[]{24,26,27,28,32,36,40,44,48,52,56,60,64,66,68,70,72,74,76,80,84,88,92,94,96,100,104,108,112,116,120};
  public static final int[] OP64sec = new int[]{24,26,27,28,32,36,40,44,48,52,56,60,64,66,68,70,72,74,76,80,84,88,92,94,96,104,112,120,128,132,136};

  public void OPinfo( int row )
  {
    //Select Bytes.

    WindowCompoents.Offset.setSelected( Data.PE + ( Data.is64bit ? OP64sec[row] : OP32sec[row] ), Data.PE + ( Data.is64bit ? OP64sec[row+1] : OP32sec[row+1] ) - 1 );

    //No description outputs yet.

  }

  //Detailed description of the data Directory Array.

  public void DDRinfo( int row )
  {
    int pos = (int)Data.PE + ( Data.is64bit ? 136 : 120 ); pos += ( row / 3 ) << 3;

    //Select Bytes.

    int end = row % 3;

    if( end == 0 ) { end = pos + 7; }

    if( end == 1 ) { end = pos + 3; }

    if( end == 2 ) { pos += 4; end = pos + 3; }

    WindowCompoents.Offset.setSelected( pos, end );

    //No extra information possible.
  }

  //Detailed description of the sections to RAM memory.

  public void Sinfo( int row )
  {
    int pos = (int)Data.PE + ( Data.is64bit ? 136 : 120 ); pos += ( Data.DDS / 3 ) << 3;

    //Select Bytes.

    int end = row % 7; pos += ( row / 7 ) * 40;

    if( end == 0 ){ end = pos + 7; }
    else if( end < 5 ) { end -= 1; pos += 8 + (end << 2); end = pos + 3; }
    else if( end == 5 ) { pos += 24; end = pos + 11; }
    else if( end == 6 ) { pos += 36; end = pos + 3; }

    WindowCompoents.Offset.setSelected( pos, end );

    //Some detailed descriptions can be added.
  }
}