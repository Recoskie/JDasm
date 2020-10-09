import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.lang.reflect.*;
import WindowCompoents.*;

//New file components.

import RandomAccessFileV.*;
import VHex.*;

public class app extends WindowCompoents implements TreeWillExpandListener, TreeSelectionListener, ActionListener, MouseListener
{
  //Current file path.

  public String Path = "";
  public String Sep = System.getProperty("file.separator");

  //The file system stream to the file type.

  public static RandomAccessFileV file;

  public static boolean diskMode = false;

  //File path history.

  public String[] History = new String[10];

  //File path index.

  public int h = -1, h2 = -1;

  //Enable, or disable file path recoding.

  public boolean REC = true;

  //Converts the file chooser tree into binary data sections. For binary file format readers.
  
  public boolean Open = false;

  //Each integer is the file format decoder to use by file extensions.

  public int UseDecoder[] = new int[] { 0, 0, 0, 0, 0 };

  //Supported file format extensions.

  public String Suports[] = new String[] { ".exe", ".dll", ".sys", ".drv", ".ocx" };

  //The file to load. To begin decoding file types.

  public String DecodeAPP[] = new String[]{ "Format.EXE" };
  
  //get system Disks.
  
  public class getDisks
  {
    private boolean end = false , check = false;
    private int r = 0;
    private File f;
    private DefaultMutableTreeNode root;
    
    public boolean admin = false;
    public int disks = 0;
    
    public getDisks( DefaultMutableTreeNode r ){ root = r; }
      
    public void checkDisk( String Root, String type, boolean Zero )
    {
      r = 0; end = false; while(!end)
      {
        try
        {
          f = new File (Root + ( r == 0 && Zero ? "" : r ) + ""); check = f.exists();
          new RandomAccessFile( f, "r");
          root.add( new DefaultMutableTreeNode( type + r + "#" + Root + ( r == 0 && Zero ? "" : r ) + ".disk" ) );
          r += 1; disks += 1;
        }
        catch( Exception er ) { if(check) { admin = true; } end = true; }
      }
    }
  }

  //Create the application.

  public app()
  {
    f = new JFrame("JFH-Disassembly"); f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //Tool window.

    infoData.setBorder( BorderFactory.createLineBorder( Color.BLUE, 3 ) );
    infoData.setVerticalAlignment(JLabel.TOP); infoData.setHorizontalAlignment(JLabel.LEFT);

    //File chooser controls.

    fcBar = new JMenuBar();

    JMenuItem Back = new JMenuItem( "Back", new ImageIcon( app.class.getResource( "AppPictures/back.png" ) ) );
    JMenuItem Home = new JMenuItem( "User", new ImageIcon( app.class.getResource( "AppPictures/home.png" ) ) );
    JMenuItem Go = new JMenuItem( "Forward", new ImageIcon( app.class.getResource( "AppPictures/go.png" ) ) );
    JMenuItem Up = new JMenuItem( "Up a Folder", new ImageIcon( app.class.getResource( "AppPictures/up.png" ) ) );
    JMenuItem Computer = new JMenuItem( "My Computer", new ImageIcon( app.class.getResource( "AppPictures/computer.png" ) ) );
    JMenuItem OpenDisk = new JMenuItem( "Open Disk", new ImageIcon( app.class.getResource( "AppPictures/OpenDisk.png" ) ) );

    fcBar.add( Computer ); fcBar.add( Back ); fcBar.add( Home ); fcBar.add( Go ); fcBar.add( Up ); fcBar.add( OpenDisk );
  
    //Action commands.
  
    Back.setActionCommand( "B" ); Back.addActionListener(this);
    Go.setActionCommand( "G" ); Go.addActionListener(this);
    Computer.setActionCommand( "C" ); Computer.addActionListener(this);
    Home.setActionCommand( "H" ); Home.addActionListener(this);
    Up.setActionCommand( "U" ); Up.addActionListener(this);
    OpenDisk.setActionCommand( "O" ); OpenDisk.addActionListener(this);

    //Binary tools menu bar.

    bdBar = new JMenuBar();

    JMenu fm = new JMenu("File");
    JMenu vm = new JMenu("View");
 
    JMenuItem f1 = new JMenuItem("Open new File");

    JMenuItem v1 = new JMenuItem("Toggle text View");
    JMenuItem v2 = new JMenuItem("Toggle virtual space View");
    JMenuItem v3 = new JMenuItem("Toggle offset View");
    JMenuItem v4 = new JMenuItem("Toggle Data Inspector");

    fm.add(f1); vm.add(v1); vm.add(v2); vm.add(v3); vm.add(v4);

    bdBar.add(fm); bdBar.add(vm);
  
    //add ActionListener to menuItems.
    
    f1.addActionListener(this);
    
    v1.addActionListener(this); v2.addActionListener(this);
    v3.addActionListener(this); v4.addActionListener(this);

    //The tree is used for file chooser, and for decoded data view.

    tree = new JTree();
  
    //Update the tree with a directory search for file chooser.
  
    dirSerach();

    //tree properties.

    tree.setRootVisible(false); tree.setShowsRootHandles(false);
    tree.addTreeWillExpandListener(this); tree.addMouseListener(this);
    tree.addTreeSelectionListener(this);
  
    //Custom file Icon manager.
  
    tree.setCellRenderer(new FileIconManager());

    //Simple grid layout, for the tree.

    f.setLayout(new GridLayout(1,0)); f.add(tree);

    //scroll bar for the tree.

    f.add(new JScrollPane(tree));

    //set the menu bar controls for file chooser.

    f.setJMenuBar(fcBar);

    //Set application icon image.

    f.setIconImage( new ImageIcon( app.class.getResource( "AppPictures/app.png" ) ).getImage() );

    //Display the window.

    f.pack();
  
    f.setLocationRelativeTo(null);
  
    f.setVisible(true);
  }

