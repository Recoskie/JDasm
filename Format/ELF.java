package Format;

import swingIO.*;
import swingIO.tree.*;
import javax.swing.tree.*;
import Format.ELFDecode.*;

import core.x86.*;

public class ELF extends Data implements JDEventListener
{
  private static boolean init = false;

  //Descriptors.

  private Descriptor[][] des = new Descriptor[7][];

  private JDNode root;

  //ELF reader plugins.

  private static Headers header = new Headers();

  private static final sec[] Reader = new sec[]
  {
    new libReader(),
    new symReader(), //Symbol table Sections.
    new relocReader(), //Relocations.
    new arrayReader(), //init-fini array reader.
    null, //Local thread storage.
  };

  public static final String[] SInfo = new String[]
  {
    //ELF headers.
    "<html>ELF application Has three headers.<br /><br />" +
    "The ELF header defines the CPU type, and start address of the program after all the headers are read.<br /><br />" +
    "The ELF header defines the location to the \"Program Header\", and \"Section header\".<br /><br />" +
    "The \"program header\" defines the link libraries, and section that must be loaded, or run before calling the start address of the program.<br /><br />" +
    "The \"Section header\" gives every section of the program a name. It defines the rest of the program such as debugging information if any.<br /><br />" +
    "After the \"program header\" sections are executed, and loaded, and all named \"sections\" placed in memory. Then the programs start address is called.<br /><br /><hr /><br />" +
    "The ELF binary format is used in PlayStation Portable, PlayStation Vita, PlayStation (console), PlayStation 2, PlayStation 3, PlayStation 4, PlayStation 5, GP2X, Dreamcast, Gamecube, Wii, Wii U.<br /><br />" +
    "Each consol uses a unique signature code before the ELF, and may also include an location to the picture of the game before the main ELF. A small set of plugins may be added to read these headers that plug in with the ELF reader.<br /><br />" +
    "The ELF format is also used in Android to compile java android apps, and is also used by all Linux/Unix operating systems.</html>",
    //Code Sections.
    "<html>Note that the \"program header entires\" are run before jumping the CPU to the start address of the program.<br /><br />" +
    "The \".init\" section is usually run by the \"program header\" before the \"section header\" maps it as a named section called \".init\".<br /><br />" +
    "We do not have to call it a \".init\" section. As sections that have runnable processor instructions are defined by flag setting.<br /><br />" +
    "The \".fini\" section is the termination code that is called to exit the program. Note that \".fini\" has no program header entire, because program would exit before start.<br /><br /><hr /><br />" +
    "The CPU instructions in sections usually named \".plt\", and \".plt.got\" reads a location in named sections \".got\", or \".got.plt\" (global pointer table), and calls a link library method.<br /><br />" +
    "To understand how the global pointer addresses are configured see the \"link library section\".<br /><br /><hr /><br />" +
    "The \".text\" section is usually the set program start address defined in the ELF header. Which is run after all headers are read.</html>",
    //Link libraries.
    "<html>Note that the program header entires are run before jumping the CPU to the start address of the program.<br /><br />" +
    "The \".dynamic\" section is usually run by the \"program header\" before the \"section header\" maps it as \".dynamic\". This section tells us which link libraries to load.<br /><br />" +
    "The symbol section tells us which method names. The relocation sections tell us where to place the address of symbol names.<br /><br />" +
    "The link library section locates to symbol section (mandatory), and relocation section (mandatory). You can look under the folder \"Setup data\", for details.</html>",
    //String tables.
    "<html>An string table is just a set of text with a 00 hex code for the end of the text.<br /><br />" +
    "The \"section header\" names is a string section type usually named \".shstrtab\".<br /><br />" +
    "The link library section usually uses a string table named \".dynstr\".</html>",
    //Symbol information.
    "<html>Defines methods in link library section, and also defines variables names, and functions within the program.<br /><br />" +
    "In some cases the symbols have no address, or size. Then we have to read the relocation section. The relocation section tells us which symbol is which address in (global pointer table).<br /><br />" +
    "The addresses the relocations locate to usually are sections named \".got\", and \".got.plt\" (global pointer table). Some symbols might have a defined location, and size if they are not dynamically loaded.</html>",
    //Relocation.
    "<html>All locations would be correct if the locations the ELF header specifies to put sections into RAM are not already used.<br /><br />" +
    "Also relocations do more than just add difference in location to addresses. Some relocation types are used to set an address to a loaded link library/method.<br /><br />" +
    "The symbol table tells us the name, and type of data, but some symbols have zero size, and zero location. Relocations tell us which symbol, and the address that needs to locate to the method.<br /><br />" +
    "The relocations usually locate to the sections named \".got\", or \".got.plt\" (global pointer table). You can use unique names if you like though.<br /><br />" +
    "In the case of this disassembler. We need to read the symbols, and then map there address given in the relocation section.<br /><br />" +
    "Don't forget that the machine code instructions in the \".plt\", and \".plt.got\" sections should read and jump to the locations.<br /><br /></html>",
    //Thread local storage.
    "<html></html>",
    //The init, fini, pre-init array sections.
    "<html>The init, and pre-init array stores an array of address locations to the program header entires that run before the programs start address defined in the ELF header.<br /><br />" +
    "The fini array is sections that exit the program, or terminate the processes.</html>",
    //Notes sections.
    "<html></html>",
    //Sections defined as data only.
    "<html>Some sections are marked as data only. Such sections may be file data, or sections used by external tools.<br /><br />" +
    "<html>Note that the global pointer table is usually used with the relocation addresses, for dynamically loaded libraries, and functions.<br /><br />" +
    "<table border=\"1\">" +
    "<tr><td>Section Name.</td><td>Use</td></tr>" +
    "<tr><td>.rodata</td><td>This section holds read-only data.</td></tr>" +
    "<tr><td>.data</td><td>This section holds initialize data that contribute to the program's memory.</td></tr>" +
    "<tr><td>.comment</td><td>This section holds version control information.</td></tr>" +
    "<tr><td>.got</td><td>This section holds the global offset table.</td></tr>" +
    "<tr><td>.interp</td><td>This section holds the pathname of a program interpreter.</td></tr>" +
    "<tr><td>.gnu_debuglink</td><td>This tells GDB the file that stores the debug information.</td></tr>" +
    "<tr><td>.eh_frame</td><td>Not needed (call frame), but is used by a program called DWARF, for debugging (try and catch).</td></tr>" +
    "<tr><td>.eh_framehdr</td><td>Not needed (call frame), but is used by a program called DWARF, for debugging (try and catch).</td></tr>" +
    "</table></html>"
  };

