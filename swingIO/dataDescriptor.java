package swingIO;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class dataDescriptor extends JComponent
{
  //The current data descriptor.

  private static Descriptor data;

  //The table.

  private JTable td;

  //The data type inspector.

  private static dataInspector di;

  //Allows us to switch, and set data models.

  private boolean set = false, cset = false;

  //Cols.

  private String[] cols = new String[]{"Use", "Raw Data", "Value"};

  //Data type.

  private int type = 0;

  //The data descriptor model is for headers, and data.

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

      else if( type == 13 )
      {
        di.setStringLen( data.rpos.get(row + 1) - data.rpos.get(row) ); di.setType( type );
      }
      
      else if( type == 14 )
      {
        di.setStringLen( ( data.rpos.get(row + 1) - data.rpos.get(row) ) >> 1 ); di.setType( type );
      }

      else if( type == 15 ) { di.setOther( data.rpos.get(row + 1) - data.rpos.get(row) ); }

      else if( type == 16 ) { di.setOther( data.apos.get(row + 1) - data.rpos.get(row) ); }

      data.Event.accept( row );

      return ( false );
    }
  };

  //The core data model is for address mapping.

  private static core.Core core;

  private String[] coreCols = new String[] { "Operation", "Location" };

  private AbstractTableModel coreModel = new AbstractTableModel()
  {
    public int getColumnCount() { return( 2 ); }

    public int getRowCount() { return( core.locations.size() + ( core.data_off.size() >> 1 ) ); }

    public String getColumnName( int col ) { return ( coreCols[ col ] ); }
    
    public Object getValueAt( int row, int col )
    {
      if( row < core.locations.size() )
      {
        return( col == 0 ? "Disassemble" : "0x" + String.format( "%1$016X", core.locations.get( row ) ) );
      }
      else
      {
        row -= core.locations.size();
        return( col == 0 ? "Data Location" : "0x" + String.format( "%1$016X", core.data_off.get( row << 1 ) ) );
      }
    }

    public boolean isCellEditable( int row, int col )
    {
      if( row < core.locations.size() )
      {
        core.disLoc( row );
      }
      else
      {
        row -= core.locations.size(); row = row << 1;

        try { core.setLoc( core.data_off.get( row ) ); } catch( Exception e ) { }

        di.setOther( core.data_off.get( row + 1 ).intValue() );
      }
      
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
    
    if( !set ) { cset = false; set = true; td.setModel( dModel ); }
    
    dModel.fireTableDataChanged();

    data.Event.accept( -1 ); //Initial description of data structure.
  }

  //Set a core disassembly model.

  public void setDescriptor( core.Core d )
  {
    core = d;
    
    if( !cset ) { set = false; cset = true; td.setModel( coreModel ); }
    
    coreModel.fireTableDataChanged();
  }

  //Main use is for setting a blank data model.

  public void clear()
  {
    data = new Descriptor( null );
    
    if( !set ) { cset = false; set = true; td.setModel( dModel ); }

    dModel.fireTableDataChanged();
  }
}