package EXEDecode;
import javax.swing.*;
import java.io.*;
import javax.swing.tree.*;
import java.awt.*;
import RandomAccessFileV.*;

public class Headers extends Data
{
  //*********************************creates the data of the MZ header***********************************

  public javax.swing.table.DefaultTableModel readMZ(RandomAccessFileV b) throws IOException
  {
    javax.swing.table.DefaultTableModel mzData = new javax.swing.table.DefaultTableModel();

    mzData.addColumn("Usage"); mzData.addColumn("Hex"); mzData.addColumn("Dec");

    b.read(b2); String MZ = toHex( b2 ); mzData.addRow( new Object[]{ "SIGNATRUE", MZ, toText( b2 ) } );
    b.read(b2); mzData.addRow( new Object[]{ "Size of Last Page", toHex(b2), Short.toUnsignedInt( toShort(b2) ) + "" } );
    b.read(b2); mzData.addRow( new Object[]{ "Number of 512 byte pages in file", toHex(b2), Short.toUnsignedInt( toShort(b2) ) + "" } );
    b.read(b2); mzData.addRow( new Object[]{ "Number of Relocation Entries", toHex(b2), Short.toUnsignedInt( toShort(b2) ) + "" } );
    b.read(b2); mzData.addRow( new Object[]{ "Header size in Paragraphs", toHex(b2), Short.toUnsignedInt( toShort(b2) ) + "" } );
    b.read(b2); mzData.addRow( new Object[]{ "Minimum additional Memory required in paragraphs", toHex(b2), Short.toUnsignedInt( toShort(b2) ) + "" } );
    b.read(b2); mzData.addRow( new Object[]{ "Maximum additional Memory required in paragraphs", toHex(b2), Short.toUnsignedInt( toShort(b2) ) + "" } );
    b.read(b2); mzData.addRow( new Object[]{ "Initial SS relative to start of file", toHex(b2), Short.toUnsignedInt( toShort(b2) ) + "" } );
    b.read(b2); mzData.addRow( new Object[]{ "Initial SP", toHex(b2), Short.toUnsignedInt( toShort(b2) ) + "" } );
    b.read(b2); mzData.addRow( new Object[]{ "Checksum (unused)", toHex(b2), Short.toUnsignedInt( toShort(b2) ) + "" } );
    b.read(b2); mzData.addRow( new Object[]{ "Initial IP", toHex(b2), Short.toUnsignedInt( toShort(b2) ) + "" } );
    b.read(b2); mzData.addRow( new Object[]{ "Initial CS relative to start of file", toHex(b2), Short.toUnsignedInt( toShort(b2) ) + "" } );
    b.read(b2); mzData.addRow( new Object[]{ "Offset within Header of Relocation Table", toHex(b2), Short.toUnsignedInt( toShort(b2) ) + "" } );
    b.read(b2); mzData.addRow( new Object[]{ "Overlay Number", toHex(b2), Short.toUnsignedInt( toShort(b2) ) + "" } );
    b.read(b8); mzData.addRow( new Object[]{ "Reserved", toHex(b8), "" } );
    b.read(b2); mzData.addRow( new Object[]{ "ID", toHex(b2), Short.toUnsignedInt( toShort(b2) ) + "" } );
    b.read(b2); mzData.addRow( new Object[]{ "INFO", toHex(b2), Short.toUnsignedInt( toShort(b2) ) + "" } );
    byte[] bd = new byte[20]; b.read(bd); mzData.addRow( new Object[]{ "Reserved", toHex(bd), "" } );
    b.read(b4); PE = toInt(b4); mzData.addRow( new Object[]{ "PE Header Location", toHex(b4), PE + "" } );

    //The section before the PE header is the small MZ dos program.
    
    bd = new byte[ (int)( PE - 64 ) ]; b.read(bd); mzData.addRow( new Object[]{ "8086 16-bit", toHex(bd), "" } );
    
    //Return Table data.
    
    if( !MZ.equals("4D 5A ") ) { throw new IOException("Wong MZ SIGNATRUE."); }; return( mzData );
  }

  //*********************************creates the nicely styled data of the PE header***********************************

  public javax.swing.table.DefaultTableModel readPE(RandomAccessFileV b) throws IOException
  {
    javax.swing.table.DefaultTableModel peData = new javax.swing.table.DefaultTableModel();

    peData.addColumn( "Usage" ); peData.addColumn( "Hex" ); peData.addColumn( "Dec" );

    //data decode to table

    b.read(b4); String PES = toHex(b4); peData.addRow( new Object[]{ "SIGNATRUE", PES, "" } );
    b.read(b2); peData.addRow( new Object[]{ "Machine", toHex(b2), "" } );
    b.read(b2); NOS = toShort( b2 ); peData.addRow( new Object[]{ "Number Of Sections", toHex(b2), NOS + "" } );
    b.read(b4); peData.addRow( new Object[]{ "Time Date Stamp", toHex(b4), toInt(b4) + "" } );
    b.read(b4); peData.addRow( new Object[]{ "Pointer To Symbol Table", toHex(b4), toInt(b4) + "" } );
    b.read(b4); peData.addRow( new Object[]{ "Number Of Symbols", toHex(b4), toInt(b4) + "" } );
    b.read(b2); peData.addRow( new Object[]{ "Size Of OP Header", toHex(b2), toShort(b2) + "" } );
    b.read(b2); peData.addRow( new Object[]{ "Characteristics", toHex(b2), "" } );

    //Test if PE header was read correctly.

    if( !PES.equals("50 45 00 00 ") ) { throw new IOException("Wong PE SIGNATRUE."); }; return( peData );
  }

