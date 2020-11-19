package dataTools;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.util.*;

public class dataDescriptor extends JComponent
{
  //The current data descriptor.

  private static Descriptor data;

  //The table.

  private JTable td;

  //The data type inspector.

  private static dataInspector di;

  //data model is set.

  private boolean set = false;

  //Cols.

  private String[] cols = new String[]{"Use", "Raw Data", "Value"}; 

  //Data type.

  private int type = 0;

  //The main display.

  private AbstractTableModel dModel = new AbstractTableModel()
  {
    public int getColumnCount() { return( 3 ); }

    public int getRowCount() { return( data.rows ); }

    public String getColumnName( int col ) { return ( cols[col] ); }
    
    public Object getValueAt(int row, int col)
    {
      return( data.data.get(row)[col] );
    }

    public boolean isCellEditable( int row, int col )
    {
      data.loc( row ); type = data.type.get(row);

      if ( type < 13 ) { di.setType( type ); }

      else if( type == 13 || type == 14 ) { di.setStringLen( data.rpos.get(row + 1) - data.rpos.get(row) ); di.setType( type ); }
      
      else if( type == 15 ) { di.setOther( data.rpos.get(row + 1) - data.rpos.get(row) ); }

      else if( type == 16 ) { di.setOther( data.apos.get(row + 1) - data.rpos.get(row) ); }

      data.Event.accept( row );

      return ( false );
    }
  };

  //Create Data descriptor table.

  public dataDescriptor( dataInspector d )
  {
    di = d; super.setLayout( new GridLayout(1,1) ); td = new JTable();
    
    td.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    super.add( new JScrollPane( td ) );
  }

  //Set the data model.

  public void setDescriptor( Descriptor d )
  {
    data = d; data.loc( 0 ); di.setOther( data.length );
    
    if( !set ) { set = true; td.setModel( dModel ); }
    
    dModel.fireTableDataChanged();
  }
}