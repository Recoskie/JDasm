package Format.MACDecode;

public class linkEdit extends Data
{
  public void rebase( long pos, long end )
  {
    /*byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); Offset.setSelected( pos, end ); file.Events = false; file.read(d); } catch( java.io.IOException er ) {}

    String out = "<table border='1'><tr><td>Hex</td><td>Description.</td><td>Value</td></tr>";

    //out += "<tr><td></td><td></td><td></td></tr>";

    end -= pos; pos = 0;

    while( pos < end )
    {
      
    }

    file.Events = true;

    info("<html>Decoding of the link edit rebase information.<br /><br />" + out + "</table></html>");*/
  }

  public void bind( long pos, long end )
  {
    byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); Offset.setSelected( pos, end ); file.Events = false; file.read(d); } catch( java.io.IOException er ) {}

    String out = "<table border='1'><tr><td>Hex</td><td>Description</td><td>Value</td><td>Current location</td><td>Current name</td><td>Current Flag</td></tr>";

    int Pos = 0, End = d.length;

    String name = "", hex = "";
    long loc = 0, offset = 0;
    int opcode = 0, arg = 0, bpos = 0;

    int bindType = 0;

    try
    {
      while( Pos < End )
      {
        opcode = d[Pos]; arg = opcode & 0x0F; opcode &= 0xF0;

        //The name of the method to look up in the export of another binary.

        if( opcode == 0x40 )
        {
          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Set Symbol name</td><td>Opcode</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td></tr>"; Pos += 1;
          Pos += 1; while( d[Pos] != 0x00 ) { hex += String.format("%1$02X", d[Pos] ) + " "; name += (char)d[Pos]; Pos += 1; }
          out += "<tr><td>" + hex + "</td><td>Symbol name</td><td>" + name + "</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td></tr>"; hex = "";
        }

        //The segment that the method call happens.

        else if( opcode == 0x70 )
        {
          loc = segment.get( arg );
          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Set loc to segment " + arg + "</td><td>Opcode</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td></tr>";
          Pos += 1;

          //The offset within the segment the pointer is at.
          //The lower 7 bits is combined as the number value as long as bit 8 is set.
          //This allows variable in length encoding of a number.

          while( d[Pos] < 0 ) { hex += String.format("%1$02X", d[Pos] ) + " "; offset |= ( d[Pos++] & 0x7F ) << bpos; bpos += 7; } 
          hex += String.format("%1$02X", d[Pos] ) + " "; offset |= d[Pos] << bpos; bpos = 0;

          loc += offset;

          out += "<tr><td>" + hex + "</td><td>Offset in segment " + offset + "</td><td>Opcode</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td></tr>";

          offset = 0; hex = "";
        }

        //Bind the method.

        else if( opcode == 0x90 )
        {
          //After every bind we add the location by the size of the pointer.

          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Bind method to location</td><td>Opcode</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td></tr>";

          loc += is64bit ? 8 : 4; name = "";
        }

        //Set dyld ordinal.

        else if( opcode == 0x10 )
        {
          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Set dyld(" + arg + ")</td><td>Opcode</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td></tr>";
        }

        //Set binding type imm.

        else if( opcode == 0x50 )
        {
          bindType = arg;
          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Set Bind loc type " + bindType + ".</td><td>Opcode</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td></tr>";
        }

        //Reset everything.

        else if( opcode == 0x00 )
        {
          loc = 0; name = "";  
          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Reset.</td><td>Opcode</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td></tr>";
        }
        else
        {
          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Unknown Opcode.</td><td>?</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td></tr>";
        }

        Pos += 1;
      }
    }
    catch( Exception er ) { } //Incase the file is corrupted and we read an bad opcode that goes out of bound of the import data. This way we still load what we can.

    file.Events = true;

    info("<html>Each two hex digits is one opcode. The first hex digit is the opcode and the last hex digit is used as an 0 to 15 value.<br /><br />" +
    "<table border='1'><tr><td>Opcode</td><td>Description</td></tr>" +
    "<tr><td>47</td><td>Sets the name for the current method. The last hex digit is 7 meaning we set the flag settings to 7.</td></tr>" +
    "<tr><td>51</td><td>Sets the location type. The last hex digit is used as 1 to 3 value (pointer = 1, relative = 2, or absolute = 3).<br />" +
    "Pointer means a location that is read and used as the location to the method in the program.<br />" +
    "Relative means an location that is read and added to from the current location in the code to call the method.<br />" +
    "Absolute means an location that must locate directly to the method.</td></tr>" +
    "<tr><td>72</td><td>Sets the location to the victual address location of a segment load command data. The last hex digit is which segment. Following this opcode is the offset that the location is at in the segment.</td></tr>" +
    "<tr><td>90</td><td>Use the current location, and set it to the location of the current set method name. After this opcode we add 4 to the location for 32 bit binaries, or add 8 to the current location in 64 bit binaries.</td></tr>" +
    "<tr><td>19</td><td>Sets the current flag to 9. The last hex digit is used are a 4 bit flag setting.</td></tr>" +
    "<tr><td>00</td><td>Resets the currently location, and name to noting.</td></tr>" +
    "</table>" +
    "<br />Lets read the opcodes and show what locations must be set to which methods.<br /><br />" +
    out + "</table></html>");
  }

  //Fully bind and decode the method calls.

  public static bind[] bindSyms( long pos, long end )
  {
    java.util.ArrayList<bind> syms = new java.util.ArrayList<bind>();

    byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); file.read(d); } catch( java.io.IOException er ) {}

    int Pos = 0, End = d.length;

    String name = "";
    long loc = 0, offset = 0;
    int opcode = 0, arg = 0, bpos = 0;

    try
    {
      while( Pos < End )
      {
        opcode = d[Pos]; arg = opcode & 0x0F; opcode &= 0xF0;

        //The name of the method to look up in the export of another binary.

        if( opcode == 0x40 ) { Pos += 1; while( d[Pos] != 0x00 ) { name += (char)d[Pos]; Pos += 1; } }

        //The segment that the method call happens.

        else if( opcode == 0x70 )
        {
          Pos += 1; loc = segment.get( arg );

          //The offset within the segment the pointer is at.
          //The lower 7 bits is combined as the number value as long as bit 8 is set.
          //This allows variable in length encoding of a number.

          while( d[Pos] < 0 ) { offset |= ( d[Pos++] & 0x7F ) << bpos; bpos += 7; } offset |= d[Pos] << bpos; bpos = 0;

          loc += offset; offset = 0;
        }

        //Bind the method.

        else if( opcode == 0x90 )
        {
          syms.add( new bind( loc, name ) );

          //After every bind we add the location by the size of the pointer.

          loc += is64bit ? 8 : 4; name = "";
        }

        //Reset everything.

        else if( opcode == 0x00 ) { loc = 0; name = ""; }

        Pos += 1;
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
}
