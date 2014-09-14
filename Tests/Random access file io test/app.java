import java.io.*;

public class app
{
public static void main(String[]args) throws Exception
{
System.out.println("useing random acess file");

RandomAccessFile b=new RandomAccessFile(new File("DIFxAPI.dll"),"rw");

byte[] buf=new byte[4];


b.seek(1); //moves to a reading position

b.read(buf,0,buf.length);

for(int i=0;i<buf.length;i++)
{
System.out.print(B2H(Integer.toHexString(buf[i]&0xFF).toUpperCase())+" ");
}

System.out.println("\r\nread over agen");

b.seek(0);

b.read(buf,0,buf.length);

for(int i=0;i<buf.length;i++)
{
System.out.print(B2H(Integer.toHexString(buf[i]&0xFF).toUpperCase())+" ");
}

System.out.println("");
}

public static String B2H(String b){if(b.length()<=1){b="0"+b;};return(b);}
}