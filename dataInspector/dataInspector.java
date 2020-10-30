package dataInspector;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;

import RandomAccessFileV.*;
import WindowCompoents.*;

public class dataInspector extends JComponent implements IOEventListener, ActionListener
{
  private static RandomAccessFileV d;

  //buffer.

  private static byte[] b8 = new byte[8];

  //integers.

  private static byte b = 0;
  private static short s = 0;
  private static int i = 0;
  private static long l = 0;

  //Position.
  
  private static long t;

  //length of each data type.

  private static final int[] len = new int[]{1,1,1,2,2,4,4,8,8,4,8,1,2,-1};

  //Remember the users selected type.

  private int type = len.length - 1;

  //Data type names.

  private static final String[] dtype = new String[]
  {
    "Binary (8 bit)",
    "Int8",
    "UInt8",
    "Int16",
    "UInt16",
    "Int32",
    "UInt32",
    "Int64",
    "UInt64",
    "Float32",
    "Float64",
    "Char8",
    "Char16",
    "Use No Data type"
  };

  private static String[] ddata = new String[14];

  //Fields for data entry

  private static JTable td;

  //The main hex editor display.

  AbstractTableModel dataModel = new AbstractTableModel()
  {
    public int getColumnCount() { return( 2 ); }

    public int getRowCount() { return( len.length ); }

    public String getColumnName( int col ) { return ( col == 0 ? "Type" : "Value" ); }
    
    public Object getValueAt(int row, int col)
    {
      if( col == 0 ) { return( dtype[ row ] ); }

      return( ddata[ row ] );
    }

    public boolean isCellEditable( int row, int col )
    {
      type = row;

      try { t = d.getFilePointer(); } catch(Exception e) {}

      if( len[type] > 0 ) { WindowCompoents.Offset.setSelectedEl( t, len[type] - 1 ); }
      else
      {
        WindowCompoents.Offset.setSelectMode( true );
      }

      return ( false );
    }

    public void setValueAt( Object value, int row, int col ) { }
  };

  //String formatter.

  private int r = 10;

  //Byte order.

  private boolean littleEndian = true;

  //Create controls.

  public dataInspector( RandomAccessFileV data )
  {
    td = new JTable( dataModel );

    d = data; d.addIOEventListener( this );

    super.setLayout( new GridBagLayout() );

    //Byte order panel.

    JRadioButton Little = new JRadioButton("Little Endian"); Little.setActionCommand("L"); Little.setSelected(true);
    JRadioButton Big = new JRadioButton("Big Endian"); Big.setActionCommand("B"); Big.setSelected(false);

    ButtonGroup endian = new ButtonGroup(); endian.add(Little); endian.add(Big);

    JPanel p1 = new JPanel( new GridLayout( 1, 2 ) ); p1.add(Little); p1.add(Big);

    javax.swing.border.TitledBorder byteOrder = BorderFactory.createTitledBorder( BorderFactory.createLineBorder( Color.black, 1 ), "Byte Order");
    byteOrder.setTitleJustification( byteOrder.LEFT ); p1.setBorder( byteOrder );

    Big.addActionListener(this); Little.addActionListener(this);

    //Base conversion panel.
    
    JRadioButton Bin = new JRadioButton("Native Binary"); Bin.setActionCommand("b"); Bin.setSelected(false);
    JRadioButton Oct = new JRadioButton("Octal"); Oct.setActionCommand("o"); Oct.setSelected(false);
    JRadioButton Dec = new JRadioButton("Decimal"); Dec.setActionCommand("d"); Dec.setSelected(true);
    JRadioButton Hex = new JRadioButton("Hexadecimal"); Hex.setActionCommand("h"); Hex.setSelected(false);

    ButtonGroup Base = new ButtonGroup(); Base.add(Bin); Base.add(Oct); Base.add(Dec); Base.add(Hex);

    JPanel p2 = new JPanel( new GridLayout( 1, 4 ) ); p2.add(Bin); p2.add(Oct); p2.add(Dec); p2.add(Hex);

    javax.swing.border.TitledBorder base = BorderFactory.createTitledBorder( BorderFactory.createLineBorder( Color.black, 1 ), "Integer Base");
    base.setTitleJustification( base.LEFT ); p2.setBorder( base );

    Bin.addActionListener(this); Oct.addActionListener(this); Dec.addActionListener(this); Hex.addActionListener(this);

    //Component layout.

    GridBagConstraints c = new GridBagConstraints(); c.anchor = GridBagConstraints.PAGE_START;

    c.gridy = 0; super.add( new JScrollPane( td ), c );
    
    c.gridy = 1; c.fill = GridBagConstraints.HORIZONTAL; super.add( p1, c );

    c.gridy = 2; c.weighty = 1; super.add( p2, c );

    try { d.seek(d.getFilePointer()); } catch( java.io.IOException e ) { }
  }

