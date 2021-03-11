package WindowComponents;

import javax.swing.*;
import VHex.*;
import dataTools.*;
import cellPane.*;

public class WindowComponents
{
  //Main application Window.

  public static JFrame f;

  //File chooser menu bar.

  public static JMenuBar fcBar;

  //Binary tool options, and views.

  public static JMenuBar bdBar;

  //Menu item actions.

  public static JPopupMenu pm;

  //File chooser tree, or data from a file format reader.

  public static JTree tree;
  public static JScrollPane stree;

  //Additional detailed information output. For data in table cells, or section.
  //Also disassembly output.

  public static JTextPane infoData = new JTextPane();
  public static JScrollPane iData;

  //Hex editor.

  public static VHex Virtual, Offset;

  //Data type inspector tool.

  public static dataInspector di;

  //Data descriptor tool.

  public static dataDescriptor ds;

  //Component layout system.

  public static CellPane tools;

  //Once hex editor is initialized. Then the target is set afterwards for new files.

  public static boolean HInit = false;

  //Hex editor view options.

  public static boolean textV = true;

  //The current file reader. Used for handling events to decode for the section of interest in the tree.

  public static Object UsedDecoder;

  //Additional info text.

  public static void info( String s ) { infoData.setText(s); }
}