  public static void main(String[]args){ new app(); }

  public void treeWillExpand(TreeExpansionEvent e)
  {
    if( !Open )
    {
      Path += tree.getLastSelectedPathComponent().toString(); Path += Sep; dirSerach();
    }
  }

  public void treeWillCollapse(TreeExpansionEvent e) { }

  //Adjust the tree to current directory path.

  public void dirSerach()
  {
    //Only record file path history if in file chooser mode.

    if( REC ) { AddToHistory( Path ); }

    //Clear the current tree nodes.

    ((DefaultTreeModel)tree.getModel()).setRoot( null ); DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");

    //If no file path list disk drives.
    
    if( Path == "" )
    {
      File[] roots = File.listRoots();
      
      for(int i = 0; i < roots.length; i++)
      {
        DefaultMutableTreeNode temp = new DefaultMutableTreeNode( roots[i] );
        temp.add(new DefaultMutableTreeNode(""));
        root.add(temp);
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
          root.add( new DefaultMutableTreeNode( fix( list[i].toString() ) ) );
        }
        else if(list[i].isDirectory())
        {
          DefaultMutableTreeNode temp = new DefaultMutableTreeNode( fix( list[i].toString() ) );
          temp.add( new DefaultMutableTreeNode("") ); root.add( temp );
        }
      }
    }

    //Set the new tree.
    