  //Expand sections once on single click.

  private boolean[] expandOnce = new boolean[]{ true, true, true, true, true, true, true, true, true, true };

  //Read the ELF binary.

  public ELF()
  {
    tree.setEventListener( this ); init = false;

    file.Events = false;

    //Root node is now the target file.
 
    ((DefaultTreeModel)tree.getModel()).setRoot(null); tree.setRootVisible(true); tree.setShowsRootHandles(true); root = new JDNode( fc.getFileName() + ( fc.getFileName().indexOf(".") > 0 ? "" : ".elf" ) );

    //The ELF header.
  
    JDNode ELFHeader = new JDNode( "ELF Header.h", new long[]{ 0, 0 } );
    JDNode PHeader = new JDNode( "Program Header", new long[]{ 0, 1 } );
    JDNode SECHeader = new JDNode( "Section Header", new long[]{ 1, 0 } );

    for( int i = 0; i < sections.length; i++ ) { if ( sections[i].getChildCount() > 0 ) { sections[i].removeAllChildren(); } }
    
    des[0] = new Descriptor[3];

    try
    {
      des[0][0] = header.readELF(); sections[0].add(ELFHeader); 
      if( !Data.error && Data.programHeader != 0 ) { des[0][1] = header.readProgram(PHeader); sections[0].add(PHeader); }
      if( !Data.error && Data.Sections != 0 ) { des[1] = header.readSections(SECHeader); sections[0].add(SECHeader); }
    }
    catch(Exception e) { Data.error = true; }

    for( int i = 0; i < sections.length; i++ ) { if ( sections[i].getChildCount() > 0 ) { root.add( sections[i] ); } }

    if( !Data.error )
    {
      //Load processor core type.

      if( coreType == 0x0003 || coreType == 0x003E )
      {
        if( core == null || core.type() != 0 ){ core = new X86( file ); } else { core.setTarget( file ); }

        core.setBit( is64Bit ? X86.x86_64 : X86.x86_32 );
              
        core.setEvent( this::Dis ); coreLoaded = true;
      }
      else { coreLoaded = false; }

      if( core != null ) { core.setAddressMode(true); }

      //Machine code start pos.

      sect t; for( int l = st.length - 1; l > 0; l-- )
      {
        t = st[l]; if( start >= t.virtual && start < ( t.virtual + t.size ) )
        {
          int El = -1; for( int n = 0; n < sections[1].getChildCount(); n++ )
          {
            if( ((JDNode)sections[1].getChildAt(n)).getUserObject().toString().startsWith(t.Name) ){ El = n; break; }
          }

          if( t.virtual == start ) { root.add( new JDNode( "Program Start (Machine code).h", new long[]{ -4, 1, El } ) ); break; }
          else
          {
            root.add( new JDNode( "Program Start (Machine code).h", new long[]{ -1, start, ( t.virtual + t.size ) - start } ) ); break;
          }
        }
      }

      //Decode the setup headers.
    
      ((DefaultTreeModel)tree.getModel()).setRoot(root); file.Events = true;

      //Set the default node.

      tree.setSelectionPath( new TreePath( sections[0].getPath() ) ); open( new JDEvent( this, "", 0 ) );
    }
    else{ file.Events = true; }
  }

