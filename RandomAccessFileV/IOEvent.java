package RandomAccessFileV;
import java.io.*;
import java.util.*;
import javax.swing.event.*;

//Event constructor.

public class IOEvent extends EventObject
{
  private long Pos = 0;
  private long End = 0;
  private long PosV = 0;
  private long EndV = 0;
  private boolean MapV = false;
  
  public IOEvent( Object source ) { super( source ); }
  
  public IOEvent( Object source, long Pos, long End, long PosV, long EndV, boolean Maped )
  {
    super( source ); this.Pos = Pos; this.End = End; this.PosV = PosV; this.EndV = EndV; this.MapV = Maped;
  }
  
  public long SPos(){ return( Pos ); }
  
  public long EPos(){ return( End ); }

  public long SPosV(){ return( PosV ); }
  
  public long EPosV(){ return( EndV ); }
  
  public long length(){ return( End - Pos ); }

  public long lengthV(){ return( EndV - PosV ); }

  public boolean MapV(){ return( MapedV ); }
}