  //************************************************READ OP HEADER********************************************

  public javax.swing.table.DefaultTableModel readOP(RandomAccessFileV b) throws IOException
  {
    javax.swing.table.DefaultTableModel opData = new javax.swing.table.DefaultTableModel();

    opData.addColumn( "Usage" ); opData.addColumn( "Hex" ); opData.addColumn( "Dec" );

    //The OP header has different signature meanings.
    //OP = 0B 01 is a 32 bit program, and 0B 02 is a 64 bit one. Additional 01 07 is a ROM image.
    //Note I could compare numbers 267 for 32 bit, 523 for 64 bit, and 263 for a ROM image.

    b.read(b2); String OPS = toHex(b2); opData.addRow( new Object[]{ "SIGNATRUE", OPS, "" } );

    is64bit = OPS.equals("0B 02 ");

    b.read(b1); opData.addRow( new Object[]{ "Major Linker Version", toHex(b1), ((int)b1[0]) + "" } );
    b.read(b1); opData.addRow( new Object[]{ "Minor Linker Version", toHex(b1), ((int)b1[0]) + "" } );

    b.read(b4); sizeOfCode = toInt(b4); opData.addRow( new Object[]{ "Size Of Code", toHex(b4), sizeOfCode + "" } );
    b.read(b4); opData.addRow( new Object[]{ "Size Of Initialized Data", toHex(b4), toInt(b4) + "" } );
    b.read(b4); opData.addRow( new Object[]{ "Size Of Uninitialized Data", toHex(b4), toInt(b4) + "" } );
    b.read(b4); startOfCode = toInt(b4); opData.addRow( new Object[]{ "Start Of Code.", toHex(b4), startOfCode + "" } );
    b.read(b4); baseOfCode = toInt(b4); opData.addRow( new Object[]{ "Base Of Code", toHex(b4), baseOfCode + "" } );
    b.read(b4); opData.addRow( new Object[]{ "Base Of Data", toHex(b4), toInt(b4) + "" } );

    b.read(b4); opData.addRow( new Object[]{ "Image Base", toHex(b4), toInt(b4) + "" } );
    b.read(b4); opData.addRow( new Object[]{ "Section Alignment", toHex(b4), toInt(b4) + "" } );
    b.read(b4); opData.addRow( new Object[]{ "File Alignment", toHex(b4), toInt(b4) + "" } );

    b.read(b2); opData.addRow( new Object[]{ "Major Operating System Version", toHex(b2), toShort(b2) + "" } );
    b.read(b2); opData.addRow( new Object[]{ "Minor Operating System Version", toHex(b2), toShort(b2) + "" } );
    b.read(b2); opData.addRow( new Object[]{ "Major Image Version", toHex(b2), toShort(b2) + "" } );
    b.read(b2); opData.addRow( new Object[]{ "Minor Image Version", toHex(b2), toShort(b2) + "" } );
    b.read(b2); opData.addRow( new Object[]{ "Major Sub system Version", toHex(b2), toShort(b2) + "" } );
    b.read(b2); opData.addRow( new Object[]{ "Minor Sub system Version", toHex(b2), toShort(b2) + "" } );

    b.read(b4); opData.addRow( new Object[]{ "Win 32 Version Value", toHex(b4), toInt(b4) + "" } );
    b.read(b4); opData.addRow( new Object[]{ "Size Of Image", toHex(b4), toInt(b4) + "" } );
    b.read(b4); opData.addRow( new Object[]{ "Size Of Headers", toHex(b4), toInt(b4) + "" } );
    b.read(b4); opData.addRow( new Object[]{ "Check Sum", toHex(b4), toInt(b4) + "" } );

    b.read(b2); opData.addRow( new Object[]{ "Sub system", toHex(b2), "" } );
    b.read(b2); opData.addRow( new Object[]{ "Dll Characteristics", toHex(b2), toShort(b2) + "" } );

    //64 bit.

    if(is64bit)
    {
      b.read(b8); opData.addRow( new Object[]{ "Size Of Stack Reserve", toHex(b8), toLong(b8) + "" } );
      b.read(b8); opData.addRow( new Object[]{ "Size Of Stack Commit", toHex(b8), toLong(b8) + "" } );
      b.read(b8); opData.addRow( new Object[]{ "Size Of Heap Reserve", toHex(b8), toLong(b8) + "" } );
      b.read(b8); opData.addRow( new Object[]{ "Size Of Heap Commit", toHex(b8), toLong(b8) + "" } );
    }

    //32 bit.

    else
    {
      b.read(b4); opData.addRow( new Object[]{ "Size Of Stack Reserve", toHex(b4), toInt(b4) + "" } );
      b.read(b4); opData.addRow( new Object[]{ "Size Of Stack Commit", toHex(b4), toInt(b4) + "" } );
      b.read(b4); opData.addRow( new Object[]{ "Size Of Heap Reserve", toHex(b4), toInt(b4) + "" } );
      b.read(b4); opData.addRow( new Object[]{ "Size Of Heap Commit", toHex(b4), toInt(b4) + "" } );
    }

    b.read(b4); opData.addRow( new Object[]{ "Loader Flags", toHex(b4), toInt(b4) } );
    b.read(b4); DDS = toInt(b4); opData.addRow( new Object[]{ "Data Directory Array Size", toHex(b4), DDS } );

    DDS *= 3;

    //If op header was read properly.

    if( !OPS.equals("0B 01 ") && !is64bit ) { throw new IOException("Wong OP SIGNATRUE."); }; return( opData );
  }