  public void Uninitialize()
  {
    des = new Descriptor[7][]; st = null; sym_names = null; sym_pos = null;

    sections = new swingIO.tree.JDNode[]
    {
      new swingIO.tree.JDNode("Headers", 0), //Headers.
      new swingIO.tree.JDNode("Code Sections", 1), //Sections marked as runnable code.
      new swingIO.tree.JDNode("Link library Sections", 2), //Linked libraries method sections.
      new swingIO.tree.JDNode("String Table Sections", 3), //String table sections.
      new swingIO.tree.JDNode("Symbol Sections", 4), //Defines methods in link library section, and defines code and variables.
      new swingIO.tree.JDNode("Relocation Sections", 5), //Relocations to be applied if program loads in different address.
      new swingIO.tree.JDNode("Local thread storage Sections", 6), //The local thread storage section.
      new swingIO.tree.JDNode("Array Sections", 7), //Array of init, fini, pre-init.
      new swingIO.tree.JDNode("Notes Sections", 8), //The section notes sections.
      new swingIO.tree.JDNode("Other Sections", 9) //Sections that are marked as straight data with no type.
    };

    if( core != null ) { core.clear(); }
  }

  public void open( JDEvent e )
  {
    if( e.getID().equals("UInit") ) { Uninitialize(); }

    else if( e.getArgs().length == 1 )
    {
      if( e.getArg(0) >= 0 )
      {
        if( expandOnce[ (int)e.getArg(0) ] ){ expandOnce[ (int)e.getArg(0) ] = false; tree.expandPath( tree.getLeadSelectionPath() ); }
        info(SInfo[(int)e.getArg(0)]); ds.clear();
      }
      else
      {
        info("<html></html>"); ds.clear();
      }
    }

    else if( e.getArg(0) < 0 )
    {
      if( e.getArg(0) == -1 )
      {
        if( coreLoaded )
        {
          if( !init ) { open(new JDEvent(this, "", new long[]{4,0})); }

          core.clear();

          if(e.getArgs().length > 2)
          {
            try
            {
              core.Linear.add(e.getArg(1)); core.Linear.add(e.getArg(2)); core.disLoc(0, false);
            }
            catch( Exception err ) { err.printStackTrace(); }
          }
          else
          {
            core.Crawl.add(e.getArg(1)); core.disLoc(0, true);
          }

          ds.setDescriptor( core );
        }
        else { try{ file.seekV( e.getArg(1) ); Virtual.setSelected( e.getArg(1), e.getArg(1) ); } catch(Exception er) { } noCore(); }
      }
      else if( e.getArg(0) == -2 )
      {
        try
        {
          file.seekV( e.getArg(2) ); file.seek( e.getArg(1) );
          Offset.setSelected( e.getArg(1), e.getArg(1) + e.getArg(3) - 1 );
          Virtual.setSelected( e.getArg(2), e.getArg(2) + e.getArg(3) - 1 );
          info("<html></html>"); ds.clear();
        }
        catch( Exception er ) { } 
      }
      else if( e.getArg(0) == -3 )
      {
        try
        {
          file.seekV( e.getArg(1) );
          Virtual.setSelected( e.getArg(1), e.getArg(1) + e.getArg(2) - 1 );
          info("<html></html>"); ds.clear();
        }
        catch( Exception er ) { } 
      }

      //Link to section. Some sections define which sections to use. So it makes sense to link to the categorized sections.

      else if( e.getArg(0) == -4 )
      {
        JDNode  n = ((JDNode)sections[(int)e.getArg(1)].getChildAt((int)e.getArg(2)));

        tree.scrollPathToVisible( new TreePath( n.getPath() ) );

        tree.setSelectionPath( new TreePath( n.getPath() ) );
        
        open( new JDEvent( this, "", n.getArgs() ) );
      }
    }
    else if( e.getArgs().length > 1 )
    {
      if( des[ (int)e.getArg(0) ] == null )
      {
        if( Reader[ (int)e.getArg(0) - 2 ] == null )
        {
          try
          {
            file.seekV( e.getArg(1) ); Virtual.setSelected( e.getArg(1), e.getArg(1) + e.getArg(2) - 1 );
            Offset.setSelected( file.getFilePointer(), file.getFilePointer() + e.getArg(2) - 1 );
          }
          catch( Exception er ) { } 

          info("<html>There is currently no reader for this section yet.</html>"); ds.clear();
        }
        else
        {
          file.Events = false;

          try
          {
            //WE can not read the dynamic symbol table if we do not read the link library section.
            //Before we read the relocations. We must also load the link library, and map the symbols.

            if( ( e.getArg(0) == 3 || e.getArg(0) == 4 ) && sections[2].getChildCount() > 0 && des[2] == null )
            {
              des[2] = Reader[0].read(); tree.expandPath( new TreePath( ((JDNode)sections[2].getFirstChild()).getPath() ) );
            }

            //if relocation we must map symbols.

            if( e.getArg(0) == 4 && sections[3].getChildCount() > 0 && des[3] == null )
            {
              des[3] = Reader[1].read(); tree.expandPath( new TreePath( ((JDNode)sections[4].getFirstChild()).getPath() ) ); init = true;
            }

            //Read section user selected.

            des[ (int)e.getArg(0) ] = Reader[ (int)e.getArg(0) - 2 ].read();
          }
          catch( Exception er ) { er.printStackTrace(); }

          file.Events = true;

          for( int i = 0, el = (int)e.getArg(0) - 2, size = sections[el].getChildCount(); i < size; i++ )
          {
            ((DefaultTreeModel)tree.getModel()).nodeChanged( sections[el].getChildAt(i) );
          }

          tree.expandPath( new TreePath( tree.getSelectionPath().getPath() ) );
          open( new JDEvent( this, "", ((JDNode)tree.getLastSelectedPathComponent()).getArgs() ) );
        }
      }

      //Section descriptors. Only exist after section is read.

      else { ds.setDescriptor( des[ (int)e.getArg(0) ][ (int)e.getArg(1) ] ); }
    }
  }

  public void noCore() { info("<html>The processor core engine is not supported.</html>"); }
}
