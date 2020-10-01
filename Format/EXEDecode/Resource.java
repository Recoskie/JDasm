package Format.EXEDecode;
import javax.swing.*;
import java.io.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.*;

public class Resource extends Data
{
java.util.Vector<String> Files=new java.util.Vector<String>();
java.util.Vector<String> Folders=new java.util.Vector<String>();
boolean exist=false;

public DefaultMutableTreeNode Decode(DefaultMutableTreeNode RE,VraReader b)
{
Folders.add("$0");

while(Folders.size()>0){ScanDIR(Folders.get(0),b);Folders.remove(0);}

FileTree(RE);

return(RE);

}

public void ScanDIR(String in,VraReader b)
{
long l=0x80000000L;
long E1=0,E2=0;

int BASE=(int)DataDir[4];
int POS=0;

String[] fix=new String[2];
fix[0]=in.substring(0,in.indexOf("$"));
fix[1]=in.substring((in.indexOf("$")+1),in.length());
in=fix[0];
POS=Integer.parseInt(fix[1],10);
fix=null;

System.out.println("Scan DIR = "+in+"");
System.out.println("Drectory Position IN RAM = "+(BASE+POS)+"");

Object[] Folder=new Object[6];
Folder[0]=b.ReadDWORD((BASE+POS));
Folder[1]=b.ReadDWORD((BASE+(POS+4)));
Folder[2]=b.ReadWORD((BASE+(POS+8)));
Folder[3]=b.ReadWORD((BASE+(POS+10)));
Folder[4]=b.ReadWORD((BASE+(POS+12)));
Folder[5]=b.ReadWORD((BASE+(POS+14)));
int Enterys=(((int)b.ReadWORD((BASE+(POS+12))))+((int)b.ReadWORD((BASE+(POS+14)))));

System.out.println("Drectory = Files/Folders = "+Enterys);POS+=16;

try{for(int i=0;i<Enterys;i++){E1=b.ReadDWORD((BASE+POS));E2=b.ReadDWORD((BASE+(POS+4)));POS+=8;

//****************************String Name****************************
if((E1-l)>0)
{
//Folder
if((E2-l)>0)
{String Name=b.ReadASCIIUND(((int)(BASE+(E1-l))));
Folders.add(in+Name+"/$"+(E2-l)+"");}

//File
else{String Name=b.ReadASCIIUND(((int)(BASE+(E1-l))));
Files.add(in+Name+".h#R"+E2+"");}
}

//******************************ID Name******************************
else
{
//Folder
if((E2-l)>0)
{Folders.add(in+E1+"/$"+(E2-l)+"");}

//File
else{Files.add(in+E1+".h#R"+E2+"");}
}
}
}catch(Exception e){System.out.println(e+"");}
}

//Decode the Files Array into Folders And Files Leading to the Files

public void FileTree(DefaultMutableTreeNode R)
{for(int i=0;i<Files.size();i++){AddFile(Files.get(i),R);}
Folders=null;}

public DefaultMutableTreeNode DirMatch(String folder,DefaultMutableTreeNode temp)
{exist=false;DefaultMutableTreeNode nobe=new DefaultMutableTreeNode("");
Enumeration e=temp.children();while(e.hasMoreElements())
{nobe=((DefaultMutableTreeNode)e.nextElement());
if((nobe+"").equals(folder)){exist=true;break;}}
if(!exist){nobe=temp.getLastLeaf();if((nobe+"").equals(folder)){exist=true;}}return(nobe);}

public void AddFile(String d2,DefaultMutableTreeNode d)
{DefaultMutableTreeNode temp1=d;DefaultMutableTreeNode temp2=d;
String[] D=d2.split("/");for(int i=0;i<D.length;i++)
{temp2=DirMatch(D[i],temp1);if(exist){temp1=temp2;}
else{temp1.add(new DefaultMutableTreeNode(D[i]));i--;}}}

public void ExtractFile(int pos,String Name,VraReader b)
{
int v=JOptionPane.showConfirmDialog(null,"Extract File "+Name,"Resouce Decoder",JOptionPane.YES_NO_OPTION);

if(v==JOptionPane.YES_OPTION)
{
try{int BASE=(int)DataDir[4];
Object[] FileH=new Object[4];
long Pos=b.ReadDWORD((BASE+pos));
long Size=b.ReadDWORD(((BASE+pos)+4));
long l=0x80000000L;

if(Pos>=l){Pos-=l;//Pos+=BASE;
System.out.println("RVA POS = "+Pos+"");}

File outFile=new File(Name+".txt");
BufferedOutputStream out=new BufferedOutputStream(new FileOutputStream(outFile));

//read(long pos,int length)

try{out.write(b.read(Pos,((int)Size)));}catch(Exception e){System.out.println("EX 5");}

System.out.println("Finished");out.close();}
catch(Exception e){System.out.println(e+"");}

}

}

public void ExtractFileAsIcon(int pos,String Name,VraReader b)
{
int v=JOptionPane.showConfirmDialog(null,"Extract File "+Name,"Resouce Decoder",JOptionPane.YES_NO_OPTION);

if(v==JOptionPane.YES_OPTION)
{
try{int BASE=(int)DataDir[4];
Object[] FileH=new Object[4];
long Pos=b.ReadDWORD((BASE+pos));
long Size=b.ReadDWORD(((BASE+pos)+4));
long l=0x80000000L;

if(Pos>=l){Pos-=l;//Pos+=BASE;
System.out.println("RVA POS = "+Pos+"");}

byte[] b2=new byte[]{0x00,0x00,0x01,0x00,0x01,0x00,0x30,0x30,0x00,0x00,0x01,0x00,0x00,0x00,(byte)(Size&0xFF),(byte)((Size>>8)&0xFF),(byte)((Size>>16)&0xFF),(byte)(Size>>24),0x16,0x00,0x00,0x00};

File outFile=new File(Name+".ico");
BufferedOutputStream out=new BufferedOutputStream(new FileOutputStream(outFile));
out.write(b2);

try{out.write(b.read(Pos,((int)Size)));}catch(Exception e){System.out.println("EX 5");}

System.out.println("Finished");out.close();}
catch(Exception e){System.out.println(e+"");}

}

}

}