  //************************************************READ Data Directory Array********************************************
  //Each section is given in virtual address position if used. Sections that are not used have a virtual address of 0.
  //The next header defines the sections that are to be read and placed in ram memory.

  public javax.swing.table.DefaultTableModel readDataDrectory(RandomAccessFileV b) throws IOException
  {
    //Names of the data array locations

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

    //Create table data.

    javax.swing.table.DefaultTableModel ddData = new javax.swing.table.DefaultTableModel();

    ddData.addColumn("Usage"); ddData.addColumn("Hex"); ddData.addColumn("Dec");

    //The Number of data Directory array sections.

    DataDir = new long[ ( ( DDS / 3 ) * 2 ) ]; DataDirUsed = new boolean[ ( DDS / 3 ) ];

    //Create the table data.

    for( int i=0, i2=0, i3=0; i < DDS; i+=3, i2+=2, i3++ )
    {
      ddData.addRow( new Object[]{ "Array Element " + ( i / 3 ) + "", ( ( i / 3 ) < Types.length ) ? Types[ ( i / 3 ) ] : "Unknown use", "" } );

      b.read(b4); DataDir[ i2 ] = toInt(b4); ddData.addRow( new Object[]{ "Virtual Address", toHex( b4 ), DataDir[ i2 ] + "" } );
      
      b.read(b4); DataDir[ i2 + 1 ] = toInt(b4); ddData.addRow( new Object[]{ "Size", toHex( b4 ), DataDir[ i2 + 1 ] + "" } );

      //Test if data Dir Is used.

      DataDirUsed[ i3 ] = ( DataDir[ i2 ] > 0 ) && ( DataDir[ i2 + 1 ] > 0 );
    }

    return(ddData);
  }

  //****************************************Read the Mapped Sections of executable, or dll*******************************************
  //The PE header defines the number of sections. Without this the virtual addresses of each section in DataDrectory is useless.

  public javax.swing.table.DefaultTableModel readSections(RandomAccessFileV b) throws IOException
  {
    byte[] bd = new byte[ 12 ]; long virtualSize = 0, virtualOffset = 0, size = 0, offset = 0;

    //Create table data.

    javax.swing.table.DefaultTableModel sData = new javax.swing.table.DefaultTableModel();

    sData.addColumn("Usage"); sData.addColumn("Data Type"); sData.addColumn("Decode");

    for( int i = 0, i2 = 0; i < ( NOS * 7 ); i += 7, i2 += 4 )
    {
      //Section name.
      
      b.read(b8); sData.addRow( new Object[]{ "Section Name", "ASCII 8 Bytes", toText( b8 ) } );

      //Virtual address.

      b.read(b4); virtualSize = toInt(b4); sData.addRow( new Object[]{ "Section Size Loaded In Ram", "DWORD", virtualSize + "" } );
      b.read(b4); virtualOffset = toInt(b4); sData.addRow( new Object[]{ "Where to Store Bytes in Ram", "DWORD", virtualOffset + "" } );
      b.read(b4); size = toInt(b4); sData.addRow( new Object[]{ "Byte length to read from EXE file", "DWORD", size + "" } );
      b.read(b4); offset = toInt(b4); sData.addRow( new Object[]{ "Position to Start Reading EXE", "DWORD", offset + "" } );

      //Reserved section.

      b.read(bd); sData.addRow( new Object[]{ "Reserved 12 bytes", "Hex", toHex(bd) } );

      //Section FLAGS.

      b.read(b4); sData.addRow( new Object[]{ "Section flags", "FLAG Hex", toHex(b4) } );

      //Add virtual address to IO system.

      b.addV( offset, size, virtualOffset, virtualSize );
    }

    return(sData);
  }
}