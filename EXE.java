import javax.swing.*;
import java.io.*;
import javax.swing.tree.*;
import java.awt.*;
import EXEDecode.*;

public class EXE extends DefaultWindowCompoents implements ExploerEventListener
{
//the output table
public JTable out=new JTable();

public JTable DebugOut[]=new JTable[5];

//plug in the executable Readers

public Headers Header=new Headers();

public DLLImport DLL=new DLLImport();
public static Data data=new Data();

public Resource RSRC=new Resource();

public static VraReader c;
public static String File="";

public EXE(){UsedDecoder=this;}

//*****************************************plug the seprate decoders of the exe format togather

public void Read(String F){((DefaultTreeModel)tree.getModel()).setRoot(null);
tree.setRootVisible(true);tree.setShowsRootHandles(true);
DefaultMutableTreeNode root=new DefaultMutableTreeNode(F);

//header data folder to organize exe data batter
DefaultMutableTreeNode Headers=new DefaultMutableTreeNode("Header Data");

try{File=F;c=new VraReader(new File(F));

try{DebugOut[0]=Header.ReadMZ(c);}catch(Exception e){}
try{DebugOut[1]=Header.ReadPE(c);}catch(Exception e){}
try{DebugOut[2]=Header.ReadOP(c);}catch(Exception e){}
try{DebugOut[3]=Header.ReadDataDrectory(c);}catch(Exception e){}
try{DebugOut[4]=Header.ReadSections(c);}catch(Exception e){}

Headers.add(new DefaultMutableTreeNode("MZ Header.h"));
Headers.add(new DefaultMutableTreeNode("PE Header.h"));
Headers.add(new DefaultMutableTreeNode("OP Header.h"));
Headers.add(new DefaultMutableTreeNode("Data Directory Array.h"));
Headers.add(new DefaultMutableTreeNode("Maped EXE SECTOINS TO RAM.h"));

root.add(Headers);

//Location of the export directory

c.SetVraMode(true);

if(data.DataDirUsed[0]){DefaultMutableTreeNode Export=new DefaultMutableTreeNode("function Export Table.h");

root.add(Export);}

//Location of the import directory

if(data.DataDirUsed[1]){DefaultMutableTreeNode Import=new DefaultMutableTreeNode("DLL Import Table");
Import=DLL.LoadDLLImport(Import,c);root.add(Import);}

//Location of the resource directory

if(data.DataDirUsed[2]){DefaultMutableTreeNode RE=new DefaultMutableTreeNode("Resource Files");
RE=RSRC.Decode(RE,c);root.add(RE);}

//Exception

if(data.DataDirUsed[3]){DefaultMutableTreeNode EX=new DefaultMutableTreeNode("Exception Table.h");

//decoder goes here

root.add(EX);}

//Security

if(data.DataDirUsed[4]){DefaultMutableTreeNode Security=new DefaultMutableTreeNode("Security Leval Settings.h");

//decoder goes here

root.add(Security);}

//Relocation/Patching

if(data.DataDirUsed[5]){DefaultMutableTreeNode RELOC=new DefaultMutableTreeNode("Relocation/Patching.h");

//decoder goes here

root.add(RELOC);}

//Debug

if(data.DataDirUsed[6]){DefaultMutableTreeNode DEBUG=new DefaultMutableTreeNode("DEBUG TABLE.h");

//decoder goes here

root.add(DEBUG);}

//Decription/Architecture

if(data.DataDirUsed[7]){DefaultMutableTreeNode Decription=new DefaultMutableTreeNode("Decription/Architecture.h");

//decoder goes here

root.add(Decription);}

//Machine Value

if(data.DataDirUsed[8]){DefaultMutableTreeNode MV=new DefaultMutableTreeNode("Machine Value.h");

//decoder goes here

root.add(MV);}

//Thread Storage

if(data.DataDirUsed[9]){DefaultMutableTreeNode TS=new DefaultMutableTreeNode("Thread Storage Lowcation.h");

//decoder goes here

root.add(TS);}

//Load System Configuration

if(data.DataDirUsed[10]){DefaultMutableTreeNode ConFIG=new DefaultMutableTreeNode("Load System Configuration.h");

//decoder goes here

root.add(ConFIG);}

//Location of alternate import-binding director

if(data.DataDirUsed[11]){DefaultMutableTreeNode BoundImport=new DefaultMutableTreeNode("Import Table of Functions inside program.h");

//decoder goes here

root.add(BoundImport);}

//Import Address Table

if(data.DataDirUsed[12]){DefaultMutableTreeNode ImportAddress=new DefaultMutableTreeNode("Import Address Settup Table.h");

//decoder goes here

root.add(ImportAddress);}

//Delayed Imports

if(data.DataDirUsed[13]){DefaultMutableTreeNode DelayImport=new DefaultMutableTreeNode("Delayed Import Table LOL.h");

//decoder goes here

root.add(DelayImport);}

//COM Runtime Descriptor

if(data.DataDirUsed[14]){DefaultMutableTreeNode COM=new DefaultMutableTreeNode("COM Runtime Descriptor.h");

//decoder goes here

root.add(COM);}

((DefaultTreeModel)tree.getModel()).setRoot(root);f.setVisible(true);}
catch(Exception e){JOptionPane.showMessageDialog(null,"erorr "+e);}}

//****************************************************Change What To Display Based on what the user clicks on

public void ElementOpen(String h){


if(h.equals("MZ Header.h")){out=DebugOut[0];}
else if(h.equals("PE Header.h")){out=DebugOut[1];}
else if(h.equals("OP Header.h")){out=DebugOut[2];}
else if(h.equals("Data Directory Array.h")){out=DebugOut[3];}
else if(h.equals("Maped EXE SECTOINS TO RAM.h")){out=DebugOut[4];}
else if(h.equals("DLL IMPORT ARRAY DECODE.H")){out=data.DLLTable[0];}

//for each dll name has a table used for it

else if((h.lastIndexOf(35)>0)&(h.substring((h.lastIndexOf(35)+1),(h.lastIndexOf(35)+2))).equals("D"))
{out=data.DLLTable[Integer.parseInt(h.substring((h.lastIndexOf(35)+2),h.length()))];}

//extract an resouce file

else if((h.lastIndexOf(35)>0)&(h.substring((h.lastIndexOf(35)+1),(h.lastIndexOf(35)+2))).equals("R"))
{
try{int v=JOptionPane.showConfirmDialog(null,"Extract File As A Icon","Resouce Decoder",JOptionPane.YES_NO_OPTION);

if(v==JOptionPane.YES_OPTION){RSRC.ExtractFileAsIcon(Integer.parseInt(h.substring((h.lastIndexOf(35)+2),h.length())),h.substring(0,h.lastIndexOf(35)),c);}

else{RSRC.ExtractFile(Integer.parseInt(h.substring((h.lastIndexOf(35)+2),h.length())),h.substring(0,h.lastIndexOf(35)),c);}
}

catch(Exception e){System.out.println(e+"");}

}

//no decode of section

else{JOptionPane.showMessageDialog(null,"No Decoder Has Ben Created Yet By\r\nDamian Recoskie for That Section\r\nof the curent decoded program");
out=new JTable((new Object[][]{{"NO DECODER"}}),(new Object[]{"NO DECODER HAS BEN WORTE YET"}));}

f.setLayout(new GridLayout(0,2));f.getContentPane().removeAll();
f.add(tree);f.add(new JScrollPane(tree));f.add(out);f.add(new JScrollPane(out));
tree.setShowsRootHandles(true);tree.setRootVisible(true);f.setVisible(true);}

}