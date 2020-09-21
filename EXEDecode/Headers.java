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

    byte[] bd = new byte[2]; b.read(bd);

    String MZ = toHex( bd ); rowData[0][1] = MZ;

    //String.format( "%1$02X", ); Convert number to hex sting.

    b.read(bd); rowData[1][1] = toHex(bd); rowData[1][2] = Short.toUnsignedInt( toShort(bd) ) + "";
    b.read(bd); rowData[2][1] = toHex(bd); rowData[2][2] = Short.toUnsignedInt( toShort(bd) ) + "";
    b.read(bd); rowData[3][1] = toHex(bd); rowData[3][2] = Short.toUnsignedInt( toShort(bd) ) + "";
    b.read(bd); rowData[4][1] = toHex(bd); rowData[4][2] = Short.toUnsignedInt( toShort(bd) ) + "";
    b.read(bd); rowData[5][1] = toHex(bd); rowData[5][2] = Short.toUnsignedInt( toShort(bd) ) + "";
    b.read(bd); rowData[6][1] = toHex(bd); rowData[6][2] = Short.toUnsignedInt( toShort(bd) ) + "";
    b.read(bd); rowData[7][1] = toHex(bd); rowData[7][2] = Short.toUnsignedInt( toShort(bd) ) + "";
    b.read(bd); rowData[8][1] = toHex(bd); rowData[8][2] = Short.toUnsignedInt( toShort(bd) ) + "";
    b.read(bd); rowData[9][1] = toHex(bd); rowData[9][2] = Short.toUnsignedInt( toShort(bd) ) + "";
    b.read(bd); rowData[10][1] = toHex(bd); rowData[10][2] = Short.toUnsignedInt( toShort(bd) ) + "";
    b.read(bd); rowData[11][1] = toHex(bd); rowData[11][2] = Short.toUnsignedInt( toShort(bd) ) + "";
    b.read(bd); rowData[12][1] = toHex(bd); rowData[12][2] = Short.toUnsignedInt( toShort(bd) ) + "";
    b.read(bd); rowData[13][1] = toHex(bd); rowData[13][2] = Short.toUnsignedInt( toShort(bd) ) + "";

    bd = new byte[8]; b.read(bd); rowData[14][1] = toHex(bd);
    
    bd = new byte[2]; b.read(bd); rowData[15][1] = toHex(bd); rowData[15][2] = Short.toUnsignedInt( toShort(bd) ) + "";

    b.read(bd); rowData[16][1] = toHex(bd); rowData[16][2] = Short.toUnsignedInt( toShort(bd) ) + "";

    bd = new byte[20]; b.read(bd); rowData[17][1] = toHex(bd);

    //Location to the PE header.

    bd = new byte[4]; b.read(bd);
    
    rowData[18][1] = toHex(bd); PE = toInt(bd); rowData[18][2] = PE + "";

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

    byte[] bd = new byte[4]; b.read(bd); String PES = toHex(bd); RowData[0][1] = PES;
  
    bd = new byte[2]; b.read(bd); RowData[1][1] = toHex( bd );
    b.read(bd); RowData[2][1] = toHex(bd); NOS = toShort( bd ); RowData[2][2] = NOS + "";
    bd = new byte[4]; b.read(bd); RowData[3][1] = toHex( bd ); RowData[3][2] = toInt(bd) + "";
    b.read(bd); RowData[4][1] = toHex( bd ); RowData[4][2] = toInt(bd) + "";
    b.read(bd); RowData[5][1] = toHex( bd ); RowData[5][2] = toInt(bd) + "";
    bd = new byte[2]; b.read(bd); RowData[6][1] = toHex( bd ); RowData[6][2] = toShort(bd) + "";
    b.read(bd); RowData[7][1] = toHex( bd );

    //return the output

    JTable T=new JTable(RowData,columnNames);

    //Test if PE header was read correctly.

    if( PES.equals("50 45 00 00 ") ) { return(T); }
    
    //Else error.

    return( new JTable( ( new Object[][]{ {"ERROR READING PE Header"} } ), ( new Object[]{"ERR"} ) ) );
  }

//************************************************READ OP HEADER********************************************

