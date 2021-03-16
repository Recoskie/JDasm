package WindowComponents;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

public class FileIconManager extends DefaultTreeCellRenderer
{
  public static String Folder = "Icons/f.gif", UnknownFile = "Icons/u.gif";
  public static ImageIcon FolderPic[] = new ImageIcon[2];

  public static String FType[] = new String[] { ".h", ".disk", ".exe",".dll",".sys",".drv",".ocx" };

  public static String Load[] = new String[] { "Icons/H.gif", "Icons/disk.gif", "Icons/EXE.gif","Icons/dll.gif",
  "Icons/sys.gif","Icons/sys.gif","Icons/sys.gif" };

  public static ImageIcon LoadedPic[] = new ImageIcon[7];

  public static boolean Open = false, LoadData = true;

  public FileIconManager()
  {
    //Load file format Icons.

    if( LoadData )
    {
      LoadData = false;

      FolderPic[0] = new ImageIcon( FileIconManager.class.getResource( Folder ) );
      FolderPic[1] = new ImageIcon( FileIconManager.class.getResource( UnknownFile ) );
    
      for( int i = 0; i < Load.length; i++ )
      {
        LoadedPic[ i ] = new ImageIcon( FileIconManager.class.getResource( Load[i] ) ); 
      }
    }
  }

  //Draw pictures related to file format icon.

  public Component getTreeCellRendererComponent(JTree tree,Object value,boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus)
  {
    boolean check = false;

    super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row, hasFocus );

    if( leaf ) { check = SetImage( value + "" ); }

    if( !check&leaf ) { UnknownFileType( value + "" ); } else if( !leaf ) { FolderIcon( value + "" ); }
    
    return( this );
  }

  //int value from a loaded set of icons in an array

  protected boolean SetImage(String name)
  {
    String EX=getExtension( name );
      
    if( !EX.equals("") )
    {
      int n = GetExtensionNumber( EX );
        
      if( n != -1 )
      {
        setIcon( LoadedPic[n] );
        setText( FilterExtension( FilterReference( name ) ) );
        return( true );
      }
    }
    else
    {
      setText( FilterReference( name ) );
    }
      
    return(false);
  }

  protected void FolderIcon(String f)
  {
    if( Open )
    {
      if( !SetImage(f) )
      {
        setIcon( FolderPic[0] );
      }
    }
    else
    {
      setIcon( FolderPic[0] );
    }
  }

  protected void UnknownFileType(String f)
  {
    //In case the file uses a value at the end of it's name to use as reference in a list

    if( Open )
    {
      f = FilterReference( f );

      if( !SetImage( f ) )
      {
        setIcon( FolderPic[1] );
      }
    }

    else { setIcon( FolderPic[1] ); }
  }

  protected String getExtension(String f)
  {
    if( f.lastIndexOf(46) > 0 )
    {
      if( f.lastIndexOf(35) > 0 ) { f = f.substring( 0, f.lastIndexOf(35) ); }

      return( f.substring( f.lastIndexOf(46), f.length() ).toLowerCase() );
    }
      
    return("");
  }
    
  protected int GetExtensionNumber(String Ex)
  {
    for(int i = 0; i < FType.length; i++ )
    {
      if( FType[i].equals(Ex) )
      {
        return(i);
      }
    }
      
    return(-1);
  }
    
  protected String FilterExtension(String File)
  {
    return( File.substring( 0, File.lastIndexOf(46) ) );
  }

  protected String FilterReference(String File)
  {
    if( File.lastIndexOf(35) > 0 )
    {
      return( File.substring( 0, File.lastIndexOf(35) ) );
    }
      
    return(File);
  }
}