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

  private long EOI = 0;
  private int skip = 0;

  //Picture dimensions.

  private int width = 0, height = 0;
  
  public JPEG() throws java.io.IOException
  {
    //Setup.

    tree.setEventListener( this ); file.Events = false; EOI = file.length();

    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true); tree.setShowsRootHandles(true); root = new JDNode( fc.getFileName(), -1 );
    
    //Set -1 incase invalid JPEG with no start of image marker.

    JDNode h = new JDNode("JPEG Data", -1);

    //Read the jpeg markers. All markers start with a 0xFF = -1 code.

    int MCode = 0, size = 0, type = 0;

    //Read image data in 1k buffer.

    long pos = -1; int buf = 1024; byte[] b = new byte[buf];

    //Read all the markers and define the data of known marker types.

    while( pos < EOI )
    {
      if( buf > 1022 ) { pos += 1; buf = 0; file.read(b); }

      MCode = b[buf]; type = b[buf + 1] & 0xFF;

      //These are not markers.

      if( MCode == -1 && (type > 0x01 && type != 0xFF) )
      {
        if( skip > 0 ) { h.add( new JDNode("Image Data.h", new long[]{ -2, pos - skip, pos - 3 } ) ); skip = 0; }

        file.seek( pos ); //Seek the actual position.

        markerData = new Descriptor(file); des.add(markerData);
  
        markerData.UINT8("Maker Code"); markerData.UINT8("Marker type");

        //Markers between 0xD0 to 0xD9 have no size.

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

          else if( type == 0xD9 ) { h.add( new JDNode("End of Image.h", ref++) ); }
        }

        //Decode maker data types.

        else
        {
          markerData.UINT16("Maker size"); size = ( ((short)markerData.value) & 0xFFFF ) - 2;

          //Decode the marker if it is a known type.

          if( !decodeMarker( type, size, h ) ) { markerData.Other("Maker Data", size); }
        }

        pos = file.getVirtualPointer(); buf = 0; file.read(b);
      }

      //Start of scan data.

      else { skip += 1; buf += 1; pos += 1; }
    }

    //Setup headers.
    
    ((DefaultTreeModel)tree.getModel()).setRoot(root); file.Events = true;

    //Set the first node.

    tree.setSelectionPath( new TreePath( h.getPath() ) ); open( new JDEvent( this, "", 0 ) );
  }

  //All of this moves into sub functions when user clicks on a maker.

  private boolean decodeMarker( int type, int size, JDNode marker ) throws java.io.IOException
  {
    if( ( type & 0xF0 ) == 0xC0 && !( type == 0xC4 || type == 0xC8 || type == 0xCC ) )
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

      else if( type == 0xCD ) { n = new JDNode("Start Of Frame (Differential sequential DCT)", ref++); }
      else if( type == 0xCE ) { n = new JDNode("Start Of Frame (Differential progressive DCT)", ref++); }
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
      markerData.UINT8("Class/Table Number");

      int classType = (((byte)markerData.value) & 0xF0) >> 4;

      JDNode n = new JDNode("Huffman Table #" + (((byte)markerData.value) & 0x0F) + " (Class = " + classType + ")", ref++); marker.add( n );

      //Begin reading Huffman Tables.

      int Sum = 0;

      while( size > 0 )
      {
        Descriptor Huff = new Descriptor(file); des.add(Huff);

        JDNode HRow = new JDNode("Huffman codes.h", ref++); n.add( HRow );

        for( int i = 1; i <= 16; i++ ) { Huff.UINT8("EL #" + i + ""); Sum += ((byte)Huff.value) & 0xFF; }

        Huff = new Descriptor(file); des.add(Huff);

        HRow = new JDNode("Data.h", ref++); n.add( HRow );

        Huff.Other("Huffman Data", Sum);

        //The tables can be grouped together under one marker.

        size -= 17 + Sum; Sum = 0; if( size > 0 )
        {
          Descriptor nTable = new Descriptor(file); des.add(nTable); nTable.UINT8("Class/Table Number");

          classType = (((byte)nTable.value) & 0xF0) >> 4;

          n = new JDNode("Huffman Table #" + (((byte)nTable.value) & 0x0F) + " (Class = " + classType + ")", ref++); marker.add( n );
        }
      }

      return( true );
    }
    else if( type == 0xDA )
    {
      JDNode n = new JDNode("Start Of Scan", ref++); marker.add( n );

      markerData.UINT8("Number of Components"); int Ns = ((byte)markerData.value) & 0xFF;

      for( int c = 1; c <= Ns; c++ )
      {
        Descriptor Scan = new Descriptor(file); des.add(Scan);

        JDNode comp = new JDNode("Component #" + c + ".h", ref++); n.add( comp );

        Scan.UINT8("Scan component");
        Scan.UINT8("Entropy Table");
      }

      Descriptor info = new Descriptor(file); des.add(info);

      JDNode end = new JDNode("Scan info.h", ref++); n.add( end );

      info.UINT8("Start of Spectral");
      info.UINT8("End of Spectral");
      info.UINT8("ah/al");

      return( true );
    }
    else if( type == 0xDB )
    {
      markerData.UINT8("Precision/Table Number");

      int Precision = (((byte)markerData.value) & 0xF0) >> 4;

      JDNode n = new JDNode("Quantization Table #" + (((byte)markerData.value) & 0x0F) + " (" + ( Precision == 0 ? "8 Bit" : "16 bit" ) + ")", ref++); marker.add( n );

      //Begin reading Quantization Tables.

      int eSize = Precision == 0 ? 65 : 129;

      while( size >= eSize )
      {
        for( int i = 1; i <= 8; i++ )
        {
          Descriptor QMat = new Descriptor(file); des.add(QMat);

          JDNode matRow = new JDNode("Row #" + i + ".h", ref++); n.add( matRow );

          if( Precision == 0 )
          {
            for( int i2 = 1; i2 <= 8; i2++ ) { QMat.UINT8("EL #" + i2 + ""); }
          }
          else
          {
            for( int i2 = 1; i2 <= 8; i2++ ) { QMat.UINT16("EL #" + i2 + ""); }
          }
        }

        //The tables can be grouped together under one marker.

        size -= eSize; if( size >= eSize )
        {
          Descriptor nTable = new Descriptor(file); des.add(nTable); nTable.UINT8("Precision/Table Number");

          Precision = (((byte)nTable.value) & 0xF0) >> 4; eSize = Precision == 0 ? 65 : 129;

          n = new JDNode("Quantization Table #" + (((byte)nTable.value) & 0x0F) + " (" + ( Precision == 0 ? "8 Bit" : "16 bit" ) + ")", ref++); marker.add( n );
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

    //Highlight data.

    else if( e.getArg(0) == -2 )
    {
      try { file.seek( e.getArg(1) ); } catch( Exception er ) { }
      
      Offset.setSelected( e.getArg(1), e.getArg(2) );
    }
  }
}
