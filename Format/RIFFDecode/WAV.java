package Format.RIFFDecode;

import swingIO.*;
import swingIO.tree.*;

public class WAV extends Data implements RSection
{
  //Basic wave audio data info.

  private int channels = 0;
  private int sampleRate = 0;
  private int bitPerSample = 0;
  private int sampleSize = 0;
  private int reData = 0;
  private int samplePoint = 0;
  private long dataSize = 0;
  private long dataPos = 0;

  //Number of samples/sec are not known till the format header is read.

  private Descriptor[] samples;

  //The format header must be read.

  public boolean init( String tag ) { return( tag.equals("fmt ") ); }

  //The RIFF data sections are supplied here.

  public void section( String name, long size, JDNode node ) throws java.io.IOException
  {
    //This is the WAVE format section. It stores the information about the raw audio data.

    if( name.equals("fmt ") )
    {
      node.removeAllChildren();

      Descriptor wavHeader = new Descriptor( file ); des.add( wavHeader ); wavHeader.setEvent( this::WAVInfo );

      node.add( new JDNode( "WAV Header.h", ref++ ) );

      wavHeader.LUINT16("Type");
      wavHeader.LUINT16("Audio channels"); channels = (short)wavHeader.value;
      wavHeader.LUINT32("Sample rate"); sampleRate = (int)wavHeader.value;
      wavHeader.LUINT32("1-sec Sample size"); sampleSize = (int)wavHeader.value;
      wavHeader.LUINT16("Sample Point size"); samplePoint = (short)wavHeader.value;
      wavHeader.LUINT16("Bit per Sample"); bitPerSample = (short)wavHeader.value;

      if( size - 16 > 0 ){ wavHeader.Other( "Extended data", (int)(size - 16) ); }

      initPaths.add( 0, new javax.swing.tree.TreePath( node.getFirstLeaf().getPath() ) );
    }

    //This is the raw audio data section.

    else if( name.equals("data") )
    {
      node.removeAllChildren();

      dataPos = file.getFilePointer(); dataSize = size; reData = (int)(dataSize % sampleSize);

      //setup number of samples if the data signature was found.

      int e = (int)(dataSize / sampleSize); samples = new Descriptor[ reData != 0 ? e + 1 : e ];
            
      for( int i = 1; i <= e; i++ ) { node.add( new JDNode( "Sample sec #" + i + ".h", "R", ( i - 1 ) ) ); }
      
      //Check if there is a remainder.
      
      if( reData != 0 ) { node.add( new JDNode( "Sample sec #" + ( e + ( reData / (float)sampleSize ) ) + ".h", "R", e ) ); }
    }
  }

  public void Uninitialize() { samples = null; }

  public void open( JDEvent e )
  {
    if( e.getID().equals("UInit") ) { Uninitialize(); }

    else
    {
      int readSample = (int)e.getArg(0);

      if( samples[ readSample ] == null )
      {
        long samplePos = ( readSample * sampleSize ) + dataPos;

        int sampleBytes = bitPerSample / 8;

        file.Events = false;

        try
        {
          file.seek( samplePos ); Descriptor s = new Descriptor( file ); samples[readSample] = s; s.setEvent( this::SampleInfo );

          int size = samples.length == (int)e.getArg(0) + 1 ? reData / samplePoint : sampleRate; //The last sample is not always a full second.
      
          for( int i = 0; i < size; i++ )
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
    "<html>This should always be set 1 meaning PCM audio format.<br /><br />" +
    "PCM is the standard way of playing audio on all digital devices.</html>",
    "<html>This is the number of audio outputs. Stereo audio or headphones uses two audio channels for both the right and left ear.</html>",
    "<html>This is the number of outputs that are given to each audio channel per second.<br /><br />" +
    "The Common values are 44100 (CD), 48000 (DAT).<br /><br />" +
    "Sample Rate = Number of Samples per second or Hertz.<br /><br />" +
    "A certain amount of points are needed in one second to produce high-frequency sounds per point in one second.<br /><br />" +
    "You can set this to whatever you like, but you may lose the ability to produce certain sounds with a limited point space per second.</html>",
    "<html>This is the total size of one sample in one second.<br /><br />" +
    "It is calculated as follows (SampleRate * BitsPerSample * Channels) / 8.<br /><br />" +
    "Bits per sample are generally in sizes 8, 16, 24, 32. In which 8-bit audio would be 8 bits per sample.<br /><br />" +
    "The sample rate is how many points we are giving to each speaker channel per second, in the case of 8 bits PCM audio signal with 2 channels.<br /><br />" +
    "Every two 8-bit points are read for channel 1, then channel 2. You can have as many channels as you like.<br /><br />" +
    "So the number of sample points in one second is multiplied by the number of channels and number of bits for each sample point, then divided by 8 for actual size in bytes.</html>",
    "<html>This is the number of bytes it takes to send one sample point to each speaker channel." +
    "It is calculated as follows (BitsPerSample * Channels) / 8.<br /><br />" +
    "Bits per sample are generally in sizes 8, 16, 24, 32. In which 8-bit audio would be 8 bits per sample point, in the case of an 8-bit audio signal with 2 channels.<br /><br />" +
    "Every two 8-bit points are read for channel 1, then channel 2.<br /><br />" +
    "The number of channels and number of bits is multiplied to find the size for one complete output to all speaker channels, then divided by 8 for actual size in bytes.<br /><br />" +
    "This can be multiplied by the sample rate to find the size of each PCM sample in one second.</html>",
    "<html>The number of bits is used as a value for each sample point.<br /><br />" +
    "Bits per sample are generally in sizes 8, 16, 24, 32. In which 8-bit audio would be 8 bits per sample point.</html>",
    "<html>Extended format information.</html>"
  };

  public void WAVInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>The RIFF header specifies the file type as a WAVE audio file. The section named \"fmt \" is then used as the wave audio header.</html>");
    }
    else
    {
      info( WAVInfo[ el ] );
    }
  }

  public void SampleInfo( int el )
  {
    info("<html>This is the raw Audio data given to the PCM device.<br /><br />" +
    "We usually draw all these points on a graph for each speaker channel.</html>");
  }
}
