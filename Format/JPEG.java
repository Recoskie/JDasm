package Format;

import swingIO.*;
import swingIO.tree.*;
import javax.swing.tree.*;

public class JPEG extends Window.Window implements JDEventListener
{
  private java.util.LinkedList<Descriptor> des = new java.util.LinkedList<Descriptor>();

  private int ref = 0;

  private JDNode root;

  private Descriptor markerData;

  //Picture dimensions.

  private int width = 0, height = 0;
  
  public JPEG() throws java.io.IOException
  {
    //Setup.

    tree.setEventListener( this ); file.Events = false;

    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true); tree.setShowsRootHandles(true); root = new JDNode( fc.getFileName(), -1 );
    
    //Set -1 incase invailed JPEG with no start of image marker.

    JDNode h = new JDNode("JPEG Data", -1);

    //Read the jpeg markers. All markers start with a 0xFF = -1 code.

    int nx = 0, size = 0, type = 0;

    //Set nx to the first byte which should be a marker code (start of image).

    long t = file.getFilePointer(); file.read(1); nx = file.toByte(); file.seek(t);

    //Read all the markers and define the data of known marker types.

    while( nx == -1 )
    {
      markerData = new Descriptor(file); des.add(markerData);
  
      markerData.UINT8("Maker Code");
      markerData.UINT8("Marker type"); type = ((byte)markerData.value) & 0xFF;

      //Markers betwean 0xD0 to 0xD9 have no size.

      if( type >= 0xD0 && type <= 0xD9 )
      {
        //Restart marker

        if( ( type & 0xF8 ) == 0xD0 )
        {
          h.add( new JDNode("Restart.h", ref++) );
        }

        //Set Start of image as read.

        else if( type == 0xD8 )
        {
          h = new JDNode("JPEG Data", ref++); root.add( h );
        }

        //End of image

        else if( type == 0xD9 )
        {

        }
      }

      //Decode maker data types.

      else
      {
        markerData.UINT16("Maker size"); size = (short)markerData.value - 2;

        //Decode the marker if it is a known type.

        if( !decodeMarker( type, size, h ) ) { markerData.Other("Maker Data", size); }
      }

      //Read the next byte to check if there is another marker.

      t = file.getFilePointer(); file.read(1); nx = file.toByte(); file.seek(t);
    }

    //Setup headers.
    
    ((DefaultTreeModel)tree.getModel()).setRoot(root); file.Events = true;

    //Set the first node.

