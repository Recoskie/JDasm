package EXEDecode;
import javax.swing.*;
import java.io.*;
import javax.swing.tree.*;
import java.awt.*;
import RandomAccessFileV.*;

public class Headers extends Data
{
  //*********************************creates the nicely styled data of the MZ header***********************************

  public JTable ReadMZ(RandomAccessFileV b) throws IOException
  {
    Object rowData[][]={
      {"SIGNATRUE","",""},
      {"Size of Last Page","",""},
      {"Number of 512 byte pages in file","",""},
      {"Number of Relocation Entries","",""},
      {"Header size in Paragraphs","",""},
      {"Minimum additional Memory required in paragraphs","",""},
      {"Maximum additional Memory required in paragraphs","",""},
      {"Initial SS relative to start of file","",""},
      {"Initial SP","",""},
      {"Checksum (unused)","",""},
      {"Initial IP","",""},
      {"Initial CS relative to start of file","",""},
      {"Offset within Header of Relocation Table","",""},
      {"Overlay Number","",""},
      {"Reserved","",""},
      {"ID","",""},
      {"INFO","",""},
      {"Reserved","",""},
      {"PE Header Location","",""},
      {"8086 ASM CODE","",""}
    };
    
    Object columnNames[]={"Usage","Hex","Dec"};

    b.read(b2); String MZ = toHex( b2 ); rowData[0][1] = MZ;

    b.read(b2); rowData[1][1] = toHex(b2); rowData[1][2] = Short.toUnsignedInt( toShort(b2) ) + "";
    b.read(b2); rowData[2][1] = toHex(b2); rowData[2][2] = Short.toUnsignedInt( toShort(b2) ) + "";
    b.read(b2); rowData[3][1] = toHex(b2); rowData[3][2] = Short.toUnsignedInt( toShort(b2) ) + "";
    b.read(b2); rowData[4][1] = toHex(b2); rowData[4][2] = Short.toUnsignedInt( toShort(b2) ) + "";
    b.read(b2); rowData[5][1] = toHex(b2); rowData[5][2] = Short.toUnsignedInt( toShort(b2) ) + "";
    b.read(b2); rowData[6][1] = toHex(b2); rowData[6][2] = Short.toUnsignedInt( toShort(b2) ) + "";
    b.read(b2); rowData[7][1] = toHex(b2); rowData[7][2] = Short.toUnsignedInt( toShort(b2) ) + "";
    b.read(b2); rowData[8][1] = toHex(b2); rowData[8][2] = Short.toUnsignedInt( toShort(b2) ) + "";
    b.read(b2); rowData[9][1] = toHex(b2); rowData[9][2] = Short.toUnsignedInt( toShort(b2) ) + "";
    b.read(b2); rowData[10][1] = toHex(b2); rowData[10][2] = Short.toUnsignedInt( toShort(b2) ) + "";
    b.read(b2); rowData[11][1] = toHex(b2); rowData[11][2] = Short.toUnsignedInt( toShort(b2) ) + "";
    b.read(b2); rowData[12][1] = toHex(b2); rowData[12][2] = Short.toUnsignedInt( toShort(b2) ) + "";
    b.read(b2); rowData[13][1] = toHex(b2); rowData[13][2] = Short.toUnsignedInt( toShort(b2) ) + "";

    b.read(b8); rowData[14][1] = toHex(b8);
    
    b.read(b2); rowData[15][1] = toHex(b2); rowData[15][2] = Short.toUnsignedInt( toShort(b2) ) + "";

    b.read(b2); rowData[16][1] = toHex(b2); rowData[16][2] = Short.toUnsignedInt( toShort(b2) ) + "";

    byte[] bd = new byte[20]; b.read(bd); rowData[17][1] = toHex(bd);

    //Location to the PE header.

    b.read(b4); rowData[18][1] = toHex(b4); PE = toInt(b4); rowData[18][2] = PE + "";

    //The section before the PE header is the small MZ dos program.
    
    bd = new byte[ (int)( PE - 64 ) ]; b.read(bd); rowData[19][1] = toHex(bd);

    //Create the table data.

    JTable T=new JTable(rowData,columnNames);

    if( MZ.equals("4D 5A ") ) { return( T ); }

    return( new JTable( ( new Object[][]{ {"ERROR READING MZ Header"} } ), ( new Object[]{"ERR"} ) ) );
  }

