package Format.RIFFDecode;

//A riff file is made up of data sections. The main RIFF header block specifies the file format.
//This allows us to define the data sections by loaded plugin format type.

public interface RSection
{
  public boolean section( String name, int size, swingIO.tree.JDNode node ) throws java.io.IOException;

  public void open( swingIO.tree.JDEvent e );
}