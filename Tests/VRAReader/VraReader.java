import java.io.*;

public class VraReader
{
//use some normal io functions

RandomAccessFile Data;

//vra mode off or on

boolean VRAM=false;

//if vra is set read useing the maped ram to disk array

public static long[] Map=new long[0];

//start of testing code

public static void main(String[]args){}

//**************************************Data Type Read Functions****************************************

//*******************************************Read Hex***************************************************

public String ReadHEX(int pos,int len){String out="",temp="";byte b2[]=read(pos,len);
for(int i=0;i<b2.length;i++){out+=B2H(b2[i])+" ";};return((out.toUpperCase()));}

//*******************************************Read Hex***************************************************

public String ReadHEX(long pos){return(null);}

//*****************************************Read A Dword*************************************************

public long ReadDWORD(int pos){long out=0;String temp="";byte b2[]=read(pos,4);
for(int i=(b2.length-1);i>=0;i--){temp+=B2H(b2[i]);};out=Long.parseLong(temp,16);
return(out);}

//******************************************Read A Word*************************************************

public int ReadWORD(int pos){byte b2[]=read(pos,2);String s1=B2H(b2[0]),s2=B2H(b2[1]);
int l=Integer.parseInt((s2+s1),16);return(l);}

//******************************************Read A Byte*************************************************

public short ReadBYTE(int pos){short l=(short)Integer.parseInt(B2H(read(pos,1)[0]),16);return(l);}

//******************************************ASCII 8 bit*************************************************

public String ReadASCII(int pos,int pos2){return(new String(read(pos,pos2)));}

//******************************************ASCII 8 bit*************************************************

public String ReadASCII(long pos){int l=0,b2=0;while(true){try{b2=read(((int)(pos+l)),1)[0];}
catch(Exception e){System.out.println(e+"");}if(((int)b2&0xFF)==0x00){break;}else{l+=1;}}
return(new String(read((int)pos,l)));}

//******************************************ASCII 16 bit************************************************

public String ReadASCIIUND(long pos){int UNDL=ReadWORD(((int)pos));byte[] b1=read(((int)pos+2),((int)UNDL*2));
byte[] b2=new byte[UNDL];for(int i=0,i2=0;i<UNDL;i++,i2+=2){b2[i]=b1[i2];};return(new String(b2));}

//****************************************byte to hex value*********************************************

public String B2H(byte b){String temp=Integer.toHexString(b);if(temp.length()<=1){temp="0"+temp;}if(temp.length()>2)
{temp=temp.substring(6,8);}return(temp);}

//************************************the file to use file io on****************************************

public VraReader(File f){try{Data=new RandomAccessFile(f,"r");}catch(Exception e){}}

//***************************************set VRA reading Mode*******************************************

public void SetVraMode(boolean b){VRAM=b;}

//*************************the read function for reading x amount of bytes******************************

public byte[] read(long pos,int length){byte[] b=new byte[length];try{if(VRAM){long end=pos+length;int r=0;long[] disk;
while(pos<end){disk=VraToPh(pos);if(disk[0]>0){Data.seek(disk[0]);Data.read(b,r,(int)disk[1]);};r+=disk[1];pos+=disk[1];}}
else{Data.seek(pos);Data.read(b,0,b.length);}}catch(Exception e){};return(b);}

//************************used to check if the reading boundarys are corect*****************************

public void DebugMap(){long[] l=new long[]{1,1};long pos=0;while(l[0]>0|l[1]>0){l=VraToPh(pos);
if(l[0]>0|l[1]>0){System.out.println("Ram Address="+pos+"");System.out.println("disk pos="+l[0]+"");
System.out.println("end pos="+l[1]+"\r\n");pos+=l[1];try{System.in.read();}catch(Exception e){}}}}

//*****************************************Reset the Map************************************************

public void ResetVraReader(){Map=new long[0];}

//************************************set up the ram addresses******************************************

public void AddVraPos(long DumpSize,long RamPos,long ReadSize,long DiskPos){long[] l=new long[Map.length+4];
for(int i=0;i<Map.length;l[i]=Map[i],i++);l[Map.length]=DumpSize;l[Map.length+1]=RamPos;l[Map.length+2]=ReadSize;
l[Map.length+3]=DiskPos;Map=l;}

//******************************where the start and end of the VRA is***********************************

public long[] VraToPh(long VRA){int el1=(Map.length-4),el2=0;
long NextVra=0,l1=0x7FFFFFFFFFFFFFFFL,end=0,StartPos=VRA,EndPos=0,ClostsPos=0;
for(;el1>-1;el1-=4){end=Map[el1]+Map[el1+1];if(Map[el1+1]<=VRA&VRA<end){break;}}
end=0;for(int i=(Map.length-4);i>el1;i-=4){if((Map[i+1]>=VRA)&(Map[i+1]<l1)){l1=Map[i+1];el2=i;}};ClostsPos=Map[el2+1];
if(el1>=0){StartPos=Map[el1+1];EndPos=(StartPos+Map[el1+2]);}else{return(new long[]{-1,ClostsPos-VRA});}
if(l1!=0x7FFFFFFFFFFFFFFFL){if(ClostsPos>=StartPos&ClostsPos<=EndPos){EndPos=ClostsPos;NextVra=Map[el2+1];}
else{l1=0x7FFFFFFFFFFFFFFFL;}}if(l1==0x7FFFFFFFFFFFFFFFL){el2=Map.length-4;NextVra=VRA+(end-VRA);end=0;
for(;el2>-1;el2-=4){end=Map[el2]+Map[el2+1];if(Map[el2+1]<=NextVra&NextVra<end){l1=0;break;}}}
StartPos=((VRA-StartPos)+Map[el1+3]);EndPos=(EndPos-VRA);if(EndPos<=0){StartPos=-1;EndPos=(NextVra-VRA);
if(EndPos<0){EndPos=(Map[el1+1]+Map[el1])-VRA;}if(EndPos<0){EndPos=-1;}};return(new long[]{StartPos,EndPos});}

}