  //*********************************creates the nicely styled data of the PE header***********************************

  public JTable ReadPE(RandomAccessFileV b) throws IOException
  {
    Object RowData[][] = {
      {"SIGNATRUE","",""}, //4
      {"Machine","",""},   //2
      {"Number Of Sections","",""}, //2
      {"Time Date Stamp","",""},    //4
      {"Pointer To Symbol Table","",""}, //4
      {"Number Of Symbols","",""},       //4
      {"Size Of OP Header","",""}, //2
      {"Characteristics","",""}    //2
    };
    Object columnNames[] = {"Useage","Hex","Dec"};

    //data decode to table

    b.read(b4); String PES = toHex(b4); RowData[0][1] = PES;
  
    b.read(b2); RowData[1][1] = toHex( b2 );
    b.read(b2); RowData[2][1] = toHex(b2); NOS = toShort( b2 ); RowData[2][2] = NOS + "";
    b.read(b4); RowData[3][1] = toHex( b4 ); RowData[3][2] = toInt(b4) + "";
    b.read(b4); RowData[4][1] = toHex( b4 ); RowData[4][2] = toInt(b4) + "";
    b.read(b4); RowData[5][1] = toHex( b4 ); RowData[5][2] = toInt(b4) + "";
    b.read(b2); RowData[6][1] = toHex( b2 ); RowData[6][2] = toShort(b2) + "";
    b.read(b2); RowData[7][1] = toHex( b2 );

    //return the output

    JTable T=new JTable(RowData,columnNames);

    //Test if PE header was read correctly.

    if( PES.equals("50 45 00 00 ") ) { return(T); }
    
    //Else error.

    return( new JTable( ( new Object[][]{ {"ERROR READING PE Header"} } ), ( new Object[]{"ERR"} ) ) );
  }

  //************************************************READ OP HEADER********************************************

