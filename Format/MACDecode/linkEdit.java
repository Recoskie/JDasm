package Format.MACDecode;

import swingIO.tree.JDNode;
import swingIO.Descriptor;

public class linkEdit extends Data
{
  //Show full decoding of the method binding information.

  public String bindInfo( long pos, long end )
  {
    byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); file.Events = false; file.seek( pos ); Offset.setSelected( pos, end - 1 ); file.read(d); } catch( java.io.IOException er ) {}

    String out = binding + "<table border='1'><tr><td>Hex</td><td>Description</td><td>Value</td><td>Current location</td><td>Current name</td><td>Current Flags</td><td>Current bind type</td></tr>";

    int Pos = 0, End = d.length;

    String bind_type = "pointer", opcodeh = "", hex1 = "", hex2 = "", name = "", s = is64bit ? "8" : "4", fmt = is64bit ? "%1$016X" : "%1$08X";
    long loc = 0, bloc = 0, offset = 0, mask = is64bit ? -1 : 0x00000000FFFFFFFFL;
    int opcode = 0, arg = 0, bpos = 0, count = 0, adj = 0;

    int bindType = 1, flag = 0;

    try
    {
      while( Pos < End )
      {
        opcodeh = String.format("%1$02X", d[Pos] ); opcode = d[Pos]; arg = opcode & 0x0F; opcode &= 0xF0; bloc = loc; adj = opcode >= 0x90 && opcode <= 0xC0 ? ( is64bit ? 8 : 4 ) : 0; count = opcode == 0x20 || opcode == 0x60 ? 0 : 1;

        if( opcode == 0x00 ) { loc = 0; flag = 0; bind_type = "pointer"; bindType = 1; name = ""; } else if( opcode == 0xB0 ) { offset = is64bit ? arg << 3 : arg << 2; }

        else if( opcode == 0x50 ) { bindType = arg; if( bindType == 1 ){ bind_type = "pointer"; } else if( bindType == 2 ) { bind_type = "relative"; } else if( bindType == 3 ) { bind_type = "absolute"; } else { bind_type = "???"; } }

        else if( opcode == 0x40 ) { name = ""; flag = arg; Pos += 1; while( d[Pos] != 0x00 ) { hex1 += String.format("%1$02X", d[Pos] ) + " "; name += (char)d[Pos]; Pos += 1; } hex1 += String.format("%1$02X", d[Pos] ); }

        else if ( opcode == 0x20 || opcode == 0x60 || opcode == 0x70 || opcode == 0x80 || opcode == 0xA0 || opcode == 0xC0 )
        {
          if( opcode == 0x70 ) { bloc = loc = segment.get( arg ); }

          if( opcode == 0xC0 )
          {
            Pos += 1; count = 0; while( d[Pos] < 0 ) { hex1 += String.format("%1$02X", d[Pos] ) + " "; count |= ( d[Pos++] & 0x7F ) << bpos; bpos += 7; } 
            hex1 += String.format("%1$02X", d[Pos] ) + " "; count |= d[Pos] << bpos; bpos = 0;
          }

          Pos += 1; while( d[Pos] < 0 ) { hex2 += String.format("%1$02X", d[Pos] ) + " "; offset |= ( (long)d[Pos++] & 0x7F ) << bpos; bpos += 7; } 
          hex2 += String.format("%1$02X", d[Pos] ) + " "; offset |= (long)d[Pos] << bpos; bpos = 0;
        }

        loc += ( offset + adj ) * count; loc -= adj; loc &= mask;

        if( opcode == 0x00 )
        {
          out += "<tr><td>" + opcodeh + "</td><td>Reset.</td><td>Opcode</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
        }
        else if( opcode == 0x10 )
        {
          out += "<tr><td>" + opcodeh + "</td><td>Set library ordinal = " + arg + "</td><td>Opcode</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
        }
        else if( opcode == 0x20 )
        {
          out += "<tr><td>" + opcodeh + "</td><td>Set library ordinal.</td><td>Opcode</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
          out += "<tr><td>" + hex2 + "</td><td>Set library ordinal = " + offset + "</td><td>dyld = " + offset + "</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
        }
        else if( opcode == 0x40 )
        {
          out += "<tr><td>" + opcodeh + "</td><td>Set Symbol name</td><td>Opcode</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
          out += "<tr><td>" + hex1 + "</td><td>Symbol name</td><td>" + name + "</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
        }
        else if( opcode == 0x50 )
        {
          out += "<tr><td>" + opcodeh + "</td><td>Set Bind loc type " + bindType + ".</td><td>Opcode</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
        }
        else if( opcode == 0x60 )
        {
          out += "<tr><td>" + opcodeh + "</td><td>Set addend</td><td>Opcode</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
          out += "<tr><td>" + hex2 + "</td><td>Addend(" + offset + ")</td><td>Addend = " + offset + "</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";

        }
        else if( opcode == 0x70 )
        {
          out += "<tr><td>" + opcodeh + "</td><td>Set loc to segment " + arg + "</td><td>Opcode</td><td>" + String.format(fmt, bloc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
          out += "<tr><td>" + hex2 + "</td><td>loc + " + offset + "</td><td>offset = " + offset + "</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
        }
        else if( opcode == 0x80 )
        {
          out += "<tr><td>" + opcodeh + "</td><td>Add loc to offset</td><td>Opcode</td><td>" + String.format(fmt, bloc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
          out += "<tr><td>" + hex2 + "</td><td>Loc + " + offset + "</td><td>Offset = " + offset + "</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
        }
        else if( opcode == 0x90 )
        {
          out += "<tr><td>" + opcodeh + "</td><td>Bind method to location</td><td>Opcode (loc + " + s + ")</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
        }
        else if( opcode == 0xA0 )
        {
          out += "<tr><td>" + opcodeh + "</td><td>Bind method to location</td><td>Opcode (loc + " + s + ")</td><td>" + String.format(fmt, bloc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
          out += "<tr><td>" + hex2 + "</td><td>loc + " + offset + "</td><td>Offset = " + offset + "</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
        }
        else if( opcode == 0xB0 )
        {
          out += "<tr><td>" + opcodeh + "</td><td>Bind method to location scale = " + arg + "</td><td>Opcode (loc + " + s + " * scale + " + s + ")</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
        }
        else if( opcode == 0xC0 )
        {
          out += "<tr><td>" + opcodeh + "</td><td>Number of Binds plus skip</td><td>Opcode</td><td>" + String.format(fmt, bloc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
          out += "<tr><td>" + hex1 + "</td><td>Number of binds plus skip offset " + offset + "</td><td>Count = " + offset + "</td><td>" + String.format(fmt, bloc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
          out += "<tr><td>" + hex2 + "</td><td>Skip " + offset + "</td><td>Opcode</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
        }
        else
        {
          out += "<tr><td>" + opcodeh + "</td><td>Unknown Opcode.</td><td>?</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
        }

        loc += adj; offset = 0; hex1 = ""; hex2 = ""; Pos += 1;
      }
    }
    catch( Exception er ) { } //Incase the file is corrupted and we read an bad opcode that goes out of bound of the import data. This way we still load what we can.

    file.Events = true; return(out + "</table></html>");
  }

  //Fully bind and decode the method calls, or only return actions as string.

  public static String bindSyms( long pos, long end, boolean bind )
  {
    byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); if(!bind) { file.Events = false; file.seek( pos ); Offset.setSelected( pos, end - 1 ); } file.read(d); } catch( java.io.IOException er ) {}

    int Pos = 0, End = d.length;

    String name = "", out = "", fmt = "";

    if( !bind ){ out = "<table border='1'><tr><td>Set address.</td><td>Export method.</td></tr>"; fmt = fmt = is64bit ? "%1$016X" : "%1$08X"; }

    long loc = 0, offset = 0;
    int opcode = 0, arg = 0, bpos = 0, count = 1, ptr_size = is64bit ? 8 : 4;

    boolean adj = false;

    try
    {
      while( Pos < End )
      {
        opcode = d[Pos]; arg = opcode & 0x0F; opcode &= 0xF0; adj = opcode >= 0x90 && opcode <= 0xC0;

        if( opcode == 0x00 ) { loc = 0; } else if( opcode == 0xB0 ) { offset = arg << 3; }

        else if( opcode == 0x40 ) { name = ""; Pos += 1; while( d[Pos] != 0x00 ) { name += (char)d[Pos]; Pos += 1; } }

        else if ( opcode == 0x20 || opcode == 0x60 || opcode == 0x70 || opcode == 0x80 || opcode == 0xA0 || opcode == 0xC0 )
        {
          if( opcode == 0x70 ) { loc = segment.get( arg ); }

          if( opcode == 0xC0 ) { Pos += 1; count = 0; while( d[Pos] < 0 ) { count |= ( d[Pos++] & 0x7F ) << bpos; bpos += 7; } count |= d[Pos] << bpos; bpos = 0; }

          Pos += 1; while( d[Pos] < 0 ) { offset |= ( (long)d[Pos++] & 0x7F ) << bpos; bpos += 7; } offset |= (long)d[Pos] << bpos; bpos = 0;
        }

        if( adj )
        {
          if( !is64bit ){ loc &= 0x00000000FFFFFFFFL; }

          for( int times = 0; times < count; times++ )
          {
            if( bind )
            {
              core.mapped_pos.add( loc ); core.mapped_pos.add( loc + ptr_size ); core.mapped_loc.add( name );
            }
            else { out += "<tr><td>" + String.format( fmt, loc ) + "</td><td>" + name + "</td></tr>"; }
            
            loc += offset + ptr_size;
          }

          count = 1;
        }
        else if( opcode != 0x20 || opcode != 0x60 ) { loc += offset; }

        offset = 0; Pos += 1;
      }
    }
    catch( Exception er ) { } //Incase the file is corrupted and we read an bad opcode that goes out of bound of the import data. This way we still load what we can.

    return( out + ( (file.Events = !bind) ? "</table>" : "" ) );
  }

  //The export section is not structured by opcodes like the other sections. Instead it is broken up into nodes that forum the method name.

  private class node { String name = ""; int loc = 0; JDNode n; public node( String Name, int Loc, JDNode N ) { name = Name; loc = Loc; n = N; } }

  public void export( long pos, long end, JDNode n )
  {
    n.removeAllChildren(); ((JDNode)n).setArgs( new long[]{0xC000000000000302L, pos, end - 1 } ); JDNode t;

    java.util.ArrayList<node> Nodes = new java.util.ArrayList<node>();

    JDNode dec = new JDNode("Decoding", 0x4000000000000400L), temp; n.add( dec ); node nd = new node( "", 0, dec );
    
    byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); file.Events = false; file.seek( pos ); Offset.setSelected( pos, end - 1 ); file.read(d); } catch( java.io.IOException er ) {}

    int term = 0, nodes = 0, curNode = 0, numNodes = 0, Pos = 0, bpos = 0;

    String name = "", pfx = ""; long eLoc = 0;

    try
    {
      while( curNode <= numNodes )
      {
        term = d[Pos++] & 0xFF; if( term > 0 )
        {
          Pos += 1; //Flags.
          eLoc = 0; while( d[Pos] < 0 ) { eLoc |= ( (long)d[Pos++] & 0x7F ) << bpos; bpos += 7; } eLoc |= (long)d[Pos++] << bpos; eLoc += base; bpos = 0; //Location
        
          t = new JDNode( name, 0xC000000000000000L ); t.add(new JDNode("Location.h", new long[]{ 0xC000000000000002L, eLoc, eLoc } ) );
          t.add(new JDNode("Disassemble.h", new long[]{ 0xC000000000000004L, file.toVirtual( eLoc ) } ) ); n.add( t );
          nd.n.add(new JDNode("Terminal.h", new long[]{ 0x4000000000000007L, pos + nd.loc } ) );;
        }

        nodes = d[Pos++] & 0xFF; numNodes += nodes;

        for( int i = 0; i < nodes; i++ )
        {
          pfx = ""; while( d[Pos] != 0x00 ) { pfx += (char)d[Pos]; Pos += 1; } Pos += 1;
          eLoc = 0; while( d[Pos] < 0 ) { eLoc |= ( d[Pos++] & 0x7F ) << bpos; bpos += 7; } eLoc |= d[Pos++] << bpos; bpos = 0;
          temp = new JDNode(name + pfx, new long[]{ 0x4000000000000007L, pos + nd.loc }); Nodes.add( new node( name + pfx, (int)eLoc, temp ) ); dec.add( temp );
        }

        if( curNode < numNodes ) { nd = Nodes.get( curNode ); name = nd.name; Pos = nd.loc; dec = nd.n; } curNode++;
      }
    } catch( Exception er ) { } //Incase we read out of bounds because of bad export information. This way we still load what we can.
    
    Nodes.clear(); file.Events = true;
  }

  //Show the decoding of an single Export node.

  public String export( long pos )
  {
    String out = export + "<table border='1'><tr><td>Description</td><td>Hex</td><td>Value</td></tr>", pfx = "", hex = "";

    int b = 0, bpos = 0, size = 0;
    long eLoc = 0;

    try
    {
      file.seek( pos ); file.Events = false; file.seek( pos );

      b = file.read(); out += "<tr><td>Terminal size</td><td>" + String.format("%1$02X", b ) + "</td><td>" + b + "</td></tr>";

      if( b > 0 )
      {
        b = file.read(); out += "<tr><td>FLAGS</td><td>" + String.format("%1$02X", b ) + "</td><td>" + b + "</td></tr>";
        eLoc = 0; while( ( b = file.read() ) >= 0x80 ) { hex += String.format("%1$02X", b ) + " "; eLoc |= ( (long)b & 0x7F ) << bpos; bpos += 7; }
        hex += String.format("%1$02X", b ); eLoc |= (long)b << bpos; bpos = 0;
        out += "<tr><td>Node Data Location</td><td>" + hex + "</td><td>" + eLoc + "</td></tr>"; hex = "";
      }

      size = file.read(); out += "<tr><td>Cur node name + Child nodes</td><td>" + String.format("%1$02X", size ) + "</td><td>" + size + "</td></tr>";

      for( int i = 0; i < size; i++ )
      {
        pfx = ""; while( ( b = file.read() ) != 0x00 ) { hex += String.format("%1$02X", b ) + " "; pfx += (char)b; } hex += String.format("%1$02X", b );
        out += "<tr><td>Add text to parent node.</td><td>" + hex + "</td><td>" + pfx + "</td></tr>"; hex = "";
        eLoc = 0; while( ( b = file.read() ) >= 0x80 ) { hex += String.format("%1$02X", b ) + " "; eLoc |= ( (long)b & 0x7F ) << bpos; bpos += 7; }
        hex += String.format("%1$02X", b ); eLoc |= (long)b << bpos; bpos = 0;
        out += "<tr><td>Offset to terminal + nodes.</td><td>" + hex + "</td><td>" + eLoc + "</td></tr>"; hex = "";
      }

      Offset.setSelected( pos, file.getFilePointer() - 1 );
    }
    catch( java.io.IOException er ) {}

    file.Events = true; return( out + "</table></html>" );
  }

  //Show full decoding of the rebase information.

  public String rebaseInfo( long pos, long end )
  {
    byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); Offset.setSelected( pos, end - 1 ); file.Events = false; file.read(d); } catch( java.io.IOException er ) {}

    String out = rebase + "<table border='1'><tr><td>Hex</td><td>Description</td><td>Value</td><td>Current location</td><td>Current bind type</td></tr>";

    int Pos = 0, End = d.length;

    String bind_type = "pointer", opcodeh = "", hex1 = "", hex2 = "", s = is64bit ? "8" : "4", fmt = is64bit ? "%1$016X" : "%1$08X";;
    long loc = 0, bloc = 0, offset = 0, mask = is64bit ? -1 : 0x00000000FFFFFFFFL;
    int opcode = 0, arg = 0, bpos = 0, count = 0, adj = 0;

    int bindType = 1;

    try
    {
      while( Pos < End )
      {
        opcodeh = String.format("%1$02X", d[Pos] ); opcode = d[Pos]; arg = opcode & 0x0F; opcode &= 0xF0; bloc = loc; adj = opcode >= 0x50 && opcode <= 0x80 ? ( is64bit ? 8 : 4 ) : 0; count = opcode != 0x50 ? 1 : arg;

        if( opcode == 0x00 ) { loc = 0; bind_type = "pointer"; } else if( opcode == 0x40 ) { offset = is64bit ? arg << 3 : arg << 2; }

        else if( opcode == 0x10 ) { bindType = arg; if( bindType == 1 ){ bind_type = "pointer"; } else if( bindType == 2 ) { bind_type = "relative"; } else if( bindType == 3 ) { bind_type = "absolute"; } else { bind_type = "???"; } }

        if( opcode == 0x60 || opcode == 0x80 )
        {
          Pos += 1; count = 0; while( d[Pos] < 0 ) { hex1 += String.format("%1$02X", d[Pos] ) + " "; count |= ( d[Pos++] & 0x7F ) << bpos; bpos += 7; } 
          hex1 += String.format("%1$02X", d[Pos] ) + " "; count |= d[Pos] << bpos; bpos = 0;
        }

        if ( opcode == 0x70 || opcode == 0x80 || opcode == 0x20 || opcode == 0x30 )
        {
          if( opcode == 0x20 ) { bloc = loc = segment.get( arg ); }

          Pos += 1; while( d[Pos] < 0 ) { hex2 += String.format("%1$02X", d[Pos] ) + " "; offset |= ( (long)d[Pos++] & 0x7F ) << bpos; bpos += 7; } 
          hex2 += String.format("%1$02X", d[Pos] ) + " "; offset |= (long)d[Pos] << bpos; bpos = 0;
        }

        loc += ( offset + adj ) * count; loc -= adj; loc &= mask;

        if( opcode == 0x00 ){ out += "<tr><td>" + opcodeh + "</td><td>Reset.</td><td>Opcode</td><td>" + String.format(fmt, loc) + "</td><td>" + bind_type + "</td></tr>"; }
        if( opcode == 0x10 ){ out += "<tr><td>" + opcodeh + "</td><td>Set loc type " + bindType + ".</td><td>Opcode</td><td>" + String.format(fmt, loc) + "</td><td>" + bind_type + "</td></tr>"; }
        if( opcode == 0x20 )
        {
          out += "<tr><td>" + opcodeh + "</td><td>Set loc to segment " + arg + "</td><td>Opcode</td><td>" + String.format(fmt, bloc) + "</td><td>" + bind_type + "</td></tr>";
          out += "<tr><td>" + hex2 + "</td><td>Offset</td><td>loc + " + offset + "</td><td>" + String.format(fmt, loc) + "</td><td>" + bind_type + "</td></tr>";
        }
        else if( opcode == 0x30 ) { out += "<tr><td>" + opcodeh + "</td><td>Add loc to offset</td><td>Opcode</td><td>" + String.format(fmt, loc) + "</td><td>" + bind_type + "</td></tr>"; }
        else if( opcode == 0x40 ) { out += "<tr><td>" + opcodeh + "</td><td>loc scale = " + arg + "</td><td>Opcode (loc + " + s + " * scale)</td><td>" + String.format(fmt, loc) + "</td><td>" + bind_type + "</td></tr>"; }
        else if( opcode == 0x50 ) { out += "<tr><td>" + opcodeh + "</td><td>Adjust loc times = " + arg + "</td><td>Opcode</td><td>" + String.format(fmt, loc) + "</td><td>" + bind_type + "</td></tr>"; }
        else if( opcode == 0x60 )
        {
          out += "<tr><td>" + opcodeh + "</td><td>Adjust loc times</td><td>Opcode</td><td>" + String.format(fmt, bloc) + "</td><td>" + bind_type + "</td></tr>";
          out += "<tr><td>" + hex1 + "</td><td>Times = " + count + "</td><td>Adjust loc times = " + count + "</td><td>" + String.format(fmt, loc) + "</td><td>" + bind_type + "</td></tr>";
        }
        else if( opcode == 0x70 )
        {
          out += "<tr><td>" + opcodeh + "</td><td>Adjust loc</td><td>Opcode</td><td>" + String.format(fmt, bloc) + "</td><td>" + bind_type + "</td></tr>";
          out += "<tr><td>" + hex2 + "</td><td>Add Loc + " + offset + "</td><td>Offset = " + offset + "</td><td>" + String.format(fmt, loc) + "</td><td>" + bind_type + "</td></tr>";
        }
        else if( opcode == 0x80 )
        {
          out += "<tr><td>" + opcodeh + "</td><td>Number of adjusts plus skip</td><td>Opcode</td><td>" + String.format(fmt, bloc) + "</td><td>" + bind_type + "</td></tr>";
          out += "<tr><td>" + hex1 + "</td><td>Number of adjusts plus skip offset " + offset + "</td><td>Count = " + offset + "</td><td>" + String.format(fmt, bloc) + "</td><td>" + bind_type + "</td></tr>";
          out += "<tr><td>" + hex2 + "</td><td>Skip " + offset + "</td><td>" + offset + "</td><td>" + String.format(fmt, loc) + "</td><td>" + bind_type + "</td></tr>";
        }

        loc += adj; offset = 0; hex1 = ""; hex2 = ""; Pos += 1;
      }
    }
    catch( Exception er ) { } //Incase the file is corrupted and we read an bad opcode that goes out of bound of the import data. This way we still load what we can.

    file.Events = true; return(out + "</table></html>");
  }

  //Show decoding of only the rebase actions being carried out.

  public String rebase( long pos, long end )
  {
    byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); file.Events = false; Offset.setSelected( pos, end - 1 ); file.read(d); } catch( java.io.IOException er ) {}

    String out = "<html>Decoding of the link edit rebase information.<br /><br />" +
    "<table border='1'><tr><td>Adjust location</td><td>type</td></tr>";

    int Pos = 0, End = d.length;

    String bind_type = "pointer";
    long loc = 0, offset = 0;
    int opcode = 0, arg = 0, bpos = 0, count = 0, ptr_size = is64bit ? 8 : 4;

    int bindType = 1; boolean adj = false;

    try
    {
      while( Pos < End )
      {
        opcode = d[Pos]; arg = opcode & 0x0F; opcode &= 0xF0; adj = opcode >= 0x50 && opcode <= 0x80; count = opcode != 0x50 ? 1 : arg;

        if( opcode == 0x00 ) { loc = 0; bind_type = "pointer"; } else if( opcode == 0x40 ) { offset = is64bit ? arg << 3 : arg << 2; }

        else if( opcode == 0x10 ) { bindType = arg; if( bindType == 1 ){ bind_type = "pointer"; } else if( bindType == 2 ) { bind_type = "relative"; } else if( bindType == 3 ) { bind_type = "absolute"; } else { bind_type = "???"; } }

        if( opcode == 0x60 || opcode == 0x80 )
        {
          Pos += 1; count = 0; while( d[Pos] < 0 ) { count |= ( d[Pos++] & 0x7F ) << bpos; bpos += 7; } count |= d[Pos] << bpos; bpos = 0;
        }

        if ( opcode == 0x70 || opcode == 0x80 || opcode == 0x20 || opcode == 0x30 )
        {
          if( opcode == 0x20 ) { loc = segment.get( arg ); }

          Pos += 1; while( d[Pos] < 0 ) { offset |= ( (long)d[Pos++] & 0x7F ) << bpos; bpos += 7; } offset |= (long)d[Pos] << bpos; bpos = 0;
        }

        if( adj )
        {
          if( !is64bit ){ loc &= 0x00000000FFFFFFFFL; }

          for( int times = 0; times < count; times++ )
          {
            out += "<tr><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + bind_type + "</td></tr>"; loc += offset + ptr_size;
          }
        } else { loc += offset; }

        offset = 0; Pos += 1;
      }
    }
    catch( Exception er ) { } //Incase the file is corrupted and we read an bad opcode that goes out of bound of the import data. This way we still load what we can.

    file.Events = true; return(out + "</table></html>");
  }

  //Faster mapping and loading of all symbols and methods.

  public static void mapSyms( int symOff, int symSize, int strOff, int strSize, int indOff, int indSize, boolean dyld )
  {
    byte[] syms = new byte[symSize], str = new byte[strSize], ind = new byte[indSize];

    try
    {
      file.seek( symOff ); file.read(syms); file.seek( strOff ); file.read(str);

      int pos = 0, namePos = 0; long Pos = 0; String name = "";

      //We only add linkable symbol methods if the they are not mapped yet as modern Mach-O binaries use the compressed dyld linker.
    
      if( dyld && indOff > 0 )
      {
        file.seek( indOff ); file.read(ind);

        int symNum = 0, Pointer = 0; Pointers p = ptr.get( Pointer ); Pos = p.loc;

        while( pos < ind.length )
        {
          symNum = ( ind[pos] & 0xFF ) | ( (ind[pos + 1] << 8) & 0xFF00 ) | ( (ind[pos + 2] << 16) & 0xFF0000 ) | ( (ind[pos + 3] << 24) & 0xFF000000 );

          if( ( symNum & 0xC0000000 ) == 0 )
          {
            //The size and spacing of each symbol.

            symNum = is64bit ? symNum << 4 : ( symNum << 2 ) + ( symNum << 3 );

            //The position for the symbol name in the string table.

            namePos = ( syms[symNum] & 0xFF ) | ( (syms[symNum + 1] << 8) & 0xFF00 ) | ( (syms[symNum + 2] << 16) & 0xFF0000 ) | ( (syms[symNum + 3] << 24) & 0xFF000000 );

            //Read the symbol name.

            while( str[namePos] != 0 ){ name += (char)str[namePos++]; }
          }
          else { name = symNum < 0 ? "Local_Method" : "Absolute_Method"; }

          //Map the method call.

          core.mapped_pos.add(Pos); Pos += p.ptr_size; core.mapped_pos.add(Pos); core.mapped_loc.add(name); name = "";

          //Move to the next pointer.

          if( Pos >= p.size && ( Pointer + 1 ) < ptr.size() ){ Pointer += 1; p = ptr.get( Pointer ); Pos = p.loc; }

          //Move to the next indirect symbol number.

          pos += 4;
        }
  
        pos = 0; name = "";
      }

      //This time we add all symbols that locate to variables or data, or callable methods locations in file.

      int size = is64bit ? 8 : 4, type = 0; while( pos < syms.length )
      {
        namePos = ( syms[pos] & 0xFF ) | ( (syms[pos + 1] << 8) & 0xFF00 ) | ( (syms[pos + 2] << 16) & 0xFF0000 ) | ( (syms[pos + 3] << 24) & 0xFF000000 ); type = syms[pos + 4]; pos += 8;

        if( is64bit )
        {
          Pos = ( (long)syms[pos] & 0xFFL ) | ( ((long)syms[pos + 1] << 8) & 0xFF00L ) | ( ((long)syms[pos + 2] << 16) & 0xFF0000L ) | ( ((long)syms[pos + 3] << 24) & 0xFF000000L ) |
          ( ( (long)syms[pos + 4] << 32 ) & 0xFF00000000L ) | ( ( (long)syms[pos + 5] << 40 ) & 0xFF0000000000L ) | ( ( (long)syms[pos + 6] << 48 ) & 0xFF000000000000L ) | ( ( (long)syms[pos + 7] << 56 ) & 0xFF00000000000000L );
        }
        else
        {
          Pos = ( syms[pos] & 0xFF ) | ( (syms[pos + 1] << 8) & 0xFF00 ) | ( (syms[pos + 2] << 16) & 0xFF0000 ) | ( (syms[pos + 3] << 24) & 0xFF000000 );
        }

        pos += size;
        
        //Check if Symbol address is defined, and Symbol name is not undefined.
        //We also skip Debug symbols as they are positions in machine code for line numbers and sections.
        //The current core engine does not yet support line numbers.

        if( Pos > 0 && namePos != 0 && str[namePos] != 0 && ( type & 0xE0 ) == 0 )
        {
          while( str[namePos] != 0 ){ name += (char)str[namePos++]; }

          //Define the symbol location.
          
          core.mapped_pos.add(Pos); core.mapped_pos.add(Pos + size); core.mapped_loc.add(name); name = "";
        }
      }      
    } catch( Exception e ){ e.printStackTrace(); }
  }

  //Decode the symbol table in detail when the user wants to view the symbol table.
  //The reason for separating the detailed view from loading is for performance, but we end up
  //using more memory because we can't pass the symbol names from detailed view to the core address map.

  public void decodeSyms( long pos, long syms, long strOff, JDNode n )
  {
    try
    {
      file.Events= false; file.seek(pos); DTemp = new Descriptor( file ); DTemp.setEvent( this::symsInfo ); des.add( DTemp );
    
      n.removeAllChildren(); ((JDNode)n).setArgs( new long[]{ 0, ref++ } );

      JDNode Debug = new JDNode( "Local (Debug)", new long[]{ 0x4000000000000000L } );

      JDNode Local = new JDNode( "Local (Other)", new long[]{ 0x4000000000000000L } );

      JDNode Ordinals = new JDNode( "Undefined (Library Function calls)", new long[]{ 0x4000000000000000L } );

      JDNode External = new JDNode( "External", new long[]{ 0x4000000000000000L } );

      JDNode ExternalP = new JDNode( "External Private", new long[]{ 0x4000000000000000L } );
    
      //Begin reading all the symbols.
    
      Descriptor string; long t = 0;
      
      int name, type, DInfo;
  
      for( int i2 = 0; i2 < syms; i2++ )
      {
        DTemp.Array("Symbol #" + i2 + "", is64bit ? 16 : 12 );
        DTemp.LUINT32("Name"); name = (int)DTemp.value;
        DTemp.UINT8("Type"); type = (byte)DTemp.value;
        DTemp.UINT8("Section Number");
        DTemp.LUINT16("Data info"); DInfo = (Short)DTemp.value & 0xFFFF;

        if( is64bit ) { DTemp.LUINT64("Symbol Address"); } else { DTemp.LUINT32("Symbol Address"); }
      
        if( name != 0 )
        {
          t = file.getFilePointer(); file.seek( name + strOff );

          string = new Descriptor( file ); string.setEvent( this::string );

          string.String8("Symbol name", (byte)0x00 ); if( string.value.equals("") ) { string.value = "No Name"; }

          //Categorize the symbols.

          if( ( DInfo & 0xFF00 ) != 0 )
          {
            Ordinals.add( new JDNode( string.value + " (Sym=" + i2 + ").h", new long[]{ 0, ref++ }) );
          }
          else if( ( type & 0xE0 ) != 0 )
          {
            Debug.add( new JDNode( string.value + " (Sym=" + i2 + ").h", new long[]{ 0, ref++ }) );
          }
          else if( ( type & 0x01 ) != 0 )
          {
            External.add( new JDNode( string.value + " (Sym=" + i2 + ").h", new long[]{ 0, ref++ }) );
          }
          else if( ( type & 0x10 ) != 0 )
          {
            ExternalP.add( new JDNode( string.value + " (Sym=" + i2 + ").h", new long[]{ 0, ref++ }) );
          }
          else
          {
            Local.add( new JDNode( string.value + " (Sym=" + i2 + ").h", new long[]{ 0, ref++ }) );
          }

          des.add( string ); file.seek( t );
        }
        else
        {
          //Categorize the symbols.

          if( ( DInfo & 0xFF00 ) != 0 )
          {
            Ordinals.add( new JDNode( "No Name (Sym=" + i2 + ").h" ) );
          }
          else if( ( type & 0xE0 ) != 0 )
          {
            Debug.add( new JDNode( "No Name (Sym=" + i2 + ").h" ) );
          }
          else if( ( type & 0x01 ) != 0 )
          {
            External.add( new JDNode( "No Name (Sym=" + i2 + ").h" ) );
          }
          else if( ( type & 0x10 ) != 0 )
          {
            ExternalP.add( new JDNode( "No Name (Sym=" + i2 + ").h" ) );
          }
          else
          {
            Local.add( new JDNode( "No Name (Sym=" + i2 + ").h" ) );
          }
        }
      }

      if( Ordinals.getChildCount() > 0 ) { n.insert( Ordinals, 0 ); }
      if( External.getChildCount() > 0 ) { n.insert( External, 0 ); }
      if( ExternalP.getChildCount() > 0 ) { n.insert( ExternalP, 0 ); }
      if( Local.getChildCount() > 0 ) { n.insert( Local, 0 ); }
      if( Debug.getChildCount() > 0 ) { n.insert( Debug, 0 ); }
    }
    catch( Exception e ) { e.printStackTrace(); }

    file.Events = true;
  }

  //Create data descriptor for indirect symbol table.

  public void decodeIndSym( long pos, long size, JDNode n )
  {
    file.Events = false; size += pos;
    
    try
    {
      file.seek(pos); DTemp = new Descriptor( file ); des.add( DTemp ); DTemp.setEvent( this::indInfo );
      
      n.setArgs( new long[]{ 0, ref++, n.getArg(1), n.getArg(2), n.getArg(3), n.getArg(4), n.getArg(5), n.getArg(6) } );

      while( pos < size ) { DTemp.LUINT32("Symbol Number"); pos += 4; }
    }
    catch(Exception e) { e.printStackTrace(); }

    file.Events = true;
  }

  //We do the same code as when we are mapping the symbols to show the actions.
  //The reason for separating the detailed view from loading is for performance.

  public String indSymInfo( int symOff, int symSize, int strOff, int strSize, int indOff, int indSize )
  {
    String out = indInfo, name = "", fmt = is64bit ? "%1$016X" : "%1$08X";;

    byte[] syms = new byte[symSize], str = new byte[strSize], ind = new byte[indSize];

    try
    {
      file.Events = false; file.seek( symOff ); file.read(syms); file.seek( strOff ); file.read(str); file.seek( indOff ); file.read(ind);

      int pos = 0, namePos = 0; long Pos = 0;

      int symNum = 0, symPos = 0, Pointer = 0; Pointers p = ptr.get( Pointer ); Pos = p.loc;

      out += "Locating First pointer section in load commands " + String.format( fmt, p.loc ) + " to " + String.format( fmt, p.size ) + "";
      out += ( ( p.ptr_size != (is64bit ? 8 : 4) ) ? ". Each jump instruction size " : ". Each pointer size " ) + p.ptr_size + ".";
      out += "<br /><br /><table border='1'><tr><td>Hex</td><td>Symbol Number</td><td>Symbol Name</td><td>Location</td></tr>";

      while( pos < ind.length )
      {
        symNum = ( ind[pos] & 0xFF ) | ( (ind[pos + 1] << 8) & 0xFF00 ) | ( (ind[pos + 2] << 16) & 0xFF0000 ) | ( (ind[pos + 3] << 24) & 0xFF000000 );

        if( ( symNum & 0xC0000000 ) == 0 )
        {
          //The size and spacing of each symbol.

          symPos = is64bit ? symNum << 4 : ( symNum << 2 ) + ( symNum << 3 );

          //The position for the symbol name in the string table.

          namePos = ( syms[symPos] & 0xFF ) | ( (syms[symPos + 1] << 8) & 0xFF00 ) | ( (syms[symPos + 2] << 16) & 0xFF0000 ) | ( (syms[symPos + 3] << 24) & 0xFF000000 );

          //Read the symbol name.

          while( str[namePos] != 0 ){ name += (char)str[namePos++]; }
        }
        else { name = symNum < 0 ? "Local_Method" : "Absolute_Method"; }

        //Map the method call.

        out += "<tr><td>" + String.format("%1$02X", ind[pos] ) + " " + String.format("%1$02X", ind[pos + 1] ) + " " + String.format("%1$02X", ind[pos + 2] ) + " " + String.format("%1$02X", ind[pos + 3] ) + "</td>";
        out += "<td>" + symNum + "</td><td>" + name + "</td><td>" + String.format( fmt, Pos ) + "</td></tr>";

        Pos += p.ptr_size; name = "";

        //Move to the next pointer.

        if( Pos >= p.size && ( Pointer + 1 ) < ptr.size() )
        {
          Pointer += 1; p = ptr.get( Pointer ); Pos = p.loc;
          out += "</table><br />Locating Next pointer section " + String.format( fmt, p.loc ) + " to " + String.format( fmt, p.size ) + "";
          out += ( ( p.ptr_size != (is64bit ? 8 : 4) ) ? ". Each jump instruction size " : ". Each pointer size " ) + p.ptr_size + ".";
          out += "<br /><br /><table border='1'><tr><td>Hex</td><td>Symbol Number</td><td>Symbol Name</td><td>Location</td></tr>";
        }

        //Move to the next indirect symbol number.

        pos += 4;
      }
  
      pos = 0; name = "";
    }
    catch( Exception e ){ e.printStackTrace(); }

    file.Events = true; return( out + "</table></html>" );
  }

  //Description on how the indirect symbol table is read.

  private static final String indInfo = "<html>Any section under load commands with flag setting 6, or 7 (Lazy) set is a pointer list. We record the position of each of these sections as we dump them to Memory by load commands.<br /><br />" +
  "Pointers are read by the machine code in the program as the location to jump to the function/method. Each pointer is 4 in size in 32 bit programs, and 8 in size for 64 bit programs.<br /><br />" +
  "Any section under load commands with flag setting 8 is a jump list. This section is set machine code that should jump to the method. The size of each jump instruction is stored in the reserved2 value in the section load command.<br /><br />" +
  "The symbol numbers tell us which symbol to set to each pointer across the pointer lists, and jump lists. The indirect symbol number list is structured to go in order to each pointer/jump section.<br /><br />" +
  "There are two symbol numbers that are used to define pointers that locate to a method in the file and are not dynamically loaded, and have no name.<br /><br />" +
  "2147483648 = Local_Method meaning the pointer its self locates to the method relative (Local method).<br /><br />" +
  "1073741824 = Absolute_Method meaning the pointer locates to the exact address of a method.<br /><br />";

  //Description on the symbol table.

  private static final String[] symsInfo = new String[]
  {
    "<html>Array element for defining one symbol.</html>",
    "<html>The name value is added to the file position for the string table. If this value is 0 then the symbol has no name.</html>",
    "<html>This value is broken into tow sections. First is the flag setting. Any of the binary digits that are set one correspond to the following settings.<br /><br />" +
    "<table border='1'>" +
    "<tr><td>Digit</td><td>Setting</td></tr>" +
    "<tr><td>xxx00000</td><td>Combination for symbolic debugging entry type.</td></tr>" +
    "<tr><td>00010000</td><td>Private external symbol.</td></tr>" +
    "<tr><td>0000xxx0</td><td>Combination for the type setting.</td></tr>" +
    "<tr><td>00000001</td><td>External symbol.</td></tr>" +
    "</table><br />" +
    "The type setting uses the three digits marked as x in the above table as the type setting combination. The hyphens are used to separate the three bits for the type setting.<br /><br />" +
    "<table border='1'>" +
    "<tr><td>Combination</td><td>Setting</td></tr>" +
    "<tr><td>0000-000-0</td><td>Symbol undefined.</td></tr>" +
    "<tr><td>0000-001-0</td><td>Symbol absolute.</td></tr>" +
    "<tr><td>0000-101-0</td><td>Symbol indirect.</td></tr>" +
    "<tr><td>0000-110-0</td><td>Symbol prebound undefined.</td></tr>" +
    "<tr><td>0000-111-0</td><td>Symbol defined in section number.</td></tr>" +
    "</table></html>",
    "<html>An integer specifying the section number that this symbol can be found in.<br /><br />" +
    "Section numbers start at 1 so an value of 0 means no section. This means the symbol may be a method name in another file.<br /><br />" +
    "If this symbol is an method in another file then the Data info felid will tell us which load link library command by ordinal.</html>",
    "<html>The data info value setting describes additional information about the type of symbol this is.<br /><br />" +
    "The Last 2 hex digits is the library ordinal. As we load link libraries we label them starting from library 1 to nth library as ordinals.<br /><br />" +
    "If the symbol is not part of any section and section number is set 0, but has an non zero ordinal then it is a method name in a link library.<br /><br />" +
    "An data info section with the last 2 hex digits set 03 07 would mean ordinal 07. We ignore the first 2 hex digits.<br /><br />" +
    "The first 2 hex digits are used as 4 optional settings, and a 4 bit combination code. An digit that is set one corresponds to the following settings.<br /><br />" +
    "<table border='1'>"+
    "<tr><td>Digit</td><td>Setting</td></tr>" +
    "<tr><td>0001-xxxx</td><td>Must be set for any defined symbol that is referenced by dynamic-loader.</td></tr>" +
    "<tr><td>0010-xxxx</td><td>Used by the dynamic linker at runtime.</td></tr>" +
    "<tr><td>0100-xxxx</td><td>If the dynamic linker cannot find a definition for this symbol, it sets the address of this symbol to 0.</td></tr>" +
    "<tr><td>1000-xxxx</td><td>If the static linker or the dynamic linker finds another definition for this symbol, the definition is ignored.</td></tr>" +
    "</table><br />" +
    "The last four binary digits are used as a combination for the symbol function/method call type which are separated by a hyphen.<br /><br />" +
    "These settings are used to define the type of function/method call that this symbol defines by ordinal.<br /><br />" +
    "<table border='1'>" +
    "<tr><td>Value</td><td>Description</td></tr>" +
    "<tr><td>xxxx-0000</td><td>Non Lazy loaded pointer method call.</td></tr>" +
    "<tr><td>xxxx-0001</td><td>Lazy loaded pointer method call.</td></tr>" +
    "<tr><td>xxxx-0010</td><td>Method call defined in this library/program.</td></tr>" +
    "<tr><td>xxxx-0011</td><td>Private Method call defined in this library/program.</td></tr>" +
    "<tr><td>xxxx-0100</td><td>Private Non Lazy loaded pointer method call.</td></tr>" +
    "<tr><td>xxxx-0101</td><td>Private Lazy loaded pointer method call.</td></tr>" +
    "</table><br />" +
    "A Pointer is a value that is read by the program to call a method from another binary file. Private means other programs are not meant to be able to read or call the function/methods other than the binary it's self.</html>",
    "<html>The address location that the symbol is at. If address is 0 and ordinal number is set, then the location is set by the matching external symbol name in the other file.</html>",
  };

  private void symsInfo( int i )
  {
    if( i < 0 )
    {
      info( "<html>This is the symbol array. The symbol type and data info are divided into subfolders here.<br /><br />" +
      "Each link library we load is given a number starting from 1 incrementing upward. The ordinal number is used to specify which link library the method is in.<br /><br />" +
      "A symbol with an ordinal set in \"data info\" other than 0 means it is a function/method in a link library.<br /><br />" +
      "Symbols that are of an ordinal type have an address of 0, and a section number of 0 meaning no section along the load commands and are considered to be undefined.<br /><br />" +
      "If we examine the symbol table in the linked library we will find the symbol defined as an external symbol with its address in the library.<br /><br />" +
      "We set the ordinal symbol to the address of the external symbol.<br /><br />" +
      "A debug symbol is put in its own category as it is used to define address positions in the code relative to the original source code lines, so some symbols may have no names.<br /><br />" +
      "The Debug symbols are more commonly known as local symbols. External symbols that are readable from other binary files are put in their own category.<br /><br />" +
      "A full detailed breakdown of a symbol's type setting and data info can be viewed by clicking on the data fields in the symbol array.<br /><br />" +
      "The symbol array goes in order by symbol types local, external, undefined and is further organized by the \"symbol info\" load command.</html>" );
    }
    else
    {
      info( symsInfo[ i % 6 ] );
    }
  }

  private void indInfo( int i )
  {
    if( i < 0 )
    {
      long[] n = ((JDNode)tree.getLastSelectedPathComponent()).getArgs(); info( indSymInfo( (int)n[2], (int)n[3], (int)n[4], (int)n[5], (int)n[6], (int)n[7]) );
    }
    else
    {
      info("<html>Symbol number from the symbol table. There are two Special symbol number types.<br /><br />" +
      "2147483648 = Local_Method meaning the pointer its self locates to the method and that there is no method name (Local method).<br /><br />" +
      "1073741824 = Absolute_Method meaning the pointer locates to the exact address of a method elsewhere.</html>");
    }
  }

  private void string( int i )
  {
    info("<html>The end if the symbols name is defined by the first byte that is 00.</html>");
  }

  //Descriptions on what everything is in the compressed link edit table.

  private static final String ulib128 = "The first 7 binary digits are the value, and if the last binary digit is set one then we read the next value as the next 7 binary digits for the number.<br />" +
  "The last 7 binary digits for the number should end with a value that is smaller than 80 hex as the last binary digit should be zero.<br /><br />";

  private static final String rebase = "<html>The first hex digit is the opcode and the last hex digit is used as an 0 to 15 value.<br /><br />" +
  "<table border='1'><tr><td>Opcode</td><td>Description</td></tr>" +
  "<tr><td>2?</td><td>Sets the location to the address location of a segment load command data. The last ? hex digit is which segment, for example 27 would mean Seg=7.<br />" +
  "After this opcode is a number that is added to the location in the segment.</td></tr>" +
  "<tr><td>1?</td><td>Sets the location type. The last ? hex digit is used as 1 to 3 value (pointer = 1, relative = 2, or absolute = 3).<br />" +
  "Pointer means a location that is read and used as the location to the method in the program.<br />" +
  "Relative means a location that is read and added to from the current location in the code to call the method.<br />" +
  "Absolute means a location that must locate directly to the method.</td></tr>" +
  "<tr><td>3?</td><td>Read an number after this opcode and add it to the currently set location.</td></tr>" +
  "<tr><td>4?</td><td>Use the last ? hex digit and multiply it by 8 for 64 bit binaries, or 4 for 32 bit and add it to current location.</td></tr>" +
  "<tr><td>5?</td><td>Use the last ? hex digit as 0 to 15 number of addresses to adjust.</td></tr>" +
  "<tr><td>7?</td><td>adjust an single location at current set address location.</td></tr>" +
  "<tr><td>6?</td><td>Read a number after this opcode for number of addresses to adjust.</td></tr>" +
  "<tr><td>8?</td><td>Read a number for count, and a number for skip after this opcode. Count is number of addresses to adjust.<br />" +
  "After each adjustment we add skip. Is useful when we wish to adjust addresses evenly spaced apart.</td></tr>" +
  "<tr><td>0?</td><td>Set all current values to noting (Reset).</td></tr>" +
  "</table><br />Each number that is read after an opcode uses an variable in length number encoding called ulib128.<br />" + ulib128 +
  "Each adjust (opcodes 5? to 8?) in 32-bit binaries is plus 4 bytes to the current location, and is plus 8 to the current location in 64-bit binaries.<br /><br />" +
  "Let's read the opcodes and show what locations must be adjusted if the program is offset from its defined precalculated addresses.<br /><br />";

  private static final String binding = "<html>The first hex digit is the opcode and the last hex digit is used as an 0 to 15 value.<br /><br />" +
  "<table border='1'><tr><td>Opcode</td><td>Description</td></tr>" +
  "<tr><td>4?</td><td>Sets the name for the current method. The last ? hex digit is the flag setting, for example 47 set the flag settings to 7.<br />" +
  "Flag setting 8 means the method is week imported.<br />Flag setting 1 means the method is non week imported.<br />" +
  "The end of the name after the opcode is signified by the first value that is 00.</td></tr>" +
  "<tr><td>5?</td><td>Sets the location type. The last ? hex digit is used as 1 to 3 value (pointer = 1, relative = 2, or absolute = 3).<br />" +
  "Pointer means a location that is read and used as the location to the method in the program.<br />" +
  "Relative means a location that is read and added to from the current location in the code to call the method.<br />" +
  "Absolute means a location that must locate directly to the method.</td></tr>" +
  "<tr><td>7?</td><td>Sets the location to the address location of a segment load command data. The last ? hex digit is which segment, for example 72 would mean Seg=2.<br />" +
  "After this opcode is a number that is added to the location in the segment.</td></tr>" +
  "<tr><td>8?</td><td>Read an number after this opcode and add it to the currently set location.</td></tr>" +
  "<tr><td>9?</td><td>Use the current location, and set it to the location of the current set method name in respect to the current binding type.</td></tr>" +
  "<tr><td>A?</td><td>Use the current location, and set it to the location of the current set method name in respect to the current binding type.<br />" +
  "Additionally read a number after this opcode and add it to current location.</td></tr>" +
  "<tr><td>B?</td><td>Use the current location, and set it to the location of the current set method name in respect to the current binding type.<br />" +
  "Additionally use the last ? hex digit and multiply it by 8 for 64 bit binaries, or 4 for 32 bit and add it to current location.</td></tr>" +
  "<tr><td>C?</td><td>Read a number for count, and a number for skip after this opcode. Bind the method to the current location, then add skip to location and repeat till count number of times.<br />" +
  "In some cases we want to bind the same method to different locations evenly spaced apart number of times.</td></tr>" +
  "<tr><td>6?</td><td>Sets the addend to the number read after the opcode.</td></tr>" +
  "<tr><td>1?</td><td>Sets dyld ordinal index. The last ? hex digit is used as 0 to 15 ordinal.</td></tr>" +
  "<tr><td>2?</td><td>Sets dyld ordinal index to the number read after the opcode.</td></tr>" +
  "<tr><td>3?</td><td>Sets where to lookup the method names when binding. The last ? hex digit is the lookup type.<br />" +
  "0 = Default lookup.<br />E = Current binary export list.<br />D = Flat lookup.<br />C = Week lookup.</td></tr>" +
  "<tr><td>0?</td><td>Set all current values to noting (Reset).</td></tr>" +
  "</table><br />Each number that is read after an opcode uses an variable in length number encoding called ulib128.<br />" + ulib128 +
  "After each bind opcodes 9? to C? we add 4 to the location for 32-bit binaries or add 8 to the current location in 64-bit binaries. As that is the size of the address.<br /><br />" +
  "The ordinal tells us which link library as each link library we load in is given a ordinal number starting at 1 and up.<br /><br />" +
  "Let's read the opcodes and show what locations must be set to which methods.<br /><br />";

  private static final String export = "<html>Unlike rebase and binding information which use opcodes to define information, the export section uses names that are broken into parts.<br /><br />" +
  "We start by reading one value for terminal size. If terminal size is set other than 0, then it sets the location of the current built up name. After the terminal node is another value for number of nodes.<br /><br />" +
  "Each node is a small set of text followed by a value that is 00 that represents the end of the text, and then an offset that locates to another section with the same structure.<br /><br />" +
  "This allows us to build the method names in parts and to define the location of a method or data in the smallest space possible.<br /><br />" +
  "Each location uses an variable in length number encoding called ulib128.<br />" + ulib128 +
  "Each location for each child node is an offset within the export section, and the terminal node is an offset in the file for the exact location of the method or data.<br /><br />";
}
