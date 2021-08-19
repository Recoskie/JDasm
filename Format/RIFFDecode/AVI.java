package Format.RIFFDecode;

import swingIO.*;
import swingIO.tree.*;

public class AVI extends Data implements RSection
{
  /*************************************************************************
  The preceding format header varies based on the stream header.
  **************************************************************************
  0 = Video
  1 = Audio
  2 = MIDI sequenced audio
  3 = Text (subtitles). 
  *************************************************************************/

  private int streamFormat = 0;

  //Load all of the format headers on init. The precising foumart header is defined by the stream format header.

  public boolean init( String tag ) { return( tag.equals("LISThdrl") || tag.equals("LISTstrl") || tag.equals("avih") || tag.equals("strh") || tag.equals("strf") || tag.equals("strd") || tag.equals("strn") ); }

  //The RIFF data sections are supplied here.

  public void section( String name, long size, JDNode node ) throws java.io.IOException
  {
    //The AVI header.

    if( name.equals("avih") )
    {
      node.removeAllChildren();

      Descriptor aviHeader = new Descriptor( file ); des.add( aviHeader ); aviHeader.setEvent( this::AVIInfo );

      node.add( new JDNode( "AVI Header.h", ref++ ) );

      if( size > 3 ) { aviHeader.LUINT32("Microseconds Per Frame"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Max Bytes Per Second"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Padding Granularity"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Flags"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Total Frames"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Initial Frames"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Streams"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Suggested Buffer Size"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Width"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Height"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Reserved"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Reserved"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Reserved"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Reserved"); size -= 4; }
      if( size > 0 ) { aviHeader.Other( "Extended data", (int)size ); }

      initPaths.add( 0, new javax.swing.tree.TreePath( node.getFirstLeaf().getPath() ) );
    }

    //The AVI stream header.

    else if( name.equals("strh") )
    {
      node.removeAllChildren();

      Descriptor aviHeader = new Descriptor( file ); des.add( aviHeader ); aviHeader.setEvent( this::StreamInfo );

      node.add( new JDNode( "Stream Header.h", ref++ ) );

      if( size > 3 )
      {
        aviHeader.String8("Type", 4); String t = (String)aviHeader.value;

        if( t.equals("vids") ) { streamFormat = 0; }
        else if( t.equals("auds") ) { streamFormat = 1; }
        else if( t.equals("mids") ) { streamFormat = 2; }
        else if( t.equals("txts") ) { streamFormat = 3; }
        else { streamFormat = 4; }

        size -= 4;
      }
      else { streamFormat = 4; }

      if( size > 3 ) { aviHeader.String8("Handler", 4); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Flags"); size -= 4; }
      if( size > 1 ) { aviHeader.LUINT16("Priority"); size -= 2; }
      if( size > 1 ) { aviHeader.LUINT16("Language"); size -= 2; }
      if( size > 3 ) { aviHeader.LUINT32("Initial Frames"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Scale"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Rate"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Start"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Length"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Suggested Buffer Size"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Quality"); size -= 4; }
      if( size > 3 ) { aviHeader.LUINT32("Sample Size"); size -= 4; }
      if( size > 1 ) { aviHeader.LUINT16("Left"); size -= 2; }
      if( size > 1 ) { aviHeader.LUINT16("Top"); size -= 2; }
      if( size > 1 ) { aviHeader.LUINT16("Right"); size -= 2; }
      if( size > 1 ) { aviHeader.LUINT16("Bottom"); size -= 2; }
      if( size > 0 ) { aviHeader.Other( "Extended data", (int)size ); }

      initPaths.add( new javax.swing.tree.TreePath( node.getPath() ) );
    }

    //The AVI stream format header.

    else if( name.equals("strf") )
    {
      if( streamFormat == 0 )
      {
        node.removeAllChildren();

        Descriptor aviHeader = new Descriptor( file ); des.add( aviHeader ); aviHeader.setEvent( this::VideoInfo );
  
        node.add( new JDNode( "Stream format.h", ref++ ) );
  
        if( size > 3 ) { aviHeader.LUINT32("Size"); size -= 4; }
        if( size > 3 ) { aviHeader.LUINT32("Width"); size -= 4; }
        if( size > 3 ) { aviHeader.LUINT32("Height"); size -= 4; }
        if( size > 1 ) { aviHeader.LUINT16("Planes"); size -= 2; }
        if( size > 1 ) { aviHeader.LUINT16("BitCount"); size -= 2; }
        if( size > 3 ) { aviHeader.LUINT32("Compression"); size -= 4; }
        if( size > 3 ) { aviHeader.LUINT32("Image Size"); size -= 4; }
        if( size > 3 ) { aviHeader.LUINT32("X Pixels per meter"); size -= 4; }
        if( size > 3 ) { aviHeader.LUINT32("Y Pixels per meter"); size -= 4; }
        if( size > 3 ) { aviHeader.LUINT32("Colors used"); size -= 4; }
        if( size > 3 ) { aviHeader.LUINT32("Important colors"); size -= 4; }
        if( size > 0 ) { aviHeader.Other( "Extended data", (int)size ); }
      }

      else if( streamFormat == 1 )
      {
        node.removeAllChildren();
        
        Descriptor aviHeader = new Descriptor( file ); des.add( aviHeader ); aviHeader.setEvent( this::AudioInfo );
  
        node.add( new JDNode( "Stream format.h", ref++ ) );

        if( size > 1 ) { aviHeader.LUINT16("Format"); size -= 2; }
        if( size > 1 ) { aviHeader.LUINT16("Number of Channels"); size -= 2; }
        if( size > 3 ) { aviHeader.LUINT32("Samples per sec"); size -= 4; }
        if( size > 3 ) { aviHeader.LUINT32("Bytes per sec"); size -= 4; }
        if( size > 1 ) { aviHeader.LUINT16("Block align"); size -= 2; }
        if( size > 1 ) { aviHeader.LUINT16("Bits per sample"); size -= 2; }
        if( size > 1 ) { aviHeader.LUINT16("Size"); size -= 2; }
        if( size > 0 ){ aviHeader.Other( "Extended data", (int)size ); }
      }

      initPaths.add( new javax.swing.tree.TreePath( node.getPath() ) );
    }
  }

  public void Uninitialize()
  {
  }

  public void open( JDEvent e )
  {
  }

  public static final String res = "<html>A section that is reserved, is skipped. So that some day the empty space may be used for something new.</html>";
  public static final String unknown = "<html>Unknown data. May be used as padding.</html>";

  public static final String[] AVI = new String[]
  {
    "<html>Specifies the number of microseconds between frames.<br /><br />This value indicates the overall timing for the file.</html>",
    "<html>Specifies the approximate maximum data rate of the file.<br /><br />" +
    "This value indicates the number of bytes per second the system must handle to present an AVI sequence as specified by the other parameters contained in the main avi header and stream headers.</html>",
    "<html>Specifies the alignment for data, in bytes. Pad the data to multiples of this value.</html>",
    "<html>Contains a bit combination of zero or more of the following flags:<br /><br />" +
    "<table border=\"1\">" +
    "<tr><td>Bit</td><td>Description</td></tr>" +
    "<tr><td>00000000000000000000000000010000</td><td>Indicates the AVI file has an \"idx1\" index list.</td></tr>" +
    "<tr><td>00000000000000000000000000100000</td><td>Indicates that the application should use the \"idx1\" list section, rather than the ordering of the \"LIST (movi)\", to determine the order of presentation of the data.<br /><br />" +
    "For example, this flag could be used to create a list of frames for editing.</td></tr>" +
    "<tr><td>00000000000000000000000100000000</td><td>Indicates the AVI file is interleaved.</td></tr>" +
    "<tr><td>00000000000000000000100000000000</td><td>Indicates that the key frames in \"LIST (odml)\" are reliable. Only if \"LIST (odml)\" is present.<br /><br />" +
    "If this is not set in an Open-DML file, then the key frame could be defective without technically rendering the file as invalid.</td></tr>" +
    "<tr><td>00000000000000010000000000000000</td><td>Indicates the AVI file is a specially allocated file used for capturing real-time video.<br /><br />" +
    "Applications should warn the user before writing over a file with this flag set because the user probably defragmented this file.</td></tr>" +
    "<tr><td>00000000000000100000000000000000</td><td>Indicates the AVI file contains copyrighted data and software. When this flag is used, software should not permit the data to be duplicated.</td></tr>" +
    "</table></html>",
    "<html>Specifies the total number of frames of data in the file.</html>",
    "<html>Specifies the initial frame for interleaved files. Non interleaved files should specify zero.<br /><br />" +
    "If you are creating interleaved files, specify the number of frames in the file prior to the initial frame of the AVI sequence in this member.<br /><br />" +
    "To give the audio driver enough audio to work with, the audio data in an interleaved file must be skewed from the video data.<br /><br />" +
    "Typically, the audio data should be moved forward enough frames to allow approximately 0.75 seconds of audio data to be preloaded. Also set the same value for the Initial Frames member in the stream header.</html>",
    "<html>Specifies the number of streams in the file. For example, a file with audio and video has two streams.</html>",
    "<html>Specifies the suggested buffer size for reading the file. Generally, this size should be large enough to contain the largest chunk in the file.<br /><br />" +
    "If set to zero, or if it is too small, the playback software will have to reallocate memory during playback, which will reduce performance.<br /><br />" +
    "For an interleaved file, the buffer size should be large enough to read an entire record, and not just a chunk.</html>",
    "<html>Specifies the width of the AVI file in pixels.</html>",
    "<html>Specifies the height of the AVI file in pixels.</html>",
    res, res, res, res, unknown
  };

  public static final String[] Stream = new String[]
  {
    "<html>Contains a four letter code that specifies the type of the data contained in the stream. The following standard AVI values for video and audio are defined.<br /><br />" +
    "<table border=\"1\">" +
    "<tr><td>Code</td><td>Description</td></tr>" +
    "<tr><td>vids</td><td>Video.</td></tr>" +
    "<tr><td>auds</td><td>Audio.</td></tr>" +
    "<tr><td>mids</td><td>MIDI sequenced based audio.</td></tr>" +
    "<tr><td>txts</td><td>Text (Subtitles).</td></tr></html>",
    "<html>Optionally, contains a four letter code that identifies a specific data handler.<br /><br />" +
    "The data handler is the preferred handler for the stream. For audio and video streams, this specifies the codec for decoding the stream.</html>",
    "<html>Contains a bit combination of zero or more of the following flags:<br /><br />" +
    "<table border=\"1\">" +
    "<tr><td>Bit</td><td>Description</td></tr>" +
    "<tr><td>00000000000000000000000000000001</td><td>Indicates this stream should not be enabled by default.</td></tr>" +
    "<tr><td>00000000000000010000000000000000</td><td>Indicates this video stream contains palette changes.<br /><br />" +
    "This flag warns the playback software that it will need to animate the palette.</td></tr>" +
    "</table></html>",
    "<html>Specifies priority of a stream type. For example, in a file with multiple audio streams, the one with the highest priority might be the default stream.</html>",
    "<html>Language.</html>",
    "<html>Specifies the initial frame for interleaved files. Non interleaved files should specify zero.<br /><br />" +
    "If you are creating interleaved files, specify the number of frames in the file prior to the initial frame of the AVI sequence in this member.<br /><br />" +
    "To give the audio driver enough audio to work with, the audio data in an interleaved file must be skewed from the video data.<br /><br />" +
    "Typically, the audio data should be moved forward enough frames to allow approximately 0.75 seconds of audio data to be preloaded. Also set the same value for the Initial Frames member of the stream header.</html>",
    "<html>Used with Rate member to specify the time scale that this stream will use. Dividing Rate by Scale gives the number of samples per second. For video streams, this is the frame rate.<br /><br />" +
    "For audio streams, this rate corresponds to the time needed to play 1 second bytes of audio, which for PCM audio is the just the sample rate.</html>",
    "<html>See the Scale member, for details. As rate and scale are used together.</html>",
    "<html>Specifies the starting time for this stream. The units are defined by the Rate and Scale members in the avi header.<br /><br />" +
    "Usually, this is zero, but it can specify a delay time for a stream that does not start concurrently with the file.</html>",
    "<html>Specifies the length of this stream. The units are defined by the Rate and Scale members.</html>",
    "<html>Specifies how large a buffer should be used to read this stream. Typically, this contains a value corresponding to the largest chunk present in the stream index list.<br /><br />" +
    "Using the correct buffer size makes playback more efficient. Use zero if you do not know the correct buffer size.</html>",
    "<html>Specifies an indicator, for the quality of the data in the stream. Quality is represented as a number between 0 and 10,000.<br /><br />" +
    "For compressed data, this typically represents the value of the quality parameter passed to the compression software. If set to –1, drivers use the default quality value.</html>",
    "<html>Specifies the size of a single sample of data. This is set to zero if the samples can vary in size.<br /><br />" +
    "If this number is nonzero, then multiple samples of data can be grouped into a single chunk within the file.<br /><br />" +
    "If it is zero, each sample of data (such as a video frame) must be in a separate chunk.<br /><br />" +
    "For video streams, this number is typically zero, although it can be nonzero if all video frames are the same size.<br /><br />" +
    "For audio streams, this number should be the same as the block alignment member in stream format header.</html>",
    "<html>Rectangle X. Defines an area in the video to display this text or video stream.</html>",
    "<html>Rectangle Y. Defines an area in the video to display this text or video stream.</html>",
    "<html>Rectangle width. Defines an area in the video to display this text or video stream.</html>",
    "<html>Rectangle Height. Defines an area in the video to display this text or video stream.</html>",
    unknown
  };

  public static final String[] Audio = new String[]
  {
    "<html>Audio format type. Typically 1, or 65534 = standard raw PCM audio.</html>",
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
    "<html>Must be equal to Channels times Bits Per Sample divided by 8.<br /><br />" +
    "For non raw PCM formats, this member must be computed according to the specification of the compressed audio format.</html>",
    "<html>The number of bits is used as a value for each sample point.<br /><br />" +
    "Bits per sample are generally in sizes 8, 16, 24, 32. In which 8-bit audio would be 8 bits per sample point.</html>",
    "<html>Size, in bytes, of extra format information.</html>",
    unknown
  };

  public static final String[] Video = new String[]
  {
    "<html>Specifies the number of bytes required for this stream header.</html>",
    "<html>Specifies the width of the bitmap, in pixels.</html>",
    "<html>Specifies the height of the bitmap, in pixels.<br /><br />" +
    "For uncompressed RGB bitmaps, if Height is positive, the bitmap is a bottom-up with the origin at the lower left corner.<br /><br />" +
    "If Height is negative, the bitmap is a top-down with the origin at the upper left corner per pixel.<br /><br />" +
    "For compressed formats, biHeight must be positive, regardless of image orientation.</html>",
    "<html>Specifies the number of images per bitmap. This value must be set to 1.</html>",
    "<html>Specifies the number of bits per pixel color. For uncompressed formats, this value is 24 one byte for each Red, Green, Blue color.</html>",
    "<html>This is a four letter code specifying the picture compression type.</html>",
    "<html>Specifies the size, in bytes, of each image. This can be set to 0 for uncompressed RGB bitmaps.</html>",
    "<html>Specifies the horizontal resolution, in pixels per meter, of the target device for the bitmap.</html>",
    "<html>Specifies the vertical resolution, in pixels per meter, of the target device for the bitmap.</html>",
    "<html>Specifies the number of color indices in the color table that are actually used by the bitmap.</html>",
    "<html>Specifies the number of color indices that are considered important for displaying the bitmap. If this value is zero, all colors are important.</html>",
    unknown
  };

  public void AVIInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>The AVI header specifies the number of streams. For example, a movie with both video and audio will have 2 streams.<br /><br />" +
      "The AVI header sets the width and height of the video. In an uncompressed AVI a video stream is made of bitmap pictures.<br /><br />" +
      "Each \"LIST (strl)\" is each steam. Each stream has a header for the stream type, and a subsequent format header specifying the format information of the stream type.<br /><br />" +
      "Each section labeled \"JUNK\" is used as padding to make sure frames and audio are equally spaced apart in memory for faster reading.<br /><br />" +
      "The \"LIST (movi)\" uses the first two numbers which corresponds to the stream number. The last two characters is the expected type of data.<br /><br />" +
      "<table border=\"1\">" +
      "<tr><td>Last Two-character code</td><td>Description</td></tr>" +
      "<tr><td>db</td><td>Uncompressed video frame</td></tr>" +
      "<tr><td>dc</td><td>Compressed video frame</td></tr>" +
      "<tr><td>pc</td><td>Palette change</td></tr>" +
      "<tr><td>wb</td><td>Audio data</td></tr>" +
      "</table><br />" +
      "Depending on the size of the recoding or movie it may take a few seconds to open the \"LIST (movi)\" section.<br /><br />" +
      "The \"idx1\" list is the memory location and position to each frame, or audio in \"LIST (movi)\" it is called an index list.</html>");
    }
    else
    {
      info( AVI[el] );
    }
  }

