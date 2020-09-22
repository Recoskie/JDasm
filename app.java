import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.lang.reflect.*;

//New file components.

import RandomAccessFileV.*;
import VHex.*;

public class app extends DefaultWindowCompoents implements TreeWillExpandListener, TreeSelectionListener, ActionListener, MouseListener
{
  //Current file path.

  public String Path = "";

  //The file system stream to the file type.

  public static RandomAccessFileV file;

  //File path history.

  public String[] History = new String[10];

  //File path index.

  public int h = -1, h2 = -1;

  //Enable, or disable file path recoding.

  public boolean REC = true;

  //Converts the file chooser tree into binary data sections. For binary file format readers.
  
  public boolean Debug = false;

  //Each integer is the file decoder to use by file extensions.

  public int UseDecoder[] = new int[] { 0, 0, 0, 0, 0 };

  //Supported file format extensions.

  public String Suports[] = new String[] { ".exe", ".dll", ".sys", ".drv", ".ocx" };

  //The file to load. To begin decoding file types.

  public String DecodeAPP[] = new String[]{ "EXE" };

  //Create the application.

  public app()
  {
    f = new JFrame("Decoder"); f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //File chooser controls.

    fcBar = new JMenuBar();

    JMenuItem Back = new JMenuItem( "Back", new ImageIcon( app.class.getResource( "AppPictures/back.png" ) ) );
    JMenuItem Home = new JMenuItem( "User", new ImageIcon( app.class.getResource( "AppPictures/home.png" ) ) );
    JMenuItem Go = new JMenuItem( "Forward", new ImageIcon( app.class.getResource( "AppPictures/go.png" ) ) );
    JMenuItem Up = new JMenuItem( "Up a Folder", new ImageIcon( app.class.getResource( "AppPictures/up.png" ) ) );
    JMenuItem Computer = new JMenuItem( "My Computer", new ImageIcon( app.class.getResource( "AppPictures/computer.png" ) ) );

    fcBar.add( Computer ); fcBar.add( Back ); fcBar.add( Home ); fcBar.add( Go ); fcBar.add( Up );
  
    //Action commands.
  
    Back.setActionCommand( "B" ); Back.addActionListener(this);
    Go.setActionCommand( "G" ); Go.addActionListener(this);
    Computer.setActionCommand( "C" ); Computer.addActionListener(this);
    Home.setActionCommand( "H" ); Home.addActionListener(this);
    Up.setActionCommand( "U" ); Up.addActionListener(this);

    //Binary tools menu bar.

    bdBar = new JMenuBar();

    JMenu fm = new JMenu("File");
    JMenu vm = new JMenu("View");
 
    JMenuItem f1 = new JMenuItem("Open new File");

    JMenuItem v1 = new JMenuItem("Toggle text View");
    JMenuItem v2 = new JMenuItem("Toggle virtual space View");

    fm.add(f1); vm.add(v1); vm.add(v2);

    bdBar.add(fm); bdBar.add(vm);
  
    //add ActionListener to menuItems.
    
    f1.addActionListener(this); v1.addActionListener(this); v2.addActionListener(this);

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
    if( !Debug )
    {
      Path += tree.getLastSelectedPathComponent().toString(); Path += "\\"; dirSerach();
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
        
      if(temp.equals("\\"))
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
      h+=1;Path=History[h];
      
      REC = false; dirSerach(); REC = true;
    }
  }

  //handling menu item events
  
  public void actionPerformed(ActionEvent e)
  {
    //Basic file path commands.
    
    if( e.getActionCommand() == "B" ) { back(); }
    
    if( e.getActionCommand() == "G" ) { go(); }
    
    if( e.getActionCommand() == "C" ) { Path = ""; dirSerach(); }
    
    if( e.getActionCommand() == "H" ) { Path = System.getProperty("user.home")+"\\"; dirSerach(); }

    //Up one folder.
    
    if( e.getActionCommand() == "U" )
    {
      if( Path.length() > 4 )
      {
        String temp = ""; int i; Path = Path.substring( 0, ( Path.length() - 1 ) );

        for( i = Path.length(); i > 0; i-- )
        {
          temp = Path.substring( i - 1, i );
          
          if( temp.equals("\\") )
          {
            Path = Path.substring( 0, i - 1 ); Path += "\\"; break;
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

    //Binary tool display controls.

    else if( e.getActionCommand().equals("Toggle text View") )
    {
      textV = !textV; Offset.enableText( textV ); Virtual.enableText( textV );
    }

    else if( e.getActionCommand().equals("Toggle virtual space View") )
    {
      addV = !addV; if(!Debug) { editMode(); } else { updateWindow(); }
    }

    else if( e.getActionCommand().equals("Open new File") )
    {
      Reset();
    }
  }

  //check the file type

  public void checkFT(String ft)
  {
    String ex = ft.substring( ( ft.lastIndexOf(46) ), ft.length() ).toLowerCase();

    //If file format is Supported. Open in reader with binary tools.

    int I = DefaultProgram( ex );

    if( I >= 0 & !Debug )
    {
      Debug = true; new FileIconManager().Debug = true; AddToHistory(Path);

      try
      {
        Class.forName(DecodeAPP[UseDecoder[I]]).getConstructor().newInstance();
      }
      catch(Exception e)
      {
        JOptionPane.showMessageDialog(null,"Unable to Load Decode Program For This File Format!");
      }

      try
      {
        file = new RandomAccessFileV( Path + "\\" + ft, "rw" );

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

        ((ExploerEventListener)UsedDecoder).read( Path + "\\" + ft, file );

        updateWindow(); f.pack(); f.setLocationRelativeTo(null);
      }
      catch(Exception er)
      {
        JOptionPane.showMessageDialog(null,"Need Administrative privilege to read this file"); Reset();
      }
    }

    //Else open file in binary tools.

    else if( !Debug )
    {
      try
      {
        file = new RandomAccessFileV( Path + "\\" + ft, "rw" );

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
        
        editMode(); f.pack(); f.setLocationRelativeTo(null);
      }
      catch(Exception er)
      {
        JOptionPane.showMessageDialog(null,"Need Administrative privilege to read this file"); Reset();
      }
    }

    else
    {
      ((ExploerEventListener)UsedDecoder).elementOpen(ft);
    }
  }

  //Exit file format reader.

  public void Reset()
  {
    Debug = false; new FileIconManager().Debug = false; fileChooser();
  }
  
  public void mouseExited(MouseEvent e) { }
  
  public void mouseEntered(MouseEvent e) { }
  
  public void mouseReleased(MouseEvent e) { }

  public void mouseClicked(MouseEvent e) { }
  
  //Handel tree clicked events.

  public void mousePressed(MouseEvent e)
  {
    int r = tree.getRowForLocation( e.getX(), e.getY() );

    if( r != -1 )
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