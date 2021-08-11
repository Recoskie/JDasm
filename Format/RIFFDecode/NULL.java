package Format.RIFFDecode;

//This is for RIFF file formats that do not yet have a reader plugin.

public class NULL implements RSection
{
  public boolean init( String s )
  {
    return( false );
  }

  public void section( String name, long size, swingIO.tree.JDNode node ) throws java.io.IOException
  {

  }

  public void open( swingIO.tree.JDEvent e )
  {
    
  }
}
