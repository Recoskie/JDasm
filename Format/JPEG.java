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

  private static final String pad = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

  //Is scanned is used to check if the huffman markers and scan markers are read before reding image data.

  private boolean scanFrame = false;
  private boolean isScanned = false;

  //Some markers are semi read to setup memory for decoding the picture. Then it is set false.
  //This boolean value identifies if a marker needs to be skipped when parsing.

  private boolean skipM = true;

  //Image data format. This is determined by the start of frame marker number.

  private int imageType = -1;

  //Picture dimensions. This is determined by the start of frame marker.
  
  private int width = 0, height = 0;

  //Sub sampling is number of 8 by 8 matrixes that are used for each color component.
  //This allows us to forum bigger matrixes like 16x16 using four 8 by 8.
  //Table is which quantization table number and huffman table number that is being used for a color.

  private int[] subSampling, table;
  
  //Start of scan marker locations and set colors.

  private int[][] scanC;
  private int[] Spectral;

  //Initialized after parsing the JPEG markers.

  private static int[][] HuffmanCodes;

  //Initialized after parsing the JPEG markers.

  private static int[][] QMat;

  //A simple map for Y in zigzag matrix when showing the DCT matrix.
  //We can easily compute Y, and X fast, but it is a bit faster using a matrix.

  private static final int[] zigzag = new int[]
  {
    0, //Mid point +2.
    0,1,2,1,0, //mid point +2.
    0,1,2,3,4,3,2,1,0, //Mid point +2.
    0,1,2,3,4,5,6,5,4,3,2,1,0, //Mid point +2.
    0,1,2,3,4,5,6,7, //Mid point transition.
    7,6,5,4,3,2,1,2,3,4,5,6,7, //Mid point 1.
    7,6,5,4,3,4,5,6,7, //Mid point +2.
    7,6,5,6,7, //Mid point +2.
    7 //Mid point +2.
  };

  //Decoding of huffman table expansion.

  private static java.util.LinkedList<String> huffExpansion = new java.util.LinkedList<String>();
  private static int HuffTable = 0, HuffTables = 0;
      
  private static final int[] bits = new int[]
  {
    0x80000000,0xC0000000,0xE0000000,0xF0000000,0xF8000000,0xFC000000,0xFE000000,0xFF000000,
    0xFF800000,0xFFC00000,0xFFE00000,0xFFF00000,0xFFF80000,0xFFFC0000,0xFFFE0000,0xFFFF0000
  };

  //Stores the end of the image. Speeds up reading the markers.

  private long EOI = 0;

  //We store the tables position, and type/number.
  //This lets us know which tables to use as we use the most recently defined tables going backwards from our current offset in image data.

  private class markerInfo
  {
    int type = 0; long Offset = 0;

    public markerInfo( int t, long o ) { type = t; Offset = o; }
  };

  private static java.util.LinkedList<markerInfo> H = new java.util.LinkedList<markerInfo>();
  private static java.util.LinkedList<markerInfo> Q = new java.util.LinkedList<markerInfo>();
  private static java.util.LinkedList<markerInfo> S = new java.util.LinkedList<markerInfo>();

  //We parse the JPEG markers, but do not really read them on first load.
  
  public JPEG() throws java.io.IOException
  {
    //The first 4 default huffman tables.

    H.add( new markerInfo( 0, 0 ) );
    H.add( new markerInfo( 16, 0 ) );
    H.add( new markerInfo( 1, 0 ) );
    H.add( new markerInfo( 17, 0 ) );

    //The first 2 default quantization matrixes.

    Q.add( new markerInfo( 0, 0 ) );
    Q.add( new markerInfo( 1, 0 ) );

    //Setup.

    tree.setEventListener( this ); file.Events = false; EOI = file.length();

    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true); tree.setShowsRootHandles(true); root = new JDNode( fc.getFileName(), -1 );
    
    //Set -1 incase invalid JPEG with no start of image marker.

    JDNode h = new JDNode("JPEG Data", -1);

    //Read the jpeg markers. All markers start with a 0xFF = -1 code.

    int MCode = 0, size = 0, type = 0;

    //Parse image data in 4k buffer.

    long pos = 0, markerPos = 0; int buf = 0; byte[] b = new byte[4096]; file.read(b);

    //Read all the markers and define the data of known marker types.

    while( ( pos + buf ) < EOI )
    {
      if( buf > 4094 )
      {
        pos = buf + pos; buf = 0;
        
        if( buf != 4096 ) { file.seek(pos); }
        
        file.read(b);
      }

      MCode = b[buf]; type = b[buf + 1] & 0xFF;

      //Check if it is a valid maker.

      if( MCode == -1 && (type > 0x01 && type != 0xFF) )
      {
        if( pos + buf != markerPos )
        {
          JDNode t = new JDNode("Image Data", new long[]{ -3, markerPos, ( pos + buf ) - 1 } ); t.add( new JDNode( pad ) ); h.add( t );
        }

        markerPos = pos + buf; file.seek( markerPos ); //Seek the actual position.

        markerData = new Descriptor(file); des.add(markerData); markerData.setEvent( this::MInfo );
  
        markerData.UINT8("Maker Code"); markerData.UINT8("Marker type");

        //Markers between 0xD0 to 0xD9 have no size.

        if( type >= 0xD0 && type <= 0xD9 )
        {
          //Restart marker

          if( ( type & 0xF8 ) == 0xD0 )
          {
            h.add( new JDNode("Restart #" + ( type & 0x0F ) + ".h", ref++) );
          }

          //Set Start of image as read.

          else if( type == 0xD8 )
          {
            h = new JDNode("JPEG Data", ref++); root.add( h );
          }

          //End of image

          else if( type == 0xD9 ) { h.add( new JDNode("End of Image.h", ref++) ); break; }

          markerPos += 2; buf += 2;
        }

        //Decode maker data types.

        else
        {
          markerData.UINT16("Maker size"); size = ( ((short)markerData.value) & 0xFFFF ) - 2;

          //Decode the marker if it is a known type.

          if( !decodeMarker( type, size, h ) ) { markerData.Other("Maker Data", size); }

          markerPos += size + 4; buf += size + 4;

          if( skipM ) { file.skipBytes(size); } else { skipM = true; }
        }
      } else { buf += type != 0xFF ? 2 : 1; }
    }

    //We setup the memory for our huffman tables, and quantization markers as needed.

    scanC = new int[S.size()][]; Spectral = new int[S.size() << 1];

    //We setup the defaults that are used if there is no assigned huffman tables, or quantization matrixes.

    HuffmanCodes = new int[H.size()][];
    HuffmanCodes[0] = new int[]
    {
      0x00000001,0x40000012,0x60000022,0x80000032,0xA0000042,0xC0000052,0xE0000063,0xF0000074,0xF8000085,0xFC000096,0xFE0000A7,0xFF0000B8
    };
    HuffmanCodes[1] = new int[]
    {
      0x00000011,0x40000021,0x80000032,0xA0000003,0xB0000043,0xC0000113,0xD0000054,0xD8000124,0xE0000214,0xE8000315,0xEC000415,0xF0000066,0xF2000136,0xF4000516,0xF6000616,0xF8000077,0xF9000227,0xFA000717,0xFB000148,0xFB800328,0xFC000818,0xFC800918,0xFD000A18,0xFD800089,0xFDC00239,0xFE000429,0xFE400B19,0xFE800C19,0xFEC0015A,0xFEE0052A,0xFF000D1A,0xFF200F0A,0xFF40024B,0xFF50033B,0xFF60062B,0xFF70072B,0xFF80082E,0xFF82009F,0xFF8300AF,0xFF84016F,0xFF85017F,0xFF86018F,0xFF87019F,0xFF8801AF,0xFF89025F,0xFF8A026F,0xFF8B027F,0xFF8C028F,0xFF8D029F,0xFF8E02AF,0xFF8F034F,0xFF90035F,0xFF91036F,0xFF92037F,0xFF93038F,0xFF94039F,0xFF9503AF,0xFF96043F,0xFF97044F,0xFF98045F,0xFF99046F,0xFF9A047F,0xFF9B048F,0xFF9C049F,0xFF9D04AF,0xFF9E053F,0xFF9F054F,0xFFA0055F,0xFFA1056F,0xFFA2057F,0xFFA3058F,0xFFA4059F,0xFFA505AF,0xFFA6063F,0xFFA7064F,0xFFA8065F,0xFFA9066F,0xFFAA067F,0xFFAB068F,0xFFAC069F,0xFFAD06AF,0xFFAE073F,0xFFAF074F,0xFFB0075F,0xFFB1076F,0xFFB2077F,0xFFB3078F,0xFFB4079F,0xFFB507AF,0xFFB6083F,0xFFB7084F,0xFFB8085F,0xFFB9086F,0xFFBA087F,0xFFBB088F,0xFFBC089F,0xFFBD08AF,0xFFBE092F,0xFFBF093F,0xFFC0094F,0xFFC1095F,0xFFC2096F,0xFFC3097F,0xFFC4098F,0xFFC5099F,0xFFC609AF,0xFFC70A2F,0xFFC80A3F,0xFFC90A4F,0xFFCA0A5F,0xFFCB0A6F,0xFFCC0A7F,0xFFCD0A8F,0xFFCE0A9F,0xFFCF0AAF,0xFFD00B2F,0xFFD10B3F,0xFFD20B4F,0xFFD30B5F,0xFFD40B6F,0xFFD50B7F,0xFFD60B8F,0xFFD70B9F,0xFFD80BAF,0xFFD90C2F,0xFFDA0C3F,0xFFDB0C4F,0xFFDC0C5F,0xFFDD0C6F,0xFFDE0C7F,0xFFDF0C8F,0xFFE00C9F,0xFFE10CAF,0xFFE20D2F,0xFFE30D3F,0xFFE40D4F,0xFFE50D5F,0xFFE60D6F,0xFFE70D7F,0xFFE80D8F,0xFFE90D9F,0xFFEA0DAF,0xFFEB0E1F,0xFFEC0E2F,0xFFED0E3F,0xFFEE0E4F,0xFFEF0E5F,0xFFF00E6F,0xFFF10E7F,0xFFF20E8F,0xFFF30E9F,0xFFF40EAF,0xFFF50F1F,0xFFF60F2F,0xFFF70F3F,0xFFF80F4F,0xFFF90F5F,0xFFFA0F6F,0xFFFB0F7F,0xFFFC0F8F,0xFFFD0F9F,0xFFFE0FAF
    };
    HuffmanCodes[2] = new int[]
    {
      0x00000001,0x40000011,0x80000021,0xC0000032,0xE0000043,0xF0000054,0xF8000065,0xFC000076,0xFE000087,0xFF000098,0xFF8000A9,0xFFC000BA
    };
    HuffmanCodes[3] = new int[]
    {
      0x00000001,0x40000011,0x80000022,0xA0000033,0xB0000113,0xC0000044,0xC8000054,0xD0000214,0xD8000314,0xE0000065,0xE4000125,0xE8000415,0xEC000515,0xF0000076,0xF2000616,0xF4000716,0xF6000137,0xF7000227,0xF8000327,0xF9000817,0xFA000088,0xFA800148,0xFB000428,0xFB800918,0xFC000A18,0xFC800B18,0xFD000C18,0xFD800099,0xFDC00239,0xFE000339,0xFE400529,0xFE800F09,0xFEC0015A,0xFEE0062A,0xFF00072A,0xFF200D1A,0xFF4000AB,0xFF50016B,0xFF60024B,0xFF70034B,0xFF800E1D,0xFF84025E,0xFF860F1E,0xFF88017F,0xFF89018F,0xFF8A019F,0xFF8B01AF,0xFF8C026F,0xFF8D027F,0xFF8E028F,0xFF8F029F,0xFF9002AF,0xFF91035F,0xFF92036F,0xFF93037F,0xFF94038F,0xFF95039F,0xFF9603AF,0xFF97043F,0xFF98044F,0xFF99045F,0xFF9A046F,0xFF9B047F,0xFF9C048F,0xFF9D049F,0xFF9E04AF,0xFF9F053F,0xFFA0054F,0xFFA1055F,0xFFA2056F,0xFFA3057F,0xFFA4058F,0xFFA5059F,0xFFA605AF,0xFFA7063F,0xFFA8064F,0xFFA9065F,0xFFAA066F,0xFFAB067F,0xFFAC068F,0xFFAD069F,0xFFAE06AF,0xFFAF073F,0xFFB0074F,0xFFB1075F,0xFFB2076F,0xFFB3077F,0xFFB4078F,0xFFB5079F,0xFFB607AF,0xFFB7082F,0xFFB8083F,0xFFB9084F,0xFFBA085F,0xFFBB086F,0xFFBC087F,0xFFBD088F,0xFFBE089F,0xFFBF08AF,0xFFC0092F,0xFFC1093F,0xFFC2094F,0xFFC3095F,0xFFC4096F,0xFFC5097F,0xFFC6098F,0xFFC7099F,0xFFC809AF,0xFFC90A2F,0xFFCA0A3F,0xFFCB0A4F,0xFFCC0A5F,0xFFCD0A6F,0xFFCE0A7F,0xFFCF0A8F,0xFFD00A9F,0xFFD10AAF,0xFFD20B2F,0xFFD30B3F,0xFFD40B4F,0xFFD50B5F,0xFFD60B6F,0xFFD70B7F,0xFFD80B8F,0xFFD90B9F,0xFFDA0BAF,0xFFDB0C2F,0xFFDC0C3F,0xFFDD0C4F,0xFFDE0C5F,0xFFDF0C6F,0xFFE00C7F,0xFFE10C8F,0xFFE20C9F,0xFFE30CAF,0xFFE40D2F,0xFFE50D3F,0xFFE60D4F,0xFFE70D5F,0xFFE80D6F,0xFFE90D7F,0xFFEA0D8F,0xFFEB0D9F,0xFFEC0DAF,0xFFED0E2F,0xFFEE0E3F,0xFFEF0E4F,0xFFF00E5F,0xFFF10E6F,0xFFF20E7F,0xFFF30E8F,0xFFF40E9F,0xFFF50EAF,0xFFF60F2F,0xFFF70F3F,0xFFF80F4F,0xFFF90F5F,0xFFFA0F6F,0xFFFB0F7F,0xFFFC0F8F,0xFFFD0F9F,0xFFFE0FAF
    };

    QMat = new int[Q.size()][];
    QMat[0] = new int[]
    {
      16,11,10,16,124,140,151,161,
      12,12,14,19,126,158,160,155,
      14,13,16,24,140,157,169,156,
      14,17,22,29,151,187,180,162,
      18,22,37,56,168,109,103,177,
      24,35,55,64,181,104,113,192,
      49,64,78,87,103,121,120,101,
      72,92,95,98,112,100,103,199
    };
    QMat[1] = new int[]
    {
      17,18,24,47,99,99,99,99,
      18,21,26,66,99,99,99,99,
      24,26,56,99,99,99,99,99,
      47,66,99,99,99,99,99,99,
      99,99,99,99,99,99,99,99,
      99,99,99,99,99,99,99,99,
      99,99,99,99,99,99,99,99,
      99,99,99,99,99,99,99,99
    };

    //Setup headers.
    
    ((DefaultTreeModel)tree.getModel()).setRoot(root); file.Events = true;

    //Set the first node.

    tree.setSelectionPath( new TreePath( h.getPath() ) ); open( new JDEvent( this, "", 0 ) );
  }
  
  //Decode all markers of a particular type. Some parts of the image can not be read without the markers being read.

  private void openMarkers( int[] m )
  {
    TreePath currentPath = new TreePath( ((JDNode)tree.getLastSelectedPathComponent()).getPath() );
    
    JDNode node, nodes = (JDNode)root.getFirstChild(); long[] a;

    for( int i1 = nodes.getChildCount() - 1; i1 > 0; i1-- )
    {
      for( int i2 = 0; i2 < m.length; i2++ )
      {
        a = ( node = (JDNode)nodes.getChildAt(i1)).getArgs();

        if( m[i2] == ( a.length > 3 ? a[3] : -1 ) )
        {
          tree.setSelectionPath( new TreePath( node.getPath() ) ); open( new JDEvent(this, "", a ) );
        }
      }
    }

    tree.setSelectionPath( currentPath );
  }

  //All of this moves into sub functions when user clicks on a maker.
  //We can also open required markers when needed by type using the "openMarkers" method.

  private boolean decodeMarker( int type, int size, JDNode marker ) throws java.io.IOException
  {
    JDNode n;

    if( ( type & 0xF0 ) == 0xC0 && !( type == 0xC4 || type == 0xC8 || type == 0xCC ) )
    {
      imageType = type - 0xC0;

      //Non-Deferential Huffman coded pictures.

      if( type == 0xC0 ) { n = new JDNode("Start Of Frame (baseline DCT)", new long[]{ ref++, file.getFilePointer(), size, 0 } ); }
      else if( type == 0xC1 ) { n = new JDNode("Start Of Frame (Extended Sequential DCT)", new long[]{ ref++, file.getFilePointer(), size, 0 }); }
      else if( type == 0xC2 ) { n = new JDNode("Start Of Frame (progressive DCT)", new long[]{ ref++, file.getFilePointer(), size, 0 } ); }
      else if( type == 0xC3 ) { n = new JDNode("Start Of Frame (Lossless Sequential)", new long[]{ ref++, file.getFilePointer(), size, 0 } ); }

      //Huffman Deferential codded pictures.
      
      else if( type == 0xC5 ) { n = new JDNode("Start Of Frame (Differential sequential DCT)", new long[]{ ref++, file.getFilePointer(), size, 0 } ); }
      else if( type == 0xC6 ) { n = new JDNode("Start Of Frame (Differential progressive DCT)", new long[]{ ref++, file.getFilePointer(), size, 0 } ); }
      else if( type == 0xC7 ) { n = new JDNode("Start Of Frame (Differential Lossless)", new long[]{ ref++, file.getFilePointer(), size, 0 } ); }

      //Non-Deferential Arithmetic codded pictures.

      else if( type == 0xC9 ) { n = new JDNode("Start Of Frame (Extended Sequential DCT)", new long[]{ ref++, file.getFilePointer(), size, 0 } ); }
      else if( type == 0xCA ) { n = new JDNode("Start Of Frame (Progressive DCT)", new long[]{ ref++, file.getFilePointer(), size, 0 } ); }
      else if( type == 0xCB ) { n = new JDNode("Start Of Frame (Lossless Sequential)", new long[]{ ref++, file.getFilePointer(), size, 0 } ); }

      //Deferential Arithmetic codded pictures.

      else if( type == 0xCD ) { n = new JDNode("Start Of Frame (Differential sequential DCT)", new long[]{ ref++, file.getFilePointer(), size, 0 } ); }
      else if( type == 0xCE ) { n = new JDNode("Start Of Frame (Differential progressive DCT)", new long[]{ ref++, file.getFilePointer(), size, 0 } ); }
      else { n = new JDNode("Start Of Frame (Differential Lossless)", new long[]{ ref++, file.getFilePointer(), size, 0 } ); }
    }
    else if( type == 0xC4 )
    {
      n = new JDNode("Huffman Table", new long[]{ ref++, H.size(), size, 1 } );

      long pos = file.getFilePointer();

      //Huffman tables can be grouped together so we sum them up and skip them.
      //We do not decode them fully here.

      long t = pos, e = pos + size;
      int sum = 0;

      while( t < e )
      {
        //Tables are assigned by offset, and their table number.
        //This is so we know which ones to load when needed.

        H.add( new markerInfo( file.readByte(), pos ) );

        //We do not need to load and decode the Huffman table at this time.
        //We are only setting up memory to Read the JPEG.

        sum = 0; for( int i = 0; i < 16; sum += file.readByte(), i++ );

        file.skipBytes(sum); t = t + 17 + sum;
      }

      skipM = false; //We do not need to skip the marker as it should be at the end of the marker.
    }
    else if( type == 0xDA )
    {
      n = new JDNode("Start Of Scan", new long[]{ ref++, S.size(), size, 2 } );

      S.add( new markerInfo( 0, file.getFilePointer() ) );
    }
    else if( type == 0xDB )
    {
      n = new JDNode("Quantization Table", new long[]{ ref++, Q.size(), size, 3 } );

      long pos = file.getFilePointer();

      //Quantization tables can be grouped together so we skip them and count them.
      //We do not decode them fully here.

      long t = pos, e = pos + size;
      int tableInfo = 0;

      while( t < e )
      {
        //Tables are assigned by offset, and their table number.
        //This is so we know which ones to load when needed.

        Q.add( new markerInfo( tableInfo = file.readByte(), pos ) );

        file.skipBytes( tableInfo >= 16 ? 128 : 64 );
        
        t += tableInfo >= 16 ? 129 : 65;
      }

      skipM = false; //We do not need to skip the marker as it should be at the end of the marker.
    }
    else if( type == 0xDD )
    {
      n = new JDNode("Restart", new long[]{ ref++, file.getFilePointer(), size, 4 } );
    }
    else if( ( type & 0xF0 ) == 0xE0 )
    {
      n = new JDNode("Application (info)", new long[]{ ref++, file.getFilePointer(), size, 5 } );
    }
    else if( type == 0xFE )
    {
      n = new JDNode("Comment", new long[]{ ref++, file.getFilePointer(), size, 6 } );
    }
    else { n = new JDNode("Maker.h", ref++ ); return( false ); }

    n.add( new JDNode(pad) ); marker.add( n ); return( true );
  }

  public void Uninitialize()
  {
    des.clear(); H.clear(); Q.clear(); S.clear(); ref = 0;
    HuffmanCodes = null; QMat = null; scanC = null; huffExpansion.clear(); HuffTables = 0;
  }

  public void open( JDEvent e )
  {
    if( e.getID().equals("UInit") ) { Uninitialize(); }

    else if( e.getArg(0) >= 0 )
    {
      tree.expandPath( tree.getLeadSelectionPath() );

      if( e.getArgs().length == 2 ) { HuffTable = (int)e.getArg(1); }

      ds.setDescriptor(des.get((int)e.getArg(0)));
    }

    //Highlight data.

    else if( e.getArg(0) == -2 )
    {
      JDNode n = ((JDNode)tree.getLastSelectedPathComponent());

      try { file.seek( e.getArg(1) ); } catch( Exception er ) { }

      if( n.toString().equals("Image Data") )
      {
        info("<html>" + imageData + "</html>");
      }
      else if( n.toString().startsWith("MCU") )
      {
        info("<html>This is one 8x8 square in the image. Each color can have a max of 64 values, or less.</html>");
      }

      tree.expandPath( tree.getLeadSelectionPath() );
      
      Offset.setSelected( e.getArg(1), e.getArg(2) ); ds.clear();
    }

    //Image data

    else if( e.getArg(0) == -3 )
    {
      JDNode n = ((JDNode)tree.getLastSelectedPathComponent());

      if( !isScanned ) { isScanned = true; openMarkers( new int[]{ 1, 2 } ); }

      n.setArgs( new long[]{ -2, n.getArg(1), n.getArg(2) } );

      try
      {
        scanImage( (int)e.getArg(1), (int)e.getArg(2) + 1 );
      }
      catch( Exception er ) { er.printStackTrace(); }

      info("<html>" + imageData + "</html>"); ds.clear();

      tree.expandPath( tree.getLeadSelectionPath() );
      
      Offset.setSelected( e.getArg(1), e.getArg(2) );
    }

    //Read an marker.

    if( e.getArgs().length > 3 )
    {
      JDNode root = (JDNode)tree.getLastSelectedPathComponent(), node = new JDNode("");
      
      if( root.getChildCount() > 0 ){ node = (JDNode)root.getFirstChild(); }

      DefaultTreeModel model = ((DefaultTreeModel)tree.getModel());

      int size = (int)e.getArg(2), type = (int)e.getArg(3);

      file.Events = false; 
      
      try
      {
        if( type == 1 )
        {
          int base = (int)e.getArg(1); file.seek( H.get( base ).Offset );

          //Begin reading Huffman Tables.

          Descriptor nTable = new Descriptor(file); des.add(nTable); nTable.UINT8("Class/Table Number"); nTable.setEvent( this::HTableInfo );

          int classType = (((byte)nTable.value) & 0xF0) >> 4, num = (((byte)nTable.value) & 0x0F);

          node.setUserObject("Huffman Table #" + num + " (Class = " + ( classType > 0 ? "AC" : "DC" ) + ")"); node.setArgs( new long[]{ ref++ } );

          int Sum = 0, tableNum = 0;

          while( size > 0 )
          {
            Descriptor Huff = new Descriptor(file); des.add(Huff); Huff.setEvent( this::HTableCodes );

            JDNode HRow = new JDNode("Huffman codes.h", ref++); node.add( HRow );

            java.util.LinkedList<Integer> codes = new java.util.LinkedList<Integer>();

            int[] bits = new int[16]; int bitPos = 0, code = 0;

            String bitDecode = "<table border=\"1\">";
            bitDecode += "<tr><td>Bits</td><td>Code</td><td>Length</td></tr>";

            for( int i = 1; i <= 16; i++ ) { Huff.UINT8("Bits " + i + ""); Sum += bits[ i - 1 ] = ((byte)Huff.value) & 0xFF; }

            Huff = new Descriptor(file); des.add(Huff); Huff.setEvent( this::HTableData );
            
            long[] arg = new long[2]; arg[0] = ref++; arg[1] = HuffTables++;

            HRow = new JDNode("Data.h", arg ); node.add( HRow );

            for( int i1 = 1; i1 <= 16; i1++ )
            {
              for( int i2 = 0; i2 < bits[ i1 - 1 ]; i2++ )
              {
                Huff.UINT8("Huffman Code " + i1 + " bits"); code = ((byte)Huff.value) & 0xFF;

                //Store the code in the integer with the higher 16 bits as the bit combination.
                //The lower 4 bits as the 0 to 16 bit length next 4 bits as the huffman code.

                codes.add( ( bitPos << ( 16 + ( 16 - i1 ) ) ) + ( code << 4 ) + ( i1 - 1 ) );

                bitDecode += "<tr><td>" + pad( Integer.toBinaryString(bitPos), i1 ) + "</td><td>" + pad( Integer.toHexString( code ), 2 ) + "</td><td>" + ( i1 + ( code & 0x0F ) ) + "</td></tr>";

                bitPos += 1;
              }

              bitPos <<= 1;
            }

            bitDecode += "</table>"; huffExpansion.add( bitDecode );

            HuffmanCodes[base + tableNum] = codes.stream().mapToInt(Integer::intValue).toArray();

            //The tables can be grouped together under one marker.

            size -= 17 + Sum; Sum = 0; if( size > 0 )
            {
              nTable = new Descriptor(file); des.add(nTable); nTable.UINT8("Class/Table Number"); nTable.setEvent( this::HTableInfo );

              classType = (((byte)nTable.value) & 0xF0) >> 4; num = (((byte)nTable.value) & 0x0F);

              node = new JDNode("Huffman Table #" + num + " (Class = " + ( classType > 0 ? "AC" : "DC" ) + ")", ref++);

              model.insertNodeInto(node, root, root.getChildCount());
            }

            tableNum += 1;
          }
        }
        else if( type == 2 )
        {
          if( !scanFrame ){ openMarkers( new int[]{ 0 } ); scanFrame = true; }

          int base = (int)e.getArg(1); file.seek( S.get( base ).Offset );

          node.setUserObject("Components.h"); node.setArgs( new long[]{ ref++ } );

          Descriptor Scan = new Descriptor(file); des.add(Scan);

          Scan.UINT8("Number of Components"); int Ns = ((byte)Scan.value) & 0xFF; scanC[base] = new int[subSampling.length];

          for( int c = 1; c <= Ns; c++ )
          {
            Scan.Array("Component #" + c + "", 2);
            Scan.UINT8("Scan component"); scanC[base][(byte)Scan.value - 1] = subSampling[(byte)Scan.value - 1];
            Scan.UINT8("Entropy Table");
          }
    
          Descriptor info = new Descriptor(file); des.add(info);
    
          model.insertNodeInto(new JDNode("Scan info.h", ref++), root, root.getChildCount());
    
          info.UINT8("Start of Spectral"); Spectral[base << 1] = (byte)info.value;
          info.UINT8("End of Spectral"); Spectral[(base << 1) + 1] = (byte)info.value;
          info.UINT8("ah/al");
        }
        else if( type == 3 )
        {
          int base = (int)e.getArg(1); file.seek( Q.get( base ).Offset );

          //Begin reading Quantization Tables.

          Descriptor nTable = new Descriptor(file); des.add(nTable); nTable.UINT8("Precision/Table Number"); nTable.setEvent( this::QTableInfo );

          int Precision = (((byte)nTable.value) & 0xF0) >> 4;

          node.setUserObject("Quantization Table #" + (((byte)nTable.value) & 0x0F) + " (" + ( Precision == 0 ? "8 Bit" : "16 bit" ) + ")"); node.setArgs( new long[]{ ref++ } );

          int eSize = Precision == 0 ? 65 : 129;

          while( size >= eSize )
          {
            for( int i = 1; i <= 8; i++ )
            {
              Descriptor QMat = new Descriptor(file); des.add(QMat);
    
              JDNode matRow = new JDNode("Row #" + i + ".h", ref++); node.add( matRow );
    
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
              nTable = new Descriptor(file); des.add(nTable); nTable.UINT8("Precision/Table Number"); nTable.setEvent( this::QTableInfo );

              Precision = (((byte)nTable.value) & 0xF0) >> 4; eSize = Precision == 0 ? 65 : 129;

              node = new JDNode("Quantization Table #" + (((byte)nTable.value) & 0x0F) + " (" + ( Precision == 0 ? "8 Bit" : "16 bit" ) + ")", ref++);

              model.insertNodeInto(node, root, root.getChildCount());
            }
          }
        }
        else { file.seek( e.getArg(1) ); }

        if( type == 0 )
        {
          Descriptor image = new Descriptor(file); des.add(image); image.setEvent( this::StartOfFrame );

          node.setUserObject("Image Information"); node.setArgs( new long[]{ ref++ } );
    
          image.UINT8("Sample Precision");
          image.UINT16("Picture Height"); width = ((short)image.value) & 0xFFFF;
          image.UINT16("Picture Width"); height = ((short)image.value) & 0xFFFF;
    
          image.UINT8("Number of Color Components in Picture"); int Nf = ((byte)image.value) & 0xFF, comp = 0;

          subSampling = new int[Nf]; table = new int[Nf];
    
          for( int i = 1; i <= Nf; i++ )
          {
            Descriptor imageComp = new Descriptor(file); des.add(imageComp); imageComp.setEvent( this::ComponentInfo );
            
            node.add( new JDNode("Color Component" + i + ".h", ref++) );
            
            imageComp.UINT8("Component Indemnifier"); comp = ((byte)imageComp.value) - 1;
            imageComp.UINT8("Vertical Horizontal squares");

            subSampling[comp] = ((byte)imageComp.value); subSampling[comp] = ( subSampling[comp] & 0x0F ) * ( ( subSampling[comp] >> 4 ) & 0x0F );

            imageComp.UINT8("Table Number"); table[comp] = ((byte)imageComp.value);
          }

          ((DefaultTreeModel)tree.getModel()).nodeChanged( (JDNode)tree.getLastSelectedPathComponent() );
        }
        else if( type == 4 )
        {
          Descriptor r = new Descriptor(file); des.add(r); r.UINT16("Restart interval");

          node.setUserObject("Restart interval.h"); node.setArgs( new long[]{ ref++ } );
        }
        else if( type == 5 )
        {
          Descriptor m = new Descriptor(file); des.add(m);

          m.String8("Type", (byte)0x00); String Type = (String)m.value;

          node.setUserObject( Type + ".h" ); node.setArgs(new long[]{ ref++ });

          if( Type.equals("JFIF") )
          {
            m.setEvent( this::JFIFInfo );
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
          else { m.Other("Application Data", size - Type.length() - 1 ); m.setEvent( this::AppInfo ); }
        }
        else if( type == 6 )
        {
          Descriptor text = new Descriptor(file); des.add(text); text.String8("Comment", size);

          node.setUserObject("Text.h"); node.setArgs( new long[]{ ref++ } );
        }
        else if( type == 7 )
        {
          if( imageType > 8 ){ info("<html>The start of frame marker type value specifies the image data type.<br /><br />" +
          "Right now Arithmetic encoded JPEG image data is not supported. Only baseline image data is supported.</html>"); ds.clear(); return; }

          long t = file.getFilePointer();

          file.Events = false;

          String out = "<table border=\"1\">";
          out += "<tr><td colspan=\"2\">Huffman table.</td><td colspan=\"2\">RAW Binary Data</td><td rowspan=\"2\">Decoded Value</td></tr>";
          out += "<tr><td>Huff Table</td><td>Huff Code</td><td>Match</td><td>Binary Value</td></tr>";

          boolean match = false, EOB = false;

          int v = 0;
          
          int c = 0;
          
          int bit = 0, code = 0, len = 0, zrl = 0;
          
          int value = 0;

          int bitPos = (int)e.getArg(2) + 32, bytes = 0, bitLen = 0, mp = Integer.MAX_VALUE, mpx = 0;

          int loop = 0;

          int[][] HuffTables = getHuffmanTables((int)t);
          
          int sc = (int)e.getArg(4); boolean DC = sc < 0 ? true : false;
          
          int[] HuffTable = HuffTables[ sc < 0 ? -sc : sc ];

          sc = getScan((int)t); loop = Spectral[sc << 1]; int end = Spectral[(sc << 1) + 1];

          if( !DC ) { end += 1; }

          //Each code has a length for the number of bits is the binary number value.

          int[] DCT = new int[64];

          while( !EOB && loop < end )
          {
            //Load in new bytes as needed.

            bytes = bitPos / 8; for( int i = 0; i < bytes; i++ )
            {
              bitPos -= 8; code = file.read();
              
              if( code > 0 )
              {
                v |= code << bitPos; if( code == 255 ){ if( ( file.read() & 0xFF ) == 0 ) { mp = ( bitLen + ( 24 - bitPos ) ) & -8; mpx += 8; } }
              }
            }

            //There is only one DC per 8x8 block. The rest are AC.

            c = 0; while( !match && c < HuffTable.length )
            {
              code = HuffTable[c++]; bit = code & 0xF; if( ( v & bits[bit] ) == ( code & 0xFFFF0000 ) ){ len = ( code >>> 4 ) & 0x0F; zrl = ( code >>> 8 ) & 0x0F; match = true; }
            }

            if( match )
            {
              out += "<tr><td>Table #" + e.getArg(4) + " Class " + ( !DC ? "DC" : "AC" ) + "</td><td>" + String.format( "%02X", ( code >>> 4 ) & 0xFF ) + "</td>";

              out += "<td>" + pad( Integer.toBinaryString( code >>> ( 16 + ( 15 - bit ) ) ), bit + 1 ) + "</td>"; v <<= bit + 1;

              bitPos += bit + 1; bitLen += bit + len + 1;

              if( ( len + zrl ) > 0 )
              {
                if( len > 0 )
                {
                  value = ( v & bits[len - 1] ) >>> ( 32 - len );

                  out += "<td>" + pad( Integer.toBinaryString( value ), len );

                  value = value < ( 1 << ( len - 1 ) ) ? value - ( ( 1 << len ) - 1 ) : value;
                  
                  out += "</td><td>" + value + "</td>"; v <<= len; bitPos += len;
                }
              }
              else { out += "<td>EOB</td><td>0</td>"; EOB = loop > 0; }

              out += "</tr>";
            }

            if( !DC ) { HuffTable = HuffTables[(int)e.getArg(4) + 1]; if( HuffTable == null ){ EOB = true; } DC = true; }

            if( bitLen >= mp ) { mp = Integer.MAX_VALUE; bitLen += mpx; mpx = 0; }

            loop += zrl + 1; if( loop < 64 ) { DCT[ loop - 1 ] = value; } value = 0; match = false;
          }

          out += "</table>";

          //The binary and hex string.

          if( ( bitLen & 7 ) != 0 ) { bitLen += 8; }

          //Show the raw binary data.

          file.seek(t); file.read( bitLen >> 3 );

          String bin = ""; for( int i = 0; i < ( bitLen >> 3 ); i++ ) { bin += pad( Integer.toBinaryString( ((int)file.toByte(i)) & 0xFF ), 8 ) + " "; }

          //Decode the DCT matrix.

          String[] row = new String[]{"<tr>","<tr>","<tr>","<tr>","<tr>","<tr>","<tr>","<tr>"}; for( int i = 0; i < zigzag.length; i++ ) { row[zigzag[i]] += "<td>" + DCT[i] + "</td>"; }

          String DCTm = "<table border=\"1\"><tr><td colspan=\"8\">8x8 DCT matrix</td></tr>" + row[0] + "</tr>" + row[1] + "</tr>"+ row[2] + "</tr>"+ row[3] + "</tr>" + row[4] + "</tr>"+ row[5] + "</tr>"+ row[6] + "</tr>"+ row[7] + "</tr></table>"; row = null;

          ds.clear(); info( "<html>The RAW binary data = " + bin + "<br /><br />" + 
          ( e.getArg(2) > 0 ? "The previous 8x8 ended at binary digit " + e.getArg(2) + ", so decoding starts at binary digit " + e.getArg(2) + ".<br /><br />" : "" ) +
          "The binary data is split apart by matching one Huffman combination, and reading a variable in length binary number value.<br /><br />" +
          "The RAW binary data is split apart in 2 columns. The first 2 columns show what the matching Huffman code is for the binary combination. The last column shows what the binary value decodes as.<br /><br />" +
          "Table DC is used for the first value, then the rest use table AC.<br /><br />" +
          "Each huffman code is split into tow values. The first 0 to 15 hex digit tells us how many zero values are used before our color value.<br /><br />" +
          "The Last 0 to 15 hex digit tells us how many binary digits to read for the color value. This allows us to define all 64 values in 8x8 using as little data as possible.<br /><br />" +
          "If we match a Huffman code that is 00 meanings no value. This means there is no more color values for the 8x8, so we terminate early with EOB.<br /><br />" +
          out + "<br /><br />Each of the decoded values are placed into a matrix in a zigzag pattern.<br /><br />" + DCTm + "</html>" );

          out = null; DCTm = null;

          Offset.setSelected( t, t + ( bitLen >> 3 ) - 1 );
          
          file.Events = true; return;
        }

        root.setArgs( new long[]{ root.getArg(0) } );
      }
      catch( Exception er ) { er.printStackTrace(); }

      file.Events = true;
    }
  }

  //Finds the huffman tables that are active at an offset for each color component.

  private int[][] getHuffmanTables( int Offset )
  {
    int[][] tables = new int[table.length << 1][];
    int num;
    boolean DC, AC;
    markerInfo i;

    //For each color component.

    for( int i1 = 0; i1 < table.length; i1++ )
    {
      num = table[i1]; DC = false; AC = false;

      //Find closest previous DC/AC class for table number needed.

      for( int i2 = H.size() - 1; i2 >= 0; i2-- )
      {
        i = H.get(i2);

        if( i.Offset < Offset && ( i.type & 0x0F ) == num )
        {
          if( !DC && i.type < 16 ) { tables[i1 << 1] = HuffmanCodes[i2]; DC = true; }
          else if( !AC ) { tables[( i1 << 1 ) + 1] = HuffmanCodes[i2]; AC = true; }
        }

        if( AC & DC ){ break; }
      }
    }

    return( tables );
  }

  //Finds the closest scan marker for the active color components in image data.

  private int getScan( int Offset )
  {
    for( int i1 = scanC.length - 1; i1 >= 0; i1-- )
    {
      if( S.get( i1 ).Offset < Offset ){ return( i1 ); }
    }

    return( -1 );
  }

  //The first scan of a color component should have one DC value. Any new scans should only scan the AC values.
  //This method checks the previous scan markers to see if the DC value still needs to be read.

  private boolean[] checkDC( int Scan )
  {
    int[] scan;
    boolean end = false;
    boolean[] o = new boolean[table.length];

    for( int i1 = Scan - 1; i1 >= 0 && !end; i1-- )
    {
      end = true; scan = scanC[i1]; for( int i2 = 0; i2 < scan.length; i2++ )
      {
        if( scan[i2] > 0 ){ o[i2] = true; }
      }

      for( int i2 = 0; i2 < o.length; i2++ ){ if( !o[i2] ){ end = false; } }
    }

    return( o );
  }

  //Do A fast optimized full scan of the image data defining each DCT start and end position.
  //When user clicks on a DCT, then the algorithm is run with detailed output for just the one DCT at any position in image.

  private void scanImage( int Start, int End ) throws java.io.IOException
  {
    JDNode node = (JDNode)tree.getLastSelectedPathComponent(); node.removeAllChildren();

    if( imageType > 8 ) { node.add( new JDNode("Unsupported.h", new long[]{-1,0,0,7}) ); return; }

    file.Events = false; file.seek( Start ); file.read( End - Start );

    int size = End - Start, bitSize = ( size << 3 ) - 7, v = 0;

    boolean match = false, EOB = false;
    
    int c = 0;
    
    int bit = 0, code = 0, len = 0, zrl = 0;
    
    int bitPos = 32, bytes = 0, BPos = 0, pos = 0;

    int loop = 0;

    int mp = Integer.MAX_VALUE, mpx = 0;

    //WE have to search for which huffman tables to use that are closest to the image data.

    int[][] Huffman = getHuffmanTables(Start); int TableNum = 0;
    
    int[] HuffTable = Huffman[TableNum];

    //Color components. The closest start of scan marker tells us which colors are used in image data.

    int sc = getScan( Start ); int[] samples = scanC[sc];

    //Check for a previous scan markers that used the color components.
    //The DC color value is only read once.

    boolean[] DC = checkDC( sc );

    //Each scan marker tells us what color to start at and which color to end at.
    //This allows us to add more color details to previously scanned color components.
    //Spectral start and end does not include the DC color value that must be read on first scan of a color component.

    int start = Spectral[sc << 1], end = Spectral[(sc << 1) + 1];

    //We define the color component names if used. We can define fewer or more colors in the start of frame marker.
    //It is unknown what the color names would be if they exceed past the YCbCr color type.

    int nComps = samples.length - 1, comp = 0, smp = 0;

    String[] Colors = new String[16]; Colors[0] = " (Y)"; Colors[1] = " (Cb)"; Colors[2] = " (Cr)";

    //Define the first MCU.

    JDNode MCU = new JDNode( pad, new long[]{ 0, 0 } );

    //Each code has a length for the number of bits is the binary number value.

    for( int mcu = 0; pos < bitSize; smp++ )
    {
      if( smp >= samples[comp] ){ smp = 0; comp = comp == nComps ? 0 : comp + 1; TableNum = TableNum == ( nComps << 1 ) ? 0 : TableNum + 2; }

      //Add DCT matrix node with number, and position.

      HuffTable = Huffman[ DC[comp] ? TableNum + 1 : TableNum ];

      if( smp == 0 && comp == 0 )
      {
        MCU.setArgs( new long[]{ -2, MCU.getArg(1), ( Start + ( pos >> 3 ) ) } );
        MCU = new JDNode("MCU #" + mcu + "", new long[]{ -2, ( Start + ( pos >> 3 ) ), 0 } );
        node.add( MCU ); mcu += 1;
      }

      //Skip color components that are 0.

      if( samples[comp] > 0 )
      {
        MCU.add( new JDNode("DCT #" + smp + Colors[comp] + ".h", new long[]{ -1, Start + ( pos >> 3 ), pos & 7, 7, DC[comp] ? -TableNum : TableNum } ) );

        sc = 0;

        loop = start; if( !DC[comp] ) { end += 1; }
        
        EOB = false;

        System.out.println("start = " + loop + ", end = " + end + "" );
        
        while( !EOB && loop < end )
        {
          //Load in new bytes as needed into integer.

          bytes = bitPos / 8; for( int i = 0; i < bytes; i++ )
          {
            bitPos -= 8; if( BPos < size )
            {
              code = file.toByte( BPos ) & 0xFF; v |= code << bitPos;
                
              //The code 0xFF is used for markers. In some cases we need to use 0xFF as value.
              //If the next byte after 0xFF is 0x00, then 0xFF is used as a value, and 0x00 is skipped.
                            
              if( code == 255 ) { mp = BPos << 3; mpx += 8; BPos += 1; }
            }
                
            BPos += 1;
          }

          //There is only one DC per 8x8 block. The rest are AC.
        
          c = 0; while( !match && c < HuffTable.length )
          {
            code = HuffTable[c++]; bit = code & 0xF; if( ( v & bits[bit] ) == ( code & 0xFFFF0000 ) ){ bit += 1; len = ( code >>> 4 ) & 0x0F; zrl = ( code >>> 8 ) & 0x0F; match = true; }
          }

          if( match ) { bitPos += bit; v <<= bit; if( ( len + zrl ) > 0 ) { v <<= len; bitPos += len; } else { EOB = loop > 0; } }

          pos += bit + len; if( pos > mp ){ mp = Integer.MAX_VALUE; pos += mpx; mpx = 0; }
        
          loop += zrl + 1;
          
          if( !DC[comp] && sc != -1 ) { HuffTable = Huffman[ TableNum + 1 ]; if( HuffTable == null ){ EOB = true; } sc = -1; }
          
          match = false;
        }

        if( !DC[comp] ) { end -= 1; }
      }
    }

    file.Events = true;
  }

  //Pad string with 0.

  private String pad( String s, int size ) { for( int i = s.length(); i < size; i++, s = "0" + s ); return( s ); }

  private static String markerTypes = "<table border='1'>" +
  "<tr><td>Type</td><td>Format</td><td>Marker Defines.</td></tr>" +
  "<tr><td>192</td><td>Start of Frame</td><td>Image data is Baseline DCT.</td></tr>" +
  "<tr><td>193</td><td>Start of Frame</td><td>Image data is Extended Sequential DCT.</td></tr>" +
  "<tr><td>194</td><td>Start of Frame</td><td>Image data is Progressive DCT.</td></tr>" +
  "<tr><td>195</td><td>Start of Frame</td><td>Image data is Lossless (sequential).</td></tr>" +
  "<tr><td>196</td><td>Define Huffman Table</td><td></td></tr>" +
  "<tr><td>197</td><td>Start of Frame</td><td>Image data is Differential sequential DCT.</td></tr>" +
  "<tr><td>198</td><td>Start of Frame</td><td>Image data is Differential progressive DCT.</td></tr>" +
  "<tr><td>199</td><td>Start of Frame</td><td>Image data is Differential lossless (sequential).</td></tr>" +
  "<tr><td>201</td><td>Start of Frame</td><td>Image data is Extended sequential DCT, Arithmetic coding.</td></tr>" +
  "<tr><td>202</td><td>Start of Frame</td><td>Image data is Progressive DCT, Arithmetic coding.</td></tr>" +
  "<tr><td>203</td><td>Start of Frame</td><td>Image data is Lossless (sequential), Arithmetic coding.</td></tr>" +
  "<tr><td>204</td><td>Define Arithmetic Coding</td><td></td></tr>" +
  "<tr><td>205</td><td>Start of Frame</td><td>Image data is Differential sequential DCT, Arithmetic coding.</td></tr>" +
  "<tr><td>206</td><td>Start of Frame</td><td>Image data is Differential progressive DCT, Arithmetic coding.</td></tr>" +
  "<tr><td>207</td><td>Start of Frame</td><td>Image data is Differential lossless (sequential), Arithmetic coding.</td></tr>" +
  "<tr><td>208 to 215</td><td>Restart Marker</td><td>Restart Markers 0 to 7.</td></tr>" +
  "<tr><td>216</td><td>Start of Image</td><td>Image must start with this marker.</td></tr>" +
  "<tr><td>217</td><td>End of Image</td><td>Image must end with this marker.</td></tr>" +
  "<tr><td>218</td><td>Start of Scan</td><td></td></tr>" +
  "<tr><td>219</td><td>Define Quantization Table</td><td></td></tr>" +
  "<tr><td>220</td><td>Define Number of Lines</td><td>(Not common)</td></tr>" +
  "<tr><td>221</td><td>Define Restart Interval</td><td></td></tr>" +
  "<tr><td>222</td><td>Define Hierarchical Progression</td><td>(Not common)</td></tr>" +
  "<tr><td>223</td><td>Expand Reference Component</td><td>(Not common)</td></tr>" +
  "<tr><td>224 to 239</td><td>App Marker</td><td>Picture Application info only 0 to 15.</td></tr>" +
  "<tr><td>254</td><td></td><td>Textural based comment.</td></tr>" +
  "</table>";

  public static String markerRule = "Every marker must start with 255, and must not have a maker type with 255, 0, or 1.<br /><br />" +
  "Marker codes do not have to start one after another. Invalid makers are skipped as padding is allowed.";

  public static final String imageData = "The huffman tables are used to decode the image data.<br /><br />" +
  "<table border=\"1\">" +
  "<tr><td>Table</td><td>Usage</td></tr>" +
  "<tr><td>Huffman table #0 (Class = DC)</td><td>Used for DC component of Luminance (Y).</td></tr>" +
  "<tr><td>Huffman table #1 (Class = DC)</td><td>Used for DC component of Chrominance (Cb & Cr).</td></tr>" +
  "<tr><td>Huffman table #0 (Class = AC)</td><td>Used for AC component of Luminance (Y).</td></tr>" +
  "<tr><td>Huffman table #1 (Class = AC)</td><td>Used for AC component of Chrominance (Cb & Cr).</td></tr>" +
  "</table><br />" +
  "JPEG pictures use variable length numbers for each Y, Cb, Cr color in the image data.<br /><br />" +
  "Each color uses one DC value that is added to the 8 by 8 quantized matrix. The AC huffman table is for individual values in the 8 by 8 matrix.<br /><br />" +
  "If we chose not to filter out anything, then we would have 1 DC following 63 AC values for each 8x8.<br /><br />" +
  "A huffman table can say binary digits 01 uses the next 4 binary digits as a number.<br /><br />" +
  "And also that binary digits 11 uses the next 13 in length binary number.<br /><br />" +
  "A huffman table cen specify 111 for a 0 in length number value. The 0 in length codes are used to set a end point for AC values.<br /><br />" +
  "This way we use as little data as possible for image color data. An optimized huffman table is the most used bit combinations as 1 to 2 bit combinations.";

  public static final String[] markers = new String[]
  {
    "<html>This Must always be 255.</html>",
    "<html>" + markerRule + "<br /><br />" +
    "Marker types 208 to 223 do not have a marker size number after marker type.<br /><br /><hr /><br />" +
    "All JPEG pictures start with a start of image marker type = 216. The maker does not contain a size after it as it is in the maker range 208 to 223.<br /><br />" +
    "There is always one \"Start of frame\" marker in a JPEG which defines the picture width and height and the format the image data is read in.<br /><br />" +
    "The \"Start of frame\" type number defines how to read the image data so there is a lot of \"start of frame\" maker types even though they are all read the same.<br /><br />" +
    "Because of the \"Start of frame\" marker I have defined a marker format column, and an extended description of what the maker implies the image data is by marker type.<br /><br />" +
    markerTypes + "</html>",
    "<html>This is the size of the marker. The two bytes that are read for the size are included as part of the marker size.<br /><br />Markers types 208 to 223 do not have a size.</html>",
    "<html>Unknown marker data. This happens when a unknown maker type is used.</html>",
  };

  public static final String[] AppInfo = new String[]
  {
    "<html>Each application marker has a 00 byte terminated text string. This is earthier the application name, or URL.<br /><br />" +
    "The application markers define information specific to the application used to create, or make the JPEG.<br /><br />" +
    "The information defined in these markers are not necessary to draw the picture.</html>",
    "<html>This is the application specific data.</html>"
  };

  public static final String[] JFIFInfo = new String[]
  {
    "<html>Each application marker has a 00 byte terminated text string. This is the JFIF header.</html>",
    "<html>This is the major version.<br /><br />" +
    "If major version is 7, and minor version is 2 we when up with version number 7.2v.</html>",
    "<html>This is the major version.<br /><br />" +
    "If major version is 7, and minor version is 2 we when up with version number 7.2v.</html>",
    "<html>Image density.</html>",
    "<html>Horizontal pixel Density.</html>",
    "<html>Vertical pixel Density.</html>",
    "<html>Horizontal pixel count.</html>",
    "<html>Vertical pixel count.</html>",
    "<html>Extended JFIF picture information.</html>"
  };

  public static final String[] StartOfFrame = new String[]
  {
    "<html>This should always be 8 by 8 sample size.</html>",
    "<html>Picture Height in pixels.</html>",
    "<html>Picture Width in pixels.</html>",
    "<html>Number of component's. Usually 3, for Y, Cb, Cr.<br /><br />" +
    "Each component uses an 8 by 8 quantization table, and huffman table. You can change the table number each color uses if you like." +
    "You can also modify the 8 by 8 quantization matrix an color uses if you like.</html>"
  };

  public static final String[] DefineComponents = new String[]
  {
    "<html>This is the assigned color number. Each \"start of scan\" marker uses the color number.<br /><br />" +
    "You can switch color numbers if you like. You can set color 1 to 2, and color 2 to 1.</html>",
    "<html>The first hex digit is Vertical 0 to 15, and the last hex digit is 0 to 15 horizontal.</html>",
    "<html>This is the huffman table, and quantization matrix that will be used with this color number.<br /><br />" +
    "You can modify the quantization matrix this component uses, or set it to a different one defined in this image.</html>"
  };

  public void MInfo( int el )
  {
    if( el < 0 ) { info("<html>" + markerRule + "</html>"); }
    else
    {
      info( markers[el] );
    }
  }

  public void JFIFInfo( int el )
  {
    if( el < 0 ) { info("<html>Picture application only information.</html>"); }
    else
    {
      info( JFIFInfo[el] );
    }
  }

  public void AppInfo( int el )
  {
    if( el < 0 ) { info("<html>Picture application only information.</html>"); }
    else
    {
      info( AppInfo[el] );
    }
  }

  public void StartOfFrame( int el )
  {
    if( el < 0 )
    {
      info("The <strong>Start Of Frame</strong> defines the width and height of the JPEG picture.<br /><br />" +
      "The frame also usually specifies 3 color components, Y, Cb, and Cr per pixel color.<br /><br />" +
      "Each <strong>Start Of Scan</strong> marker lets us pick which colors to use preceding the <strong>Image Data</strong>.<br /><br />" +
      "Color is read in 8 by 8 squares then we move to the next color in image data till we complete one 8 by 8 square called a MCU (Minium codded unit).<br /><br />" +
      "Each color has a SubSampling setting that lets us set number of vertical and horizontal squares one color takes up.<br /><br />" +
      "This allows us to make one color 16 by 8, or 16 by 16, but if the other colors are 8 by 8 then that means we must scale the other colors into 16 by 16.<br /><br />" +
      "JPEGS often do this with Color Y and leave Cr, and Cb as 8 by 8 per square. This is called halving the quality of the chroma color per pixel.<br /><br />" +
      "The color components specify which table number to use for both the quantization table, and Huffman table number.<br /><br />" +
      "This is a general description. You can see how the two tables are used to read the picture in the image data sections.");
    }
    else
    {
      info( StartOfFrame[el] );
    }
  }

  public void ComponentInfo( int el )
  {
    if( el < 0 )
    {
      info("Defines the image colors which are assigned by the <strong>Start Of Scan</strong> maker preceding the image data.");
    }
    else
    {
      info( DefineComponents[el] );
    }
  }

  public void QTableInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>The quantitation tables are used with the start of frame marker which defines the color components that will be used with the quantization table number.<br /><br />" +
      "JPEG pictures are compressed into luminous data that present 8 by 8 tiles of the image. Image color data is approximated using the quantitation matrix.</html>");
    }
    else
    {
      info("<html>The first 0 to F digit is the 0 to 15 precision. The last digit 0 to F is 0 to 15 table number.<br /><br />" +
      "If precision is 1 then each point is 16 bits instead of 8 bits.</html>");
    }
  }

  public void HTableInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>" + imageData + "</html>");
    }
    else
    {
      info("<html>The first 0 to F digit is the 0 to 15 is the Class type 1 = AC, and 0 = DC. The last digit 0 to F is 0 to 15 table number.</html>");
    }
  }

  public void HTableCodes( int el )
  {
    if( el < 0 )
    {
      info("<html>A Huffman table specifies the number of codes that use a set bit combination length 1 to 16.<br /><br />" +
      "Say bit length 3 has 3 codes. Then we count from 000 binary going 000 = ?, 001 = ?, 010 = ?.<br /><br />" +
      "We add one more time to the 3-bit combination before moving to the next bit combination 010 + 1 = 011.<br /><br />" +
      "Now say bit length 5 has 2 values. We then make our current three-bit combination into 5 by moving to the left 2 times, making 011 into 011 00.<br /><br />" +
      "The next 2 codes are then 01100 = ?, 01101 = ? as we continue the counting sequence.<br /><br />" +
      "After this last 5 bit combination we then must not forget to add +1 before moving to the next combination length.<br /><br />" +
      "The question marks are filled in with the bytes that are read after the 16 bytes.<br /><br />" +
      "The counting sequence can also be graphed out as a binary tree using 0 to 1 nodes. Which for some makes it easier to map the code combinations.</html>");
    }
    else
    {
      info("<html>Number of codes existent under bit combination.</html>");
    }
  }

  public void HTableData( int el )
  {
    if( el < 0 )
    {
      info("<html>To get a general understanding of Huffman binary tree expansion, see the \"Huffman codes\" section.<br /><br />" +
      "The bit combinations are the codes that appear in the image data. After the code is the color value.<br /><br />" +
      "A Huffman code that is 73 would mean the next 7 color values are zero [0,0,0,0,0,0,0,?] then the 8th value is our number value in 8x8.<br /><br />" +
      "The last hex digit is 3 meaning the next 3 binary digits is the color value.<br /><br />" +
      "Some JPEG programs do not optimize the Huffman table to compact as much data as possible.<br /><br />" +
      "Some programs use already made Huffman tables which it pick combinations following the color value giving reasonable compression.<br /><br />" +
      "This is because optimized Huffman tables can sometimes take a while to generate.<br /><br />" +
      "It also requires you to set optimizations when creating the JPEG.<br /><br />" +
      huffExpansion.get( HuffTable ) + "</html>");
    }
    else
    {
      info("<html>Each byte is the Huffman code for a bit combination. The code is broken into tow 4 bit numbers.<br /><br />" +
      "The first 0 to 15 hex digit is the number of 0 values in the 8x8 matrix before the value.<br /><br />" +
      "The Last 0 to 15 hex digit is the length for the number of binary digits to read as the number value.<br /><br />" +
      "A Huffman code that is 73 would mean the next 7 color values are [0,0,0,0,0,0,0,?] then the 8th value is our number value in 8x8.<br /><br />" +
      "The last hex digit is 3 meaning the next 3 binary digits us used for our value.</html>");
    }
  }
}
