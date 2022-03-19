package Format.MACDecode;

public class linkEdit extends Data
{
  //64 bit locations.

  public void bindInfo( long pos, long end )
  {
    byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); Offset.setSelected( pos, end - 1 ); file.Events = false; file.read(d); } catch( java.io.IOException er ) {}

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
          out += "<tr><td>" + opcodeh + "</td><td>Set dylid(" + arg + ")</td><td>Opcode</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
        }
        else if( opcode == 0x20 )
        {
          out += "<tr><td>" + opcodeh + "</td><td>Set dylid.</td><td>Opcode</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
          out += "<tr><td>" + hex2 + "</td><td>dyld(" + offset + ")</td><td>dyld = " + offset + "</td><td>" + String.format(fmt, loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
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

    file.Events = true; info(out + "</table></html>");
  }

  //Fully bind and decode the method calls.

  public static bind[] bindSyms( long pos, long end )
  {
    java.util.ArrayList<bind> syms = new java.util.ArrayList<bind>();

    byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); Offset.setSelected( pos, end - 1 ); file.read(d); } catch( java.io.IOException er ) {}

    int Pos = 0, End = d.length;

    String name = "";

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

          for( int times = 0; times < count; times++ ) { syms.add( new bind( loc, name ) ); loc += offset + ptr_size; }

          count = 1;
        }
        else if( opcode != 0x20 || opcode != 0x60 ) { loc += offset; }

        offset = 0; Pos += 1;
      }
    }
    catch( Exception er ) { } //Incase the file is corrupted and we read an bad opcode that goes out of bound of the import data. This way we still load what we can.

    return( syms.toArray( new bind[ syms.size() ] ) );
  }

  public void export( long pos, long end )
  {
    /*byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); Offset.setSelected( pos, end ); file.Events = false; file.read(d); } catch( java.io.IOException er ) {}

    String out = "<table border='1'><tr><td>Hex</td><td>Description.</td><td>Value</td></tr>";

    end -= pos; pos = 0;

    while( pos < end )
    {
      
    }

    file.Events = true;

    info("<html>Decoding of the link edit export information.<br /><br />" + out + "</table></html>");*/
  }

  public void rebaseInfo( long pos, long end )
  {
    byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); Offset.setSelected( pos, end - 1 ); file.Events = false; file.read(d); } catch( java.io.IOException er ) {}

    String out = "<html>Decoding of the link edit rebase information.<br /><br />" +
    "<table border='1'><tr><td>Hex</td><td>Description</td><td>Value</td><td>Current location</td><td>Current bind type</td></tr>";

    int Pos = 0, End = d.length;

    String bind_type = "pointer", opcodeh = "", hex1 = "", hex2 = "", s = is64bit ? "8" : "4", fmt = is64bit ? "%1$016X" : "%1$08X";
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

    file.Events = true; info(out + "</table></html>");
  }

  public void rebase( long pos, long end )
  {
    byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); Offset.setSelected( pos, end - 1 ); file.Events = false; file.read(d); } catch( java.io.IOException er ) {}

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

    file.Events = true; info(out + "</table></html>");
  }

  private static final String ulib128 = "Each number that is read uses after an opcode uses an variable in length number encoding called ulib128.<br />" +
  "The first 7 binary digits are the value, and if the last binary digit is set one then we read the next value as the next 7 binary digits for the number.<br />" +
  "The last 7 binary digits for the number should end with a value that is smaller than 80 hex as the last binary digit should be zero.<br /><br />";

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
  "<tr><td>1?</td><td>Sets dylid ordinal index. The last ? hex digit is used as 0 to 15 ordinal.</td></tr>" +
  "<tr><td>2?</td><td>Sets dylid ordinal index to the number read after the opcode.</td></tr>" +
  "<tr><td>3?</td><td>Sets where to lookup the method names when binding. The last ? hex digit is the lookup type.<br />" +
  "0 = Default lookup.<br />E = Current binary export list.<br />D = Flat lookup.<br />C = Week lookup.</td></tr>" +
  "<tr><td>0?</td><td>Set all current values to noting (Reset).</td></tr>" +
  "</table><br />" + ulib128 +
  "After each bind opcodes 9? to C? we add 4 to the location for 32 bit binaries, or add 8 to the current location in 64 bit binaries. As that is the size of the address.<br /><br />" +
  "Lets read the opcodes and show what locations must be set to which methods.<br /><br />";
}
