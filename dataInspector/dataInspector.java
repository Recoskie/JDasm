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

  private static final int[] len = new int[]{1,1,1,2,2,4,4,8,8,4,8,1,2};

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
    "Char16"
  };

  private static String[] ddata = new String[13];

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
      long p = 0;

      try { p = d.getFilePointer(); } catch(Exception e) {}

      WindowCompoents.Offset.setSelected( p, p + len[row] - 1 );

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

    JRadioButton Little = new JRadioButton("Little Indian"); Little.setActionCommand("L"); Little.setSelected(true);
    JRadioButton Big = new JRadioButton("Big Indian"); Big.setActionCommand("B"); Big.setSelected(false);

    ButtonGroup indian = new ButtonGroup(); indian.add(Little); indian.add(Big);

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

  //Event handling.

  public void onSeek( IOEvent e )
  {
    d.Events = false;

    if (td.isEditing()) { td.getCellEditor().cancelCellEditing(); }
    
    try { t = d.getFilePointer(); d.read(b8); d.seek( t ); } catch( java.io.IOException er ) { }

    if ( littleEndian )
    {
      //Put bytes into integers.
    
      b = b8[0];

      s = (short)( ( b8[0] & 0xFF ) | ( ( b8[1] << 8 ) & 0xFF00 ) );

      i = ( b8[0] & 0xFF ) | ( (b8[1] << 8) & 0xFF00 ) | ( (b8[2] << 16) & 0xFF0000 ) | ( (b8[3] << 24) & 0xFF000000 );

      l = ( (long)b8[0] & 0xFFL ) | ( ((long)b8[1] << 8) & 0xFF00L ) | ( ((long)b8[2] << 16) & 0xFF0000L ) | ( ((long)b8[3] << 24) & 0xFF000000L ) |
      ( ( (long)b8[4] << 32 ) & 0xFF00000000L ) | ( ( (long)b8[5] << 40 ) & 0xFF0000000000L ) | ( ( (long)b8[6] << 48 ) & 0xFF000000000000L ) | ( ( (long)b8[7] << 56 ) & 0xFF00000000000000L );
    }
    else
    {
      //Put bytes into integers.
    
      b = b8[0];

      s = (short)( ( b8[1] & 0xFF ) | ( ( b8[0] << 8 ) & 0xFF00 ) );

      i = ( b8[3] & 0xFF ) | ( (b8[2] << 8) & 0xFF00 ) | ( (b8[1] << 16) & 0xFF0000 ) | ( (b8[0] << 24) & 0xFF000000 );

      l = ( (long)b8[7] & 0xFFL ) | ( ((long)b8[6] << 8) & 0xFF00L ) | ( ((long)b8[5] << 16) & 0xFF0000L ) | ( ((long)b8[4] << 24) & 0xFF000000L ) |
      ( ( (long)b8[3] << 32 ) & 0xFF00000000L ) | ( ( (long)b8[2] << 40 ) & 0xFF0000000000L ) | ( ( (long)b8[1] << 48 ) & 0xFF000000000000L ) | ( ( (long)b8[0] << 56 ) & 0xFF00000000000000L );
    }

    //Update table cells.

    ddata[0] = format( b, 2, 0xFF, false );

    ddata[1] = format( b, r, 0xFF, true ); ddata[2] = format( b, r, 0xFF, false );

    ddata[3] = format( s, r, 0xFFFF, true ); ddata[4] = format( s, r, 0xFFFF, false );

    ddata[5] = format( i, r, 0xFFFFFFFFL, true ); ddata[6] = format( i, r, 0xFFFFFFFFL, false );

    ddata[7] = format( l, r, 0xFFFFFFFFFFFFFFFFL, true ); ddata[8] = format( l, r, 0xFFFFFFFFFFFFFFFFL, false );

    ddata[9] = Float.intBitsToFloat(i) + ""; ddata[10] = Double.longBitsToDouble(l) + "";

    ddata[11] = ( (char)b ) + ""; ddata[12] = ( (char)s ) + "";

    dataModel.fireTableDataChanged();

    d.Events = true;
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
  
  public void onRead( IOEvent e )
  {
  
  }

  public void onWrite( IOEvent e )
  {
  
  }
  
  public void actionPerformed(ActionEvent e)
  {
    String ac = e.getActionCommand();

    if( ac == "b" ) { r = 2; } else if( ac == "o" ) { r = 8; } else if( ac == "d" ) { r = 10; } else if( ac == "h" ) { r = 16; }

    if( ac == "L" ) { littleEndian = true; } else if( ac == "B" ) { littleEndian = false; }

    try{ d.seek( d.getFilePointer() ); } catch( java.io.IOException er ) {  }
  }
}