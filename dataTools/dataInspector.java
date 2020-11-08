package dataTools;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.util.*;

import RandomAccessFileV.*;
import VHex.*;
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

  //String.

  private static String s1, s2;

  //String length.

  private static int sLen = 0;

  //Position.
  
  private static long t;

  //length of each data type.

  private int[] len = new int[]{1,1,1,2,2,4,4,8,8,4,8,1,2,0,0,-1};

  //Weather data is virtual, or offset.

  private static boolean mode = false;
  private static boolean emode = false;

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
    "String8",
    "String16",
    "Use No Data type"
  };

  private static String[] ddata = new String[16];

  //Fields for data entry

  private static JTable td;

  //Linked editors if any.

  protected java.util.List<VHex> h = new ArrayList<VHex>();

  private VHex e;

  private boolean v;

  public void addEditor( VHex editor ) { h.add( editor ); }

  //The main hex editor display.

  private AbstractTableModel dataModel = new AbstractTableModel()
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
      type = row; System.out.println("TEST");

      try { t = mode ? d.getVirtualPointer() : d.getFilePointer(); } catch(Exception e) {}

      if( len[type] > 0 )
      {
        for( int i = 0; i < h.size(); i++ )
        {
          if( h.get(i).isVirtual() == mode )
          {
            h.get(i).setSelectedEl( t, len[type] - 1 );
          }
        }

        for( int i = 0; i < h.size(); i++ )
        {
          if( h.get(i).isVirtual() != mode )
          {
            try { h.get(i).setSelectedEl( mode ? d.getFilePointer() : d.getVirtualPointer(), len[type] - 1 ); } catch( Exception e ) { }
          }
        }
      }
      else
      {
        for( int i = 0; i < h.size(); i++ ) { h.get(i).setSelectMode( true ); }
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

  //Disable events when component is not visible.

  @Override public void setVisible( boolean v )
  {
    if( v ) { d.addIOEventListener( this ); } else { d.removeIOEventListener(this); }
    super.setVisible( v );
  }

  //Set new target.

  public void setTarget( RandomAccessFileV data )
  {
    d.removeIOEventListener( this );
    d = data;
    d.addIOEventListener( this );
    try { d.seek(d.getFilePointer()); } catch( java.io.IOException e ) { }
  }

  public void setType( int row ) { type = row; update(); }

  public void setOther( int l )
  {
    type = len.length - 1; td.setRowSelectionInterval(type, type);

    long p = 0;

    try { p = mode ? d.getVirtualPointer() : d.getFilePointer(); } catch( java.io.IOException er ) { }

    for( int i = 0; i < h.size(); i++ )
    {
      e = h.get(i); if( mode == e.isVirtual() ) { e.setSelected( p, ( p + l ) - 1 ); }

      e.setSelectMode( true );
    }
  }

  //Updating the data inspector.

  public void update()
  {
    d.Events = false;

    if (td.isEditing()) { td.getCellEditor().cancelCellEditing(); }

    try
    {
      t = mode ? d.getVirtualPointer() : d.getFilePointer();

      checkEdit(); if( emode ) { setBaseEl(); }

      if( mode ) { d.readV( sLen <= 8 ? 8 : sLen ); d.seekV( t ); } else { d.read( sLen <= 8 ? 8 : sLen ); d.seek( t ); }
    }
    catch( java.io.IOException er ) { er.printStackTrace(); }

    //Convert read bytes to different types.

    b = d.toByte(); s1 = d.toText8(0,sLen);
    
    if ( littleEndian ) { s = d.toLShort(); i = d.toLInt(); l = d.toLLong(); s2 = d.toLText16(0,sLen); } else { s = d.toShort(); i = d.toInt(); l = d.toLong(); s2 = d.toText16(0,sLen); }

    //Update table cells.

    ddata[0] = format( b, 2, 0xFF, false );

    ddata[1] = format( b, r, 0xFF, true ); ddata[2] = format( b, r, 0xFF, false );

    ddata[3] = format( s, r, 0xFFFF, true ); ddata[4] = format( s, r, 0xFFFF, false );

    ddata[5] = format( i, r, 0xFFFFFFFFL, true ); ddata[6] = format( i, r, 0xFFFFFFFFL, false );

    ddata[7] = format( l, r, 0xFFFFFFFFFFFFFFFFL, true ); ddata[8] = format( l, r, 0xFFFFFFFFFFFFFFFFL, false );

    ddata[9] = Float.intBitsToFloat(i) + ""; ddata[10] = Double.longBitsToDouble(l) + "";

    ddata[11] = ( (char)b ) + ""; ddata[12] = ( (char)s ) + "";

    ddata[13] = s1; ddata[14] = s2;

    dataModel.fireTableDataChanged(); td.setRowSelectionInterval(type, type);

    //Update hex editors based on selected type.

    if( !emode ) { setEl(); }

    d.Events = true;
  }

  //Check if any editors are in edit mode.

  private void checkEdit() { emode = false; for( int i = 0; i < h.size(); i++ ) { if( h.get(i).isEditing() ) { emode = true; } } }

  //Update editors to the base of selected element being edited.

  private void setBaseEl()
  {
    try
    {
      for( int i = h.size(); i > 0; i++ )
      {
        e = h.get(i); v = e.isVirtual();
      
        if( len[type] > 0 ) { if( v ) { d.seekV( e.selectEl() ); } else { d.seek( e.selectEl() ); } }
      }
    } catch( Exception e ) { }
  }

  //Set selected element.

  private void setEl()
  {
    for( int i = 0; i < h.size(); i++ )
    {
      e = h.get(i); v = e.isVirtual();

      //Only set selected element, for the editors that match the event mode.

      if( len[type] > 0 && !e.isEditing() )
      {
        if( v == mode ) { e.setSelectedEl( t, len[type] - 1 ); }
      }
    }

    for( int i = 0; i < h.size(); i++ )
    {
      e = h.get(i); v = e.isVirtual();

      //Else set the other hex editors based on offset potion.
          
      try
      {
        if( len[type] > 0 && v != mode ) { if( mode ) { e.setSelectedEl( d.getFilePointer(), len[type] - 1 ); } else { e.setSelectedEl( d.getVirtualPointer(), len[type] - 1 ); } }
      }
      catch( Exception er ) { }
    }
  }

  //Event handling.

  public void onSeek( IOEvent e )
  {
    d.Events = false;

    mode = e.isVirtual(); update();

    d.Events = true;
  }
  
  public void onRead( IOEvent e ) { }

  public void onWrite( IOEvent e )
  {
    if( len[type] > 0 )
    {
      d.Events = false;

      mode = e.isVirtual(); update();

      d.Events = true;
    }
  }

  //Set string length;

  public void setStringLen( int n ){ len[13] = n; len[14] = n; sLen = n; }

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