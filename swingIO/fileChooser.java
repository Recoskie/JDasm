package swingIO;

import swingIO.tree.*;
import java.io.*;

public class fileChooser implements JDEventListener
{
  protected JDTree jd;
  protected JDEventListener Event = this;

  //Current file path.

  private String Path = "";
  private String fileName = "";
  private static final String Sep = System.getProperty("file.separator");

  //File path history.

  private String[] History = new String[10];
  private boolean REC = true;

  //File path index.

  private int h = -1, h2 = -1;

  //Initialize.

  public fileChooser( JDTree t ) { jd = t; jd.setEventListener( this ); dirSearch(); }

  public fileChooser() { }

  public void setTree( JDTree t ) { t.setEventListener( this ); dirSearch(); }

  //Set the event listener.

  public void setEventListener( JDEventListener listener ) { Event = listener; }

  //reset the event listener.

  public void removeEventListener( JDEventListener listener ) { Event = this; }

  //Search current DIR and construct tree.

  private void dirSearch()
  {
    //Only record file path history if in file chooser mode.

    if( REC ) { AddToHistory( Path ); }

    //Clear the current tree nodes.

    ((javax.swing.tree.DefaultTreeModel)jd.getModel()).setRoot( null ); JDNode root = new JDNode("Root");

    //If no file path. Then list disk drives.
    
    if( Path == "" )
    {
      File[] roots = File.listRoots();
      
      for(int i = 0; i < roots.length; i++)
      {
        JDNode t = new JDNode( roots[i].toString(), "", new long[]{ -1 } ); t.add( new JDNode("") ); root.add( t );
      }
    }

    //Else Add folders, and file from current path. 

    else
    {
      File folder = new File( Path );
      File[] list = folder.listFiles();
      
      for(int i = 0; i < list.length; i++ )
      {
        if( list[i].isFile() )
        {
          root.add( new JDNode( fix( list[i].toString() ) ) );
        }
        else
        {
          JDNode t = new JDNode( fix( list[i].toString() ), "", new long[]{ -1 } ); t.add( new JDNode("") ); root.add( t );
        }
      }
    }

    //Set the new tree.
    
    ((javax.swing.tree.DefaultTreeModel)jd.getModel()).setRoot( root );
  }

  //get only the folder or file name. Not the full file path.
    
  private String fix(String path)
  {
    String temp = "";
      
    for(int i = path.length(); i > 0;i-- )
    {
      temp=path.substring( i - 1, i );
        
      if(temp.equals(Sep))
      {
        path = path.substring( i, path.length() );
        break;
      }
    }
     
    return(path);
  }

  //Add to the history.

  public void AddToHistory( String Path )
  {
    if( h < ( History.length - 1 ) )
    {
      h += 1; h2 = h; History[h] = Path;
    }
    else
    {
      System.arraycopy( History, 1, History, 0, History.length - 1 ); History[ History.length - 1 ] = Path;
    }
  }

  //Go back in file path history.

  public void back()
  {
    if( h > 1 )
    {
      h-=1; Path = History[h];
        
      REC = false; dirSearch(); REC = true;
    }
  }
  
  //Go forward in file path history.
  
  public void go()
  {
    if( h < h2 )
    {
      h += 1; Path = History[h];
        
      REC = false; dirSearch(); REC = true;
    }
  }

  //Up one folder in file path.

  public void up()
  {
    if( Path.length() > 4 )
    {
      String temp = ""; int i; Path = Path.substring( 0, ( Path.length() - 1 ) );

      for( i = Path.length(); i > 0; i-- )
      {
        temp = Path.substring( i - 1, i );
      
        if( temp.equals(Sep) )
        {
          Path = Path.substring( 0, i - 1 ); Path += Sep; break;
        }
      }
      
      if( i == 0 )
      {
        Path = "";
      }
      
      dirSearch();
    }
    else if( Path.length() > 0 )
    {
      Path = ""; dirSearch();
    }
  }

  //User home folders.

  public void home() { Path = System.getProperty("user.home") + Sep; dirSearch(); }

  //Root drives.

  public void root() { Path = ""; dirSearch(); }

  //Get the last open file name.

  public String getFileName(){ return( fileName ); }

  //Add to path when folder, or fire event.

  public void open( JDEvent e )
  {
    if( e.getArgs()[0] < 0 )
    {
      Path += e.getPath() + Sep; dirSearch();
    }
    else
    {
      fileName = e.getPath(); Event.open( new JDEvent( this, Path + Sep + fileName + "", e.getExtension(), e.getID(), e.getArgs() ) );
    }
  }
}