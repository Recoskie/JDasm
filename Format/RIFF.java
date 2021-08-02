package Format;

import swingIO.*;
import swingIO.tree.*;
import javax.swing.tree.*;
import Format.RIFFDecode.*;

public class RIFF extends Data implements JDEventListener
{
  //The file size is used to know which is the last riff tag element.

  private long fileSize = 0;

  //These are pre defined to speed up reading the RIFF sections tags.

  private String type = "";
  private RSection format = new NULL();
  private long tagSize = 0;
  private Descriptor Data;
  private JDNode list1, list2, temp;

  //A list of 64 bit numbers are used in the RF64 format.
  //Any 32 bit value that is -1 is an 64 bit value.

  private long[] size64;
  private int index64 = 0;

  public RIFF() throws java.io.IOException
  {
    //Setup the root node and event system.

    tree.setEventListener( this ); file.Events = false;

    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true); tree.setShowsRootHandles(true); JDNode root = new JDNode( fc.getFileName(), -1 );

    //The main RIFF header block.

    Descriptor RiffHeader = new Descriptor( file ); des.add( RiffHeader ); RiffHeader.setEvent( this::RIFFInfo );

    RiffHeader.String8("Signature", 4);
    RiffHeader.LUINT32("File size"); fileSize = ((int)RiffHeader.value) & 0xFFFFFFFFl;
    
    RiffHeader.String8("File type", 4); type = (String)RiffHeader.value;

    root.add( new JDNode("RIFF Header.h", ref++) );

    //The wave audio plugin defines the data sections of a wave audio file.
    
    if( type.equals("WAVE") ) { format = new WAV(); }

    //The data sub blocks.

    while( file.getFilePointer() < fileSize )
    {
      Data = new Descriptor( file ); des.add(Data); Data.setEvent( this::CKInfo );

      //This is the sub structure that is used for all data tags. 

      Data.String8("Section name", 4); type = (String)Data.value;
      Data.LUINT32("Section size"); tagSize = ((int)Data.value) & 0xFFFFFFFFl;

      //A maxed out 32 bit section size is set to the next 64 bit section size.

      if( tagSize == 4294967295l ){ tagSize = size64[index64++]; }

      tagSize += tagSize & 1; //Note every tag must land on a even address position.

      //Specifies the 64 bit size of sections that are -1 size 32 bits.

      if( type.equals("ds64") )
      {
        temp = new JDNode( type, ref++ ); temp.add( new JDNode( "Section Sizes.h", ref++ ) ); root.add(temp);

        Descriptor D64 = new Descriptor( file ); des.add( D64 );

        int re = (int)tagSize & 7; size64 = new long[(int)tagSize / 8];

        for( int i = 0; i < size64.length; i++ ) { D64.LUINT64("Section size 64"); size64[i] = (long)D64.value; }

        if( re != 0 ) { D64.Other( "Padding", re ); }

        //If the file size is max 32 bit integer, then the first 64-bit size is the file size.

        if( fileSize == 4294967295l ) { fileSize = size64[index64++]; }
      }

      //list sub data blocks.

      else if( type.equals("LIST") )
      {
        Data.String8("List type", 4); tagSize -= 4;

        list1 = new JDNode( type, ref++ );

        list( file.getFilePointer() + tagSize, list1 );

        root.add(list1);
      }
      else
      {
        temp = new JDNode( type, ref++ ); root.add( temp );

        //Check if this block is important to the loaded RIFF format plugin.
        
        if( !format.section(type, tagSize, temp) )
        {
          tagSize = tagSize < 0 ? (int)(fileSize - file.getFilePointer()) : tagSize;

          //Otherwise define the nodes data.

          temp.add( new JDNode( "Section Data.h", new long[]{ -2, file.getFilePointer(), file.getFilePointer() + tagSize - 1 } ) );

          //Move to next data tag.

          file.seek( file.getFilePointer() + tagSize );
        }
      }
    }

    //Decode the setup headers.
    
    ((DefaultTreeModel)tree.getModel()).setRoot(root); file.Events = true;

    //Set the first node.

    tree.setSelectionPath( new TreePath( root.getFirstLeaf().getPath() ) ); open( new JDEvent( this, "", 0 ) );
  }

  public void Uninitialize() { ref = 0; des.clear(); }

  public void open( JDEvent e )
  {
    if( e.getID().equals("UInit") ) { format.open( e ); Uninitialize(); }

    //If the event type is "R", then the event is sent to the RIFF format plugin.

    else if( e.getID().equals("R") ) { format.open(e); }

    //The tag headers.

    else if( e.getArg(0) >= 0 )
    {
      tree.expandPath( tree.getLeadSelectionPath() );

      ds.setDescriptor( des.get( (int)e.getArg(0) ) );
    }

    //the data of a particular tag.

    else if( e.getArg(0) == -2 ) { try { file.seek( e.getArg(1) ); } catch( Exception er ) { } Offset.setSelected( e.getArg(1), e.getArg(2) ); ds.clear(); }
  }

  private void list(long end, JDNode List) throws java.io.IOException
  {
    while( file.getFilePointer() < end )
    {
      Data = new Descriptor( file ); des.add(Data);

      Data.String8("Data type", 4); type = (String)Data.value;
      Data.LUINT32("Data size"); tagSize = (int)Data.value;

      tagSize += tagSize & 1; //Note every tag must land on an even address position.

      temp = new JDNode( type, ref++ );

      if( type.equals("LIST") )
      {
        Data.String8("List type", 4); tagSize -= 4;

        des.add(Data);

        list2 = new JDNode( type, ref++ ); temp.add( list2 ); 

        list( file.getFilePointer() + tagSize, list2 );

        List.add(list2);
      }
      else
      {
        temp.add( new JDNode( "Section Data.h", new long[]{ -2, file.getFilePointer(), file.getFilePointer() + tagSize - 1 } ) );
        
        List.add(temp);

        file.seek( file.getFilePointer() + tagSize );
      }
    }
  }

  //RIFF header.

  public static final String[] RIFFInfo = new String[]
  {
    "<html>The RIFF header supports many video/audio formats.<br /><br />If it does not pass the signature test then the video/audio file is corrupted.</html>",
    "<html>The file size needs to be known, so that we know when we have reached the end of the file.<br /><br />" +
    "The RIFF format is divided into parts using a 4 byte (character) name, and size of preceding data.<br /><br />" +
    "We need to know when we reached the end of the file otherwise we will be reding undefined sections after the files end.</html>",
    "<html>This identifies the file format type. It determine how the named chunks of data are read.</html>"
  };

  public void RIFFInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>The RIFF header supports many video/audio formats. The RIFF header specifies the format type.</html>");
    }
    else
    {
      info( RIFFInfo[el] );
    }
  }

  //Data chunk header.

  public static final String[] CKInfo = new String[]
  {
    "<html>After the 4 byte (character) name is the size of the data for this named section.</html>",
    "<html>After section size is another 4 byte (character) section name unless it is a list type.</html>",
    "<html>We could easily skip over the list using the LIST section size.<br /><br />" +
    "If we chose to ignore the list section size, then the next 4 bytes is the list type.<br /><br />" +
    "A list is made up of section names and size as well after the 4 byte list type.</html>"
  };

  public void CKInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>A RIFF file is made up of 4 byte section names, and an section size for the data the section contains.<br /><br />" +
      "This pattern is repeated till file size. It is very important that the RIFF header specifies the exact file size.</html>");
    }
    else
    {
      info( CKInfo[el] );
    }
  }
}