  public JTable ReadOP(RandomAccessFileV b) throws IOException
  {
    Object RowData[][] = {
      {"SIGNATRUE","",""}, //2

      {"Major Linker Version","",""},   //1
      {"Minor Linker Version","",""}, //1

      {"Size Of Code","",""},    //4
      {"Size Of Initialized Data","",""}, //4
      {"Size Of Uninitialized Data","",""},       //4
      {"Address Of Entry Point","",""}, //4
      {"Base Of Code","",""},    //4
      {"Base Of Data","",""},    //4
      {"Image Base","",""},    //4
      {"Section Alignment","",""},    //4
      {"File Alignment","",""},    //4

      {"Major Operating System Version","",""},    //2
      {"Minor Operating System Version","",""},    //2
      {"Major Image Version","",""},    //2
      {"Minor Image Version","",""},    //2
      {"Major Sub system Version","",""},    //2
      {"Minor Sub system Version","",""},    //2

      {"Win 32 Version Value","",""},    //4
      {"Size Of Image","",""},    //4
      {"Size Of Headers","",""},    //4
      {"Check Sum","",""},    //4

      {"Sub system","",""},    //2
      {"Dll Characteristics","",""},     //2

      {"Size Of Stack Reserve","",""},     //4
      {"Size Of Stack Commit","",""},     //4
      {"Size Of Heap Reserve","",""},     //4
      {"Size Of Heap Commit","",""},     //4
      {"Loader Flags","",""},     //4
      {"Data Directory Array Size","",""},     //4
    };
    Object columnNames[]={"Usage","Hex","Dec"};

    //The OP header has different signature meanings.
    //OP = 0B 01 is a 32 bit program, and 0B 02 is a 64 bit one. Additional 01 07 is a ROM image.
    //Note I could compare numbers 267 for 32 bit, 523 for 64 bit, and 263 for a ROM image.

    b.read(b2); String OPS = toHex(b2); RowData[0][1] = OPS;

    is64bit = OPS.equals("0B 02 ");

    b.read(b1); RowData[1][1] = toHex(b1); RowData[1][2] = ((int)b1[0]) + "";
    b.read(b1); RowData[2][1] = toHex(b1); RowData[2][2] = ((int)b1[0]) + "";

    b.read(b4); RowData[3][1] = toHex(b4); RowData[3][2] = toInt(b4);
    b.read(b4); RowData[4][1] = toHex(b4); RowData[4][2] = toInt(b4);
    b.read(b4); RowData[5][1] = toHex(b4); RowData[5][2] = toInt(b4);
    b.read(b4); RowData[6][1] = toHex(b4); RowData[6][2] = toInt(b4);
    b.read(b4); RowData[7][1] = toHex(b4); RowData[7][2] = toInt(b4);
    b.read(b4); RowData[8][1] = toHex(b4); RowData[8][2] = toInt(b4);

    b.read(b4); RowData[9][1] = toHex(b4); RowData[9][2] = toInt(b4);
    b.read(b4); RowData[10][1] = toHex(b4); RowData[10][2] = toInt(b4);
    b.read(b4); RowData[11][1] = toHex(b4); RowData[11][2] = toInt(b4);

    b.read(b2); RowData[12][1] = toHex(b2); RowData[12][2] = toShort(b2);
    b.read(b2); RowData[13][1] = toHex(b2); RowData[13][2] = toShort(b2);
    b.read(b2); RowData[14][1] = toHex(b2); RowData[14][2] = toShort(b2);
    b.read(b2); RowData[15][1] = toHex(b2); RowData[15][2] = toShort(b2);
    b.read(b2); RowData[16][1] = toHex(b2); RowData[16][2] = toShort(b2);
    b.read(b2); RowData[17][1] = toHex(b2); RowData[17][2] = toShort(b2);

    b.read(b4); RowData[18][1] = toHex(b4); RowData[18][2] = toInt(b4);
    b.read(b4); RowData[19][1] = toHex(b4); RowData[19][2] = toInt(b4);
    b.read(b4); RowData[20][1] = toHex(b4); RowData[20][2] = toInt(b4);
    b.read(b4); RowData[21][1] = toHex(b4); RowData[21][2] = toInt(b4);

    b.read(b2); RowData[22][1] = toHex(b2);
    b.read(b2); RowData[23][1] = toHex(b2); RowData[23][2] = toShort(b2);

    //64 bit.

    if(is64bit)
    {
      b.read(b8); RowData[24][1] = toHex(b8); RowData[24][2] = toLong(b8);
      b.read(b8); RowData[25][1] = toHex(b8); RowData[25][2] = toLong(b8);
      b.read(b8); RowData[26][1] = toHex(b8); RowData[26][2] = toLong(b8);
      b.read(b8); RowData[27][1] = toHex(b8); RowData[27][2] = toLong(b8);
    }

    //32 bit.

    else
    {
      b.read(b4); RowData[24][1] = toHex(b4); RowData[24][2] = toInt(b4);
      b.read(b4); RowData[25][1] = toHex(b4); RowData[25][2] = toInt(b4);
      b.read(b4); RowData[26][1] = toHex(b4); RowData[26][2] = toInt(b4);
      b.read(b4); RowData[27][1] = toHex(b4); RowData[27][2] = toInt(b4);
    }

    b.read(b4); RowData[28][1] = toHex(b4); RowData[28][2] = toInt(b4);
    b.read(b4); DDS = toInt(b4); RowData[29][1] = toHex(b4); RowData[29][2] = DDS;
  
    //return the output

    JTable T = new JTable( RowData,columnNames );

    DDS *= 3;

    //If op header was read properly.

    if( OPS.equals("0B 01 ") || is64bit ) { return(T); }
  
    //Else error.

    return( new JTable( ( new Object[][]{ {"ERROR READING OP Header"} } ), ( new Object[]{"ERR"} ) ) );
  }

  //************************************************READ Data Directory Array********************************************
  //Each section is given in virtual address position if used. Sections that are not used have a virtual address of 0.
  //The next header defines the sections that are to be read and placed in ram memory.