  //Set new target.

  public void setTarget( RandomAccessFileV data )
  {
    d.removeIOEventListener( this );
    d = data;
    d.addIOEventListener( this );
    try { d.seek(d.getFilePointer()); } catch( java.io.IOException e ) { }
  }

  //Updating the data inspector.

  public void update()
  {
    if (td.isEditing()) { td.getCellEditor().cancelCellEditing(); }
    
    try { t = d.getFilePointer(); if( len[type] > 0 && WindowCompoents.Offset.isEditing() ) { d.seek( WindowCompoents.Offset.selectEl() ); } d.read(8); d.seek( t ); } catch( java.io.IOException er ) { }

    b = d.toByte(); if ( littleEndian ) {  s = d.toLShort(); i = d.toLInt(); l = d.toLLong(); } else { s = d.toShort(); i = d.toInt(); l = d.toLong(); }

    //Update table cells.

    ddata[0] = format( b, 2, 0xFF, false );

    ddata[1] = format( b, r, 0xFF, true ); ddata[2] = format( b, r, 0xFF, false );

    ddata[3] = format( s, r, 0xFFFF, true ); ddata[4] = format( s, r, 0xFFFF, false );

    ddata[5] = format( i, r, 0xFFFFFFFFL, true ); ddata[6] = format( i, r, 0xFFFFFFFFL, false );

    ddata[7] = format( l, r, 0xFFFFFFFFFFFFFFFFL, true ); ddata[8] = format( l, r, 0xFFFFFFFFFFFFFFFFL, false );

    ddata[9] = Float.intBitsToFloat(i) + ""; ddata[10] = Double.longBitsToDouble(l) + "";

    ddata[11] = ( (char)b ) + ""; ddata[12] = ( (char)s ) + "";

    dataModel.fireTableDataChanged();

    td.setRowSelectionInterval(type, type);
    
    if( len[type] > 0 && !WindowCompoents.Offset.isEditing() ) { WindowCompoents.Offset.setSelectedEl( t, len[type] - 1 ); }
  }

  //Event handling.

  public void onSeek( IOEvent e )
  {
    d.Events = false;

    update();

    d.Events = true;
  }
  
  public void onRead( IOEvent e )
  {
  }

  public void onWrite( IOEvent e )
  {
    if( len[type] > 0 )
    {
      d.Events = false;

      update();

      d.Events = true;
    }
  }

  //String.format is not flexible enough.

  private String format( long val, int base, long size, boolean sing )
  {
    String o = "";

    if( sing )
    {
      o = Long.toString(val, base).toUpperCase();
      sing = o.indexOf("-") >= 0; if( sing ) { o = o.substring( 1, o.length() ); }
      size = (long)( Math.log( toFloat( size ) ) / Math.log(base) + 0.5 );
    }
    else
    {
      o = Long.toUnsignedString(val & size, base).toUpperCase(); size = (long)( Math.log( toFloat( size ) ) / Math.log(base) + 0.5 );
    }

    for( int i = o.length(); i < size; o = "0" + o, i++ ); return( sing ? "-" + o : o );
  }

  private float toFloat(long v) { if( v < 0 ) { return( ( (float)(v & 0x7FFFFFFFFFFFFFFFL) ) + 9223372036854775808f ); } return( (float)v ); }
  
  public void actionPerformed(ActionEvent e)
  {
    String ac = e.getActionCommand();

    if( ac == "b" ) { r = 2; } else if( ac == "o" ) { r = 8; } else if( ac == "d" ) { r = 10; } else if( ac == "h" ) { r = 16; }

    if( ac == "L" ) { littleEndian = true; } else if( ac == "B" ) { littleEndian = false; }

    try{ d.seek( d.getFilePointer() ); } catch( java.io.IOException er ) {  }
  }
}