  public void StreamInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>Each stream has a header that specifies if the stream is audio, video, or text (subtitles).<br /><br />" +
      "At the end of this header is a rectangle that defines where the subtitles go, or video stream.<br /><br />" +
      "The preceding stream format header defines the denials of the particular stream.<br /><br />" +
      "Each steam goes in order by stream number starting from 00 to 01 and so on. These numbers are used in the \"LIST (movi)\" section.</html>");
    }
    else
    {
      info( Stream[el] );
    }
  }

  public void VideoInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>Uncompressed video is just a set of bit map pictures that are gone through at a set frame rate.<br /><br />" +
      "Bitmap pictures are very simple and easy to read pictures in which each pixel has a standard Red, Green, blue color value.<br /><br />" +
      "AVI does support compressed picture formats for each frame rather than raw per pixel color format.</html>");
    }
    else
    {
      info( Video[el] );
    }
  }

  public void AudioInfo( int el )
  {
    if( el < 0 )
    {
      info("<html>Uncompressed audio is just a set of positions to move the speaker magnet in a speaker cone at a set rate.<br /><br />" +
      "Uses the same format as a wave audio file. We can plot the audio as a graph if we like.<br /><br />" +
      "AVI does support compressed audio formats rather than raw ready to play audio output to speakers.</html>");
    }
    else
    {
      info( Audio[el] );
    }
  }
}