  public JTable ReadDataDrectory(RandomAccessFileV b) throws IOException
  {
    //names of the data array locations

    String[] Types=new String[] {"Export DLL FUNCTIONS Location",
      "Import DLL FUNCTIONS Location",
      "Resource Location to Files In DLL or EXE",
      "Exceptions",
      "Security",
      "Relocation used for patching",
      "Debug",
      "Description/Architecture",
      "Machine Value (MIPS GP)",
      "Thread Storage",
      "Load Configuration",
      "Bound Import DLL Function Inside EXE",
      "Import Address Table",
      "Delayed Imports",
      "COM Runtime Descriptor",
      "USED BY MS DOS EXE DLL SYS Loader"};

    DataDir = new long[ ( ( DDS / 3 ) * 2 ) ]; DataDirUsed = new boolean[ ( DDS / 3 ) ];

    //create the table logarithmically to data array size.

    Object RowData[][] = new Object[ DDS ][ 3 ];

    for( int i=0, i2=0, i3=0; i < DDS; i+=3, i2+=2, i3++ )
    {
      b.read(b4); DataDir[i2] = toInt(b4);

      RowData[i][0] = "Array Element " + ( i / 3 ) + "";

      if( ( i / 3 ) < Types.length )
      {
        RowData[i][1] = Types[ ( i / 3 ) ];
      }
      else { RowData[i][1] = "Unknown use"; }

      RowData[ i + 1 ][ 0 ] = "Virtual Address"; RowData[ i + 1 ][ 1 ] = toHex(b4); RowData[ i + 1 ][ 2 ] = DataDir[ i2 ];
      
      b.read(b4);
      
      DataDir[ i2 + 1 ] = toInt(b4);
      
      RowData[ i + 2 ][ 0 ] = "Size"; RowData[ i + 2 ][ 1 ] = toHex( b4 ); RowData[ i + 2 ][ 2 ] = DataDir[ i2 + 1 ];

      DataDirUsed[ i3 ] = ( DataDir[ i2 ] > 0 ) && ( DataDir[ i2 + 1 ] > 0 );
    }

    JTable T=new JTable(RowData,new Object[]{"Usage","Hex","Dec"});

    return(T);
  }

  //****************************************Read the Mapped Sections of executable, or dll*******************************************
  //There are always 4 sections. Without this the virtual addresses of each section in DataDrectory is useless.

  public JTable ReadSections(RandomAccessFileV b) throws IOException
  {
    long v1=0, v2=0, v3=0, v4=0;
    byte[] bd = new byte[12];

    Object RowData[][] = new Object[ ( NOS * 7 ) ][ 3 ];

    for( int i = 0, i2 = 0; i < ( NOS * 7 ); i += 7, i2 += 4 )
    {
      //Section name.
      
      b.read(b8); RowData[i][0] = "Section Name"; RowData[i][1] = "ASCII 8 Bytes"; RowData[i][2] = toText( b8 );

      //Virtual address.

      b.read(b4); RowData[i+1][0] = "Section Size Loaded In Ram"; RowData[i+1][1] = "DWORD"; v1 = toInt(b4); RowData[i+1][2] = v1;

      b.read(b4); RowData[i+2][0] = "Where to Store Bytes in Ram"; RowData[i+2][1] = "DWORD"; v2 = toInt(b4); RowData[i+2][2] = v2;

      b.read(b4); RowData[i+3][0] = "Byte length to read from EXE file"; RowData[i+3][1] = "DWORD"; v3 = toInt(b4); RowData[i+3][2] = v3;

      b.read(b4); RowData[i+4][0] = "Position to Start Reading EXE"; RowData[i+4][1] = "DWORD"; v4 = toInt(b4); RowData[i+4][2] = v4;

      //Reserved section.

      b.read(bd); RowData[i+5][0] = "Reserved 12 bytes"; RowData[i+5][1] = "HEX"; RowData[i+5][2] = toHex(bd);

      //Section FLAGS.

      b.read(b4); RowData[i+6][0] = "Section flags"; RowData[i+6][1] = "FLAG HEX"; RowData[i+6][2] = toHex(b4);

      //Add virtual address to IO system.

      b.addV( v1, v2, v3, v4);
    }

    JTable T=new JTable(RowData,new Object[]{"Usage","Data Type","Decode"});

    return(T);
  }
}