import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.lang.reflect.*;

public class app extends DefaultWindowCompoents implements TreeWillExpandListener,TreeSelectionListener,ActionListener,MouseListener
{
public String Path="";

public String[] History=new String[10];
public int h=-1,h2=-1;public Boolean REC=true,Debug=false;

//fully aoutomated program decoder adder for other fily types
public int UseDecoder[]=new int[]{0,0,0,0,0};
public String Suports[]=new String[]{".exe",".dll",".sys",".drv",".ocx"};
public String DecodeAPP[]=new String[]{"EXE"};

public app(){f=new JFrame("Decoder");
f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
f.setResizable(true);
JMenuBar menuBar=new JMenuBar();
JMenuItem Back=new JMenuItem("Back",new ImageIcon(app.class.getResource("AppPictures/back.png")));
JMenuItem Home=new JMenuItem("User",new ImageIcon(app.class.getResource("AppPictures/home.png")));
JMenuItem Go=new JMenuItem("Foward",new ImageIcon(app.class.getResource("AppPictures/go.png")));
JMenuItem Up=new JMenuItem("Up a Folder",new ImageIcon(app.class.getResource("AppPictures/up.png")));
JMenuItem Computer=new JMenuItem("My Computer",new ImageIcon(app.class.getResource("AppPictures/computer.png")));
menuBar.add(Computer);menuBar.add(Back);menuBar.add(Home);
menuBar.add(Go);menuBar.add(Up);
Back.setActionCommand("B");
Back.addActionListener(this);
Go.setActionCommand("G");
Go.addActionListener(this);
Computer.setActionCommand("C");
Computer.addActionListener(this);
Home.setActionCommand("H");
Home.addActionListener(this);
Up.setActionCommand("U");
Up.addActionListener(this);
tree=new JTree();dirSerach();
tree.setRootVisible(false);
tree.setShowsRootHandles(false);
tree.addTreeWillExpandListener(this);
tree.addMouseListener(this);
tree.addTreeSelectionListener(this);
tree.setCellRenderer(new FileIconManager());
f.setLayout(new GridLayout(0,1));
f.add(tree);
f.add(new JScrollPane(tree));
f.setJMenuBar(menuBar);
f.setIconImage(new ImageIcon(app.class.getResource("AppPictures/app.png")).getImage());
f.pack();f.setLocationRelativeTo(null);f.setVisible(true);}

public static void main(String[]args){new app();}

public void treeWillExpand(TreeExpansionEvent e)
{if(!Debug){Path+=tree.getLastSelectedPathComponent().toString();Path+="\\";dirSerach();}}
public void treeWillCollapse(TreeExpansionEvent e){}

public void dirSerach(){if(REC){AddToHistory(Path);}
((DefaultTreeModel)tree.getModel()).setRoot(null);
DefaultMutableTreeNode root=new DefaultMutableTreeNode("Root");
if(Path==""){File[] roots=File.listRoots();for(int i=0;i<roots.length;i++)
{DefaultMutableTreeNode temp=new DefaultMutableTreeNode(roots[i]);
temp.add(new DefaultMutableTreeNode(""));
root.add(temp);}}else{File folder=new File(Path);
File[] list=folder.listFiles();for(int i=0;i<list.length;i++)
{if(list[i].isFile()){root.add(new DefaultMutableTreeNode(fix(list[i].toString())));}
else if(list[i].isDirectory()){DefaultMutableTreeNode temp=new DefaultMutableTreeNode(fix(list[i].toString()));
temp.add(new DefaultMutableTreeNode(""));root.add(temp);}}}((DefaultTreeModel)tree.getModel()).setRoot(root);}

//get only the folder or file name not the full file path then the name
public String fix(String path){String temp="";for(int i=path.length();i>0;i--){temp=path.substring(i-1,i);
if(temp.equals("\\")){path=path.substring(i,path.length());break;}}return(path);}

//back and foward History Functions
public void AddToHistory(String p){if(h<(History.length-1)){h+=1;h2=h;History[h]=p;}
else{System.arraycopy(History,1,History,0,History.length-1);History[History.length-1]=p;}}
public void back(){if(h>1){h-=1;Path=History[h];REC=false;dirSerach();REC=true;}}
public void go(){if(h<h2){h+=1;Path=History[h];REC=false;dirSerach();REC=true;}}

//handleing menu itemes events
public void actionPerformed(ActionEvent e){Reset();
if(e.getActionCommand()=="B"){back();}if(e.getActionCommand()=="G"){go();}if(e.getActionCommand()=="C"){Path="";dirSerach();}
if(e.getActionCommand()=="H"){Path=System.getProperty("user.home")+"\\";dirSerach();}
if(e.getActionCommand()=="U"){if(Path.length()>4){String temp="";int i;Path=Path.substring(0,(Path.length()-1));
for(i=Path.length();i>0;i--){temp=Path.substring(i-1,i);if(temp.equals("\\"))
{Path=Path.substring(0,i-1);Path+="\\";break;}}if(i==0){Path="";}dirSerach();}else if(Path.length()>0){Path="";dirSerach();}}}

//check the file type
public void CheckFT(String f){String ex=f.substring((f.lastIndexOf(46)),f.length()).toLowerCase();

int I=DefaultProgram(ex);

//System.out.println(I+" the extention number");
//System.out.println("Using decoder = "+DecodeAPP[UseDecoder[I]]);

if(I>=0&!Debug){Debug=true;new FileIconManager().Debug=true;AddToHistory(Path);
try{Class.forName(DecodeAPP[UseDecoder[I]]).getConstructor().newInstance();}catch(Exception e)
{System.out.println(e.getCause()+"");
JOptionPane.showMessageDialog(null,"Unable to Loade Decode Program For This File Format");}
((ExploerEventListener)UsedDecoder).Read(Path+"\\"+f);
}

else if(!Debug){JOptionPane.showMessageDialog(null,"There is no Decoder For Your Seleceted File Format");}

else{
((ExploerEventListener)UsedDecoder).ElementOpen(f);
}
}

public void Reset(){Debug=false;new FileIconManager().Debug=false;SetDefault();}

//mouse events
public void mouseExited(MouseEvent e){}public void mouseEntered(MouseEvent e){}public void mouseReleased(MouseEvent e){}
public void mouseClicked(MouseEvent e){}public void mousePressed(MouseEvent e){int r=tree.getRowForLocation(e.getX(),e.getY());
if(r!=-1){if(tree.getLastSelectedPathComponent()!=null){String p=tree.getLastSelectedPathComponent().toString();
if(e.getClickCount()==2){CheckFT(p);}}}}

public void valueChanged(TreeSelectionEvent e){}

public int DefaultProgram(String EX){for(int i=0;i<Suports.length;i++){if(Suports[i].equals(EX)){return(i);}}return(-1);}

}