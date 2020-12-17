package Format.EXEDecode;
import java.io.*;
import javax.swing.tree.*;

import dataTools.*;
import RandomAccessFileV.*;
import WindowCompoents.*;

public class Resource extends Data
{
  //Data structure data descriptors.

  java.util.LinkedList<Descriptor> des = new java.util.LinkedList<Descriptor>();

  //Read files, and folders.

  int ref = 0;

  public Descriptor[] readFiles( RandomAccessFileV b, DefaultMutableTreeNode Resource ) throws IOException
  {
    readDir( Resource, 0, b ); //Read DIR at current position.

    return( des.toArray( new Descriptor[ des.size() ] ) );
  }

  //Each DIR contains a list to another Dir list, or File.

  DefaultMutableTreeNode nDir;
  Descriptor File_Str;
  long t = 0;

  public void readDir( DefaultMutableTreeNode Dir, long pos, RandomAccessFileV b ) throws IOException
  {
    Descriptor des_Dir;

    //Number of DIR/File locations under this DIR.

    int  size = 0;
    
    //The position of the current DIR.
    
    long Pos = pos;

    //The position of the DIR. IF 0 use current IO position.

    if( pos != 0 ) { b.seekV( pos ); }

    //The DIR info descriptor.

    des_Dir = new Descriptor( b, true );

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

      des_Dir.LUINT32("Name, or ID");

      t = ( (Integer)des_Dir.value );

      //Negative value locates to a string name.

      if( t < 0 )
      {
        pos = t & 0x7FFFFFFF; t = b.getVirtualPointer(); b.seekV( pos + DataDir[4] );

        File_Str = new Descriptor( b, true ); des.add( File_Str );

        File_Str.LUINT16("Name length"); File_Str.LString16("Entire Name", ((Short)File_Str.value).intValue() );
        
        nDir = new DefaultMutableTreeNode( File_Str.value + "#R," + ref + "" ); ref += 1;

        b.seekV( t );
      }

      //Positive value is a ID name.

      else { nDir = new DefaultMutableTreeNode( t + "" ); }

      Dir.add( nDir );

      des_Dir.LUINT32("Directory, or File");
      
      pos = ((Integer)des_Dir.value).intValue();

      //Factorial.

      if( pos < 0 )
      {
        Pos = b.getVirtualPointer();
        
        readDir( nDir, ( pos & 0x7FFFFFFF ) + DataDir[4], b );
        
        b.seekV( Pos );
      }
      
      //File info.
      
      else
      {
        nDir.setUserObject( new DefaultMutableTreeNode( t + "#R," + ref + "" ) ); ref += 1;
        
        t = b.getVirtualPointer(); b.seekV( pos + DataDir[4] );

        File_Str = new Descriptor( b, true );

        File_Str.LUINT32("File location");
        File_Str.LUINT32("File size");
        File_Str.LUINT32("Code Page");
        File_Str.LUINT32("Reserved");

        des.add( File_Str );

        b.seekV( t );
      }
    }
  }
}
