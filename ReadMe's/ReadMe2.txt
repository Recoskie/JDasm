Note this explorer program is 100% fully programable vary litle work neaded to add a file decoder

Note the lowded in decode is unloaded and loads a new decoder if decodeing a difrent file type
this is done to Optimize Ram Memory

Note All Icons of all file types stay loaded into ram for extreamly fast file browsing

Note This Browser will not draw the exectables Icons Of Exe files or draw a small Privew
Icon of a image For Grater Optimization of Spead

Note this browser is not desinged for internet use only reads drectly from disk unlike expoler
in windows wich is part of internet exploer

what do all the difrent files do

*********************************************the file App*********************************************

is the main application with the back foward up a folder and computer buttons and handlers
of what to do when thay are clicked and ceaps a history of 10 items optimized for ram and
extream spead

************************************the file DefaultWindowCompoents***********************************

is an extendsable file to be able to interact with the window drectly and change it for the output
and contains a reset function to go back to explor mode also contains the decode program that
extended the window so that the event handeling functions can be trigred in the decoder at there
corect addresses

**************************************the file ExploerEventListener**********************************

that is the interface for event handeling in a decode program like when to tell the program to start
decodeing or if a tree element whas selected to show difrent sections of the decode output
this is just an holder of what functions to call in the decoder program when the APP part of the
program sends the file to decode or wich part of the decode your looking at that are wirten in the decode
program that the app trigeres though this interface file

*****************************************the file FileIconManager************************************

this draws the icond pictures you chose for wich file types and if its reconised and draws a picture
for the file it also hides the file type exstention to make it more user frendly



these 4 programs pulg into gather and make an extreamly stable decodeing program

how do we program the main app to use a decode programe we worte for a file type

we use the vary first varibles in the top of the program wich should look like

//fully aoutomated program decoder adder for other fily types
int UseDecoder[]=new int[]{0,0};
String Suports[]=new String[]{".exe",".dll"};
String DecodeAPP[]=new String[]{"test"};

the array UseDecoder points to wich decoding program in the array Suports

so the first element of UseDecoder is 0 and the first element of Suports is .exe wich is the file exstention
of the file type so its saying it supotrs the exe format and in the UseDecoder array is wich decoder it uses
the number in UseDecoder array is used as a number position in the DecodeAPP array wich both exe and dll
format both open the decodeing program test so it's vary easy to add a decoder in the arrays

wich test is just a simple java class file it loads

so now how do we setup an icon for the file exstention once we have the default program to decode it

we open the program file FileIconManager and work with 3 arrays wich look llike the folowing

String FType[]=new String[]{".h",".exe",".dll"};
String Load[]=new String[]{"H.gif","EXE.gif","dll.gif"};
ImageIcon LoadedPic[]=new ImageIcon[3];

FType[] is the file exstention the file exstention position in array in the number that is used for wich image to load
in the Load[] string array wich are the icon images and the array LoadedPic[] has to be set to the amount of picture icons
in the Load[] array because the images perload ionto the array LoadedPic[] and are dren from there after thay load from
Load[] array but the position thay are in Load array are the Same

now once we do all this how do we write the decode program to be compatible with this program

first we extend the defaultwindow and then we implement the interface so the program can conunicate to the decoder

wich should look like this

public class DecoderPluginName extends DefaultWindowCompoents implements ExploerEventListener
{}

DecoderPluginName can be changed to the name you want to name the decoder

then it should contain a public function the same name as the file in it

public class DecoderPluginName extends DefaultWindowCompoents implements ExploerEventListener
{
public DecoderPluginName(){}
}

inside the public function of the file should be seting the varible in DefaultWindowCompoents to the cuent class file
so the app program can tigger the functions in the decode wich are

public void ElementOpen(String Element){}
public void Read(String File){}

so we create a link to the file

public class DecoderPluginName extends DefaultWindowCompoents implements ExploerEventListener
{
public DecoderPluginName(){UsedDecoder=this;}
}

then we add the functions that are in the interface and the app program calls wich are


public void ElementOpen(String Element){when a section of the decode whas clicked in the folder and file view}

public void Read(String File){the address to the file to read and decode by this decoder}

the app program sends the information to those functions though the DefaultWindowCompoents though the varible UsedDecoder
wich is the decode file link wich the keyword this is used to represent the class file

public class DecoderPluginName extends DefaultWindowCompoents implements ExploerEventListener
{
public DecoderPluginName(){UsedDecoder=this;}

public void ElementOpen(String Element){your code to decode output selected to diplay gos here}

public void Read(String File){your code to read the file and to create the decode output gos here}
}

and that is what you program should look like and it should be extreamly easy to link to the program

there are a few varibles that becomes usable with delclearing them in the decode file because when you

extends DefaultWindowCompoents in your class file the the varbles that are in the DefaultWindowCompoents are usable

there are only 4 new things though

f
tree;
UsedDecoder;
SetDefault();

f is the varible of the window you can set the layout of the graphic componet programs by accessing it and even set the windows size
add new componets and draw graphics and even get rid of the file folder view depending on the output of the decode you want

tree is the access to the file and folder view where you can manuly add strings of what you want it to draw a file like of if somthing
uses a specal name it can be used to print it as if it's a file or create a folder to put the names in

UsedDecoder this is where the link to you program is in order for the app program to call the interface functions

SetDefault(); just sets the window back to normal and in explor mode if you nead proff you can pirt the ramm memory addresses of the varibles
like this from the class or your decoder

public class DecoderPluginName extends DefaultWindowCompoents implements ExploerEventListener
{
public DecoderPluginName(){UsedDecoder=this;}

public void ElementOpen(String Element){}

public void Read(String File){System.out.println(f+"");System.out.println(tree+"");
System.out.println(UsedDecoder+"");System.out.println(SetDefault+"");}
}

print them useing the system function as soon as it gos to use it to tell the decoder the file to decode

lastly the app program extends DefaultWindowCompoents this is also whiy it can enstablis a link to the folder file view
and draw the folders and files at the path it's at and also set up a link to the decoder that is all you nead know right down to the
detail to write a decoder for this program wich is the simplest as posable when your only wordy about the decoder then all you
have to wory about is writing the decoder code and what it will disply and your done


if a company cindaly wants to add there format to my program go ahead but note the ideai decided to make this program an all purpose decoder

1. implemented a exploer to viwe files and folders on the users computer

2. implemented an MZ Header Decoder

3. implemented Data Sorting Of Excutable file and Data Selector

4. implemented A gater MZ Header Decoder Works In All Situations Of Modifcation to MZ Header

5. implemented PE Decoder to map out the sections of the executable

6. implemented OP

7. implemented icons for files that this decoder can decode

8. implemented 100% Programmeble Expoler

9. implemented a section loader for exe and dll

10. Implementing A Ram Address To Memory Address Decoder Function

11. implemented A Ram Address To Memory Address Decoder Function So The Exe Does Not Have To Be Loaded To the Ram Chip Or DLL or System Driver Excetera

12. implemented Resouce Decoder DIR Scaner