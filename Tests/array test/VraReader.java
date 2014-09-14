import java.io.*;

public class VraReader
{
//if vra is set read useing the maped ram to disk array

public static long[] Map=new long[0];

//start of testing code

public static void main(String[]args) throws Exception
{
AddVraPos(400,10,400,0);
AddVraPos(170,150,200,0);
AddVraPos(150,403,700,0);
DebugMap();
}

//************************used to check if the reading boundarys are corect*****************************

public static void DebugMap(){long[] l=new long[]{1,1};long pos=0;while(l[0]>0|l[1]>0){l=VraToPh(pos);
if(l[0]>0|l[1]>0){System.out.println("Ram Address="+pos+"");System.out.println("disk pos="+l[0]+"");
System.out.println("end pos="+l[1]+"\r\n");pos+=l[1];try{System.in.read();}catch(Exception e){}}}}

//*****************************************Reset the Map************************************************

public static void ResetVraReader(){Map=new long[0];}

//************************************set up the ram addresses******************************************

public static void AddVraPos(long DumpSize,long RamPos,long ReadSize,long DiskPos){long[] l=new long[Map.length+4];
for(int i=0;i<Map.length;l[i]=Map[i],i++);l[Map.length]=DumpSize;l[Map.length+1]=RamPos;l[Map.length+2]=ReadSize;
l[Map.length+3]=DiskPos;Map=l;}

//******************************where the start and end of the VRA is***********************************

public static long[] VraToPh(long VRA){int el1=(Map.length-4),el2=0;
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