    tree.setSelectionPath( new TreePath( h.getPath() ) ); open( new JDEvent( this, "", 0 ) );
  }

  private boolean decodeMarker( int type, int size, JDNode marker ) throws java.io.IOException
  {
    if( ( type & 0xFC ) == 0xC0 )
    {
      JDNode n;
      
      //Non-Deferential Huffman coded pictures.

      if( type == 0xC0 ) { n = new JDNode("Start Of Frame (baseline DCT)", ref++); }      
      else if( type == 0xC1 ) { n = new JDNode("Start Of Frame (Extended Sequential DCT)", ref++); }
      else if( type == 0xC2 ) { n = new JDNode("Start Of Frame (progressive DCT)", ref++); }
      else if( type == 0xC3 ) { n = new JDNode("Start Of Frame (Lossless Sequential)", ref++); }

      //Huffman Deferential codded pictures.
      
      else if( type == 0xC5 ) { n = new JDNode("Start Of Frame (Differential sequential DCT)", ref++); }
      else if( type == 0xC6 ) { n = new JDNode("Start Of Frame (Differential progressive DCT)", ref++); }
      else if( type == 0xC7 ) { n = new JDNode("Start Of Frame (Differential Lossless)", ref++); }

      //Non-Deferential Arithmetic codded pictures.

      else if( type == 0xC9 ) { n = new JDNode("Start Of Frame (Extended Sequential DCT)", ref++); }
      else if( type == 0xCA ) { n = new JDNode("Start Of Frame (Progressive DCT)", ref++); }
      else if( type == 0xCB ) { n = new JDNode("Start Of Frame (Lossless Sequential)", ref++); }

      //Deferential Arithmetic codded pictures.

      else if( type == 0xCC ) { n = new JDNode("Start Of Frame (Differential sequential DCT)", ref++); }
      else if( type == 0xCD ) { n = new JDNode("Start Of Frame (Differential progressive DCT)", ref++); }
      else { n = new JDNode("Start Of Frame (Differential Lossless)", ref++); }
      
      marker.add( n );

      Descriptor image = new Descriptor(file); des.add(image);
      n.add( new JDNode("Image Information.h", ref++) );

      image.UINT8("Sample Precision");
      image.UINT16("Picture Height"); width = ((short)image.value) & 0xFFFF;
      image.UINT16("Picture Width"); height = ((short)image.value) & 0xFFFF;

      image.UINT8("Number of Components in Picture"); int Nf = ((byte)image.value) & 0xFF;

      for( int i = 1; i <= Nf; i++ )
      {
        Descriptor imageComp = new Descriptor(file); des.add(imageComp);
        n.add( new JDNode("Image Component" + i + ".h", ref++) );
        
        imageComp.UINT8("Component Indemnifier");
        imageComp.UINT8("Vertical/Horizontal Sampling factor");
        imageComp.UINT8("Quantization table Number");
      }

      return(true);
    }
    else if( type == 0xC4 )
    {
      markerData.UINT8("Table Number");

      JDNode n = new JDNode("Huffman Table #" + (((byte)markerData.value) & 0xFF) + "", ref++); marker.add( n );

      //Begin reading Huffman Tables.

      int Sum = 0;

      while( size > 0 )
      {
        for( int i = 1; i <= 2; i++ )
        {
          Descriptor Huff = new Descriptor(file); des.add(Huff);

          JDNode HRow = new JDNode("Row #" + i + ".h", ref++); n.add( HRow );

          for( int i2 = 1; i2 <= 8; i2++ ) { Huff.UINT8("EL #" + i2 + ""); Sum += ((byte)Huff.value) & 0xFF; }
        }

        Descriptor Huff = new Descriptor(file); des.add(Huff);

        JDNode HRow = new JDNode("Data.h", ref++); n.add( HRow );

        Huff.Other("Huffman Data", Sum);

        //The tables can be grouped toghter under one marker.

        size -= 17 + Sum; Sum = 0; if( size > 0 )
        {
          Descriptor nTable = new Descriptor(file); des.add(nTable); nTable.UINT8("Table Number");

          n = new JDNode("Huffman Table #" + (((byte)nTable.value) & 0xFF) + "", ref++); marker.add( n );
        }
      }

      return( true );
    }
    else if( type == 0xDA )
    {
      marker.add( new JDNode("Start Of Scan.h", ref++) );
    }
    else if( type == 0xDB )
    {
      markerData.UINT8("Table Number");

      JDNode n = new JDNode("Quantization Table #" + (((byte)markerData.value) & 0xFF) + "", ref++); marker.add( n );

      //Begin reading Quantization Tables.

      while( size > 64 )
      {
        for( int i = 1; i <= 8; i++ )
        {
          Descriptor QMat = new Descriptor(file); des.add(QMat);

          JDNode matRow = new JDNode("Row #" + i + ".h", ref++); n.add( matRow );

          for( int i2 = 1; i2 <= 8; i2++ ) { QMat.UINT8("EL #" + i2 + ""); }
        }

        //The tables can be grouped toghter under one marker.

        size -= 65; if( size > 64 )
        {
          Descriptor nTable = new Descriptor(file); des.add(nTable); nTable.UINT8("Table Number");

          n = new JDNode("Quantization Table #" + (((byte)nTable.value) & 0xFF) + "", ref++); marker.add( n );
        }
      }

      return( true );
    }
    else if( ( type & 0xF0 ) == 0xE0 )
    {
      JDNode n = new JDNode("Application (info)", ref++);

      marker.add( n );

      Descriptor m = new Descriptor(file); des.add(m);

      m.String8("Type", (byte)0x00); String Type = (String)m.value;

      if( Type.equals("JFIF") )
      {
        m.UINT8("Major version");
        m.UINT8("Minor version");
        m.UINT8("Density");
        m.UINT16("Horizontal pixel Density");
        m.UINT16("Vertical pixel Density");
        m.UINT8("Horizontal pixel count");
        m.UINT8("Vertical pixel count");

        if( size - 14 > 0 )
        {
          m.Other("Other Data", size - 14 );
        }
      }
      else { m.Other("Marker Data", size - Type.length() - 1 ); }

      n.add( new JDNode(Type + ".h", ref++) );

      return(true);
    }
    else if( type == 0xFE )
    {
      JDNode n = new JDNode("Comment", ref++);

      marker.add( n );

      Descriptor m = new Descriptor(file); des.add(m);

      m.String8("Comment Text", size);

      n.add( new JDNode("Text.h", ref++) );

      return( true );
    }
    else
    {
      marker.add( new JDNode("Marker.h", ref++) );
    }

    return( false );
  }

  public void Uninitialize() { des.clear(); ref = 0; }

  public void open( JDEvent e )
  {
    if( e.getID().equals("UInit") ) { Uninitialize(); }

    else if( e.getArg(0) >= 0 )
    {
      tree.expandPath( tree.getLeadSelectionPath() );

      ds.setDescriptor(des.get((int)e.getArg(0)));
    }
    else
    {

    }
  }
}
