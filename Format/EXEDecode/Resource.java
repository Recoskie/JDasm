package Format.EXEDecode;
import java.io.*;
import swingIO.*;
import javax.swing.tree.*;

import RandomAccessFileV.*;

public class Resource extends Data
{
  //Data structure data descriptors.

  java.util.LinkedList<Descriptor> des = new java.util.LinkedList<Descriptor>();

  //Read files, and folders.

  int ref = 0;

  //Each DIR contains a list to another Dir list, or File.

  DefaultMutableTreeNode nDir;
  Descriptor File_Str;
  long t = 0;

  //Use current IO potion if no defined position.

  public Descriptor[] readResource( DefaultMutableTreeNode Dir, RandomAccessFileV b ) throws IOException { return( readResource( Dir, b, 0 ) ); }

  //Recursively read Resource at a set position.

  public Descriptor[] readResource( DefaultMutableTreeNode Dir, RandomAccessFileV b, long pos ) throws IOException
  {
    Descriptor des_Dir;

    //Number of DIR/File locations under this DIR.

    int  size = 0;
    
    //The position of the current DIR.
    
    long Pos = pos;

    //The position of the DIR. IF not used then current IO position is used.

    if( pos != 0 ) { b.seekV( pos ); }

    //The DIR info descriptor.

    des_Dir = new Descriptor( b, true ); des_Dir.setEvent( this::dirInfo );

    des_Dir.LUINT32("Characteristics");
    des_Dir.LUINT32("Time Date Stamp");
    des_Dir.LUINT16("Major Version");
    des_Dir.LUINT16("Minor Version");
    des_Dir.LUINT16("Number Of Named Entries"); size = ((Short)des_Dir.value).intValue();
    des_Dir.LUINT16("Number Of Id Entries"); size += ((Short)des_Dir.value).intValue();

    des.add(des_Dir);

    Dir.add( new DefaultMutableTreeNode( "Directory Info.h#R," + ref + "" ) ); ref += 1;

    for( int i = 0; i < size; i++ )
    {
      des_Dir.Array("Array Element " + i + "", 8 );

      des_Dir.LINT32("Name, or ID");

      t = ( (Integer)des_Dir.value );

      //Negative value locates to a string name.

      if( t < 0 )
      {
        pos = t & 0x7FFFFFFF; t = b.getVirtualPointer(); b.seekV( pos + DataDir[4] );

        File_Str = new Descriptor( b, true ); des.add( File_Str ); File_Str.setEvent( this::strInfo );

        File_Str.LUINT16("Name length"); File_Str.LString16("Entire Name", ((Short)File_Str.value).intValue() );
        
        nDir = new DefaultMutableTreeNode( File_Str.value + "#R," + ref + "" ); ref += 1;

        b.seekV( t );
      }

      //Positive value is a ID name.

      else { nDir = new DefaultMutableTreeNode( t + "" ); }

      Dir.add( nDir );

      des_Dir.LINT32("Directory, or File");
      
      pos = ((Integer)des_Dir.value).intValue();

      //Factorial.

      if( pos < 0 )
      {
        Pos = b.getVirtualPointer();
        
        readResource( nDir, b, ( pos & 0x7FFFFFFF ) + DataDir[4] );
        
        b.seekV( Pos );
      }
      
      //File info.
      
      else
      {
        nDir.add( new DefaultMutableTreeNode( "File Info.h#R," + ref ) ); ref += 1;
        
        t = b.getVirtualPointer(); b.seekV( pos + DataDir[4] );

        File_Str = new Descriptor( b, true ); File_Str.setEvent( this::fileInfo );

        File_Str.LUINT32("File location"); pos = ((Integer)File_Str.value).longValue() + imageBase;
        File_Str.LUINT32("File size"); nDir.add( new DefaultMutableTreeNode( "File Data#Sv," + pos + "," + ( pos + ( ( (Integer)File_Str.value ).longValue() ) - 1 ) + "" ) );
        File_Str.LUINT32("Code Page");
        File_Str.LUINT32("Reserved");

        des.add( File_Str );

        b.seekV( t );
      }
    }

    return( des.toArray( new Descriptor[ des.size() ] ) );
  }

  public static final String res = "A section that is reserved, is skipped. So that some day the empty space may be used for something new.";

  public static final String Ver = "Major, and Minor are put together to forum the version number.<br /><br />Example.<br /><br />Major version = 5<br /><br />Minor version = 12<br /><br />Would mean version 5.12V.";

  public static final String[] DirInfo = new String[] {"<html>Characteristics are reserved, for future use.<br /><br />" + res + "</html>",
    "<html>A date time stamp is in seconds. The seconds are added to the starting date \"Wed Dec 31 7:00:00PM 1969\".<br /><br />" +
    "If the time date stamp is \"37\" in value, then it is plus 37 seconds giving \"Wed Dec 31 7:00:37PM 1969\".</html>",
    "<html>" + Ver + "</html>",
    "<html>" + Ver + "</html>",
    "<html>Number of files, or folders with names.<br /><br />Named entires, and numeral named ID are added together for array size.</html>",
    "<html>Number of files, or folders with numerical names.<br /><br />Named entires, and numeral named ID are added together for array size.</html>",
    "<html>File, or Folder array element.</html>",
    "<html>If the value is positive. The number value is the name.<br /><br />" +
    "If the value is negative. Flip the number from negative to positive. Subtract the value into 2147483648. This removes the sing.<br /><br />" +
    "The location is added to the start of the resource section. The string at that location is then the name, of the folder, or file.</html>",
    "<html>If the value is positive. It is a location to a file.<br /><br />" +
    "If the value is negative. Flip the number from negative to positive. Subtract the value into 2147483648. This removes the sing.<br /><br />" +
    "The location is added to the start of the resource section. The location locates to anther Directory of files, or folders.</html>",
  };

  public void dirInfo( int el )
  {
    if( el < 0 )
    {
      info( "<html>A directory consisting, of characteristics, time date stamp, and number of files, or folders.</html>" );
    }
    else
    {
      if( el > 6 ) { el = ( ( el - 6 ) % 3 ) + 6; }

      info( DirInfo[ el ] );
    }
  }

  public static final String[] FileInfo = new String[] {"<html>The location to the file. This location is added to the base address of the program.</html>",
    "<html>The size of the file.</html>",
    "<html></html>",
    "<html>" + res + "</html>"
  };

  public void fileInfo( int el )
  {
    if( el < 0 )
    {
      info( "<html>Each file location. Has a location to the actual data, size, and code page.</html>" );
    }
    else
    {
      info( FileInfo[ el ] );
    }
  }

  public static final String[] StrInfo = new String[] {"<html>The character length of the string. Each character is 16 bits.</html>",
    "<html>The name of the folder, or file.</html>"
  };

  public void strInfo( int el )
  {
    if( el < 0 )
    {
      info( "<html>Location to the named Folder, or File.</html>" );
    }
    else
    {
      info( StrInfo[ el ] );
    }
  }
}
