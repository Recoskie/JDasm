package swingIO;

import swingIO.tree.*;
import java.io.*;

public class diskChooser implements JDEventListener
{
  protected JDTree jd;
  protected JDEventListener Event = this;

  //Check system information.

  private static final String Sys = System.getProperty("os.name");
  private static final boolean windows = Sys.startsWith("Windows");
  private static final boolean linux = Sys.startsWith("Linux");
  private static final boolean mac = Sys.startsWith("Mac");
  //private static final boolean solaris = Sys.startsWith("SunOS");
  //private static final boolean iOS = Sys.startsWith("iOS");

  //get system Disks.
  
  public class getDisks
  {
    private boolean end = false , check = false;
    private int r = 0;
    private File f;
    private JDNode root;

    public int disks = 0;
    
    public getDisks( JDNode r ){ root = r; }
      
    public void checkDisk( String Root, String type, boolean Zero )
    {
      r = 0; end = false; while(!end)
      {
        try
        {
          f = new File (Root + ( r == 0 && Zero ? "" : r ) + ""); check = f.exists(); new RandomAccessFile( f, "r");
          root.add( new JDNode( type + r + ".disk", Root + ( r == 0 && Zero ? "" : r ) ) );
          r += 1; disks += 1;
        }
        catch( Exception er )
        {
          if( check || er.getMessage().indexOf("Access is denied") > 0 )
          {
            root.add( new JDNode( type + r + ".disk", Root + ( r == 0 && Zero ? "" : r ) ) );
            r += 1; disks += 1;
          }
          else
          {
            end = true;
          }
        }
      }
    }
  }

  //Initialize.

  public diskChooser( JDTree t ) { jd = t; jd.setEventListener( this ); findDisks(); }

  public diskChooser( ) { }

  public void setTree( JDTree t ) { jd = t; findDisks(); }

  //Set the event listener.

  public void setEventListener( JDEventListener listener ) { Event = listener; }

  //reset the event listener.

  public void removeEventListener( JDEventListener listener ) { Event = this; }

  //Search system for disks.

  private void findDisks()
  {
    //Clear the current tree nodes.

    ((javax.swing.tree.DefaultTreeModel)jd.getModel()).setRoot( null ); JDNode root = new JDNode("Root");

    //Setup disk check utility.

    getDisks d = new getDisks( root );
      
    //Windows uses Physical drive. Needs admin permission.

    if( windows ) { d.checkDisk( "\\\\.\\PhysicalDrive", "Disk", false ); }

    //Linux. Needs admin permission.
      
    if( linux ) { d.checkDisk("/dev/sda", "Disk", true ); d.checkDisk("/dev/sdb", "Removable Disk", true ); }

    //Mac OS X. Needs admin permission.

    if( mac ) { d.checkDisk("/dev/disk", "Disk", false ); }

    //Update tree.
      
    if( d.disks != 0 )
    {
      ((javax.swing.tree.DefaultTreeModel)jd.getModel()).setRoot( root );
    }
    else
    {
      javax.swing.JOptionPane.showMessageDialog(null,"Unable to Find any Disk drives on this System.");
    }
  }

  public void open( JDEvent jd ) { Event.open( jd ); }
}