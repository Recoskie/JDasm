package Format;

import swingIO.*;
import swingIO.tree.*;
import javax.swing.tree.*;

public class WAV extends Window.Window implements JDEventListener
{
  //Descriptors.

  private Descriptor[] headers = new Descriptor[1];

  //Basic wave audio data info.

  private int channels = 0;
  private int sampleRate = 0;
  private int bitPerSample = 0;
  private int sampleSize = 0;
  private int samplePoint = 0;
  private int dataSize = 0;
  private long dataPos = 0;
  private boolean opInfo = false;

  //Number of samples/sec are not known till the headers are read.

  private Descriptor[] samples;

  public WAV() throws java.io.IOException
  {
    //Setup.

    tree.setEventListener( this ); file.Events = false;

    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true); tree.setShowsRootHandles(true); JDNode root = new JDNode( fc.getFileName() + ( fc.getFileName().indexOf(".") > 0 ? "" : ".wav" ), -1 );

    //Begin reading the BMP header.

    Descriptor wavHeader = new Descriptor( file ); headers[0] = wavHeader; wavHeader.setEvent( this::WAVInfo );

    JDNode BHeader = new JDNode("WAV Header.h", 0); root.add( BHeader );

    wavHeader.String8("Signature", 4);
    wavHeader.LUINT32("File size");
    wavHeader.String8("Signature", 4);
    wavHeader.String8("End", 4);
    wavHeader.LUINT32("File Format header size");
    wavHeader.LUINT16("Type");
    wavHeader.LUINT16("Audio channels"); channels = (short)wavHeader.value;
    wavHeader.LUINT32("Sample rate"); sampleRate = (int)wavHeader.value;
    wavHeader.LUINT32("1-sec Sample size"); sampleSize = (int)wavHeader.value;
    wavHeader.LUINT16("Sample Point size"); samplePoint = (short)wavHeader.value;
    wavHeader.LUINT16("Bit per Sample"); bitPerSample = (short)wavHeader.value;

    long t = file.getFilePointer();
    int data = 0;
    boolean found = false;

    for( ; data < 512; data++ )
    {
      file.seek( t + data ); file.read(4); if( file.toLInt() == 1635017060 ) { data = (int)file.getFilePointer() - 4; found = true; break; }
    }

    file.seek( t ); if( found && data-t > 0 ) { opInfo = true; wavHeader.Other("Other data", (int)(data-t) ); }

    if( found )
    {
      wavHeader.Other("Data Signature", 4 );
      wavHeader.LUINT32("Data Size"); dataSize = (int)wavHeader.value;

      dataPos = file.getFilePointer();

      //setup number of samples if the data signature was found.

      JDNode Samples = new JDNode("Sample Array", 1);

      int e = dataSize / sampleSize; samples = new Descriptor[e];
      
      for( int i = 1; i <= e; i++ ) { Samples.add( new JDNode( "Sample sec #" + i + ".h", ( i + 1 ) ) ); }

      root.add( Samples );
    }

    //Decode the setup headers.
    
    ((DefaultTreeModel)tree.getModel()).setRoot(root); file.Events = true;

    //Set the default node.

    tree.setSelectionPath( new TreePath( BHeader.getPath() ) ); open( new JDEvent( this, "", 0 ) );
  }

  public void Uninitialize() { headers = new Descriptor[1]; samples = null; }

  public void open( JDEvent e )
  {
    if( e.getID().equals("UInit") ) { Uninitialize(); }

    else if( e.getArg(0) < 1 )
    {
      ds.setDescriptor(headers[(int)e.getArg(0)]);
    }
    else if( e.getArg(0) == 1 )
    {
      tree.expandPath( tree.getLeadSelectionPath() );

      info("<html>Contains the raw PCM audio data.</html>"); ds.clear();
    }
    else
    {
      int readSample = (int)e.getArg(0) - 2;

      if( samples[ readSample ] == null )
      {
        long samplePos = ( readSample * sampleSize ) + dataPos;

        int sampleBytes = bitPerSample / 8;

        file.Events = false;

        try
        {
          file.seek( samplePos ); Descriptor s = new Descriptor( file ); samples[(int)e.getArg(0) - 2] = s;
      
          for( int i = 0; i < sampleRate; i++ )
          {
            for( int c = 1; c <= channels; c++ )
            {
              if( sampleBytes == 1 ) { s.INT8("Channel #" + c + ""); }
              else if( sampleBytes == 2 ) { s.LINT16("Channel #" + c + ""); }
              else if( sampleBytes == 3 ) { s.Other("Channel #" + c + "", 3); }
              else if( sampleBytes == 4 ) { s.LINT32("Channel #" + c + ""); }
            }
          }
        }
        catch( Exception er ) { file.Events = true; }
      
        file.Events = true;
      }

      ds.setDescriptor( samples[ readSample ] );
    }
  }

  //RIFF header as audio wave file format.

  public static final String[] WAVInfo = new String[]
  {
    "<html>The WAV/RIFF header must start with RIFF = 52, 49, 46, 46.<br /><br />If it does not pass the signature test then the audio file is corrupted.</html>",
    "<html>File Size.</html>",
    "<html>Wave signature. The RIFF header does supports other audio formats. Since this is an wave audio file it should always be wave signature.</html>",
    "<html>Marks the end of the file format type info.</html>",
    "<html>This is the length of the file format info.<br /><br />This is everything that came before \"fmt \", which marks the end of the file format type info.</html>",
    "<html>Type of format (1 is PCM). This should not be set anything other than PCM.<br /><br />" +
    "What is funny is that this is the only type setting. PCM is the standard way of playing audio on all digital devices.</html>",
    "<html>Number of Channels. This is the number of audio outputs, for example stereo audio, or headphones use two audio channels for both the right and left ear.</html>",
    "<html>This is the number of outputs that is given to each audio chanel per second.<br /><br />" +
    "The Common values are 44100 (CD), 48000 (DAT).<br /><br />" +
    "Sample Rate = Number of Samples per second, or Hertz.</html>",
    "<html>This is the total size of one sample in one second.<br /><br />" +
    "It is calculated as follows (SampleRate * BitsPerSample * Channels) / 8.</html>",
    "<html>This is the size of one sample point to all speaker channels." +
    "It is calculated as follows (BitsPerSample * Channels) / 8.</html>",
    "<html>The number of bits used as a value for each sample point.</html>",
    "<html>Contains additional information about the track, comments, or artist.</html>",
    "<html>Audio Data start signature.</html>",
    "<html>Size of Raw Audio Data.</html>"
  };

  public void WAVInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>The wave audio header is a RIFF audio format. The RIFF header uses a format header tha specifies the file as a WAVE audio file.</html>");
    }
    else
    {
      info( WAVInfo[ ( el >= 11 && !opInfo ) ? el + 1 : el ] );
    }
  }
}