    ((DefaultTreeModel)tree.getModel()).setRoot( root );
  }

  //get only the folder or file name. Not the full file path.
    
  public String fix(String path)
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

  //back and forward History Functions
  
  public void AddToHistory(String p)
  {
    if( h < ( History.length - 1 ) )
    {
      h += 1; h2 = h; History[h] = p;
    }
    else
    {
      System.arraycopy( History, 1, History, 0, History.length - 1 );
      History[ History.length - 1 ] = p;
    }
  }

  //Go back in file path history.

  public void back()
  {
    if( h > 1 )
    {
      h-=1; Path = History[h];
      
      REC = false; dirSerach(); REC = true;
    }
  }

  //Go forward in file path history.

  public void go()
  {
    if( h < h2 )
    {
      h += 1; Path = History[h];
      
      REC = false; dirSerach(); REC = true;
    }
  }

  //handling menu item events
  
  public void actionPerformed(ActionEvent e)
  {
    //Basic file path commands.
    
    if( e.getActionCommand() == "B" ) { diskMode = false; back(); }
    
    if( e.getActionCommand() == "G" ) { diskMode = false; go(); }
    
    if( e.getActionCommand() == "C" ) { diskMode = false; Path = ""; dirSerach(); }
    
    if( e.getActionCommand() == "H" ) { diskMode = false; Path = System.getProperty("user.home") + Sep; dirSerach(); }

    //Up one folder.
    
    if( e.getActionCommand() == "U" )
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
          
        dirSerach();
      }
      else if( Path.length() > 0 )
      {
        Path = ""; dirSerach();
      }
    }

    //Disk selector.
    
    if( e.getActionCommand() == "O" )
    {
      //Clear the current tree nodes.

      Boolean err = false;

      ((DefaultTreeModel)tree.getModel()).setRoot( null ); DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");

      getDisks d = new getDisks( root );
      
      //Windows uses Physical drive. Does not need admin to read disks.

      d.checkDisk( "\\\\.\\PhysicalDrive", "Disk", false );

      if( d.admin )
      {
        err = true;
        JOptionPane.showMessageDialog(null,"Unable to read disk drives. Try running as administrator."); REC = false; dirSerach(); REC = true;
      }

      //Linux.
      
      d.checkDisk("/dev/sda", "Disk", true );
      d.checkDisk("/dev/sdb", "Removable Disk", true );

      if( !err && d.admin )
      {
        err = true;
        JOptionPane.showMessageDialog(null,"In order to read disk drives you must run as 'sudo'."); REC = false; dirSerach(); REC = true;
      }

      //Mac OS X.

      d.checkDisk("/dev/disk", "Disk", false );
      
      if( !err && d.admin )
      {
        err = true;
        JOptionPane.showMessageDialog(null,"In order to read disk drives you must run as root on Mac OS.\r\n" +
        "On Mac OS Mojave (10.14), and higher. Full Disk access must be enabled under Settings, for java."); REC = false; dirSerach(); REC = true;
      }
      
      if( !err && d.disks == 0 )
      {
        JOptionPane.showMessageDialog(null,"Unable to Find any Disk drives on this System."); REC = false; dirSerach(); REC = true;
      }
      
      if( !err )
      {
      	//Set the new tree.
    
      	((DefaultTreeModel)tree.getModel()).setRoot( root ); diskMode = true;
      }
    }

    //Binary tool display controls.

    else if( e.getActionCommand().equals("Toggle text View") )
    {
      textV = !textV; Offset.enableText( textV ); Virtual.enableText( textV );
    }

    else if( e.getActionCommand().equals("Toggle virtual space View") )
    {
      Virtual.setVisible(!Virtual.isVisible());
    }

    else if( e.getActionCommand().equals("Toggle offset View") )
    {
      Offset.setVisible(!Offset.isVisible());
    }

    else if( e.getActionCommand().equals("Open new File") )
    {
      Path = History[h]; Reset(); REC = false; dirSerach(); REC = true;
    }
  }

  //check the file type

  public void checkFT(String ft)
  {
    String ex = ft.substring( ( ft.lastIndexOf(46) ), ft.length() ).toLowerCase();

    //If file format is Supported. Open in reader with binary tools.

    int I = DefaultProgram( ex );

    Open = true; new FileIconManager().Open = true;

    try
    {
      if( I >= 0 ) { Class.forName(DecodeAPP[UseDecoder[I]]).getConstructor().newInstance(); }
    }
    catch(Exception e)
    {
      I = -1; JOptionPane.showMessageDialog(null,"Unable to Load Decode Program For This File Format!");
    }

    try
    {
      file = new RandomAccessFileV( Path + "/" + ft, "rw" );

      if(!HInit)
      {
        Virtual = new VHex( file, true ); Offset = new VHex( file, false );
        Offset.enableText( textV ); Virtual.enableText( textV );
        HInit = true;
      }
      else
      {
        Offset.setTarget( file ); Virtual.setTarget( file );
      }

      if( I >= 0 )
      {
        ((ExploerEventListener)UsedDecoder).read( Path + Sep + ft, file );

        openFile();
      }
      else { editMode(); }

      f.pack(); f.setLocationRelativeTo(null);
    }
    catch(Exception er)
    {
      JOptionPane.showMessageDialog(null,"Need Administrative privilege to read this file"); Reset();
    }
  }

  //Exit file format reader.

  public void Reset()
  {
    Open = false; new FileIconManager().Open = false; fileChooser();
  }
  
  public void mouseExited(MouseEvent e) { }
  
  public void mouseEntered(MouseEvent e) { }
  
  public void mouseReleased(MouseEvent e) { }

  public void mouseClicked(MouseEvent e) { }
  
  //Handel tree clicked events.

  public void mousePressed(MouseEvent e)
  {
    int r = tree.getRowForLocation( e.getX(), e.getY() );

    if( diskMode )
    {
      String p = tree.getLastSelectedPathComponent().toString();

      p = p.substring( p.lastIndexOf(35) + 1, p.lastIndexOf(46) );

      try
      {
        file = new RandomAccessFileVS( p, "r" );

        if(!HInit)
        {
          Virtual = new VHex( file, true ); Offset = new VHex( file, false );
          Offset.enableText( textV ); Virtual.enableText( textV );
          HInit = true;
        }
        else
        {
          Offset.setTarget( file ); Virtual.setTarget( file );
        }
        
        editMode();
      }
      catch(Exception er)
      {
        er.printStackTrace();
        JOptionPane.showMessageDialog(null,"Can't Open disk!"); Reset();
      }
    }
    else if( !Open && r != -1 )
    {
      if( tree.getLastSelectedPathComponent() != null )
      {
        String p = tree.getLastSelectedPathComponent().toString();
        
        if( e.getClickCount() == 2 )
        {
          checkFT(p);
        }
      }
    }
    else
    {
      ((ExploerEventListener)UsedDecoder).elementOpen(tree.getLastSelectedPathComponent().toString());
    }
  }

  public void valueChanged(TreeSelectionEvent e) { }

  public int DefaultProgram(String EX)
  {
    for( int i = 0; i < Suports.length; i++ )
    {
      if( Suports[i].equals(EX) )
      {
        return(i);
      }
    }

    return( -1 );
  }
}
