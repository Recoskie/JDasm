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
