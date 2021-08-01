package Format.RIFFDecode;

//This is for RIFF file formats that do not yet have a reader plugin.

public class NULL implements RSection
{
  public boolean section( String name, long size, swingIO.tree.JDNode node ) throws java.io.IOException
  {
    return( false );
  }

  public void open( swingIO.tree.JDEvent e )
  {
    
  }
}
