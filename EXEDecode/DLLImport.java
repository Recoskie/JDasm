package EXEDecode;
import javax.swing.*;
import java.io.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.Vector;

public class DLLImport extends Data
{

public DefaultMutableTreeNode LoadDLLImport(DefaultMutableTreeNode IMPORT,VraReader b)
{

//get the phisical address to data drectory array links to dll import table

int pos2=((int)DataDir[2]);

System.out.println("DLL RVA ARRAY POSITION "+pos2+"");

Vector<JTable> v1=new Vector<JTable>();

//for dll names

Vector<String> v2=new Vector<String>();

Vector<Object> v4=new Vector<Object>();

IMPORT.add(new DefaultMutableTreeNode("DLL IMPORT ARRAY DECODE.H"));

String end="";int DLLS=0,ref=1;

JTable T[];

while(true)
{try{end=b.ReadHEX(pos2,20);}catch(Exception e){System.out.println(e+"");}
if(end.equals("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ")){break;}
else{

v4.add(new Object[]{"","",""});
v4.add(new Object[]{"","",""});
v4.add(new Object[]{"","",""});
v4.add(new Object[]{"","",""});
v4.add(new Object[]{"","",""});

//unkowen resion whiy i have to do this to get it to read from lowcation corectly

//dec

long ArrayDataDec[]=new long[]{b.ReadDWORD((int)pos2),b.ReadDWORD((int)(pos2+4)),
b.ReadDWORD((int)(pos2+8)),b.ReadDWORD((int)(pos2+12)),b.ReadDWORD((int)(pos2+16))};

//hex

String ArrayDataHex[]=new String[]{b.ReadHEX((int)pos2,4),b.ReadHEX((int)(pos2+4),4),
b.ReadHEX((int)(pos2+8),4),b.ReadHEX((int)(pos2+12),4),b.ReadHEX((int)(pos2+16),4)};

((Object[])v4.get(DLLS))[0]="Original Array DLL Load Functions";
((Object[])v4.get(DLLS))[1]=ArrayDataHex[0];
((Object[])v4.get(DLLS))[2]=ArrayDataDec[0];

((Object[])v4.get(DLLS+1))[0]="Time Date Stamp";
((Object[])v4.get(DLLS+1))[1]=ArrayDataHex[1];
((Object[])v4.get(DLLS+1))[2]=ArrayDataDec[1];

((Object[])v4.get(DLLS+2))[0]="Forwarder Chain";
((Object[])v4.get(DLLS+2))[1]=ArrayDataHex[2];
((Object[])v4.get(DLLS+2))[2]=ArrayDataDec[2];

((Object[])v4.get(DLLS+3))[0]="DLL Name Ram Lowcation";
((Object[])v4.get(DLLS+3))[1]=ArrayDataHex[3];
((Object[])v4.get(DLLS+3))[2]=ArrayDataDec[3];

((Object[])v4.get(DLLS+4))[0]="First Array DLL Load Functions";
((Object[])v4.get(DLLS+4))[1]=ArrayDataHex[4];
((Object[])v4.get(DLLS+4))[2]=ArrayDataDec[4];

//decode to phisical addresses
int OriginalFirst=(int)ArrayDataDec[0];

T=ThunkArrayDecode(OriginalFirst,b);
v1.add(T[0]);v1.add(T[1]);

int namepos2=(int)ArrayDataDec[3];

int First=(int)ArrayDataDec[4];

T=ThunkArrayDecode(First,b);
v1.add(T[0]);v1.add(T[1]);

v2.add(b.ReadASCII(namepos2));

/*System.out.println("position VRA Name "+ArrayDataDec[3]+"");
System.out.println("File position Name "+namepos2+"");
System.out.println("DLL Name "+b.ReadASCII(namepos2));*/

DefaultMutableTreeNode I=new DefaultMutableTreeNode(b.ReadASCII(namepos2));
I.add(new DefaultMutableTreeNode("First Original Array Decode.H#D"+(ref)+""));
I.add(new DefaultMutableTreeNode("Secont Original Array Decode.H#D"+(ref+2)));
I.add(new DefaultMutableTreeNode("First Original Array Lowcation Of Function Names.dll#D"+(ref+1)+""));
I.add(new DefaultMutableTreeNode("Secont Original Array Lowcation Of Function Names.dll#D"+(ref+3)+""));

IMPORT.add(I);

pos2+=20;DLLS+=5;ref+=4;}}

//convert vector into the row data of the table of the decode of this array of the dll
Object RowData[][]=new Object[v4.size()][3];

for(int i=0;i<v4.size();i++)
{RowData[i][0]=((Object[])v4.get(i))[0];
RowData[i][1]=((Object[])v4.get(i))[1];
RowData[i][2]=((Object[])v4.get(i))[2];}

v4.clear();

v1.add(0,new JTable(RowData,new Object[]{"Useage","hex","dec"}));

DLLTable=new JTable[v1.size()];

for(int i=0;i<v1.size();i++){DLLTable[i]=v1.get(i);}

//create the dll name array
DLLName=new String[v2.size()];
for(int i=0;i<v2.size();i++){DLLName[i]=v2.get(i);}

v2.clear();

v1.clear();

return(IMPORT);}


public JTable[] ThunkArrayDecode(long Lowcation,VraReader b)
{
Vector<Object> v1=new Vector<Object>();
Vector<Object> v2=new Vector<Object>();

String hex="";long function=0;

while(true){hex=b.ReadHEX((int)Lowcation,4);

if(hex.equals("00 00 00 00 ")){break;}
else{v1.add(new Object[]{"","",""});
function=b.ReadDWORD((int)Lowcation);
((Object[])v1.get((v1.size()-1)))[0]="Lowcation to ASCII Function Import Name";
((Object[])v1.get((v1.size()-1)))[1]=hex;
((Object[])v1.get((v1.size()-1)))[2]=function;

v2.add(new Object[]{"","",""});

((Object[])v2.get((v2.size()-1)))[0]="Import Function ID value";
((Object[])v2.get((v2.size()-1)))[1]="WORD";


//System.out.println("Read VRA Position "+function+"");


((Object[])v2.get((v2.size()-1)))[2]=b.ReadWORD((int)function);
function+=2;

v2.add(new Object[]{"","",""});

((Object[])v2.get((v2.size()-1)))[0]="Import Function Name";
((Object[])v2.get((v2.size()-1)))[1]="ASCII";
((Object[])v2.get((v2.size()-1)))[2]=b.ReadASCII((int)function)+"()";

//System.out.println(b.ReadASCII((int)function)+"()");

Lowcation+=4;
}}

//convert to Object arrays for JTable
Object RowData1[][]=new Object[v1.size()][3];

for(int i=0;i<v1.size();i++)
{RowData1[i][0]=((Object[])v1.get(i))[0];
RowData1[i][1]=((Object[])v1.get(i))[1];
RowData1[i][2]=((Object[])v1.get(i))[2];}

Object RowData2[][]=new Object[v2.size()][3];

for(int i=0;i<v2.size();i++)
{RowData2[i][0]=((Object[])v2.get(i))[0];
RowData2[i][1]=((Object[])v2.get(i))[1];
RowData2[i][2]=((Object[])v2.get(i))[2];}

JTable t[]=new JTable[]{new JTable(RowData1,new Object[]{"Useage","hex","dec"}),new JTable(RowData2,new Object[]{"Useage","Data Type","Output"})};

v1.clear();v2.clear();

return(t);
}
}