/*public JTable ReadOP(RandomAccessFileV b)
{
PE+=24;
String OPS=b.ReadHEX((int)PE,2);

Object RowData[][]={
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
{"Data Drectory Array Size","",""},     //4
};
Object columnNames[]={"Useage","Hex","Dec"};

//data decode to table

DDS=(int)b.ReadDWORD((int)PE+92);

System.out.println("Data Drector Array Size = "+DDS+"");

RowData[0][1]=OPS;

RowData[1][1]=b.ReadHEX((int)PE+2,1);RowData[1][2]=b.ReadBYTE((int)PE+2);
RowData[2][1]=b.ReadHEX((int)PE+3,1);RowData[2][2]=b.ReadBYTE((int)PE+3);

RowData[3][1]=b.ReadHEX((int)PE+4,4);RowData[3][2]=b.ReadDWORD((int)PE+4);
RowData[4][1]=b.ReadHEX((int)PE+8,4);RowData[4][2]=b.ReadDWORD((int)PE+8);
RowData[5][1]=b.ReadHEX((int)PE+12,4);RowData[5][2]=b.ReadDWORD((int)PE+12);
RowData[6][1]=b.ReadHEX((int)PE+16,4);RowData[6][2]=b.ReadDWORD((int)PE+16);
RowData[7][1]=b.ReadHEX((int)PE+20,4);RowData[7][2]=b.ReadDWORD((int)PE+20);
RowData[8][1]=b.ReadHEX((int)PE+24,4);RowData[8][2]=b.ReadDWORD((int)PE+24);

RowData[9][1]=b.ReadHEX((int)PE+28,4);RowData[9][2]=b.ReadDWORD((int)PE+28);
RowData[10][1]=b.ReadHEX((int)PE+32,4);RowData[10][2]=b.ReadDWORD((int)PE+32);
RowData[11][1]=b.ReadHEX((int)PE+36,4);RowData[11][2]=b.ReadDWORD((int)PE+36);

RowData[12][1]=b.ReadHEX((int)PE+40,2);RowData[12][2]=b.ReadWORD((int)PE+40);
RowData[13][1]=b.ReadHEX((int)PE+42,2);RowData[13][2]=b.ReadWORD((int)PE+42);
RowData[14][1]=b.ReadHEX((int)PE+44,2);RowData[14][2]=b.ReadWORD((int)PE+44);
RowData[15][1]=b.ReadHEX((int)PE+46,2);RowData[15][2]=b.ReadWORD((int)PE+46);
RowData[16][1]=b.ReadHEX((int)PE+48,2);RowData[16][2]=b.ReadWORD((int)PE+48);
RowData[17][1]=b.ReadHEX((int)PE+50,2);RowData[17][2]=b.ReadWORD((int)PE+50);

RowData[18][1]=b.ReadHEX((int)PE+52,4);RowData[18][2]=b.ReadDWORD((int)PE+52);
RowData[19][1]=b.ReadHEX((int)PE+56,4);RowData[19][2]=b.ReadDWORD((int)PE+56);
RowData[20][1]=b.ReadHEX((int)PE+60,4);RowData[20][2]=b.ReadDWORD((int)PE+60);
RowData[21][1]=b.ReadHEX((int)PE+64,4);RowData[21][2]=b.ReadDWORD((int)PE+64);

RowData[22][1]=b.ReadHEX((int)PE+68,2);
RowData[23][1]=b.ReadHEX((int)PE+70,2);RowData[23][2]=b.ReadWORD((int)PE+70);

RowData[24][1]=b.ReadHEX((int)PE+72,4);RowData[24][2]=b.ReadDWORD((int)PE+72);
RowData[25][1]=b.ReadHEX((int)PE+76,4);RowData[25][2]=b.ReadDWORD((int)PE+76);
RowData[26][1]=b.ReadHEX((int)PE+80,4);RowData[26][2]=b.ReadDWORD((int)PE+80);
RowData[27][1]=b.ReadHEX((int)PE+84,4);RowData[27][2]=b.ReadDWORD((int)PE+84);
RowData[28][1]=b.ReadHEX((int)PE+88,4);RowData[28][2]=b.ReadDWORD((int)PE+88);
RowData[29][1]=b.ReadHEX((int)PE+92,4);RowData[29][2]=DDS;
//return the output

JTable T=new JTable(RowData,columnNames);

DDS*=3;

if(OPS.equals("0B 01 ")){return(T);}return(new JTable((new Object[][]{{"ERROR READING OP Header"}}),(new Object[]{"ERR"})));}

//************************************************READ Data Drectory Array********************************************

public JTable ReadDataDrectory(RandomAccessFileV b)
{
PE+=96;

System.out.println("Loading Data Drectory Array pos = "+PE+"");

//names of the data array lowcations

String[] Types=new String[]
{"Export DLL FUNCTIONS Lowcation",
"Import DLL FUNCTIONS Lowcation",
"Resource Lowcation to Files In DLL or EXE",
"Exceptions",
"Security",
"Relocation used for patching",
"Debug",
"Decription/Architecture",
"Machine Value (MIPS GP)",
"Thread Storage",
"Load Configuration",
"Bound Import DLL Function Inside EXE",
"Import Address Table",
"Delayed Imports",
"COM Runtime Descriptor",
"USED BY MS DOS EXE DLL SYS Loader"};

DataDir=new long[((DDS/3)*2)];
DataDirUsed=new boolean[(DDS/3)];

//create the table algarithamacly to data array size

Object RowData[][]=new Object[DDS][3];

for(int i=0,i2=0,i3=0;i<DDS;i+=3,i2+=2,i3++)
{
DataDir[i2]=b.ReadDWORD((int)PE);

RowData[i][0]="Array Element "+(i/3)+"";

if((i/3)<Types.length){RowData[i][1]=Types[(i/3)];}
else{RowData[i][1]="Unkowen use";}

RowData[i+1][0]="Virtual Address";
RowData[i+1][1]=b.ReadHEX((int)PE,4);
RowData[i+1][2]=DataDir[i2];PE+=4;
DataDir[i2+1]=b.ReadDWORD((int)PE);
RowData[i+2][0]="Size";
RowData[i+2][1]=b.ReadHEX((int)PE,4);
RowData[i+2][2]=DataDir[i2+1];PE+=4;

DataDirUsed[i3]=(DataDir[i2]!=0)&(DataDir[i2+1]!=0);}

JTable T=new JTable(RowData,new Object[]{"Useage","Hex","Dec"});

return(T);}

//****************************************Read the Maped Sections of executable or dll*******************************************

public JTable ReadSections(RandomAccessFileV b)
{
System.out.println("Reading Section Dump Pos = "+PE+"");

long v1=0,v2=0,v3=0,v4=0;

Object RowData[][]=new Object[(NOS*7)][3];

for(int i=0,i2=0;i<(NOS*7);i+=7,i2+=4)
{RowData[i][0]="Section Name";
RowData[i][1]="ASCII 8 Bytes";
RowData[i][2]=b.ReadASCII((int)PE,8);

System.out.println("Section Nane = "+b.ReadASCII((int)PE,8));

PE+=8;

RowData[i+1][0]="Section Size Loaded In Ram";
RowData[i+1][1]="DWORD";

v1=b.ReadDWORD((int)PE);
RowData[i+1][2]=v1;PE+=4;

RowData[i+2][0]="Where to Store Bytes in Ram";
RowData[i+2][1]="DWORD";

System.out.println("Ram Address position = "+b.ReadDWORD((int)PE)+"");

v2=b.ReadDWORD((int)PE);
RowData[i+2][2]=v2;PE+=4;

RowData[i+3][0]="Byte length to read from EXE file";
RowData[i+3][1]="DWORD";

v3=b.ReadDWORD((int)PE);
RowData[i+3][2]=v3;PE+=4;

RowData[i+4][0]="Position to Start Reading EXE";
RowData[i+4][1]="DWORD";

System.out.println("Exe Disk Position = "+b.ReadDWORD((int)PE));

v4=b.ReadDWORD((int)PE);
RowData[i+4][2]=v4;PE+=4;

RowData[i+5][0]="Reserved 12 bytes";
RowData[i+5][1]="HEX";
RowData[i+5][2]=b.ReadHEX((int)PE,12);PE+=12;
RowData[i+6][0]="Section flags";
RowData[i+6][1]="FLAG HEX";
RowData[i+6][2]=b.ReadHEX((int)PE,4);PE+=4;

System.out.println("RVA Section Start "+v2+"");
System.out.println("RVA Section End "+(v1+v2)+"");
System.out.println("Disk Reed length "+v1+"");

b.AddVraPos(v1,v2,v3,v4);}

JTable T=new JTable(RowData,new Object[]{"Useage","Data Type","Decode"});

return(T);}*/

}