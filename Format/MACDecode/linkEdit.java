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

    String out = "<table border='1'><tr><td>Hex</td><td>Description.</td><td>Value</td></tr>";

    //out += "<tr><td></td><td></td><td></td></tr>";

    int Pos = 0, End = (int)(end - pos);

    while( Pos < End )
    {
      if( d[Pos] == 0x40 )
      {
        out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Set Symbol name</td><td>Opcode</td></tr>"; Pos += 1;
    
        String name = "", hex = "";
    
        while( d[Pos] != 0x00 ) { hex += String.format("%1$02X", d[Pos] ) + " "; name += (char)d[Pos]; Pos += 1; }

        hex += String.format("%1$02X", d[Pos] ); Pos += 1;

        out += "<tr><td>" + hex + "</td><td>Symbol name</td><td>" + name + "</td></tr>";
      }
      else if( d[Pos] == 0x00 )
      {
        out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Reset.</td><td>Opcode</td></tr>"; Pos += 1;
      }
      else
      {
        out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Unknown Opcode.</td><td>?</td></tr>"; Pos += 1;
      }
    }

    file.Events = true;

    info("<html>Decoding of the link edit bind information.<br /><br />" + out + "</table></html>");
  }

  //Fully bind and decode the method calls.

  public static bind[] bindSyms( long pos, long end )
  {
    java.util.ArrayList<bind> syms = new java.util.ArrayList<bind>();

    byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); file.read(d); } catch( java.io.IOException er ) {}

    int Pos = 0, End = (int)(end - pos);

    String name = "";
    long loc = 0;
    long offset = 0;
    int opcode = 0, arg = 0, bpos = 0;

    while( Pos < End )
    {
      opcode = d[Pos] & 0xF0; arg = d[Pos] & 0x0F;

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
